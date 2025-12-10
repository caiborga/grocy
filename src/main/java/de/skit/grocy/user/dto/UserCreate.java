package de.skit.grocy.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserCreate(
    @NotBlank(message = "Name darf nicht leer sein")
    String displayName,
    @NotBlank(message = "Email darf nicht leer sein")
    String email,
    @NotBlank(message = "Passwort darf nicht leer sein")
    String password) {}
