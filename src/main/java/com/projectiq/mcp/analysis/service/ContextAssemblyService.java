package com.projectiq.mcp.analysis.service;

import com.projectiq.mcp.analysis.dto.ContextAssemblyResponse;
import com.projectiq.mcp.analysis.dto.TaskAnalysisResponse;
import com.projectiq.mcp.analysis.dto.TaskAnalysisResponse.ExecutionStep;
import com.projectiq.mcp.client.dto.BuildContextRequest;
import com.projectiq.mcp.client.dto.DevelopmentContext;
import com.projectiq.mcp.client.dto.PromptContext;
import com.projectiq.mcp.client.dto.RepositoryContext;
import com.projectiq.mcp.client.service.DevelopmentContextService;
import com.projectiq.mcp.client.service.PromptContextService;
import com.projectiq.mcp.client.service.RepositoryContextBuilderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Service that automatically collects and assembles repository context based
 * on an execution plan produced by {@link TaskAnalysisService}. Orchestrates
 * existing MCP services to build a complete development context while
 * minimizing redundant repository queries.
 *
 * <p>Execution is deterministic: tools are executed sequentially, duplicates
 * are skipped, and non-critical failures are tolerated.</p>
 */
@Service
public class ContextAssemblyService {

    private static final Logger logger = LoggerFactory.getLogger(ContextAssemblyService.class);

    private final TaskAnalysisService taskAnalysisService;
    private final RepositoryContextBuilderService contextBuilderService;
    private final DevelopmentContextService developmentContextService;
    private final PromptContextService promptContextService;

    // Tools that require indexer data (resolved via RepositoryContextBuilderService)
    private static final Set<String> INDEXER_TOOLS = Set.of(
            "repository_summary",
            "repository_statistics",
            "search_code",
            "find_spring_component",
            "find_rest_api",
            "find_dependency",
            "find_class",
            "find_method",
            "list_related_files"
    );

    public ContextAssemblyService(
            TaskAnalysisService taskAnalysisService,
            RepositoryContextBuilderService contextBuilderService,
            DevelopmentContextService developmentContextService,
            PromptContextService promptContextService) {
        this.taskAnalysisService = taskAnalysisService;
        this.contextBuilderService = contextBuilderService;
        this.developmentContextService = developmentContextService;
        this.promptContextService = promptContextService;
    }

    /**
     * Assembles a complete development context for the given natural language
     * task. Invokes task analysis, executes the generated execution plan,
     * and consolidates all retrieved information.
     *
     * @param task          the natural language development task
     * @param repositoryName the repository name
     * @param branch         the git branch (optional, defaults to "main")
     * @return the fully assembled context response
     */
    public ContextAssemblyResponse assembleContext(
            String task, String repositoryName, String branch) {
        long startTime = System.currentTimeMillis();
        logger.info("Assembling context for task: {} in repository: {}", task, repositoryName);

        ContextAssemblyResponse response = new ContextAssemblyResponse();
        response.setOriginalTask(task);

        // Step 1: Analyze the task to get the execution plan
        TaskAnalysisResponse analysis = taskAnalysisService.analyze(task);
        response.setTaskAnalysis(analysis);
        response.setExecutionPlan(analysis.getExecutionPlan());

        List<ExecutionStep> plan = analysis.getExecutionPlan();
        if (plan == null || plan.isEmpty()) {
            response.setExecutionSummary("No execution steps generated. Task could not be analyzed.");
            response.setTotalExecutionTimeMillis(System.currentTimeMillis() - startTime);
            return response;
        }

        // Step 2: Extract unique tool names from the plan (preserving order)
        List<String> uniqueToolNames = extractUniqueToolNames(plan);

        // Step 3: Collect tools that need indexer data and those that don't
        boolean needsIndexerData = uniqueToolNames.stream().anyMatch(INDEXER_TOOLS::contains);
        boolean needsDevelopmentContext = uniqueToolNames.contains("development_context")
                || uniqueToolNames.contains("prompt_context");
        boolean needsPromptContext = uniqueToolNames.contains("prompt_context");

        // Step 4: Execute tools sequentially, skipping duplicates
        RepositoryContext repositoryContext = null;
        DevelopmentContext developmentContext = null;
        PromptContext promptContext = null;

        Set<String> processedTools = new LinkedHashSet<>();

        for (String toolName : uniqueToolNames) {
            if (processedTools.contains(toolName)) {
                response.addSkippedTool(toolName + " (duplicate)");
                logger.debug("Skipping duplicate tool: {}", toolName);
                continue;
            }

            try {
                boolean executed = executeTool(
                        toolName, task, repositoryName, branch,
                        needsIndexerData, needsDevelopmentContext, needsPromptContext,
                        response, processedTools
                );
                if (executed) {
                    processedTools.add(toolName);
                }
            } catch (Exception e) {
                logger.warn("Tool execution failed for {}: {}", toolName, e.getMessage());
                response.addFailedTool(toolName + ": " + e.getMessage());
                // Continue with next tool (non-critical failure)
            }
        }

        // Step 5: Retrieve assembled contexts (they may have been created during execution)
        if (needsIndexerData && response.getRepositoryContext() == null) {
            // Only build if not already set during execution
            try {
                repositoryContext = buildIndexerContext(task, repositoryName, branch);
                response.setRepositoryContext(repositoryContext);
                markIndexerToolsAsExecuted(uniqueToolNames, response, processedTools);
            } catch (Exception e) {
                logger.warn("Failed to build indexer context: {}", e.getMessage());
                for (String tool : INDEXER_TOOLS) {
                    if (uniqueToolNames.contains(tool) && !processedTools.contains(tool)) {
                        response.addFailedTool(tool + ": " + e.getMessage());
                    }
                }
            }
        }

        // Ensure repository context is available for development context
        if (repositoryContext == null) {
            repositoryContext = response.getRepositoryContext();
        }

        if (needsDevelopmentContext && response.getDevelopmentContext() == null && repositoryContext != null) {
            try {
                BuildContextRequest request = createRequest(task, repositoryName, branch);
                developmentContext = developmentContextService.createDevelopmentContext(request);
                response.setDevelopmentContext(developmentContext);
                if (!processedTools.contains("development_context")) {
                    response.addExecutedTool("development_context");
                    processedTools.add("development_context");
                }
            } catch (Exception e) {
                logger.warn("Failed to build development context: {}", e.getMessage());
                response.addFailedTool("development_context: " + e.getMessage());
            }
        }

        if (needsPromptContext && response.getDevelopmentContext() != null) {
            try {
                promptContext = promptContextService.createPromptContext(response.getDevelopmentContext());
                // Prompt context is included implicitly in the response
                if (!processedTools.contains("prompt_context")) {
                    response.addExecutedTool("prompt_context");
                    processedTools.add("prompt_context");
                }
            } catch (Exception e) {
                logger.warn("Failed to build prompt context: {}", e.getMessage());
                response.addFailedTool("prompt_context: " + e.getMessage());
            }
        }

        // Step 6: Generate execution summary
        long executionTime = System.currentTimeMillis() - startTime;
        response.setTotalExecutionTimeMillis(executionTime);
        response.setExecutionSummary(generateSummary(
                response.getExecutedTools(),
                response.getSkippedTools(),
                response.getFailedTools(),
                executionTime
        ));

        logger.info("Context assembly complete: executed={}, skipped={}, failed={}, time={}ms",
                response.getExecutedTools().size(),
                response.getSkippedTools().size(),
                response.getFailedTools().size(),
                executionTime);

        return response;
    }

