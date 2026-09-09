package de.skit.grocy.common.enums;

public enum Role {

    OWNER,
    EDITOR,
    VIEWER;

    public boolean canInvite() {
        return this == OWNER;
    }

    public boolean isOwner() {
        return this == OWNER;
    }
}
