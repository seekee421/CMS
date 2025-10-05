package com.cms.permissions.controller;

import com.cms.permissions.dto.EditorContentRequest;
import com.cms.permissions.dto.EditorContentResponse;
import com.cms.permissions.dto.EditorPreviewResponse;
import com.cms.permissions.service.EditorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/editor")
@Tag(name = "Editor", description = "在线编辑器API")
public class EditorController {

    private static final Logger logger = LoggerFactory.getLogger(EditorController.class);

    @Autowired
    private EditorService editorService;

    @PostMapping("/content/save")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    @Operation(summary = "保存文档内容", description = "保存Markdown文档内容到指定文档")
    public ResponseEntity<EditorContentResponse> saveContent(
            @Valid @RequestBody EditorContentRequest request,
            Principal principal) {
        logger.info("Editor.saveContent principal={} documentId={} isDraft={} titleLen={} contentLen={}",
                getPrincipalNameSafe(),
                request.getDocumentId(),
                request.getIsDraft(),
                len(request.getTitle()),
                len(request.getContent()));
        Long userId = getCurrentUserId(principal);
        EditorContentResponse response = editorService.saveContent(request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/content/{documentId}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    @Operation(summary = "获取文档内容", description = "获取指定文档的Markdown内容用于编辑")
    public ResponseEntity<EditorContentResponse> getContent(
            @Parameter(description = "文档ID") @PathVariable Long documentId) {
        logger.info("Editor.getContent principal={} documentId={}", getPrincipalNameSafe(), documentId);
        EditorContentResponse response = editorService.getContent(documentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/preview")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    @Operation(summary = "预览Markdown内容", description = "将Markdown内容转换为HTML进行预览")
    public ResponseEntity<EditorPreviewResponse> previewContent(
            @RequestBody Map<String, String> request) {
        String markdownContent = request.get("content");
        logger.info("Editor.previewContent principal={} contentLen={}", getPrincipalNameSafe(), len(markdownContent));
        EditorPreviewResponse response = editorService.previewMarkdown(markdownContent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload/media")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    @Operation(summary = "上传媒体文件", description = "上传图片、视频等媒体文件用于文档编辑")
    public ResponseEntity<Map<String, String>> uploadMedia(
            @Parameter(description = "媒体文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "文档ID") @RequestParam(value = "documentId", required = false) Long documentId,
            Principal principal) {
        logger.info("Editor.uploadMedia principal={} documentId={} fileName={} contentType={} size={}",
                getPrincipalNameSafe(),
                documentId,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize());
        Long userId = getCurrentUserId(principal);
        Map<String, String> response = editorService.uploadMedia(file, documentId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/autosave")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    @Operation(summary = "自动保存", description = "自动保存文档内容草稿")
    public ResponseEntity<Map<String, Object>> autoSave(
            @Valid @RequestBody EditorContentRequest request,
            Principal principal) {
        logger.info("Editor.autoSave principal={} documentId={} isDraft={} titleLen={} contentLen={}",
                getPrincipalNameSafe(),
                request.getDocumentId(),
                request.getIsDraft(),
                len(request.getTitle()),
                len(request.getContent()));
        Long userId = getCurrentUserId(principal);
        Map<String, Object> response = editorService.autoSave(request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/draft/{documentId}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    @Operation(summary = "获取草稿内容", description = "获取文档的自动保存草稿内容")
    public ResponseEntity<EditorContentResponse> getDraft(
            @Parameter(description = "文档ID") @PathVariable Long documentId,
            Principal principal) {
        logger.info("Editor.getDraft principal={} documentId={}", getPrincipalNameSafe(), documentId);
        Long userId = getCurrentUserId(principal);
        EditorContentResponse response = editorService.getDraft(documentId, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/draft/{documentId}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    @Operation(summary = "清除草稿", description = "清除文档的自动保存草稿")
    public ResponseEntity<Void> clearDraft(
            @Parameter(description = "文档ID") @PathVariable Long documentId,
            Principal principal) {
        logger.info("Editor.clearDraft principal={} documentId={}", getPrincipalNameSafe(), documentId);
        Long userId = getCurrentUserId(principal);
        editorService.clearDraft(documentId, userId);
        return ResponseEntity.ok().build();
    }

    private Long getCurrentUserId(Principal principal) {
        // 直接使用SecurityUtils获取当前用户ID，它会从SecurityContext中获取CustomUserDetails
        return com.cms.permissions.util.SecurityUtils.getCurrentUserId();
    }

    private String getPrincipalNameSafe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "ANONYMOUS";
    }

    private Integer len(String s) {
        return s == null ? null : s.length();
    }
}