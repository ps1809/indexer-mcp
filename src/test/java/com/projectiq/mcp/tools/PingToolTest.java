package com.projectiq.mcp.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PingTool.
 */
class PingToolTest {

    private PingTool pingTool;

    @BeforeEach
    void setUp() {
        pingTool = new PingTool();
    }

    @Test
    @DisplayName("ping should return 'pong' for any message")
    void ping_shouldReturnPong() {
        String result = pingTool.ping("hello");
        assertEquals("pong", result);
    }

    @Test
    @DisplayName("ping should return 'pong' for empty message")
    void ping_shouldReturnPongForEmptyMessage() {
        String result = pingTool.ping("");
        assertEquals("pong", result);
    }

    @Test
    @DisplayName("ping should return 'pong' for null message")
    void ping_shouldReturnPongForNullMessage() {
        String result = pingTool.ping(null);
        assertEquals("pong", result);
    }

    @Test
    @DisplayName("ping should return 'pong' for long message")
    void ping_shouldReturnPongForLongMessage() {
        String longMessage = "a".repeat(10000);
        String result = pingTool.ping(longMessage);
        assertEquals("pong", result);
    }
}