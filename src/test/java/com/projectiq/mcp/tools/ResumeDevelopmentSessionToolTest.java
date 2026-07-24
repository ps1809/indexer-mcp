package com.projectiq.mcp.tools;

import static org.assertj.core.api.Assertions.assertThat;

import com.projectiq.mcp.session.service.DevelopmentSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResumeDevelopmentSessionToolTest {

    private DevelopmentSessionService service;
    private ResumeDevelopmentSessionTool tool;

    @BeforeEach
    void setUp() {
        service = new DevelopmentSessionService();
        tool = new ResumeDevelopmentSessionTool(service);
    }

    @Test
    void testResumeExistingSession() {
        var session = service.createSession("repo", "request", null, null);

        String result = tool.resumeDevelopmentSession(session.getSessionId());
        assertThat(result).isNotNull();
        assertThat(result).contains(session.getSessionId());
        assertThat(result).contains("IN_PROGRESS");
    }

    @Test
    void testResumeMissingId() {
        String result = tool.resumeDevelopmentSession(null);
        assertThat(result).contains("INVALID_ARGUMENT");
        assertThat(result).contains("Session ID is required");
    }

    @Test
    void testResumeNotFound() {
        String result = tool.resumeDevelopmentSession("nonexistent");
        assertThat(result).contains("INVALID_ARGUMENT");
        assertThat(result).contains("Session not found");
    }

    @Test
    void testResumeCompletedSession() {
        var session = service.createSession("repo", "request", null, null);
        service.completeSession(session.getSessionId());

        String result = tool.resumeDevelopmentSession(session.getSessionId());
        assertThat(result).contains("INVALID_STATE");
        assertThat(result).contains("Cannot resume completed session");
    }
}