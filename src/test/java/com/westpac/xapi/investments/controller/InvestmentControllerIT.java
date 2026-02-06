package com.westpac.xapi.investments.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test template for investment API endpoints.
 *
 * This test starts the full Spring Boot application with an embedded server
 * and uses WireMock to stub external ESB API calls. The full request flow
 * is tested end-to-end:
 *
 *   Real HTTP → Controller → Service → ESB Client → [WireMock] ESB API
 *
 * Usage:
 *   1. Replace placeholder endpoint paths with your actual API paths
 *   2. Replace placeholder DTOs (use String or Map initially, then switch to real DTOs)
 *   3. Update WireMock stubs to match your ESB contract
 *   4. Add scenarios specific to your business logic
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EnableWireMock({
    @ConfigureWireMock(name = "esb-service", property = "esb.base-url")
})
@DisplayName("Investment Controller Integration Tests")
class InvestmentControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @InjectWireMock("esb-service")
    private WireMockServer esbMock;

    @BeforeEach
    void resetStubs() {
        esbMock.resetAll();
    }

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Correlation-ID", "test-correlation-id");
        // Add any other required headers (e.g., Authorization, X-Customer-ID)
        return headers;
    }

    // -------------------------------------------------------------------------
    // GET endpoint tests
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /xapi/v1/investments/{customerId}/holdings")
    class GetHoldings {

        private static final String ESB_HOLDINGS_PATH = "/esb/v1/accounts/%s/holdings";
        private static final String API_HOLDINGS_PATH = "/xapi/v1/investments/%s/holdings";

        @Test
        @DisplayName("should return holdings when ESB returns valid data")
        void shouldReturnHoldings() {
            String customerId = "CUST001";

            // Stub ESB response
            esbMock.stubFor(get(urlPathEqualTo(ESB_HOLDINGS_PATH.formatted(customerId)))
                .willReturn(okJson("""
                    {
                        "Data": {
                            "holdings": [
                                {
                                    "fundCode": "KS-GROWTH",
                                    "fundName": "KiwiSaver Growth Fund",
                                    "units": 1500.5432,
                                    "unitPrice": 2.3456,
                                    "marketValue": 3519.43
                                },
                                {
                                    "fundCode": "KS-CONSERV",
                                    "fundName": "KiwiSaver Conservative Fund",
                                    "units": 800.0000,
                                    "unitPrice": 1.8901,
                                    "marketValue": 1512.08
                                }
                            ]
                        }
                    }
                    """)));

            // Make real HTTP call
            ResponseEntity<String> response = restTemplate.exchange(
                API_HOLDINGS_PATH.formatted(customerId),
                HttpMethod.GET,
                new HttpEntity<>(defaultHeaders()),
                String.class);

            // Assert response
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            // TODO: Replace String.class with your actual response DTO and assert fields
            // assertThat(response.getBody().getData().getHoldings()).hasSize(2);

            // Verify ESB was called with correct path and headers
            esbMock.verify(getRequestedFor(urlPathEqualTo(ESB_HOLDINGS_PATH.formatted(customerId))));
        }

        @Test
        @DisplayName("should return empty holdings when customer has no investments")
        void shouldReturnEmptyHoldings() {
            String customerId = "CUST_NEW";

            esbMock.stubFor(get(urlPathEqualTo(ESB_HOLDINGS_PATH.formatted(customerId)))
                .willReturn(okJson("""
                    {
                        "Data": {
                            "holdings": []
                        }
                    }
                    """)));

            ResponseEntity<String> response = restTemplate.exchange(
                API_HOLDINGS_PATH.formatted(customerId),
                HttpMethod.GET,
                new HttpEntity<>(defaultHeaders()),
                String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            // TODO: Assert empty holdings list in response DTO
        }

        @Test
        @DisplayName("should return 404 when customer not found in ESB")
        void shouldReturn404WhenCustomerNotFound() {
            String customerId = "UNKNOWN";

            esbMock.stubFor(get(urlPathEqualTo(ESB_HOLDINGS_PATH.formatted(customerId)))
                .willReturn(aResponse()
                    .withStatus(404)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                        {
                            "error": "Customer not found",
                            "code": "CUSTOMER_NOT_FOUND"
                        }
                        """)));

            ResponseEntity<String> response = restTemplate.exchange(
                API_HOLDINGS_PATH.formatted(customerId),
                HttpMethod.GET,
                new HttpEntity<>(defaultHeaders()),
                String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("should return 502 when ESB returns 500")
        void shouldReturn502WhenEsbFails() {
            String customerId = "CUST001";

            esbMock.stubFor(get(urlPathEqualTo(ESB_HOLDINGS_PATH.formatted(customerId)))
                .willReturn(serverError()
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                        {
                            "error": "Internal ESB error"
                        }
                        """)));

            ResponseEntity<String> response = restTemplate.exchange(
                API_HOLDINGS_PATH.formatted(customerId),
                HttpMethod.GET,
                new HttpEntity<>(defaultHeaders()),
                String.class);

            // Service should translate ESB 500 → 502 Bad Gateway (upstream failure)
            assertThat(response.getStatusCode()).isIn(
                HttpStatus.BAD_GATEWAY,
                HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("should return 504 when ESB times out")
        void shouldReturn504WhenEsbTimesOut() {
            String customerId = "CUST001";

            esbMock.stubFor(get(urlPathEqualTo(ESB_HOLDINGS_PATH.formatted(customerId)))
                .willReturn(ok()
                    .withFixedDelay(10_000)));  // exceed configured timeout

            ResponseEntity<String> response = restTemplate.exchange(
                API_HOLDINGS_PATH.formatted(customerId),
                HttpMethod.GET,
                new HttpEntity<>(defaultHeaders()),
                String.class);

            assertThat(response.getStatusCode()).isIn(
                HttpStatus.GATEWAY_TIMEOUT,
                HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("should return 502 when ESB returns malformed JSON")
        void shouldReturn502WhenEsbReturnsMalformedJson() {
            String customerId = "CUST001";

            esbMock.stubFor(get(urlPathEqualTo(ESB_HOLDINGS_PATH.formatted(customerId)))
                .willReturn(ok()
                    .withHeader("Content-Type", "application/json")
                    .withBody("this is not json")));

            ResponseEntity<String> response = restTemplate.exchange(
                API_HOLDINGS_PATH.formatted(customerId),
                HttpMethod.GET,
                new HttpEntity<>(defaultHeaders()),
                String.class);

            assertThat(response.getStatusCode()).isIn(
                HttpStatus.BAD_GATEWAY,
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // -------------------------------------------------------------------------
    // POST endpoint tests
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /xapi/v1/investments/{customerId}/switch")
    class SwitchFund {

        private static final String ESB_SWITCH_PATH = "/esb/v1/accounts/%s/switch";
        private static final String API_SWITCH_PATH = "/xapi/v1/investments/%s/switch";

        @Test
        @DisplayName("should successfully switch funds")
        void shouldSwitchFunds() {
            String customerId = "CUST001";

            String requestBody = """
                {
                    "fromFundCode": "KS-CONSERV",
                    "toFundCode": "KS-GROWTH",
                    "switchType": "FULL",
                    "amount": null
                }
                """;

            // Stub ESB success response
            esbMock.stubFor(post(urlPathEqualTo(ESB_SWITCH_PATH.formatted(customerId)))
                .willReturn(okJson("""
                    {
                        "Data": {
                            "switchId": "SW-20240101-001",
                            "status": "ACCEPTED",
                            "estimatedCompletionDate": "2024-01-05"
                        }
                    }
                    """)));

            HttpEntity<String> request = new HttpEntity<>(requestBody, defaultHeaders());

            ResponseEntity<String> response = restTemplate.exchange(
                API_SWITCH_PATH.formatted(customerId),
                HttpMethod.POST,
                request,
                String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            // TODO: Assert response DTO fields

            // Verify ESB received the correct request body
            esbMock.verify(postRequestedFor(urlPathEqualTo(ESB_SWITCH_PATH.formatted(customerId)))
                .withRequestBody(containing("KS-GROWTH")));
        }

        @Test
        @DisplayName("should return 400 for invalid switch request")
        void shouldReturn400ForInvalidRequest() {
            String customerId = "CUST001";

            // Missing required fields
            String invalidBody = """
                {
                    "fromFundCode": null,
                    "toFundCode": ""
                }
                """;

            HttpEntity<String> request = new HttpEntity<>(invalidBody, defaultHeaders());

            ResponseEntity<String> response = restTemplate.exchange(
                API_SWITCH_PATH.formatted(customerId),
                HttpMethod.POST,
                request,
                String.class);

            // Bean validation should reject before reaching ESB
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

            // ESB should NOT have been called
            esbMock.verify(0, postRequestedFor(urlPathAnyMatching(".*")));
        }

        @Test
        @DisplayName("should return 409 when ESB rejects duplicate switch")
        void shouldReturn409ForDuplicateSwitch() {
            String customerId = "CUST001";

            String requestBody = """
                {
                    "fromFundCode": "KS-CONSERV",
                    "toFundCode": "KS-GROWTH",
                    "switchType": "FULL"
                }
                """;

            esbMock.stubFor(post(urlPathEqualTo(ESB_SWITCH_PATH.formatted(customerId)))
                .willReturn(aResponse()
                    .withStatus(409)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                        {
                            "error": "Switch already in progress",
                            "code": "DUPLICATE_SWITCH"
                        }
                        """)));

            HttpEntity<String> request = new HttpEntity<>(requestBody, defaultHeaders());

            ResponseEntity<String> response = restTemplate.exchange(
                API_SWITCH_PATH.formatted(customerId),
                HttpMethod.POST,
                request,
                String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }

        @Test
        @DisplayName("should return 403 when ESB rejects unauthorized operation")
        void shouldReturn403WhenUnauthorized() {
            String customerId = "CUST001";

            String requestBody = """
                {
                    "fromFundCode": "KS-CONSERV",
                    "toFundCode": "KS-GROWTH",
                    "switchType": "FULL"
                }
                """;

            esbMock.stubFor(post(urlPathEqualTo(ESB_SWITCH_PATH.formatted(customerId)))
                .willReturn(aResponse()
                    .withStatus(403)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                        {
                            "error": "Account locked for switching",
                            "code": "OPERATION_NOT_PERMITTED"
                        }
                        """)));

            HttpEntity<String> request = new HttpEntity<>(requestBody, defaultHeaders());

            ResponseEntity<String> response = restTemplate.exchange(
                API_SWITCH_PATH.formatted(customerId),
                HttpMethod.POST,
                request,
                String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }
    }

    // -------------------------------------------------------------------------
    // Cross-cutting concerns
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Cross-Cutting Concerns")
    class CrossCuttingConcerns {

        @Test
        @DisplayName("should propagate correlation ID to ESB")
        void shouldPropagateCorrelationId() {
            String customerId = "CUST001";
            String correlationId = "test-corr-12345";

            esbMock.stubFor(get(urlPathMatching("/esb/v1/accounts/.*/holdings"))
                .willReturn(okJson("""
                    {"Data": {"holdings": []}}
                    """)));

            HttpHeaders headers = defaultHeaders();
            headers.set("X-Correlation-ID", correlationId);

            restTemplate.exchange(
                "/xapi/v1/investments/%s/holdings".formatted(customerId),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

            // Verify correlation ID was forwarded to ESB
            esbMock.verify(getRequestedFor(urlPathMatching("/esb/v1/accounts/.*/holdings"))
                .withHeader("X-Correlation-ID", equalTo(correlationId)));
        }

        @Test
        @DisplayName("should return correlation ID in response headers")
        void shouldReturnCorrelationIdInResponse() {
            esbMock.stubFor(get(urlPathMatching("/esb/v1/accounts/.*/holdings"))
                .willReturn(okJson("""
                    {"Data": {"holdings": []}}
                    """)));

            HttpHeaders headers = defaultHeaders();
            headers.set("X-Correlation-ID", "my-correlation-id");

            ResponseEntity<String> response = restTemplate.exchange(
                "/xapi/v1/investments/CUST001/holdings",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

            assertThat(response.getHeaders().getFirst("X-Correlation-ID"))
                .isEqualTo("my-correlation-id");
        }

        @Test
        @DisplayName("should return standard error envelope on failure")
        void shouldReturnStandardErrorEnvelope() {
            esbMock.stubFor(get(urlPathMatching("/esb/v1/accounts/.*/holdings"))
                .willReturn(serverError()));

            ResponseEntity<String> response = restTemplate.exchange(
                "/xapi/v1/investments/CUST001/holdings",
                HttpMethod.GET,
                new HttpEntity<>(defaultHeaders()),
                String.class);

            assertThat(response.getBody()).isNotNull();
            // TODO: Assert error envelope structure matches your API standard:
            // {
            //   "Meta": { "status": 500, "correlationId": "..." },
            //   "Errors": [{ "code": "...", "message": "..." }]
            // }
        }
    }
}
