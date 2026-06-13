import { ACCESS_TOKEN_KEY } from "@/constants/auth";
import { LoginRequest } from "@/models/Login";
import { Me } from "@/models/Me";
import { RegisterRequest } from "@/models/Register";
import { authService } from "@/services/authService";
import { userService } from "@/services/userService";
import { defineStore } from "pinia";
import { computed, ref } from "vue";
import { useHouseholdStore } from "./householdStore";

export const useUserStore = defineStore("user", () => {
	const me = ref<Me | null>(null);
	const loading = ref(false);

	const token = ref<string | null>(localStorage.getItem(ACCESS_TOKEN_KEY));

	const isAuthenticated = computed(() => !!token.value);

	const activeHouseholdId = computed(
		() => me.value?.activeHouseholdId ?? null
	);

	const role = computed(() => me.value?.role ?? null);

	async function loadMe(force = false) {
		if (!token.value) {
			me.value = null;
			return;
		}

		if (loading.value && !force) return;

		loading.value = true;
		try {
			const res = await userService.me();
			me.value = res.data;
		} finally {
			loading.value = false;
		}
	}

	async function login(payload: LoginRequest) {
		const data = await authService.login(payload);
		token.value = data.accessToken;

		await loadMe(true);

		const householdStore = useHouseholdStore();

		if (me.value?.activeHouseholdId) {
			await householdStore.loadActiveHousehold();
		} else {
			householdStore.activeHousehold = null;
			householdStore.activeHouseholdId = null;
		}
	}

	async function logout() {
		token.value = null;
		me.value = null;
		localStorage.removeItem(ACCESS_TOKEN_KEY);

		const householdStore = useHouseholdStore();
		householdStore.activeHousehold = null;
		householdStore.activeHouseholdId = null;
	}

	async function setActiveHousehold(householdId: string) {
		await userService.setActiveHousehold(householdId);
		if (me.value) me.value.activeHouseholdId = householdId;

		const householdStore = useHouseholdStore();
		await householdStore.loadActiveHousehold();
	}

	async function register(payload: RegisterRequest) {
		return authService.register(payload);
	}

	return {
		me,
		isAuthenticated,
		activeHouseholdId,
		role,
		loadMe,
		login,
		logout,
		register,
		setActiveHousehold
	};
});
