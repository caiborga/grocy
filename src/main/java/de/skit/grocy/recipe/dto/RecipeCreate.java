package de.skit.grocy.recipe.dto;

import java.util.List;

import de.skit.grocy.recipe.ingredient.dto.RecipeIngredientCreate;
import de.skit.grocy.recipe.step.dto.RecipeStepCreate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record RecipeCreate(

        @NotBlank String title,

        String description,

        @Positive int baseServings,

        Integer prepTimeMinutes,

        List<RecipeIngredientCreate> ingredients,

        List<RecipeStepCreate> steps

) {
}
