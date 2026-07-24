package com.projectiq.mcp.pipeline.service;

import com.projectiq.mcp.analysis.dto.*;
import com.projectiq.mcp.analysis.service.ArchitectureInsightsService;
import com.projectiq.mcp.analysis.service.ImpactAnalysisService;
import com.projectiq.mcp.analysis.service.RepositoryConventionAnalyzerService;
import com.projectiq.mcp.analysis.service.RepositoryHealthService;
import com.projectiq.mcp.client.dto.*;
import com.projectiq.mcp.client.service.DevelopmentContextService;
import com.projectiq.mcp.client.service.PromptContextService;
import com.projectiq.mcp.client.service.RepositoryContextBuilderService;
import com.projectiq.mcp.pipeline.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link IntelligentContextPipelineService}.
 */
@ExtendWith(MockitoExtension.class)
class IntelligentContextPipelineServiceTest {

    @Mock
    private RepositoryContextBuilderService contextBuilderService;
    @Mock
    private DevelopmentContextService developmentContextService;
    @Mock
    private PromptContextService promptContextService;
    @Mock
    private ArchitectureInsightsService architectureInsightsService;
    @Mock
    private RepositoryConventionAnalyzerService conventionAnalyzerService;
    @Mock
    private RepositoryHealthService repositoryHealthService;
    @Mock
    private ImpactAnalysisService impactAnalysisService;

    private IntelligentContextPipelineService pipelineService;

    @BeforeEach
    void setUp() {
        pipelineService = new IntelligentContextPipelineService(
                contextBuilderService, developmentContextService, promptContextService,
                architectureInsightsService, conventionAnalyzerService,
                repositoryHealthService, impactAnalysisService);
    }

