package com.cms.permissions.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * MinIO文件存储服务
 * 提供文件上传、下载、删除等功能
 */
@Service
public class MinIOService {

    private static final Logger logger = LoggerFactory.getLogger(MinIOService.class);

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    /**
     * 初始化，确保bucket存在
     */
    @PostConstruct
    public void init() {
        try {
            createBucketIfNotExists();
        } catch (Exception e) {
            logger.error("初始化MinIO服务失败", e);
        }
    }

    /**
     * 创建bucket（如果不存在）
     */
    private void createBucketIfNotExists() throws Exception {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            logger.info("创建bucket: {}", bucketName);
        }
    }

    /**
     * 上传文件
     * @param file 要上传的文件
     * @param objectName 对象名称（文件在MinIO中的路径）
     * @return 文件的访问URL
     */
    public String uploadFile(MultipartFile file, String objectName) throws Exception {
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            logger.info("文件上传成功: {}", objectName);
            return getFileUrl(objectName);
        }
    }

    /**
     * 上传文件流
     * @param inputStream 输入流
     * @param objectName 对象名称
     * @param contentType 内容类型
     * @param size 文件大小
     * @return 文件的访问URL
     */
    public String uploadFile(InputStream inputStream, String objectName, String contentType, long size) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(inputStream, size, -1)
                        .contentType(contentType)
                        .build()
        );
        logger.info("文件上传成功: {}", objectName);
        return getFileUrl(objectName);
    }

    /**
     * 下载文件
     * @param objectName 对象名称
     * @return 文件输入流
     */
    public InputStream downloadFile(String objectName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }

    /**
     * 删除文件
     * @param objectName 对象名称
     */
    public void deleteFile(String objectName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
        logger.info("文件删除成功: {}", objectName);
    }

    /**
     * 获取文件访问URL（预签名URL，有效期7天）
     * @param objectName 对象名称
     * @return 预签名URL
     */
    public String getFileUrl(String objectName) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(objectName)
                        .expiry(7, TimeUnit.DAYS)
                        .build()
        );
    }

    /**
     * 检查文件是否存在
     * @param objectName 对象名称
     * @return 是否存在
     */
    public boolean fileExists(String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取文件信息
     * @param objectName 对象名称
     * @return 文件统计信息
     */
    public StatObjectResponse getFileInfo(String objectName) throws Exception {
        return minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }

    /**
     * 生成唯一的对象名称
     * @param originalFilename 原始文件名
     * @param prefix 前缀（如：documents/, images/）
     * @return 唯一的对象名称
     */
    public String generateObjectName(String originalFilename, String prefix) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return prefix + timestamp + "_" + originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}