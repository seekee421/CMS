package com.cms.permissions.service;

import com.cms.permissions.entity.*;
import com.cms.permissions.exception.ResourceNotFoundException;
import com.cms.permissions.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

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

    // 导出文档（根据ID列表）
    @PreAuthorize("hasAuthority('DOC:EXPORT')")
    public List<Document> exportDocuments(List<Long> ids) {
        return documentRepository.findAllById(ids);
    }

    // 导入文档（JSON列表），将当前用户设为创建者并初始分配为编辑者
    @PreAuthorize("hasAuthority('DOC:IMPORT')")
    public List<Document> importDocuments(List<Document> documents, Long userId) {
        java.util.ArrayList<Document> saved = new java.util.ArrayList<>();
        for (Document doc : documents) {
            // 清理导入数据：不允许携带已有ID或敏感字段
            doc.setId(null);
            doc.setCreatedBy(userId);
            doc.setUpdatedAt(java.time.LocalDateTime.now());
            Document persisted = documentRepository.save(doc);
            saved.add(persisted);

            // 初始分配编辑者
            DocumentAssignment assignment = new DocumentAssignment(
                    persisted.getId(), userId, DocumentAssignment.AssignmentType.EDITOR, userId);
            documentAssignmentRepository.save(assignment);
        }
        // 清除相关用户的文档分配缓存
        permissionCacheService.evictUserDocumentAssignments(userId);
        return saved;
    }

    // 新增：分页与筛选查询（不破坏原有方法）
    @PreAuthorize("hasAuthority('DOC:VIEW:LIST') or hasPermission(#userId, 'user', 'DOC:VIEW:ASSIGNED')")
    public Page<Document> searchDocumentsForUser(Long userId, Document.DocumentStatus status, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return documentRepository.searchAccessibleDocuments(userId, status, keyword, pageable);
    }

    // 独立的状态更新方法，供批量操作使用
    @PreAuthorize("hasAuthority('" + com.cms.permissions.util.PermissionConstants.DOC_STATUS_UPDATE + "') OR hasPermission(#documentId, 'document', '" + com.cms.permissions.util.PermissionConstants.DOC_EDIT + "')")
    public Document updateDocumentStatus(Long documentId, Document.DocumentStatus status) {
        Document existingDocument = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));
        existingDocument.setStatus(status);
        existingDocument.setUpdatedAt(java.time.LocalDateTime.now());
        Document savedDocument = documentRepository.save(existingDocument);
        // 状态变更可能影响公开状态缓存
        permissionCacheService.evictDocumentPublicStatus(documentId);
        return savedDocument;
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