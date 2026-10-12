<template>
    <Teleport to="body">
        <aside ref="dock" class="presence-dock" aria-label="Auf dieser Liste anwesend">
            <div v-if="connected && users.length" class="presence-chips">
                <el-popover
                    v-for="user in users"
                    :key="user.id"
                    :visible="openedUser === user.id"
                    placement="top-end"
                    :width="260"
                >
                    <div data-presence-details @pointerenter="cancelHoverClose" @pointerleave="closeHover(openedUser)">
                        <p class="font-semibold text-ink break-words">{{ user.name }}</p>
                        <p class="mt-1 text-xs text-muted">Hat diese Liste geöffnet</p>
                        <dl class="mt-3 space-y-2 text-sm">
                            <div>
                                <dt class="text-xs text-muted">Rolle im Haushalt</dt>
                                <dd>{{ roleLabel(user.role) }}</dd>
                            </div>
                            <div v-if="user.joinedAt">
                                <dt class="text-xs text-muted">Im Haushalt seit</dt>
                                <dd>{{ formatDate(user.joinedAt) }}</dd>
                            </div>
                        </dl>
                    </div>
                    <template #reference>
                        <button
                            type="button"
                            class="presence-chip"
                            :aria-label="`Details zu ${user.name} anzeigen`"
                            :aria-expanded="openedUser === user.id"
                            @pointerenter="showOnHover($event, user.id)"
                            @pointerleave="closeHover(user.id)"
                            @focus="openedUser = user.id"
                            @blur="closeDetails"
                            @click="togglePinned(user.id)"
                        >
                            {{ initials(user.name) }}
                            <span class="presence-dot" aria-hidden="true" />
                        </button>
                    </template>
                </el-popover>
            </div>
            <span v-else class="presence-status" role="status">Live-Verbindung wird hergestellt…</span>
        </aside>
    </Teleport>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from "vue";

const props = defineProps({
    users: { type: Array, default: () => [] },
    connected: { type: Boolean, default: false }
});
const dock = ref(null);
const openedUser = ref(null);
let pinnedUser = null;
let hoverCloseTimer;

function cancelHoverClose() {
    clearTimeout(hoverCloseTimer);
}

function initials(name) {
    const parts = (name ?? "").trim().split(/\s+/).filter(Boolean);
    if (!parts.length) return "?";
    const first = Array.from(parts[0])[0];
    const last = parts.length > 1 ? Array.from(parts.at(-1))[0] : "";
    return (first + last).toLocaleUpperCase("de-DE");
}

function roleLabel(role) {
    return { OWNER: "Eigentümer", EDITOR: "Bearbeiter", VIEWER: "Leser" }[role] ?? "Mitglied";
}

function formatDate(value) {
    return new Intl.DateTimeFormat("de-DE", { dateStyle: "medium" }).format(new Date(value));
}

function showOnHover(event, id) {
    cancelHoverClose();
    // Touch browsers can synthesize hover. Only a tap should pin details on mobile.
    if (event.pointerType !== "touch" && !pinnedUser) openedUser.value = id;
}

function closeHover(id) {
    cancelHoverClose();
    hoverCloseTimer = setTimeout(() => {
        if (!pinnedUser && openedUser.value === id) openedUser.value = null;
    }, 150);
}

function togglePinned(id) {
    const wasPinned = pinnedUser === id;
    closeDetails();
    if (!wasPinned) {
        pinnedUser = id;
        openedUser.value = id;
    }
}

function closeDetails() {
    cancelHoverClose();
    pinnedUser = null;
    openedUser.value = null;
}

function closeOutside(event) {
    if (!dock.value?.contains(event.target) && !event.target.closest("[data-presence-details]")) closeDetails();
}

function closeOnEscape(event) {
    if (event.key === "Escape") closeDetails();
}

watch(() => [props.connected, props.users], () => {
    if (!props.connected || !props.users.some(user => user.id === openedUser.value)) closeDetails();
});

onMounted(() => {
    document.addEventListener("pointerdown", closeOutside);
    document.addEventListener("keydown", closeOnEscape);
});
onBeforeUnmount(() => {
    cancelHoverClose();
    document.removeEventListener("pointerdown", closeOutside);
    document.removeEventListener("keydown", closeOnEscape);
});
</script>

<style scoped>
.presence-dock {
    position: fixed;
    right: max(1rem, env(safe-area-inset-right));
    bottom: calc(7rem + env(safe-area-inset-bottom));
    z-index: 30;
    max-width: calc(100vw - 2rem);
    padding: 0.5rem;
    border: 1px solid rgba(15, 23, 42, 0.08);
    border-radius: 1.25rem;
    background: rgba(255, 255, 255, 0.96);
    box-shadow: 0 4px 18px rgba(15, 23, 42, 0.12);
}
.presence-chips {
    display: flex;
    gap: 0.5rem;
    overflow-x: auto;
    padding: 0.25rem;
}
.presence-chip {
    position: relative;
    flex-shrink: 0;
    width: 44px;
    height: 44px;
    border-radius: 50%;
    border: 1px solid #bfdbfe;
    background: #eff6ff;
    color: #1d4ed8;
    font-size: 0.875rem;
    font-weight: 700;
    cursor: pointer;
}
.presence-chip:hover,
.presence-chip[aria-expanded="true"] {
    background: #dbeafe;
}
.presence-chip:focus-visible {
    outline: 2px solid #2563eb;
    outline-offset: 2px;
}
.presence-dot {
    position: absolute;
    right: 1px;
    bottom: 1px;
    width: 10px;
    height: 10px;
    border-radius: 50%;
    background: #22c55e;
    border: 2px solid white;
}
.presence-status {
    display: block;
    padding: 0.5rem;
    font-size: 0.75rem;
    color: #64748b;
}
@media (min-width: 768px) {
    .presence-dock { bottom: 1.5rem; }
}
</style>
