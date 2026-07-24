package com.projectiq.mcp.recommendation.service;

import com.projectiq.mcp.analysis.dto.ArchitectureInsightsResponse;
import com.projectiq.mcp.analysis.dto.RepositoryConventionResponse;
import com.projectiq.mcp.analysis.dto.RepositoryHealthResponse;
import com.projectiq.mcp.analysis.dto.TestImpactAnalysisResponse;
import com.projectiq.mcp.analysis.service.ArchitectureInsightsService;
import com.projectiq.mcp.analysis.service.ImplementationPlanningService;
import com.projectiq.mcp.analysis.service.RepositoryConventionAnalyzerService;
import com.projectiq.mcp.analysis.service.RepositoryHealthService;
import com.projectiq.mcp.analysis.service.TestImpactAnalysisService;
import com.projectiq.mcp.orchestration.service.WorkflowExecutionService;
import com.projectiq.mcp.planning.dto.ExecutionPlanRequest;
import com.projectiq.mcp.planning.dto.ExecutionPlanRequest.PlanDependency;
import com.projectiq.mcp.planning.dto.ExecutionPlanRequest.PlanStep;
import com.projectiq.mcp.planning.dto.ExecutionPlanResponse;
import com.projectiq.mcp.planning.service.ExecutionPlanningService;
import com.projectiq.mcp.recommendation.dto.Recommendation;
import com.projectiq.mcp.recommendation.dto.RecommendationCategory;
import com.projectiq.mcp.recommendation.dto.RecommendationPriority;
import com.projectiq.mcp.recommendation.dto.RecommendationReport;
import com.projectiq.mcp.recommendation.dto.RecommendationReport.ReportSummary;
import com.projectiq.mcp.validation.dto.ValidationFinding;
import com.projectiq.mcp.validation.dto.ValidationReport;
import com.projectiq.mcp.validation.dto.ValidationSeverity;
import com.projectiq.mcp.validation.service.WorkflowValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service that implements the Intelligent Recommendation Engine.
 * Analyzes workflow results, validation reports, repository intelligence,
 * and architecture insights to generate deterministic, prioritized recommendations.
 *
 * <p>This engine does NOT use AI/LLM reasoning. All recommendations are generated
 * using deterministic rules based on collected data. Recommendations are ordered
 * by priority (Critical first), with stable ordering within each priority level.
 * No duplicate recommendations are produced.</p>
 */
