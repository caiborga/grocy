package de.skit.grocy.households.member.dto;

import java.util.UUID;

import de.skit.grocy.common.enums.Role;

public record HouseholdMemberEdit(
        UUID actingUserId,
        Role role) {
}
