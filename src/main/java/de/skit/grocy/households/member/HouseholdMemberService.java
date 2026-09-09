package de.skit.grocy.households.member;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import de.skit.grocy.common.enums.Permission;
import de.skit.grocy.common.enums.Role;
import de.skit.grocy.common.exceptions.ConflictException;
import de.skit.grocy.common.exceptions.NotFoundException;
import de.skit.grocy.households.HouseholdEntity;
import de.skit.grocy.households.HouseholdRepository;
import de.skit.grocy.security.HouseholdAuthorizationService;
import de.skit.grocy.security.UserPrincipal;
import de.skit.grocy.user.UserEntity;
import de.skit.grocy.user.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class HouseholdMemberService {

    private final HouseholdRepository householdRepository;
    private final UserRepository userRepository;
    private final HouseholdMemberRepository memberRepository;
    private final HouseholdAuthorizationService householdAuth;

    public HouseholdMemberService(
            HouseholdRepository householdRepository,
            UserRepository userRepository,
            HouseholdMemberRepository memberRepository,
            HouseholdAuthorizationService householdAuth) {
        this.householdRepository = householdRepository;
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.householdAuth = householdAuth;
    }

    public HouseholdMemberEntity addUserToHousehold(UUID householdId, UUID userId, Role role, UserPrincipal principal) {
        householdAuth.require(householdId, principal.getUser(), Permission.MEMBER_INVITE);
        if (role == Role.OWNER) {
            throw new IllegalArgumentException("OWNER kann nicht direkt hinzugefügt werden");
        }
        return addUserToHouseholdInternal(householdId, userId, role);
    }

    public HouseholdMemberEntity addUserToHouseholdIfNotExists(UUID householdId, UUID userId, Role role) {
        if (memberRepository.existsByHouseholdIdAndUserId(householdId, userId)) {
            return memberRepository.findByHouseholdIdAndUserId(householdId, userId)
                    .orElseThrow();
        }
        return addUserToHouseholdInternal(householdId, userId, role);
    }

    private HouseholdMemberEntity addUserToHouseholdInternal(UUID householdId, UUID userId, Role role) {
        HouseholdEntity household = householdRepository.findById(householdId)
                .orElseThrow(() -> new NotFoundException("Nicht gefunden"));

        UserEntity user = userRepository.findById(userId)
                .filter(candidate -> candidate.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Nicht gefunden"));

        if (memberRepository.existsByHouseholdIdAndUserId(householdId, userId)) {
            throw new ConflictException("already_member", "User already assigned to this household", null);
        }

        HouseholdMemberEntity member = new HouseholdMemberEntity();
        member.setHousehold(household);
        member.setUser(user);
        member.setRole(role);
        return memberRepository.save(member);
    }

    @Transactional
    public HouseholdMemberEntity editUserRole(
            UUID householdId,
            UUID targetUserId,
            Role newRole,
            UserPrincipal principal) {
        householdAuth.require(householdId, principal.getUser(), Permission.MEMBER_ROLE_CHANGE);

        HouseholdMemberEntity target = memberRepository
                .findByHouseholdIdAndUserId(householdId, targetUserId)
                .orElseThrow(() -> new NotFoundException("Nicht gefunden"));

        UUID actingUserId = principal.getUser().getId();
        if (targetUserId.equals(actingUserId)) {
            throw new IllegalArgumentException("Die eigene Rolle kann nicht geändert werden");
        }

        if (target.getRole() == Role.OWNER && newRole != Role.OWNER) {
            long ownerCount = memberRepository.countByHouseholdIdAndRole(householdId, Role.OWNER);
            if (ownerCount <= 1) {
                throw new ConflictException(
                        "last_owner",
                        "Der Haushalt braucht mindestens einen Eigentümer",
                        null);
            }
        }

        target.setRole(newRole);
        return memberRepository.save(target);
    }

    @Transactional
    public void removeUserFromHousehold(UUID householdId, UUID userId, UserPrincipal principal) {
        UUID actingUserId = principal.getUser().getId();
        boolean selfLeave = actingUserId.equals(userId);

        if (selfLeave) {
            householdAuth.requireMember(householdId, principal.getUser());
        } else {
            householdAuth.require(householdId, principal.getUser(), Permission.MEMBER_DELETE);
        }

        HouseholdMemberEntity target = memberRepository
                .findByHouseholdIdAndUserId(householdId, userId)
                .orElseThrow(() -> new NotFoundException("Nicht gefunden"));

        if (target.getRole() == Role.OWNER) {
            long ownerCount = memberRepository.countByHouseholdIdAndRole(householdId, Role.OWNER);
            long memberCount = memberRepository.countByHouseholdId(householdId);
            if (ownerCount <= 1 && memberCount > 1) {
                throw new ConflictException(
                        "last_owner",
                        "Bitte übertrage zuerst das Eigentum, bevor der letzte Eigentümer den Haushalt verlässt.",
                        null);
            }
        }

        memberRepository.deleteByHouseholdIdAndUserId(householdId, userId);
    }

    @Transactional
    public HouseholdMemberEntity transferOwnership(UUID householdId, UUID newOwnerId, UserPrincipal principal) {
        householdAuth.require(householdId, principal.getUser(), Permission.HOUSEHOLD_TRANSFER);

        UUID actingUserId = principal.getUser().getId();
        if (actingUserId.equals(newOwnerId)) {
            throw new IllegalArgumentException("Eigentum kann nicht an sich selbst übertragen werden");
        }

        HouseholdMemberEntity currentOwner = memberRepository
                .findByHouseholdIdAndUserId(householdId, actingUserId)
                .orElseThrow(() -> new AccessDeniedException("Keine Berechtigung"));

        HouseholdMemberEntity target = memberRepository
                .findByHouseholdIdAndUserId(householdId, newOwnerId)
                .orElseThrow(() -> new NotFoundException("Nicht gefunden"));

        target.setRole(Role.OWNER);
        currentOwner.setRole(Role.EDITOR);
        memberRepository.save(target);
        return memberRepository.save(currentOwner);
    }
}
