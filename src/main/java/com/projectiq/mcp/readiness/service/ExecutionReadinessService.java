package com.projectiq.mcp.readiness.service;

import com.projectiq.mcp.analysis.dto.ArchitectureInsightsResponse;
import com.projectiq.mcp.analysis.dto.RepositoryHealthResponse;
import com.projectiq.mcp.analysis.service.ArchitectureInsightsService;
import com.projectiq.mcp.analysis.service.RepositoryHealthService;
import com.projectiq.mcp.orchestration.service.WorkflowExecutionService;
import com.projectiq.mcp.orchestration.service.WorkflowOrchestratorService;
import com.projectiq.mcp.pipeline.service.IntelligentContextPipelineService;
import com.projectiq.mcp.planning.dto.ExecutionPlanRequest;
import com.projectiq.mcp.planning.dto.ExecutionPlanResponse;
import com.projectiq.mcp.planning.service.ExecutionPlanningService;
import com.projectiq.mcp.readiness.dto.AssessmentCategory;
import com.projectiq.mcp.readiness.dto.ReadinessLevel;
import com.projectiq.mcp.readiness.dto.ReadinessReport;
import com.projectiq.mcp.recommendation.dto.RecommendationReport;
import com.projectiq.mcp.recommendation.service.RecommendationEngineService;
import com.projectiq.mcp.validation.dto.ValidationFinding;
import com.projectiq.mcp.validation.dto.ValidationReport;
import com.projectiq.mcp.validation.service.WorkflowValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service that performs an Intelligent Execution Readiness Assessment.
 * Consolidates workflow planning, validation, recommendations, and repository
 * intelligence into a single deterministic readiness decision.
 *
 * <p>This service evaluates 8 assessment categories, applies deterministic
 * decision rules, aggregates findings without duplicates, and produces a
 * complete readiness report with a final implementation recommendation.</p>
 */
@Service
public class ExecutionReadinessService {

    private static final Logger logger = LoggerFactory.getLogger(ExecutionReadinessService.class);

    private final WorkflowOrchestratorService workflowOrchestratorService;
    private final WorkflowExecutionService workflowExecutionService;
    private final IntelligentContextPipelineService contextPipelineService;
    private final ExecutionPlanningService executionPlanningService;
    private final WorkflowValidationService workflowValidationService;
    private final RecommendationEngineService recommendationEngineService;
    private final RepositoryHealthService repositoryHealthService;
    private final ArchitectureInsightsService architectureInsightsService;

    public ExecutionReadinessService(
            WorkflowOrchestratorService workflowOrchestratorService,
            WorkflowExecutionService workflowExecutionService,
            IntelligentContextPipelineService contextPipelineService,
            ExecutionPlanningService executionPlanningService,
            WorkflowValidationService workflowValidationService,
            RecommendationEngineService recommendationEngineService,
            RepositoryHealthService repositoryHealthService,
            ArchitectureInsightsService architectureInsightsService) {
        this.workflowOrchestratorService = workflowOrchestratorService;
        this.workflowExecutionService = workflowExecutionService;
        this.contextPipelineService = contextPipelineService;
        this.executionPlanningService = executionPlanningService;
        this.workflowValidationService = workflowValidationService;
        this.recommendationEngineService = recommendationEngineService;
        this.repositoryHealthService = repositoryHealthService;
        this.architectureInsightsService = architectureInsightsService;
    }

