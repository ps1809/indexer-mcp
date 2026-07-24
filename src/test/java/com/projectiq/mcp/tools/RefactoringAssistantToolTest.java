package com.projectiq.mcp.tools;

import com.projectiq.mcp.analysis.dto.RefactoringAssistantResponse;
import com.projectiq.mcp.analysis.service.RefactoringAssistantService;
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
 * Tests for {@link RefactoringAssistantTool}.
 */
@ExtendWith(MockitoExtension.class)
class RefactoringAssistantToolTest {

    @Mock
    private RefactoringAssistantService refactoringAssistantService;

    private RefactoringAssistantTool tool;

    @BeforeEach
    void setUp() {
        tool = new RefactoringAssistantTool(refactoringAssistantService);
    }

    @Test
    void testAnalyzeRefactoring_Success() {
        // Arrange
        String task = "Rename class UserService to UserManagementService";
        String repositoryName = "my-repo";

        RefactoringAssistantResponse response = new RefactoringAssistantResponse();
        response.setOriginalTask(task);
        response.setRefactoringType("Rename Class");
        response.setAffectedClasses(List.of("UserService", "UserManagementService (new name)"));
        response.setAffectedMethods(List.of());
        response.setAffectedPackages(List.of());
        response.setDependenciesInvolved(List.of("All import statements referencing the renamed class"));
        response.setSuggestedExecutionOrder(List.of(
                "1. Identify all references to the class across the codebase",
                "2. Update the class declaration with the new name"
        ));
        response.setValidationChecklist(List.of(
                "Verify the behavior remains unchanged after refactoring",
                "Check for any compilation or import issues"
        ));
        response.setRecommendedTests(List.of("UserServiceTest", "UserManagementServiceTest"));
        response.setRisks(List.of("External consumers may reference the old class name"));
        response.setConfidenceLevel("High");

        when(refactoringAssistantService.analyzeRefactoring(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = tool.analyzeRefactoring(task, repositoryName, "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Rename Class"));
        assertTrue(result.contains("UserService"));
        assertTrue(result.contains("UserManagementService"));
        assertTrue(result.contains("High"));

        verify(refactoringAssistantService).analyzeRefactoring(task.trim(), repositoryName.trim(), "main");
    }

    @Test
    void testAnalyzeRefactoring_EmptyTask() {
        // Act
        String result = tool.analyzeRefactoring("", "my-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("required"));

        verify(refactoringAssistantService, never()).analyzeRefactoring(anyString(), anyString(), anyString());
    }

    @Test
    void testAnalyzeRefactoring_NullTask() {
        // Act
        String result = tool.analyzeRefactoring(null, "my-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));

        verify(refactoringAssistantService, never()).analyzeRefactoring(anyString(), anyString(), anyString());
    }

    @Test
    void testAnalyzeRefactoring_EmptyRepositoryName() {
        // Act
        String result = tool.analyzeRefactoring("Refactor the codebase", "", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));

        verify(refactoringAssistantService, never()).analyzeRefactoring(anyString(), anyString(), anyString());
    }

    @Test
    void testAnalyzeRefactoring_NullRepositoryName() {
        // Act
        String result = tool.analyzeRefactoring("Refactor the codebase", null, "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));

        verify(refactoringAssistantService, never()).analyzeRefactoring(anyString(), anyString(), anyString());
    }

    @Test
    void testAnalyzeRefactoring_DefaultBranch() {
        // Arrange
        String task = "Rename class UserService to UserAdminService";
        String repositoryName = "my-repo";

        RefactoringAssistantResponse response = new RefactoringAssistantResponse();
        response.setOriginalTask(task);
        response.setRefactoringType("Rename Class");
        response.setConfidenceLevel("Medium");

        when(refactoringAssistantService.analyzeRefactoring(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = tool.analyzeRefactoring(task, repositoryName, null);

        // Assert
        assertNotNull(result);
        verify(refactoringAssistantService).analyzeRefactoring(task.trim(), repositoryName.trim(), "main");
    }

    @Test
    void testAnalyzeRefactoring_ServiceThrowsIllegalArgument() {
        // Arrange
        when(refactoringAssistantService.analyzeRefactoring(anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid task format"));

        // Act
        String result = tool.analyzeRefactoring("Some invalid task format", "my-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Invalid task format"));
    }

    @Test
    void testAnalyzeRefactoring_ServiceThrowsUnexpected() {
        // Arrange
        when(refactoringAssistantService.analyzeRefactoring(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Connection timeout"));

        // Act
        String result = tool.analyzeRefactoring("Refactor the codebase", "my-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INTERNAL_ERROR"));
        assertTrue(result.contains("Connection timeout"));
    }

    @Test
    void testAnalyzeRefactoring_ReturnsValidJson() {
        // Arrange
        String task = "Extract method calculateTotal from processOrder";
        String repositoryName = "my-repo";

        RefactoringAssistantResponse response = new RefactoringAssistantResponse();
        response.setOriginalTask(task);
        response.setRefactoringType("Extract Method");
        response.setAffectedMethods(List.of("calculateTotal (extracted)", "processOrder (source)"));
        response.setConfidenceLevel("Medium");

        when(refactoringAssistantService.analyzeRefactoring(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = tool.analyzeRefactoring(task, repositoryName, "main");

        // Assert
        assertNotNull(result);
        // Verify valid JSON format
        assertTrue(result.startsWith("{"));
        assertTrue(result.endsWith("}"));
        assertTrue(result.contains("Extract Method"));
        assertTrue(result.contains("calculateTotal"));
        assertTrue(result.contains("processOrder"));
    }

    @Test
    void testAnalyzeRefactoring_RenameMethod() {
        // Arrange
        String task = "Rename method calculateTotal to computeTotal";
        String repositoryName = "my-repo";

        RefactoringAssistantResponse response = new RefactoringAssistantResponse();
        response.setOriginalTask(task);
        response.setRefactoringType("Rename Method");
        response.setAffectedMethods(List.of("calculateTotal", "computeTotal (new name)"));
        response.setConfidenceLevel("Medium");

        when(refactoringAssistantService.analyzeRefactoring(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = tool.analyzeRefactoring(task, repositoryName, "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Rename Method"));
        assertTrue(result.contains("calculateTotal"));
        assertTrue(result.contains("computeTotal"));
    }

    @Test
    void testAnalyzeRefactoring_DeleteDeadCode() {
        // Arrange
        String task = "Delete dead code - remove unused class ObsoleteHelper";
        String repositoryName = "my-repo";

        RefactoringAssistantResponse response = new RefactoringAssistantResponse();
        response.setOriginalTask(task);
        response.setRefactoringType("Delete Dead Code");
        response.setRisks(List.of("Code may appear unused but be accessed via reflection"));
        response.setConfidenceLevel("Medium");

        when(refactoringAssistantService.analyzeRefactoring(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = tool.analyzeRefactoring(task, repositoryName, "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Delete Dead Code"));
        assertTrue(result.contains("risks"));
    }
}