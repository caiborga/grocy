import type { ListActivityFeed } from "@/models/Activity";
import api from "@/services/api";

export const activityService = {
	getUnreadCount(householdId: string) {
		return api.get<number>(`/households/${householdId}/activity/unread`);
	},
	getFeed(householdId: string, before?: string | null, older = false) {
		return api.get<ListActivityFeed>(`/households/${householdId}/activity`, { params: { before, older } });
	},

	markRead(householdId: string, eventIds: string[]) {
		return api.post(`/households/${householdId}/activity/read`, eventIds);
	}
};
