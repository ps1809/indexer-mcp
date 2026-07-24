package com.projectiq.mcp.analysis.service;

import com.projectiq.mcp.analysis.dto.RefactoringImpactSimulationResponse;
import com.projectiq.mcp.pipeline.service.IntelligentContextPipelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link RefactoringImpactSimulationService}.
 */
@ExtendWith(MockitoExtension.class)
class RefactoringImpactSimulationServiceTest {

    @Mock
    private CodeChangeAnalysisService codeChangeAnalysisService;

    @Mock
    private DependencyChangePredictionService dependencyChangePredictionService;

    @Mock
    private RefactoringAssistantService refactoringAssistantService;

    @Mock
    private ImpactAnalysisService impactAnalysisService;

    @Mock
    private ArchitectureInsightsService architectureInsightsService;

    @Mock
    private IntelligentContextPipelineService intelligentContextPipelineService;

    private RefactoringImpactSimulationService service;

    @BeforeEach
    void setUp() {
        service = new RefactoringImpactSimulationService(
                codeChangeAnalysisService,
                dependencyChangePredictionService,
                refactoringAssistantService,
                impactAnalysisService,
                architectureInsightsService,
                intelligentContextPipelineService);
    }

    @Test
    void testSimulateRenameClass() {
        RefactoringImpactSimulationResponse response = service.simulateRefactoring(
                "Rename Class", "UserService", "UserManagementService",
                "my-repo", "main");

        assertNotNull(response);
        assertEquals("Rename Class", response.getRefactoringType());
        assertEquals("UserService", response.getTargetEntity());
        assertEquals("UserManagementService", response.getSourceContext());
        assertNotNull(response.getRefactoringSummary());
        assertTrue(response.getRefactoringSummary().contains("Rename Class"));
        assertTrue(response.getRefactoringSummary().contains("UserService"));

        // Verify impacted files
        assertNotNull(response.getImpactedFiles());
        assertTrue(response.getImpactedFiles().contains("UserService.java"));
        assertTrue(response.getImpactedFiles().contains("UserServiceTest.java"));

        // Verify impacted classes
        assertNotNull(response.getImpactedClasses());
        assertTrue(response.getImpactedClasses().contains("UserService (to be renamed)"));
        assertTrue(response.getImpactedClasses().contains("UserManagementService (new name)"));

        // Verify broken references
        assertNotNull(response.getBrokenReferences());
        assertTrue(response.getBrokenReferences().stream()
                .anyMatch(ref -> ref.contains("Import statements referencing UserService")));

        // Verify dependency changes
        assertNotNull(response.getDependencyChanges());

        // Verify architectural effects
        assertNotNull(response.getArchitecturalEffects());

        // Verify testing impact
        assertNotNull(response.getTestingImpact());

        // Verify risk assessment
        assertNotNull(response.getRiskAssessment());

        // Verify implementation sequence
        assertNotNull(response.getSuggestedImplementationSequence());
        assertFalse(response.getSuggestedImplementationSequence().isEmpty());

        // Verify estimated effort
        assertNotNull(response.getEstimatedEffort());
    }

    @Test
    void testSimulateMoveClass() {
        RefactoringImpactSimulationResponse response = service.simulateRefactoring(
                "Move Class", "OrderService", "com.projectiq.newpackage",
                "my-repo", "main");

        assertNotNull(response);
        assertEquals("Move Class", response.getRefactoringType());
        assertEquals("OrderService", response.getTargetEntity());
        assertEquals("com.projectiq.newpackage", response.getSourceContext());
    }

    @Test
    void testSimulateExtractInterface() {
        RefactoringImpactSimulationResponse response = service.simulateRefactoring(
                "Extract Interface", "PaymentProcessor", "",
                "my-repo", "main");

        assertNotNull(response);
        assertEquals("Extract Interface", response.getRefactoringType());
        assertEquals("PaymentProcessor", response.getTargetEntity());

        // Verify impacted classes include interface and implementation
        assertTrue(response.getImpactedClasses().contains("PaymentProcessor (interface)"));
        assertTrue(response.getImpactedClasses().contains("PaymentProcessorImpl (implementation)"));
    }

    @Test
    void testSimulateSplitClass() {
        RefactoringImpactSimulationResponse response = service.simulateRefactoring(
                "Split Class", "LargeService", "",
                "my-repo", "develop");

        assertNotNull(response);
        assertEquals("Split Class", response.getRefactoringType());
        assertTrue(response.getImpactedClasses().contains("LargeService (to be split)"));
        assertTrue(response.getImpactedClasses().contains("LargeServicePart1 (extracted)"));
        assertTrue(response.getImpactedClasses().contains("LargeServicePart2 (extracted)"));
    }

