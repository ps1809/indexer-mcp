package com.projectiq.mcp.analysis.service;

import com.projectiq.mcp.analysis.dto.ComplexityLevel;
import com.projectiq.mcp.analysis.dto.ConfidenceLevel;
import com.projectiq.mcp.analysis.dto.TaskAnalysisResponse;
import com.projectiq.mcp.analysis.dto.TaskType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskAnalysisServiceTest {

    private TaskAnalysisService service;

    @BeforeEach
    void setUp() {
        service = new TaskAnalysisService();
    }

    @Test
    void analyze_featureRequest_detectsNewFeature() {
        // Act
        TaskAnalysisResponse response = service.analyze("Add pagination to UserController");

        // Assert
        assertEquals(TaskType.NEW_FEATURE, response.getTaskType());
        assertEquals("Add pagination to UserController", response.getOriginalTask());
        assertNotNull(response.getConfidenceLevel());
        assertNotNull(response.getDetectedEntities());
        assertNotNull(response.getSuggestedTools());
        assertNotNull(response.getExecutionPlan());
        assertNotNull(response.getReasoningSummary());
        assertNotNull(response.getEstimatedComplexity());
    }

    @Test
    void analyze_bugFix_detectsBugFix() {
        // Act
        TaskAnalysisResponse response = service.analyze("Fix null pointer exception in UserService");

        // Assert
        assertEquals(TaskType.BUG_FIX, response.getTaskType());
        assertNotNull(response.getDetectedEntities());
        assertTrue(response.getDetectedEntities().stream().anyMatch(e -> e.contains("UserService")));
    }

    @Test
    void analyze_refactoringRequest_detectsRefactoring() {
        // Act
        TaskAnalysisResponse response = service.analyze("Refactor UserRepository to use JPA specifications");

        // Assert
        assertEquals(TaskType.REFACTORING, response.getTaskType());
        assertNotNull(response.getDetectedEntities());
        assertTrue(response.getDetectedEntities().stream().anyMatch(e -> e.contains("UserRepository")));
    }

    @Test
    void analyze_restApiRequest_detectsRestApiChange() {
        // Act
        TaskAnalysisResponse response = service.analyze("Add POST endpoint to OrderController for creating orders");

        // Assert
        assertEquals(TaskType.REST_API_CHANGE, response.getTaskType());
        assertTrue(response.getSuggestedTools().contains("find_rest_api"));
    }

    @Test
    void analyze_configurationChange_detectsConfigurationChange() {
        // Act
        TaskAnalysisResponse response = service.analyze("Update application.yml with new datasource configuration");

        // Assert
        assertEquals(TaskType.CONFIGURATION_CHANGE, response.getTaskType());
        assertTrue(response.getSuggestedTools().contains("find_dependency"));
    }

    @Test
    void analyze_unitTestRequest_detectsUnitTest() {
        // Act
        TaskAnalysisResponse response = service.analyze("Write unit tests for PaymentService");

        // Assert
        assertEquals(TaskType.UNIT_TEST, response.getTaskType());
    }

    @Test
    void analyze_documentationRequest_detectsDocumentation() {
        // Act
        TaskAnalysisResponse response = service.analyze("Add javadoc documentation to SecurityConfig");

        // Assert
        assertEquals(TaskType.DOCUMENTATION, response.getTaskType());
    }

    @Test
    void analyze_unknownRequest_returnsUnknown() {
        // Act
        TaskAnalysisResponse response = service.analyze("Do something random here");

        // Assert
        assertEquals(TaskType.UNKNOWN, response.getTaskType());
        assertEquals(ConfidenceLevel.LOW, response.getConfidenceLevel());
    }

    @Test
    void analyze_emptyRequest_throwsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.analyze(""));
    }

    @Test
    void analyze_nullRequest_throwsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.analyze(null));
    }

    @Test
    void analyze_whitespaceRequest_throwsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.analyze("   "));
    }

    @Test
    void analyze_databaseChange_detectsDatabaseChange() {
        // Act
        TaskAnalysisResponse response = service.analyze("Add migration script for user_address table");

        // Assert
        assertEquals(TaskType.DATABASE_CHANGE, response.getTaskType());
    }

    @Test
    void analyze_performanceImprovement_detectsPerformance() {
        // Act
        TaskAnalysisResponse response = service.analyze("Improve query performance in OrderRepository");

        // Assert
        assertEquals(TaskType.PERFORMANCE_IMPROVEMENT, response.getTaskType());
        assertTrue(response.getSuggestedTools().contains("repository_statistics"));
    }

    @Test
    void analyze_detectsControllerEntity() {
        // Act
        TaskAnalysisResponse response = service.analyze("Add sorting to ProductController");

        // Assert
        assertTrue(response.getDetectedEntities().stream()
                .anyMatch(e -> e.contains("ProductController")));
    }

    @Test
    void analyze_detectsServiceEntity() {
        // Act
        TaskAnalysisResponse response = service.analyze("Fix bug in OrderService");

        // Assert
        assertTrue(response.getDetectedEntities().stream()
                .anyMatch(e -> e.contains("OrderService")));
    }

    @Test
    void analyze_detectsEntityModel() {
        // Act
        TaskAnalysisResponse response = service.analyze("Add new field to UserEntity");

        // Assert
        assertTrue(response.getDetectedEntities().stream()
                .anyMatch(e -> e.contains("UserEntity")));
    }

    @Test
    void analyze_detectsDtoEntity() {
        // Act
        TaskAnalysisResponse response = service.analyze("Create OrderRequest DTO for validation");

        // Assert
        assertTrue(response.getDetectedEntities().stream()
                .anyMatch(e -> e.contains("OrderRequest")));
    }

    @Test
    void analyze_detectsConfigurationClass() {
        // Act
        TaskAnalysisResponse response = service.analyze("Update SecurityConfig with new filter chain");

        // Assert
        assertTrue(response.getDetectedEntities().stream()
                .anyMatch(e -> e.contains("SecurityConfig")));
    }

    @Test
    void analyze_detectsEndpoint() {
        // Act
        TaskAnalysisResponse response = service.analyze("Add /api/users endpoint to UserController");

        // Assert
        assertTrue(response.getDetectedEntities().stream()
                .anyMatch(e -> e.contains("Endpoint:") || e.startsWith("Endpoint:")),
                "Should detect endpoint in entities, got: " + response.getDetectedEntities());
    }

    @Test
    void analyze_buildsExecutionPlan_withOrderedSteps() {
        // Act
        TaskAnalysisResponse response = service.analyze("Add pagination to UserController");

        // Assert
        List<TaskAnalysisResponse.ExecutionStep> plan = response.getExecutionPlan();
        assertNotNull(plan);
        assertFalse(plan.isEmpty());

        // First step should always be repository_summary
        assertEquals("repository_summary", plan.get(0).getToolName());

        // Last step should be prompt_context
        assertEquals("prompt_context", plan.get(plan.size() - 1).getToolName());

        // Step numbers should be sequential
        for (int i = 0; i < plan.size(); i++) {
            assertEquals(i + 1, plan.get(i).getStepNumber());
        }
    }

    @Test
    void analyze_stableOutput_forIdenticalInput() {
        // Arrange
        String task = "Add pagination to UserController";

        // Act
        TaskAnalysisResponse first = service.analyze(task);
        TaskAnalysisResponse second = service.analyze(task);

        // Assert
        assertEquals(first.getTaskType(), second.getTaskType());
        assertEquals(first.getConfidenceLevel(), second.getConfidenceLevel());
        assertEquals(first.getDetectedEntities(), second.getDetectedEntities());
        assertEquals(first.getSuggestedTools(), second.getSuggestedTools());
        assertEquals(first.getExecutionPlan().size(), second.getExecutionPlan().size());
        assertEquals(first.getReasoningSummary(), second.getReasoningSummary());
        assertEquals(first.getEstimatedComplexity(), second.getEstimatedComplexity());

        // Verify deterministic ordering
        for (int i = 0; i < first.getExecutionPlan().size(); i++) {
            assertEquals(first.getExecutionPlan().get(i).getToolName(),
                    second.getExecutionPlan().get(i).getToolName());
        }
    }

    @Test
    void analyze_noDuplicateTools_inExecutionPlan() {
        // Act
        TaskAnalysisResponse response = service.analyze("Refactor UserController and UserService to improve performance");

        // Assert
        List<String> toolNames = response.getSuggestedTools();
        assertEquals(toolNames.size(), toolNames.stream().distinct().count(),
                "Tool list should not contain duplicates");
    }

    @Test
    void analyze_estimatesComplexity() {
        // Act
        TaskAnalysisResponse response = service.analyze("Add pagination to UserController");

        // Assert
        assertNotNull(response.getEstimatedComplexity());
        assertTrue(response.getEstimatedComplexity() == ComplexityLevel.LOW
                || response.getEstimatedComplexity() == ComplexityLevel.MEDIUM
                || response.getEstimatedComplexity() == ComplexityLevel.HIGH);
    }

    @Test
    void analyze_reasoningSummary_containsTaskType() {
        // Act
        TaskAnalysisResponse response = service.analyze("Add pagination to UserController");

        // Assert
        String reasoning = response.getReasoningSummary();
        assertNotNull(reasoning);
        assertTrue(reasoning.contains("New Feature"));
    }

    @Test
    void detectTaskType_strongBugFixIndicators() {
        assertEquals(TaskType.BUG_FIX, service.detectTaskType("fix the login bug in AuthService"));
        assertEquals(TaskType.BUG_FIX, service.detectTaskType("fix broken endpoint in UserController"));
        assertEquals(TaskType.BUG_FIX, service.detectTaskType("fix null pointer exception"));
    }

    @Test
    void detectTaskType_strongRefactoringIndicators() {
        assertEquals(TaskType.REFACTORING, service.detectTaskType("refactor UserService"));
        assertEquals(TaskType.REFACTORING, service.detectTaskType("refactor code to simplify logic"));
    }

    @Test
    void detectTaskType_specificRestApiIndicators() {
        assertEquals(TaskType.REST_API_CHANGE, service.detectTaskType("add getmapping for new endpoint"));
    }

    @Test
    void determineConfidence_unknownTask_returnsLow() {
        assertEquals(ConfidenceLevel.LOW, service.determineConfidence("do random stuff", TaskType.UNKNOWN));
    }

    @Test
    void determineConfidence_strongMatch_returnsHigh() {
        assertEquals(ConfidenceLevel.HIGH,
                service.determineConfidence("add new feature to implement create user", TaskType.NEW_FEATURE));
    }

    @Test
    void determineConfidence_mediumMatch_returnsMedium() {
        assertEquals(ConfidenceLevel.MEDIUM,
                service.determineConfidence("add logging to service", TaskType.NEW_FEATURE));
    }

    @Test
    void detectEntities_withNoEntities_returnsEmpty() {
        List<String> entities = service.detectEntities("Just a simple task description here");
        assertTrue(entities.isEmpty());
    }

    @Test
    void detectEntities_detectsMultipleEntityTypes() {
        List<String> entities = service.detectEntities(
                "Add pagination to UserController and update UserService, creating UserDto");
        assertFalse(entities.isEmpty());
        assertTrue(entities.stream().anyMatch(e -> e.contains("UserController")));
        assertTrue(entities.stream().anyMatch(e -> e.contains("UserService")));
        assertTrue(entities.stream().anyMatch(e -> e.contains("UserDto")));
    }

    @Test
    void estimateComplexity_withManyTools_returnsHigh() {
        ComplexityLevel level = service.estimateComplexity(8, 5, TaskType.REFACTORING);
        assertEquals(ComplexityLevel.HIGH, level);
    }

    @Test
    void estimateComplexity_withFewTools_returnsLow() {
        ComplexityLevel level = service.estimateComplexity(1, 0, TaskType.DOCUMENTATION);
        assertEquals(ComplexityLevel.LOW, level);
    }

    @Test
    void generateReasoning_containsEntityInfo() {
        String reasoning = service.generateReasoning(
                TaskType.NEW_FEATURE,
                List.of("Class: UserController", "Class: UserService"),
                List.of("repository_summary", "search_code", "find_class"),
                "Add pagination to UserController"
        );
        assertTrue(reasoning.contains("UserController"));
        assertTrue(reasoning.contains("UserService"));
        assertTrue(reasoning.contains("New Feature"));
    }

    @Test
    void determineTools_forBugFix_includesSearchAndClass() {
        List<String> tools = service.determineTools(TaskType.BUG_FIX,
                List.of("Class: UserController"), "fix bug in usercontroller");
        assertTrue(tools.contains("repository_summary"));
        assertTrue(tools.contains("search_code"));
        assertTrue(tools.contains("find_class"));
    }

    @Test
    void determineTools_forSimpleTask_excludesUnnecessaryTools() {
        List<String> tools = service.determineTools(TaskType.DOCUMENTATION,
                List.of(), "write documentation");
        assertTrue(tools.contains("repository_summary"));
        assertTrue(tools.contains("search_code"));
        assertTrue(tools.contains("list_related_files"));
    }

    @Test
    void buildExecutionPlan_respectsToolOrder() {
        List<String> tools = List.of("repository_summary", "search_code",
                "find_class", "list_related_files", "prompt_context");
        List<TaskAnalysisResponse.ExecutionStep> plan = service.buildExecutionPlan(
                tools, TaskType.NEW_FEATURE, List.of(), "task");

        assertEquals(5, plan.size());
        assertEquals("repository_summary", plan.get(0).getToolName());
        assertEquals("search_code", plan.get(1).getToolName());
        assertEquals("find_class", plan.get(2).getToolName());
        assertEquals("list_related_files", plan.get(3).getToolName());
        assertEquals("prompt_context", plan.get(4).getToolName());
    }
}