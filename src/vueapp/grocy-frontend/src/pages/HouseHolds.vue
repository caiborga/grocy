<template>
    <div class="min-h-screen bg-gray-50">
        <div class="max-w-5xl mx-auto p-4">
            <!-- Header -->
            <header class="bg-white rounded-2xl shadow-sm p-4 mb-4">
                <div class="grid grid-cols-[1fr_auto] items-center gap-3">
                    <div>
                        <h1 class="text-2xl font-bold leading-tight">
                            Haushalte
                        </h1>

                        <p class="text-sm text-gray-500 mt-1">
                            Verwalte Haushalte & Mitglieder
                        </p>
                    </div>

                    <el-button type="primary" circle size="large" title="Neuer Haushalt" @click="openCreate = true">
                        <el-icon>
                            <Plus />
                        </el-icon>
                    </el-button>
                </div>
            </header>

            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <!-- Households -->
                <section class="md:col-span-1 bg-white rounded-2xl shadow-sm p-4">
                    <div class="flex items-center justify-between gap-3 mb-4">
                        <h2 class="text-lg font-bold">
                            Deine Haushalte
                        </h2>

                        <el-tag size="large" type="success" round>
                            {{ activeHouseholds.length }}
                        </el-tag>
                    </div>

                    <div v-if="activeHouseholds.length === 0" class="text-center text-gray-500 py-10">
                        <div class="text-lg font-semibold mb-1">
                            Keine Haushalte
                        </div>

                        <div class="text-sm">
                            Erstelle oben rechts deinen ersten Haushalt.
                        </div>
                    </div>

                    <el-menu v-else :default-active="selected?.id" class="household-menu space-y-2"
                        @select="selectHouseholdById">
                        <el-menu-item v-for="h in activeHouseholds" :key="h.id" :index="h.id"
                            class="household-menu-item">
                            <span class="truncate">
                                {{ householdName(h) }}
                            </span>
                        </el-menu-item>
                    </el-menu>
                </section>

                <!-- Details -->
                <section class="md:col-span-2 bg-white rounded-2xl shadow-sm p-4">
                    <div class="grid grid-cols-[1fr_auto] items-start gap-3 mb-4">
                        <div>
                            <h2 class="text-xl font-bold leading-tight">
                                {{ selectedName }}
                            </h2>

                            <p v-if="selected" class="text-sm text-gray-500 mt-1">
                                Haushalt verwalten
                            </p>
                        </div>

                        <div class="flex items-center justify-end">
                            <el-button v-if="can(PERM.HOUSEHOLD_RENAME)" circle plain :disabled="!selected"
                                title="Haushalt umbenennen" @click="openRenameDialog">
                                <el-icon :size="18">
                                    <Edit />
                                </el-icon>
                            </el-button>

                            <el-button v-if="can(PERM.HOUSEHOLD_DELETE)" circle size="default" plain
                                title="Haushalt löschen" class="!ml-1" @click="deleteHousehold"
                                :disabled="!selected || activeHouseholds.length === 1">
                                <el-icon :size="14" class="text-red-500">
                                    <Delete />
                                </el-icon>
                            </el-button>
                        </div>
                    </div>

                    <div v-if="!selected" class="text-center text-gray-500 py-10">
                        <div class="text-lg font-semibold mb-1">
                            Wähle links einen Haushalt
                        </div>
                    </div>

                    <template v-else>
                        <el-tabs v-model="activeTab" type="border-card" class="household-tabs">
                            <!-- Lists -->
                            <el-tab-pane name="lists">
                                <template #label>
                                    <span class="custom-tabs-label">
                                        <el-icon>
                                            <List />
                                        </el-icon>
                                        <span>Listen</span>
                                    </span>
                                </template>

                                <div v-if="lists.length === 0" class="text-center text-gray-500 py-10">
                                    <div class="text-lg font-semibold mb-1">
                                        Keine Listen
                                    </div>

                                    <div class="text-sm">
                                        Keine Listen in diesem Haushalt.
                                    </div>
                                </div>

                                <div v-else class="space-y-3">
                                    <el-card v-for="l in lists" :key="l.id" shadow="never" class="list-card"
                                        @click="goToList(l.id)">
                                        <div class="grid grid-cols-[1fr_auto] items-center gap-3">
                                            <div class="min-w-0">
                                                <div class="flex items-center gap-2 min-w-0">
                                                    <span class="font-semibold truncate text-gray-900">
                                                        {{ l.title ?? 'Liste' }}
                                                    </span>

                                                    <el-icon v-if="l.isDefault" :size="18" class="text-yellow-500"
                                                        title="Default-Liste">
                                                        <StarFilled />
                                                    </el-icon>
                                                </div>

                                                <p v-if="l.stats" class="text-sm text-gray-500 mt-1">
                                                    {{ l.stats.checked }} / {{ l.stats.total }} erledigt
                                                </p>
                                            </div>

                                            <el-dropdown v-if="can(PERM.LIST_EDIT)" trigger="click" @click.stop>
                                                <el-button text title="Optionen" @click.stop>
                                                    <el-icon>
                                                        <MoreFilled />
                                                    </el-icon>
                                                </el-button>

                                                <template #dropdown>
                                                    <el-dropdown-menu>
                                                        <el-dropdown-item @click="openEditList(l)">
                                                            Bearbeiten
                                                        </el-dropdown-item>

                                                        <el-dropdown-item class="text-red-500" :disabled="l.isDefault"
                                                            @click="confirmDeleteList(l)">
                                                            Löschen
                                                        </el-dropdown-item>
                                                    </el-dropdown-menu>
                                                </template>
                                            </el-dropdown>
                                        </div>
                                    </el-card>
                                </div>

                                <el-card v-if="can(PERM.LIST_CREATE)" shadow="never"
                                    class="list-card household-menu-item cursor-pointer mt-3"
                                    @click="openCreateList = true">
                                    <div class="flex items-center justify-center">
                                        <el-icon>
                                            <Plus />
                                        </el-icon>
                                    </div>
                                </el-card>
                            </el-tab-pane>

                            <!-- Members -->
                            <el-tab-pane name="members">
                                <template #label>
                                    <span class="custom-tabs-label">
                                        <el-icon>
                                            <User />
                                        </el-icon>
                                        <span>Mitglieder</span>
                                    </span>
                                </template>

                                <div v-if="members.length === 0" class="text-center text-gray-500 py-10">
                                    <div class="text-lg font-semibold mb-1">
                                        Keine Mitglieder
                                    </div>

                                    <div class="text-sm">
                                        Keine Mitglieder gefunden.
                                    </div>
                                </div>

                                <el-table v-else :data="members" stripe style="width: 100%">
                                    <el-table-column prop="displayName" label="Name" min-width="180">
                                        <template #default="{ row }">
                                            <span class="font-medium">
                                                {{ row.displayName ?? 'User' }}
                                            </span>
                                        </template>
                                    </el-table-column>

                                    <el-table-column prop="role" label="Rolle" width="150">
                                        <template #default="{ row }">
                                            <el-select v-model="row.role" size="small" style="width: 130px"
                                                :disabled="!can(PERM.MEMBER_INVITE) || userIsOwner(row.role)"
                                                @change="(val) => updateRole(row, val)">
                                                <el-option v-if="row.role === 'OWNER'" label="Besitzer" value="OWNER" />

                                                <el-option v-for="r in selectableRoles" :key="r.value" :label="r.label"
                                                    :value="r.value" />
                                            </el-select>
                                        </template>
                                    </el-table-column>

                                    <el-table-column v-if="can(PERM.MEMBER_DELETE)" label="" width="50" align="center">
                                        <template #default="{ row }">
                                            <el-button circle plain size="small" title="Mitglied entfernen"
                                                :disabled="!can(PERM.MEMBER_DELETE) || userIsOwner(row.role)"
                                                @click="removeMember(row)">
                                                <el-icon class="text-red-500">
                                                    <Delete />
                                                </el-icon>
                                            </el-button>
                                        </template>
                                    </el-table-column>
                                </el-table>

                                <el-card v-if="can(PERM.MEMBER_INVITE)" shadow="never"
                                    class="list-card household-menu-item cursor-pointer mt-3"
                                    @click="openInvite = true">
                                    <div class="flex items-center justify-center">
                                        <el-icon>
                                            <Plus />
                                        </el-icon>
                                    </div>
                                </el-card>
                            </el-tab-pane>
                        </el-tabs>
                    </template>

                    <el-alert v-if="error" class="mt-4" type="error" :title="error" show-icon :closable="false" />
                </section>
            </div>

            <!-- Create household dialog -->
            <el-dialog v-model="openCreate" title="Neuer Haushalt" width="92%" class="max-w-[420px]" align-center>
                <el-form label-position="top">
                    <el-form-item label="Name">
                        <el-input v-model="createName" placeholder="z. B. Zuhause" size="large"
                            @keyup.enter="createHousehold" />
                    </el-form-item>
                </el-form>

                <template #footer>
                    <el-button @click="openCreate = false">
                        Abbrechen
                    </el-button>

                    <el-button type="primary" :loading="createLoading" :disabled="isBlank(createName)"
                        @click="createHousehold">
                        Erstellen
                    </el-button>
                </template>
            </el-dialog>

            <!-- Rename household dialog -->
            <el-dialog v-model="openRename" title="Haushalt umbenennen" width="92%" class="max-w-[420px]" align-center>
                <el-form label-position="top">
                    <el-form-item label="Neuer Name">
                        <el-input v-model="renameName" placeholder="Neuer Name" size="large"
                            @keyup.enter="renameHousehold" />
                    </el-form-item>
                </el-form>

                <template #footer>
                    <el-button @click="openRename = false">
                        Abbrechen
                    </el-button>

                    <el-button type="primary" :loading="renameLoading" :disabled="isBlank(renameName)"
                        @click="renameHousehold">
                        Speichern
                    </el-button>
                </template>
            </el-dialog>

            <!-- Invite dialog -->
            <el-dialog v-model="openInvite" title="Mitglied hinzufügen" width="92%" class="max-w-[480px]" align-center>
                <p class="text-sm text-gray-500 mb-3">
                    Erstelle einen Einladungslink. Der Link ist einmalig nutzbar und 2 Stunden gültig.
                </p>

                <el-form label-position="top">
                    <el-form-item label="Rolle">
                        <el-select v-model="inviteRole" size="large" class="!w-full">
                            <el-option label="Kann bearbeiten (Editor)" value="EDITOR" />
                            <el-option label="Nur ansehen (Betrachter)" value="VIEWER" />
                        </el-select>
                    </el-form-item>

                    <el-form-item v-if="inviteUrl" label="Einladungslink">
                        <el-input v-model="inviteUrl" readonly size="large" />
                        <p class="text-xs text-gray-500 mt-1">
                            Gültig bis: {{ inviteExpiresAt }}
                        </p>
                    </el-form-item>
                </el-form>

                <template #footer>
                    <el-button @click="closeInviteDialog">
                        Schließen
                    </el-button>

                    <el-button v-if="!inviteUrl" type="primary" :loading="inviteLoading" @click="createInviteLink">
                        Link erstellen
                    </el-button>

                    <el-button v-else type="primary" @click="copyInviteLink">
                        Link kopieren
                    </el-button>
                </template>
            </el-dialog>

            <!-- Create list dialog -->
            <el-dialog v-model="openCreateList" title="Neue Liste" width="92%" class="max-w-[420px]" align-center>
                <el-form label-position="top">
                    <el-form-item label="Name der Liste">
                        <el-input v-model="createListName" placeholder="z. B. Wocheneinkauf" size="large"
                            @keyup.enter="createList" />
                    </el-form-item>
                </el-form>

                <template #footer>
                    <el-button @click="openCreateList = false">
                        Abbrechen
                    </el-button>

                    <el-button type="primary" :loading="createListLoading" :disabled="isBlank(createListName)"
                        @click="createList">
                        Erstellen
                    </el-button>
                </template>
            </el-dialog>

            <!-- Edit list dialog -->
            <el-dialog v-model="openEditListDialog" title="Liste bearbeiten" width="92%" class="max-w-[420px]"
                align-center>
                <el-form label-position="top">
                    <el-form-item label="Name">
                        <el-input v-model="editListTitle" size="large" @keyup.enter="saveListEdit" />
                    </el-form-item>
                </el-form>

                <template #footer>
                    <el-button @click="openEditListDialog = false">
                        Abbrechen
                    </el-button>

                    <el-button type="primary" :loading="editListLoading" :disabled="isBlank(editListTitle)"
                        @click="saveListEdit">
                        Speichern
                    </el-button>
                </template>
            </el-dialog>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { ElLoading, ElMessage, ElMessageBox } from "element-plus";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/userStore";
