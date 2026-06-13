package de.skit.grocy.security.token;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import de.skit.grocy.user.UserEntity;

public interface AuthTokenRepository extends JpaRepository<AuthTokenEntity, UUID> {
    Optional<AuthTokenEntity> findByTokenHashAndType(String tokenHash, AuthTokenType type);

    List<AuthTokenEntity> findByUserAndTypeAndUsedAtIsNull(UserEntity user, AuthTokenType type);
}
