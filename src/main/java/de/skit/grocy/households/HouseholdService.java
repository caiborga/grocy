package de.skit.grocy.households;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import de.skit.grocy.common.NotFoundException;
import de.skit.grocy.households.dto.HouseholdCreate;
import de.skit.grocy.households.dto.HouseholdResponse;
import de.skit.grocy.households.dto.HouseholdUpdate;
import de.skit.grocy.households.mapper.HouseholdMapper;
import de.skit.grocy.households.member.HouseholdMemberEntity;
import de.skit.grocy.households.member.HouseholdMemberRepository;
import de.skit.grocy.user.UserEntity;
import de.skit.grocy.user.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class HouseholdService {
    private final HouseholdRepository householdRepository;
    private final HouseholdMemberRepository householdMemberRepository;
    private final UserRepository userRepository;
    private final HouseholdMapper mapper;

    public HouseholdService(
            HouseholdRepository householdRepository,
            HouseholdMemberRepository householdMemberRepository,
            UserRepository userRepository,
            HouseholdMapper mapper) {
        this.householdRepository = householdRepository;
        this.householdMemberRepository = householdMemberRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    public List<HouseholdResponse> getAllHouseholds() {
        return householdRepository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public HouseholdResponse addHousehold(HouseholdCreate dto) {
        HouseholdEntity entity = mapper.toEntity(dto);
        HouseholdEntity saved = householdRepository.save(entity);

        UserEntity creator = userRepository.findById(dto.createdBy())
            .orElseThrow(() -> new NotFoundException("User not found"));

        HouseholdMemberEntity member = new HouseholdMemberEntity();
        member.setHousehold(saved);
        member.setUser(creator);
        member.setRole("ADMIN");

        householdMemberRepository.save(member);

        return mapper.toDto(saved);
    }

    public HouseholdResponse getHousehold(UUID id) {
        HouseholdEntity entity = householdRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Household " + id + " not found"));
        return mapper.toDto(entity);
    }

    @Transactional
    public HouseholdResponse updateHousehold(UUID id, HouseholdUpdate update) {

        HouseholdEntity entity = householdRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Household " + id + " not found"));

        mapper.applyPatch(update, entity);

        HouseholdEntity updated = householdRepository.save(entity);

        return mapper.toDto(updated);
    }
}
