package com.cms.permissions.controller;

import com.cms.permissions.entity.DocumentFeedback;
import com.cms.permissions.service.DocumentFeedbackService;
import com.cms.permissions.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@Tag(name = "Feedback", description = "文档反馈API")
public class FeedbackController {

    @Autowired
    private DocumentFeedbackService feedbackService;

    @PostMapping
    @PreAuthorize("hasAuthority('FEEDBACK:SUBMIT')")
    @Operation(summary = "提交反馈")
    public ResponseEntity<DocumentFeedback> submit(@RequestBody SubmitFeedbackRequest req) {
        Long userId = SecurityUtils.getCurrentUserId();
        DocumentFeedback saved = feedbackService.submitFeedback(req.documentId, userId, req.feedbackType, req.description, req.contactInfo);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/document/{documentId}")
    @PreAuthorize("hasAuthority('FEEDBACK:READ')")
    @Operation(summary = "查看文档反馈")
    public ResponseEntity<List<DocumentFeedback>> listByDocument(@PathVariable Long documentId) {
        return ResponseEntity.ok(feedbackService.getFeedbacksForDocument(documentId));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('FEEDBACK:READ')")
    @Operation(summary = "查看用户反馈")
    public ResponseEntity<List<DocumentFeedback>> listByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(feedbackService.getFeedbacksByUser(userId));
    }

    @PostMapping("/process/{id}")
    @PreAuthorize("hasAuthority('FEEDBACK:PROCESS')")
    @Operation(summary = "反馈处理")
    public ResponseEntity<DocumentFeedback> process(@PathVariable Long id) {
        Long operatorId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(feedbackService.processFeedback(id, operatorId));
    }

    public static class SubmitFeedbackRequest {
        @NotNull
        public Long documentId;
        @NotNull
        public DocumentFeedback.FeedbackType feedbackType;
        public String description;
        public String contactInfo;
    }
}