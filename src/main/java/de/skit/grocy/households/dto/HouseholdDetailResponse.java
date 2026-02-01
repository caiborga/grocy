package de.skit.grocy.households.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import de.skit.grocy.households.member.dto.HouseholdMemberResponse;
import de.skit.grocy.lists.dto.ListResponse;

public record HouseholdDetailResponse(
        UUID id,
        String name,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        boolean archived,
        List<HouseholdMemberResponse> members,
        List<ListResponse> lists) {
}
