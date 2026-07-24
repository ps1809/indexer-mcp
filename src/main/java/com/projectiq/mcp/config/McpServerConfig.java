package com.projectiq.mcp.config;

import com.projectiq.mcp.tools.AnalyzeImpactTool;
import com.projectiq.mcp.tools.AnalyzeTaskTool;
import com.projectiq.mcp.tools.ArchitectureInsightsTool;
import com.projectiq.mcp.tools.AssembleContextTool;
import com.projectiq.mcp.tools.BuildContextTool;
import com.projectiq.mcp.tools.DevelopmentContextTool;
import com.projectiq.mcp.tools.FindClassTool;
import com.projectiq.mcp.tools.FindDependencyTool;
import com.projectiq.mcp.tools.FindMethodTool;
import com.projectiq.mcp.tools.FindRestApiTool;
import com.projectiq.mcp.tools.FindSpringComponentTool;
import com.projectiq.mcp.tools.ListRelatedFilesTool;
import com.projectiq.mcp.tools.PingTool;
import com.projectiq.mcp.tools.PromptContextTool;
import com.projectiq.mcp.tools.RepositoryStatisticsTool;
import com.projectiq.mcp.tools.RepositorySummaryTool;
import com.projectiq.mcp.tools.SearchCodeTool;
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
     * @param repositorySummaryTool the repository summary tool
     * @param repositoryStatisticsTool the repository statistics tool
     * @param searchCodeTool the search code tool
     * @param findSpringComponentTool the find Spring component tool
     * @param findRestApiTool the find REST API tool
     * @param findDependencyTool the find dependency tool
     * @param findClassTool the find class tool
     * @param findMethodTool the find method tool
     * @param listRelatedFilesTool the list related files tool
     * @param buildContextTool the build context tool
     * @param architectureInsightsTool the architecture insights tool
     * @param analyzeTaskTool the task analysis tool
     * @param analyzeImpactTool the impact analysis tool
     * @param assembleContextTool the context assembly tool
     * @param developmentContextTool the development context tool
     * @param promptContextTool the prompt context tool
     * @return ToolCallbackProvider containing all registered tools
     */
    @Bean
    public ToolCallbackProvider toolCallbackProvider(
            PingTool pingTool,
            RepositorySummaryTool repositorySummaryTool,
            RepositoryStatisticsTool repositoryStatisticsTool,
            SearchCodeTool searchCodeTool,
            FindSpringComponentTool findSpringComponentTool,
            FindRestApiTool findRestApiTool,
            FindDependencyTool findDependencyTool,
            FindClassTool findClassTool,
            FindMethodTool findMethodTool,
            ListRelatedFilesTool listRelatedFilesTool,
            BuildContextTool buildContextTool,
            ArchitectureInsightsTool architectureInsightsTool,
            AnalyzeTaskTool analyzeTaskTool,
            AnalyzeImpactTool analyzeImpactTool,
            AssembleContextTool assembleContextTool,
            DevelopmentContextTool developmentContextTool,
            PromptContextTool promptContextTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(
                        pingTool,
                        repositorySummaryTool,
                        repositoryStatisticsTool,
                        searchCodeTool,
                        findSpringComponentTool,
                        findRestApiTool,
                        findDependencyTool,
                        findClassTool,
                        findMethodTool,
                        listRelatedFilesTool,
                        buildContextTool,
                        architectureInsightsTool,
                        analyzeTaskTool,
                        analyzeImpactTool,
                        assembleContextTool,
                        developmentContextTool,
                        promptContextTool
                )
                .build();
    }
}