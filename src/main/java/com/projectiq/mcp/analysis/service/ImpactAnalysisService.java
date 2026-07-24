package com.projectiq.mcp.analysis.service;

import com.projectiq.mcp.analysis.dto.ConfidenceLevel;
import com.projectiq.mcp.analysis.dto.ContextAssemblyResponse;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse.ImpactedComponent;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse.RiskItem;
import com.projectiq.mcp.analysis.dto.RiskLevel;
import com.projectiq.mcp.analysis.dto.ScopeLevel;
import com.projectiq.mcp.analysis.dto.TaskAnalysisResponse;
import com.projectiq.mcp.analysis.dto.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service that performs deterministic rule-based impact analysis for
 * proposed development tasks. Analyzes the task, identifies affected
 * repository components, estimates change scope, highlights dependencies,
 * and produces a structured impact report without using any AI model or
 * LLM integration.
 *
 * <p>Relies on {@link TaskAnalysisService} for task type detection and
 * entity identification, and on {@link ContextAssemblyService} for
 * assembling the repository context needed for impact assessment.</p>
 */
@Service
public class ImpactAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(ImpactAnalysisService.class);

    private final TaskAnalysisService taskAnalysisService;
    private final ContextAssemblyService contextAssemblyService;

    // --- Patterns for impact detection ---

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

    private static final Pattern PACKAGE_PATTERN = Pattern.compile(
            "\\b([a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]*)+)\\b"
    );

    private static final Pattern METHOD_PATTERN = Pattern.compile(
            "\\b([a-z][a-zA-Z0-9]*\\(\\s*\\)|[a-z][a-zA-Z0-9]*By[A-Z][a-zA-Z0-9]*)\\b"
    );

    private static final Pattern CLASS_PATTERN = Pattern.compile(
            "\\b([A-Z][a-zA-Z0-9]{2,})\\b"
    );

    // --- Thresholds for scope estimation ---

    private static final int SCOPE_SCORE_LARGE = 5;
    private static final int SCOPE_SCORE_MEDIUM = 3;

    // --- Common word filter ---

    private static final Set<String> COMMON_WORDS = Set.of(
            "the", "this", "that", "with", "from", "have", "been",
            "were", "will", "what", "when", "where", "which", "their",
            "there", "could", "should", "would", "about", "every",
            "after", "before", "between", "other", "under", "above",
            "while", "since", "until", "during", "through", "without",
            "within", "along", "among", "these", "those", "being",
            "having", "doing", "making", "using", "working", "going",
            "later", "still", "just", "java", "spring", "boot",
            "maven", "gradle", "junit", "test"
    );

    public ImpactAnalysisService(
            TaskAnalysisService taskAnalysisService,
            ContextAssemblyService contextAssemblyService) {
        this.taskAnalysisService = taskAnalysisService;
        this.contextAssemblyService = contextAssemblyService;
    }

    /**
     * Analyzes the potential impact of a proposed development task on the repository.
     * Invokes task analysis, assembles repository context, and produces a structured
     * impact report.
     *
     * @param task           the natural language development request
     * @param repositoryName the repository name
     * @param branch         the git branch (optional, defaults to "main")
     * @return a structured impact analysis response
     * @throws IllegalArgumentException if the task is null or empty
     */
    public ImpactAnalysisResponse analyzeImpact(
            String task, String repositoryName, String branch) {
        if (task == null || task.trim().isEmpty()) {
            throw new IllegalArgumentException("Task description cannot be null or empty");
        }

        logger.info("Analyzing impact for task: {} in repository: {}", task, repositoryName);

        // Step 1: Analyze the task
        TaskAnalysisResponse taskAnalysis = taskAnalysisService.analyze(task);
        String normalizedTask = task.trim().toLowerCase();

        // Step 2: Attempt to assemble repository context (non-critical)
        try {
            String effectiveBranch = (branch != null && !branch.trim().isEmpty()) ? branch.trim() : "main";
            contextAssemblyService.assembleContext(task.trim(), repositoryName, effectiveBranch);
        } catch (Exception e) {
            logger.warn("Failed to assemble repository context: {}", e.getMessage());
            // Continue with partial context - we can still produce a basic impact analysis
        }

        // Step 3: Build impact analysis response
        ImpactAnalysisResponse response = new ImpactAnalysisResponse();
        response.setOriginalTask(task.trim());

        // Set task type display name
        TaskType taskType = taskAnalysis.getTaskType();
        response.setTaskType(taskType.getDisplayName());

        // Step 4: Identify primary targets
        List<String> primaryTargets = identifyPrimaryTargets(taskAnalysis, task, normalizedTask);
        response.setPrimaryTargets(primaryTargets);

        // Step 5: Identify directly affected components
        List<ImpactedComponent> directlyAffected = identifyDirectlyAffectedComponents(
                taskAnalysis, task, normalizedTask);
        response.setDirectlyAffectedComponents(directlyAffected);

        // Step 6: Identify indirectly affected components
        List<ImpactedComponent> indirectlyAffected = identifyIndirectlyAffectedComponents(
                taskAnalysis, task, normalizedTask, directlyAffected);
        response.setIndirectlyAffectedComponents(indirectlyAffected);

        // Step 7: Identify dependency impact
        List<String> dependencyImpact = identifyDependencyImpact(normalizedTask, taskType);
        response.setDependencyImpact(dependencyImpact);

        // Step 8: Estimate implementation scope
        ScopeLevel implScope = estimateImplementationScope(
                taskType, directlyAffected, indirectlyAffected, normalizedTask);
        response.setEstimatedImplementationScope(implScope);

        // Step 9: Estimate testing scope
        ScopeLevel testScope = estimateTestingScope(
                implScope, directlyAffected, indirectlyAffected, taskType);
        response.setEstimatedTestingScope(testScope);

        // Step 10: Identify potential risks
        List<RiskItem> risks = identifyRisks(
                taskType, directlyAffected, indirectlyAffected, normalizedTask);
        response.setPotentialRisks(risks);

        // Step 11: Determine confidence level
        ConfidenceLevel confidence = determineConfidence(
                taskAnalysis.getConfidenceLevel());
        response.setConfidenceLevel(confidence);

        logger.info("Impact analysis complete: type={}, direct={}, indirect={}, implScope={}, confidence={}",
                taskType, directlyAffected.size(), indirectlyAffected.size(), implScope, confidence);

        return response;
    }

    /**
     * Identifies the primary targets of the proposed change based on task analysis.
     */
    List<String> identifyPrimaryTargets(TaskAnalysisResponse analysis, String task, String normalizedTask) {
        Set<String> targets = new LinkedHashSet<>();

        // Add detected entities as potential primary targets
        if (analysis.getDetectedEntities() != null) {
            for (String entity : analysis.getDetectedEntities()) {
                if (targets.size() < 5) {
                    targets.add(entity);
                }
            }
        }

        // If no entities detected, derive targets from task type
        if (targets.isEmpty()) {
            if (normalizedTask.contains("controller") || normalizedTask.contains("endpoint")
                    || normalizedTask.contains("api")) {
                targets.add("REST API layer");
            }
            if (normalizedTask.contains("service") || normalizedTask.contains("business")) {
                targets.add("Service layer");
            }
            if (normalizedTask.contains("repository") || normalizedTask.contains("database")
                    || normalizedTask.contains("entity")) {
                targets.add("Data access layer");
            }
            if (normalizedTask.contains("config") || normalizedTask.contains("property")) {
                targets.add("Configuration");
            }
            if (targets.isEmpty()) {
                targets.add("Unspecified (" + analysis.getTaskType().getDisplayName() + ")");
            }
        }

        return new ArrayList<>(targets);
    }

    /**
     * Identifies components that are directly affected by the proposed change.
     */
    List<ImpactedComponent> identifyDirectlyAffectedComponents(
            TaskAnalysisResponse analysis, String task, String normalizedTask) {
        Set<ImpactedComponent> components = new LinkedHashSet<>();

        // Detect controller classes
        addDirectMatches(task, CONTROLLER_PATTERN, components, "Class",
                "Directly referenced controller in the task description");
        // Detect service classes
        addDirectMatches(task, SERVICE_PATTERN, components, "Class",
                "Directly referenced service in the task description");
        // Detect repository classes
        addDirectMatches(task, REPOSITORY_PATTERN, components, "Class",
                "Directly referenced repository in the task description");
        // Detect entity/model classes
        addDirectMatches(task, ENTITY_PATTERN, components, "Class",
                "Directly referenced entity in the task description");
        // Detect config classes
        addDirectMatches(task, CONFIG_CLASS_PATTERN, components, "Configuration",
                "Directly referenced configuration in the task description");

        // Detect REST endpoints
        detectDirectEndpoints(task, components);

        // Detect methods directly referenced
        addDirectMethodMatches(task, components);

        // Detect packages
        addDirectPackageMatches(task, components);

        // If no components found via pattern matching, add default based on task type
        if (components.isEmpty()) {
            addDefaultComponents(analysis.getTaskType(), normalizedTask, components);
        }

        return new ArrayList<>(components);
    }

    /**
     * Identifies components that are indirectly affected by the proposed change.
     */
    List<ImpactedComponent> identifyIndirectlyAffectedComponents(
            TaskAnalysisResponse analysis, String task, String normalizedTask,
            List<ImpactedComponent> directlyAffected) {
        Set<ImpactedComponent> components = new LinkedHashSet<>();
        TaskType taskType = analysis.getTaskType();

        // If a controller is affected, its service layer is indirectly affected
        for (ImpactedComponent direct : directlyAffected) {
            String name = direct.getComponentName();
            if (name.contains("Controller") || name.contains("Resource")
                    || name.contains("Endpoint")) {
                String serviceName = name
                        .replace("Controller", "Service")
                        .replace("Resource", "Service")
                        .replace("Endpoint", "Service");
                components.add(new ImpactedComponent(
                        serviceName, "Class",
                        "Service layer associated with controller " + name));
            }
            if (name.contains("Service")) {
                String repoName = name
                        .replace("Service", "Repository")
                        .replace("Manager", "Repository")
                        .replace("Helper", "Repository");
                components.add(new ImpactedComponent(
                        repoName, "Class",
                        "Data access layer associated with service " + name));
            }
        }

        // Configuration changes affect classes that read those properties
        if (taskType == TaskType.CONFIGURATION_CHANGE
                || normalizedTask.contains("config") || normalizedTask.contains("property")) {
            components.add(new ImpactedComponent(
                    "Configuration consumers", "Configuration",
                    "Classes that read affected properties may need updates"));
            components.add(new ImpactedComponent(
                    "Environment profiles", "Configuration",
                    "Other environment profiles (dev, staging, prod) may need aligned changes"));
        }

        // REST API changes affect clients and documentation
        if (taskType == TaskType.REST_API_CHANGE
                || normalizedTask.contains("endpoint") || normalizedTask.contains("api")) {
            components.add(new ImpactedComponent(
                    "API consumers", "Dependency",
                    "External or internal clients consuming the changed API"));
            components.add(new ImpactedComponent(
                    "API specification", "Documentation",
                    "Swagger/OpenAPI specs need updating to reflect API changes"));
        }

        // Database/entity changes affect related entities and queries
        if (taskType == TaskType.DATABASE_CHANGE
                || normalizedTask.contains("entity") || normalizedTask.contains("schema")) {
            components.add(new ImpactedComponent(
                    "Related entities", "Class",
                    "Entities with relationships to the changed entity"));
            components.add(new ImpactedComponent(
                    "Database migrations", "Configuration",
                    "New migration scripts may be needed for schema changes"));
            components.add(new ImpactedComponent(
                    "Data access layer", "Class",
                    "Repository and DAO methods may need updates for new schema"));
        }

        // Refactoring affects all related code paths
        if (taskType == TaskType.REFACTORING) {
            components.add(new ImpactedComponent(
                    "Caller classes", "Class",
                    "Any class that references the refactored components"));
            components.add(new ImpactedComponent(
                    "Unit tests", "Testing",
                    "Existing tests may need restructuring to match new design"));
        }

        // Performance improvements affect monitoring and metrics
        if (taskType == TaskType.PERFORMANCE_IMPROVEMENT) {
            components.add(new ImpactedComponent(
                    "Monitoring and metrics", "Configuration",
                    "Performance monitoring configuration may need updates"));
            components.add(new ImpactedComponent(
                    "Load balancer configuration", "Configuration",
                    "Infrastructure configuration may need tuning"));
        }

        // DTO/API changes affect data transfer layer
        if (analysis.getDetectedEntities() != null) {
            for (String entity : analysis.getDetectedEntities()) {
                String entityLower = entity.toLowerCase();
                if (entityLower.contains("dto") || entityLower.contains("request")
                        || entityLower.contains("response")) {
                    components.add(new ImpactedComponent(
                            entity, "DTO",
                            "Data transfer object affected by entity or API changes"));
                }
            }
        }

        // Always include testing as indirectly affected
        components.add(new ImpactedComponent(
                "Unit tests", "Testing",
                "Tests for all affected components need validation"));

        if (taskType != TaskType.UNIT_TEST) {
            components.add(new ImpactedComponent(
                    "Integration tests", "Testing",
                    "End-to-end tests for affected features need validation"));
        }

        return new ArrayList<>(components);
    }

    /**
     * Identifies the dependency impact of the proposed change.
     */
    List<String> identifyDependencyImpact(String normalizedTask, TaskType taskType) {
        Set<String> impacts = new LinkedHashSet<>();

        // Check for dependency-related keywords
        if (normalizedTask.contains("dependency") || normalizedTask.contains("maven")
                || normalizedTask.contains("gradle") || normalizedTask.contains("pom")
                || normalizedTask.contains("library") || normalizedTask.contains("import")) {
            impacts.add("External library dependencies may need version updates");
            impacts.add("Transitive dependencies may introduce compatibility issues");
        }

        // Task-type specific dependency impacts
        switch (taskType) {
            case NEW_FEATURE:
                if (!containsAny(normalizedTask, "simple", "minor", "cosmetic", "trivial")) {
                    impacts.add("New dependencies may be required for the feature implementation");
                }
                break;
            case BUG_FIX:
                impacts.add("Bug fix may require dependency version rollback or updates");
                impacts.add("Fix may affect backward compatibility with existing APIs");
                break;
            case REFACTORING:
                impacts.add("Refactoring may change internal APIs affecting dependent modules");
                impacts.add("Dependency injection wiring may need updates");
                break;
            case REST_API_CHANGE:
                impacts.add("API changes may break client dependencies");
                impacts.add("Request/response schema changes may affect serialization libraries");
                break;
            case DATABASE_CHANGE:
                impacts.add("Database driver or ORM dependencies may need version alignment");
                impacts.add("Migration tool dependencies may be affected");
                break;
            case CONFIGURATION_CHANGE:
                impacts.add("Property changes may affect externalized configuration sources");
                break;
            case PERFORMANCE_IMPROVEMENT:
                impacts.add("Caching or async dependencies may be introduced or changed");
                break;
            default:
                break;
        }

        // General dependency impact for non-trivial tasks
        if (!impacts.isEmpty() || taskType != TaskType.UNIT_TEST) {
            impacts.add("Internal module dependencies may need coordination across teams");
        }

        return new ArrayList<>(impacts);
    }

    /**
     * Estimates the implementation scope based on task characteristics.
     */
    ScopeLevel estimateImplementationScope(
            TaskType taskType, List<ImpactedComponent> directlyAffected,
            List<ImpactedComponent> indirectlyAffected, String normalizedTask) {
        int score = 0;

        // Score based on number of directly affected components
        int directCount = directlyAffected.size();
        if (directCount >= 4) {
            score += 3;
        } else if (directCount >= 2) {
            score += 2;
        } else if (directCount >= 1) {
            score += 1;
        }

        // Score based on total affected components
        int totalCount = directlyAffected.size() + indirectlyAffected.size();
        if (totalCount >= 8) {
            score += 3;
        } else if (totalCount >= 4) {
            score += 2;
        }

        // Score based on task type complexity
        switch (taskType) {
            case DATABASE_CHANGE:
            case REFACTORING:
            case PERFORMANCE_IMPROVEMENT:
                score += 2;
                break;
            case NEW_FEATURE:
            case REST_API_CHANGE:
                score += 1;
                break;
            default:
                break;
        }

        // Score based on scope keywords
        if (containsAny(normalizedTask,
                "database", "migration", "refactor", "restructure", "overhaul",
                "performance", "security", "authentication", "authorization",
                "microservice", "integration", "batch", "async")) {
            score += 2;
        }

        if (score >= SCOPE_SCORE_LARGE) {
            return ScopeLevel.LARGE;
        } else if (score >= SCOPE_SCORE_MEDIUM) {
            return ScopeLevel.MEDIUM;
        } else {
            return ScopeLevel.SMALL;
        }
    }

    /**
     * Estimates the testing scope based on the implementation scope and affected components.
     */
    ScopeLevel estimateTestingScope(
            ScopeLevel implScope, List<ImpactedComponent> directlyAffected,
            List<ImpactedComponent> indirectlyAffected, TaskType taskType) {
        int score = 0;

        // Testing scope is at least as large as implementation scope
        switch (implScope) {
            case LARGE:
                score += 2;
                break;
            case MEDIUM:
                score += 1;
                break;
            default:
                break;
        }

        // More affected components means more testing
        int totalCount = directlyAffected.size() + indirectlyAffected.size();
        if (totalCount >= 6) {
            score += 2;
        } else if (totalCount >= 3) {
            score += 1;
        }

        // Database and refactoring tasks require more testing
        if (taskType == TaskType.DATABASE_CHANGE
                || taskType == TaskType.REFACTORING
                || taskType == TaskType.PERFORMANCE_IMPROVEMENT) {
            score += 1;
        }

        if (score >= SCOPE_SCORE_LARGE) {
            return ScopeLevel.LARGE;
        } else if (score >= SCOPE_SCORE_MEDIUM) {
            return ScopeLevel.MEDIUM;
        } else {
            return ScopeLevel.SMALL;
        }
    }

    /**
     * Identifies potential risks for the proposed change.
     */
    List<RiskItem> identifyRisks(
            TaskType taskType, List<ImpactedComponent> directlyAffected,
            List<ImpactedComponent> indirectlyAffected, String normalizedTask) {
        List<RiskItem> risks = new ArrayList<>();

        // Risk based on task type
        if (taskType == TaskType.DATABASE_CHANGE) {
            risks.add(new RiskItem(
                    "Data migration errors could cause data loss or corruption",
                    RiskLevel.HIGH,
                    "Implement thorough data validation and rollback procedures"));
        }

        if (taskType == TaskType.PERFORMANCE_IMPROVEMENT) {
            risks.add(new RiskItem(
                    "Performance changes may introduce regressions in other areas",
                    RiskLevel.HIGH,
                    "Establish performance baselines and run comprehensive benchmarks"));
        }

        if (taskType == TaskType.REFACTORING) {
            risks.add(new RiskItem(
                    "Refactoring may introduce behavioral changes in unexpected areas",
                    RiskLevel.HIGH,
                    "Ensure comprehensive test coverage before and after refactoring"));
        }

        if (taskType == TaskType.REST_API_CHANGE) {
            risks.add(new RiskItem(
                    "API changes may break existing clients and integrations",
                    RiskLevel.HIGH,
                    "Maintain backward compatibility or coordinate API versioning"));
        }

        if (taskType == TaskType.NEW_FEATURE) {
            int totalComponents = directlyAffected.size() + indirectlyAffected.size();
            if (totalComponents >= 5) {
                risks.add(new RiskItem(
                        "Large feature scope may introduce integration issues across multiple components",
                        RiskLevel.MEDIUM,
                        "Implement feature incrementally with continuous integration"));
            }
        }

        // Risk based on component count
        if (directlyAffected.size() >= 3) {
            risks.add(new RiskItem(
                    "Multiple directly affected components increase change collision risk",
                    RiskLevel.MEDIUM,
                    "Coordinate changes across component boundaries"));
        }

        // Security-related risk
        if (containsAny(normalizedTask, "authentication", "authorization", "security",
                "permission", "role", "user", "login", "password", "token")) {
            risks.add(new RiskItem(
                    "Security-related changes have high impact and require thorough review",
                    RiskLevel.HIGH,
                    "Perform security review and penetration testing"));
        }

        // Configuration risk
        if (containsAny(normalizedTask, "config", "property", "setting", "environment")) {
            risks.add(new RiskItem(
                    "Configuration changes may have environment-specific side effects",
                    RiskLevel.MEDIUM,
                    "Test configuration changes across all environments"));
        }

        // Add a low-risk item for general awareness
        if (risks.isEmpty()) {
            risks.add(new RiskItem(
                    "Limited information available for comprehensive risk assessment",
                    RiskLevel.LOW,
                    "Verify assumptions by examining the actual codebase"));
        }

        return risks;
    }

    /**
     * Determines the overall confidence level of the impact analysis.
     */
    ConfidenceLevel determineConfidence(ConfidenceLevel taskAnalysisConfidence) {
        // The confidence level is derived from the task analysis confidence,
        // which is based on the strength of keyword matching
        return taskAnalysisConfidence;
    }

    // --- Private helper methods ---

    private void addDirectMatches(
            String text, Pattern pattern,
            Set<ImpactedComponent> components, String componentType, String reason) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String name = matcher.group(1);
            components.add(new ImpactedComponent(name, componentType, reason));
        }
    }

    private void detectDirectEndpoints(String task, Set<ImpactedComponent> components) {
        Matcher matcher = ENDPOINT_PATTERN.matcher(task);
        while (matcher.find()) {
            String group2 = matcher.group(2);
            if (group2 != null) {
                components.add(new ImpactedComponent(
                        "Endpoint: " + group2, "REST API",
                        "Directly referenced endpoint in the task description"));
            } else {
                String match = matcher.group(1);
                if (match != null && match.startsWith("/")) {
                    components.add(new ImpactedComponent(
                            "Endpoint: " + match, "REST API",
                            "Directly referenced endpoint in the task description"));
                }
            }
        }
    }

    private void addDirectMethodMatches(String task, Set<ImpactedComponent> components) {
        Matcher matcher = METHOD_PATTERN.matcher(task);
        while (matcher.find()) {
            String method = matcher.group().trim();
            components.add(new ImpactedComponent(
                    method, "Method",
                    "Directly referenced method in the task description"));
        }
    }

    private void addDirectPackageMatches(String task, Set<ImpactedComponent> components) {
        Matcher matcher = PACKAGE_PATTERN.matcher(task);
        while (matcher.find()) {
            String pkg = matcher.group(1);
            if (!pkg.equals("com") && !pkg.equals("org") && !pkg.equals("io")
                    && !pkg.equals("net") && !pkg.startsWith("http")) {
                components.add(new ImpactedComponent(
                        "Package: " + pkg, "Package",
                        "Directly referenced package in the task description"));
            }
        }
    }

    private void addDefaultComponents(
            TaskType taskType, String normalizedTask, Set<ImpactedComponent> components) {
        switch (taskType) {
            case NEW_FEATURE:
                components.add(new ImpactedComponent(
                        "New feature implementation", "Class",
                        "Primary implementation target for the new feature"));
                break;
            case BUG_FIX:
                components.add(new ImpactedComponent(
                        "Bug-affected component", "Class",
                        "Component containing the defect to be fixed"));
                break;
            case REFACTORING:
                components.add(new ImpactedComponent(
                        "Target component for refactoring", "Class",
                        "Component identified for restructuring"));
                break;
            case REST_API_CHANGE:
                components.add(new ImpactedComponent(
                        "REST Controller", "Class",
                        "Primary target for REST API modification"));
                break;
            case DATABASE_CHANGE:
                components.add(new ImpactedComponent(
                        "Database entity", "Class",
                        "Entity affected by database schema changes"));
                break;
            case CONFIGURATION_CHANGE:
                components.add(new ImpactedComponent(
                        "Configuration properties", "Configuration",
                        "Configuration properties to be modified"));
                break;
            case PERFORMANCE_IMPROVEMENT:
                components.add(new ImpactedComponent(
                        "Performance-critical component", "Class",
                        "Component targeted for performance optimization"));
                break;
            case UNIT_TEST:
                components.add(new ImpactedComponent(
                        "Test class", "Testing",
                        "Test class to be created or modified"));
                break;
            case DOCUMENTATION:
                components.add(new ImpactedComponent(
                        "Documentation files", "Documentation",
                        "Documentation to be created or updated"));
                break;
            default:
                components.add(new ImpactedComponent(
                        "Unspecified component", "Class",
                        "Component type could not be determined from the task"));
                break;
        }
    }

    private boolean containsComponentType(List<ImpactedComponent> components, String type) {
        return components.stream().anyMatch(c -> type.equals(c.getComponentType()));
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