@Service
public class RecommendationEngineService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationEngineService.class);

    private final WorkflowValidationService workflowValidationService;
    private final ExecutionPlanningService executionPlanningService;
    private final WorkflowExecutionService workflowExecutionService;
    private final ImplementationPlanningService implementationPlanningService;
    private final ArchitectureInsightsService architectureInsightsService;
    private final RepositoryConventionAnalyzerService repositoryConventionAnalyzerService;
    private final RepositoryHealthService repositoryHealthService;
    private final TestImpactAnalysisService testImpactAnalysisService;

    public RecommendationEngineService(
            WorkflowValidationService workflowValidationService,
            ExecutionPlanningService executionPlanningService,
            WorkflowExecutionService workflowExecutionService,
            ImplementationPlanningService implementationPlanningService,
            ArchitectureInsightsService architectureInsightsService,
            RepositoryConventionAnalyzerService repositoryConventionAnalyzerService,
            RepositoryHealthService repositoryHealthService,
            TestImpactAnalysisService testImpactAnalysisService) {
        this.workflowValidationService = workflowValidationService;
        this.executionPlanningService = executionPlanningService;
        this.workflowExecutionService = workflowExecutionService;
        this.implementationPlanningService = implementationPlanningService;
        this.architectureInsightsService = architectureInsightsService;
        this.repositoryConventionAnalyzerService = repositoryConventionAnalyzerService;
        this.repositoryHealthService = repositoryHealthService;
        this.testImpactAnalysisService = testImpactAnalysisService;
    }

    /**
     * Generates a complete deterministic recommendation report by analyzing the
     * validated workflow, collected intelligence, validation results, execution plan,
     * repository insights, and architectural guidance.
     *
     * @param workflowName     the name of the workflow
     * @param workflowType     the type of workflow (e.g., "Feature Implementation", "Bug Fix")
     * @param originalRequest  the original developer request
     * @param repositoryName   the target repository name
     * @param branch           the target branch (optional, defaults to "main")
     * @return a complete deterministic recommendation report
     */
    public RecommendationReport generateRecommendations(
            String workflowName,
            String workflowType,
            String originalRequest,
            String repositoryName,
            String branch) {

        logger.info("Generating recommendations for workflow: {} (type: {}) in repository: {}",
                workflowName, workflowType, repositoryName);

        long startTime = System.currentTimeMillis();
        String effectiveBranch = (branch != null && !branch.trim().isEmpty()) ? branch.trim() : "main";

        RecommendationReport report = new RecommendationReport();
        report.setWorkflowName(workflowName);
        report.setWorkflowType(workflowType);

        List<Recommendation> recommendations = new ArrayList<>();
        List<String> implementationAdvice = new ArrayList<>();
        List<String> testingRecs = new ArrayList<>();
        List<String> architecturalGuidance = new ArrayList<>();
        List<String> repositoryBestPractices = new ArrayList<>();
        List<String> riskMitigationSuggestions = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // Track seen recommendation signatures to prevent duplicates
        Set<String> seenSignatures = new LinkedHashSet<>();
        AtomicInteger idCounter = new AtomicInteger(0);

        // =========================================================
        // Phase 1: Analyze Workflow Results
        // =========================================================
        try {
            analyzeWorkflowResults(workflowName, workflowType,
                    recommendations, seenSignatures, idCounter);
        } catch (Exception e) {
            logger.warn("Workflow analysis failed: {}", e.getMessage());
            errors.add("Workflow analysis error: " + e.getMessage());
        }

        // =========================================================
        // Phase 2: Analyze Validation Reports
        // =========================================================
        try {
            analyzeValidationResults(workflowName, workflowType, repositoryName, effectiveBranch,
                    recommendations, seenSignatures, idCounter, implementationAdvice, testingRecs, errors);
        } catch (Exception e) {
            logger.warn("Validation analysis failed: {}", e.getMessage());
            errors.add("Validation analysis error: " + e.getMessage());
        }

        // =========================================================
        // Phase 3: Analyze Repository Intelligence
        // =========================================================
        RepositoryHealthResponse healthResponse = null;
        try {
            healthResponse = repositoryHealthService.analyzeHealth(repositoryName, effectiveBranch);
            analyzeRepositoryIntelligence(healthResponse,
                    recommendations, seenSignatures, idCounter, repositoryBestPractices,
                    riskMitigationSuggestions);
        } catch (Exception e) {
            logger.warn("Repository intelligence analysis failed: {}", e.getMessage());
            errors.add("Repository intelligence analysis error: " + e.getMessage());
        }

        // =========================================================
        // Phase 4: Analyze Architecture Insights
        // =========================================================
        try {
            var archResponse = architectureInsightsService.analyzeArchitecture(repositoryName, effectiveBranch);
            analyzeArchitectureInsights(archResponse,
                    recommendations, seenSignatures, idCounter, architecturalGuidance);
        } catch (Exception e) {
            logger.warn("Architecture insights analysis failed: {}", e.getMessage());
            errors.add("Architecture insights analysis error: " + e.getMessage());
        }

        // =========================================================
        // Phase 5: Analyze Conventions
        // =========================================================
        try {
            var convResponse = repositoryConventionAnalyzerService.analyzeConventions(repositoryName, effectiveBranch);
            analyzeConventions(convResponse,
                    recommendations, seenSignatures, idCounter, repositoryBestPractices);
        } catch (Exception e) {
            logger.warn("Convention analysis failed: {}", e.getMessage());
            errors.add("Convention analysis error: " + e.getMessage());
        }

        // =========================================================
        // Phase 6: Analyze Test Impact
        // =========================================================
        try {
            analyzeTestImpact(originalRequest, repositoryName, effectiveBranch,
                    recommendations, seenSignatures, idCounter, testingRecs);
        } catch (Exception e) {
            logger.warn("Test impact analysis failed: {}", e.getMessage());
            errors.add("Test impact analysis error: " + e.getMessage());
        }

        // =========================================================
        // Phase 7: Analyze Implementation Plan
        // =========================================================
        try {
            analyzeImplementationPlan(workflowName, workflowType, originalRequest, repositoryName, effectiveBranch,
                    recommendations, seenSignatures, idCounter, implementationAdvice,
                    riskMitigationSuggestions, errors);
        } catch (Exception e) {
            logger.warn("Implementation plan analysis failed: {}", e.getMessage());
            errors.add("Implementation plan analysis error: " + e.getMessage());
        }

        // =========================================================
        // Build Report
        // =========================================================

        // Sort recommendations by priority (Critical first, then High, Medium, Low)
        // and stable ordering by ID within same priority
        recommendations.sort(Comparator
                .comparingInt((Recommendation r) -> priorityOrder(r.getPriority()))
                .thenComparing(Recommendation::getId));

        report.setPrioritizedRecommendations(recommendations);
        report.setImplementationAdvice(implementationAdvice);
        report.setTestingRecommendations(testingRecs);
        report.setArchitecturalGuidance(architecturalGuidance);
        report.setRepositoryBestPractices(repositoryBestPractices);
        report.setRiskMitigationSuggestions(riskMitigationSuggestions);
        report.setErrors(errors);

        // Build summary
        int criticalCount = 0;
        int highCount = 0;
        int mediumCount = 0;
        int lowCount = 0;
        int implCount = 0;
        int archCount = 0;
        int testCount = 0;
        int riskCount = 0;

        for (Recommendation rec : recommendations) {
            switch (rec.getPriority()) {
                case CRITICAL -> criticalCount++;
                case HIGH -> highCount++;
                case MEDIUM -> mediumCount++;
                case LOW -> lowCount++;
            }
            switch (rec.getCategory()) {
                case IMPLEMENTATION -> implCount++;
                case ARCHITECTURE -> archCount++;
                case TESTING -> testCount++;
                case RISK_MITIGATION -> riskCount++;
                default -> {}
            }
        }

        ReportSummary summary = new ReportSummary();
        summary.setTotalRecommendations(recommendations.size());
        summary.setCriticalCount(criticalCount);
        summary.setHighCount(highCount);
        summary.setMediumCount(mediumCount);
        summary.setLowCount(lowCount);
        summary.setImplementationCount(implCount);
        summary.setArchitectureCount(archCount);
        summary.setTestingCount(testCount);
        summary.setRiskCount(riskCount);
        report.setRecommendationSummary(summary);

        // Build executive summary
        String execSummary = buildExecutiveSummary(workflowName, workflowType, recommendations,
                implementationAdvice, testingRecs, healthResponse);
        report.setExecutiveSummary(execSummary);

        // Compute confidence score
        int confidenceScore = computeConfidenceScore(recommendations, errors);
        report.setConfidenceScore(confidenceScore);

        long duration = System.currentTimeMillis() - startTime;
        logger.info("Recommendation engine completed in {}ms: {} recommendations generated, confidence={}",
                duration, recommendations.size(), confidenceScore);

        return report;
    }

    // =========================================================
    // Analysis Phases
    // =========================================================

    private void analyzeWorkflowResults(
            String workflowName,
            String workflowType,
            List<Recommendation> recommendations,
            Set<String> seenSignatures,
            AtomicInteger idCounter) {

        if (workflowName == null || workflowName.trim().isEmpty()) {
            addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                    RecommendationCategory.IMPLEMENTATION, RecommendationPriority.CRITICAL,
                    "Define workflow name",
                    "The workflow must have a non-empty name to ensure traceability and identification.",
                    "Without a defined workflow name, the implementation cannot be properly tracked across the development lifecycle.",
                    List.of("Provide a descriptive workflow name that reflects the implementation goal"));
            return;
        }

        addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                RecommendationCategory.IMPLEMENTATION, RecommendationPriority.LOW,
                "Workflow identified: " + workflowName,
                "The workflow has been identified and will be used as the basis for recommendations.",
                "A defined workflow name enables proper tracking and organization of implementation tasks.",
                List.of());

        if (workflowType == null || workflowType.trim().isEmpty()) {
            addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                    RecommendationCategory.IMPLEMENTATION, RecommendationPriority.MEDIUM,
                    "Specify workflow type",
                    "The workflow type is not specified, which may affect the relevance of recommendations.",
                    "Workflow type helps tailor recommendations to the specific kind of development task (feature, bug fix, refactoring).",
                    List.of("Set the workflow type to 'Feature Implementation', 'Bug Fix', 'Refactoring', or 'Documentation'"));
        }
    }

    private void analyzeValidationResults(
            String workflowName,
            String workflowType,
            String repositoryName,
            String branch,
            List<Recommendation> recommendations,
            Set<String> seenSignatures,
            AtomicInteger idCounter,
            List<String> implementationAdvice,
            List<String> testingRecs,
            List<String> errors) {

        ValidationReport validationReport;
        try {
            validationReport = workflowValidationService.validateWorkflow(
                    workflowName, workflowType, null,
                    new ArrayList<>(),
                    new ArrayList<>(),
                    repositoryName, branch);
        } catch (Exception e) {
            logger.warn("Could not run full validation for recommendations: {}", e.getMessage());
            errors.add("Validation not available: " + e.getMessage());
            return;
        }

        if (validationReport == null) {
            errors.add("Validation report is null");
            return;
        }

        String status = validationReport.getOverallStatus();
        if ("BLOCKED".equals(status)) {
            addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                    RecommendationCategory.IMPLEMENTATION, RecommendationPriority.CRITICAL,
                    "Workflow validation is BLOCKED",
                    "The workflow validation has identified blocking issues that prevent implementation from starting.",
                    "Blocking issues must be resolved before any implementation can proceed safely.",
                    List.of("Review the validation report and resolve all blocking issues",
                            "Re-run validation after addressing blocking issues"));
            implementationAdvice.add("Resolve all blocking validation issues before starting implementation");
        } else if ("WARNINGS".equals(status)) {
            addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                    RecommendationCategory.IMPLEMENTATION, RecommendationPriority.HIGH,
                    "Workflow validation has warnings",
                    "The workflow validation has warnings that should be reviewed before proceeding.",
                    "Non-blocking warnings indicate potential issues that may affect implementation quality.",
                    List.of("Review all validation warnings before starting implementation"));
            implementationAdvice.add("Review validation warnings before beginning implementation");
        }

        List<ValidationFinding> findings = validationReport.getFindings();
        if (findings != null) {
            for (ValidationFinding finding : findings) {
                if (finding.getSeverity() == ValidationSeverity.CRITICAL) {
                    addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                            RecommendationCategory.RISK_MITIGATION, RecommendationPriority.CRITICAL,
                            "Critical finding: " + truncate(finding.getMessage(), 80),
                            "A critical validation finding requires immediate attention: " + finding.getMessage(),
                            finding.getDetails() != null ? finding.getDetails() : "Critical findings represent high-risk issues that must be resolved.",
                            List.of("Address: " + finding.getMessage()));
                } else if (finding.getSeverity() == ValidationSeverity.HIGH) {
                    addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                            RecommendationCategory.RISK_MITIGATION, RecommendationPriority.HIGH,
                            "High severity finding: " + truncate(finding.getMessage(), 80),
                            "A high severity validation finding needs attention: " + finding.getMessage(),
                            finding.getDetails() != null ? finding.getDetails() : "High severity findings represent significant risks.",
                            List.of("Review and address: " + finding.getMessage()));
                }
            }
        }

        List<String> validationActions = validationReport.getRecommendedActions();
        if (validationActions != null) {
            implementationAdvice.addAll(validationActions);
        }

        int readinessScore = validationReport.getReadinessScore();
        if (readinessScore < 50) {
            addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                    RecommendationCategory.IMPLEMENTATION, RecommendationPriority.HIGH,
                    "Low implementation readiness score: " + readinessScore + "/100",
                    "The implementation readiness score is low, indicating significant risks or issues.",
                    "A low readiness score suggests that the project is not prepared for implementation.",
                    List.of("Improve readiness by addressing validation findings"));
        }
    }

    private void analyzeRepositoryIntelligence(
            RepositoryHealthResponse healthResponse,
            List<Recommendation> recommendations,
            Set<String> seenSignatures,
            AtomicInteger idCounter,
            List<String> repositoryBestPractices,
            List<String> riskMitigationSuggestions) {

        if (healthResponse == null) {
            addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                    RecommendationCategory.IMPLEMENTATION, RecommendationPriority.MEDIUM,
                    "Repository health data unavailable",
                    "Repository health information could not be retrieved.",
                    "Without health data, recommendations related to repository quality cannot be provided.",
                    List.of("Ensure the repository is properly indexed by the ProjectIQ Indexer"));
            return;
        }

        int healthScore = healthResponse.getHealthScore();
        if (healthScore < 30) {
            addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                    RecommendationCategory.RISK_MITIGATION, RecommendationPriority.CRITICAL,
                    "Repository health critically low (" + healthScore + "/100)",
                    "The repository health score is critically low, indicating significant maintainability and quality issues.",
                    "A critically low health score suggests the repository requires substantial improvement before implementation can proceed safely.",
                    List.of("Address critical repository health issues before implementation",
                            "Improve code maintainability and reduce complexity"));
            riskMitigationSuggestions.add("Critical repository health issues must be resolved before implementation");
        } else if (healthScore < 50) {
            addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                    RecommendationCategory.RISK_MITIGATION, RecommendationPriority.HIGH,
                    "Repository health is low (" + healthScore + "/100)",
                    "The repository health score is low, indicating potential maintainability concerns.",
                    "Low health scores correlate with increased implementation difficulty and defect rates.",
                    List.of("Review repository health metrics and address key issues",
                            "Focus on maintainability and test coverage improvements"));
            riskMitigationSuggestions.add("Address repository health concerns before starting implementation");
        } else if (healthScore < 70) {
            addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                    RecommendationCategory.RISK_MITIGATION, RecommendationPriority.MEDIUM,
                    "Repository health is moderate (" + healthScore + "/100)",
                    "The repository health score is moderate, suggesting some areas may need attention.",
                    "Moderate health scores indicate room for improvement in specific areas.",
                    List.of("Review specific health metrics for improvement opportunities"));
        }

        String maintainability = healthResponse.getMaintainabilityRating();
        if (maintainability != null && maintainability.equalsIgnoreCase("poor")) {
            addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                    RecommendationCategory.REFACTORING, RecommendationPriority.HIGH,
                    "Improve repository maintainability",
                    "The repository maintainability is rated as 'poor', which will increase implementation effort.",
                    "Poor maintainability makes it harder to understand, modify, and extend the codebase.",
                    List.of("Refactor complex code sections",
                            "Improve code documentation and comments",
                            "Reduce coupling between components"));
            repositoryBestPractices.add("Prioritize maintainability improvements during implementation");
        }

        String dependencyHealth = healthResponse.getDependencyHealth();
        if (dependencyHealth != null && dependencyHealth.equalsIgnoreCase("poor")) {
            addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                    RecommendationCategory.DEPENDENCY, RecommendationPriority.HIGH,
                    "Resolve dependency health issues",
                    "The repository dependency health is rated as 'poor', which may cause integration issues.",
                    "Poor dependency health can lead to build failures, version conflicts, and security vulnerabilities.",
                    List.of("Audit and update project dependencies",
                            "Resolve any dependency conflicts",
                            "Remove unused dependencies"));
            riskMitigationSuggestions.add("Resolve dependency health issues before integration");
        }

        String testingMaturity = healthResponse.getTestingMaturity();
        if (testingMaturity != null && testingMaturity.equalsIgnoreCase("immature")) {
            addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                    RecommendationCategory.TESTING, RecommendationPriority.HIGH,
                    "Improve test coverage",
                    "The repository testing maturity is rated as 'immature', indicating insufficient test coverage.",
                    "Low test coverage increases the risk of regressions during implementation.",
                    List.of("Add unit tests for new and modified code",
                            "Establish testing standards and patterns"));
        }

        String archConsistency = healthResponse.getArchitectureConsistency();
        if (archConsistency != null && archConsistency.equalsIgnoreCase("inconsistent")) {
            addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                    RecommendationCategory.ARCHITECTURE, RecommendationPriority.MEDIUM,
                    "Architecture consistency needs improvement",
                    "The repository architecture consistency is rated as 'inconsistent'.",
                    "Inconsistent architecture patterns can lead to maintenance challenges and increased defect rates.",
                    List.of("Review architecture guidelines and ensure consistent pattern usage",
                            "Document architectural decisions and standards"));
        }
    }

    private void analyzeArchitectureInsights(
            ArchitectureInsightsResponse archResponse,
            List<Recommendation> recommendations,
            Set<String> seenSignatures,
            AtomicInteger idCounter,
            List<String> architecturalGuidance) {

        if (archResponse == null) {
            addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                    RecommendationCategory.ARCHITECTURE, RecommendationPriority.MEDIUM,
                    "Architecture insights unavailable",
                    "Architecture insights could not be retrieved, limiting architectural recommendations.",
                    "Architecture analysis helps ensure implementation aligns with the existing system design.",
                    List.of("Ensure the ProjectIQ Indexer can analyze the repository architecture"));
            return;
        }

        String archStyle = archResponse.getArchitecturalStyle();
        if (archStyle != null && !archStyle.isEmpty()) {
            addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                    RecommendationCategory.ARCHITECTURE, RecommendationPriority.LOW,
                    "Follow " + archStyle + " architecture patterns",
                    "The repository follows a " + archStyle + " architectural style.",
                    "Adhering to the established architectural style ensures consistency across the codebase.",
                    List.of("Apply " + archStyle + " patterns during implementation"));
            architecturalGuidance.add("Maintain " + archStyle + " architectural patterns throughout implementation");
        }

        var layers = archResponse.getDetectedLayers();
        if (layers != null && !layers.isEmpty()) {
            architecturalGuidance.add("Respect existing architectural layers: " + String.join(", ", layers));
        } else {
            addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                    RecommendationCategory.ARCHITECTURE, RecommendationPriority.LOW,
                    "Define architectural layers",
                    "No architectural layers were detected in the repository.",
                    "Clear layer separation helps maintain a well-organized codebase.",
                    List.of("Consider defining and documenting architectural layers for the repository"));
        }

        var strengths = archResponse.getArchitecturalStrengths();
        if (strengths != null && !strengths.isEmpty()) {
            for (String strength : strengths) {
                architecturalGuidance.add("Leverage architectural strength: " + strength);
            }
        }

        var concerns = archResponse.getPotentialConcerns();
        if (concerns != null && !concerns.isEmpty()) {
            for (String concern : concerns) {
                addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                        RecommendationCategory.ARCHITECTURE, RecommendationPriority.MEDIUM,
                        "Architecture concern: " + truncate(concern, 80),
                        "Potential architecture concern: " + concern,
                        "Addressing architecture concerns early helps prevent technical debt.",
                        List.of("Review and address: " + concern));
            }
        }
    }

    private void analyzeConventions(
            RepositoryConventionResponse convResponse,
            List<Recommendation> recommendations,
            Set<String> seenSignatures,
            AtomicInteger idCounter,
            List<String> repositoryBestPractices) {

        if (convResponse == null) {
            addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                    RecommendationCategory.IMPLEMENTATION, RecommendationPriority.LOW,
                    "Convention analysis unavailable",
                    "Convention analysis could not be performed.",
                    "Without convention analysis, recommendations about coding standards may be limited.",
                    List.of("Ensure repository indexing includes convention analysis"));
            return;
        }

        var namingConventions = convResponse.getNamingConventions();
        if (namingConventions != null) {
            repositoryBestPractices.add("Follow established naming conventions for classes, methods, and packages");
            addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                    RecommendationCategory.DOCUMENTATION, RecommendationPriority.LOW,
                    "Follow repository naming conventions",
                    "The repository has established naming conventions that should be followed.",
                    "Consistent naming improves readability and maintainability across the codebase.",
                    List.of("Refer to naming conventions when creating new classes, methods, and packages"));
        }

        var observations = convResponse.getProjectSpecificObservations();
        if (observations != null && !observations.isEmpty()) {
            for (String obs : observations) {
                addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                        RecommendationCategory.IMPLEMENTATION, RecommendationPriority.MEDIUM,
                        "Convention note: " + truncate(obs, 80),
                        "Project-specific observation: " + obs,
                        "Project-specific conventions are important for maintaining codebase consistency.",
                        List.of(obs));
                repositoryBestPractices.add(obs);
            }
        }
    }

    private void analyzeTestImpact(
            String originalRequest,
            String repositoryName,
            String branch,
            List<Recommendation> recommendations,
            Set<String> seenSignatures,
            AtomicInteger idCounter,
            List<String> testingRecs) {

        testingRecs.add("Run existing test suite before starting implementation to establish a baseline");
        testingRecs.add("Add unit tests for all new and modified code");
        testingRecs.add("Verify no regressions by running the full test suite after implementation");

        if (originalRequest == null || originalRequest.trim().isEmpty()) {
            addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                    RecommendationCategory.TESTING, RecommendationPriority.MEDIUM,
                    "Establish testing baseline",
                    "Run existing tests before implementation to establish a baseline for regression detection.",
                    "A testing baseline helps identify regressions introduced by the implementation.",
                    List.of("Run the full test suite before starting",
                            "Document current test results for comparison"));
            return;
        }

        try {
            var testImpactResponse = testImpactAnalysisService.analyzeTestImpact(originalRequest, repositoryName, branch);
            if (testImpactResponse != null) {
                var relatedTests = testImpactResponse.getRelatedTestClasses();
                if (relatedTests != null && !relatedTests.isEmpty()) {
                    addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                            RecommendationCategory.TESTING, RecommendationPriority.HIGH,
                            relatedTests.size() + " related test classes identified",
                            "Test impact analysis indicates " + relatedTests.size()
                                    + " existing test classes may be related to the implementation.",
                            "Identifying related tests helps prevent regressions and ensures comprehensive test coverage.",
                            List.of("Review and update related tests after implementation"));
                    testingRecs.add("Review " + relatedTests.size() + " potentially affected tests after implementation");
                }

                var missingTests = testImpactResponse.getMissingTests();
                if (missingTests != null && !missingTests.isEmpty()) {
                    for (String missingTest : missingTests) {
                        addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                                RecommendationCategory.TESTING, RecommendationPriority.MEDIUM,
                                "Missing test: " + truncate(missingTest, 80),
                                "Test impact analysis identified a missing test: " + missingTest,
                                "Adequate test coverage reduces the risk of defects in production.",
                                List.of("Add tests for: " + missingTest));
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Test impact analysis unavailable: {}", e.getMessage());
            testingRecs.add("Manually identify and update affected tests after implementation");
        }

        addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                RecommendationCategory.TESTING, RecommendationPriority.MEDIUM,
                "Establish testing baseline",
                "Run existing tests before implementation to establish a baseline for regression detection.",
                "A testing baseline helps identify regressions introduced by the implementation.",
                List.of("Run the full test suite before starting",
                        "Document current test results for comparison"));
    }

    private void analyzeImplementationPlan(
            String workflowName,
            String workflowType,
            String originalRequest,
            String repositoryName,
            String branch,
            List<Recommendation> recommendations,
            Set<String> seenSignatures,
            AtomicInteger idCounter,
            List<String> implementationAdvice,
            List<String> riskMitigationSuggestions,
            List<String> errors) {

        ExecutionPlanResponse planResponse = null;
        try {
            planResponse = executionPlanningService.generateExecutionPlan(
                    new ExecutionPlanRequest(
                            workflowName,
                            workflowType,
                            originalRequest,
                            new ArrayList<>(),
                            new ArrayList<>()));
        } catch (Exception e) {
            logger.warn("Execution planning unavailable: {}", e.getMessage());
            errors.add("Execution planning unavailable: " + e.getMessage());
        }

        implementationAdvice.add("Follow the recommended execution plan step by step");
        implementationAdvice.add("Validate each step before proceeding to the next");
        implementationAdvice.add("Commit changes incrementally with descriptive messages");

        if (planResponse != null) {
            String planStatus = planResponse.getPlanStatus();
            if ("VALID".equals(planStatus) || "READY".equals(planStatus)) {
                addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                        RecommendationCategory.IMPLEMENTATION, RecommendationPriority.LOW,
                        "Execution plan is ready",
                        "The execution plan has been generated and is ready for implementation.",
                        "A valid execution plan provides a structured roadmap for implementation.",
                        List.of("Follow the execution plan phases in order"));

                var phases = planResponse.getExecutionPhases();
                if (phases != null) {
                    implementationAdvice.add("Implementation phases: " + phases.size() + " phases defined");
                }

                var tasks = planResponse.getOrderedImplementationTasks();
                if (tasks != null) {
                    implementationAdvice.add("Implementation tasks: " + tasks.size() + " ordered tasks defined");
                }
            } else {
                addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                        RecommendationCategory.IMPLEMENTATION, RecommendationPriority.HIGH,
                        "Execution plan has issues: " + planStatus,
                        "The execution plan status is '" + planStatus + "', indicating potential problems.",
                        "A non-ready execution plan may lead to unclear implementation steps.",
                        List.of("Review execution plan issues and adjust workflow definition"));
            }

            var planRisks = planResponse.getPotentialRisks();
            if (planRisks != null && !planRisks.isEmpty()) {
                for (var risk : planRisks) {
                    if (risk.getSeverity() != null) {
                        RecommendationPriority riskPriority = mapRiskSeverityToPriority(risk.getSeverity());
                        String mit = risk.getMitigation() != null ? risk.getMitigation() : "Review and mitigate this risk";
                        addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                                RecommendationCategory.RISK_MITIGATION, riskPriority,
                                "Risk: " + truncate(risk.getDescription(), 80),
                                "Identified risk: " + risk.getDescription()
                                        + (risk.getImpact() != null ? " (Impact: " + risk.getImpact() + ")" : ""),
                                mit,
                                List.of(mit));
                        if (risk.getMitigation() != null) {
                            riskMitigationSuggestions.add(risk.getMitigation());
                        }
                    }
                }
            }

            var prerequisites = planResponse.getRequiredPrerequisites();
            if (prerequisites != null && !prerequisites.isEmpty()) {
                for (String prereq : prerequisites) {
                    addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                            RecommendationCategory.CONFIGURATION, RecommendationPriority.HIGH,
                            "Prerequisite: " + truncate(prereq, 80),
                            "A prerequisite must be met before implementation: " + prereq,
                            "Prerequisites ensure the environment is properly configured for implementation.",
                            List.of(prereq));
                }
            }
        }

        if (workflowType != null) {
            String lowerType = workflowType.toLowerCase();
            if (lowerType.contains("bug") || lowerType.contains("fix")) {
                addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                        RecommendationCategory.TESTING, RecommendationPriority.HIGH,
                        "Add regression tests for bug fix",
                        "For bug fix workflows, add regression tests to prevent the bug from recurring.",
                        "Regression tests help ensure that fixed bugs stay fixed.",
                        List.of("Write a test that reproduces the bug before fixing",
                                "Verify the test passes after the fix"));
            }

            if (lowerType.contains("refactor") || lowerType.contains("refactoring")) {
                addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                        RecommendationCategory.REFACTORING, RecommendationPriority.HIGH,
                        "Preserve behavior during refactoring",
                        "For refactoring workflows, ensure existing behavior is preserved.",
                        "Refactoring should improve code structure without changing external behavior.",
                        List.of("Ensure comprehensive test coverage before refactoring",
                                "Verify no behavior changes after refactoring",
                                "Refactor in small, verifiable steps"));
                implementationAdvice.add("Refactor in small steps with validation after each step");
            }

            if (lowerType.contains("feature") || lowerType.contains("enhancement")) {
                addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                        RecommendationCategory.IMPLEMENTATION, RecommendationPriority.MEDIUM,
                        "Follow feature implementation best practices",
                        "For feature implementation workflows, follow standard development practices.",
                        "Feature implementations benefit from clear requirements and iterative development.",
                        List.of("Start with a minimal viable implementation",
                                "Add tests alongside implementation",
                                "Document new functionality"));
                implementationAdvice.add("Implement the feature incrementally, starting with the core functionality");
            }

            if (lowerType.contains("perform")) {
                addUniqueRecommendation(recommendations, seenSignatures, idCounter,
                        RecommendationCategory.PERFORMANCE, RecommendationPriority.HIGH,
                        "Establish performance baseline",
                        "For performance-related workflows, establish a performance baseline before making changes.",
                        "A performance baseline enables accurate measurement of improvements.",
                        List.of("Measure current performance metrics before changes",
                                "Define target performance improvements",
                                "Verify improvements after implementation"));
            }
        }
    }

    // =========================================================
    // Helper Methods
    // =========================================================

    private void addUniqueRecommendation(
            List<Recommendation> recommendations,
            Set<String> seenSignatures,
            AtomicInteger idCounter,
            RecommendationCategory category,
            RecommendationPriority priority,
            String title,
            String description,
            String rationale,
            List<String> actionItems) {

        String signature = category.name() + "|" + (title != null ? title : "") + "|" + (description != null ? description : "");
        if (seenSignatures.contains(signature)) {
            logger.debug("Skipping duplicate recommendation: {}", title);
            return;
        }
        seenSignatures.add(signature);

        String id = String.format("REC-%04d", idCounter.incrementAndGet());
        Recommendation rec = new Recommendation(id, category, priority, title, description, rationale);
        rec.setActionItems(actionItems);
        recommendations.add(rec);
    }

    private int priorityOrder(RecommendationPriority priority) {
        return switch (priority) {
            case CRITICAL -> 0;
            case HIGH -> 1;
            case MEDIUM -> 2;
            case LOW -> 3;
        };
    }

    private RecommendationPriority mapRiskSeverityToPriority(String severity) {
        if (severity == null) return RecommendationPriority.MEDIUM;
        return switch (severity.toUpperCase()) {
            case "CRITICAL" -> RecommendationPriority.CRITICAL;
            case "HIGH" -> RecommendationPriority.HIGH;
            case "MEDIUM" -> RecommendationPriority.MEDIUM;
            case "LOW" -> RecommendationPriority.LOW;
            default -> RecommendationPriority.MEDIUM;
        };
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return "";
        if (value.length() <= maxLength) return value;
        return value.substring(0, maxLength - 3) + "...";
    }

    private String buildExecutiveSummary(
            String workflowName,
            String workflowType,
            List<Recommendation> recommendations,
            List<String> implementationAdvice,
            List<String> testingRecs,
            RepositoryHealthResponse healthResponse) {

        StringBuilder summary = new StringBuilder();
        summary.append("Recommendation Report for ");
        summary.append(workflowName != null ? workflowName : "unnamed workflow");
        if (workflowType != null && !workflowType.isEmpty()) {
            summary.append(" (").append(workflowType).append(")");
        }
        summary.append(". ");

        int total = recommendations.size();
        long critical = recommendations.stream().filter(r -> r.getPriority() == RecommendationPriority.CRITICAL).count();
        long high = recommendations.stream().filter(r -> r.getPriority() == RecommendationPriority.HIGH).count();
        long medium = recommendations.stream().filter(r -> r.getPriority() == RecommendationPriority.MEDIUM).count();
        long low = recommendations.stream().filter(r -> r.getPriority() == RecommendationPriority.LOW).count();

        summary.append("Generated ").append(total).append(" recommendation(s): ");
        summary.append(critical).append(" critical, ");
        summary.append(high).append(" high, ");
        summary.append(medium).append(" medium, ");
        summary.append(low).append(" low. ");

        if (healthResponse != null) {
            summary.append("Repository health score: ").append(healthResponse.getHealthScore()).append("/100. ");
        }

        if (!implementationAdvice.isEmpty()) {
            summary.append(implementationAdvice.size()).append(" implementation advice item(s) provided. ");
        }
        if (!testingRecs.isEmpty()) {
            summary.append(testingRecs.size()).append(" testing recommendation(s) provided. ");
        }

        summary.append("Review all recommendations before proceeding with implementation.");
        return summary.toString();
    }

    private int computeConfidenceScore(List<Recommendation> recommendations, List<String> errors) {
        int score = 100;

        if (errors != null && !errors.isEmpty()) {
            score -= errors.size() * 10;
        }

        if (recommendations.isEmpty()) {
            score -= 50;
        }

        boolean hasCriticalOrHigh = recommendations.stream()
                .anyMatch(r -> r.getPriority() == RecommendationPriority.CRITICAL
                        || r.getPriority() == RecommendationPriority.HIGH);
        if (!hasCriticalOrHigh && !recommendations.isEmpty()) {
            score -= 10;
        }

        return Math.max(0, Math.min(100, score));
    }
}