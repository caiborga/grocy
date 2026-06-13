package de.skit.grocy.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "Token darf nicht leer sein")
        String token,

        @Size(min = 8, message = "Passwort muss mindestens 8 Zeichen lang sein")
        @NotBlank(message = "Passwort darf nicht leer sein")
        String password) {
}
