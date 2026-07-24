package com.projectiq.mcp.session.dto;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.projectiq.mcp.orchestration.dto.WorkflowType;
import com.projectiq.mcp.session.dto.DevelopmentSession.ExecutionHistoryEntry;
import com.projectiq.mcp.session.dto.DevelopmentSession.SessionSummary;

import java.util.List;

class DevelopmentSessionTest {

    @Test
    void testDefaultConstruction() {
        DevelopmentSession session = new DevelopmentSession();
        assertThat(session.getStatus()).isEqualTo(SessionStatus.CREATED);
        assertThat(session.getWorkflowProgress()).isEqualTo(0.0);
        assertThat(session.getCompletedStages()).isEmpty();
        assertThat(session.getPendingStages()).isEmpty();
        assertThat(session.getExecutionHistory()).isEmpty();
        assertThat(session.getCreatedAt()).isNotNull();
        assertThat(session.getUpdatedAt()).isNotNull();
    }

    @Test
    void testSettersAndGetters() {
        DevelopmentSession session = new DevelopmentSession();
        session.setSessionId("test-session-123");
        session.setRepositoryId("test-repo");
        session.setDeveloperRequest("Implement feature X");
        session.setWorkflowType(WorkflowType.FEATURE_IMPLEMENTATION);
        session.setStatus(SessionStatus.IN_PROGRESS);
        session.setCurrentStage("ANALYSIS");
        session.setWorkflowProgress(0.5);

        assertThat(session.getSessionId()).isEqualTo("test-session-123");
        assertThat(session.getRepositoryId()).isEqualTo("test-repo");
        assertThat(session.getDeveloperRequest()).isEqualTo("Implement feature X");
        assertThat(session.getWorkflowType()).isEqualTo(WorkflowType.FEATURE_IMPLEMENTATION);
        assertThat(session.getStatus()).isEqualTo(SessionStatus.IN_PROGRESS);
        assertThat(session.getCurrentStage()).isEqualTo("ANALYSIS");
        assertThat(session.getWorkflowProgress()).isEqualTo(0.5);
    }

    @Test
    void testCompletedStagesNoDuplicates() {
        DevelopmentSession session = new DevelopmentSession();
        session.addCompletedStage("ANALYSIS");
        session.addCompletedStage("ANALYSIS");
        assertThat(session.getCompletedStages()).hasSize(1);
        assertThat(session.getCompletedStages()).containsExactly("ANALYSIS");
    }

    @Test
    void testPendingStagesNoDuplicates() {
        DevelopmentSession session = new DevelopmentSession();
        session.addPendingStage("ANALYSIS");
        session.addPendingStage("ANALYSIS");
        assertThat(session.getPendingStages()).hasSize(1);
        assertThat(session.getPendingStages()).containsExactly("ANALYSIS");
    }

    @Test
    void testTimestamps() {
        DevelopmentSession session = new DevelopmentSession();
        session.setCompletedAt(java.time.LocalDateTime.now());
        session.setArchivedAt(java.time.LocalDateTime.now());
        assertThat(session.getCompletedAt()).isNotNull();
        assertThat(session.getArchivedAt()).isNotNull();
    }

    @Test
    void testExecutionHistoryEntry() {
        ExecutionHistoryEntry entry = new ExecutionHistoryEntry("STAGE1", "CREATE", "COMPLETED", "Test details");
        assertThat(entry.getStageName()).isEqualTo("STAGE1");
        assertThat(entry.getAction()).isEqualTo("CREATE");
        assertThat(entry.getStatus()).isEqualTo("COMPLETED");
        assertThat(entry.getDetails()).isEqualTo("Test details");
        assertThat(entry.getTimestamp()).isNotNull();
    }

    @Test
    void testSessionSummary() {
        SessionSummary summary = new SessionSummary();
        summary.setCurrentWorkflow("Feature Implementation");
        summary.setCompletedStages(List.of("ANALYSIS", "PLANNING"));
        summary.setPendingStages(List.of("EXECUTION"));
        summary.setRepositoryIntelligenceSummary("Context available");
        summary.setValidationSummary("All passed");
        summary.setRecommendationSummary("2 recommendations");
        summary.setReadinessStatus("READY");
        summary.setSuggestedNextStep("Proceed to execution");

        assertThat(summary.getCurrentWorkflow()).isEqualTo("Feature Implementation");
        assertThat(summary.getCompletedStages()).containsExactly("ANALYSIS", "PLANNING");
        assertThat(summary.getPendingStages()).containsExactly("EXECUTION");
        assertThat(summary.getRepositoryIntelligenceSummary()).isEqualTo("Context available");
        assertThat(summary.getValidationSummary()).isEqualTo("All passed");
        assertThat(summary.getRecommendationSummary()).isEqualTo("2 recommendations");
        assertThat(summary.getReadinessStatus()).isEqualTo("READY");
        assertThat(summary.getSuggestedNextStep()).isEqualTo("Proceed to execution");
    }
}