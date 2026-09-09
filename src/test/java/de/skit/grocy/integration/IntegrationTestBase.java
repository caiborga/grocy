package de.skit.grocy.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import java.time.OffsetDateTime;
import java.util.UUID;

import de.skit.grocy.user.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class IntegrationTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @BeforeEach
    void resetRateLimitsNotNeeded() {
        // Rate limiting is disabled in the test profile.
    }

    protected String registerAndLogin(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "displayName": "Test User",
                      "email": "%s",
                      "password": "secret123"
                    }
                """.formatted(email)))
            .andExpect(status().isCreated());

        userRepository.findByEmail(email)
                .ifPresent(user -> {
                    user.setEmailVerified(true);
                    user.setEmailVerifiedAt(OffsetDateTime.now());
                    userRepository.saveAndFlush(user);
                });

        String loginResponse =
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "email": "%s",
                          "password": "secret123"
                        }
                    """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(loginResponse, "$.accessToken");
    }

    protected UUID defaultHouseholdId(String token) throws Exception {
        String response = mockMvc.perform(get("/api/households")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return UUID.fromString(JsonPath.read(response, "$[0].id"));
    }

    protected String createHousehold(String token, String name) throws Exception {
        String response =
            mockMvc.perform(post("/api/households")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        { "name": "%s" }
                    """.formatted(name)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(response, "$.id");
    }

    protected String createList(String token, String householdId, String name) throws Exception {
        String response =
            mockMvc.perform(post("/api/households/{id}/lists", householdId)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        { "title": "%s", "householdId": "%s", "isDefault": false }
                    """.formatted(name, householdId)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(response, "$.id");
    }
}
