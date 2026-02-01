package de.skit.grocy.security.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import de.skit.grocy.common.enums.Role;

public record MeResponse(
        UUID id,
        String name,
        String email,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        UUID activeHouseholdId,
        Role role
) {}