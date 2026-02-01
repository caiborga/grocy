<template>
	<div
		class="min-h-screen flex justify-center bg-background-light dark:bg-background-dark py-10"
	>
		<div class="w-full max-w-3xl px-4">
			<el-card class="w-full">
				<!-- HEADER -->
				<header
					class="flex items-center justify-between pb-4 mb-4 border-b"
				>
					<div class="flex items-center gap-3">
						<div class="w-7 h-7 text-primary">
							<svg
								fill="none"
								viewBox="0 0 48 48"
								xmlns="http://www.w3.org/2000/svg"
							>
								<g clip-path="url(#clip0_6_330)">
									<path
										clip-rule="evenodd"
										fill="currentColor"
										fill-rule="evenodd"
										d="M24 0.757355L47.2426 24L24 47.2426L0.757355 24L24 0.757355ZM21 35.7574V12.2426L9.24264 24L21 35.7574Z"
									/>
								</g>
								<defs>
									<clipPath id="clip0_6_330">
										<rect
											fill="white"
											width="48"
											height="48"
										/>
									</clipPath>
								</defs>
							</svg>
						</div>
						<h2 class="text-2xl font-bold tracking-tight">grocy</h2>
					</div>
				</header>

				<!-- MAIN -->
				<main class="flex flex-col gap-6">
					<!-- Title -->
					<section>
						<h1 class="text-3xl font-extrabold mb-1">
							My Shopping List
						</h1>
						<p
							class="text-text-secondary-light dark:text-text-secondary-dark"
						>
							Add, check, and remove items below.
						</p>
					</section>

					<!-- ADD ITEM BAR -->
					<section class="space-y-2">
						<p class="text-base font-medium">New Item</p>
						<div class="flex gap-3 w-full">
							<el-input
								v-model="newItem"
								placeholder="Add a new item..."
								clearable
								@keyup.enter="addItem"
							/>
							<el-button
								type="primary"
								size="large"
								circle
								@click="addItem"
							>
								<el-icon><Plus /></el-icon>
							</el-button>
						</div>
					</section>

					<!-- ITEM LIST -->
					<section>
						<el-empty
							v-if="items.length === 0"
							description="No items yet. Add your first one!"
						/>
						<div
							v-else
							class="divide-y border rounded-lg overflow-hidden bg-white/70 dark:bg-background-dark/60"
						>
							<div
								v-for="item in items"
								:key="item.id"
								class="flex items-center justify-between gap-4 px-4 py-3"
							>
								<!-- LEFT: Checkbox + Text / Edit -->
								<div class="flex items-center gap-3 flex-1">
									<el-checkbox v-model="item.checked" />

									<!-- Anzeige-Modus -->
									<span
										v-if="!item.editing"
										class="text-base"
										:class="
											item.checked
												? 'line-through text-text-secondary-light dark:text-text-secondary-dark'
												: 'text-text-light dark:text-text-dark'
										"
									>
										{{ item.name }}
									</span>

									<!-- Edit-Modus -->
									<div v-else class="flex-1 max-w-md">
										<el-input
											v-model="item.tempName"
											size="small"
											@keyup.enter="saveEdit(item)"
										/>
									</div>
								</div>

								<!-- BUTTONS -->
								<div class="flex items-center gap-1">
									<!-- EDIT -->
									<el-button
										v-if="!item.editing"
										size="small"
										@click="startEdit(item)"
										circle
									>
										<el-icon><Edit /></el-icon>
									</el-button>
									<!-- SAVE -->
									<el-button
										v-else
										type="success"
										size="small"
										@click="saveEdit(item)"
										circle
									>
										<el-icon><Check /></el-icon>
									</el-button>

									<!-- DELETE -->
									<el-button
										type="danger"
										size="small"
										@click="removeItem(item.id)"
										circle
									>
										<el-icon><Delete /></el-icon>
									</el-button>
								</div>
							</div>
						</div>
					</section>
				</main>
			</el-card>
		</div>
	</div>
</template>

<script setup>
import { ref } from "vue";
import { Edit, Delete, Check, Plus } from "@element-plus/icons-vue";

const newItem = ref("");

const items = ref([
	{
		id: crypto.randomUUID(),
		name: "Milk",
		checked: true,
		editing: false,
		tempName: ""
	},
	{
		id: crypto.randomUUID(),
		name: "Bread",
		checked: false,
		editing: false,
		tempName: ""
	},
	{
		id: crypto.randomUUID(),
		name: "Eggs",
		checked: false,
		editing: false,
		tempName: ""
	}
]);

function addItem() {
	const name = newItem.value.trim();
	if (!name) return;

	items.value.push({
		id: crypto.randomUUID(),
		name,
		checked: false,
		editing: false,
		tempName: ""
	});

	newItem.value = "";
}

function removeItem(id) {
	items.value = items.value.filter((i) => i.id !== id);
}

function startEdit(item) {
	item.tempName = item.name;
	item.editing = true;
}

function saveEdit(item) {
	const name = (item.tempName || "").trim();
	if (name) {
		item.name = name;
	}
	item.editing = false;
}
</script>
