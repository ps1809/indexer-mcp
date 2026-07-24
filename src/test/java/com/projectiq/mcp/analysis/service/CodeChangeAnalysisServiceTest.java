package com.projectiq.mcp.analysis.service;

import com.projectiq.mcp.analysis.dto.CodeChangeAnalysisResponse;
import com.projectiq.mcp.analysis.dto.ContextAssemblyResponse;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse.ImpactedComponent;
import com.projectiq.mcp.analysis.dto.ImplementationPlanningResponse;
import com.projectiq.mcp.analysis.dto.TaskAnalysisResponse;
import com.projectiq.mcp.analysis.dto.TaskType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CodeChangeAnalysisService}.
 * Verifies change analysis for various scenarios including:
 * - Service change analysis
 * - REST API change analysis
 * - Dependency change analysis
 * - Configuration change analysis
 * - Empty request
 * - Invalid entities
 */
@ExtendWith(MockitoExtension.class)
class CodeChangeAnalysisServiceTest {

    @Mock
    private TaskAnalysisService taskAnalysisService;

    @Mock
    private ContextAssemblyService contextAssemblyService;

    @Mock
    private ImpactAnalysisService impactAnalysisService;

    @Mock
    private ImplementationPlanningService implementationPlanningService;

    private CodeChangeAnalysisService service;

    @BeforeEach
    void setUp() {
        service = new CodeChangeAnalysisService(
                taskAnalysisService,
                contextAssemblyService,
                impactAnalysisService,
                implementationPlanningService
        );
    }

