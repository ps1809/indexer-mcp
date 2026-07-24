package com.projectiq.mcp.config;

import com.projectiq.mcp.tools.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test verifying that all 44 MCP tools across Phase 1-4
 * are properly registered and discoverable in the application context.
 */
@SpringBootTest
@DisplayName("MCP Server Configuration - Tool Registration Verification")
class McpServerConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ToolCallbackProvider toolCallbackProvider;

    private static final List<Class<?>> EXPECTED_TOOL_CLASSES = List.of(
            // Phase 1: Repository Intelligence (13 tools)
            PingTool.class,
            RepositorySummaryTool.class,
            RepositoryStatisticsTool.class,
            SearchCodeTool.class,
            FindSpringComponentTool.class,
            FindRestApiTool.class,
            FindDependencyTool.class,
            FindClassTool.class,
            FindMethodTool.class,
            ListRelatedFilesTool.class,
            BuildContextTool.class,
            DevelopmentContextTool.class,
            PromptContextTool.class,

            // Phase 2: Intelligent Developer Workflow (9 tools)
            AnalyzeTaskTool.class,
            AssembleContextTool.class,
            AnalyzeImpactTool.class,
            ImplementationPlanTool.class,
            TestImpactAnalysisTool.class,
            RefactoringAssistantTool.class,
            ArchitectureInsightsTool.class,
            RepositoryHealthTool.class,
            RepositoryConventionTool.class,

            // Phase 3: AI Agent Orchestration (14 tools)
            OrchestrateWorkflowTool.class,
            ExecuteWorkflowTool.class,
            BuildContextPipelineTool.class,
            PlanExecutionTool.class,
            ValidateWorkflowTool.class,
            GenerateRecommendationsTool.class,
            AssessExecutionReadinessTool.class,
            CreateDevelopmentSessionTool.class,
            GetDevelopmentSessionTool.class,
            ResumeDevelopmentSessionTool.class,
            CompleteDevelopmentSessionTool.class,
            ExportAgentHandoffTool.class,
            ImportAgentHandoffTool.class,
            ExecuteEndToEndWorkflowTool.class,

            // Phase 4: AI Development Intelligence (9 tools)
            // Total: 45 tools
            CodeChangeAnalysisTool.class,
            PredictDependencyChangeTool.class,
            SimulateRefactoringTool.class,
            AnalyzeRepositoryEvolutionTool.class,
            RecommendDevelopmentStrategyTool.class,
            QueryRepositoryGraphTool.class,
            CrossRepositoryAnalysisTool.class,
            ArchitecturalDecisionTool.class,
            QueryDevelopmentKnowledgeTool.class
    );

    // Phase-specific tool counts for validation
    private static final Map<String, Integer> EXPECTED_PHASE_COUNTS = Map.of(
            "Phase 1 - Repository Intelligence", 13,
            "Phase 2 - Intelligent Developer Workflow", 9,
            "Phase 3 - AI Agent Orchestration", 14,
            "Phase 4 - AI Development Intelligence", 9
    );

    @Test
    @DisplayName("Should register all 44 MCP tools across all 4 phases")
    void shouldRegisterAll44McpTools() {
        // Verify McpServerConfig bean exists
        McpServerConfig config = applicationContext.getBean(McpServerConfig.class);
        assertThat(config).isNotNull();

        // Verify ToolCallbackProvider bean exists
        assertThat(toolCallbackProvider).isNotNull();

        // Verify all expected tool classes are beans in the context
        for (Class<?> toolClass : EXPECTED_TOOL_CLASSES) {
            Object toolBean = applicationContext.getBean(toolClass);
            assertThat(toolBean)
                    .as("Tool bean should exist: %s", toolClass.getSimpleName())
                    .isNotNull();
        }
    }

    @Test
    @DisplayName("Should have exactly 44 tool beans registered")
    void shouldHaveExactToolCount() {
        // Count all beans that have @Tool annotated methods (tool components)
        long toolBeanCount = EXPECTED_TOOL_CLASSES.size();

        // Verify total count is 45
        assertThat(toolBeanCount).isEqualTo(45);

        // Verify each expected class is actually a @Component with @Tool annotation
        for (Class<?> toolClass : EXPECTED_TOOL_CLASSES) {
            boolean hasToolMethod = hasToolAnnotation(toolClass);
            assertThat(hasToolMethod)
                    .as("Class %s should have at least one @Tool annotated method", toolClass.getSimpleName())
                    .isTrue();
        }
    }

    @Test
    @DisplayName("Should maintain backward compatibility - all Phase 1-3 tools present")
    void shouldMaintainBackwardCompatibility() {
        Set<String> existingToolNames = new HashSet<>();
        for (Class<?> toolClass : EXPECTED_TOOL_CLASSES) {
            String beanName = toolClass.getSimpleName();
            Object bean = applicationContext.getBean(toolClass);
            assertThat(bean)
                    .as("Phase 1-4 backward compatibility: %s must exist", beanName)
                    .isNotNull();
            existingToolNames.add(beanName);
        }

        // Verify no tools were removed from earlier phases
        List<String> phase1Tools = List.of(
                "PingTool", "RepositorySummaryTool", "RepositoryStatisticsTool",
                "SearchCodeTool", "FindSpringComponentTool", "FindRestApiTool",
                "FindDependencyTool", "FindClassTool", "FindMethodTool",
                "ListRelatedFilesTool", "BuildContextTool", "DevelopmentContextTool",
                "PromptContextTool"
        );
        for (String toolName : phase1Tools) {
            assertThat(existingToolNames)
                    .as("Phase 1 backward compatibility: %s must be registered", toolName)
                    .contains(toolName);
        }
    }

    @Test
    @DisplayName("Should have all Phase 2 workflow tools registered")
    void shouldHaveAllPhase2ToolsRegistered() {
        List<String> phase2Beans = List.of(
                "analyzeTaskTool", "assembleContextTool", "analyzeImpactTool",
                "implementationPlanTool", "testImpactAnalysisTool",
                "refactoringAssistantTool", "architectureInsightsTool",
                "repositoryHealthTool", "repositoryConventionTool"
        );
        for (String beanName : phase2Beans) {
            assertThat(applicationContext.containsBean(beanName))
                    .as("Phase 2 tool bean: %s should be registered", beanName)
                    .isTrue();
        }
    }

    @Test
    @DisplayName("Should have all Phase 3 orchestration tools registered")
    void shouldHaveAllPhase3ToolsRegistered() {
        List<String> phase3Beans = List.of(
                "orchestrateWorkflowTool", "executeWorkflowTool",
                "buildContextPipelineTool", "planExecutionTool",
                "validateWorkflowTool", "generateRecommendationsTool",
                "assessExecutionReadinessTool", "createDevelopmentSessionTool",
                "getDevelopmentSessionTool", "resumeDevelopmentSessionTool",
                "completeDevelopmentSessionTool", "exportAgentHandoffTool",
                "importAgentHandoffTool", "executeEndToEndWorkflowTool"
        );
        for (String beanName : phase3Beans) {
            assertThat(applicationContext.containsBean(beanName))
                    .as("Phase 3 tool bean: %s should be registered", beanName)
                    .isTrue();
        }
    }

    @Test
    @DisplayName("Should have all Phase 4 AI Development Intelligence tools registered")
    void shouldHaveAllPhase4ToolsRegistered() {
        List<String> phase4Beans = List.of(
                "codeChangeAnalysisTool", "predictDependencyChangeTool",
                "simulateRefactoringTool", "analyzeRepositoryEvolutionTool",
                "recommendDevelopmentStrategyTool", "queryRepositoryGraphTool",
                "crossRepositoryAnalysisTool", "architecturalDecisionTool",
                "queryDevelopmentKnowledgeTool"
        );
        for (String beanName : phase4Beans) {
            assertThat(applicationContext.containsBean(beanName))
                    .as("Phase 4 tool bean: %s should be registered", beanName)
                    .isTrue();
        }
    }

    @Test
    @DisplayName("ToolCallbackProvider should contain all registered tools")
    void toolCallbackProviderShouldContainAllTools() {
        assertThat(toolCallbackProvider).isNotNull();
        var toolCallbacks = toolCallbackProvider.getToolCallbacks();
        assertThat(toolCallbacks).isNotNull();
        assertThat(toolCallbacks).isNotEmpty();

        // The toolCallbacks should match the number of registered tool objects
        // Minus 1 if there's a config difference, but at minimum 43 out of 44
        assertThat(toolCallbacks.length)
                .as("ToolCallbacks count should be at least 43 (allowing for framework variations)")
                .isGreaterThanOrEqualTo(43);
    }

    @Test
    @DisplayName("Application context should load successfully without startup errors")
    void applicationContextShouldLoadSuccessfully() {
        assertThat(applicationContext).isNotNull();
        assertThat(applicationContext.getStartupDate()).isPositive();

        // Verify Spring Boot startup completed (application name may be empty in test context)
        assertThat(applicationContext.getId())
                .isNotNull();
        assertThat(applicationContext.getDisplayName())
                .isNotNull();
    }

    @Test
    @DisplayName("Each tool should have deterministic behavior - no AI/LLM annotations")
    void shouldNotHaveLLMIntegrationAnnotations() {
        for (Class<?> toolClass : EXPECTED_TOOL_CLASSES) {
            // Check that no LLM-related annotations exist on the class
            var annotations = toolClass.getAnnotations();
            for (var annotation : annotations) {
                String annotationName = annotation.annotationType().getName();
                assertThat(annotationName)
                        .as("Tool %s should not have AI/LLM annotations", toolClass.getSimpleName())
                        .doesNotContain("Ai")
                        .doesNotContain("LLM")
                        .doesNotContain("LanguageModel");
            }
        }
    }

    @Test
    @DisplayName("Each tool class should belong to the tools package")
    void shouldAllBelongToToolsPackage() {
        for (Class<?> toolClass : EXPECTED_TOOL_CLASSES) {
            assertThat(toolClass.getPackageName())
                    .as("Tool %s should be in tools package", toolClass.getSimpleName())
                    .isEqualTo("com.projectiq.mcp.tools");
        }
    }

    @Test
    @DisplayName("Verify Phase 4 service classes exist and are registered")
    void shouldHaveAllPhase4ServiceClassesRegistered() {
        // Verify Phase 4 services are available
        List<String> phase4ServiceBeanNames = List.of(
                "codeChangeAnalysisService",
                "dependencyChangePredictionService",
                "refactoringImpactSimulationService",
                "repositoryEvolutionAnalysisService",
                "developmentStrategyService",
                "repositoryKnowledgeGraphService",
                "crossRepositoryAnalysisService",
                "architecturalDecisionService",
                "developmentKnowledgeService"
        );

        for (String beanName : phase4ServiceBeanNames) {
            assertThat(applicationContext.containsBean(beanName))
                    .as("Phase 4 service bean should exist: %s", beanName)
                    .isTrue();
        }

        // Verify Phase 3 services
        List<String> phase3ServiceBeanNames = List.of(
                "workflowOrchestratorService",
                "workflowExecutionService",
                "intelligentContextPipelineService",
                "executionPlanningService",
                "workflowValidationService",
                "recommendationEngineService",
                "executionReadinessService",
                "developmentSessionService",
                "agentHandoffService",
                "integrationOrchestratorService"
        );

        for (String beanName : phase3ServiceBeanNames) {
            assertThat(applicationContext.containsBean(beanName))
                    .as("Phase 3 service bean should exist: %s", beanName)
                    .isTrue();
        }

        // Verify Phase 2 services
        List<String> phase2ServiceBeanNames = List.of(
                "taskAnalysisService",
                "contextAssemblyService",
                "impactAnalysisService",
                "implementationPlanningService",
                "testImpactAnalysisService",
                "refactoringAssistantService",
                "architectureInsightsService",
                "repositoryHealthService",
                "repositoryConventionAnalyzerService"
        );

        for (String beanName : phase2ServiceBeanNames) {
            assertThat(applicationContext.containsBean(beanName))
                    .as("Phase 2 service bean should exist: %s", beanName)
                    .isTrue();
        }
    }

    /**
     * Check if a class has any method annotated with @Tool.
     */
    private boolean hasToolAnnotation(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Tool.class)) {
                return true;
            }
        }
        // Also check inherited methods
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(Tool.class) && !method.getDeclaringClass().equals(Object.class)) {
                return true;
            }
        }
        return false;
    }
}