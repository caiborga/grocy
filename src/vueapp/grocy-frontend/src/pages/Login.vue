<template>
	<AuthLayout
		title="Einloggen"
		subtitle="Willkommen zurück bei Grocy!"
	>
		<el-form
			ref="formRef"
			:model="form"
			:rules="rules"
			label-position="top"
			size="large"
			@submit.prevent="submit"
		>
			<el-form-item label="E-Mail" prop="email">
				<el-input
					v-model="form.email"
					placeholder="du@example.com"
					autocomplete="email"
				/>
			</el-form-item>

			<el-form-item label="Passwort" prop="password">
				<el-input
					v-model="form.password"
					type="password"
					show-password
					autocomplete="current-password"
				/>
			</el-form-item>

			<div class="flex justify-end -mt-2 mb-4 text-sm">
				<router-link
					to="/forgot-password"
					class="font-semibold text-primary hover:underline"
				>
					Passwort vergessen?
				</router-link>
			</div>

			<el-button
				type="primary"
				class="w-full"
				round
				:loading="loading"
				:disabled="loading"
				native-type="submit"
				@click="submit"
			>
				Einloggen
			</el-button>

		</el-form>

		<div class="mt-6 text-center text-sm">
			<span class="text-muted">Noch keinen Account?</span>
			<router-link
				:to="{
					path: '/register',
					query: route.query.redirect
						? { redirect: route.query.redirect }
						: {}
				}"
				class="ml-1 font-semibold text-primary hover:underline"
			>
				Registrieren
			</router-link>
		</div>
	</AuthLayout>
</template>

<script setup>
import { ref } from "vue";
import { ElMessage } from "element-plus";
import { useRouter, useRoute } from "vue-router";
import { useUserStore } from "@/stores/userStore";
import AuthLayout from "@/components/AuthLayout.vue";

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const formRef = ref(null);
const loading = ref(false);

const form = ref({
	email: "",
	password: ""
});

const rules = {
	email: [
		{ required: true, message: "Bitte E-Mail eingeben", trigger: "blur" },
		{
			type: "email",
			message: "E-Mail-Adresse ist ungültig",
			trigger: "blur"
		}
	],
	password: [
		{ required: true, message: "Bitte Passwort eingeben", trigger: "blur" }
	]
};

function submit() {
	formRef.value?.validate(async (valid) => {
		if (!valid) return;

		loading.value = true;
		try {
			await userStore.login({
				email: form.value.email,
				password: form.value.password
			});

			ElMessage.success("Login erfolgreich");

			const redirect = route.query.redirect;
			if (typeof redirect === "string" && redirect.startsWith("/")) {
				router.push(redirect);
			} else {
				router.push("/lists/default");
			}
		} catch (e) {
			console.error("LOGIN ERROR", e);
			if (
				e?.response?.status === 403 &&
				e?.response?.data?.tokenType === "email_not_verified"
			) {
				ElMessage.error(
					"Bitte bestätige zuerst deine E-Mail-Adresse."
				);
			} else {
				ElMessage.error(
					"Login fehlgeschlagen. Prüfe E-Mail und Passwort."
				);
			}
		} finally {
			loading.value = false;
		}
	});
}
</script>
