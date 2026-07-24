package com.projectiq.mcp.client;

import com.projectiq.mcp.client.dto.IndexerHealthResponse;
import com.projectiq.mcp.client.exception.IndexerClientException;
import com.projectiq.mcp.client.exception.IndexerHttpException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class IndexerRestClientImplTest {

    private MockRestServiceServer mockServer;
    private IndexerRestClientImpl indexerRestClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://localhost:8081");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        indexerRestClient = new IndexerRestClientImpl(builder.build());
    }

    @Test
    void checkHealth_shouldReturnHealthResponse() {
        String responseBody = """
                {
                    "status": "UP",
                    "version": "1.0.0"
                }
                """;

        mockServer.expect(requestTo("http://localhost:8081/actuator/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        IndexerHealthResponse response = indexerRestClient.checkHealth();

        assertNotNull(response);
        assertEquals("UP", response.getStatus());
        assertEquals("1.0.0", response.getVersion());
        assertTrue(response.isHealthy());
        mockServer.verify();
    }

    @Test
    void checkHealth_shouldThrowIndexerHttpExceptionOn4xx() {
        mockServer.expect(requestTo("http://localhost:8081/actuator/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withBadRequest().body("Bad Request").contentType(MediaType.TEXT_PLAIN));

        IndexerHttpException exception = assertThrows(IndexerHttpException.class,
                () -> indexerRestClient.checkHealth());

        assertEquals(400, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("400"));
        mockServer.verify();
    }

    @Test
    void checkHealth_shouldThrowIndexerHttpExceptionOn5xx() {
        mockServer.expect(requestTo("http://localhost:8081/actuator/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError().body("Internal Server Error").contentType(MediaType.TEXT_PLAIN));

        IndexerHttpException exception = assertThrows(IndexerHttpException.class,
                () -> indexerRestClient.checkHealth());

        assertEquals(500, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("500"));
        mockServer.verify();
    }

    @Test
    void checkHealth_shouldThrowIndexerHttpExceptionOn404() {
        mockServer.expect(requestTo("http://localhost:8081/actuator/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withResourceNotFound().body("Not Found").contentType(MediaType.TEXT_PLAIN));

        IndexerHttpException exception = assertThrows(IndexerHttpException.class,
                () -> indexerRestClient.checkHealth());

        assertEquals(404, exception.getStatusCode());
        mockServer.verify();
    }

    @Test
    void isReachable_shouldReturnTrueWhenServerResponds() {
        mockServer.expect(requestTo("http://localhost:8081/actuator/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("OK", MediaType.TEXT_PLAIN));

        boolean reachable = indexerRestClient.isReachable();

        assertTrue(reachable);
        mockServer.verify();
    }

    @Test
    void isReachable_shouldReturnFalseOnHttpError() {
        mockServer.expect(requestTo("http://localhost:8081/actuator/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError().body("Error").contentType(MediaType.TEXT_PLAIN));

        boolean reachable = indexerRestClient.isReachable();

        assertFalse(reachable);
        mockServer.verify();
    }

    @Test
    void checkHealth_shouldHandleDeserializationError() {
        String invalidJson = "not valid json {{{";

        mockServer.expect(requestTo("http://localhost:8081/actuator/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(invalidJson, MediaType.APPLICATION_JSON));

        assertThrows(IndexerClientException.class, () -> indexerRestClient.checkHealth());
        mockServer.verify();
    }
}