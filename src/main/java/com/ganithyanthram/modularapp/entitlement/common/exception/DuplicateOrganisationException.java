package com.ganithyanthram.modularapp.entitlement.common.exception;

/**
 * Exception thrown when attempting to create a duplicate organisation
 */
public class DuplicateOrganisationException extends RuntimeException {
    
    public DuplicateOrganisationException(String message) {
        super(message);
    }
    
    public DuplicateOrganisationException(String message, Throwable cause) {
        super(message, cause);
    }
}
