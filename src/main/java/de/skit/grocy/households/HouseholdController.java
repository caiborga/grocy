package de.skit.grocy.households;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import de.skit.grocy.households.dto.HouseholdCreate;
import de.skit.grocy.households.dto.HouseholdDetailResponse;
import de.skit.grocy.households.dto.HouseholdResponse;
import de.skit.grocy.households.dto.HouseholdUpdate;
import de.skit.grocy.security.UserPrincipal;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/households")
class HouseholdController {

    private final HouseholdService service;

    HouseholdController(HouseholdService service) {
        this.service = service;
    }

    @GetMapping()
    List<HouseholdResponse> allUserHouseholds(@AuthenticationPrincipal UserPrincipal principal) {
        return service.getAllUserHouseholds(principal);
    }

    @PostMapping
    public HouseholdResponse create(
            @RequestBody 
            @Valid HouseholdCreate dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        return service.addHousehold(dto, principal);
    }

    @GetMapping("/{id}")
    HouseholdDetailResponse one(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return service.getHousehold(id, principal);
    }

    @PatchMapping("/{id}")
    public HouseholdResponse updateHousehold(@PathVariable UUID id, @RequestBody HouseholdUpdate dto) {
        return service.updateHousehold(id, dto);
    }
}
