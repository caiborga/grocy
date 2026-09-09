package de.skit.grocy.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.jayway.jsonpath.JsonPath;

import de.skit.grocy.common.enums.Role;
import de.skit.grocy.households.invites.HouseholdInviteEntity;
import de.skit.grocy.households.invites.HouseholdInviteRepository;
import de.skit.grocy.households.member.HouseholdMemberRepository;

class HouseholdAuthorizationTest extends IntegrationTestBase {

    @Autowired
    HouseholdInviteRepository inviteRepository;

    @Autowired
    HouseholdMemberRepository memberRepository;

    @Test
    void viewerCannotMutateAndEditorCannotManageMembers() throws Exception {
        String ownerToken = registerAndLogin("owner@example.com");
        String editorUser = "editor@example.com";
        String viewerUser = "viewer@example.com";
        String editorToken = registerAndLogin(editorUser);
        String viewerToken = registerAndLogin(viewerUser);

        UUID householdId = defaultHouseholdId(ownerToken);
        String listId = createList(ownerToken, householdId.toString(), "Einkauf");

        String editorInvite = createInvite(ownerToken, householdId, "EDITOR");
        String viewerInvite = createInvite(ownerToken, householdId, "VIEWER");

        mockMvc.perform(post("/api/invites/{token}/accept", editorInvite)
                .header("Authorization", "Bearer " + editorToken))
            .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/invites/{token}/accept", viewerInvite)
                .header("Authorization", "Bearer " + viewerToken))
            .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/lists/{id}/items", listId)
                .header("Authorization", "Bearer " + editorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "title": "Brot" }
                """))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/lists/{id}/items", listId)
                .header("Authorization", "Bearer " + viewerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "title": "Käse" }
                """))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/lists/{id}", listId)
                .header("Authorization", "Bearer " + viewerToken))
            .andExpect(status().isOk());

        UUID editorId = userRepository.findByEmail(editorUser).orElseThrow().getId();
        mockMvc.perform(patch("/api/households/{id}/members/{userId}", householdId, editorId)
                .header("Authorization", "Bearer " + editorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "role": "OWNER" }
                """))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/households/{id}/invites", householdId)
                .header("Authorization", "Bearer " + editorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "role": "VIEWER" }
                """))
            .andExpect(status().isForbidden());
    }

    @Test
    void expiredAndUsedInvitesAreRejected() throws Exception {
        String ownerToken = registerAndLogin("invite-owner@example.com");
        String guestToken = registerAndLogin("invite-guest@example.com");
        UUID householdId = defaultHouseholdId(ownerToken);

        String token = createInvite(ownerToken, householdId, "EDITOR");
        HouseholdInviteEntity invite = inviteRepository.findAll().stream()
                .filter(entity -> householdId.equals(entity.getHouseholdId()))
                .findFirst()
                .orElseThrow();
        invite.setExpiresAt(OffsetDateTime.now().minusHours(1));
        inviteRepository.saveAndFlush(invite);

        mockMvc.perform(get("/api/invites/{token}", token)
                .header("Authorization", "Bearer " + guestToken))
            .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/invites/{token}/accept", token)
                .header("Authorization", "Bearer " + guestToken))
            .andExpect(status().isGone());

        String fresh = createInvite(ownerToken, householdId, "VIEWER");
        mockMvc.perform(post("/api/invites/{token}/accept", fresh)
                .header("Authorization", "Bearer " + guestToken))
            .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/invites/{token}/accept", fresh)
                .header("Authorization", "Bearer " + registerAndLogin("third@example.com")))
            .andExpect(status().isConflict());
    }

    @Test
    void actingUserIdInBodyCannotEscalateRole() throws Exception {
        String ownerToken = registerAndLogin("owner-spoof@example.com");
        String attackerToken = registerAndLogin("attacker@example.com");
        UUID householdId = defaultHouseholdId(ownerToken);
        UUID ownerId = userRepository.findByEmail("owner-spoof@example.com").orElseThrow().getId();
        UUID attackerId = userRepository.findByEmail("attacker@example.com").orElseThrow().getId();

        mockMvc.perform(patch("/api/households/{id}/members/{userId}", householdId, ownerId)
                .header("Authorization", "Bearer " + attackerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "actingUserId": "%s", "role": "VIEWER" }
                """.formatted(ownerId)))
            .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/households/{id}/members", householdId)
                .header("Authorization", "Bearer " + attackerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "userId": "%s", "role": "OWNER" }
                """.formatted(attackerId)))
            .andExpect(status().isNotFound());
    }

    private String createInvite(String ownerToken, UUID householdId, String role) throws Exception {
        String response = mockMvc.perform(post("/api/households/{id}/invites", householdId)
                .header("Authorization", "Bearer " + ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "role": "%s" }
                """.formatted(role)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return JsonPath.read(response, "$.token");
    }
}
