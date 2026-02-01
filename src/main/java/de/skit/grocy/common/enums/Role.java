package de.skit.grocy.common.enums;

public enum Role {

    OWNER,
    EDITOR,
    VIEWER;

    /**
     * Darf Mitglieder einladen?
     */
    public boolean canInvite() {
        return this == OWNER;
    }

    /**
     * Darf Haushalt administrieren?
     */
    public boolean isAdmin() {
        return this == EDITOR;
    }
}
