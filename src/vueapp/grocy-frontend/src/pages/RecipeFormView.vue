<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { Delete, Plus } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { recipeService } from "@/services/recepieService";
import type { RecipeCreate, RecipeIngredient } from "@/models/Recipe";
import { useHouseholdStore } from "@/stores/householdStore";

type RecipeForm = Omit<RecipeCreate, "ingredients"> & {
	ingredients: Array<Omit<RecipeIngredient, "quantity"> & {
		quantity: number | string | null;
	}>;
};

const router = useRouter();
const route = useRoute();
const householdStore = useHouseholdStore();

const householdId = computed(() => householdStore.activeHousehold?.id);
const recipeId = computed(() =>
	typeof route.params.id === "string" ? route.params.id : undefined
);
const isEditMode = computed(() => !!recipeId.value);
const pageEyebrow = computed(() =>
	isEditMode.value ? "Rezept bearbeiten" : "Neues Rezept"
);
const pageTitle = computed(() =>
	isEditMode.value ? "Rezept bearbeiten" : "Rezept anlegen"
);
const submitLabel = computed(() =>
	isEditMode.value ? "Änderungen speichern" : "Rezept speichern"
);
const loading = ref(false);
const saving = ref(false);

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

const form = reactive<RecipeForm>({
	title: "",
	description: "",
	baseServings: 4,
	prepTimeMinutes: undefined,
	ingredients: [{ name: "", quantity: null, unitText: "g", sortIndex: 0 }],
	steps: [{ instruction: "", sortIndex: 0 }]
});

async function loadRecipe() {
	if (!householdId.value || !recipeId.value) {
		return;
	}

	loading.value = true;

	try {
		const response = await recipeService.getById(
			householdId.value,
			recipeId.value
		);
		const recipe = response.data;

		form.title = recipe.title;
		form.description = recipe.description ?? "";
		form.baseServings = recipe.baseServings;
		form.prepTimeMinutes = recipe.prepTimeMinutes;
		form.ingredients = recipe.ingredients.length
			? recipe.ingredients.map((ingredient, index) => ({
					name: ingredient.name,
					quantity: ingredient.quantity,
					unitText: ingredient.unitText ?? "",
					sortIndex: index
				}))
			: [{ name: "", quantity: null, unitText: "", sortIndex: 0 }];
		form.steps = recipe.steps.length
			? recipe.steps.map((step, index) => ({
					instruction: step.instruction,
					sortIndex: index
				}))
			: [{ instruction: "", sortIndex: 0 }];
	} finally {
		loading.value = false;
	}
}

function addIngredient() {
	form.ingredients.push({
		name: "",
		quantity: null,
		unitText: "",
		sortIndex: form.ingredients.length
	});
}

function removeIngredient(index: number) {
	form.ingredients.splice(index, 1);
	form.ingredients.forEach((ingredient, i) => {
		ingredient.sortIndex = i;
	});
}

function addStep() {
	form.steps.push({
		instruction: "",
		sortIndex: form.steps.length
	});
}

function removeStep(index: number) {
	form.steps.splice(index, 1);
	form.steps.forEach((step, i) => {
		step.sortIndex = i;
	});
}

function queryUnits(
	queryString: string,
	callback: (results: { value: string }[]) => void
) {
	const results = unitOptions
		.filter((unit) =>
			unit.toLowerCase().includes(queryString.toLowerCase())
		)
		.map((unit) => ({ value: unit }));

	callback(results);
}

async function submit() {
	if (!householdId.value) {
		ElMessage.error("Kein Haushalt ausgewählt");
		return;
	}

	if (!form.title.trim()) {
		ElMessage.error("Bitte gib einen Titel ein");
		return;
	}

	saving.value = true;

	try {
		const payload = normalizeRecipeForm();
		const response =
			isEditMode.value && recipeId.value
				? await recipeService.updateRecipe(
						householdId.value,
						recipeId.value,
						payload
					)
				: await recipeService.createRecipe(householdId.value, payload);

		ElMessage.success(
			isEditMode.value ? "Rezept gespeichert" : "Rezept erstellt"
		);
		router.push(`/recipes/${response.data.id}`);
	} finally {
		saving.value = false;
	}
}

function normalizeRecipeForm(): RecipeCreate {
	return {
		...form,
		ingredients: form.ingredients.map((ingredient) => ({
			...ingredient,
			quantity: normalizeQuantity(ingredient.quantity),
			unitText: normalizeUnitText(ingredient.unitText)
		}))
	};
}

function normalizeQuantity(value: number | string | null | undefined) {
	if (value === null || value === undefined || value === "") {
		return null;
	}

	const numberValue = Number(value);
	return Number.isNaN(numberValue) ? null : numberValue;
}

