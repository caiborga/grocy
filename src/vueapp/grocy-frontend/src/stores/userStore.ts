import { ACCESS_TOKEN_KEY } from "@/constants/auth";
import { LoginRequest } from "@/models/Login";
import { Me } from "@/models/Me";
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
		await householdStore.loadActiveHousehold();
		const mel = me.value;
		await householdStore.selectHousehold(me?.value?.activeHouseholdId);
	}

	async function logout() {
		me.value = null;

		const householdStore = useHouseholdStore();
		// householdStore.clear();
	}

	async function setActiveHousehold(householdId: string) {
		await userService.setActiveHousehold(householdId);
		if (me.value) me.value.activeHouseholdId = householdId;

		const householdStore = useHouseholdStore();
		await householdStore.loadActiveHousehold();
	}

	async function registerAndLogin(payload: {
		displayName: string;
		email: string;
		password: string;
	}) {
		await userService.register(payload);
		await authService.login({
			email: payload.email,
			password: payload.password
		});
		await loadMe(true);
		const householdStore = useHouseholdStore();
		await householdStore.loadActiveHousehold();
	}

	return {
		me,
		isAuthenticated,
		activeHouseholdId,
		role,
		loadMe,
		login,
		logout,
		registerAndLogin,
		setActiveHousehold
	};
});
