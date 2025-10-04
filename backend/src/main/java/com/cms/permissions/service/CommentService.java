package com.cms.permissions.service;

import com.cms.permissions.entity.Comment;
import com.cms.permissions.exception.ResourceNotFoundException;
import com.cms.permissions.repository.CommentRepository;
import com.cms.permissions.repository.DocumentRepository;
import com.cms.permissions.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @PreAuthorize("hasAuthority('COMMENT:CREATE')")
    public Comment createComment(Long documentId, String content, Long userId) {
        // Verify document exists
        if (!documentRepository.existsById(documentId)) {
            throw new ResourceNotFoundException("Document not found with id: " + documentId);
        }

        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        Comment comment = new Comment(content, documentId, userId);
        return commentRepository.save(comment);
    }

    @PreAuthorize("hasAuthority('COMMENT:MANAGE') OR hasPermission(#commentId, 'comment', 'manage')")
    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new ResourceNotFoundException("Comment not found with id: " + commentId);
        }
        commentRepository.deleteById(commentId);
    }

    @PreAuthorize("hasAuthority('COMMENT:READ')")
    public List<Comment> getCommentsForDocument(Long documentId) {
        return commentRepository.findByDocumentId(documentId);
    }

    public List<Comment> getCommentsByUser(Long userId) {
        return commentRepository.findByUserId(userId);
    }
}