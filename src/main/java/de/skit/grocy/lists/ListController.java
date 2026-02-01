package de.skit.grocy.lists;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import de.skit.grocy.common.exceptions.NotFoundException;
import de.skit.grocy.households.HouseholdRepository;
import de.skit.grocy.lists.dto.ListCreate;
import de.skit.grocy.lists.dto.ListResponse;
import de.skit.grocy.lists.dto.ListUpdate;
import de.skit.grocy.security.UserPrincipal;
import de.skit.grocy.user.UserRepository;

@RestController
class ListController {

    private final ListService service;

    public ListController(
            HouseholdRepository householdRepository,
            UserRepository userRepository,
            ListService service) {
        this.service = service;
    }

    @GetMapping("/lists")
    List<ListResponse> all() {
        return service.getAllLists();
    }

    @PostMapping("/households/{householdId}/lists")
    ListResponse createList(
            @PathVariable UUID householdId,
            @RequestBody ListCreate dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        return service.createList(householdId, dto, principal);
    }

    @GetMapping("/lists/{id}")
    public ListResponse one(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return service.getList(id, principal);
    }

    @GetMapping("/lists/default")
    public ListResponse getDefaultList(@AuthenticationPrincipal UserPrincipal principal) {
        return service.getDefaultList(principal);
    }

    @PatchMapping("/lists/{id}")
    public ListResponse updateList(@PathVariable UUID id, @RequestBody ListUpdate dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/lists/{id}")
    void deleteList(@PathVariable UUID id) {
        service.deleteList(id);
    }

}
