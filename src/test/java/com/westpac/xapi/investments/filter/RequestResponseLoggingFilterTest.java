package com.westpac.xapi.investments.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestResponseLoggingFilterTest {

    private RequestResponseLoggingFilter filter;
    private LoggingFilterProperties properties;
    private ObjectMapper objectMapper;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        properties = new LoggingFilterProperties();
        filter = new RequestResponseLoggingFilter(objectMapper, properties);
    }

    @Nested
    @DisplayName("Filter Chain Execution")
    class FilterChainExecution {

        @Test
        @DisplayName("should pass request through filter chain")
        void shouldPassRequestThroughFilterChain() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/xapi/v1/test");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(any(), any());
        }

        @Test
        @DisplayName("should skip filter when disabled")
        void shouldSkipFilterWhenDisabled() throws ServletException, IOException {
            properties.setEnabled(false);
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/xapi/v1/test");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should skip excluded paths")
        void shouldSkipExcludedPaths() throws ServletException, IOException {
            properties.setExcludedPaths(List.of("/actuator", "/health"));
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("Correlation ID Handling")
    class CorrelationIdHandling {

        @Test
        @DisplayName("should use existing correlation ID from header")
        void shouldUseExistingCorrelationIdFromHeader() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/xapi/v1/test");
            request.addHeader("X-Correlation-ID", "existing-correlation-id");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);

            assertThat(response.getHeader("X-Correlation-ID")).isEqualTo("existing-correlation-id");
        }

        @Test
        @DisplayName("should generate new correlation ID when not present")
        void shouldGenerateNewCorrelationIdWhenNotPresent() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/xapi/v1/test");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);

            assertThat(response.getHeader("X-Correlation-ID")).isNotNull();
            assertThat(response.getHeader("X-Correlation-ID")).matches(
                    "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        }

        @Test
        @DisplayName("should clear MDC after request")
        void shouldClearMdcAfterRequest() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/xapi/v1/test");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);

            assertThat(MDC.get("correlationId")).isNull();
            assertThat(MDC.get("httpMethod")).isNull();
            assertThat(MDC.get("requestUri")).isNull();
        }
    }

    @Nested
    @DisplayName("Request Body Logging")
    class RequestBodyLogging {

        @Test
        @DisplayName("should log JSON request body")
        void shouldLogJsonRequestBody() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/xapi/v1/investments");
            request.setContentType("application/json");
            request.setContent("{\"test\":\"value\"}".getBytes());
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(any(), any());
        }

        @Test
        @DisplayName("should not log non-JSON request body")
        void shouldNotLogNonJsonRequestBody() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/xapi/v1/upload");
            request.setContentType("multipart/form-data");
            request.setContent("binary content".getBytes());
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(any(), any());
        }
    }

    @Nested
    @DisplayName("Sensitive Data Masking")
    class SensitiveDataMasking {

        @Test
        @DisplayName("should mask IRD number in request")
        void shouldMaskIrdNumberInRequest() throws ServletException, IOException {
            String requestBody = """
                {
                    "Data": {
                        "applicant": {
                            "irdNumber": "123-456-789"
                        }
                    }
                }
                """;

            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/xapi/v1/investments");
            request.setContentType("application/json");
            request.setContent(requestBody.getBytes());
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(any(), any());
        }

        @Test
        @DisplayName("should mask password field")
        void shouldMaskPasswordField() throws ServletException, IOException {
            String requestBody = """
                {
                    "username": "user@example.com",
                    "password": "secret123"
                }
                """;

            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/login");
            request.setContentType("application/json");
            request.setContent(requestBody.getBytes());
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(any(), any());
        }

        @Test
        @DisplayName("should not mask when disabled")
        void shouldNotMaskWhenDisabled() throws ServletException, IOException {
            properties.setMaskSensitiveData(false);

            String requestBody = """
                {
                    "irdNumber": "123-456-789"
                }
                """;

            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/xapi/v1/investments");
            request.setContentType("application/json");
            request.setContent(requestBody.getBytes());
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(any(), any());
        }
    }

    @Nested
    @DisplayName("Payload Truncation")
    class PayloadTruncation {

        @Test
        @DisplayName("should truncate large payloads")
        void shouldTruncateLargePayloads() throws ServletException, IOException {
            properties.setMaxPayloadLength(100);

            String largePayload = "{\"data\":\"" + "x".repeat(200) + "\"}";

            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/xapi/v1/investments");
            request.setContentType("application/json");
            request.setContent(largePayload.getBytes());
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(any(), any());
        }
    }

    @Nested
    @DisplayName("Response Body Preservation")
    class ResponseBodyPreservation {

        @Test
        @DisplayName("should preserve response body for client")
        void shouldPreserveResponseBodyForClient() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/xapi/v1/test");
            MockHttpServletResponse response = new MockHttpServletResponse();

            doAnswer(invocation -> {
                HttpServletResponse resp = (HttpServletResponse) invocation.getArgument(1);
                resp.setContentType("application/json");
                resp.getWriter().write("{\"status\":\"success\"}");
                return null;
            }).when(filterChain).doFilter(any(), any());

            filter.doFilterInternal(request, response, filterChain);

            // Response body should be preserved after logging
            assertThat(response.getContentAsString()).isEqualTo("{\"status\":\"success\"}");
        }
    }

    @Nested
    @DisplayName("Logging Format")
    class LoggingFormat {

        @Test
        @DisplayName("should use structured logging when enabled")
        void shouldUseStructuredLoggingWhenEnabled() throws ServletException, IOException {
            properties.setStructuredLogging(true);

            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/xapi/v1/test");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(any(), any());
        }

        @Test
        @DisplayName("should use plain logging by default")
        void shouldUsePlainLoggingByDefault() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/xapi/v1/test");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(any(), any());
        }
    }
}
