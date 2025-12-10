package de.skit.grocy.households;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import de.skit.grocy.households.dto.HouseholdCreate;
import de.skit.grocy.households.dto.HouseholdResponse;
import de.skit.grocy.households.dto.HouseholdUpdate;

@RestController
class HouseholdController {

    private final HouseholdService service;

    HouseholdController(HouseholdService service) {
        this.service = service;
    }

    @GetMapping("/households")
    List<HouseholdResponse> all() {
        return service.getAllHouseholds();
    }

    @PostMapping("/households")
    public HouseholdResponse create(@RequestBody HouseholdCreate dto) {
        return service.addHousehold(dto);
    }

    @GetMapping("/households/{id}")
    HouseholdResponse one(@PathVariable UUID id) {
        return service.getHousehold(id);
    }

    @PatchMapping("/households/{id}")
    public HouseholdResponse updateHousehold(@PathVariable UUID id, @RequestBody HouseholdUpdate dto) {
         return service.updateHousehold(id, dto);
    }
}
