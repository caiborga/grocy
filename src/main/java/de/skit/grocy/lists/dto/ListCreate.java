package de.skit.grocy.lists.dto;

import jakarta.validation.constraints.NotBlank;

public record ListCreate(
    @NotBlank String title,
    @NotBlank String householdId,
    boolean isDefault) {
}
