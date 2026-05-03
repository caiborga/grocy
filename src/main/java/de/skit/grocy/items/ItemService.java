package de.skit.grocy.items;

import de.skit.grocy.common.exceptions.EntityNotFoundException;
import de.skit.grocy.common.exceptions.NotFoundException;
import de.skit.grocy.households.HouseholdEntity;
import de.skit.grocy.households.member.HouseholdMemberRepository;
import de.skit.grocy.items.dto.ItemCreate;
import de.skit.grocy.items.dto.ItemPatch;
import de.skit.grocy.items.dto.ListItem;
import de.skit.grocy.lists.ItemSort;
import de.skit.grocy.lists.ListEntity;
import de.skit.grocy.lists.ListRepository;
import de.skit.grocy.security.UserPrincipal;
import de.skit.grocy.user.UserEntity;
import jakarta.transaction.Transactional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ItemService {

    private final ItemRepository repository;
    private final ListRepository listRepository;
    private final HouseholdMemberRepository householdMemberRepository;

    public ItemService(
            ItemRepository repository,
            ListRepository listRepository,
            HouseholdMemberRepository householdMemberRepository) {
        this.repository = repository;
        this.listRepository = listRepository;
        this.householdMemberRepository = householdMemberRepository;
    }

    @Transactional
    public ListItem createItem(
            UUID listId,
            ItemCreate dto,
            UserPrincipal principal) {

        UserEntity user = principal.getUser();

        ListEntity list = listRepository.findById(listId)
                .orElseThrow(() -> new NotFoundException("List not found"));

        assertUserIsMemberOfHousehold(user, list.getHousehold());

        ItemEntity item = new ItemEntity();
        item.setTitle(dto.title());
        item.setList(list);
        item.setHousehold(list.getHousehold());
        item.setCreatedBy(user);
        item.setQuantity(dto.quantity());
        item.setUnitText(dto.unitText());
        item.setNotes(dto.notes());

        repository.save(item);

        return toDto(item);
    }

    public List<ListItem> getItems(
            UUID listId,
            String filter,
            String sort,
            UserPrincipal principal) {

        ListEntity list = listRepository.findById(listId)
                .orElseThrow(() -> new EntityNotFoundException("List not found"));

        assertUserIsMemberOfHousehold(
                principal.getUser(),
                list.getHousehold());

        String normalizedFilter = (filter == null) ? "all" : filter.toLowerCase();

        Sort springSort = ItemSort.fromString(sort).toSort();

        List<ItemEntity> entities = switch (normalizedFilter) {
            case "all" -> repository.findByList(list, springSort);
            case "open" -> repository.findByListAndChecked(list, false, springSort);
            case "checked" -> repository.findByListAndChecked(list, true, springSort);
            default -> throw new IllegalArgumentException("Invalid filter: " + filter);
        };

        return entities.stream()
                .map(this::toDto)
                .toList();
    }

    public Optional<ListItem> getItem(
            UUID listId,
            UUID itemId,
            UserPrincipal principal) {

        ListEntity list = listRepository.findById(listId)
                .orElseThrow(() -> new EntityNotFoundException("List not found"));

        assertUserIsMemberOfHousehold(
                principal.getUser(),
                list.getHousehold());

        return repository.findByIdAndList(itemId, list)
                .map(this::toDto);
    }

    @Transactional
    public ListItem deleteItem(
            UUID listId,
            UUID itemId,
            UserPrincipal principal) {

        ListEntity list = listRepository.findById(listId)
                .orElseThrow(() -> new EntityNotFoundException("List not found"));

        assertUserIsMemberOfHousehold(
                principal.getUser(),
                list.getHousehold());

        ItemEntity item = repository.findByIdAndList(itemId, list)
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));

        repository.delete(item);
        return toDto(item);

    }

    @Transactional
    public int deleteCheckedItems(UUID listId, UserPrincipal principal) {
        ListEntity list = listRepository.findById(listId)
                .orElseThrow(() -> new EntityNotFoundException("List not found"));

        assertUserIsMemberOfHousehold(principal.getUser(), list.getHousehold());

        return repository.deleteByListAndCheckedTrue(list);
    }

    @Transactional
    public ListItem setChecked(
            UUID listId,
            UUID itemId,
            boolean checked,
            UserPrincipal principal) {

        ListEntity list = listRepository.findById(listId)
                .orElseThrow(() -> new EntityNotFoundException("List not found"));

        assertUserIsMemberOfHousehold(
                principal.getUser(),
                list.getHousehold());

        ItemEntity item = repository.findByIdAndList(itemId, list)
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));

        item.setChecked(checked);

        return toDto(item);
    }

    @Transactional
    public ListItem updateItem(
            UUID listId,
            UUID itemId,
            ItemPatch body,
            UserPrincipal principal) {
        // 1. Liste laden
        ListEntity list = listRepository.findById(listId)
                .orElseThrow(() -> new EntityNotFoundException("List not found"));

        // 2. Berechtigung prüfen
        assertUserIsMemberOfHousehold(
                principal.getUser(),
                list.getHousehold());

        // 3. Item laden
        ItemEntity item = repository.findByIdAndList(itemId, list)
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));

        boolean changed = false;

        // 4. checked patchen (auch false zulassen!)
        if (body.checked() != null) {
            item.setChecked(body.checked());
            changed = true;
        }

        // 5. title patchen (wenn vorhanden → validieren)
        if (body.title() != null) {
            String trimmed = body.title().trim();
            if (trimmed.isEmpty()) {
                throw new IllegalArgumentException("title must not be blank");
            }
            item.setTitle(trimmed);
            changed = true;
        }

        // 6. Optional: nichts zu patchen → Fehler
        if (!changed) {
            throw new IllegalArgumentException("No fields provided to update");
        }

        // 7. JPA dirty checking übernimmt das Speichern
        return toDto(item);
    }

    private ListItem toDto(ItemEntity entity) {
        return new ListItem(
                entity.getId(),
                entity.getList().getId(),
                entity.getHousehold().getId(),
                entity.getTitle(),
                entity.getQuantity(),
                entity.getUnitText(),
                entity.getCategoryId(),
                entity.isChecked(),
                entity.getNotes(),
                entity.getSortIndex(),
                entity.getVersion(),
                entity.getCreatedBy().getId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private void assertUserIsMemberOfHousehold(
            UserEntity user,
            HouseholdEntity household) {

        boolean isMember = householdMemberRepository
                .existsByUserAndHousehold(user, household);

        if (!isMember) {
            throw new AccessDeniedException(
                    "User is not a member of this household");
        }
    }
}
