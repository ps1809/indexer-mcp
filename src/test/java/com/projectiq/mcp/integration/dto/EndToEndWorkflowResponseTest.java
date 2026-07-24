package com.projectiq.mcp.integration.dto;

import com.projectiq.mcp.analysis.dto.TaskAnalysisResponse;
import com.projectiq.mcp.analysis.dto.TaskType;
import com.projectiq.mcp.analysis.dto.ConfidenceLevel;
import com.projectiq.mcp.analysis.dto.ComplexityLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EndToEndWorkflowResponseTest {

    @Test
    void testDefaultConstruction() {
        EndToEndWorkflowResponse response = new EndToEndWorkflowResponse();
        assertNull(response.getWorkflowId());
        assertNull(response.getOriginalRequest());
        assertNull(response.getRepositoryName());
        assertEquals(0, response.getTotalDurationMillis());
        assertNull(response.getOverallStatus());
        assertTrue(response.getErrors().isEmpty());
        assertTrue(response.getWarnings().isEmpty());
        assertFalse(response.hasErrors());
        assertFalse(response.hasWarnings());
    }

    @Test
    void testBuilderSetters() {
        EndToEndWorkflowResponse response = new EndToEndWorkflowResponse()
                .withWorkflowId("wf-001")
                .withOriginalRequest("Add new feature")
                .withRepositoryName("my-repo")
                .withTotalDurationMillis(1500L);

        assertEquals("wf-001", response.getWorkflowId());
        assertEquals("Add new feature", response.getOriginalRequest());
        assertEquals("my-repo", response.getRepositoryName());
        assertEquals(1500L, response.getTotalDurationMillis());
    }

    @Test
    void testAddErrorAndWarning() {
        EndToEndWorkflowResponse response = new EndToEndWorkflowResponse();
        response.addError("Error 1");
        response.addError("Error 2");
        response.addWarning("Warning 1");

        assertEquals(2, response.getErrors().size());
        assertEquals(1, response.getWarnings().size());
        assertTrue(response.hasErrors());
        assertTrue(response.hasWarnings());
        assertEquals("Error 1", response.getErrors().get(0));
        assertEquals("Warning 1", response.getWarnings().get(0));
    }

    @Test
    void testSetTaskAnalysis() {
        EndToEndWorkflowResponse response = new EndToEndWorkflowResponse();
        TaskAnalysisResponse taskAnalysis = new TaskAnalysisResponse();
        taskAnalysis.setTaskType(TaskType.NEW_FEATURE);
        taskAnalysis.setConfidenceLevel(ConfidenceLevel.HIGH);
        taskAnalysis.setEstimatedComplexity(ComplexityLevel.MEDIUM);

        response.setTaskAnalysis(taskAnalysis);
        assertNotNull(response.getTaskAnalysis());
        assertEquals(TaskType.NEW_FEATURE, response.getTaskAnalysis().getTaskType());
        assertEquals(ConfidenceLevel.HIGH, response.getTaskAnalysis().getConfidenceLevel());
    }

    @Test
    void testSetOverallStatus() {
        EndToEndWorkflowResponse response = new EndToEndWorkflowResponse();
        response.setOverallStatus("COMPLETED");
        assertEquals("COMPLETED", response.getOverallStatus());

        response.setOverallStatus("COMPLETED_WITH_WARNINGS");
        assertEquals("COMPLETED_WITH_WARNINGS", response.getOverallStatus());

        response.setOverallStatus("FAILED");
        assertEquals("FAILED", response.getOverallStatus());
    }

    @Test
    void testSetHandoffPackage() {
        EndToEndWorkflowResponse response = new EndToEndWorkflowResponse();
        String handoffJson = "{\"sessionId\":\"test-session\"}";
        response.setHandoffPackage(handoffJson);
        assertEquals(handoffJson, response.getHandoffPackage());
    }

    @Test
    void testSetErrorsAndWarnings() {
        EndToEndWorkflowResponse response = new EndToEndWorkflowResponse();
        response.setErrors(java.util.List.of("err1", "err2"));
        response.setWarnings(java.util.List.of("warn1"));

        assertEquals(2, response.getErrors().size());
        assertEquals(1, response.getWarnings().size());
        assertTrue(response.hasErrors());
        assertTrue(response.hasWarnings());
    }

    @Test
    void testEmptyErrorsAndWarnings() {
        EndToEndWorkflowResponse response = new EndToEndWorkflowResponse();
        response.setErrors(java.util.List.of());
        response.setWarnings(java.util.List.of());

        assertFalse(response.hasErrors());
        assertFalse(response.hasWarnings());
    }

    @Test
    void testNullErrorsAndWarnings() {
        EndToEndWorkflowResponse response = new EndToEndWorkflowResponse();
        response.setErrors(null);
        response.setWarnings(null);

        assertFalse(response.hasErrors());
        assertFalse(response.hasWarnings());
    }
}