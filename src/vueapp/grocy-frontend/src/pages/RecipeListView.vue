<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { recipeService } from "@/services/recepieService";
import type { Recipe } from "@/models/Recipe";
import { useHouseholdStore } from "@/stores/householdStore";

const router = useRouter();
const householdStore = useHouseholdStore();

const loading = ref(false);
const search = ref("");
const recipes = ref<Recipe[]>([]);
const selectedRecipeId = ref<string | null>(null);
const activeTab = ref("ingredients");
const deleting = ref(false);

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

const recipeCountLabel = computed(() => {
	const count = filteredRecipes.value.length;
	return count === 1 ? "1 Rezept" : `${count} Rezepte`;
});

async function loadRecipes() {
	if (!householdId.value) {
		return;
	}

	loading.value = true;

	try {
		const response = await recipeService.getAll(householdId.value);
		recipes.value = response.data;
		selectedRecipeId.value = recipes.value[0]?.id ?? null;
	} finally {
		loading.value = false;
	}
}

function selectRecipe(id: string) {
	selectedRecipeId.value = id;
}

function goToRecipe(id?: string) {
	if (!id) {
		return;
	}

	router.push(`/recipes/${id}`);
}

function goToRecipeEdit(id?: string) {
	if (!id) {
		return;
	}

	router.push(`/recipes/${id}/edit`);
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
	} finally {
		deleting.value = false;
	}
}

onMounted(loadRecipes);
</script>

