package com.cms.permissions.service;

import com.cms.permissions.entity.DocumentFeedback;
import com.cms.permissions.exception.ResourceNotFoundException;
import com.cms.permissions.repository.DocumentFeedbackRepository;
import com.cms.permissions.repository.DocumentRepository;
import com.cms.permissions.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class DocumentFeedbackService {

    @Autowired
    private DocumentFeedbackRepository feedbackRepository;
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private UserRepository userRepository;

    @PreAuthorize("hasAuthority('FEEDBACK:SUBMIT')")
    public DocumentFeedback submitFeedback(Long documentId, Long userId, DocumentFeedback.FeedbackType type, String description, String contactInfo) {
        if (!documentRepository.existsById(documentId)) {
            throw new ResourceNotFoundException("Document not found with id: " + documentId);
        }
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        DocumentFeedback feedback = new DocumentFeedback(documentId, userId, type, description, contactInfo);
        return feedbackRepository.save(feedback);
    }

    @PreAuthorize("hasAuthority('FEEDBACK:READ')")
    public List<DocumentFeedback> getFeedbacksForDocument(Long documentId) {
        return feedbackRepository.findByDocumentId(documentId);
    }

    @PreAuthorize("hasAuthority('FEEDBACK:READ')")
    public List<DocumentFeedback> getFeedbacksByUser(Long userId) {
        return feedbackRepository.findByUserId(userId);
    }

    @PreAuthorize("hasAuthority('FEEDBACK:PROCESS')")
    public DocumentFeedback processFeedback(Long feedbackId, Long operatorUserId) {
        DocumentFeedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found with id: " + feedbackId));
        feedback.setProcessed(true);
        feedback.setProcessedAt(LocalDateTime.now());
        feedback.setProcessedBy(operatorUserId);
        feedback.setUpdatedAt(LocalDateTime.now());
        return feedbackRepository.save(feedback);
    }
}