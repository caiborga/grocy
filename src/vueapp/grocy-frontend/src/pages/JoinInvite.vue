<template>
    <div class="min-h-screen bg-gray-50">
        <div class="max-w-xl mx-auto p-4">
            <header class="bg-white rounded-2xl shadow-sm p-6 mb-4">
                <h1 class="text-2xl font-bold leading-tight">
                    Einladung zu einem Haushalt
                </h1>
                <p class="text-sm text-gray-500 mt-1">
                    Öffne die Einladung, um einem Haushalt beizutreten.
                </p>
            </header>

            <div class="bg-white rounded-2xl shadow-sm p-6">
                <!-- Missing token -->
                <div v-if="!token" class="space-y-3">
                    <p class="text-sm text-gray-700">
                        In diesem Link fehlt der Token.
                    </p>
                    <el-button type="primary" @click="goHome">Zur Startseite</el-button>
                </div>

                <!-- Not authenticated -->
                <div v-else-if="token && !isAuthenticated" class="space-y-4">
                    <p class="text-sm text-gray-700">
                        Du wurdest zu einem Haushalt eingeladen.
                    </p>

                    <div class="text-xs text-gray-500">
                        Bitte melde dich an (oder registriere dich), um die
                        Einladung anzunehmen.
                    </div>

                    <div class="flex gap-2">
                        <el-button type="primary" @click="goToLogin">
                            Anmelden / Registrieren
                        </el-button>
                        <el-button @click="goHome">Abbrechen</el-button>
                    </div>
                </div>

                <!-- Loading / Error / Preview (authenticated) -->
                <div v-else class="space-y-4">
                    <div v-if="loading" class="text-sm text-gray-600">
                        Einladung wird geladen…
                    </div>

                    <div v-else-if="error" class="space-y-3">
                        <el-alert :title="error.title" :type="error.type" show-icon :closable="false" />
                        <div class="flex gap-2">
                            <el-button v-if="error.canRetry" type="primary" @click="loadInvite">Erneut
                                versuchen</el-button>
                            <el-button @click="goHome">Zur Startseite</el-button>
                        </div>
                    </div>

                    <div v-else-if="invite" class="space-y-4">
                        <div>
                            <div class="text-sm text-gray-500">Haushalt</div>
                            <div class="text-lg font-semibold">
                                {{ invite.householdName }}
                            </div>
                        </div>

                        <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                            <div class="p-3 rounded-xl bg-gray-50">
                                <div class="text-xs text-gray-500">Rolle</div>
                                <div class="text-sm font-medium">
                                    {{ roleLabel(invite.role) }}
                                </div>
                            </div>

                            <div class="p-3 rounded-xl bg-gray-50">
                                <div class="text-xs text-gray-500">
                                    Gültig bis
                                </div>
                                <div class="text-sm font-medium">
                                    {{ formatDate(invite.expiresAt) }}
                                </div>
                            </div>
                        </div>

                        <div class="text-xs text-gray-500">
                            Einladungslink ist einmalig nutzbar.
                        </div>

                        <div class="flex gap-2">
                            <el-button type="primary" :loading="acceptLoading" :disabled="!canAccept"
                                @click="acceptInvite">
                                Haushalt beitreten
                            </el-button>

                            <el-button @click="goHome">Abbrechen</el-button>
                        </div>

                        <div v-if="!canAccept" class="text-sm text-gray-600">
                            Diese Einladung kann nicht mehr verwendet werden.
                        </div>
                    </div>
                </div>
            </div>

            <div class="text-xs text-gray-400 mt-4">
                Tipp: Wenn die Einladung abgelaufen ist, bitte den Admin um
                einen neuen Link.
            </div>
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
        // GET /api/invites/{token}
        const res = await api.get(
            `/invites/${encodeURIComponent(token.value)}`
        );
        invite.value = res.data;

        // Optional: falls Backend status nicht hart setzt
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
            // falls Preview doch auth-geschützt ist
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
        // POST /api/invites/{token}/accept
        await api.post(
            `/invites/${encodeURIComponent(token.value)}/accept`,
            {}
        );

        ElMessage.success("Du bist dem Haushalt beigetreten.");

        if (!householdStore.activeHouseholdId) {
            await householdStore.selectHousehold(invite.value.householdId);

        } else {
            // fallback: wenn du es anders machst
            // userStore.activeHouseholdId = invite.value.householdId
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
    if (role === "OWNER") return "Owner";
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
