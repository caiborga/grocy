package de.skit.grocy.items;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<ItemEntity, UUID> {

    List<ItemEntity> findByListId(UUID listId);

    List<ItemEntity> findByListIdAndNameContainingIgnoreCase(UUID listId, String name);

    Optional<ItemEntity> findByListIdAndId(UUID listId, UUID itemId);

    int deleteByListIdAndId(UUID listId, UUID itemId);
}
