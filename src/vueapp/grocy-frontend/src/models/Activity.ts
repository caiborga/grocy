export type ListActivityType =
	| "ITEM_ADDED"
	| "ITEM_CHECKED"
	| "ITEM_UNCHECKED"
	| "ITEM_UPDATED"
	| "ITEM_DELETED"
	| "CHECKED_CLEARED"
	| "RECIPE_ADDED_TO_LIST";

export interface ListActivityEvent {
	id: string;
	householdId: string;
	listId: string;
	listTitle: string;
	actorId: string;
	actorName: string;
	type: ListActivityType;
	itemId: string | null;
	itemTitle: string | null;
	metaCount: number | null;
	metaText: string | null;
	createdAt: string;
}

export interface ListActivityFeed {
	unreadCount: number;
	events: ListActivityEvent[];
}
