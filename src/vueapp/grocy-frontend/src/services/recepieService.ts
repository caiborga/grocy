import { Recipe, RecipeCreate } from "@/models/Recipe";
import api from "@/services/api";

type RecipeAddToList = {
	listId: string;
	servings: number;
};

export const recipeService = {
	getAll(householdId: string) {
		return api.get<Recipe[]>(`/households/${householdId}/recipes`);
	},

	getById(householdId: string, recipeId: string) {
		return api.get<Recipe>(
			`/households/${householdId}/recipes/${recipeId}`
		);
	},

	createRecipe(householdId: string, body: RecipeCreate) {
		return api.post<Recipe>(`/households/${householdId}/recipes`, body);
	},

	updateRecipe(householdId: string, recipeId: string, body: RecipeCreate) {
		return api.put<Recipe>(
			`/households/${householdId}/recipes/${recipeId}`,
			body
		);
	},

	deleteRecipe(householdId: string, recipeId: string) {
		return api.delete(
			`/households/${householdId}/recipes/${recipeId}`
		);
	},

	addToList(
		householdId: string,
		recipeId: string,
		body: RecipeAddToList
	) {
		return api.post<{ added: number }>(
			`/households/${householdId}/recipes/${recipeId}/add-to-list`,
			body
		);
	}
};
