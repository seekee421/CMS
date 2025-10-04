package com.cms.permissions.repository;

import com.cms.permissions.entity.DocumentAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentAssignmentRepository extends JpaRepository<DocumentAssignment, DocumentAssignment.DocumentAssignmentId> {
    List<DocumentAssignment> findByUserId(Long userId);

    List<DocumentAssignment> findByDocumentId(Long documentId);

    @Query("SELECT da FROM DocumentAssignment da WHERE da.documentId = :documentId AND da.userId = :userId AND da.assignmentType = :assignmentType")
    Optional<DocumentAssignment> findByDocumentIdAndUserIdAndAssignmentType(
            @Param("documentId") Long documentId,
            @Param("userId") Long userId,
            @Param("assignmentType") DocumentAssignment.AssignmentType assignmentType
    );

    boolean existsByDocumentIdAndUserIdAndAssignmentType(Long documentId, Long userId, DocumentAssignment.AssignmentType assignmentType);
}