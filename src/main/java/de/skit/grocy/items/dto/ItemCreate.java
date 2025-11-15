package de.skit.grocy.items.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record ItemCreate(
    @NotBlank String title,
    String notes,
    Double quantity,
    String unitText,
    UUID categoryId,
    UUID clientItemId // optionaler Idempotenz-Key
) {}
