package de.skit.grocy.security.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(
        @NotBlank(message = "Token darf nicht leer sein")
        String token) {
}
