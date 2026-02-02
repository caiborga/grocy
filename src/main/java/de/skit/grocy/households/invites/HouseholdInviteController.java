package de.skit.grocy.households.invites;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import de.skit.grocy.households.invites.dto.HouseholdInviteCreateRequest;
import de.skit.grocy.households.invites.dto.HouseholdInviteCreatedResponse;
import de.skit.grocy.households.invites.dto.HouseholdInvitePreviewResponse;
import de.skit.grocy.security.UserPrincipal;

@RestController
@RequestMapping("/api")
public class HouseholdInviteController {

    private final HouseholdInviteService inviteService;

    private final String publicAppBaseUrl;

    public HouseholdInviteController(
            HouseholdInviteService inviteService,
            @Value("${app.public-base-url:http://localhost:5173}") String publicAppBaseUrl
    ) {
        this.inviteService = inviteService;
        this.publicAppBaseUrl = publicAppBaseUrl;
    }

    // ------------------------------------------------------------
    // POST /households/{householdId}/invites  (Invite)
    // ------------------------------------------------------------
    @PostMapping("/households/{householdId}/invites")
    @ResponseStatus(HttpStatus.CREATED)
    public HouseholdInviteCreatedResponse createInvite(
            @PathVariable UUID householdId,
            @RequestBody HouseholdInviteCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UUID currentUserId = principal.getUser().getId();
        return inviteService.createInvite(householdId, request, currentUserId, publicAppBaseUrl);
    }

    // ------------------------------------------------------------
    // GET /invites/{token}  (Preview / Join-Seite)
    // ------------------------------------------------------------
    @GetMapping("/invites/{token}")
    public HouseholdInvitePreviewResponse getInvitePreview(@PathVariable String token) {
        return inviteService.getInvitePreview(token);
    }

    // ------------------------------------------------------------
    // POST /invites/{token}/accept  (Invite annehmen)
    // ------------------------------------------------------------
    @PostMapping("/invites/{token}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void acceptInvite(
            @PathVariable String token,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UUID currentUserId = principal.getUser().getId();
        inviteService.acceptInvite(token, currentUserId);
    }
}
