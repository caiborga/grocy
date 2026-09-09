package de.skit.grocy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class GrocyApplicationTests {

    @Autowired
    Environment environment;

    @Test
    void contextLoads() {
        assertThat(environment.getProperty("app.rate-limit.enabled")).isEqualTo("false");
        assertThat(environment.getProperty("app.registration.enabled")).isEqualTo("true");
        assertThat(environment.getProperty("spring.jpa.show-sql")).isEqualTo("false");
    }
}
