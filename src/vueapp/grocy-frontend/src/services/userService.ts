import { RegisterRequest, RegisterResponse } from "@/models/Register";
import api from "@/services/api";

export const userService = {
	register(payload: RegisterRequest) {
		return api.post<RegisterResponse>("/users", payload);
	},

	me() {
		return api.get("/me");
	},

	setActiveHousehold(householdId: string) {
		return api.patch(`/me/active-household/${householdId}`);
	}
};