function normalizeUnitText(value: string | null | undefined) {
	const normalized = (value ?? "").toString().trim();
	return normalized.length > 0 ? normalized : null;
}

onMounted(loadRecipe);
</script>

<template>
	<div class="min-h-screen bg-gray-50">
		<div class="mx-auto max-w-5xl p-4" v-loading="loading">
			<header class="mb-4 rounded-2xl bg-white p-4 shadow-sm">
				<div class="grid grid-cols-[minmax(0,1fr)_auto] items-center gap-3">
					<div class="min-w-0">
						<p class="text-sm text-gray-500">{{ pageEyebrow }}</p>

						<h1 class="mt-1 truncate text-2xl font-bold leading-tight text-gray-900">
							{{ pageTitle }}
						</h1>
					</div>

					<el-button
						circle
						plain
						title="Zurück"
						@click="router.back()"
					>
						<el-icon :size="18">
							<ArrowLeft />
						</el-icon>
					</el-button>
				</div>
			</header>

			<section class="rounded-2xl bg-white p-4 shadow-sm">
				<el-form label-position="top">
				<div class="grid gap-3 md:grid-cols-[minmax(0,1fr)_140px_170px]">
					<el-form-item label="Titel" class="!mb-2">
						<el-input
							v-model="form.title"
							size="default"
							placeholder="z. B. Spaghetti Carbonara"
						/>
					</el-form-item>

					<el-form-item label="Personen" class="!mb-2">
						<el-input-number
							v-model="form.baseServings"
							:min="1"
							size="default"
							class="!w-full"
							controls-position="right"
						/>
					</el-form-item>

					<el-form-item label="Minuten" class="!mb-2">
						<el-input-number
							v-model="form.prepTimeMinutes"
							:min="1"
							size="default"
							class="!w-full"
							controls-position="right"
						/>
					</el-form-item>
				</div>

				<el-form-item label="Beschreibung" class="!mb-2">
					<el-input
						v-model="form.description"
						type="textarea"
						:rows="2"
						placeholder="Kurze Beschreibung..."
					/>
				</el-form-item>

				<el-divider />

				<div class="mb-3 flex items-center justify-between">
					<h2 class="text-lg font-semibold text-gray-900">Zutaten</h2>

					<el-button :icon="Plus" round @click="addIngredient">
						Zutat
					</el-button>
				</div>

				<div class="space-y-3">
					<div
						v-for="(ingredient, index) in form.ingredients"
						:key="index"
						class="grid gap-2 rounded-xl border border-gray-100 bg-gray-50 p-3 sm:grid-cols-[minmax(0,1fr)_110px_130px_auto]"
					>
						<el-input
							v-model="ingredient.name"
							placeholder="Name"
							class="sm:col-auto"
						/>

						<el-input
							v-model="ingredient.quantity"
							type="number"
							:min="0"
							placeholder="Menge"
							clearable
							class="!w-full"
						/>

						<el-autocomplete
							v-model="ingredient.unitText"
							:fetch-suggestions="queryUnits"
							placeholder="Einheit"
							clearable
							class="!w-full"
						/>

						<el-button
							:icon="Delete"
							circle
							text
							type="danger"
							class="justify-self-end sm:justify-self-auto"
							@click="removeIngredient(index)"
						/>
					</div>
				</div>

				<el-divider />

				<div class="mb-3 flex items-center justify-between">
					<h2 class="text-lg font-semibold text-gray-900">
						Zubereitung
					</h2>

					<el-button :icon="Plus" round @click="addStep">
						Schritt
					</el-button>
				</div>

				<div class="space-y-3">
					<div
						v-for="(step, index) in form.steps"
						:key="index"
						class="grid grid-cols-[auto_minmax(0,1fr)_auto] gap-2 rounded-xl border border-gray-100 bg-gray-50 p-3"
					>
						<div
							class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-sky-100 text-sm font-semibold text-sky-700"
						>
							{{ index + 1 }}
						</div>

						<el-input
							v-model="step.instruction"
							type="textarea"
							:rows="2"
							placeholder="Was passiert in diesem Schritt?"
						/>

						<el-button
							:icon="Delete"
							circle
							text
							type="danger"
							class="!ml-0"
							@click="removeStep(index)"
						/>
					</div>
				</div>

				<div class="mt-6 flex justify-end gap-2">
					<el-button round @click="router.back()"
						>Abbrechen</el-button
					>

					<el-button
						type="primary"
						round
						:loading="saving"
						@click="submit"
					>
						{{ submitLabel }}
					</el-button>
				</div>
				</el-form>
			</section>
		</div>
	</div>
</template>
