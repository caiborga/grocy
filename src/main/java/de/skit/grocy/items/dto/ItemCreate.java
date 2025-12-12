package de.skit.grocy.items.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record ItemCreate(
        @NotBlank String title,
        @NotBlank UUID householdId,
        @NotBlank UUID userId,
        Double quantity,
        String unitText,
        UUID categoryId,
        String notes,
        UUID clientItemId
) {}
