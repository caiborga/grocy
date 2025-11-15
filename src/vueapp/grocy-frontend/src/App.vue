<script setup>
import { ref, onMounted } from "vue";
import ShoppingList from "./components/ShoppingList.vue";

const message = ref("Loading...");

onMounted(async () => {
	const res = await fetch("/api/hello");
	message.value = await res.text();
});
</script>

<template>
	<h1>{{ message }}</h1>
	<div class="bg-red-500 text-white p-4">Tailwind test</div>

	<ShoppingList />
	<p>
		Visit
		<a href="https://vuejs.org/" target="_blank" rel="noopener"
			>vuejs.org</a
		>
		to read the documentation
	</p>
</template>

<style scoped></style>
