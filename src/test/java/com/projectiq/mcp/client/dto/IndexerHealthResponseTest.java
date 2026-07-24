package com.projectiq.mcp.client.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IndexerHealthResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldDeserializeFromJson() throws Exception {
        String json = """
                {
                    "status": "UP",
                    "version": "1.0.0"
                }
                """;

        IndexerHealthResponse response = objectMapper.readValue(json, IndexerHealthResponse.class);

        assertEquals("UP", response.getStatus());
        assertEquals("1.0.0", response.getVersion());
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        IndexerHealthResponse response = new IndexerHealthResponse("UP", "1.0.0");

        String json = objectMapper.writeValueAsString(response);

        assertTrue(json.contains("\"status\":\"UP\""));
        assertTrue(json.contains("\"version\":\"1.0.0\""));
    }

    @Test
    void shouldIgnoreUnknownFields() throws Exception {
        String json = """
                {
                    "status": "UP",
                    "version": "1.0.0",
                    "unknownField": "value"
                }
                """;

        IndexerHealthResponse response = objectMapper.readValue(json, IndexerHealthResponse.class);

        assertEquals("UP", response.getStatus());
        assertEquals("1.0.0", response.getVersion());
    }

    @Test
    void shouldReturnHealthyForUpStatus() {
        IndexerHealthResponse response = new IndexerHealthResponse("UP", "1.0.0");
        assertTrue(response.isHealthy());
    }

    @Test
    void shouldReturnHealthyForOkStatus() {
        IndexerHealthResponse response = new IndexerHealthResponse("OK", "1.0.0");
        assertTrue(response.isHealthy());
    }

    @Test
    void shouldReturnNotHealthyForDownStatus() {
        IndexerHealthResponse response = new IndexerHealthResponse("DOWN", "1.0.0");
        assertFalse(response.isHealthy());
    }

    @Test
    void shouldReturnNotHealthyForNullStatus() {
        IndexerHealthResponse response = new IndexerHealthResponse();
        assertFalse(response.isHealthy());
    }

    @Test
    void shouldHaveDefaultConstructor() {
        IndexerHealthResponse response = new IndexerHealthResponse();
        assertNull(response.getStatus());
        assertNull(response.getVersion());
    }

    @Test
    void shouldHaveToString() {
        IndexerHealthResponse response = new IndexerHealthResponse("UP", "1.0.0");
        String str = response.toString();
        assertTrue(str.contains("UP"));
        assertTrue(str.contains("1.0.0"));
    }
}