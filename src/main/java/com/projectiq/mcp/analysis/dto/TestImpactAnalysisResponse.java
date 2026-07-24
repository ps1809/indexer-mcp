package com.projectiq.mcp.analysis.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the complete result of analyzing the test impact of a proposed
 * development task. Contains the original task, affected production classes,
 * related test classes, missing tests, recommended test execution order,
 * estimated testing effort, confidence level, and testing rationale.
 *
 * <p>This response is deterministic and does not include any generated code
 * or test execution. It is designed to guide AI coding agents in focusing
 * validation efforts on the most relevant test suite.</p>
 */
public class TestImpactAnalysisResponse {

    private String originalTask;
    private List<String> affectedProductionClasses;
    private List<String> relatedTestClasses;
    private List<String> missingTests;
    private List<String> recommendedTestExecutionOrder;
    private String estimatedTestingEffort;
    private String confidenceLevel;
    private String testingRationale;

    public TestImpactAnalysisResponse() {
        this.affectedProductionClasses = new ArrayList<>();
        this.relatedTestClasses = new ArrayList<>();
        this.missingTests = new ArrayList<>();
        this.recommendedTestExecutionOrder = new ArrayList<>();
    }

    public String getOriginalTask() {
        return originalTask;
    }

    public void setOriginalTask(String originalTask) {
        this.originalTask = originalTask;
    }

    public List<String> getAffectedProductionClasses() {
        return affectedProductionClasses;
    }

    public void setAffectedProductionClasses(List<String> affectedProductionClasses) {
        this.affectedProductionClasses = affectedProductionClasses;
    }

    public void addAffectedProductionClass(String productionClass) {
        if (this.affectedProductionClasses == null) {
            this.affectedProductionClasses = new ArrayList<>();
        }
        this.affectedProductionClasses.add(productionClass);
    }

    public List<String> getRelatedTestClasses() {
        return relatedTestClasses;
    }

    public void setRelatedTestClasses(List<String> relatedTestClasses) {
        this.relatedTestClasses = relatedTestClasses;
    }

    public void addRelatedTestClass(String testClass) {
        if (this.relatedTestClasses == null) {
            this.relatedTestClasses = new ArrayList<>();
        }
        this.relatedTestClasses.add(testClass);
    }

    public List<String> getMissingTests() {
        return missingTests;
    }

    public void setMissingTests(List<String> missingTests) {
        this.missingTests = missingTests;
    }

    public void addMissingTest(String missingTest) {
        if (this.missingTests == null) {
            this.missingTests = new ArrayList<>();
        }
        this.missingTests.add(missingTest);
    }

    public List<String> getRecommendedTestExecutionOrder() {
        return recommendedTestExecutionOrder;
    }

    public void setRecommendedTestExecutionOrder(List<String> recommendedTestExecutionOrder) {
        this.recommendedTestExecutionOrder = recommendedTestExecutionOrder;
    }

    public void addRecommendedTestExecutionStep(String step) {
        if (this.recommendedTestExecutionOrder == null) {
            this.recommendedTestExecutionOrder = new ArrayList<>();
        }
        this.recommendedTestExecutionOrder.add(step);
    }

    public String getEstimatedTestingEffort() {
        return estimatedTestingEffort;
    }

    public void setEstimatedTestingEffort(String estimatedTestingEffort) {
        this.estimatedTestingEffort = estimatedTestingEffort;
    }

    public String getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(String confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }

    public String getTestingRationale() {
        return testingRationale;
    }

    public void setTestingRationale(String testingRationale) {
        this.testingRationale = testingRationale;
    }
}