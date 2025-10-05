package com.cms.permissions.controller;

import com.cms.permissions.entity.Document;
import com.cms.permissions.service.DocumentService;
import com.cms.permissions.dto.BatchOperationRequest;
import com.cms.permissions.dto.BatchOperationResult;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @PostMapping
    @PreAuthorize("hasAuthority('DOC:CREATE')")
    public ResponseEntity<Document> createDocument(@Valid @RequestBody Document document, Principal principal) {
        Long userId = getCurrentUserId(principal);
        Document createdDocument = documentService.createDocument(document, userId);
        return ResponseEntity.ok(createdDocument);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(#id, 'document', 'DOC:VIEW:LOGGED')")
    public ResponseEntity<Document> getDocument(@PathVariable Long id) {
        Document document = documentService.getDocument(id);
        return ResponseEntity.ok(document);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(#id, 'document', 'DOC:EDIT')")
    public ResponseEntity<Document> updateDocument(@PathVariable Long id, @Valid @RequestBody Document document) {
        Document updatedDocument = documentService.updateDocument(id, document);
        return ResponseEntity.ok(updatedDocument);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DOC:DELETE') OR hasPermission(#id, 'document', 'DOC:EDIT')")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasPermission(#id, 'document', 'DOC:PUBLISH')")
    public ResponseEntity<Document> publishDocument(@PathVariable Long id) {
        Document publishedDocument = documentService.publishDocument(id);
        return ResponseEntity.ok(publishedDocument);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('DOC:APPROVE:ALL') OR hasPermission(#id, 'document', 'DOC:APPROVE:ASSIGNED')")
    public ResponseEntity<Document> approveDocument(@PathVariable Long id, @RequestParam boolean approved) {
        Document approvedDocument = documentService.approveDocument(id, approved);
        return ResponseEntity.ok(approvedDocument);
    }

    @GetMapping
    public ResponseEntity<List<Document>> getDocumentsForUser(Principal principal) {
        Long userId = getCurrentUserId(principal);
        List<Document> documents = documentService.getDocumentsForUser(userId);
        return ResponseEntity.ok(documents);
    }

    // 新增：分页与筛选列表（保持向后兼容）
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('DOC:VIEW:LIST')")
    public ResponseEntity<com.cms.permissions.dto.PageResult<Document>> getDocumentsPage(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Document.DocumentStatus status) {
        Long userId = getCurrentUserId(principal);
        org.springframework.data.domain.Page<Document> result = documentService.searchDocumentsForUser(userId, status, keyword, page, size);
        return ResponseEntity.ok(com.cms.permissions.dto.PageResult.of(result.getTotalElements(), page, size, result.getContent()));
    }

    @PostMapping("/{documentId}/assign")
    @PreAuthorize("hasAuthority('DOC:ASSIGN')")
    public ResponseEntity<String> assignUserToDocument(
            @PathVariable Long documentId,
            @RequestParam Long userId,
            @RequestParam String assignmentType,
            Principal principal) {
        Long assignerId = getCurrentUserId(principal);
        boolean success = documentService.assignUserToDocument(
                documentId,
                userId,
                com.cms.permissions.entity.DocumentAssignment.AssignmentType.valueOf(assignmentType.toUpperCase()),
                assignerId);
        return success ? ResponseEntity.ok("User assigned successfully") : ResponseEntity.badRequest().build();
    }

    // 批量操作端点：DELETE / PUBLISH / UPDATE_STATUS
    @PostMapping("/batch")
    @PreAuthorize("hasAuthority('DOC:BATCH')")
    public ResponseEntity<BatchOperationResult> batchOperate(
            @Valid @RequestBody BatchOperationRequest request,
            Principal principal) {
        Long userId = getCurrentUserId(principal);
        int success = 0;
        java.util.ArrayList<Long> failed = new java.util.ArrayList<>();

        for (Long docId : request.getDocumentIds()) {
            try {
                switch (request.getOperation()) {
                    case DELETE -> {
                        // 复用delete接口的权限策略
                        documentService.deleteDocument(docId);
                        success++;
                    }
                    case PUBLISH -> {
                        // 复用publish接口的权限策略
                        documentService.publishDocument(docId);
                        success++;
                    }
                    case UPDATE_STATUS -> {
                        // 使用服务层的状态更新方法
                        documentService.updateDocumentStatus(docId, request.getTargetStatus());
                        success++;
                    }
                }
            } catch (Exception e) {
                failed.add(docId);
            }
        }

        BatchOperationResult result = BatchOperationResult.of(success, failed.size(), failed, failed.isEmpty() ? "OK" : "Partial failures");
        return ResponseEntity.ok(result);
    }

    // 导出JSON
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('DOC:EXPORT')")
    public ResponseEntity<List<Document>> exportDocuments(@RequestParam List<Long> ids) {
        List<Document> docs = documentService.exportDocuments(ids);
        return ResponseEntity.ok(docs);
    }

    // 导入JSON
    @PostMapping("/import")
    @PreAuthorize("hasAuthority('DOC:IMPORT')")
    public ResponseEntity<List<Document>> importDocuments(@Valid @RequestBody List<Document> documents,
                                                          Principal principal) {
        Long userId = getCurrentUserId(principal);
        List<Document> saved = documentService.importDocuments(documents, userId);
        return ResponseEntity.ok(saved);
    }

    private Long getCurrentUserId(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("User not authenticated");
        }
        // Using SecurityUtils to get the current user ID from the security context
        return com.cms.permissions.util.SecurityUtils.getCurrentUserId();
    }
}