package com.projectiq.mcp.tools;

import com.projectiq.mcp.analysis.dto.DependencyChangePredictionResponse;
import com.projectiq.mcp.analysis.service.DependencyChangePredictionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PredictDependencyChangeTool}.
 */
@ExtendWith(MockitoExtension.class)
class PredictDependencyChangeToolTest {

    @Mock
    private DependencyChangePredictionService predictionService;

    private PredictDependencyChangeTool tool;

    @BeforeEach
    void setUp() {
        tool = new PredictDependencyChangeTool(predictionService);
    }

    @Test
    void testPredictDependencyChange() {
        DependencyChangePredictionResponse mockResponse = createMockAddResponse();

        when(predictionService.predictDependencyChange(
                eq("com.example:new-lib"), eq("ADD"), isNull(), eq("1.0.0"), eq("test-repo")))
                .thenReturn(mockResponse);

        String result = tool.predictDependencyChange("com.example:new-lib", "ADD", null, "1.0.0", "test-repo");

        assertNotNull(result);
        assertTrue(result.contains("Dependency Change Prediction Report"));
        assertTrue(result.contains("com.example:new-lib"));
        assertTrue(result.contains("ADD"));
        assertTrue(result.contains("Third-Party Frameworks"));
        assertTrue(result.contains("MEDIUM"));
    }

    @Test
    void testPredictDependencyChangeWithVersions() {
        DependencyChangePredictionResponse mockResponse = createMockUpgradeResponse();

        when(predictionService.predictDependencyChange(
                eq("org.springframework.boot:spring-boot-starter-web"), eq("UPGRADE"),
                eq("3.0.0"), eq("3.1.0"), eq("test-repo")))
                .thenReturn(mockResponse);

        String result = tool.predictDependencyChange(
                "org.springframework.boot:spring-boot-starter-web", "UPGRADE",
                "3.0.0", "3.1.0", "test-repo");

        assertNotNull(result);
        assertTrue(result.contains("org.springframework.boot:spring-boot-starter-web"));
        assertTrue(result.contains("UPGRADE"));
        assertTrue(result.contains("3.0.0"));
        assertTrue(result.contains("3.1.0"));
    }

    @Test
    void testPredictDependencyChangeFromDescription() {
        DependencyChangePredictionResponse mockResponse = createMockAddResponse();

        when(predictionService.predictDependencyChangeFromDescription(
                eq("Add com.example:new-lib version 1.0.0"), eq("test-repo")))
                .thenReturn(mockResponse);

        String result = tool.predictDependencyChangeFromDescription(
                "Add com.example:new-lib version 1.0.0", "test-repo");

        assertNotNull(result);
        assertTrue(result.contains("com.example:new-lib"));
    }

