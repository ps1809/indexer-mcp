package com.projectiq.mcp.tools;

import static org.assertj.core.api.Assertions.assertThat;

import com.projectiq.mcp.orchestration.dto.WorkflowType;
import com.projectiq.mcp.session.service.DevelopmentSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GetDevelopmentSessionToolTest {

    private DevelopmentSessionService service;
    private GetDevelopmentSessionTool tool;

    @BeforeEach
    void setUp() {
        service = new DevelopmentSessionService();
        tool = new GetDevelopmentSessionTool(service);
    }

    @Test
    void testGetExistingSession() {
        var session = service.createSession("repo", "request", WorkflowType.BUG_FIX, null);

        String result = tool.getDevelopmentSession(session.getSessionId());
        assertThat(result).isNotNull();
        assertThat(result).contains(session.getSessionId());
        assertThat(result).contains("repo");
        assertThat(result).contains("request");
        assertThat(result).contains("Bug Fix");
    }

    @Test
    void testGetSessionMissingId() {
        String result = tool.getDevelopmentSession(null);
        assertThat(result).contains("INVALID_ARGUMENT");
        assertThat(result).contains("Session ID is required");
    }

    @Test
    void testGetSessionNotFound() {
        String result = tool.getDevelopmentSession("nonexistent");
        assertThat(result).contains("INVALID_ARGUMENT");
        assertThat(result).contains("Session not found");
    }
}