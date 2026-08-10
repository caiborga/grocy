<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { onBeforeRouteLeave, useRoute, useRouter } from "vue-router";
import { Delete, Plus } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
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
const isDirty = ref(false);
const savedSuccessfully = ref(false);

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
	} catch (e) {
		console.error(e);
		ElMessage.error(
			"Rezept konnte nicht geladen werden. Bitte zurück und erneut öffnen."
		);
	} finally {
		loading.value = false;
		isDirty.value = false;
	}
}

function markDirty() {
	isDirty.value = true;
}

function addIngredient() {
	form.ingredients.push({
		name: "",
		quantity: null,
		unitText: "",
		sortIndex: form.ingredients.length
	});
	markDirty();
}

function removeIngredient(index: number) {
	form.ingredients.splice(index, 1);
	form.ingredients.forEach((ingredient, i) => {
		ingredient.sortIndex = i;
	});
	markDirty();
}

function addStep() {
	form.steps.push({
		instruction: "",
		sortIndex: form.steps.length
	});
	markDirty();
}

function removeStep(index: number) {
	form.steps.splice(index, 1);
	form.steps.forEach((step, i) => {
		step.sortIndex = i;
	});
	markDirty();
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
		ElMessage.error(
			"Kein Haushalt ausgewählt. Bitte zuerst einen Haushalt wählen."
		);
		return;
	}

	if (!form.title.trim()) {
		ElMessage.error("Bitte gib einen Titel ein.");
		return;
	}

	const filledIngredients = form.ingredients.filter((ingredient) =>
		ingredient.name.trim()
	);
	const filledSteps = form.steps.filter((step) =>
		step.instruction.trim()
	);

	if (filledIngredients.length === 0) {
		ElMessage.error("Bitte mindestens eine Zutat mit Namen angeben.");
		return;
	}

	if (filledSteps.length === 0) {
		ElMessage.error("Bitte mindestens einen Zubereitungsschritt angeben.");
		return;
	}

	saving.value = true;

	try {
		const payload = normalizeRecipeForm(filledIngredients, filledSteps);
		const response =
			isEditMode.value && recipeId.value
				? await recipeService.updateRecipe(
						householdId.value,
						recipeId.value,
						payload
					)
				: await recipeService.createRecipe(householdId.value, payload);

		savedSuccessfully.value = true;
		isDirty.value = false;
		ElMessage.success(
			isEditMode.value ? "Rezept gespeichert" : "Rezept erstellt"
		);
		router.push("/recipes");
	} catch (e) {
		console.error(e);
		ElMessage.error(
			"Speichern fehlgeschlagen. Bitte Eingaben prüfen und erneut versuchen."
		);
	} finally {
		saving.value = false;
	}
}

function normalizeRecipeForm(
	ingredients = form.ingredients.filter((ingredient) =>
		ingredient.name.trim()
	),
	steps = form.steps.filter((step) => step.instruction.trim())
): RecipeCreate {
	return {
		title: form.title,
		description: form.description,
		baseServings: form.baseServings,
		prepTimeMinutes: form.prepTimeMinutes,
		ingredients: ingredients.map((ingredient, index) => ({
			...ingredient,
			sortIndex: index,
			quantity: normalizeQuantity(ingredient.quantity),
			unitText: normalizeUnitText(ingredient.unitText)
		})),
		steps: steps.map((step, index) => ({
			...step,
			sortIndex: index
		}))
	};
}

async function cancelForm() {
	if (isDirty.value && !savedSuccessfully.value) {
		try {
			await ElMessageBox.confirm(
				"Ungespeicherte Änderungen verwerfen?",
				"Abbrechen",
				{
					confirmButtonText: "Verwerfen",
					cancelButtonText: "Weiter bearbeiten",
					type: "warning"
				}
			);
		} catch {
			return;
		}
	}

	router.back();
}

