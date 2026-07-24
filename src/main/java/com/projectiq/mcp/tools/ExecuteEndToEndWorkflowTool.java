package com.projectiq.mcp.tools;

import com.projectiq.mcp.integration.dto.EndToEndWorkflowResponse;
import com.projectiq.mcp.integration.service.IntegrationOrchestratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * MCP Tool that executes the complete end-to-end development workflow,
 * integrating all Phase 3 Intelligent AI Agent Orchestration capabilities.
 *
 * <p>This tool accepts a developer request, repository name, and optional branch,
 * then coordinates all Phase 3 services in sequence:
 * <ol>
 *   <li>Task Analysis</li>
 *   <li>Workflow Orchestration</li>
 *   <li>Context Assembly</li>
 *   <li>Execution Planning</li>
 *   <li>Workflow Validation</li>
 *   <li>Recommendation Generation</li>
 *   <li>Readiness Assessment</li>
 *   <li>Development Session</li>
 *   <li>Agent Handoff</li>
 * </ol>
 */
@Component
public class ExecuteEndToEndWorkflowTool {

    private static final Logger logger = LoggerFactory.getLogger(ExecuteEndToEndWorkflowTool.class);

    private final IntegrationOrchestratorService integrationOrchestratorService;

    public ExecuteEndToEndWorkflowTool(IntegrationOrchestratorService integrationOrchestratorService) {
        this.integrationOrchestratorService = integrationOrchestratorService;
    }

    /**
     * Executes the complete end-to-end development workflow.
     * Coordinates task analysis, workflow orchestration, context assembly,
     * execution planning, validation, recommendations, readiness assessment,
     * session management, and agent handoff.
     *
     * @param request        the developer request/description
     * @param repositoryName the repository name
     * @param branch         the branch name (optional)
     * @return the end-to-end workflow response
     */
    @Tool(description = "Execute the complete end-to-end development workflow. " +
            "Coordinates task analysis, workflow orchestration, context assembly, " +
            "execution planning, validation, recommendations, readiness assessment, " +
            "session management, and agent handoff in a single orchestrated pipeline.")
    public EndToEndWorkflowResponse executeEndToEndWorkflow(
            @ToolParam(description = "The developer request or task description") String request,
            @ToolParam(description = "The repository name") String repositoryName,
            @ToolParam(description = "The branch name (optional)", required = false) String branch) {

        logger.info("ExecuteEndToEndWorkflowTool called with request: {}, repository: {}",
                request, repositoryName);

        if (request == null || request.trim().isEmpty()) {
            EndToEndWorkflowResponse errorResponse = new EndToEndWorkflowResponse();
            errorResponse.setOverallStatus("FAILED");
            errorResponse.addError("Developer request cannot be null or empty");
            return errorResponse;
        }

        if (repositoryName == null || repositoryName.trim().isEmpty()) {
            EndToEndWorkflowResponse errorResponse = new EndToEndWorkflowResponse();
            errorResponse.setOverallStatus("FAILED");
            errorResponse.addError("Repository name cannot be null or empty");
            return errorResponse;
        }

        try {
            return integrationOrchestratorService.executeEndToEndWorkflow(
                    request, repositoryName, branch);
        } catch (IllegalArgumentException e) {
            EndToEndWorkflowResponse errorResponse = new EndToEndWorkflowResponse();
            errorResponse.setOverallStatus("FAILED");
            errorResponse.addError("Invalid argument: " + e.getMessage());
            return errorResponse;
        } catch (Exception e) {
            logger.error("Unexpected error in ExecuteEndToEndWorkflowTool: {}", e.getMessage(), e);
            EndToEndWorkflowResponse errorResponse = new EndToEndWorkflowResponse();
            errorResponse.setOverallStatus("FAILED");
            errorResponse.addError("Unexpected error: " + e.getMessage());
            return errorResponse;
        }
    }
}
