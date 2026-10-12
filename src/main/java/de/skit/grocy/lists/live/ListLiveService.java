package de.skit.grocy.lists.live;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import de.skit.grocy.common.enums.Permission;
import de.skit.grocy.common.exceptions.NotFoundException;
import de.skit.grocy.lists.ListRepository;
import de.skit.grocy.security.HouseholdAuthorizationService;
import de.skit.grocy.security.JwtService;
import de.skit.grocy.security.UserPrincipal;
import de.skit.grocy.security.session.AuthSessionService;
import org.springframework.security.core.userdetails.UserDetailsService;
import jakarta.annotation.PreDestroy;

@Service
public class ListLiveService {
    private final ListRepository lists;
    private final HouseholdAuthorizationService authorization;
    private final JwtService jwt;
    private final UserDetailsService users;
    private final AuthSessionService sessions;
    private final Map<UUID, Connection> connections = new LinkedHashMap<>();
    private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();

    public ListLiveService(ListRepository lists, HouseholdAuthorizationService authorization,
            JwtService jwt, UserDetailsService users, AuthSessionService sessions) {
        this.lists = lists;
        this.authorization = authorization;
        this.jwt = jwt;
        this.users = users;
        this.sessions = sessions;
        timer.scheduleAtFixedRate(this::heartbeat, 15, 15, TimeUnit.SECONDS);
    }

    public synchronized SseEmitter connect(UUID listId, UserPrincipal principal, String token) {
        UUID householdId = requireRead(listId, principal);
        UUID connectionId = UUID.randomUUID();
        SseEmitter emitter = new SseEmitter(60_000L);
        Connection connection = new Connection(listId, householdId, principal.getUser().getId(),
                principal.getUser().getName(), token, emitter);
        connections.put(connectionId, connection);
        emitter.onCompletion(() -> remove(connectionId));
        emitter.onTimeout(() -> remove(connectionId));
        emitter.onError(error -> remove(connectionId));
        send(connectionId, connection, "connected", connectionId);
        broadcastPresence(listId);
        return emitter;
    }

    private UUID requireRead(UUID listId, UserPrincipal principal) {
        var list = lists.findById(listId).orElseThrow(() -> new NotFoundException("Liste nicht gefunden"));
        authorization.require(list.getHousehold().getId(), principal.getUser(), Permission.LIST_READ);
        return list.getHousehold().getId();
    }

    public synchronized void disconnect(UUID listId, UUID connectionId, UUID userId) {
        Connection connection = connections.get(connectionId);
        if (connection != null && connection.listId.equals(listId) && connection.userId.equals(userId)) {
            remove(connectionId);
            connection.emitter.complete();
        }
    }

    // Check current credentials and permissions, not the snapshot from opening the stream.
    private boolean allowed(Connection connection) {
        try {
            var principal = (UserPrincipal) users.loadUserByUsername(jwt.extractUsername(connection.token));
            if (!principal.isEnabled() || !principal.isAccountNonLocked()
                    || !jwt.isTokenValid(connection.token, principal)) return false;
            String jti = jwt.extractJti(connection.token);
            if (jti != null && !sessions.isActive(jti, connection.userId)) return false;
            authorization.require(connection.householdId, principal.getUser(), Permission.LIST_READ);
            return true;
        } catch (RuntimeException error) {
            return false;
        }
    }

    // Rollbacks must never make other browsers reload an uncommitted list state.
    @TransactionalEventListener
    public synchronized void changed(ListChanged event) {
        broadcast(event.listId(), "list-changed", event.listId());
        // Existing viewers receive the invalidation even if the list was just deleted.
        if (!lists.existsById(event.listId())) {
            for (var entry : Map.copyOf(connections).entrySet()) {
                if (entry.getValue().listId.equals(event.listId())) {
                    disconnect(event.listId(), entry.getKey(), entry.getValue().userId);
                }
            }
        }
    }

    private synchronized void heartbeat() {
        for (var entry : Map.copyOf(connections).entrySet()) {
            var connection = entry.getValue();
            if (!allowed(connection)) {
                connection.emitter.complete();
                remove(entry.getKey());
            } else {
                send(entry.getKey(), connection, "heartbeat", "");
            }
        }
    }

    private synchronized void remove(UUID id) {
        Connection removed = connections.remove(id);
        if (removed != null) broadcastPresence(removed.listId);
    }

    private void broadcastPresence(UUID listId) {
        broadcast(listId, "presence", null);
    }

    private List<PresenceUser> presence(UUID listId) {
        // A person with several tabs appears once. The last closed tab removes them.
        Map<UUID, PresenceUser> present = new LinkedHashMap<>();
        connections.values().stream().filter(c -> c.listId.equals(listId))
                .forEach(c -> present.put(c.userId, new PresenceUser(c.userId, c.name)));
        return present.values().stream().toList();
    }

    private void broadcast(UUID listId, String event, Object data) {
        for (var entry : Map.copyOf(connections).entrySet()) {
            var connection = entry.getValue();
            if (!connection.listId.equals(listId)) continue;
            if (!connections.containsKey(entry.getKey())) continue;
            if (allowed(connection)) send(entry.getKey(), connection, event,
                    event.equals("presence") ? presence(listId) : data);
            else {
                connection.emitter.complete();
                remove(entry.getKey());
            }
        }
    }

    private void send(UUID id, Connection connection, String event, Object data) {
        try {
            connection.emitter.send(SseEmitter.event().name(event).data(data));
        } catch (IOException | IllegalStateException error) {
            connection.emitter.complete();
            remove(id);
        }
    }

    @PreDestroy
    public synchronized void close() {
        timer.shutdownNow();
        var active = Map.copyOf(connections);
        connections.clear();
        active.values().forEach(c -> c.emitter.complete());
    }

    public record PresenceUser(UUID id, String name) {}
    private record Connection(UUID listId, UUID householdId, UUID userId, String name, String token, SseEmitter emitter) {}
}
