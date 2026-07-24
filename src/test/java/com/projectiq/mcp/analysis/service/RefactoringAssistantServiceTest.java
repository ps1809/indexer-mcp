package com.projectiq.mcp.analysis.service;

import com.projectiq.mcp.analysis.dto.ConfidenceLevel;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse.ImpactedComponent;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse.RiskItem;
import com.projectiq.mcp.analysis.dto.ImplementationPlanningResponse;
import com.projectiq.mcp.analysis.dto.RefactoringAssistantResponse;
import com.projectiq.mcp.analysis.dto.RiskLevel;
import com.projectiq.mcp.analysis.dto.TaskAnalysisResponse;
import com.projectiq.mcp.analysis.dto.TaskType;
import com.projectiq.mcp.analysis.dto.TestImpactAnalysisResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link RefactoringAssistantService}.
 */
@ExtendWith(MockitoExtension.class)
class RefactoringAssistantServiceTest {

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

    private RefactoringAssistantService service;

    @BeforeEach
    void setUp() {
        service = new RefactoringAssistantService(
                taskAnalysisService, contextAssemblyService, impactAnalysisService,
                implementationPlanningService, testImpactAnalysisService);
    }

    @Test
    void testAnalyzeRefactoring_RenameClass() {
        // Arrange
        String task = "Rename class UserService to UserManagementService in repository my-repo";
        String repositoryName = "my-repo";

        TaskAnalysisResponse taskAnalysis = new TaskAnalysisResponse();
        taskAnalysis.setTaskType(TaskType.REFACTORING);
        taskAnalysis.setConfidenceLevel(ConfidenceLevel.HIGH);
        taskAnalysis.setDetectedEntities(List.of("UserService (Service)"));

        when(taskAnalysisService.analyze(anyString())).thenReturn(taskAnalysis);
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(createEmptyImpactAnalysis());
        when(implementationPlanningService.generatePlan(anyString(), anyString(), anyString()))
                .thenReturn(createEmptyImplPlan());
        when(testImpactAnalysisService.analyzeTestImpact(anyString(), anyString(), anyString()))
                .thenReturn(createEmptyTestImpact());

        // Act
        RefactoringAssistantResponse response = service.analyzeRefactoring(task, repositoryName, "main");

        // Assert
        assertNotNull(response);
        assertEquals(task, response.getOriginalTask());
        assertEquals("Rename Class", response.getRefactoringType());
        assertFalse(response.getAffectedClasses().isEmpty());
        assertTrue(response.getAffectedClasses().stream().anyMatch(c -> c.contains("UserService")));
        assertFalse(response.getSuggestedExecutionOrder().isEmpty());
        assertFalse(response.getValidationChecklist().isEmpty());
        assertFalse(response.getRisks().isEmpty());
        assertNotNull(response.getConfidenceLevel());

        verify(taskAnalysisService).analyze(anyString());
        verify(contextAssemblyService).assembleContext(anyString(), anyString(), anyString());
        verify(impactAnalysisService).analyzeImpact(anyString(), anyString(), anyString());
        verify(implementationPlanningService).generatePlan(anyString(), anyString(), anyString());
        verify(testImpactAnalysisService).analyzeTestImpact(anyString(), anyString(), anyString());
    }

    @Test
    void testAnalyzeRefactoring_ExtractMethod() {
        // Arrange
        String task = "Extract method calculateTotal from processOrder in the repository";
        String repositoryName = "my-repo";

        TaskAnalysisResponse taskAnalysis = new TaskAnalysisResponse();
        taskAnalysis.setTaskType(TaskType.REFACTORING);
        taskAnalysis.setConfidenceLevel(ConfidenceLevel.HIGH);
        taskAnalysis.setDetectedEntities(List.of());

        when(taskAnalysisService.analyze(anyString())).thenReturn(taskAnalysis);
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(createEmptyImpactAnalysis());
        when(implementationPlanningService.generatePlan(anyString(), anyString(), anyString()))
                .thenReturn(createEmptyImplPlan());
        when(testImpactAnalysisService.analyzeTestImpact(anyString(), anyString(), anyString()))
                .thenReturn(createEmptyTestImpact());

        // Act
        RefactoringAssistantResponse response = service.analyzeRefactoring(task, repositoryName, "main");

        // Assert
        assertNotNull(response);
        assertEquals("Extract Method", response.getRefactoringType());
        assertFalse(response.getAffectedMethods().isEmpty());
        assertTrue(response.getAffectedMethods().stream().anyMatch(m -> m.contains("calculatetotal")));
        assertFalse(response.getSuggestedExecutionOrder().isEmpty());
        assertFalse(response.getValidationChecklist().isEmpty());
    }

