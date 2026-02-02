package de.skit.grocy.households.member;

import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.skit.grocy.common.enums.Role;
import de.skit.grocy.households.member.dto.HouseholdMemberCreate;
import de.skit.grocy.households.member.dto.HouseholdMemberEdit;
import de.skit.grocy.households.member.dto.HouseholdMemberResponse;
import de.skit.grocy.households.member.mapper.HouseholdMemberMapper;

import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/households")
public class HouseholdMemberController {
    private final HouseholdMemberService service;
    private final HouseholdMemberMapper mapper;

    public HouseholdMemberController(HouseholdMemberService service, HouseholdMemberMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping("/{householdId}/members")
    public HouseholdMemberResponse addMember(
            @PathVariable UUID householdId,
            @RequestBody HouseholdMemberCreate body) {

        var member = service.addUserToHousehold(householdId, body.userId(), body.role());
        return mapper.toDto(member);
    }

    @PatchMapping("/{householdId}/members/{userId}")
    public HouseholdMemberResponse editRole(
            @PathVariable UUID householdId,
            @PathVariable UUID userId,
            @RequestBody HouseholdMemberEdit body
    ) {
        var member = service.editUserRole(householdId, userId, body.role(), body.actingUserId());
        return mapper.toDto(member);
    }


    @DeleteMapping("/{householdId}/members/{userId}")
    public void removeMember(
            @PathVariable UUID householdId,
            @PathVariable UUID userId) {
        service.removeUserFromHousehold(householdId, userId);
    }

}
