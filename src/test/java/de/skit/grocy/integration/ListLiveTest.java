package de.skit.grocy.integration;

import de.skit.grocy.lists.live.ListChanged;
import de.skit.grocy.lists.live.ListLiveService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ListLiveTest extends IntegrationTestBase {
    @Autowired ApplicationEventPublisher publisher;
    @Autowired PlatformTransactionManager transactions;
    @Autowired ListLiveService live;
    @Autowired de.skit.grocy.security.JwtService jwt;
    @Autowired de.skit.grocy.security.session.AuthSessionService sessions;
    @Autowired de.skit.grocy.households.HouseholdRepository households;
    @Autowired de.skit.grocy.households.member.HouseholdMemberRepository members;

    private String content(MvcResult stream) {
        return new String(stream.getResponse().getContentAsByteArray(), java.nio.charset.StandardCharsets.UTF_8);
    }

    private UUID connectionId(MvcResult stream) {
        var match = java.util.regex.Pattern.compile("event:connected\\ndata:([^\\n]+)").matcher(content(stream));
        assertThat(match.find()).isTrue();
        return UUID.fromString(match.group(1).replace("\"", ""));
    }

    private MvcResult connect(String listId, String token) throws Exception {
        return mockMvc.perform(get("/api/lists/{id}/events", listId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(request().asyncStarted()).andReturn();
    }

    @Test
    void householdMembersSeeEachOtherAndRevokedSessionsStopReceivingChanges() throws Exception {
        String ownerEmail = UUID.randomUUID() + "@example.com";
        String memberEmail = UUID.randomUUID() + "@example.com";
        String owner = registerAndLogin(ownerEmail);
        String member = registerAndLogin(memberEmail);
        UUID householdId = defaultHouseholdId(owner);
        var membership = new de.skit.grocy.households.member.HouseholdMemberEntity();
        membership.setHousehold(households.findById(householdId).orElseThrow());
        membership.setUser(userRepository.findByEmail(memberEmail).orElseThrow());
        membership.setRole(de.skit.grocy.common.enums.Role.EDITOR);
        members.saveAndFlush(membership);
        String listId = createList(owner, householdId.toString(), "Shared list");
        var ownerStream = connect(listId, owner);
        var memberStream = connect(listId, member);
        UUID ownerId = userRepository.findByEmail(ownerEmail).orElseThrow().getId();
        UUID memberId = membership.getUser().getId();
        try {
            assertThat(content(ownerStream).substring(content(ownerStream).lastIndexOf("event:presence")))
                    .contains(ownerId.toString(), memberId.toString());
            var transaction = new TransactionTemplate(transactions);
            transaction.executeWithoutResult(status -> publisher.publishEvent(new ListChanged(UUID.fromString(listId))));
            assertThat(content(memberStream)).contains("event:list-changed");
            sessions.revokeByJti(jwt.extractJti(member));
            String beforeRevocation = content(memberStream);
            transaction.executeWithoutResult(status -> publisher.publishEvent(new ListChanged(UUID.fromString(listId))));
            assertThat(content(memberStream)).isEqualTo(beforeRevocation);
            assertThat(content(ownerStream).substring(content(ownerStream).lastIndexOf("event:presence")))
                    .contains(ownerId.toString()).doesNotContain(memberId.toString());
        } finally {
            live.disconnect(UUID.fromString(listId), connectionId(ownerStream), ownerId);
            live.disconnect(UUID.fromString(listId), connectionId(memberStream), memberId);
        }
    }

    @Test
    void multipleTabsCountOnceAndOnlyTheOwnerCanDisconnectThem() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        String owner = registerAndLogin(email);
        String outsider = registerAndLogin(UUID.randomUUID() + "@example.com");
        String listId = createList(owner, defaultHouseholdId(owner).toString(), "Presence list");
        var first = connect(listId, owner);
        var second = connect(listId, owner);
        UUID userId = userRepository.findByEmail(email).orElseThrow().getId();
        try {
            String lastPresence = content(first).substring(content(first).lastIndexOf("event:presence"));
            assertThat(lastPresence.split(userId.toString(), -1)).hasSize(2);
            mockMvc.perform(delete("/api/lists/{id}/events/{connection}", listId, connectionId(second))
                    .header("Authorization", "Bearer " + outsider)).andExpect(status().isOk());
            assertThat(second.getRequest().isAsyncStarted()).isTrue();
            mockMvc.perform(delete("/api/lists/{id}/events/{connection}", listId, connectionId(second))
                    .header("Authorization", "Bearer " + owner)).andExpect(status().isOk());
            assertThat(content(first).substring(content(first).lastIndexOf("event:presence")))
                    .contains(userId.toString());
            mockMvc.perform(post("/api/lists/{id}/items", listId)
                    .header("Authorization", "Bearer " + owner)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"Milk\"}")).andExpect(status().isOk());
            assertThat(content(first)).contains("event:list-changed");
        } finally {
            live.disconnect(UUID.fromString(listId), connectionId(first), userId);
            live.disconnect(UUID.fromString(listId), connectionId(second), userId);
        }
    }

    @Test
    void streamRequiresLoginAndListMembership() throws Exception {
        String owner = registerAndLogin(UUID.randomUUID() + "@example.com");
        String outsider = registerAndLogin(UUID.randomUUID() + "@example.com");
        String listId = createList(owner, defaultHouseholdId(owner).toString(), "Private list");

        mockMvc.perform(get("/api/lists/{id}/events", listId))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/lists/{id}/events", listId)
                .header("Authorization", "Bearer " + outsider))
                .andExpect(status().isNotFound());
    }

    @Test
    void changesAreDeliveredOnlyAfterCommitAndOnlyToTheSubscribedList() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        String owner = registerAndLogin(email);
        String household = defaultHouseholdId(owner).toString();
        String listId = createList(owner, household, "Live list");
        String otherId = createList(owner, household, "Other list");
        MvcResult stream = mockMvc.perform(get("/api/lists/{id}/events", listId)
                .header("Authorization", "Bearer " + owner))
                .andExpect(status().isOk()).andExpect(request().asyncStarted())
                .andExpect(header().string("X-Accel-Buffering", "no"))
                .andReturn();
        try {
            assertThat(content(stream)).contains("event:connected", "event:presence");
            TransactionTemplate transaction = new TransactionTemplate(transactions);
            transaction.executeWithoutResult(status -> {
                publisher.publishEvent(new ListChanged(UUID.fromString(listId)));
                assertThat(content(stream)).doesNotContain("event:list-changed");
                status.setRollbackOnly();
            });
            assertThat(content(stream)).doesNotContain("event:list-changed");
            transaction.executeWithoutResult(status -> publisher.publishEvent(new ListChanged(UUID.fromString(otherId))));
            assertThat(content(stream)).doesNotContain("event:list-changed");
            transaction.executeWithoutResult(status -> publisher.publishEvent(new ListChanged(UUID.fromString(listId))));
            assertThat(content(stream)).contains("event:list-changed");
        } finally {
            // Close the mock HTTP response without shutting down the shared application service.
            var user = userRepository.findByEmail(email).orElseThrow();
            live.disconnect(UUID.fromString(listId), connectionId(stream), user.getId());
        }
    }
}
