package de.skit.grocy.recipe;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import de.skit.grocy.activity.ListActivityService;
import de.skit.grocy.activity.ListActivityType;
import de.skit.grocy.common.exceptions.EntityNotFoundException;
import de.skit.grocy.common.exceptions.NotFoundException;
import de.skit.grocy.households.HouseholdEntity;
import de.skit.grocy.households.HouseholdRepository;
import de.skit.grocy.items.ItemEntity;
import de.skit.grocy.items.ItemRepository;
import de.skit.grocy.lists.ListEntity;
import de.skit.grocy.lists.ListRepository;
import de.skit.grocy.recipe.dto.RecipeAddToList;
import de.skit.grocy.recipe.dto.Recipe;
import de.skit.grocy.recipe.dto.RecipeCreate;
import de.skit.grocy.recipe.ingredient.IngredientEntity;
import de.skit.grocy.recipe.ingredient.dto.RecipeIngredient;
import de.skit.grocy.recipe.step.StepEntity;
import de.skit.grocy.recipe.step.dto.RecipeStep;
import de.skit.grocy.security.UserPrincipal;
import de.skit.grocy.user.UserEntity;
import jakarta.transaction.Transactional;

@Service
public class RecipeService {

    private final HouseholdRepository householdRepository;
    private final RecipeRepository recipeRepository;
    private final ListRepository listRepository;
    private final ItemRepository itemRepository;
    private final ListActivityService listActivityService;

    public RecipeService(HouseholdRepository householdRepository,
            RecipeRepository recipeRepository,
            ListRepository listRepository,
            ItemRepository itemRepository,
            ListActivityService listActivityService) {
        this.householdRepository = householdRepository;
        this.recipeRepository = recipeRepository;
        this.listRepository = listRepository;
        this.itemRepository = itemRepository;
        this.listActivityService = listActivityService;
    }

    @Transactional
    public Recipe createRecipe(UUID householdId, RecipeCreate dto, UserPrincipal principal) {

        UserEntity user = principal.getUser();
        HouseholdEntity household = householdRepository.findById(householdId)
                .orElseThrow(() -> new NotFoundException("Household not found"));

        RecipeEntity recipe = new RecipeEntity();

        List<IngredientEntity> ingredients = dto.ingredients().stream().map(i -> {
            IngredientEntity ingredient = new IngredientEntity();
            ingredient.setRecipe(recipe);
            ingredient.setName(i.name());
            ingredient.setQuantity(i.quantity());
            ingredient.setUnitText(i.unitText());
            ingredient.setSortIndex(i.sortIndex());
            return ingredient;
        }).toList();

        List<StepEntity> steps = dto.steps().stream().map(i -> {
            StepEntity step = new StepEntity();
            step.setRecipe(recipe);
            step.setInstruction(i.instruction());
            step.setSortIndex(i.sortIndex());
            return step;
        }).toList();

        recipe.setHousehold(household);
        recipe.setCreatedBy(user);
        recipe.setTitle(dto.title());
        recipe.setDescription(dto.description());
        recipe.setBaseServings(dto.baseServings());
        recipe.setPrepTimeMinutes(dto.prepTimeMinutes());
        recipe.setIngredients(ingredients);
        recipe.setSteps(steps);
        recipeRepository.save(recipe);

        return toDto(recipe);
    }

    public List<Recipe> getRecipes(UUID householdId, String sort, UserPrincipal principal) {

        householdRepository.findById(householdId)
                .orElseThrow(() -> new EntityNotFoundException("Household not found"));

        List<RecipeEntity> recipes = recipeRepository.findByHouseholdId(householdId);
        return recipes.stream().map(this::toDto).toList();

    }

