package com.ganithyanthram.modularapp.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT configuration properties.
 * Maps to jwt.* properties in application.properties.
 */
@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {
    
    private String secret;
    private Long accessTokenExpirationMs;
    private Long refreshTokenExpirationMs;
    private String issuer;
    private String audience;
}
