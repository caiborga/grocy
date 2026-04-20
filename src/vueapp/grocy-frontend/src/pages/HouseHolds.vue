<template>
    <div class="min-h-screen bg-gray-50">
        <div class="max-w-5xl mx-auto p-4">
            <div class="flex items-center justify-between mb-4">
                <div>
                    <h1 class="text-2xl font-bold">Haushalte</h1>
                    <p class="text-sm text-gray-500">
                        Verwalte Haushalte & Mitglieder
                    </p>
                </div>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <!-- Left: Households list -->
                <div class="md:col-span-1">
                    <div class="bg-white rounded-2xl shadow-sm border border-gray-100">
                        <div class="p-4 border-b border-gray-100 flex items-center justify-between gap-3">
                            <span class="text-xl font-bold">Deine Haushalte</span>
                            <el-tag size="large" type="success" class="px-5">{{
                                activeHouseholds.length
                            }}</el-tag>
                        </div>

                        <div v-if="activeHouseholds.length === 0" class="p-6 text-center text-gray-500">
                            <div class="font-semibold mb-1">
                                Keine Haushalte
                            </div>
                            <div class="text-sm">
                                Erstelle rechts oben einen neuen Haushalt.
                            </div>
                        </div>

                        <div v-else class="divide-y">
                            <button v-for="h in activeHouseholds" :key="h.id"
                                class="w-full text-left px-4 py-3 flex items-start gap-3" :class="selected?.id === h.id
                                    ? 'bg-blue-100'
                                    : 'hover:bg-blue-50'
                                    " @click="selectHousehold(h)">
                                <div class="min-w-0 flex-1">
                                    <div class="font-medium truncate">
                                        {{ h.name ?? h.title ?? "Haushalt" }}
                                    </div>
                                </div>
                            </button>

                        </div>
                    </div>
                </div>

                <!-- Right: Selected household details -->
                <div class="md:col-span-2">
                    <div class="bg-white rounded-2xl shadow-sm border border-gray-100">
                        <div class="p-4 border-b border-gray-100 flex items-start justify-between gap-3">
                            <div>
                                <div class="text-xl font-bold">
                                    {{ selectedName }}
                                </div>
                            </div>

                            <div class="flex">
                                <el-button v-if="can(PERM.HOUSEHOLD_RENAME)" type="primary" plain
                                    @click="openRename = true">
                                    <el-icon>
                                        <Edit />
                                    </el-icon>
                                </el-button>

                                <el-button v-if="can(PERM.HOUSEHOLD_DELETE)"
                                    :disabled="!selected || activeHouseholds.length === 1" type="danger" plain
                                    @click="deleteHousehold">
                                    <el-icon>
                                        <Delete />
                                    </el-icon>
                                </el-button>
                                <el-button type="primary" @click="openCreate = true">
                                    <el-icon>
                                        <Plus />
                                    </el-icon>
                                </el-button>
                            </div>
                        </div>

                        <div v-if="!selected" class="p-8 text-center text-gray-500">
                            <div class="font-semibold mb-1">
                                Wähle links einen Haushalt
                            </div>
                        </div>

                        <div>
                            <el-tabs type="border-card" class="demo-tabs">
                                <!-- LISTS -->
                                <el-tab-pane><template #label>
                                        <span class="custom-tabs-label">
                                            <el-icon>
                                                <List />
                                            </el-icon>
                                            <span>Listen</span>
                                        </span>
                                    </template>
                                    <div>
                                        <div class="flex items-center justify-between mb-3">
                                            <div class="font-semibold">
                                                Listen
                                            </div>
                                            <div class="flex items-center justify-between mb-3"></div>
                                        </div>

                                        <div v-if="listsLoading" class="text-gray-600">
                                            Lade Listen…
                                        </div>

                                        <div v-else-if="lists.length === 0" class="text-gray-500">
                                            Keine Listen in diesem Haushalt.
                                        </div>

                                        <div v-else class="grid grid-cols-1 sm:grid-cols-1 gap-3">
                                            <div v-for="l in lists" :key="l.id" @click="goToList(l.id)"
                                                class="border border-gray-100 rounded-2xl p-4 hover:bg-gray-50 cursor-pointer">
                                                <div class="flex items-center justify-between gap-4">
                                                    <!-- Left: Title -->
                                                    <div class="min-w-0 flex-1">
                                                        <div class="font-semibold truncate text-gray-900">
                                                            {{
                                                                l.title ??
                                                                "Liste"
                                                            }}
                                                            <!-- Default Star -->
                                                            <el-icon v-if="
                                                                l.isDefault
                                                            " :size="22" class="text-yellow-500" title="Default-Liste">
                                                                <StarFilled />
                                                            </el-icon>
                                                        </div>
                                                    </div>

                                                    <!-- Right: Star + Actions -->
                                                    <div v-if="can(PERM.LIST_EDIT)" class="flex items-center">
                                                        <!-- Edit -->
                                                        <el-button circle size="default" plain @click.stop="
                                                            openEditList(l)
                                                            " title="Liste bearbeiten">
                                                            <el-icon :size="18">
                                                                <Edit />
                                                            </el-icon>
                                                        </el-button>

                                                        <!-- Delete -->
                                                        <el-button circle size="default" type="danger" plain
                                                            :disabled="l.isDefault" @click.stop="
                                                                confirmDeleteList(
                                                                    l
                                                                )
                                                                " title="Liste löschen">
                                                            <el-icon :size="18">
                                                                <Delete />
                                                            </el-icon>
                                                        </el-button>
                                                    </div>
                                                </div>

                                                <!-- Stats -->
                                                <div v-if="l.stats" class="mt-3 text-sm text-gray-600">
                                                    {{ l.stats.checked }} /
                                                    {{ l.stats.total }} erledigt
                                                </div>
                                            </div>

                                            <div v-if="can(PERM.LIST_CREATE)" @click="openCreateList = true"
                                                class="border border-gray-100 rounded-2xl p-4 hover:bg-gray-50 cursor-pointer bg-blue-50">
                                                <div class="flex items-center justify-center h-full">
                                                    <el-icon>
                                                        <Plus />
                                                    </el-icon>
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                    <div v-if="error" class="mt-4 text-sm text-red-600">
                                        {{ error }}
                                    </div>
                                </el-tab-pane>
                                <!-- MEMBERS -->
                                <el-tab-pane>
                                    <template #label>
                                        <span class="custom-tabs-label">
                                            <el-icon>
                                                <User />
                                            </el-icon>
                                            <span>Mitglieder</span>
                                        </span>
                                    </template>
                                    <!-- Members -->
                                    <div class="flex items-center justify-between mb-3">
                                        <div class="font-semibold">
                                            Mitglieder
                                        </div>
                                        <div class="flex gap-2">
                                            <el-button @click="openInvite = true" v-if="can(PERM.MEMBER_INVITE)"
                                                type="primary" size="small" plain>
                                                Mitglied hinzufügen
                                            </el-button>
                                        </div>
                                    </div>

                                    <div v-if="membersLoading" class="text-gray-600">
                                        Lade Mitglieder…
                                    </div>

                                    <div v-else-if="members.length === 0" class="text-gray-500">
                                        Keine Mitglieder gefunden.
                                    </div>

                                    <el-table v-else :data="members" style="width: 100%" stripe>
                                        <el-table-column prop="displayName" label="Name" min-width="180">
                                            <template #default="{ row }">
                                                <div class="font-medium">
                                                    {{
                                                        row.displayName ??
                                                        "User"
                                                    }}
                                                </div>
                                            </template>
                                        </el-table-column>



                                        <el-table-column prop="role" label="Rolle" width="140">

                                            <template #default="{ row }">
                                                <el-select v-model="row.role" size="small"
                                                    :disabled="!can(PERM.MEMBER_INVITE) || userIsOwner(row.role)"
                                                    @change="val => updateRole(row, val)" style="width: 120px">
                                                    <!-- Aktueller Wert (OWNER) anzeigen, aber nicht im Dropdown -->
                                                    <el-option v-if="row.role === 'OWNER'" label="Besitzer"
                                                        value="OWNER" />

                                                    <!-- Nur auswählbare Rollen -->
                                                    <el-option v-for="r in selectableRoles" :key="r.value"
                                                        :label="r.label" :value="r.value" />
                                                </el-select>
                                            </template>
                                        </el-table-column>

                                        <el-table-column v-if="can(PERM.MEMBER_DELETE)" label="" width="120"
                                            align="right">
                                            <template #default="{ row }">
                                                <el-button type="danger" plain size="small"
                                                    :disabled="!can(PERM.MEMBER_DELETE) || userIsOwner(row.role)"
                                                    @click="removeMember(row)">
                                                    Entfernen
                                                </el-button>
                                            </template>
                                        </el-table-column>
                                    </el-table>
                                </el-tab-pane>
                            </el-tabs>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Create household dialog -->
            <el-dialog v-model="openCreate" title="Neuer Haushalt" width="420px">
                <el-form label-position="top">
                    <el-form-item label="Name">
                        <el-input v-model="createName" placeholder="z. B. Zuhause" />
                    </el-form-item>
                </el-form>

                <template #footer>
                    <el-button @click="openCreate = false">Abbrechen</el-button>
                    <el-button type="primary" :loading="createLoading" @click="createHousehold"
                        :disabled="isValueValid(createName)">
                        Erstellen
                    </el-button>
                </template>
            </el-dialog>
            <!-- Rename dialog -->
            <el-dialog v-model="openRename" title="Haushalt umbenennen" width="420px">
                <el-form label-position="top">
                    <el-form-item label="Neuer Name">
                        <el-input v-model="renameName" placeholder="Neuer Name" />
                    </el-form-item>
                </el-form>

                <template #footer>
                    <el-button @click="openRename = false">Abbrechen</el-button>
                    <el-button type="primary" :loading="renameLoading" @click="renameHousehold"
                        :disabled="isValueValid(renameName)">
                        Speichern
                    </el-button>
                </template>
            </el-dialog>

            <!-- Invite/Add member dialog (optional) -->
            <el-dialog v-model="openInvite" title="Mitglied hinzufügen" width="480px">
                <div class="text-sm text-gray-500 mb-3">
                    Erstelle einen Einladungslink (einmalig nutzbar, gültig 2
                    Stunden).
                </div>

                <el-form label-position="top">
                    <el-form-item label="Rolle">
                        <el-select v-model="inviteRole" style="width: 100%">
                            <el-option label="Kann bearbeiten (Editor)" value="EDITOR" />
                            <el-option label="Nur ansehen (Betrachter)" value="VIEWER" />
                        </el-select>
                    </el-form-item>

                    <el-form-item v-if="inviteUrl" label="Einladungslink">
                        <el-input v-model="inviteUrl" readonly />
                        <div class="text-xs text-gray-500 mt-1">
                            Gültig bis: {{ inviteExpiresAt }}
                        </div>
                    </el-form-item>
                </el-form>

                <template #footer>
                    <el-button @click="closeInviteDialog">Schließen</el-button>

                    <el-button v-if="!inviteUrl" type="primary" :loading="inviteLoading" @click="createInviteLink">
                        Link erstellen
                    </el-button>

                    <el-button v-else type="primary" @click="copyInviteLink">
                        Link kopieren
                    </el-button>
                </template>
            </el-dialog>

            <!-- Create list dialog -->
            <el-dialog v-model="openCreateList" title="Neue Liste" width="420px">
                <el-form label-position="top">
                    <el-form-item label="Name der Liste">
                        <el-input v-model="createListName" placeholder="z. B. Wocheneinkauf"
                            @keyup.enter="createList" />
                    </el-form-item>
                </el-form>

                <template #footer>
                    <el-button @click="openCreateList = false">
                        Abbrechen
                    </el-button>
                    <el-button type="primary" :loading="createListLoading" @click="createList"
                        :disabled="isValueValid(createListName)">
                        Erstellen
                    </el-button>
                </template>
            </el-dialog>

            <!-- Edit list dialog -->
            <el-dialog v-model="openEditListDialog" title="Liste bearbeiten" width="420px">
                <el-form label-position="top">
                    <el-form-item label="Name">
                        <el-input v-model="editListTitle" @keyup.enter="saveListEdit" />
                    </el-form-item>
                </el-form>

                <template #footer>
                    <el-button @click="openEditListDialog = false">Abbrechen</el-button>
                    <el-button type="primary" :loading="editListLoading" @click="saveListEdit"
                        :disabled="isValueValid(editListTitle)">
                        Speichern
                    </el-button>
                </template>
            </el-dialog>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { ElLoading, ElMessage, ElMessageBox, ElTabs } from "element-plus";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/userStore";
