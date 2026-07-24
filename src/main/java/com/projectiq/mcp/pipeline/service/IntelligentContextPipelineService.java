package com.projectiq.mcp.pipeline.service;

import com.projectiq.mcp.analysis.dto.ArchitectureInsightsResponse;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse.ImpactedComponent;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse.RiskItem;
import com.projectiq.mcp.analysis.dto.RepositoryConventionResponse;
import com.projectiq.mcp.analysis.dto.RepositoryHealthResponse;
import com.projectiq.mcp.analysis.service.ArchitectureInsightsService;
import com.projectiq.mcp.analysis.service.ImpactAnalysisService;
import com.projectiq.mcp.analysis.service.RepositoryConventionAnalyzerService;
import com.projectiq.mcp.analysis.service.RepositoryHealthService;
import com.projectiq.mcp.client.dto.*;
import com.projectiq.mcp.client.service.DevelopmentContextService;
import com.projectiq.mcp.client.service.PromptContextService;
import com.projectiq.mcp.client.service.RepositoryContextBuilderService;
import com.projectiq.mcp.pipeline.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service that implements the Intelligent Context Pipeline.
 * <p>
 * Collects workflow context by gathering repository intelligence from all
 * existing analysis services, removes duplicate information, ranks context
 * by relevance priority, filters unnecessary data, and produces one
 * optimized, AI-ready context package.
 * </p>
 *
 * <p>All operations are deterministic. No LLM integration or token counting
 * is performed. No persistent storage or repository modification occurs.</p>
 */
@Service
public class IntelligentContextPipelineService {

    private static final Logger logger = LoggerFactory.getLogger(IntelligentContextPipelineService.class);

    private static final int MAX_CONTENT_LENGTH_PER_ITEM = 500;

    // Maximum items per source type to prevent oversized collections
    private static final int MAX_ITEMS_PER_SOURCE = 15;

    private final RepositoryContextBuilderService contextBuilderService;
    private final DevelopmentContextService developmentContextService;
    private final PromptContextService promptContextService;
    private final ArchitectureInsightsService architectureInsightsService;
    private final RepositoryConventionAnalyzerService conventionAnalyzerService;
    private final RepositoryHealthService repositoryHealthService;
    private final ImpactAnalysisService impactAnalysisService;

    public IntelligentContextPipelineService(
            RepositoryContextBuilderService contextBuilderService,
            DevelopmentContextService developmentContextService,
            PromptContextService promptContextService,
            ArchitectureInsightsService architectureInsightsService,
            RepositoryConventionAnalyzerService conventionAnalyzerService,
            RepositoryHealthService repositoryHealthService,
            ImpactAnalysisService impactAnalysisService) {
        this.contextBuilderService = contextBuilderService;
        this.developmentContextService = developmentContextService;
        this.promptContextService = promptContextService;
        this.architectureInsightsService = architectureInsightsService;
        this.conventionAnalyzerService = conventionAnalyzerService;
        this.repositoryHealthService = repositoryHealthService;
        this.impactAnalysisService = impactAnalysisService;
    }

