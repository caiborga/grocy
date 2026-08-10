package de.skit.grocy.lists;

import org.springframework.data.jpa.repository.JpaRepository;

import de.skit.grocy.households.HouseholdEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ListRepository extends JpaRepository<ListEntity, UUID> {

    List<ListEntity> findByHousehold(HouseholdEntity household);

    List<ListEntity> findByHouseholdId(UUID householdId);

    Optional<ListEntity> findByHouseholdAndIsDefaultTrue(HouseholdEntity household);

    Optional<ListEntity> findByHouseholdIdAndIsDefaultTrue(UUID householdId);

    long countByHouseholdIdAndArchivedFalse(UUID householdId);
}

