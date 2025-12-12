package de.skit.grocy.items;

import de.skit.grocy.common.EntityNotFoundException;
import de.skit.grocy.items.dto.ItemCreate;
import de.skit.grocy.items.dto.ListItem;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ItemService {

    private final ItemRepository repository;

    public ItemService(ItemRepository repository) {
        this.repository = repository;
    }

    public ListItem createItem(UUID listId, ItemCreate body) {
        var entity = toEntity(listId, body);
        var saved = repository.save(entity);
        return toDto(saved);
    }

    public List<ListItem> getItems(UUID listId, String filter) {
        String normalized = (filter == null) ? "all" : filter.toLowerCase();
        List<ItemEntity> entities;

        switch (normalized) {
            case "all" -> entities = repository.getAll(listId);
            case "open" -> entities = repository.getAllUnchecked(listId);
            case "checked" -> entities = repository.getAllChecked(listId);
            default -> throw new IllegalArgumentException("Invalid filter: " + filter);
        }

        return entities.stream()
                .map(this::toDto)
                .toList();
    }

    public Optional<ListItem> getItem(UUID listId, UUID itemId) {
        return repository.findByListIdAndId(listId, itemId)
                .map(this::toDto);
    }

    public void deleteItem(UUID listId, UUID itemId) {
        int deletedRows = repository.deleteByListIdAndId(listId, itemId);
        if (deletedRows == 0) {
            throw new EntityNotFoundException("Item " + itemId + " not found in list " + listId);
        }
    }

    public ListItem checkItem(UUID listId, UUID itemId) {
        return setChecked(listId, itemId, true);
    }

    public ListItem uncheckItem(UUID listId, UUID itemId) {
        return setChecked(listId, itemId, false);
    }

    private ListItem setChecked(UUID listId, UUID itemId, boolean checked) {
        var entity = repository.findByListIdAndId(listId, itemId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Item " + itemId + " not found in list " + listId));

        entity.setChecked(checked);
        entity.setUpdatedAt(OffsetDateTime.now());
        // optional: Version hochzählen:
        entity.setVersion(entity.getVersion() + 1);

        var saved = repository.save(entity);
        return toDto(saved);
    }

    private ItemEntity toEntity(UUID listId, ItemCreate body) {
        var now = OffsetDateTime.now();

        return new ItemEntity(
                listId,
                body.householdId(),
                body.userId(),
                body.title(),
                body.quantity(),
                body.unitText(),
                body.categoryId(),
                false, // checked am Anfang immer false
                body.notes(),
                0, // sortIndex vorerst 0
                0, // version = 0
                now,
                now);
    }

    private ListItem toDto(ItemEntity entity) {
        return new ListItem(
                entity.getId(),
                entity.getListId(),
                entity.getHouseholdId(),
                entity.getTitle(),
                entity.getQuantity(),
                entity.getUnitText(),
                entity.getCategoryId(),
                entity.isChecked(),
                entity.getNotes(),
                entity.getSortIndex(),
                entity.getVersion(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
