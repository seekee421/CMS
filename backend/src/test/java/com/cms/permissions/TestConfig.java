package com.cms.permissions;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

/**
 * 测试配置类，排除数据库配置以避免测试时的数据库连接问题
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableCaching
@ComponentScan(basePackages = "com.cms.permissions")
public class TestConfig {
    // 测试配置类，用于隔离测试环境
}
