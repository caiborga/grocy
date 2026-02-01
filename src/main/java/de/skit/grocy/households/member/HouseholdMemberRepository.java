package de.skit.grocy.households.member;

import org.springframework.data.jpa.repository.JpaRepository;

import de.skit.grocy.common.enums.Role;
import de.skit.grocy.households.HouseholdEntity;
import de.skit.grocy.user.UserEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HouseholdMemberRepository extends JpaRepository<HouseholdMemberEntity, UUID> {
    List<HouseholdMemberEntity> findByHouseholdId(UUID householdId);

    List<HouseholdMemberEntity> findByUserId(UUID userId);

    void deleteByHouseholdIdAndUserId(UUID householdId, UUID userId);

    boolean existsByHouseholdIdAndUserId(UUID householdId, UUID userId);

    boolean existsByUserAndHousehold(
            UserEntity user,
            HouseholdEntity household);

    Optional<HouseholdMemberEntity> findByHouseholdIdAndUserId(UUID householdId, UUID userId);

    long countByHouseholdIdAndRole(UUID householdId, Role role);
}