import {
	createRouter,
	createWebHistory,
	type NavigationGuardReturn,
	type RouteLocationNormalized
} from "vue-router";

import { useUserStore } from "@/stores/userStore";
import { useHouseholdStore } from "@/stores/householdStore";
import { ACCESS_TOKEN_KEY } from "@/constants/auth";

import Register from "@/pages/Register.vue";
import Login from "@/pages/Login.vue";
import List from "@/pages/List.vue";
import HouseHolds from "@/pages/HouseHolds.vue";
import JoinInvite from "@/pages/JoinInvite.vue";
import NotFound from "@/pages/NotFound.vue";

type AppRouteMeta = {
	requiresAuth?: boolean;
};

const router = createRouter({
	history: createWebHistory(),
	routes: [
		{
			path: "/",
			redirect: "/lists/default",
			meta: { requiresAuth: true } satisfies AppRouteMeta
		},
		{
			path: "/login",
			component: Login
		},
		{
			path: "/register",
			component: Register
		},
		{
			path: "/join",
			component: JoinInvite
		},
		{
			path: "/lists/:id?",
			component: List,
			meta: { requiresAuth: true } satisfies AppRouteMeta
		},
		{
			path: "/households",
			component: HouseHolds,
			meta: { requiresAuth: true } satisfies AppRouteMeta
		},
		{ path: "/:pathMatch(.*)*", component: NotFound }
	]
});

router.beforeEach(
	async (to: RouteLocationNormalized): Promise<NavigationGuardReturn> => {
		const token = localStorage.getItem(ACCESS_TOKEN_KEY);
		const requiresAuth = !!(to.meta as AppRouteMeta)?.requiresAuth;

		// Public
		if (!requiresAuth) {
			if (to.path === "/login" && token) return "/lists/default";
			return true;
		}

		// Secure Route without Token -> Login
		if (!token) {
			return { path: "/login", query: { redirect: to.fullPath } };
		}

		// Token available
		const userStore = useUserStore();
		const householdStore = useHouseholdStore();

		try {
			await userStore.loadMe();
			await householdStore.loadActiveHousehold();
			return true;
		} catch (err: unknown) {
			const status = (err as any)?.response?.status as number | undefined;

			if (status === 401 || status === 403) {
				householdStore.deleteActiveHousehold?.();
				localStorage.removeItem(ACCESS_TOKEN_KEY);
				localStorage.removeItem("user");
				return { path: "/login", query: { redirect: to.fullPath } };
			}

			console.error("Guard preload failed:", err);
			return true;
		}
	}
);

export default router;
