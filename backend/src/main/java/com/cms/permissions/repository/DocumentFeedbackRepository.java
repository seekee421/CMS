package com.cms.permissions.repository;

import com.cms.permissions.entity.DocumentFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentFeedbackRepository extends JpaRepository<DocumentFeedback, Long> {
    List<DocumentFeedback> findByDocumentId(Long documentId);
    List<DocumentFeedback> findByUserId(Long userId);
}