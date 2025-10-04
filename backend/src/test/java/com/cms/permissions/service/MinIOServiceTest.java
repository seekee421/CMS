package com.cms.permissions.service;

import io.minio.MinioClient;
import io.minio.StatObjectResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MinIO服务测试类
 * 测试文件上传、下载、删除等功能
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
public class MinIOServiceTest {

    @Autowired
    private MinIOService minIOService;

    @Autowired
    private MinioClient minioClient;

    private MockMultipartFile testFile;
    private String testObjectName;

    @BeforeEach
    void setUp() {
        // 创建测试文件
        String testContent = "这是一个测试文件内容，用于验证MinIO存储功能。";
        testFile = new MockMultipartFile(
            "test-file.txt",
            "test-file.txt",
            "text/plain",
            testContent.getBytes()
        );
        
        testObjectName = "test/" + System.currentTimeMillis() + "_test-file.txt";
    }

    @Test
    void testUploadFile() throws Exception {
        // 测试文件上传
        String fileUrl = minIOService.uploadFile(testFile, testObjectName);
        
        assertNotNull(fileUrl);
        assertTrue(fileUrl.contains(testObjectName));
        
        // 验证文件是否存在
        assertTrue(minIOService.fileExists(testObjectName));
        
        // 清理测试文件
        minIOService.deleteFile(testObjectName);
    }

    @Test
    void testUploadFileStream() throws Exception {
        // 测试文件流上传
        String testContent = "测试文件流上传功能";
        InputStream inputStream = new ByteArrayInputStream(testContent.getBytes());
        
        String fileUrl = minIOService.uploadFile(
            inputStream, 
            testObjectName, 
            "text/plain", 
            testContent.getBytes().length
        );
        
        assertNotNull(fileUrl);
        assertTrue(fileUrl.contains(testObjectName));
        
        // 验证文件是否存在
        assertTrue(minIOService.fileExists(testObjectName));
        
        // 清理测试文件
        minIOService.deleteFile(testObjectName);
    }

    @Test
    void testDownloadFile() throws Exception {
        // 先上传文件
        String testContent = "测试文件下载功能";
        InputStream uploadStream = new ByteArrayInputStream(testContent.getBytes());
        minIOService.uploadFile(uploadStream, testObjectName, "text/plain", testContent.getBytes().length);
        
        // 测试文件下载
        InputStream downloadStream = minIOService.downloadFile(testObjectName);
        assertNotNull(downloadStream);
        
        // 读取下载的内容
        byte[] downloadedBytes = downloadStream.readAllBytes();
        String downloadedContent = new String(downloadedBytes);
        assertEquals(testContent, downloadedContent);
        
        downloadStream.close();
        
        // 清理测试文件
        minIOService.deleteFile(testObjectName);
    }

    @Test
    void testDeleteFile() throws Exception {
        // 先上传文件
        minIOService.uploadFile(testFile, testObjectName);
        assertTrue(minIOService.fileExists(testObjectName));
        
        // 测试文件删除
        minIOService.deleteFile(testObjectName);
        assertFalse(minIOService.fileExists(testObjectName));
    }

    @Test
    void testFileExists() throws Exception {
        // 测试不存在的文件
        assertFalse(minIOService.fileExists("non-existent-file.txt"));
        
        // 上传文件后测试存在性
        minIOService.uploadFile(testFile, testObjectName);
        assertTrue(minIOService.fileExists(testObjectName));
        
        // 清理测试文件
        minIOService.deleteFile(testObjectName);
        assertFalse(minIOService.fileExists(testObjectName));
    }

    @Test
    void testGetFileInfo() throws Exception {
        // 上传文件
        minIOService.uploadFile(testFile, testObjectName);
        
        // 获取文件信息
        StatObjectResponse fileInfo = minIOService.getFileInfo(testObjectName);
        assertNotNull(fileInfo);
        assertEquals(testFile.getSize(), fileInfo.size());
        assertEquals("text/plain", fileInfo.contentType());
        
        // 清理测试文件
        minIOService.deleteFile(testObjectName);
    }

    @Test
    void testGenerateObjectName() {
        String originalFilename = "test file.txt";
        String prefix = "documents/";
        
        String objectName = minIOService.generateObjectName(originalFilename, prefix);
        
        assertNotNull(objectName);
        assertTrue(objectName.startsWith(prefix));
        assertTrue(objectName.contains("test_file.txt"));
        assertTrue(objectName.matches(".*\\d+_.*"));
    }

    @Test
    void testGetFileUrl() throws Exception {
        // 上传文件
        minIOService.uploadFile(testFile, testObjectName);
        
        // 获取文件URL
        String fileUrl = minIOService.getFileUrl(testObjectName);
        assertNotNull(fileUrl);
        assertTrue(fileUrl.contains(testObjectName));
        assertTrue(fileUrl.startsWith("http"));
        
        // 清理测试文件
        minIOService.deleteFile(testObjectName);
    }
}