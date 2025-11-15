<template>
	<div
		class="relative flex h-auto min-h-screen w-full flex-col group/design-root overflow-x-hidden font-display bg-background-light dark:bg-background-dark"
	>
		<div class="layout-container flex h-full grow flex-col">
			<div
				class="px-4 sm:px-10 md:px-20 lg:px-40 flex flex-1 justify-center py-5"
			>
				<div
					class="layout-content-container flex flex-col w-full max-w-[960px] flex-1"
				>
					<!-- HEADER -->
					<header
						class="flex items-center justify-between whitespace-nowrap border-b border-border-light dark:border-border-dark px-4 sm:px-10 py-4 mb-8"
					>
						<div
							class="flex items-center gap-4 text-text-light dark:text-text-dark"
						>
							<div class="size-6 text-primary">
								<!-- logo -->
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
							<h2
								class="text-2xl font-bold leading-tight tracking-[-0.015em]"
							>
								grocy
							</h2>
						</div>
					</header>

					<!-- MAIN -->
					<main class="flex flex-col gap-8 px-4">
						<!-- Title -->
						<div class="flex flex-wrap justify-between gap-3">
							<div class="flex min-w-72 flex-col gap-2">
								<p
									class="text-4xl font-black leading-tight tracking-[-0.033em]"
								>
									My Shopping List
								</p>
								<p
									class="text-text-secondary-light dark:text-text-secondary-dark"
								>
									Add, check, and remove items below.
								</p>
							</div>
						</div>

						<!-- ADD ITEM BAR -->
						<div
							class="flex flex-col sm:flex-row items-end gap-3 w-full"
						>
							<label class="flex flex-col w-full flex-1">
								<p class="text-base font-medium pb-2">
									New Item
								</p>
								<div
									class="flex w-full items-stretch rounded-lg"
								>
									<input
										v-model="newItem"
										@keyup.enter="addItem"
										class="form-input flex w-full min-w-0 rounded-l-lg h-14 p-[15px] text-base border border-border-light dark:border-border-dark bg-background-light dark:bg-background-dark focus:ring-2 focus:ring-primary/50"
										placeholder="Add a new item..."
									/>
								</div>
							</label>

							<button
								@click="addItem"
								class="flex w-full sm:w-auto min-w-[84px] cursor-pointer items-center justify-center rounded-lg h-14 px-6 bg-primary text-white font-bold hover:bg-primary/90 transition-colors"
							>
								<span class="truncate">Add Item</span>
							</button>
						</div>

						<!-- ITEM LIST -->
						<div
							class="mt-4 flex flex-col gap-2 bg-white dark:bg-background-dark/50 rounded-xl border border-border-light dark:border-border-dark p-4 sm:p-6"
						>
							<!-- Item Row -->
							<div
								v-for="item in items"
								:key="item.id"
								class="group flex items-center justify-between gap-4 py-3 border-b border-border-light dark:border-border-dark last:border-b-0"
							>
								<!-- CHECK + LABEL/EDIT FIELD -->
								<label
									v-if="!item.editing"
									class="flex items-center gap-x-4 cursor-pointer"
								>
									<input
										type="checkbox"
										v-model="item.checked"
										class="h-5 w-5 rounded border-2 border-border-light dark:border-border-dark text-primary checked:bg-primary checked:border-primary focus:ring-0"
									/>
									<p
										class="text-base"
										:class="
											item.checked
												? 'text-text-secondary-light dark:text-text-secondary-dark line-through'
												: 'text-text-light dark:text-text-dark'
										"
									>
										{{ item.name }}
									</p>
								</label>

								<!-- Edit Mode -->
								<div
									v-else
									class="flex flex-1 items-center gap-x-4"
								>
									<input
										v-model="item.tempName"
										class="form-input -my-1 w-full rounded-md border-border-light dark:border-border-dark bg-background-light/50 dark:bg-background-dark text-text-light dark:text-text-dark focus:border-primary focus:ring-primary/50 px-2 py-1"
									/>
								</div>

								<!-- BUTTONS -->
								<div class="flex items-center gap-x-1">
									<!-- Edit button -->
									<button
										v-if="!item.editing"
										@click="startEdit(item)"
										class="opacity-0 group-hover:opacity-100 text-text-secondary-light dark:text-text-secondary-dark hover:text-primary hover:bg-primary/10 size-8 rounded-full flex items-center justify-center transition-all"
									>
										<span
											class="material-symbols-outlined text-xl"
											>edit</span
										>
									</button>

									<!-- Confirm edit -->
									<button
										v-if="item.editing"
										@click="saveEdit(item)"
										class="text-text-secondary-light dark:text-text-secondary-dark hover:text-primary hover:bg-primary/10 size-8 rounded-full flex items-center justify-center"
									>
										<span
											class="material-symbols-outlined text-xl"
											>done</span
										>
									</button>

									<!-- Delete -->
									<button
										@click="removeItem(item.id)"
										class="opacity-0 group-hover:opacity-100 text-text-secondary-light dark:text-text-secondary-dark hover:text-red-500 hover:bg-red-500/10 size-8 rounded-full flex items-center justify-center transition-all"
									>
										<span
											class="material-symbols-outlined text-xl"
											>delete</span
										>
									</button>
								</div>
							</div>
						</div>
					</main>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup>
import { ref } from "vue";

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
	if (!newItem.value.trim()) return;

	items.value.push({
		id: crypto.randomUUID(),
		name: newItem.value.trim(),
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
	if (item.tempName.trim()) item.name = item.tempName.trim();
	item.editing = false;
}
</script>

<style scoped>
:root {
	--checkbox-tick-svg: url("data:image/svg+xml,%3csvg viewBox=%270 0 16 16%27 fill=%27rgb(16,34,22)%27 xmlns=%27http://www.w3.org/2000/svg%27%3e%3cpath d=%27M12.207 4.793a1 1 0 010 1.414l-5 5a1 1 0 01-1.414 0l-2-2a1 1 0 011.414-1.414L6.5 9.086l4.293-4.293a1 1 0 011.414 0z%27/%3e%3c/svg%3e");
}
.group:hover .group-hover\:opacity-100 {
	opacity: 1;
}
</style>
