package de.skit.grocy.households.member;

import java.util.UUID;

import org.springframework.stereotype.Service;

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

    public HouseholdMemberEntity addUserToHousehold(UUID householdId, UUID userId, String role) {

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

    @Transactional
    public void removeUserFromHousehold(UUID householdId, UUID userId) {

        boolean exists = memberRepository.existsByHouseholdIdAndUserId(householdId, userId);
        if (!exists) {
            throw new RuntimeException("User is not a member of this household");
        }

        memberRepository.deleteByHouseholdIdAndUserId(householdId, userId);
    }

}
