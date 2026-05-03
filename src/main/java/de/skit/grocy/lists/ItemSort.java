package de.skit.grocy.lists;
import org.springframework.data.domain.Sort;

public enum ItemSort {
    CREATED_ASC,
    CREATED_DESC,
    TITLE_ASC,
    TITLE_DESC,
    CHECKED_ASC,
    CHECKED_DESC;

    public Sort toSort() {
        return switch (this) {
            case CREATED_ASC -> Sort.by("createdAt").ascending();
            case CREATED_DESC -> Sort.by("createdAt").descending();
            case TITLE_ASC -> Sort.by("title").ascending();
            case TITLE_DESC -> Sort.by("title").descending();
            case CHECKED_ASC -> Sort.by("checked").ascending();
            case CHECKED_DESC -> Sort.by("checked").descending();
        };
    }

    public static ItemSort fromString(String value) {
        if (value == null || value.isBlank()) {
            return CREATED_DESC;
        }

        return switch (value.toLowerCase()) {
            case "created_asc" -> CREATED_ASC;
            case "created_desc" -> CREATED_DESC;
            case "title_asc" -> TITLE_ASC;
            case "title_desc" -> TITLE_DESC;
            case "checked_asc" -> CHECKED_ASC;
            case "checked_desc" -> CHECKED_DESC;
            default -> throw new IllegalArgumentException("Invalid sort: " + value);
        };
    }
}
