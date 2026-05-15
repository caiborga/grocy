<template>
    <div class="min-h-screen bg-gray-50">
        <div class="max-w-xl mx-auto p-4">
            <!-- Header -->
            <header class="bg-white rounded-2xl shadow-sm p-4 mb-4">
                <div class="grid grid-cols-[1fr_auto] items-center gap-3">
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
                    <div class="flex items-center justify-end">
                        <!-- Rename -->
                        <el-button v-if="list && can(PERM.LIST_EDIT)" circle plain @click="openRenameListModal()"
                            title="Liste umbenennen">
                            <el-icon :size="18">
                                <Edit />
                            </el-icon>
                        </el-button>

                        <!-- Default -->
                        <el-button v-if="list" circle plain class="!ml-1" :disabled="list.isDefault"
                            @click="toggleDefault(list)"
                            :title="list.isDefault ? 'Default-Liste' : 'Als Default setzen'">
                            <el-icon :size="18" :class="list.isDefault ? 'text-yellow-500' : 'text-gray-400'">
                                <StarFilled v-if="list.isDefault" />
                                <Star v-else />
                            </el-icon>
                        </el-button>
                    </div>
                </div>

                <!-- Stats -->
                <div v-if="list" class="mt-4">
                    <div class="flex items-center justify-between text-sm text-gray-600 mb-2">
                        <span>
                            {{ list.stats.checked }} /
                            {{ list.stats.total }} erledigt
                        </span>

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
                <div v-if="can(PERM.LIST_EDIT)" class="mb-4 space-y-2">
                    <div class="flex gap-2">

                        <el-input v-model="newItem" placeholder="Neues Item hinzufügen…" clearable size="large"
                            class="!w-full" @keyup.enter="addItem" />

                        <el-button circle plain size="large" title="Details" class="!m-0"
                            @click="showAddDetails = !showAddDetails">
                            <el-icon>
                                <MoreFilled />
                            </el-icon>
                        </el-button>

                        <el-button type="primary" circle size="large" class="!m-0"
                            :disabled="!list || !newItem.trim() || adding || hasQuantityUnitMismatch(newQuantity, newUnitText)"
                            :loading="adding" title="Hinzufügen" @click="addItem">
                            <el-icon>
                                <Plus />
                            </el-icon>
                        </el-button>
                    </div>

                    <transition name="details">
                        <div v-if="showAddDetails" class="grid grid-cols-[1fr_1fr] gap-2">

                            <el-input v-model="newQuantity" type="number" placeholder="Menge" clearable size="large"
                                class="!w-full" />

                            <el-autocomplete v-model="newUnitText" :fetch-suggestions="queryUnits" placeholder="Einheit"
                                clearable size="large" class="!w-full" />
                        </div>
                    </transition>
                </div>

                <!-- Empty state -->
                <div v-if="items.length === 0" class="text-center text-gray-500 py-10">
                    <div class="text-lg font-semibold mb-1">
                        Noch keine Items
                    </div>

                    <div v-if="can(PERM.LIST_EDIT)" class="text-sm">
                        Füge oben dein erstes Item hinzu.
                    </div>
                </div>

                <!-- Items -->
                <div v-else>
                    <!-- Open items -->
                    <section v-if="openItems.length">
                        <div class="flex items-center justify-between gap-3 mb-3">
                            <h2 class="text-sm font-semibold text-gray-600">
                                Offen
                            </h2>

                            <div class="flex items-center gap-3">
                                <span class="text-sm text-gray-500">
                                    Einträge ({{ items.length - doneItems.length }})
                                </span>

                                <el-dropdown trigger="click" @command="selectedSort = $event">
                                    <button class="text-action inline-flex items-center gap-1">
                                        {{ selectedSortLabel }}

                                        <el-button circle size="default" plain title="Sortieren">
                                            <el-icon>
                                                <Sort />
                                            </el-icon>
                                        </el-button>
                                    </button>

                                    <template #dropdown>
                                        <el-dropdown-menu>
                                            <el-dropdown-item command="created_desc">
                                                Neueste zuerst
                                            </el-dropdown-item>

                                            <el-dropdown-item command="created_asc">
                                                Älteste zuerst
                                            </el-dropdown-item>

                                            <el-dropdown-item command="title_asc">
                                                A–Z
                                            </el-dropdown-item>

                                            <el-dropdown-item command="title_desc">
                                                Z–A
                                            </el-dropdown-item>
                                        </el-dropdown-menu>
                                    </template>
                                </el-dropdown>
                            </div>
                        </div>

                        <ul class="divide-y divide-gray-100">
                            <li v-for="item in openItems" :key="item.id"
                                class="group grid grid-cols-[auto_minmax(0,1fr)_auto] sm:grid-cols-[auto_minmax(0,1fr)_90px_auto] items-center gap-3 py-2">
                                <!-- Checkbox -->
                                <input type="checkbox" class="h-4 w-4 accent-blue-600 cursor-pointer"
                                    v-model="item.checked" @change="toggle(item)" />

                                <!-- Title -->
                                <div class="min-w-0">
                                    <span class="block truncate text-gray-900">
                                        {{ item.title }}
                                    </span>

                                    <!-- Mobile quantity -->
                                    <span v-if="item.quantity || item.unitText"
                                        class="block sm:hidden text-xs text-gray-400 mt-0.5">
                                        {{ formatQuantity(item.quantity) }}
                                        {{ item.unitText }}
                                    </span>
                                </div>

                                <!-- Desktop quantity -->
                                <div
                                    class="hidden sm:block text-sm text-gray-500 text-right tabular-nums whitespace-nowrap">
                                    <template v-if="item.quantity || item.unitText">
                                        {{ formatQuantity(item.quantity) }}
                                        {{ item.unitText }}
                                    </template>
                                </div>

                                <!-- Actions -->
                                <el-dropdown v-if="can(PERM.LIST_EDIT)" trigger="click">
                                    <button class="icon-action">
                                        <el-icon>
                                            <MoreFilled />
                                        </el-icon>
                                    </button>

                                    <template #dropdown>
                                        <el-dropdown-menu>
                                            <el-dropdown-item @click="openRenameItemModal(item)">
                                                Bearbeiten
                                            </el-dropdown-item>

                                            <el-dropdown-item class="text-red-500" @click="removeItem(item)">
                                                Löschen
                                            </el-dropdown-item>
                                        </el-dropdown-menu>
                                    </template>
                                </el-dropdown>
                            </li>
                        </ul>
                    </section>

                    <!-- Done items -->
                    <section v-if="doneItems.length" class="mt-6">
                        <div class="flex items-center justify-between gap-3 mb-3">
                            <h2 class="text-sm font-semibold text-gray-600">
                                Erledigt
                            </h2>

                            <div class="flex items-center gap-3">
                                <button class="text-action" @click="showDone = !showDone">
                                    {{ showDone ? "Verbergen" : "Anzeigen" }}
                                    ({{ doneItems.length }})
                                </button>

                                <el-button v-if="doneCount > 0" circle size="default" plain title="Löschen"
                                    class="!ml-0" @click="clearDone">
                                    <el-icon :size="14" class="text-red-500">
                                        <Delete />
                                    </el-icon>
                                </el-button>
                            </div>
                        </div>

                        <ul v-if="showDone" class="divide-y divide-gray-100 opacity-80">
                            <li v-for="item in doneItems" :key="item.id"
                                class="group grid grid-cols-[auto_minmax(0,1fr)_auto] sm:grid-cols-[auto_minmax(0,1fr)_90px_auto] items-center gap-3 py-2">
                                <!-- Checkbox -->
                                <input type="checkbox" class="h-4 w-4 accent-blue-600 cursor-pointer"
                                    v-model="item.checked" @change="toggle(item)" />

                                <!-- Title -->
                                <div class="min-w-0">
                                    <span class="block truncate line-through text-gray-500">
                                        {{ item.title }}
                                    </span>

                                    <!-- Mobile quantity -->
                                    <span v-if="item.quantity || item.unitText"
                                        class="block sm:hidden text-xs text-gray-400 mt-0.5 line-through">
                                        {{ formatQuantity(item.quantity) }}
                                        {{ item.unitText }}
                                    </span>
                                </div>

                                <!-- Desktop quantity -->
                                <div
                                    class="hidden sm:block text-sm text-gray-500 text-right tabular-nums whitespace-nowrap line-through">
                                    <template v-if="item.quantity || item.unitText">
                                        {{ formatQuantity(item.quantity) }}
                                        {{ item.unitText }}
                                    </template>
                                </div>

                                <!-- Actions -->
                                <el-dropdown v-if="can(PERM.LIST_EDIT)" trigger="click">
                                    <button class="icon-action">
                                        <el-icon>
                                            <MoreFilled />
                                        </el-icon>
                                    </button>

                                    <template #dropdown>
                                        <el-dropdown-menu>
                                            <el-dropdown-item @click="openRenameItemModal(item)">
                                                Bearbeiten
                                            </el-dropdown-item>

                                            <el-dropdown-item class="text-red-500" @click="removeItem(item)">
                                                Löschen
                                            </el-dropdown-item>
                                        </el-dropdown-menu>
                                    </template>
                                </el-dropdown>
                            </li>
                        </ul>
                    </section>
                </div>

                <!-- Rename list dialog -->
                <el-dialog v-model="openRenameList" title="Liste umbenennen" width="92%" class="max-w-[420px]"
                    align-center>
                    <el-form label-position="top">
                        <el-form-item label="Neuer Name">
                            <el-input v-model="renameListTitle" placeholder="z. B. Wocheneinkauf"
                                @keyup.enter="saveRenameList" />
                        </el-form-item>
                    </el-form>

                    <template #footer>
                        <el-button @click="openRenameList = false">
                            Abbrechen
                        </el-button>

                        <el-button type="primary" :loading="renameLoading" @click="saveRenameList" :disabled="!renameListTitle">
                            Speichern
                        </el-button>
                    </template>
                </el-dialog>

                <!-- Rename item dialog -->
                <el-dialog v-model="openRenameItem" title="Element umbenennen" width="92%" class="max-w-[420px]"
                    align-center>
                    <el-form label-position="top">
                        <el-form-item label="Bezeichnung">
                            <el-input v-model="renameItemTitle" size="large" @keyup.enter="saveRenameItem" />
                        </el-form-item>

                        <div class="grid grid-cols-[1fr_1fr] gap-2">
                            <el-form-item label="Menge">
                                <el-input v-model="renameItemQuantity" type="number" placeholder="Menge" clearable
                                    size="large" class="w-full" />
                            </el-form-item>

                            <el-form-item label="Einheit">
                                <el-autocomplete v-model="renameItemUnitText" :fetch-suggestions="queryUnits"
                                    placeholder="Einheit" clearable size="large" class="w-full" />
                            </el-form-item>
                        </div>
                    </el-form>

                    <template #footer>
                        <el-button @click="openRenameItem = false">
                            Abbrechen
                        </el-button>

                        <el-button type="primary" :loading="renameLoading" @click="saveRenameItem"
                            :disabled="!renameItemTitle || hasQuantityUnitMismatch(renameItemQuantity, renameItemUnitText)">
                            Speichern
                        </el-button>
                    </template>
                </el-dialog>
            </div>
        </div>
    </div>
