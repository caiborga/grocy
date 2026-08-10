import type { ListActivityFeed } from "@/models/Activity";
import api from "@/services/api";

export const activityService = {
	getFeed(householdId: string) {
		return api.get<ListActivityFeed>(`/households/${householdId}/activity`);
	},

	markRead(householdId: string) {
		return api.post(`/households/${householdId}/activity/read`);
	}
};
