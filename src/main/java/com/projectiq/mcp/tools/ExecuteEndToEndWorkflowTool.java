package com.projectiq.mcp.tools;

import com.projectiq.mcp.integration.dto.EndToEndWorkflowResponse;
import com.projectiq.mcp.integration.service.IntegrationOrchestratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.function.Function;

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
public class ExecuteEndToEndWorkflowTool implements Function<ExecuteEndToEndWorkflowTool.Request, EndToEndWorkflowResponse> {

    private static final Logger logger = LoggerFactory.getLogger(ExecuteEndToEndWorkflowTool.class);

    private final IntegrationOrchestratorService integrationOrchestratorService;

    public ExecuteEndToEndWorkflowTool(IntegrationOrchestratorService integrationOrchestratorService) {
        this.integrationOrchestratorService = integrationOrchestratorService;
    }

    @Override
    public EndToEndWorkflowResponse apply(Request request) {
        logger.info("ExecuteEndToEndWorkflowTool called with request: {}, repository: {}",
                request.request(), request.repositoryName());

        if (request.request() == null || request.request().trim().isEmpty()) {
            EndToEndWorkflowResponse errorResponse = new EndToEndWorkflowResponse();
            errorResponse.setOverallStatus("FAILED");
            errorResponse.addError("Developer request cannot be null or empty");
            return errorResponse;
        }

        if (request.repositoryName() == null || request.repositoryName().trim().isEmpty()) {
            EndToEndWorkflowResponse errorResponse = new EndToEndWorkflowResponse();
            errorResponse.setOverallStatus("FAILED");
            errorResponse.addError("Repository name cannot be null or empty");
            return errorResponse;
        }

        try {
            return integrationOrchestratorService.executeEndToEndWorkflow(
                    request.request(),
                    request.repositoryName(),
                    request.branch());
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

    /**
     * Request record for the ExecuteEndToEndWorkflow tool.
     */
    public record Request(String request, String repositoryName, String branch) {

        public Request {
            if (request == null) {
                throw new IllegalArgumentException("request must not be null");
            }
            if (repositoryName == null) {
                throw new IllegalArgumentException("repositoryName must not be null");
            }
        }

        public Request(String request, String repositoryName) {
            this(request, repositoryName, null);
        }
    }
}