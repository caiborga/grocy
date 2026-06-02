package de.skit.grocy.recipe.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import de.skit.grocy.recipe.ingredient.dto.RecipeIngredient;
import de.skit.grocy.recipe.step.dto.RecipeStep;

public record Recipe(
    UUID id,
    UUID householdId,
    UUID createdBy,
    String title,
    String description,
    int baseServings,
    int version,
    Integer prepTimeMinutes,
    List<RecipeIngredient> ingredients,
    List<RecipeStep> steps,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {
}
