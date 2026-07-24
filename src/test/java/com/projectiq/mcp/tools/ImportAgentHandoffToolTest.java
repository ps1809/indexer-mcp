package com.projectiq.mcp.tools;

import static org.assertj.core.api.Assertions.assertThat;

import com.projectiq.mcp.handoff.service.AgentHandoffService;
import com.projectiq.mcp.session.service.DevelopmentSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ImportAgentHandoffToolTest {

    private DevelopmentSessionService sessionService;
    private AgentHandoffService handoffService;
    private ImportAgentHandoffTool importTool;
    private ExportAgentHandoffTool exportTool;

    @BeforeEach
    void setUp() {
        sessionService = new DevelopmentSessionService();
        handoffService = new AgentHandoffService(sessionService);
        importTool = new ImportAgentHandoffTool(handoffService);
        exportTool = new ExportAgentHandoffTool(handoffService);
    }

    @Test
    void testSuccessfulImport() {
        var session = sessionService.createSession("repo", "request", null, null);
        String exported = exportTool.exportAgentHandoff(session.getSessionId());

        String result = importTool.importAgentHandoff(exported);
        assertThat(result).isNotNull();
        assertThat(result).contains(session.getSessionId());
        assertThat(result).contains("repo");
    }

    @Test
    void testImportMissingPackage() {
        String result = importTool.importAgentHandoff(null);
        assertThat(result).contains("INVALID_ARGUMENT");
        assertThat(result).contains("Handoff package is required");
    }

    @Test
    void testImportInvalidPackage() {
        String result = importTool.importAgentHandoff("not-valid-json");
        assertThat(result).contains("INVALID_PACKAGE");
    }

    @Test
    void testImportCorruptedPackage() {
        var session = sessionService.createSession("repo", "request", null, null);
        String exported = exportTool.exportAgentHandoff(session.getSessionId());

        // Corrupt by removing the integrity hash value
        String corrupted = exported.replaceFirst(
                "\"integrityHash\" : \"[a-f0-9]+\"",
                "\"integrityHash\" : \"corrupted\"");

        String result = importTool.importAgentHandoff(corrupted);
        assertThat(result).containsAnyOf("CORRUPTED_PACKAGE", "INVALID_PACKAGE");
    }
}
