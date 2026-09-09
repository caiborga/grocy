package de.skit.grocy.security;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.skit.grocy.account.AccountService;
import de.skit.grocy.security.dto.AuthMessageResponse;
import de.skit.grocy.security.dto.MeResponse;

@RestController
@RequestMapping("/api")
public class MeController {

    private final MeService service;
    private final AccountService accountService;

    MeController(MeService service, AccountService accountService) {
        this.service = service;
        this.accountService = accountService;
    }

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        return service.getMe(principal);
    }

    @PatchMapping("/me/active-household/{householdId}")
    public void setActiveHousehold(
            @PathVariable UUID householdId,
            @AuthenticationPrincipal UserPrincipal principal) {
        service.setActiveHousehold(householdId, principal);
    }

    @GetMapping("/me/export")
    public ResponseEntity<Map<String, Object>> export(@AuthenticationPrincipal UserPrincipal principal) {
        Map<String, Object> body = accountService.export(principal);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"grocy-export.json\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    @DeleteMapping("/me")
    public AuthMessageResponse deleteAccount(@AuthenticationPrincipal UserPrincipal principal) {
        accountService.deleteAccount(principal);
        return new AuthMessageResponse("Account wurde gelöscht.");
    }
}
