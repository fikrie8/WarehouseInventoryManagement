package com.example.fikrie.smoke;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Notice: NO properties = {...} override to force Spring to load the real application.properties exactly as it runs locally.
@SpringBootTest
class ApplicationStartupTest {

    @Test
    @DisplayName("Smoke Test: Application context should boot cleanly using default application.properties")
    void contextLoadsWithDefaultProperties() {
        // If any ${ENV_VAR:default} placeholder in application.properties has a typo,
        // Spring will fail to boot right here during mvn test.
    }
}
