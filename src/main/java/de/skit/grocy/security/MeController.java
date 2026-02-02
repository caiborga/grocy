package de.skit.grocy.security;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.skit.grocy.security.dto.MeResponse;


@RestController
@RequestMapping("/api")
public class MeController {

    private final MeService service;

    MeController(MeService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public MeResponse me(
            @AuthenticationPrincipal UserPrincipal principal) {
        return service.getMe(principal);

    }

    @PatchMapping("/me/active-household/{householdId}")
    public void setActiveHousehold(
            @PathVariable UUID householdId,
            @AuthenticationPrincipal UserPrincipal principal) {
        service.setActiveHousehold(householdId, principal);
    }
}
