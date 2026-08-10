<template>
	<div class="g-page">
		<div class="g-page-inner-narrow space-y-4" v-loading="pageLoading">
			<header class="g-panel">
				<div class="grid grid-cols-[1fr_auto] items-center gap-3">
					<div class="min-w-0">
						<h1 class="g-page-title truncate">
							{{ list?.title ?? "Einkaufsliste" }}
						</h1>

						<p class="g-page-sub" v-if="list">
							{{ list.isDefault ? "Standardliste" : "Liste" }}

							<span
								v-if="list.archived"
								class="g-chip ml-2 !h-6 !px-2 text-xs"
							>
								Archiviert
							</span>
						</p>
					</div>

					<div class="flex items-center justify-end gap-1">
						<el-button
							v-if="list && can(PERM.LIST_EDIT)"
							circle
							plain
							title="Liste umbenennen"
							@click="openRenameListModal()"
						>
							<el-icon :size="18">
								<Edit />
							</el-icon>
						</el-button>

						<el-button
							v-if="list"
							circle
							plain
							class="!ml-0"
							:disabled="list.isDefault"
							@click="toggleDefault(list)"
							:title="
								list.isDefault
									? 'Standardliste'
									: 'Als Standardliste setzen'
							"
						>
							<el-icon
								:size="18"
								:class="
									list.isDefault
										? 'text-amber-500'
										: 'text-slate-400'
								"
							>
								<StarFilled v-if="list.isDefault" />
								<Star v-else />
							</el-icon>
						</el-button>
					</div>
				</div>

				<div v-if="list" class="mt-5">
					<div
						class="mb-2 flex items-center justify-between text-sm text-muted"
					>
						<span>
							{{ list.stats.checked }} /
							{{ list.stats.total }} erledigt
						</span>

						<span v-if="list.stats.total > 0" class="font-semibold text-primary">
							{{ progressPercent }}%
						</span>
					</div>

					<div
						class="h-2 w-full overflow-hidden rounded-full bg-slate-100"
					>
						<div
							class="h-2 rounded-full bg-primary transition-all duration-300"
							:style="{ width: progressPercent + '%' }"
						/>
					</div>

					<div class="mt-3 grid grid-cols-2 gap-2">
						<div class="rounded-xl bg-primary-soft/70 px-3 py-3">
							<div class="text-xs font-medium text-muted">
								Offen
							</div>
							<div class="font-display text-xl font-bold text-ink">
								{{ openCount }}
							</div>
						</div>

						<div class="rounded-xl bg-slate-50 px-3 py-3">
							<div class="text-xs font-medium text-muted">
								Erledigt
							</div>
							<div class="font-display text-xl font-bold text-ink">
								{{ doneCount }}
							</div>
						</div>
					</div>
				</div>
			</header>

			<div class="g-panel">
				<div v-if="can(PERM.LIST_EDIT)" class="mb-4 space-y-2">
					<div class="flex gap-2">
						<el-input
							v-model="newItem"
							placeholder="Neues Item hinzufügen…"
							clearable
							size="large"
							class="!w-full"
							@keyup.enter="addItem"
						/>

						<el-button
							circle
							plain
							size="large"
							title="Menge, Einheit & Marke"
							class="!m-0"
							@click="showAddDetails = !showAddDetails"
						>
							<el-icon>
								<MoreFilled />
							</el-icon>
						</el-button>

						<el-button
							type="primary"
							circle
							size="large"
							class="!m-0"
							:disabled="
								!list ||
								!newItem.trim() ||
								adding ||
								hasQuantityUnitMismatch(newQuantity, newUnitText)
							"
							:loading="adding"
							title="Hinzufügen"
							@click="addItem"
						>
							<el-icon>
								<Plus />
							</el-icon>
						</el-button>
					</div>

					<transition name="details">
						<div
							v-if="showAddDetails"
							class="grid grid-cols-2 gap-2"
						>
							<el-input
								v-model="newQuantity"
								type="number"
								placeholder="Menge"
								clearable
								size="large"
								class="!w-full"
							/>

							<el-autocomplete
								v-model="newUnitText"
								:fetch-suggestions="queryUnits"
								placeholder="Einheit"
								clearable
								size="large"
								class="!w-full"
							/>

							<el-input
								v-model="newBrand"
								placeholder="Marke / Hersteller (optional)"
								clearable
								size="large"
								class="col-span-2 !w-full"
							/>
						</div>
					</transition>
				</div>

				<div class="mb-4 flex flex-wrap items-center gap-2">
					<button
						v-for="option in filterOptions"
						:key="option.value"
						type="button"
						class="filter-chip"
						:class="{
							'filter-chip--active':
								selectedFilter === option.value
						}"
						@click="selectedFilter = option.value"
					>
						{{ option.label }}
					</button>
				</div>

				<div v-if="items.length === 0" class="g-empty">
					<div class="g-empty-title">
						{{
							selectedFilter === "all"
								? "Noch keine Items"
								: "Keine Einträge für diesen Filter"
						}}
					</div>
					<div v-if="can(PERM.LIST_EDIT) && selectedFilter === 'all'" class="g-empty-text">
						Füge oben dein erstes Item hinzu.
					</div>
					<div v-else-if="selectedFilter !== 'all'" class="g-empty-text">
						Wechsle den Filter oder füge neue Items hinzu.
					</div>
				</div>

				<div v-else>
					<section v-if="openItems.length">
						<div
							class="mb-3 flex items-center justify-between gap-3"
						>
							<h2
								class="text-sm font-semibold uppercase tracking-wide text-muted"
							>
								Offen
							</h2>

							<div class="flex items-center gap-3">
								<span class="text-sm text-muted">
									{{ openItems.length }} Einträge
								</span>

								<el-dropdown
									trigger="click"
									@command="selectedSort = $event"
								>
									<el-button
										circle
										size="default"
										plain
										title="Sortieren"
									>
										<el-icon>
											<Sort />
										</el-icon>
									</el-button>

									<template #dropdown>
										<el-dropdown-menu>
											<el-dropdown-item
												command="created_desc"
											>
												Neueste zuerst
											</el-dropdown-item>
											<el-dropdown-item
												command="created_asc"
											>
												Älteste zuerst
											</el-dropdown-item>
											<el-dropdown-item command="title_asc">
												A–Z
											</el-dropdown-item>
											<el-dropdown-item
												command="title_desc"
											>
												Z–A
											</el-dropdown-item>
										</el-dropdown-menu>
									</template>
								</el-dropdown>
							</div>
						</div>

						<ul class="divide-y divide-slate-100">
							<li
								v-for="item in openItems"
								:key="item.id"
								class="group grid grid-cols-[auto_minmax(0,1fr)_auto] items-center gap-3 py-2.5 sm:grid-cols-[auto_minmax(0,1fr)_90px_auto]"
							>
								<input
									type="checkbox"
									class="h-4 w-4 cursor-pointer accent-primary disabled:opacity-50"
									v-model="item.checked"
									:disabled="togglingIds.has(item.id)"
									@change="toggle(item)"
								/>

								<div class="min-w-0">
									<div class="flex min-w-0 items-center gap-1.5">
										<span class="truncate text-ink">
											{{ item.title }}
										</span>

										<el-tooltip
											v-if="item.sourceRecipeId"
											placement="top"
											:content="sourceRecipeTooltip(item)"
										>
											<el-icon
												:size="14"
												class="shrink-0 text-primary"
											>
												<InfoFilled />
											</el-icon>
										</el-tooltip>
									</div>

									<span
										v-if="itemMeta(item)"
										class="mt-0.5 block text-xs text-muted sm:hidden"
									>
										{{ itemMeta(item) }}
									</span>
								</div>

								<div
									class="hidden max-w-[140px] truncate whitespace-nowrap text-right text-sm tabular-nums text-muted sm:block"
									:title="itemMeta(item) || undefined"
								>
									{{ itemMeta(item) }}
								</div>

								<el-dropdown
									v-if="can(PERM.LIST_EDIT)"
									trigger="click"
								>
									<button class="g-icon-btn" type="button" title="Optionen">
										<el-icon>
											<MoreFilled />
										</el-icon>
									</button>

									<template #dropdown>
										<el-dropdown-menu>
											<el-dropdown-item
												@click="openRenameItemModal(item)"
											>
												Bearbeiten
											</el-dropdown-item>
											<el-dropdown-item
												class="text-danger"
												@click="removeItem(item)"
											>
												Löschen
											</el-dropdown-item>
										</el-dropdown-menu>
									</template>
								</el-dropdown>
							</li>
						</ul>
					</section>

					<section v-if="doneItems.length" class="mt-6">
						<div
							class="mb-3 flex items-center justify-between gap-3"
						>
							<h2
								class="text-sm font-semibold uppercase tracking-wide text-muted"
							>
								Erledigt
							</h2>

							<div class="flex items-center gap-3">
								<button
									class="text-sm font-medium text-muted transition-colors hover:text-ink"
									type="button"
									@click="showDone = !showDone"
								>
									{{ showDone ? "Verbergen" : "Anzeigen" }}
									({{ doneItems.length }})
								</button>

								<el-button
									v-if="doneCount > 0"
									circle
									size="default"
									plain
									title="Erledigte löschen"
									class="!ml-0"
									@click="clearDone"
								>
									<el-icon :size="14" class="text-danger">
										<Delete />
									</el-icon>
								</el-button>
							</div>
						</div>

						<ul
							v-if="showDone"
							class="divide-y divide-slate-100 opacity-80"
						>
							<li
								v-for="item in doneItems"
								:key="item.id"
								class="group grid grid-cols-[auto_minmax(0,1fr)_auto] items-center gap-3 py-2.5 sm:grid-cols-[auto_minmax(0,1fr)_90px_auto]"
							>
								<input
									type="checkbox"
									class="h-4 w-4 cursor-pointer accent-primary disabled:opacity-50"
									v-model="item.checked"
									:disabled="togglingIds.has(item.id)"
									@change="toggle(item)"
								/>

								<div class="min-w-0">
									<div class="flex min-w-0 items-center gap-1.5">
										<span
											class="truncate text-muted line-through"
										>
											{{ item.title }}
										</span>

										<el-tooltip
											v-if="item.sourceRecipeId"
											placement="top"
											:content="sourceRecipeTooltip(item)"
										>
											<el-icon
												:size="14"
												class="shrink-0 text-blue-400"
											>
												<InfoFilled />
											</el-icon>
										</el-tooltip>
									</div>

									<span
										v-if="itemMeta(item)"
										class="mt-0.5 block text-xs text-muted line-through sm:hidden"
									>
										{{ itemMeta(item) }}
									</span>
								</div>

								<div
									class="hidden max-w-[140px] truncate whitespace-nowrap text-right text-sm tabular-nums text-muted line-through sm:block"
									:title="itemMeta(item) || undefined"
								>
									{{ itemMeta(item) }}
								</div>

								<el-dropdown
									v-if="can(PERM.LIST_EDIT)"
									trigger="click"
								>
									<button class="g-icon-btn" type="button" title="Optionen">
										<el-icon>
											<MoreFilled />
										</el-icon>
									</button>

									<template #dropdown>
										<el-dropdown-menu>
											<el-dropdown-item
												@click="openRenameItemModal(item)"
											>
												Bearbeiten
											</el-dropdown-item>
											<el-dropdown-item
												class="text-danger"
												@click="removeItem(item)"
											>
												Löschen
											</el-dropdown-item>
										</el-dropdown-menu>
									</template>
								</el-dropdown>
							</li>
						</ul>
					</section>
				</div>

				<el-dialog
					v-model="openRenameList"
					title="Liste umbenennen"
					width="92%"
					class="max-w-[420px]"
					align-center
				>
					<el-form label-position="top">
						<el-form-item label="Neuer Name">
							<el-input
								v-model="renameListTitle"
								placeholder="z. B. Wocheneinkauf"
								@keyup.enter="saveRenameList"
							/>
						</el-form-item>
					</el-form>

					<template #footer>
						<el-button @click="openRenameList = false">
							Abbrechen
						</el-button>
						<el-button
							type="primary"
							:loading="renameLoading"
							:disabled="!renameListTitle"
							@click="saveRenameList"
						>
							Speichern
						</el-button>
					</template>
				</el-dialog>

				<el-dialog
					v-model="openRenameItem"
					title="Item bearbeiten"
					width="92%"
					class="max-w-[420px]"
					align-center
				>
					<el-form label-position="top">
						<el-form-item label="Bezeichnung">
							<el-input
								v-model="renameItemTitle"
								size="large"
								@keyup.enter="saveRenameItem"
							/>
						</el-form-item>

						<div class="grid grid-cols-2 gap-2">
							<el-form-item label="Menge">
								<el-input
									v-model="renameItemQuantity"
									type="number"
									placeholder="Menge"
									clearable
									size="large"
									class="w-full"
								/>
							</el-form-item>

							<el-form-item label="Einheit">
								<el-autocomplete
									v-model="renameItemUnitText"
									:fetch-suggestions="queryUnits"
									placeholder="Einheit"
									clearable
									size="large"
									class="w-full"
								/>
							</el-form-item>
						</div>

						<el-form-item label="Marke / Hersteller">
							<el-input
								v-model="renameItemBrand"
								placeholder="optional"
								clearable
								size="large"
							/>
						</el-form-item>
					</el-form>

					<template #footer>
						<el-button @click="openRenameItem = false">
							Abbrechen
						</el-button>
						<el-button
							type="primary"
							:loading="renameLoading"
							:disabled="
								!renameItemTitle ||
								hasQuantityUnitMismatch(
									renameItemQuantity,
									renameItemUnitText
								)
							"
							@click="saveRenameItem"
						>
							Speichern
						</el-button>
					</template>
				</el-dialog>
			</div>
		</div>
	</div>
