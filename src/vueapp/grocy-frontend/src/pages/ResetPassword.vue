<template>
	<AuthLayout
		title="Neues Passwort"
		subtitle="Wähle ein neues Passwort für deinen Account."
	>
		<el-form
			ref="formRef"
			:model="form"
			:rules="rules"
			label-position="top"
			size="large"
		>
			<el-form-item label="Passwort" prop="password">
				<el-input
					v-model="form.password"
					type="password"
					show-password
					autocomplete="new-password"
				/>
			</el-form-item>

			<el-form-item label="Passwort wiederholen" prop="passwordRepeat">
				<el-input
					v-model="form.passwordRepeat"
					type="password"
					show-password
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
				Passwort speichern
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
import { useRoute, useRouter } from "vue-router";
import { authService } from "@/services/authService";
import AuthLayout from "@/components/AuthLayout.vue";

const route = useRoute();
const router = useRouter();
const formRef = ref(null);
const loading = ref(false);

const form = ref({
	password: "",
	passwordRepeat: ""
});

const rules = {
	password: [
		{
			required: true,
			message: "Bitte Passwort eingeben",
			trigger: "blur"
		},
		{ min: 8, message: "Mindestens 8 Zeichen", trigger: "blur" }
	],
	passwordRepeat: [
		{
			required: true,
			message: "Bitte Passwort wiederholen",
			trigger: "blur"
		},
		{
			validator: (_, value, callback) => {
				if (value !== form.value.password)
					callback(new Error("Passwörter stimmen nicht überein"));
				else callback();
			},
			trigger: "blur"
		}
	]
};

function submit() {
	formRef.value?.validate(async (valid) => {
		if (!valid) return;

		const token = route.query.token;
		if (typeof token !== "string" || !token) {
			ElMessage.error("Reset-Link ist ungültig.");
			return;
		}

		loading.value = true;
		try {
			await authService.resetPassword(token, form.value.password);
			ElMessage.success("Passwort wurde geändert.");
			router.push("/login");
		} catch (e) {
			console.error(e);
			ElMessage.error("Reset-Link ist ungültig oder abgelaufen.");
		} finally {
			loading.value = false;
		}
	});
}
</script>
