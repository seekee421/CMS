package com.cms.permissions.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.cms.permissions.repository")
@EnableTransactionManagement
public class DatabaseConfig {
    // Database configuration class to enable JPA repositories and transaction management
}