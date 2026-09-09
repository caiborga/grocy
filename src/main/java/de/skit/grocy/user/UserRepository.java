package de.skit.grocy.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByDeletedAtIsNull();

    long countByDeletedAtIsNullAndCreatedAtAfter(OffsetDateTime createdAfter);

    long countByDeletedAtIsNullAndLastLoginAtAfter(OffsetDateTime lastLoginAfter);
}
