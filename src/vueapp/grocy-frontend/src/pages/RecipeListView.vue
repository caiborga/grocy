<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { householdService } from "@/services/householdService";
import { recipeService } from "@/services/recepieService";
import type { List } from "@/models/List";
import type { Recipe } from "@/models/Recipe";
import { useHouseholdStore } from "@/stores/householdStore";

const router = useRouter();
const householdStore = useHouseholdStore();

const loading = ref(false);
const loadError = ref("");
const search = ref("");
const recipes = ref<Recipe[]>([]);
const selectedRecipeId = ref<string | null>(null);
const contentTab = ref<"ingredients" | "steps">("ingredients");
const deleting = ref(false);

const addDialogOpen = ref(false);
const addingToList = ref(false);
const listsLoading = ref(false);
const lists = ref<List[]>([]);
const selectedListId = ref("");
const targetServings = ref(1);

const householdId = computed(() => householdStore.activeHousehold?.id);

const filteredRecipes = computed(() => {
	const term = search.value.trim().toLowerCase();

	if (!term) {
		return recipes.value;
	}

	return recipes.value.filter((recipe) =>
		recipe.title.toLowerCase().includes(term)
	);
});

const selectedRecipe = computed(() => {
	if (!selectedRecipeId.value) {
		return filteredRecipes.value[0] ?? null;
	}

	return (
		filteredRecipes.value.find(
			(recipe) => recipe.id === selectedRecipeId.value
		) ??
		filteredRecipes.value[0] ??
		null
	);
});

const selectedName = computed(
	() => selectedRecipe.value?.title ?? "Kein Rezept ausgewählt"
);

const selectedIngredients = computed(
	() => selectedRecipe.value?.ingredients ?? []
);

const selectedSteps = computed(() => selectedRecipe.value?.steps ?? []);

async function loadRecipes() {
	if (!householdId.value) {
		loadError.value =
			"Kein Haushalt ausgewählt. Wähle zuerst einen Haushalt.";
		return;
	}

	loading.value = true;
	loadError.value = "";

	try {
		const response = await recipeService.getAll(householdId.value);
		recipes.value = response.data;
		selectedRecipeId.value = recipes.value[0]?.id ?? null;
		if (recipes.value[0]) {
			targetServings.value = recipes.value[0].baseServings;
		}
	} catch (e) {
		console.error(e);
		recipes.value = [];
		selectedRecipeId.value = null;
		loadError.value =
			"Rezepte konnten nicht geladen werden. Bitte Seite neu laden oder später erneut versuchen.";
		ElMessage.error(loadError.value);
	} finally {
		loading.value = false;
	}
}

function selectRecipe(id: string) {
	selectedRecipeId.value = id;
	contentTab.value = "ingredients";
	const recipe = recipes.value.find((entry) => entry.id === id);
	if (recipe) {
		targetServings.value = recipe.baseServings;
	}
}

function goToRecipeEdit(id?: string) {
	if (!id) {
		return;
	}

	router.push(`/recipes/${id}/edit`);
}

async function openAddToListDialog() {
	if (!householdId.value || !selectedRecipe.value) {
		return;
	}

	targetServings.value = selectedRecipe.value.baseServings;
	addDialogOpen.value = true;

	if (lists.value.length > 0) {
		return;
	}

	listsLoading.value = true;

	try {
		const response = await householdService.getById(householdId.value);
		lists.value = (response.data.lists ?? []).filter(
			(list) => !list.archived
		);

		selectedListId.value =
			lists.value.find((list) => list.isDefault)?.id ??
			lists.value[0]?.id ??
			"";
	} finally {
		listsLoading.value = false;
	}
}

async function addToShoppingList() {
	if (!householdId.value || !selectedRecipe.value) {
		return;
	}

	if (!selectedListId.value) {
		ElMessage.error("Bitte wähle eine Liste aus");
		return;
	}

	addingToList.value = true;

	try {
		const response = await recipeService.addToList(
			householdId.value,
			selectedRecipe.value.id,
			{
				listId: selectedListId.value,
				servings: targetServings.value
			}
		);

		addDialogOpen.value = false;
		ElMessage.success(
			`${response.data.added} Zutaten zur Einkaufsliste hinzugefügt`
		);
	} catch (e) {
		console.error(e);
		ElMessage.error(
			"Zutaten konnten nicht hinzugefügt werden. Bitte erneut versuchen."
		);
	} finally {
		addingToList.value = false;
	}
}

