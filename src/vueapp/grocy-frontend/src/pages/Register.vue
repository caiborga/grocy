<template>
	<AuthLayout
		title="Account erstellen"
		subtitle="In wenigen Schritten startklar für deinen Haushalt."
	>
		<el-form
			:model="form"
			:rules="rules"
			ref="formRef"
			label-position="top"
			size="large"
		>
			<el-form-item label="Name" prop="name">
				<el-input
					v-model="form.name"
					placeholder="Max"
					autocomplete="name"
				/>
			</el-form-item>

			<el-form-item label="E-Mail" prop="email">
				<el-input
					v-model="form.email"
					placeholder="max@mueller.de"
					autocomplete="email"
				/>
			</el-form-item>

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
				Account erstellen
			</el-button>
		</el-form>

		<div class="mt-6 text-center text-sm">
			<span class="text-muted">Bereits registriert?</span>
			<router-link
				:to="{
					path: '/login',
					query: route.query.redirect
						? { redirect: route.query.redirect }
						: {}
				}"
				class="ml-1 font-semibold text-primary hover:underline"
			>
				Einloggen
			</router-link>
		</div>
	</AuthLayout>
</template>

<script setup>
import { ref } from "vue";
import { ElMessage, ElLoading } from "element-plus";
import { useRouter, useRoute } from "vue-router";
import { useUserStore } from "@/stores/userStore";
import AuthLayout from "@/components/AuthLayout.vue";

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const formRef = ref(null);
const loading = ref(false);

const form = ref({
	name: "",
	email: "",
	password: "",
	passwordRepeat: ""
});

const rules = {
	name: [
		{ required: true, message: "Bitte Namen eingeben", trigger: "blur" }
	],
	email: [
		{ required: true, message: "Bitte E-Mail eingeben", trigger: "blur" },
		{
			type: "email",
			message: "E-Mail-Adresse ist ungültig",
			trigger: "blur"
		}
	],
	password: [
		{
			required: true,
			message: "Bitte Passwort eingeben",
			trigger: "blur"
		},
		{
			min: 8,
			message: "Passwort muss mindestens 8 Zeichen lang sein",
			trigger: "blur"
		}
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

		loading.value = true;
		const loader = ElLoading.service({ lock: true, text: "Account wird erstellt…" });

		try {
			await userStore.register({
				displayName: form.value.name,
				email: form.value.email,
				password: form.value.password
			});

			ElMessage.success("Account erstellt");
			router.push({
				path: "/check-email",
				query: { email: form.value.email }
			});
		} catch (e) {
			console.error(e);
			ElMessage.error(
				"Registrierung fehlgeschlagen. Bitte später erneut versuchen."
			);
		} finally {
			loader.close();
			loading.value = false;
		}
	});
}
</script>
