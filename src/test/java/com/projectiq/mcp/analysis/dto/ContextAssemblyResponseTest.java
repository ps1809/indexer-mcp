package com.projectiq.mcp.analysis.dto;

import com.projectiq.mcp.analysis.dto.TaskAnalysisResponse.ExecutionStep;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ContextAssemblyResponse}.
 */
class ContextAssemblyResponseTest {

    @Test
    void testDefaultConstructorInitializesLists() {
        ContextAssemblyResponse response = new ContextAssemblyResponse();

        assertNotNull(response.getExecutedTools());
        assertNotNull(response.getSkippedTools());
        assertNotNull(response.getFailedTools());
        assertNotNull(response.getExecutionPlan());
        assertTrue(response.getExecutedTools().isEmpty());
        assertTrue(response.getSkippedTools().isEmpty());
        assertTrue(response.getFailedTools().isEmpty());
        assertTrue(response.getExecutionPlan().isEmpty());
    }

    @Test
    void testSetAndGetOriginalTask() {
        ContextAssemblyResponse response = new ContextAssemblyResponse();
        response.setOriginalTask("Add pagination");
        assertEquals("Add pagination", response.getOriginalTask());
    }

    @Test
    void testSetAndGetTaskAnalysis() {
        ContextAssemblyResponse response = new ContextAssemblyResponse();
        TaskAnalysisResponse analysis = new TaskAnalysisResponse();
        analysis.setOriginalTask("Test task");
        response.setTaskAnalysis(analysis);
        assertNotNull(response.getTaskAnalysis());
        assertEquals("Test task", response.getTaskAnalysis().getOriginalTask());
    }

    @Test
    void testSetAndGetExecutionPlan() {
        ContextAssemblyResponse response = new ContextAssemblyResponse();
        List<ExecutionStep> plan = new ArrayList<>();
        plan.add(new ExecutionStep(1, "repository_summary", "Get summary"));
        plan.add(new ExecutionStep(2, "search_code", "Search code"));
        response.setExecutionPlan(plan);

        assertEquals(2, response.getExecutionPlan().size());
        assertEquals("repository_summary", response.getExecutionPlan().get(0).getToolName());
    }

    @Test
    void testSetExecutionPlanWithNull() {
        ContextAssemblyResponse response = new ContextAssemblyResponse();
        response.setExecutionPlan(null);
        assertNotNull(response.getExecutionPlan());
        assertTrue(response.getExecutionPlan().isEmpty());
    }

    @Test
    void testAddExecutedTool() {
        ContextAssemblyResponse response = new ContextAssemblyResponse();
        response.addExecutedTool("repository_summary");
        response.addExecutedTool("search_code");

        assertEquals(2, response.getExecutedTools().size());
        assertTrue(response.getExecutedTools().contains("repository_summary"));
        assertTrue(response.getExecutedTools().contains("search_code"));
    }

    @Test
    void testSetExecutedToolsWithNull() {
        ContextAssemblyResponse response = new ContextAssemblyResponse();
        response.setExecutedTools(null);
        assertNotNull(response.getExecutedTools());
        assertTrue(response.getExecutedTools().isEmpty());
    }

    @Test
    void testAddSkippedTool() {
        ContextAssemblyResponse response = new ContextAssemblyResponse();
        response.addSkippedTool("duplicate_tool (duplicate)");

        assertEquals(1, response.getSkippedTools().size());
        assertTrue(response.getSkippedTools().get(0).contains("duplicate"));
    }

    @Test
    void testSetSkippedToolsWithNull() {
        ContextAssemblyResponse response = new ContextAssemblyResponse();
        response.setSkippedTools(null);
        assertNotNull(response.getSkippedTools());
        assertTrue(response.getSkippedTools().isEmpty());
    }

    @Test
    void testAddFailedTool() {
        ContextAssemblyResponse response = new ContextAssemblyResponse();
        response.addFailedTool("repository_summary: Connection refused");

        assertEquals(1, response.getFailedTools().size());
        assertTrue(response.getFailedTools().get(0).contains("Connection refused"));
    }

    @Test
    void testSetFailedToolsWithNull() {
        ContextAssemblyResponse response = new ContextAssemblyResponse();
        response.setFailedTools(null);
        assertNotNull(response.getFailedTools());
        assertTrue(response.getFailedTools().isEmpty());
    }

    @Test
    void testSetAndGetRepositoryContext() {
        ContextAssemblyResponse response = new ContextAssemblyResponse();
        com.projectiq.mcp.client.dto.RepositoryContext repoContext =
                new com.projectiq.mcp.client.dto.RepositoryContext();
        repoContext.setRepositoryName("test-repo");
        response.setRepositoryContext(repoContext);

        assertNotNull(response.getRepositoryContext());
        assertEquals("test-repo", response.getRepositoryContext().getRepositoryName());
    }

    @Test
    void testSetAndGetDevelopmentContext() {
        ContextAssemblyResponse response = new ContextAssemblyResponse();
        com.projectiq.mcp.client.dto.DevelopmentContext devContext =
                new com.projectiq.mcp.client.dto.DevelopmentContext();
        devContext.setRepositoryName("test-repo");
        response.setDevelopmentContext(devContext);

        assertNotNull(response.getDevelopmentContext());
        assertEquals("test-repo", response.getDevelopmentContext().getRepositoryName());
    }

    @Test
    void testSetAndGetExecutionSummary() {
        ContextAssemblyResponse response = new ContextAssemblyResponse();
        response.setExecutionSummary("2 tools executed");
        assertEquals("2 tools executed", response.getExecutionSummary());
    }

    @Test
    void testSetAndGetTotalExecutionTime() {
        ContextAssemblyResponse response = new ContextAssemblyResponse();
        response.setTotalExecutionTimeMillis(1234L);
        assertEquals(1234L, response.getTotalExecutionTimeMillis());
    }

    @Test
    void testAddExecutedToolWhenListIsNull() {
        ContextAssemblyResponse response = new ContextAssemblyResponse();
        // Simulate null executedTools list
        response.setExecutedTools(null);
        response.addExecutedTool("test_tool");
        assertEquals(1, response.getExecutedTools().size());
        assertEquals("test_tool", response.getExecutedTools().get(0));
    }

    @Test
    void testAddSkippedToolWhenListIsNull() {
        ContextAssemblyResponse response = new ContextAssemblyResponse();
        response.setSkippedTools(null);
        response.addSkippedTool("skipped_tool");
        assertEquals(1, response.getSkippedTools().size());
        assertEquals("skipped_tool", response.getSkippedTools().get(0));
    }

    @Test
    void testAddFailedToolWhenListIsNull() {
        ContextAssemblyResponse response = new ContextAssemblyResponse();
        response.setFailedTools(null);
        response.addFailedTool("failed_tool");
        assertEquals(1, response.getFailedTools().size());
        assertEquals("failed_tool", response.getFailedTools().get(0));
    }
}