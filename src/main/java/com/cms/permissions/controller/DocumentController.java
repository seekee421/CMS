package com.cms.permissions.controller;

import com.cms.permissions.entity.Document;
import com.cms.permissions.service.DocumentService;
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

    private Long getCurrentUserId(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("User not authenticated");
        }
        // Using SecurityUtils to get the current user ID from the security context
        return com.cms.permissions.util.SecurityUtils.getCurrentUserId();
    }
}