    @Test
    void testBuildContextPipelineCompleteWorkflow() {
        // Arrange
        when(contextBuilderService.buildContext(any())).thenReturn(createMockRepositoryContext());
        when(developmentContextService.createDevelopmentContext(any())).thenReturn(createMockDevelopmentContext());
        when(promptContextService.createPromptContext(any())).thenReturn(createMockPromptContext());
        when(architectureInsightsService.analyzeArchitecture(anyString(), anyString())).thenReturn(createMockArchitectureInsights());
        when(conventionAnalyzerService.analyzeConventions(anyString(), anyString())).thenReturn(createMockConventions());
        when(repositoryHealthService.analyzeHealth(anyString(), anyString())).thenReturn(createMockHealth());
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString())).thenReturn(createMockImpact());

        // Act
        ContextPackage result = pipelineService.buildContextPipeline(
                "Test workflow",
                "implementation",
                "test-repo",
                "main",
                "Add pagination to UserController");

        // Assert
        assertNotNull(result);
        assertEquals("Test workflow", result.getWorkflowSummary());
        assertNotNull(result.getRepositorySummary());
        assertTrue(result.getTotalContextItems() > 0);
        assertTrue(result.getHighPriorityCount() >= 0);
        assertTrue(result.getMediumPriorityCount() >= 0);
        assertTrue(result.getProcessingTimeMillis() >= 0);
    }

    @Test
    void testBuildContextPipelineWithMissingContext() {
        // Arrange - all services return null
        when(contextBuilderService.buildContext(any())).thenReturn(null);

        // Act
        ContextPackage result = pipelineService.buildContextPipeline(
                "Test workflow", "analysis", "test-repo", "main", "Test task");

        // Assert
        assertNotNull(result);
        assertEquals("Test workflow", result.getWorkflowSummary());
        assertTrue(result.getWarnings().stream().anyMatch(w -> w.contains("not available")));
        assertEquals("Not available", result.getRepositorySummary());
    }

    @Test
    void testDuplicateRemoval() {
        // Arrange
        List<ContextItem> items = new ArrayList<>();
        items.add(new ContextItem(ContextSourceType.CLASS_ANALYSIS, ContextPriority.HIGH, "ClassA", "key1"));
        items.add(new ContextItem(ContextSourceType.METHOD_ANALYSIS, ContextPriority.MEDIUM, "MethodA", "key2"));
        items.add(new ContextItem(ContextSourceType.CLASS_ANALYSIS, ContextPriority.HIGH, "ClassA-Dup", "key1")); // duplicate key

        // Act
        List<ContextItem> deduplicated = pipelineService.removeDuplicates(items);

        // Assert
        assertEquals(2, deduplicated.size());
        assertTrue(deduplicated.stream().anyMatch(i -> "ClassA".equals(i.getContent())));
        assertTrue(deduplicated.stream().anyMatch(i -> "MethodA".equals(i.getContent())));
    }

    @Test
    void testPriorityOrdering() {
        // Arrange
        List<ContextItem> items = new ArrayList<>();
        items.add(new ContextItem(ContextSourceType.CLASS_ANALYSIS, ContextPriority.LOW, "Low1", "key1"));
        items.add(new ContextItem(ContextSourceType.CLASS_ANALYSIS, ContextPriority.HIGH, "High1", "key2"));
        items.add(new ContextItem(ContextSourceType.CLASS_ANALYSIS, ContextPriority.MEDIUM, "Med1", "key3"));

        // Act
        List<ContextItem> ranked = pipelineService.rankByPriority(items);

        // Assert
        assertEquals(ContextPriority.HIGH, ranked.get(0).getPriority());
        assertEquals(ContextPriority.MEDIUM, ranked.get(1).getPriority());
        assertEquals(ContextPriority.LOW, ranked.get(2).getPriority());
    }

    @Test
    void testFilterItemsLimitsPerSource() {
        // Arrange
        List<ContextItem> items = new ArrayList<>();
        // Add 20 items of the same source type
        for (int i = 0; i < 20; i++) {
            items.add(new ContextItem(ContextSourceType.CLASS_ANALYSIS, ContextPriority.MEDIUM,
                    "Class" + i, "key-class-" + i));
        }

        // Act
        List<ContextItem> filtered = pipelineService.filterItems(items);

        // Assert
        assertTrue(filtered.size() <= 15); // MAX_ITEMS_PER_SOURCE
        assertTrue(filtered.size() > 0);
    }

    @Test
    void testEmptyWorkflow() {
        // Arrange
        when(contextBuilderService.buildContext(any())).thenReturn(createMockRepositoryContext());
        when(developmentContextService.createDevelopmentContext(any())).thenReturn(createMockDevelopmentContext());
        when(promptContextService.createPromptContext(any())).thenReturn(createMockPromptContext());

        // Act
        ContextPackage result = pipelineService.buildContextPipeline(
                "", "analysis", "test-repo", "main", "Test task");

        // Assert
        assertNotNull(result);
        assertEquals("", result.getWorkflowSummary());
    }

    @Test
    void testExtractRepositoryContextItems() {
        // Arrange
        RepositoryContext ctx = createMockRepositoryContext();

        // Act
        List<ContextItem> items = pipelineService.extractRepositoryContextItems(ctx);

        // Assert
        assertFalse(items.isEmpty());
        assertTrue(items.stream().anyMatch(i -> i.getSourceType() == ContextSourceType.REPOSITORY_SUMMARY));
    }

    @Test
    void testExtractDevelopmentContextItemsWithNull() {
        // Act
        List<ContextItem> items = pipelineService.extractDevelopmentContextItems(null);

        // Assert
        assertTrue(items.isEmpty());
    }

    @Test
    void testExtractConventionItems() {
        // Arrange
        RepositoryConventionResponse response = createMockConventions();

        // Act
        List<ContextItem> items = pipelineService.extractConventionItems(response);

        // Assert
        assertFalse(items.isEmpty());
        assertTrue(items.stream().anyMatch(i -> i.getContent().contains("Class Naming")));
    }

    @Test
    void testExtractHealthItems() {
        // Arrange
        RepositoryHealthResponse response = createMockHealth();

        // Act
        List<ContextItem> items = pipelineService.extractHealthItems(response);

        // Assert
        assertFalse(items.isEmpty());
    }

    @Test
    void testExtractImpactAnalysisItems() {
        // Arrange
        ImpactAnalysisResponse response = createMockImpact();

        // Act
        List<ContextItem> items = pipelineService.extractImpactAnalysisItems(response);

        // Assert
        assertFalse(items.isEmpty());
    }

    @Test
    void testExtractArchitectureInsightsItems() {
        // Arrange
        ArchitectureInsightsResponse response = createMockArchitectureInsights();

        // Act
        List<ContextItem> items = pipelineService.extractArchitectureInsightsItems(response);

        // Assert
        assertFalse(items.isEmpty());
    }

    // --- Helper methods ---

    private RepositoryContext createMockRepositoryContext() {
        RepositoryContext ctx = new RepositoryContext();
        ctx.setTask("Test task");
        ctx.setRepositoryName("test-repo");
        ctx.setBranch("main");

        RepositorySummaryResponse summary = new RepositorySummaryResponse();
        summary.setRepositoryName("test-repo");
        summary.setBranch("main");
        summary.setStatus("active");
        summary.setFileCount(100);
        summary.setClassCount(50);
        summary.setCommitCount(200);
        ctx.setRepositorySummary(summary);

        RepositoryStatsResponse stats = new RepositoryStatsResponse();
        stats.setFileCount(100);
        stats.setTotalLinesOfCode(10000);
        stats.setClassCount(50);
        stats.setMethodCount(200);
        ctx.setRepositoryStatistics(stats);

        List<ClassInfo> classes = new ArrayList<>();
        ClassInfo classInfo = new ClassInfo();
        classInfo.setClassName("UserController");
        classInfo.setPackageName("com.example.controller");
        classInfo.setClassType(ClassType.CLASS);
        classes.add(classInfo);
        ctx.setClasses(classes);

        return ctx;
    }

    private DevelopmentContext createMockDevelopmentContext() {
        DevelopmentContext ctx = new DevelopmentContext();
        ctx.setTask("Add pagination to UserController");
        ctx.setRepositoryName("test-repo");
        ctx.setBranch("main");
        return ctx;
    }

    private PromptContext createMockPromptContext() {
        PromptContext ctx = new PromptContext();
        ctx.setTask("Add pagination to UserController");
        ctx.setRepositoryName("test-repo");
        ctx.setBranch("main");

        List<String> packages = new ArrayList<>();
        packages.add("com.example.controller");
        packages.add("com.example.service");
        ctx.setRelevantPackages(packages);

        PromptContext.RepositoryConventionsInfo conventions = new PromptContext.RepositoryConventionsInfo();
        conventions.setNamingConventions("CamelCase");
        conventions.setBuildTool("Maven");
        conventions.setJavaVersion("21");
        ctx.setRepositoryConventions(conventions);

        return ctx;
    }

    private ArchitectureInsightsResponse createMockArchitectureInsights() {
        ArchitectureInsightsResponse response = new ArchitectureInsightsResponse();
        response.setRepositoryName("test-repo");
        response.setArchitecturalStyle("Layered Architecture");
        response.setRepositoryOverview("Repository with layered structure");
        response.setDetectedLayers(Arrays.asList("Controller", "Service", "Repository"));
        response.setArchitecturalStrengths(Arrays.asList("Layered Architecture"));
        response.setDependencyFlow("Controller -> Service -> Repository");
        return response;
    }

    private RepositoryConventionResponse createMockConventions() {
        RepositoryConventionResponse response = new RepositoryConventionResponse();
        response.setRepositoryName("test-repo");

        RepositoryConventionResponse.NamingConventions naming = new RepositoryConventionResponse.NamingConventions();
        naming.setClassNamingConvention("PascalCase");
        naming.setMethodNamingConvention("camelCase");
        naming.setPackageNamingConvention("lowercase.dot.separated");
        response.setNamingConventions(naming);

        RepositoryConventionResponse.TestingConventions testing = new RepositoryConventionResponse.TestingConventions();
        testing.setTestFramework("JUnit 5");
        response.setTestingConventions(testing);

        return response;
    }

    private RepositoryHealthResponse createMockHealth() {
        RepositoryHealthResponse response = new RepositoryHealthResponse();
        response.setRepositoryName("test-repo");
        response.setMaintainabilityRating("Good");
        response.setPotentialRisks(Arrays.asList("High coupling detected"));
        response.setObservations(Arrays.asList("Well structured codebase"));
        return response;
    }

    private ImpactAnalysisResponse createMockImpact() {
        ImpactAnalysisResponse response = new ImpactAnalysisResponse();
        response.setOriginalTask("Add pagination");
        response.setPrimaryTargets(Arrays.asList("UserController"));
        response.setEstimatedImplementationScope(ScopeLevel.MEDIUM);
        response.setEstimatedTestingScope(ScopeLevel.SMALL);

        List<ImpactAnalysisResponse.ImpactedComponent> components = new ArrayList<>();
        components.add(new ImpactAnalysisResponse.ImpactedComponent(
                "UserService", "Service", "Will need pagination logic"));
        response.setDirectlyAffectedComponents(components);

        List<ImpactAnalysisResponse.RiskItem> risks = new ArrayList<>();
        risks.add(new ImpactAnalysisResponse.RiskItem(
                "Large result sets", RiskLevel.MEDIUM, "Use pagination limits"));
        response.setPotentialRisks(risks);

        return response;
    }
}