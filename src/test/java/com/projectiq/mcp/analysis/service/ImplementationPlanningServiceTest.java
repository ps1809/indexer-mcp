package com.projectiq.mcp.analysis.service;

import com.projectiq.mcp.analysis.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImplementationPlanningServiceTest {

    @Mock
    private TaskAnalysisService taskAnalysisService;

    @Mock
    private ContextAssemblyService contextAssemblyService;

    @Mock
    private ImpactAnalysisService impactAnalysisService;

    private ImplementationPlanningService planningService;

    @BeforeEach
    void setUp() {
        planningService = new ImplementationPlanningService(
                taskAnalysisService, contextAssemblyService, impactAnalysisService);
    }

    @Test
    void generatePlan_withFeatureRequest_returnsCompletePlan() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.NEW_FEATURE,
                "Add pagination to UserController", ConfidenceLevel.HIGH, ComplexityLevel.MEDIUM);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        ImpactAnalysisResponse impactAnalysis = createImpactAnalysis("New Feature");
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(impactAnalysis);

        // Act
        ImplementationPlanningResponse response = planningService.generatePlan(
                "Add pagination to UserController", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertEquals("Add pagination to UserController", response.getOriginalTask());
        assertEquals("New Feature", response.getTaskType());
        assertEquals("MEDIUM", response.getEstimatedComplexity());
        assertFalse(response.getRecommendedImplementationOrder().isEmpty());
        assertFalse(response.getFilesToModify().isEmpty());
        assertFalse(response.getFilesToReview().isEmpty());
        assertFalse(response.getComponentsAffected().isEmpty());
        assertFalse(response.getDependenciesInvolved().isEmpty());
        assertFalse(response.getSuggestedValidationSteps().isEmpty());
        assertNotNull(response.getSuggestedTestingScope());
        assertFalse(response.getRisks().isEmpty());
        assertFalse(response.getAssumptions().isEmpty());

        verify(taskAnalysisService).analyze("Add pagination to UserController");
        verify(impactAnalysisService).analyzeImpact("Add pagination to UserController", "test-repo", "main");
    }

    @Test
    void generatePlan_withBugFixRequest_returnsBugFixPlan() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.BUG_FIX,
                "Fix null pointer exception in UserService", ConfidenceLevel.HIGH, ComplexityLevel.LOW);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        ImpactAnalysisResponse impactAnalysis = createImpactAnalysis("Bug Fix");
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(impactAnalysis);

        // Act
        ImplementationPlanningResponse response = planningService.generatePlan(
                "Fix null pointer exception in UserService", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertEquals("Bug Fix", response.getTaskType());
        assertEquals("LOW", response.getEstimatedComplexity());
        assertFalse(response.getRecommendedImplementationOrder().isEmpty());
        assertTrue(response.getAssumptions().stream()
                .anyMatch(a -> a.contains("Bug reproduction")));
    }

    @Test
    void generatePlan_withRestApiEnhancement_returnsApiPlan() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.REST_API_CHANGE,
                "Add new endpoint to UserController", ConfidenceLevel.HIGH, ComplexityLevel.MEDIUM);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        ImpactAnalysisResponse impactAnalysis = createImpactAnalysis("REST API Change");
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(impactAnalysis);

        // Act
        ImplementationPlanningResponse response = planningService.generatePlan(
                "Add new endpoint to UserController", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertEquals("REST API Change", response.getTaskType());
        assertFalse(response.getRecommendedImplementationOrder().isEmpty());
        assertTrue(response.getAssumptions().stream()
                .anyMatch(a -> a.contains("API consumers")));
    }

    @Test
    void generatePlan_withRefactoringRequest_returnsRefactoringPlan() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.REFACTORING,
                "Refactor UserService to use new pattern", ConfidenceLevel.HIGH, ComplexityLevel.HIGH);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        ImpactAnalysisResponse impactAnalysis = createImpactAnalysis("Refactoring");
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(impactAnalysis);

        // Act
        ImplementationPlanningResponse response = planningService.generatePlan(
                "Refactor UserService to use new pattern", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertEquals("Refactoring", response.getTaskType());
        assertEquals("HIGH", response.getEstimatedComplexity());
        assertFalse(response.getRecommendedImplementationOrder().isEmpty());
        assertTrue(response.getAssumptions().stream()
                .anyMatch(a -> a.contains("test coverage")));
    }

    @Test
    void generatePlan_withEmptyTask_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                planningService.generatePlan("", "test-repo", "main"));
    }

    @Test
    void generatePlan_withNullTask_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                planningService.generatePlan(null, "test-repo", "main"));
    }

    @Test
    void generatePlan_withContextAssemblyFailure_continuesGracefully() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.NEW_FEATURE,
                "Add pagination", ConfidenceLevel.MEDIUM, ComplexityLevel.LOW);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);
        when(contextAssemblyService.assembleContext(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Indexer unreachable"));

        ImpactAnalysisResponse impactAnalysis = createImpactAnalysis("New Feature");
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(impactAnalysis);

        // Act
        ImplementationPlanningResponse response = planningService.generatePlan(
                "Add pagination", "test-repo", "main");

        // Assert - should still produce a valid plan
        assertNotNull(response);
        assertEquals("Add pagination", response.getOriginalTask());
        assertFalse(response.getRecommendedImplementationOrder().isEmpty());
        assertFalse(response.getFilesToModify().isEmpty());
    }

    @Test
    void generatePlan_withImpactAnalysisFailure_continuesGracefully() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.NEW_FEATURE,
                "Add pagination", ConfidenceLevel.MEDIUM, ComplexityLevel.LOW);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Impact analysis failed"));

        // Act
        ImplementationPlanningResponse response = planningService.generatePlan(
                "Add pagination", "test-repo", "main");

        // Assert - should still produce a valid plan
        assertNotNull(response);
        assertEquals("Add pagination", response.getOriginalTask());
        assertFalse(response.getRecommendedImplementationOrder().isEmpty());
        assertFalse(response.getFilesToModify().isEmpty());
        assertFalse(response.getRisks().isEmpty());
    }

    @Test
    void generatePlan_withNullBranch_defaultsToMain() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.NEW_FEATURE,
                "Add pagination", ConfidenceLevel.MEDIUM, ComplexityLevel.LOW);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        // Act
        ImplementationPlanningResponse response = planningService.generatePlan(
                "Add pagination", "test-repo", null);

        // Assert
        assertNotNull(response);
        verify(contextAssemblyService).assembleContext(
                "Add pagination", "test-repo", "main");
    }

    @Test
    void generatePlan_withEmptyBranch_defaultsToMain() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.NEW_FEATURE,
                "Add pagination", ConfidenceLevel.MEDIUM, ComplexityLevel.LOW);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        // Act
        ImplementationPlanningResponse response = planningService.generatePlan(
                "Add pagination", "test-repo", "");

        // Assert
        assertNotNull(response);
        verify(contextAssemblyService).assembleContext(
                "Add pagination", "test-repo", "main");
    }

    @Test
    void generatePlan_returnsDeterministicOutput() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.NEW_FEATURE,
                "Add pagination to UserController", ConfidenceLevel.HIGH, ComplexityLevel.MEDIUM);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        ImpactAnalysisResponse impactAnalysis = createImpactAnalysis("New Feature");
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(impactAnalysis);

        // Act - run twice
        ImplementationPlanningResponse response1 = planningService.generatePlan(
                "Add pagination to UserController", "test-repo", "main");
        ImplementationPlanningResponse response2 = planningService.generatePlan(
                "Add pagination to UserController", "test-repo", "main");

        // Assert - both responses should be identical
        assertEquals(response1.getTaskType(), response2.getTaskType());
        assertEquals(response1.getEstimatedComplexity(), response2.getEstimatedComplexity());
        assertEquals(response1.getRecommendedImplementationOrder().size(),
                response2.getRecommendedImplementationOrder().size());
        assertEquals(response1.getFilesToModify().size(), response2.getFilesToModify().size());
        assertEquals(response1.getFilesToReview().size(), response2.getFilesToReview().size());
        assertEquals(response1.getComponentsAffected().size(), response2.getComponentsAffected().size());
        assertEquals(response1.getDependenciesInvolved().size(), response2.getDependenciesInvolved().size());
        assertEquals(response1.getSuggestedValidationSteps().size(),
                response2.getSuggestedValidationSteps().size());
        assertEquals(response1.getSuggestedTestingScope(), response2.getSuggestedTestingScope());
        assertEquals(response1.getRisks().size(), response2.getRisks().size());
        assertEquals(response1.getAssumptions().size(), response2.getAssumptions().size());
    }

    @Test
    void generatePlan_withDatabaseChange_returnsDatabasePlan() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.DATABASE_CHANGE,
                "Add new column to UserEntity", ConfidenceLevel.HIGH, ComplexityLevel.HIGH);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        ImpactAnalysisResponse impactAnalysis = createImpactAnalysis("Database Change");
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(impactAnalysis);

        // Act
        ImplementationPlanningResponse response = planningService.generatePlan(
                "Add new column to UserEntity", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertEquals("Database Change", response.getTaskType());
        assertFalse(response.getRecommendedImplementationOrder().isEmpty());
        assertTrue(response.getAssumptions().stream()
                .anyMatch(a -> a.contains("Database schema")));
    }

    @Test
    void generatePlan_withConfigurationChange_returnsConfigPlan() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.CONFIGURATION_CHANGE,
                "Update application properties", ConfidenceLevel.MEDIUM, ComplexityLevel.LOW);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        // Use a configuration-specific impact analysis
        ImpactAnalysisResponse impactAnalysis = new ImpactAnalysisResponse();
        impactAnalysis.setOriginalTask("Update application properties");
        impactAnalysis.setTaskType("Configuration Change");

        List<String> primaryTargets = new ArrayList<>();
        primaryTargets.add("Configuration");
        impactAnalysis.setPrimaryTargets(primaryTargets);

        List<ImpactAnalysisResponse.ImpactedComponent> direct = new ArrayList<>();
        direct.add(new ImpactAnalysisResponse.ImpactedComponent(
                "ApplicationProperties", "Configuration", "Configuration to be modified"));
        impactAnalysis.setDirectlyAffectedComponents(direct);

        List<ImpactAnalysisResponse.ImpactedComponent> indirect = new ArrayList<>();
        indirect.add(new ImpactAnalysisResponse.ImpactedComponent(
                "Environment profiles", "Configuration", "Other environments may need aligned changes"));
        indirect.add(new ImpactAnalysisResponse.ImpactedComponent(
                "Unit tests", "Testing", "Tests for affected components"));
        impactAnalysis.setIndirectlyAffectedComponents(indirect);

        List<String> deps = new ArrayList<>();
        deps.add("Property changes may affect externalized configuration sources");
        impactAnalysis.setDependencyImpact(deps);

        impactAnalysis.setEstimatedImplementationScope(ScopeLevel.SMALL);
        impactAnalysis.setEstimatedTestingScope(ScopeLevel.SMALL);

        List<ImpactAnalysisResponse.RiskItem> risks = new ArrayList<>();
        risks.add(new ImpactAnalysisResponse.RiskItem(
                "Configuration changes may have environment-specific side effects",
                com.projectiq.mcp.analysis.dto.RiskLevel.MEDIUM,
                "Test configuration changes across all environments"));
        impactAnalysis.setPotentialRisks(risks);

        impactAnalysis.setConfidenceLevel(ConfidenceLevel.MEDIUM);

        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(impactAnalysis);

        // Act
        ImplementationPlanningResponse response = planningService.generatePlan(
                "Update application properties", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertEquals("Configuration Change", response.getTaskType());
        assertFalse(response.getFilesToModify().isEmpty());
        assertTrue(response.getFilesToModify().stream()
                .anyMatch(f -> f.contains("Configuration") || f.contains("application")));
    }

    @Test
    void generatePlan_withPerformanceImprovement_returnsPerformancePlan() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.PERFORMANCE_IMPROVEMENT,
                "Optimize database query performance", ConfidenceLevel.HIGH, ComplexityLevel.HIGH);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        ImpactAnalysisResponse impactAnalysis = createImpactAnalysis("Performance Improvement");
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(impactAnalysis);

        // Act
        ImplementationPlanningResponse response = planningService.generatePlan(
                "Optimize database query performance", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertEquals("Performance Improvement", response.getTaskType());
        assertFalse(response.getRecommendedImplementationOrder().isEmpty());
        assertTrue(response.getAssumptions().stream()
                .anyMatch(a -> a.contains("Performance baselines")));
    }

    @Test
    void generatePlan_withUnitTestRequest_returnsTestPlan() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.UNIT_TEST,
                "Add unit tests for UserService", ConfidenceLevel.HIGH, ComplexityLevel.LOW);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        ImpactAnalysisResponse impactAnalysis = createImpactAnalysis("Unit Test");
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(impactAnalysis);

        // Act
        ImplementationPlanningResponse response = planningService.generatePlan(
                "Add unit tests for UserService", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertEquals("Unit Test", response.getTaskType());
        assertFalse(response.getRecommendedImplementationOrder().isEmpty());
        assertTrue(response.getAssumptions().stream()
                .anyMatch(a -> a.contains("Test framework")));
    }

    @Test
    void generatePlan_withDocumentationRequest_returnsDocPlan() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.DOCUMENTATION,
                "Update API documentation", ConfidenceLevel.MEDIUM, ComplexityLevel.LOW);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        ImpactAnalysisResponse impactAnalysis = createImpactAnalysis("Documentation");
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(impactAnalysis);

        // Act
        ImplementationPlanningResponse response = planningService.generatePlan(
                "Update API documentation", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertEquals("Documentation", response.getTaskType());
        assertFalse(response.getRecommendedImplementationOrder().isEmpty());
        assertTrue(response.getAssumptions().stream()
                .anyMatch(a -> a.contains("Documentation generation")));
    }

    @Test
    void generatePlan_withUnknownTask_returnsDefaultPlan() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.UNKNOWN,
                "Do something", ConfidenceLevel.LOW, ComplexityLevel.LOW);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        // Act
        ImplementationPlanningResponse response = planningService.generatePlan(
                "Do something", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertEquals("Unknown", response.getTaskType());
        assertFalse(response.getRecommendedImplementationOrder().isEmpty());
        assertTrue(response.getAssumptions().stream()
                .anyMatch(a -> a.contains("unknown")));
    }

    /**
     * Creates a sample TaskAnalysisResponse for testing.
     */
    private TaskAnalysisResponse createAnalysis(TaskType type, String task,
                                                 ConfidenceLevel confidence, ComplexityLevel complexity) {
        TaskAnalysisResponse analysis = new TaskAnalysisResponse();
        analysis.setOriginalTask(task);
        analysis.setTaskType(type);
        analysis.setConfidenceLevel(confidence);
        analysis.setEstimatedComplexity(complexity);

        List<String> entities = new ArrayList<>();
        if (task.contains("UserController")) {
            entities.add("UserController (Controller)");
        }
        if (task.contains("UserService")) {
            entities.add("UserService (Service)");
        }
        if (task.contains("UserEntity")) {
            entities.add("UserEntity (Entity/Model)");
        }
        analysis.setDetectedEntities(entities);

        return analysis;
    }

    /**
     * Creates a sample ImpactAnalysisResponse for testing.
     */
    private ImpactAnalysisResponse createImpactAnalysis(String taskType) {
        ImpactAnalysisResponse response = new ImpactAnalysisResponse();
        response.setOriginalTask("Test task");
        response.setTaskType(taskType);

        List<String> primaryTargets = new ArrayList<>();
        primaryTargets.add("UserController (Controller)");
        response.setPrimaryTargets(primaryTargets);

        List<ImpactAnalysisResponse.ImpactedComponent> direct = new ArrayList<>();
        direct.add(new ImpactAnalysisResponse.ImpactedComponent(
                "UserController", "Class", "Directly referenced controller"));
        response.setDirectlyAffectedComponents(direct);

        List<ImpactAnalysisResponse.ImpactedComponent> indirect = new ArrayList<>();
        indirect.add(new ImpactAnalysisResponse.ImpactedComponent(
                "UserService", "Class", "Service layer associated with controller"));
        indirect.add(new ImpactAnalysisResponse.ImpactedComponent(
                "Unit tests", "Testing", "Tests for affected components"));
        response.setIndirectlyAffectedComponents(indirect);

        List<String> deps = new ArrayList<>();
        deps.add("Internal module dependencies");
        response.setDependencyImpact(deps);

        response.setEstimatedImplementationScope(ScopeLevel.MEDIUM);
        response.setEstimatedTestingScope(ScopeLevel.MEDIUM);

        List<ImpactAnalysisResponse.RiskItem> risks = new ArrayList<>();
        risks.add(new ImpactAnalysisResponse.RiskItem(
                "Test risk", com.projectiq.mcp.analysis.dto.RiskLevel.LOW, "Test mitigation"));
        response.setPotentialRisks(risks);

        response.setConfidenceLevel(ConfidenceLevel.HIGH);

        return response;
    }
}