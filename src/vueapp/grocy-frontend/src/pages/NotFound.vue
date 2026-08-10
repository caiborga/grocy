<template>
	<AuthLayout
		title="Seite nicht gefunden"
		subtitle="Die gewünschte Seite existiert nicht oder wurde verschoben."
	>
		<p class="mb-6 break-all text-center text-sm text-muted">
			Pfad:
			<span class="font-medium text-ink">{{ route.fullPath }}</span>
		</p>

		<el-button
			type="primary"
			class="w-full"
			round
			size="large"
			@click="goHome"
		>
			Zur Startseite
		</el-button>
	</AuthLayout>
</template>

<script setup>
import { useRouter, useRoute } from "vue-router";
import AuthLayout from "@/components/AuthLayout.vue";

const router = useRouter();
const route = useRoute();

const goHome = () => {
	router.push("/");
};
</script>