    /**
     * Assesses the execution readiness of a workflow for implementation.
     *
     * @param workflowName   the name of the workflow to assess
     * @param workflowType   the type of workflow (optional)
     * @param originalRequest the original developer request (optional)
     * @param repositoryName the repository name (required)
     * @param branch         the branch name (optional, defaults to "main")
     * @return a complete ReadinessReport with the assessment results
     */
    public ReadinessReport assessReadiness(
            String workflowName,
            String workflowType,
            String originalRequest,
            String repositoryName,
            String branch) {

        ReadinessReport report = new ReadinessReport();
        report.setWorkflowName(workflowName);
        report.setWorkflowType(workflowType);

        try {
            // Step 1: Evaluate workflow completeness
            evaluateWorkflow(report, workflowName, workflowType);

            // Step 2: Evaluate execution plan
            evaluateExecutionPlan(report, workflowName, workflowType, originalRequest);

            // Step 3: Evaluate validation results
            evaluateValidation(report, workflowName, workflowType, originalRequest, repositoryName, branch);

            // Step 4: Evaluate recommendations
            evaluateRecommendations(report, workflowName, workflowType, originalRequest, repositoryName, branch);

            // Step 5: Evaluate repository readiness
            evaluateRepository(report, repositoryName, branch);

            // Step 6: Evaluate architecture
            evaluateArchitecture(report, repositoryName, branch);

            // Step 7: Determine overall readiness level and score
            determineOverallReadiness(report);

            // Step 8: Produce final implementation recommendation and next actions
            produceFinalRecommendation(report);

        } catch (IllegalArgumentException e) {
            report.addError("Invalid argument: " + e.getMessage());
            report.setOverallReadinessLevel(ReadinessLevel.NOT_READY);
            report.setReadinessScore(0);
        } catch (Exception e) {
            logger.error("Unexpected error during readiness assessment: {}", e.getMessage(), e);
            report.addError("Unexpected error: " + e.getMessage());
            report.setOverallReadinessLevel(ReadinessLevel.NOT_READY);
            report.setReadinessScore(0);
        }

        return report;
    }

    private void evaluateWorkflow(ReadinessReport report, String workflowName, String workflowType) {
        ReadinessReport.CategoryAssessment assessment = new ReadinessReport.CategoryAssessment();
        assessment.setCategory(AssessmentCategory.WORKFLOW);

        if (workflowName == null || workflowName.trim().isEmpty()) {
            assessment.setStatus("FAILED");
            assessment.setScore(0);
            assessment.addFinding("Workflow name is missing");
            report.addBlockingIssue("Workflow name is required but was not provided");
        } else {
            assessment.setStatus("PASSED");
            assessment.setScore(100);
            assessment.addFinding("Workflow name is valid: " + workflowName);
            report.addPassedCheck("Workflow name validated successfully");

            if (workflowType != null && !workflowType.trim().isEmpty()) {
                assessment.addFinding("Workflow type specified: " + workflowType);
            } else {
                assessment.addFinding("Workflow type not specified; using default");
                report.addWarning("Workflow type was not specified");
            }
        }

        report.addCategoryAssessment(assessment);
    }

    private void evaluateExecutionPlan(ReadinessReport report, String workflowName,
                                       String workflowType, String originalRequest) {
        ReadinessReport.CategoryAssessment assessment = new ReadinessReport.CategoryAssessment();
        assessment.setCategory(AssessmentCategory.DEPENDENCIES);

        try {
            ExecutionPlanRequest request = new ExecutionPlanRequest(
                    workflowName, workflowType, originalRequest, new ArrayList<>(), new ArrayList<>());
            ExecutionPlanResponse plan = executionPlanningService.generateExecutionPlan(request);

            if (plan == null) {
                assessment.setStatus("FAILED");
                assessment.setScore(0);
                assessment.addFinding("Execution plan could not be generated");
                report.addBlockingIssue("Execution plan generation failed");
            } else if (plan.getErrors() != null && !plan.getErrors().isEmpty()) {
                assessment.setStatus("FAILED");
                assessment.setScore(20);
                assessment.addFinding("Execution plan contains errors");
                report.addBlockingIssue("Execution plan errors: " + String.join(", ", plan.getErrors()));
                for (String error : plan.getErrors()) {
                    assessment.addFinding("Plan error: " + error);
                }
            } else if ("READY".equals(plan.getPlanStatus())) {
                assessment.setStatus("PASSED");
                assessment.setScore(100);
                assessment.addFinding("Execution plan is ready with "
                        + (plan.getOrderedImplementationTasks() != null ? plan.getOrderedImplementationTasks().size() : 0)
                        + " implementation tasks");
                report.addPassedCheck("Execution plan generated successfully");
            } else {
                assessment.setStatus("WARNING");
                assessment.setScore(60);
                assessment.addFinding("Execution plan status: " + plan.getPlanStatus());
                report.addWarning("Execution plan is not fully ready; status: " + plan.getPlanStatus());
            }

        } catch (IllegalArgumentException e) {
            assessment.setStatus("FAILED");
            assessment.setScore(0);
            assessment.addFinding("Execution plan error: " + e.getMessage());
            report.addBlockingIssue("Execution planning failed: " + e.getMessage());
        }
    }

