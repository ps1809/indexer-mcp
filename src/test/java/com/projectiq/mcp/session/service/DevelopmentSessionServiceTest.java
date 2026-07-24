package com.projectiq.mcp.session.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.projectiq.mcp.orchestration.dto.WorkflowType;
import com.projectiq.mcp.session.dto.DevelopmentSession;
import com.projectiq.mcp.session.dto.SessionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class DevelopmentSessionServiceTest {

    private DevelopmentSessionService service;

    @BeforeEach
    void setUp() {
        service = new DevelopmentSessionService();
    }

    @Test
    void testCreateSession() {
        DevelopmentSession session = service.createSession(
                "test-repo",
                "Implement feature X",
                WorkflowType.FEATURE_IMPLEMENTATION,
                List.of("ANALYSIS", "PLANNING", "EXECUTION"));

        assertThat(session.getSessionId()).isNotNull();
        assertThat(session.getRepositoryId()).isEqualTo("test-repo");
        assertThat(session.getDeveloperRequest()).isEqualTo("Implement feature X");
        assertThat(session.getWorkflowType()).isEqualTo(WorkflowType.FEATURE_IMPLEMENTATION);
        assertThat(session.getStatus()).isEqualTo(SessionStatus.CREATED);
        assertThat(session.getCurrentStage()).isEqualTo("INITIALIZED");
        assertThat(session.getWorkflowProgress()).isEqualTo(0.0);
        assertThat(session.getPendingStages()).containsExactly("ANALYSIS", "PLANNING", "EXECUTION");
        assertThat(session.getCompletedStages()).isEmpty();
        assertThat(session.getExecutionHistory()).hasSize(1);
        assertThat(session.getSessionSummary()).isNotNull();
    }

    @Test
    void testCreateSessionWithNullWorkflowType() {
        DevelopmentSession session = service.createSession(
                "test-repo", "Implement feature X", null, null);

        assertThat(session.getWorkflowType()).isEqualTo(WorkflowType.UNKNOWN);
        assertThat(session.getPendingStages()).isEmpty();
    }

    @Test
    void testCreateSessionMissingRepositoryId() {
        assertThatThrownBy(() -> service.createSession(null, "request", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Repository ID is required");
    }

    @Test
    void testCreateSessionMissingDeveloperRequest() {
        assertThatThrownBy(() -> service.createSession("repo", null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Developer request is required");
    }

    @Test
    void testLoadSession() {
        DevelopmentSession created = service.createSession(
                "test-repo", "Test request", null, null);

        DevelopmentSession loaded = service.loadSession(created.getSessionId());
        assertThat(loaded.getSessionId()).isEqualTo(created.getSessionId());
        assertThat(loaded.getRepositoryId()).isEqualTo("test-repo");
    }

    @Test
    void testLoadSessionNotFound() {
        assertThatThrownBy(() -> service.loadSession("nonexistent"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Session not found");
    }

    @Test
    void testLoadSessionNullId() {
        assertThatThrownBy(() -> service.loadSession(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Session ID is required");
    }

    @Test
    void testUpdateSession() {
        DevelopmentSession created = service.createSession(
                "test-repo", "Test request", WorkflowType.BUG_FIX,
                List.of("ANALYSIS", "FIX", "TEST"));

        DevelopmentSession updated = service.updateSession(
                created.getSessionId(),
                "ANALYSIS",
                0.3,
                "ANALYSIS",
                null,
                null,
                null);

        assertThat(updated.getStatus()).isEqualTo(SessionStatus.IN_PROGRESS);
        assertThat(updated.getCurrentStage()).isEqualTo("ANALYSIS");
        assertThat(updated.getWorkflowProgress()).isEqualTo(0.3);
        assertThat(updated.getCompletedStages()).containsExactly("ANALYSIS");
        assertThat(updated.getPendingStages()).containsExactly("FIX", "TEST");
        assertThat(updated.getExecutionHistory()).hasSize(2);
    }

    @Test
    void testUpdateCompletedSessionThrows() {
        DevelopmentSession created = service.createSession(
                "test-repo", "Test", null, null);
        service.completeSession(created.getSessionId());

        assertThatThrownBy(() -> service.updateSession(
                created.getSessionId(), "STAGE", null, null, null, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot modify completed session");
    }

    @Test
    void testUpdateArchivedSessionThrows() {
        DevelopmentSession created = service.createSession(
                "test-repo", "Test", null, null);
        service.archiveSession(created.getSessionId());

        assertThatThrownBy(() -> service.updateSession(
                created.getSessionId(), "STAGE", null, null, null, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot modify archived session");
    }

    @Test
    void testResumeSession() {
        DevelopmentSession created = service.createSession(
                "test-repo", "Test", null, List.of("STAGE1", "STAGE2"));

        DevelopmentSession resumed = service.resumeSession(created.getSessionId());
        assertThat(resumed.getStatus()).isEqualTo(SessionStatus.IN_PROGRESS);
        assertThat(resumed.getExecutionHistory()).hasSize(2);
    }

    @Test
    void testResumeCompletedSessionThrows() {
        DevelopmentSession created = service.createSession(
                "test-repo", "Test", null, null);
        service.completeSession(created.getSessionId());

        assertThatThrownBy(() -> service.resumeSession(created.getSessionId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot resume completed session");
    }

    @Test
    void testResumeArchivedSessionThrows() {
        DevelopmentSession created = service.createSession(
                "test-repo", "Test", null, null);
        service.archiveSession(created.getSessionId());

        assertThatThrownBy(() -> service.resumeSession(created.getSessionId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot resume archived session");
    }

    @Test
    void testCompleteSession() {
        DevelopmentSession created = service.createSession(
                "test-repo", "Test", null, null);

        DevelopmentSession completed = service.completeSession(created.getSessionId());
        assertThat(completed.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(completed.getWorkflowProgress()).isEqualTo(1.0);
        assertThat(completed.getCompletedAt()).isNotNull();
    }

    @Test
    void testCompleteArchivedSessionThrows() {
        DevelopmentSession created = service.createSession(
                "test-repo", "Test", null, null);
        service.archiveSession(created.getSessionId());

        assertThatThrownBy(() -> service.completeSession(created.getSessionId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot complete archived session");
    }

    @Test
    void testArchiveSession() {
        DevelopmentSession created = service.createSession(
                "test-repo", "Test", null, null);

        DevelopmentSession archived = service.archiveSession(created.getSessionId());
        assertThat(archived.getStatus()).isEqualTo(SessionStatus.ARCHIVED);
        assertThat(archived.getArchivedAt()).isNotNull();
    }

    @Test
    void testVerifyRepositoryMatch() {
        DevelopmentSession session = service.createSession(
                "test-repo", "Test", null, null);

        assertThat(service.verifyRepositoryMatch(session.getSessionId(), "test-repo")).isTrue();
        assertThat(service.verifyRepositoryMatch(session.getSessionId(), "other-repo")).isFalse();
    }

    @Test
    void testSessionExists() {
        DevelopmentSession session = service.createSession(
                "test-repo", "Test", null, null);

        assertThat(service.sessionExists(session.getSessionId())).isTrue();
        assertThat(service.sessionExists("nonexistent")).isFalse();
    }

    @Test
    void testSessionCounts() {
        assertThat(service.getTotalSessionCount()).isEqualTo(0);
        assertThat(service.getActiveSessionCount()).isEqualTo(0);

        service.createSession("repo1", "Request 1", null, null);
        service.createSession("repo2", "Request 2", null, null);

        assertThat(service.getTotalSessionCount()).isEqualTo(2);
        assertThat(service.getActiveSessionCount()).isEqualTo(2);
    }

    @Test
    void testGenerateSessionSummary() {
        DevelopmentSession session = service.createSession(
                "test-repo", "Test request",
                WorkflowType.FEATURE_IMPLEMENTATION,
                List.of("STAGE1", "STAGE2"));

        var summary = service.generateSessionSummary(session.getSessionId());
        assertThat(summary).isNotNull();
        assertThat(summary.getCurrentWorkflow()).isEqualTo("Feature Implementation");
        assertThat(summary.getCompletedStages()).isEmpty();
        assertThat(summary.getPendingStages()).containsExactly("STAGE1", "STAGE2");
        assertThat(summary.getReadinessStatus()).isEqualTo("CREATED");
        assertThat(summary.getSuggestedNextStep()).isEqualTo("Continue with next pending stage: STAGE1");
    }

    @Test
    void testEmptyWorkflow() {
        DevelopmentSession session = service.createSession(
                "test-repo", "Test request", null, null);

        assertThat(session.getPendingStages()).isEmpty();
        assertThat(session.getCompletedStages()).isEmpty();
        assertThat(session.getWorkflowProgress()).isEqualTo(0.0);
    }

    @Test
    void testResumeAfterInterruption() {
        DevelopmentSession session = service.createSession(
                "test-repo", "Test request",
                WorkflowType.REFACTORING,
                List.of("ANALYSIS", "PLANNING", "EXECUTION", "VERIFICATION"));

        // Simulate progress: complete ANALYSIS and PLANNING
        service.updateSession(session.getSessionId(), "PLANNING", 0.5, "ANALYSIS", null, null, null);
        service.updateSession(session.getSessionId(), "EXECUTION", 0.5, "PLANNING", null, null, null);

        // Simulate interruption and resume
        DevelopmentSession resumed = service.resumeSession(session.getSessionId());
        assertThat(resumed.getStatus()).isEqualTo(SessionStatus.IN_PROGRESS);
        assertThat(resumed.getCompletedStages()).containsExactly("ANALYSIS", "PLANNING");
        assertThat(resumed.getPendingStages()).containsExactly("EXECUTION", "VERIFICATION");
        assertThat(resumed.getCurrentStage()).isEqualTo("EXECUTION");
        assertThat(resumed.getWorkflowProgress()).isEqualTo(0.5);
        assertThat(resumed.getSessionSummary().getSuggestedNextStep())
                .isEqualTo("Continue with next pending stage: EXECUTION");
    }

    @Test
    void testRepositoryMismatch() {
        DevelopmentSession session = service.createSession(
                "repo-A", "Test", null, null);

        assertThat(service.verifyRepositoryMatch(session.getSessionId(), "repo-B")).isFalse();
    }
}