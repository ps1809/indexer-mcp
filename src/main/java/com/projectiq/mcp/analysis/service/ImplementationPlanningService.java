package com.projectiq.mcp.analysis.service;

import com.projectiq.mcp.analysis.dto.ComplexityLevel;
import com.projectiq.mcp.analysis.dto.ImplementationPlanningResponse;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse.ImpactedComponent;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse.RiskItem;
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

/**
 * Service that generates a deterministic implementation plan for a requested
 * development task. Builds upon Task Analysis, Context Assembly, and Impact
 * Analysis to produce a structured plan that guides AI coding agents through
 * the recommended sequence of implementation steps without generating any code.
 *
 * <p>This service is entirely rule-based and does not use any AI model or
 * LLM integration. All outputs are stable, deterministic, and free of
 * duplicate entries.</p>
 */
@Service
public class ImplementationPlanningService {

    private static final Logger logger = LoggerFactory.getLogger(ImplementationPlanningService.class);

    private final TaskAnalysisService taskAnalysisService;
    private final ContextAssemblyService contextAssemblyService;
    private final ImpactAnalysisService impactAnalysisService;

    // --- Deterministic implementation order by task type ---

    private static final Map<TaskType, List<String>> IMPLEMENTATION_ORDER = buildImplementationOrder();

    // --- Validation step templates ---

    private static final Map<TaskType, List<String>> VALIDATION_STEPS = buildValidationSteps();

    public ImplementationPlanningService(
            TaskAnalysisService taskAnalysisService,
            ContextAssemblyService contextAssemblyService,
            ImpactAnalysisService impactAnalysisService) {
        this.taskAnalysisService = taskAnalysisService;
        this.contextAssemblyService = contextAssemblyService;
        this.impactAnalysisService = impactAnalysisService;
    }

    /**
     * Generates a deterministic implementation plan for the given development task.
     * Invokes task analysis, context assembly, and impact analysis to produce
     * a structured plan that includes recommended order, affected files,
     * components, dependencies, validation steps, testing scope, risks, and
     * assumptions.
     *
     * @param task           the natural language development request
     * @param repositoryName the repository name
     * @param branch         the git branch (optional, defaults to "main")
     * @return a structured implementation planning response
     * @throws IllegalArgumentException if the task is null or empty
     */
    public ImplementationPlanningResponse generatePlan(
            String task, String repositoryName, String branch) {
        if (task == null || task.trim().isEmpty()) {
            throw new IllegalArgumentException("Task description cannot be null or empty");
        }

        logger.info("Generating implementation plan for task: {} in repository: {}",
                task, repositoryName);

        String effectiveBranch = (branch != null && !branch.trim().isEmpty()) ? branch.trim() : "main";
        String normalizedTask = task.trim().toLowerCase();

        // Step 1: Analyze the task
        TaskAnalysisResponse taskAnalysis = taskAnalysisService.analyze(task);

        // Step 2: Assemble repository context (non-critical)
        try {
            contextAssemblyService.assembleContext(task.trim(), repositoryName, effectiveBranch);
        } catch (Exception e) {
            logger.warn("Failed to assemble repository context: {}", e.getMessage());
            // Continue with partial context
        }

        // Step 3: Analyze impact (non-critical)
        ImpactAnalysisResponse impactAnalysis = null;
        try {
            impactAnalysis = impactAnalysisService.analyzeImpact(task.trim(), repositoryName, effectiveBranch);
        } catch (Exception e) {
            logger.warn("Failed to analyze impact: {}", e.getMessage());
            // Continue with partial impact analysis
        }

        // Step 4: Build the implementation plan
        ImplementationPlanningResponse response = new ImplementationPlanningResponse();
        response.setOriginalTask(task.trim());

        // Set task type
        TaskType taskType = taskAnalysis.getTaskType();
        response.setTaskType(taskType.getDisplayName());

        // Set estimated complexity
        ComplexityLevel complexity = taskAnalysis.getEstimatedComplexity();
        response.setEstimatedComplexity(complexity.name());

        // Build recommended implementation order
        List<String> implOrder = buildRecommendedOrder(taskType, taskAnalysis, normalizedTask);
        response.setRecommendedImplementationOrder(implOrder);

        // Build files to modify
        List<String> filesToModify = buildFilesToModify(taskType, taskAnalysis, impactAnalysis, normalizedTask);
        response.setFilesToModify(filesToModify);

        // Build files to review
        List<String> filesToReview = buildFilesToReview(taskType, taskAnalysis, impactAnalysis, normalizedTask);
        response.setFilesToReview(filesToReview);

        // Build components affected
        List<String> componentsAffected = buildComponentsAffected(taskType, impactAnalysis, normalizedTask);
        response.setComponentsAffected(componentsAffected);

        // Build dependencies involved
        List<String> dependenciesInvolved = buildDependencies(taskType, impactAnalysis, normalizedTask);
        response.setDependenciesInvolved(dependenciesInvolved);

        // Build validation steps
        List<String> validationSteps = buildValidationSteps(taskType, normalizedTask);
        response.setSuggestedValidationSteps(validationSteps);

        // Build testing scope
        String testingScope = buildTestingScope(taskType, impactAnalysis, normalizedTask);
        response.setSuggestedTestingScope(testingScope);

        // Build risks
        List<String> risks = buildRisks(taskType, impactAnalysis, normalizedTask);
        response.setRisks(risks);

        // Build assumptions
        List<String> assumptions = buildAssumptions(taskType, taskAnalysis, normalizedTask);
        response.setAssumptions(assumptions);

        logger.info("Implementation plan generated: type={}, complexity={}, steps={}",
                taskType, complexity, implOrder.size());

        return response;
    }