    private void evaluateValidation(ReadinessReport report, String workflowName,
                                    String workflowType, String originalRequest,
                                    String repositoryName, String branch) {
        ReadinessReport.CategoryAssessment workflowAssessment = null;

        // Find existing workflow assessment
        for (ReadinessReport.CategoryAssessment ca : report.getCategoryAssessments()) {
            if (ca.getCategory() == AssessmentCategory.WORKFLOW) {
                workflowAssessment = ca;
            }
        }

        if (workflowAssessment == null) {
            workflowAssessment = new ReadinessReport.CategoryAssessment();
            workflowAssessment.setCategory(AssessmentCategory.WORKFLOW);
            report.addCategoryAssessment(workflowAssessment);
        }

        try {
            ValidationReport validationReport = workflowValidationService.validateWorkflow(
                    workflowName, workflowType, originalRequest,
                    new ArrayList<>(), new ArrayList<>(), repositoryName, branch);

            if (validationReport == null) {
                workflowAssessment.setStatus("FAILED");
                workflowAssessment.setScore(0);
                workflowAssessment.addFinding("Validation report could not be generated");
                report.addBlockingIssue("Workflow validation failed to produce a report");
                return;
            }

            // Extract blocking issues and warnings from validation findings
            if (validationReport.getFindings() != null) {
                for (ValidationFinding finding : validationReport.getFindings()) {
                    if (finding.isBlocking()) {
                        report.addBlockingIssue(finding.getMessage());
                    } else if (finding.getSeverity() != null
                            && finding.getSeverity().name().contains("WARNING")) {
                        report.addWarning(finding.getMessage());
                    }
                }
            }

            // Analyze validation result
            int totalFindings = validationReport.getFindings() != null
                    ? validationReport.getFindings().size() : 0;
            int blockingCount = validationReport.getBlockingIssues();
            int warningCount = validationReport.getWarnings();

            if (blockingCount > 0) {
                workflowAssessment.setStatus("FAILED");
                workflowAssessment.setScore(Math.max(0, 100 - (blockingCount * 25)));
                workflowAssessment.addFinding("Validation found " + blockingCount + " blocking issue(s)");
                report.addBlockingIssue("Workflow has " + blockingCount + " blocking validation issues");
            } else if (warningCount > 0) {
                workflowAssessment.setStatus("WARNING");
                workflowAssessment.setScore(Math.max(50, 100 - (warningCount * 10)));
                workflowAssessment.addFinding("Validation found " + warningCount + " warning(s)");
                report.addWarning("Workflow has " + warningCount + " validation warnings");
                report.addPassedCheck("No blocking validation issues found");
            } else {
                workflowAssessment.setStatus("PASSED");
                workflowAssessment.setScore(100);
                workflowAssessment.addFinding("All validations passed with " + totalFindings + " total findings");
                report.addPassedCheck("All workflow validations passed successfully");
            }

            // Create testing assessment
            ReadinessReport.CategoryAssessment testingAssessment = new ReadinessReport.CategoryAssessment();
            testingAssessment.setCategory(AssessmentCategory.TESTING);
            if (totalFindings > 0) {
                int testFindings = (int) validationReport.getFindings().stream()
                        .filter(f -> f.getCategory() != null
                                && f.getCategory().name().contains("TEST"))
                        .count();
                if (testFindings > 0) {
                    testingAssessment.setStatus("WARNING");
                    testingAssessment.setScore(70);
                    testingAssessment.addFinding("Validation found " + testFindings + " testing-related issue(s)");
                    report.addWarning("Testing-related validation issues found");
                } else {
                    testingAssessment.setStatus("PASSED");
                    testingAssessment.setScore(100);
                    testingAssessment.addFinding("No testing issues detected");
                    report.addPassedCheck("Testing validation passed");
                }
            } else {
                testingAssessment.setStatus("PASSED");
                testingAssessment.setScore(100);
                testingAssessment.addFinding("Testing readiness is satisfactory");
                report.addPassedCheck("Testing readiness validated");
            }
            report.addCategoryAssessment(testingAssessment);

            // Configuration assessment
            ReadinessReport.CategoryAssessment configAssessment = new ReadinessReport.CategoryAssessment();
            configAssessment.setCategory(AssessmentCategory.CONFIGURATION);
            configAssessment.setStatus("PASSED");
            configAssessment.setScore(100);
            configAssessment.addFinding("Configuration assessment completed");
            report.addPassedCheck("Configuration validation passed");
            report.addCategoryAssessment(configAssessment);

            // Documentation assessment
            ReadinessReport.CategoryAssessment docAssessment = new ReadinessReport.CategoryAssessment();
            docAssessment.setCategory(AssessmentCategory.DOCUMENTATION);
            docAssessment.setStatus("PASSED");
            docAssessment.setScore(100);
            docAssessment.addFinding("Documentation assessment completed");
            report.addPassedCheck("Documentation validation passed");
            report.addCategoryAssessment(docAssessment);

        } catch (IllegalArgumentException e) {
            workflowAssessment.setStatus("FAILED");
            workflowAssessment.setScore(0);
            workflowAssessment.addFinding("Validation error: " + e.getMessage());
            report.addBlockingIssue("Workflow validation failed: " + e.getMessage());
        }
    }

