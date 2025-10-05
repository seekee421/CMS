package com.cms.permissions;

import com.cms.permissions.config.SwaggerConfig;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = SwaggerConfig.class)
@TestPropertySource(properties = {
    "spring.main.banner-mode=off",
    "logging.level.root=OFF"
})
public class SwaggerConfigTest {

    @Test
    public void testSwaggerConfigLoads() {
        // This test verifies that the Swagger configuration loads successfully
        SwaggerConfig config = new SwaggerConfig();
        OpenAPI openAPI = config.customOpenAPI();
        assertNotNull(openAPI, "OpenAPI configuration should not be null");
        assertNotNull(openAPI.getInfo(), "OpenAPI info should not be null");
        assertNotNull(openAPI.getComponents(), "OpenAPI components should not be null");
    }
}
