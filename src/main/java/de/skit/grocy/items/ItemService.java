package de.skit.grocy.items;

import de.skit.grocy.items.dto.ItemCreate;
import de.skit.grocy.items.dto.ListItem;
import de.skit.grocy.common.EntityNotFoundException;

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

    public ListItem createItem(UUID listId, UUID householdId, UUID createdBy, ItemCreate body) {
        var entity = toEntity(listId, body);
        var saved = repository.save(entity);
        return toDto(saved);
    }

    public List<ListItem> getItems(UUID listId, String filter) {
        List<ItemEntity> entities;

        if (filter == null || filter.isBlank()) {
            entities = repository.findByListId(listId);
        } else {
            entities = repository.findByListIdAndNameContainingIgnoreCase(listId, filter);
        }

        return entities.stream()
                .map(this::toDto)
                .toList();
    }

    public Optional<ListItem> getItem(UUID listId, UUID itemId) {
        return repository.findByListIdAndId(listId, itemId).map(this::toDto);
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
        var entity = repository.findByListIdAndId(listId, itemId).orElseThrow(() -> new EntityNotFoundException(
                "Item " + itemId + " not found in list " + listId));
        entity.setChecked(checked);
        entity.setUpdatedAt(OffsetDateTime.now());
        var saved = repository.save(entity);
        return toDto(saved);
    }

    private ItemEntity toEntity(UUID listId, ItemCreate body) {
        var now = OffsetDateTime.now();

        var entity = new ItemEntity(
                listId,
                body.title(), // oder body.getTitle(), je nachdem wie ItemCreate aussieht
                body.notes(), // an deine Felder anpassen
                false, // checked = false am Anfang
                body.quantity(), // oder null, falls du noch kein quantity hast
                body.unitText(), // anpassen
                0, // sortOrder, vorerst 0 oder später berechnet
                now,
                now);

        return entity;
    }

    private ListItem toDto(ItemEntity entity) {
        return new ListItem(
                entity.getId(),
                entity.getListId(),
                /* householdId */ null, // falls dein DTO das hat und du es noch nicht aus DB holst
                /* createdBy */ null, // dito
                entity.getName(),
                entity.getNote(),
                entity.getAmount(),
                entity.getUnit(),
                /* categoryId */ null, // ggf. später
                entity.getChecked(),
                entity.getSortOrder(),
                /* version */ 0, // wenn du Versionierung hast
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

}
