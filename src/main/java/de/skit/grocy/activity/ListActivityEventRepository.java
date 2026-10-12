package de.skit.grocy.activity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ListActivityEventRepository extends JpaRepository<ListActivityEventEntity, UUID> {

    List<ListActivityEventEntity> findByHouseholdIdOrderByCreatedAtDesc(UUID householdId, Pageable pageable);

    @Query("""
            select count(e) from ListActivityEventEntity e
            where e.household.id = :householdId
              and e.createdAt > :since
              and e.actor.id <> :actorId
              and :actorId not member of e.readers
            """)
    long countUnread(
            @Param("householdId") UUID householdId,
            @Param("since") OffsetDateTime since,
            @Param("actorId") UUID actorId);

    @Query("""
            select e from ListActivityEventEntity e
            where e.household.id = :householdId and e.actor.id <> :actorId
              and e.createdAt >= :since
              and (e.createdAt < :beforeTime or (e.createdAt = :beforeTime and e.id < :beforeId))
            order by e.createdAt desc, e.id desc
            """)
    List<ListActivityEventEntity> findFeed(UUID householdId, UUID actorId, OffsetDateTime since,
            OffsetDateTime beforeTime, UUID beforeId, Pageable pageable);

    void deleteByHouseholdId(UUID householdId);

    @Modifying
    @Query("update ListActivityEventEntity e set e.actorName = :name where e.actor.id = :userId")
    int anonymizeActorName(@Param("userId") UUID userId, @Param("name") String name);
}