async function deleteSelectedRecipe() {
	if (!householdId.value || !selectedRecipe.value) {
		return;
	}

	const recipe = selectedRecipe.value;

	try {
		await ElMessageBox.confirm(
			`"${recipe.title}" wirklich löschen?`,
			"Rezept löschen",
			{
				confirmButtonText: "Löschen",
				cancelButtonText: "Abbrechen",
				type: "warning"
			}
		);
	} catch {
		return;
	}

	deleting.value = true;

	try {
		await recipeService.deleteRecipe(householdId.value, recipe.id);
		recipes.value = recipes.value.filter((entry) => entry.id !== recipe.id);
		selectedRecipeId.value = filteredRecipes.value[0]?.id ?? null;
		ElMessage.success("Rezept gelöscht");
	} catch (e) {
		console.error(e);
		ElMessage.error(
			"Rezept konnte nicht gelöscht werden. Bitte erneut versuchen."
		);
	} finally {
		deleting.value = false;
	}
}

onMounted(loadRecipes);
</script>

<template>
	<div class="g-page">
		<div class="g-page-inner space-y-4">
			<header class="g-panel">
				<div class="grid grid-cols-[1fr_auto] items-center gap-3">
					<div>
						<h1 class="g-page-title">Rezepte</h1>
						<p class="g-page-sub">
							Rezept wählen und Zutaten direkt einkaufen
						</p>
					</div>

					<el-button
						type="primary"
						circle
						size="large"
						title="Neues Rezept"
						@click="router.push('/recipes/new')"
					>
						<el-icon>
							<Plus />
						</el-icon>
					</el-button>
				</div>
			</header>

			<div class="grid grid-cols-1 gap-4 md:grid-cols-3">
				<section class="g-panel md:col-span-1">
					<div class="mb-3 flex items-center justify-between gap-3">
						<h2 class="font-display text-lg font-bold text-ink">
							Deine Rezepte
						</h2>
						<span class="g-chip g-chip-success">
							{{ filteredRecipes.length }}
						</span>
					</div>

					<el-input
						v-model="search"
						class="mb-3"
						size="large"
						clearable
						placeholder="Rezepte suchen…"
					/>

					<div v-loading="loading">
						<div v-if="!loading && loadError" class="g-empty">
							<div class="g-empty-title">Laden fehlgeschlagen</div>
							<div class="g-empty-text">{{ loadError }}</div>
							<el-button
								class="mt-4"
								type="primary"
								round
								@click="loadRecipes"
							>
								Erneut versuchen
							</el-button>
						</div>

						<div
							v-else-if="!loading && filteredRecipes.length === 0"
							class="g-empty"
						>
							<div class="g-empty-title">Keine Rezepte</div>
							<div class="g-empty-text">
								Erstelle oben rechts dein erstes Rezept.
							</div>
						</div>

						<ul v-else class="recipe-list">
							<li v-for="recipe in filteredRecipes" :key="recipe.id">
								<button
									type="button"
									class="recipe-item"
									:class="{
										'recipe-item--active':
											selectedRecipe?.id === recipe.id
									}"
									@click="selectRecipe(recipe.id)"
								>
									<span class="recipe-item-title">
										{{ recipe.title }}
									</span>
									<span class="recipe-item-meta">
										{{ recipe.ingredients?.length ?? 0 }}
										Zutaten
										<template v-if="recipe.prepTimeMinutes">
											· {{ recipe.prepTimeMinutes }} Min.
										</template>
									</span>
								</button>
							</li>
						</ul>
					</div>
				</section>

				<section class="g-panel md:col-span-2">
					<div v-if="!selectedRecipe" class="g-empty">
						<div class="g-empty-title">Wähle links ein Rezept</div>
						<div class="g-empty-text">
							Danach kannst du die Zutaten zur Einkaufsliste
							hinzufügen.
						</div>
					</div>

					<template v-else>
						<div
							class="mb-4 grid grid-cols-[1fr_auto] items-start gap-3"
						>
							<div class="min-w-0">
								<h2
									class="truncate font-display text-xl font-bold leading-snug text-ink pb-0.5"
								>
									{{ selectedName }}
								</h2>
								<p class="mt-2 text-sm leading-relaxed text-muted">
									{{
										selectedRecipe.description ||
										"Keine Beschreibung vorhanden."
									}}
								</p>
							</div>

							<div class="flex items-center justify-end">
								<el-button
									circle
									plain
									title="Rezept bearbeiten"
									@click="goToRecipeEdit(selectedRecipe.id)"
								>
									<el-icon :size="18">
										<Edit />
									</el-icon>
								</el-button>

								<el-button
									circle
									plain
									class="!ml-1"
									title="Rezept löschen"
									:loading="deleting"
									@click="deleteSelectedRecipe"
								>
									<el-icon :size="14" class="text-danger">
										<Delete />
									</el-icon>
								</el-button>
							</div>
						</div>

						<div class="cta-bar mb-4">
							<div>
								<p class="cta-title">Zutaten einkaufen</p>
								<p class="cta-sub">
									{{ selectedIngredients.length }} Zutaten für
									{{ selectedRecipe.baseServings }} Personen
									vorbereitet.
								</p>
							</div>
							<el-button
								type="primary"
								size="large"
								round
								class="cta-button"
								:disabled="selectedIngredients.length === 0"
								@click="openAddToListDialog"
							>
								<el-icon class="mr-1"><ShoppingCart /></el-icon>
								Zur Einkaufsliste
							</el-button>
						</div>

						<div class="segmented" role="tablist">
							<button
								type="button"
								role="tab"
								class="segment"
								:class="{
									'segment--active':
										contentTab === 'ingredients'
								}"
								:aria-selected="contentTab === 'ingredients'"
								@click="contentTab = 'ingredients'"
							>
								Zutaten ({{ selectedIngredients.length }})
							</button>
							<button
								type="button"
								role="tab"
								class="segment"
								:class="{
									'segment--active': contentTab === 'steps'
								}"
								:aria-selected="contentTab === 'steps'"
								@click="contentTab = 'steps'"
							>
								Zubereitung ({{ selectedSteps.length }})
							</button>
						</div>

						<div
							v-if="contentTab === 'ingredients'"
							class="mt-4"
						>
							<div
								v-if="selectedIngredients.length === 0"
								class="g-empty"
							>
								<div class="g-empty-title">Keine Zutaten</div>
							</div>

							<ul v-else class="ingredient-list">
								<li
									v-for="ingredient in selectedIngredients"
									:key="
										ingredient.id ?? ingredient.sortIndex
									"
									class="ingredient-row"
								>
									<span class="ingredient-name">
										{{ ingredient.name }}
									</span>
									<span class="ingredient-qty">
										<template
											v-if="
												ingredient.quantity !== null ||
												ingredient.unitText
											"
										>
											{{ ingredient.quantity }}
											{{ ingredient.unitText }}
										</template>
									</span>
								</li>
							</ul>
						</div>

						<div v-else class="mt-4">
							<div
								v-if="selectedSteps.length === 0"
								class="g-empty"
							>
								<div class="g-empty-title">Keine Schritte</div>
							</div>

							<ol v-else class="space-y-3">
								<li
									v-for="(step, index) in selectedSteps"
									:key="step.id ?? step.sortIndex"
									class="grid grid-cols-[auto_minmax(0,1fr)] gap-3 rounded-xl border border-border bg-slate-50 p-3"
								>
									<span
										class="flex h-7 w-7 items-center justify-center rounded-full bg-primary-soft text-sm font-semibold text-primary-deep"
									>
										{{ index + 1 }}
									</span>
									<p class="text-sm leading-6 text-ink">
										{{ step.instruction }}
									</p>
								</li>
							</ol>
						</div>
					</template>
				</section>
			</div>

			<el-dialog
				v-model="addDialogOpen"
				title="Zur Einkaufsliste hinzufügen"
				width="92%"
				class="max-w-[420px]"
				align-center
			>
				<p v-if="selectedRecipe" class="mb-4 text-sm text-muted">
					Alle Zutaten von „{{ selectedRecipe.title }}“ werden für
					{{ targetServings }}
					{{ targetServings === 1 ? "Person" : "Personen" }}
					übernommen.
				</p>

				<el-form label-position="top" v-loading="listsLoading">
					<el-form-item label="Einkaufsliste">
						<el-select
							v-model="selectedListId"
							size="large"
							class="!w-full"
							placeholder="Liste auswählen"
						>
							<el-option
								v-for="list in lists"
								:key="list.id"
								:label="list.title"
								:value="list.id"
							/>
						</el-select>
					</el-form-item>

					<el-form-item label="Personen">
						<el-input-number
							v-model="targetServings"
							:min="1"
							size="large"
							class="!w-full"
						/>
					</el-form-item>
				</el-form>

				<template #footer>
					<el-button @click="addDialogOpen = false">
						Abbrechen
					</el-button>
					<el-button
						type="primary"
						:loading="addingToList"
						:disabled="!selectedListId"
						@click="addToShoppingList"
					>
						Zutaten hinzufügen
					</el-button>
				</template>
			</el-dialog>
		</div>
	</div>
