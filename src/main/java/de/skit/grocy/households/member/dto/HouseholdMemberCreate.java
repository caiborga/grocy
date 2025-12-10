package de.skit.grocy.households.member.dto;

import java.util.UUID;

public record HouseholdMemberCreate(UUID userId, String role) {}
