<template>
	<AuthLayout title="E-Mail bestätigen" :subtitle="message">
		<el-button
			type="primary"
			class="w-full"
			round
			@click="router.push('/login')"
		>
			Zum Login
		</el-button>
	</AuthLayout>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { authService } from "@/services/authService";
import AuthLayout from "@/components/AuthLayout.vue";

const route = useRoute();
const router = useRouter();
const message = ref("Wir prüfen deinen Bestätigungslink…");

onMounted(async () => {
	const token = route.query.token;

	if (typeof token !== "string" || !token) {
		message.value = "Der Bestätigungslink ist ungültig.";
		return;
	}

	try {
		await authService.verifyEmail(token);
		message.value =
			"Deine E-Mail wurde bestätigt. Du kannst dich jetzt einloggen.";
	} catch (e) {
		console.error(e);
		message.value = "Der Bestätigungslink ist ungültig oder abgelaufen.";
	}
});
</script>
