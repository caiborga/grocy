package de.skit.grocy.households.mapper;

import org.springframework.stereotype.Component;

import de.skit.grocy.households.HouseholdEntity;
import de.skit.grocy.households.dto.HouseholdCreate;
import de.skit.grocy.households.dto.HouseholdResponse;
import de.skit.grocy.households.dto.HouseholdUpdate;

@Component
public class HouseholdMapper {

    public HouseholdResponse toDto(HouseholdEntity entity) {
        return new HouseholdResponse(
                entity.getId(),
                entity.getName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getArchived()
            );
    }

    public HouseholdEntity toEntity(HouseholdCreate dto) {

        HouseholdEntity entity = new HouseholdEntity();
        entity.setName(dto.name());
        return entity;
    }

    public void applyPatch(HouseholdUpdate patch, HouseholdEntity entity) {
        if (patch.name() != null) {
            entity.setName(patch.name());
            entity.setArchived(patch.archived());
        }
    }
}
