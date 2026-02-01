package de.skit.grocy.lists.mapper;

import org.springframework.stereotype.Component;

import de.skit.grocy.lists.dto.*;
import de.skit.grocy.lists.ListEntity;

@Component
public class ListMapper {

    public ListEntity toEntity(ListCreate dto) {
        ListEntity entity = new ListEntity();
        entity.setTitle(dto.title());
        return entity;
    }

    public ListResponse toResponse(ListEntity entity, Stats stats) {
        return new ListResponse(
            entity.getId(),
            entity.getTitle(),
            entity.isArchived(),
            stats,
            entity.isDefault()
        );
    }
}
