package com.projectiq.mcp.analysis.service;

import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse.ImpactedComponent;
import com.projectiq.mcp.analysis.dto.ImplementationPlanningResponse;
import com.projectiq.mcp.analysis.dto.TaskAnalysisResponse;
import com.projectiq.mcp.analysis.dto.TaskType;
import com.projectiq.mcp.analysis.dto.TestImpactAnalysisResponse;
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
 * Service that performs deterministic Test Impact Analysis for a proposed
 * repository change. This service analyzes a development task, determines
 * impacted production components, identifies related test classes, and
 * recommends the minimum test scope.
 *
 * <p>This service relies on {@link TaskAnalysisService}, {@link ContextAssemblyService},
 * {@link ImpactAnalysisService}, and {@link ImplementationPlanningService} to
 * gather the necessary analysis data. All outputs are deterministic, stable,
 * and free of duplicate entries.</p>
 *
 * <p>This service NEVER executes any tests. It only recommends which tests
 * should be considered for execution or update.</p>
 */
@Service
public class TestImpactAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(TestImpactAnalysisService.class);

    private final TaskAnalysisService taskAnalysisService;
    private final ContextAssemblyService contextAssemblyService;
    private final ImpactAnalysisService impactAnalysisService;
    private final ImplementationPlanningService implementationPlanningService;

    // --- Patterns for test class detection from production class names ---

    private static final Pattern TEST_SUFFIX_PATTERN = Pattern.compile(
            "^(Test.+|.+Test)$|^(Test.+)"
    );

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

    // --- Deterministic test effort mapping ---

    private static final Map<TaskType, String> EFFORT_BY_TASK_TYPE = buildEffortByTaskType();

    // --- Deterministic confidence mapping ---

    private static final Map<TaskType, String> CONFIDENCE_BY_TASK_TYPE = buildConfidenceByTaskType();

    // --- Deterministic test execution order templates ---

    private static final List<String> DEFAULT_TEST_ORDER = List.of(
            "1. Unit tests for directly affected production classes",
            "2. Integration tests for affected service and repository layers",
            "3. Spring Boot context tests for configuration changes",
            "4. Controller tests for REST API endpoint changes",
            "5. Repository tests for data access layer changes",
            "6. End-to-end tests for complete feature validation"
    );

    public TestImpactAnalysisService(
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
     * Analyzes the test impact of a proposed development task. Invokes task
     * analysis, context assembly, impact analysis, and implementation planning
     * to produce a structured test impact report.
     *
     * @param task           the natural language development request
     * @param repositoryName the repository name
     * @param branch         the git branch (optional, defaults to "main")
     * @return a structured test impact analysis response
     * @throws IllegalArgumentException if the task is null or empty
     */
    public TestImpactAnalysisResponse analyzeTestImpact(
            String task, String repositoryName, String branch) {
        if (task == null || task.trim().isEmpty()) {
            throw new IllegalArgumentException("Task description cannot be null or empty");
        }

        logger.info("Analyzing test impact for task: {} in repository: {}", task, repositoryName);

        String effectiveBranch = (branch != null && !branch.trim().isEmpty()) ? branch.trim() : "main";
        String normalizedTask = task.trim().toLowerCase();

        // Step 1: Analyze the task
        TaskAnalysisResponse taskAnalysis = taskAnalysisService.analyze(task);
        TaskType taskType = taskAnalysis.getTaskType();

        // Step 2: Assemble repository context (non-critical)
        try {
            contextAssemblyService.assembleContext(task.trim(), repositoryName, effectiveBranch);
        } catch (Exception e) {
            logger.warn("Failed to assemble repository context: {}", e.getMessage());
        }

        // Step 3: Analyze impact (non-critical)
        ImpactAnalysisResponse impactAnalysis = null;
        try {
            impactAnalysis = impactAnalysisService.analyzeImpact(task.trim(), repositoryName, effectiveBranch);
        } catch (Exception e) {
            logger.warn("Failed to analyze impact: {}", e.getMessage());
        }

        // Step 4: Generate implementation plan (non-critical)
        ImplementationPlanningResponse implPlan = null;
        try {
            implPlan = implementationPlanningService.generatePlan(task.trim(), repositoryName, effectiveBranch);
        } catch (Exception e) {
            logger.warn("Failed to generate implementation plan: {}", e.getMessage());
        }

        // Step 5: Build test impact analysis response
        TestImpactAnalysisResponse response = new TestImpactAnalysisResponse();
        response.setOriginalTask(task.trim());

        // Step 6: Determine affected production classes
        List<String> affectedClasses = determineAffectedProductionClasses(
                task, normalizedTask, taskType, taskAnalysis, impactAnalysis);
        response.setAffectedProductionClasses(affectedClasses);

        // Step 7: Identify related test classes
        List<String> relatedTests = identifyRelatedTestClasses(
                task, normalizedTask, taskType, affectedClasses, impactAnalysis);
        response.setRelatedTestClasses(relatedTests);

        // Step 8: Identify missing tests
        List<String> missingTests = identifyMissingTests(
                taskType, normalizedTask, affectedClasses, relatedTests);
        response.setMissingTests(missingTests);

        // Step 9: Build recommended test execution order
        List<String> testOrder = buildTestExecutionOrder(taskType, normalizedTask, affectedClasses);
        response.setRecommendedTestExecutionOrder(testOrder);

        // Step 10: Estimate testing effort
        String effort = estimateTestingEffort(taskType, affectedClasses, relatedTests, missingTests);
        response.setEstimatedTestingEffort(effort);

        // Step 11: Determine confidence level
        String confidence = determineConfidence(taskType, affectedClasses, impactAnalysis);
        response.setConfidenceLevel(confidence);

        // Step 12: Generate testing rationale
        String rationale = generateTestingRationale(
                taskType, normalizedTask, affectedClasses, relatedTests, missingTests);
        response.setTestingRationale(rationale);

        logger.info("Test impact analysis complete: type={}, affected={}, tests={}, effort={}, confidence={}",
                taskType, affectedClasses.size(), relatedTests.size(), effort, confidence);

        return response;
    }

    /**
     * Determines the production classes that will be affected by the proposed change.
     */
    List<String> determineAffectedProductionClasses(
            String task, String normalizedTask, TaskType taskType,
            TaskAnalysisResponse taskAnalysis, ImpactAnalysisResponse impactAnalysis) {
        Set<String> classes = new LinkedHashSet<>();

        // Extract directly mentioned production classes from the task
        addPatternMatches(task, CONTROLLER_PATTERN, classes);
        addPatternMatches(task, SERVICE_PATTERN, classes);
        addPatternMatches(task, REPOSITORY_PATTERN, classes);
        addPatternMatches(task, ENTITY_PATTERN, classes);

        // Add directly affected components from impact analysis
        if (impactAnalysis != null && impactAnalysis.getDirectlyAffectedComponents() != null) {
            for (ImpactedComponent component : impactAnalysis.getDirectlyAffectedComponents()) {
                String name = component.getComponentName();
                String type = component.getComponentType();
                if ("Class".equals(type) || "Configuration".equals(type)
                        || "DTO".equals(type) || "REST API".equals(type)) {
                    // Clean up prefix if present
                    if (name.startsWith("Endpoint: ")) {
                        classes.add("Controller (" + name.substring("Endpoint: ".length()) + ")");
                    } else {
                        classes.add(name + " (" + type + ")");
                    }
                }
            }
        }

        // Add indirectly affected components that are production classes
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

        // Add classes from task analysis detected entities
        if (taskAnalysis.getDetectedEntities() != null) {
            for (String entity : taskAnalysis.getDetectedEntities()) {
                if (entity.contains("(Controller)") || entity.contains("(Service)")
                        || entity.contains("(Repository)") || entity.contains("(Entity/Model)")
                        || entity.contains("(DTO)") || entity.contains("(Configuration)")) {
                    classes.add(entity);
                }
            }
        }

        // If still empty, derive default based on task type
        if (classes.isEmpty()) {
            classes.addAll(getDefaultAffectedClasses(taskType, normalizedTask));
        }

        return new ArrayList<>(classes);
    }

    /**
     * Identifies test classes related to the affected production classes.
     * Uses naming conventions to derive test class names:
     * - For class XxxController -> XxxControllerTest (controller test)
     * - For class XxxService -> XxxServiceTest (service test)
     * - For class XxxRepository -> XxxRepositoryTest (repository test)
     * - For class XxxEntity -> XxxEntityTest (entity test)
     * - General pattern: Xxx -> XxxTest
     */
    List<String> identifyRelatedTestClasses(
            String task, String normalizedTask, TaskType taskType,
            List<String> affectedClasses, ImpactAnalysisResponse impactAnalysis) {
        Set<String> tests = new LinkedHashSet<>();

        // Derive test classes from affected production classes
        for (String affectedClass : affectedClasses) {
            String cleanName = affectedClass;
            // Remove category suffix like " (Controller)", " (Service)", etc.
            int parenIdx = cleanName.indexOf(" (");
            if (parenIdx > 0) {
                cleanName = cleanName.substring(0, parenIdx);
            }
            // Remove "Class: " prefix if present
            if (cleanName.startsWith("Class: ")) {
                cleanName = cleanName.substring("Class: ".length());
            }

            // Generate test class name based on naming convention
            String testClass = deriveTestClassName(cleanName);
            if (testClass != null) {
                tests.add(testClass);
            }
        }

        // Add task-type-specific test classes
        tests.addAll(getTypeSpecificTests(taskType, normalizedTask, affectedClasses));

        // Add testing-related items from impact analysis indirectly affected
        if (impactAnalysis != null && impactAnalysis.getIndirectlyAffectedComponents() != null) {
            for (ImpactedComponent component : impactAnalysis.getIndirectlyAffectedComponents()) {
                if ("Testing".equals(component.getComponentType())) {
                    String name = component.getComponentName();
                    if ("Unit tests".equals(name)) {
                        // Already covered by derived tests
                    } else if ("Integration tests".equals(name)) {
                        tests.add("Integration tests for affected features");
                    }
                }
            }
        }

        // Add a generic end-to-end test if applicable
        if (taskType == TaskType.REST_API_CHANGE || taskType == TaskType.NEW_FEATURE) {
            tests.add("End-to-end tests for " + taskType.getDisplayName().toLowerCase());
        }

        return new ArrayList<>(tests);
    }

    /**
     * Identifies tests that are missing (should exist but may not yet be created).
     */
    List<String> identifyMissingTests(
            TaskType taskType, String normalizedTask,
            List<String> affectedClasses, List<String> relatedTests) {
        Set<String> missing = new LinkedHashSet<>();

        // For new features, tests typically don't exist yet
        if (taskType == TaskType.NEW_FEATURE) {
            for (String affectedClass : affectedClasses) {
                String cleanName = affectedClass;
                int parenIdx = cleanName.indexOf(" (");
                if (parenIdx > 0) {
                    cleanName = cleanName.substring(0, parenIdx);
                }
                if (cleanName.startsWith("Class: ")) {
                    cleanName = cleanName.substring("Class: ".length());
                }
                String testName = deriveTestClassName(cleanName);
                if (testName != null && !testName.contains("Integration")
                        && !testName.contains("End-to-end")) {
                    missing.add("Missing: " + testName + " (may need to be created)");
                }
            }
        }

        // For bug fixes, may need regression tests
        if (taskType == TaskType.BUG_FIX) {
            missing.add("Regression tests for the fixed bug scenario");
        }

        // For refactoring, may need additional coverage
        if (taskType == TaskType.REFACTORING) {
            missing.add("Additional unit tests to cover refactored code paths");
        }

        // For REST API changes, may need API contract tests
        if (taskType == TaskType.REST_API_CHANGE) {
            missing.add("API contract/integration tests for new or modified endpoints");
        }

        // For database changes, may need migration tests
        if (taskType == TaskType.DATABASE_CHANGE) {
            missing.add("Database migration and rollback tests");
        }

        // For performance changes, may need benchmark tests
        if (taskType == TaskType.PERFORMANCE_IMPROVEMENT) {
            missing.add("Performance benchmark tests to validate improvements");
        }

        // Check for entity tests if entities are affected
        for (String affectedClass : affectedClasses) {
            if (affectedClass.contains("Entity") || affectedClass.contains("Model")) {
                missing.add("Entity validation tests for data integrity");
                break;
            }
        }

        return new ArrayList<>(missing);
    }

    /**
     * Builds a deterministic, ordered list of test execution recommendations.
     */
    List<String> buildTestExecutionOrder(
            TaskType taskType, String normalizedTask, List<String> affectedClasses) {
        List<String> order = new ArrayList<>();

        // Start with type-specific test order adjustments
        List<String> baseOrder = getTestExecutionOrderForType(taskType, normalizedTask);
        order.addAll(baseOrder);

        // Add class-specific test suggestions
        for (String affectedClass : affectedClasses) {
            String cleanName = affectedClass;
            int parenIdx = cleanName.indexOf(" (");
            if (parenIdx > 0) {
                cleanName = cleanName.substring(0, parenIdx);
            }
            if (cleanName.startsWith("Class: ")) {
                cleanName = cleanName.substring("Class: ".length());
            }
            String testName = deriveTestClassName(cleanName);
            if (testName != null) {
                String step = "Execute " + testName;
                if (!order.contains(step)) {
                    order.add(step);
                }
            }
        }

        // Add final verification step
        order.add("Final verification: Run full test suite to check for regressions");

        return order;
    }

    /**
     * Estimates the testing effort as Low, Medium, or High.
     */
    String estimateTestingEffort(
            TaskType taskType, List<String> affectedClasses,
            List<String> relatedTests, List<String> missingTests) {
        int score = 0;

        // Base score from task type
        String baseEffort = EFFORT_BY_TASK_TYPE.getOrDefault(taskType, "Low");
        switch (baseEffort) {
            case "High":
                score += 3;
                break;
            case "Medium":
                score += 2;
                break;
            default:
                score += 1;
                break;
        }

        // Score based on number of affected classes
        int classCount = affectedClasses.size();
        if (classCount >= 5) {
            score += 3;
        } else if (classCount >= 3) {
            score += 2;
        } else if (classCount >= 1) {
            score += 1;
        }

        // Score based on number of missing tests
        int missingCount = missingTests.size();
        if (missingCount >= 3) {
            score += 2;
        } else if (missingCount >= 1) {
            score += 1;
        }

        if (score >= 6) {
            return "High";
        } else if (score >= 3) {
            return "Medium";
        } else {
            return "Low";
        }
    }

    /**
     * Determines the confidence level of the test impact analysis.
     */
    String determineConfidence(
            TaskType taskType, List<String> affectedClasses,
            ImpactAnalysisResponse impactAnalysis) {
        // Base confidence from task type
        String baseConfidence = CONFIDENCE_BY_TASK_TYPE.getOrDefault(taskType, "Medium");

        // Adjust based on available data
        boolean hasImpactData = impactAnalysis != null
                && impactAnalysis.getDirectlyAffectedComponents() != null
                && !impactAnalysis.getDirectlyAffectedComponents().isEmpty();

        boolean hasClasses = !affectedClasses.isEmpty();

        if (hasImpactData && hasClasses) {
            return "High";
        }

        if (hasClasses || hasImpactData) {
            if ("Low".equals(baseConfidence)) {
                return "Low";
            }
            return "Medium";
        }

        return baseConfidence;
    }

    /**
     * Generates a human-readable rationale explaining the test impact analysis.
     */
    String generateTestingRationale(
            TaskType taskType, String normalizedTask,
            List<String> affectedClasses, List<String> relatedTests,
            List<String> missingTests) {
        StringBuilder rationale = new StringBuilder();

        rationale.append("Test impact analysis for ")
                .append(taskType.getDisplayName().toLowerCase())
                .append(" task. ");

        if (affectedClasses.isEmpty()) {
            rationale.append("No specific production classes could be identified from the task description. ");
        } else {
            rationale.append("Found ")
                    .append(affectedClasses.size())
                    .append(" affected production class(es): ")
                    .append(String.join(", ", affectedClasses))
                    .append(". ");
        }

        if (relatedTests.isEmpty()) {
            rationale.append("No specific test classes could be derived from the affected production classes. ");
        } else {
            rationale.append("Identified ")
                    .append(relatedTests.size())
                    .append(" related test class(es): ")
                    .append(String.join(", ", relatedTests))
                    .append(". ");
        }

        if (!missingTests.isEmpty()) {
            rationale.append("Identified ")
                    .append(missingTests.size())
                    .append(" potential missing test(s): ")
                    .append(String.join(", ", missingTests))
                    .append(". ");
        }

        // Type-specific rationale details
        switch (taskType) {
            case NEW_FEATURE:
                rationale.append("New features require comprehensive test coverage including unit, integration, and end-to-end tests. ");
                break;
            case BUG_FIX:
                rationale.append("Bug fixes should include regression tests to prevent reoccurrence. ");
                break;
            case REFACTORING:
                rationale.append("Refactoring requires thorough regression testing to verify no behavioral changes. ");
                break;
            case REST_API_CHANGE:
                rationale.append("API changes require contract testing and backward compatibility validation. ");
                break;
            case DATABASE_CHANGE:
                rationale.append("Database changes require migration testing and data integrity verification. ");
                break;
            case CONFIGURATION_CHANGE:
                rationale.append("Configuration changes should be validated across all environments. ");
                break;
            case PERFORMANCE_IMPROVEMENT:
                rationale.append("Performance changes require benchmark tests to validate improvements. ");
                break;
            case UNIT_TEST:
                rationale.append("Existing tests may need updating to accommodate the new test structure. ");
                break;
            case DOCUMENTATION:
                rationale.append("Documentation changes do not require test updates unless functional behavior changed. ");
                break;
            default:
                rationale.append("General testing applies based on the affected components. ");
                break;
        }

        return rationale.toString().trim();
    }

    // --- Private helper methods ---

    /**
     * Derives a test class name from a production class name using naming conventions.
     * Examples:
     * - UserController -> UserControllerTest (controller test)
     * - UserService -> UserServiceTest (service test)
     * - UserRepository -> UserRepositoryTest (repository test)
     * - UserEntity -> UserEntityTest (entity test)
     * - UserDto -> UserDtoTest (DTO test)
     * - UserConfig -> UserConfigTest
     */
    private String deriveTestClassName(String className) {
        if (className == null || className.trim().isEmpty()) {
            return null;
        }

        String trimmed = className.trim();

        // If the class name already ends with "Test", return as-is
        if (trimmed.endsWith("Test")) {
            return trimmed + " (already a test class)";
        }

        // Determine test type suffix based on class type.
        // If the class name already ends with the type suffix, just append "Test".
        if (trimmed.endsWith("Controller") || trimmed.endsWith("Resource")
                || trimmed.endsWith("Endpoint")) {
            return trimmed + "Test";
        }
        if (trimmed.endsWith("Service") || trimmed.endsWith("Manager")
                || trimmed.endsWith("Provider") || trimmed.endsWith("Factory")) {
            return trimmed + "Test";
        }
        if (trimmed.endsWith("Repository") || trimmed.endsWith("Dao")
                || trimmed.endsWith("DataAccess") || trimmed.endsWith("Storage")) {
            return trimmed + "Test";
        }
        if (trimmed.endsWith("Entity") || trimmed.endsWith("Model")
                || trimmed.endsWith("Domain")) {
            return trimmed + "Test";
        }
        if (trimmed.endsWith("Dto") || trimmed.endsWith("Request")
                || trimmed.endsWith("Response") || trimmed.endsWith("Vo")
                || trimmed.endsWith("Form") || trimmed.endsWith("View")) {
            return trimmed + "Test";
        }
        if (trimmed.endsWith("Config") || trimmed.endsWith("Configuration")
                || trimmed.endsWith("Properties") || trimmed.endsWith("Settings")) {
            return trimmed + "Test";
        }

        // Generic case: append Test
        return trimmed + "Test";
    }

    private void addPatternMatches(String text, Pattern pattern, Set<String> matches) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String name = matcher.group(1);
            if (name.length() >= 3) {
                matches.add(name);
            }
        }
    }

    private List<String> getTypeSpecificTests(
            TaskType taskType, String normalizedTask, List<String> affectedClasses) {
        Set<String> tests = new LinkedHashSet<>();

        // Controller tests
        if (taskType == TaskType.REST_API_CHANGE
                || containsAny(normalizedTask, "controller", "endpoint", "api")) {
            tests.add("Controller tests for REST API changes");
        }

        // Service tests
        if (containsAny(normalizedTask, "service", "business", "logic")) {
            tests.add("Service layer unit tests");
        }

        // Repository tests
        if (taskType == TaskType.DATABASE_CHANGE
                || containsAny(normalizedTask, "repository", "database", "jpa", "hibernate")) {
            tests.add("Repository/data access tests");
        }

        // Integration tests
        if (taskType == TaskType.NEW_FEATURE || taskType == TaskType.REFACTORING) {
            tests.add("Integration tests for affected components");
        }

        // Spring Boot context tests for configuration changes
        if (taskType == TaskType.CONFIGURATION_CHANGE) {
            tests.add("Spring Boot context configuration tests");
        }

        return new ArrayList<>(tests);
    }

    private List<String> getDefaultAffectedClasses(TaskType taskType, String normalizedTask) {
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

    private List<String> getTestExecutionOrderForType(TaskType taskType, String normalizedTask) {
        List<String> order = new ArrayList<>();

        // Base test execution order
        switch (taskType) {
            case NEW_FEATURE:
                order.add("1. Unit tests for new feature components");
                order.add("2. Integration tests for new feature integration");
                order.add("3. End-to-end tests for feature validation");
                break;
            case BUG_FIX:
                order.add("1. Unit tests verifying the bug fix");
                order.add("2. Regression tests for related components");
                order.add("3. Integration tests for affected workflows");
                break;
            case REFACTORING:
                order.add("1. Existing unit tests to verify unchanged behavior");
                order.add("2. Additional unit tests for refactored code paths");
                order.add("3. Integration tests for affected workflows");
                break;
            case REST_API_CHANGE:
                order.add("1. Controller unit tests for endpoint changes");
                order.add("2. API contract/integration tests");
                order.add("3. End-to-end tests for API workflows");
                break;
            case DATABASE_CHANGE:
                order.add("1. Entity validation tests");
                order.add("2. Repository/data access tests");
                order.add("3. Database migration tests");
                order.add("4. Integration tests for affected services");
                break;
            case CONFIGURATION_CHANGE:
                order.add("1. Configuration loading tests");
                order.add("2. Spring Boot context tests");
                order.add("3. Integration tests for dependent components");
                break;
            case PERFORMANCE_IMPROVEMENT:
                order.add("1. Unit tests for optimized components");
                order.add("2. Performance benchmark tests");
                order.add("3. Integration tests to verify no regressions");
                break;
            case UNIT_TEST:
                order.add("1. New unit tests for target class");
                order.add("2. Existing unit tests for related classes");
                break;
            case DOCUMENTATION:
                order.add("1. No functional tests required (documentation only)");
                break;
            default:
                order.addAll(DEFAULT_TEST_ORDER);
                break;
        }

        return order;
    }

    private static Map<TaskType, String> buildEffortByTaskType() {
        Map<TaskType, String> map = new LinkedHashMap<>();
        map.put(TaskType.NEW_FEATURE, "High");
        map.put(TaskType.BUG_FIX, "Medium");
        map.put(TaskType.REFACTORING, "High");
        map.put(TaskType.REST_API_CHANGE, "Medium");
        map.put(TaskType.DATABASE_CHANGE, "High");
        map.put(TaskType.CONFIGURATION_CHANGE, "Low");
        map.put(TaskType.PERFORMANCE_IMPROVEMENT, "High");
        map.put(TaskType.UNIT_TEST, "Low");
        map.put(TaskType.DOCUMENTATION, "Low");
        map.put(TaskType.UNKNOWN, "Medium");
        return map;
    }

    private static Map<TaskType, String> buildConfidenceByTaskType() {
        Map<TaskType, String> map = new LinkedHashMap<>();
        map.put(TaskType.NEW_FEATURE, "Medium");
        map.put(TaskType.BUG_FIX, "Medium");
        map.put(TaskType.REFACTORING, "Medium");
        map.put(TaskType.REST_API_CHANGE, "High");
        map.put(TaskType.DATABASE_CHANGE, "High");
        map.put(TaskType.CONFIGURATION_CHANGE, "High");
        map.put(TaskType.PERFORMANCE_IMPROVEMENT, "Medium");
        map.put(TaskType.UNIT_TEST, "High");
        map.put(TaskType.DOCUMENTATION, "High");
        map.put(TaskType.UNKNOWN, "Low");
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