    public Recipe getRecipe(UUID householdId, UUID recipeId, UserPrincipal principal) {
        householdRepository.findById(householdId)
                .orElseThrow(() -> new EntityNotFoundException("Household not found"));

        RecipeEntity recipe = recipeRepository.findByIdAndHouseholdId(recipeId, householdId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

        return toDto(recipe);
    }

    @Transactional
    public Recipe updateRecipe(UUID householdId, UUID recipeId, RecipeCreate dto,
            UserPrincipal principal) {

        householdRepository.findById(householdId)
                .orElseThrow(() -> new EntityNotFoundException("Household not found"));

        RecipeEntity recipe = recipeRepository.findByIdAndHouseholdId(recipeId, householdId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

        recipe.setTitle(dto.title());
        recipe.setDescription(dto.description());
        recipe.setBaseServings(dto.baseServings());
        recipe.setPrepTimeMinutes(dto.prepTimeMinutes());

        recipe.getIngredients().clear();
        dto.ingredients().forEach(i -> {
            IngredientEntity ingredient = new IngredientEntity();
            ingredient.setRecipe(recipe);
            ingredient.setName(i.name());
            ingredient.setQuantity(i.quantity());
            ingredient.setUnitText(i.unitText());
            ingredient.setSortIndex(i.sortIndex());
            recipe.getIngredients().add(ingredient);
        });

        recipe.getSteps().clear();
        dto.steps().forEach(s -> {
            StepEntity step = new StepEntity();
            step.setRecipe(recipe);
            step.setInstruction(s.instruction());
            step.setSortIndex(s.sortIndex());
            recipe.getSteps().add(step);
        });

        return toDto(recipe);
    }

    @Transactional
    public void deleteRecipe(UUID householdId, UUID recipeId, UserPrincipal principal) {
        RecipeEntity recipe = recipeRepository.findByIdAndHouseholdId(recipeId, householdId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

        recipeRepository.delete(recipe);
    }

    @Transactional
    public int addToList(UUID householdId, UUID recipeId, RecipeAddToList dto,
            UserPrincipal principal) {

        RecipeEntity recipe = recipeRepository.findByIdAndHouseholdId(recipeId, householdId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

        ListEntity list = listRepository.findById(dto.listId())
                .orElseThrow(() -> new EntityNotFoundException("List not found"));

        if (!list.getHousehold().getId().equals(householdId)) {
            throw new EntityNotFoundException("List not found");
        }

        double factor = (double) dto.servings() / recipe.getBaseServings();

        List<ItemEntity> items = recipe.getIngredients().stream()
                .map(ingredient -> {
                    ItemEntity item = new ItemEntity();
                    item.setList(list);
                    item.setHousehold(list.getHousehold());
                    item.setCreatedBy(principal.getUser());
                    item.setTitle(ingredient.getName());
                    item.setQuantity(scaleQuantity(ingredient.getQuantity(), factor));
                    item.setUnitText(ingredient.getUnitText());
                    item.setSourceRecipeId(recipe.getId());
                    item.setSourceRecipeTitle(recipe.getTitle());
                    item.setSortIndex(ingredient.getSortIndex());
                    return item;
                })
                .toList();

        itemRepository.saveAll(items);

        if (!items.isEmpty()) {
            listActivityService.record(
                    list,
                    principal.getUser(),
                    ListActivityType.RECIPE_ADDED_TO_LIST,
                    null,
                    null,
                    items.size(),
                    recipe.getTitle());
        }

        return items.size();
    }

    private Double scaleQuantity(Double quantity, double factor) {
        if (quantity == null) {
            return null;
        }

        return Math.round(quantity * factor * 100.0) / 100.0;
    }

    private Recipe toDto(RecipeEntity entity) {
        return new Recipe(entity.getId(), entity.getHousehold().getId(),
                entity.getCreatedBy().getId(), entity.getTitle(), entity.getDescription(),
                entity.getBaseServings(), entity.getVersion(), entity.getPrepTimeMinutes(),
                entity.getIngredients().stream()
                        .map(i -> new RecipeIngredient(i.getId(), i.getName(), i.getQuantity(),
                                i.getUnitText(), i.getSortIndex()))
                        .toList(),
                entity.getSteps().stream()
                        .map(s -> new RecipeStep(s.getId(), s.getInstruction(), s.getSortIndex()))
                        .toList(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }

}
