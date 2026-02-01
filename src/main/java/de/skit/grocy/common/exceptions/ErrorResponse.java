package de.skit.grocy.common.exceptions;

public record ErrorResponse(
        String code,
        String message,
        Object details) {
}