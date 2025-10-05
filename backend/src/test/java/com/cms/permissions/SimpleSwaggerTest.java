package com.cms.permissions;

import com.cms.permissions.config.SwaggerConfig;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SimpleSwaggerTest {

    @Test
    public void testSwaggerConfig() {
        // Test that Swagger configuration can be loaded without errors
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(SwaggerConfig.class);
        context.refresh();

        OpenAPI openAPI = context.getBean(OpenAPI.class);
        assertNotNull(openAPI, "OpenAPI bean should be created");
        assertNotNull(openAPI.getInfo(), "OpenAPI info should not be null");
        assertNotNull(openAPI.getInfo().getTitle(), "OpenAPI title should not be null");

        context.close();
    }
}
