package de.skit.grocy.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @Email(message = "Email muss gültig sein")
        @NotBlank(message = "Email darf nicht leer sein")
        String email) {
}
