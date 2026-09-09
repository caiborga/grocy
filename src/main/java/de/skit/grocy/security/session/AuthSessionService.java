package de.skit.grocy.security.session;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.skit.grocy.security.crypto.TokenHashes;
import de.skit.grocy.user.UserEntity;

@Service
public class AuthSessionService {

    private final AuthSessionRepository sessionRepository;

    public AuthSessionService(AuthSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public void create(UserEntity user, String jti, Instant expiresAt) {
        AuthSessionEntity session = new AuthSessionEntity();
        session.setUser(user);
        session.setJtiHash(TokenHashes.sha256Hex(jti));
        session.setExpiresAt(OffsetDateTime.ofInstant(expiresAt, ZoneOffset.UTC));
        session.setLastSeenAt(OffsetDateTime.now());
        sessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public boolean isActive(String jti, UUID userId) {
        if (jti == null || jti.isBlank() || userId == null) {
            return false;
        }
        return sessionRepository.findByJtiHash(TokenHashes.sha256Hex(jti))
                .filter(AuthSessionEntity::isActive)
                .filter(session -> userId.equals(session.getUser().getId()))
                .isPresent();
    }

    @Transactional
    public void revokeByJti(String jti) {
        if (jti == null || jti.isBlank()) {
            return;
        }
        sessionRepository.findByJtiHash(TokenHashes.sha256Hex(jti)).ifPresent(session -> {
            if (session.getRevokedAt() == null) {
                session.setRevokedAt(OffsetDateTime.now());
            }
        });
    }

    @Transactional
    public void revokeAll(UserEntity user) {
        sessionRepository.revokeAllForUser(user, OffsetDateTime.now());
    }

    @Transactional
    public void deleteAll(UserEntity user) {
        sessionRepository.deleteByUser(user);
    }
}
