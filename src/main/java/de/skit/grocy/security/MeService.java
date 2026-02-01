package de.skit.grocy.security;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.skit.grocy.common.exceptions.NotFoundException;
import de.skit.grocy.households.member.HouseholdMemberEntity;
import de.skit.grocy.households.member.HouseholdMemberRepository;
import de.skit.grocy.security.dto.MeResponse;
import de.skit.grocy.user.UserEntity;
import de.skit.grocy.user.UserRepository;

@Service
public class MeService {

    private final HouseholdMemberRepository householdMemberRepository;
    private final UserRepository userRepository;

    public MeService(
            HouseholdMemberRepository householdMemberRepository,
            UserRepository userRepository) {
        this.householdMemberRepository = householdMemberRepository;
        this.userRepository = userRepository;
    }

    public MeResponse getMe(UserPrincipal principal) {
        UserEntity user = principal.getUser();
        UUID householdId = user.getActiveHouseholdId();
        HouseholdMemberEntity member = householdMemberRepository
                .findByHouseholdIdAndUserId(householdId, user.getId())
                .orElseThrow(() -> new NotFoundException("User is not member of household"));
        
        return new MeResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getActiveHouseholdId(),
                member.getRole()
            );
    }

    @Transactional
    public void setActiveHousehold(UUID householdId, UserPrincipal principal) {
        UserEntity user = principal.getUser();

        boolean isMember = householdMemberRepository
                .existsByHouseholdIdAndUserId(householdId, user.getId());

        if (!isMember) {
            throw new NotFoundException("Household " + householdId + " not found");
        }

        user.setActiveHouseholdId(householdId);
        userRepository.save(user);
    }
}
