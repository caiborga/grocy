<template>
    <div class="min-h-screen flex items-center justify-center bg-background-light dark:bg-background-dark px-4">
        <el-card class="w-full max-w-md" shadow="always">
            <!-- Header -->
            <div class="mb-6 text-center">
                <h1 class="text-2xl font-bold mb-1">Account erstellen</h1>
                <p class="text-sm text-text-secondary-light dark:text-text-secondary-dark"></p>
            </div>

            <!-- Register Form -->
            <el-form :model="form" :rules="rules" ref="formRef" label-position="top" size="large">
                <el-form-item label="Name" prop="name">
                    <el-input v-model="form.name" placeholder="Max" autocomplete="name" />
                </el-form-item>

                <el-form-item label="Email" prop="email">
                    <el-input v-model="form.email" placeholder="max@mueller.de" autocomplete="email" />
                </el-form-item>

                <el-form-item label="Passwort" prop="password">
                    <el-input v-model="form.password" type="password" show-password autocomplete="new-password" />
                </el-form-item>

                <el-form-item label="Passwort wiederholen" prop="passwordRepeat">
                    <el-input v-model="form.passwordRepeat" type="password" show-password />
                </el-form-item>

                <!-- Submit -->
                <el-button type="primary" class="w-full mt-2" :loading="loading" @click="submit">
                    Account erstellen
                </el-button>
            </el-form>

            <!-- Footer -->
            <div class="text-center mt-6 text-sm">
                <span class="text-text-secondary-light dark:text-text-secondary-dark">
                    Bereits regisitriert?
                </span>
                <router-link :to="{
                    path: '/login',
                    query: route.query.redirect
                        ? { redirect: route.query.redirect }
                        : {}
                }" class="text-primary font-medium ml-1 hover:underline">
                    Login
                </router-link>
            </div>
        </el-card>
    </div>
</template>

<script setup>
import { ref } from "vue";
import { ElMessage, ElLoading } from "element-plus";
import { useRouter, useRoute } from "vue-router";
import { useUserStore } from "@/stores/userStore";

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
    name: [{ required: true, message: "Name ist notwendig", trigger: "blur" }],
    email: [
        { required: true, message: "Email ist notwendig", trigger: "blur" },
        { type: "email", message: "Email Adresse ist ungültig", trigger: "blur" }
    ],
    password: [
        { required: true, message: "Passwort ist notwendig", trigger: "blur" },
        { min: 6, message: "Mindestens 6 Zeichen", trigger: "blur" }
    ],
    passwordRepeat: [
        { required: true, message: "Bitte Passwort wiederholen", trigger: "blur" },
        {
            validator: (_, value, callback) => {
                if (value !== form.value.password) callback(new Error("Passwörter stimmen nicht überein"));
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
        const loader = ElLoading.service({ lock: true });

        try {
            await userStore.registerAndLogin({
                displayName: form.value.name,
                email: form.value.email,
                password: form.value.password
            });

            ElMessage.success("Account erstellt");

            const redirect = route.query.redirect;
            if (typeof redirect === "string" && redirect.startsWith("/")) {
                router.push(redirect);
            } else {
                router.push("/lists/default");
            }
        } catch (e) {
            console.error(e);
            ElMessage.error("Registrierung fehlgeschlagen");
        } finally {
            loader.close();
            loading.value = false;
        }
    });
}
</script>
