package com.ganithyanthram.modularapp.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to inject the current authenticated user's ID into controller methods.
 * 
 * Usage:
 * <pre>
 * {@code
 * @PostMapping
 * public ResponseEntity<?> create(@CurrentUser UUID userId, @RequestBody Request request) {
 *     // userId is automatically injected from JWT token
 * }
 * }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}
