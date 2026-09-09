package de.skit.grocy.households.invites.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import de.skit.grocy.common.enums.Role;
import de.skit.grocy.households.invites.enums.InviteStatus;

public record HouseholdInviteListResponse(
        UUID id,
        Role role,
        InviteStatus status,
        OffsetDateTime expiresAt,
        int uses,
        int maxUses,
        OffsetDateTime createdAt) {
}
