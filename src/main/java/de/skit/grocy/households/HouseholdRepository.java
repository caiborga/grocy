package de.skit.grocy.households;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HouseholdRepository extends JpaRepository<HouseholdEntity, UUID> {
    List<HouseholdEntity> findAll();
    List<HouseholdEntity> findDistinctByMembersUserId(UUID userId);
}
