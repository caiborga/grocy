<template>
	<AuthLayout
		title="E-Mail prüfen"
		subtitle="Wir haben dir einen Bestätigungslink gesendet."
	>
		<p class="mb-6 text-center text-sm leading-relaxed text-muted">
			Öffne den Link in deiner Mail, um deinen Account zu aktivieren.
			<span v-if="email" class="mt-2 block font-medium text-ink">
				{{ email }}
			</span>
		</p>

		<el-button
			type="primary"
			class="w-full"
			round
			:loading="loading"
			:disabled="loading"
			@click="resend"
		>
			E-Mail erneut senden
		</el-button>

		<div class="mt-6 text-center text-sm">
			<router-link
				to="/login"
				class="font-semibold text-primary hover:underline"
			>
				Zurück zum Login
			</router-link>
		</div>
	</AuthLayout>
</template>

<script setup>
import { computed, ref } from "vue";
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { authService } from "@/services/authService";
import AuthLayout from "@/components/AuthLayout.vue";

const route = useRoute();
const loading = ref(false);

const email = computed(() => {
	const value = route.query.email;
	return typeof value === "string" ? value : "";
});

async function resend() {
	if (!email.value) {
		ElMessage.error("Keine E-Mail-Adresse gefunden.");
		return;
	}

	loading.value = true;
	try {
		await authService.resendVerification(email.value);
		ElMessage.success(
			"Falls der Account existiert, wurde eine E-Mail versendet."
		);
	} catch (e) {
		console.error(e);
		ElMessage.error("E-Mail konnte nicht angefordert werden.");
	} finally {
		loading.value = false;
	}
}
</script>
