package de.skit.grocy.items.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record ItemCreate(
        @NotBlank String title,
        Double quantity,
        String unitText,
        String brand,
        UUID categoryId,
        String notes) {
}
