package de.skit.grocy.integration;

import de.skit.grocy.activity.ListActivityEventEntity;
import de.skit.grocy.activity.ListActivityEventRepository;
import de.skit.grocy.activity.ListActivityType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import java.time.OffsetDateTime;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ActivityFeedTest extends IntegrationTestBase {
    @Autowired ListActivityEventRepository events;
    @Autowired de.skit.grocy.lists.ListRepository lists;
    @Autowired de.skit.grocy.households.HouseholdRepository households;
    @Autowired de.skit.grocy.households.member.HouseholdMemberRepository members;

    @Test
    void pagesExcludeOwnChangesAndReadOnlySubmittedEvents() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        String owner = registerAndLogin(email);
        String otherEmail = UUID.randomUUID() + "@example.com";
        registerAndLogin(otherEmail);
        UUID householdId = defaultHouseholdId(owner);
        var household = households.findById(householdId).orElseThrow();
        var membership = new de.skit.grocy.households.member.HouseholdMemberEntity();
        membership.setHousehold(household);
        membership.setUser(userRepository.findByEmail(otherEmail).orElseThrow());
        membership.setRole(de.skit.grocy.common.enums.Role.EDITOR);
        members.saveAndFlush(membership);
        var list = lists.findById(UUID.fromString(createList(owner, householdId.toString(), "Shared"))).orElseThrow();
        for (int i = 0; i < 26; i++) {
            var event = new ListActivityEventEntity();
            event.setHousehold(household);
            event.setList(list);
            event.setListTitle("Shared");
            event.setActor(i == 25 ? userRepository.findByEmail(email).orElseThrow() : membership.getUser());
            event.setActorName("Member");
            event.setType(ListActivityType.ITEM_ADDED);
            events.saveAndFlush(event);
            org.springframework.test.util.ReflectionTestUtils.setField(event, "createdAt", OffsetDateTime.now().minusMinutes(i + 1));
        }
        events.flush();
        String url = "/api/households/" + householdId + "/activity";
        var first = objectMapper.readTree(mockMvc.perform(get(url).header("Authorization", "Bearer " + owner))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(first.get("events").size()).isEqualTo(20);
        assertThat(first.get("unreadCount").asInt()).isEqualTo(25);
        mockMvc.perform(get(url + "/unread").header("Authorization", "Bearer " + owner))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string("25"));
        var ids = new java.util.ArrayList<String>();
        first.get("events").forEach(e -> ids.add(e.get("id").asText()));
        mockMvc.perform(post(url + "/read").header("Authorization", "Bearer " + owner)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk());
        var second = objectMapper.readTree(mockMvc.perform(get(url).param("older", "true")
                .param("before", first.get("nextCursor").asText()).header("Authorization", "Bearer " + owner))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(second.get("events").size()).isEqualTo(5);
        assertThat(second.get("unreadCount").asInt()).isEqualTo(5);
        mockMvc.perform(get(url + "/unread").header("Authorization", "Bearer " + owner))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string("5"));
        assertThat(second.get("hasMore").asBoolean()).isFalse();
        second.get("events").forEach(e -> assertThat(ids).doesNotContain(e.get("id").asText()));
        // Once the recent window is empty, older history must still be reachable.
        events.findAll().stream().filter(e -> e.getHousehold().getId().equals(householdId)).forEach(e ->
                org.springframework.test.util.ReflectionTestUtils.setField(e, "createdAt", OffsetDateTime.now().minusDays(8)));
        events.flush();
        var recent = objectMapper.readTree(mockMvc.perform(get(url).header("Authorization", "Bearer " + owner))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(recent.get("events").size()).isZero();
        assertThat(recent.get("hasMore").asBoolean()).isTrue();
        var older = objectMapper.readTree(mockMvc.perform(get(url).param("older", "true")
                .header("Authorization", "Bearer " + owner))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(older.get("events").size()).isEqualTo(20);
        var foreignHousehold = defaultHouseholdId(registerAndLogin(UUID.randomUUID() + "@example.com"));
        mockMvc.perform(post("/api/households/" + foreignHousehold + "/activity/read")
                .header("Authorization", "Bearer " + owner).contentType(MediaType.APPLICATION_JSON).content("[]"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/households/" + foreignHousehold + "/activity/unread")
                .header("Authorization", "Bearer " + owner)).andExpect(status().isForbidden());
    }
}
