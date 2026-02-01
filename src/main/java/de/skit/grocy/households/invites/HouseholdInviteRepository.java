package de.skit.grocy.households.invites;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import de.skit.grocy.common.enums.Role;
import de.skit.grocy.households.invites.enums.InviteStatus;

import jakarta.persistence.LockModeType;

public interface HouseholdInviteRepository extends JpaRepository<HouseholdInviteEntity, UUID> {

    Optional<HouseholdInviteEntity> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from HouseholdInviteEntity i where i.tokenHash = :tokenHash")
    Optional<HouseholdInviteEntity> findForUpdateByTokenHash(@Param("tokenHash") String tokenHash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                update HouseholdInviteEntity i
                   set i.status = :revokedStatus
                 where i.householdId = :householdId
                   and i.role = :role
                   and i.status = :pendingStatus
                   and i.expiresAt > :now
                   and i.uses < i.maxUses
            """)
    int revokeActiveInvitesForRole(
            @Param("householdId") UUID householdId,
            @Param("role") Role role,
            @Param("pendingStatus") InviteStatus pendingStatus,
            @Param("revokedStatus") InviteStatus revokedStatus,
            @Param("now") OffsetDateTime now);
}
