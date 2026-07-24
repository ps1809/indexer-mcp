package com.projectiq.mcp.monitoring;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link RequestIdManager}.
 * Verifies request ID generation, retrieval, and cleanup.
 */
class RequestIdManagerTest {

    @AfterEach
    void tearDown() {
        RequestIdManager.clear();
    }

    @Test
    void shouldGenerateUniqueRequestId() {
        String requestId1 = RequestIdManager.generateRequestId();
        String requestId2 = RequestIdManager.generateRequestId();

        assertNotNull(requestId1);
        assertNotNull(requestId2);
        assertNotEquals(requestId1, requestId2);
    }

    @Test
    void shouldSetRequestIdInMdc() {
        String requestId = RequestIdManager.generateRequestId();

        String mdcValue = MDC.get("requestId");
        assertEquals(requestId, mdcValue);
    }

    @Test
    void shouldGetCurrentRequestId() {
        String requestId = RequestIdManager.generateRequestId();

        String currentId = RequestIdManager.getCurrentRequestId();
        assertEquals(requestId, currentId);
    }

    @Test
    void shouldReturnNAWhenNoRequestId() {
        RequestIdManager.clear();

        String currentId = RequestIdManager.getCurrentRequestId();
        assertEquals("N/A", currentId);
    }

    @Test
    void shouldClearRequestIdFromMdc() {
        RequestIdManager.generateRequestId();
        assertNotNull(MDC.get("requestId"));

        RequestIdManager.clear();
        assertNull(MDC.get("requestId"));
    }

    @Test
    void shouldSetSpecificRequestId() {
        String customId = "custom-request-id";
        RequestIdManager.setRequestId(customId);

        assertEquals(customId, RequestIdManager.getCurrentRequestId());
    }

    @Test
    void shouldIgnoreEmptyRequestId() {
        RequestIdManager.clear();
        RequestIdManager.setRequestId("");

        assertEquals("N/A", RequestIdManager.getCurrentRequestId());
    }

    @Test
    void shouldIgnoreNullRequestId() {
        RequestIdManager.clear();
        RequestIdManager.setRequestId(null);

        assertEquals("N/A", RequestIdManager.getCurrentRequestId());
    }
}