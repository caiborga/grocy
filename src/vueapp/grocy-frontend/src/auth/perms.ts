import type { Role } from "@/models/Role";

export const PERM = {
	HOUSEHOLD_RENAME: "household:rename",
	HOUSEHOLD_DELETE: "household:delete",
	MEMBER_INVITE: "member:invite",
	MEMBER_ROLE_CHANGE: "member:role_change",
	MEMBER_DELETE: "member:delete",
	LIST_CREATE: "list:create",
	LIST_READ: "list:read",
	LIST_EDIT: "list:edit",
	ITEM_TOGGLE: "item:toggle",
	ITEM_RENAME: "item:rename",
	ITEM_DELETE: "item:delete"
} as const;

export type Permission = (typeof PERM)[keyof typeof PERM];

const ROLE_PERMISSIONS: Record<Role, Permission[]> = {
	OWNER: [
		PERM.HOUSEHOLD_RENAME,
		PERM.HOUSEHOLD_DELETE,
		PERM.MEMBER_INVITE,
		PERM.MEMBER_ROLE_CHANGE,
		PERM.MEMBER_DELETE,
		PERM.LIST_CREATE,
		PERM.LIST_EDIT,
		PERM.ITEM_TOGGLE,
		PERM.ITEM_RENAME,
		PERM.ITEM_DELETE,
		PERM.LIST_READ
	],
	EDITOR: [
		PERM.LIST_CREATE,
		PERM.LIST_EDIT,
		PERM.ITEM_TOGGLE,
		PERM.ITEM_RENAME,
		PERM.ITEM_DELETE,
		PERM.LIST_READ
	],
	VIEWER: [PERM.LIST_READ]
};

export function permissionsForRole(
	role: Role | null | undefined
): Set<Permission> {
	return new Set(role ? (ROLE_PERMISSIONS[role] ?? []) : []);
}
