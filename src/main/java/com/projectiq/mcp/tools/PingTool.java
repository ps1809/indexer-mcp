package com.projectiq.mcp.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Simple MCP Tool for verifying MCP Server connectivity.
 * This tool allows AI agents like Cline to discover and invoke
 * the MCP server to verify it is working correctly.
 */
@Component
public class PingTool {

    /**
     * Ping tool to verify MCP server connectivity.
     *
     * @param message the message to send (can be any string)
     * @return "pong" to confirm the server is responsive
     */
    @Tool(description = "Verify MCP server connectivity. Returns 'pong' to confirm the server is responsive.")
    public String ping(@ToolParam(description = "A message to send to the server") String message) {
        return "pong";
    }
}