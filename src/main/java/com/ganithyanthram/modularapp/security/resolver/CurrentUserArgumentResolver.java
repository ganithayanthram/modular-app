package com.ganithyanthram.modularapp.security.resolver;

import com.ganithyanthram.modularapp.security.annotation.CurrentUser;
import com.ganithyanthram.modularapp.security.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.UUID;

/**
 * Argument resolver for @CurrentUser annotation.
 * Extracts user ID from JWT token and injects it into controller method parameters.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {
    
    private final JwtUtil jwtUtil;
    
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && UUID.class.equals(parameter.getParameterType());
    }
    
    @Override
    public Object resolveArgument(
            @NonNull MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            @NonNull NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {
        
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        
        if (request == null) {
            log.error("Failed to get native request");
            return null;
        }
        
        String jwt = extractJwtFromRequest(request);
        
        if (jwt == null || !jwtUtil.isValidToken(jwt)) {
            log.warn("Invalid or missing JWT token");
            return null;
        }
        
        UUID userId = jwtUtil.extractUserId(jwt);
        log.debug("Resolved current user ID: {}", userId);
        
        return userId;
    }
    
    /**
     * Extract JWT token from Authorization header
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        return null;
    }
}
