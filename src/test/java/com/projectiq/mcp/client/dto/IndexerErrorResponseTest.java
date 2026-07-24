package com.projectiq.mcp.client.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IndexerErrorResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldDeserializeFromJson() throws Exception {
        String json = """
                {
                    "message": "Not Found",
                    "error": "Resource not found",
                    "status": 404,
                    "timestamp": 1700000000000
                }
                """;

        IndexerErrorResponse response = objectMapper.readValue(json, IndexerErrorResponse.class);

        assertEquals("Not Found", response.getMessage());
        assertEquals("Resource not found", response.getError());
        assertEquals(404, response.getStatus());
        assertEquals(1700000000000L, response.getTimestamp());
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        IndexerErrorResponse response = new IndexerErrorResponse("Not Found", "Resource not found", 404, 1700000000000L);

        String json = objectMapper.writeValueAsString(response);

        assertTrue(json.contains("\"message\":\"Not Found\""));
        assertTrue(json.contains("\"error\":\"Resource not found\""));
        assertTrue(json.contains("\"status\":404"));
        assertTrue(json.contains("\"timestamp\":1700000000000"));
    }

    @Test
    void shouldIgnoreUnknownFields() throws Exception {
        String json = """
                {
                    "message": "Error",
                    "error": "Some error",
                    "status": 500,
                    "timestamp": 1700000000000,
                    "extraField": "value"
                }
                """;

        IndexerErrorResponse response = objectMapper.readValue(json, IndexerErrorResponse.class);

        assertEquals("Error", response.getMessage());
        assertEquals(500, response.getStatus());
    }

    @Test
    void shouldHaveDefaultConstructor() {
        IndexerErrorResponse response = new IndexerErrorResponse();
        assertNull(response.getMessage());
        assertNull(response.getError());
        assertEquals(0, response.getStatus());
        assertEquals(0L, response.getTimestamp());
    }

    @Test
    void shouldHaveToString() {
        IndexerErrorResponse response = new IndexerErrorResponse("Error", "Bad Request", 400, 1700000000000L);
        String str = response.toString();
        assertTrue(str.contains("Error"));
        assertTrue(str.contains("Bad Request"));
        assertTrue(str.contains("400"));
    }

    @Test
    void shouldSetAndGetFields() {
        IndexerErrorResponse response = new IndexerErrorResponse();
        response.setMessage("Test message");
        response.setError("Test error");
        response.setStatus(503);
        response.setTimestamp(1234567890L);

        assertEquals("Test message", response.getMessage());
        assertEquals("Test error", response.getError());
        assertEquals(503, response.getStatus());
        assertEquals(1234567890L, response.getTimestamp());
    }
}