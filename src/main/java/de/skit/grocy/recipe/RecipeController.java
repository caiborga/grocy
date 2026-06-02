package de.skit.grocy.recipe;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import de.skit.grocy.recipe.dto.RecipeAddToList;
import de.skit.grocy.recipe.dto.Recipe;
import de.skit.grocy.recipe.dto.RecipeCreate;
import de.skit.grocy.security.UserPrincipal;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/households/{householdId}/recipes")
public class RecipeController {
    private final RecipeService service;

    public RecipeController(RecipeService service) {
        this.service = service;
    }

    @GetMapping
    public List<Recipe> getRecipes(@PathVariable UUID householdId,
            @RequestParam(required = false) String sort,
            @AuthenticationPrincipal UserPrincipal principal) {

        return service.getRecipes(householdId, sort, principal);
    }

    @PostMapping
    public Recipe createRecipe(@PathVariable UUID householdId, @Valid @RequestBody RecipeCreate body,
            @AuthenticationPrincipal UserPrincipal principal) {

        return service.createRecipe(householdId, body, principal);
    }

    @GetMapping("/{recipeId}")
    public Recipe getRecipe(@PathVariable UUID householdId, @PathVariable UUID recipeId,
            @AuthenticationPrincipal UserPrincipal principal) {

        return service.getRecipe(householdId, recipeId, principal);
    }

    @PutMapping("/{recipeId}")
    public Recipe updateRecipe(@PathVariable UUID householdId, @PathVariable UUID recipeId,
            @Valid @RequestBody RecipeCreate body,
            @AuthenticationPrincipal UserPrincipal principal) {

        return service.updateRecipe(householdId, recipeId, body, principal);
    }

    @DeleteMapping("/{recipeId}")
    public void deleteRecipe(@PathVariable UUID householdId, @PathVariable UUID recipeId,
            @AuthenticationPrincipal UserPrincipal principal) {

        service.deleteRecipe(householdId, recipeId, principal);
    }

    @PostMapping("/{recipeId}/add-to-list")
    public Map<String, Integer> addToList(@PathVariable UUID householdId, @PathVariable UUID recipeId,
            @Valid @RequestBody RecipeAddToList body,
            @AuthenticationPrincipal UserPrincipal principal) {

        int added = service.addToList(householdId, recipeId, body, principal);
        return Map.of("added", added);
    }

}