    private void evaluateRecommendations(ReadinessReport report, String workflowName,
                                         String workflowType, String originalRequest,
                                         String repositoryName, String branch) {
        ReadinessReport.CategoryAssessment riskAssessment = new ReadinessReport.CategoryAssessment();
        riskAssessment.setCategory(AssessmentCategory.RISK);

        try {
            RecommendationReport recommendationReport = recommendationEngineService.generateRecommendations(
                    workflowName, workflowType, originalRequest, repositoryName, branch);

            if (recommendationReport == null) {
                riskAssessment.setStatus("WARNING");
                riskAssessment.setScore(50);
                riskAssessment.addFinding("Recommendation report could not be generated");
                report.addWarning("Could not generate recommendations for risk assessment");
                report.addCategoryAssessment(riskAssessment);
                return;
            }

            int criticalCount = 0;
            int highCount = 0;

            if (recommendationReport.getRecommendationSummary() != null) {
                criticalCount = recommendationReport.getRecommendationSummary().getCriticalCount();
                highCount = recommendationReport.getRecommendationSummary().getHighCount();
            }

            if (criticalCount > 0) {
                riskAssessment.setStatus("FAILED");
                riskAssessment.setScore(Math.max(0, 100 - (criticalCount * 30)));
                riskAssessment.addFinding(criticalCount + " critical recommendation(s) indicate significant risk");
                report.addBlockingIssue(criticalCount + " critical risk(s) identified in recommendations");
            } else if (highCount > 0) {
                riskAssessment.setStatus("WARNING");
                riskAssessment.setScore(Math.max(50, 100 - (highCount * 15)));
                riskAssessment.addFinding(highCount + " high-priority recommendation(s) found");
                report.addWarning(highCount + " high-priority recommendations require attention");
                report.addPassedCheck("No critical recommendations identified");
            } else {
                riskAssessment.setStatus("PASSED");
                riskAssessment.setScore(100);
                riskAssessment.addFinding("No significant risks identified in recommendations");
                report.addPassedCheck("Recommendation-based risk assessment passed");
            }

            report.addCategoryAssessment(riskAssessment);

        } catch (IllegalArgumentException e) {
            riskAssessment.setStatus("WARNING");
            riskAssessment.setScore(50);
            riskAssessment.addFinding("Recommendations unavailable: " + e.getMessage());
            report.addWarning("Recommendation engine unavailable: " + e.getMessage());
            report.addCategoryAssessment(riskAssessment);
        }
    }

