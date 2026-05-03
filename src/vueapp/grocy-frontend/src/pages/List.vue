<template>
    <div class="min-h-screen bg-gray-50">
        <div class="max-w-xl mx-auto p-4">
            <!-- Header -->
            <header class="bg-white rounded-2xl shadow-sm p-4 mb-4">
                <div class="grid grid-cols-[1fr_auto] items-center gap-3">
                    <!-- Titel + Subline -->
                    <div>
                        <h1 class="text-2xl font-bold leading-tight">
                            {{ list?.title ?? "Einkaufsliste" }}
                        </h1>
                        <p class="text-sm text-gray-500 mt-1" v-if="list">
                            {{ list.isDefault ? "Default-Liste" : "Liste" }}
                            <span v-if="list.archived"
                                class="ml-2 inline-flex items-center px-2 py-0.5 rounded-full text-xs bg-gray-100">
                                Archiviert
                            </span>
                        </p>
                    </div>

                    <!-- Right actions -->
                    <div class="flex items-center gap-2 justify-end">
                        <!-- Rename -->
                        <el-button v-if="list && can(PERM.LIST_EDIT)" circle size="default" plain
                            @click="openRenameListModal()" title="Liste umbenennen">
                            <el-icon>
                                <Edit />
                            </el-icon>
                        </el-button>

                        <!-- Default -->
                        <button v-if="list" @click="toggleDefault(list)" :disabled="list.isDefault" :title="list.isDefault
                            ? 'Default-Liste'
                            : 'Als Default setzen'
                            " class="p-2 rounded-full hover:bg-gray-100 transition-colors disabled:cursor-not-allowed">
                            <span v-if="list.isDefault" class="text-yellow-500 text-xl leading-none">
                                <el-icon>
                                    <StarFilled />
                                </el-icon>
                            </span>
                            <span v-else class="text-gray-400 hover:text-blue-500 text-xl leading-none">
                                <el-icon>
                                    <Star />
                                </el-icon>
                            </span>
                        </button>
                    </div>
                </div>

                <!-- Stats -->
                <div v-if="list" class="mt-4">
                    <div class="flex items-center justify-between text-sm text-gray-600 mb-2">
                        <span>{{ list.stats.checked }} /
                            {{ list.stats.total }} erledigt</span>
                        <span v-if="list.stats.total > 0">
                            {{ progressPercent }}%
                        </span>
                    </div>

                    <div class="w-full h-2 bg-gray-100 rounded-full overflow-hidden">
                        <div class="h-2 bg-blue-600" :style="{ width: progressPercent + '%' }"></div>
                    </div>

                    <div class="flex gap-2 mt-3">
                        <div class="flex-1 bg-gray-50 rounded-xl p-3">
                            <div class="text-xs text-gray-500">Offen</div>
                            <div class="text-lg font-semibold">
                                {{ openCount }}
                            </div>
                        </div>
                        <div class="flex-1 bg-gray-50 rounded-xl p-3">
                            <div class="text-xs text-gray-500">Erledigt</div>
                            <div class="text-lg font-semibold">
                                {{ doneCount }}
                            </div>
                        </div>
                    </div>
                </div>
            </header>

            <!-- Content -->
            <div class="bg-white rounded-2xl shadow-sm p-4">
                <!-- Add -->
                <div v-if="can(PERM.LIST_EDIT)" class="flex gap-2 mb-4">
                    <input v-model="newItem" @keyup.enter="addItem" placeholder="Neues Item hinzufügen…"
                        class="border rounded-xl px-3 py-2 flex-1 focus:outline-none focus:ring-2 focus:ring-blue-200" />
                    <button :disabled="!list || !newItem.trim() || adding"
                        class="bg-blue-600 text-white px-4 rounded-xl text-lg disabled:opacity-50" @click="addItem">
                        +
                    </button>
                </div>

                <!-- Filters -->
                <div class="flex flex-col sm:flex-row gap-2 mb-4">
                    <select v-model="selectedSort"
                        class="border rounded-xl px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-200">
                        <option value="created_desc">Neueste zuerst</option>
                        <option value="created_asc">Älteste zuerst</option>
                        <option value="title_asc">A–Z</option>
                        <option value="title_desc">Z–A</option>
                    </select>
                </div>

                <!-- Empty state -->
                <div v-if="items.length === 0" class="text-center text-gray-500 py-10">
                    <div class="text-lg font-semibold mb-1">
                        Noch keine Items
                    </div>
                    <div v-if="can(PERM.LIST_EDIT)" class="text-sm">Füge oben dein erstes Item hinzu.</div>
                </div>
                <!-- Open items -->
                <div v-else>
                    <h2 class="text-sm font-semibold text-gray-600 mb-2" v-if="openItems.length">
                        Offen
                    </h2>
                    <ul class="divide-y" v-if="openItems.length">
                        <li v-for="item in items.filter((i) => !i.checked)" :key="item.id"
                            class="group flex items-center gap-3 py-2">
                            <!-- Checkbox -->
                            <input type="checkbox" class="h-4 w-4 accent-blue-600" v-model="item.checked"
                                @change="toggle(item)" />

                            <!-- Title / Edit -->
                            <div class="flex-1 min-w-0">
                                <!-- Anzeige -->
                                <span v-if="editingId !== item.id" :class="[
                                    'block truncate',
                                    item.checked
                                        ? 'line-through text-gray-400'
                                        : 'text-gray-900'
                                ]">
                                    {{ item.title }}
                                </span>

                                <!-- Edit -->
                                <input v-else v-model="editTitle"
                                    class="w-full border-b border-gray-300 focus:border-blue-500 outline-none text-sm py-0.5"
                                    @keyup.enter="saveEdit(item)" @keyup.esc="cancelEdit" autofocus />
                            </div>

                            <!-- Actions -->
                            <div class="flex items-center">
                                <!-- Edit -->
                                <el-button circle size="default" plain @click="openRenameItemModal(item)"
                                    title="Bearbeiten">
                                    <el-icon :size="18">
                                        <Edit />
                                    </el-icon>
                                </el-button>

                                <!-- Delete -->
                                <el-button circle size="default" type="danger" plain @click="removeItem(item)"
                                    title="Löschen">
                                    <el-icon :size="18">
                                        <Delete />
                                    </el-icon>
                                </el-button>
                            </div>
                        </li>
                    </ul>

                    <!-- Done items -->
                    <div v-if="doneItems.length" class="mt-6">
                        <div class="flex items-center justify-between gap-3 mb-3">
                            <h2 class="text-sm font-semibold text-gray-600">
                                Erledigt
                            </h2>

                            <div class="flex items-center gap-2">
                                <button
                                    class="text-sm px-3 py-2 rounded-xl bg-gray-100 hover:bg-gray-200 text-gray-700 whitespace-nowrap"
                                    @click="showDone = !showDone">
                                    {{ showDone ? "Verbergen" : "Anzeigen" }}
                                    ({{ doneItems.length }})
                                </button>

                                <button v-if="doneCount > 0"
                                    class="text-sm px-3 py-2 rounded-xl border border-red-200 text-red-600 hover:bg-red-50 whitespace-nowrap"
                                    @click="clearDone">
                                    Löschen ({{ doneCount }})
                                </button>
                            </div>
                        </div>

                        <ul v-if="showDone" class="divide-y opacity-80">
                            <li v-for="item in doneItems" :key="item.id" class="flex items-center gap-3 py-3">
                                <input type="checkbox" class="h-5 w-5" v-model="item.checked" @change="toggle(item)" />
                                <div class="flex-1">
                                    <div class="font-medium line-through text-gray-500">
                                        {{ item.title }}
                                    </div>
                                </div>
                            </li>
                        </ul>
                    </div>

                    <!-- Error hint -->
                    <div v-if="error" class="mt-4 text-sm text-red-600">
                        {{ error }}
                    </div>
                </div>
                <!-- Rename list dialog -->
                <el-dialog v-model="openRenameList" title="Liste umbenennen" width="420px">
                    <el-form label-position="top">
                        <el-form-item label="Neuer Name">
                            <el-input v-model="renameListTitle" placeholder="z. B. Wocheneinkauf"
                                @keyup.enter="saveRenameList" />
                        </el-form-item>
                    </el-form>

                    <template #footer>
                        <el-button @click="openRenameList = false">Abbrechen</el-button>
                        <el-button type="primary" :loading="renameLoading" @click="saveRenameList">
                            Speichern
                        </el-button>
                    </template>
                </el-dialog>
                <!-- Rename item dialog -->
                <el-dialog v-model="openRenameItem" title="Element umbenennen" width="420px">
                    <el-form label-position="top">
                        <el-form-item label="Bezeichnung">
                            <el-input v-model="renameItemTitle" @keyup.enter="saveRenameItem" />
                        </el-form-item>
                    </el-form>

                    <template #footer>
                        <el-button @click="openRenameItem = false">Abbrechen</el-button>
                        <el-button type="primary" :loading="renameLoading" @click="saveRenameItem">
                            Speichern
                        </el-button>
                    </template>
                </el-dialog>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useRoute } from "vue-router";
