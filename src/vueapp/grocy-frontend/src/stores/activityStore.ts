import { defineStore } from "pinia";
import type { ListActivityEvent } from "@/models/Activity";
import { activityService } from "@/services/activityService";

export const useActivityStore = defineStore("activity", {
	state: () => ({
		unreadCount: 0,
		events: [] as ListActivityEvent[],
		loading: false,
		householdId: null as string | null
	}),

	getters: {
		hasUnread: (s) => s.unreadCount > 0
	},

	actions: {
		async loadFeed(householdId: string | null | undefined) {
			if (!householdId) {
				this.reset();
				return;
			}

			this.loading = true;
			this.householdId = householdId;
			try {
				const res = await activityService.getFeed(householdId);
				const data = res.data;
				this.unreadCount = data.unreadCount ?? 0;
				this.events = data.events ?? [];
			} catch {
				this.unreadCount = 0;
				this.events = [];
			} finally {
				this.loading = false;
			}
		},

		async markRead() {
			if (!this.householdId) return;

			this.unreadCount = 0;
			try {
				await activityService.markRead(this.householdId);
			} catch {
				// Badge already cleared locally; next loadFeed will resync.
			}
		},

		reset() {
			this.unreadCount = 0;
			this.events = [];
			this.householdId = null;
			this.loading = false;
		}
	}
});