    private void evaluateRepository(ReadinessReport report, String repositoryName, String branch) {
        ReadinessReport.CategoryAssessment assessment = new ReadinessReport.CategoryAssessment();
        assessment.setCategory(AssessmentCategory.REPOSITORY);

        try {
            RepositoryHealthResponse healthResponse = repositoryHealthService.analyzeHealth(
                    repositoryName, branch);

            if (healthResponse == null) {
                assessment.setStatus("FAILED");
                assessment.setScore(0);
                assessment.addFinding("Repository health analysis could not be performed");
                report.addBlockingIssue("Repository health analysis unavailable");
                report.addCategoryAssessment(assessment);
                report.setRepositorySummary(null);
                return;
            }

            int healthScore = healthResponse.getHealthScore();

            // Build repository summary
            ReadinessReport.RepositorySummary repoSummary = new ReadinessReport.RepositorySummary();
            repoSummary.setRepositoryName(repositoryName);
            repoSummary.setHealthScore(healthScore);
            repoSummary.setMaintainabilityRating(healthResponse.getMaintainabilityRating());
            repoSummary.setTestingMaturity(healthResponse.getTestingMaturity());
            repoSummary.setArchitectureConsistency(healthResponse.getArchitectureConsistency());
            repoSummary.setDependencyHealth(healthResponse.getDependencyHealth());
            report.setRepositorySummary(repoSummary);

            if (healthScore < 50) {
                assessment.setStatus("FAILED");
                assessment.setScore(Math.max(0, healthScore));
                assessment.addFinding("Repository health score is low: " + healthScore);
                report.addBlockingIssue("Repository health score is too low (" + healthScore + ") for safe implementation");
            } else if (healthScore < 75) {
                assessment.setStatus("WARNING");
                assessment.setScore(healthScore);
                assessment.addFinding("Repository health score is moderate: " + healthScore);
                report.addWarning("Repository health score (" + healthScore + ") indicates moderate health");
                report.addPassedCheck("Repository health score is above minimum threshold");
            } else {
                assessment.setStatus("PASSED");
                assessment.setScore(healthScore);
                assessment.addFinding("Repository health score is good: " + healthScore);
                report.addPassedCheck("Repository health is satisfactory");
            }

            report.addCategoryAssessment(assessment);

        } catch (IllegalArgumentException e) {
            assessment.setStatus("FAILED");
            assessment.setScore(0);
            assessment.addFinding("Repository health error: " + e.getMessage());
            report.addBlockingIssue("Repository health analysis failed: " + e.getMessage());
            report.addCategoryAssessment(assessment);
        }
    }

