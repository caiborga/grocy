import { Item } from "@/models/Item";
import api from "@/services/api";

// Live reloads wait for local writes so an early SSE event cannot duplicate a new item.
export const listWriteState = { pending: 0, revision: 0 };
async function write<T>(request: () => Promise<T>): Promise<T> {
    listWriteState.pending++;
    listWriteState.revision++;
    try {
        return await request();
    } finally {
        listWriteState.pending--;
        listWriteState.revision++;
    }
}

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
		return write(() => api.post(`/lists/${listId}/items`, item));
	},

	patchItem(
		listId: string,
		itemId: string,
		patch: Item,
	) {
		return write(() => api.patch(`/lists/${listId}/items/${itemId}`, patch));
	},

	deleteList(listId: string) {
		return write(() => api.delete(`/lists/${listId}`));
	},

	deleteItem(listId: string, itemId: string) {
		return write(() => api.delete(`/lists/${listId}/items/${itemId}`));
	},

	clearChecked(listId: string) {
		return write(() => api.delete(`/lists/${listId}/items/checked`));
	},

	patchList(listId: string, patch: { title?: string; isDefault?: boolean }) {
		return write(() => api.patch(`/lists/${listId}`, patch));
	}
};
