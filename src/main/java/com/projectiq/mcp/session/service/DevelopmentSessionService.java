package com.projectiq.mcp.session.service;

import com.projectiq.mcp.orchestration.dto.WorkflowType;
import com.projectiq.mcp.pipeline.dto.ContextPackage;
import com.projectiq.mcp.recommendation.dto.RecommendationReport;
import com.projectiq.mcp.session.dto.DevelopmentSession;
import com.projectiq.mcp.session.dto.DevelopmentSession.ExecutionHistoryEntry;
import com.projectiq.mcp.session.dto.DevelopmentSession.SessionSummary;
import com.projectiq.mcp.session.dto.SessionStatus;
import com.projectiq.mcp.validation.dto.ValidationReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service that manages the complete lifecycle of AI-assisted development sessions.
 * Sessions preserve workflow progress, execution context, collected repository
 * intelligence, validation results, recommendations, and implementation history.
 *
 * <p>All operations are deterministic. Sessions are stored in-memory.
 * No distributed sessions, multi-user collaboration, or cloud synchronization
 * is implemented.</p>
 */
@Service
public class DevelopmentSessionService {

    private static final Logger logger = LoggerFactory.getLogger(DevelopmentSessionService.class);

    private final Map<String, DevelopmentSession> sessions = new ConcurrentHashMap<>();

    /**
     * Creates a new development session with the specified parameters.
     *
     * @param repositoryId     the repository identifier (required)
     * @param developerRequest the developer request (required)
     * @param workflowType     the workflow type (optional, defaults to UNKNOWN)
     * @param pendingStages    list of pending workflow stages (optional)
     * @return the created DevelopmentSession
     * @throws IllegalArgumentException if repositoryId or developerRequest is null/empty
     */
    public DevelopmentSession createSession(
            String repositoryId,
            String developerRequest,
            WorkflowType workflowType,
            List<String> pendingStages) {
        if (repositoryId == null || repositoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Repository ID is required");
        }
        if (developerRequest == null || developerRequest.trim().isEmpty()) {
            throw new IllegalArgumentException("Developer request is required");
        }

        DevelopmentSession session = new DevelopmentSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setRepositoryId(repositoryId.trim());
        session.setDeveloperRequest(developerRequest.trim());
        session.setWorkflowType(workflowType != null ? workflowType : WorkflowType.UNKNOWN);
        session.setStatus(SessionStatus.CREATED);
        session.setCurrentStage("INITIALIZED");
        session.setWorkflowProgress(0.0);

        if (pendingStages != null && !pendingStages.isEmpty()) {
            for (String stage : pendingStages) {
                session.addPendingStage(stage.trim());
            }
        }

        // Add creation history entry
        session.addExecutionHistoryEntry(new ExecutionHistoryEntry(
                "INITIALIZED", "CREATE_SESSION", "COMPLETED",
                "Session created for repository: " + repositoryId));

        // Build initial session summary
        session.setSessionSummary(buildSessionSummary(session));

        sessions.put(session.getSessionId(), session);

        logger.info("Created development session: {} for repository: {}",
                session.getSessionId(), repositoryId);

        return session;
    }

