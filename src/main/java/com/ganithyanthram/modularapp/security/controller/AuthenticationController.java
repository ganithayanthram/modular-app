package com.ganithyanthram.modularapp.security.controller;

import com.ganithyanthram.modularapp.security.annotation.CurrentUser;
import com.ganithyanthram.modularapp.security.dto.AuthenticationResponse;
import com.ganithyanthram.modularapp.security.dto.LoginRequest;
import com.ganithyanthram.modularapp.security.dto.RefreshTokenRequest;
import com.ganithyanthram.modularapp.security.dto.RefreshTokenResponse;
import com.ganithyanthram.modularapp.security.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller for authentication operations.
 * Provides endpoints for login, logout, and token refresh.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    
    private final AuthenticationService authenticationService;
    
    /**
     * Authenticate user and return JWT tokens
     * 
     * @param request Login credentials
     * @return Authentication response with access and refresh tokens
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthenticationResponse response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Refresh access token using refresh token
     * 
     * @param request Refresh token request
     * @return New access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = authenticationService.refreshToken(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Logout user (client-side token removal)
     * 
     * @param authentication Current authentication
     * @return Success message
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            authenticationService.logout(email);
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
