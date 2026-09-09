package de.skit.grocy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.registration.enabled=false")
class RegistrationDisabledTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void registerIsRejectedWhenDisabled() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content("""
                    {
                      "displayName": "No",
                      "email": "closed@example.com",
                      "password": "secret123"
                    }
                """))
            .andExpect(status().isForbidden());
    }
}
