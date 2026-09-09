package de.skit.grocy.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import de.skit.grocy.user.UserEntity;
import jakarta.annotation.PostConstruct;

@Service
public class JwtService {
    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms:604800000}")
    private long expirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @PostConstruct
    void validateSecret() {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("app.jwt.secret must be at least 32 bytes");
        }
    }

    public IssuedJwt generateToken(UserDetails user) {
        String jti = UUID.randomUUID().toString();
        Date issuedAt = new Date();
        Date expiresAt = new Date(System.currentTimeMillis() + expirationMs);
        String token = Jwts.builder()
                .setId(jti)
                .setSubject(user.getUsername())
                .setIssuedAt(issuedAt)
                .setExpiration(expiresAt)
                .signWith(getSigningKey())
                .compact();
        return new IssuedJwt(token, jti, expiresAt.toInstant());
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public String extractJti(String token) {
        return getClaims(token).getId();
    }

    public Instant extractIssuedAt(String token) {
        Date issuedAt = getClaims(token).getIssuedAt();
        return issuedAt == null ? null : issuedAt.toInstant();
    }

    public boolean isTokenValid(String token, UserDetails user) {
        if (!extractUsername(token).equals(user.getUsername()) || isTokenExpired(token)) {
            return false;
        }
        if (user instanceof UserPrincipal principal) {
            UserEntity entity = principal.getUser();
            if (entity.getDeletedAt() != null) {
                return false;
            }
            OffsetDateTime validFrom = entity.getCredentialsValidFrom();
            Instant issuedAt = extractIssuedAt(token);
            if (validFrom != null && (issuedAt == null || issuedAt.isBefore(validFrom.toInstant()))) {
                return false;
            }
        }
        return true;
    }

    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public record IssuedJwt(String token, String jti, Instant expiresAt) {}
}
