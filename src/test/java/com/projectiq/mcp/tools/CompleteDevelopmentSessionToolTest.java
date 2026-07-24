package com.projectiq.mcp.tools;

import static org.assertj.core.api.Assertions.assertThat;

import com.projectiq.mcp.session.service.DevelopmentSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CompleteDevelopmentSessionToolTest {

    private DevelopmentSessionService service;
    private CompleteDevelopmentSessionTool tool;

    @BeforeEach
    void setUp() {
        service = new DevelopmentSessionService();
        tool = new CompleteDevelopmentSessionTool(service);
    }

    @Test
    void testCompleteExistingSession() {
        var session = service.createSession("repo", "request", null, null);

        String result = tool.completeDevelopmentSession(session.getSessionId());
        assertThat(result).isNotNull();
        assertThat(result).contains(session.getSessionId());
        assertThat(result).contains("COMPLETED");
    }

    @Test
    void testCompleteMissingId() {
        String result = tool.completeDevelopmentSession(null);
        assertThat(result).contains("INVALID_ARGUMENT");
        assertThat(result).contains("Session ID is required");
    }

    @Test
    void testCompleteNotFound() {
        String result = tool.completeDevelopmentSession("nonexistent");
        assertThat(result).contains("INVALID_ARGUMENT");
        assertThat(result).contains("Session not found");
    }

    @Test
    void testCompleteArchivedSession() {
        var session = service.createSession("repo", "request", null, null);
        service.archiveSession(session.getSessionId());

        String result = tool.completeDevelopmentSession(session.getSessionId());
        assertThat(result).contains("INVALID_STATE");
        assertThat(result).contains("Cannot complete archived session");
    }
}