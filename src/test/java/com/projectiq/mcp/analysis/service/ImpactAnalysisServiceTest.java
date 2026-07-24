package com.projectiq.mcp.analysis.service;

import com.projectiq.mcp.analysis.dto.*;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse.ImpactedComponent;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse.RiskItem;
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
class ImpactAnalysisServiceTest {

    @Mock
    private TaskAnalysisService taskAnalysisService;

    @Mock
    private ContextAssemblyService contextAssemblyService;

    private ImpactAnalysisService impactAnalysisService;

    @BeforeEach
    void setUp() {
        impactAnalysisService = new ImpactAnalysisService(
                taskAnalysisService, contextAssemblyService);
    }

    @Test
    void analyzeImpact_withFeatureRequest_returnsCompleteReport() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.NEW_FEATURE,
                "Add pagination to UserController", ConfidenceLevel.HIGH);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        // Act
        ImpactAnalysisResponse response = impactAnalysisService.analyzeImpact(
                "Add pagination to UserController", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertEquals("Add pagination to UserController", response.getOriginalTask());
        assertEquals("New Feature", response.getTaskType());
        assertFalse(response.getPrimaryTargets().isEmpty());
        assertFalse(response.getDirectlyAffectedComponents().isEmpty());
        assertFalse(response.getIndirectlyAffectedComponents().isEmpty());
        assertNotNull(response.getEstimatedImplementationScope());
        assertNotNull(response.getEstimatedTestingScope());
        assertNotNull(response.getPotentialRisks());
        assertNotNull(response.getConfidenceLevel());
        assertFalse(response.getDependencyImpact().isEmpty());

        verify(taskAnalysisService).analyze("Add pagination to UserController");
    }

    @Test
    void analyzeImpact_withBugFixRequest_returnsBugFixReport() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.BUG_FIX,
                "Fix null pointer exception in UserService", ConfidenceLevel.HIGH);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        // Act
        ImpactAnalysisResponse response = impactAnalysisService.analyzeImpact(
                "Fix null pointer exception in UserService", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertEquals("Bug Fix", response.getTaskType());
        assertFalse(response.getDirectlyAffectedComponents().isEmpty());
        assertFalse(response.getIndirectlyAffectedComponents().isEmpty());
        assertFalse(response.getDependencyImpact().isEmpty());
    }

    @Test
    void analyzeImpact_withRestApiChange_returnsApiReport() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.REST_API_CHANGE,
                "Add new endpoint to UserController", ConfidenceLevel.HIGH);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        // Act
        ImpactAnalysisResponse response = impactAnalysisService.analyzeImpact(
                "Add new endpoint to UserController", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertEquals("REST API Change", response.getTaskType());
        assertFalse(response.getDirectlyAffectedComponents().isEmpty());
        assertFalse(response.getDependencyImpact().isEmpty());
    }

    @Test
    void analyzeImpact_withDependencyChange_returnsDependencyReport() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.CONFIGURATION_CHANGE,
                "Update database dependency version", ConfidenceLevel.MEDIUM);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        // Act
        ImpactAnalysisResponse response = impactAnalysisService.analyzeImpact(
                "Update database dependency version", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertEquals("Configuration Change", response.getTaskType());
        assertFalse(response.getDependencyImpact().isEmpty());
    }

    @Test
    void analyzeImpact_withConfigurationChange_returnsConfigReport() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.CONFIGURATION_CHANGE,
                "Modify ApplicationProperties config settings", ConfidenceLevel.MEDIUM);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        // Act
        ImpactAnalysisResponse response = impactAnalysisService.analyzeImpact(
                "Modify ApplicationProperties config settings", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertEquals("Configuration Change", response.getTaskType());
        assertTrue(response.getDirectlyAffectedComponents().stream()
                .anyMatch(c -> "Configuration".equals(c.getComponentType())),
                () -> "Expected Configuration component in: " + response.getDirectlyAffectedComponents());
    }

    @Test
    void analyzeImpact_withEmptyTask_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                impactAnalysisService.analyzeImpact("", "test-repo", "main"));
    }

    @Test
    void analyzeImpact_withNullTask_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                impactAnalysisService.analyzeImpact(null, "test-repo", "main"));
    }

    @Test
    void analyzeImpact_withContextAssemblyFailure_continuesGracefully() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.NEW_FEATURE,
                "Add pagination", ConfidenceLevel.MEDIUM);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);
        when(contextAssemblyService.assembleContext(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Indexer unreachable"));

        // Act
        ImpactAnalysisResponse response = impactAnalysisService.analyzeImpact(
                "Add pagination", "test-repo", "main");

        // Assert - should still produce a valid response even without context
        assertNotNull(response);
        assertEquals("Add pagination", response.getOriginalTask());
        assertFalse(response.getDirectlyAffectedComponents().isEmpty());
        assertFalse(response.getIndirectlyAffectedComponents().isEmpty());
        assertNotNull(response.getEstimatedImplementationScope());
        assertNotNull(response.getConfidenceLevel());
    }

    @Test
    void analyzeImpact_withNullBranch_defaultsToMain() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.NEW_FEATURE,
                "Add pagination", ConfidenceLevel.MEDIUM);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        // Act
        ImpactAnalysisResponse response = impactAnalysisService.analyzeImpact(
                "Add pagination", "test-repo", null);

        // Assert
        assertNotNull(response);
        verify(contextAssemblyService).assembleContext(
                "Add pagination", "test-repo", "main");
    }

    @Test
    void analyzeImpact_withEmptyBranch_defaultsToMain() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.NEW_FEATURE,
                "Add pagination", ConfidenceLevel.MEDIUM);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        // Act
        ImpactAnalysisResponse response = impactAnalysisService.analyzeImpact(
                "Add pagination", "test-repo", "");

        // Assert
        assertNotNull(response);
        verify(contextAssemblyService).assembleContext(
                "Add pagination", "test-repo", "main");
    }

    @Test
    void analyzeImpact_returnsDeterministicOutput() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.NEW_FEATURE,
                "Add pagination to UserController", ConfidenceLevel.HIGH);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        // Act - run twice
        ImpactAnalysisResponse response1 = impactAnalysisService.analyzeImpact(
                "Add pagination to UserController", "test-repo", "main");
        ImpactAnalysisResponse response2 = impactAnalysisService.analyzeImpact(
                "Add pagination to UserController", "test-repo", "main");

        // Assert - both responses should have the same structure
        assertEquals(response1.getTaskType(), response2.getTaskType());
        assertEquals(response1.getPrimaryTargets().size(), response2.getPrimaryTargets().size());
        assertEquals(response1.getDirectlyAffectedComponents().size(),
                response2.getDirectlyAffectedComponents().size());
        assertEquals(response1.getIndirectlyAffectedComponents().size(),
                response2.getIndirectlyAffectedComponents().size());
        assertEquals(response1.getEstimatedImplementationScope(),
                response2.getEstimatedImplementationScope());
        assertEquals(response1.getEstimatedTestingScope(),
                response2.getEstimatedTestingScope());
        assertEquals(response1.getConfidenceLevel(), response2.getConfidenceLevel());
    }

    @Test
    void identifyPrimaryTargets_withDetectedEntities_returnsEntities() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.NEW_FEATURE,
                "Modify UserController", ConfidenceLevel.HIGH);
        analysis.addDetectedEntity("UserController (Controller)");
        analysis.addDetectedEntity("UserService (Service)");

        // Act
        List<String> targets = impactAnalysisService.identifyPrimaryTargets(
                analysis, "Modify UserController", "modify usercontroller");

        // Assert
        assertFalse(targets.isEmpty());
        assertTrue(targets.contains("UserController (Controller)"));
    }

    @Test
    void identifyPrimaryTargets_withoutEntities_derivesFromTaskType() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.NEW_FEATURE,
                "Add new feature", ConfidenceLevel.LOW);

        // Act
        List<String> targets = impactAnalysisService.identifyPrimaryTargets(
                analysis, "Add new feature", "add new feature");

        // Assert
        assertFalse(targets.isEmpty());
        assertTrue(targets.get(0).contains("New Feature"));
    }

    @Test
    void identifyDirectlyAffectedComponents_withController_returnsController() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.REST_API_CHANGE,
                "Modify UserController", ConfidenceLevel.HIGH);

        // Act
        List<ImpactedComponent> components = impactAnalysisService
                .identifyDirectlyAffectedComponents(analysis, "Modify UserController", "modify usercontroller");

        // Assert
        assertFalse(components.isEmpty());
        assertTrue(components.stream().anyMatch(c -> c.getComponentName().contains("UserController")));
    }

    @Test
    void identifyIndirectlyAffectedComponents_includesTesting() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.NEW_FEATURE,
                "Add pagination", ConfidenceLevel.HIGH);
        List<ImpactedComponent> directlyAffected = new ArrayList<>();
        directlyAffected.add(new ImpactedComponent("UserController", "Class", "Test"));

        // Act
        List<ImpactedComponent> components = impactAnalysisService
                .identifyIndirectlyAffectedComponents(
                        analysis, "Add pagination", "add pagination", directlyAffected);

        // Assert
        assertTrue(components.stream().anyMatch(c -> "Testing".equals(c.getComponentType())));
    }

    @Test
    void identifyDependencyImpact_withDependencyKeywords_returnsImpacts() {
        // Act
        List<String> impacts = impactAnalysisService.identifyDependencyImpact(
                "update maven dependency version", TaskType.CONFIGURATION_CHANGE);

        // Assert
        assertFalse(impacts.isEmpty());
        assertTrue(impacts.stream().anyMatch(i -> i.contains("dependencies")),
                () -> "Expected dependency-related impact in: " + impacts);
    }

    @Test
    void estimateImplementationScope_withManyComponents_returnsLarge() {
        // Arrange
        List<ImpactedComponent> direct = new ArrayList<>();
        direct.add(new ImpactedComponent("C1", "Class", "R1"));
        direct.add(new ImpactedComponent("C2", "Class", "R2"));
        direct.add(new ImpactedComponent("C3", "Class", "R3"));
        direct.add(new ImpactedComponent("C4", "Class", "R4"));
        List<ImpactedComponent> indirect = new ArrayList<>();
        indirect.add(new ImpactedComponent("I1", "Class", "R5"));
        indirect.add(new ImpactedComponent("I2", "Class", "R6"));

        // Act
        ScopeLevel scope = impactAnalysisService.estimateImplementationScope(
                TaskType.DATABASE_CHANGE, direct, indirect,
                "database migration refactor");

        // Assert
        assertEquals(ScopeLevel.LARGE, scope);
    }

    @Test
    void estimateImplementationScope_withFewComponents_returnsSmall() {
        // Arrange
        List<ImpactedComponent> direct = new ArrayList<>();
        direct.add(new ImpactedComponent("C1", "Class", "R1"));
        List<ImpactedComponent> indirect = new ArrayList<>();

        // Act
        ScopeLevel scope = impactAnalysisService.estimateImplementationScope(
                TaskType.UNIT_TEST, direct, indirect, "add unit test");

        // Assert
        assertEquals(ScopeLevel.SMALL, scope);
    }

    @Test
    void estimateTestingScope_withLargeImplScopeAndManyComponents_returnsLarge() {
        // Arrange
        List<ImpactedComponent> direct = new ArrayList<>();
        direct.add(new ImpactedComponent("C1", "Class", "R1"));
        direct.add(new ImpactedComponent("C2", "Class", "R2"));
        direct.add(new ImpactedComponent("C3", "Class", "R3"));
        List<ImpactedComponent> indirect = new ArrayList<>();
        indirect.add(new ImpactedComponent("I1", "Class", "R4"));
        indirect.add(new ImpactedComponent("I2", "Class", "R5"));
        indirect.add(new ImpactedComponent("I3", "Class", "R6"));

        // Act
        ScopeLevel scope = impactAnalysisService.estimateTestingScope(
                ScopeLevel.LARGE, direct, indirect, TaskType.DATABASE_CHANGE);

        // Assert
        assertEquals(ScopeLevel.LARGE, scope);
    }

    @Test
    void estimateTestingScope_withSmallImplScope_returnsSmall() {
        // Act
        ScopeLevel scope = impactAnalysisService.estimateTestingScope(
                ScopeLevel.SMALL, new ArrayList<>(), new ArrayList<>(), TaskType.DOCUMENTATION);

        // Assert
        assertEquals(ScopeLevel.SMALL, scope);
    }

    @Test
    void identifyRisks_withDatabaseChange_returnsHighRisk() {
        // Act
        List<RiskItem> risks = impactAnalysisService.identifyRisks(
                TaskType.DATABASE_CHANGE, new ArrayList<>(), new ArrayList<>(),
                "database migration");

        // Assert
        assertFalse(risks.isEmpty());
        assertTrue(risks.stream().anyMatch(r -> r.getRiskLevel() == RiskLevel.HIGH));
    }

    @Test
    void identifyRisks_withSecurityKeywords_returnsHighRisk() {
        // Act
        List<RiskItem> risks = impactAnalysisService.identifyRisks(
                TaskType.NEW_FEATURE, new ArrayList<>(), new ArrayList<>(),
                "add authentication");

        // Assert
        assertFalse(risks.isEmpty());
        assertTrue(risks.stream().anyMatch(r -> r.getRiskLevel() == RiskLevel.HIGH));
    }

    @Test
    void identifyRisks_withSimpleTask_returnsLowRisk() {
        // Act
        List<RiskItem> risks = impactAnalysisService.identifyRisks(
                TaskType.DOCUMENTATION, new ArrayList<>(), new ArrayList<>(),
                "update readme");

        // Assert
        assertFalse(risks.isEmpty());
        assertTrue(risks.stream().anyMatch(r -> r.getRiskLevel() == RiskLevel.LOW));
    }

    @Test
    void determineConfidence_preservesTaskAnalysisConfidence() {
        assertEquals(ConfidenceLevel.HIGH,
                impactAnalysisService.determineConfidence(ConfidenceLevel.HIGH));
        assertEquals(ConfidenceLevel.MEDIUM,
                impactAnalysisService.determineConfidence(ConfidenceLevel.MEDIUM));
        assertEquals(ConfidenceLevel.LOW,
                impactAnalysisService.determineConfidence(ConfidenceLevel.LOW));
    }

    @Test
    void analyzeImpact_withDatabaseChange_returnsDatabaseReport() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.DATABASE_CHANGE,
                "Add new column to UserEntity", ConfidenceLevel.HIGH);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        // Act
        ImpactAnalysisResponse response = impactAnalysisService.analyzeImpact(
                "Add new column to UserEntity", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertEquals("Database Change", response.getTaskType());
        assertFalse(response.getDirectlyAffectedComponents().isEmpty());
        assertFalse(response.getIndirectlyAffectedComponents().isEmpty());
        assertTrue(response.getPotentialRisks().stream()
                .anyMatch(r -> r.getRiskLevel() == RiskLevel.HIGH));
    }

    @Test
    void analyzeImpact_withRefactoring_returnsRefactoringReport() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.REFACTORING,
                "Refactor UserService to use new pattern", ConfidenceLevel.HIGH);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        // Act
        ImpactAnalysisResponse response = impactAnalysisService.analyzeImpact(
                "Refactor UserService to use new pattern", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertEquals("Refactoring", response.getTaskType());
        assertFalse(response.getDirectlyAffectedComponents().isEmpty());
        assertTrue(response.getPotentialRisks().stream()
                .anyMatch(r -> r.getRiskLevel() == RiskLevel.HIGH));
    }

    /**
     * Creates a sample TaskAnalysisResponse for testing.
     */
    private TaskAnalysisResponse createAnalysis(TaskType type, String task, ConfidenceLevel confidence) {
        TaskAnalysisResponse analysis = new TaskAnalysisResponse();
        analysis.setOriginalTask(task);
        analysis.setTaskType(type);
        analysis.setConfidenceLevel(confidence);

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
}