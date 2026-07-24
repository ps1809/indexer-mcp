package com.projectiq.mcp.exception;

import com.projectiq.mcp.dto.ErrorResponse;
import com.projectiq.mcp.monitoring.RequestIdManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for the MCP Server.
 * Provides centralized exception handling for all MCP tool invocations.
 * Produces consistent error responses with request correlation IDs.
 * Logs each failure exactly once to avoid duplicate logging.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles all uncaught exceptions.
     * Logs at ERROR level once and returns a consistent error response.
     *
     * @param ex the exception
     * @return error response with details
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        String requestId = RequestIdManager.getCurrentRequestId();
        logger.error("Unexpected error [requestId={}]: {}", requestId, ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Handles illegal argument exceptions.
     * Logs at WARN level once and returns a consistent error response.
     *
     * @param ex the exception
     * @return error response with details
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        String requestId = RequestIdManager.getCurrentRequestId();
        logger.warn("Invalid argument [requestId={}]: {}", requestId, ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}