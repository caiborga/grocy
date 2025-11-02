<script setup>
import { ref, onMounted } from "vue";

const message = ref("Loading...");

onMounted(async () => {
	const res = await fetch("/api/hello");
	message.value = await res.text();
});
</script>

<template>
	<h1>{{ message }}</h1>
	<p>
		Visit
		<a href="https://vuejs.org/" target="_blank" rel="noopener"
			>vuejs.org</a
		>
		to read the documentation
	</p>
</template>

<style scoped></style>
