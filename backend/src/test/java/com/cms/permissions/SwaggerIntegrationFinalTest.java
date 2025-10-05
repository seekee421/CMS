package com.cms.permissions;

import com.cms.permissions.config.SwaggerConfig;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = SwaggerConfig.class)
@TestPropertySource(properties = {
    "spring.main.banner-mode=off",
    "logging.level.root=WARN"
})
public class SwaggerIntegrationFinalTest {

    @Test
    public void testSwaggerConfigurationLoadsSuccessfully() {
        // Test that the Swagger configuration loads without errors
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(SwaggerConfig.class);
        context.refresh();

        // Verify that the OpenAPI bean is created
        OpenAPI openAPI = context.getBean(OpenAPI.class);
        assertNotNull(openAPI, "OpenAPI bean should be created successfully");

        // Verify API information
        Info info = openAPI.getInfo();
        assertNotNull(info, "OpenAPI info should not be null");
        assertEquals("CMS 权限管理系统 API", info.getTitle(), "API title should match");
        assertEquals("1.0.0", info.getVersion(), "API version should match");
        assertNotNull(info.getDescription(), "API description should not be null");
        assertNotNull(info.getLicense(), "API license should not be null");

        // Verify security configuration
        assertNotNull(openAPI.getComponents(), "OpenAPI components should not be null");
        assertNotNull(openAPI.getComponents().getSecuritySchemes(), "Security schemes should not be null");
        assertTrue(openAPI.getComponents().getSecuritySchemes().containsKey("bearerAuth"),
                  "BearerAuth security scheme should be configured");

        // Verify security requirements
        assertNotNull(openAPI.getSecurity(), "Security requirements should not be null");
        assertFalse(openAPI.getSecurity().isEmpty(), "Security requirements should not be empty");

        context.close();
    }

    @Test
    public void testSwaggerBeanCreation() {
        // Test that the customOpenAPI bean can be created
        SwaggerConfig swaggerConfig = new SwaggerConfig();
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Verify the bean is properly configured
        assertNotNull(openAPI, "Custom OpenAPI bean should be created");
        assertNotNull(openAPI.getInfo(), "Info section should be present");
        assertNotNull(openAPI.getComponents(), "Components section should be present");
        assertNotNull(openAPI.getSecurity(), "Security section should be present");

        // Verify specific configurations
        assertEquals("CMS 权限管理系统 API", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
        assertTrue(openAPI.getSecurity().stream()
                  .anyMatch(req -> req.containsKey("bearerAuth")),
                  "Should have bearerAuth security requirement");
    }
}
