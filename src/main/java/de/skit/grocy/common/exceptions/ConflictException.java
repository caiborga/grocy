package de.skit.grocy.common.exceptions;

public class ConflictException extends RuntimeException {

    private final String code;
    private final Object details;

    public ConflictException(String code, String message, Object details) {
        super(message);
        this.code = code;
        this.details = details;
    }

    public String getCode() {
        return code;
    }

    public Object getDetails() {
        return details;
    }
}
