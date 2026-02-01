package de.skit.grocy.households.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import de.skit.grocy.households.HouseholdEntity;
import de.skit.grocy.households.dto.HouseholdCreate;
import de.skit.grocy.households.dto.HouseholdDetailResponse;
import de.skit.grocy.households.dto.HouseholdResponse;
import de.skit.grocy.households.dto.HouseholdUpdate;
import de.skit.grocy.households.member.dto.HouseholdMemberResponse;
import de.skit.grocy.lists.dto.ListResponse;

@Component
public class HouseholdMapper {

    public HouseholdResponse toDto(HouseholdEntity entity) {
        return new HouseholdResponse(
                entity.getId(),
                entity.getName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getArchived());
    }

    public HouseholdDetailResponse toDetailDto(
            HouseholdEntity entity,
            List<HouseholdMemberResponse> members,
            List<ListResponse> lists) {
        return new HouseholdDetailResponse(
                entity.getId(),
                entity.getName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getArchived(),
                members,
                lists);
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
