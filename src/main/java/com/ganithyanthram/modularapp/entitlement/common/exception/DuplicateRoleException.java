package com.ganithyanthram.modularapp.entitlement.common.exception;

/**
 * Exception thrown when attempting to create a duplicate role
 */
public class DuplicateRoleException extends RuntimeException {
    
    public DuplicateRoleException(String message) {
        super(message);
    }
    
    public DuplicateRoleException(String message, Throwable cause) {
        super(message, cause);
    }
}
