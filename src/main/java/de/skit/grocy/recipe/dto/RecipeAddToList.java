package de.skit.grocy.recipe.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RecipeAddToList(
        @NotNull UUID listId,
        @Positive int servings) {
}