</template>

<style scoped>
.details-enter-active,
.details-leave-active {
	transition: all 0.2s ease;
}

.details-enter-from,
.details-leave-to {
	opacity: 0;
	transform: translateY(-4px);
}

.filter-chip {
	padding: 6px 12px;
	border-radius: 9999px;
	border: 1px solid rgba(15, 23, 42, 0.08);
	background: #f8fafc;
	font-size: 0.8rem;
	font-weight: 600;
	color: #64748b;
	cursor: pointer;
	transition:
		background 0.15s ease,
		color 0.15s ease,
		border-color 0.15s ease;
}

.filter-chip:hover {
	border-color: #bfdbfe;
	color: #1d4ed8;
}

.filter-chip--active {
	background: #eff6ff;
	border-color: #93c5fd;
	color: #1d4ed8;
}
</style>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useRoute } from "vue-router";
import { listService } from "@/services/listService";
import { ElMessage, ElMessageBox } from "element-plus";
import { PERM } from "@/auth/perms";
import { usePermissions } from "@/composables/permissionComp";

const { can } = usePermissions();
const route = useRoute();

const pageLoading = ref(false);
const togglingIds = ref(new Set());

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
	{ label: "Erledigt", value: "checked" }
];

const showAddDetails = ref(false);

const newQuantity = ref(null);
const newUnitText = ref("");
const newBrand = ref("");

