package de.skit.grocy.households.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record HouseholdResponse(
        UUID id,
        String name,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        Boolean archived) {
}