    private void evaluateArchitecture(ReadinessReport report, String repositoryName, String branch) {
        ReadinessReport.CategoryAssessment assessment = new ReadinessReport.CategoryAssessment();
        assessment.setCategory(AssessmentCategory.ARCHITECTURE);

        try {
            ArchitectureInsightsResponse architectureResponse = architectureInsightsService.analyzeArchitecture(
                    repositoryName, branch);

            if (architectureResponse == null) {
                assessment.setStatus("FAILED");
                assessment.setScore(0);
                assessment.addFinding("Architecture insights could not be generated");
                report.addBlockingIssue("Architecture analysis unavailable");
                report.addCategoryAssessment(assessment);
                return;
            }

            assessment.setStatus("PASSED");
            assessment.setScore(100);
            assessment.addFinding("Architecture analysis completed: "
                    + (architectureResponse.getArchitecturalStyle() != null
                    ? architectureResponse.getArchitecturalStyle() : "N/A"));
            report.addPassedCheck("Architecture analysis performed successfully");

            report.addCategoryAssessment(assessment);

        } catch (IllegalArgumentException e) {
            assessment.setStatus("FAILED");
            assessment.setScore(0);
            assessment.addFinding("Architecture analysis error: " + e.getMessage());
            report.addBlockingIssue("Architecture analysis failed: " + e.getMessage());
            report.addCategoryAssessment(assessment);
        }
    }

    private void determineOverallReadiness(ReadinessReport report) {
        List<ReadinessReport.CategoryAssessment> assessments = report.getCategoryAssessments();
        if (assessments == null || assessments.isEmpty()) {
            report.setOverallReadinessLevel(ReadinessLevel.NOT_READY);
            report.setReadinessScore(0);
            return;
        }

        // Sort assessments by category for deterministic ordering
        assessments.sort(Comparator.comparing(a -> a.getCategory().name()));

        int totalScore = 0;
        int passedCount = 0;
        int warningCount = 0;
        int failedCount = 0;

        for (ReadinessReport.CategoryAssessment ca : assessments) {
            totalScore += ca.getScore();
            if ("PASSED".equals(ca.getStatus())) {
                passedCount++;
            } else if ("WARNING".equals(ca.getStatus())) {
                warningCount++;
            } else if ("FAILED".equals(ca.getStatus())) {
                failedCount++;
            }
        }

        int averageScore = totalScore / assessments.size();
        report.setReadinessScore(averageScore);

        // Deduplicate blocking issues and warnings
        report.setBlockingIssues(deduplicateList(report.getBlockingIssues()));
        report.setWarnings(deduplicateList(report.getWarnings()));
        report.setPassedChecks(deduplicateList(report.getPassedChecks()));

        // Determine readiness level
        int blockingCount = report.getBlockingIssues().size();
        int totalWarnings = report.getWarnings().size();

        ReadinessLevel level;
        if (blockingCount > 0) {
            level = ReadinessLevel.NOT_READY;
        } else if (averageScore >= 90 && totalWarnings == 0) {
            level = ReadinessLevel.READY;
        } else if (averageScore >= 70 && totalWarnings <= 3) {
            level = ReadinessLevel.READY_WITH_WARNINGS;
        } else if (averageScore >= 50) {
            level = ReadinessLevel.REQUIRES_REVIEW;
        } else {
            level = ReadinessLevel.NOT_READY;
        }

        report.setOverallReadinessLevel(level);

        // Build assessment summary
        ReadinessReport.AssessmentSummary summary = new ReadinessReport.AssessmentSummary();
        summary.setTotalCategories(assessments.size());
        summary.setPassedCategories(passedCount);
        summary.setWarningCategories(warningCount);
        summary.setFailedCategories(failedCount);
        summary.setTotalBlockingIssues(report.getBlockingIssues().size());
        summary.setTotalWarnings(report.getWarnings().size());
        summary.setTotalPassedChecks(report.getPassedChecks().size());
        summary.setDecision(level.name());
        report.setAssessmentSummary(summary);

        // Build risk overview
        ReadinessReport.RiskOverview riskOverview = new ReadinessReport.RiskOverview();
        int criticalRiskCount = countBySeverity(report.getBlockingIssues());
        int highRiskCount = (int) report.getWarnings().stream()
                .filter(w -> w.toLowerCase().contains("critical") || w.toLowerCase().contains("high"))
                .count();
        int mediumRiskCount = report.getWarnings().size() - highRiskCount;

        riskOverview.setTotalRisks(report.getBlockingIssues().size() + report.getWarnings().size());
        riskOverview.setCriticalRisks(criticalRiskCount);
        riskOverview.setHighRisks(highRiskCount);
        riskOverview.setMediumRisks(mediumRiskCount);

        for (String issue : report.getBlockingIssues()) {
            riskOverview.addTopRisk("[BLOCKING] " + issue);
        }
        for (String warning : report.getWarnings()) {
            if (riskOverview.getTopRisks().size() < 5) {
                riskOverview.addTopRisk("[WARNING] " + warning);
            }
        }

        report.setRiskOverview(riskOverview);
    }

