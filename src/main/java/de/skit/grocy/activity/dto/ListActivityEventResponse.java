package de.skit.grocy.activity.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import de.skit.grocy.activity.ListActivityType;

public record ListActivityEventResponse(
        UUID id,
        UUID householdId,
        UUID listId,
        String listTitle,
        UUID actorId,
        String actorName,
        ListActivityType type,
        UUID itemId,
        String itemTitle,
        Integer metaCount,
        String metaText,
        OffsetDateTime createdAt) {
}
