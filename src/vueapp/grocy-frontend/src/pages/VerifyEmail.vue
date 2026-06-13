<template>
    <div class="min-h-screen flex items-center justify-center bg-background-light dark:bg-background-dark px-4">
        <el-card class="w-full max-w-md" shadow="always">
            <div class="mb-6 text-center">
                <h1 class="text-2xl font-bold mb-1">E-Mail bestätigen</h1>
                <p class="text-sm text-text-secondary-light dark:text-text-secondary-dark">
                    {{ message }}
                </p>
            </div>

            <el-button type="primary" class="w-full" @click="router.push('/login')">
                Zum Login
            </el-button>
        </el-card>
    </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { authService } from "@/services/authService";

const route = useRoute();
const router = useRouter();
const message = ref("Wir prüfen deinen Bestätigungslink.");

onMounted(async () => {
    const token = route.query.token;

    if (typeof token !== "string" || !token) {
        message.value = "Der Bestätigungslink ist ungültig.";
        return;
    }

    try {
        await authService.verifyEmail(token);
        message.value = "Deine E-Mail wurde bestätigt. Du kannst dich jetzt einloggen.";
    } catch (e) {
        console.error(e);
        message.value = "Der Bestätigungslink ist ungültig oder abgelaufen.";
    }
});
</script>