import { useHouseholdStore } from "@/stores/householdStore";
import { usePermissions } from "@/composables/permissionComp";
import { PERM } from "@/auth/perms";
import { householdService } from "@/services/householdService";
import { householdMemberService } from "@/services/householdMemberService";
import { inviteService } from "@/services/inviteService";
import { listService } from "@/services/listService";

const { can, userIsOwner } = usePermissions();
const router = useRouter();
const householdStore = useHouseholdStore();
const userStore = useUserStore();

const loading = ref(false);
let loadingInstance = null;

const error = ref("");
const activeTab = ref("lists");

const households = ref([]);
const activeHouseholds = ref([]);
const selected = ref(null);

const members = ref([]);
const lists = ref([]);

const openCreate = ref(false);
const createName = ref("");
const createLoading = ref(false);

const openRename = ref(false);
const renameName = ref("");
const renameLoading = ref(false);

const openInvite = ref(false);
const inviteLoading = ref(false);
const inviteRole = ref("EDITOR");
const inviteUrl = ref("");
const inviteExpiresAt = ref("");

const openCreateList = ref(false);
const createListName = ref("");
const createListLoading = ref(false);

const openEditListDialog = ref(false);
const editListLoading = ref(false);
const editListId = ref(null);
const editListTitle = ref("");

