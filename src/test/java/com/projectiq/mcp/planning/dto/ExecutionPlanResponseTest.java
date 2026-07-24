package com.projectiq.mcp.planning.dto;

import com.projectiq.mcp.planning.dto.ExecutionPlanResponse.DependencyValidationInfo;
import com.projectiq.mcp.planning.dto.ExecutionPlanResponse.EffortEstimate;
import com.projectiq.mcp.planning.dto.ExecutionPlanResponse.ExecutionPhase;
import com.projectiq.mcp.planning.dto.ExecutionPlanResponse.ImplementationTask;
import com.projectiq.mcp.planning.dto.ExecutionPlanResponse.PlanningSummary;
import com.projectiq.mcp.planning.dto.ExecutionPlanResponse.RiskAssessment;
import com.projectiq.mcp.planning.dto.ExecutionPlanResponse.TestingPoint;
import com.projectiq.mcp.planning.dto.ExecutionPlanResponse.ValidationCheckpoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ExecutionPlanResponse and all its nested types.
 */
class ExecutionPlanResponseTest {

    @Test
    void testDefaultConstructor() {
        ExecutionPlanResponse response = new ExecutionPlanResponse();
        assertNotNull(response.getExecutionPhases());
        assertNotNull(response.getOrderedImplementationTasks());
        assertNotNull(response.getRequiredPrerequisites());
        assertNotNull(response.getValidationCheckpoints());
        assertNotNull(response.getRecommendedTestingPoints());
        assertNotNull(response.getPotentialRisks());
        assertNotNull(response.getCriticalPath());
        assertNotNull(response.getErrors());
        assertTrue(response.getExecutionPhases().isEmpty());
        assertTrue(response.getOrderedImplementationTasks().isEmpty());
    }

    @Test
    void testSettersAndGetters() {
        ExecutionPlanResponse response = new ExecutionPlanResponse();
        response.setWorkflowName("TestWorkflow");
        response.setWorkflowType("Feature Implementation");
        response.setOriginalRequest("Add feature X");
        response.setPlanStatus("READY");

        assertEquals("TestWorkflow", response.getWorkflowName());
        assertEquals("Feature Implementation", response.getWorkflowType());
        assertEquals("Add feature X", response.getOriginalRequest());
        assertEquals("READY", response.getPlanStatus());
    }

    @Test
    void testDependencyValidationInfo() {
        DependencyValidationInfo info = new DependencyValidationInfo();
        assertNotNull(info.getErrors());
        assertNotNull(info.getWarnings());
        assertTrue(info.getErrors().isEmpty());

        info.setValid(true);
        info.getErrors().add("error1");
        info.getWarnings().add("warning1");

        assertTrue(info.isValid());
        assertEquals(1, info.getErrors().size());
        assertEquals(1, info.getWarnings().size());
    }

    @Test
    void testExecutionPhase() {
        ExecutionPhase phase = new ExecutionPhase();
        phase.setName("Analysis");
        phase.setDescription("Analysis phase");
        phase.setOrder(1);
        phase.setStatus("PENDING");
        phase.setEstimatedEffort("Low");
        phase.addTask("Task1");
        phase.addTask("Task2");

        assertEquals("Analysis", phase.getName());
        assertEquals("Analysis phase", phase.getDescription());
        assertEquals(1, phase.getOrder());
        assertEquals("PENDING", phase.getStatus());
        assertEquals("Low", phase.getEstimatedEffort());
        assertEquals(2, phase.getTasks().size());
        assertEquals("Task1", phase.getTasks().get(0));
    }

    @Test
    void testImplementationTask() {
        ImplementationTask task = new ImplementationTask();
        task.setOrder(1);
        task.setName("Implement service");
        task.setDescription("Implement the service layer");
        task.setPhase("Implementation");
        task.setStatus("PENDING");
        task.setEstimatedComplexity("Medium");
        task.addDependency("Analyze");
        task.addRequiredContext("Context data");

        assertEquals(1, task.getOrder());
        assertEquals("Implement service", task.getName());
        assertEquals("Implementation", task.getPhase());
        assertEquals("Medium", task.getEstimatedComplexity());
        assertEquals(1, task.getDependencies().size());
        assertEquals(1, task.getRequiredContext().size());
    }

