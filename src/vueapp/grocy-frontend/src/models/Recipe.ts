export type RecipeIngredient = {
	id?: string;
	name: string;
	quantity: number | null;
	unitText: string | null;
	sortIndex: number;
};

export type RecipeStep = {
	id?: string;
	instruction: string;
	sortIndex: number;
};

export type Recipe = {
	id: string;
	householdId: string;
	title: string;
	description?: string;
	baseServings: number;
	prepTimeMinutes?: number;
	ingredients: RecipeIngredient[];
	steps: RecipeStep[];
};

export type RecipeCreate = {
	title: string;
	description?: string;
	baseServings: number;
	prepTimeMinutes?: number;
	ingredients: RecipeIngredient[];
	steps: RecipeStep[];
};
