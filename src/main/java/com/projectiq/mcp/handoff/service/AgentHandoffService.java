package com.projectiq.mcp.handoff.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.projectiq.mcp.handoff.dto.AgentHandoffPackage;
import com.projectiq.mcp.session.dto.DevelopmentSession;
import com.projectiq.mcp.session.dto.DevelopmentSession.ExecutionHistoryEntry;
import com.projectiq.mcp.session.dto.SessionStatus;
import com.projectiq.mcp.session.service.DevelopmentSessionService;
import com.projectiq.mcp.orchestration.dto.WorkflowType;
import com.projectiq.mcp.validation.dto.ValidationReport;
import com.projectiq.mcp.recommendation.dto.RecommendationReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service that generates self-contained AI Agent Handoff Packages.
 * These packages capture the complete development session state, workflow
 * progress, repository intelligence, execution history, validation results,
 * recommendations, and readiness assessment into a deterministic,
 * serializable artifact that any compatible AI coding agent can use to
 * immediately continue work.
 *
 * <p>All operations are deterministic. Package ordering is stable.
 * No duplicate information is included. Execution history is immutable once
 * recorded.</p>
 */
@Service
public class AgentHandoffService {

    private static final Logger logger = LoggerFactory.getLogger(AgentHandoffService.class);

    private final DevelopmentSessionService sessionService;
    private final ObjectMapper objectMapper;

    private static final String PACKAGE_VERSION = "1.0";

    public AgentHandoffService(DevelopmentSessionService sessionService) {
        this.sessionService = sessionService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Exports a complete handoff package for the given development session.
     *
     * @param sessionId the session identifier (required)
     * @return the serialized AgentHandoffPackage as a JSON string
     * @throws IllegalArgumentException if session is not found
     */
    public String exportHandoffPackage(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID is required");
        }

        logger.info("Exporting agent handoff package for session: {}", sessionId);

        DevelopmentSession session = sessionService.loadSession(sessionId.trim());

        AgentHandoffPackage handoff = buildHandoffPackage(session);

        // Compute integrity hash
        handoff.setIntegrityHash(computeIntegrityHash(handoff));

        return serializePackage(handoff);
    }

    /**
     * Imports a handoff package and restores the development session.
     *
     * @param serializedPackage the serialized AgentHandoffPackage JSON string (required)
     * @return the restored DevelopmentSession as a JSON string
     * @throws IllegalArgumentException if package is invalid, corrupted,
     *                                  repository mismatch, or version incompatibility
     */
    public String importHandoffPackage(String serializedPackage) {
        if (serializedPackage == null || serializedPackage.trim().isEmpty()) {
            throw new IllegalArgumentException("Handoff package is required");
        }

        logger.info("Importing agent handoff package");

        // Deserialize the package
        AgentHandoffPackage handoff = deserializePackage(serializedPackage.trim());

        // Validate package version
        if (handoff.getPackageVersion() == null || !handoff.getPackageVersion().equals(PACKAGE_VERSION)) {
            throw new IllegalArgumentException(
                    "Version incompatibility: expected " + PACKAGE_VERSION
                            + " but got " + handoff.getPackageVersion());
        }

        // Validate session ID
        if (handoff.getSessionId() == null || handoff.getSessionId().trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid handoff package: missing session ID");
        }

        // Validate repository ID
        if (handoff.getRepositoryId() == null || handoff.getRepositoryId().trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid handoff package: missing repository ID");
        }

        // Verify integrity hash
        String storedHash = handoff.getIntegrityHash();
        handoff.setIntegrityHash(null); // Remove hash before computing
        String computedHash = computeIntegrityHash(handoff);
        handoff.setIntegrityHash(storedHash); // Restore hash

        if (storedHash == null || !storedHash.equals(computedHash)) {
            throw new IllegalArgumentException("Corrupted handoff package: integrity hash mismatch");
        }

        // Check if session already exists and verify repository match
        if (sessionService.sessionExists(handoff.getSessionId())) {
            DevelopmentSession existing = sessionService.loadSession(handoff.getSessionId());
            if (!existing.getRepositoryId().equals(handoff.getRepositoryId())) {
                throw new IllegalArgumentException(
                        "Repository mismatch: session belongs to repository '"
                                + existing.getRepositoryId()
                                + "' but package is for repository '"
                                + handoff.getRepositoryId() + "'");
            }
            logger.info("Session {} already exists, will restore state on top of existing session",
                    handoff.getSessionId());
        }

        // Restore the development session
        DevelopmentSession restoredSession = restoreSession(handoff);

        return serializeSession(restoredSession);
    }