import { useHouseholdStore } from "@/stores/householdStore";

import { usePermissions } from "@/composables/permissionComp";
import { PERM } from "@/auth/perms";
import { householdService } from "@/services/householdService";
import { householdMemberService } from "@/services/householdMemberService";
import { inviteService } from "@/services/inviteService";
import { listService } from "@/services/listService";

const { can, isOwner, role, userIsOwner } = usePermissions();

let loadingInstance = null;

const router = useRouter();

const loading = ref(false);
const error = ref("");

const households = ref([]);
const selected = ref(null);

const members = ref([]);
const membersLoading = ref(false);

const lists = ref([]);
const listsLoading = ref(false);

const activeHouseholds = ref([]);

// stores
const householdStore = useHouseholdStore();
const userStore = useUserStore();

// dialogs
const openCreate = ref(false);
const createName = ref("");
const createLoading = ref(false);

const openRename = ref(false);
const renameName = ref("");
const renameLoading = ref(false);

const openEditListDialog = ref(false);
const editListLoading = ref(false);
const editListId = ref(null);
const editListTitle = ref("");

// create list
const openCreateList = ref(false);
const createListName = ref("");
const createListLoading = ref(false);

const openInvite = ref(false);
const inviteLoading = ref(false);
const inviteRole = ref("EDITOR");
const inviteUrl = ref("");
const inviteExpiresAt = ref("");

