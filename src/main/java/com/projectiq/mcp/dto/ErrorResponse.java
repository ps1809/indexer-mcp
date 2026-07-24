package com.projectiq.mcp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.projectiq.mcp.monitoring.RequestIdManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Standard error response DTO for consistent error responses across the application.
 * Includes request ID for correlation and timestamp for debugging.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final String timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String requestId;

    public ErrorResponse(int status, String error, String message) {
        this.timestamp = LocalDateTime.now().format(FORMATTER);
        this.status = status;
        this.error = error;
        this.message = message;
        this.requestId = RequestIdManager.getCurrentRequestId();
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getRequestId() {
        return requestId;
    }
}