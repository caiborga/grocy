package de.skit.grocy.activity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ListActivityEventRepository extends JpaRepository<ListActivityEventEntity, UUID> {

    List<ListActivityEventEntity> findByHouseholdIdOrderByCreatedAtDesc(UUID householdId, Pageable pageable);

    @Query("""
            select count(e) from ListActivityEventEntity e
            where e.household.id = :householdId
              and e.createdAt > :since
              and e.actor.id <> :actorId
            """)
    long countUnread(
            @Param("householdId") UUID householdId,
            @Param("since") OffsetDateTime since,
            @Param("actorId") UUID actorId);
}