    @Test
    void testValidationCheckpoint() {
        ValidationCheckpoint cp = new ValidationCheckpoint();
        cp.setName("Mid-Plan Validation");
        cp.setDescription("Validate at midpoint");
        cp.setAfterTaskOrder(3);
        cp.setValidationType("INTEGRATION");

        assertEquals("Mid-Plan Validation", cp.getName());
        assertEquals(3, cp.getAfterTaskOrder());
        assertEquals("INTEGRATION", cp.getValidationType());
    }

    @Test
    void testTestingPoint() {
        TestingPoint tp = new TestingPoint();
        tp.setName("Integration Test");
        tp.setDescription("Test integration");
        tp.setAfterTaskOrder(5);
        tp.setTestScope("Integration");

        assertEquals("Integration Test", tp.getName());
        assertEquals(5, tp.getAfterTaskOrder());
        assertEquals("Integration", tp.getTestScope());
    }

    @Test
    void testRiskAssessment() {
        RiskAssessment risk = new RiskAssessment();
        risk.setDescription("High complexity risk");
        risk.setSeverity("HIGH");
        risk.setImpact("May cause delays");
        risk.setMitigation("Break into smaller tasks");

        assertEquals("High complexity risk", risk.getDescription());
        assertEquals("HIGH", risk.getSeverity());
        assertEquals("May cause delays", risk.getImpact());
        assertEquals("Break into smaller tasks", risk.getMitigation());
    }

    @Test
    void testEffortEstimate() {
        EffortEstimate effort = new EffortEstimate();
        effort.setOverallComplexity("MEDIUM");
        effort.setTotalTasks(5);
        effort.setEstimatedMinutes(120);
        effort.setDescription("Estimated effort description");

        assertEquals("MEDIUM", effort.getOverallComplexity());
        assertEquals(5, effort.getTotalTasks());
        assertEquals(120, effort.getEstimatedMinutes());
    }

    @Test
    void testPlanningSummary() {
        PlanningSummary summary = new PlanningSummary();
        summary.setTotalPhases(3);
        summary.setTotalTasks(10);
        summary.setValidatedDependencies(5);
        summary.setTotalRisks(2);
        summary.setCriticalPathLength(4);
        summary.setRecommendation("Start with analysis phase");

        assertEquals(3, summary.getTotalPhases());
        assertEquals(10, summary.getTotalTasks());
        assertEquals(5, summary.getValidatedDependencies());
        assertEquals(2, summary.getTotalRisks());
        assertEquals(4, summary.getCriticalPathLength());
    }

    @Test
    void testAddMethods() {
        ExecutionPlanResponse response = new ExecutionPlanResponse();

        ExecutionPhase phase = new ExecutionPhase();
        phase.setName("Test Phase");
        response.addExecutionPhase(phase);
        assertEquals(1, response.getExecutionPhases().size());

        ImplementationTask task = new ImplementationTask();
        task.setName("Test Task");
        response.addImplementationTask(task);
        assertEquals(1, response.getOrderedImplementationTasks().size());

        response.addRequiredPrerequisite("Context needed");
        assertEquals(1, response.getRequiredPrerequisites().size());

        ValidationCheckpoint cp = new ValidationCheckpoint();
        response.addValidationCheckpoint(cp);
        assertEquals(1, response.getValidationCheckpoints().size());

        TestingPoint tp = new TestingPoint();
        response.addTestingPoint(tp);
        assertEquals(1, response.getRecommendedTestingPoints().size());

        RiskAssessment risk = new RiskAssessment();
        response.addRisk(risk);
        assertEquals(1, response.getPotentialRisks().size());

        response.addCriticalPathStep("Step1");
        assertEquals(1, response.getCriticalPath().size());

        response.addError("Error1");
        assertEquals(1, response.getErrors().size());
    }
}