package de.skit.grocy.lists;

public enum ItemFilter {    
    ALL,
    OPEN,
    CHECKED;

    public static ItemFilter fromString(String value) {
        if (value == null || value.isBlank()) {
            return ALL;
        }

        return switch (value.toLowerCase()) {
            case "all" -> ALL;
            case "open" -> OPEN;
            case "checked" -> CHECKED;
            default -> throw new IllegalArgumentException("Invalid filter: " + value);
        };
    }
}
