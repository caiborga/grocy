package de.skit.grocy.households.member.dto;

import java.util.UUID;

import de.skit.grocy.common.enums.Role;

public record HouseholdMemberCreate(
        UUID userId,
        Role role) {
}