    /**
     * Builds a deterministic, ordered list of implementation steps based on task type.
     */
    private List<String> buildRecommendedOrder(
            TaskType taskType, TaskAnalysisResponse analysis, String normalizedTask) {
        List<String> order = new ArrayList<>();
        int stepNumber = 1;

        // Get template steps for this task type
        List<String> templateSteps = IMPLEMENTATION_ORDER.getOrDefault(taskType, IMPLEMENTATION_ORDER.get(null));

        // Add detected entities as context
        if (analysis.getDetectedEntities() != null && !analysis.getDetectedEntities().isEmpty()) {
            String entityContext = "Review detected entities: " +
                    String.join(", ", analysis.getDetectedEntities());
            order.add(stepNumber++ + ". " + entityContext);
        }

        // Add template steps
        for (String step : templateSteps) {
            order.add(stepNumber++ + ". " + step);
        }

        // Add task-type-specific refinement steps
        List<String> refinements = getTypeSpecificSteps(taskType, normalizedTask);
        for (String refinement : refinements) {
            if (!containsDuplicate(order, refinement)) {
                order.add(stepNumber++ + ". " + refinement);
            }
        }

        // Add final verification step
        order.add(stepNumber + ". Final verification: Run full test suite and verify " +
                "no regressions are introduced");

        return order;
    }

    /**
     * Builds the list of files that are expected to be modified.
     */
    private List<String> buildFilesToModify(
            TaskType taskType, TaskAnalysisResponse analysis,
            ImpactAnalysisResponse impactAnalysis, String normalizedTask) {
        Set<String> files = new LinkedHashSet<>();

        // Add primary targets from impact analysis
        if (impactAnalysis != null && impactAnalysis.getPrimaryTargets() != null) {
            for (String target : impactAnalysis.getPrimaryTargets()) {
                files.add(mapTargetToFile(target, taskType));
            }
        }

        // Add directly affected components as potential files
        if (impactAnalysis != null && impactAnalysis.getDirectlyAffectedComponents() != null) {
            for (ImpactedComponent component : impactAnalysis.getDirectlyAffectedComponents()) {
                String file = mapComponentToFile(component, taskType);
                if (file != null) {
                    files.add(file);
                }
            }
        }

        // If no files identified, add a generic placeholder based on task type
        if (files.isEmpty()) {
            files.addAll(getDefaultFilesToModify(taskType, normalizedTask));
        }

        return new ArrayList<>(files);
    }

