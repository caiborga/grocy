package de.skit.grocy.items;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Sort;

import de.skit.grocy.lists.ListEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemRepository extends JpaRepository<ItemEntity, UUID> {

    List<ItemEntity> findByList(ListEntity list, Sort sort);

    List<ItemEntity> findByListAndChecked(ListEntity list, boolean checked, Sort sort);

    Optional<ItemEntity> findByIdAndList(UUID id, ListEntity list);

    @Modifying
    @Query("delete from ItemEntity i where i.list = :list and i.checked = true")
    int deleteByListAndCheckedTrue(@Param("list") ListEntity list);

    void deleteByIdAndList(UUID id, ListEntity list);

    long countByList(ListEntity list);

    long countByListAndChecked(ListEntity list, boolean checked);
}
