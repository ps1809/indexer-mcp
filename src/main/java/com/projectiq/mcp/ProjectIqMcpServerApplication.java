package com.projectiq.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * Main application class for ProjectIQ MCP Server.
 * This server exposes ProjectIQ capabilities as MCP tools
 * for AI coding agents like Cline to discover and invoke.
 */
@SpringBootApplication
public class ProjectIqMcpServerApplication {

    private static final Logger logger = LoggerFactory.getLogger(ProjectIqMcpServerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ProjectIqMcpServerApplication.class, args);
    }

    /**
     * Logs application startup completion.
     * Listens for ApplicationReadyEvent to log that the server has started successfully.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("ProjectIQ MCP Server started successfully");
        logger.info("Server is ready to accept MCP tool requests");
    }
}