package de.skit.grocy.households.member.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record HouseholdMemberResponse(
        UUID id,
        UUID householdId,
        UUID userId,
        String role,
        OffsetDateTime joinedAt) {
}