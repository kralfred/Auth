package org.example.reservation_api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.reservation_api.services.DpopService;
import org.example.reservation_api.services.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class DpopAuthenticationFilter extends OncePerRequestFilter {

    private final DpopService dpopService;
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // No bearer token → nothing to bind. Let the rest of the chain decide.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        // If JwtAuthenticationFilter didn't authenticate the request, the token was
        // invalid and downstream authorization will reject it. Skip DPoP so we don't
        // mask the real reason with a misleading error.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            chain.doFilter(request, response);
            return;
        }

        String accessToken = authHeader.substring(7);

        String tokenJkt;
        try {
            tokenJkt = jwtService.extractDpopJkt(accessToken);
        } catch (Exception e) {
            log.warn("Unable to read DPoP binding from token: {}", e.getMessage());
            reject(response, "Invalid access token");
            return;
        }

        // Token isn't DPoP-bound → allow. This covers tokens issued at login
        // without a DPoP header (legacy / non-DPoP clients).
        if (tokenJkt == null || tokenJkt.isBlank()) {
            chain.doFilter(request, response);
            return;
        }

        // Token IS DPoP-bound → a proof is mandatory.
        String dpopHeader = request.getHeader("DPoP");
        if (dpopHeader == null || dpopHeader.isBlank()) {
            reject(response, "DPoP proof required for this token");
            return;
        }

        try {
            DpopService.Proof proof = dpopService.verifyProof(
                    dpopHeader,
                    request.getMethod(),
                    buildExpectedUri(request),
                    accessToken
            );

            if (!tokenJkt.equals(proof.jkt())) {
                log.warn("DPoP jkt mismatch: token={} proof={}", tokenJkt, proof.jkt());
                reject(response, "DPoP proof does not match access token");
                return;
            }

            chain.doFilter(request, response);
        } catch (BadCredentialsException e) {
            log.debug("DPoP validation failed: {}", e.getMessage());
            reject(response, e.getMessage());
        }
    }

    /**
     * Build the expected 'htu'. Prefer the configured external base URL when set,
     * otherwise fall back to the request's own URL. Handles reverse proxies by
     * respecting X-Forwarded-Proto / X-Forwarded-Host.
     */
    private String buildExpectedUri(HttpServletRequest request) {
        String scheme = firstNonBlank(
                request.getHeader("X-Forwarded-Proto"),
                request.getScheme());
        String host = firstNonBlank(
                request.getHeader("X-Forwarded-Host"),
                request.getHeader("Host"),
                request.getServerName());
        return scheme + "://" + host + request.getRequestURI();
    }

    private String firstNonBlank(String... values) {
        for (String v : values) if (v != null && !v.isBlank()) return v;
        return "";
    }

    private void reject(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + msg + "\"}");
    }
}