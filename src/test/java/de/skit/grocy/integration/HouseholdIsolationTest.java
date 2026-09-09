package de.skit.grocy.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class HouseholdIsolationTest extends IntegrationTestBase {

    @Test
    void userCannotReadOrChangeAnotherHousehold() throws Exception {
        String aliceToken = registerAndLogin("alice@example.com");
        String bobToken = registerAndLogin("bob@example.com");

        UUID aliceHousehold = defaultHouseholdId(aliceToken);
        UUID bobHousehold = defaultHouseholdId(bobToken);

        mockMvc.perform(get("/api/households/{id}", aliceHousehold)
                .header("Authorization", "Bearer " + bobToken))
            .andExpect(status().isNotFound());

        mockMvc.perform(patch("/api/households/{id}", aliceHousehold)
                .header("Authorization", "Bearer " + bobToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "name": "Hacked" }
                """))
            .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/households/{id}/recipes", aliceHousehold)
                .header("Authorization", "Bearer " + bobToken))
            .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/households/{id}/recipes", aliceHousehold)
                .header("Authorization", "Bearer " + bobToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "Secret",
                      "baseServings": 2,
                      "ingredients": [],
                      "steps": []
                    }
                """))
            .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/households/{id}/activity", aliceHousehold)
                .header("Authorization", "Bearer " + bobToken))
            .andExpect(status().isNotFound());

        String aliceListId = createList(aliceToken, aliceHousehold.toString(), "Alice Liste");

        mockMvc.perform(get("/api/lists/{id}", aliceListId)
                .header("Authorization", "Bearer " + bobToken))
            .andExpect(status().isNotFound());

        mockMvc.perform(patch("/api/lists/{id}", aliceListId)
                .header("Authorization", "Bearer " + bobToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "title": "Stolen" }
                """))
            .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/lists/{id}/items", aliceListId)
                .header("Authorization", "Bearer " + bobToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "title": "Milk" }
                """))
            .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/lists")
                .header("Authorization", "Bearer " + bobToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(aliceListId)).doesNotExist());

        mockMvc.perform(get("/api/households")
                .header("Authorization", "Bearer " + bobToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(aliceHousehold)).doesNotExist())
            .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(bobHousehold)).exists());
    }

    @Test
    void manipulatedResourceIdsAreRejected() throws Exception {
        String aliceToken = registerAndLogin("alice2@example.com");
        String bobToken = registerAndLogin("bob2@example.com");
        UUID aliceHousehold = defaultHouseholdId(aliceToken);
        String aliceListId = createList(aliceToken, aliceHousehold.toString(), "Liste");

        String createItem = mockMvc.perform(post("/api/lists/{id}/items", aliceListId)
                .header("Authorization", "Bearer " + aliceToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "title": "Butter" }
                """))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String itemId = com.jayway.jsonpath.JsonPath.read(createItem, "$.id");

        mockMvc.perform(patch("/api/lists/{listId}/items/{itemId}", aliceListId, itemId)
                .header("Authorization", "Bearer " + bobToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "title": "Hacked" }
                """))
            .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/lists/{listId}/items/{itemId}", aliceListId, itemId)
                .header("Authorization", "Bearer " + bobToken))
            .andExpect(status().isNotFound());
    }
}