    /**
     * Validates the integrity of a serialized handoff package.
     *
     * @param serializedPackage the serialized AgentHandoffPackage JSON string (required)
     * @return a JSON response indicating whether integrity is valid
     */
    public String validatePackageIntegrity(String serializedPackage) {
        try {
            AgentHandoffPackage handoff = deserializePackage(serializedPackage);

            String storedHash = handoff.getIntegrityHash();
            if (storedHash == null || storedHash.trim().isEmpty()) {
                return "{\"valid\":false,\"reason\":\"Missing integrity hash\"}";
            }

            handoff.setIntegrityHash(null);
            String computedHash = computeIntegrityHash(handoff);

            boolean valid = storedHash.equals(computedHash);
            if (valid) {
                return "{\"valid\":true,\"reason\":\"Integrity check passed\"}";
            } else {
                return "{\"valid\":false,\"reason\":\"Integrity hash mismatch - package may be corrupted\"}";
            }
        } catch (IllegalArgumentException e) {
            return "{\"valid\":false,\"reason\":\"" + escapeJson(e.getMessage()) + "\"}";
        } catch (Exception e) {
            return "{\"valid\":false,\"reason\":\"Invalid package format\"}";
        }
    }

    // ========== Package Builder ==========

    private AgentHandoffPackage buildHandoffPackage(DevelopmentSession session) {
        AgentHandoffPackage handoff = new AgentHandoffPackage();

        handoff.setPackageVersion(PACKAGE_VERSION);
        handoff.setExportedAt(LocalDateTime.now());
        handoff.setSessionId(session.getSessionId());
        handoff.setRepositoryId(session.getRepositoryId());
        handoff.setRepositoryName(session.getRepositoryId()); // Use repositoryId as name
        handoff.setDeveloperRequest(session.getDeveloperRequest());
        handoff.setWorkflowType(session.getWorkflowType());
        handoff.setCurrentStage(session.getCurrentStage());
        handoff.setCompletedStages(new ArrayList<>(session.getCompletedStages()));
        handoff.setPendingStages(new ArrayList<>(session.getPendingStages()));
        handoff.setWorkflowProgress(session.getWorkflowProgress());
        handoff.setSessionStatus(session.getStatus() != null ? session.getStatus().name() : "UNKNOWN");
        handoff.setExecutionHistory(new ArrayList<>(session.getExecutionHistory()));
        handoff.setContextPackage(session.getContextPackage());
        handoff.setValidationReport(session.getValidationReport());
        handoff.setRecommendationReport(session.getRecommendationReport());
        handoff.setSessionSummary(session.getSessionSummary());

        // Build derived summaries
        handoff.setValidationSummary(buildValidationSummary(session));
        handoff.setRecommendationSummary(buildRecommendationSummary(session));
        handoff.setReadinessAssessment(buildReadinessAssessment(session));
        handoff.setOutstandingRisks(buildOutstandingRisks(session));
        handoff.setSuggestedNextActions(buildSuggestedNextActions(session));

        return handoff;
    }

    private DevelopmentSession restoreSession(AgentHandoffPackage handoff) {
        DevelopmentSession session;

        if (sessionService.sessionExists(handoff.getSessionId())) {
            session = sessionService.loadSession(handoff.getSessionId());
        } else {
            // Create a new session through the service to ensure it's tracked
            session = sessionService.createSession(
                    handoff.getRepositoryId(),
                    handoff.getDeveloperRequest(),
                    handoff.getWorkflowType() != null ? handoff.getWorkflowType() : WorkflowType.UNKNOWN,
                    handoff.getPendingStages());
        }

        // Restore workflow state
        session.setCurrentStage(handoff.getCurrentStage());
        session.setWorkflowProgress(handoff.getWorkflowProgress());
        session.setCompletedStages(new ArrayList<>(handoff.getCompletedStages()));
        session.setPendingStages(new ArrayList<>(handoff.getPendingStages()));

        // Restore status
        if (handoff.getSessionStatus() != null) {
            try {
                session.setStatus(SessionStatus.valueOf(handoff.getSessionStatus()));
            } catch (IllegalArgumentException e) {
                session.setStatus(SessionStatus.IN_PROGRESS);
            }
        }

        // Restore execution history (immutable, append only)
        for (ExecutionHistoryEntry entry : handoff.getExecutionHistory()) {
            session.addExecutionHistoryEntry(entry);
        }

        // Add restoration history entry
        session.addExecutionHistoryEntry(new ExecutionHistoryEntry(
                handoff.getCurrentStage(), "IMPORT_HANDOFF", "COMPLETED",
                "Session restored from handoff package"));

        // Restore reports
        session.setContextPackage(handoff.getContextPackage());
        session.setValidationReport(handoff.getValidationReport());
        session.setRecommendationReport(handoff.getRecommendationReport());

        // Update timestamps
        session.setUpdatedAt(LocalDateTime.now());

        // Generate updated session summary
        try {
            session.setSessionSummary(sessionService.generateSessionSummary(handoff.getSessionId()));
        } catch (IllegalArgumentException e) {
            // Session summary generation may fail if session not fully tracked yet
            logger.warn("Could not generate session summary for restored session: {}", e.getMessage());
        }

        return session;
    }