import { listService } from "@/services/listService";
import { ElLoading, ElMessage, ElMessageBox } from "element-plus";
import { PERM } from "@/auth/perms";
import { usePermissions } from "@/composables/permissionComp";

const { can, isOwner, role, userIsOwner } = usePermissions();
const route = useRoute();

const loading = ref(false);
let loadingInstance = null;

const list = ref(null);
const item = ref(null);
const items = ref([]);
const newItem = ref("");
const adding = ref(false);
const error = ref("");
const showDone = ref(false);

const openRenameList = ref(false);
const openRenameItem = ref(false);

const renameListTitle = ref("");
const renameItemTitle = ref("");
const renameLoading = ref(false);

const doneItems = computed(() =>
    (items.value ?? []).filter((i) => i && i.checked === true)
);

const openItems = computed(() =>
    (items.value ?? []).filter((i) => i && i.checked !== true)
);

const openCount = computed(() => openItems.value.length);
const doneCount = computed(() => doneItems.value.length);

const progressPercent = computed(() => {
    const total = list.value?.stats?.total ?? items.value.length;
    const checked = list.value?.stats?.checked ?? doneCount.value;
    if (!total) return 0;
    return Math.round((checked / total) * 100);
});

const selectedFilter = ref("all");
const selectedSort = ref("created_desc");

