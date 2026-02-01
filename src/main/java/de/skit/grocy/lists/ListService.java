package de.skit.grocy.lists;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.skit.grocy.common.enums.Permission;
import de.skit.grocy.common.exceptions.EntityNotFoundException;
import de.skit.grocy.common.exceptions.NotFoundException;
import de.skit.grocy.households.HouseholdEntity;
import de.skit.grocy.households.HouseholdRepository;
import de.skit.grocy.households.member.HouseholdMemberEntity;
import de.skit.grocy.households.member.HouseholdMemberRepository;
import de.skit.grocy.items.ItemRepository;
import de.skit.grocy.lists.dto.ListCreate;
import de.skit.grocy.lists.dto.ListResponse;
import de.skit.grocy.lists.dto.ListUpdate;
import de.skit.grocy.lists.dto.Stats;
import de.skit.grocy.lists.mapper.ListMapper;
import de.skit.grocy.security.HouseholdAuthorizationService;
import de.skit.grocy.security.UserPrincipal;
import de.skit.grocy.user.UserEntity;
import de.skit.grocy.user.UserRepository;

@Service
public class ListService {

    private final ListRepository listRepository;
    private final HouseholdRepository householdRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final HouseholdMemberRepository householdMemberRepository;
    private final ListMapper mapper;
    private final HouseholdAuthorizationService householdAuth;

    public ListService(
            ListRepository listRepository,
            HouseholdRepository householdRepository,
            UserRepository userRepository,
            ItemRepository itemRepository,
            HouseholdMemberRepository householdMemberRepository,
            ListMapper mapper,
            HouseholdAuthorizationService householdAuth) {
        this.listRepository = listRepository;
        this.householdRepository = householdRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.householdMemberRepository = householdMemberRepository;
        this.mapper = mapper;
        this.householdAuth = householdAuth;
    }

    @Transactional
    public ListResponse createList(UUID householdId, ListCreate dto, UserPrincipal principal) {

        HouseholdEntity household = householdRepository.findById(householdId)
                .orElseThrow(() -> new NotFoundException("Household not found"));

        UserEntity creator = userRepository.findById(principal.getUser().getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (dto.isDefault()) {
            unsetDefaultList(household.getId());
        }

        ListEntity entity = new ListEntity();
        entity.setTitle(dto.title());
        entity.setHousehold(household);
        entity.setCreatedBy(creator);
        entity.setDefault(dto.isDefault());

        ListEntity saved = listRepository.save(entity);

        long total = itemRepository.countByList(saved);
        long checked = itemRepository.countByListAndChecked(saved, true);

        Stats stats = new Stats(
                (int) total,
                (int) checked);

        return mapper.toResponse(saved, stats);
    }

    public List<ListResponse> getAllLists() {

        return listRepository.findAll()
                .stream()
                .map(list -> {

                    long total = itemRepository.countByList(list);
                    long checked = itemRepository.countByListAndChecked(list, true);

                    Stats stats = new Stats(
                            (int) total,
                            (int) checked);

                    return mapper.toResponse(list, stats);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public ListResponse getList(UUID listId, UserPrincipal principal) {

        ListEntity list = listRepository.findById(listId)
                .orElseThrow(() -> new NotFoundException("List " + listId + " not found"));

        householdAuth.require(list.getHousehold().getId(), principal.getUser(), Permission.LIST_READ);

        long total = itemRepository.countByList(list);
        long checked = itemRepository.countByListAndChecked(list, true);

        return new ListResponse(
                list.getId(),
                list.getTitle(),
                list.isArchived(),
                new Stats((int) total, (int) checked),
                list.isDefault());
    }

    public void deleteList(UUID listId) {
        listRepository.deleteById(listId);
    }

    @Transactional
    public ListResponse update(UUID listId, ListUpdate dto) {

        ListEntity list = listRepository.findById(listId)
                .orElseThrow(() -> new NotFoundException("List not found"));

        if (dto.title() != null) {
            list.setTitle(dto.title());
        }

        if (dto.isDefault() && !list.isDefault()) {
            unsetDefaultList(list.getHousehold().getId());
            list.setDefault(true);
        }

        long total = itemRepository.countByList(list);
        long checked = itemRepository.countByListAndChecked(list, true);

        return new ListResponse(
                list.getId(),
                list.getTitle(),
                list.isArchived(),
                new Stats(
                        (int) total,
                        (int) checked),
                list.isDefault());
    }

    @Transactional(readOnly = true)
    public ListResponse getDefaultList(UserPrincipal principal) {
        UserEntity user = principal.getUser();

        // 1) aktiven Haushalt verwenden (wenn gesetzt)
        UUID activeHouseholdId = user.getActiveHouseholdId();

        HouseholdEntity household = null;

        if (activeHouseholdId != null) {
            // Security: User muss Mitglied sein
            boolean isMember = householdMemberRepository
                    .existsByHouseholdIdAndUserId(activeHouseholdId, user.getId());

            if (isMember) {
                household = householdRepository.findById(activeHouseholdId)
                        .orElse(null);
            }
        }

        // 2) Fallback: erstes Household des Users
        if (household == null) {
            List<HouseholdMemberEntity> memberships = householdMemberRepository.findByUserId(user.getId());

            if (memberships.isEmpty()) {
                throw new IllegalStateException("User has no household");
            }

            household = memberships.get(0).getHousehold();
        }

        // 3) Default-Liste des Households holen
        ListEntity list = listRepository
                .findByHouseholdIdAndIsDefaultTrue(household.getId())
                .orElseThrow(() -> new IllegalStateException("Household has no default list"));

        long total = itemRepository.countByList(list);
        long checked = itemRepository.countByListAndChecked(list, true);

        return new ListResponse(
                list.getId(),
                list.getTitle(),
                list.isArchived(),
                new Stats((int) total, (int) checked),
                list.isDefault());
    }

    private void unsetDefaultList(UUID householdId) {
        listRepository.findByHouseholdIdAndIsDefaultTrue(householdId)
                .ifPresent(list -> {
                    list.setDefault(false);
                    listRepository.save(list);
                });
    }
}
