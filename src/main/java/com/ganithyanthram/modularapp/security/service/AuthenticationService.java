package com.ganithyanthram.modularapp.security.service;

import com.ganithyanthram.modularapp.db.jooq.tables.pojos.Individual;
import com.ganithyanthram.modularapp.security.config.JwtProperties;
import com.ganithyanthram.modularapp.security.dto.AuthenticationResponse;
import com.ganithyanthram.modularapp.security.dto.LoginRequest;
import com.ganithyanthram.modularapp.security.dto.RefreshTokenRequest;
import com.ganithyanthram.modularapp.security.dto.RefreshTokenResponse;
import com.ganithyanthram.modularapp.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for handling authentication operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    
    /**
     * Authenticate user and generate JWT tokens
     */
    public AuthenticationResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getEmail());
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Individual individual = userDetailsService.loadIndividualByEmail(request.getEmail());
            
            String accessToken = jwtUtil.generateAccessToken(
                    userDetails, 
                    individual.getId(), 
                    request.getOrgId()
            );
            
            String refreshToken = jwtUtil.generateRefreshToken(
                    userDetails, 
                    individual.getId(),
                    request.getOrgId()
            );
            
            log.info("Login successful for user: {}", request.getEmail());
            
            return AuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtProperties.getAccessTokenExpirationMs())
                    .userId(individual.getId())
                    .email(individual.getEmail())
                    .name(individual.getName())
                    .build();
            
        } catch (BadCredentialsException e) {
            log.error("Login failed for user: {} - Invalid credentials", request.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }
    }
    
    /**
     * Refresh access token using refresh token
     */
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }
        
        String username = jwtUtil.extractUsername(refreshToken);
        UUID userId = jwtUtil.extractUserId(refreshToken);
        UUID orgId = jwtUtil.extractOrgId(refreshToken);
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        
        String newAccessToken = jwtUtil.generateAccessToken(
                userDetails, 
                userId, 
                orgId
        );
        
        log.info("Token refreshed for user: {}", username);
        
        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenExpirationMs())
                .build();
    }
    
    /**
     * Logout user (client-side token removal)
     */
    public void logout(String email) {
        log.info("User logged out: {}", email);
    }
}
