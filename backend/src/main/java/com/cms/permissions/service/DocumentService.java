package com.cms.permissions.service;

import com.cms.permissions.entity.*;
import com.cms.permissions.exception.ResourceNotFoundException;
import com.cms.permissions.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentAssignmentRepository documentAssignmentRepository;

    @Autowired
    private PermissionCacheService permissionCacheService;

    @PreAuthorize("hasAuthority('DOC:CREATE') or hasPermission(#document, 'DOC:EDIT')")
    public Document createDocument(Document document, Long userId) {
        document.setCreatedBy(userId);
        Document savedDocument = documentRepository.save(document);

        // Assign the creator as the initial editor
        DocumentAssignment assignment = new DocumentAssignment(
                savedDocument.getId(), userId, DocumentAssignment.AssignmentType.EDITOR, userId);
        documentAssignmentRepository.save(assignment);

        // 清除相关用户的文档分配缓存
        permissionCacheService.evictUserDocumentAssignments(userId);

        return savedDocument;
    }

    @PreAuthorize("hasPermission(#documentId, 'document', 'DOC:VIEW:LOGGED')")
    public Document getDocument(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));
    }

    @PreAuthorize("hasPermission(#documentId, 'document', 'DOC:EDIT')")
    public Document updateDocument(Long documentId, Document updatedDocument) {
        Document existingDocument = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        existingDocument.setTitle(updatedDocument.getTitle());
        existingDocument.setContent(updatedDocument.getContent());
        existingDocument.setUpdatedAt(java.time.LocalDateTime.now());
        return documentRepository.save(existingDocument);
    }

    @PreAuthorize("hasAuthority('DOC:DELETE') OR hasPermission(#documentId, 'document', 'DOC:EDIT')")
    public void deleteDocument(Long documentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new ResourceNotFoundException("Document not found with id: " + documentId);
        }
        
        // 清除文档相关的缓存
        permissionCacheService.evictDocumentPublicStatus(documentId);
        permissionCacheService.evictAllUserDocumentAssignments();
        
        documentRepository.deleteById(documentId);
    }

    @PreAuthorize("hasPermission(#documentId, 'document', 'DOC:PUBLISH')")
    public Document publishDocument(Long documentId) {
        Document existingDocument = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        if (existingDocument.getStatus() != Document.DocumentStatus.PUBLISHED) {
            existingDocument.setStatus(Document.DocumentStatus.PUBLISHED);
            existingDocument.setUpdatedAt(java.time.LocalDateTime.now());
            Document savedDocument = documentRepository.save(existingDocument);
            
            // 清除文档公开状态缓存
            permissionCacheService.evictDocumentPublicStatus(documentId);
            
            return savedDocument;
        }
        return existingDocument;
    }

    @PreAuthorize("hasAuthority('DOC:APPROVE:ALL') OR hasPermission(#documentId, 'document', 'DOC:APPROVE:ASSIGNED')")
    public Document approveDocument(Long documentId, boolean approved) {
        Document existingDocument = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        if (existingDocument.getStatus() == Document.DocumentStatus.PENDING_APPROVAL) {
            existingDocument.setStatus(approved ? Document.DocumentStatus.PUBLISHED : Document.DocumentStatus.REJECTED);
            existingDocument.setUpdatedAt(java.time.LocalDateTime.now());
            Document savedDocument = documentRepository.save(existingDocument);
            
            // 清除文档公开状态缓存
            permissionCacheService.evictDocumentPublicStatus(documentId);
            
            return savedDocument;
        }
        return existingDocument;
    }

    public List<Document> getDocumentsForUser(Long userId) {
        return documentRepository.findDocumentsForUser(userId);
    }

    // Method to assign a user to a document
    @PreAuthorize("hasAuthority('DOC:ASSIGN')")
    public boolean assignUserToDocument(Long documentId, Long userId, DocumentAssignment.AssignmentType assignmentType, Long assignerId) {
        DocumentAssignment assignment = new DocumentAssignment(documentId, userId, assignmentType, assignerId);
        documentAssignmentRepository.save(assignment);
        
        // 清除相关用户的文档分配缓存
        permissionCacheService.evictUserDocumentAssignments(userId);
        
        return true;
    }
}