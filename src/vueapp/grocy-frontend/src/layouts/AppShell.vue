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
					<button
						v-if="activeHousehold"
						type="button"
						class="activity-btn"
						:aria-label="
							hasUnread
								? `Änderungen anzeigen (${unreadCount} neu)`
								: 'Änderungen anzeigen'
						"
						@click="openActivity"
					>
						<el-icon :size="18">
							<Bell />
						</el-icon>
						<span
							v-if="hasUnread"
							class="activity-dot"
							aria-hidden="true"
						/>
					</button>

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

				<button
					v-if="activeHousehold"
					type="button"
					class="activity-btn activity-btn--mobile-top"
					:aria-label="
						hasUnread
							? `Änderungen anzeigen (${unreadCount} neu)`
							: 'Änderungen anzeigen'
					"
					@click="openActivity"
				>
					<el-icon :size="18">
						<Bell />
					</el-icon>
					<span
						v-if="hasUnread"
						class="activity-dot"
						aria-hidden="true"
					/>
				</button>
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

		<div
			v-if="activityOpen"
			class="activity-backdrop"
			@click="closeActivity"
		/>

		<aside
			v-if="activityOpen"
			class="activity-panel"
			role="dialog"
			aria-modal="true"
			aria-labelledby="activity-title"
		>
			<header class="activity-header">
				<div>
					<h2 id="activity-title" class="activity-title">
						Änderungen
					</h2>
					<p class="activity-subtitle">
						Was sich in diesem Haushalt getan hat
					</p>
				</div>
				<button
					type="button"
					class="activity-close"
					aria-label="Schließen"
					@click="closeActivity"
				>
					<el-icon :size="18">
						<Close />
					</el-icon>
				</button>
			</header>

			<div class="activity-body">
				<p v-if="activityLoading" class="activity-empty">
					Lade Änderungen…
				</p>
				<p v-else-if="events.length === 0" class="activity-empty">
					Noch keine Änderungen.
				</p>
				<ul v-else class="activity-list">
					<li
						v-for="event in events"
						:key="event.id"
						class="activity-item"
					>
						<button
							type="button"
							class="activity-item-btn"
							@click="goToList(event.listId)"
						>
							<span class="activity-item-text">
								{{ formatEvent(event) }}
							</span>
							<span class="activity-item-meta">
								<span>{{ event.listTitle }}</span>
								<span aria-hidden="true">·</span>
								<span>{{ formatRelative(event.createdAt) }}</span>
							</span>
						</button>
					</li>
				</ul>
			</div>
		</aside>
	</div>
</template>

