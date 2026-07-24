package com.projectiq.mcp.analysis.service;

import com.projectiq.mcp.analysis.dto.CodeChangeAnalysisResponse;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse.ImpactedComponent;
import com.projectiq.mcp.analysis.dto.ImplementationPlanningResponse;
import com.projectiq.mcp.analysis.dto.TaskAnalysisResponse;
import com.projectiq.mcp.analysis.dto.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service that performs deterministic Intelligent Code Change Analysis for
 * proposed source code modifications. Analyzes the proposed change, predicts
 * impacted repository components, identifies affected services, APIs, and
 * dependencies, estimates implementation scope, and produces a structured
 * change impact report without modifying any repository code.
 *
 * <p>This service relies on {@link TaskAnalysisService}, {@link ContextAssemblyService},
 * {@link ImpactAnalysisService}, and {@link ImplementationPlanningService} to
 * gather the necessary analysis data. All outputs are deterministic, stable,
 * and free of duplicate entries.</p>
 *
 * <p>This service NEVER generates code, modifies the repository, performs
 * git operations, or uses any AI/LLM reasoning.</p>
 */
@Service
public class CodeChangeAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(CodeChangeAnalysisService.class);

    private final TaskAnalysisService taskAnalysisService;
    private final ContextAssemblyService contextAssemblyService;
    private final ImpactAnalysisService impactAnalysisService;
    private final ImplementationPlanningService implementationPlanningService;

    // --- Patterns for code change analysis ---

    private static final Pattern CONTROLLER_PATTERN = Pattern.compile(
            "\\b([A-Z][a-zA-Z0-9]*(?:Controller|Resource|Endpoint))\\b"
    );

    private static final Pattern SERVICE_PATTERN = Pattern.compile(
            "\\b([A-Z][a-zA-Z0-9]*(?:Service|Manager|Helper|Util|Provider|Factory))\\b"
    );

    private static final Pattern REPOSITORY_PATTERN = Pattern.compile(
            "\\b([A-Z][a-zA-Z0-9]*(?:Repository|Dao|DataAccess|Storage))\\b"
    );

    private static final Pattern ENTITY_PATTERN = Pattern.compile(
            "\\b([A-Z][a-zA-Z0-9]*(?:Entity|Model|Domain|Dto|Vo|Pojo|Bean))\\b"
    );

    private static final Pattern CONFIG_CLASS_PATTERN = Pattern.compile(
            "\\b([A-Z][a-zA-Z0-9]*(?:Config|Configuration|Properties|Settings))\\b"
    );

    private static final Pattern ENDPOINT_PATTERN = Pattern.compile(
            "/(?:api/)?[a-zA-Z0-9/{}._-]+|\\b(endpoint|route|path|uri)\\s+['\"]?([a-zA-Z0-9/{}._-]+)['\"]?"
    );

    private static final Pattern METHOD_PATTERN = Pattern.compile(
            "\\b([a-z][a-zA-Z0-9]*\\(\\s*\\)|[a-z][a-zA-Z0-9]*By[A-Z][a-zA-Z0-9]*)\\b"
    );

    private static final Pattern FILE_PATTERN = Pattern.compile(
            "\\b([a-zA-Z0-9_]+\\.[a-z]+)\\b"
    );

    private static final Pattern PACKAGE_PATTERN = Pattern.compile(
            "\\b([a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]*)+)\\b"
    );

    // --- Deterministic implementation order templates ---

    private static final Map<TaskType, List<String>> IMPLEMENTATION_ORDER_BY_TYPE = buildImplementationOrderByType();

    // --- Deterministic risk templates ---

    private static final Map<TaskType, List<String>> RISKS_BY_TYPE = buildRisksByType();

    // --- Deterministic testing recommendations ---

    private static final Map<TaskType, List<String>> TESTING_RECOMMENDATIONS_BY_TYPE = buildTestingRecommendationsByType();

    public CodeChangeAnalysisService(
            TaskAnalysisService taskAnalysisService,
            ContextAssemblyService contextAssemblyService,
            ImpactAnalysisService impactAnalysisService,
            ImplementationPlanningService implementationPlanningService) {
        this.taskAnalysisService = taskAnalysisService;
        this.contextAssemblyService = contextAssemblyService;
        this.impactAnalysisService = impactAnalysisService;
        this.implementationPlanningService = implementationPlanningService;
    }

    /**
     * Analyzes a proposed code change and produces a comprehensive change
     * impact report. Invokes task analysis, context assembly, impact analysis,
     * and implementation planning to gather the necessary data.
     *
     * @param proposedChange the proposed code change description
     * @param repositoryName the repository name
     * @param branch         the git branch (optional, defaults to "main")
     * @return a structured change impact report
     * @throws IllegalArgumentException if the proposed change is null or empty
     */
    public CodeChangeAnalysisResponse analyzeCodeChange(
            String proposedChange, String repositoryName, String branch) {
        if (proposedChange == null || proposedChange.trim().isEmpty()) {
            throw new IllegalArgumentException("Proposed change description cannot be null or empty");
        }

        logger.info("Analyzing code change: {} in repository: {}", proposedChange, repositoryName);

        String effectiveBranch = (branch != null && !branch.trim().isEmpty()) ? branch.trim() : "main";
        String normalizedChange = proposedChange.trim().toLowerCase();

        // Step 1: Analyze the task
        TaskAnalysisResponse taskAnalysis = taskAnalysisService.analyze(proposedChange);
        TaskType taskType = taskAnalysis.getTaskType();

        // Step 2: Assemble repository context (non-critical)
        try {
            contextAssemblyService.assembleContext(proposedChange.trim(), repositoryName, effectiveBranch);
        } catch (Exception e) {
            logger.warn("Failed to assemble repository context: {}", e.getMessage());
        }

        // Step 3: Analyze impact (non-critical)
        ImpactAnalysisResponse impactAnalysis = null;
        try {
            impactAnalysis = impactAnalysisService.analyzeImpact(proposedChange.trim(), repositoryName, effectiveBranch);
        } catch (Exception e) {
            logger.warn("Failed to analyze impact: {}", e.getMessage());
        }

        // Step 4: Generate implementation plan (non-critical)
        ImplementationPlanningResponse implPlan = null;
        try {
            implPlan = implementationPlanningService.generatePlan(proposedChange.trim(), repositoryName, effectiveBranch);
        } catch (Exception e) {
            logger.warn("Failed to generate implementation plan: {}", e.getMessage());
        }

        // Step 5: Build the change analysis response
        CodeChangeAnalysisResponse response = new CodeChangeAnalysisResponse();

        // Step 6: Generate proposed change summary
        String summary = generateChangeSummary(proposedChange.trim(), taskType, taskAnalysis);
        response.setProposedChangeSummary(summary);

        // Step 7: Identify impacted files
        List<String> impactedFiles = identifyImpactedFiles(proposedChange.trim(), normalizedChange, taskType, impactAnalysis);
        response.setImpactedFiles(impactedFiles);

        // Step 8: Identify impacted classes
        List<String> impactedClasses = identifyImpactedClasses(proposedChange.trim(), normalizedChange, taskType, impactAnalysis);
        response.setImpactedClasses(impactedClasses);

        // Step 9: Identify impacted methods
        List<String> impactedMethods = identifyImpactedMethods(proposedChange.trim(), normalizedChange, taskType);
        response.setImpactedMethods(impactedMethods);

        // Step 10: Identify impacted REST APIs
        List<String> impactedRestApis = identifyImpactedRestApis(proposedChange.trim(), normalizedChange, taskType);
        response.setImpactedRestApis(impactedRestApis);

        // Step 11: Identify dependency changes
        List<String> dependencyChanges = identifyDependencyChanges(normalizedChange, taskType);
        response.setDependencyChanges(dependencyChanges);

        // Step 12: Generate testing recommendations
        List<String> testingRecommendations = generateTestingRecommendations(taskType, normalizedChange, impactedClasses);
        response.setTestingRecommendations(testingRecommendations);

        // Step 13: Generate risk assessment
        List<String> riskAssessment = generateRiskAssessment(taskType, normalizedChange, impactedClasses, impactedFiles);
        response.setRiskAssessment(riskAssessment);

        // Step 14: Generate suggested implementation order
        List<String> implementationOrder = generateImplementationOrder(taskType, normalizedChange, impactedClasses);
        response.setSuggestedImplementationOrder(implementationOrder);

        logger.info("Code change analysis complete: type={}, files={}, classes={}, methods={}, apis={}",
                taskType, impactedFiles.size(), impactedClasses.size(),
                impactedMethods.size(), impactedRestApis.size());

        return response;
    }

    /**
     * Generates a human-readable summary of the proposed change.
     */
    String generateChangeSummary(String proposedChange, TaskType taskType, TaskAnalysisResponse taskAnalysis) {
        StringBuilder summary = new StringBuilder();
        summary.append("Proposed Change: ").append(proposedChange).append(". ");
        summary.append("Change Type: ").append(taskType.getDisplayName()).append(". ");

        if (taskAnalysis.getDetectedEntities() != null && !taskAnalysis.getDetectedEntities().isEmpty()) {
            summary.append("Detected Entities: ")
                    .append(String.join(", ", taskAnalysis.getDetectedEntities()))
                    .append(". ");
        }

        summary.append("This analysis predicts the repository-wide impact of the proposed change ")
                .append("without modifying any code.");

        return summary.toString();
    }

    /**
     * Identifies files that will be impacted by the proposed change.
     */
    List<String> identifyImpactedFiles(
            String proposedChange, String normalizedChange,
            TaskType taskType, ImpactAnalysisResponse impactAnalysis) {
        Set<String> files = new LinkedHashSet<>();

        // Extract file names from the proposed change
        Matcher fileMatcher = FILE_PATTERN.matcher(proposedChange);
        while (fileMatcher.find()) {
            String file = fileMatcher.group(1);
            if (isSourceFile(file)) {
                files.add(file);
            }
        }

        // Derive files from impacted components
        if (impactAnalysis != null) {
            if (impactAnalysis.getDirectlyAffectedComponents() != null) {
                for (ImpactedComponent component : impactAnalysis.getDirectlyAffectedComponents()) {
                    String name = component.getComponentName();
                    String type = component.getComponentType();
                    String derivedFile = deriveFileName(name, type);
                    if (derivedFile != null) {
                        files.add(derivedFile);
                    }
                }
            }
            if (impactAnalysis.getIndirectlyAffectedComponents() != null) {
                for (ImpactedComponent component : impactAnalysis.getIndirectlyAffectedComponents()) {
                    String name = component.getComponentName();
                    String type = component.getComponentType();
                    if ("Class".equals(type) || "Configuration".equals(type) || "DTO".equals(type)) {
                        String derivedFile = deriveFileName(name, type);
                        if (derivedFile != null) {
                            files.add(derivedFile);
                        }
                    }
                }
            }
        }

        // Add type-specific files
        files.addAll(getTypeSpecificFiles(taskType, normalizedChange));

        // If still empty, add default files
        if (files.isEmpty()) {
            files.addAll(getDefaultFiles(taskType));
        }

        return new ArrayList<>(files);
    }

    /**
     * Identifies classes that will be impacted by the proposed change.
     */
    List<String> identifyImpactedClasses(
            String proposedChange, String normalizedChange,
            TaskType taskType, ImpactAnalysisResponse impactAnalysis) {
        Set<String> classes = new LinkedHashSet<>();

        // Extract class names from the proposed change
        addPatternMatches(proposedChange, CONTROLLER_PATTERN, classes);
        addPatternMatches(proposedChange, SERVICE_PATTERN, classes);
        addPatternMatches(proposedChange, REPOSITORY_PATTERN, classes);
        addPatternMatches(proposedChange, ENTITY_PATTERN, classes);
        addPatternMatches(proposedChange, CONFIG_CLASS_PATTERN, classes);

        // Add directly affected components from impact analysis
        if (impactAnalysis != null && impactAnalysis.getDirectlyAffectedComponents() != null) {
            for (ImpactedComponent component : impactAnalysis.getDirectlyAffectedComponents()) {
                String name = component.getComponentName();
                String type = component.getComponentType();
                if ("Class".equals(type) || "Configuration".equals(type)
                        || "DTO".equals(type) || "REST API".equals(type)) {
                    if (name.startsWith("Endpoint: ")) {
                        classes.add("Controller (" + name.substring("Endpoint: ".length()) + ")");
                    } else {
                        classes.add(name + " (" + type + ")");
                    }
                }
            }
        }

        // Add indirectly affected components that are classes
        if (impactAnalysis != null && impactAnalysis.getIndirectlyAffectedComponents() != null) {
            for (ImpactedComponent component : impactAnalysis.getIndirectlyAffectedComponents()) {
                String name = component.getComponentName();
                String type = component.getComponentType();
                if ("Class".equals(type) || "DTO".equals(type)) {
                    classes.add(name + " (" + type + ")");
                }
            }
        }

        // Add primary targets from impact analysis
        if (impactAnalysis != null && impactAnalysis.getPrimaryTargets() != null) {
            for (String target : impactAnalysis.getPrimaryTargets()) {
                if (!target.contains("(") && !target.contains("Endpoint:")) {
                    classes.add(target + " (Target)");
                }
            }
        }

        // If still empty, derive default based on task type
        if (classes.isEmpty()) {
            classes.addAll(getDefaultImpactedClasses(taskType, normalizedChange));
        }

        return new ArrayList<>(classes);
    }

    /**
     * Identifies methods that will be impacted by the proposed change.
     */
    List<String> identifyImpactedMethods(
            String proposedChange, String normalizedChange, TaskType taskType) {
        Set<String> methods = new LinkedHashSet<>();

        // Extract method names from the proposed change
        Matcher methodMatcher = METHOD_PATTERN.matcher(proposedChange);
        while (methodMatcher.find()) {
            String method = methodMatcher.group().trim();
            if (method.length() >= 3) {
                methods.add(method);
            }
        }

        // Add type-specific methods
        methods.addAll(getTypeSpecificMethods(taskType, normalizedChange));

        return new ArrayList<>(methods);
    }

    /**
     * Identifies REST APIs that will be impacted by the proposed change.
     */
    List<String> identifyImpactedRestApis(
            String proposedChange, String normalizedChange, TaskType taskType) {
        Set<String> apis = new LinkedHashSet<>();

        // Extract endpoints from the proposed change
        Matcher endpointMatcher = ENDPOINT_PATTERN.matcher(proposedChange);
        while (endpointMatcher.find()) {
            String group2 = endpointMatcher.group(2);
            if (group2 != null) {
                apis.add(group2);
            } else {
                String match = endpointMatcher.group(1);
                if (match != null && match.startsWith("/")) {
                    apis.add(match);
                }
            }
        }

        // Add type-specific APIs
        apis.addAll(getTypeSpecificApis(taskType, normalizedChange));

        return new ArrayList<>(apis);
    }

    /**
     * Identifies dependency changes that will be required.
     */
    List<String> identifyDependencyChanges(String normalizedChange, TaskType taskType) {
        Set<String> changes = new LinkedHashSet<>();

        // Check for dependency-related keywords
        if (containsAny(normalizedChange, "dependency", "maven", "gradle", "pom",
                "library", "import", "version", "upgrade", "downgrade")) {
            changes.add("External library dependencies may need version updates");
            changes.add("Transitive dependencies may introduce compatibility issues");
        }

        // Task-type specific dependency impacts
        switch (taskType) {
            case NEW_FEATURE:
                if (!containsAny(normalizedChange, "simple", "minor", "cosmetic", "trivial")) {
                    changes.add("New dependencies may be required for the feature implementation");
                }
                changes.add("Internal module dependencies may need coordination");
                break;
            case BUG_FIX:
                changes.add("Bug fix may require dependency version rollback or updates");
                changes.add("Fix may affect backward compatibility with existing APIs");
                break;
            case REFACTORING:
                changes.add("Refactoring may change internal APIs affecting dependent modules");
                changes.add("Dependency injection wiring may need updates");
                break;
            case REST_API_CHANGE:
                changes.add("API changes may break client dependencies");
                changes.add("Request/response schema changes may affect serialization libraries");
                break;
            case DATABASE_CHANGE:
                changes.add("Database driver or ORM dependencies may need version alignment");
                changes.add("Migration tool dependencies may be affected");
                break;
            case CONFIGURATION_CHANGE:
                changes.add("Property changes may affect externalized configuration sources");
                break;
            case PERFORMANCE_IMPROVEMENT:
                changes.add("Caching or async dependencies may be introduced or changed");
                break;
            default:
                break;
        }

        // General dependency impact for non-trivial tasks
        if (changes.isEmpty() && taskType != TaskType.UNIT_TEST && taskType != TaskType.DOCUMENTATION) {
            changes.add("Internal module dependencies may need coordination across teams");
        }

        return new ArrayList<>(changes);
    }

    /**
     * Generates testing recommendations based on the proposed change.
     */
    List<String> generateTestingRecommendations(
            TaskType taskType, String normalizedChange, List<String> impactedClasses) {
        Set<String> recommendations = new LinkedHashSet<>();

        // Add type-specific testing recommendations
        List<String> typeRecommendations = TESTING_RECOMMENDATIONS_BY_TYPE.getOrDefault(taskType, List.of());
        recommendations.addAll(typeRecommendations);

        // Add class-specific testing recommendations
        for (String impactedClass : impactedClasses) {
            String cleanName = impactedClass;
            int parenIdx = cleanName.indexOf(" (");
            if (parenIdx > 0) {
                cleanName = cleanName.substring(0, parenIdx);
            }
            if (cleanName.endsWith("Controller") || cleanName.endsWith("Resource")
                    || cleanName.endsWith("Endpoint")) {
                recommendations.add("Write/update controller tests for " + cleanName);
            } else if (cleanName.endsWith("Service") || cleanName.endsWith("Manager")
                    || cleanName.endsWith("Provider") || cleanName.endsWith("Factory")) {
                recommendations.add("Write/update service layer tests for " + cleanName);
            } else if (cleanName.endsWith("Repository") || cleanName.endsWith("Dao")
                    || cleanName.endsWith("DataAccess")) {
                recommendations.add("Write/update data access tests for " + cleanName);
            } else if (cleanName.endsWith("Entity") || cleanName.endsWith("Model")
                    || cleanName.endsWith("Domain")) {
                recommendations.add("Write/update entity validation tests for " + cleanName);
            } else if (cleanName.endsWith("Config") || cleanName.endsWith("Configuration")
                    || cleanName.endsWith("Properties")) {
                recommendations.add("Write/update configuration loading tests for " + cleanName);
            }
        }

        // Add general recommendations
        if (!impactedClasses.isEmpty()) {
            recommendations.add("Run full regression test suite to verify no regressions");
        }

        return new ArrayList<>(recommendations);
    }

    /**
     * Generates a risk assessment for the proposed change.
     */
    List<String> generateRiskAssessment(
            TaskType taskType, String normalizedChange,
            List<String> impactedClasses, List<String> impactedFiles) {
        Set<String> risks = new LinkedHashSet<>();

        // Add type-specific risks
        List<String> typeRisks = RISKS_BY_TYPE.getOrDefault(taskType, List.of());
        risks.addAll(typeRisks);

        // Risk based on number of impacted classes
        if (impactedClasses.size() >= 5) {
            risks.add("High number of impacted classes increases integration risk");
        } else if (impactedClasses.size() >= 3) {
            risks.add("Multiple impacted classes require coordinated changes");
        }

        // Risk based on number of impacted files
        if (impactedFiles.size() >= 5) {
            risks.add("Wide file impact increases change collision risk");
        }

        // Security-related risk
        if (containsAny(normalizedChange, "authentication", "authorization", "security",
                "permission", "role", "user", "login", "password", "token", "oauth")) {
            risks.add("Security-related changes require thorough security review");
        }

        // Configuration risk
        if (containsAny(normalizedChange, "config", "property", "setting", "environment")) {
            risks.add("Configuration changes may have environment-specific side effects");
        }

        // Database risk
        if (containsAny(normalizedChange, "database", "migration", "schema", "entity", "table")) {
            risks.add("Database changes may require data migration and rollback planning");
        }

        // API risk
        if (containsAny(normalizedChange, "api", "endpoint", "rest", "controller")) {
            risks.add("API changes may break existing client integrations");
        }

        // If no specific risks identified, add a general one
        if (risks.isEmpty()) {
            risks.add("Limited information available for comprehensive risk assessment");
        }

        return new ArrayList<>(risks);
    }

    /**
     * Generates a suggested implementation order for the proposed change.
     */
    List<String> generateImplementationOrder(
            TaskType taskType, String normalizedChange, List<String> impactedClasses) {
        List<String> order = new ArrayList<>();

        // Start with type-specific implementation order
        List<String> baseOrder = IMPLEMENTATION_ORDER_BY_TYPE.getOrDefault(taskType, List.of());
        order.addAll(baseOrder);

        // Add class-specific implementation steps
        for (String impactedClass : impactedClasses) {
            String cleanName = impactedClass;
            int parenIdx = cleanName.indexOf(" (");
            if (parenIdx > 0) {
                cleanName = cleanName.substring(0, parenIdx);
            }
            String step = "Implement changes in " + cleanName;
            if (!order.contains(step)) {
                order.add(step);
            }
        }

        // Add final verification step
        order.add("Final verification: Run full test suite and verify build");

        return order;
    }

    // --- Private helper methods ---

    private void addPatternMatches(String text, Pattern pattern, Set<String> matches) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String name = matcher.group(1);
            if (name.length() >= 3) {
                matches.add(name);
            }
        }
    }

    private boolean isSourceFile(String fileName) {
        String lower = fileName.toLowerCase();
        return lower.endsWith(".java") || lower.endsWith(".kt") || lower.endsWith(".xml")
                || lower.endsWith(".yml") || lower.endsWith(".yaml") || lower.endsWith(".properties")
                || lower.endsWith(".json") || lower.endsWith(".sql") || lower.endsWith(".ts")
                || lower.endsWith(".js") || lower.endsWith(".css") || lower.endsWith(".html")
                || lower.endsWith(".md") || lower.endsWith(".gradle") || lower.endsWith(".pom");
    }

    private String deriveFileName(String componentName, String componentType) {
        if (componentName == null || componentName.trim().isEmpty()) {
            return null;
        }

        String cleanName = componentName.trim();
        // Remove prefixes like "Endpoint: ", "Package: "
        if (cleanName.startsWith("Endpoint: ")) {
            cleanName = cleanName.substring("Endpoint: ".length());
        } else if (cleanName.startsWith("Package: ")) {
            return null; // Packages don't map to single files
        }

        // Map component type to file extension
        switch (componentType) {
            case "Class":
            case "DTO":
                return cleanName + ".java";
            case "Configuration":
                if (cleanName.toLowerCase().contains("properties")
                        || cleanName.toLowerCase().contains("settings")) {
                    return cleanName + ".java";
                }
                return cleanName + ".java";
            case "REST API":
                return cleanName + ".java";
            case "Testing":
                return null; // Testing is not a single file
            case "Documentation":
                return cleanName + ".md";
            default:
                return cleanName + ".java";
        }
    }

    private List<String> getTypeSpecificFiles(TaskType taskType, String normalizedChange) {
        Set<String> files = new LinkedHashSet<>();

        switch (taskType) {
            case NEW_FEATURE:
                files.add("NewFeatureImplementation.java");
                break;
            case BUG_FIX:
                files.add("BugAffectedClass.java");
                break;
            case REFACTORING:
                files.add("RefactoredClass.java");
                break;
            case REST_API_CHANGE:
                files.add("ApiController.java");
                files.add("ApiService.java");
                break;
            case DATABASE_CHANGE:
                files.add("DatabaseEntity.java");
                files.add("DatabaseRepository.java");
                break;
            case CONFIGURATION_CHANGE:
                files.add("application.yml");
                files.add("ApplicationConfig.java");
                break;
            case PERFORMANCE_IMPROVEMENT:
                files.add("PerformanceOptimizedClass.java");
                break;
            case UNIT_TEST:
                files.add("TestClass.java");
                break;
            case DOCUMENTATION:
                files.add("README.md");
                break;
            default:
                break;
        }

        return new ArrayList<>(files);
    }

    private List<String> getDefaultFiles(TaskType taskType) {
        List<String> defaults = new ArrayList<>();
        switch (taskType) {
            case NEW_FEATURE:
                defaults.add("src/main/java/com/projectiq/NewFeature.java");
                break;
            case BUG_FIX:
                defaults.add("src/main/java/com/projectiq/AffectedClass.java");
                break;
            case REFACTORING:
                defaults.add("src/main/java/com/projectiq/RefactoredClass.java");
                break;
            case REST_API_CHANGE:
                defaults.add("src/main/java/com/projectiq/controller/ApiController.java");
                defaults.add("src/main/java/com/projectiq/service/ApiService.java");
                break;
            case DATABASE_CHANGE:
                defaults.add("src/main/java/com/projectiq/entity/DatabaseEntity.java");
                defaults.add("src/main/java/com/projectiq/repository/DatabaseRepository.java");
                break;
            case CONFIGURATION_CHANGE:
                defaults.add("src/main/resources/application.yml");
                defaults.add("src/main/java/com/projectiq/config/ApplicationConfig.java");
                break;
            case PERFORMANCE_IMPROVEMENT:
                defaults.add("src/main/java/com/projectiq/service/PerformanceOptimizedService.java");
                break;
            case UNIT_TEST:
                defaults.add("src/test/java/com/projectiq/ClassUnderTestTest.java");
                break;
            case DOCUMENTATION:
                defaults.add("README.md");
                break;
            default:
                defaults.add("src/main/java/com/projectiq/AffectedComponent.java");
                break;
        }
        return defaults;
    }

    private List<String> getDefaultImpactedClasses(TaskType taskType, String normalizedChange) {
        List<String> defaults = new ArrayList<>();
        switch (taskType) {
            case NEW_FEATURE:
                defaults.add("NewFeatureImplementation (Class)");
                break;
            case BUG_FIX:
                defaults.add("BugAffectedComponent (Class)");
                break;
            case REFACTORING:
                defaults.add("RefactoredComponent (Class)");
                break;
            case REST_API_CHANGE:
                defaults.add("ApiController (Controller)");
                defaults.add("ApiService (Service)");
                break;
            case DATABASE_CHANGE:
                defaults.add("DatabaseEntity (Entity/Model)");
                defaults.add("DatabaseRepository (Repository)");
                break;
            case CONFIGURATION_CHANGE:
                defaults.add("ApplicationConfig (Configuration)");
                break;
            case PERFORMANCE_IMPROVEMENT:
                defaults.add("PerformanceOptimizedComponent (Class)");
                break;
            case UNIT_TEST:
                defaults.add("ClassUnderTest (Class)");
                break;
            case DOCUMENTATION:
                defaults.add("DocumentedComponent (Class)");
                break;
            default:
                defaults.add("AffectedComponent (Class)");
                break;
        }
        return defaults;
    }

    private List<String> getTypeSpecificMethods(TaskType taskType, String normalizedChange) {
        Set<String> methods = new LinkedHashSet<>();

        if (containsAny(normalizedChange, "create", "add", "new", "insert")) {
            methods.add("create()");
            methods.add("save()");
        }
        if (containsAny(normalizedChange, "update", "modify", "change", "edit")) {
            methods.add("update()");
        }
        if (containsAny(normalizedChange, "delete", "remove", "drop")) {
            methods.add("delete()");
            methods.add("remove()");
        }
        if (containsAny(normalizedChange, "get", "find", "search", "query", "retrieve")) {
            methods.add("findById()");
            methods.add("findAll()");
            methods.add("search()");
        }
        if (containsAny(normalizedChange, "validate", "validation", "check")) {
            methods.add("validate()");
        }
        if (containsAny(normalizedChange, "transform", "convert", "map")) {
            methods.add("convert()");
            methods.add("transform()");
        }

        return new ArrayList<>(methods);
    }

    private List<String> getTypeSpecificApis(TaskType taskType, String normalizedChange) {
        Set<String> apis = new LinkedHashSet<>();

        if (taskType == TaskType.REST_API_CHANGE
                || containsAny(normalizedChange, "api", "endpoint", "rest", "controller")) {
            if (containsAny(normalizedChange, "get", "fetch", "retrieve", "list")) {
                apis.add("GET /api/resource");
                apis.add("GET /api/resource/{id}");
            }
            if (containsAny(normalizedChange, "create", "add", "post", "new")) {
                apis.add("POST /api/resource");
            }
            if (containsAny(normalizedChange, "update", "put", "modify")) {
                apis.add("PUT /api/resource/{id}");
                apis.add("PATCH /api/resource/{id}");
            }
            if (containsAny(normalizedChange, "delete", "remove")) {
                apis.add("DELETE /api/resource/{id}");
            }
            if (apis.isEmpty()) {
                apis.add("GET /api/resource");
                apis.add("POST /api/resource");
                apis.add("PUT /api/resource/{id}");
                apis.add("DELETE /api/resource/{id}");
            }
        }

        return new ArrayList<>(apis);
    }

    private static Map<TaskType, List<String>> buildImplementationOrderByType() {
        Map<TaskType, List<String>> map = new LinkedHashMap<>();
        map.put(TaskType.NEW_FEATURE, List.of(
                "1. Define data models and DTOs",
                "2. Implement repository/data access layer",
                "3. Implement service layer with business logic",
                "4. Implement controller/REST API endpoints",
                "5. Add input validation and error handling",
                "6. Write unit tests for all new components",
                "7. Write integration tests for new workflows"
        ));
        map.put(TaskType.BUG_FIX, List.of(
                "1. Reproduce the bug and write a failing test",
                "2. Identify the root cause in the affected component",
                "3. Implement the fix",
                "4. Verify the fix passes the failing test",
                "5. Run regression tests to verify no side effects"
        ));
        map.put(TaskType.REFACTORING, List.of(
                "1. Ensure existing tests pass before refactoring",
                "2. Refactor the target component",
                "3. Update all references to the refactored component",
                "4. Run existing tests to verify unchanged behavior",
                "5. Add additional tests for new code paths"
        ));
        map.put(TaskType.REST_API_CHANGE, List.of(
                "1. Update API contract/specification",
                "2. Modify the controller endpoint",
                "3. Update request/response DTOs",
                "4. Update service layer if needed",
                "5. Update API documentation",
                "6. Write/update controller tests",
                "7. Write/update integration tests"
        ));
        map.put(TaskType.DATABASE_CHANGE, List.of(
                "1. Create database migration script",
                "2. Update entity/model class",
                "3. Update repository/data access layer",
                "4. Update service layer for new schema",
                "5. Write migration tests",
                "6. Write entity validation tests"
        ));
        map.put(TaskType.CONFIGURATION_CHANGE, List.of(
                "1. Update configuration properties file",
                "2. Update configuration class if needed",
                "3. Update dependent components",
                "4. Test configuration loading",
                "5. Verify configuration across environments"
        ));
        map.put(TaskType.PERFORMANCE_IMPROVEMENT, List.of(
                "1. Establish performance baseline",
                "2. Identify performance bottleneck",
                "3. Implement optimization",
                "4. Run performance benchmarks",
                "5. Verify no functional regressions"
        ));
        map.put(TaskType.UNIT_TEST, List.of(
                "1. Identify the class under test",
                "2. Write test cases covering all scenarios",
                "3. Run tests and verify coverage",
                "4. Review test quality and edge cases"
        ));
        map.put(TaskType.DOCUMENTATION, List.of(
                "1. Identify documentation gaps",
                "2. Write/update documentation",
                "3. Review documentation for accuracy",
                "4. Publish updated documentation"
        ));
        map.put(TaskType.UNKNOWN, List.of(
                "1. Analyze the proposed change in detail",
                "2. Identify all affected components",
                "3. Implement changes incrementally",
                "4. Test each change before proceeding",
                "5. Run full regression test suite"
        ));
        return map;
    }

    private static Map<TaskType, List<String>> buildRisksByType() {
        Map<TaskType, List<String>> map = new LinkedHashMap<>();
        map.put(TaskType.NEW_FEATURE, List.of(
                "New feature may introduce integration issues with existing components",
                "Feature scope may expand during implementation"
        ));
        map.put(TaskType.BUG_FIX, List.of(
                "Fix may introduce regressions in other areas",
                "Root cause may be deeper than initially identified"
        ));
        map.put(TaskType.REFACTORING, List.of(
                "Refactoring may introduce behavioral changes in unexpected areas",
                "Comprehensive test coverage is required before and after refactoring"
        ));
        map.put(TaskType.REST_API_CHANGE, List.of(
                "API changes may break existing clients and integrations",
                "Backward compatibility must be maintained or versioned"
        ));
        map.put(TaskType.DATABASE_CHANGE, List.of(
                "Data migration errors could cause data loss or corruption",
                "Schema changes may affect existing queries and reports"
        ));
        map.put(TaskType.CONFIGURATION_CHANGE, List.of(
                "Configuration changes may have environment-specific side effects",
                "Property changes may affect multiple dependent components"
        ));
        map.put(TaskType.PERFORMANCE_IMPROVEMENT, List.of(
                "Performance changes may introduce regressions in other areas",
                "Optimizations may increase code complexity"
        ));
        map.put(TaskType.UNIT_TEST, List.of(
                "Insufficient test coverage may miss edge cases",
                "Mocking may not accurately reflect real behavior"
        ));
        map.put(TaskType.DOCUMENTATION, List.of(
                "Outdated documentation may mislead developers",
                "Documentation changes have no functional risk"
        ));
        map.put(TaskType.UNKNOWN, List.of(
                "Limited information available for comprehensive risk assessment",
                "Verify assumptions by examining the actual codebase"
        ));
        return map;
    }

    private static Map<TaskType, List<String>> buildTestingRecommendationsByType() {
        Map<TaskType, List<String>> map = new LinkedHashMap<>();
        map.put(TaskType.NEW_FEATURE, List.of(
                "Write unit tests for all new classes and methods",
                "Write integration tests for new feature workflows",
                "Write end-to-end tests for complete feature validation"
        ));
        map.put(TaskType.BUG_FIX, List.of(
                "Write a regression test for the fixed bug scenario",
                "Run existing tests for affected components",
                "Run full regression test suite"
        ));
        map.put(TaskType.REFACTORING, List.of(
                "Ensure existing tests pass before refactoring",
                "Add additional tests for refactored code paths",
                "Run full regression test suite after refactoring"
        ));
        map.put(TaskType.REST_API_CHANGE, List.of(
                "Write/update controller unit tests",
                "Write API contract/integration tests",
                "Write end-to-end tests for API workflows"
        ));
        map.put(TaskType.DATABASE_CHANGE, List.of(
                "Write entity validation tests",
                "Write repository/data access tests",
                "Write database migration and rollback tests"
        ));
        map.put(TaskType.CONFIGURATION_CHANGE, List.of(
                "Write configuration loading tests",
                "Write Spring Boot context tests",
                "Verify configuration across all environments"
        ));
        map.put(TaskType.PERFORMANCE_IMPROVEMENT, List.of(
                "Write performance benchmark tests",
                "Write unit tests for optimized components",
                "Write integration tests to verify no regressions"
        ));
        map.put(TaskType.UNIT_TEST, List.of(
                "Write comprehensive unit tests for the target class",
                "Ensure edge cases and error scenarios are covered",
                "Verify test coverage meets project standards"
        ));
        map.put(TaskType.DOCUMENTATION, List.of(
                "No functional tests required (documentation only)",
                "Review documentation for technical accuracy"
        ));
        map.put(TaskType.UNKNOWN, List.of(
                "Write unit tests for all affected components",
                "Write integration tests for affected workflows",
                "Run full regression test suite"
        ));
        return map;
    }

    private boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }
}