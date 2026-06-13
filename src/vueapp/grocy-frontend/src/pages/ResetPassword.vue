<template>
    <div class="min-h-screen flex items-center justify-center bg-background-light dark:bg-background-dark px-4">
        <el-card class="w-full max-w-md" shadow="always">
            <div class="mb-6 text-center">
                <h1 class="text-2xl font-bold mb-1">Neues Passwort</h1>
                <p class="text-sm text-text-secondary-light dark:text-text-secondary-dark">
                    Wähle ein neues Passwort für deinen Account.
                </p>
            </div>

            <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large">
                <el-form-item label="Passwort" prop="password">
                    <el-input v-model="form.password" type="password" show-password autocomplete="new-password" />
                </el-form-item>

                <el-form-item label="Passwort wiederholen" prop="passwordRepeat">
                    <el-input v-model="form.passwordRepeat" type="password" show-password />
                </el-form-item>

                <el-button type="primary" class="w-full mt-2" :loading="loading" @click="submit">
                    Passwort speichern
                </el-button>
            </el-form>
        </el-card>
    </div>
</template>

<script setup>
import { ref } from "vue";
import { ElMessage } from "element-plus";
import { useRoute, useRouter } from "vue-router";
import { authService } from "@/services/authService";

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
        { required: true, message: "Passwort ist notwendig", trigger: "blur" },
        { min: 8, message: "Mindestens 8 Zeichen", trigger: "blur" }
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
