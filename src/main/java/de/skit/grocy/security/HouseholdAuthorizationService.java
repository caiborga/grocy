package de.skit.grocy.security;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import de.skit.grocy.common.enums.Permission;
import de.skit.grocy.common.enums.Role;
import de.skit.grocy.households.member.HouseholdMemberEntity;
import de.skit.grocy.households.member.HouseholdMemberRepository;
import de.skit.grocy.user.UserEntity;

@Service
public class HouseholdAuthorizationService {

    private final HouseholdMemberRepository repo;

    public HouseholdAuthorizationService(HouseholdMemberRepository repo) {
        this.repo = repo;
    }

    public void require(
        UUID householdId,
        UserEntity user,
        Permission permission
    ) {
        Role role = repo.findByHouseholdIdAndUserId(householdId, user.getId())
            .map(HouseholdMemberEntity::getRole)
            .orElseThrow(() -> new AccessDeniedException("Not a member"));

        if (!Permissions.forRole(role).contains(permission)) {
            throw new AccessDeniedException("Missing permission: " + permission);
        }
    }
}

