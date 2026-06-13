package de.skit.grocy.security.token;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.skit.grocy.user.UserEntity;

@Service
public class AuthTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final AuthTokenRepository tokenRepository;

    public AuthTokenService(AuthTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public String createToken(UserEntity user, AuthTokenType type, Duration ttl) {
        tokenRepository.findByUserAndTypeAndUsedAtIsNull(user, type)
                .forEach(token -> token.setUsedAt(OffsetDateTime.now()));

        String rawToken = generateRawToken();

        AuthTokenEntity entity = new AuthTokenEntity();
        entity.setUser(user);
        entity.setType(type);
        entity.setTokenHash(hash(rawToken));
        entity.setExpiresAt(OffsetDateTime.now().plus(ttl));

        tokenRepository.save(entity);
        return rawToken;
    }

    @Transactional
    public UserEntity consumeToken(String rawToken, AuthTokenType type) {
        AuthTokenEntity entity = tokenRepository
                .findByTokenHashAndType(hash(rawToken), type)
                .orElseThrow(() -> new IllegalArgumentException("Token ist ungültig oder abgelaufen"));

        if (entity.getUsedAt() != null || entity.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Token ist ungültig oder abgelaufen");
        }

        entity.setUsedAt(OffsetDateTime.now());
        return entity.getUser();
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
