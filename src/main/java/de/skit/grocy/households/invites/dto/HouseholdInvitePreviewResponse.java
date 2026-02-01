package de.skit.grocy.households.invites.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import de.skit.grocy.common.enums.Role;
import de.skit.grocy.households.invites.enums.InviteStatus;

public class HouseholdInvitePreviewResponse {

    private UUID householdId;
    private String householdName;
    private Role role;
    private OffsetDateTime expiresAt;
    private InviteStatus status;
    private int remainingUses;

    public UUID getHouseholdId() {
        return householdId;
    }

    public void setHouseholdId(UUID householdId) {
        this.householdId = householdId;
    }

    public String getHouseholdName() {
        return householdName;
    }

    public void setHouseholdName(String householdName) {
        this.householdName = householdName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public InviteStatus getStatus() {
        return status;
    }

    public void setStatus(InviteStatus status) {
        this.status = status;
    }

    public int getRemainingUses() {
        return remainingUses;
    }

    public void setRemainingUses(int remainingUses) {
        this.remainingUses = remainingUses;
    }
}
