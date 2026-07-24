package com.projectiq.mcp.analysis.service;

import com.projectiq.mcp.analysis.dto.ComplexityLevel;
import com.projectiq.mcp.analysis.dto.ConfidenceLevel;
import com.projectiq.mcp.analysis.dto.TaskAnalysisResponse;
import com.projectiq.mcp.analysis.dto.TaskAnalysisResponse.ExecutionStep;
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
 * Service that performs deterministic rule-based analysis of natural language
 * development tasks. This service detects the task type, identifies repository
 * entities, determines required MCP tools, and produces a structured execution
 * plan without using any AI model or LLM integration.
 */
@Service
public class TaskAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(TaskAnalysisService.class);

    // --- Keyword patterns for task type detection ---

    private static final String[] NEW_FEATURE_KEYWORDS = {
            "add", "implement", "feature", "create", "introduce", "new", "build",
            "develop", "support for", "enable"
    };

    private static final String[] BUG_FIX_KEYWORDS = {
            "bug", "fix", "issue", "error", "broken", "incorrect", "wrong",
            "failing", "crash", "exception", "defect", "repair", "patch"
    };

    private static final String[] REFACTORING_KEYWORDS = {
            "refactor", "restructure", "reorganize", "clean up", "simplify",
            "improve", "optimize", "extract", "inline", "rename", "move"
    };

    private static final String[] REST_API_KEYWORDS = {
            "endpoint", "api", "rest", "http", "request", "response",
            "controller", "getmapping", "postmapping", "putmapping",
            "deletemapping", "patchmapping", "requestmapping"
    };

    private static final String[] DATABASE_KEYWORDS = {
            "database", "db", "sql", "query", "table", "schema", "migration",
            "entity", "jpa", "hibernate", "repository", "datasource",
            "persistence", "column", "index"
    };

    private static final String[] PERFORMANCE_KEYWORDS = {
            "performance", "slow", "latency", "throughput", "cache", "optimize",
            "bottleneck", "memory", "cpu", "response time", "concurrent"
    };

    private static final String[] CONFIGURATION_KEYWORDS = {
            "config", "configuration", "property", "setting", "yml", "yaml",
            "application.properties", "environment", "profile", "setup"
    };

    private static final String[] UNIT_TEST_KEYWORDS = {
            "test", "unit test", "integration test", "test case", "junit",
            "mock", "assert", "test coverage", "testable", "spec"
    };

    private static final String[] DOCUMENTATION_KEYWORDS = {
            "documentation", "doc", "readme", "javadoc", "comment",
            "swagger", "openapi", "wiki", "markdown", "api doc"
    };

    // --- Patterns for entity detection ---

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

    private static final Pattern DTO_PATTERN = Pattern.compile(
            "\\b([A-Z][a-zA-Z0-9]*(?:Dto|Request|Response|Form|View))\\b"
    );

    private static final Pattern CONFIG_CLASS_PATTERN = Pattern.compile(
            "\\b([A-Z][a-zA-Z0-9]*(?:Config|Configuration|Properties|Settings))\\b"
    );

    private static final Pattern CLASS_PATTERN = Pattern.compile(
            "\\b([A-Z][a-zA-Z0-9]{2,})\\b"
    );

    private static final Pattern METHOD_PATTERN = Pattern.compile(
            "\\b([a-z][a-zA-Z0-9]*\\(\\s*\\)|[a-z][a-zA-Z0-9]*By[A-Z][a-zA-Z0-9]*)\\b"
    );

    private static final Pattern ENDPOINT_PATTERN = Pattern.compile(
            "/(?:api/)?[a-zA-Z0-9/{}._-]+|\\b(endpoint|route|path|uri)\\s+['\"]?([a-zA-Z0-9/{}._-]+)['\"]?"
    );

    private static final int TOOL_COUNT_MEDIUM = 5;
    private static final int TOOL_COUNT_HIGH = 8;
    private static final int ENTITY_COUNT_MEDIUM = 3;
    private static final int ENTITY_COUNT_HIGH = 5;
    private static final int COMPLEXITY_SCORE_MEDIUM = 3;
    private static final int COMPLEXITY_SCORE_HIGH = 5;

    private static final Pattern PACKAGE_PATTERN = Pattern.compile(
            "\\b([a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]*)+)\\b"
    );

    /**
     * Analyzes a natural language development task and produces a deterministic
     * execution plan with detected entities, tools, and complexity assessment.
     *
     * @param task the natural language development request
     * @return a complete TaskAnalysisResponse with the execution plan
     * @throws IllegalArgumentException if the task is null or empty
     */
    public TaskAnalysisResponse analyze(String task) {
        if (task == null || task.trim().isEmpty()) {
            throw new IllegalArgumentException("Task description cannot be null or empty");
        }

        String normalizedTask = task.trim().toLowerCase();
        logger.info("Analyzing task: {}", task);

        TaskAnalysisResponse response = new TaskAnalysisResponse();
        response.setOriginalTask(task.trim());

        // 1. Detect task type
        TaskType taskType = detectTaskType(normalizedTask);
        response.setTaskType(taskType);

        // 2. Determine confidence
        ConfidenceLevel confidence = determineConfidence(normalizedTask, taskType);
        response.setConfidenceLevel(confidence);

        // 3. Detect entities
        List<String> entities = detectEntities(task);
        response.setDetectedEntities(entities);

        // 4. Determine required tools
        List<String> tools = determineTools(taskType, entities, normalizedTask);
        response.setSuggestedTools(tools);

        // 5. Build execution plan
        List<ExecutionStep> plan = buildExecutionPlan(tools, taskType, entities, task);
        response.setExecutionPlan(plan);

        // 6. Generate reasoning summary
        String reasoning = generateReasoning(taskType, entities, tools, task);
        response.setReasoningSummary(reasoning);

        // 7. Estimate complexity
        ComplexityLevel complexity = estimateComplexity(tools.size(), entities.size(), taskType);
        response.setEstimatedComplexity(complexity);

        logger.info("Task analysis complete: type={}, confidence={}, entities={}, tools={}",
                taskType, confidence, entities.size(), tools.size());

        return response;
    }

    /**
     * Detects the development activity type using keyword matching.
     * Rules are checked in priority order; the first strong match wins.
     */
    TaskType detectTaskType(String normalizedTask) {
        // Count matches for each type
        int newFeatureScore = countKeywordMatches(normalizedTask, NEW_FEATURE_KEYWORDS);
        int bugFixScore = countKeywordMatches(normalizedTask, BUG_FIX_KEYWORDS);
        int refactoringScore = countKeywordMatches(normalizedTask, REFACTORING_KEYWORDS);
        int restApiScore = countKeywordMatches(normalizedTask, REST_API_KEYWORDS);
        int databaseScore = countKeywordMatches(normalizedTask, DATABASE_KEYWORDS);
        int performanceScore = countKeywordMatches(normalizedTask, PERFORMANCE_KEYWORDS);
        int configScore = countKeywordMatches(normalizedTask, CONFIGURATION_KEYWORDS);
        int testScore = countKeywordMatches(normalizedTask, UNIT_TEST_KEYWORDS);
        int docScore = countKeywordMatches(normalizedTask, DOCUMENTATION_KEYWORDS);

        // Check for specific strong indicators first (high-specificity rules)
        if (containsWord(normalizedTask, "bug") || containsWord(normalizedTask, "fix") && !containsWord(normalizedTask, "setup")) {
            // Bug fix is strongly indicated
            if (bugFixScore >= 2 || (bugFixScore >= 1 && newFeatureScore == 0)) {
                return TaskType.BUG_FIX;
            }
        }

        if (containsWord(normalizedTask, "refactor") && refactoringScore >= 1) {
            return TaskType.REFACTORING;
        }

        // REST API specific patterns - check for both mapping annotations and HTTP method mentions
        if (containsAny(normalizedTask, "postmapping", "getmapping", "putmapping",
                "deletemapping", "restcontroller", "requestmapping", "patchmapping")) {
            return TaskType.REST_API_CHANGE;
        }
        if (restApiScore >= 2 && (containsAny(normalizedTask, "endpoint", "api", "rest"))) {
            return TaskType.REST_API_CHANGE;
        }

        // Database specific patterns
        if (containsWord(normalizedTask, "database") || containsWord(normalizedTask, "sql")
                || containsWord(normalizedTask, "migration") || containsWord(normalizedTask, "schema")) {
            if (databaseScore >= newFeatureScore && databaseScore >= bugFixScore) {
                return TaskType.DATABASE_CHANGE;
            }
        }

        // Performance specific patterns
        if (containsWord(normalizedTask, "performance") || containsWord(normalizedTask, "slow")) {
            if (performanceScore >= 1) {
                return TaskType.PERFORMANCE_IMPROVEMENT;
            }
        }

        // Documentation specific patterns - check BEFORE configuration to avoid "javadoc" + "config" false positives
        if (containsWord(normalizedTask, "javadoc") || containsWord(normalizedTask, "readme")
                || containsWord(normalizedTask, "swagger") || containsWord(normalizedTask, "openapi")) {
            if (docScore >= 1) {
                return TaskType.DOCUMENTATION;
            }
        }
        if (containsWord(normalizedTask, "documentation") && !containsWord(normalizedTask, "config")
                && !containsWord(normalizedTask, "property")) {
            if (docScore >= 1) {
                return TaskType.DOCUMENTATION;
            }
        }

        // Configuration specific patterns
        if (containsWord(normalizedTask, "config") || containsWord(normalizedTask, "configuration")
                || containsWord(normalizedTask, "property")) {
            if (configScore >= 1) {
                return TaskType.CONFIGURATION_CHANGE;
            }
        }

        // Unit test specific patterns
        if ((containsWord(normalizedTask, "test") || containsWord(normalizedTask, "junit"))
                && !containsWord(normalizedTask, "feature") && !containsWord(normalizedTask, "implement")) {
            if (testScore >= 2 || (containsWord(normalizedTask, "unit") && containsWord(normalizedTask, "test"))) {
                return TaskType.UNIT_TEST;
            }
        }

        // Documentation specific patterns
        if (containsWord(normalizedTask, "documentation") || containsWord(normalizedTask, "readme")
                || containsWord(normalizedTask, "javadoc") || containsWord(normalizedTask, "swagger")) {
            if (docScore >= 1) {
                return TaskType.DOCUMENTATION;
            }
        }

        // General new feature detection
        if (newFeatureScore >= 2 || (newFeatureScore >= 1 && containsAny(normalizedTask,
                "add", "implement", "create", "new", "feature"))) {
            return TaskType.NEW_FEATURE;
        }

        // If no strong match, pick the highest scoring type
        int[] scores = {newFeatureScore, bugFixScore, refactoringScore, restApiScore,
                databaseScore, performanceScore, configScore, testScore, docScore};
        TaskType[] types = {TaskType.NEW_FEATURE, TaskType.BUG_FIX, TaskType.REFACTORING,
                TaskType.REST_API_CHANGE, TaskType.DATABASE_CHANGE, TaskType.PERFORMANCE_IMPROVEMENT,
                TaskType.CONFIGURATION_CHANGE, TaskType.UNIT_TEST, TaskType.DOCUMENTATION};

        int maxScore = 0;
        TaskType bestType = TaskType.UNKNOWN;
        for (int i = 0; i < scores.length; i++) {
            if (scores[i] > maxScore) {
                maxScore = scores[i];
                bestType = types[i];
            }
        }

        return bestType;
    }

    /**
     * Determines confidence level based on keyword match strength.
     */
    ConfidenceLevel determineConfidence(String normalizedTask, TaskType taskType) {
        if (taskType == TaskType.UNKNOWN) {
            return ConfidenceLevel.LOW;
        }

        // Get the appropriate keyword set for this task type
        String[] keywords = getKeywordsForType(taskType);
        int matchCount = countKeywordMatches(normalizedTask, keywords);
        int totalWords = normalizedTask.split("\\s+").length;

        // High confidence: strong keyword presence relative to task length
        if (matchCount >= 3 || (matchCount >= 2 && totalWords <= 10)) {
            return ConfidenceLevel.HIGH;
        }

        // Medium confidence: reasonable keyword match
        if (matchCount >= 1) {
            return ConfidenceLevel.MEDIUM;
        }

        return ConfidenceLevel.LOW;
    }

    /**
     * Detects repository entities mentioned in the task using pattern matching.
     */
    List<String> detectEntities(String task) {
        Set<String> entities = new LinkedHashSet<>();

        // Detect controller names (e.g., UserController)
        addMatches(task, CONTROLLER_PATTERN, entities, "Controller");
        // Detect service names (e.g., UserService)
        addMatches(task, SERVICE_PATTERN, entities, "Service");
        // Detect repository names (e.g., UserRepository)
        addMatches(task, REPOSITORY_PATTERN, entities, "Repository");
        // Detect entity/model names (e.g., UserEntity)
        addMatches(task, ENTITY_PATTERN, entities, "Entity/Model");
        // Detect DTO names (e.g., UserDto)
        addMatches(task, DTO_PATTERN, entities, "DTO");
        // Detect config classes (e.g., SecurityConfig)
        addMatches(task, CONFIG_CLASS_PATTERN, entities, "Configuration");
        // Detect REST endpoints
        detectEndpoints(task, entities);
        // Detect packages/namespaces
        addPackageMatches(task, entities);
        // Detect methods
        addMethodMatches(task, entities);
        // Detect general Java classes (uppercase names)
        addClassMatches(task, entities);

        return new ArrayList<>(entities);
    }

    /**
     * Determines which MCP tools are required based on task type and detected entities.
     */
    List<String> determineTools(TaskType taskType, List<String> entities, String normalizedTask) {
        Set<String> tools = new LinkedHashSet<>();

        // repository_summary is always needed for baseline understanding
        tools.add("repository_summary");

        // search_code for finding relevant code
        tools.add("search_code");

        // find_class if entities look like classes
        if (hasClassLikeEntities(entities)) {
            tools.add("find_class");
        }

        // find_method if task mentions specific methods
        if (hasMethodReferences(normalizedTask, entities)) {
            tools.add("find_method");
        }

        // find_rest_api if REST-related task or controller detected
        if (taskType == TaskType.REST_API_CHANGE || hasControllerEntities(entities)
                || containsAny(normalizedTask, REST_API_KEYWORDS)) {
            tools.add("find_rest_api");
        }

        // find_spring_component if Spring-related task
        if (hasSpringComponentEntities(entities)
                || containsAny(normalizedTask, "service", "component", "bean", "autowired")) {
            tools.add("find_spring_component");
        }

        // find_dependency for dependency-related tasks
        if (taskType == TaskType.CONFIGURATION_CHANGE
                || containsAny(normalizedTask, "dependency", "maven", "gradle", "pom", "library", "import")) {
            tools.add("find_dependency");
        }

        // repository_statistics for large-scale tasks
        if (taskType == TaskType.PERFORMANCE_IMPROVEMENT
                || taskType == TaskType.REFACTORING
                || containsAny(normalizedTask, "statistics", "metrics", "overview")) {
            tools.add("repository_statistics");
        }

        // list_related_files always useful for understanding scope
        tools.add("list_related_files");

        // prompt_context as the final integration step
        if (!hasIndexerToolsOnly(tools)) {
            tools.add("prompt_context");
        }

        return new ArrayList<>(tools);
    }

    /**
     * Builds a deterministic, ordered execution plan from the required tools.
     */
    List<ExecutionStep> buildExecutionPlan(List<String> tools, TaskType taskType,
                                           List<String> entities, String originalTask) {
        List<ExecutionStep> plan = new ArrayList<>();
        int stepNumber = 1;

        // Step 1: Always start with repository summary
        if (tools.contains("repository_summary")) {
            plan.add(new ExecutionStep(stepNumber++, "repository_summary",
                    "Obtain a high-level overview of the repository structure, packages, and key statistics"));
        }

        // Step 2: Repository statistics for scale assessment
        if (tools.contains("repository_statistics")) {
            plan.add(new ExecutionStep(stepNumber++, "repository_statistics",
                    "Retrieve repository statistics including contributor activity and file type distribution"));
        }

        // Step 3: Search code for relevant implementations
        if (tools.contains("search_code")) {
            plan.add(new ExecutionStep(stepNumber++, "search_code",
                    "Search the codebase for files and code related to the task description"));
        }

        // Step 4: Find Spring components
        if (tools.contains("find_spring_component")) {
            plan.add(new ExecutionStep(stepNumber++, "find_spring_component",
                    "Identify Spring-managed components (controllers, services, repositories, beans)"));
        }

        // Step 5: Find specific classes
        if (tools.contains("find_class")) {
            plan.add(new ExecutionStep(stepNumber++, "find_class",
                    "Locate specific classes and their metadata based on detected entities"));
        }

        // Step 6: Find specific methods
        if (tools.contains("find_method")) {
            plan.add(new ExecutionStep(stepNumber++, "find_method",
                    "Find method signatures and implementations within relevant classes"));
        }

        // Step 7: Find REST APIs
        if (tools.contains("find_rest_api")) {
            plan.add(new ExecutionStep(stepNumber++, "find_rest_api",
                    "Discover REST API endpoints, HTTP methods, and request/response types"));
        }

        // Step 8: Find dependencies
        if (tools.contains("find_dependency")) {
            plan.add(new ExecutionStep(stepNumber++, "find_dependency",
                    "Analyze project dependencies and their configurations"));
        }

        // Step 9: List related files
        if (tools.contains("list_related_files")) {
            plan.add(new ExecutionStep(stepNumber++, "list_related_files",
                    "List files related to the detected entities and task scope"));
        }

        // Step 10: Generate prompt context (final step)
        if (tools.contains("prompt_context")) {
            plan.add(new ExecutionStep(stepNumber, "prompt_context",
                    "Generate a consolidated AI-ready prompt context with all gathered information"));
        }

        return plan;
    }

    /**
     * Generates a deterministic reasoning summary explaining tool selection.
     */
    String generateReasoning(TaskType taskType, List<String> entities,
                             List<String> tools, String originalTask) {
        StringBuilder reasoning = new StringBuilder();

        reasoning.append("Task classified as '").append(taskType.getDisplayName()).append("'");
        if (entities.isEmpty()) {
            reasoning.append(" with no specific entities detected");
        } else {
            reasoning.append(" with ").append(entities.size()).append(" entity references: ");
            reasoning.append(String.join(", ", entities));
        }
        reasoning.append(". ");

        reasoning.append(tools.size()).append(" MCP tools required for execution: ");
        reasoning.append(String.join(", ", tools)).append(". ");

        reasoning.append("The execution plan follows a logical progression: ");
        reasoning.append("start with repository overview, search for relevant code, ");
        reasoning.append("identify specific components and classes, examine implementations, ");
        reasoning.append("and finally consolidate into a development context.");

        return reasoning.toString();
    }

    /**
     * Estimates task complexity based on tool count, entity count, and task type.
     */
    ComplexityLevel estimateComplexity(int toolCount, int entityCount, TaskType taskType) {
        int complexityScore = 0;

        // More tools = higher complexity
        if (toolCount >= 8) {
            complexityScore += 3;
        } else if (toolCount >= 5) {
            complexityScore += 2;
        } else {
            complexityScore += 1;
        }

        // More entities = higher complexity
        if (entityCount >= 5) {
            complexityScore += 3;
        } else if (entityCount >= 3) {
            complexityScore += 2;
        } else if (entityCount >= 1) {
            complexityScore += 1;
        }

        // Certain task types are inherently more complex
        if (taskType == TaskType.DATABASE_CHANGE || taskType == TaskType.REFACTORING
                || taskType == TaskType.PERFORMANCE_IMPROVEMENT) {
            complexityScore += 1;
        }

        if (complexityScore >= 5) {
            return ComplexityLevel.HIGH;
        } else if (complexityScore >= 3) {
            return ComplexityLevel.MEDIUM;
        } else {
            return ComplexityLevel.LOW;
        }
    }

    // --- Private helper methods ---

    private int countKeywordMatches(String text, String[] keywords) {
        int count = 0;
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                count++;
            }
        }
        return count;
    }

    private boolean containsWord(String text, String word) {
        return text.contains(word);
    }

    private boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private String[] getKeywordsForType(TaskType taskType) {
        return switch (taskType) {
            case NEW_FEATURE -> NEW_FEATURE_KEYWORDS;
            case BUG_FIX -> BUG_FIX_KEYWORDS;
            case REFACTORING -> REFACTORING_KEYWORDS;
            case REST_API_CHANGE -> REST_API_KEYWORDS;
            case DATABASE_CHANGE -> DATABASE_KEYWORDS;
            case PERFORMANCE_IMPROVEMENT -> PERFORMANCE_KEYWORDS;
            case CONFIGURATION_CHANGE -> CONFIGURATION_KEYWORDS;
            case UNIT_TEST -> UNIT_TEST_KEYWORDS;
            case DOCUMENTATION -> DOCUMENTATION_KEYWORDS;
            default -> new String[0];
        };
    }

    private void addMatches(String text, Pattern pattern, Set<String> entities, String category) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String match = matcher.group(1);
            entities.add(match + " (" + category + ")");
        }
    }

    private void detectEndpoints(String task, Set<String> entities) {
        Matcher matcher = ENDPOINT_PATTERN.matcher(task);
        while (matcher.find()) {
            String group2 = matcher.group(2);
            if (group2 != null) {
                entities.add("Endpoint: " + group2);
            } else {
                String match = matcher.group(1);
                if (match != null && match.startsWith("/")) {
                    entities.add("Endpoint: " + match);
                }
            }
        }
    }

    private void addPackageMatches(String text, Set<String> entities) {
        // Only match if it looks like a real package (multiple segments)
        Matcher matcher = PACKAGE_PATTERN.matcher(text);
        while (matcher.find()) {
            String pkg = matcher.group(1);
            // Filter out common false positives
            if (!pkg.equals("com") && !pkg.equals("org") && !pkg.equals("io")
                    && !pkg.equals("net") && !pkg.startsWith("http")) {
                entities.add("Package: " + pkg);
            }
        }
    }

    private void addMethodMatches(String text, Set<String> entities) {
        Matcher matcher = METHOD_PATTERN.matcher(text);
        while (matcher.find()) {
            String method = matcher.group().trim();
            entities.add("Method: " + method);
        }
    }

    private void addClassMatches(String text, Set<String> entities) {
        // Only add general class names if they look like Java classes
        // (start with uppercase, have at least 2 chars, and aren't simple words)
        Matcher matcher = CLASS_PATTERN.matcher(text);
        while (matcher.find()) {
            String className = matcher.group(1);
            // Filter out non-class words that are uppercase
            if (className.length() >= 3 && !isCommonWord(className)) {
                entities.add("Class: " + className);
            }
        }
    }

    private boolean isCommonWord(String word) {
        return switch (word.toLowerCase()) {
            case "the", "this", "that", "with", "from", "have", "been",
                 "were", "will", "what", "when", "where", "which", "their",
                 "there", "could", "should", "would", "about", "every",
                 "after", "before", "between", "other", "under", "above",
                 "while", "since", "until", "during", "through", "without",
                 "within", "along", "among", "these", "those", "being",
                 "having", "doing", "making", "using", "working", "going",
                 "later", "still", "just", "java" -> true;
            default -> false;
        };
    }

    private boolean hasClassLikeEntities(List<String> entities) {
        for (String entity : entities) {
            if (entity.startsWith("Class:") || entity.contains("(Controller)")
                    || entity.contains("(Service)") || entity.contains("(Repository)")
                    || entity.contains("(Entity/Model)") || entity.contains("(DTO)")
                    || entity.contains("(Configuration)")) {
                return true;
            }
        }
        return false;
    }

    private boolean hasMethodReferences(String normalizedTask, List<String> entities) {
        if (entities.stream().anyMatch(e -> e.startsWith("Method:"))) {
            return true;
        }
        return containsAny(normalizedTask, "method", "function", "invoke", "call",
                "signature", "parameter", "return type");
    }

    private boolean hasControllerEntities(List<String> entities) {
        return entities.stream().anyMatch(e -> e.contains("(Controller)"));
    }

    private boolean hasSpringComponentEntities(List<String> entities) {
        return entities.stream().anyMatch(e ->
                e.contains("(Controller)") || e.contains("(Service)")
                        || e.contains("(Repository)"));
    }

    private boolean hasIndexerToolsOnly(Set<String> tools) {
        return tools.size() <= 2 && tools.contains("repository_summary");
    }
}