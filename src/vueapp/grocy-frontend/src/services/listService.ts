import { Item } from "@/models/Item";
import api from "@/services/api";

export const listService = {
	getList(id: string | "default") {
		return api.get(`/lists/${id}`);
	},

	getItems(listId: string, filter: string, sort: string) {
		return api.get(`/lists/${listId}/items`, {
			params: {
				filter: filter,
				sort: sort
			}
		});
	},

	addItem(
		listId: string,
		item: Item
	) {
		return api.post(`/lists/${listId}/items`, item);
	},

	patchItem(
		listId: string,
		itemId: string,
		patch: Item,
	) {
		return api.patch(`/lists/${listId}/items/${itemId}`, patch);
	},

	deleteList(listId: string) {
		return api.delete(`/lists/${listId}`);
	},

	deleteItem(listId: string, itemId: string) {
		return api.delete(`/lists/${listId}/items/${itemId}`);
	},

	clearChecked(listId: string) {
		return api.delete(`/lists/${listId}/items/checked`);
	},

	patchList(listId: string, patch: { title?: string; isDefault?: boolean }) {
		return api.patch(`/lists/${listId}`, patch);
	}
};
