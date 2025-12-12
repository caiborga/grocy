package de.skit.grocy.lists.dto;

import java.util.UUID;

public record ListResponse(
    UUID id,
    String title,
    boolean archived,
    Stats stats
) {}