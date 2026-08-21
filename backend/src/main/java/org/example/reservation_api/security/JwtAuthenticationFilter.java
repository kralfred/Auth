package org.example.reservation_api.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.reservation_api.entities.RefreshToken;
import org.example.reservation_api.repositories.TokenRepository;
import org.example.reservation_api.services.JwtService;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenRepository tokenRepository;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // 1. Pass-through if no Bearer token is provided
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            // 2. Extract JWT claims
            Claims claims = jwtService.extractAllClaims(jwt);
            String username = claims.getSubject();

            // Extract custom nested group ID claim from JWT
            String nestedGroupIdStr = claims.get("env_id", String.class);

            // 3. Set ThreadLocal context for the active request thread
            if (nestedGroupIdStr != null) {
                CurrentEnvironmentContext.set(UUID.fromString(nestedGroupIdStr));
            }

            // 4. Authenticate in Spring Security Context
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                if (jwtService.validateToken(jwt).isValid()) { // Validate signature & expiration
                    List<SimpleGrantedAuthority> authorities = jwtService.getAuthorities(jwt);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            authorities
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            // 5. Pass down the filter chain
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            logger.error("Could not set user authentication", e);
            filterChain.doFilter(request, response);
        } finally {
            // 6. CRUCIAL: Always clear ThreadLocal when request finishes
            CurrentEnvironmentContext.clear();
        }
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs");
    }
}