const selectableRoles = computed(() => [
    { label: "Bearbeiter", value: "EDITOR" },
    { label: "Betrachter", value: "VIEWER" }
]);

const selectedName = computed(() => {
    if (!selected.value) return "Kein Haushalt ausgewählt";
    return householdName(selected.value);
});

onMounted(async () => {
    await getHouseholds();

    let active = householdStore.activeHousehold;

    if (!active && activeHouseholds.value.length > 0) {
        active = activeHouseholds.value[0];
    }

    if (active) {
        await selectHousehold(active);
    }
});

function goToList(id) {
    router.push(`/lists/${id}`);
}

function toggleLoading() {
    loading.value = !loading.value;

    if (loading.value) {
        loadingInstance = ElLoading.service({
            lock: true,
            text: "Lade Haushalte..."
        });
    } else {
        loadingInstance?.close();
        loadingInstance = null;
    }
}

function isBlank(value) {
    return !value || value.trim().length === 0;
}

function householdName(household) {
    return household?.name ?? household?.title ?? "Haushalt";
}

function getActiveHouseholds(value) {
    return (value ?? []).filter((hh) => hh && !hh.archived);
}

function selectHouseholdById(id) {
    const household = activeHouseholds.value.find((h) => h.id === id);
    if (!household) return;
    selectHousehold(household);
}