    /**
     * Loads an existing development session by its identifier.
     *
     * @param sessionId the session identifier (required)
     * @return the DevelopmentSession
     * @throws IllegalArgumentException if sessionId is null/empty or session not found
     */
    public DevelopmentSession loadSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID is required");
        }

        DevelopmentSession session = sessions.get(sessionId.trim());
        if (session == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }

        return session;
    }

    /**
     * Updates an existing development session with new information.
     *
     * @param sessionId          the session identifier (required)
     * @param currentStage       the current execution stage (optional)
     * @param workflowProgress   the workflow progress (0.0 to 1.0)
     * @param completedStage     a completed stage to add (optional)
     * @param contextPackage     the context package (optional)
     * @param validationReport   the validation report (optional)
     * @param recommendationReport the recommendation report (optional)
     * @return the updated DevelopmentSession
     * @throws IllegalArgumentException if session is invalid or completed/archived
     */
    public DevelopmentSession updateSession(
            String sessionId,
            String currentStage,
            Double workflowProgress,
            String completedStage,
            ContextPackage contextPackage,
            ValidationReport validationReport,
            RecommendationReport recommendationReport) {
        DevelopmentSession session = loadSession(sessionId);

        validateSessionModifiable(session);

        // Update status to IN_PROGRESS if currently CREATED
        if (session.getStatus() == SessionStatus.CREATED) {
            session.setStatus(SessionStatus.IN_PROGRESS);
        }

        if (currentStage != null && !currentStage.trim().isEmpty()) {
            session.setCurrentStage(currentStage.trim());
        }

        if (workflowProgress != null) {
            session.setWorkflowProgress(Math.max(0.0, Math.min(1.0, workflowProgress)));
        }

        if (completedStage != null && !completedStage.trim().isEmpty()) {
            String stage = completedStage.trim();
            session.addCompletedStage(stage);
            session.getPendingStages().remove(stage);

            // Add execution history entry for completed stage
            session.addExecutionHistoryEntry(new ExecutionHistoryEntry(
                    stage, "COMPLETE_STAGE", "COMPLETED",
                    "Stage completed at progress: " + session.getWorkflowProgress()));
        }

        if (contextPackage != null) {
            session.setContextPackage(contextPackage);
            session.addExecutionHistoryEntry(new ExecutionHistoryEntry(
                    session.getCurrentStage(), "UPDATE_CONTEXT", "COMPLETED",
                    "Context package updated"));
        }

        if (validationReport != null) {
            session.setValidationReport(validationReport);
            session.addExecutionHistoryEntry(new ExecutionHistoryEntry(
                    session.getCurrentStage(), "UPDATE_VALIDATION", "COMPLETED",
                    "Validation report updated"));
        }

        if (recommendationReport != null) {
            session.setRecommendationReport(recommendationReport);
            session.addExecutionHistoryEntry(new ExecutionHistoryEntry(
                    session.getCurrentStage(), "UPDATE_RECOMMENDATIONS", "COMPLETED",
                    "Recommendation report updated"));
        }

        // Update session summary
        session.setSessionSummary(buildSessionSummary(session));
        session.setUpdatedAt(LocalDateTime.now());

        logger.debug("Updated session: {} - stage: {}, progress: {}",
                sessionId, session.getCurrentStage(), session.getWorkflowProgress());

        return session;
    }

    /**
     * Resumes an interrupted workflow by loading the session and updating its status.
     *
     * @param sessionId the session identifier (required)
     * @return the DevelopmentSession with status set to IN_PROGRESS
     * @throws IllegalArgumentException if session is invalid or completed/archived
     */
    public DevelopmentSession resumeSession(String sessionId) {
        DevelopmentSession session = loadSession(sessionId);

        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Cannot resume completed session: " + sessionId);
        }
        if (session.getStatus() == SessionStatus.ARCHIVED) {
            throw new IllegalStateException(
                    "Cannot resume archived session: " + sessionId);
        }

        session.setStatus(SessionStatus.IN_PROGRESS);
        session.setUpdatedAt(LocalDateTime.now());

        session.addExecutionHistoryEntry(new ExecutionHistoryEntry(
                session.getCurrentStage(), "RESUME_SESSION", "COMPLETED",
                "Session resumed at progress: " + session.getWorkflowProgress()));

        // Rebuild session summary on resume
        session.setSessionSummary(buildSessionSummary(session));

        logger.info("Resumed session: {} at stage: {}, progress: {}",
                sessionId, session.getCurrentStage(), session.getWorkflowProgress());

        return session;
    }

    /**
     * Completes a development session.
     *
     * @param sessionId the session identifier (required)
     * @return the DevelopmentSession with status set to COMPLETED
     * @throws IllegalArgumentException if session is invalid or archived
     */
    public DevelopmentSession completeSession(String sessionId) {
        DevelopmentSession session = loadSession(sessionId);

        if (session.getStatus() == SessionStatus.ARCHIVED) {
            throw new IllegalStateException(
                    "Cannot complete archived session: " + sessionId);
        }

        session.setStatus(SessionStatus.COMPLETED);
        session.setWorkflowProgress(1.0);
        session.setCompletedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());

        session.addExecutionHistoryEntry(new ExecutionHistoryEntry(
                session.getCurrentStage(), "COMPLETE_SESSION", "COMPLETED",
                "Session completed successfully"));

        // Rebuild final session summary
        session.setSessionSummary(buildSessionSummary(session));

        logger.info("Completed session: {}", sessionId);

        return session;
    }

    /**
     * Archives a development session.
     *
     * @param sessionId the session identifier (required)
     * @return the DevelopmentSession with status set to ARCHIVED
     * @throws IllegalArgumentException if session is invalid
     */
    public DevelopmentSession archiveSession(String sessionId) {
        DevelopmentSession session = loadSession(sessionId);

        session.setStatus(SessionStatus.ARCHIVED);
        session.setArchivedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());

        session.addExecutionHistoryEntry(new ExecutionHistoryEntry(
                session.getCurrentStage(), "ARCHIVE_SESSION", "COMPLETED",
                "Session archived"));

        logger.info("Archived session: {}", sessionId);

        return session;
    }

    /**
     * Generates a summary for the given session.
     *
     * @param sessionId the session identifier
     * @return the SessionSummary
     * @throws IllegalArgumentException if session is not found
     */
    public SessionSummary generateSessionSummary(String sessionId) {
        DevelopmentSession session = loadSession(sessionId);
        return buildSessionSummary(session);
    }

    // ========== Validation Helpers ==========

    private void validateSessionModifiable(DevelopmentSession session) {
        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Cannot modify completed session: " + session.getSessionId());
        }
        if (session.getStatus() == SessionStatus.ARCHIVED) {
            throw new IllegalStateException(
                    "Cannot modify archived session: " + session.getSessionId());
        }
    }

    // ========== Summary Builder ==========

    private SessionSummary buildSessionSummary(DevelopmentSession session) {
        SessionSummary summary = new SessionSummary();
        summary.setCurrentWorkflow(
                session.getWorkflowType() != null
                        ? session.getWorkflowType().getDisplayName()
                        : "Unknown");
        summary.setCompletedStages(new ArrayList<>(session.getCompletedStages()));
        summary.setPendingStages(new ArrayList<>(session.getPendingStages()));
        summary.setRepositoryIntelligenceSummary(buildRepositoryIntelligenceSummary(session));
        summary.setValidationSummary(buildValidationSummary(session));
        summary.setRecommendationSummary(buildRecommendationSummary(session));
        summary.setReadinessStatus(buildReadinessStatus(session));
        summary.setSuggestedNextStep(buildSuggestedNextStep(session));
        return summary;
    }

    private String buildRepositoryIntelligenceSummary(DevelopmentSession session) {
        if (session.getContextPackage() != null) {
            return "Context package available with assembled repository intelligence";
        }
        return "No repository intelligence assembled yet";
    }

    private String buildValidationSummary(DevelopmentSession session) {
        if (session.getValidationReport() != null) {
            ValidationReport report = session.getValidationReport();
            return String.format("Status: %s, Passed: %d, Failed: %d, Warnings: %d, Blocking: %d",
                    report.getOverallStatus() != null ? report.getOverallStatus() : "N/A",
                    report.getPassedValidations(),
                    report.getFailedValidations(),
                    report.getWarnings(),
                    report.getBlockingIssues());
        }
        return "No validation performed yet";
    }

    private String buildRecommendationSummary(DevelopmentSession session) {
        if (session.getRecommendationReport() != null) {
            RecommendationReport report = session.getRecommendationReport();
            int total = report.getPrioritizedRecommendations() != null
                    ? report.getPrioritizedRecommendations().size() : 0;
            int critical = 0;
            int high = 0;
            if (report.getRecommendationSummary() != null) {
                critical = report.getRecommendationSummary().getCriticalCount();
                high = report.getRecommendationSummary().getHighCount();
            }
            return String.format("Total recommendations: %d, Critical: %d, High: %d",
                    total, critical, high);
        }
        return "No recommendations generated yet";
    }

    private String buildReadinessStatus(DevelopmentSession session) {
        if (session.getStatus() == SessionStatus.COMPLETED) {
            return "COMPLETED";
        }
        if (session.getStatus() == SessionStatus.ARCHIVED) {
            return "ARCHIVED";
        }
        if (session.getWorkflowProgress() > 0) {
            return "IN_PROGRESS (" + Math.round(session.getWorkflowProgress() * 100) + "%)";
        }
        return "CREATED";
    }

    private String buildSuggestedNextStep(DevelopmentSession session) {
        if (session.getStatus() == SessionStatus.COMPLETED) {
            return "Session is complete. No further steps required.";
        }
        if (session.getStatus() == SessionStatus.ARCHIVED) {
            return "Session is archived.";
        }

        List<String> pending = session.getPendingStages();
        if (!pending.isEmpty()) {
            return "Continue with next pending stage: " + pending.get(0);
        }

        if (session.getValidationReport() == null) {
            return "Validate workflow before proceeding";
        }
        if (session.getRecommendationReport() == null) {
            return "Generate recommendations before proceeding";
        }
        if (session.getContextPackage() == null) {
            return "Assemble repository context before proceeding";
        }

        return "Session is ready to proceed";
    }

    // ========== Repository Verification ==========

    /**
     * Verifies that the session belongs to the expected repository.
     *
     * @param sessionId    the session identifier
     * @param repositoryId the expected repository identifier
     * @return true if session repository matches
     * @throws IllegalArgumentException if session not found
     */
    public boolean verifyRepositoryMatch(String sessionId, String repositoryId) {
        DevelopmentSession session = loadSession(sessionId);
        return session.getRepositoryId().equals(repositoryId);
    }

    /**
     * Checks if a session with the given ID exists.
     *
     * @param sessionId the session identifier
     * @return true if the session exists
     */
    public boolean sessionExists(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    /**
     * Returns the number of active (non-completed, non-archived) sessions.
     *
     * @return count of active sessions
     */
    public int getActiveSessionCount() {
        return (int) sessions.values().stream()
                .filter(s -> s.getStatus() == SessionStatus.CREATED
                        || s.getStatus() == SessionStatus.IN_PROGRESS)
                .count();
    }

    /**
     * Returns the total number of sessions.
     *
     * @return total session count
     */
    public int getTotalSessionCount() {
        return sessions.size();
    }
}