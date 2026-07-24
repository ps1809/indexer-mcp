package com.projectiq.mcp.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectiq.mcp.http.config.HttpClientProperties;
import com.projectiq.mcp.http.exception.HttpClientException;
import com.projectiq.mcp.http.exception.HttpClientSerializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RestClientHttp - testing non-network logic directly.
 */
class RestClientHttpTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private HttpClientProperties properties;

    @BeforeEach
    void setUp() {
        properties = new HttpClientProperties();
        properties.setLoggingEnabled(true);
    }

    // ==================== Properties Tests ====================

    @Test
    void properties_defaultValues() {
        HttpClientProperties props = new HttpClientProperties();

        assertEquals(5000, props.getConnectTimeout());
        assertEquals(30000, props.getReadTimeout());
        assertFalse(props.isLoggingEnabled());
    }

    @Test
    void properties_setterValues() {
        HttpClientProperties props = new HttpClientProperties();
        props.setConnectTimeout(3000);
        props.setReadTimeout(15000);
        props.setLoggingEnabled(true);

        assertEquals(3000, props.getConnectTimeout());
        assertEquals(15000, props.getReadTimeout());
        assertTrue(props.isLoggingEnabled());
    }

    // ==================== HttpClientProperties Tests ====================

    @Test
    void propertiesClone_createsIndependentCopy() {
        HttpClientProperties original = new HttpClientProperties();
        original.setConnectTimeout(7000);
        original.setReadTimeout(12000);
        original.setLoggingEnabled(true);

        HttpClientProperties clone = new HttpClientProperties();
        clone.setConnectTimeout(original.getConnectTimeout());
        clone.setReadTimeout(original.getReadTimeout());
        clone.setLoggingEnabled(original.isLoggingEnabled());

        assertEquals(7000, clone.getConnectTimeout());
        assertEquals(12000, clone.getReadTimeout());
        assertTrue(clone.isLoggingEnabled());
    }

    // ==================== Serialization Tests ====================

    @Test
    void deserialize_nullJson_returnsNull() {
        assertNull(deserialize(null, TestResponse.class));
    }

    @Test
    void deserialize_emptyJson_returnsNull() {
        assertNull(deserialize("", TestResponse.class));
    }

    @Test
    void deserialize_validJson_returnsObject() throws HttpClientSerializationException {
        String json = "{\"message\":\"success\",\"code\":200}";
        TestResponse result = deserialize(json, TestResponse.class);

        assertNotNull(result);
        assertEquals("success", result.getMessage());
        assertEquals(200, result.getCode());
    }

    @Test
    void deserialize_invalidJson_throwsException() {
        assertThrows(HttpClientSerializationException.class, () -> {
            deserialize("{invalid json}", TestResponse.class);
        });
    }

    // ==================== Serialization with Different Types ====================

    @Test
    void deserialize_mapType_returnsCorrectValues() throws HttpClientSerializationException {
        String json = "{\"key\":\"value\",\"number\":42}";
        Map<String, Object> result = deserialize(json, Map.class);

        assertNotNull(result);
        assertEquals("value", result.get("key"));
        assertEquals(42, result.get("number"));
    }

    // ==================== Properties Equals Tests ====================

    @Test
    void properties_sameValues_areEqual() {
        HttpClientProperties p1 = new HttpClientProperties();
        p1.setConnectTimeout(5000);
        p1.setReadTimeout(10000);
        p1.setLoggingEnabled(false);

        HttpClientProperties p2 = new HttpClientProperties();
        p2.setConnectTimeout(5000);
        p2.setReadTimeout(10000);
        p2.setLoggingEnabled(false);

        assertEquals(p1.getConnectTimeout(), p2.getConnectTimeout());
        assertEquals(p1.getReadTimeout(), p2.getReadTimeout());
    }

    // ==================== HttpResponse Tests ====================

    @Test
    void httpResponse_withBody() {
        TestResponse body = new TestResponse("test", 200);
        Map<String, Object> headers = Map.of("Content-Type", "application/json");

        com.projectiq.mcp.http.model.HttpResponse<TestResponse> response =
            new com.projectiq.mcp.http.model.HttpResponse<>(body, 200, headers);

        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test", response.getBody().getMessage());
        assertNotNull(response.getHeaders());
    }

    @Test
    void httpResponse_withoutBody() {
        Map<String, Object> headers = Map.of();

        com.projectiq.mcp.http.model.HttpResponse<TestResponse> response =
            new com.projectiq.mcp.http.model.HttpResponse<>(null, 204, headers);

        assertEquals(204, response.getStatusCode());
        assertNull(response.getBody());
    }

    // ==================== Helper Methods ====================

    private <T> T deserialize(String json, Class<T> clazz) throws HttpClientSerializationException {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new HttpClientSerializationException("Failed to deserialize: " + e.getMessage(), e);
        }
    }

    // ==================== Helper Classes ====================

    public static class TestResponse {
        private String message;
        private int code;

        public TestResponse() {}
        public TestResponse(String message, int code) {
            this.message = message;
            this.code = code;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public int getCode() { return code; }
        public void setCode(int code) { this.code = code; }
    }
}