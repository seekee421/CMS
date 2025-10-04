package com.cms.permissions.controller;

import com.cms.permissions.entity.MediaResource;
import com.cms.permissions.repository.MediaResourceRepository;
import com.cms.permissions.service.MinIOService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 文件管理控制器
 * 提供文件上传、下载、删除等API
 */
@RestController
@RequestMapping("/api/files")
@Tag(name = "文件管理", description = "文件上传、下载、删除等操作")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private MinIOService minIOService;

    @Autowired
    private MediaResourceRepository mediaResourceRepository;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传文件到MinIO存储")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @Parameter(description = "要上传的文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "文件分类") @RequestParam(value = "category", defaultValue = "general") String category,
            @Parameter(description = "文件描述") @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "关联文档ID") @RequestParam(value = "documentId", required = false) Long documentId) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "文件不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 生成唯一的对象名称
            String objectName = minIOService.generateObjectName(file.getOriginalFilename(), category + "/");

            // 上传文件到MinIO
            String fileUrl = minIOService.uploadFile(file, objectName);

            // 保存文件元数据到数据库
            MediaResource mediaResource = new MediaResource(
                    file.getOriginalFilename(),
                    objectName,
                    file.getContentType(),
                    file.getSize()
            );
            mediaResource.setFileUrl(fileUrl);
            mediaResource.setFileCategory(category);
            mediaResource.setDescription(description);
            mediaResource.setDocumentId(documentId);
            // TODO: 从安全上下文获取当前用户ID
            mediaResource.setUploadedBy(1L); // 临时硬编码

            MediaResource savedResource = mediaResourceRepository.save(mediaResource);

            response.put("success", true);
            response.put("message", "文件上传成功");
            
            Map<String, Object> fileData = new HashMap<>();
            fileData.put("id", savedResource.getId());
            fileData.put("originalFilename", savedResource.getOriginalFilename());
            fileData.put("objectName", savedResource.getObjectName());
            fileData.put("fileUrl", savedResource.getFileUrl());
            fileData.put("fileSize", savedResource.getFileSize());
            fileData.put("contentType", savedResource.getContentType());
            fileData.put("category", savedResource.getFileCategory());
            fileData.put("uploadTime", savedResource.getUploadTime());
            response.put("data", fileData);

            logger.info("文件上传成功: {}", file.getOriginalFilename());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("文件上传失败", e);
            response.put("success", false);
            response.put("message", "文件上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 下载文件
     */
    @GetMapping("/download/{id}")
    @Operation(summary = "下载文件", description = "根据文件ID下载文件")
    public ResponseEntity<?> downloadFile(@Parameter(description = "文件ID") @PathVariable Long id) {
        try {
            Optional<MediaResource> resourceOpt = mediaResourceRepository.findById(id);
            if (resourceOpt.isEmpty() || resourceOpt.get().getIsDeleted()) {
                return ResponseEntity.notFound().build();
            }

            MediaResource resource = resourceOpt.get();
            InputStream inputStream = minIOService.downloadFile(resource.getObjectName());

            // 更新最后访问时间
            resource.setLastAccessed(LocalDateTime.now());
            mediaResourceRepository.save(resource);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getOriginalFilename() + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, resource.getContentType());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(inputStream));

        } catch (Exception e) {
            logger.error("文件下载失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "文件下载失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取文件访问URL
     */
    @GetMapping("/url/{id}")
    @Operation(summary = "获取文件访问URL", description = "获取文件的预签名访问URL")
    public ResponseEntity<Map<String, Object>> getFileUrl(@Parameter(description = "文件ID") @PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<MediaResource> resourceOpt = mediaResourceRepository.findById(id);
            if (resourceOpt.isEmpty() || resourceOpt.get().getIsDeleted()) {
                response.put("success", false);
                response.put("message", "文件不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            MediaResource resource = resourceOpt.get();
            String fileUrl = minIOService.getFileUrl(resource.getObjectName());

            // 更新最后访问时间
            resource.setLastAccessed(LocalDateTime.now());
            mediaResourceRepository.save(resource);

            response.put("success", true);
            
            Map<String, Object> urlData = new HashMap<>();
            urlData.put("id", resource.getId());
            urlData.put("originalFilename", resource.getOriginalFilename());
            urlData.put("fileUrl", fileUrl);
            urlData.put("contentType", resource.getContentType());
            response.put("data", urlData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取文件URL失败", e);
            response.put("success", false);
            response.put("message", "获取文件URL失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除文件", description = "删除指定ID的文件")
    public ResponseEntity<Map<String, Object>> deleteFile(@Parameter(description = "文件ID") @PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<MediaResource> resourceOpt = mediaResourceRepository.findById(id);
            if (resourceOpt.isEmpty() || resourceOpt.get().getIsDeleted()) {
                response.put("success", false);
                response.put("message", "文件不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            MediaResource resource = resourceOpt.get();

            // 从MinIO删除文件
            minIOService.deleteFile(resource.getObjectName());

            // 软删除数据库记录
            resource.setIsDeleted(true);
            mediaResourceRepository.save(resource);

            response.put("success", true);
            response.put("message", "文件删除成功");

            logger.info("文件删除成功: {}", resource.getOriginalFilename());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("文件删除失败", e);
            response.put("success", false);
            response.put("message", "文件删除失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取文件列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取文件列表", description = "获取所有文件的列表")
    public ResponseEntity<Map<String, Object>> getFileList(
            @Parameter(description = "文件分类") @RequestParam(value = "category", required = false) String category,
            @Parameter(description = "文档ID") @RequestParam(value = "documentId", required = false) Long documentId) {

        Map<String, Object> response = new HashMap<>();

        try {
            List<MediaResource> resources;

            if (documentId != null) {
                resources = mediaResourceRepository.findByDocumentIdAndIsDeletedFalse(documentId);
            } else if (category != null) {
                resources = mediaResourceRepository.findByFileCategoryAndIsDeletedFalse(category);
            } else {
                resources = mediaResourceRepository.findByIsDeletedFalseOrderByUploadTimeDesc();
            }

            List<Map<String, Object>> fileList = resources.stream()
                    .map(resource -> {
                        Map<String, Object> fileMap = new HashMap<>();
                        fileMap.put("id", resource.getId());
                        fileMap.put("originalFilename", resource.getOriginalFilename());
                        fileMap.put("fileSize", resource.getFileSize());
                        fileMap.put("contentType", resource.getContentType());
                        fileMap.put("category", resource.getFileCategory() != null ? resource.getFileCategory() : "");
                        fileMap.put("description", resource.getDescription() != null ? resource.getDescription() : "");
                        fileMap.put("uploadTime", resource.getUploadTime());
                        fileMap.put("lastAccessed", resource.getLastAccessed());
                        return fileMap;
                    })
                    .toList();

            response.put("success", true);
            response.put("data", fileList);
            response.put("total", fileList.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取文件列表失败", e);
            response.put("success", false);
            response.put("message", "获取文件列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取文件信息
     */
    @GetMapping("/info/{id}")
    @Operation(summary = "获取文件信息", description = "获取指定文件的详细信息")
    public ResponseEntity<Map<String, Object>> getFileInfo(@Parameter(description = "文件ID") @PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<MediaResource> resourceOpt = mediaResourceRepository.findById(id);
            if (resourceOpt.isEmpty() || resourceOpt.get().getIsDeleted()) {
                response.put("success", false);
                response.put("message", "文件不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            MediaResource resource = resourceOpt.get();

            response.put("success", true);
            
            Map<String, Object> infoData = new HashMap<>();
            infoData.put("id", resource.getId());
            infoData.put("originalFilename", resource.getOriginalFilename());
            infoData.put("objectName", resource.getObjectName());
            infoData.put("contentType", resource.getContentType());
            infoData.put("fileSize", resource.getFileSize());
            infoData.put("category", resource.getFileCategory() != null ? resource.getFileCategory() : "");
            infoData.put("description", resource.getDescription() != null ? resource.getDescription() : "");
            infoData.put("documentId", resource.getDocumentId());
            infoData.put("uploadedBy", resource.getUploadedBy());
            infoData.put("uploadTime", resource.getUploadTime());
            infoData.put("lastAccessed", resource.getLastAccessed());
            infoData.put("tags", resource.getTags() != null ? resource.getTags() : "");
            response.put("data", infoData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取文件信息失败", e);
            response.put("success", false);
            response.put("message", "获取文件信息失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}