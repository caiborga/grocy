import { Role } from "@/models/Role";
import api from "@/services/api";

export const inviteService = {
	preview(token: string) {
		return api.get(`/invites/${token}`);
	},

	accept(token: string) {
		return api.post(`/invites/${token}/accept`);
	},

	create(householdId: string, payload: { role: Role }) {
		return api.post(`/households/${householdId}/invites`, payload);
	}
};
