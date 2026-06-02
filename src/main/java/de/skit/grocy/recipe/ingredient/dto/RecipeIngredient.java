package de.skit.grocy.recipe.ingredient.dto;

import java.util.UUID;

public record RecipeIngredient(
    UUID id,
    String name,
    Double quantity,
    String unitText,
    int sortIndex
) {}