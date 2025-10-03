package com.cms.permissions.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "doc_assignment")
@IdClass(DocumentAssignment.DocumentAssignmentId.class)
public class DocumentAssignment {
    @Id
    @Column(name = "doc_id")
    private Long documentId;

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_type")
    private AssignmentType assignmentType;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "assigned_by")
    private Long assignedBy;

    // Constructors
    public DocumentAssignment() {}

    public DocumentAssignment(Long documentId, Long userId, AssignmentType assignmentType, Long assignedBy) {
        this.documentId = documentId;
        this.userId = userId;
        this.assignmentType = assignmentType;
        this.assignedBy = assignedBy;
        this.assignedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public AssignmentType getAssignmentType() {
        return assignmentType;
    }

    public void setAssignmentType(AssignmentType assignmentType) {
        this.assignmentType = assignmentType;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public Long getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(Long assignedBy) {
        this.assignedBy = assignedBy;
    }

    public enum AssignmentType {
        EDITOR, APPROVER
    }

    // Composite ID class
    public static class DocumentAssignmentId implements Serializable {
        private Long documentId;
        private Long userId;

        public DocumentAssignmentId() {}

        public DocumentAssignmentId(Long documentId, Long userId) {
            this.documentId = documentId;
            this.userId = userId;
        }

        public Long getDocumentId() { return documentId; }
        public void setDocumentId(Long documentId) { this.documentId = documentId; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DocumentAssignmentId)) return false;
            DocumentAssignmentId that = (DocumentAssignmentId) o;
            return documentId.equals(that.documentId) && userId.equals(that.userId);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(documentId, userId);
        }
    }
}