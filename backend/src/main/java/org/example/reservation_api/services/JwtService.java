package org.example.reservation_api.services;

import io.jsonwebtoken.*;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.example.reservation_api.DTO.PermissionInfo;
import org.example.reservation_api.entities.Token;
import org.example.reservation_api.entities.User;
import org.example.reservation_api.repositories.PermissionRepository;
import org.example.reservation_api.repositories.TokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static javax.crypto.Cipher.SECRET_KEY;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final TokenRepository tokenRepository;
    private final PermissionRepository permissionRepository;

    @Value("${JWT_SECRET}")
    private String secretKey;

    public String generateTimedToken(User user, long timeInMin) {

        List<PermissionInfo> perms = permissionRepository.findAllCategorizedPermissions(user.getId());
        Map<String, List<String>> categorizedClaims = perms.stream()
                .collect(Collectors.groupingBy(
                        PermissionInfo::category,
                        Collectors.mapping(PermissionInfo::action, Collectors.toList())
                ));

        Date now = new Date();
        Date expiry = new Date(now.getTime() + 1000L * 60 * timeInMin);
        Token tokenEntity = new Token(user.getId(), expiry, "ACTIVE");
        tokenEntity = tokenRepository.save(tokenEntity);
        return Jwts.builder()
                .header().add("typ", "JWT").and()
                .id(tokenEntity.getId().toString())
                .subject(user.getUsername())
                .claim("perms", categorizedClaims)
                .claim("userId", user.getId().toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSignInKey())
                .compact();
    }

    public Token isTokenValid(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            UUID databaseTokenId = UUID.fromString(claims.getId());
            UUID ownerId = UUID.fromString(claims.get("userId", String.class));

            Token returnVal = new Token(ownerId, claims.getExpiration(), "CORRECT");
            returnVal.setId(databaseTokenId);

            return returnVal;

        } catch (ExpiredJwtException e) {
            return new Token(null, null, "Expired");
        } catch (Exception e) {
            return new Token(null, null, "Invalid");
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Generic helper to extract a single claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
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

        List<String> permissions = claims.get("authorities", List.class);

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

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64URL.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
