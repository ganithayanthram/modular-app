package com.ganithyanthram.modularapp.entitlement.common.exception;

/**
 * Exception thrown when attempting to create a duplicate assignment
 */
public class DuplicateAssignmentException extends RuntimeException {
    
    public DuplicateAssignmentException(String message) {
        super(message);
    }
    
    public DuplicateAssignmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
