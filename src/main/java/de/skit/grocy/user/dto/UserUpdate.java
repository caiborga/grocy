package de.skit.grocy.user.dto;

public record UserUpdate(
        String displayName,
        String email,
        String password
) {}