    /**
     * Executes a single tool from the plan, coordinating with the bulk
     * context builders as needed.
     *
     * @return true if the tool was executed, false if it was deferred to bulk processing
     */
    private boolean executeTool(
            String toolName, String task, String repositoryName, String branch,
            boolean needsIndexerData, boolean needsDevelopmentContext, boolean needsPromptContext,
            ContextAssemblyResponse response, Set<String> processedTools) {

        if (INDEXER_TOOLS.contains(toolName)) {
            // Defer indexer tools to bulk execution
            return false;
        }

        if ("development_context".equals(toolName) && needsDevelopmentContext) {
            // Will be handled after indexer data is collected
            return false;
        }

        if ("prompt_context".equals(toolName) && needsPromptContext) {
            // Will be handled after development context is built
            return false;
        }

        // Unknown tool - record and skip
        logger.warn("Unknown tool in execution plan: {}", toolName);
        response.addSkippedTool(toolName + " (unknown)");
        return false;
    }

    /**
     * Executes the indexer data fetch once and marks all indexer tools as executed.
     */
    private RepositoryContext buildIndexerContext(String task, String repositoryName, String branch) {
        BuildContextRequest request = createRequest(task, repositoryName, branch);
        return contextBuilderService.buildContext(request);
    }

    /**
     * Marks all indexer tools as executed in the response.
     */
    private void markIndexerToolsAsExecuted(
            List<String> toolNames, ContextAssemblyResponse response, Set<String> processedTools) {
        for (String tool : INDEXER_TOOLS) {
            if (toolNames.contains(tool) && !processedTools.contains(tool)) {
                response.addExecutedTool(tool);
                processedTools.add(tool);
            }
        }
    }

    /**
     * Extracts unique tool names from the execution plan, preserving order.
     */
    private List<String> extractUniqueToolNames(List<ExecutionStep> plan) {
        Set<String> seen = new LinkedHashSet<>();
        for (ExecutionStep step : plan) {
            seen.add(step.getToolName());
        }
        return List.copyOf(seen);
    }

    /**
     * Creates a BuildContextRequest from the task parameters.
     */
    private BuildContextRequest createRequest(String task, String repositoryName, String branch) {
        String effectiveBranch = (branch != null && !branch.isEmpty()) ? branch : "main";
        BuildContextRequest request = new BuildContextRequest();
        request.setTask(task);
        request.setRepositoryName(repositoryName);
        request.setBranch(effectiveBranch);
        return request;
    }

    /**
     * Generates a deterministic execution summary.
     */
    private String generateSummary(
            List<String> executed, List<String> skipped,
            List<String> failed, long executionTimeMillis) {
        StringBuilder sb = new StringBuilder();
        sb.append("Context assembly completed in ").append(executionTimeMillis).append("ms. ");
        sb.append(executed.size()).append(" tools executed");
        if (!skipped.isEmpty()) {
            sb.append(", ").append(skipped.size()).append(" skipped");
        }
        if (!failed.isEmpty()) {
            sb.append(", ").append(failed.size()).append(" failed");
        }
        sb.append(".");
        return sb.toString();
    }
}