async function selectHousehold(household) {
    if (!household?.id) return;

    toggleLoading();

    inviteUrl.value = "";
    inviteExpiresAt.value = "";
    selected.value = household;

    try {
        await householdStore.selectHousehold(household.id);
        await userStore.loadMe();
        await getHousehold(household);
    } catch (e) {
        console.error(e);
        ElMessage.error("Haushalt konnte nicht ausgewählt werden.");
    } finally {
        toggleLoading();
    }
}

async function getHouseholds() {
    toggleLoading();
    error.value = "";

    try {
        const response = await householdService.getAll();
        households.value = Array.isArray(response.data)
            ? response.data
            : (response.data ?? []);
        activeHouseholds.value = getActiveHouseholds(households.value);
    } catch (e) {
        console.error(e);
        error.value = "Haushalte konnten nicht geladen werden.";
    } finally {
        toggleLoading();
    }
}

async function getHousehold(household) {
    if (!household?.id) return;

    error.value = "";

    try {
        const response = await householdService.getById(household.id);
        members.value = Array.isArray(response.data?.members)
            ? response.data.members
            : [];
        lists.value = Array.isArray(response.data?.lists)
            ? response.data.lists
            : [];
    } catch (e) {
        console.error(e);
        error.value = "Haushalt konnte nicht geladen werden.";
    }
}

