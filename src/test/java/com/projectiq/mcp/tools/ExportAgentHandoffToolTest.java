package com.projectiq.mcp.tools;

import static org.assertj.core.api.Assertions.assertThat;

import com.projectiq.mcp.handoff.service.AgentHandoffService;
import com.projectiq.mcp.session.service.DevelopmentSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExportAgentHandoffToolTest {

    private DevelopmentSessionService sessionService;
    private AgentHandoffService handoffService;
    private ExportAgentHandoffTool tool;

    @BeforeEach
    void setUp() {
        sessionService = new DevelopmentSessionService();
        handoffService = new AgentHandoffService(sessionService);
        tool = new ExportAgentHandoffTool(handoffService);
    }

    @Test
    void testExportExistingSession() {
        var session = sessionService.createSession("repo", "request", null, null);

        String result = tool.exportAgentHandoff(session.getSessionId());
        assertThat(result).isNotNull();
        assertThat(result).contains(session.getSessionId());
        assertThat(result).contains("integrityHash");
        assertThat(result).contains("packageVersion");
    }

    @Test
    void testExportMissingId() {
        String result = tool.exportAgentHandoff(null);
        assertThat(result).contains("INVALID_ARGUMENT");
        assertThat(result).contains("Session ID is required");
    }

    @Test
    void testExportNotFound() {
        String result = tool.exportAgentHandoff("nonexistent");
        assertThat(result).contains("INVALID_ARGUMENT");
        assertThat(result).contains("Session not found");
    }
}