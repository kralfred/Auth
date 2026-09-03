package org.example.reservation_api.services;

import io.jsonwebtoken.*;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.reservation_api.DTO.TokenValidationResult;
import org.example.reservation_api.entities.RefreshToken;
import org.example.reservation_api.entities.Session;
import org.example.reservation_api.entities.User;
import org.example.reservation_api.repositories.PermissionRepository;
import org.example.reservation_api.repositories.TokenRepository;
import org.example.reservation_api.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.crypto.SecretKey;
import java.security.Key;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static javax.crypto.Cipher.SECRET_KEY;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final TokenRepository tokenRepository;
    private final PermissionRepository permissionRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${JWT_SECRET}")
    private String secretKey;
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;


    public String generateAccessToken(String username,UUID userId, UUID nestedGroupId, List<String> permissions, String dpop_jkt) {
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId.toString())
                .claim("env_id", nestedGroupId.toString())  // Read by JwtAuthenticationFilter
                .claim("permissions", permissions)          // Used for authority mapping
                .claim("dpop_jkt", dpop_jkt)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    public RefreshToken generateRefreshToken(Session session, long expirationInMinutes){
        String rawRefreshToken = generateOpaqueRefreshToken();


        String hashedToken = SecurityUtils.hashToken(rawRefreshToken);

        return new RefreshToken(
                UUID.randomUUID(),
                session.id(),
                hashedToken,
                Instant.now().plus(7, ChronoUnit.DAYS),
                false,
                Instant.now()
        );
    }

    public String generateOpaqueRefreshToken() {
        byte[] randomBytes = new byte[48];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }


    public TokenValidationResult validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            UUID tokenId = UUID.fromString(claims.getId());
            UUID userId = UUID.fromString(claims.get("userId", String.class));

            return new TokenValidationResult(tokenId, userId, claims, TokenValidationResult.ValidationStatus.VALID);

        } catch (ExpiredJwtException e) {
            return new TokenValidationResult(null, null, null, TokenValidationResult.ValidationStatus.EXPIRED);
        } catch (Exception e) {
            return new TokenValidationResult(null, null, null, TokenValidationResult.ValidationStatus.INVALID);
        }
    }


    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public List<SimpleGrantedAuthority> getAuthorities(String token) {
        Claims claims = extractAllClaims(token);

        List<String> permissions = claims.get("permissions", List.class);

        String role = claims.get("role", String.class);
        List<SimpleGrantedAuthority> authList = new ArrayList<>();

        if (permissions != null) {
            permissions.forEach(p -> authList.add(new SimpleGrantedAuthority(p)));
        }

        if (role != null) {
            authList.add(new SimpleGrantedAuthority(role));
        }

        return authList;
    }

}
