package de.skit.grocy.households.member.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import de.skit.grocy.common.enums.Role;

public record HouseholdMemberResponse(
        UUID id,
        UUID householdId,
        UUID userId,
        Role role,
        OffsetDateTime joinedAt,
        String displayName) {
}