import { defineStore } from "pinia";
import type { ListActivityEvent, ListActivityFeed } from "@/models/Activity";
import { activityService } from "@/services/activityService";

export const useActivityStore = defineStore("activity", {
	state: () => ({
		unreadCount: 0,
		events: [] as ListActivityEvent[],
		loading: false,
		loadingMore: false,
		hasMore: false,
		nextCursor: null as string | null,
		acknowledgedIds: [] as string[],
		householdId: null as string | null,
		requestVersion: 0,
		countRequestVersion: 0,
		panelOpen: false,
		refreshingFeed: false,
		refreshQueued: false
	}),
	getters: { hasUnread: (s) => s.unreadCount > 0 },
	actions: {
		async refreshUnread() {
			if (this.panelOpen) {
				await this.refreshOpenFeed();
				return;
			}
			if (!this.householdId) return;
			const householdId = this.householdId;
			const version = this.requestVersion;
			const countVersion = ++this.countRequestVersion;
			try {
				const { data } = await activityService.getUnreadCount(householdId);
				if (version === this.requestVersion && countVersion === this.countRequestVersion) {
					this.unreadCount = data;
				}
			} catch {
				// Keep the last known count; the next live event or timer retries.
			}
		},
		async refreshOpenFeed() {
			if (!this.householdId || !this.panelOpen) return;
			if (this.loading || this.loadingMore || this.refreshingFeed) {
				this.refreshQueued = true;
				return;
			}
			const householdId = this.householdId;
			const version = this.requestVersion;
			this.refreshingFeed = true;
			this.refreshQueued = false;
			try {
				const known = new Set(this.events.map(e => e.id));
				const fresh: ListActivityEvent[] = [];
				let before: string | null = null;
				let firstPage: ListActivityFeed | undefined;
				while (true) {
					const { data } = await activityService.getFeed(householdId, before, before !== null);
					if (version !== this.requestVersion || !this.panelOpen) return;
					firstPage ??= data;
					const overlap = data.events.findIndex(e => known.has(e.id));
					fresh.push(...(overlap < 0 ? data.events : data.events.slice(0, overlap)));
					// Recover all missed events, even when more than one page arrived.
					if (!known.size || overlap >= 0 || !data.hasMore || !data.nextCursor) break;
					before = data.nextCursor;
				}
				if (!firstPage) return;
				this.countRequestVersion++;
				this.unreadCount = firstPage.unreadCount;
				if (!known.size) {
					this.hasMore = firstPage.hasMore;
					this.nextCursor = firstPage.nextCursor;
				}
				this.events = [...fresh, ...this.events];
				await this.markRead(fresh);
			} catch {
				// Retry on the next live event or periodic refresh without clearing history.
			} finally {
				if (version === this.requestVersion) {
					this.refreshingFeed = false;
					if (this.refreshQueued) void this.refreshOpenFeed();
				}
			}
		},
		async loadFeed(householdId: string | null | undefined) {
			if (!householdId) { this.reset(); return; }
			if (this.householdId !== householdId) this.reset();
			const version = ++this.requestVersion;
			this.loading = true;
			this.loadingMore = false;
			this.refreshingFeed = false;
			this.acknowledgedIds = [];
			this.householdId = householdId;
			try {
				const { data } = await activityService.getFeed(householdId);
				if (version !== this.requestVersion) return;
				this.unreadCount = data.unreadCount;
				this.events = data.events;
				this.hasMore = data.hasMore;
				this.nextCursor = data.nextCursor;
			} finally {
				if (version === this.requestVersion) {
					this.loading = false;
					if (this.refreshQueued) void this.refreshOpenFeed();
				}
			}
		},
		async loadMore() {
			if (!this.householdId || this.loading || this.loadingMore || this.refreshingFeed || !this.hasMore) return;
			const householdId = this.householdId;
			const version = this.requestVersion;
			this.loadingMore = true;
			try {
				const { data } = await activityService.getFeed(householdId, this.nextCursor, true);
				if (version !== this.requestVersion) return;
				const ids = new Set(this.events.map(e => e.id));
				this.events.push(...data.events.filter(e => !ids.has(e.id)));
				this.hasMore = data.hasMore;
				this.nextCursor = data.nextCursor;
				this.unreadCount = data.unreadCount;
				await this.markRead(data.events);
			} finally {
				if (version === this.requestVersion) {
					this.loadingMore = false;
					if (this.refreshQueued) void this.refreshOpenFeed();
				}
			}
		},
		async markRead(events = this.events) {
			if (!this.householdId) return;
			const householdId = this.householdId;
			const version = this.requestVersion;
			const unread = events.filter(e => e.unread && !this.acknowledgedIds.includes(e.id));
			for (let i = 0; i < unread.length; i += 20) {
				const batch = unread.slice(i, i + 20);
				await activityService.markRead(householdId, batch.map(e => e.id));
				if (version !== this.requestVersion) return;
				this.countRequestVersion++;
				this.unreadCount = Math.max(0, this.unreadCount - batch.length);
				this.acknowledgedIds.push(...batch.map(e => e.id));
			}
		},
		reset() {
			this.requestVersion++;
			this.unreadCount = 0;
			this.events = [];
			this.householdId = null;
			this.loading = false;
			this.loadingMore = false;
			this.hasMore = false;
			this.nextCursor = null;
			this.acknowledgedIds = [];
			this.refreshingFeed = false;
			this.refreshQueued = false;
		}
	}
});
