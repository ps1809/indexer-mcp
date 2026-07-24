package com.projectiq.mcp.orchestration.service;

import com.projectiq.mcp.analysis.dto.*;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse.ImpactedComponent;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse.RiskItem;
import com.projectiq.mcp.analysis.dto.TaskAnalysisResponse.ExecutionStep;
import com.projectiq.mcp.analysis.service.*;
import com.projectiq.mcp.orchestration.dto.WorkflowDefinition;
import com.projectiq.mcp.orchestration.dto.WorkflowResult;
import com.projectiq.mcp.orchestration.dto.WorkflowType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowOrchestratorServiceTest {

    @Mock
    private TaskAnalysisService taskAnalysisService;
    @Mock
    private ContextAssemblyService contextAssemblyService;
    @Mock
    private ImpactAnalysisService impactAnalysisService;
    @Mock
    private ImplementationPlanningService implementationPlanningService;
    @Mock
    private TestImpactAnalysisService testImpactAnalysisService;
    @Mock
    private RefactoringAssistantService refactoringAssistantService;
    @Mock
    private ArchitectureInsightsService architectureInsightsService;
    @Mock
    private RepositoryConventionAnalyzerService repositoryConventionAnalyzerService;
    @Mock
    private RepositoryHealthService repositoryHealthService;

    private WorkflowOrchestratorService orchestratorService;

    @BeforeEach
    void setUp() {
        orchestratorService = new WorkflowOrchestratorService(
                taskAnalysisService,
                contextAssemblyService,
                impactAnalysisService,
                implementationPlanningService,
                testImpactAnalysisService,
                refactoringAssistantService,
                architectureInsightsService,
                repositoryConventionAnalyzerService,
                repositoryHealthService
        );
    }

    @Test
    void testOrchestrateFeatureImplementationWorkflow() {
        // Arrange
        String request = "Add pagination to UserController";
        String repositoryName = "test-repo";
        String branch = "main";

        TaskAnalysisResponse taskAnalysis = createTaskAnalysis(TaskType.NEW_FEATURE);
        when(taskAnalysisService.analyze(request)).thenReturn(taskAnalysis);
        when(contextAssemblyService.assembleContext(anyString(), anyString(), anyString()))
                .thenReturn(createContextAssemblyResponse());
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(createImpactAnalysisResponse());
        when(implementationPlanningService.generatePlan(anyString(), anyString(), anyString()))
                .thenReturn(createImplementationPlanningResponse());
        when(testImpactAnalysisService.analyzeTestImpact(anyString(), anyString(), anyString()))
                .thenReturn(createTestImpactAnalysisResponse());
        when(architectureInsightsService.analyzeArchitecture(anyString(), anyString()))
                .thenReturn(createArchitectureInsightsResponse());
        when(repositoryConventionAnalyzerService.analyzeConventions(anyString(), anyString()))
                .thenReturn(createRepositoryConventionResponse());

        // Act
        WorkflowResult result = orchestratorService.orchestrate(request, repositoryName, branch);

        // Assert
        assertNotNull(result);
        assertEquals(request, result.getOriginalRequest());
        assertEquals(WorkflowType.FEATURE_IMPLEMENTATION.getDisplayName(), result.getWorkflowType());
        assertFalse(result.getExecutionPlan().isEmpty());
        assertFalse(result.getCompletedSteps().isEmpty());
        assertNotNull(result.getSummary());
        assertTrue(result.getTotalDurationMillis() >= 0);
        assertFalse(result.getSuggestedNextActions().isEmpty());
    }

    @Test
    void testOrchestrateBugFixWorkflow() {
        // Arrange
        String request = "Fix null pointer exception in UserService";
        String repositoryName = "test-repo";

        TaskAnalysisResponse taskAnalysis = createTaskAnalysis(TaskType.BUG_FIX);
        when(taskAnalysisService.analyze(request)).thenReturn(taskAnalysis);
        when(contextAssemblyService.assembleContext(anyString(), anyString(), anyString()))
                .thenReturn(createContextAssemblyResponse());
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(createImpactAnalysisResponse());
        when(implementationPlanningService.generatePlan(anyString(), anyString(), anyString()))
                .thenReturn(createImplementationPlanningResponse());
        when(testImpactAnalysisService.analyzeTestImpact(anyString(), anyString(), anyString()))
                .thenReturn(createTestImpactAnalysisResponse());

        // Act
        WorkflowResult result = orchestratorService.orchestrate(request, repositoryName, null);

        // Assert
        assertNotNull(result);
        assertEquals(request, result.getOriginalRequest());
        assertEquals(WorkflowType.BUG_FIX.getDisplayName(), result.getWorkflowType());
        assertFalse(result.getCompletedSteps().isEmpty());
        assertTrue(result.getCompletedSteps().size() >= 4);
    }

    @Test
    void testOrchestrateRefactoringWorkflow() {
        // Arrange
        String request = "Refactor UserService to improve maintainability";
        String repositoryName = "test-repo";

        TaskAnalysisResponse taskAnalysis = createTaskAnalysis(TaskType.REFACTORING);
        when(taskAnalysisService.analyze(request)).thenReturn(taskAnalysis);
        when(contextAssemblyService.assembleContext(anyString(), anyString(), anyString()))
                .thenReturn(createContextAssemblyResponse());
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(createImpactAnalysisResponse());
        when(refactoringAssistantService.analyzeRefactoring(anyString(), anyString(), anyString()))
                .thenReturn(createRefactoringAssistantResponse());
        when(implementationPlanningService.generatePlan(anyString(), anyString(), anyString()))
                .thenReturn(createImplementationPlanningResponse());
        when(testImpactAnalysisService.analyzeTestImpact(anyString(), anyString(), anyString()))
                .thenReturn(createTestImpactAnalysisResponse());

        // Act
        WorkflowResult result = orchestratorService.orchestrate(request, repositoryName, "develop");

        // Assert
        assertNotNull(result);
        assertEquals(WorkflowType.REFACTORING.getDisplayName(), result.getWorkflowType());
        assertFalse(result.getCompletedSteps().isEmpty());
    }

    @Test
    void testOrchestrateConfigurationChangeWorkflow() {
        // Arrange
        String request = "Update database configuration for production";
        String repositoryName = "test-repo";

        TaskAnalysisResponse taskAnalysis = createTaskAnalysis(TaskType.CONFIGURATION_CHANGE);
        when(taskAnalysisService.analyze(request)).thenReturn(taskAnalysis);
        when(contextAssemblyService.assembleContext(anyString(), anyString(), anyString()))
                .thenReturn(createContextAssemblyResponse());
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(createImpactAnalysisResponse());
        when(implementationPlanningService.generatePlan(anyString(), anyString(), anyString()))
                .thenReturn(createImplementationPlanningResponse());
        when(testImpactAnalysisService.analyzeTestImpact(anyString(), anyString(), anyString()))
                .thenReturn(createTestImpactAnalysisResponse());

        // Act
        WorkflowResult result = orchestratorService.orchestrate(request, repositoryName, "main");

        // Assert
        assertNotNull(result);
        assertEquals(WorkflowType.CONFIGURATION_CHANGE.getDisplayName(), result.getWorkflowType());
        assertFalse(result.getCompletedSteps().isEmpty());
    }

    @Test
    void testOrchestrateDocumentationUpdateWorkflow() {
        // Arrange
        String request = "Update README with new API documentation";
        String repositoryName = "test-repo";

        TaskAnalysisResponse taskAnalysis = createTaskAnalysis(TaskType.DOCUMENTATION);
        when(taskAnalysisService.analyze(request)).thenReturn(taskAnalysis);
        when(contextAssemblyService.assembleContext(anyString(), anyString(), anyString()))
                .thenReturn(createContextAssemblyResponse());
        when(implementationPlanningService.generatePlan(anyString(), anyString(), anyString()))
                .thenReturn(createImplementationPlanningResponse());
        when(repositoryConventionAnalyzerService.analyzeConventions(anyString(), anyString()))
                .thenReturn(createRepositoryConventionResponse());

        // Act
        WorkflowResult result = orchestratorService.orchestrate(request, repositoryName, "main");

        // Assert
        assertNotNull(result);
        assertEquals(WorkflowType.DOCUMENTATION_UPDATE.getDisplayName(), result.getWorkflowType());
        assertFalse(result.getCompletedSteps().isEmpty());
    }

    @Test
    void testOrchestrateWithPartialFailures() {
        // Arrange
        String request = "Add pagination to UserController";
        String repositoryName = "test-repo";

        TaskAnalysisResponse taskAnalysis = createTaskAnalysis(TaskType.NEW_FEATURE);
        when(taskAnalysisService.analyze(request)).thenReturn(taskAnalysis);
        when(contextAssemblyService.assembleContext(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Indexer connection failed"));
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(createImpactAnalysisResponse());
        when(implementationPlanningService.generatePlan(anyString(), anyString(), anyString()))
                .thenReturn(createImplementationPlanningResponse());
        when(testImpactAnalysisService.analyzeTestImpact(anyString(), anyString(), anyString()))
                .thenReturn(createTestImpactAnalysisResponse());
        when(architectureInsightsService.analyzeArchitecture(anyString(), anyString()))
                .thenReturn(createArchitectureInsightsResponse());
        when(repositoryConventionAnalyzerService.analyzeConventions(anyString(), anyString()))
                .thenReturn(createRepositoryConventionResponse());

        // Act
        WorkflowResult result = orchestratorService.orchestrate(request, repositoryName, "main");

        // Assert
        assertNotNull(result);
        assertFalse(result.getCompletedSteps().isEmpty());
        assertTrue(result.getFailedSteps().size() >= 1);
        assertNotNull(result.getExecutionStatus());
    }

    @Test
    void testOrchestrateWithEmptyRequest() {
        // Arrange
        String request = "";
        String repositoryName = "test-repo";

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> orchestratorService.orchestrate(request, repositoryName, "main"));
    }

    @Test
    void testOrchestrateWithNullRequest() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> orchestratorService.orchestrate(null, "test-repo", "main"));
    }

    @Test
    void testBuildWorkflowDefinition() {
        // Arrange
        String request = "Add pagination to UserController";
        TaskAnalysisResponse taskAnalysis = createTaskAnalysis(TaskType.NEW_FEATURE);

        // Act
        WorkflowDefinition definition = orchestratorService.buildWorkflowDefinition(
                request, WorkflowType.FEATURE_IMPLEMENTATION, taskAnalysis);

        // Assert
        assertNotNull(definition);
        assertEquals(WorkflowType.FEATURE_IMPLEMENTATION, definition.getWorkflowType());
        assertEquals(request, definition.getOriginalRequest());
        assertFalse(definition.getSteps().isEmpty());
        assertNotNull(definition.getReasoning());
        assertTrue(definition.getReasoning().contains("Feature Implementation"));
    }

    @Test
    void testOrchestrateRepositoryAnalysisWorkflow() {
        // Arrange
        String request = "Analyze the repository structure";
        String repositoryName = "test-repo";

        TaskAnalysisResponse taskAnalysis = createTaskAnalysis(TaskType.UNKNOWN);
        when(taskAnalysisService.analyze(request)).thenReturn(taskAnalysis);
        when(contextAssemblyService.assembleContext(anyString(), anyString(), anyString()))
                .thenReturn(createContextAssemblyResponse());
        when(architectureInsightsService.analyzeArchitecture(anyString(), anyString()))
                .thenReturn(createArchitectureInsightsResponse());
        when(repositoryConventionAnalyzerService.analyzeConventions(anyString(), anyString()))
                .thenReturn(createRepositoryConventionResponse());
        when(repositoryHealthService.analyzeHealth(anyString(), anyString()))
                .thenReturn(createRepositoryHealthResponse());
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(createImpactAnalysisResponse());

        // Act
        WorkflowResult result = orchestratorService.orchestrate(request, repositoryName, "main");

        // Assert
        assertNotNull(result);
        assertEquals(WorkflowType.REPOSITORY_ANALYSIS.getDisplayName(), result.getWorkflowType());
        assertFalse(result.getCompletedSteps().isEmpty());
        assertFalse(result.getRepositoryInsights().isEmpty());
    }

    // --- Helper methods ---

    private TaskAnalysisResponse createTaskAnalysis(TaskType taskType) {
        TaskAnalysisResponse response = new TaskAnalysisResponse();
        response.setOriginalTask("Test task");
        response.setTaskType(taskType);
        response.setConfidenceLevel(ConfidenceLevel.HIGH);
        response.setEstimatedComplexity(ComplexityLevel.MEDIUM);
        response.setDetectedEntities(List.of("UserController (Controller)", "UserService (Service)"));
        response.setSuggestedTools(List.of("repository_summary", "search_code", "find_class"));
        response.setExecutionPlan(List.of(
                new ExecutionStep(1, "repository_summary", "Get repository overview"),
                new ExecutionStep(2, "search_code", "Search for relevant code")
        ));
        response.setReasoningSummary("Test reasoning");
        return response;
    }

    private ContextAssemblyResponse createContextAssemblyResponse() {
        ContextAssemblyResponse response = new ContextAssemblyResponse();
        response.setOriginalTask("Test task");
        response.setExecutedTools(List.of("repository_summary", "search_code"));
        response.setTotalExecutionTimeMillis(100);
        response.setExecutionSummary("Context assembled");
        return response;
    }

    private ImpactAnalysisResponse createImpactAnalysisResponse() {
        ImpactAnalysisResponse response = new ImpactAnalysisResponse();
        response.setOriginalTask("Test task");
        response.setTaskType("New Feature");
        response.setPrimaryTargets(List.of("UserController"));
        response.setDirectlyAffectedComponents(List.of(
                new ImpactedComponent("UserController", "Class", "Directly referenced")
        ));
        response.setIndirectlyAffectedComponents(List.of(
                new ImpactedComponent("UserService", "Class", "Associated service")
        ));
        response.setEstimatedImplementationScope(ScopeLevel.MEDIUM);
        response.setEstimatedTestingScope(ScopeLevel.MEDIUM);
        response.setPotentialRisks(List.of(
                new RiskItem("Test risk", RiskLevel.LOW, "Test mitigation")
        ));
        response.setConfidenceLevel(ConfidenceLevel.HIGH);
        return response;
    }

    private ImplementationPlanningResponse createImplementationPlanningResponse() {
        ImplementationPlanningResponse response = new ImplementationPlanningResponse();
        response.setOriginalTask("Test task");
        response.setTaskType("New Feature");
        response.setEstimatedComplexity("MEDIUM");
        response.setRecommendedImplementationOrder(List.of(
                "1. Analyze repository structure",
                "2. Implement the feature"
        ));
        return response;
    }

    private TestImpactAnalysisResponse createTestImpactAnalysisResponse() {
        TestImpactAnalysisResponse response = new TestImpactAnalysisResponse();
        response.setOriginalTask("Test task");
        response.setAffectedProductionClasses(List.of("UserController"));
        response.setRelatedTestClasses(List.of("UserControllerTest"));
        response.setEstimatedTestingEffort("Medium");
        response.setConfidenceLevel("High");
        return response;
    }

    private RefactoringAssistantResponse createRefactoringAssistantResponse() {
        RefactoringAssistantResponse response = new RefactoringAssistantResponse();
        response.setOriginalTask("Test refactoring");
        response.setRefactoringType("General Refactoring");
        response.setAffectedClasses(List.of("UserService"));
        return response;
    }

    private ArchitectureInsightsResponse createArchitectureInsightsResponse() {
        ArchitectureInsightsResponse response = new ArchitectureInsightsResponse();
        response.setRepositoryName("test-repo");
        response.setArchitecturalStyle("Layered Architecture");
        response.setDetectedLayers(List.of("Controller (Presentation)", "Service (Business Logic)"));
        response.setConfidenceLevel("HIGH");
        return response;
    }

    private RepositoryConventionResponse createRepositoryConventionResponse() {
        RepositoryConventionResponse response = new RepositoryConventionResponse();
        response.setRepositoryName("test-repo");
        RepositoryConventionResponse.NamingConventions naming = new RepositoryConventionResponse.NamingConventions();
        naming.setClassNamingConvention("PascalCase");
        response.setNamingConventions(naming);
        response.setProjectSpecificObservations(List.of("Standard layered architecture detected"));
        response.setConfidenceLevel("HIGH");
        return response;
    }

    private RepositoryHealthResponse createRepositoryHealthResponse() {
        RepositoryHealthResponse response = new RepositoryHealthResponse();
        response.setRepositoryName("test-repo");
        response.setHealthScore(75);
        response.setMaintainabilityRating("Good");
        response.setPotentialRisks(List.of("No test coverage detected"));
        return response;
    }
}