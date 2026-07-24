package com.projectiq.mcp.tools;

import com.projectiq.mcp.pipeline.dto.ContextPackage;
import com.projectiq.mcp.pipeline.service.IntelligentContextPipelineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP Tool for building an intelligent context pipeline.
 * Accepts workflow information, assembles optimized context from all
 * available repository intelligence sources, and returns a final
 * AI-ready context package.
 */
@Component
public class BuildContextPipelineTool {

    private static final Logger logger = LoggerFactory.getLogger(BuildContextPipelineTool.class);

    private final IntelligentContextPipelineService pipelineService;

    public BuildContextPipelineTool(IntelligentContextPipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    /**
     * Builds an intelligent context pipeline by gathering, prioritizing,
     * filtering, and assembling repository intelligence into an optimized
     * AI-ready context package.
     *
     * @param workflowSummary a summary of the workflow being executed (required)
     * @param workflowType    the type of workflow (e.g., "analysis", "implementation", "refactoring") (required)
     * @param repositoryName  the name of the repository to analyze (required)
     * @param branch          the git branch to analyze (optional, defaults to "main")
     * @param taskDescription a description of the specific development task (required)
     * @return a formatted string containing the AI-ready context package
     */
    @Tool(description = """
            Builds an intelligent context pipeline by gathering, prioritizing,
            filtering, and assembling repository intelligence from all available
            analysis services into an optimized AI-ready context package.
            
            Returns a structured context package including:
            - Workflow summary
            - Repository summary
            - Relevant classes and methods
            - Related APIs and dependencies
            - Configuration and risks
            - Repository conventions
            - Architecture insights
            - Suggested implementation focus
            
            All context is deduplicated, prioritized (HIGH/MEDIUM/LOW),
            and deterministically ordered for efficient AI consumption.
            """)
    public String buildContextPipeline(
            String workflowSummary,
            String workflowType,
            String repositoryName,
            String branch,
            String taskDescription) {

        try {
            if (workflowSummary == null || workflowSummary.isEmpty()) {
                return "Error [INVALID_ARGUMENT]: Workflow summary is required";
            }

            if (workflowType == null || workflowType.isEmpty()) {
                return "Error [INVALID_ARGUMENT]: Workflow type is required";
            }

            if (repositoryName == null || repositoryName.isEmpty()) {
                return "Error [INVALID_ARGUMENT]: Repository name is required";
            }

            if (taskDescription == null || taskDescription.isEmpty()) {
                return "Error [INVALID_ARGUMENT]: Task description is required";
            }

            String effectiveBranch = (branch != null && !branch.isEmpty()) ? branch : "main";

            logger.info("Executing build_context_pipeline tool for workflow: {} type: {} repo: {}",
                    workflowSummary, workflowType, repositoryName);

            ContextPackage contextPackage = pipelineService.buildContextPipeline(
                    workflowSummary, workflowType, repositoryName, effectiveBranch, taskDescription);

            return formatContextPackage(contextPackage);

        } catch (Exception e) {
            logger.error("Unexpected error in build_context_pipeline: {}", e.getMessage(), e);
            return "Error [INTERNAL_ERROR]: " + e.getMessage();
        }
    }

    /**
     * Formats the context package into a human-readable string.
     */
    private String formatContextPackage(ContextPackage pkg) {
        StringBuilder sb = new StringBuilder();

        sb.append("Context Pipeline Package\n");
        sb.append("========================\n\n");

        // Workflow Summary
        sb.append("Workflow Summary\n");
        sb.append("----------------\n");
        sb.append("  ").append(pkg.getWorkflowSummary()).append("\n\n");

        // Repository Summary
        sb.append("Repository Summary\n");
        sb.append("------------------\n");
        sb.append("  ").append(pkg.getRepositorySummary()).append("\n\n");

        // Relevant Classes
        appendListSection(sb, "Relevant Classes", pkg.getRelevantClasses());

        // Relevant Methods
        appendListSection(sb, "Relevant Methods", pkg.getRelevantMethods());

        // Related APIs
        appendListSection(sb, "Related APIs", pkg.getRelatedApis());

        // Dependencies
        appendListSection(sb, "Dependencies", pkg.getDependencies());

        // Configuration
        appendListSection(sb, "Configuration", pkg.getConfiguration());

        // Risks
        appendListSection(sb, "Risks", pkg.getRisks());

        // Conventions
        appendListSection(sb, "Conventions", pkg.getConventions());

        // Architecture Insights
        appendListSection(sb, "Architecture Insights", pkg.getArchitectureInsights());

        // Suggested Implementation Focus
        sb.append("Suggested Implementation Focus\n");
        sb.append("-----------------------------\n");
        sb.append("  ").append(pkg.getSuggestedImplementationFocus() != null
                ? pkg.getSuggestedImplementationFocus() : "Not available").append("\n\n");

        // Metadata
        sb.append("Package Metadata\n");
        sb.append("----------------\n");
        sb.append("  Total Context Items: ").append(pkg.getTotalContextItems()).append("\n");
        sb.append("  High Priority: ").append(pkg.getHighPriorityCount()).append("\n");
        sb.append("  Medium Priority: ").append(pkg.getMediumPriorityCount()).append("\n");
        sb.append("  Low Priority: ").append(pkg.getLowPriorityCount()).append("\n");
        sb.append("  Processing Time: ").append(pkg.getProcessingTimeMillis()).append("ms\n");

        // Warnings
        if (pkg.getWarnings() != null && !pkg.getWarnings().isEmpty()) {
            sb.append("\nWarnings\n");
            sb.append("--------\n");
            for (String warning : pkg.getWarnings()) {
                sb.append("  - ").append(warning).append("\n");
            }
        }

        return sb.toString();
    }

    private void appendListSection(StringBuilder sb, String title, java.util.List<String> items) {
        sb.append(title).append("\n");
        sb.append(new String(new char[title.length()]).replace("\0", "-")).append("\n");
        if (items != null && !items.isEmpty()) {
            for (int i = 0; i < Math.min(items.size(), 20); i++) {
                sb.append("  [").append(i + 1).append("] ").append(items.get(i)).append("\n");
            }
            if (items.size() > 20) {
                sb.append("  ... and ").append(items.size() - 20).append(" more\n");
            }
        } else {
            sb.append("  None\n");
        }
        sb.append("\n");
    }
}