    /**
     * Builds an optimized context package for the given workflow information.
     *
     * @param workflowSummary a summary of the workflow being executed
     * @param workflowType    the type of workflow (e.g., "analysis", "implementation")
     * @param repositoryName  the repository name to gather context for
     * @param branch          the git branch (optional, defaults to "main")
     * @param taskDescription the specific development task description
     * @return a fully assembled, prioritized, and deduplicated ContextPackage
     */
    public ContextPackage buildContextPipeline(
            String workflowSummary,
            String workflowType,
            String repositoryName,
            String branch,
            String taskDescription) {

        long startTime = System.currentTimeMillis();
        logger.info("Building context pipeline for workflow: {} type: {} repo: {}",
                workflowSummary, workflowType, repositoryName);

        ContextPackage contextPackage = new ContextPackage();
        contextPackage.setWorkflowSummary(workflowSummary);

        String effectiveBranch = (branch != null && !branch.isEmpty()) ? branch : "main";

        // Collection of raw context items before filtering
        List<ContextItem> allItems = new ArrayList<>();

        // --- Phase 1: Gather context from all sources ---

        // 1. Gather repository intelligence via RepositoryContextBuilderService
        RepositoryContext repoContext = gatherRepositoryContext(taskDescription, repositoryName, effectiveBranch);
        if (repoContext != null) {
            allItems.addAll(extractRepositoryContextItems(repoContext));
        } else {
            contextPackage.addWarning("Repository context not available");
        }

        // 2. Gather development context
        DevelopmentContext devContext = gatherDevelopmentContext(taskDescription, repositoryName, effectiveBranch, repoContext);
        if (devContext != null) {
            allItems.addAll(extractDevelopmentContextItems(devContext));
        } else {
            contextPackage.addWarning("Development context not available");
        }

        // 3. Gather prompt context
        PromptContext promptContext = gatherPromptContext(devContext);
        if (promptContext != null) {
            allItems.addAll(extractPromptContextItems(promptContext));
        }

        // 4. Gather architecture insights
        try {
            ArchitectureInsightsResponse archInsights = architectureInsightsService.analyzeArchitecture(repositoryName, effectiveBranch);
            if (archInsights != null) {
                allItems.addAll(extractArchitectureInsightsItems(archInsights));
            }
        } catch (Exception e) {
            logger.warn("Failed to gather architecture insights: {}", e.getMessage());
            contextPackage.addWarning("Architecture insights unavailable: " + e.getMessage());
        }

        // 5. Gather repository conventions
        try {
            RepositoryConventionResponse conventions = conventionAnalyzerService.analyzeConventions(repositoryName, effectiveBranch);
            if (conventions != null) {
                allItems.addAll(extractConventionItems(conventions));
            }
        } catch (Exception e) {
            logger.warn("Failed to gather repository conventions: {}", e.getMessage());
            contextPackage.addWarning("Repository conventions unavailable: " + e.getMessage());
        }

        // 6. Gather repository health
        try {
            RepositoryHealthResponse health = repositoryHealthService.analyzeHealth(repositoryName, effectiveBranch);
            if (health != null) {
                allItems.addAll(extractHealthItems(health));
            }
        } catch (Exception e) {
            logger.warn("Failed to gather repository health: {}", e.getMessage());
            contextPackage.addWarning("Repository health data unavailable: " + e.getMessage());
        }

        // 7. Gather impact analysis
        try {
            ImpactAnalysisResponse impact = impactAnalysisService.analyzeImpact(taskDescription, repositoryName, effectiveBranch);
            if (impact != null) {
                allItems.addAll(extractImpactAnalysisItems(impact));
            }
        } catch (Exception e) {
            logger.warn("Failed to gather impact analysis: {}", e.getMessage());
            contextPackage.addWarning("Impact analysis unavailable: " + e.getMessage());
        }

        // --- Phase 2: Remove duplicates ---
        List<ContextItem> deduplicatedItems = removeDuplicates(allItems);
        int duplicatesRemoved = allItems.size() - deduplicatedItems.size();

        // --- Phase 3: Rank by priority ---
        List<ContextItem> prioritizedItems = rankByPriority(deduplicatedItems);

        // --- Phase 4: Filter ---
        List<ContextItem> filteredItems = filterItems(prioritizedItems);

        // --- Phase 5: Assemble into context package ---
        assembleContextPackage(contextPackage, filteredItems, repoContext, devContext, promptContext);

        // Set metadata
        long elapsed = System.currentTimeMillis() - startTime;
        contextPackage.setProcessingTimeMillis(elapsed);
        contextPackage.setTotalContextItems(filteredItems.size());
        contextPackage.setHighPriorityCount((int) filteredItems.stream()
                .filter(i -> i.getPriority() == ContextPriority.HIGH).count());
        contextPackage.setMediumPriorityCount((int) filteredItems.stream()
                .filter(i -> i.getPriority() == ContextPriority.MEDIUM).count());
        contextPackage.setLowPriorityCount((int) filteredItems.stream()
                .filter(i -> i.getPriority() == ContextPriority.LOW).count());

        if (duplicatesRemoved > 0) {
            contextPackage.addWarning("Removed " + duplicatesRemoved + " duplicate context items");
        }

        logger.info("Context pipeline complete: {} items ({} high, {} med, {} low), {} duplicates removed, {}ms",
                filteredItems.size(), contextPackage.getHighPriorityCount(),
                contextPackage.getMediumPriorityCount(), contextPackage.getLowPriorityCount(),
                duplicatesRemoved, elapsed);

        return contextPackage;
    }

