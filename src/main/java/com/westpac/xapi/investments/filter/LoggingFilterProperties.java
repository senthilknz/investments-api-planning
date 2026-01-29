package com.westpac.xapi.investments.filter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for the request/response logging filter.
 *
 * <p>Properties can be configured in application.yml:
 * <pre>{@code
 * logging:
 *   filter:
 *     enabled: true
 *     structured-logging: false
 *     mask-sensitive-data: true
 *     max-payload-length: 10000
 *     excluded-paths:
 *       - /actuator
 *       - /health
 *       - /swagger-ui
 *       - /v3/api-docs
 * }</pre>
 */
@Component
@ConfigurationProperties(prefix = "logging.filter")
public class LoggingFilterProperties {

    /**
     * Enable or disable the logging filter entirely.
     * Default: true
     */
    private boolean enabled = true;

    /**
     * Use structured logging format (single line with key=value pairs).
     * When false, uses multi-line formatted output with borders.
     * Default: false
     */
    private boolean structuredLogging = false;

    /**
     * Mask sensitive data in request/response bodies.
     * Fields like irdNumber, password, token, etc. will be masked.
     * Default: true
     */
    private boolean maskSensitiveData = true;

    /**
     * Maximum payload length to log. Payloads exceeding this will be truncated.
     * Default: 10000 characters
     */
    private int maxPayloadLength = 10_000;

    /**
     * List of path prefixes to exclude from logging.
     * Useful for health checks, actuator endpoints, and static resources.
     */
    private List<String> excludedPaths = new ArrayList<>(List.of(
            "/actuator",
            "/health",
            "/swagger-ui",
            "/v3/api-docs",
            "/favicon.ico"
    ));

    /**
     * Log level for request/response logging.
     * Default: INFO
     */
    private String logLevel = "INFO";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isStructuredLogging() {
        return structuredLogging;
    }

    public void setStructuredLogging(boolean structuredLogging) {
        this.structuredLogging = structuredLogging;
    }

    public boolean isMaskSensitiveData() {
        return maskSensitiveData;
    }

    public void setMaskSensitiveData(boolean maskSensitiveData) {
        this.maskSensitiveData = maskSensitiveData;
    }

    public int getMaxPayloadLength() {
        return maxPayloadLength;
    }

    public void setMaxPayloadLength(int maxPayloadLength) {
        this.maxPayloadLength = maxPayloadLength;
    }

    public List<String> getExcludedPaths() {
        return excludedPaths;
    }

    public void setExcludedPaths(List<String> excludedPaths) {
        this.excludedPaths = excludedPaths;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }
}
