<template>
    <div class="min-h-screen flex items-center justify-center bg-background-light dark:bg-background-dark px-4">
        <el-card class="w-full max-w-md" shadow="always">
            <!-- Header -->
            <div class="mb-6 text-center">
                <h1 class="text-2xl font-bold mb-1">404</h1>
                <p class="text-sm text-text-secondary-light dark:text-text-secondary-dark">
                    Seite nicht gefunden
                </p>
            </div>

            <!-- Body -->
            <div class="text-center space-y-3">
                <p class="text-text-secondary-light dark:text-text-secondary-dark">
                    Die Seite
                    <span class="font-medium text-text-primary-light dark:text-text-primary-dark break-all">
                        {{ route.fullPath }}
                    </span>
                    existiert nicht.
                </p>

                <p class="text-sm text-text-secondary-light dark:text-text-secondary-dark">
                    Prüfe die URL oder geh zurück zur Startseite.
                </p>
            </div>

            <!-- Actions -->
            <div class="mt-6">
                <el-button type="primary" class="w-full" size="large" @click="goBack">
                    Startseite
                </el-button>
            </div>
        </el-card>
    </div>
</template>

<script setup>
import { useRouter, useRoute } from "vue-router";

const router = useRouter();
const route = useRoute();

const goBack = () => {
    router.push("/");
};

</script>