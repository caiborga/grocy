package de.skit.grocy.common;

public record ErrorResponse(
        String code,
        String message,
        Object details) {
}