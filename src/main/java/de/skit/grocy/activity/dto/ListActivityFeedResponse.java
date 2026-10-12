package de.skit.grocy.activity.dto;

import java.util.List;

public record ListActivityFeedResponse(
        long unreadCount,
        List<ListActivityEventResponse> events,
        boolean hasMore,
        java.util.UUID nextCursor) {
}
