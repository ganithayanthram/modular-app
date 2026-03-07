package com.ganithyanthram.modularapp.config;

import com.ganithyanthram.modularapp.security.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class for generating JWT tokens in tests.
 * Allows testing API endpoints with different user credentials and roles.
 */
@Component
public class JwtTestUtil {
    
    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;
    
    public JwtTestUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = Keys.hmacShaKeyFor(
            jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }
    
    /**
     * Generate an access token for testing with default user
     */
    public String generateAccessToken() {
        return generateAccessToken(UUID.randomUUID(), UUID.randomUUID(), "test@example.com");
    }
    
    /**
     * Generate an access token with specific user details
     */
    public String generateAccessToken(UUID userId, UUID orgId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("orgId", orgId.toString());
        claims.put("type", "access");
        
        return createToken(claims, email, jwtProperties.getAccessTokenExpirationMs());
    }
    
    /**
     * Generate an access token with roles for testing authorization
     */
    public String generateAccessTokenWithRoles(UUID userId, UUID orgId, String email, String... roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("orgId", orgId.toString());
        claims.put("type", "access");
        claims.put("roles", roles);
        
        return createToken(claims, email, jwtProperties.getAccessTokenExpirationMs());
    }
    
    /**
     * Generate an expired token for testing token expiration
     */
    public String generateExpiredToken(UUID userId, UUID orgId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("orgId", orgId.toString());
        claims.put("type", "access");
        
        return createToken(claims, email, -1000L); // Expired 1 second ago
    }
    
    /**
     * Create token with claims and expiration
     */
    private String createToken(Map<String, Object> claims, String subject, Long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);
        
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer(jwtProperties.getIssuer())
                .audience().add(jwtProperties.getAudience()).and()
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey)
                .compact();
    }
}
