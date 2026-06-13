package de.skit.grocy.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserCreate(
    @NotBlank(message = "Name darf nicht leer sein")
    String displayName,

    @Email(message = "Email muss gültig sein")
    @NotBlank(message = "Email darf nicht leer sein")
    String email,

    @Size(min = 8, message = "Passwort muss mindestens 8 Zeichen lang sein")
    @NotBlank(message = "Passwort darf nicht leer sein")
    String password) {}