const renameItemQuantity = ref(null);
const renameItemUnitText = ref("");
const renameItemBrand = ref("");

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

watch(
	() => route.params.id,
	async () => {
		await getList();
	}
);

watch([selectedFilter, selectedSort], async () => {
	if (!list.value?.id) return;
	pageLoading.value = true;
	try {
		await loadItems();
	} catch (e) {
		console.error(e);
		ElMessage.error(
			"Einträge konnten nicht geladen werden. Bitte erneut versuchen."
		);
	} finally {
		pageLoading.value = false;
	}
});

async function getList() {
	const raw = route.params.id;
	const id = raw ? raw : "default";
	pageLoading.value = true;
	try {
		const res = await listService.getList(id);
		list.value = res.data;
		await loadItems();
	} catch (e) {
		console.error(e);
		list.value = null;
		items.value = [];
		ElMessage.error(
			"Liste konnte nicht geladen werden. Prüfe den Link oder wähle eine Liste unter Haushalte."
		);
	} finally {
		pageLoading.value = false;
	}
}

async function loadItems() {
	const listId = list.value?.id;
	if (!listId) return;
	const filter = selectedFilter.value;
	const sort = selectedSort.value;
	const res = await listService.getItems(listId, filter, sort);
	items.value = res.data;
}

async function addItem() {
	if (!list.value?.id) return;

	const title = newItem.value.trim();
	if (!title) return;

	if (hasQuantityUnitMismatch(newQuantity.value, newUnitText.value)) {
		ElMessage.warning("Bitte Menge und Einheit gemeinsam angeben.");
		return;
	}

	const quantity = normalizeQuantity(newQuantity.value);
	const unitText = normalizeUnitText(newUnitText.value);
	const brand = normalizeUnitText(newBrand.value);

	adding.value = true;

	try {
		const response = await listService.addItem(list.value.id, {
			title,
			quantity,
			unitText,
			brand
		});

		items.value.unshift(response.data);
		recomputeStatsFromItems();

		newItem.value = "";
		newQuantity.value = null;
		newUnitText.value = "";
		newBrand.value = "";

		ElMessage.success(`${title} hinzugefügt`);
	} catch (e) {
		console.error(e);
		ElMessage.error(
			"Item konnte nicht hinzugefügt werden. Bitte Verbindung prüfen und erneut versuchen."
		);
	} finally {
		adding.value = false;
	}
}

