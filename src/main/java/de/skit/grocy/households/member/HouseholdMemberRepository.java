package de.skit.grocy.households.member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HouseholdMemberRepository extends JpaRepository<HouseholdMemberEntity, UUID> {
    List<HouseholdMemberEntity> findByHouseholdId(UUID householdId);

    List<HouseholdMemberEntity> findByUserId(UUID userId);

    void deleteByHouseholdIdAndUserId(UUID householdId, UUID userId);

    boolean existsByHouseholdIdAndUserId(UUID householdId, UUID userId);
}