    /**
     * Builds the list of files that should be reviewed.
     */
    private List<String> buildFilesToReview(
            TaskType taskType, TaskAnalysisResponse analysis,
            ImpactAnalysisResponse impactAnalysis, String normalizedTask) {
        Set<String> files = new LinkedHashSet<>();

        // Add indirectly affected components
        if (impactAnalysis != null && impactAnalysis.getIndirectlyAffectedComponents() != null) {
            for (ImpactedComponent component : impactAnalysis.getIndirectlyAffectedComponents()) {
                String file = mapComponentToFile(component, taskType);
                if (file != null && !"Class".equals(component.getComponentType())) {
                    files.add(file);
                }
            }
        }

        // Add entity files mentioned in task analysis
        if (analysis.getDetectedEntities() != null) {
            for (String entity : analysis.getDetectedEntities()) {
                if (entity.contains("(Configuration)") || entity.contains("(DTO)")) {
                    files.add("Review configuration: " + entity);
                }
            }
        }

        // Add testing files
        files.add("Review existing tests for all affected components");

        // If no files identified, add defaults
        if (files.isEmpty()) {
            files.addAll(getDefaultFilesToReview(taskType, normalizedTask));
        }

        return new ArrayList<>(files);
    }

    /**
     * Builds the list of components affected by the change.
     */
    private List<String> buildComponentsAffected(
            TaskType taskType, ImpactAnalysisResponse impactAnalysis, String normalizedTask) {
        Set<String> components = new LinkedHashSet<>();

        // Add primary targets
        if (impactAnalysis != null && impactAnalysis.getPrimaryTargets() != null) {
            components.addAll(impactAnalysis.getPrimaryTargets());
        }

        // Add directly affected component names
        if (impactAnalysis != null && impactAnalysis.getDirectlyAffectedComponents() != null) {
            for (ImpactedComponent component : impactAnalysis.getDirectlyAffectedComponents()) {
                components.add(component.getComponentName() + " (" + component.getComponentType() + ")");
            }
        }

        // Add indirectly affected component names
        if (impactAnalysis != null && impactAnalysis.getIndirectlyAffectedComponents() != null) {
            for (ImpactedComponent component : impactAnalysis.getIndirectlyAffectedComponents()) {
                String entry = component.getComponentName() + " (" + component.getComponentType() + ")";
                if (!components.contains(entry)) {
                    components.add(entry);
                }
            }
        }

        // If no components identified, add defaults
        if (components.isEmpty()) {
            components.add(getDefaultComponent(taskType));
        }

        return new ArrayList<>(components);
    }

    /**
     * Builds the list of dependencies involved in the change.
     */
    private List<String> buildDependencies(
            TaskType taskType, ImpactAnalysisResponse impactAnalysis, String normalizedTask) {
        Set<String> deps = new LinkedHashSet<>();

        // Add dependency impact from impact analysis
        if (impactAnalysis != null && impactAnalysis.getDependencyImpact() != null) {
            deps.addAll(impactAnalysis.getDependencyImpact());
        }

        // Add type-specific dependencies
        if (containsAny(normalizedTask, "database", "sql", "entity", "repository")) {
            deps.add("Database schema dependencies");
            deps.add("ORM/Hibernate dependencies");
        }

        if (taskType == TaskType.REST_API_CHANGE || containsAny(normalizedTask, "api", "endpoint")) {
            deps.add("REST API contract dependencies");
            deps.add("API specification dependencies");
        }

        if (containsAny(normalizedTask, "spring", "bean", "autowired")) {
            deps.add("Spring dependency injection wiring");
        }

        // Ensure at least one dependency listed
        if (deps.isEmpty()) {
            deps.add("Internal module dependencies");
        }

        return new ArrayList<>(deps);
    }

    /**
     * Builds validation steps appropriate for the task type.
     */
    private List<String> buildValidationSteps(TaskType taskType, String normalizedTask) {
        List<String> steps = new ArrayList<>();

        // Get template validation steps for this task type
        List<String> templateSteps = VALIDATION_STEPS.getOrDefault(taskType, VALIDATION_STEPS.get(null));
        steps.addAll(templateSteps);

        // Add additional validation based on keywords
        if (containsAny(normalizedTask, "security", "authentication", "authorization")) {
            steps.add("Validate security requirements and permissions");
        }

        if (containsAny(normalizedTask, "performance", "optimize", "slow")) {
            steps.add("Validate performance benchmarks against baseline");
        }

        return steps;
    }

