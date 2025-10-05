package com.cms.permissions.dto;

import java.util.List;

public class BatchOperationResult {
    private int successCount;
    private int failureCount;
    private List<Long> failedIds;
    private String message;

    public BatchOperationResult() {}

    public BatchOperationResult(int successCount, int failureCount, List<Long> failedIds, String message) {
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.failedIds = failedIds;
        this.message = message;
    }

    public static BatchOperationResult of(int successCount, int failureCount, List<Long> failedIds, String message) {
        return new BatchOperationResult(successCount, failureCount, failedIds, message);
    }

    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }

    public int getFailureCount() { return failureCount; }
    public void setFailureCount(int failureCount) { this.failureCount = failureCount; }

    public List<Long> getFailedIds() { return failedIds; }
    public void setFailedIds(List<Long> failedIds) { this.failedIds = failedIds; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}