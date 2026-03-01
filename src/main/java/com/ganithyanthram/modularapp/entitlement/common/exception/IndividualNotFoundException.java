package com.ganithyanthram.modularapp.entitlement.common.exception;

/**
 * Exception thrown when an individual is not found
 */
public class IndividualNotFoundException extends RuntimeException {
    
    public IndividualNotFoundException(String message) {
        super(message);
    }
    
    public IndividualNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
