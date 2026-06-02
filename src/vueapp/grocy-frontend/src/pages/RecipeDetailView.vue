<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { householdService } from "@/services/householdService";
import { recipeService } from "@/services/recepieService";
import type { List } from "@/models/List";
import type { Recipe } from "@/models/Recipe";
import { useHouseholdStore } from "@/stores/householdStore";

const route = useRoute();
const router = useRouter();
const householdStore = useHouseholdStore();

const recipe = ref<Recipe | null>(null);
const loading = ref(false);
const addDialogOpen = ref(false);
const addingToList = ref(false);
const deleting = ref(false);
const listsLoading = ref(false);
const lists = ref<List[]>([]);
const selectedListId = ref("");
const targetServings = ref(1);

const householdId = computed(() => householdStore.activeHousehold?.id);
const recipeId = computed(() => String(route.params.id));

const factor = computed(() => {
	if (!recipe.value) {
		return 1;
	}

	return targetServings.value / recipe.value.baseServings;
});

const scaledIngredients = computed(() => {
	if (!recipe.value) {
		return [];
	}

	return recipe.value.ingredients.map((ingredient) => ({
		...ingredient,
		scaledQuantity:
			ingredient.quantity === null
				? null
				: Math.round(ingredient.quantity * factor.value * 100) / 100
	}));
});

function formatQuantity(value: number | null | undefined) {
	if (value === null || value === undefined) {
		return "";
	}

	return Number.isInteger(value)
		? value.toString()
		: value.toFixed(2).replace(/\.?0+$/, "");
}

async function loadRecipe() {
	if (!householdId.value) {
		return;
	}

	loading.value = true;

	try {
		const response = await recipeService.getById(
			householdId.value,
			recipeId.value
		);
		recipe.value = response.data;
		targetServings.value = response.data.baseServings;
	} finally {
		loading.value = false;
	}
}

async function openAddToListDialog() {
	if (!householdId.value) {
		return;
	}

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
	if (!householdId.value || !recipe.value) {
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
			recipe.value.id,
			{
				listId: selectedListId.value,
				servings: targetServings.value
			}
		);

		addDialogOpen.value = false;
		ElMessage.success(`${response.data.added} Zutaten hinzugefügt`);
	} finally {
		addingToList.value = false;
	}
}

