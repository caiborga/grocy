<template>
    <div class="min-h-screen bg-gray-50">
        <!-- Topbar (Desktop + optional Mobile) -->
        <header v-if="showNav" class="sticky top-0 z-40 bg-white/90 backdrop-blur border-b border-gray-100">
            <div class="max-w-5xl mx-auto px-4 py-3 flex items-center justify-between">
                <div class="flex flex-wrap items-center gap-x-2 gap-y-1 text-sm">
                    <img class="h-6 w-46 object-cover" src="/public/grocy.png" />

                    <span class="text-gray-400 select-none">/</span>

                    <span class="font-semibold text-sky-600 truncate max-w-[200px]" :title="activeHousehold?.name">
                        {{ activeHousehold?.name ?? "Kein Haushalt gewählt" }}
                    </span>

                    <!-- Rollen-Badge -->
                    <span v-if="role === Role.OWNER"
                        class="inline-flex items-center gap-1 rounded-md bg-amber-100 px-2 py-0.5 text-amber-700">
                        <el-icon>
                            <Key />
                        </el-icon>
                        <span class="hidden sm:inline">Besitzer</span>
                    </span>

                    <span v-else-if="role === Role.EDITOR"
                        class="inline-flex items-center gap-1 rounded-md bg-sky-100 px-2 py-0.5 text-sky-700">
                        <el-icon>
                            <Edit />
                        </el-icon>
                        <span class="hidden sm:inline">Bearbeiter</span>
                    </span>

                    <span v-else-if="role === Role.VIEWER"
                        class="inline-flex items-center gap-1 rounded-md bg-gray-100 px-2 py-0.5 text-gray-600">
                        <el-icon>
                            <View />
                        </el-icon>
                        <span class="hidden sm:inline">Betrachter</span>
                    </span>

                    <!-- 🔽 User-Info: immer neue Zeile -->
                    <span class="w-full font-medium text-gray-700 truncate">
                        {{ me?.name ?? "" }}
                        <span class="text-gray-400">
                            ({{ me?.email ?? "" }})
                        </span>
                    </span>
                </div>



                <!-- Desktop Nav -->
                <nav class="hidden md:flex items-center gap-2">
                    <RouterLink v-for="item in navItems" :key="item.to" :to="item.to"
                        class="px-3 py-2 rounded-xl text-sm text-gray-600 hover:bg-gray-100" :class="isActive(item.to) ? 'bg-gray-100 text-gray-900' : ''
                            ">
                        {{ item.label }}
                    </RouterLink>

                    <button class="ml-2 px-3 py-2 rounded-xl text-sm text-gray-600 hover:bg-gray-100" @click="logout">
                        Logout
                    </button>
                </nav>
            </div>
        </header>

        <!-- Content -->
        <main :class="showNav ? 'pb-16 md:pb-0' : ''">
            <RouterView />
        </main>

        <!-- Bottom Nav (Mobile) -->
        <nav v-if="showNav" class="md:hidden fixed bottom-0 left-0 right-0 z-50 bg-white border-t border-gray-200"
            style="padding-bottom: env(safe-area-inset-bottom)">
            <div class="max-w-xl mx-auto px-2 py-2 grid grid-cols-3 gap-2">
                <RouterLink v-for="item in navItemsMobile" :key="item.to" :to="item.to"
                    class="flex flex-col items-center justify-center py-2 rounded-xl text-xs text-gray-500" :class="isActive(item.to)
                        ? 'bg-gray-100 text-gray-900'
                        : 'hover:bg-gray-50'
                        ">
                    <el-icon :size="24">
                        <component :is="item.icon" />
                    </el-icon>
                </RouterLink>
                <button class="ml-2 px-3 py-2 rounded-xl text-sm text-gray-600 hover:bg-gray-100" @click="logout">
                    <el-icon :size="24">
                        <SwitchButton />
                    </el-icon>
                </button>
            </div>
        </nav>
    </div>
</template>

<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { storeToRefs } from "pinia";
import { ElMessage } from "element-plus";

import { useHouseholdStore } from "@/stores/householdStore";
import { useUserStore } from "@/stores/userStore";
import { ACCESS_TOKEN_KEY } from "@/constants/auth";
import { Role } from "@/models/Role";

const route = useRoute();
const router = useRouter();

const userStore = useUserStore();
const householdStore = useHouseholdStore();

const { me, role, activeHouseholdId } = storeToRefs(userStore);
const { activeHousehold } = storeToRefs(householdStore);

const showNav = computed(() => !!route.meta.requiresAuth);

const navItems = [
    { label: "Liste", to: "/lists" },
    { label: "Haushalte", to: "/households" }
];

const navItemsMobile = [
    { label: "Liste", to: "/lists", icon: "List" },
    { label: "Haushalte", to: "/households", icon: "House" }
];

function isActive(to) {
    return route.path === to || route.path.startsWith(to + "/");
}

async function logout() {
    try {
        localStorage.removeItem(ACCESS_TOKEN_KEY);
    } finally {
        router.push("/login");
        ElMessage.success("Bis bald!");
    }
}
</script>