</template>

<style scoped>
.recipe-list {
	margin: 0;
	padding: 0;
	list-style: none;
	display: flex;
	flex-direction: column;
	gap: 8px;
}

.recipe-item {
	display: flex;
	width: 100%;
	flex-direction: column;
	align-items: flex-start;
	gap: 2px;
	padding: 12px 14px;
	border: 1px solid rgba(15, 23, 42, 0.06);
	border-radius: 0.9rem;
	background: #f8fafc;
	text-align: left;
	cursor: pointer;
	transition:
		background 0.15s ease,
		border-color 0.15s ease;
}

.recipe-item:hover {
	background: #eff6ff;
	border-color: #bfdbfe;
}

.recipe-item--active {
	background: rgba(219, 234, 254, 0.8);
	border-color: #93c5fd;
}

.recipe-item-title {
	font-weight: 600;
	color: #0f172a;
}

.recipe-item--active .recipe-item-title {
	color: #1d4ed8;
}

.recipe-item-meta {
	font-size: 0.75rem;
	color: #64748b;
}

.cta-bar {
	display: flex;
	flex-wrap: wrap;
	align-items: center;
	justify-content: space-between;
	gap: 16px;
	padding: 16px 18px;
	border-radius: 1rem;
	border: 1px solid #bfdbfe;
	background: linear-gradient(135deg, #eff6ff, #dbeafe);
}

.cta-title {
	margin: 0;
	font-family: Figtree, ui-sans-serif, system-ui, sans-serif;
	font-size: 1.05rem;
	font-weight: 700;
	color: #1e3a8a;
}

.cta-sub {
	margin: 4px 0 0;
	font-size: 0.85rem;
	color: #1d4ed8;
}

.cta-button {
	min-width: 200px;
	box-shadow: 0 8px 20px rgba(37, 99, 235, 0.25);
}

.segmented {
	display: grid;
	grid-template-columns: 1fr 1fr;
	gap: 4px;
	padding: 4px;
	border-radius: 9999px;
	background: #f1f5f9;
}

.segment {
	padding: 10px 12px;
	border: none;
	border-radius: 9999px;
	background: transparent;
	font-size: 0.875rem;
	font-weight: 600;
	color: #64748b;
	cursor: pointer;
}

.segment--active {
	background: #fff;
	color: #1d4ed8;
	box-shadow: 0 1px 3px rgba(15, 23, 42, 0.08);
}

.ingredient-list {
	margin: 0;
	padding: 0;
	list-style: none;
}

.ingredient-row {
	display: grid;
	grid-template-columns: minmax(0, 1fr) auto;
	gap: 12px;
	align-items: baseline;
	padding: 12px 0;
	border-bottom: 1px solid #f1f5f9;
}

.ingredient-row:last-child {
	border-bottom: none;
}

.ingredient-name {
	color: #0f172a;
	font-weight: 500;
}

.ingredient-qty {
	font-size: 0.9rem;
	font-variant-numeric: tabular-nums;
	font-weight: 600;
	color: #475569;
	white-space: nowrap;
}
</style>
