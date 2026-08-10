package de.skit.grocy.items.dto;

public record ItemPatch(
        String title,
        Boolean checked,
        Double quantity,
        String unitText,
        String brand) {
}
