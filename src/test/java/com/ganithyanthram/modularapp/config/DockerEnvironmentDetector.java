package com.ganithyanthram.modularapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures TestContainers to work with Multipass Docker
 */
public class DockerEnvironmentDetector {
    
    private static final Logger log = LoggerFactory.getLogger(DockerEnvironmentDetector.class);
    private static final String MULTIPASS_DOCKER_HOST = "tcp://192.168.64.8:2376";
    
    /**
     * Configure TestContainers for Multipass Docker environment
     * Call this before any TestContainers usage
     */
    public static void configureForMultipass() {
        log.info("Configuring TestContainers for Multipass Docker: {}", MULTIPASS_DOCKER_HOST);
        
        // Set environment variables (TestContainers 2.x prefers env vars)
        setEnvironmentVariable("DOCKER_HOST", MULTIPASS_DOCKER_HOST);
        setEnvironmentVariable("TESTCONTAINERS_RYUK_DISABLED", "true");
        setEnvironmentVariable("TESTCONTAINERS_CHECKS_DISABLE", "true");
        setEnvironmentVariable("DOCKER_API_VERSION", "1.44");
        
        // Also set system properties as fallback
        System.setProperty("DOCKER_HOST", MULTIPASS_DOCKER_HOST);
        System.setProperty("TESTCONTAINERS_RYUK_DISABLED", "true");
        System.setProperty("TESTCONTAINERS_CHECKS_DISABLE", "true");
        System.setProperty("DOCKER_API_VERSION", "1.44");
        
        log.info("✅ TestContainers configured for Multipass Docker");
        log.info("DOCKER_HOST: {}", System.getenv("DOCKER_HOST"));
    }
    
    /**
     * Set environment variable using reflection (since env vars are read-only by default)
     */
    private static void setEnvironmentVariable(String key, String value) {
        try {
            java.util.Map<String, String> env = System.getenv();
            Class<?> clazz = env.getClass();
            java.lang.reflect.Field field = clazz.getDeclaredField("m");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<String, String> writableEnv = (java.util.Map<String, String>) field.get(env);
            writableEnv.put(key, value);
        } catch (Exception e) {
            log.warn("Failed to set environment variable {}: {}", key, e.getMessage());
        }
    }
}