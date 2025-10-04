package com.cms.permissions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CMSApplication.class)
@ActiveProfiles("test")
public class SwaggerTest {
    
    @Test
    public void contextLoads() {
        // 测试Spring上下文是否能正常加载
    }
}