const selectableRoles = computed(() => {
    return [
        { label: "Bearbeiter", value: "EDITOR" },
        { label: "Betrachter", value: "VIEWER" }
    ];
});

const selectedName = computed(() => {
    if (!selected.value) return "Kein Haushalt ausgewählt";
    return selected.value.name ?? selected.value.title ?? "Haushalt";
});

onMounted(async () => {
    await getHouseholds();
    let active = householdStore.activeHousehold;

    if (!active && activeHouseholds.value.length > 0) {
        active = activeHouseholds.value[0];
        await selectHousehold(active);
        return;
    }
    await selectHousehold(active);
});

function goToList(id) {
    router.push(`/lists/${id}`);
}

function toggleLoading() {
    loading.value = !loading.value;

    if (loading.value) {
        loadingInstance = ElLoading.service({
            lock: true,
            text: "Lade...",
        });
    } else {
        loadingInstance?.close();
        loadingInstance = null;
    }
}

function isValueValid(value) {
    return !value || value.trim().length === 0
}

// HOUSEHOLD

async function selectHousehold(h) {
    inviteUrl.value = '';
    inviteExpiresAt.value = '';
    selected.value = h;
    await householdStore.selectHousehold(h.id);
    await userStore.loadMe();
    await getHousehold(h);
}

