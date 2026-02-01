package de.skit.grocy.households.invites.dto;

import java.util.UUID;

public class HouseholdInviteAcceptResponse {

    private UUID householdId;
    private boolean joined;

    public HouseholdInviteAcceptResponse(UUID householdId, boolean joined) {
        this.householdId = householdId;
        this.joined = joined;
    }

    public UUID getHouseholdId() {
        return householdId;
    }

    public boolean isJoined() {
        return joined;
    }
}