</template>

<style scoped>
.icon-action {
    @apply h-8 w-8 inline-flex items-center justify-center rounded-full text-gray-400 hover:text-gray-700 hover:bg-gray-100 transition-colors;
}

.icon-action-danger {
    @apply h-8 w-8 inline-flex items-center justify-center rounded-full text-gray-400 hover:text-red-600 hover:bg-red-50 transition-colors;
}

.text-action {
    @apply text-sm text-gray-500 hover:text-gray-900 transition-colors;
}
</style>

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

const showAddDetails = ref(false);

const newQuantity = ref(null);
const newUnitText = ref("");

const renameItemQuantity = ref(null);
const renameItemUnitText = ref("");

const unitOptions = [
    "g",
    "kg",
    "ml",
    "l",
    "Stück",
    "Packung",
    "Dose",
    "Glas",
    "Flasche",
    "Bund",
    "EL",
    "TL",
    "Prise"
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
    try {
        const res = await listService.getList(id);
        list.value = res.data;
        await loadItems();
    } catch (e) {
        console.error(e);
        ElMessage.error("Liste konnte nicht geladen werden.");
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
    toggleLoading();

    const title = newItem.value.trim();
    if (!title) return;

    if (hasQuantityUnitMismatch(newQuantity.value, newUnitText.value)) {
        ElMessage.warning("Bitte Menge und Einheit gemeinsam angeben.");
        return;
    }

    const quantity = normalizeQuantity(newQuantity.value);
    const unitText = normalizeUnitText(newUnitText.value);

    adding.value = true;

    try {
        const response = await listService.addItem(list.value.id, {
            title,
            quantity,
            unitText
        });

        items.value.unshift(response.data);

        newItem.value = "";
        newQuantity.value = null;
        newUnitText.value = "";

        await getList();
        ElMessage.success(title + " erfolgreich hinzugefügt");
    } catch (e) {
        console.error(e);
        ElMessage.error("Es ist ein Fehler aufgetreten");
    } finally {
        toggleLoading();
        adding.value = false;
    }
}

async function removeItem(item) {
    if (!list.value?.id) return;
    toggleLoading();

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
    finally {
        toggleLoading();
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

    toggleLoading();

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
    } finally {
        toggleLoading();
    }
}

async function toggle(item) {
    if (!list.value?.id) return;
    recomputeStatsFromItems();
    toggleLoading();

    try {
        await listService.patchItem(list.value.id, item.id, {
            checked: item.checked
        });
    } catch (e) {
        console.error(e);
        // rollback
        item.checked = !item.checked;
        recomputeStatsFromItems();
        ElMessage.error("Konnte Status nicht speichern.");
    } finally {
        toggleLoading();
    }
}

async function toggleDefault(listObject) {
    if (!listObject.id) return;
    toggleLoading();
    const isDefault = true;

    try {
        const response = await listService.patchList(listObject.id, { isDefault })
        list.value = response.data;
        ElMessage.success(`${listObject.title} ist Deine neue Default-Liste`);
    } catch (e) {
        console.error(e);
        ElMessage.error("Konnte Default-Liste nicht ändern");
    } finally {
        toggleLoading();
    }
}

async function saveRenameList() {
    if (!list.value?.id) return;

    const title = renameListTitle.value.trim();
    if (!title) return;
    toggleLoading();
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
        toggleLoading();
    }
}