<template>
	<div class="min-h-screen bg-gray-50">
		<div class="mx-auto max-w-5xl p-4">
			<header class="mb-4 rounded-2xl bg-white p-4 shadow-sm">
				<div class="grid grid-cols-[1fr_auto] items-center gap-3">
					<div>
						<h1 class="text-2xl font-bold leading-tight">
							Rezepte
						</h1>

						<p class="mt-1 text-sm text-gray-500">
							Verwalte deine Rezeptsammlung
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
				<section
					class="rounded-2xl bg-white p-4 shadow-sm md:col-span-1"
				>
					<div class="mb-4 flex items-center justify-between gap-3">
						<h2 class="text-lg font-bold">Deine Rezepte</h2>

						<span
							class="inline-flex h-8 min-w-8 items-center justify-center rounded-full bg-emerald-50 px-3 text-sm font-semibold text-emerald-700"
						>
							{{ filteredRecipes.length }}
						</span>
					</div>

					<el-input
						v-model="search"
						class="mb-4"
						size="large"
						clearable
						placeholder="Rezepte suchen..."
					/>

					<div v-loading="loading">
						<div
							v-if="!loading && filteredRecipes.length === 0"
							class="py-10 text-center text-gray-500"
						>
							<div class="mb-1 text-lg font-semibold">
								Keine Rezepte
							</div>

							<div class="text-sm">
								Erstelle oben rechts dein erstes Rezept.
							</div>
						</div>

						<el-menu
							v-else
							:default-active="selectedRecipe?.id"
							class="space-y-2 !border-r-0 [&_.el-menu-item.is-active]:!border-blue-200 [&_.el-menu-item.is-active]:!bg-blue-100/70 [&_.el-menu-item]:!h-auto [&_.el-menu-item]:!rounded-xl [&_.el-menu-item]:!leading-snug [&_.el-menu-item]:!text-gray-900"
							@select="selectRecipe"
						>
							<el-menu-item
								v-for="recipe in filteredRecipes"
								:key="recipe.id"
								:index="recipe.id"
								class="!border !border-gray-100 !bg-gray-50 !py-2.5 hover:!bg-blue-50"
							>
								<div class="min-w-0">
									<span class="block truncate font-medium">
										{{ recipe.title }}
									</span>

									<span
										class="block truncate text-xs text-gray-500"
									>
										{{ recipe.ingredients?.length ?? 0 }}
										Zutaten
									</span>
								</div>
							</el-menu-item>
						</el-menu>
					</div>
				</section>

				<section
					class="rounded-2xl bg-white p-4 shadow-sm md:col-span-2"
				>
					<div
						class="mb-4 grid grid-cols-[1fr_auto] items-start gap-3"
					>
						<div class="min-w-0">
							<h2
								class="truncate text-xl font-bold leading-tight"
							>
								{{ selectedName }}
							</h2>

							<p
								v-if="selectedRecipe"
								class="mt-1 text-sm text-gray-500"
							>
								{{ recipeCountLabel }}
							</p>
						</div>

						<div class="flex items-center justify-end gap-1">
							<el-button
								circle
								plain
								title="Rezept öffnen"
								:disabled="!selectedRecipe"
								@click="goToRecipe(selectedRecipe?.id)"
							>
								<el-icon :size="18">
									<Document />
								</el-icon>
							</el-button>

							<el-button
								circle
								plain
								class="!ml-0"
								title="Rezept bearbeiten"
								:disabled="!selectedRecipe"
								@click="goToRecipeEdit(selectedRecipe?.id)"
							>
								<el-icon :size="18">
									<Edit />
								</el-icon>
							</el-button>

							<el-button
								circle
								plain
								class="!ml-0"
								title="Rezept löschen"
								:loading="deleting"
								:disabled="!selectedRecipe"
								@click="deleteSelectedRecipe"
							>
								<el-icon :size="16" class="text-red-500">
									<Delete />
								</el-icon>
							</el-button>
						</div>
					</div>

					<div
						v-if="!selectedRecipe"
						class="py-10 text-center text-gray-500"
					>
						<div class="mb-1 text-lg font-semibold">
							Wähle links ein Rezept
						</div>
					</div>

					<template v-else>
						<p class="mb-4 text-sm text-gray-500">
							{{
								selectedRecipe.description ||
								"Keine Beschreibung vorhanden."
							}}
						</p>

						<div class="mb-4 flex flex-wrap gap-2">
							<span
								class="inline-flex h-7 items-center gap-1.5 rounded-full border border-gray-200 bg-white px-3 text-sm leading-none text-gray-700"
							>
								<el-icon :size="14" class="shrink-0">
									<User />
								</el-icon>
								{{ selectedRecipe.baseServings }} Pers.
							</span>

							<span
								v-if="selectedRecipe.prepTimeMinutes"
								class="inline-flex h-7 items-center gap-1.5 rounded-full border border-blue-100 bg-blue-50 px-3 text-sm leading-none text-blue-700"
							>
								<el-icon :size="14" class="shrink-0">
									<Timer />
								</el-icon>
								{{ selectedRecipe.prepTimeMinutes }} Min.
							</span>

							<span
								class="inline-flex h-7 items-center rounded-full border border-emerald-100 bg-emerald-50 px-3 text-sm leading-none text-emerald-700"
							>
								{{ selectedIngredients.length }} Zutaten
							</span>
						</div>

						<el-tabs
							v-model="activeTab"
							type="border-card"
							class="overflow-hidden rounded-2xl"
						>
							<el-tab-pane name="ingredients">
								<template #label>
									<span
										class="inline-flex items-center gap-1.5"
									>
										<el-icon>
											<List />
										</el-icon>
										<span>Zutaten</span>
									</span>
								</template>

								<div
									v-if="selectedIngredients.length === 0"
									class="py-10 text-center text-gray-500"
								>
									<div class="mb-1 text-lg font-semibold">
										Keine Zutaten
									</div>
								</div>

								<ul v-else class="divide-y divide-gray-100">
									<li
										v-for="ingredient in selectedIngredients"
										:key="
											ingredient.id ??
											ingredient.sortIndex
										"
										class="grid grid-cols-[minmax(0,1fr)_auto] items-center gap-3 py-2"
									>
										<span class="truncate text-gray-900">
											{{ ingredient.name }}
										</span>

										<span
											class="text-sm tabular-nums text-gray-500"
										>
											<template
												v-if="
													ingredient.quantity !==
														null ||
													ingredient.unitText
												"
											>
												{{ ingredient.quantity }}
												{{ ingredient.unitText }}
											</template>
										</span>
									</li>
								</ul>
							</el-tab-pane>

							<el-tab-pane name="steps">
								<template #label>
									<span
										class="inline-flex items-center gap-1.5"
									>
										<el-icon>
											<List />
										</el-icon>
										<span>Schritte</span>
									</span>
								</template>

								<div
									v-if="selectedSteps.length === 0"
									class="py-10 text-center text-gray-500"
								>
									<div class="mb-1 text-lg font-semibold">
										Keine Schritte
									</div>
								</div>

								<ol v-else class="space-y-3">
									<li
										v-for="(step, index) in selectedSteps"
										:key="step.id ?? step.sortIndex"
										class="grid grid-cols-[auto_minmax(0,1fr)] gap-3 rounded-xl border border-gray-100 bg-gray-50 p-3"
									>
										<span
											class="font-semibold text-gray-500"
										>
											{{ index + 1 }}.
										</span>

										<p class="text-sm text-gray-700">
											{{ step.instruction }}
										</p>
									</li>
								</ol>
							</el-tab-pane>
						</el-tabs>
					</template>
				</section>
			</div>
		</div>
	</div>
</template>
