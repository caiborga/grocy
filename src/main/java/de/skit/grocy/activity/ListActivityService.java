package de.skit.grocy.activity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import de.skit.grocy.lists.live.ListChanged;

import de.skit.grocy.activity.dto.ListActivityEventResponse;
import de.skit.grocy.activity.dto.ListActivityFeedResponse;
import de.skit.grocy.common.exceptions.NotFoundException;
import de.skit.grocy.households.HouseholdEntity;
import de.skit.grocy.households.HouseholdRepository;
import de.skit.grocy.households.member.HouseholdMemberRepository;
import de.skit.grocy.lists.ListEntity;
import de.skit.grocy.common.enums.Permission;
import de.skit.grocy.security.HouseholdAuthorizationService;
import de.skit.grocy.security.UserPrincipal;
import de.skit.grocy.user.UserEntity;
import jakarta.transaction.Transactional;

@Service
public class ListActivityService {

    private static final int FEED_LIMIT = 20;
    private static final int DEFAULT_LOOKBACK_DAYS = 7;

    private final ApplicationEventPublisher publisher;
    private final ListActivityEventRepository eventRepository;
    private final ActivityReadStateRepository readStateRepository;
    private final HouseholdRepository householdRepository;
    private final HouseholdMemberRepository householdMemberRepository;
    private final HouseholdAuthorizationService householdAuth;

    public ListActivityService(
            ApplicationEventPublisher publisher,
            ListActivityEventRepository eventRepository,
            ActivityReadStateRepository readStateRepository,
            HouseholdRepository householdRepository,
            HouseholdMemberRepository householdMemberRepository,
            HouseholdAuthorizationService householdAuth) {
        this.publisher = publisher;
        this.eventRepository = eventRepository;
        this.readStateRepository = readStateRepository;
        this.householdRepository = householdRepository;
        this.householdMemberRepository = householdMemberRepository;
        this.householdAuth = householdAuth;
    }

    @Transactional
    public void record(
            ListEntity list,
            UserEntity actor,
            ListActivityType type,
            UUID itemId,
            String itemTitle,
            Integer metaCount,
            String metaText) {

        ListActivityEventEntity event = new ListActivityEventEntity();
        event.setHousehold(list.getHousehold());
        event.setList(list);
        event.setListTitle(list.getTitle());
        event.setActor(actor);
        event.setActorName(actor.getName());
        event.setType(type);
        event.setItemId(itemId);
        event.setItemTitle(itemTitle);
        event.setMetaCount(metaCount);
        event.setMetaText(metaText);
        eventRepository.save(event);
        publisher.publishEvent(new ListChanged(list.getId()));
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ListActivityFeedResponse getFeed(UUID householdId, UserPrincipal principal, UUID before, boolean older) {
        requireMemberHousehold(householdId, principal.getUser());
        UUID userId = principal.getUser().getId();
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime since = resolveUnreadSince(userId, householdId);
        OffsetDateTime beforeTime = now;
        UUID beforeId = new UUID(-1L, -1L);
        if (before != null) {
            var cursor = eventRepository.findById(before)
                    .filter(e -> e.getHousehold().getId().equals(householdId))
                    .orElseThrow(() -> new NotFoundException("Nicht gefunden"));
            beforeTime = cursor.getCreatedAt();
            beforeId = cursor.getId();
        }
        var all = eventRepository.findFeed(householdId, userId, OffsetDateTime.parse("1970-01-01T00:00:00Z"),
                beforeTime, beforeId, PageRequest.of(0, FEED_LIMIT + 1));
        var visible = all.stream()
                .filter(e -> older || e.getCreatedAt().isAfter(now.minusDays(DEFAULT_LOOKBACK_DAYS)))
                .limit(FEED_LIMIT).toList();
        var events = visible.stream().map(e -> toDto(e, e.getCreatedAt().isAfter(since)
                && !e.getReaders().contains(userId))).toList();
        return new ListActivityFeedResponse(eventRepository.countUnread(householdId, since, userId), events,
                all.size() > visible.size(), visible.isEmpty() ? before : visible.getLast().getId());
    }

    @Transactional
    public void markRead(UUID householdId, UserPrincipal principal, List<UUID> eventIds) {
        requireMemberHousehold(householdId, principal.getUser());
        if (eventIds.size() > FEED_LIMIT) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Zu viele Einträge");
        }
        var events = eventRepository.findAllById(eventIds);
        if (events.stream().anyMatch(e -> !e.getHousehold().getId().equals(householdId))) {
            throw new AccessDeniedException("Nicht erlaubt");
        }
        events.forEach(e -> e.getReaders().add(principal.getUser().getId()));
    }

    public long getUnreadCount(UUID householdId, UserPrincipal principal) {
        requireMemberHousehold(householdId, principal.getUser());
        UUID userId = principal.getUser().getId();
        return eventRepository.countUnread(householdId, resolveUnreadSince(userId, householdId), userId);
    }
    private OffsetDateTime resolveUnreadSince(UUID userId, UUID householdId) {
        return readStateRepository.findByUserIdAndHouseholdId(userId, householdId)
                .map(ActivityReadStateEntity::getLastReadAt)
                .orElseGet(() -> OffsetDateTime.now().minusDays(DEFAULT_LOOKBACK_DAYS));
    }

    private HouseholdEntity requireMemberHousehold(UUID householdId, UserEntity user) {
        householdAuth.require(householdId, user, Permission.ACTIVITY_READ);
        return householdRepository.findById(householdId)
                .orElseThrow(() -> new NotFoundException("Nicht gefunden"));
    }

    private ListActivityEventResponse toDto(ListActivityEventEntity entity, boolean unread) {
        return new ListActivityEventResponse(
                entity.getId(),
                entity.getHousehold().getId(),
                entity.getList().getId(),
                entity.getListTitle(),
                entity.getActor().getId(),
                entity.getActorName(),
                entity.getType(),
                entity.getItemId(),
                entity.getItemTitle(),
                entity.getMetaCount(),
                entity.getMetaText(),
                entity.getCreatedAt(), unread);
    }
}
