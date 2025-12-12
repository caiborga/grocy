package de.skit.grocy.households.member.mapper;

import org.springframework.stereotype.Component;

import de.skit.grocy.households.member.HouseholdMemberEntity;
import de.skit.grocy.households.member.dto.HouseholdMemberResponse;

@Component
public class HouseholdMemberMapper {

    public HouseholdMemberResponse toDto(HouseholdMemberEntity entity) {
        return new HouseholdMemberResponse(
            entity.getId(),
            entity.getHousehold().getId(),
            entity.getUser().getId(),
            entity.getRole(),
            entity.getJoinedAt()
        );
    }
}

