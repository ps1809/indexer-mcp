package com.projectiq.mcp.validation.service;

import com.projectiq.mcp.analysis.dto.RepositoryConventionResponse;
import com.projectiq.mcp.analysis.dto.RepositoryHealthResponse;
import com.projectiq.mcp.analysis.service.ArchitectureInsightsService;
import com.projectiq.mcp.analysis.service.ImpactAnalysisService;
import com.projectiq.mcp.analysis.service.RepositoryConventionAnalyzerService;
import com.projectiq.mcp.analysis.service.RepositoryHealthService;
import com.projectiq.mcp.analysis.service.TestImpactAnalysisService;
import com.projectiq.mcp.orchestration.dto.WorkflowDefinition;
import com.projectiq.mcp.orchestration.service.WorkflowOrchestratorService;
import com.projectiq.mcp.planning.dto.ExecutionPlanRequest;
import com.projectiq.mcp.planning.dto.ExecutionPlanRequest.PlanStep;
import com.projectiq.mcp.planning.service.ExecutionPlanningService;
import com.projectiq.mcp.validation.dto.ValidationCategory;
import com.projectiq.mcp.validation.dto.ValidationFinding;
import com.projectiq.mcp.validation.dto.ValidationReport;
import com.projectiq.mcp.validation.dto.ValidationReport.RepositoryHealthSummary;
import com.projectiq.mcp.validation.dto.ValidationReport.RiskSummary;
import com.projectiq.mcp.validation.dto.ValidationSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service that implements the Intelligent Validation Pipeline.
 * Validates an entire execution workflow before an AI coding agent begins implementation.
 *
 * <p>The pipeline verifies repository state, execution prerequisites, dependency integrity,
 * architectural consistency, testing readiness, and implementation risks to prevent invalid
 * development workflows while remaining fully deterministic.</p>
 *
 * <p>Validation is performed in stable order across eight categories:
 * Workflow, Repository, Dependency, Architecture, Convention, Test Coverage, Risk, and
 * Execution Readiness. Each finding is classified with a severity level. Critical and High
 * severity findings may be flagged as blocking. The pipeline continues after non-critical
 * failures and produces a complete validation report.</p>
 */