onBeforeRouteLeave(async () => {
	if (!isDirty.value || savedSuccessfully.value || saving.value) {
		return true;
	}

	try {
		await ElMessageBox.confirm(
			"Ungespeicherte Änderungen verwerfen?",
			"Seite verlassen",
			{
				confirmButtonText: "Verwerfen",
				cancelButtonText: "Bleiben",
				type: "warning"
			}
		);
		return true;
	} catch {
		return false;
	}
});

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
	<div class="g-page">
		<div class="g-page-inner space-y-4" v-loading="loading">
			<header class="g-panel">
				<div class="grid grid-cols-[minmax(0,1fr)_auto] items-center gap-3">
					<div class="min-w-0">
						<p class="text-sm font-semibold text-primary">
							{{ pageEyebrow }}
						</p>

						<h1 class="g-page-title mt-1 truncate">
							{{ pageTitle }}
						</h1>
					</div>

					<el-button
						circle
						plain
						title="Zurück"
						@click="cancelForm"
					>
						<el-icon :size="18">
							<ArrowLeft />
						</el-icon>
					</el-button>
				</div>
			</header>

			<section class="g-panel">
				<el-form label-position="top" @input="markDirty">
				<div class="grid gap-3 md:grid-cols-[minmax(0,1fr)_140px_170px]">
					<el-form-item label="Titel" class="!mb-2">
						<el-input
							v-model="form.title"
							size="default"
							placeholder="z. B. Spaghetti Carbonara"
							@input="markDirty"
						/>
					</el-form-item>

					<el-form-item label="Personen" class="!mb-2">
						<el-input-number
							v-model="form.baseServings"
							:min="1"
							size="default"
							class="!w-full"
							controls-position="right"
							@change="markDirty"
						/>
					</el-form-item>

					<el-form-item label="Minuten" class="!mb-2">
						<el-input-number
							v-model="form.prepTimeMinutes"
							:min="1"
							size="default"
							class="!w-full"
							controls-position="right"
							@change="markDirty"
						/>
					</el-form-item>
				</div>

				<el-form-item label="Beschreibung" class="!mb-2">
					<el-input
						v-model="form.description"
						type="textarea"
						:rows="2"
						placeholder="Kurze Beschreibung…"
						@input="markDirty"
					/>
				</el-form-item>

				<el-divider />

				<h2 class="mb-3 font-display text-lg font-semibold text-ink">
					Zutaten
				</h2>

				<div class="space-y-3">
					<div
						v-for="(ingredient, index) in form.ingredients"
						:key="index"
						class="grid gap-2 rounded-xl border border-border bg-slate-50 p-3 sm:grid-cols-[minmax(0,1fr)_110px_130px_auto]"
					>
						<el-input
							v-model="ingredient.name"
							placeholder="Name"
							class="sm:col-auto"
							@input="markDirty"
						/>

						<el-input
							v-model="ingredient.quantity"
							type="number"
							:min="0"
							placeholder="Menge"
							clearable
							class="!w-full"
							@input="markDirty"
						/>

						<el-autocomplete
							v-model="ingredient.unitText"
							:fetch-suggestions="queryUnits"
							placeholder="Einheit"
							clearable
							class="!w-full"
							@input="markDirty"
						/>

						<el-button
							:icon="Delete"
							circle
							text
							type="danger"
							class="justify-self-end sm:justify-self-auto"
							title="Zutat entfernen"
							@click="removeIngredient(index)"
						/>
					</div>

					<el-button
						:icon="Plus"
						round
						class="w-full sm:w-auto"
						@click="addIngredient"
					>
						Zutat hinzufügen
					</el-button>
				</div>

				<el-divider />

				<h2 class="mb-3 font-display text-lg font-semibold text-ink">
					Zubereitung
				</h2>

				<div class="space-y-3">
					<div
						v-for="(step, index) in form.steps"
						:key="index"
						class="grid grid-cols-[auto_minmax(0,1fr)_auto] gap-2 rounded-xl border border-border bg-slate-50 p-3"
					>
						<div
							class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-primary-soft text-sm font-semibold text-primary-deep"
						>
							{{ index + 1 }}
						</div>

						<el-input
							v-model="step.instruction"
							type="textarea"
							:rows="2"
							placeholder="Was passiert in diesem Schritt?"
							@input="markDirty"
						/>

						<el-button
							:icon="Delete"
							circle
							text
							type="danger"
							class="!ml-0"
							title="Schritt entfernen"
							@click="removeStep(index)"
						/>
					</div>

					<el-button
						:icon="Plus"
						round
						class="w-full sm:w-auto"
						@click="addStep"
					>
						Schritt hinzufügen
					</el-button>
				</div>

				<div class="mt-6 flex justify-end gap-2">
					<el-button round @click="cancelForm">
						Abbrechen
					</el-button>

					<el-button
						type="primary"
						round
						:loading="saving"
						:disabled="saving"
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
