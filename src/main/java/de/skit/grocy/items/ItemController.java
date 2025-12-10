package de.skit.grocy.items;

import de.skit.grocy.items.dto.ItemCreate;
import de.skit.grocy.items.dto.ListItem;
import de.skit.grocy.common.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/lists/{listId}/items")
public class ItemController {

    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    // GET /lists/{listId}/items?filter=all|open|checked
    @GetMapping
    public List<ListItem> getItems(
            @PathVariable UUID listId,
            @RequestParam(name = "filter", required = false) String filter) {
        return service.getItems(listId, filter);
    }

    // POST /lists/{listId}/items
    @PostMapping
    public ListItem createItem(
            @PathVariable UUID listId,
            @RequestBody ItemCreate body) {
        return service.createItem(listId, body);
    }

    // GET /lists/{listId}/items/{itemId}
    @GetMapping("/{itemId}")
    public ResponseEntity<ListItem> getItem(
            @PathVariable UUID listId,
            @PathVariable UUID itemId) {
        return service.getItem(listId, itemId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new NotFoundException(
                        "Item " + itemId + " not found in list " + listId));
    }

    // DELETE /lists/{listId}/items/{itemId}
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable UUID listId,
            @PathVariable UUID itemId) {
        service.deleteItem(listId, itemId);
        return ResponseEntity.noContent().build();
    }

    // PATCH /lists/{listId}/items/{itemId}/check
    @PatchMapping("/{itemId}/check")
    public ResponseEntity<ListItem> checkItem(
            @PathVariable UUID listId,
            @PathVariable UUID itemId) {
        var result = service.checkItem(listId, itemId);
        return ResponseEntity.ok(result);
    }

    // PATCH /lists/{listId}/items/{itemId}/uncheck
    @PatchMapping("/{itemId}/uncheck")
    public ResponseEntity<ListItem> uncheckItem(
            @PathVariable UUID listId,
            @PathVariable UUID itemId) {
        var result = service.uncheckItem(listId, itemId);
        return ResponseEntity.ok(result);
    }
}
