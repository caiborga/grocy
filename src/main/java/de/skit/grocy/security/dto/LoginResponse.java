package de.skit.grocy.security.dto;

import de.skit.grocy.user.dto.UserResponse;

public record LoginResponse(
        String accessToken,
        String tokenType,
        UserResponse user
    ) {}
