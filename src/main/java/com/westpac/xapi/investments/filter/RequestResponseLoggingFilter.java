package com.westpac.xapi.investments.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * HTTP request/response logging filter that captures and logs JSON payloads.
 *
 * <p>Features:
 * <ul>
 *   <li>Logs request and response bodies for JSON content types</li>
 *   <li>Masks sensitive fields (IRD numbers, passwords, tokens, etc.)</li>
 *   <li>Adds correlation ID for distributed tracing via MDC</li>
 *   <li>Configurable payload size limits</li>
 *   <li>Preserves request/response streams for downstream processing</li>
 * </ul>
 *
 * <p>This filter is ordered with highest precedence to ensure correlation IDs
 * are available throughout the request lifecycle.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String MDC_CORRELATION_ID = "correlationId";
    private static final String MDC_REQUEST_METHOD = "httpMethod";
    private static final String MDC_REQUEST_URI = "requestUri";

    private static final int DEFAULT_MAX_PAYLOAD_LENGTH = 10_000;
    private static final String TRUNCATION_MESSAGE = "...[TRUNCATED]";

    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "irdnumber", "ird_number", "ird-number",
            "password", "passwd", "secret",
            "token", "accesstoken", "access_token", "refreshtoken", "refresh_token",
            "authorization", "apikey", "api_key", "api-key",
            "creditcard", "credit_card", "cardnumber", "card_number",
            "cvv", "cvc", "pin",
            "ssn", "socialsecuritynumber",
            "accountnumber", "account_number"
    );

    private static final Pattern IRD_NUMBER_PATTERN = Pattern.compile("\\d{2,3}-?\\d{3}-?\\d{3}");

    private static final Set<String> JSON_CONTENT_TYPES = Set.of(
            MediaType.APPLICATION_JSON_VALUE,
            "application/json;charset=UTF-8",
            "application/json;charset=utf-8"
    );

    private final ObjectMapper objectMapper;
    private final LoggingFilterProperties properties;

    public RequestResponseLoggingFilter(ObjectMapper objectMapper, LoggingFilterProperties properties) {
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (shouldSkip(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String correlationId = extractOrGenerateCorrelationId(request);

        ContentCachingRequestWrapper wrappedRequest = wrapRequest(request);
        ContentCachingResponseWrapper wrappedResponse = wrapResponse(response);

        Instant startTime = Instant.now();

        try {
            MDC.put(MDC_CORRELATION_ID, correlationId);
            MDC.put(MDC_REQUEST_METHOD, request.getMethod());
            MDC.put(MDC_REQUEST_URI, request.getRequestURI());

            wrappedResponse.setHeader(CORRELATION_ID_HEADER, correlationId);

            filterChain.doFilter(wrappedRequest, wrappedResponse);

        } finally {
            Duration duration = Duration.between(startTime, Instant.now());

            logRequest(wrappedRequest, correlationId);
            logResponse(wrappedResponse, correlationId, duration);

            wrappedResponse.copyBodyToResponse();

            MDC.remove(MDC_CORRELATION_ID);
            MDC.remove(MDC_REQUEST_METHOD);
            MDC.remove(MDC_REQUEST_URI);
        }
    }

    private boolean shouldSkip(HttpServletRequest request) {
        String uri = request.getRequestURI();

        return properties.getExcludedPaths().stream()
                .anyMatch(uri::startsWith);
    }

    private String extractOrGenerateCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        return correlationId;
    }

    private ContentCachingRequestWrapper wrapRequest(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper wrapper) {
            return wrapper;
        }
        return new ContentCachingRequestWrapper(request, properties.getMaxPayloadLength());
    }

    private ContentCachingResponseWrapper wrapResponse(HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper wrapper) {
            return wrapper;
        }
        return new ContentCachingResponseWrapper(response);
    }

    private void logRequest(ContentCachingRequestWrapper request, String correlationId) {
        try {
            String method = request.getMethod();
            String uri = request.getRequestURI();
            String queryString = request.getQueryString();
            String contentType = request.getContentType();

            String fullPath = queryString != null ? uri + "?" + queryString : uri;

            String body = null;
            if (isJsonContentType(contentType) && hasBody(request)) {
                body = extractAndMaskRequestBody(request);
            }

            if (properties.isStructuredLogging()) {
                logStructuredRequest(correlationId, method, fullPath, contentType, body);
            } else {
                logPlainRequest(correlationId, method, fullPath, contentType, body);
            }

        } catch (Exception e) {
            log.warn("Failed to log request: {}", e.getMessage());
        }
    }

    private void logResponse(ContentCachingResponseWrapper response, String correlationId, Duration duration) {
        try {
            int status = response.getStatus();
            String contentType = response.getContentType();

            String body = null;
            if (isJsonContentType(contentType)) {
                body = extractAndMaskResponseBody(response);
            }

            if (properties.isStructuredLogging()) {
                logStructuredResponse(correlationId, status, contentType, body, duration);
            } else {
                logPlainResponse(correlationId, status, contentType, body, duration);
            }

        } catch (Exception e) {
            log.warn("Failed to log response: {}", e.getMessage());
        }
    }

    private boolean isJsonContentType(String contentType) {
        if (contentType == null) {
            return false;
        }
        String lowerContentType = contentType.toLowerCase();
        return lowerContentType.contains("application/json")
                || lowerContentType.contains("application/hal+json")
                || lowerContentType.contains("application/problem+json");
    }

    private boolean hasBody(ContentCachingRequestWrapper request) {
        return request.getContentLength() > 0
                || "POST".equalsIgnoreCase(request.getMethod())
                || "PUT".equalsIgnoreCase(request.getMethod())
                || "PATCH".equalsIgnoreCase(request.getMethod());
    }

    private String extractAndMaskRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length == 0) {
            return null;
        }

        String body = new String(content, StandardCharsets.UTF_8);
        return maskSensitiveData(truncateIfNeeded(body));
    }

    private String extractAndMaskResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length == 0) {
            return null;
        }

        String body = new String(content, StandardCharsets.UTF_8);
        return maskSensitiveData(truncateIfNeeded(body));
    }

    private String truncateIfNeeded(String content) {
        if (content.length() <= properties.getMaxPayloadLength()) {
            return content;
        }
        return content.substring(0, properties.getMaxPayloadLength()) + TRUNCATION_MESSAGE;
    }

    private String maskSensitiveData(String json) {
        if (!properties.isMaskSensitiveData()) {
            return json;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(json);
            maskNode(rootNode);
            return objectMapper.writeValueAsString(rootNode);
        } catch (Exception e) {
            return maskPlainText(json);
        }
    }

    private void maskNode(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.fieldNames().forEachRemaining(fieldName -> {
                JsonNode childNode = objectNode.get(fieldName);

                if (isSensitiveField(fieldName)) {
                    objectNode.put(fieldName, "***MASKED***");
                } else if (childNode.isTextual() && containsIrdNumber(childNode.asText())) {
                    objectNode.put(fieldName, maskIrdNumber(childNode.asText()));
                } else if (childNode.isObject() || childNode.isArray()) {
                    maskNode(childNode);
                }
            });
        } else if (node.isArray()) {
            node.forEach(this::maskNode);
        }
    }

    private boolean isSensitiveField(String fieldName) {
        return SENSITIVE_FIELDS.contains(fieldName.toLowerCase().replace("_", "").replace("-", ""));
    }

    private boolean containsIrdNumber(String value) {
        return IRD_NUMBER_PATTERN.matcher(value).matches();
    }

    private String maskIrdNumber(String irdNumber) {
        if (irdNumber.length() < 4) {
            return "***";
        }
        return "***-***-" + irdNumber.substring(irdNumber.length() - 3);
    }

    private String maskPlainText(String text) {
        String masked = text;
        for (String field : SENSITIVE_FIELDS) {
            masked = masked.replaceAll(
                    "(?i)\"" + field + "\"\\s*:\\s*\"[^\"]*\"",
                    "\"" + field + "\":\"***MASKED***\""
            );
        }
        return IRD_NUMBER_PATTERN.matcher(masked).replaceAll("***-***-***");
    }

    private void logStructuredRequest(String correlationId, String method, String path,
                                       String contentType, String body) {
        log.info("HTTP Request | correlationId={} | method={} | path={} | contentType={} | body={}",
                correlationId, method, path, contentType,
                body != null ? body : "[no body]");
    }

    private void logPlainRequest(String correlationId, String method, String path,
                                  String contentType, String body) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔══════════════════════════════════════════════════════════════\n");
        sb.append("║ INCOMING REQUEST\n");
        sb.append("╠══════════════════════════════════════════════════════════════\n");
        sb.append("║ Correlation ID: ").append(correlationId).append("\n");
        sb.append("║ Method: ").append(method).append("\n");
        sb.append("║ Path: ").append(path).append("\n");
        sb.append("║ Content-Type: ").append(contentType != null ? contentType : "N/A").append("\n");

        if (body != null) {
            sb.append("╠──────────────────────────────────────────────────────────────\n");
            sb.append("║ Body:\n");
            appendFormattedJson(sb, body);
        }

        sb.append("╚══════════════════════════════════════════════════════════════");

        log.info(sb.toString());
    }

    private void logStructuredResponse(String correlationId, int status, String contentType,
                                        String body, Duration duration) {
        String statusText = HttpStatus.valueOf(status).getReasonPhrase();
        log.info("HTTP Response | correlationId={} | status={} {} | contentType={} | duration={}ms | body={}",
                correlationId, status, statusText, contentType, duration.toMillis(),
                body != null ? body : "[no body]");
    }

    private void logPlainResponse(String correlationId, int status, String contentType,
                                   String body, Duration duration) {
        String statusText = HttpStatus.valueOf(status).getReasonPhrase();

        StringBuilder sb = new StringBuilder();
        sb.append("\n╔══════════════════════════════════════════════════════════════\n");
        sb.append("║ OUTGOING RESPONSE\n");
        sb.append("╠══════════════════════════════════════════════════════════════\n");
        sb.append("║ Correlation ID: ").append(correlationId).append("\n");
        sb.append("║ Status: ").append(status).append(" ").append(statusText).append("\n");
        sb.append("║ Content-Type: ").append(contentType != null ? contentType : "N/A").append("\n");
        sb.append("║ Duration: ").append(duration.toMillis()).append("ms\n");

        if (body != null) {
            sb.append("╠──────────────────────────────────────────────────────────────\n");
            sb.append("║ Body:\n");
            appendFormattedJson(sb, body);
        }

        sb.append("╚══════════════════════════════════════════════════════════════");

        log.info(sb.toString());
    }

    private void appendFormattedJson(StringBuilder sb, String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
            for (String line : prettyJson.split("\n")) {
                sb.append("║   ").append(line).append("\n");
            }
        } catch (Exception e) {
            sb.append("║   ").append(json).append("\n");
        }
    }
}
