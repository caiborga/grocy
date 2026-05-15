import { Household } from "@/models/Household";
import api from "@/services/api";

export const householdService = {
	getAll() {
		return api.get<Household[]>("/households");
	},

	getById(householdId: string) {
		return api.get<Household>(`/households/${householdId}`);
	},

	create(name: string) {
		return api.post("/households", name);
	},

	rename(householdId: string, payload: { name: string; archived?: boolean }) {
		return api.patch(`/households/${householdId}`, payload);
	},

	archive(householdId: string, payload: { name: string; archived?: boolean }) {
		return api.patch(`/households/${householdId}`, payload);
	},

    createList(householdId: string, payload: { name: string }) {
		return api.post(`/households/${householdId}/lists`, payload);
	}
};
