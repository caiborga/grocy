package de.skit.grocy.items.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ListItem(
    UUID id,
    UUID listId,
    UUID householdId,
    UUID createdBy,
    String title,
    String notes,
    Double quantity,
    String unitText,
    UUID categoryId,
    boolean checked,
    int sortIndex,
    int version,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
