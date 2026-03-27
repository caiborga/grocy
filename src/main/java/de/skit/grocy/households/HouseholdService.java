package de.skit.grocy.households;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import de.skit.grocy.common.enums.Role;
import de.skit.grocy.common.exceptions.NotFoundException;
import de.skit.grocy.households.dto.HouseholdCreate;
import de.skit.grocy.households.dto.HouseholdDetailResponse;
import de.skit.grocy.households.dto.HouseholdResponse;
import de.skit.grocy.households.dto.HouseholdUpdate;
import de.skit.grocy.households.mapper.HouseholdMapper;
import de.skit.grocy.households.member.HouseholdMemberEntity;
import de.skit.grocy.households.member.HouseholdMemberRepository;
import de.skit.grocy.households.member.mapper.HouseholdMemberMapper;
import de.skit.grocy.items.ItemRepository;
import de.skit.grocy.lists.ListEntity;
import de.skit.grocy.lists.ListRepository;
import de.skit.grocy.lists.dto.Stats;
import de.skit.grocy.lists.mapper.ListMapper;
import de.skit.grocy.security.UserPrincipal;
import de.skit.grocy.user.UserEntity;
import de.skit.grocy.user.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class HouseholdService {
    private final HouseholdRepository householdRepository;
    private final HouseholdMemberRepository householdMemberRepository;
    private final ListRepository listRepository;
    private final ItemRepository itemRepository;
    private final HouseholdMapper householdMapper;
    private final HouseholdMemberMapper memberMapper;
    private final ListMapper listMapper;

    public HouseholdService(
            HouseholdRepository householdRepository,
            HouseholdMemberRepository householdMemberRepository,
            UserRepository userRepository,
            ListRepository listRepository,
            ItemRepository itemRepository,
            HouseholdMapper householdMapper,
            HouseholdMemberMapper memberMapper,
            ListMapper listMapper) {
        this.householdRepository = householdRepository;
        this.householdMemberRepository = householdMemberRepository;
        this.listRepository = listRepository;
        this.itemRepository = itemRepository;
        this.householdMapper = householdMapper;
        this.memberMapper = memberMapper;
        this.listMapper = listMapper;
    }

    public List<HouseholdResponse> getAllHouseholds() {
        return householdRepository.findAll()
                .stream()
                .map(householdMapper::toDto)
                .toList();
    }

    public List<HouseholdResponse> getAllUserHouseholds(UserPrincipal principal) {
        return householdRepository.findDistinctByMembersUserId(principal.getUser().getId())
                .stream()
                .map(householdMapper::toDto)
                .toList();
    }

    @Transactional
    public HouseholdResponse addHousehold(
            HouseholdCreate dto,
            UserPrincipal principal) {

        // Create Household
        HouseholdEntity household = householdMapper.toEntity(dto);
        HouseholdEntity saved = householdRepository.save(household);

        // CreatedBy -> SecurityContext
        UserEntity creator = principal.getUser();

        // Membership
        HouseholdMemberEntity member = new HouseholdMemberEntity();
        member.setHousehold(saved);
        member.setUser(creator);
        member.setRole(Role.OWNER);

        householdMemberRepository.save(member);

        // Default list
        ListEntity defaultList = new ListEntity();
        defaultList.setTitle("Einkaufsliste");
        defaultList.setHousehold(saved);
        defaultList.setCreatedBy(creator);
        defaultList.setDefault(true);
        defaultList.setArchived(false);

        listRepository.save(defaultList);

        // Response
        return householdMapper.toDto(saved);
    }

    public HouseholdDetailResponse getHousehold(UUID id, UserPrincipal principal) {
        HouseholdEntity household = householdRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Household " + id + " not found"));

        var members = householdMemberRepository.findByHouseholdId(household.getId());
        var lists = listRepository.findByHouseholdId(household.getId());

        var memberDtos = members.stream()
                .map(m -> memberMapper.toDto(m))
                .toList();

        var listDtos = lists.stream()
                .map(l -> {
                    long total = itemRepository.countByList(l);
                    long checked = itemRepository.countByListAndChecked(l, true);
                    Stats stats = new Stats((int) total, (int) checked);
                    return listMapper.toResponse(l, stats);
                })
                .toList();

        var householdDto = householdMapper.toDetailDto(household, memberDtos, listDtos);

        return householdDto;
    }

    @Transactional
    public HouseholdResponse updateHousehold(UUID id, HouseholdUpdate update) {

        HouseholdEntity entity = householdRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Household " + id + " not found"));

        householdMapper.applyPatch(update, entity);

        HouseholdEntity updated = householdRepository.save(entity);

        return householdMapper.toDto(updated);
    }
}
