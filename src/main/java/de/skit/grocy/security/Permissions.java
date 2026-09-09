package de.skit.grocy.security;

import java.util.EnumSet;
import java.util.Set;

import de.skit.grocy.common.enums.Permission;
import de.skit.grocy.common.enums.Role;

/**
 * Zentrale Rechte für Haushaltsrollen.
 * Vorhandenes Modell: OWNER / EDITOR / VIEWER
 * (entspricht fachlich Eigentümer / Verwaltung der Inhalte / Mitglied mit Leserecht).
 */
public final class Permissions {

    private Permissions() {}

    public static Set<Permission> forRole(Role role) {
        return switch (role) {
            case OWNER -> EnumSet.allOf(Permission.class);
            case EDITOR -> EnumSet.of(
                    Permission.LIST_CREATE,
                    Permission.LIST_EDIT,
                    Permission.LIST_READ,
                    Permission.ITEM_TOGGLE,
                    Permission.ITEM_RENAME,
                    Permission.ITEM_DELETE,
                    Permission.RECIPE_READ,
                    Permission.RECIPE_EDIT,
                    Permission.ACTIVITY_READ);
            case VIEWER -> EnumSet.of(
                    Permission.LIST_READ,
                    Permission.RECIPE_READ,
                    Permission.ACTIVITY_READ);
        };
    }
}
