package com.projectiq.mcp.tools;

import com.projectiq.mcp.analysis.service.RefactoringImpactSimulationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link SimulateRefactoringTool}.
 */
@ExtendWith(MockitoExtension.class)
class SimulateRefactoringToolTest {

    @Mock
    private RefactoringImpactSimulationService simulationService;

    private SimulateRefactoringTool tool;

    @BeforeEach
    void setUp() {
        tool = new SimulateRefactoringTool(simulationService);
    }

    @Test
    void testNullRefactoringType() {
        String result = tool.simulateRefactoring(null, "SomeClass", "",
                "my-repo", "main");
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Refactoring type is required"));
    }

    @Test
    void testEmptyRefactoringType() {
        String result = tool.simulateRefactoring("", "SomeClass", "",
                "my-repo", "main");
        assertTrue(result.contains("INVALID_ARGUMENT"));
    }

    @Test
    void testNullTargetEntity() {
        String result = tool.simulateRefactoring("Rename Class", null, "",
                "my-repo", "main");
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Target entity is required"));
    }

    @Test
    void testNullRepositoryName() {
        String result = tool.simulateRefactoring("Rename Class", "SomeClass", "",
                null, "main");
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Repository name is required"));
    }

    @Test
    void testEmptyRepositoryName() {
        String result = tool.simulateRefactoring("Rename Class", "SomeClass", "",
                "", "main");
        assertTrue(result.contains("INVALID_ARGUMENT"));
    }

    @Test
    void testInvalidRefactoringType() {
        // The service will throw IllegalArgumentException when given an invalid refactoring type
        when(simulationService.simulateRefactoring(eq("Invalid Type"), eq("SomeClass"), eq(""),
                eq("my-repo"), eq("main")))
                .thenThrow(new IllegalArgumentException("Unsupported refactoring type: Invalid Type"));

        String result = tool.simulateRefactoring("Invalid Type", "SomeClass", "",
                "my-repo", "main");
        // The tool catches IllegalArgumentException and returns INVALID_ARGUMENT error
        assertTrue(result.contains("INVALID_ARGUMENT"));
    }
}