    @Test
    void testInvalidRefactoringType() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.simulateRefactoring("Invalid Type", "SomeClass", "",
                        "my-repo", "main"));
        assertTrue(exception.getMessage().contains("Unsupported refactoring type"));
    }

    @Test
    void testNullRefactoringType() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.simulateRefactoring(null, "SomeClass", "",
                        "my-repo", "main"));
        assertTrue(exception.getMessage().contains("Refactoring type is required"));
    }

    @Test
    void testNullTargetEntity() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.simulateRefactoring("Rename Class", null, "",
                        "my-repo", "main"));
        assertTrue(exception.getMessage().contains("Target entity is required"));
    }

    @Test
    void testEmptyRequest() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.simulateRefactoring("", "", "",
                        "my-repo", "main"));
        assertTrue(exception.getMessage().contains("Refactoring type is required"));
    }

    @Test
    void testBuildRefactoringSummary() {
        String summary = service.buildRefactoringSummary("Rename Class", "UserService", "NewName");
        assertTrue(summary.contains("Rename Class"));
        assertTrue(summary.contains("UserService"));
        assertTrue(summary.contains("NewName"));
        assertTrue(summary.contains("without modifying any code"));
    }

    @Test
    void testEstimateEffortLow() {
        String effort = service.estimateEffort("Rename Class", List.of("ClassA"), List.of("fileA.java"));
        assertEquals("Low", effort);
    }

    @Test
    void testEstimateEffortHigh() {
        String effort = service.estimateEffort("Split Class",
                List.of("Class1", "Class2", "Class3", "Class4", "Class5"),
                List.of("f1.java", "f2.java", "f3.java", "f4.java"));
        assertEquals("High", effort);
    }

    @Test
    void testEstimateEffortBaseForHighRefactoring() {
        // With 2 items (not <= 1), the base effort "High" for Move Package should be returned
        String effort = service.estimateEffort("Move Package", List.of("ClassA"), List.of("fileA.java"));
        assertEquals("High", effort);
    }

    @Test
    void testSimulateDeleteDeadCode() {
        RefactoringImpactSimulationResponse response = service.simulateRefactoring(
                "Delete Dead Code", "ObsoleteUtil", "",
                "my-repo", "main");

        assertNotNull(response);
        assertEquals("Delete Dead Code", response.getRefactoringType());
        assertTrue(response.getImpactedClasses().contains("ObsoleteUtil (to be removed)"));
    }

    @Test
    void testSimulateMergeClasses() {
        RefactoringImpactSimulationResponse response = service.simulateRefactoring(
                "Merge Classes", "TargetClass", "SourceClass",
                "my-repo", "main");

        assertNotNull(response);
        assertEquals("Merge Classes", response.getRefactoringType());
        assertTrue(response.getImpactedClasses().contains("TargetClass (target)"));
        assertTrue(response.getImpactedClasses().contains("SourceClass (source)"));
    }

    @Test
    void testSimulateExtractService() {
        RefactoringImpactSimulationResponse response = service.simulateRefactoring(
                "Extract Service", "NotificationService", "EmailManager",
                "my-repo", "main");

        assertNotNull(response);
        assertEquals("Extract Service", response.getRefactoringType());
        assertTrue(response.getImpactedClasses().contains("NotificationService (extracted service)"));
        assertTrue(response.getImpactedClasses().contains("EmailManager (source class)"));
    }

    @Test
    void testSimulateMovePackage() {
        RefactoringImpactSimulationResponse response = service.simulateRefactoring(
                "Move Package", "com.oldpackage", "com.newpackage",
                "my-repo", "main");

        assertNotNull(response);
        assertEquals("Move Package", response.getRefactoringType());
    }

    @Test
    void testDeterministicOutput() {
        // Same inputs should produce identical outputs
        RefactoringImpactSimulationResponse response1 = service.simulateRefactoring(
                "Rename Class", "TestService", "NewTestService",
                "my-repo", "main");

        RefactoringImpactSimulationResponse response2 = service.simulateRefactoring(
                "Rename Class", "TestService", "NewTestService",
                "my-repo", "main");

        assertEquals(response1.getRefactoringType(), response2.getRefactoringType());
        assertEquals(response1.getTargetEntity(), response2.getTargetEntity());
        assertEquals(response1.getImpactedFiles(), response2.getImpactedFiles());
        assertEquals(response1.getImpactedClasses(), response2.getImpactedClasses());
        assertEquals(response1.getImpactedMethods(), response2.getImpactedMethods());
        assertEquals(response1.getBrokenReferences(), response2.getBrokenReferences());
        assertEquals(response1.getRiskAssessment(), response2.getRiskAssessment());
        assertEquals(response1.getSuggestedImplementationSequence(), response2.getSuggestedImplementationSequence());
        assertEquals(response1.getEstimatedEffort(), response2.getEstimatedEffort());
    }
}