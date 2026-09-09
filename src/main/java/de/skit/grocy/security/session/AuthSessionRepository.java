package de.skit.grocy.security.session;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import de.skit.grocy.user.UserEntity;

public interface AuthSessionRepository extends JpaRepository<AuthSessionEntity, UUID> {

    Optional<AuthSessionEntity> findByJtiHash(String jtiHash);

    List<AuthSessionEntity> findByUserAndRevokedAtIsNull(UserEntity user);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update AuthSessionEntity s
               set s.revokedAt = :now
             where s.user = :user
               and s.revokedAt is null
            """)
    int revokeAllForUser(@Param("user") UserEntity user, @Param("now") OffsetDateTime now);

    @Modifying
    @Query("delete from AuthSessionEntity s where s.user = :user")
    int deleteByUser(@Param("user") UserEntity user);

    @Modifying
    @Query("delete from AuthSessionEntity s where s.expiresAt < :cutoff")
    int deleteExpired(@Param("cutoff") OffsetDateTime cutoff);
}
