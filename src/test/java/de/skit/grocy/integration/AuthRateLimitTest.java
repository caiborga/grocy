package de.skit.grocy.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@TestPropertySource(properties = "app.rate-limit.enabled=true")
class AuthRateLimitTest extends IntegrationTestBase {

    @Test
    void nginxForwardedClientsDoNotShareOneLoopbackBucket() throws Exception {
        for (int i = 0; i < 10; i++) {
            loginFrom("127.0.0.1", "203.0.113.20").andExpect(status().isUnauthorized());
        }
        loginFrom("127.0.0.1", "203.0.113.20").andExpect(status().isTooManyRequests());
        loginFrom("127.0.0.1", "203.0.113.21").andExpect(status().isUnauthorized());
    }

    @Test
    void spoofedForwardedHeaderFromUntrustedPeerIsIgnored() throws Exception {
        for (int i = 0; i < 10; i++) {
            loginFrom("198.51.100.10", "203.0.113.1").andExpect(status().isUnauthorized());
        }
        loginFrom("198.51.100.10", "203.0.113.1").andExpect(status().isTooManyRequests());
        loginFrom("198.51.100.11", "203.0.113.1").andExpect(status().isUnauthorized());
    }

    private org.springframework.test.web.servlet.ResultActions loginFrom(String remoteAddr, String forwardedFor)
            throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .with(remoteAddr(remoteAddr))
                .header("X-Forwarded-For", forwardedFor)
                .header("X-Real-IP", forwardedFor)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "email": "rate-limit@example.com", "password": "wrong-password" }
                """));
    }

    private static RequestPostProcessor remoteAddr(String ip) {
        return request -> {
            request.setRemoteAddr(ip);
            return request;
        };
    }
}
