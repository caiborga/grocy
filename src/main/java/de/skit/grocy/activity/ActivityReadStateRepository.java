package de.skit.grocy.activity;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityReadStateRepository extends JpaRepository<ActivityReadStateEntity, UUID> {

    Optional<ActivityReadStateEntity> findByUserIdAndHouseholdId(UUID userId, UUID householdId);
}
