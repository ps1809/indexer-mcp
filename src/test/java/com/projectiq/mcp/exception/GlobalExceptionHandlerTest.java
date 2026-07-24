package com.projectiq.mcp.exception;

import com.projectiq.mcp.dto.ErrorResponse;
import com.projectiq.mcp.monitoring.RequestIdManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link GlobalExceptionHandler}.
 * Verifies centralized exception handling produces consistent error responses.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        RequestIdManager.generateRequestId();
    }

    @AfterEach
    void tearDown() {
        RequestIdManager.clear();
    }

    @Test
    void shouldHandleGenericException() {
        Exception ex = new RuntimeException("Something went wrong");

        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("Something went wrong", response.getBody().getMessage());
    }

    @Test
    void shouldHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument: name is required");

        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgumentException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Invalid argument: name is required", response.getBody().getMessage());
    }

    @Test
    void shouldIncludeRequestIdInGenericErrorResponse() {
        String requestId = RequestIdManager.getCurrentRequestId();
        Exception ex = new RuntimeException("Error");

        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);

        assertNotNull(response.getBody());
        assertEquals(requestId, response.getBody().getRequestId());
    }

    @Test
    void shouldIncludeRequestIdInIllegalArgumentErrorResponse() {
        String requestId = RequestIdManager.getCurrentRequestId();
        IllegalArgumentException ex = new IllegalArgumentException("Bad input");

        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgumentException(ex);

        assertNotNull(response.getBody());
        assertEquals(requestId, response.getBody().getRequestId());
    }

    @Test
    void shouldHandleNullMessageException() {
        Exception ex = new NullPointerException();

        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
    }

    @Test
    void shouldHandleExceptionWithCause() {
        Exception cause = new IllegalArgumentException("Root cause");
        Exception ex = new RuntimeException("Wrapper exception", cause);

        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Wrapper exception", response.getBody().getMessage());
    }
}