    @Test
    void testErrorHandlingForIllegalArgumentException() {
        when(predictionService.predictDependencyChange(
                any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Invalid dependency name"));

        String result = tool.predictDependencyChange(null, "ADD", null, "1.0.0", "test-repo");

        assertTrue(result.startsWith("ERROR:"));
        assertTrue(result.contains("Invalid dependency name"));
    }

    @Test
    void testErrorHandlingForUnexpectedException() {
        when(predictionService.predictDependencyChange(
                any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Unexpected error"));

        String result = tool.predictDependencyChange("com.example:lib", "ADD", null, "1.0.0", "test-repo");

        assertTrue(result.startsWith("ERROR:"));
        assertTrue(result.contains("unexpected error"));
    }

    @Test
    void testErrorHandlingForDescriptionIllegalArgumentException() {
        when(predictionService.predictDependencyChangeFromDescription(
                anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Unable to extract dependency name"));

        String result = tool.predictDependencyChangeFromDescription("Invalid description", "test-repo");

        assertTrue(result.startsWith("ERROR:"));
        assertTrue(result.contains("Unable to extract dependency name"));
    }

    @Test
    void testFormattingContainsSections() {
        DependencyChangePredictionResponse response = new DependencyChangePredictionResponse();
        response.setProposedDependencyChange("ADD com.example:lib");
        response.setDependencyName("com.example:lib");
        response.setChangeType("ADD");
        response.setPredictionCategory("Testing Libraries");
        response.setMigrationEffortEstimate("LOW");
        response.setImpactedModules(Arrays.asList("Module 1", "Module 2"));
        response.setImpactedServices(Arrays.asList("Service A"));
        response.setTransitiveDependencyEffects(Arrays.asList("Effect 1"));
        response.setCompatibilityRisks(Arrays.asList("Risk 1"));
        response.setBuildRisks(Arrays.asList("Build Risk 1"));
        response.setTestingImpact(Arrays.asList("Test Impact 1"));
        response.setMigrationRecommendations(Arrays.asList("Recommendation 1"));
        response.setSuggestedValidationChecklist(Arrays.asList("[ ] Verify build compiles"));

        when(predictionService.predictDependencyChange(
                any(), any(), any(), any(), any()))
                .thenReturn(response);

        String result = tool.predictDependencyChange("com.example:lib", "ADD", null, "1.0.0", null);

        assertTrue(result.contains("Proposed Change"));
        assertTrue(result.contains("Impacted Modules"));
        assertTrue(result.contains("Impacted Services"));
        assertTrue(result.contains("Transitive Dependency Effects"));
        assertTrue(result.contains("Compatibility Risks"));
        assertTrue(result.contains("Build Risks"));
        assertTrue(result.contains("Testing Impact"));
        assertTrue(result.contains("Migration Recommendations"));
        assertTrue(result.contains("Suggested Validation Checklist"));
    }

    @Test
    void testFormattingWithoutOptionalVersions() {
        DependencyChangePredictionResponse response = new DependencyChangePredictionResponse();
        response.setProposedDependencyChange("REMOVE com.example:lib");
        response.setDependencyName("com.example:lib");
        response.setChangeType("REMOVE");
        response.setPredictionCategory("Third-Party Frameworks");
        response.setCurrentVersion("1.0.0");
        response.setMigrationEffortEstimate("HIGH");
        response.setCircularDependencyDetected(false);

        when(predictionService.predictDependencyChange(
                any(), any(), any(), any(), any()))
                .thenReturn(response);

        String result = tool.predictDependencyChange("com.example:lib", "REMOVE", "1.0.0", null, null);

        assertNotNull(result);
        assertTrue(result.contains("com.example:lib"));
        assertTrue(result.contains("REMOVE"));
        assertTrue(result.contains("1.0.0"));
        assertTrue(result.contains("No"));
    }

    /**
     * Creates a mock response for ADD scenario.
     */
    private DependencyChangePredictionResponse createMockAddResponse() {
        DependencyChangePredictionResponse response = new DependencyChangePredictionResponse();
        response.setProposedDependencyChange("ADD com.example:new-lib");
        response.setDependencyName("com.example:new-lib");
        response.setChangeType("ADD");
        response.setPredictionCategory("Third-Party Frameworks");
        response.setNewVersion("1.0.0");
        response.setMigrationEffortEstimate("MEDIUM");
        response.setCircularDependencyDetected(false);
        response.setImpactedModules(Arrays.asList(
                "Core application module",
                "Build configuration (pom.xml / build.gradle)",
                "Module dependency graph",
                "Dependency resolution tree"
        ));
        response.setImpactedServices(Arrays.asList(
                "Integration service",
                "External API client service"
        ));
        response.setTransitiveDependencyEffects(Arrays.asList(
                "New transitive dependencies will be pulled in",
                "May introduce version conflicts with existing dependencies"
        ));
        response.setCompatibilityRisks(Arrays.asList(
                "New dependency may conflict with existing dependency versions",
                "License compatibility with existing dependencies should be verified"
        ));
        response.setBuildRisks(Arrays.asList(
                "Build time may increase due to additional dependency resolution",
                "New dependency may increase final artifact size"
        ));
        response.setTestingImpact(Arrays.asList(
                "Verify new dependency works correctly in all environments",
                "Test integration with existing code that uses the new dependency"
        ));
        response.setMigrationRecommendations(Arrays.asList(
                "Review dependency scope (compile, runtime, test, provided)",
                "Check for existing transitive availability before adding explicitly"
        ));
        response.setSuggestedValidationChecklist(Arrays.asList(
                "[ ] Verify build compiles successfully after dependency change",
                "[ ] Run full test suite and verify all tests pass",
                "[ ] Verify dependency convergence in multi-module projects"
        ));
        return response;
    }

    /**
     * Creates a mock response for UPGRADE scenario.
     */
    private DependencyChangePredictionResponse createMockUpgradeResponse() {
        DependencyChangePredictionResponse response = new DependencyChangePredictionResponse();
        response.setProposedDependencyChange("UPGRADE org.springframework.boot:spring-boot-starter-web");
        response.setDependencyName("org.springframework.boot:spring-boot-starter-web");
        response.setChangeType("UPGRADE");
        response.setPredictionCategory("Spring Dependencies");
        response.setCurrentVersion("3.0.0");
        response.setNewVersion("3.1.0");
        response.setMigrationEffortEstimate("HIGH");
        response.setCircularDependencyDetected(false);
        response.setImpactedModules(Arrays.asList(
                "Build configuration (pom.xml / build.gradle)",
                "Module dependency graph",
                "Dependency resolution tree",
                "Transitive dependency chain"
        ));
        response.setImpactedServices(Arrays.asList(
                "Spring context management",
                "Bean lifecycle management",
                "Dependency injection service",
                "Services using the dependency may need recompilation"
        ));
        response.setCompatibilityRisks(Arrays.asList(
                "API breaking changes in newer versions may affect consuming code",
                "Deprecated APIs removed in newer versions require code changes",
                "Major version jump (3.0.0 -> 3.1.0) indicates potential breaking changes"
        ));
        return response;
    }
}