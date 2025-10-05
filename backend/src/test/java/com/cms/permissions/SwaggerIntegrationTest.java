package com.cms.permissions;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = SwaggerIntegrationTestApplication.class)
@ActiveProfiles("test")
public class SwaggerIntegrationTest {

    @Test
    public void contextLoads() {
        // 测试Spring上下文是否能正常加载，包括Swagger配置
    }
}