async function createHousehold() {
    const name = createName.value.trim();
    if (!name) return;

    toggleLoading();
    createLoading.value = true;

    try {
        const response = await householdService.create({ name });
        const created = response.data?.data ?? response.data;

        households.value.unshift(created);
        activeHouseholds.value = getActiveHouseholds(households.value);

        openCreate.value = false;
        createName.value = "";

        await getHouseholds();
        await selectHousehold(created);

        ElMessage.success("Haushalt erstellt");
    } catch (e) {
        console.error(e);
        ElMessage.error("Erstellen fehlgeschlagen");
    } finally {
        createLoading.value = false;
        toggleLoading();
    }
}

function openRenameDialog() {
    if (!selected.value) return;
    renameName.value = householdName(selected.value);
    openRename.value = true;
}

async function renameHousehold() {
    const id = selected.value?.id;
    const name = renameName.value.trim();
    if (!id || !name) return;

    toggleLoading();
    renameLoading.value = true;

    try {
        const response = await householdService.rename(id, {
            name,
            archived: selected.value.archived
        });

        const updated = response.data?.data ?? response.data;
        const idx = households.value.findIndex((h) => h?.id === id);

        if (idx !== -1) {
            households.value[idx] = { ...households.value[idx], ...updated };
        }

        selected.value = { ...selected.value, ...updated };
        activeHouseholds.value = getActiveHouseholds(households.value);

        if (householdStore.activeHouseholdId === id && householdStore.activeHousehold) {
            householdStore.activeHousehold = {
                ...householdStore.activeHousehold,
                ...updated
            };
        }

        openRename.value = false;
        ElMessage.success("Umbenannt");
    } catch (e) {
        console.error(e);
        ElMessage.error("Umbenennen fehlgeschlagen");
    } finally {
        renameLoading.value = false;
        toggleLoading();
    }
}

async function deleteHousehold() {
    if (!selected.value?.id) return;

    const id = selected.value.id;
    const name = householdName(selected.value);

    try {
        await ElMessageBox.confirm(
            `"${name}" wirklich löschen?`,
            "Haushalt löschen",
            {
                confirmButtonText: "Löschen",
                cancelButtonText: "Abbrechen",
                type: "warning"
            }
        );
    } catch {
        return;
    }

    toggleLoading();

    try {
        await householdService.archive(id, {
            name,
            archived: true
        });

        households.value = households.value.filter((h) => h?.id !== id);
        activeHouseholds.value = getActiveHouseholds(households.value);

        selected.value = null;
        members.value = [];
        lists.value = [];

        const nextHousehold = activeHouseholds.value[activeHouseholds.value.length - 1];
        if (nextHousehold) {
            await selectHousehold(nextHousehold);
        }

        ElMessage.success("Gelöscht");
    } catch (e) {
        console.error(e);
        ElMessage.error("Löschen fehlgeschlagen");
    } finally {
        toggleLoading();
    }
}

async function createList() {
    const title = createListName.value.trim();
    if (!selected.value?.id || !title) return;

    toggleLoading();
    createListLoading.value = true;

    try {
        await householdService.createList(selected.value.id, { title });

        openCreateList.value = false;
        createListName.value = "";

        await getHousehold(selected.value);
        ElMessage.success("Liste erstellt");
    } catch (e) {
        console.error(e);
        ElMessage.error("Liste konnte nicht erstellt werden");
    } finally {
        createListLoading.value = false;
        toggleLoading();
    }
}

function openEditList(list) {
    editListId.value = list.id;
    editListTitle.value = (list.title ?? list.name ?? "").toString();
    openEditListDialog.value = true;
}

async function saveListEdit() {
    const title = editListTitle.value.trim();
    if (!editListId.value || !title) return;

    toggleLoading();
    editListLoading.value = true;

    try {
        await listService.patchList(editListId.value, { title });
        openEditListDialog.value = false;
        await getHousehold(selected.value);
        ElMessage.success("Liste gespeichert");
    } catch (e) {
        console.error(e);
        ElMessage.error("Speichern fehlgeschlagen");
    } finally {
        editListLoading.value = false;
        toggleLoading();
    }
}

