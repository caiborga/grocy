package de.skit.grocy.recipe.step.dto;

import jakarta.validation.constraints.NotBlank;

public record RecipeStepCreate(
    @NotBlank String instruction,
    int sortIndex) {
}
