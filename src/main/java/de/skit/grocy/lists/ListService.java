package de.skit.grocy.lists;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import de.skit.grocy.common.EntityNotFoundException;
import de.skit.grocy.common.NotFoundException;
import de.skit.grocy.households.HouseholdEntity;
import de.skit.grocy.households.HouseholdRepository;
import de.skit.grocy.items.ItemRepository;
import de.skit.grocy.lists.dto.ListCreate;
import de.skit.grocy.lists.dto.ListResponse;
import de.skit.grocy.lists.dto.Stats;
import de.skit.grocy.lists.mapper.ListMapper;
import de.skit.grocy.user.UserEntity;
import de.skit.grocy.user.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class ListService {

    private final ListRepository listRepository;
    private final HouseholdRepository householdRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ListMapper mapper;

    public ListService(
            ListRepository listRepository,
            HouseholdRepository householdRepository,
            UserRepository userRepository,
            ItemRepository itemRepository,
            ListMapper mapper) {
        this.listRepository = listRepository;
        this.householdRepository = householdRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.mapper = mapper;
    }

    @Transactional
    public ListResponse createList(UUID householdId, ListCreate dto) {

        HouseholdEntity household = householdRepository.findById(householdId)
                .orElseThrow(() -> new NotFoundException("Household not found"));

        UserEntity creator = userRepository.findById(dto.createdBy())
                .orElseThrow(() -> new NotFoundException("User not found"));

        ListEntity entity = mapper.toEntity(dto);
        entity.setHousehold(household);
        entity.setCreatedBy(creator);

        ListEntity saved = listRepository.save(entity);

        Stats stats = new Stats(
                itemRepository.countByListId(saved.getId()),
                itemRepository.countByListIdAndChecked(saved.getId(), true));

        return mapper.toResponse(saved, stats);
    }

    public List<ListResponse> getAllLists() {

        return listRepository.findAll()
                .stream()
                .map(list -> {
                    Stats stats = new Stats(
                            itemRepository.countByListId(list.getId()),
                            itemRepository.countByListIdAndChecked(list.getId(), true));
                    return mapper.toResponse(list, stats);
                })
                .toList();
    }

    public ListResponse getList(UUID listId) {

        ListEntity list = listRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List not found"));

        int total = itemRepository.countByListId(listId);
        int checked = itemRepository.countByListIdAndChecked(listId, true);

        return new ListResponse(
                list.getId(),
                list.getTitle(),
                list.isArchived(),
                new Stats(total, checked));
    }

    public void deleteList(UUID listId) {
        listRepository.deleteById(listId);
    }
}
