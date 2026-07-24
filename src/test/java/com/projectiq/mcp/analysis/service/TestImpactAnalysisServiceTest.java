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
class TestImpactAnalysisServiceTest {

    @Mock
    private TaskAnalysisService taskAnalysisService;

    @Mock
    private ContextAssemblyService contextAssemblyService;

    @Mock
    private ImpactAnalysisService impactAnalysisService;

    @Mock
    private ImplementationPlanningService implementationPlanningService;

    private TestImpactAnalysisService testImpactService;

    @BeforeEach
    void setUp() {
        testImpactService = new TestImpactAnalysisService(
                taskAnalysisService, contextAssemblyService,
                impactAnalysisService, implementationPlanningService);
    }

    @Test
    void analyzeTestImpact_withFeatureRequest_returnsCompleteReport() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.NEW_FEATURE,
                "Add pagination to UserController", ConfidenceLevel.HIGH, ComplexityLevel.MEDIUM);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        ImpactAnalysisResponse impactAnalysis = createImpactAnalysis("New Feature");
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(impactAnalysis);

        ImplementationPlanningResponse implPlan = createImplPlan("New Feature");
        when(implementationPlanningService.generatePlan(anyString(), anyString(), anyString()))
                .thenReturn(implPlan);

        // Act
        TestImpactAnalysisResponse response = testImpactService.analyzeTestImpact(
                "Add pagination to UserController", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertEquals("Add pagination to UserController", response.getOriginalTask());
        assertFalse(response.getAffectedProductionClasses().isEmpty());
        assertFalse(response.getRelatedTestClasses().isEmpty());
        assertFalse(response.getRecommendedTestExecutionOrder().isEmpty());
        assertNotNull(response.getEstimatedTestingEffort());
        assertNotNull(response.getConfidenceLevel());
        assertNotNull(response.getTestingRationale());
    }

    @Test
    void analyzeTestImpact_withBugFix_returnsBugFixReport() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.BUG_FIX,
                "Fix null pointer in UserService", ConfidenceLevel.HIGH, ComplexityLevel.LOW);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        ImpactAnalysisResponse impactAnalysis = createImpactAnalysis("Bug Fix");
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(impactAnalysis);

        ImplementationPlanningResponse implPlan = createImplPlan("Bug Fix");
        when(implementationPlanningService.generatePlan(anyString(), anyString(), anyString()))
                .thenReturn(implPlan);

        // Act
        TestImpactAnalysisResponse response = testImpactService.analyzeTestImpact(
                "Fix null pointer in UserService", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertTrue(response.getRelatedTestClasses().stream()
                .anyMatch(t -> t.contains("UserServiceTest")));
        assertTrue(response.getMissingTests().stream()
                .anyMatch(m -> m.contains("Regression")));
    }

    @Test
    void analyzeTestImpact_withRefactoring_returnsRefactoringReport() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.REFACTORING,
                "Refactor UserService to use new pattern", ConfidenceLevel.MEDIUM, ComplexityLevel.HIGH);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        ImpactAnalysisResponse impactAnalysis = createImpactAnalysis("Refactoring");
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(impactAnalysis);

        ImplementationPlanningResponse implPlan = createImplPlan("Refactoring");
        when(implementationPlanningService.generatePlan(anyString(), anyString(), anyString()))
                .thenReturn(implPlan);

        // Act
        TestImpactAnalysisResponse response = testImpactService.analyzeTestImpact(
                "Refactor UserService to use new pattern", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertTrue(response.getMissingTests().stream()
                .anyMatch(m -> m.contains("refactored")));
    }

    @Test
    void analyzeTestImpact_withRestApiChange_returnsApiReport() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.REST_API_CHANGE,
                "Add new endpoint to UserController", ConfidenceLevel.HIGH, ComplexityLevel.MEDIUM);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        ImpactAnalysisResponse impactAnalysis = createImpactAnalysis("REST API Change");
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(impactAnalysis);

        ImplementationPlanningResponse implPlan = createImplPlan("REST API Change");
        when(implementationPlanningService.generatePlan(anyString(), anyString(), anyString()))
                .thenReturn(implPlan);

        // Act
        TestImpactAnalysisResponse response = testImpactService.analyzeTestImpact(
                "Add new endpoint to UserController", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertTrue(response.getRelatedTestClasses().stream()
                .anyMatch(t -> t.contains("Controller tests")));
        assertTrue(response.getMissingTests().stream()
                .anyMatch(m -> m.contains("API contract")));
    }

    @Test
    void analyzeTestImpact_withRepositoryChange_returnsRepoReport() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.DATABASE_CHANGE,
                "Add new field to UserEntity", ConfidenceLevel.HIGH, ComplexityLevel.MEDIUM);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        ImpactAnalysisResponse impactAnalysis = createImpactAnalysis("Database Change");
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(impactAnalysis);

        ImplementationPlanningResponse implPlan = createImplPlan("Database Change");
        when(implementationPlanningService.generatePlan(anyString(), anyString(), anyString()))
                .thenReturn(implPlan);

        // Act
        TestImpactAnalysisResponse response = testImpactService.analyzeTestImpact(
                "Add new field to UserEntity", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertTrue(response.getMissingTests().stream()
                .anyMatch(m -> m.contains("Database migration")));
    }

    @Test
    void analyzeTestImpact_withEmptyTask_throwsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> testImpactService.analyzeTestImpact("", "test-repo", "main"));
        assertThrows(IllegalArgumentException.class,
                () -> testImpactService.analyzeTestImpact(null, "test-repo", "main"));
    }

    @Test
    void analyzeTestImpact_withPartialContextFailure_returnsPartialReport() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.NEW_FEATURE,
                "Add pagination to UserController", ConfidenceLevel.HIGH, ComplexityLevel.MEDIUM);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        // Simulate context assembly failure
        doThrow(new RuntimeException("Context assembly failed"))
                .when(contextAssemblyService).assembleContext(anyString(), anyString(), anyString());

        // Simulate impact analysis failure
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Impact analysis failed"));

        // Simulate implementation plan failure
        when(implementationPlanningService.generatePlan(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Implementation plan failed"));

        // Act
        TestImpactAnalysisResponse response = testImpactService.analyzeTestImpact(
                "Add pagination to UserController", "test-repo", "main");

        // Assert - should still produce a report with defaults
        assertNotNull(response);
        assertFalse(response.getAffectedProductionClasses().isEmpty());
        assertFalse(response.getRelatedTestClasses().isEmpty());
        assertFalse(response.getRecommendedTestExecutionOrder().isEmpty());
        assertNotNull(response.getEstimatedTestingEffort());
        assertNotNull(response.getConfidenceLevel());
    }

    @Test
    void determineAffectedProductionClasses_withController_returnsController() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.REST_API_CHANGE,
                "Modify UserController", ConfidenceLevel.HIGH, ComplexityLevel.MEDIUM);
        ImpactAnalysisResponse impactAnalysis = createImpactAnalysis("REST API Change");

        // Act
        List<String> classes = testImpactService.determineAffectedProductionClasses(
                "Modify UserController", "modify usercontroller".toLowerCase(),
                TaskType.REST_API_CHANGE, analysis, impactAnalysis);

        // Assert
        assertFalse(classes.isEmpty());
        assertTrue(classes.stream().anyMatch(c -> c.contains("UserController")));
    }

    @Test
    void identifyRelatedTestClasses_withController_returnsControllerTest() {
        // Arrange
        List<String> affectedClasses = List.of("UserController (Controller)");

        // Act
        List<String> tests = testImpactService.identifyRelatedTestClasses(
                "Modify UserController", "modify usercontroller".toLowerCase(),
                TaskType.REST_API_CHANGE, affectedClasses, null);

        // Assert
        assertTrue(tests.stream().anyMatch(t -> t.contains("UserControllerTest")));
    }

    @Test
    void identifyMissingTests_withNewFeature_returnsMissingTests() {
        // Arrange
        List<String> affectedClasses = List.of("UserController (Controller)");
        List<String> relatedTests = List.of("UserControllerTest");

        // Act
        List<String> missing = testImpactService.identifyMissingTests(
                TaskType.NEW_FEATURE, "add new feature".toLowerCase(),
                affectedClasses, relatedTests);

        // Assert
        assertFalse(missing.isEmpty());
        assertTrue(missing.stream().anyMatch(m -> m.contains("Missing")));
    }

    @Test
    void buildTestExecutionOrder_withNewFeature_returnsOrderedSteps() {
        // Act
        List<String> order = testImpactService.buildTestExecutionOrder(
                TaskType.NEW_FEATURE, "add new feature".toLowerCase(),
                List.of("UserController (Controller)"));

        // Assert
        assertFalse(order.isEmpty());
        assertTrue(order.stream().anyMatch(s -> s.contains("Unit tests")));
        assertTrue(order.stream().anyMatch(s -> s.contains("Final verification")));
    }

    @Test
    void estimateTestingEffort_withHighComplexity_returnsHigh() {
        // Act
        String effort = testImpactService.estimateTestingEffort(
                TaskType.DATABASE_CHANGE,
                List.of("Entity1", "Entity2", "Entity3", "Entity4", "Entity5"),
                List.of("Test1", "Test2"),
                List.of("Missing1", "Missing2", "Missing3"));

        // Assert
        assertEquals("High", effort);
    }

    @Test
    void estimateTestingEffort_withLowComplexity_returnsLow() {
        // Act
        String effort = testImpactService.estimateTestingEffort(
                TaskType.DOCUMENTATION,
                List.of("DocComponent"),
                List.of(),
                List.of());

        // Assert
        assertEquals("Low", effort);
    }

    @Test
    void determineConfidence_withFullData_returnsHigh() {
        // Arrange
        ImpactAnalysisResponse impactAnalysis = createImpactAnalysis("Test");

        // Act
        String confidence = testImpactService.determineConfidence(
                TaskType.NEW_FEATURE,
                List.of("UserController"),
                impactAnalysis);

        // Assert
        assertEquals("High", confidence);
    }

    @Test
    void determineConfidence_withNoData_returnsBaseConfidence() {
        // Act
        String confidence = testImpactService.determineConfidence(
                TaskType.UNKNOWN, List.of(), null);

        // Assert
        assertEquals("Low", confidence);
    }

    @Test
    void generateTestingRationale_withAllData_returnsCompleteRationale() {
        // Act
        String rationale = testImpactService.generateTestingRationale(
                TaskType.NEW_FEATURE, "add new feature".toLowerCase(),
                List.of("UserController (Controller)"),
                List.of("UserControllerTest"),
                List.of("Missing: UserControllerTest (may need to be created)"));

        // Assert
        assertNotNull(rationale);
        assertTrue(rationale.contains("new feature"));
        assertTrue(rationale.contains("UserController"));
        assertTrue(rationale.contains("UserControllerTest"));
    }

    @Test
    void analyzeTestImpact_withDocumentation_returnsLowEffort() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.DOCUMENTATION,
                "Update API documentation", ConfidenceLevel.HIGH, ComplexityLevel.LOW);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        // Act
        TestImpactAnalysisResponse response = testImpactService.analyzeTestImpact(
                "Update API documentation", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertEquals("Low", response.getEstimatedTestingEffort());
    }

    @Test
    void analyzeTestImpact_withUnitTest_returnsLowEffort() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.UNIT_TEST,
                "Add unit tests for UserService", ConfidenceLevel.HIGH, ComplexityLevel.LOW);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        // Act
        TestImpactAnalysisResponse response = testImpactService.analyzeTestImpact(
                "Add unit tests for UserService", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertEquals("Low", response.getEstimatedTestingEffort());
    }

    @Test
    void analyzeTestImpact_withPerformanceImprovement_returnsHighEffort() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.PERFORMANCE_IMPROVEMENT,
                "Optimize database query performance", ConfidenceLevel.MEDIUM, ComplexityLevel.HIGH);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        ImpactAnalysisResponse impactAnalysis = createImpactAnalysis("Performance Improvement");
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(impactAnalysis);

        ImplementationPlanningResponse implPlan = createImplPlan("Performance Improvement");
        when(implementationPlanningService.generatePlan(anyString(), anyString(), anyString()))
                .thenReturn(implPlan);

        // Act
        TestImpactAnalysisResponse response = testImpactService.analyzeTestImpact(
                "Optimize database query performance", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertTrue(response.getMissingTests().stream()
                .anyMatch(m -> m.contains("benchmark")));
    }

    @Test
    void analyzeTestImpact_withConfigurationChange_returnsConfigReport() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.CONFIGURATION_CHANGE,
                "Update application properties", ConfidenceLevel.HIGH, ComplexityLevel.LOW);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        // Act
        TestImpactAnalysisResponse response = testImpactService.analyzeTestImpact(
                "Update application properties", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertFalse(response.getRecommendedTestExecutionOrder().isEmpty());
        assertTrue(response.getRecommendedTestExecutionOrder().stream()
                .anyMatch(s -> s.contains("Configuration")));
    }

    @Test
    void analyzeTestImpact_deterministicOutput_returnsSameResult() {
        // Arrange
        TaskAnalysisResponse analysis = createAnalysis(TaskType.NEW_FEATURE,
                "Add pagination to UserController", ConfidenceLevel.HIGH, ComplexityLevel.MEDIUM);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        ImpactAnalysisResponse impactAnalysis = createImpactAnalysis("New Feature");
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(impactAnalysis);

        ImplementationPlanningResponse implPlan = createImplPlan("New Feature");
        when(implementationPlanningService.generatePlan(anyString(), anyString(), anyString()))
                .thenReturn(implPlan);

        // Act
        TestImpactAnalysisResponse first = testImpactService.analyzeTestImpact(
                "Add pagination to UserController", "test-repo", "main");
        TestImpactAnalysisResponse second = testImpactService.analyzeTestImpact(
                "Add pagination to UserController", "test-repo", "main");

        // Assert
        assertEquals(first.getAffectedProductionClasses(), second.getAffectedProductionClasses());
        assertEquals(first.getRelatedTestClasses(), second.getRelatedTestClasses());
        assertEquals(first.getMissingTests(), second.getMissingTests());
        assertEquals(first.getRecommendedTestExecutionOrder(), second.getRecommendedTestExecutionOrder());
        assertEquals(first.getEstimatedTestingEffort(), second.getEstimatedTestingEffort());
        assertEquals(first.getConfidenceLevel(), second.getConfidenceLevel());
    }

    // --- Helper methods ---

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

    private ImplementationPlanningResponse createImplPlan(String taskType) {
        ImplementationPlanningResponse response = new ImplementationPlanningResponse();
        response.setOriginalTask("Test task");
        response.setTaskType(taskType);
        response.setEstimatedComplexity("MEDIUM");

        List<String> order = new ArrayList<>();
        order.add("1. Analyze the repository structure");
        order.add("2. Implement the changes");
        response.setRecommendedImplementationOrder(order);

        List<String> files = new ArrayList<>();
        files.add("UserController class");
        response.setFilesToModify(files);

        List<String> review = new ArrayList<>();
        review.add("Review existing tests");
        response.setFilesToReview(review);

        List<String> components = new ArrayList<>();
        components.add("UserController (Controller)");
        response.setComponentsAffected(components);

        List<String> deps = new ArrayList<>();
        deps.add("Internal module dependencies");
        response.setDependenciesInvolved(deps);

        List<String> validation = new ArrayList<>();
        validation.add("Verify the changes compile");
        response.setSuggestedValidationSteps(validation);

        response.setSuggestedTestingScope("Medium scope");

        List<String> risks = new ArrayList<>();
        risks.add("Test risk [Mitigation: Test mitigation]");
        response.setRisks(risks);

        List<String> assumptions = new ArrayList<>();
        assumptions.add("Task analysis is based on keyword detection");
        response.setAssumptions(assumptions);

        return response;
    }
}