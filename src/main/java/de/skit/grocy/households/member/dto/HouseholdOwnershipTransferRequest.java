package de.skit.grocy.households.member.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record HouseholdOwnershipTransferRequest(
        @NotNull UUID userId) {
}
