package de.skit.grocy.recipe.ingredient.dto;


import jakarta.validation.constraints.NotBlank;

public record RecipeIngredientCreate(
    @NotBlank String name,
    Double quantity,
    String unitText,
    int sortIndex
) {}
