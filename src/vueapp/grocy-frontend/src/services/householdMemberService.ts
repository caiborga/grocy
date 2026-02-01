import { Role } from "@/models/Role";
import api from "@/services/api";

export const householdMemberService = {
	updateRole(householdId: string, memberId: string, payload: { role: Role }) {
		return api.patch(`/households/${householdId}/members/${memberId}`, payload);
	},

	remove(householdId: string, memberId: string) {
		return api.delete(`/households/${householdId}/members/${memberId}`);
	}
};
