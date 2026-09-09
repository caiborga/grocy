package de.skit.grocy.households.member;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.skit.grocy.households.member.dto.HouseholdMemberCreate;
import de.skit.grocy.households.member.dto.HouseholdMemberEdit;
import de.skit.grocy.households.member.dto.HouseholdMemberResponse;
import de.skit.grocy.households.member.dto.HouseholdOwnershipTransferRequest;
import de.skit.grocy.households.member.mapper.HouseholdMemberMapper;
import de.skit.grocy.security.UserPrincipal;

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
            @RequestBody HouseholdMemberCreate body,
            @AuthenticationPrincipal UserPrincipal principal) {

        var member = service.addUserToHousehold(householdId, body.userId(), body.role(), principal);
        return mapper.toDto(member);
    }

    @PatchMapping("/{householdId}/members/{userId}")
    public HouseholdMemberResponse editRole(
            @PathVariable UUID householdId,
            @PathVariable UUID userId,
            @RequestBody HouseholdMemberEdit body,
            @AuthenticationPrincipal UserPrincipal principal) {
        var member = service.editUserRole(householdId, userId, body.role(), principal);
        return mapper.toDto(member);
    }

    @DeleteMapping("/{householdId}/members/{userId}")
    public void removeMember(
            @PathVariable UUID householdId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserPrincipal principal) {
        service.removeUserFromHousehold(householdId, userId, principal);
    }

    @PostMapping("/{householdId}/transfer-ownership")
    public HouseholdMemberResponse transferOwnership(
            @PathVariable UUID householdId,
            @RequestBody HouseholdOwnershipTransferRequest body,
            @AuthenticationPrincipal UserPrincipal principal) {
        var member = service.transferOwnership(householdId, body.userId(), principal);
        return mapper.toDto(member);
    }
}
