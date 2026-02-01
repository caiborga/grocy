package de.skit.grocy.households.invites.dto;

import de.skit.grocy.common.enums.Role;
import jakarta.validation.constraints.NotNull;

public class HouseholdInviteCreateRequest {

    @NotNull
    private Role role;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}