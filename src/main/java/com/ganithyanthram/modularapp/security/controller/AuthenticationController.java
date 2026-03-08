package com.ganithyanthram.modularapp.security.controller;

import com.ganithyanthram.modularapp.security.annotation.CurrentUser;
import com.ganithyanthram.modularapp.security.dto.AuthenticationResponse;
import com.ganithyanthram.modularapp.security.dto.LoginRequest;
import com.ganithyanthram.modularapp.security.dto.RefreshTokenRequest;
import com.ganithyanthram.modularapp.security.dto.RefreshTokenResponse;
import com.ganithyanthram.modularapp.security.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthenticationController {
    
    private final AuthenticationService authenticationService;
    
    /**
     * Authenticate user and return JWT tokens
     * 
     * @param request Login credentials
     * @return Authentication response with access and refresh tokens
     */
    @Operation(
        summary = "Login",
        description = "Authenticate user with email and password. Returns JWT access token and refresh token."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request body",
            content = @Content
        )
    })
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
    @Operation(
        summary = "Refresh Token",
        description = "Generate a new access token using a valid refresh token"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token refreshed successfully",
            content = @Content(schema = @Schema(implementation = RefreshTokenResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid or expired refresh token",
            content = @Content
        )
    })
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
    @Operation(
        summary = "Logout",
        description = "Logout the current user. Client should discard the JWT token after this call."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Logout successful"
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            authenticationService.logout(email);
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
