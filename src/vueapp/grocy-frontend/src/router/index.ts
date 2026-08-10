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
import CheckEmail from "@/pages/CheckEmail.vue";
import VerifyEmail from "@/pages/VerifyEmail.vue";
import ForgotPassword from "@/pages/ForgotPassword.vue";
import ResetPassword from "@/pages/ResetPassword.vue";
import Landing from "@/pages/Landing.vue";
import Login from "@/pages/Login.vue";
import List from "@/pages/List.vue";
import HouseHolds from "@/pages/HouseHolds.vue";
import JoinInvite from "@/pages/JoinInvite.vue";
import NotFound from "@/pages/NotFound.vue";
import RecipeListView from "@/pages/RecipeListView.vue";
import RecipeFormView from "@/pages/RecipeFormView.vue";

type AppRouteMeta = {
	requiresAuth?: boolean;
	guestOnly?: boolean;
};

const router = createRouter({
	history: createWebHistory(),
	routes: [
		{
			path: "/",
			component: Landing,
			meta: { guestOnly: true } satisfies AppRouteMeta
		},
		{
			path: "/login",
			component: Login,
			meta: { guestOnly: true } satisfies AppRouteMeta
		},
		{
			path: "/register",
			component: Register,
			meta: { guestOnly: true } satisfies AppRouteMeta
		},
		{
			path: "/check-email",
			component: CheckEmail,
			meta: { guestOnly: true } satisfies AppRouteMeta
		},
		{
			path: "/verify-email",
			component: VerifyEmail,
			meta: { guestOnly: true } satisfies AppRouteMeta
		},
		{
			path: "/forgot-password",
			component: ForgotPassword,
			meta: { guestOnly: true } satisfies AppRouteMeta
		},
		{
			path: "/reset-password",
			component: ResetPassword,
			meta: { guestOnly: true } satisfies AppRouteMeta
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
		{
			path: "/recipes",
			component: RecipeListView,
			meta: { requiresAuth: true } satisfies AppRouteMeta
		},
		{
			path: "/recipes/new",
			component: RecipeFormView,
			meta: { requiresAuth: true } satisfies AppRouteMeta
		},
		{
			path: "/recipes/:id/edit",
			component: RecipeFormView,
			meta: { requiresAuth: true } satisfies AppRouteMeta
		},
		{
			path: "/recipes/:id",
			redirect: "/recipes",
			meta: { requiresAuth: true } satisfies AppRouteMeta
		},
		{
			path: "/:pathMatch(.*)*",
			component: NotFound
		}
	]
});

router.beforeEach(
	async (to: RouteLocationNormalized): Promise<NavigationGuardReturn> => {
		const token = localStorage.getItem(ACCESS_TOKEN_KEY);
		const meta = to.meta as AppRouteMeta;

		if (meta.guestOnly && token) {
			return "/lists/default";
		}

		if (!meta.requiresAuth) {
			return true;
		}

		if (!token) {
			return { path: "/login", query: { redirect: to.fullPath } };
		}

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