async function saveRenameItem() {
    if (!list.value?.id || !item.value?.id) return;

    const title = renameItemTitle.value.trim();
    if (!title) return;

    if (hasQuantityUnitMismatch(renameItemQuantity.value, renameItemUnitText.value)) {
        ElMessage.warning("Bitte Menge und Einheit gemeinsam angeben.");
        return;
    }

    toggleLoading();

    renameLoading.value = true;
    try {
        const response = await listService.patchItem(list.value.id, item.value.id, {
            title,
            quantity: normalizeQuantity(renameItemQuantity.value),
            unitText: normalizeUnitText(renameItemUnitText.value)
        });

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
        toggleLoading();
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
    renameItemQuantity.value = itemClicked.quantity ?? null;
    renameItemUnitText.value = itemClicked.unitText ?? "";

    openRenameItem.value = true;
}

function normalizeQuantity(value) {
    if (value === null || value === undefined || value === "") return null;

    const numberValue = Number(value);
    if (Number.isNaN(numberValue)) return null;

    return numberValue;
}

function normalizeUnitText(value) {
    const normalized = (value ?? "").toString().trim();
    return normalized.length > 0 ? normalized : null;
}

function formatItemLabel(item) {
    const quantity = item.quantity ?? null;
    const unitText = item.unitText ?? "";
    const title = item.title ?? "";

    if (quantity !== null && unitText) return `${quantity} ${unitText} ${title}`;
    if (quantity !== null) return `${quantity} ${title}`;
    if (unitText) return `${unitText} ${title}`;

    return title;
}

function formatQuantity(value) {
    if (value === null || value === undefined) return "";

    const numberValue = Number(value);

    if (Number.isInteger(numberValue)) {
        return numberValue.toString();
    }

    return numberValue.toFixed(2).replace(/\.?0+$/, "");
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

function queryUnits(queryString, callback) {
    const results = unitOptions
        .filter((u) =>
            u.toLowerCase().includes(queryString.toLowerCase())
        )
        .map((u) => ({ value: u }));

    callback(results);
}

function hasQuantityUnitMismatch(quantity, unitText) {
    const hasQuantity = quantity !== null && quantity !== undefined && quantity !== "";
    const hasUnitText = !!(unitText ?? "").toString().trim();

    return hasQuantity !== hasUnitText;
}

function recomputeStatsFromItems() {
    if (!list.value?.stats) return;
    list.value.stats.total = items.value.length;
    list.value.stats.checked = items.value.filter((i) => i.checked).length;
}

</script>
