package com.cms.permissions.dto;

import com.cms.permissions.entity.Document;
import java.util.List;

public class BatchOperationRequest {
    public enum OperationType {
        DELETE,
        PUBLISH,
        UPDATE_STATUS
    }

    private OperationType operation;
    private List<Long> documentIds;
    private Document.DocumentStatus targetStatus; // 用于UPDATE_STATUS，PUBLISH时可忽略

    public BatchOperationRequest() {}

    public OperationType getOperation() { return operation; }
    public void setOperation(OperationType operation) { this.operation = operation; }

    public List<Long> getDocumentIds() { return documentIds; }
    public void setDocumentIds(List<Long> documentIds) { this.documentIds = documentIds; }

    public Document.DocumentStatus getTargetStatus() { return targetStatus; }
    public void setTargetStatus(Document.DocumentStatus targetStatus) { this.targetStatus = targetStatus; }
}