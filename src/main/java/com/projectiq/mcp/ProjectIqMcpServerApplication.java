package com.projectiq.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for ProjectIQ MCP Server.
 * This server exposes ProjectIQ capabilities as MCP tools
 * for AI coding agents like Cline to discover and invoke.
 */
@SpringBootApplication
public class ProjectIqMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectIqMcpServerApplication.class, args);
    }
}