async function getHouseholds() {
    toggleLoading();
    error.value = "";
    try {
        const response = await householdService.getAll();
        households.value = Array.isArray(response.data)
            ? response.data
            : (response.data ?? []);
        if (households.value.length > 0) {
            activeHouseholds.value = getActiveHouseholds(households.value);
        }
    } catch (e) {
        console.error(e);
        error.value = "Haushalte konnten nicht geladen werden.";
    } finally {
        toggleLoading();
    }
}

async function getHousehold(h) {
    toggleLoading();
    error.value = "";
    try {
        const response = await householdService.getById(h.id);
        const dataMembers = response.data.members;
        const dataLists = response.data.lists;
        console.log('dataLists', dataLists)
        members.value = Array.isArray(dataMembers) ? dataMembers : [];
        lists.value = Array.isArray(dataLists) ? dataLists : [];
    } catch (e) {
        console.error(e);
        error.value = "Haushalte konnten nicht geladen werden.";
    } finally {
        toggleLoading();
    }
}

async function createHousehold() {
    const name = createName.value.trim();
    if (!name) return;

    createLoading.value = true;
    try {
        const response = await householdService.create({ name });
        const created = response.data?.data ?? response.data;
        households.value.unshift(created);
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
    }
}

async function renameHousehold() {
    const id = selected.value?.id;
    if (!id) return;
    const name = renameName.value.trim();
    if (!name) return;
    const archived = selected.value.archived;

    renameLoading.value = true;
    try {
        const response = await householdService.rename(id, {
            name,
            archived
        })
        const updated = response.data?.data ?? response.data;

        // update in list
        const idx = households.value.findIndex((h) => h?.id === id);
        if (idx !== -1)
            households.value[idx] = { ...households.value[idx], ...updated };

        // update selected
        selected.value = { ...selected.value, ...updated };
        if (householdStore.activeHouseholdId === id && householdStore.activeHousehold) {
            householdStore.activeHousehold = { ...householdStore.activeHousehold, ...updated };
        }
        openRename.value = false;
        ElMessage.success("Umbenannt");
        await getHouseholds();
    } catch (e) {
        console.error(e);
        ElMessage.error("Umbenennen fehlgeschlagen");
    } finally {
        renameLoading.value = false;
    }
}