async function deleteRecipe() {
	if (!householdId.value || !recipe.value) {
		return;
	}

	try {
		await ElMessageBox.confirm(
			`"${recipe.value.title}" wirklich löschen?`,
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
		await recipeService.deleteRecipe(householdId.value, recipe.value.id);
		ElMessage.success("Rezept gelöscht");
		router.push("/recipes");
	} finally {
		deleting.value = false;
	}
}

onMounted(loadRecipe);
</script>

<template>
	<div class="min-h-screen bg-gray-50">
		<div class="mx-auto max-w-5xl p-4" v-loading="loading">
			<template v-if="recipe">
				<header class="mb-4 rounded-2xl bg-white p-4 shadow-sm">
					<div
						class="grid grid-cols-1 gap-4 sm:grid-cols-[minmax(0,1fr)_auto] sm:items-start"
					>
						<div class="min-w-0">
							<p class="text-sm text-gray-500">Rezept</p>

							<h1
								class="mt-1 truncate text-2xl font-bold leading-tight text-gray-900"
							>
								{{ recipe.title }}
							</h1>

							<p class="mt-2 max-w-3xl text-sm text-gray-500">
								{{
									recipe.description ||
									"Keine Beschreibung vorhanden."
								}}
							</p>
						</div>

						<div
							class="flex flex-wrap items-center justify-start gap-2 sm:justify-end"
						>
							<el-button
								type="primary"
								round
								class="!ml-0"
								@click="openAddToListDialog"
							>
								Zur Einkaufsliste
							</el-button>

							<el-button
								circle
								plain
								class="!ml-0"
								title="Zurück"
								@click="router.push('/recipes')"
							>
								<el-icon :size="18">
									<ArrowLeft />
								</el-icon>
							</el-button>

							<el-button
								circle
								plain
								class="!ml-0"
								title="Rezept bearbeiten"
								@click="
									router.push(`/recipes/${recipe.id}/edit`)
								"
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
								@click="deleteRecipe"
							>
								<el-icon :size="16" class="text-red-500">
									<Delete />
								</el-icon>
							</el-button>
						</div>
					</div>

					<div class="mt-4 flex flex-wrap gap-2">
						<span
							class="inline-flex h-7 items-center gap-1.5 rounded-full border border-gray-200 bg-white px-3 text-sm leading-none text-gray-700"
						>
							<el-icon :size="14" class="shrink-0">
								<User />
							</el-icon>
							Basis: {{ recipe.baseServings }} Pers.
						</span>

						<span
							v-if="recipe.prepTimeMinutes"
							class="inline-flex h-7 items-center gap-1.5 rounded-full border border-blue-100 bg-blue-50 px-3 text-sm leading-none text-blue-700"
						>
							<el-icon :size="14" class="shrink-0">
								<Timer />
							</el-icon>
							{{ recipe.prepTimeMinutes }} Min.
						</span>

						<span
							class="inline-flex h-7 items-center rounded-full border border-emerald-100 bg-emerald-50 px-3 text-sm leading-none text-emerald-700"
						>
							{{ recipe.ingredients.length }} Zutaten
						</span>
					</div>
				</header>

				<div class="grid grid-cols-1 gap-4 lg:grid-cols-2">
					<section class="rounded-2xl bg-white p-4 shadow-sm">
						<div
							class="mb-4 flex items-center justify-between gap-3"
						>
							<h2 class="text-lg font-bold">Zutaten</h2>

							<div class="flex items-center gap-2">
								<span class="text-sm text-gray-500"
									>Personen</span
								>

								<el-input-number
									v-model="targetServings"
									:min="1"
									size="small"
									class="!w-28"
								/>
							</div>
						</div>

						<div
							v-if="scaledIngredients.length === 0"
							class="py-10 text-center text-gray-500"
						>
							<div class="mb-1 text-lg font-semibold">
								Keine Zutaten
							</div>
						</div>

						<ul v-else class="divide-y divide-gray-100">
							<li
								v-for="ingredient in scaledIngredients"
								:key="ingredient.id ?? ingredient.sortIndex"
								class="grid grid-cols-[minmax(0,1fr)_auto] items-center gap-3 py-2"
							>
								<span class="truncate text-gray-900">
									{{ ingredient.name }}
								</span>

								<span
									class="whitespace-nowrap text-sm tabular-nums text-gray-500"
								>
									{{
										formatQuantity(
											ingredient.scaledQuantity
										)
									}}
									{{ ingredient.unitText ?? "" }}
								</span>
							</li>
						</ul>
					</section>

					<section class="rounded-2xl bg-white p-4 shadow-sm">
						<div
							class="mb-4 flex items-center justify-between gap-3"
						>
							<h2 class="text-lg font-bold">Zubereitung</h2>
						</div>

						<div
							v-if="recipe.steps.length === 0"
							class="py-10 text-center text-gray-500"
						>
							<div class="mb-1 text-lg font-semibold">
								Keine Schritte
							</div>
						</div>

						<ol v-else class="space-y-3">
							<li
								v-for="(step, index) in recipe.steps"
								:key="step.id ?? index"
								class="grid grid-cols-[auto_minmax(0,1fr)] gap-3 rounded-xl border border-gray-100 bg-gray-50 p-3"
							>
								<span
									class="flex h-7 w-7 items-center justify-center rounded-full bg-blue-50 text-sm font-semibold text-blue-700"
								>
									{{ index + 1 }}
								</span>

								<p class="text-sm leading-6 text-gray-700">
									{{ step.instruction }}
								</p>
							</li>
						</ol>
					</section>
				</div>

				<el-dialog
					v-model="addDialogOpen"
					title="Zur Einkaufsliste"
					width="92%"
					class="max-w-[420px]"
					align-center
				>
					<el-form label-position="top" v-loading="listsLoading">
						<el-form-item label="Liste">
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
			</template>
		</div>
	</div>
</template>
