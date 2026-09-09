package de.skit.grocy.security;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.skit.grocy.common.enums.Role;
import de.skit.grocy.common.exceptions.NotFoundException;
import de.skit.grocy.households.member.HouseholdMemberRepository;
import de.skit.grocy.security.dto.MeResponse;
import de.skit.grocy.user.UserEntity;
import de.skit.grocy.user.UserRepository;

@Service
public class MeService {

    private final HouseholdMemberRepository householdMemberRepository;
    private final UserRepository userRepository;
    private final PlatformAdminService platformAdminService;

    public MeService(
            HouseholdMemberRepository householdMemberRepository,
            UserRepository userRepository,
            PlatformAdminService platformAdminService) {
        this.householdMemberRepository = householdMemberRepository;
        this.userRepository = userRepository;
        this.platformAdminService = platformAdminService;
    }

    public MeResponse getMe(UserPrincipal principal) {
        UserEntity user = principal.getUser();
        UUID householdId = user.getActiveHouseholdId();
        Role role = null;
        if (householdId != null) {
            role = householdMemberRepository
                    .findByHouseholdIdAndUserId(householdId, user.getId())
                    .map(member -> member.getRole())
                    .orElse(null);
        }

        return new MeResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getActiveHouseholdId(),
                role,
                platformAdminService.isAdmin(user)
            );
    }

    @Transactional
    public void setActiveHousehold(UUID householdId, UserPrincipal principal) {
        UserEntity user = principal.getUser();

        boolean isMember = householdMemberRepository
                .existsByHouseholdIdAndUserId(householdId, user.getId());

        if (!isMember) {
            throw new NotFoundException("Nicht gefunden");
        }

        user.setActiveHouseholdId(householdId);
        userRepository.save(user);
    }
}
