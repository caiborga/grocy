import { defineStore } from "pinia";
import { householdService } from "@/services/householdService";
import { householdMemberService } from "@/services/householdMemberService";
import { Household } from "@/models/Household";
import { List } from "@/models/List";
import { userService } from "@/services/userService";
import { Role } from "@/models/Role";
import { useUserStore } from "./userStore";

export const useHouseholdStore = defineStore("household", {
	state: () => ({
		households: [] as Household[],
		activeHousehold: null as Household | null,
		activeHouseholdId: null as string | null,
		householdMembers: [] as any[],
		householdlists: [] as List[],
		loading: false
	}),

	getters: {
		activeHouseholds: (s) => s.households.filter((h) => !h.archived)
	},

	actions: {
		async loadHouseholds() {
			this.loading = true;
			try {
				const res = await householdService.getAll();
				this.households = res.data;
			} finally {
				this.loading = false;
			}
		},

		async loadActiveHousehold() {
			const userStore = useUserStore();
			const householdId = userStore.me?.activeHouseholdId;

			this.loading = true;
			try {
				if (!householdId) {
					this.activeHousehold = null;
					return;
				}

				const res = await householdService.getById(householdId);
				this.activeHouseholdId = householdId;
				this.activeHousehold = res.data;
				this.householdMembers = res.data.members ?? [];
				this.householdlists = res.data.lists ?? [];
			} finally {
				this.loading = false;
			}
		},

		async selectHousehold(householdId: string) {
			this.activeHouseholdId = householdId;
			await userService.setActiveHousehold(householdId);
			const res = await householdService.getById(householdId);
			this.activeHousehold = res.data;
			this.householdMembers = res.data.members ?? [];
			this.householdlists = res.data.lists ?? [];
		},

		async createHousehold(name: string) {
			const res = await householdService.create(name);
			this.households.unshift(res.data);

			await this.selectHousehold(res.data.id);
		},

		async renameActiveHousehold(name: string) {
			if (!this.activeHousehold) return;
			await householdService.rename(this.activeHousehold.id, { name });
			this.activeHousehold.name = name;
		},

		async deleteActiveHousehold() {
			if (!this.activeHousehold) return;

			const household = this.activeHousehold;

			await householdService.archive(household.id, {
				name: household.name,
				archived: true
			});

			this.households = this.households.filter(
				(h) => h.id !== household.id
			);
			this.activeHousehold = null;
			this.activeHouseholdId = null;
		},

		async updateMemberRole(memberId: string, role: Role) {
			if (!this.activeHousehold) return;
			await householdMemberService.updateRole(
				this.activeHousehold.id,
				memberId,
				{ role }
			);
		},

		async removeMember(memberId: string) {
			if (!this.activeHousehold) return;

			await householdMemberService.remove(
				this.activeHousehold.id,
				memberId
			);

			this.householdMembers = this.householdMembers.filter(
				(m) => m.id !== memberId
			);
		}
	}
});
