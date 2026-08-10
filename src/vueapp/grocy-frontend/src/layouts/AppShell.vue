<template>
	<div class="app-shell" :class="{ 'app-shell--authed': showNav }">
		<header v-if="showNav" class="app-topbar">
			<div class="app-topbar-inner">
				<div class="topbar-left">
					<RouterLink to="/lists" class="brand-link" aria-label="Grocy">
						<img
							class="brand-logo"
							src="/grocy.png"
							alt="Grocy"
						/>
					</RouterLink>

					<div class="context-rail" aria-hidden="true" />

					<div class="household-context">
						<div class="context-top">
							<span class="context-label">Haushalt</span>
							<span
								v-if="roleLabel"
								class="role-tag"
								:class="roleTone"
								:title="`Deine Rolle: ${roleLabel}`"
							>
								<el-icon :size="11">
									<component :is="roleIcon" />
								</el-icon>
								{{ roleLabel }}
							</span>
							<RouterLink
								v-else-if="!activeHousehold"
								to="/households"
								class="role-tag role-tag--action"
							>
								Auswählen
							</RouterLink>
						</div>

						<RouterLink
							to="/households"
							class="household-name"
							:title="
								activeHousehold?.name
									? 'Haushalt wechseln'
									: 'Haushalt auswählen'
							"
						>
							{{
								activeHousehold?.name ?? "Noch keiner gewählt"
							}}
						</RouterLink>
					</div>
				</div>

				<nav class="desktop-nav" aria-label="Hauptnavigation">
					<div class="nav-pills">
						<RouterLink
							v-for="item in navItems"
							:key="item.to"
							:to="item.to"
							class="nav-pill"
							:class="{ 'nav-pill--active': isActive(item.to) }"
						>
							{{ item.label }}
						</RouterLink>
					</div>

					<div v-if="me?.name" class="user-block" :title="me?.email">
						<span class="user-name">{{ me.name }}</span>
					</div>

					<button class="logout-btn" type="button" @click="logout">
						Abmelden
					</button>
				</nav>
			</div>
		</header>

		<main :class="showNav ? 'app-main app-main--nav' : 'app-main'">
			<RouterView />
		</main>

		<nav
			v-if="showNav"
			class="mobile-nav"
			aria-label="Mobile Navigation"
		>
			<div class="mobile-nav-inner">
				<RouterLink
					v-for="item in navItemsMobile"
					:key="item.to"
					:to="item.to"
					class="mobile-link"
					:class="{ 'mobile-link--active': isActive(item.to) }"
				>
					<el-icon :size="22">
						<component :is="item.icon" />
					</el-icon>
					<span>{{ item.label }}</span>
				</RouterLink>

				<button
					type="button"
					class="mobile-link mobile-link--danger"
					@click="logout"
				>
					<el-icon :size="22">
						<SwitchButton />
					</el-icon>
					<span>Abmelden</span>
				</button>
			</div>
		</nav>
	</div>
</template>

<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { storeToRefs } from "pinia";
import { ElMessage } from "element-plus";
import { StarFilled, EditPen, View } from "@element-plus/icons-vue";

import { useHouseholdStore } from "@/stores/householdStore";
import { useUserStore } from "@/stores/userStore";
import { ACCESS_TOKEN_KEY } from "@/constants/auth";
import { Role } from "@/models/Role";

const route = useRoute();
const router = useRouter();

const userStore = useUserStore();
const householdStore = useHouseholdStore();

const { me, role } = storeToRefs(userStore);
const { activeHousehold } = storeToRefs(householdStore);

const showNav = computed(() => !!route.meta.requiresAuth);

const navItems = [
	{ label: "Liste", to: "/lists" },
	{ label: "Haushalte", to: "/households" },
	{ label: "Rezepte", to: "/recipes" }
];

const navItemsMobile = [
	{ label: "Liste", to: "/lists", icon: "List" },
	{ label: "Haushalte", to: "/households", icon: "House" },
	{ label: "Rezepte", to: "/recipes", icon: "Dish" }
];

const roleLabel = computed(() => {
	if (role.value === Role.OWNER) return "Besitzer";
	if (role.value === Role.EDITOR) return "Bearbeiter";
	if (role.value === Role.VIEWER) return "Betrachter";
	return "";
});

const roleIcon = computed(() => {
	if (role.value === Role.OWNER) return StarFilled;
	if (role.value === Role.EDITOR) return EditPen;
	if (role.value === Role.VIEWER) return View;
	return View;
});

const roleTone = computed(() => {
	if (role.value === Role.OWNER) return "role-tag--owner";
	if (role.value === Role.EDITOR) return "role-tag--editor";
	if (role.value === Role.VIEWER) return "role-tag--viewer";
	return "";
});

function isActive(to) {
	return route.path === to || route.path.startsWith(to + "/");
}

async function logout() {
	try {
		localStorage.removeItem(ACCESS_TOKEN_KEY);
	} finally {
		router.push("/login");
		ElMessage.success("Bis bald!");
	}
}
</script>

<style scoped>
.app-shell {
	min-height: 100vh;
}

.app-shell--authed {
	background:
		radial-gradient(
			ellipse 70% 45% at 0% -5%,
			rgba(37, 99, 235, 0.09),
			transparent 55%
		),
		radial-gradient(
			ellipse 50% 35% at 100% 0%,
			rgba(96, 165, 250, 0.1),
			transparent 50%
		),
		#f4f7fc;
}

