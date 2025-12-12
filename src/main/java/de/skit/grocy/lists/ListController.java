package de.skit.grocy.lists;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import de.skit.grocy.common.NotFoundException;
import de.skit.grocy.households.HouseholdRepository;
import de.skit.grocy.lists.dto.ListCreate;
import de.skit.grocy.lists.dto.ListResponse;
import de.skit.grocy.user.UserRepository;

@RestController
class ListController {

    private final ListRepository repository;
    private final ListService service;

    public ListController(
            ListRepository listRepository,
            HouseholdRepository householdRepository,
            UserRepository userRepository,
            ListService service) {
        this.repository = listRepository;
        this.service = service;
    }

    @GetMapping("/lists")
    List<ListResponse> all() {
        return service.getAllLists();
    }

    @PostMapping("/households/{householdId}/lists")
    ListResponse createList(
            @PathVariable UUID householdId,
            @RequestBody ListCreate dto) {
        return service.createList(householdId, dto);
    }

    @GetMapping("/lists/{id}")
    ListEntity one(@PathVariable UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("List " + id + "not found!"));
    }

    @PatchMapping("/lists/{id}")
    public ListEntity updateList(@PathVariable UUID id, @RequestBody ListCreate dto) {
        return repository.findById(id)
                .map(list -> {
                    list.setTitle(dto.title());
                    return repository.save(list);
                })
                .orElseThrow(() -> new NotFoundException("List " + id + "not found!"));
    }

    @DeleteMapping("/lists/{id}")
    void deleteList(@PathVariable UUID id) {
        service.deleteList(id);
    }

}
