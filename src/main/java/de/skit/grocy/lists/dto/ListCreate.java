package de.skit.grocy.lists.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public record ListCreate(
    @NotBlank String title,
    @NotBlank String householdId,
    @NotBlank UUID createdBy) {
}
