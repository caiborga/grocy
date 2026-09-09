package de.skit.grocy.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @Email(message = "Email muss gültig sein")
    @NotBlank(message = "Email darf nicht leer sein")
    String email,

    @NotBlank(message = "Passwort darf nicht leer sein")
    @Size(max = 72, message = "Passwort ist zu lang")
    String password
) {}
