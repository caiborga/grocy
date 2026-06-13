<template>
    <div class="min-h-screen flex items-center justify-center bg-background-light dark:bg-background-dark px-4">
        <el-card class="w-full max-w-md" shadow="always">
            <div class="mb-6 text-center">
                <h1 class="text-2xl font-bold mb-1">E-Mail prüfen</h1>
                <p class="text-sm text-text-secondary-light dark:text-text-secondary-dark">
                    Wir haben dir einen Bestätigungslink gesendet.
                </p>
            </div>

            <p class="text-sm text-center mb-6">
                Öffne den Link in deiner Mail, um deinen Account zu aktivieren.
            </p>

            <el-button type="primary" class="w-full" :loading="loading" @click="resend">
                E-Mail erneut senden
            </el-button>

            <div class="text-center mt-6 text-sm">
                <router-link to="/login" class="text-primary font-medium hover:underline">
                    Zurück zum Login
                </router-link>
            </div>
        </el-card>
    </div>
</template>

<script setup>
import { computed, ref } from "vue";
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { authService } from "@/services/authService";

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
        ElMessage.success("Falls der Account existiert, wurde eine E-Mail versendet.");
    } catch (e) {
        console.error(e);
        ElMessage.error("E-Mail konnte nicht angefordert werden.");
    } finally {
        loading.value = false;
    }
}
</script>
