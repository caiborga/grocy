package de.skit.grocy.security.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "Email darf nicht leer sein")
    String email,

    @NotBlank(message = "Passwort darf nicht leer sein")
    String password
) {}