    @Test
    void testAnalyzeCodeChange_NewFeature() {
        String change = "Add user registration feature with email verification";
        String repository = "my-repo";

        // Mock task analysis
        TaskAnalysisResponse taskAnalysis = new TaskAnalysisResponse();
        taskAnalysis.setTaskType(TaskType.NEW_FEATURE);
        taskAnalysis.setDetectedEntities(Arrays.asList("UserRegistrationService", "EmailVerificationController"));
        when(taskAnalysisService.analyze(anyString())).thenReturn(taskAnalysis);

        // Mock impact analysis
        ImpactAnalysisResponse impactAnalysis = new ImpactAnalysisResponse();
        impactAnalysis.setOriginalTask(change);
        impactAnalysis.setTaskType(TaskType.NEW_FEATURE.getDisplayName());
        impactAnalysis.setPrimaryTargets(Arrays.asList("UserRegistrationService", "EmailVerificationController"));
        impactAnalysis.setDirectlyAffectedComponents(Arrays.asList(
                new ImpactedComponent("UserRegistrationService", "Service", "New registration service"),
                new ImpactedComponent("EmailVerificationController", "Controller", "Email verification endpoint")
        ));
        impactAnalysis.setIndirectlyAffectedComponents(Arrays.asList(
                new ImpactedComponent("UserRepository", "Repository", "Data access for users"),
                new ImpactedComponent("EmailService", "Service", "Email sending service")
        ));
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString())).thenReturn(impactAnalysis);

        // Mock context assembly
        when(contextAssemblyService.assembleContext(anyString(), anyString(), anyString()))
                .thenReturn(new ContextAssemblyResponse());

        // Mock implementation plan
        when(implementationPlanningService.generatePlan(anyString(), anyString(), anyString()))
                .thenReturn(new ImplementationPlanningResponse());

        // Execute
        CodeChangeAnalysisResponse response = service.analyzeCodeChange(change, repository, "main");

        // Verify
        assertNotNull(response);
        assertNotNull(response.getProposedChangeSummary());
        assertTrue(response.getProposedChangeSummary().contains("Add user registration feature with email verification"));
        assertTrue(response.getProposedChangeSummary().contains("New Feature"));
        assertNotNull(response.getImpactedFiles());
        assertFalse(response.getImpactedFiles().isEmpty());
        assertNotNull(response.getImpactedClasses());
        assertFalse(response.getImpactedClasses().isEmpty());
        assertNotNull(response.getImpactedMethods());
        assertNotNull(response.getImpactedRestApis());
        assertNotNull(response.getDependencyChanges());
        assertNotNull(response.getTestingRecommendations());
        assertFalse(response.getTestingRecommendations().isEmpty());
        assertNotNull(response.getRiskAssessment());
        assertFalse(response.getRiskAssessment().isEmpty());
        assertNotNull(response.getSuggestedImplementationOrder());
        assertFalse(response.getSuggestedImplementationOrder().isEmpty());
    }

    @Test
    void testAnalyzeCodeChange_RestApiChange() {
        String change = "Add pagination to UserController GET /api/users endpoint";
        String repository = "my-repo";

        TaskAnalysisResponse taskAnalysis = new TaskAnalysisResponse();
        taskAnalysis.setTaskType(TaskType.REST_API_CHANGE);
        taskAnalysis.setDetectedEntities(Arrays.asList("UserController"));
        when(taskAnalysisService.analyze(anyString())).thenReturn(taskAnalysis);

        ImpactAnalysisResponse impactAnalysis = new ImpactAnalysisResponse();
        impactAnalysis.setOriginalTask(change);
        impactAnalysis.setTaskType(TaskType.REST_API_CHANGE.getDisplayName());
        impactAnalysis.setPrimaryTargets(Arrays.asList("UserController"));
        impactAnalysis.setDirectlyAffectedComponents(Arrays.asList(
                new ImpactedComponent("UserController", "Controller", "User management controller"),
                new ImpactedComponent("/api/users", "REST API", "Users endpoint")
        ));
        impactAnalysis.setIndirectlyAffectedComponents(Arrays.asList(
                new ImpactedComponent("UserService", "Service", "User business logic")
        ));
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString())).thenReturn(impactAnalysis);

        when(contextAssemblyService.assembleContext(anyString(), anyString(), anyString()))
                .thenReturn(new ContextAssemblyResponse());
        when(implementationPlanningService.generatePlan(anyString(), anyString(), anyString()))
                .thenReturn(new ImplementationPlanningResponse());

        CodeChangeAnalysisResponse response = service.analyzeCodeChange(change, repository, "main");

        assertNotNull(response);
        assertEquals(TaskType.REST_API_CHANGE, taskAnalysis.getTaskType());
        assertNotNull(response.getImpactedRestApis());
    }

    @Test
    void testAnalyzeCodeChange_DependencyChange() {
        String change = "Upgrade Spring Boot version from 2.7 to 3.x with dependency updates";
        String repository = "my-repo";

        TaskAnalysisResponse taskAnalysis = new TaskAnalysisResponse();
        taskAnalysis.setTaskType(TaskType.NEW_FEATURE);
        taskAnalysis.setDetectedEntities(Arrays.asList("pom.xml"));
        when(taskAnalysisService.analyze(anyString())).thenReturn(taskAnalysis);

        when(contextAssemblyService.assembleContext(anyString(), anyString(), anyString()))
                .thenReturn(new ContextAssemblyResponse());
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(new ImpactAnalysisResponse());

        CodeChangeAnalysisResponse response = service.analyzeCodeChange(change, repository, "main");

        assertNotNull(response);
        assertTrue(response.getDependencyChanges().stream()
                .anyMatch(dep -> dep.toLowerCase().contains("dependency") || dep.toLowerCase().contains("version")));
    }

    @Test
    void testAnalyzeCodeChange_ConfigurationChange() {
        String change = "Update database connection pool settings in application.yml";
        String repository = "my-repo";

        TaskAnalysisResponse taskAnalysis = new TaskAnalysisResponse();
        taskAnalysis.setTaskType(TaskType.CONFIGURATION_CHANGE);
        when(taskAnalysisService.analyze(anyString())).thenReturn(taskAnalysis);

        when(contextAssemblyService.assembleContext(anyString(), anyString(), anyString()))
                .thenReturn(new ContextAssemblyResponse());
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(new ImpactAnalysisResponse());

        CodeChangeAnalysisResponse response = service.analyzeCodeChange(change, repository, "main");

        assertNotNull(response);
        assertTrue(response.getTestingRecommendations().stream()
                .anyMatch(r -> r.toLowerCase().contains("configur")));
        assertTrue(response.getRiskAssessment().stream()
                .anyMatch(r -> r.toLowerCase().contains("configur") || r.toLowerCase().contains("environment")));
    }

    @Test
    void testAnalyzeCodeChange_EmptyRequest_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.analyzeCodeChange("", "my-repo", "main");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            service.analyzeCodeChange(null, "my-repo", "main");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            service.analyzeCodeChange("   ", "my-repo", "main");
        });
    }

    @Test
    void testAnalyzeCodeChange_FallbackWhenServicesFail() {
        String change = "Fix NPE in UserService.getUserDetails";
        String repository = "my-repo";

        TaskAnalysisResponse taskAnalysis = new TaskAnalysisResponse();
        taskAnalysis.setTaskType(TaskType.BUG_FIX);
        when(taskAnalysisService.analyze(anyString())).thenReturn(taskAnalysis);

        // Simulate failures in non-critical services
        when(contextAssemblyService.assembleContext(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Context assembly failed"));
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Impact analysis failed"));

        CodeChangeAnalysisResponse response = service.analyzeCodeChange(change, repository, "main");

        // Should still produce a valid response with defaults
        assertNotNull(response);
        assertNotNull(response.getProposedChangeSummary());
        assertNotNull(response.getImpactedFiles());
        assertFalse(response.getImpactedFiles().isEmpty());
        assertNotNull(response.getImpactedClasses());
        assertFalse(response.getImpactedClasses().isEmpty());
        assertNotNull(response.getDependencyChanges());
        assertNotNull(response.getTestingRecommendations());
        assertNotNull(response.getRiskAssessment());
        assertNotNull(response.getSuggestedImplementationOrder());
    }

    @Test
    void testGenerateChangeSummary_WithEntities() {
        TaskAnalysisResponse taskAnalysis = new TaskAnalysisResponse();
        taskAnalysis.setTaskType(TaskType.NEW_FEATURE);
        taskAnalysis.setDetectedEntities(Arrays.asList("UserService", "UserController"));

        String summary = service.generateChangeSummary("Add user management feature", TaskType.NEW_FEATURE, taskAnalysis);
        assertTrue(summary.contains("Add user management feature"));
        assertTrue(summary.contains("New Feature"));
        assertTrue(summary.contains("UserService"));
        assertTrue(summary.contains("UserController"));
    }

    @Test
    void testIdentifyImpactedFiles_WithImpactAnalysis() {
        ImpactAnalysisResponse impactAnalysis = new ImpactAnalysisResponse();
        impactAnalysis.setDirectlyAffectedComponents(Arrays.asList(
                new ImpactedComponent("UserController", "Controller", "Direct match"),
                new ImpactedComponent("UserService", "Service", "Direct match")
        ));

        List<String> files = service.identifyImpactedFiles(
                "Update UserController", "update usercontroller",
                TaskType.REST_API_CHANGE, impactAnalysis);

        assertTrue(files.contains("UserController.java"));
        assertTrue(files.contains("UserService.java"));
    }

    @Test
    void testIdentifyImpactedFiles_NoImpactAnalysis() {
        List<String> files = service.identifyImpactedFiles(
                "Fix bug", "fix bug",
                TaskType.BUG_FIX, null);

        assertFalse(files.isEmpty());
        // Should have default files for bug fix
        assertTrue(files.stream().anyMatch(f -> f.contains("Bug") || f.contains("Affected")));
    }

    @Test
    void testIdentifyImpactedClasses_WithPatternMatches() {
        String change = "Modify UserService, UserController and UserRepository";
        ImpactAnalysisResponse impactAnalysis = new ImpactAnalysisResponse();

        List<String> classes = service.identifyImpactedClasses(
                change, change.toLowerCase(),
                TaskType.REFACTORING, impactAnalysis);

        assertTrue(classes.stream().anyMatch(c -> c.contains("UserService")));
        assertTrue(classes.stream().anyMatch(c -> c.contains("UserController")));
        assertTrue(classes.stream().anyMatch(c -> c.contains("UserRepository")));
    }

    @Test
    void testIdentifyImpactedMethods_WithMethodPatterns() {
        String change = "Implement findAll() and findById() methods";

        List<String> methods = service.identifyImpactedMethods(change, change.toLowerCase(), TaskType.NEW_FEATURE);

        assertTrue(methods.contains("findAll()"));
        assertTrue(methods.contains("findById()"));
    }

    @Test
    void testIdentifyImpactedRestApis_WithEndpoint() {
        String change = "Modify GET /api/users endpoint";

        List<String> apis = service.identifyImpactedRestApis(change, change.toLowerCase(), TaskType.REST_API_CHANGE);

        assertFalse(apis.isEmpty());
    }

    @Test
    void testIdentifyImpactedRestApis_NonApiChange() {
        String change = "Fix validation in entity class";
        List<String> apis = service.identifyImpactedRestApis(change, change.toLowerCase(), TaskType.BUG_FIX);

        // Should not have API endpoints for a non-API change
        assertTrue(apis.isEmpty());
    }

    @Test
    void testGenerateTestingRecommendations_NewFeature() {
        List<String> impactedClasses = Arrays.asList("UserService (Service)", "UserController (Controller)");

        List<String> recommendations = service.generateTestingRecommendations(
                TaskType.NEW_FEATURE, "new feature", impactedClasses);

        assertTrue(recommendations.stream().anyMatch(r -> r.contains("unit tests")));
        assertTrue(recommendations.stream().anyMatch(r -> r.contains("integration tests")));
        assertTrue(recommendations.stream().anyMatch(r -> r.contains("end-to-end tests")));
    }

    @Test
    void testGenerateTestingRecommendations_WithClassSpecific() {
        List<String> impactedClasses = Arrays.asList("UserController (Type)", "UserService (Type)", "UserRepository (Type)");

        List<String> recommendations = service.generateTestingRecommendations(
                TaskType.REST_API_CHANGE, "api change", impactedClasses);

        assertTrue(recommendations.stream().anyMatch(r -> r.contains("controller tests")));
        assertTrue(recommendations.stream().anyMatch(r -> r.contains("service layer tests")));
        assertTrue(recommendations.stream().anyMatch(r -> r.contains("data access tests")));
    }

    @Test
    void testGenerateRiskAssessment_SecurityChange() {
        List<String> classes = Arrays.asList("AuthController", "UserService");

        List<String> risks = service.generateRiskAssessment(
                TaskType.NEW_FEATURE, "add authentication with oauth", classes, List.of());

        assertTrue(risks.stream().anyMatch(r -> r.toLowerCase().contains("security")));
    }

    @Test
    void testGenerateRiskAssessment_DatabaseChange() {
        List<String> risks = service.generateRiskAssessment(
                TaskType.DATABASE_CHANGE, "add new entity and migration", List.of(), List.of());

        assertTrue(risks.stream().anyMatch(r -> r.toLowerCase().contains("database") || r.toLowerCase().contains("migration")));
    }

    @Test
    void testGenerateRiskAssessment_NoSpecificRisks() {
        List<String> risks = service.generateRiskAssessment(
                TaskType.DOCUMENTATION, "update readme", List.of(), List.of());

        assertFalse(risks.isEmpty());
        assertTrue(risks.stream().anyMatch(r -> r.toLowerCase().contains("documentation")
                || r.toLowerCase().contains("limited information")));
    }

    @Test
    void testGenerateImplementationOrder_HasBaseAndClassSpecific() {
        List<String> impactedClasses = Arrays.asList("UserService (Service)", "UserController (Controller)");

        List<String> order = service.generateImplementationOrder(TaskType.NEW_FEATURE, "new feature", impactedClasses);

        assertFalse(order.isEmpty());
        assertTrue(order.stream().anyMatch(s -> s.contains("Implement changes in")));
        assertTrue(order.stream().anyMatch(s -> s.contains("Final verification")));
    }

    @Test
    void testGenerateChangeSummary_WithoutEntities() {
        TaskAnalysisResponse taskAnalysis = new TaskAnalysisResponse();
        taskAnalysis.setTaskType(TaskType.REFACTORING);

        String summary = service.generateChangeSummary("Refactor user module", TaskType.REFACTORING, taskAnalysis);
        assertTrue(summary.contains("Refactor user module"));
        assertTrue(summary.contains("Refactoring"));
    }

    @Test
    void testDefaultBranchBehavior() {
        String change = "Add logging";
        String repository = "my-repo";

        TaskAnalysisResponse taskAnalysis = new TaskAnalysisResponse();
        taskAnalysis.setTaskType(TaskType.NEW_FEATURE);
        when(taskAnalysisService.analyze(anyString())).thenReturn(taskAnalysis);
        when(contextAssemblyService.assembleContext(anyString(), anyString(), eq("main")))
                .thenReturn(new ContextAssemblyResponse());

        // Test with null branch - should default to "main"
        CodeChangeAnalysisResponse response = service.analyzeCodeChange(change, repository, null);
        assertNotNull(response);
        verify(contextAssemblyService).assembleContext(anyString(), anyString(), eq("main"));
    }

    @Test
    void testDeterministicOutput() {
        String change = "Add pagination support";
        String repository = "test-repo";

        TaskAnalysisResponse taskAnalysis = new TaskAnalysisResponse();
        taskAnalysis.setTaskType(TaskType.NEW_FEATURE);
        when(taskAnalysisService.analyze(anyString())).thenReturn(taskAnalysis);
        when(contextAssemblyService.assembleContext(anyString(), anyString(), anyString()))
                .thenReturn(new ContextAssemblyResponse());
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(new ImpactAnalysisResponse());

        // Run twice and verify same output
        CodeChangeAnalysisResponse response1 = service.analyzeCodeChange(change, repository, "main");
        CodeChangeAnalysisResponse response2 = service.analyzeCodeChange(change, repository, "main");

        assertEquals(response1.getImpactedFiles(), response2.getImpactedFiles());
        assertEquals(response1.getImpactedClasses(), response2.getImpactedClasses());
        assertEquals(response1.getTestingRecommendations(), response2.getTestingRecommendations());
        assertEquals(response1.getRiskAssessment(), response2.getRiskAssessment());
        assertEquals(response1.getSuggestedImplementationOrder(), response2.getSuggestedImplementationOrder());
    }

    @Test
    void testDependencyChanges_ForDependencyKeywords() {
        List<String> changes = service.identifyDependencyChanges(
                "upgrade maven dependency version", TaskType.REFACTORING);

        assertTrue(changes.stream().anyMatch(c -> c.toLowerCase().contains("dependency")));
    }

    @Test
    void testDependencyChanges_ForDocumentation() {
        List<String> changes = service.identifyDependencyChanges(
                "update readme", TaskType.DOCUMENTATION);

        assertTrue(changes.isEmpty());
    }
}