    // ========== Summary Builders ==========

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

    private String buildReadinessAssessment(DevelopmentSession session) {
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

    private List<String> buildOutstandingRisks(DevelopmentSession session) {
        List<String> risks = new ArrayList<>();

        // Collect risks from execution history
        for (ExecutionHistoryEntry entry : session.getExecutionHistory()) {
            if ("FAILED".equals(entry.getStatus()) || "ERROR".equals(entry.getStatus())) {
                risks.add("Stage '" + entry.getStageName() + "' " + entry.getAction()
                        + " failed: " + entry.getDetails());
            }
        }

        // Collect risks from validation report
        if (session.getValidationReport() != null && session.getValidationReport().getBlockingIssues() > 0) {
            risks.add("Validation report contains " + session.getValidationReport().getBlockingIssues()
                    + " blocking issues");
        }

        // Risks from readiness data if available via session summary
        if (session.getSessionSummary() != null && session.getSessionSummary().getReadinessStatus() != null) {
            String readiness = session.getSessionSummary().getReadinessStatus();
            if (readiness.contains("BLOCKED") || readiness.contains("NOT_READY")) {
                risks.add("Readiness assessment indicates: " + readiness);
            }
        }

        return risks;
    }

    private List<String> buildSuggestedNextActions(DevelopmentSession session) {
        List<String> actions = new ArrayList<>();

        if (session.getStatus() == SessionStatus.COMPLETED) {
            actions.add("Session is complete. Review final results.");
            return actions;
        }
        if (session.getStatus() == SessionStatus.ARCHIVED) {
            actions.add("Session is archived. Create a new session to continue work.");
            return actions;
        }

        // Suggest resuming pending stages
        List<String> pending = session.getPendingStages();
        if (!pending.isEmpty()) {
            actions.add("Continue with next pending stage: " + pending.get(0));
            for (int i = 1; i < pending.size(); i++) {
                actions.add("Complete remaining stage: " + pending.get(i));
            }
        }

        // Suggest completing missing reports
        if (session.getContextPackage() == null) {
            actions.add("Assemble repository context");
        }
        if (session.getValidationReport() == null) {
            actions.add("Validate workflow");
        }
        if (session.getRecommendationReport() == null) {
            actions.add("Generate recommendations");
        }

        if (actions.isEmpty()) {
            actions.add("Review session summary and continue development");
        }

        return actions;
    }

    // ========== Integrity Hash ==========

    private String computeIntegrityHash(AgentHandoffPackage handoff) {
        try {
            // Serialize package deterministically without the hash field
            String serialized = objectMapper.writeValueAsString(handoff);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(serialized.getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            logger.error("Failed to compute integrity hash: {}", e.getMessage());
            return "hash-error";
        }
    }

    // ========== Serialization ==========

    private String serializePackage(AgentHandoffPackage handoff) {
        try {
            return objectMapper.writeValueAsString(handoff);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize handoff package: " + e.getMessage());
        }
    }

    private AgentHandoffPackage deserializePackage(String serialized) {
        try {
            return objectMapper.readValue(serialized, AgentHandoffPackage.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid handoff package: " + e.getMessage());
        }
    }

    private String serializeSession(DevelopmentSession session) {
        try {
            return objectMapper.writeValueAsString(session);
        } catch (JsonProcessingException e) {
            return "{\"errorType\":\"SERIALIZATION_ERROR\",\"message\":\""
                    + escapeJson(e.getMessage()) + "\"}";
        }
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}