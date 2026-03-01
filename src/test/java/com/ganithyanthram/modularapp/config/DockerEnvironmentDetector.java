package com.ganithyanthram.modularapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configures Testcontainers to work with both local Docker and remote Docker (e.g., Multipass VM).
 *
 * Configuration is driven by Spring properties in application-{profile}.properties:
 * - testcontainers.docker.remote: true for remote Docker, false (or omit) for local Docker
 * - testcontainers.docker.host: Docker host URI (e.g., tcp://192.168.64.8:2376)
 * - testcontainers.ryuk.disabled: true to disable Ryuk (recommended for remote Docker)
 * - testcontainers.checks.disabled: true to disable Testcontainers startup checks
 * - testcontainers.docker.api-version: Docker API version (e.g., 1.44)
 *
 * This class uses System.setProperty() instead of reflection to set Testcontainers configuration,
 * which is compatible with Java 17+ module system.
 *
 * Call configureFromProfile() in a static block BEFORE any @Container fields are initialized.
 */
public class DockerEnvironmentDetector {

    private static final Logger log = LoggerFactory.getLogger(DockerEnvironmentDetector.class);

    /**
     * Configure Testcontainers by reading properties from application-{profile}.properties.
     *
     * @param profile The Spring profile to use (e.g., "test", "dev", "prod")
     */
    public static void configureFromProfile(String profile) {
        String propertiesFile = "application-" + profile + ".properties";
        Properties props = new Properties();

        try (InputStream is = DockerEnvironmentDetector.class.getClassLoader().getResourceAsStream(propertiesFile)) {
            if (is == null) {
                log.warn("Could not find {} — using default Testcontainers configuration", propertiesFile);
                return;
            }
            props.load(is);
        } catch (IOException e) {
            log.error("Failed to load {}: {}", propertiesFile, e.getMessage());
            return;
        }

        boolean remoteDocker = Boolean.parseBoolean(props.getProperty("testcontainers.docker.remote", "false"));
        String dockerHost = props.getProperty("testcontainers.docker.host", "");
        boolean ryukDisabled = Boolean.parseBoolean(props.getProperty("testcontainers.ryuk.disabled", "false"));
        boolean checksDisabled = Boolean.parseBoolean(props.getProperty("testcontainers.checks.disabled", "false"));
        String dockerApiVersion = props.getProperty("testcontainers.docker.api-version", "");

        if (remoteDocker && !dockerHost.isEmpty()) {
            log.info("Configuring Testcontainers for remote Docker: {}", dockerHost);
            System.setProperty("DOCKER_HOST", dockerHost);
            log.info("System property DOCKER_HOST set to: {}", System.getProperty("DOCKER_HOST"));
        } else {
            log.info("Configuring Testcontainers for local Docker (auto-detect socket)");
            // Don't set DOCKER_HOST — let Testcontainers auto-detect the local socket
        }

        // Set Ryuk configuration
        System.setProperty("TESTCONTAINERS_RYUK_DISABLED", String.valueOf(ryukDisabled));
        log.info("Testcontainers Ryuk disabled: {}", ryukDisabled);

        // Set startup checks configuration
        System.setProperty("TESTCONTAINERS_CHECKS_DISABLE", String.valueOf(checksDisabled));
        log.info("Testcontainers startup checks disabled: {}", checksDisabled);

        // Set Docker API version if specified
        if (!dockerApiVersion.isEmpty()) {
            System.setProperty("DOCKER_API_VERSION", dockerApiVersion);
            log.info("Docker API version: {}", dockerApiVersion);
        }

        log.info("✅ Testcontainers configuration complete (profile: {})", profile);
    }
}