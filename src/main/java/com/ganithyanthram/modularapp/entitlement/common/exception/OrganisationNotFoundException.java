package com.ganithyanthram.modularapp.entitlement.common.exception;

/**
 * Exception thrown when an organisation is not found
 */
public class OrganisationNotFoundException extends RuntimeException {
    
    public OrganisationNotFoundException(String message) {
        super(message);
    }
    
    public OrganisationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