    @Test
    void testAnalyzeRefactoring_MovePackage() {
        // Arrange
        String task = "Move package com.example.old to com.example.new";
        String repositoryName = "my-repo";

        TaskAnalysisResponse taskAnalysis = new TaskAnalysisResponse();
        taskAnalysis.setTaskType(TaskType.REFACTORING);
        taskAnalysis.setConfidenceLevel(ConfidenceLevel.MEDIUM);
        taskAnalysis.setDetectedEntities(List.of());

        when(taskAnalysisService.analyze(anyString())).thenReturn(taskAnalysis);
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(createEmptyImpactAnalysis());
        when(implementationPlanningService.generatePlan(anyString(), anyString(), anyString()))
                .thenReturn(createEmptyImplPlan());
        when(testImpactAnalysisService.analyzeTestImpact(anyString(), anyString(), anyString()))
                .thenReturn(createEmptyTestImpact());

        // Act
        RefactoringAssistantResponse response = service.analyzeRefactoring(task, repositoryName, "main");

        // Assert
        assertNotNull(response);
        assertEquals("Move Package", response.getRefactoringType());
        assertFalse(response.getAffectedPackages().isEmpty());
        assertTrue(response.getAffectedPackages().stream().anyMatch(p -> p.contains("com.example.old")));
        assertTrue(response.getAffectedPackages().stream().anyMatch(p -> p.contains("com.example.new")));
    }

    @Test
    void testAnalyzeRefactoring_SplitLargeClass() {
        // Arrange
        String task = "Split large class OrderProcessor into smaller classes";
        String repositoryName = "my-repo";

        TaskAnalysisResponse taskAnalysis = new TaskAnalysisResponse();
        taskAnalysis.setTaskType(TaskType.REFACTORING);
        taskAnalysis.setConfidenceLevel(ConfidenceLevel.MEDIUM);
        taskAnalysis.setDetectedEntities(List.of());

        when(taskAnalysisService.analyze(anyString())).thenReturn(taskAnalysis);
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(createEmptyImpactAnalysis());
        when(implementationPlanningService.generatePlan(anyString(), anyString(), anyString()))
                .thenReturn(createEmptyImplPlan());
        when(testImpactAnalysisService.analyzeTestImpact(anyString(), anyString(), anyString()))
                .thenReturn(createEmptyTestImpact());

        // Act
        RefactoringAssistantResponse response = service.analyzeRefactoring(task, repositoryName, "main");

        // Assert
        assertNotNull(response);
        assertEquals("Split Large Class", response.getRefactoringType());
        assertFalse(response.getAffectedClasses().isEmpty());
        assertTrue(response.getAffectedClasses().stream().anyMatch(c -> c.contains("OrderProcessor")));
    }

    @Test
    void testAnalyzeRefactoring_DeleteDeadCode() {
        // Arrange
        String task = "Delete dead code in the repository - remove unused method calculateTax";
        String repositoryName = "my-repo";

        TaskAnalysisResponse taskAnalysis = new TaskAnalysisResponse();
        taskAnalysis.setTaskType(TaskType.REFACTORING);
        taskAnalysis.setConfidenceLevel(ConfidenceLevel.MEDIUM);
        taskAnalysis.setDetectedEntities(List.of());

        when(taskAnalysisService.analyze(anyString())).thenReturn(taskAnalysis);
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(createEmptyImpactAnalysis());
        when(implementationPlanningService.generatePlan(anyString(), anyString(), anyString()))
                .thenReturn(createEmptyImplPlan());
        when(testImpactAnalysisService.analyzeTestImpact(anyString(), anyString(), anyString()))
                .thenReturn(createEmptyTestImpact());

        // Act
        RefactoringAssistantResponse response = service.analyzeRefactoring(task, repositoryName, "main");

        // Assert
        assertNotNull(response);
        assertEquals("Delete Dead Code", response.getRefactoringType());
        assertFalse(response.getSuggestedExecutionOrder().isEmpty());
        assertFalse(response.getValidationChecklist().isEmpty());
    }

