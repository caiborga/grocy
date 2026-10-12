package de.skit.grocy.lists.live;

import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import de.skit.grocy.security.UserPrincipal;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/lists/{listId}/events")
public class ListLiveController {
    private final ListLiveService live;

    public ListLiveController(ListLiveService live) { this.live = live; }

    @DeleteMapping("/{connectionId}")
    public void disconnect(@PathVariable UUID listId, @PathVariable UUID connectionId,
            @AuthenticationPrincipal UserPrincipal principal) {
        live.disconnect(listId, connectionId, principal.getUser().getId());
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter events(@PathVariable UUID listId,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestHeader("Authorization") String authorization, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Accel-Buffering", "no");
        return live.connect(listId, principal, authorization.substring(7));
    }
}