@Service
public class WorkflowValidationService {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowValidationService.class);

    private final WorkflowOrchestratorService workflowOrchestratorService;
    private final ExecutionPlanningService executionPlanningService;
    private final ImpactAnalysisService impactAnalysisService;
    private final ArchitectureInsightsService architectureInsightsService;
    private final RepositoryConventionAnalyzerService repositoryConventionAnalyzerService;
    private final RepositoryHealthService repositoryHealthService;
    private final TestImpactAnalysisService testImpactAnalysisService;

    public WorkflowValidationService(
            WorkflowOrchestratorService workflowOrchestratorService,
            ExecutionPlanningService executionPlanningService,
            ImpactAnalysisService impactAnalysisService,
            ArchitectureInsightsService architectureInsightsService,
            RepositoryConventionAnalyzerService repositoryConventionAnalyzerService,
            RepositoryHealthService repositoryHealthService,
            TestImpactAnalysisService testImpactAnalysisService) {
        this.workflowOrchestratorService = workflowOrchestratorService;
        this.executionPlanningService = executionPlanningService;
        this.impactAnalysisService = impactAnalysisService;
        this.architectureInsightsService = architectureInsightsService;
        this.repositoryConventionAnalyzerService = repositoryConventionAnalyzerService;
        this.repositoryHealthService = repositoryHealthService;
        this.testImpactAnalysisService = testImpactAnalysisService;
    }

    /**
     * Validates a complete execution workflow before implementation begins.
     * Performs validation across all eight categories and produces a
     * deterministic validation report.
     *
     * @param workflowName   the name of the workflow to validate
     * @param workflowType   the type of workflow (e.g., "Feature Implementation", "Bug Fix")
     * @param originalRequest the original developer request
     * @param steps          list of workflow steps
     * @param dependencies   list of workflow dependencies
     * @param repositoryName the target repository name
     * @param branch         the target branch (optional, defaults to "main")
     * @return a complete deterministic validation report
     */
    public ValidationReport validateWorkflow(
            String workflowName,
            String workflowType,
            String originalRequest,
            List<PlanStep> steps,
            List<ExecutionPlanRequest.PlanDependency> dependencies,
            String repositoryName,
            String branch) {

        logger.info("Starting validation pipeline for workflow: {} (type: {}) in repository: {}",
                workflowName, workflowType, repositoryName);

        long startTime = System.currentTimeMillis();
        String effectiveBranch = (branch != null && !branch.trim().isEmpty()) ? branch.trim() : "main";

        ValidationReport report = new ValidationReport();
        List<ValidationFinding> findings = new ArrayList<>();
        List<String> recommendedActions = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // =========================================================
        // 1. Workflow Validation
        // =========================================================
        try {
            validateWorkflowCompleteness(workflowName, workflowType, steps, findings, recommendedActions);
        } catch (Exception e) {
            logger.warn("Workflow validation failed: {}", e.getMessage());
            errors.add("Workflow validation error: " + e.getMessage());
        }

        // =========================================================
        // 2. Repository Validation
        // =========================================================
        RepositoryHealthResponse healthResponse = null;
        try {
            healthResponse = repositoryHealthService.analyzeHealth(repositoryName, effectiveBranch);
            validateRepositoryReadiness(healthResponse, findings, recommendedActions);
        } catch (Exception e) {
            logger.warn("Repository validation failed: {}", e.getMessage());
            errors.add("Repository validation error: " + e.getMessage());
        }

        // =========================================================
        // 3. Dependency Validation
        // =========================================================
        try {
            validateDependencyConsistency(steps, dependencies, findings, recommendedActions);
        } catch (Exception e) {
            logger.warn("Dependency validation failed: {}", e.getMessage());
            errors.add("Dependency validation error: " + e.getMessage());
        }

        // =========================================================
        // 4. Architecture Validation
        // =========================================================
        try {
            var archResponse = architectureInsightsService.analyzeArchitecture(repositoryName, effectiveBranch);
            validateArchitectureCompliance(archResponse, workflowType, findings, recommendedActions);
        } catch (Exception e) {
            logger.warn("Architecture validation failed: {}", e.getMessage());
            errors.add("Architecture validation error: " + e.getMessage());
        }

        // =========================================================
        // 5. Convention Validation
        // =========================================================
        try {
            var convResponse = repositoryConventionAnalyzerService.analyzeConventions(repositoryName, effectiveBranch);
            validateConventions(convResponse, findings, recommendedActions);
        } catch (Exception e) {
            logger.warn("Convention validation failed: {}", e.getMessage());
            errors.add("Convention validation error: " + e.getMessage());
        }

        // =========================================================
        // 6. Test Coverage Validation
        // =========================================================
        try {
            validateTestReadiness(repositoryName, effectiveBranch, findings, recommendedActions);
        } catch (Exception e) {
            logger.warn("Test coverage validation failed: {}", e.getMessage());
            errors.add("Test coverage validation error: " + e.getMessage());
        }

        // =========================================================
        // 7. Risk Validation
        // =========================================================
        try {
            validateImplementationRisks(originalRequest, repositoryName, effectiveBranch, findings, recommendedActions);
        } catch (Exception e) {
            logger.warn("Risk validation failed: {}", e.getMessage());
            errors.add("Risk validation error: " + e.getMessage());
        }

        // =========================================================
        // 8. Execution Readiness
        // =========================================================
        try {
            validateExecutionPrerequisites(workflowName, workflowType, steps, findings, recommendedActions);
        } catch (Exception e) {
            logger.warn("Execution readiness validation failed: {}", e.getMessage());
            errors.add("Execution readiness validation error: " + e.getMessage());
        }

        // =========================================================
        // Build Report
        // =========================================================
        report.setFindings(findings);
        report.setErrors(errors);

        // Recompute counts from collected findings
        int passed = 0;
        int failed = 0;
        int warningCount = 0;
        int blockingCount = 0;

        for (ValidationFinding f : findings) {
            if (f.getSeverity() == ValidationSeverity.INFORMATIONAL) {
                passed++;
            } else if (f.getSeverity() == ValidationSeverity.LOW) {
                warningCount++;
            } else if (f.getSeverity() == ValidationSeverity.CRITICAL || f.getSeverity() == ValidationSeverity.HIGH) {
                failed++;
                if (f.isBlocking()) {
                    blockingCount++;
                }
            } else {
                warningCount++;
            }
        }

        report.setPassedValidations(passed);
        report.setFailedValidations(failed);
        report.setWarnings(warningCount);
        report.setBlockingIssues(blockingCount);

        // Build repository health summary
        if (healthResponse != null) {
            RepositoryHealthSummary healthSummary = new RepositoryHealthSummary();
            healthSummary.setHealthScore(healthResponse.getHealthScore());
            healthSummary.setMaintainabilityRating(healthResponse.getMaintainabilityRating());
            healthSummary.setComplexityRating(healthResponse.getComplexityRating());
            healthSummary.setTestingMaturity(healthResponse.getTestingMaturity());
            healthSummary.setDependencyHealth(healthResponse.getDependencyHealth());
            healthSummary.setArchitectureConsistency(healthResponse.getArchitectureConsistency());
            report.setRepositoryHealthSummary(healthSummary);
        }

        // Build risk summary
        RiskSummary riskSummary = new RiskSummary();
        List<String> topRisks = new ArrayList<>();
        int critRisks = 0;
        int highRisks = 0;
        int medRisks = 0;
        for (ValidationFinding f : findings) {
            if (f.getCategory() == ValidationCategory.RISK_VALIDATION) {
                if (f.getSeverity() == ValidationSeverity.CRITICAL) {
                    critRisks++;
                    topRisks.add(f.getMessage());
                } else if (f.getSeverity() == ValidationSeverity.HIGH) {
                    highRisks++;
                    topRisks.add(f.getMessage());
                } else if (f.getSeverity() == ValidationSeverity.MEDIUM) {
                    medRisks++;
                }
            }
        }
        riskSummary.setTotalRisks(critRisks + highRisks + medRisks);
        riskSummary.setCriticalRisks(critRisks);
        riskSummary.setHighRisks(highRisks);
        riskSummary.setMediumRisks(medRisks);
        riskSummary.setTopRisks(topRisks);
        report.setRiskSummary(riskSummary);

        // Compute readiness score (0-100)
        int readinessScore = computeReadinessScore(findings, healthResponse);
        report.setReadinessScore(readinessScore);
        report.setReadinessLabel(getReadinessLabel(readinessScore));

        // Determine overall status
        if (blockingCount > 0) {
            report.setOverallStatus("BLOCKED");
        } else if (failed > 0) {
            report.setOverallStatus("WARNINGS");
        } else if (warningCount > 0) {
            report.setOverallStatus("PASSED_WITH_WARNINGS");
        } else {
            report.setOverallStatus("PASSED");
        }

        report.setRecommendedActions(recommendedActions);

        long duration = System.currentTimeMillis() - startTime;
        logger.info("Validation pipeline completed in {}ms: status={}, score={}, findings={}",
                duration, report.getOverallStatus(), readinessScore, findings.size());

        return report;
    }

    // =========================================================
    // Validation Methods
    // =========================================================

    /**
     * Validates workflow completeness: checks that the workflow has a name,
     * a type, and at least one step.
     */
    private void validateWorkflowCompleteness(
            String workflowName,
            String workflowType,
            List<PlanStep> steps,
            List<ValidationFinding> findings,
            List<String> recommendedActions) {

        if (workflowName == null || workflowName.trim().isEmpty()) {
            findings.add(new ValidationFinding(
                    ValidationCategory.WORKFLOW_VALIDATION,
                    ValidationSeverity.CRITICAL,
                    "Workflow name is missing",
                    "A workflow must have a non-empty name to be identifiable",
                    true));
        } else {
            findings.add(new ValidationFinding(
                    ValidationCategory.WORKFLOW_VALIDATION,
                    ValidationSeverity.INFORMATIONAL,
                    "Workflow name is valid: " + workflowName,
                    "Workflow name is properly specified",
                    false));
        }

        if (workflowType == null || workflowType.trim().isEmpty()) {
            findings.add(new ValidationFinding(
                    ValidationCategory.WORKFLOW_VALIDATION,
                    ValidationSeverity.MEDIUM,
                    "Workflow type is not specified",
                    "Specifying a workflow type helps determine the appropriate validation rules",
                    false));
            recommendedActions.add("Specify a workflow type (e.g., 'Feature Implementation', 'Bug Fix')");
        } else {
            findings.add(new ValidationFinding(
                    ValidationCategory.WORKFLOW_VALIDATION,
                    ValidationSeverity.INFORMATIONAL,
                    "Workflow type is specified: " + workflowType,
                    "Workflow type is properly set",
                    false));
        }

        if (steps == null || steps.isEmpty()) {
            findings.add(new ValidationFinding(
                    ValidationCategory.WORKFLOW_VALIDATION,
                    ValidationSeverity.CRITICAL,
                    "Workflow has no steps defined",
                    "A workflow must contain at least one step to be executable",
                    true));
            recommendedActions.add("Define at least one workflow step with a name and description");
        } else {
            // Check each step
            for (int i = 0; i < steps.size(); i++) {
                PlanStep step = steps.get(i);
                if (step.getName() == null || step.getName().trim().isEmpty()) {
                    findings.add(new ValidationFinding(
                            ValidationCategory.WORKFLOW_VALIDATION,
                            ValidationSeverity.HIGH,
                            "Step at index " + i + " has no name",
                            "Each workflow step must have a non-empty name",
                            false));
                }
                if (step.getDescription() == null || step.getDescription().trim().isEmpty()) {
                    findings.add(new ValidationFinding(
                            ValidationCategory.WORKFLOW_VALIDATION,
                            ValidationSeverity.LOW,
                            "Step '" + (step.getName() != null ? step.getName() : "unnamed")
                                    + "' has no description",
                            "Adding a description helps understand the purpose of each step",
                            false));
                }
            }
            findings.add(new ValidationFinding(
                    ValidationCategory.WORKFLOW_VALIDATION,
                    ValidationSeverity.INFORMATIONAL,
                    "Workflow contains " + steps.size() + " step(s)",
                    "Number of steps is valid",
                    false));
        }
    }

    /**
     * Validates repository readiness using health metrics.
     */
    private void validateRepositoryReadiness(
            RepositoryHealthResponse healthResponse,
            List<ValidationFinding> findings,
            List<String> recommendedActions) {

        if (healthResponse == null) {
            findings.add(new ValidationFinding(
                    ValidationCategory.REPOSITORY_VALIDATION,
                    ValidationSeverity.CRITICAL,
                    "Repository health information is unavailable",
                    "Cannot validate repository readiness without health data",
                    true));
            return;
        }

        int healthScore = healthResponse.getHealthScore();
        findings.add(new ValidationFinding(
                ValidationCategory.REPOSITORY_VALIDATION,
                ValidationSeverity.INFORMATIONAL,
                "Repository health score: " + healthScore + "/100",
                "Health assessment based on indexed repository metadata",
                false));

        if (healthScore < 30) {
            findings.add(new ValidationFinding(
                    ValidationCategory.REPOSITORY_VALIDATION,
                    ValidationSeverity.CRITICAL,
                    "Repository health score is critically low (" + healthScore + ")",
                    "A very low health score indicates significant maintainability issues",
                    true));
            recommendedActions.add("Address critical repository health issues before implementation");
        } else if (healthScore < 50) {
            findings.add(new ValidationFinding(
                    ValidationCategory.REPOSITORY_VALIDATION,
                    ValidationSeverity.HIGH,
                    "Repository health score is low (" + healthScore + ")",
                    "Low health score suggests maintainability concerns that may affect development",
                    true));
            recommendedActions.add("Review and address repository health concerns before starting");
        } else if (healthScore < 70) {
            findings.add(new ValidationFinding(
                    ValidationCategory.REPOSITORY_VALIDATION,
                    ValidationSeverity.MEDIUM,
                    "Repository health score is moderate (" + healthScore + ")",
                    "Moderate health score - some areas may need attention",
                    false));
        }

        String maintainability = healthResponse.getMaintainabilityRating();
        if (maintainability != null && maintainability.equalsIgnoreCase("poor")) {
            findings.add(new ValidationFinding(
                    ValidationCategory.REPOSITORY_VALIDATION,
                    ValidationSeverity.HIGH,
                    "Repository maintainability is rated as poor",
                    "Poor maintainability may make implementation more difficult",
                    false));
            recommendedActions.add("Improve code maintainability before starting implementation");
        }

        String dependencyHealth = healthResponse.getDependencyHealth();
        if (dependencyHealth != null && dependencyHealth.equalsIgnoreCase("poor")) {
            findings.add(new ValidationFinding(
                    ValidationCategory.REPOSITORY_VALIDATION,
                    ValidationSeverity.HIGH,
                    "Repository dependency health is rated as poor",
                    "Poor dependency health may cause integration issues",
                    false));
            recommendedActions.add("Review and resolve dependency health issues");
        }
    }

    /**
     * Validates dependency consistency: checks for circular dependencies,
     * missing step references, and duplicate dependencies.
     */
    private void validateDependencyConsistency(
            List<PlanStep> steps,
            List<ExecutionPlanRequest.PlanDependency> dependencies,
            List<ValidationFinding> findings,
            List<String> recommendedActions) {

        Set<String> stepNames = new LinkedHashSet<>();
        if (steps != null) {
            for (PlanStep step : steps) {
                if (step.getName() != null) {
                    stepNames.add(step.getName().trim());
                }
            }
        }

        if (dependencies == null || dependencies.isEmpty()) {
            findings.add(new ValidationFinding(
                    ValidationCategory.DEPENDENCY_VALIDATION,
                    ValidationSeverity.INFORMATIONAL,
                    "No explicit dependencies defined",
                    "Workflow has no dependencies - all steps may run independently",
                    false));
            return;
        }

        findings.add(new ValidationFinding(
                ValidationCategory.DEPENDENCY_VALIDATION,
                ValidationSeverity.INFORMATIONAL,
                "Workflow has " + dependencies.size() + " dependency definition(s)",
                "Dependencies are specified and will be validated",
                false));

        Map<String, List<String>> dependencyGraph = new LinkedHashMap<>();
        Set<String> processedDepNames = new LinkedHashSet<>();
        List<String> duplicatedDeps = new ArrayList<>();

        for (ExecutionPlanRequest.PlanDependency dep : dependencies) {
            String depStepName = dep.getStepName();
            if (depStepName == null || depStepName.trim().isEmpty()) {
                continue;
            }

            depStepName = depStepName.trim();

            // Check for duplicate dependency definitions
            if (processedDepNames.contains(depStepName)) {
                duplicatedDeps.add(depStepName);
                continue;
            }
            processedDepNames.add(depStepName);

            List<String> dependsOn = dep.getDependsOn();
            if (dependsOn == null) {
                dependsOn = List.of();
            }

            dependencyGraph.put(depStepName, dependsOn);

            // Check if dependency references a non-existent step
            if (!stepNames.contains(depStepName)) {
                findings.add(new ValidationFinding(
                        ValidationCategory.DEPENDENCY_VALIDATION,
                        ValidationSeverity.HIGH,
                        "Dependency references non-existent step: '" + depStepName + "'",
                        "The step referenced in a dependency does not exist in the workflow steps",
                        false));
                recommendedActions.add("Ensure step '" + depStepName + "' is defined in the workflow steps");
            }

            // Check each dependency target
            for (String target : dependsOn) {
                if (target != null && !stepNames.contains(target.trim())) {
                    findings.add(new ValidationFinding(
                            ValidationCategory.DEPENDENCY_VALIDATION,
                            ValidationSeverity.HIGH,
                            "Step '" + depStepName + "' depends on non-existent step: '" + target + "'",
                            "The dependency target does not exist in the workflow steps",
                            false));
                }
            }
        }

        // Report duplicate dependencies
        if (!duplicatedDeps.isEmpty()) {
            for (String dup : duplicatedDeps) {
                findings.add(new ValidationFinding(
                        ValidationCategory.DEPENDENCY_VALIDATION,
                        ValidationSeverity.LOW,
                        "Duplicate dependency definition for step: '" + dup + "'",
                        "Multiple dependency definitions for the same step - duplicates will be ignored",
                        false));
            }
        }

        // Detect circular dependencies using DFS
        List<String> circularDeps = detectCircularDependencies(dependencyGraph);
        for (String circularStep : circularDeps) {
            findings.add(new ValidationFinding(
                    ValidationCategory.DEPENDENCY_VALIDATION,
                    ValidationSeverity.CRITICAL,
                    "Circular dependency detected involving step: '" + circularStep + "'",
                    "Circular dependencies prevent deterministic execution ordering",
                    true));
            recommendedActions.add("Break the circular dependency involving step '" + circularStep + "'");
        }

        if (circularDeps.isEmpty() && !dependencies.isEmpty()) {
            findings.add(new ValidationFinding(
                    ValidationCategory.DEPENDENCY_VALIDATION,
                    ValidationSeverity.INFORMATIONAL,
                    "No circular dependencies detected",
                    "Dependency graph is acyclic and can be topologically sorted",
                    false));
        }
    }

    /**
     * Validates architecture compliance for the given workflow type.
     */
    private void validateArchitectureCompliance(
            com.projectiq.mcp.analysis.dto.ArchitectureInsightsResponse archResponse,
            String workflowType,
            List<ValidationFinding> findings,
            List<String> recommendedActions) {

        if (archResponse == null) {
            findings.add(new ValidationFinding(
                    ValidationCategory.ARCHITECTURE_VALIDATION,
                    ValidationSeverity.CRITICAL,
                    "Architecture insights are unavailable",
                    "Cannot validate architecture compliance without architecture data",
                    true));
            return;
        }

        String archStyle = archResponse.getArchitecturalStyle();
        if (archStyle != null && !archStyle.isEmpty()) {
            findings.add(new ValidationFinding(
                    ValidationCategory.ARCHITECTURE_VALIDATION,
                    ValidationSeverity.INFORMATIONAL,
                    "Architecture style: " + archStyle,
                    "The repository follows a " + archStyle + " architectural style",
                    false));
        } else {
            findings.add(new ValidationFinding(
                    ValidationCategory.ARCHITECTURE_VALIDATION,
                    ValidationSeverity.LOW,
                    "Architecture style not identified",
                    "Could not determine the architectural style of the repository",
                    false));
        }

        var layers = archResponse.getDetectedLayers();
        if (layers != null && !layers.isEmpty()) {
            findings.add(new ValidationFinding(
                    ValidationCategory.ARCHITECTURE_VALIDATION,
                    ValidationSeverity.INFORMATIONAL,
                    "Detected layers: " + String.join(", ", layers),
                    "The repository has clearly defined architectural layers",
                    false));
        } else {
            findings.add(new ValidationFinding(
                    ValidationCategory.ARCHITECTURE_VALIDATION,
                    ValidationSeverity.LOW,
                    "No architectural layers detected",
                    "The repository may lack clear layer separation",
                    false));
        }
    }

    /**
     * Validates repository conventions.
     */
    private void validateConventions(
            RepositoryConventionResponse convResponse,
            List<ValidationFinding> findings,
            List<String> recommendedActions) {

        if (convResponse == null) {
            findings.add(new ValidationFinding(
                    ValidationCategory.CONVENTION_VALIDATION,
                    ValidationSeverity.MEDIUM,
                    "Convention analysis is unavailable",
                    "Cannot validate repository conventions without convention data",
                    false));
            return;
        }

        var namingConventions = convResponse.getNamingConventions();
        if (namingConventions != null) {
            findings.add(new ValidationFinding(
                    ValidationCategory.CONVENTION_VALIDATION,
                    ValidationSeverity.INFORMATIONAL,
                    "Naming conventions detected",
                    "Repository has defined naming conventions for classes, methods, and packages",
                    false));
        } else {
            findings.add(new ValidationFinding(
                    ValidationCategory.CONVENTION_VALIDATION,
                    ValidationSeverity.LOW,
                    "No naming conventions detected",
                    "Repository may lack consistent naming conventions",
                    false));
        }

        var observations = convResponse.getProjectSpecificObservations();
        if (observations != null && !observations.isEmpty()) {
            for (String obs : observations) {
                findings.add(new ValidationFinding(
                        ValidationCategory.CONVENTION_VALIDATION,
                        ValidationSeverity.MEDIUM,
                        "Convention observation: " + obs,
                        "Project-specific observation that may affect implementation conventions",
                        false));
            }
        }

        findings.add(new ValidationFinding(
                ValidationCategory.CONVENTION_VALIDATION,
                ValidationSeverity.INFORMATIONAL,
                "Convention analysis completed",
                "Repository conventions have been analyzed for consistency",
                false));
    }

    /**
     * Validates test readiness for the workflow.
     */
    private void validateTestReadiness(
            String repositoryName,
            String branch,
            List<ValidationFinding> findings,
            List<String> recommendedActions) {

        if (testImpactAnalysisService == null) {
            findings.add(new ValidationFinding(
                    ValidationCategory.TEST_COVERAGE_VALIDATION,
                    ValidationSeverity.MEDIUM,
                    "Test impact analysis service is unavailable",
                    "Cannot perform test readiness validation",
                    false));
            return;
        }

        findings.add(new ValidationFinding(
                ValidationCategory.TEST_COVERAGE_VALIDATION,
                ValidationSeverity.INFORMATIONAL,
                "Test impact analysis is available",
                "The test impact analysis service is operational",
                false));

        recommendedActions.add("Run existing test suite before starting implementation to establish a baseline");
        recommendedActions.add("Identify and update affected tests after implementation");
    }

    /**
     * Validates implementation risks using impact analysis.
     */
    private void validateImplementationRisks(
            String originalRequest,
            String repositoryName,
            String branch,
            List<ValidationFinding> findings,
            List<String> recommendedActions) {

        if (originalRequest == null || originalRequest.trim().isEmpty()) {
            findings.add(new ValidationFinding(
                    ValidationCategory.RISK_VALIDATION,
                    ValidationSeverity.MEDIUM,
                    "No original request provided for risk analysis",
                    "Without the original request, risk analysis may be incomplete",
                    false));
            return;
        }

        // Use impact analysis service for risk detection
        var impactResponse = impactAnalysisService.analyzeImpact(originalRequest, repositoryName, branch);

        if (impactResponse == null) {
            findings.add(new ValidationFinding(
                    ValidationCategory.RISK_VALIDATION,
                    ValidationSeverity.MEDIUM,
                    "Impact analysis returned no results",
                    "Could not assess implementation risks",
                    false));
            return;
        }

        var risks = impactResponse.getPotentialRisks();
        if (risks != null && !risks.isEmpty()) {
            for (var risk : risks) {
                ValidationSeverity severity = mapRiskSeverity(risk.getRiskLevel());
                findings.add(new ValidationFinding(
                        ValidationCategory.RISK_VALIDATION,
                        severity,
                        "Risk: " + risk.getDescription(),
                        "Mitigation: " + risk.getMitigation(),
                        severity == ValidationSeverity.CRITICAL || severity == ValidationSeverity.HIGH));
            }
            recommendedActions.add("Review and address identified risks before starting implementation");
        } else {
            findings.add(new ValidationFinding(
                    ValidationCategory.RISK_VALIDATION,
                    ValidationSeverity.INFORMATIONAL,
                    "No implementation risks identified",
                    "Impact analysis did not identify any risks for this workflow",
                    false));
        }

        var scope = impactResponse.getEstimatedImplementationScope();
        if (scope != null) {
            findings.add(new ValidationFinding(
                    ValidationCategory.RISK_VALIDATION,
                    ValidationSeverity.INFORMATIONAL,
                    "Implementation scope: " + scope.name(),
                    "The estimated scope helps determine the level of effort required",
                    false));
        }
    }

    /**
     * Validates execution prerequisites for the workflow.
     */
    private void validateExecutionPrerequisites(
            String workflowName,
            String workflowType,
            List<PlanStep> steps,
            List<ValidationFinding> findings,
            List<String> recommendedActions) {

        // Check that workflow can be planned
        if (workflowName != null && !workflowName.trim().isEmpty()
                && steps != null && !steps.isEmpty()) {
            findings.add(new ValidationFinding(
                    ValidationCategory.EXECUTION_READINESS,
                    ValidationSeverity.INFORMATIONAL,
                    "Workflow has basic prerequisites for execution",
                    "Workflow name and steps are defined, execution planning is possible",
                    false));
        } else {
            findings.add(new ValidationFinding(
                    ValidationCategory.EXECUTION_READINESS,
                    ValidationSeverity.CRITICAL,
                    "Workflow lacks basic prerequisites for execution",
                    "Either workflow name or steps are missing - execution planning is not possible",
                    true));
            recommendedActions.add("Complete the workflow definition with a name and at least one step");
        }

        // Recommended pre-execution actions
        recommendedActions.add("Ensure the Indexer service is running and accessible");
        recommendedActions.add("Verify the repository is in a clean state before implementation");
    }

    // =========================================================
    // Helper Methods
    // =========================================================

    /**
     * Maps RiskLevel from impact analysis to ValidationSeverity.
     */
    private ValidationSeverity mapRiskSeverity(com.projectiq.mcp.analysis.dto.RiskLevel riskLevel) {
        if (riskLevel == null) return ValidationSeverity.LOW;
        return switch (riskLevel) {
            case HIGH -> ValidationSeverity.HIGH;
            case MEDIUM -> ValidationSeverity.MEDIUM;
            case LOW -> ValidationSeverity.LOW;
        };
    }

    /**
     * Detects circular dependencies in a dependency graph using DFS.
     */
    private List<String> detectCircularDependencies(Map<String, List<String>> dependencyGraph) {
        List<String> circularSteps = new ArrayList<>();
        Set<String> visited = new LinkedHashSet<>();
        Set<String> recursionStack = new LinkedHashSet<>();

        for (String node : dependencyGraph.keySet()) {
            if (!visited.contains(node)) {
                if (hasCycle(node, dependencyGraph, visited, recursionStack)) {
                    circularSteps.add(node);
                }
            }
        }

        return circularSteps;
    }

    private boolean hasCycle(String node, Map<String, List<String>> graph,
                              Set<String> visited, Set<String> recursionStack) {
        if (recursionStack.contains(node)) {
            return true;
        }
        if (visited.contains(node)) {
            return false;
        }

        visited.add(node);
        recursionStack.add(node);

        List<String> neighbors = graph.getOrDefault(node, List.of());
        for (String neighbor : neighbors) {
            if (neighbor != null && graph.containsKey(neighbor)) {
                if (hasCycle(neighbor, graph, visited, recursionStack)) {
                    return true;
                }
            }
        }

        recursionStack.remove(node);
        return false;
    }

    /**
     * Computes a readiness score from 0-100 based on findings and health data.
     */
    private int computeReadinessScore(List<ValidationFinding> findings, RepositoryHealthResponse healthResponse) {
        int score = 100;

        // Deduct for critical and high findings
        for (ValidationFinding finding : findings) {
            switch (finding.getSeverity()) {
                case CRITICAL -> score -= 25;
                case HIGH -> score -= 15;
                case MEDIUM -> score -= 5;
                case LOW -> score -= 2;
                default -> {}
            }
        }

        // Incorporate health score if available
        if (healthResponse != null) {
            int healthScore = healthResponse.getHealthScore();
            score = (score + healthScore) / 2;
        }

        return Math.max(0, Math.min(100, score));
    }

    /**
     * Returns a human-readable readiness label based on the score.
     */
    private String getReadinessLabel(int score) {
        if (score >= 90) return "READY";
        if (score >= 75) return "MOSTLY_READY";
        if (score >= 50) return "MODERATE_RISK";
        if (score >= 25) return "HIGH_RISK";
        return "NOT_READY";
    }
}