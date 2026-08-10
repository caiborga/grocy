export interface Item {
	title: string;
	checked?: boolean;
	quantity?: number | null;
	unitText?: string | null;
	brand?: string | null;
	sourceRecipeId?: string;
	sourceRecipeTitle?: string;
}
