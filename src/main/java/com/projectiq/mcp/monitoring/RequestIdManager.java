package com.projectiq.mcp.monitoring;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * Manages request correlation IDs for MCP requests.
 * Uses SLF4J MDC (Mapped Diagnostic Context) for lightweight,
 * thread-local request ID propagation without external dependencies.
 *
 * <p>Request IDs are automatically included in log entries and
 * propagated through service calls via MDC context.
 */
public final class RequestIdManager {

    private static final String REQUEST_ID_KEY = "requestId";

    private RequestIdManager() {
        // Utility class
    }

    /**
     * Generates a new unique request ID and sets it in the MDC context.
     *
     * @return the generated request ID
     */
    public static String generateRequestId() {
        String requestId = UUID.randomUUID().toString();
        MDC.put(REQUEST_ID_KEY, requestId);
        return requestId;
    }

    /**
     * Retrieves the current request ID from MDC context.
     *
     * @return the current request ID, or "N/A" if none is set
     */
    public static String getCurrentRequestId() {
        String requestId = MDC.get(REQUEST_ID_KEY);
        return requestId != null ? requestId : "N/A";
    }

    /**
     * Clears the request ID from the MDC context.
     * Should be called after request processing is complete.
     */
    public static void clear() {
        MDC.remove(REQUEST_ID_KEY);
    }

    /**
     * Sets a specific request ID in the MDC context.
     * Used for propagating request IDs across thread boundaries.
     *
     * @param requestId the request ID to set
     */
    public static void setRequestId(String requestId) {
        if (requestId != null && !requestId.isEmpty()) {
            MDC.put(REQUEST_ID_KEY, requestId);
        }
    }
}