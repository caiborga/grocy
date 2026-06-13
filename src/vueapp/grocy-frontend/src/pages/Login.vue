<template>
    <div class="min-h-screen flex items-center justify-center bg-background-light dark:bg-background-dark px-4">
        <el-card class="w-full max-w-md" shadow="always">
            <!-- Header -->
            <div class="mb-6 text-center">
                <h1 class="text-2xl font-bold mb-1">Login</h1>
                <p class="text-sm text-text-secondary-light dark:text-text-secondary-dark">
                    Willkommen zurück bei Grocy!
                </p>
            </div>

            <!-- Login Form -->
            <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large" @submit.prevent="submit">
                <el-form-item label="Email" prop="email">
                    <el-input v-model="form.email" placeholder="you@example.com" autocomplete="email" />
                </el-form-item>

                <el-form-item label="Passwort" prop="password">
                    <el-input v-model="form.password" type="password" show-password autocomplete="current-password" />
                </el-form-item>

                <div class="flex justify-end -mt-2 mb-4 text-sm">
                    <router-link to="/forgot-password" class="text-primary font-medium hover:underline">
                        Passwort vergessen?
                    </router-link>
                </div>

                <!-- Submit -->
                <el-button type="primary" class="w-full mt-2" :loading="loading" @click="submit" native-type="submit">
                    Login
                </el-button>
            </el-form>

            <!-- Footer -->
            <div class="text-center mt-6 text-sm">
                <span class="text-text-secondary-light dark:text-text-secondary-dark">
                    Noch keinen Account?
                </span>
                <router-link :to="{
                    path: '/register',
                    query: route.query.redirect
                        ? { redirect: route.query.redirect }
                        : {}
                }" class="text-primary font-medium ml-1 hover:underline">
                    Registrieren
                </router-link>
            </div>
        </el-card>
    </div>
</template>

<script setup>
import { ref } from "vue";
import { ElMessage } from "element-plus";
import { useRouter, useRoute } from "vue-router";
import { useUserStore } from "@/stores/userStore";

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
        { required: true, message: "Pflichtfeld", trigger: "blur" },
        { type: "email", message: "Invalid email address", trigger: "blur" }
    ],
    password: [{ required: true, message: "Pflichtfeld", trigger: "blur" }]
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
            console.error("status", e?.response?.status);
            console.error("data", e?.response?.data);
            if (e?.response?.status === 403 && e?.response?.data?.tokenType === "email_not_verified") {
                ElMessage.error("Bitte bestätige zuerst deine E-Mail-Adresse.");
            } else {
                ElMessage.error("Login fehlgeschlagen");
            }
        } finally {
            loading.value = false;
        }
    });
}
</script>
