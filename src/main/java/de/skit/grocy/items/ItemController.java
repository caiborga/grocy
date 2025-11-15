package de.skit.grocy.items;

import de.skit.grocy.items.dto.ItemCreate;
import de.skit.grocy.items.dto.ListItem;
import de.skit.grocy.common.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/lists/{listId}/items")
public class ItemController {

    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    // -------------------------------
    // POST /lists/{listId}/items
    // -------------------------------
    @PostMapping
    public ResponseEntity<ListItem> createItem(
            @PathVariable UUID listId,
            @RequestHeader(value = "X-Household-ID", required = false) UUID householdId,
            @RequestHeader(value = "X-User-ID", required = false) UUID userId,
            @Valid @RequestBody ItemCreate body) {
        // Temporäre Dummy-IDs, falls Header fehlen
        if (householdId == null)
            householdId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        if (userId == null)
            userId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        var created = service.createItem(listId, householdId, userId, body);
        var location = URI.create("/lists/" + listId + "/items/" + created.id());
        return ResponseEntity.created(location).body(created); // 201 Created
    }

    // -------------------------------
    // GET /lists/{listId}/items?filter=open|checked|all
    // -------------------------------
    @GetMapping
    public ResponseEntity<List<ListItem>> listItems(
            @PathVariable UUID listId,
            @RequestParam(name = "filter", defaultValue = "all") String filter) {
        var result = service.getItems(listId, filter);
        return ResponseEntity.ok(result);
    }

    // -------------------------------
    // GET /lists/{listId}/items/{itemId}
    // -------------------------------
    @GetMapping("/{itemId}")
    public ResponseEntity<ListItem> getItem(
            @PathVariable UUID listId,
            @PathVariable UUID itemId) {
        return service.getItem(listId, itemId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Item " + itemId + " not found in list " + listId));
    }

    // -------------------------------
    // DELETE /lists/{listId}/items/{itemId}
    // -------------------------------
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable UUID listId,
            @PathVariable UUID itemId) {
        service.deleteItem(listId, itemId);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------
    // PATCH /lists/{listId}/items/{itemId}/check
    // -------------------------------
    @PatchMapping("/{itemId}/check")
    public ResponseEntity<ListItem> checkItem(
            @PathVariable UUID listId,
            @PathVariable UUID itemId) {
        var result = service.checkItem(listId, itemId);
        return ResponseEntity.ok(result);
    }

    // -------------------------------
    // PATCH /lists/{listId}/items/{itemId}/uncheck
    // -------------------------------
    @PatchMapping("/{itemId}/uncheck")
    public ResponseEntity<ListItem> uncheckItem(
            @PathVariable UUID listId,
            @PathVariable UUID itemId) {
        var result = service.uncheckItem(listId, itemId);
        return ResponseEntity.ok(result);
    }
}
