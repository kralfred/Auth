package org.example.reservation_api.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
        System.out.println("--> FILTER INTERCEPTED: " + request.getMethod() + " " + request.getRequestURI());
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.err.println("Header null: ");
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            Claims claims = jwtService.extractAllClaims(jwt);
            String username = claims.getSubject();

            String nestedGroupIdStr = claims.get("env_id", String.class);
            if (nestedGroupIdStr != null) {
                CurrentEnvironmentContext.set(UUID.fromString(nestedGroupIdStr));
            }

            String userIdStr = claims.get("userId", String.class);
            UUID userId = userIdStr != null ? UUID.fromString(userIdStr) : null;
            System.err.println("Accepted: ");
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                System.err.println("First working: ");

                var validationResult = jwtService.validateToken(jwt);
                System.err.println("Token Valid: " + validationResult.isValid());
                System.err.println("Validation Details: " + validationResult);

                if (validationResult.isValid()) {
                    System.err.println("Working: ");

                    // 1. Wrap in ArrayList to avoid UnsupportedOperationException on unmodifiable lists
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>(jwtService.getAuthorities(jwt));
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

                    if (userId != null) {
                        // 2. Set UUID as principal so SecurityUtils can extract it without throwing exceptions
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                authorities
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        System.err.println("Finished: ");
                    } else {
                        System.err.println("WARNING: userId claim was null in JWT!");
                    }
                }
            }
        } catch (Exception e) {
            // Print the exact error stack trace so you know if signature/claims failed
            System.err.println("JWT AUTHENTICATION ERROR: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                filterChain.doFilter(request, response);
            } finally {
                CurrentEnvironmentContext.clear();
            }
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs");
    }
}