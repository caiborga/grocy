import api from "@/services/api";

export type HouseholdDto = {
	id: string;
	name?: string;
	title?: string;
	archived?: boolean;
};

export type HouseholdDetailDto = {
	id: string;
	name?: string;
	title?: string;
	archived?: boolean;
	members: any[];
	lists: any[];
};

export const householdService = {
	getAll() {
		return api.get<HouseholdDto[]>("/households");
	},

	getById(householdId: string) {
		return api.get<HouseholdDetailDto>(`/households/${householdId}`);
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
