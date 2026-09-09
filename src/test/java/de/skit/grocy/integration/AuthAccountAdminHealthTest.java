package de.skit.grocy.integration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class AuthAccountAdminHealthTest extends IntegrationTestBase {

    @Test
    void loginAndResetDoNotRevealAccounts() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "email": "missing@example.com", "password": "secret123" }
                """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.accessToken").doesNotExist());

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "displayName": "Same",
                      "email": "dup@example.com",
                      "password": "secret123"
                    }
                """))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "displayName": "Same",
                      "email": "dup@example.com",
                      "password": "secret123"
                    }
                """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.message").value(containsString("Falls die Adresse")));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "email": "dup@example.com", "password": "wrong-password" }
                """))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "email": "nobody@example.com" }
                """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value(containsString("Falls der Account existiert")));

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "token": "invalid-token", "password": "newsecret1" }
                """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Token ist ungültig oder abgelaufen"));
    }

    @Test
    void accountDeletionRequiresOwnershipTransferThenSucceeds() throws Exception {
        String ownerToken = registerAndLogin("delete-owner@example.com");
        String memberToken = registerAndLogin("delete-member@example.com");
        UUID householdId = defaultHouseholdId(ownerToken);

        String invite = mockMvc.perform(post("/api/households/{id}/invites", householdId)
                .header("Authorization", "Bearer " + ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "role": "EDITOR" }
                """))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        String inviteToken = com.jayway.jsonpath.JsonPath.read(invite, "$.token");

        mockMvc.perform(post("/api/invites/{token}/accept", inviteToken)
                .header("Authorization", "Bearer " + memberToken))
            .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/me")
                .header("Authorization", "Bearer " + ownerToken))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("ownership_transfer_required"));

        UUID memberId = userRepository.findByEmail("delete-member@example.com").orElseThrow().getId();
        mockMvc.perform(post("/api/households/{id}/transfer-ownership", householdId)
                .header("Authorization", "Bearer " + ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "userId": "%s" }
                """.formatted(memberId)))
            .andExpect(status().isOk());

        mockMvc.perform(delete("/api/me")
                .header("Authorization", "Bearer " + ownerToken))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/households/{id}", householdId)
                .header("Authorization", "Bearer " + memberToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").exists());
    }

    @Test
    void adminStatsAreProtectedAndAggregated() throws Exception {
        String userToken = registerAndLogin("plain@example.com");
        mockMvc.perform(get("/api/admin/stats")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isForbidden());

        String adminToken = registerAndLogin("admin@grocy.test");
        mockMvc.perform(get("/api/admin/stats")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.usersTotal").isNumber())
            .andExpect(jsonPath("$.householdsTotal").isNumber())
            .andExpect(jsonPath("$.activeUserDefinition").exists())
            .andExpect(content().string(not(containsString("plain@example.com"))))
            .andExpect(content().string(not(containsString("Milk"))));
    }

    @Test
    void healthAndPublicConfigAndUsersApi() throws Exception {
        mockMvc.perform(get("/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ok"))
            .andExpect(content().string(not(containsString("exception"))))
            .andExpect(content().string(not(containsString("version"))));

        mockMvc.perform(get("/api/public/config"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.registrationEnabled").value(true));

        mockMvc.perform(get("/api/users"))
            .andExpect(status().isUnauthorized());

        String token = registerAndLogin("users-api@example.com");
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("no-store")));

        mockMvc.perform(get("/swagger-ui.html"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void exportContainsOwnAccount() throws Exception {
        String token = registerAndLogin("export@example.com");
        mockMvc.perform(get("/api/me/export")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.account.email").value("export@example.com"))
            .andExpect(jsonPath("$.households").isArray());
    }
}
