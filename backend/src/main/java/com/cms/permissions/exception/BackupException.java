package com.cms.permissions.exception;

/**
 * 备份操作相关的异常类
 */
public class BackupException extends RuntimeException {
    
    public BackupException(String message) {
        super(message);
    }
    
    public BackupException(String message, Throwable cause) {
        super(message, cause);
    }
}