const filterOptions = [
    { label: "Alle", value: "all" },
    { label: "Offen", value: "open" },
    { label: "Erledigt", value: "checked" },
];

onMounted(async () => {
    await getList();
});

watch([selectedFilter, selectedSort], () => {
    getList();
});

// ASYNCS

async function getList() {
    const raw = route.params.id;
    const id = raw ? raw : "default";
    toggleLoading();
    error.value = "";
    try {
        const res = await listService.getList(id);
        list.value = res.data;
        await loadItems();
    } catch (e) {
        console.error(e);
        error.value = "Liste konnte nicht geladen werden.";
    } finally {
        toggleLoading();
    }
}

async function loadItems() {
    const listId = list.value?.id;
    if (!listId) return;
    const filter = selectedFilter.value
    const sort = selectedSort.value;
    const res = await listService.getItems(listId, filter, sort);
    items.value = res.data;
}

async function addItem() {
    if (!list.value?.id) return;
    const title = newItem.value.trim();
    if (!title) return;

    adding.value = true;
    error.value = "";

    try {
        const response = await listService.addItem(list.value.id, title);
        items.value.unshift(response.data);
        newItem.value = "";
        await getList();
        ElMessage.success(title + " erfolgreich hinzugefügt");
    } catch (e) {
        console.error(e);
        ElMessage.error("Es ist ein Fehler aufgetreten");
    } finally {
        adding.value = false;
        getList();
    }
}

async function removeItem(item) {
    if (!list.value?.id) return;

    try {
        await ElMessageBox.confirm(
            `"${item.title}" wirklich löschen?`,
            "Item löschen",
            {
                confirmButtonText: "Löschen",
                cancelButtonText: "Abbrechen",
                type: "warning"
            }
        );
    } catch {
        return;
    }

    // optimistic remove
    const idx = items.value.findIndex((i) => i?.id === item.id);
    if (idx === -1) return;
    const removed = items.value[idx];
    items.value.splice(idx, 1);

    try {
        await listService.deleteItem(list.value.id, item.id)
        if (list.value?.stats) {
            list.value.stats.total = items.value.length;
            list.value.stats.checked = items.value.filter(
                (i) => i && i.checked
            ).length;
        }
        ElMessage.success(`"${removed.title}" gelöscht`);
    } catch (e) {
        console.error(e);
        // rollback
        items.value.splice(idx, 0, removed);
        ElMessage.error("Es ist ein Fehler aufgetreten");
    }
}

