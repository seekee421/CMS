package com.cms.permissions.repository;

import com.cms.permissions.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    // Find documents that are published or assigned to the user
    @Query("SELECT d FROM Document d WHERE d.status = com.cms.permissions.entity.Document$DocumentStatus.PUBLISHED OR d.id IN " +
           "(SELECT da.documentId FROM DocumentAssignment da WHERE da.userId = :userId)")
    List<Document> findDocumentsForUser(@Param("userId") Long userId);

    // Find documents assigned to a specific user with a specific role
    @Query("SELECT d FROM Document d WHERE d.id IN " +
           "(SELECT da.documentId FROM DocumentAssignment da WHERE da.userId = :userId AND da.assignmentType = :assignmentType)")
    List<Document> findDocumentsAssignedToUser(@Param("userId") Long userId,
                                               @Param("assignmentType") com.cms.permissions.entity.DocumentAssignment.AssignmentType assignmentType);
}