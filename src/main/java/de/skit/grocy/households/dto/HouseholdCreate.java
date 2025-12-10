package de.skit.grocy.households.dto;

import java.util.UUID;

public record HouseholdCreate(
    String name,
    UUID createdBy
) {}
