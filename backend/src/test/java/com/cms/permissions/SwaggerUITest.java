package com.cms.permissions;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class SwaggerUITest {

    @Test
    public void contextLoads() {
        // This test verifies that the Spring context loads successfully
        // which includes all the Swagger configuration
    }
}
