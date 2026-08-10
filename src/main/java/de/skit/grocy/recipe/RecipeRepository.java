package de.skit.grocy.recipe;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<RecipeEntity, UUID> {
    List<RecipeEntity> findByHouseholdId(UUID householdId);

    Optional<RecipeEntity> findByIdAndHouseholdId(UUID id, UUID householdId);

    long countByHouseholdId(UUID householdId);
}
