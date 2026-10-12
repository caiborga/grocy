package de.skit.grocy.activity;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;

import de.skit.grocy.activity.dto.ListActivityFeedResponse;
import de.skit.grocy.security.UserPrincipal;

@RestController
@RequestMapping("/api/households/{householdId}/activity")
class ListActivityController {

    private final ListActivityService service;

    ListActivityController(ListActivityService service) {
        this.service = service;
    }

    @GetMapping
    ListActivityFeedResponse getFeed(
            @PathVariable UUID householdId,
            @RequestParam(required = false) UUID before,
            @RequestParam(defaultValue = "false") boolean older,
            @AuthenticationPrincipal UserPrincipal principal) {
        return service.getFeed(householdId, principal, before, older);
    }

    @PostMapping("/read")
    void markRead(
            @PathVariable UUID householdId,
            @RequestBody java.util.List<UUID> eventIds,
            @AuthenticationPrincipal UserPrincipal principal) {
        service.markRead(householdId, principal, eventIds);
    }

    @GetMapping("/unread")
    long unreadCount(@PathVariable UUID householdId, @AuthenticationPrincipal UserPrincipal principal) {
        return service.getUnreadCount(householdId, principal);
    }
}