.app-topbar {
	position: sticky;
	top: 0;
	z-index: 40;
	border-bottom: 1px solid rgba(15, 23, 42, 0.06);
	background: rgba(255, 255, 255, 0.82);
	backdrop-filter: blur(12px);
	-webkit-backdrop-filter: blur(12px);
}

.app-topbar-inner {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 16px;
	width: min(1120px, calc(100% - 32px));
	margin: 0 auto;
	padding: 12px 0;
}

.topbar-left {
	display: flex;
	min-width: 0;
	align-items: center;
	gap: 14px;
}

.brand-link {
	display: inline-flex;
	flex-shrink: 0;
	align-items: center;
	text-decoration: none;
}

.brand-logo {
	display: block;
	height: 30px;
	width: auto;
	max-width: 132px;
	object-fit: contain;
}

.context-rail {
	width: 1px;
	height: 32px;
	flex-shrink: 0;
	align-self: center;
	background: linear-gradient(
		to bottom,
		transparent,
		rgba(15, 23, 42, 0.12),
		transparent
	);
}

.household-context {
	display: flex;
	min-width: 0;
	flex-direction: column;
	justify-content: center;
	gap: 3px;
}

.context-top {
	display: flex;
	min-width: 0;
	align-items: center;
	gap: 8px;
}

.context-label {
	flex-shrink: 0;
	font-size: 0.62rem;
	font-weight: 600;
	letter-spacing: 0.06em;
	text-transform: uppercase;
	color: #94a3b8;
	line-height: 1;
}

.role-tag {
	display: inline-flex;
	align-items: center;
	gap: 3px;
	max-width: 100%;
	padding: 1px 6px 1px 4px;
	border-radius: 0.35rem;
	font-size: 0.65rem;
	font-weight: 600;
	letter-spacing: 0.01em;
	line-height: 1.35;
	white-space: nowrap;
}

.role-tag--owner {
	background: #ecfdf5;
	color: #0f766e;
}

.role-tag--editor {
	background: #eff6ff;
	color: #1d4ed8;
}

.role-tag--viewer {
	background: #f1f5f9;
	color: #475569;
}

.role-tag--action {
	background: #eff6ff;
	color: #1d4ed8;
	text-decoration: none;
}

.role-tag--action:hover {
	background: #dbeafe;
}

.household-name {
	max-width: min(52vw, 260px);
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
	font-family: Figtree, ui-sans-serif, system-ui, sans-serif;
	font-size: 1rem;
	font-weight: 700;
	letter-spacing: -0.025em;
	line-height: 1.2;
	color: #0f172a;
	text-decoration: none;
	transition: color 0.15s ease;
}

.household-name:hover {
	color: #1d4ed8;
}

.user-block {
	display: none;
	max-width: 140px;
	padding-right: 4px;
	border-right: 1px solid rgba(15, 23, 42, 0.08);
}

.user-name {
	display: block;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
	padding-right: 10px;
	font-size: 0.8rem;
	font-weight: 500;
	color: #64748b;
}

.desktop-nav {
	display: none;
	align-items: center;
	gap: 10px;
}

.nav-pills {
	display: flex;
	align-items: center;
	gap: 4px;
	padding: 4px;
	border-radius: 9999px;
	background: #f1f5f9;
}

.nav-pill {
	padding: 8px 14px;
	border-radius: 9999px;
	font-size: 0.875rem;
	font-weight: 600;
	color: #64748b;
	text-decoration: none;
	transition:
		background 0.15s ease,
		color 0.15s ease,
		box-shadow 0.15s ease;
}

.nav-pill:hover {
	color: #0f172a;
}

.nav-pill--active {
	background: #fff;
	color: #1d4ed8;
	box-shadow: 0 1px 3px rgba(15, 23, 42, 0.08);
}

.logout-btn {
	padding: 8px 12px;
	border: none;
	border-radius: 9999px;
	background: transparent;
	font-size: 0.875rem;
	font-weight: 600;
	color: #64748b;
	cursor: pointer;
	transition:
		background 0.15s ease,
		color 0.15s ease;
}

.logout-btn:hover {
	background: #fef2f2;
	color: #dc2626;
}

.app-main--nav {
	padding-bottom: 5.5rem;
}

.mobile-nav {
	position: fixed;
	right: 1rem;
	bottom: 1rem;
	left: 1rem;
	z-index: 50;
}

.mobile-nav-inner {
	display: flex;
	align-items: center;
	justify-content: space-around;
	padding: 8px 6px;
	border: 1px solid rgba(15, 23, 42, 0.08);
	border-radius: 1.25rem;
	background: rgba(255, 255, 255, 0.92);
	box-shadow: 0 10px 30px rgba(15, 23, 42, 0.12);
	backdrop-filter: blur(12px);
}

.mobile-link {
	display: flex;
	min-width: 64px;
	flex-direction: column;
	align-items: center;
	gap: 2px;
	padding: 8px 10px;
	border: none;
	border-radius: 0.9rem;
	background: transparent;
	font-size: 0.68rem;
	font-weight: 600;
	color: #94a3b8;
	text-decoration: none;
	cursor: pointer;
	transition:
		background 0.15s ease,
		color 0.15s ease;
}

.mobile-link--active {
	background: #eff6ff;
	color: #2563eb;
}

.mobile-link--danger:hover {
	color: #dc2626;
}

@media (min-width: 768px) {
	.desktop-nav {
		display: flex;
	}

	.user-block {
		display: block;
	}

	.app-main--nav {
		padding-bottom: 0;
	}

	.mobile-nav {
		display: none;
	}

	.household-name {
		max-width: 260px;
	}
}
</style>
