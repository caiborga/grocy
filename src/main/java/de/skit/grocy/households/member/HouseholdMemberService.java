package de.skit.grocy.households.member;

import java.util.UUID;

import org.springframework.stereotype.Service;

import de.skit.grocy.common.enums.Role;
import de.skit.grocy.common.exceptions.NotFoundException;
import de.skit.grocy.households.HouseholdEntity;
import de.skit.grocy.households.HouseholdRepository;
import de.skit.grocy.user.UserEntity;
import de.skit.grocy.user.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class HouseholdMemberService {

    private final HouseholdRepository householdRepository;
    private final UserRepository userRepository;
    private final HouseholdMemberRepository memberRepository;

    public HouseholdMemberService(
            HouseholdRepository householdRepository,
            UserRepository userRepository,
            HouseholdMemberRepository memberRepository) {
        this.householdRepository = householdRepository;
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
    }

    public HouseholdMemberEntity addUserToHousehold(UUID householdId, UUID userId, Role role) {

        // 1. Get Household
        HouseholdEntity household = householdRepository.findById(householdId)
                .orElseThrow(() -> new RuntimeException("Household not found"));

        // 2. Get User
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Already assigned?
        boolean exists = memberRepository.existsByHouseholdIdAndUserId(householdId, userId);
        if (exists) {
            throw new RuntimeException("User already assigned to this household");
        }

        // 4. Create relation
        HouseholdMemberEntity member = new HouseholdMemberEntity();
        member.setHousehold(household);
        member.setUser(user);
        member.setRole(role);

        // 5. Save
        return memberRepository.save(member);
    }

    public HouseholdMemberEntity addUserToHouseholdIfNotExists(UUID householdId, UUID userId, Role role) {
        if (memberRepository.existsByHouseholdIdAndUserId(householdId, userId)) {
            return memberRepository.findByHouseholdIdAndUserId(householdId, userId)
                    .orElseThrow(); // sollte existieren
        }
        return addUserToHousehold(householdId, userId, role);
    }

    @Transactional
    public HouseholdMemberEntity editUserRole(
            UUID householdId,
            UUID targetUserId,
            Role newRole,
            UUID actingUserId) {
        // 1. Get target user
        HouseholdMemberEntity target = memberRepository
                .findByHouseholdIdAndUserId(householdId, targetUserId)
                .orElseThrow(() -> new NotFoundException("User is not member of this household"));

        // 2. Aktuellen Benutzer laden
        HouseholdMemberEntity actor = memberRepository
                .findByHouseholdIdAndUserId(householdId, actingUserId)
                .orElseThrow(() -> new NotFoundException("You are not member of this household"));

        // 3. Berechtigung prüfen
        if (actor.getRole() != Role.OWNER) {
            throw new NotFoundException("Only OWNER can change roles");
        }

        // 4. Selbstschutz
        if (targetUserId.equals(actingUserId)) {
            throw new NotFoundException("You cannot change your own role");
        }

        // 5. OWNER-Schutz
        if (target.getRole() == Role.OWNER && newRole != Role.OWNER) {
            long ownerCount = memberRepository.countByHouseholdIdAndRole(householdId, Role.OWNER);
            if (ownerCount <= 1) {
                throw new NotFoundException("Household must have at least one OWNER");
            }
        }

        // 6. Update
        target.setRole(newRole);
        return memberRepository.save(target);
    }

    @Transactional
    public void removeUserFromHousehold(UUID householdId, UUID userId) {

        boolean exists = memberRepository.existsByHouseholdIdAndUserId(householdId, userId);
        if (!exists) {
            throw new RuntimeException("User is not a member of this household");
        }

        memberRepository.deleteByHouseholdIdAndUserId(householdId, userId);
    }

}
