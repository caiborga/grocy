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

	<ShoppingList />
</template>

<style scoped></style>