    /**
     * Builds the suggested testing scope.
     */
    private String buildTestingScope(
            TaskType taskType, ImpactAnalysisResponse impactAnalysis, String normalizedTask) {
        // If impact analysis is available, derive from its testing scope
        if (impactAnalysis != null && impactAnalysis.getEstimatedTestingScope() != null) {
            String scope = impactAnalysis.getEstimatedTestingScope().name();
            switch (scope) {
                case "SMALL":
                    return "Small scope: Unit tests for directly affected components only";
                case "MEDIUM":
                    return "Medium scope: Unit tests and integration tests for affected components";
                case "LARGE":
                    return "Large scope: Comprehensive unit tests, integration tests, and end-to-end validation";
                default:
                    break;
            }
        }

        // Fallback based on task type
        if (taskType == TaskType.BUG_FIX || taskType == TaskType.CONFIGURATION_CHANGE) {
            return "Medium scope: Unit tests for the fix or configuration change";
        }
        if (taskType == TaskType.DATABASE_CHANGE || taskType == TaskType.REFACTORING
                || taskType == TaskType.PERFORMANCE_IMPROVEMENT) {
            return "Large scope: Comprehensive unit tests, integration tests, and end-to-end validation";
        }
        return "Medium scope: Unit tests and integration tests for affected components";
    }

    /**
     * Builds the list of risks associated with the implementation.
     */
    private List<String> buildRisks(
            TaskType taskType, ImpactAnalysisResponse impactAnalysis, String normalizedTask) {
        Set<String> risks = new LinkedHashSet<>();

        // Add risks from impact analysis
        if (impactAnalysis != null && impactAnalysis.getPotentialRisks() != null) {
            for (RiskItem risk : impactAnalysis.getPotentialRisks()) {
                risks.add(risk.getDescription() + " [Mitigation: " + risk.getMitigation() + "]");
            }
        }

        // Add type-specific risks if impact analysis was unavailable
        if (impactAnalysis == null) {
            switch (taskType) {
                case DATABASE_CHANGE:
                    risks.add("Data migration errors could cause data loss [Mitigation: Implement thorough validation]");
                    break;
                case REFACTORING:
                    risks.add("Refactoring may introduce behavioral changes [Mitigation: Ensure comprehensive test coverage]");
                    break;
                case REST_API_CHANGE:
                    risks.add("API changes may break existing clients [Mitigation: Maintain backward compatibility]");
                    break;
                default:
                    risks.add("Limited information available for risk assessment [Mitigation: Review codebase manually]");
                    break;
            }
        }

        return new ArrayList<>(risks);
    }

    /**
     * Builds the list of assumptions made during planning.
     */
    private List<String> buildAssumptions(
            TaskType taskType, TaskAnalysisResponse analysis, String normalizedTask) {
        List<String> assumptions = new ArrayList<>();

        // General assumptions
        assumptions.add("Task analysis is based on keyword detection and may not capture all nuances");
        assumptions.add("Repository context may be incomplete without a working indexer connection");
        assumptions.add("Implementation order follows a logical progression but may need adjustment");

        // Type-specific assumptions
        switch (taskType) {
            case BUG_FIX:
                assumptions.add("Bug reproduction steps are available");
                assumptions.add("The root cause can be identified from the task description");
                break;
            case NEW_FEATURE:
                assumptions.add("Feature requirements are fully specified in the task description");
                assumptions.add("New feature does not conflict with existing functionality");
                break;
            case REFACTORING:
                assumptions.add("Existing test coverage is sufficient to prevent regressions");
                assumptions.add("External API contracts remain unchanged unless specified");
                break;
            case REST_API_CHANGE:
                assumptions.add("API consumers will be notified of the change");
                assumptions.add("API versioning strategy is in place");
                break;
            case DATABASE_CHANGE:
                assumptions.add("Database schema changes are backward-compatible");
                assumptions.add("Migration scripts can be rolled back if needed");
                break;
            case CONFIGURATION_CHANGE:
                assumptions.add("Configuration changes are environment-agnostic");
                assumptions.add("Configuration values are validated before deployment");
                break;
            case PERFORMANCE_IMPROVEMENT:
                assumptions.add("Performance baselines are available for comparison");
                assumptions.add("Performance improvements do not introduce functional regressions");
                break;
            case UNIT_TEST:
                assumptions.add("Test framework and dependencies are already configured");
                assumptions.add("Code under test is designed to be testable");
                break;
            case DOCUMENTATION:
                assumptions.add("Documentation generation tools are available");
                assumptions.add("Documentation changes do not affect runtime behavior");
                break;
            default:
                assumptions.add("Task type is unknown; general implementation steps apply");
                break;
        }

        return assumptions;
    }

