package de.skit.grocy.user.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String displayName,
        String email,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        UUID activeHouseholdId
) {}
