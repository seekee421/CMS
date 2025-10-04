package com.cms.permissions.controller;

import com.cms.permissions.entity.Comment;
import com.cms.permissions.service.CommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping
    @PreAuthorize("hasAuthority('COMMENT:CREATE')")
    public ResponseEntity<Comment> createComment(@Valid @RequestBody CreateCommentRequest request) {
        Comment comment = commentService.createComment(request.getDocumentId(), request.getContent(), request.getUserId());
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('COMMENT:MANAGE') OR hasPermission(#id, 'comment', 'COMMENT:MANAGE')")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/document/{documentId}")
    @PreAuthorize("hasPermission(#documentId, 'document', 'DOC:VIEW:LOGGED')")
    public ResponseEntity<List<Comment>> getCommentsForDocument(@PathVariable Long documentId) {
        List<Comment> comments = commentService.getCommentsForDocument(documentId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Comment>> getCommentsByUser(@PathVariable Long userId) {
        List<Comment> comments = commentService.getCommentsByUser(userId);
        return ResponseEntity.ok(comments);
    }

    public static class CreateCommentRequest {
        @NotNull(message = "Document ID is required")
        private Long documentId;

        @NotBlank(message = "Comment content is required")
        @Size(max = 1000, message = "Comment content must not exceed 1000 characters")
        private String content;

        @NotNull(message = "User ID is required")
        private Long userId;

        // Getters and setters
        public Long getDocumentId() { return documentId; }
        public void setDocumentId(Long documentId) { this.documentId = documentId; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }
}