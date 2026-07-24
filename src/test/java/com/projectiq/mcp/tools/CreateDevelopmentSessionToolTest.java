package com.projectiq.mcp.tools;

import static org.assertj.core.api.Assertions.assertThat;

import com.projectiq.mcp.session.service.DevelopmentSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateDevelopmentSessionToolTest {

    private CreateDevelopmentSessionTool tool;

    @BeforeEach
    void setUp() {
        tool = new CreateDevelopmentSessionTool(new DevelopmentSessionService());
    }

    @Test
    void testCreateSession() {
        String result = tool.createDevelopmentSession(
                "test-repo", "Implement feature X", "FEATURE_IMPLEMENTATION", "ANALYSIS,PLANNING,EXECUTION");

        assertThat(result).isNotNull();
        assertThat(result).contains("sessionId");
        assertThat(result).contains("test-repo");
        assertThat(result).contains("Implement feature X");
        assertThat(result).contains("Feature Implementation");
        assertThat(result).contains("CREATED");
        assertThat(result).contains("ANALYSIS");
        assertThat(result).contains("PLANNING");
        assertThat(result).contains("EXECUTION");
    }

    @Test
    void testCreateSessionMissingRepositoryId() {
        String result = tool.createDevelopmentSession(null, "request", null, null);
        assertThat(result).contains("INVALID_ARGUMENT");
        assertThat(result).contains("Repository ID is required");
    }

    @Test
    void testCreateSessionMissingDeveloperRequest() {
        String result = tool.createDevelopmentSession("repo", null, null, null);
        assertThat(result).contains("INVALID_ARGUMENT");
        assertThat(result).contains("Developer request is required");
    }

    @Test
    void testCreateSessionInvalidWorkflowType() {
        String result = tool.createDevelopmentSession("repo", "request", "INVALID_TYPE", null);
        assertThat(result).contains("INVALID_ARGUMENT");
        assertThat(result).contains("Invalid workflow type");
    }

    @Test
    void testCreateSessionWithNoStages() {
        String result = tool.createDevelopmentSession("repo", "request", null, null);
        assertThat(result).contains("sessionId");
        assertThat(result).contains("UNKNOWN");
    }
}