async function confirmDeleteList(list) {
    try {
        await ElMessageBox.confirm(
            `"${list.title ?? list.name ?? 'Liste'}" wirklich löschen?`,
            "Liste löschen",
            {
                confirmButtonText: "Löschen",
                cancelButtonText: "Abbrechen",
                type: "warning"
            }
        );
    } catch {
        return;
    }

    toggleLoading();

    try {
        await listService.deleteList(list.id);
        await getHousehold(selected.value);
        ElMessage.success("Liste gelöscht");
    } catch (e) {
        console.error(e);
        ElMessage.error("Löschen fehlgeschlagen");
    } finally {
        toggleLoading();
    }
}

async function updateRole(row, newRole) {
    if (!selected.value?.id || !row?.id) return;

    const oldRole = row.role;
    row.role = newRole;

    toggleLoading();

    try {
        await householdMemberService.updateRole(selected.value.id, row.userId, {
            role: newRole,
            actingUserId: userStore.me.id
        });

        ElMessage.success("Rolle aktualisiert");
    } catch (e) {
        console.error(e);
        row.role = oldRole;
        ElMessage.error("Rolle konnte nicht gespeichert werden");
    } finally {
        toggleLoading();
    }
}

async function removeMember(row) {
    if (!selected.value?.id || !row?.id) return;

    try {
        await ElMessageBox.confirm(
            "Mitglied wirklich entfernen?",
            "Mitglied entfernen",
            {
                confirmButtonText: "Entfernen",
                cancelButtonText: "Abbrechen",
                type: "warning"
            }
        );
    } catch {
        return;
    }

    const before = [...members.value];
    members.value = members.value.filter((m) => m?.id !== row.id);

    toggleLoading();

    try {
        await householdMemberService.remove(selected.value.id, row.id);
        ElMessage.success("Mitglied entfernt");
    } catch (e) {
        console.error(e);
        members.value = before;
        ElMessage.error("Entfernen fehlgeschlagen");
    } finally {
        toggleLoading();
    }
}

async function createInviteLink() {
    const householdId = householdStore.activeHousehold?.id;

    if (!householdId) {
        ElMessage.error("Kein Haushalt ausgewählt.");
        return;
    }

    toggleLoading();
    inviteLoading.value = true;

    try {
        const res = await inviteService.create(householdId, {
            role: inviteRole.value
        });

        inviteUrl.value = res.data.url;
        inviteExpiresAt.value = formatExpires(res.data.expiresAt);

        ElMessage.success("Einladungslink erstellt.");
    } catch (e) {
        console.error(e);
        ElMessage.error("Invite-Link konnte nicht erstellt werden.");
    } finally {
        inviteLoading.value = false;
        toggleLoading();
    }
}

async function copyInviteLink() {
    try {
        await navigator.clipboard.writeText(inviteUrl.value);
        ElMessage.success("Link kopiert.");
    } catch (e) {
        console.error(e);
        ElMessage.error("Kopieren nicht möglich.");
    }
}

function closeInviteDialog() {
    openInvite.value = false;
    inviteRole.value = "EDITOR";
    inviteUrl.value = "";
    inviteExpiresAt.value = "";
}

function formatExpires(expiresAtIso) {
    try {
        return new Date(expiresAtIso).toLocaleString();
    } catch {
        return expiresAtIso;
    }
}
</script>

<style scoped>
.household-menu {
    border-right: 0;
}

.household-menu :deep(.el-menu-item) {
    border-radius: 0.75rem;
}

.household-tabs {
    border-radius: 1rem;
    overflow: hidden;
}

.household-menu-item {
    border: 1px solid rgb(243 244 246);
    background-color: rgb(249 250 251);
}

.household-menu-item:hover,
.list-card:hover {
    background-color: rgb(239 246 255);
}

.household-menu :deep(.el-menu-item.is-active) {
    border-color: rgb(191 219 254);
    background-color: rgba(219, 234, 254, 0.716);
}

.custom-tabs-label {
    display: inline-flex;
    align-items: center;
    gap: 0.35rem;
}

.list-card {
    cursor: pointer;
    border-radius: 1rem;
    transition: background-color 0.15s ease, border-color 0.15s ease;
}
</style>
