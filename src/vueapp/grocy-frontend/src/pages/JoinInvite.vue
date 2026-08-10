<template>
	<div class="g-page">
		<div class="g-page-inner-narrow space-y-4">
			<header class="g-panel">
				<p class="text-sm font-semibold text-primary">Einladung</p>
				<h1 class="g-page-title mt-1">Zu einem Haushalt einladen</h1>
				<p class="g-page-sub">
					Öffne die Einladung, um einem Haushalt beizutreten.
				</p>
			</header>

			<div class="g-panel space-y-4">
				<div v-if="!token" class="space-y-3">
					<p class="text-sm text-ink">
						In diesem Link fehlt der Einladungscode.
					</p>
					<el-button type="primary" round @click="goHome">
						Zur Startseite
					</el-button>
				</div>

				<div v-else-if="token && !isAuthenticated" class="space-y-4">
					<p class="text-sm text-ink">
						Du wurdest zu einem Haushalt eingeladen.
					</p>

					<p class="text-sm text-muted">
						Bitte melde dich an oder registriere dich, um die
						Einladung anzunehmen.
					</p>

					<div class="flex flex-wrap gap-2">
						<el-button type="primary" round @click="goToLogin">
							Anmelden / Registrieren
						</el-button>
						<el-button round @click="goHome">Abbrechen</el-button>
					</div>
				</div>

				<div v-else class="space-y-4">
					<div v-if="loading" class="text-sm text-muted">
						Einladung wird geladen…
					</div>

					<div v-else-if="error" class="space-y-3">
						<el-alert
							:title="error.title"
							:type="error.type"
							show-icon
							:closable="false"
						/>
						<div class="flex flex-wrap gap-2">
							<el-button
								v-if="error.canRetry"
								type="primary"
								round
								@click="loadInvite"
							>
								Erneut versuchen
							</el-button>
							<el-button round @click="goHome">
								Zur Startseite
							</el-button>
						</div>
					</div>

					<div v-else-if="invite" class="space-y-4">
						<div>
							<div class="text-sm text-muted">Haushalt</div>
							<div class="font-display text-xl font-bold text-ink">
								{{ invite.householdName }}
							</div>
						</div>

						<div class="grid grid-cols-1 gap-3 sm:grid-cols-2">
							<div class="rounded-xl bg-primary-soft/70 p-3">
								<div class="text-xs font-medium text-muted">
									Rolle
								</div>
								<div class="text-sm font-semibold text-ink">
									{{ roleLabel(invite.role) }}
								</div>
							</div>

							<div class="rounded-xl bg-slate-50 p-3">
								<div class="text-xs font-medium text-muted">
									Gültig bis
								</div>
								<div class="text-sm font-semibold text-ink">
									{{ formatDate(invite.expiresAt) }}
								</div>
							</div>
						</div>

						<p class="text-xs text-muted">
							Einladungslink ist einmalig nutzbar.
						</p>

						<div class="flex flex-wrap gap-2">
							<el-button
								type="primary"
								round
								:loading="acceptLoading"
								:disabled="!canAccept"
								@click="acceptInvite"
							>
								Haushalt beitreten
							</el-button>

							<el-button round @click="goHome">
								Abbrechen
							</el-button>
						</div>

						<p v-if="!canAccept" class="text-sm text-muted">
							Diese Einladung kann nicht mehr verwendet werden.
						</p>
					</div>
				</div>
			</div>

			<p class="text-xs text-slate-400">
				Tipp: Wenn die Einladung abgelaufen ist, bitte den Admin um
				einen neuen Link.
			</p>
		</div>
	</div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";

import api from "@/services/api";

import { useUserStore } from "@/stores/userStore";
import { useHouseholdStore } from "@/stores/householdStore";
import { Role } from "@/models/Role";
import { InvitePreview, InviteStatus } from "@/models/Invite";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const householdStore = useHouseholdStore();

const token = computed(() => String(route.query.token ?? "").trim() || "");
const isAuthenticated = computed(() => userStore.isAuthenticated);

const loading = ref(false);
const acceptLoading = ref(false);
const invite = ref<InvitePreview | null>(null);

const error = ref<null | {
	title: string;
	type: "error" | "warning" | "info";
	canRetry: boolean;
}>(null);

const canAccept = computed(() => {
	if (!invite.value) return false;
	if (invite.value.status !== InviteStatus.PENDING) return false;
	if (invite.value.remainingUses <= 0) return false;
	const exp = new Date(invite.value.expiresAt).getTime();
	return Date.now() < exp;
});

onMounted(async () => {
	if (!token.value) return;
	if (isAuthenticated.value) {
		await loadInvite();
	}
});

async function loadInvite() {
	if (!token.value) return;
	loading.value = true;
	error.value = null;

	try {
		const res = await api.get(
			`/invites/${encodeURIComponent(token.value)}`
		);
		invite.value = res.data;

		if (!invite.value.status) {
			invite.value.status = InviteStatus.PENDING;
		}
	} catch (e: any) {
		invite.value = null;

		const status = e?.response?.status;

		if (status === 404) {
			error.value = {
				title: "Einladung nicht gefunden oder ungültig.",
				type: "error",
				canRetry: false
			};
		} else if (status === 410) {
			error.value = {
				title: "Einladung ist abgelaufen.",
				type: "warning",
				canRetry: false
			};
		} else if (status === 409) {
			error.value = {
				title: "Einladung wurde bereits verwendet.",
				type: "warning",
				canRetry: false
			};
		} else if (status === 401) {
			error.value = {
				title: "Bitte melde dich an, um die Einladung zu sehen.",
				type: "info",
				canRetry: false
			};
		} else {
			error.value = {
				title: "Einladung konnte nicht geladen werden.",
				type: "error",
				canRetry: true
			};
		}
	} finally {
		loading.value = false;
	}
}

async function acceptInvite() {
	if (!token.value || !invite.value) return;

	acceptLoading.value = true;
	try {
		await api.post(
			`/invites/${encodeURIComponent(token.value)}/accept`,
			{}
		);

		ElMessage.success("Du bist dem Haushalt beigetreten.");

		if (!householdStore.activeHouseholdId) {
			await householdStore.selectHousehold(invite.value.householdId);
		}

		router.push({ path: "/lists" });
	} catch (e: any) {
		const status = e?.response?.status;
		if (status === 410) ElMessage.error("Einladung ist abgelaufen.");
		else if (status === 409)
			ElMessage.error("Einladung wurde bereits verwendet.");
		else ElMessage.error("Beitritt fehlgeschlagen.");
	} finally {
		acceptLoading.value = false;
	}
}

function goToLogin() {
	const redirect = `/join?token=${encodeURIComponent(token.value)}`;
	router.push({ path: "/login", query: { redirect } });
}

function goHome() {
	router.push({ path: "/" });
}

function roleLabel(role: Role) {
	if (role === "EDITOR") return "Kann bearbeiten";
	if (role === "VIEWER") return "Nur ansehen";
	if (role === "OWNER") return "Besitzer";
	return role;
}

function formatDate(iso: string) {
	try {
		return new Date(iso).toLocaleString();
	} catch {
		return iso;
	}
}
</script>
