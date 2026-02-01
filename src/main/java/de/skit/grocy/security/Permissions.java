package de.skit.grocy.security;

import java.util.EnumSet;
import java.util.Set;

import de.skit.grocy.common.enums.Permission;
import de.skit.grocy.common.enums.Role;

public final class Permissions {

    private Permissions() {}

    public static Set<Permission> forRole(Role role) {
        return switch (role) {
            case OWNER -> EnumSet.allOf(Permission.class);
            case EDITOR -> EnumSet.of(
                Permission.HOUSEHOLD_RENAME,
                Permission.LIST_EDIT,
                Permission.ITEM_TOGGLE,
                Permission.ITEM_RENAME,
                Permission.ITEM_DELETE,
                Permission.LIST_READ
            );
            case VIEWER -> EnumSet.of(
                Permission.LIST_READ
            );
        };
    }
}