async function removeItem(itemToRemove) {
	if (!list.value?.id) return;

	try {
		await ElMessageBox.confirm(
			`"${itemToRemove.title}" wirklich löschen?`,
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

	const idx = items.value.findIndex((i) => i?.id === itemToRemove.id);
	if (idx === -1) return;
	const removed = items.value[idx];
	items.value.splice(idx, 1);
	recomputeStatsFromItems();

	try {
		await listService.deleteItem(list.value.id, itemToRemove.id);
		showUndoToast(removed);
	} catch (e) {
		console.error(e);
		items.value.splice(idx, 0, removed);
		recomputeStatsFromItems();
		ElMessage.error(
			"Item konnte nicht gelöscht werden. Bitte erneut versuchen."
		);
	}
}

function showUndoToast(removed) {
	const msg = ElMessage({
		dangerouslyUseHTMLString: true,
		type: "success",
		duration: 6000,
		showClose: true,
		message: `<span>"${escapeHtml(removed.title)}" gelöscht — <button type="button" class="undo-link" style="all:unset;cursor:pointer;font-weight:700;text-decoration:underline;color:inherit">Rückgängig</button></span>`
	});

	queueMicrotask(() => {
		const root = document.querySelector(
			".el-message:last-of-type .undo-link"
		);
		if (!root) return;
		const handler = async () => {
			msg.close();
			await restoreItem(removed);
			root.removeEventListener("click", handler);
		};
		root.addEventListener("click", handler);
	});
}

function escapeHtml(value) {
	return String(value)
		.replaceAll("&", "&amp;")
		.replaceAll("<", "&lt;")
		.replaceAll(">", "&gt;")
		.replaceAll('"', "&quot;");
}

async function restoreItem(removed) {
	if (!list.value?.id) return;

	try {
		const response = await listService.addItem(list.value.id, {
			title: removed.title,
			quantity: removed.quantity ?? null,
			unitText: removed.unitText ?? null,
			brand: removed.brand ?? null,
			checked: removed.checked ?? false
		});
		items.value.unshift(response.data);
		recomputeStatsFromItems();
		ElMessage.success(`"${removed.title}" wiederhergestellt`);
	} catch (e) {
		console.error(e);
		ElMessage.error(
			"Wiederherstellen fehlgeschlagen. Bitte das Item erneut hinzufügen."
		);
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

	const before = items.value;
	items.value = (items.value ?? []).filter((i) => i && !i.checked);
	recomputeStatsFromItems();

	try {
		const response = await listService.clearChecked(list.value.id);
		ElMessage.success(
			`${response.data.deleted ?? doneCount.value} erledigte Einträge gelöscht`
		);
		await loadItems();
	} catch (e) {
		console.error(e);
		items.value = before;
		recomputeStatsFromItems();
		ElMessage.error(
			"Erledigte Einträge konnten nicht gelöscht werden. Bitte erneut versuchen."
		);
	}
}

async function toggle(itemToToggle) {
	if (!list.value?.id) return;

	const next = new Set(togglingIds.value);
	next.add(itemToToggle.id);
	togglingIds.value = next;
	recomputeStatsFromItems();

	try {
		await listService.patchItem(list.value.id, itemToToggle.id, {
			checked: itemToToggle.checked
		});
	} catch (e) {
		console.error(e);
		itemToToggle.checked = !itemToToggle.checked;
		recomputeStatsFromItems();
		ElMessage.error(
			"Status konnte nicht gespeichert werden. Bitte erneut antippen."
		);
	} finally {
		const cleaned = new Set(togglingIds.value);
		cleaned.delete(itemToToggle.id);
		togglingIds.value = cleaned;
	}
}

async function toggleDefault(listObject) {
	if (!listObject.id) return;
	renameLoading.value = true;

	try {
		const response = await listService.patchList(listObject.id, {
			isDefault: true
		});
		list.value = response.data;
		ElMessage.success(`${listObject.title} ist deine neue Standardliste`);
	} catch (e) {
		console.error(e);
		ElMessage.error(
			"Standardliste konnte nicht geändert werden. Bitte erneut versuchen."
		);
	} finally {
		renameLoading.value = false;
	}
}

async function saveRenameList() {
	if (!list.value?.id) return;

	const title = renameListTitle.value.trim();
	if (!title) return;
	renameLoading.value = true;
	try {
		const response = await listService.patchList(list.value.id, { title });

		list.value = response.data;

		openRenameList.value = false;
		ElMessage.success("Liste umbenannt");
	} catch (e) {
		console.error(e);
		ElMessage.error(
			"Umbenennen fehlgeschlagen. Bitte Namen prüfen und erneut speichern."
		);
	} finally {
		renameLoading.value = false;
	}
}

async function saveRenameItem() {
	if (!list.value?.id || !item.value?.id) return;

	const title = renameItemTitle.value.trim();
	if (!title) return;

	if (
		hasQuantityUnitMismatch(
			renameItemQuantity.value,
			renameItemUnitText.value
		)
	) {
		ElMessage.warning("Bitte Menge und Einheit gemeinsam angeben.");
		return;
	}

	renameLoading.value = true;
	try {
		const response = await listService.patchItem(
			list.value.id,
			item.value.id,
			{
				title,
				quantity: normalizeQuantity(renameItemQuantity.value),
				unitText: normalizeUnitText(renameItemUnitText.value),
				brand: normalizeUnitText(renameItemBrand.value)
			}
		);

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

		ElMessage.success("Item gespeichert");
	} catch (e) {
		console.error(e);
		ElMessage.error(
			"Speichern fehlgeschlagen. Bitte Eingaben prüfen und erneut versuchen."
		);
	} finally {
		renameLoading.value = false;
	}
}

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
	renameItemBrand.value = itemClicked.brand ?? "";

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

function formatQuantity(value) {
	if (value === null || value === undefined) return "";

	const numberValue = Number(value);

	if (Number.isInteger(numberValue)) {
		return numberValue.toString();
	}

	return numberValue.toFixed(2).replace(/\.?0+$/, "");
}

function itemMeta(item) {
	const quantityPart = [formatQuantity(item?.quantity), item?.unitText]
		.filter(Boolean)
		.join(" ")
		.trim();
	const brand = (item?.brand ?? "").toString().trim();

	if (quantityPart && brand) return `${quantityPart} · ${brand}`;
	return quantityPart || brand || "";
}

function sourceRecipeTooltip(item) {
	return item.sourceRecipeTitle
		? `Aus Rezept: ${item.sourceRecipeTitle}`
		: "Aus einem Rezept hinzugefügt";
}

function queryUnits(queryString, callback) {
	const results = unitOptions
		.filter((u) => u.toLowerCase().includes(queryString.toLowerCase()))
		.map((u) => ({ value: u }));

	callback(results);
}

function hasQuantityUnitMismatch(quantity, unitText) {
	const hasQuantity =
		quantity !== null && quantity !== undefined && quantity !== "";
	const hasUnitText = !!(unitText ?? "").toString().trim();

	return hasQuantity !== hasUnitText;
}

function recomputeStatsFromItems() {
	if (!list.value?.stats) return;
	list.value.stats.total = items.value.length;
	list.value.stats.checked = items.value.filter((i) => i.checked).length;
}
</script>
