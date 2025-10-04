package com.cms.permissions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.cms.permissions")
public class SwaggerTest {
    public static void main(String[] args) {
        SpringApplication.run(SwaggerTest.class, args);
    }
}
