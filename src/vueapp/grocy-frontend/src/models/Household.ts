import { List } from "./List";
import { User } from "./User";

export interface Household {
	id: string;
	name: string;
	title?: string;
	createdAt: Date;
	updatedAt: Date;
	archived: boolean;
	listCount?: number;
	recipeCount?: number;
	members?: User[];
	lists?: List[];
}
