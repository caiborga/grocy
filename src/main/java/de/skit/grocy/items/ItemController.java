package de.skit.grocy.items;

import de.skit.grocy.items.dto.ItemCreate;
import de.skit.grocy.items.dto.ItemPatch;
import de.skit.grocy.items.dto.ListItem;
import de.skit.grocy.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/lists/{listId}/items")
public class ItemController {

    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    @GetMapping
    public List<ListItem> getItems(
            @PathVariable UUID listId,
            @RequestParam(required = false) String filter,
            @AuthenticationPrincipal UserPrincipal principal) {

        return service.getItems(listId, filter, principal);
    }

    @PostMapping
    public ListItem createItem(
            @PathVariable UUID listId,
            @RequestBody ItemCreate body,
            @AuthenticationPrincipal UserPrincipal principal) {

        return service.createItem(listId, body, principal);
    }

    @PatchMapping("/{itemId}")
    public ListItem updateItem(
            @PathVariable UUID listId,
            @PathVariable UUID itemId,
            @RequestBody ItemPatch body,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (body == null) {
            throw new IllegalArgumentException("Missing body");
        }
        return service.updateItem(listId, itemId, body, principal);
    }

    @DeleteMapping("/{itemId}")
    public ListItem deleteItem(
            @PathVariable UUID listId,
            @PathVariable UUID itemId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return service.deleteItem(listId, itemId, principal);
    }

    @DeleteMapping("/checked")
    public Map<String, Integer> deleteCheckedItems(
            @PathVariable UUID listId,
            @AuthenticationPrincipal UserPrincipal principal) {
        int deleted = service.deleteCheckedItems(listId, principal);
        return Map.of("deleted", deleted);
    }
}
