package com.cms.permissions.service;

import io.minio.MinioClient;
import io.minio.BucketExistsArgs;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MinIO连接测试
 * 验证MinIO服务连接和基本功能
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "minio.endpoint=http://localhost:9000",
    "minio.access-key=minioadmin",
    "minio.secret-key=minioadmin123",
    "minio.bucket-name=cms-test-files",
    "minio.secure=false"
})
public class MinIOConnectionTest {

    @Autowired
    private MinioClient minioClient;

    @Test
    void testMinIOConnection() throws Exception {
        // 测试MinIO连接
        assertNotNull(minioClient);
        
        // 测试bucket存在性检查（这个操作不需要bucket实际存在）
        boolean bucketExists = minioClient.bucketExists(
            BucketExistsArgs.builder()
                .bucket("test-bucket")
                .build()
        );
        
        // 这个测试主要验证连接是否正常，不关心bucket是否存在
        System.out.println("MinIO连接测试成功，bucket存在性检查完成: " + bucketExists);
    }
}