    @Test
    void testAnalyzeRefactoring_EmptyRequest() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                service.analyzeRefactoring("", "my-repo", "main"));
        assertThrows(IllegalArgumentException.class, () ->
                service.analyzeRefactoring(null, "my-repo", "main"));
    }

    @Test
    void testAnalyzeRefactoring_InvalidRequest() {
        // Arrange
        TaskAnalysisResponse taskAnalysis = new TaskAnalysisResponse();
        taskAnalysis.setTaskType(TaskType.UNKNOWN);
        taskAnalysis.setConfidenceLevel(ConfidenceLevel.LOW);
        taskAnalysis.setDetectedEntities(List.of());

        when(taskAnalysisService.analyze(anyString())).thenReturn(taskAnalysis);
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(createEmptyImpactAnalysis());
        when(implementationPlanningService.generatePlan(anyString(), anyString(), anyString()))
                .thenReturn(createEmptyImplPlan());
        when(testImpactAnalysisService.analyzeTestImpact(anyString(), anyString(), anyString()))
                .thenReturn(createEmptyTestImpact());

        // Act
        RefactoringAssistantResponse response = service.analyzeRefactoring(
                "Just some random text", "my-repo", "main");

        // Assert
        assertNotNull(response);
        assertEquals("General Refactoring", response.getRefactoringType());
        assertEquals("LOW", response.getConfidenceLevel());
    }

    @Test
    void testDetectRefactoringType_RenameClass() {
        assertEquals("Rename Class", service.detectRefactoringType("rename class UserController to UserApiController"));
        assertEquals("Rename Class", service.detectRefactoringType("rename type MyType to NewType"));
        assertEquals("Rename Class", service.detectRefactoringType("rename interface DataService to DataRepository"));
    }

    @Test
    void testDetectRefactoringType_RenameMethod() {
        assertEquals("Rename Method", service.detectRefactoringType("rename method calculateTotal to computeTotal"));
        assertEquals("Rename Method", service.detectRefactoringType("rename function getData to fetchData"));
    }

    @Test
    void testDetectRefactoringType_MoveClass() {
        assertEquals("Move Class", service.detectRefactoringType("move class UserService to com.example.service"));
    }

    @Test
    void testDetectRefactoringType_MovePackage() {
        assertEquals("Move Package", service.detectRefactoringType("move package com.example.old to com.example.new"));
    }

    @Test
    void testDetectRefactoringType_ExtractMethod() {
        assertEquals("Extract Method", service.detectRefactoringType("extract method validateInput from processOrder"));
        assertEquals("Extract Method", service.detectRefactoringType("extract function parseData from readFile"));
    }

    @Test
    void testDetectRefactoringType_ExtractClass() {
        assertEquals("Extract Class", service.detectRefactoringType("extract class PaymentProcessor from OrderService"));
    }

    @Test
    void testDetectRefactoringType_InlineMethod() {
        assertEquals("Inline Method", service.detectRefactoringType("inline method getHelper"));
    }

    @Test
    void testDetectRefactoringType_DeleteDeadCode() {
        assertEquals("Delete Dead Code", service.detectRefactoringType("delete dead code in the module"));
        assertEquals("Delete Dead Code", service.detectRefactoringType("delete unused class ObsoleteHelper"));
        assertEquals("Delete Dead Code", service.detectRefactoringType("delete redundant method calculateOld"));
    }

    @Test
    void testDetectRefactoringType_SplitLargeClass() {
        assertEquals("Split Large Class", service.detectRefactoringType("split large class OrderProcessor into smaller pieces"));
        assertEquals("Split Large Class", service.detectRefactoringType("split big type DataManager into separate classes"));
    }

    @Test
    void testDetectRefactoringType_GeneralRefactoring() {
        assertEquals("General Refactoring", service.detectRefactoringType("refactor the codebase to improve performance"));
        assertEquals("General Refactoring", service.detectRefactoringType("restructure the module layout"));
        assertEquals("General Refactoring", service.detectRefactoringType("clean up the code"));
    }

    @Test
    void testBuildDependencies_RenameClass() {
        List<String> deps = service.buildDependencies("Rename Class", null, "");
        assertTrue(deps.contains("All import statements referencing the renamed class"));
        assertTrue(deps.contains("Configuration files referencing the class (e.g., Spring beans, XML config)"));
        assertTrue(deps.contains("Reflection-based references (e.g., Class.forName, Spring bean names)"));
    }

    @Test
    void testBuildDependencies_ExtractClass() {
        List<String> deps = service.buildDependencies("Extract Class", null, "");
        assertTrue(deps.contains("All references from the original class to the extracted class"));
        assertTrue(deps.contains("Dependency injection wiring for the new class"));
    }

    @Test
    void testDetermineConfidence_High() {
        // Setup task analysis with HIGH confidence
        TaskAnalysisResponse taskAnalysis = new TaskAnalysisResponse();
        taskAnalysis.setConfidenceLevel(ConfidenceLevel.HIGH);

        // Setup impact analysis with directly affected components
        ImpactAnalysisResponse impactAnalysis = createImpactAnalysisWithData();

        String confidence = service.determineConfidence(
                "Rename Class", "rename class UserService to UserAdminService",
                taskAnalysis, impactAnalysis);
        assertEquals("High", confidence);
    }

    @Test
    void testDetermineConfidence_Low() {
        TaskAnalysisResponse taskAnalysis = new TaskAnalysisResponse();
        taskAnalysis.setConfidenceLevel(ConfidenceLevel.LOW);

        String confidence = service.determineConfidence(
                "General Refactoring", "some unclear text",
                taskAnalysis, null);
        assertEquals("LOW", confidence);
    }

    @Test
    void testPartialFailure_ContextAssemblyFails() {
        // Arrange
        String task = "Refactor the codebase";
        String repositoryName = "my-repo";

        TaskAnalysisResponse taskAnalysis = new TaskAnalysisResponse();
        taskAnalysis.setTaskType(TaskType.REFACTORING);
        taskAnalysis.setConfidenceLevel(ConfidenceLevel.MEDIUM);
        taskAnalysis.setDetectedEntities(List.of());

        when(taskAnalysisService.analyze(anyString())).thenReturn(taskAnalysis);
        when(contextAssemblyService.assembleContext(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Indexer unavailable"));
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(createEmptyImpactAnalysis());
        when(implementationPlanningService.generatePlan(anyString(), anyString(), anyString()))
                .thenReturn(createEmptyImplPlan());
        when(testImpactAnalysisService.analyzeTestImpact(anyString(), anyString(), anyString()))
                .thenReturn(createEmptyTestImpact());

        // Act - should not throw despite context assembly failure
        RefactoringAssistantResponse response = service.analyzeRefactoring(task, repositoryName, "main");

        // Assert
        assertNotNull(response);
        assertEquals("General Refactoring", response.getRefactoringType());
        assertNotNull(response.getValidationChecklist());
        assertNotNull(response.getRisks());
        assertNotNull(response.getSuggestedExecutionOrder());
    }

    @Test
    void testDeterministicOutput() {
        // Arrange
        String task = "Rename class UserService to UserAdminService";
        String repositoryName = "my-repo";

        TaskAnalysisResponse taskAnalysis = new TaskAnalysisResponse();
        taskAnalysis.setTaskType(TaskType.REFACTORING);
        taskAnalysis.setConfidenceLevel(ConfidenceLevel.HIGH);
        taskAnalysis.setDetectedEntities(List.of("UserService (Service)"));

        when(taskAnalysisService.analyze(anyString())).thenReturn(taskAnalysis);
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(createEmptyImpactAnalysis());
        when(implementationPlanningService.generatePlan(anyString(), anyString(), anyString()))
                .thenReturn(createEmptyImplPlan());
        when(testImpactAnalysisService.analyzeTestImpact(anyString(), anyString(), anyString()))
                .thenReturn(createEmptyTestImpact());

        // Act - run twice
        RefactoringAssistantResponse firstRun = service.analyzeRefactoring(task, repositoryName, "main");
        RefactoringAssistantResponse secondRun = service.analyzeRefactoring(task, repositoryName, "main");

        // Assert - deterministic output
        assertEquals(firstRun.getRefactoringType(), secondRun.getRefactoringType());
        assertEquals(firstRun.getAffectedClasses(), secondRun.getAffectedClasses());
        assertEquals(firstRun.getAffectedMethods(), secondRun.getAffectedMethods());
        assertEquals(firstRun.getAffectedPackages(), secondRun.getAffectedPackages());
        assertEquals(firstRun.getSuggestedExecutionOrder(), secondRun.getSuggestedExecutionOrder());
        assertEquals(firstRun.getValidationChecklist(), secondRun.getValidationChecklist());
        assertEquals(firstRun.getConfidenceLevel(), secondRun.getConfidenceLevel());
    }

    @Test
    void testNoDuplicateEntries() {
        // Arrange
        String task = "Rename class UserService to UserService";
        String repositoryName = "my-repo";

        TaskAnalysisResponse taskAnalysis = new TaskAnalysisResponse();
        taskAnalysis.setTaskType(TaskType.REFACTORING);
        taskAnalysis.setConfidenceLevel(ConfidenceLevel.HIGH);
        taskAnalysis.setDetectedEntities(List.of("UserService (Service)"));

        when(taskAnalysisService.analyze(anyString())).thenReturn(taskAnalysis);
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(createEmptyImpactAnalysis());
        when(implementationPlanningService.generatePlan(anyString(), anyString(), anyString()))
                .thenReturn(createEmptyImplPlan());
        when(testImpactAnalysisService.analyzeTestImpact(anyString(), anyString(), anyString()))
                .thenReturn(createEmptyTestImpact());

        // Act
        RefactoringAssistantResponse response = service.analyzeRefactoring(task, repositoryName, "main");

        // Assert - no duplicates in any list
        assertNoDuplicates(response.getAffectedClasses());
        assertNoDuplicates(response.getAffectedMethods());
        assertNoDuplicates(response.getAffectedPackages());
        assertNoDuplicates(response.getDependenciesInvolved());
        assertNoDuplicates(response.getSuggestedExecutionOrder());
        assertNoDuplicates(response.getValidationChecklist());
        assertNoDuplicates(response.getRecommendedTests());
        assertNoDuplicates(response.getRisks());
    }

    // --- Private helpers ---

    private void assertNoDuplicates(List<String> list) {
        if (list != null) {
            assertEquals(list.stream().distinct().count(), list.size(),
                    "List contains duplicates: " + list);
        }
    }

    private ImpactAnalysisResponse createEmptyImpactAnalysis() {
        ImpactAnalysisResponse response = new ImpactAnalysisResponse();
        response.setOriginalTask("test");
        response.setTaskType("REFACTORING");
        response.setPrimaryTargets(List.of());
        response.setDirectlyAffectedComponents(List.of());
        response.setIndirectlyAffectedComponents(List.of());
        response.setDependencyImpact(List.of());
        response.setPotentialRisks(List.of());
        return response;
    }

    private ImpactAnalysisResponse createImpactAnalysisWithData() {
        ImpactAnalysisResponse response = new ImpactAnalysisResponse();
        response.setOriginalTask("test");
        response.setTaskType("REFACTORING");
        response.setPrimaryTargets(List.of("UserService"));
        response.setDirectlyAffectedComponents(List.of(
                new ImpactedComponent("UserService", "Class", "Direct target of rename")));
        response.setIndirectlyAffectedComponents(List.of());
        response.setDependencyImpact(List.of("Internal module dependencies may need coordination"));
        response.setPotentialRisks(List.of(
                new RiskItem("Refactoring risk", RiskLevel.MEDIUM, "Validate thoroughly")));
        return response;
    }

    private ImplementationPlanningResponse createEmptyImplPlan() {
        return new ImplementationPlanningResponse();
    }

    private TestImpactAnalysisResponse createEmptyTestImpact() {
        TestImpactAnalysisResponse response = new TestImpactAnalysisResponse();
        response.setOriginalTask("test");
        response.setAffectedProductionClasses(List.of());
        response.setRelatedTestClasses(List.of());
        response.setMissingTests(List.of());
        response.setRecommendedTestExecutionOrder(List.of());
        response.setEstimatedTestingEffort("Medium");
        response.setConfidenceLevel("Medium");
        response.setTestingRationale("Test rationale");
        return response;
    }
}