<script setup>
import { computed, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { storeToRefs } from "pinia";
import { ElMessage } from "element-plus";
import {
	Bell,
	Close,
	StarFilled,
	EditPen,
	View
} from "@element-plus/icons-vue";

import { useHouseholdStore } from "@/stores/householdStore";
import { useUserStore } from "@/stores/userStore";
import { useActivityStore } from "@/stores/activityStore";
import { ACCESS_TOKEN_KEY } from "@/constants/auth";
import { Role } from "@/models/Role";

const route = useRoute();
const router = useRouter();

const userStore = useUserStore();
const householdStore = useHouseholdStore();
const activityStore = useActivityStore();

const { me, role } = storeToRefs(userStore);
const { activeHousehold } = storeToRefs(householdStore);
const { unreadCount, events, loading: activityLoading, hasUnread } =
	storeToRefs(activityStore);

const activityOpen = ref(false);

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

watch(
	() => [showNav.value, activeHousehold.value?.id],
	([authed, householdId]) => {
		if (authed && householdId) {
			activityStore.loadFeed(householdId);
		} else if (!householdId) {
			activityStore.reset();
		}
	},
	{ immediate: true }
);

function isActive(to) {
	return route.path === to || route.path.startsWith(to + "/");
}

async function openActivity() {
	activityOpen.value = true;
	if (activeHousehold.value?.id) {
		await activityStore.loadFeed(activeHousehold.value.id);
	}
	await activityStore.markRead();
}

function closeActivity() {
	activityOpen.value = false;
}

function goToList(listId) {
	closeActivity();
	router.push(`/lists/${listId}`);
}

function formatEvent(event) {
	const name = event.actorName || "Jemand";
	const item = event.itemTitle ? `„${event.itemTitle}“` : null;

	switch (event.type) {
		case "ITEM_ADDED":
			return `${name} hat ${item ?? "einen Eintrag"} hinzugefügt`;
		case "ITEM_CHECKED":
			return `${name} hat ${item ?? "einen Eintrag"} abgehakt`;
		case "ITEM_UNCHECKED":
			return `${name} hat ${item ?? "einen Eintrag"} wieder geöffnet`;
		case "ITEM_UPDATED":
			return `${name} hat ${item ?? "einen Eintrag"} geändert`;
		case "ITEM_DELETED":
			return `${name} hat ${item ?? "einen Eintrag"} gelöscht`;
		case "CHECKED_CLEARED": {
			const count = event.metaCount ?? 0;
			return `${name} hat ${count} abgehakte Einträge entfernt`;
		}
		case "RECIPE_ADDED_TO_LIST": {
			const recipe = event.metaText ? `„${event.metaText}“` : "ein Rezept";
			const count = event.metaCount;
			const suffix =
				count != null ? ` (${count} Zutaten)` : "";
			return `${name} hat ${recipe} zur Liste hinzugefügt${suffix}`;
		}
		default:
			return `${name} hat etwas geändert`;
	}
}

function formatRelative(iso) {
	const date = new Date(iso);
	if (Number.isNaN(date.getTime())) return "";

	const diffSec = Math.round((date.getTime() - Date.now()) / 1000);
	const abs = Math.abs(diffSec);
	const rtf = new Intl.RelativeTimeFormat("de", { numeric: "auto" });

	if (abs < 60) return rtf.format(diffSec, "second");
	const diffMin = Math.round(diffSec / 60);
	if (Math.abs(diffMin) < 60) return rtf.format(diffMin, "minute");
	const diffHour = Math.round(diffMin / 60);
	if (Math.abs(diffHour) < 24) return rtf.format(diffHour, "hour");
	const diffDay = Math.round(diffHour / 24);
	if (Math.abs(diffDay) < 7) return rtf.format(diffDay, "day");
	return date.toLocaleDateString("de-DE", {
		day: "numeric",
		month: "short"
	});
}

async function logout() {
	try {
		localStorage.removeItem(ACCESS_TOKEN_KEY);
		activityStore.reset();
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

.activity-btn {
	position: relative;
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 38px;
	height: 38px;
	border: none;
	border-radius: 0.75rem;
	background: #f1f5f9;
	color: #475569;
	cursor: pointer;
	transition:
		background 0.15s ease,
		color 0.15s ease;
}

.activity-btn:hover {
	background: #e2e8f0;
	color: #0f172a;
}

.activity-btn--mobile-top {
	flex-shrink: 0;
}

.activity-dot {
	position: absolute;
	top: 8px;
	right: 8px;
	width: 8px;
	height: 8px;
	border-radius: 9999px;
	background: #dc2626;
	box-shadow: 0 0 0 2px #fff;
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

.activity-backdrop {
	position: fixed;
	inset: 0;
	z-index: 60;
	background: rgba(15, 23, 42, 0.28);
}

.activity-panel {
	position: fixed;
	top: 0;
	right: 0;
	z-index: 70;
	display: flex;
	flex-direction: column;
	width: min(100vw, 380px);
	height: 100vh;
	background: #fff;
	box-shadow: -12px 0 40px rgba(15, 23, 42, 0.12);
	animation: activity-slide 0.2s ease;
}

@keyframes activity-slide {
	from {
		transform: translateX(12px);
		opacity: 0.7;
	}
	to {
		transform: translateX(0);
		opacity: 1;
	}
}

.activity-header {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 12px;
	padding: 20px 18px 14px;
	border-bottom: 1px solid rgba(15, 23, 42, 0.06);
}

.activity-title {
	margin: 0;
	font-family: Figtree, ui-sans-serif, system-ui, sans-serif;
	font-size: 1.15rem;
	font-weight: 700;
	letter-spacing: -0.02em;
	color: #0f172a;
}

.activity-subtitle {
	margin: 4px 0 0;
	font-size: 0.8rem;
	color: #94a3b8;
}

.activity-close {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 34px;
	height: 34px;
	border: none;
	border-radius: 0.65rem;
	background: #f1f5f9;
	color: #64748b;
	cursor: pointer;
}

.activity-close:hover {
	background: #e2e8f0;
	color: #0f172a;
}

.activity-body {
	flex: 1;
	overflow-y: auto;
	padding: 8px 10px 24px;
}

.activity-empty {
	margin: 24px 10px;
	font-size: 0.9rem;
	color: #94a3b8;
	text-align: center;
}

.activity-list {
	margin: 0;
	padding: 0;
	list-style: none;
}

.activity-item-btn {
	display: flex;
	width: 100%;
	flex-direction: column;
	align-items: flex-start;
	gap: 4px;
	padding: 12px 10px;
	border: none;
	border-radius: 0.85rem;
	background: transparent;
	text-align: left;
	cursor: pointer;
	transition: background 0.15s ease;
}

.activity-item-btn:hover {
	background: #f8fafc;
}

.activity-item-text {
	font-size: 0.9rem;
	font-weight: 600;
	line-height: 1.35;
	color: #0f172a;
}

.activity-item-meta {
	display: flex;
	flex-wrap: wrap;
	align-items: center;
	gap: 6px;
	font-size: 0.75rem;
	color: #94a3b8;
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

	.activity-btn--mobile-top {
		display: none;
	}

	.household-name {
		max-width: 260px;
	}
}
</style>
