package com.projectiq.mcp.config;

import com.projectiq.mcp.tools.PingTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for MCP Server.
 * Registers MCP tools and configures tool discovery.
 */
@Configuration
public class McpServerConfig {

    /**
     * Registers all MCP tool callbacks for discovery.
     *
     * @param pingTool the ping tool to register
     * @return ToolCallbackProvider containing all registered tools
     */
    @Bean
    public ToolCallbackProvider toolCallbackProvider(PingTool pingTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(pingTool)
                .build();
    }
}