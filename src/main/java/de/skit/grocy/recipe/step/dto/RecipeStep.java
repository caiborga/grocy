package de.skit.grocy.recipe.step.dto;

import java.util.UUID;

public record RecipeStep(
    UUID id,
    String instruction,
    int sortIndex
) {}