    /**
     * Gathers repository context from the RepositoryContextBuilderService.
     */
    private RepositoryContext gatherRepositoryContext(String task, String repositoryName, String branch) {
        try {
            BuildContextRequest request = new BuildContextRequest();
            request.setTask(task);
            request.setRepositoryName(repositoryName);
            request.setBranch(branch);
            return contextBuilderService.buildContext(request);
        } catch (Exception e) {
            logger.warn("Failed to build repository context: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Gathers development context.
     */
    private DevelopmentContext gatherDevelopmentContext(
            String task, String repositoryName, String branch, RepositoryContext repoContext) {
        if (repoContext == null) {
            return null;
        }
        try {
            BuildContextRequest request = new BuildContextRequest();
            request.setTask(task);
            request.setRepositoryName(repositoryName);
            request.setBranch(branch);
            return developmentContextService.createDevelopmentContext(request);
        } catch (Exception e) {
            logger.warn("Failed to build development context: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Gathers prompt context.
     */
    private PromptContext gatherPromptContext(DevelopmentContext devContext) {
        if (devContext == null) {
            return null;
        }
        try {
            return promptContextService.createPromptContext(devContext);
        } catch (Exception e) {
            logger.warn("Failed to build prompt context: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extracts context items from a RepositoryContext.
     */
    List<ContextItem> extractRepositoryContextItems(RepositoryContext ctx) {
        List<ContextItem> items = new ArrayList<>();

        // Repository summary - HIGH priority
        if (ctx.getRepositorySummary() != null) {
            var summary = ctx.getRepositorySummary();
            String content = "Repository: " + summary.getRepositoryName()
                    + " | Branch: " + summary.getBranch()
                    + " | Status: " + summary.getStatus()
                    + " | Files: " + summary.getFileCount()
                    + " | Classes: " + summary.getClassCount()
                    + " | Commits: " + summary.getCommitCount();
            items.add(new ContextItem(ContextSourceType.REPOSITORY_SUMMARY, ContextPriority.HIGH,
                    content, "repo-summary"));
        }

        // Repository statistics - MEDIUM priority
        if (ctx.getRepositoryStatistics() != null) {
            var stats = ctx.getRepositoryStatistics();
            String content = "Stats - Files: " + stats.getFileCount()
                    + " | LOC: " + stats.getTotalLinesOfCode()
                    + " | Classes: " + stats.getClassCount()
                    + " | Methods: " + stats.getMethodCount()
                    + " | Contributors: " + (stats.getContributors() != null ? stats.getContributors().size() : 0);
            items.add(new ContextItem(ContextSourceType.REPOSITORY_STATISTICS, ContextPriority.MEDIUM,
                    content, "repo-stats"));
        }

        // Search results - HIGH priority (task-relevant)
        if (ctx.getSearchResults() != null) {
            for (int i = 0; i < Math.min(ctx.getSearchResults().size(), MAX_ITEMS_PER_SOURCE); i++) {
                SearchResult result = ctx.getSearchResults().get(i);
                String content = "Search: " + truncate(result.getFilePath(), 100)
                        + " | Snippet: " + truncate(result.getSnippet(), 150)
                        + " | Line: " + result.getLineNumber();
                String key = "search-" + result.getFilePath() + "-" + result.getLineNumber();
                items.add(new ContextItem(ContextSourceType.SEARCH_RESULTS, ContextPriority.HIGH, content, key));
            }
        }

        // Spring components - HIGH priority
        if (ctx.getSpringComponents() != null) {
            for (int i = 0; i < Math.min(ctx.getSpringComponents().size(), MAX_ITEMS_PER_SOURCE); i++) {
                SpringComponentInfo comp = ctx.getSpringComponents().get(i);
                String content = "Component: " + truncate(comp.getClassName(), 80)
                        + " | Type: " + comp.getComponentType()
                        + " | Name: " + comp.getName();
                String key = "spring-" + comp.getClassName();
                items.add(new ContextItem(ContextSourceType.SPRING_COMPONENTS, ContextPriority.HIGH, content, key));
            }
        }

        // REST APIs - HIGH priority
        if (ctx.getRestApis() != null) {
            for (int i = 0; i < Math.min(ctx.getRestApis().size(), MAX_ITEMS_PER_SOURCE); i++) {
                RestEndpointInfo api = ctx.getRestApis().get(i);
                String content = "API: " + api.getHttpMethod() + " " + truncate(api.getEndpointPath(), 100)
                        + " | Controller: " + truncate(api.getControllerName(), 60);
                String key = "api-" + api.getHttpMethod() + "-" + api.getEndpointPath();
                items.add(new ContextItem(ContextSourceType.REST_APIS, ContextPriority.HIGH, content, key));
            }
        }

        // Classes - HIGH priority
        if (ctx.getClasses() != null) {
            for (int i = 0; i < Math.min(ctx.getClasses().size(), MAX_ITEMS_PER_SOURCE); i++) {
                ClassInfo cls = ctx.getClasses().get(i);
                String content = "Class: " + truncate(cls.getClassName(), 80)
                        + " | Package: " + truncate(cls.getPackageName(), 60)
                        + " | Type: " + cls.getClassType();
                String key = "class-" + cls.getClassName();
                items.add(new ContextItem(ContextSourceType.CLASS_ANALYSIS, ContextPriority.HIGH, content, key));
            }
        }

        // Methods - MEDIUM priority
        if (ctx.getMethods() != null) {
            for (int i = 0; i < Math.min(ctx.getMethods().size(), MAX_ITEMS_PER_SOURCE); i++) {
                MethodInfo method = ctx.getMethods().get(i);
                String content = "Method: " + truncate(method.getMethodName(), 60)
                        + " | Class: " + truncate(method.getDeclaringClass(), 60)
                        + " | Params: " + (method.getParameters() != null ? method.getParameters().size() : 0);
                String key = "method-" + method.getDeclaringClass() + "-" + method.getMethodName();
                items.add(new ContextItem(ContextSourceType.METHOD_ANALYSIS, ContextPriority.MEDIUM, content, key));
            }
        }

        // Related files - MEDIUM priority
        if (ctx.getRelatedFiles() != null) {
            for (int i = 0; i < Math.min(ctx.getRelatedFiles().size(), MAX_ITEMS_PER_SOURCE); i++) {
                RelatedFile file = ctx.getRelatedFiles().get(i);
                String content = "Related: " + truncate(file.getFilePath(), 120)
                        + " | Relation: " + file.getRelationshipType();
                String key = "related-" + file.getFilePath();
                items.add(new ContextItem(ContextSourceType.RELATED_FILES, ContextPriority.MEDIUM, content, key));
            }
        }

        // Dependencies - MEDIUM priority
        if (ctx.getDependencies() != null) {
            for (int i = 0; i < Math.min(ctx.getDependencies().size(), MAX_ITEMS_PER_SOURCE); i++) {
                DependencyInfo dep = ctx.getDependencies().get(i);
                String content = "Dep: " + truncate(dep.getGroupId(), 40) + ":"
                        + truncate(dep.getArtifactId(), 40)
                        + " | Scope: " + dep.getScope();
                String key = "dep-" + dep.getGroupId() + "-" + dep.getArtifactId() + "-" + dep.getScope();
                items.add(new ContextItem(ContextSourceType.DEPENDENCY_ANALYSIS, ContextPriority.MEDIUM, content, key));
            }
        }

        // Errors as context (for awareness)
        if (ctx.hasErrors()) {
            for (ContextBuildError error : ctx.getErrors()) {
                String content = "Context Error: [" + error.getEndpoint() + "] "
                        + error.getErrorType() + ": " + error.getMessage();
                String key = "ctx-error-" + error.getEndpoint() + "-" + error.getErrorType();
                items.add(new ContextItem(ContextSourceType.REPOSITORY_CONTEXT, ContextPriority.LOW, content, key));
            }
        }

        return items;
    }

    /**
     * Extracts context items from a DevelopmentContext.
     */
    List<ContextItem> extractDevelopmentContextItems(DevelopmentContext ctx) {
        List<ContextItem> items = new ArrayList<>();

        if (ctx == null) {
            return items;
        }

        // Development context includes task summary - HIGH priority
        String devTaskInfo = "Dev Task: " + truncate(ctx.getTask(), 200)
                + " | Repo: " + ctx.getRepositoryName()
                + " | Branch: " + ctx.getBranch();
        items.add(new ContextItem(ContextSourceType.DEVELOPMENT_CONTEXT, ContextPriority.HIGH,
                devTaskInfo, "dev-task-summary"));

        // Relevant classes from development context - HIGH priority
        if (ctx.getRelevantClasses() != null) {
            for (int i = 0; i < Math.min(ctx.getRelevantClasses().size(), MAX_ITEMS_PER_SOURCE); i++) {
                ClassInfo cls = ctx.getRelevantClasses().get(i);
                String content = "Relevant Class: " + truncate(cls.getClassName(), 80)
                        + " | Package: " + truncate(cls.getPackageName(), 60);
                String key = "dev-class-" + cls.getClassName();
                items.add(new ContextItem(ContextSourceType.DEVELOPMENT_CONTEXT, ContextPriority.HIGH, content, key));
            }
        }

        // Relevant methods from development context - MEDIUM priority
        if (ctx.getRelevantMethods() != null) {
            for (int i = 0; i < Math.min(ctx.getRelevantMethods().size(), MAX_ITEMS_PER_SOURCE); i++) {
                MethodInfo method = ctx.getRelevantMethods().get(i);
                String content = "Relevant Method: " + truncate(method.getMethodName(), 60)
                        + " | Class: " + truncate(method.getDeclaringClass(), 60);
                String key = "dev-method-" + method.getDeclaringClass() + "-" + method.getMethodName();
                items.add(new ContextItem(ContextSourceType.DEVELOPMENT_CONTEXT, ContextPriority.MEDIUM, content, key));
            }
        }

        return items;
    }

    /**
     * Extracts context items from a PromptContext.
     */
    List<ContextItem> extractPromptContextItems(PromptContext ctx) {
        List<ContextItem> items = new ArrayList<>();

        if (ctx == null) {
            return items;
        }

        // Relevant packages - HIGH priority
        if (ctx.getRelevantPackages() != null && !ctx.getRelevantPackages().isEmpty()) {
            String packages = String.join(", ", ctx.getRelevantPackages());
            items.add(new ContextItem(ContextSourceType.PROMPT_CONTEXT, ContextPriority.HIGH,
                    "Relevant Packages: " + truncate(packages, MAX_CONTENT_LENGTH_PER_ITEM),
                    "prompt-packages"));
        }

        // Repository conventions from prompt context - MEDIUM priority
        if (ctx.getRepositoryConventions() != null) {
            var conventions = ctx.getRepositoryConventions();
            if (conventions.getNamingConventions() != null) {
                items.add(new ContextItem(ContextSourceType.PROMPT_CONTEXT, ContextPriority.MEDIUM,
                        "Naming: " + conventions.getNamingConventions(), "prompt-naming"));
            }
            if (conventions.getBuildTool() != null) {
                items.add(new ContextItem(ContextSourceType.PROMPT_CONTEXT, ContextPriority.MEDIUM,
                        "Build: " + conventions.getBuildTool(), "prompt-build"));
            }
            if (conventions.getJavaVersion() != null) {
                items.add(new ContextItem(ContextSourceType.PROMPT_CONTEXT, ContextPriority.MEDIUM,
                        "Java: " + conventions.getJavaVersion(), "prompt-java"));
            }
        }

        return items;
    }

    /**
     * Extracts context items from ArchitectureInsightsResponse.
     */
    List<ContextItem> extractArchitectureInsightsItems(ArchitectureInsightsResponse response) {
        List<ContextItem> items = new ArrayList<>();

        if (response == null) {
            return items;
        }

        // Architectural style - HIGH priority
        if (response.getArchitecturalStyle() != null) {
            items.add(new ContextItem(ContextSourceType.ARCHITECTURE_INSIGHTS, ContextPriority.HIGH,
                    "Architecture Style: " + response.getArchitecturalStyle(),
                    "arch-style"));
        }

        // Repository overview - HIGH priority
        if (response.getRepositoryOverview() != null) {
            items.add(new ContextItem(ContextSourceType.ARCHITECTURE_INSIGHTS, ContextPriority.HIGH,
                    "Repo Overview: " + truncate(response.getRepositoryOverview(), MAX_CONTENT_LENGTH_PER_ITEM),
                    "arch-overview"));
        }

        // Detected layers - MEDIUM priority
        if (response.getDetectedLayers() != null && !response.getDetectedLayers().isEmpty()) {
            String layers = String.join(", ", response.getDetectedLayers());
            items.add(new ContextItem(ContextSourceType.ARCHITECTURE_INSIGHTS, ContextPriority.MEDIUM,
                    "Detected Layers: " + layers, "arch-layers"));
        }

        // Architectural patterns - MEDIUM priority
        if (response.getArchitecturalStrengths() != null && !response.getArchitecturalStrengths().isEmpty()) {
            for (String pattern : response.getArchitecturalStrengths()) {
                String key = "arch-pattern-" + pattern.hashCode();
                items.add(new ContextItem(ContextSourceType.ARCHITECTURE_INSIGHTS, ContextPriority.MEDIUM,
                        "Pattern: " + pattern, key));
            }
        }

        // Potential concerns - MEDIUM priority
        if (response.getPotentialConcerns() != null && !response.getPotentialConcerns().isEmpty()) {
            for (String concern : response.getPotentialConcerns()) {
                String key = "arch-concern-" + concern.hashCode();
                items.add(new ContextItem(ContextSourceType.ARCHITECTURE_INSIGHTS, ContextPriority.MEDIUM,
                        "Concern: " + concern, key));
            }
        }

        // Dependency flow - LOW priority
        if (response.getDependencyFlow() != null) {
            items.add(new ContextItem(ContextSourceType.ARCHITECTURE_INSIGHTS, ContextPriority.LOW,
                    "Dependency Flow: " + response.getDependencyFlow(), "arch-dep-flow"));
        }

        return items;
    }

    /**
     * Extracts context items from RepositoryConventionResponse.
     */
    List<ContextItem> extractConventionItems(RepositoryConventionResponse response) {
        List<ContextItem> items = new ArrayList<>();

        if (response == null) {
            return items;
        }

        // Naming conventions - HIGH priority
        if (response.getNamingConventions() != null) {
            var naming = response.getNamingConventions();
            if (naming.getClassNamingConvention() != null) {
                items.add(new ContextItem(ContextSourceType.REPOSITORY_CONVENTIONS, ContextPriority.HIGH,
                        "Class Naming: " + naming.getClassNamingConvention(), "conv-class-naming"));
            }
            if (naming.getMethodNamingConvention() != null) {
                items.add(new ContextItem(ContextSourceType.REPOSITORY_CONVENTIONS, ContextPriority.HIGH,
                        "Method Naming: " + naming.getMethodNamingConvention(), "conv-method-naming"));
            }
            if (naming.getPackageNamingConvention() != null) {
                items.add(new ContextItem(ContextSourceType.REPOSITORY_CONVENTIONS, ContextPriority.MEDIUM,
                        "Package Naming: " + naming.getPackageNamingConvention(), "conv-pkg-naming"));
            }
            if (naming.getTestNamingConvention() != null) {
                items.add(new ContextItem(ContextSourceType.REPOSITORY_CONVENTIONS, ContextPriority.MEDIUM,
                        "Test Naming: " + naming.getTestNamingConvention(), "conv-test-naming"));
            }
        }

        // Testing conventions - MEDIUM priority
        if (response.getTestingConventions() != null) {
            var testing = response.getTestingConventions();
            if (testing.getTestFramework() != null) {
                items.add(new ContextItem(ContextSourceType.REPOSITORY_CONVENTIONS, ContextPriority.MEDIUM,
                        "Test Framework: " + testing.getTestFramework(), "conv-test-framework"));
            }
            if (testing.getTestLocation() != null) {
                items.add(new ContextItem(ContextSourceType.REPOSITORY_CONVENTIONS, ContextPriority.LOW,
                        "Test Location: " + testing.getTestLocation(), "conv-test-location"));
            }
        }

        // Project-specific observations - MEDIUM priority
        if (response.getProjectSpecificObservations() != null && !response.getProjectSpecificObservations().isEmpty()) {
            for (String observation : response.getProjectSpecificObservations()) {
                String key = "conv-obs-" + observation.hashCode();
                items.add(new ContextItem(ContextSourceType.REPOSITORY_CONVENTIONS, ContextPriority.MEDIUM,
                        "Observation: " + truncate(observation, MAX_CONTENT_LENGTH_PER_ITEM), key));
            }
        }

        return items;
    }

    /**
     * Extracts context items from RepositoryHealthResponse.
     */
    List<ContextItem> extractHealthItems(RepositoryHealthResponse response) {
        List<ContextItem> items = new ArrayList<>();

        if (response == null) {
            return items;
        }

        // Maintainability rating - HIGH priority
        if (response.getMaintainabilityRating() != null) {
            items.add(new ContextItem(ContextSourceType.REPOSITORY_HEALTH, ContextPriority.HIGH,
                    "Health: " + response.getMaintainabilityRating(), "repo-health"));
        }

        // Potential risks - HIGH priority
        if (response.getPotentialRisks() != null && !response.getPotentialRisks().isEmpty()) {
            for (String risk : response.getPotentialRisks()) {
                String key = "health-risk-" + risk.hashCode();
                items.add(new ContextItem(ContextSourceType.REPOSITORY_HEALTH, ContextPriority.HIGH,
                        "Issue: " + truncate(risk, MAX_CONTENT_LENGTH_PER_ITEM), key));
            }
        }

        // Observations - MEDIUM priority
        if (response.getObservations() != null && !response.getObservations().isEmpty()) {
            for (String observation : response.getObservations()) {
                String key = "health-obs-" + observation.hashCode();
                items.add(new ContextItem(ContextSourceType.REPOSITORY_HEALTH, ContextPriority.MEDIUM,
                        "Observation: " + truncate(observation, MAX_CONTENT_LENGTH_PER_ITEM), key));
            }
        }

        return items;
    }

    /**
     * Extracts context items from ImpactAnalysisResponse.
     */
    List<ContextItem> extractImpactAnalysisItems(ImpactAnalysisResponse response) {
        List<ContextItem> items = new ArrayList<>();

        if (response == null) {
            return items;
        }

        // Primary targets - HIGH priority
        if (response.getPrimaryTargets() != null && !response.getPrimaryTargets().isEmpty()) {
            for (String target : response.getPrimaryTargets()) {
                String key = "impact-target-" + target.hashCode();
                items.add(new ContextItem(ContextSourceType.IMPACT_ANALYSIS, ContextPriority.HIGH,
                        "Target: " + truncate(target, MAX_CONTENT_LENGTH_PER_ITEM), key));
            }
        }

        // Directly affected components - HIGH priority
        if (response.getDirectlyAffectedComponents() != null && !response.getDirectlyAffectedComponents().isEmpty()) {
            for (ImpactedComponent comp : response.getDirectlyAffectedComponents()) {
                String key = "impact-direct-" + comp.getComponentName().hashCode();
                items.add(new ContextItem(ContextSourceType.IMPACT_ANALYSIS, ContextPriority.HIGH,
                        "Affected: " + truncate(comp.getComponentName(), 80)
                                + " | Type: " + comp.getComponentType()
                                + " | Reason: " + truncate(comp.getImpactReason(), 100), key));
            }
        }

        // Potential risks - HIGH priority
        if (response.getPotentialRisks() != null && !response.getPotentialRisks().isEmpty()) {
            for (RiskItem risk : response.getPotentialRisks()) {
                String key = "impact-risk-" + risk.getDescription().hashCode();
                items.add(new ContextItem(ContextSourceType.IMPACT_ANALYSIS, ContextPriority.HIGH,
                        "Risk: " + truncate(risk.getDescription(), MAX_CONTENT_LENGTH_PER_ITEM)
                                + " | Level: " + risk.getRiskLevel(), key));
            }
        }

        // Implementation scope - MEDIUM priority
        if (response.getEstimatedImplementationScope() != null) {
            items.add(new ContextItem(ContextSourceType.IMPACT_ANALYSIS, ContextPriority.MEDIUM,
                    "Impl Scope: " + response.getEstimatedImplementationScope(), "impact-impl-scope"));
        }

        // Testing scope - MEDIUM priority
        if (response.getEstimatedTestingScope() != null) {
            items.add(new ContextItem(ContextSourceType.IMPACT_ANALYSIS, ContextPriority.MEDIUM,
                    "Test Scope: " + response.getEstimatedTestingScope(), "impact-test-scope"));
        }

        return items;
    }

    /**
     * Removes duplicate context items based on deduplication key.
     * First occurrence wins (preserves initial ordering).
     */
    List<ContextItem> removeDuplicates(List<ContextItem> items) {
        Set<String> seenKeys = new LinkedHashSet<>();
        List<ContextItem> deduplicated = new ArrayList<>();
        for (ContextItem item : items) {
            if (item.getDeduplicationKey() != null && seenKeys.add(item.getDeduplicationKey())) {
                deduplicated.add(item);
            }
        }
        return deduplicated;
    }

    /**
     * Ranks items by priority (HIGH first, then MEDIUM, then LOW)
     * and within same priority by source type name.
     */
    List<ContextItem> rankByPriority(List<ContextItem> items) {
        List<ContextItem> sorted = new ArrayList<>(items);
        Collections.sort(sorted);
        return sorted;
    }

    /**
     * Filters items to remove unnecessary data and limit oversized collections.
     * Preserves deterministic ordering.
     */
    List<ContextItem> filterItems(List<ContextItem> items) {
        List<ContextItem> filtered = new ArrayList<>();

        // Group items by source type to limit per source
        Map<ContextSourceType, List<ContextItem>> grouped = items.stream()
                .collect(Collectors.groupingBy(
                        ContextItem::getSourceType,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        for (Map.Entry<ContextSourceType, List<ContextItem>> entry : grouped.entrySet()) {
            List<ContextItem> sourceItems = entry.getValue();
            // Limit items per source type
            int limit = Math.min(sourceItems.size(), MAX_ITEMS_PER_SOURCE);
            filtered.addAll(sourceItems.subList(0, limit));
        }

        // Re-sort after limiting
        Collections.sort(filtered);
        return filtered;
    }

    /**
     * Assembles the final context package from filtered items and raw contexts.
     */
    private void assembleContextPackage(
            ContextPackage contextPackage,
            List<ContextItem> items,
            RepositoryContext repoContext,
            DevelopmentContext devContext,
            PromptContext promptContext) {

        // Set repository summary
        if (repoContext != null && repoContext.getRepositorySummary() != null) {
            var summary = repoContext.getRepositorySummary();
            contextPackage.setRepositorySummary("Repository: " + summary.getRepositoryName()
                    + " | Branch: " + summary.getBranch()
                    + " | Status: " + summary.getStatus()
                    + " | Files: " + summary.getFileCount()
                    + " | Classes: " + summary.getClassCount());
        } else {
            contextPackage.setRepositorySummary("Not available");
        }

        // Set suggested implementation focus
        if (devContext != null) {
            contextPackage.setSuggestedImplementationFocus("Implement based on: " + truncate(devContext.getTask(), 200));
        }

        // Classify items into context package sections
        for (ContextItem item : items) {
            switch (item.getSourceType()) {
                case CLASS_ANALYSIS:
                    contextPackage.addRelevantClass(item.getContent());
                    break;
                case METHOD_ANALYSIS:
                    contextPackage.addRelevantMethod(item.getContent());
                    break;
                case REST_APIS:
                    contextPackage.addRelatedApi(item.getContent());
                    break;
                case DEPENDENCY_ANALYSIS:
                    contextPackage.addDependency(item.getContent());
                    break;
                case SPRING_COMPONENTS:
                    contextPackage.addConfiguration(item.getContent());
                    break;
                case IMPACT_ANALYSIS:
                    if (item.getContent().startsWith("Risk:")) {
                        contextPackage.addRisk(item.getContent());
                    }
                    break;
                case REPOSITORY_CONVENTIONS:
                    contextPackage.addConvention(item.getContent());
                    break;
                case ARCHITECTURE_INSIGHTS:
                    contextPackage.addArchitectureInsight(item.getContent());
                    break;
                case REPOSITORY_HEALTH:
                    if (item.getContent().startsWith("Issue:")) {
                        contextPackage.addRisk(item.getContent());
                    }
                    break;
                default:
                    // Other items contribute implicitly
                    break;
            }
        }
    }

    /**
     * Truncates a string to the specified maximum length.
     */
    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }
}