async function clearDone() {
    if (!list.value?.id) return;

    try {
        await ElMessageBox.confirm(
            `${doneCount.value} erledigte Items wirklich löschen?`,
            "Erledigte löschen",
            {
                confirmButtonText: "Löschen",
                cancelButtonText: "Abbrechen",
                type: "warning"
            }
        );
    } catch {
        return;
    }

    // optimistic remove
    const before = items.value;
    items.value = (items.value ?? []).filter((i) => i && !i.checked);

    try {
        const response = await listService.clearChecked(list.value.id)
        ElMessage.success(
            `${response.data.deleted ?? doneCount.value} Element(e) gelöscht`
        );
        getList();

        if (list.value?.stats) {
            list.value.stats.total = items.value.length;
            list.value.stats.checked = 0;
        }
    } catch (e) {
        console.error(e);
        items.value = before; // rollback
        ElMessage.error("Es ist ein Fehler aufgetreten");
    }
}

async function toggle(item) {
    if (!list.value?.id) return;
    error.value = "";

    recomputeStatsFromItems();

    try {
        await listService.patchItem(list.value.id, item.id, {
            checked: item.checked
        });
    } catch (e) {
        console.error(e);
        // rollback
        item.checked = !item.checked;
        recomputeStatsFromItems();
        error.value = "Konnte Status nicht speichern.";
    }
}

async function toggleDefault(listObject) {
    if (!listObject.id) return;

    const title = listObject.title;
    const isDefault = true;

    try {
        const response = await listService.patchList(listObject.id, { isDefault })
        list.value = response.data;
        ElMessage.success(`${listObject.title} ist Deine neue Default-Liste`);
    } catch (e) {
        console.error(e);
        ElMessage.error("Konnte Default-Liste nicht ändern");
    }
}

async function saveRenameList() {
    if (!list.value?.id) return;

    const title = renameListTitle.value.trim();
    if (!title) return;

    renameLoading.value = true;
    try {
        const response = await listService.patchList(list.value.id, { title })

        list.value = response.data;

        openRenameList.value = false;
        ElMessage.success("Liste umbenannt");
    } catch (e) {
        console.error(e);
        ElMessage.error("Umbenennen fehlgeschlagen");
    } finally {
        renameLoading.value = false;
    }
}

async function saveRenameItem() {
    if (!list.value?.id || !item.value?.id) return;

    const title = renameItemTitle.value.trim();
    if (!title) return;

    renameLoading.value = true;
    try {
        const response = await listService.patchItem(list.value.id, item.value.id, { title })

        const updated = response.data;
        const idx = items.value.findIndex((i) => i?.id === item.value.id);
        if (idx !== -1) {
            items.value[idx] = { ...items.value[idx], ...updated };
        } else {
            await loadItems();
        }

        recomputeStatsFromItems();

        openRenameItem.value = false;
        item.value = null;

        ElMessage.success("Element umbenannt");
    } catch (e) {
        console.error(e);
        ElMessage.error("Umbenennen fehlgeschlagen");
    } finally {
        renameLoading.value = false;
    }
}

// HELPERS

function openRenameListModal() {
    if (!list.value) return;
    renameListTitle.value = (list.value.title ?? "").toString();
    openRenameList.value = true;
}

function openRenameItemModal(itemClicked) {
    if (!itemClicked) return;
    item.value = itemClicked;
    renameItemTitle.value = (itemClicked.title ?? "").toString();
    openRenameItem.value = true;
}

function recomputeStatsFromItems() {
    if (!list.value?.stats) return;
    list.value.stats.total = items.value.length;
    list.value.stats.checked = items.value.filter((i) => i.checked).length;
}

function toggleLoading() {
    loading.value = !loading.value;

    if (loading.value) {
        loadingInstance = ElLoading.service({
            lock: true,
            text: "Lade Liste...",
        });
    } else {
        loadingInstance?.close();
        loadingInstance = null;
    }
}

</script>
