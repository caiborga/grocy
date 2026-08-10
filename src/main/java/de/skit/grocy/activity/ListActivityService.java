package de.skit.grocy.activity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import de.skit.grocy.activity.dto.ListActivityEventResponse;
import de.skit.grocy.activity.dto.ListActivityFeedResponse;
import de.skit.grocy.common.exceptions.NotFoundException;
import de.skit.grocy.households.HouseholdEntity;
import de.skit.grocy.households.HouseholdRepository;
import de.skit.grocy.households.member.HouseholdMemberRepository;
import de.skit.grocy.lists.ListEntity;
import de.skit.grocy.security.UserPrincipal;
import de.skit.grocy.user.UserEntity;
import jakarta.transaction.Transactional;

@Service
public class ListActivityService {

    private static final int FEED_LIMIT = 50;
    private static final int DEFAULT_LOOKBACK_DAYS = 7;

    private final ListActivityEventRepository eventRepository;
    private final ActivityReadStateRepository readStateRepository;
    private final HouseholdRepository householdRepository;
    private final HouseholdMemberRepository householdMemberRepository;

    public ListActivityService(
            ListActivityEventRepository eventRepository,
            ActivityReadStateRepository readStateRepository,
            HouseholdRepository householdRepository,
            HouseholdMemberRepository householdMemberRepository) {
        this.eventRepository = eventRepository;
        this.readStateRepository = readStateRepository;
        this.householdRepository = householdRepository;
        this.householdMemberRepository = householdMemberRepository;
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
    }

    public ListActivityFeedResponse getFeed(UUID householdId, UserPrincipal principal) {
        HouseholdEntity household = requireMemberHousehold(householdId, principal.getUser());
        UserEntity user = principal.getUser();

        OffsetDateTime since = resolveUnreadSince(user.getId(), householdId);

        long unreadCount = eventRepository.countUnread(householdId, since, user.getId());

        List<ListActivityEventResponse> events = eventRepository
                .findByHouseholdIdOrderByCreatedAtDesc(householdId, PageRequest.of(0, FEED_LIMIT))
                .stream()
                .map(this::toDto)
                .toList();

        return new ListActivityFeedResponse(unreadCount, events);
    }

    @Transactional
    public void markRead(UUID householdId, UserPrincipal principal) {
        HouseholdEntity household = requireMemberHousehold(householdId, principal.getUser());
        UserEntity user = principal.getUser();

        ActivityReadStateEntity state = readStateRepository
                .findByUserIdAndHouseholdId(user.getId(), householdId)
                .orElseGet(() -> {
                    ActivityReadStateEntity created = new ActivityReadStateEntity();
                    created.setUser(user);
                    created.setHousehold(household);
                    return created;
                });

        state.setLastReadAt(OffsetDateTime.now());
        readStateRepository.save(state);
    }

    private OffsetDateTime resolveUnreadSince(UUID userId, UUID householdId) {
        return readStateRepository.findByUserIdAndHouseholdId(userId, householdId)
                .map(ActivityReadStateEntity::getLastReadAt)
                .orElseGet(() -> OffsetDateTime.now().minusDays(DEFAULT_LOOKBACK_DAYS));
    }

    private HouseholdEntity requireMemberHousehold(UUID householdId, UserEntity user) {
        HouseholdEntity household = householdRepository.findById(householdId)
                .orElseThrow(() -> new NotFoundException("Household not found"));

        if (!householdMemberRepository.existsByUserAndHousehold(user, household)) {
            throw new AccessDeniedException("User is not a member of this household");
        }

        return household;
    }

    private ListActivityEventResponse toDto(ListActivityEventEntity entity) {
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
                entity.getCreatedAt());
    }
}
