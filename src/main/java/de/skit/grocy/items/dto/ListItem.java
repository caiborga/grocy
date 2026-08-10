package de.skit.grocy.items.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ListItem(
        UUID id,
        UUID listId,
        UUID householdId,
        String title,
        Double quantity,
        String unitText,
        String brand,
        UUID categoryId,
        boolean checked,
        String notes,
        int sortIndex,
        int version,
        UUID createdBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        UUID sourceRecipeId,
        String sourceRecipeTitle
) {}