async function deleteHousehold() {
    if (!selected.value?.id) return;
    lists.value = [];

    const name = selected.value.name;
    const archived = true;
    const id = selected.value.id;


    try {
        await ElMessageBox.confirm(
            `"${selectedName.value}" wirklich löschen?`,
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

    try {
        await householdService.archive(id, {
            name,
            archived
        })
        households.value = households.value.filter((h) => h?.id !== id);
        selected.value = null;
        members.value = [];
        await getHouseholds();
        const length = activeHouseholds.value.length
        const select = activeHouseholds.value[length - 1]
        await selectHousehold(select);
        ElMessage.success("Gelöscht");
    } catch (e) {
        console.error(e);
        ElMessage.error("Löschen fehlgeschlagen");
    }
}

// LIST

async function createList() {
    if (!selected.value?.id) return;
    lists.value = [];
    const title = createListName.value.trim();
    const householdId = householdStore.activeHousehold.id;
    if (!title || !householdId) return;

    createListLoading.value = true;

    try {
        await householdService.createList(selected.value.id, { title })

        openCreateList.value = false;
        createListName.value = "";

        // Listen neu laden
        await getHousehold(selected.value);

        ElMessage.success("Liste erstellt");
    } catch (e) {
        console.error(e);
        ElMessage.error("Liste konnte nicht erstellt werden");
    } finally {
        createListLoading.value = false;
    }
}

async function updateRole(row, newRole) {
    if (!selected.value?.id || !row?.id) return;

    const old = row.role;
    row.role = newRole;

    try {
        await householdMemberService.updateRole(selected.value.id, row.userId, {
            role: newRole,
            actingUserId: userStore.me.id
        })
        ElMessage.success("Rolle aktualisiert");
    } catch (e) {
        console.error(e);
        row.role = old; // rollback
        ElMessage.error("Rolle konnte nicht gespeichert werden");
    }
}

async function removeMember(row) {
    if (!selected.value?.id || !row?.id) return;

    try {
        await ElMessageBox.confirm(
            `Mitglied wirklich entfernen?`,
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

    const before = members.value;
    members.value = members.value.filter((m) => m?.id !== row.id);

    try {
        await householdMemberService.remove(selected.value.id, row.id);
        ElMessage.success("Mitglied entfernt");
    } catch (e) {
        console.error(e);
        members.value = before;
        ElMessage.error("Entfernen fehlgeschlagen");
    }
}

async function createInviteLink() {
    const householdId = householdStore.activeHousehold.id;
    if (!householdId) {
        ElMessage.error("Kein Household ausgewählt.");
        return;
    }
    inviteLoading.value = true;
    try {
        const res = await inviteService.create(householdId, { role: inviteRole.value })
        inviteUrl.value = res.data.url;
        inviteExpiresAt.value = formatExpires(res.data.expiresAt);

        ElMessage.success("Einladungslink erstellt.");
    } catch (e) {
        console.error(e);
        ElMessage.error("Invite-Link konnte nicht erstellt werden.");
    } finally {
        inviteLoading.value = false;
    }
}

async function copyInviteLink() {
    try {
        await navigator.clipboard.writeText(inviteUrl.value);
        ElMessage.success("Link kopiert.");
    } catch (e) {
        console.error(e);
        ElMessage.error("Kopieren nicht möglich (Browser-Rechte?).");
    }
}

function closeInviteDialog() {
    openInvite.value = false;
    inviteRole.value = "EDITOR";
}

function formatExpires(expiresAtIso) {
    // "2026-01-19T10:33:41.479+01:00"
    try {
        const d = new Date(expiresAtIso);
        return d.toLocaleString();
    } catch {
        return expiresAtIso;
    }
}

function openEditList(l) {
    editListId.value = l.id;
    editListTitle.value = (l.title ?? l.name ?? "").toString();
    openEditListDialog.value = true;
}

function getActiveHouseholds(households) {
    const result = [];

    for (const hh of households) {
        if (!hh.archived) {
            result.push(hh);
        }
    }

    return result;
}

async function saveListEdit() {
    if (!editListId.value) return;
    const title = editListTitle.value.trim();
    if (!title) return;

    editListLoading.value = true;
    try {

        await listService.patchList(editListId.value, { title })
        openEditListDialog.value = false;
        await getHousehold(selected.value);
        ElMessage.success("Liste gespeichert");
    } catch (e) {
        console.error(e);
        ElMessage.error("Speichern fehlgeschlagen");
    } finally {
        editListLoading.value = false;
    }
}

async function confirmDeleteList(l) {
    try {
        await ElMessageBox.confirm(
            `"${l.title ?? l.name ?? "Liste"}" wirklich löschen?`,
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

    try {
        await listService.deleteList(l.id);
        await getHousehold(selected.value);
        ElMessage.success("Liste gelöscht");
    } catch (e) {
        console.error(e);
        ElMessage.error("Löschen fehlgeschlagen");
    }
}




</script>
