package com.projectiq.mcp.tools;

import com.projectiq.mcp.analysis.dto.DependencyChangePredictionResponse;
import com.projectiq.mcp.analysis.service.DependencyChangePredictionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * MCP Tool for predicting the downstream impact of dependency changes.
 * Allows AI coding agents to evaluate the repository-wide impact of
 * introducing, removing, or modifying project dependencies before
 * any implementation begins.
 *
 * <p>This tool is backed by {@link DependencyChangePredictionService} and
 * produces deterministic dependency impact reports without AI/LLM reasoning,
 * repository modification, or git operations.</p>
 */
@Component
public class PredictDependencyChangeTool {

    private static final Logger logger = LoggerFactory.getLogger(PredictDependencyChangeTool.class);

    private final DependencyChangePredictionService predictionService;

    public PredictDependencyChangeTool(DependencyChangePredictionService predictionService) {
        this.predictionService = predictionService;
    }

    /**
     * Predicts the downstream impact of a proposed dependency change.
     * Analyzes the dependency name, change type, and optional versions to
     * produce a comprehensive, deterministic dependency impact report.
     *
     * @param dependencyName the name of the dependency (e.g., "org.springframework.boot:spring-boot-starter-web")
     * @param changeType     the type of change: ADD, REMOVE, UPGRADE, DOWNGRADE, or MODIFY
     * @param currentVersion the current version (optional, required for UPGRADE/DOWNGRADE)
     * @param newVersion     the new/target version (optional, required for UPGRADE/DOWNGRADE/ADD)
     * @param repositoryName the name of the repository (optional)
     * @return a formatted dependency change prediction report
     */
    @Tool(description = "Predict the repository-wide impact of a dependency change. " +
            "Analyzes the dependency name, change type (ADD, REMOVE, UPGRADE, DOWNGRADE, MODIFY), " +
            "and optional versions to produce a comprehensive deterministic dependency impact report. " +
            "Includes impacted modules, services, transitive effects, compatibility risks, build risks, " +
            "testing impact, migration recommendations, and a validation checklist.")
    public String predictDependencyChange(
            @ToolParam(description = "The fully qualified dependency name (e.g., 'org.springframework.boot:spring-boot-starter-web')") String dependencyName,
            @ToolParam(description = "The type of change: ADD, REMOVE, UPGRADE, DOWNGRADE, or MODIFY") String changeType,
            @ToolParam(description = "The current version of the dependency (optional, required for UPGRADE/DOWNGRADE)", required = false) String currentVersion,
            @ToolParam(description = "The new/target version of the dependency (optional, required for UPGRADE/DOWNGRADE/ADD)", required = false) String newVersion,
            @ToolParam(description = "The repository name (optional)", required = false) String repositoryName) {

        logger.debug("predict_dependency_change tool invoked: {} {} {} -> {} in repo: {}",
                changeType, dependencyName, currentVersion, newVersion, repositoryName);

        try {
            DependencyChangePredictionResponse response = predictionService.predictDependencyChange(
                    dependencyName, changeType, currentVersion, newVersion, repositoryName);

            return formatPredictionResponse(response);
        } catch (IllegalArgumentException e) {
            String message = "Unable to predict dependency change: " + e.getMessage();
            logger.error(message, e);
            return "ERROR: " + message;
        } catch (Exception e) {
            String message = "An unexpected error occurred while predicting dependency change: " + e.getMessage();
            logger.error(message, e);
            return "ERROR: " + message;
        }
    }

    /**
     * Predicts the downstream impact of a dependency change described in natural language.
     * Parses the change description to extract the dependency name, change type, and
     * version information automatically.
     *
     * @param changeDescription a natural language description of the dependency change
     * @param repositoryName    the name of the repository (optional)
     * @return a formatted dependency change prediction report
     */
    @Tool(description = "Predict the repository-wide impact of a dependency change from a natural language description. " +
            "Parses the description to automatically extract the dependency name, change type, " +
            "and version information. Example: 'Upgrade com.example:my-lib from 1.0.0 to 2.0.0'")
    public String predictDependencyChangeFromDescription(
            @ToolParam(description = "A natural language description of the dependency change (e.g., 'Upgrade org.springframework.boot:spring-boot-starter-web from 3.0.0 to 3.1.0')") String changeDescription,
            @ToolParam(description = "The repository name (optional)", required = false) String repositoryName) {

        logger.debug("predict_dependency_change_from_description tool invoked: {} in repo: {}",
                changeDescription, repositoryName);

        try {
            DependencyChangePredictionResponse response = predictionService.predictDependencyChangeFromDescription(
                    changeDescription, repositoryName);

            return formatPredictionResponse(response);
        } catch (IllegalArgumentException e) {
            String message = "Unable to predict dependency change: " + e.getMessage();
            logger.error(message, e);
            return "ERROR: " + message;
        } catch (Exception e) {
            String message = "An unexpected error occurred while predicting dependency change: " + e.getMessage();
            logger.error(message, e);
            return "ERROR: " + message;
        }
    }

    /**
     * Formats the dependency change prediction response into a readable report.
     */
    private String formatPredictionResponse(DependencyChangePredictionResponse response) {
        StringBuilder sb = new StringBuilder();

        sb.append("## Dependency Change Prediction Report\n\n");

        // Header
        sb.append("### Proposed Change\n\n");
        sb.append("- **Dependency**: ").append(response.getDependencyName()).append("\n");
        sb.append("- **Change Type**: ").append(response.getChangeType()).append("\n");
        sb.append("- **Prediction Category**: ").append(response.getPredictionCategory()).append("\n");
        if (response.getCurrentVersion() != null) {
            sb.append("- **Current Version**: ").append(response.getCurrentVersion()).append("\n");
        }
        if (response.getNewVersion() != null) {
            sb.append("- **New Version**: ").append(response.getNewVersion()).append("\n");
        }
        sb.append("- **Migration Effort**: ").append(response.getMigrationEffortEstimate()).append("\n");
        sb.append("- **Circular Dependency Detected**: ").append(response.isCircularDependencyDetected() ? "Yes" : "No").append("\n\n");

        // Impacted Modules
        appendListSection(sb, "Impacted Modules", response.getImpactedModules());

        // Impacted Services
        appendListSection(sb, "Impacted Services", response.getImpactedServices());

        // Transitive Dependency Effects
        appendListSection(sb, "Transitive Dependency Effects", response.getTransitiveDependencyEffects());

        // Compatibility Risks
        appendListSection(sb, "Compatibility Risks", response.getCompatibilityRisks());

        // Build Risks
        appendListSection(sb, "Build Risks", response.getBuildRisks());

        // Testing Impact
        appendListSection(sb, "Testing Impact", response.getTestingImpact());

        // Migration Recommendations
        appendListSection(sb, "Migration Recommendations", response.getMigrationRecommendations());

        // Suggested Validation Checklist
        appendListSection(sb, "Suggested Validation Checklist", response.getSuggestedValidationChecklist());

        return sb.toString();
    }

    /**
     * Appends a section with a heading and bullet points to the StringBuilder.
     */
    private void appendListSection(StringBuilder sb, String heading, java.util.List<String> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        sb.append("### ").append(heading).append("\n\n");
        for (String item : items) {
            if (item.startsWith("[ ]")) {
                sb.append("- ").append(item).append("\n");
            } else {
                sb.append("- ").append(item).append("\n");
            }
        }
        sb.append("\n");
    }
}