    // --- Private helper methods ---

    /**
     * Builds the static implementation order map.
     */
    private static Map<TaskType, List<String>> buildImplementationOrder() {
        Map<TaskType, List<String>> map = new LinkedHashMap<>();

        // Default order (used for UNKNOWN)
        map.put(null, List.of(
                "Analyze the repository structure to understand the codebase",
                "Search the codebase for relevant existing implementations",
                "Identify components related to the task",
                "Review existing tests for affected components",
                "Implement the necessary changes",
                "Add or update tests for the changes",
                "Run the test suite to verify correctness"
        ));

        // New Feature
        map.put(TaskType.NEW_FEATURE, List.of(
                "Analyze the repository structure to understand existing patterns",
                "Search the codebase for similar feature implementations",
                "Identify where the new feature should be integrated",
                "Review relevant service and controller layer code",
                "Implement the data model or entity changes if needed",
                "Implement the service layer for the new feature",
                "Implement the controller or API endpoints",
                "Add unit tests for the new feature",
                "Run integration tests to verify the feature works end-to-end"
        ));

        // Bug Fix
        map.put(TaskType.BUG_FIX, List.of(
                "Reproduce the bug and understand the expected behavior",
                "Search the codebase for the affected code paths",
                "Identify the root cause of the bug",
                "Review the relevant components and their dependencies",
                "Implement the fix in the affected component",
                "Add regression tests to prevent future occurrences",
                "Run full test suite to verify the fix does not introduce regressions"
        ));

        // Refactoring
        map.put(TaskType.REFACTORING, List.of(
                "Analyze the current code structure and identify areas for improvement",
                "Review existing tests to understand the expected behavior",
                "Identify all callers and consumers of the refactored components",
                "Implement the refactoring changes gradually",
                "Update callers to use the new structure",
                "Run full test suite to verify no behavioral changes",
                "Update documentation if public APIs changed"
        ));

        // REST API Change
        map.put(TaskType.REST_API_CHANGE, List.of(
                "Analyze the existing API contract and endpoints",
                "Review the controller and service layer code",
                "Update the controller endpoint definitions",
                "Implement or modify the service layer logic",
                "Update request/response DTOs if needed",
                "Update API documentation or specification",
                "Add or update API tests",
                "Verify backward compatibility with existing clients"
        ));

        // Database Change
        map.put(TaskType.DATABASE_CHANGE, List.of(
                "Analyze the existing database schema and entity definitions",
                "Review entity classes and their relationships",
                "Review database migration scripts",
                "Implement entity changes",
                "Create new database migration scripts",
                "Update repository and query methods",
                "Add tests for the new data access logic",
                "Verify database migration rollback strategy"
        ));

        // Performance Improvement
        map.put(TaskType.PERFORMANCE_IMPROVEMENT, List.of(
                "Establish performance baselines for the affected components",
                "Analyze the codebase for performance bottlenecks",
                "Review caching and optimization strategies in use",
                "Implement performance optimizations",
                "Run performance benchmarks to validate improvements",
                "Verify no functional regressions through existing tests"
        ));

        // Configuration Change
        map.put(TaskType.CONFIGURATION_CHANGE, List.of(
                "Analyze the current configuration structure",
                "Identify all environments where the configuration is used",
                "Update configuration properties or files",
                "Review dependent components for configuration changes",
                "Validate configuration across different environments"
        ));

        // Unit Test
        map.put(TaskType.UNIT_TEST, List.of(
                "Identify the classes and methods that need test coverage",
                "Review the existing test infrastructure and patterns",
                "Create test classes following the project's test conventions",
                "Implement test cases for normal scenarios",
                "Implement test cases for edge cases and error scenarios",
                "Run tests to verify they pass correctly"
        ));

        // Documentation
        map.put(TaskType.DOCUMENTATION, List.of(
                "Identify the components or features that need documentation updates",
                "Review existing documentation for consistency",
                "Update or create documentation following existing patterns",
                "Review documentation for accuracy and completeness",
                "Verify documentation renders correctly"
        ));

        return map;
    }

