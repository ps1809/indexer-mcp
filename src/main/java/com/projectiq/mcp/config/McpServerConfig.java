package com.projectiq.mcp.config;

import com.projectiq.mcp.tools.AnalyzeImpactTool;
import com.projectiq.mcp.tools.AnalyzeTaskTool;
import com.projectiq.mcp.tools.ArchitectureInsightsTool;
import com.projectiq.mcp.tools.AssembleContextTool;
import com.projectiq.mcp.tools.BuildContextPipelineTool;
import com.projectiq.mcp.tools.BuildContextTool;
import com.projectiq.mcp.tools.DevelopmentContextTool;
import com.projectiq.mcp.tools.ExecuteWorkflowTool;
import com.projectiq.mcp.tools.FindClassTool;
import com.projectiq.mcp.tools.FindDependencyTool;
import com.projectiq.mcp.tools.FindMethodTool;
import com.projectiq.mcp.tools.FindRestApiTool;
import com.projectiq.mcp.tools.FindSpringComponentTool;
import com.projectiq.mcp.tools.ImplementationPlanTool;
import com.projectiq.mcp.tools.ListRelatedFilesTool;
import com.projectiq.mcp.tools.PingTool;
import com.projectiq.mcp.tools.PlanExecutionTool;
import com.projectiq.mcp.tools.PromptContextTool;
import com.projectiq.mcp.tools.OrchestrateWorkflowTool;
import com.projectiq.mcp.tools.RefactoringAssistantTool;
import com.projectiq.mcp.tools.RepositoryConventionTool;
import com.projectiq.mcp.tools.RepositoryHealthTool;
import com.projectiq.mcp.tools.RepositoryStatisticsTool;
import com.projectiq.mcp.tools.RepositorySummaryTool;
import com.projectiq.mcp.tools.SearchCodeTool;
import com.projectiq.mcp.tools.TestImpactAnalysisTool;
import com.projectiq.mcp.tools.ValidateWorkflowTool;
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
     * @param repositoryConventionTool the repository convention tool
     * @param repositoryHealthTool the repository health tool
     * @param analyzeImpactTool the impact analysis tool
     * @param assembleContextTool the context assembly tool
     * @param developmentContextTool the development context tool
     * @param promptContextTool the prompt context tool
     * @param implementationPlanTool the implementation plan tool
     * @param testImpactAnalysisTool the test impact analysis tool
     * @param executeWorkflowTool the workflow execution tool
     * @param orchestrateWorkflowTool the workflow orchestration tool
     * @param refactoringAssistantTool the refactoring assistant tool
     * @param buildContextPipelineTool the build context pipeline tool
     * @param planExecutionTool the plan execution tool
     * @param validateWorkflowTool the validate workflow tool
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
            BuildContextPipelineTool buildContextPipelineTool,
            ArchitectureInsightsTool architectureInsightsTool,
            AnalyzeTaskTool analyzeTaskTool,
            AnalyzeImpactTool analyzeImpactTool,
            AssembleContextTool assembleContextTool,
            DevelopmentContextTool developmentContextTool,
            PromptContextTool promptContextTool,
            RepositoryConventionTool repositoryConventionTool,
            RepositoryHealthTool repositoryHealthTool,
            ImplementationPlanTool implementationPlanTool,
            TestImpactAnalysisTool testImpactAnalysisTool,
            RefactoringAssistantTool refactoringAssistantTool,
            ExecuteWorkflowTool executeWorkflowTool,
            OrchestrateWorkflowTool orchestrateWorkflowTool,
            PlanExecutionTool planExecutionTool,
            ValidateWorkflowTool validateWorkflowTool) {
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
                        buildContextPipelineTool,
                        architectureInsightsTool,
                        analyzeTaskTool,
                        analyzeImpactTool,
                        assembleContextTool,
                        developmentContextTool,
                        promptContextTool,
                        repositoryConventionTool,
                        repositoryHealthTool,
                        implementationPlanTool,
                        testImpactAnalysisTool,
                        refactoringAssistantTool,
                        executeWorkflowTool,
                        orchestrateWorkflowTool,
                        planExecutionTool,
                        validateWorkflowTool
                )
                .build();
    }
}