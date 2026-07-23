package com.projectiq.mcp;

import com.projectiq.mcp.tools.PingTool;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ProjectIQ MCP Server Application.
 */
@SpringBootTest
class ProjectIqMcpServerApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private PingTool pingTool;

    @Test
    @DisplayName("Application context should load successfully")
    void contextLoads() {
        assertNotNull(applicationContext);
    }

    @Test
    @DisplayName("PingTool bean should be registered")
    void pingToolBeanShouldBeRegistered() {
        assertNotNull(pingTool);
    }

    @Test
    @DisplayName("PingTool should return pong")
    void pingToolShouldReturnPong() {
        String result = pingTool.ping("test");
        assertEquals("pong", result);
    }
}