    /**
     * Builds the static validation steps map.
     */
    private static Map<TaskType, List<String>> buildValidationSteps() {
        Map<TaskType, List<String>> map = new LinkedHashMap<>();

        List<String> defaultSteps = List.of(
                "Verify the changes compile without errors",
                "Run unit tests for the affected components",
                "Run the full project test suite",
                "Perform code review of the changes"
        );

        map.put(null, defaultSteps);

        map.put(TaskType.NEW_FEATURE, List.of(
                "Verify the feature works as described in the requirements",
                "Run unit tests for the new feature",
                "Run integration tests to verify feature integration",
                "Validate edge cases and error handling",
                "Verify the feature does not break existing functionality",
                "Perform code review of the new feature"
        ));

        map.put(TaskType.BUG_FIX, List.of(
                "Verify the bug is no longer reproducible",
                "Run regression tests to confirm no new issues introduced",
                "Verify the fix handles edge cases correctly",
                "Run the full test suite to check for regressions",
                "Perform code review of the fix"
        ));

        map.put(TaskType.REFACTORING, List.of(
                "Verify the behavior remains unchanged after refactoring",
                "Run existing tests to confirm no behavioral changes",
                "Verify all callers work with the refactored components",
                "Check for any compilation or import issues",
                "Perform code review of the refactoring"
        ));

        map.put(TaskType.REST_API_CHANGE, List.of(
                "Verify new or modified endpoints respond correctly",
                "Run API integration tests",
                "Verify backward compatibility with existing clients",
                "Check API documentation accuracy",
                "Validate request/response serialization",
                "Perform code review of the API changes"
        ));

        map.put(TaskType.DATABASE_CHANGE, List.of(
                "Verify database migrations execute without errors",
                "Roll back migration to verify rollback strategy works",
                "Run data access tests to verify queries work correctly",
                "Validate data integrity constraints",
                "Check migration scripts for potential data loss",
                "Perform code review of the database changes"
        ));

        map.put(TaskType.PERFORMANCE_IMPROVEMENT, List.of(
                "Run performance benchmarks to validate improvements",
                "Compare results with established baselines",
                "Verify no functional regressions",
                "Run the full test suite",
                "Perform code review of the optimization"
        ));

        map.put(TaskType.CONFIGURATION_CHANGE, List.of(
                "Verify configuration changes in a development environment first",
                "Validate configuration changes across all environments",
                "Check for any dependent components affected by the change",
                "Verify the application starts correctly with the new configuration",
                "Perform code review of the configuration changes"
        ));

        map.put(TaskType.UNIT_TEST, List.of(
                "Verify all test cases pass",
                "Check test coverage for the intended scenarios",
                "Validate edge cases are covered",
                "Verify tests are deterministic and repeatable",
                "Perform code review of the test code"
        ));

        map.put(TaskType.DOCUMENTATION, List.of(
                "Verify documentation accuracy against actual implementation",
                "Check for consistent formatting and style",
                "Review the documentation for completeness",
                "Verify all links and references are correct",
                "Perform peer review of the documentation"
        ));

        return map;
    }

    /**
     * Returns type-specific additional implementation steps.
     */
    private List<String> getTypeSpecificSteps(TaskType taskType, String normalizedTask) {
        List<String> steps = new ArrayList<>();

        if (taskType == TaskType.NEW_FEATURE) {
            if (containsAny(normalizedTask, "database", "entity", "jpa")) {
                steps.add("Create or modify database migration scripts");
            }
        }

        if (taskType == TaskType.REST_API_CHANGE && containsAny(normalizedTask, "error", "exception", "validation")) {
            steps.add("Implement error handling for new API responses");
        }

        if (taskType == TaskType.DATABASE_CHANGE) {
            steps.add("Review data migration strategy for existing records");
        }

        if (taskType == TaskType.BUG_FIX && containsAny(normalizedTask, "null", "npe", "nullpointer")) {
            steps.add("Add null safety checks and validation");
        }

        return steps;
    }

