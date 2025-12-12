package de.skit.grocy.items;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemRepository extends JpaRepository<ItemEntity, UUID> {

    List<ItemEntity> findByListId(UUID listId);

    List<ItemEntity> findByListIdAndChecked(UUID listId, boolean checked);

    Optional<ItemEntity> findByListIdAndId(UUID listId, UUID id);

    int countByListId(UUID listId);

    int countByListIdAndChecked(UUID listId, boolean checked);

    @Modifying
    @Transactional
    int deleteByListIdAndId(UUID listId, UUID id);

    // GET ITEMS
    @Query(value = "SELECT * FROM items WHERE list_id = :listId", nativeQuery = true)
    List<ItemEntity> getAll(@Param("listId") UUID listId);

    @Query(value = "SELECT * FROM items WHERE list_id = :listId AND checked = false", nativeQuery = true)
    List<ItemEntity> getAllUnchecked(@Param("listId") UUID listId);

    @Query(value = "SELECT * FROM items WHERE list_id = :listId AND checked = true", nativeQuery = true)
    List<ItemEntity> getAllChecked(@Param("listId") UUID listId);
}