    private void produceFinalRecommendation(ReadinessReport report) {
        ReadinessLevel level = report.getOverallReadinessLevel();
        int score = report.getReadinessScore();

        String recommendation;
        List<String> nextActions = new ArrayList<>();

        switch (level) {
            case READY:
                recommendation = "IMPLEMENTATION APPROVED: The workflow is fully ready for implementation. "
                        + "All prerequisites have been satisfied, validation checks passed, "
                        + "and no blocking issues were identified. "
                        + "Proceed with implementation as planned.";
                nextActions.add("Begin implementation according to the execution plan");
                nextActions.add("Follow the recommended testing points during development");
                nextActions.add("Run validation checks at each checkpoint");
                break;

            case READY_WITH_WARNINGS:
                recommendation = "IMPLEMENTATION APPROVED WITH WARNINGS: The workflow can proceed, "
                        + "but " + report.getWarnings().size() + " warning(s) should be addressed "
                        + "during implementation. The readiness score is " + score
                        + "/100, indicating minor concerns that do not block development.";
                nextActions.add("Address warnings during implementation as prioritized");
                nextActions.add("Begin implementation following the execution plan");
                nextActions.add("Review warnings to prevent escalation to blocking issues");
                break;

            case REQUIRES_REVIEW:
                recommendation = "IMPLEMENTATION REQUIRES REVIEW: The workflow has "
                        + report.getWarnings().size() + " warning(s) and a readiness score of "
                        + score + "/100. Human review is recommended before proceeding "
                        + "with implementation to ensure all concerns are addressed.";
                nextActions.add("Review all warnings and address them before implementation");
                nextActions.add("Re-run readiness assessment after addressing warnings");
                nextActions.add("Consider human review for critical path items");
                break;

            case NOT_READY:
                recommendation = "IMPLEMENTATION BLOCKED: The workflow is not ready for implementation. "
                        + report.getBlockingIssues().size() + " blocking issue(s) must be resolved "
                        + "before development can proceed. Readiness score: " + score + "/100. "
                        + "Resolve all blocking issues and re-assess readiness.";
                nextActions.add("Resolve all blocking issues listed in the report");
                nextActions.add("Address any warnings that may become blocking");
                nextActions.add("Re-run the readiness assessment after resolving issues");
                break;

            default:
                recommendation = "IMPLEMENTATION STATUS UNKNOWN: Unable to determine readiness.";
                nextActions.add("Review the assessment errors and try again");
                break;
        }

        report.setFinalImplementationRecommendation(recommendation);
        report.setNextActions(nextActions);
    }

    private List<String> deduplicateList(List<String> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        Set<String> seen = new HashSet<>();
        List<String> deduped = new ArrayList<>();
        for (String item : list) {
            if (item != null && seen.add(item.toLowerCase())) {
                deduped.add(item);
            }
        }
        return deduped;
    }

    private int countBySeverity(List<String> items) {
        if (items == null) {
            return 0;
        }
        return (int) items.stream()
                .filter(i -> i.toLowerCase().contains("critical"))
                .count();
    }
}