    /**
     * Maps a target name to a suggested file path.
     */
    private String mapTargetToFile(String target, TaskType taskType) {
        switch (target) {
            case "REST API layer":
                return "Controller classes";
            case "Service layer":
                return "Service classes";
            case "Data access layer":
                return "Repository/DAO classes";
            case "Configuration":
                return "Configuration files (application.yml, properties, config classes)";
            default:
                if (target.startsWith("Endpoint: ")) {
                    return "Controller: " + target.substring("Endpoint: ".length());
                }
                if (target.contains(" (")) {
                    int idx = target.indexOf(" (");
                    return target.substring(0, idx) + " class";
                }
                return target;
        }
    }

    /**
     * Maps a component to a suggested file path.
     */
    private String mapComponentToFile(ImpactedComponent component, TaskType taskType) {
        String name = component.getComponentName();
        String type = component.getComponentType();

        if ("Testing".equals(type)) {
            return "Test files";
        }
        if ("Documentation".equals(type)) {
            return "Documentation files";
        }
        if ("Configuration".equals(type)) {
            return name + " (configuration)";
        }
        if ("REST API".equals(type)) {
            return name + " (API endpoint)";
        }
        if ("DTO".equals(type)) {
            return name + " (DTO class)";
        }
        if ("Method".equals(type)) {
            return "Method " + name + " in its containing class";
        }
        if ("Package".equals(type)) {
            return name + " package";
        }

        // Default: treat as class
        return name + " class";
    }

    /**
     * Returns default files to modify when no specific files are identified.
     */
    private List<String> getDefaultFilesToModify(TaskType taskType, String normalizedTask) {
        List<String> defaults = new ArrayList<>();
        switch (taskType) {
            case BUG_FIX:
                defaults.add("Bug-affected component class");
                break;
            case NEW_FEATURE:
                defaults.add("New feature implementation class");
                break;
            case REST_API_CHANGE:
                defaults.add("REST Controller class");
                defaults.add("Service class");
                break;
            case DATABASE_CHANGE:
                defaults.add("Entity class");
                defaults.add("Database migration script");
                break;
            case CONFIGURATION_CHANGE:
                defaults.add("Configuration file (application.yml/properties)");
                break;
            case REFACTORING:
                defaults.add("Target component class");
                break;
            case PERFORMANCE_IMPROVEMENT:
                defaults.add("Performance-critical component class");
                break;
            case UNIT_TEST:
                defaults.add("Test class");
                break;
            case DOCUMENTATION:
                defaults.add("Documentation file");
                break;
            default:
                defaults.add("Component to be modified");
                break;
        }
        return defaults;
    }

    /**
     * Returns default files to review when no specific files are identified.
     */
    private List<String> getDefaultFilesToReview(TaskType taskType, String normalizedTask) {
        List<String> defaults = new ArrayList<>();
        defaults.add("Review existing tests for all affected components");
        switch (taskType) {
            case NEW_FEATURE:
                defaults.add("Review similar feature implementations for consistency");
                break;
            case BUG_FIX:
                defaults.add("Review code paths related to the bug");
                break;
            case REFACTORING:
                defaults.add("Review all callers of the refactored component");
                break;
            case REST_API_CHANGE:
                defaults.add("Review API documentation and client code");
                break;
            case DATABASE_CHANGE:
                defaults.add("Review related entities and their relationships");
                break;
            default:
                defaults.add("Review relevant configuration and documentation");
                break;
        }
        return defaults;
    }

    /**
     * Returns a default component description for the task type.
     */
    private String getDefaultComponent(TaskType taskType) {
        switch (taskType) {
            case NEW_FEATURE:
                return "New feature components";
            case BUG_FIX:
                return "Bug-affected components";
            case REFACTORING:
                return "Target components for refactoring";
            case REST_API_CHANGE:
                return "REST API components";
            case DATABASE_CHANGE:
                return "Database-related components";
            case CONFIGURATION_CHANGE:
                return "Configuration components";
            case PERFORMANCE_IMPROVEMENT:
                return "Performance-critical components";
            case UNIT_TEST:
                return "Test components";
            case DOCUMENTATION:
                return "Documentation components";
            default:
                return "Affected components";
        }
    }

    /**
     * Checks if the text contains any of the given words.
     */
    private boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the list already contains a string ending with the given step.
     */
    private boolean containsDuplicate(List<String> list, String step) {
        for (String item : list) {
            if (item.contains(step)) {
                return true;
            }
        }
        return false;
    }
}