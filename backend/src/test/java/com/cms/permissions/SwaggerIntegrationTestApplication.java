package com.cms.permissions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

/**
 * Swagger集成测试应用
 * 用于测试Swagger UI和API文档功能，排除数据库配置以避免测试时的依赖问题
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableCaching
@ComponentScan(basePackages = "com.cms.permissions")
public class SwaggerIntegrationTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(SwaggerIntegrationTestApplication.class, args);
    }
}
