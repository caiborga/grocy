<template>
	<AuthLayout
		title="Passwort zurücksetzen"
		subtitle="Wir senden dir einen Link zum Zurücksetzen."
	>
		<el-form
			ref="formRef"
			:model="form"
			:rules="rules"
			label-position="top"
			size="large"
		>
			<el-form-item label="E-Mail" prop="email">
				<el-input
					v-model="form.email"
					placeholder="du@example.com"
					autocomplete="email"
				/>
			</el-form-item>

			<el-button
				type="primary"
				class="w-full"
				round
				:loading="loading"
				:disabled="loading"
				@click="submit"
			>
				Link senden
			</el-button>
		</el-form>

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
import { ref } from "vue";
import { ElMessage } from "element-plus";
import { authService } from "@/services/authService";
import AuthLayout from "@/components/AuthLayout.vue";

const formRef = ref(null);
const loading = ref(false);

const form = ref({
	email: ""
});

const rules = {
	email: [
		{ required: true, message: "Bitte E-Mail eingeben", trigger: "blur" },
		{
			type: "email",
			message: "E-Mail-Adresse ist ungültig",
			trigger: "blur"
		}
	]
};

function submit() {
	formRef.value?.validate(async (valid) => {
		if (!valid) return;

		loading.value = true;
		try {
			await authService.forgotPassword(form.value.email);
			ElMessage.success(
				"Falls der Account existiert, wurde eine E-Mail versendet."
			);
		} catch (e) {
			console.error(e);
			ElMessage.error("Anfrage fehlgeschlagen. Bitte später erneut versuchen.");
		} finally {
			loading.value = false;
		}
	});
}
</script>
