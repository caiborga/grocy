import { computed } from "vue";
import { useUserStore } from "../stores/userStore";
import { type Permission, permissionsForRole } from "@/auth/perms";
import { Role } from "@/models/Role";

export function usePermissions() {
  const userStore = useUserStore();

  const role = computed<Role | null>(() => userStore.role ?? null);

  const perms = computed(() => permissionsForRole(role.value));

  const can = (p: Permission) => perms.value.has(p);

  const isOwner = computed(() => role.value === Role.OWNER);
  const canEdit = computed(() => role.value === Role.OWNER || role.value === Role.EDITOR);

  const userIsOwner = (r: Role | null | undefined) => r === Role.OWNER;

  return { role, perms, can, isOwner, canEdit, userIsOwner };
}
