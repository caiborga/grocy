<template>
    <div class="min-h-screen flex items-center justify-center bg-background-light dark:bg-background-dark px-4">
        <el-card class="w-full max-w-md" shadow="always">
            <div class="mb-6 text-center">
                <h1 class="text-2xl font-bold mb-1">Passwort zurücksetzen</h1>
                <p class="text-sm text-text-secondary-light dark:text-text-secondary-dark">
                    Wir senden dir einen Link zum Zurücksetzen.
                </p>
            </div>

            <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large">
                <el-form-item label="Email" prop="email">
                    <el-input v-model="form.email" placeholder="you@example.com" autocomplete="email" />
                </el-form-item>

                <el-button type="primary" class="w-full mt-2" :loading="loading" @click="submit">
                    Link senden
                </el-button>
            </el-form>

            <div class="text-center mt-6 text-sm">
                <router-link to="/login" class="text-primary font-medium hover:underline">
                    Zurück zum Login
                </router-link>
            </div>
        </el-card>
    </div>
</template>

<script setup>
import { ref } from "vue";
import { ElMessage } from "element-plus";
import { authService } from "@/services/authService";

const formRef = ref(null);
const loading = ref(false);

const form = ref({
    email: ""
});

const rules = {
    email: [
        { required: true, message: "Pflichtfeld", trigger: "blur" },
        { type: "email", message: "Email muss gültig sein", trigger: "blur" }
    ]
};

function submit() {
    formRef.value?.validate(async (valid) => {
        if (!valid) return;

        loading.value = true;
        try {
            await authService.forgotPassword(form.value.email);
            ElMessage.success("Falls der Account existiert, wurde eine E-Mail versendet.");
        } catch (e) {
            console.error(e);
            ElMessage.error("Anfrage fehlgeschlagen");
        } finally {
            loading.value = false;
        }
    });
}
</script>
