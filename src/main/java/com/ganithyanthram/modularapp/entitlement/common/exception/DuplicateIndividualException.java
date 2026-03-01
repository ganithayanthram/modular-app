package com.ganithyanthram.modularapp.entitlement.common.exception;

/**
 * Exception thrown when attempting to create a duplicate individual
 */
public class DuplicateIndividualException extends RuntimeException {
    
    public DuplicateIndividualException(String message) {
        super(message);
    }
    
    public DuplicateIndividualException(String message, Throwable cause) {
        super(message, cause);
    }
}
