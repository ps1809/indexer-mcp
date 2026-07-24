package com.projectiq.mcp.dto;

import com.projectiq.mcp.monitoring.RequestIdManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ErrorResponse}.
 * Verifies error response creation and consistency.
 */
class ErrorResponseTest {

    @AfterEach
    void tearDown() {
        RequestIdManager.clear();
    }

    @Test
    void shouldCreateErrorResponseWithAllFields() {
        RequestIdManager.generateRequestId();

        ErrorResponse errorResponse = new ErrorResponse(500, "Internal Server Error", "Something went wrong");

        assertEquals(500, errorResponse.getStatus());
        assertEquals("Internal Server Error", errorResponse.getError());
        assertEquals("Something went wrong", errorResponse.getMessage());
        assertNotNull(errorResponse.getTimestamp());
        assertNotNull(errorResponse.getRequestId());
    }

    @Test
    void shouldIncludeRequestIdInErrorResponse() {
        String requestId = RequestIdManager.generateRequestId();

        ErrorResponse errorResponse = new ErrorResponse(400, "Bad Request", "Invalid input");

        assertEquals(requestId, errorResponse.getRequestId());
    }

    @Test
    void shouldHandleBadRequestError() {
        RequestIdManager.generateRequestId();

        ErrorResponse errorResponse = new ErrorResponse(400, "Bad Request", "Invalid argument");

        assertEquals(400, errorResponse.getStatus());
        assertEquals("Bad Request", errorResponse.getError());
    }

    @Test
    void shouldHandleNotFoundError() {
        RequestIdManager.generateRequestId();

        ErrorResponse errorResponse = new ErrorResponse(404, "Not Found", "Resource not found");

        assertEquals(404, errorResponse.getStatus());
        assertEquals("Not Found", errorResponse.getError());
    }

    @Test
    void shouldHandleServerError() {
        RequestIdManager.generateRequestId();

        ErrorResponse errorResponse = new ErrorResponse(500, "Internal Server Error", "Unexpected error");

        assertEquals(500, errorResponse.getStatus());
        assertEquals("Internal Server Error", errorResponse.getError());
    }

    @Test
    void shouldHaveValidTimestamp() {
        RequestIdManager.generateRequestId();

        ErrorResponse errorResponse = new ErrorResponse(500, "Error", "Message");

        assertNotNull(errorResponse.getTimestamp());
        assertFalse(errorResponse.getTimestamp().isEmpty());
    }

    @Test
    void shouldReturnNAForRequestIdWhenNotSet() {
        RequestIdManager.clear();

        ErrorResponse errorResponse = new ErrorResponse(500, "Error", "Message");

        assertEquals("N/A", errorResponse.getRequestId());
    }
}