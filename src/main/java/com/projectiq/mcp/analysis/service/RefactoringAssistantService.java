package com.projectiq.mcp.analysis.service;

import com.projectiq.mcp.analysis.dto.ConfidenceLevel;
import com.projectiq.mcp.analysis.dto.ContextAssemblyResponse;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse.ImpactedComponent;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse.RiskItem;
import com.projectiq.mcp.analysis.dto.ImplementationPlanningResponse;
import com.projectiq.mcp.analysis.dto.RefactoringAssistantResponse;
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
 * Service that analyzes proposed refactoring tasks and provides a deterministic,
 * repository-aware refactoring plan. This service identifies affected repository
 * components, recommends a safe refactoring sequence, highlights possible risks,
 * and recommends validation activities without modifying any source code.
 *
 * <p>This service invokes existing analysis services ({@link TaskAnalysisService},
 * {@link ContextAssemblyService}, {@link ImpactAnalysisService},
 * {@link ImplementationPlanningService}, and {@link TestImpactAnalysisService})
 * to gather the necessary intelligence for producing a structured refactoring plan.</p>
 *
 * <p>All outputs are deterministic, stable, and free of duplicate entries.
 * This service NEVER modifies repository files or generates code changes.</p>
 */
@Service
public class RefactoringAssistantService {

    private static final Logger logger = LoggerFactory.getLogger(RefactoringAssistantService.class);

    private final TaskAnalysisService taskAnalysisService;
    private final ContextAssemblyService contextAssemblyService;
    private final ImpactAnalysisService impactAnalysisService;
    private final ImplementationPlanningService implementationPlanningService;
    private final TestImpactAnalysisService testImpactAnalysisService;

    // --- Deterministic refactoring type detection patterns ---

    private static final Pattern RENAME_CLASS_PATTERN = Pattern.compile(
            "\\brename\\s+(class|type|interface)\\s+([A-Z][a-zA-Z0-9]*)\\s+to\\s+([A-Z][a-zA-Z0-9]*)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern RENAME_METHOD_PATTERN = Pattern.compile(
            "\\brename\\s+(method|function)\\s+([a-z][a-zA-Z0-9]*)\\s+to\\s+([a-z][a-zA-Z0-9]*)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern MOVE_CLASS_PATTERN = Pattern.compile(
            "\\bmove\\s+(class|type|interface)\\s+([A-Z][a-zA-Z0-9]*)\\s+to\\s+([a-z][a-zA-Z0-9.]+)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern MOVE_PACKAGE_PATTERN = Pattern.compile(
            "\\bmove\\s+(package|namespace)\\s+([a-z][a-zA-Z0-9.]+)\\s+to\\s+([a-z][a-zA-Z0-9.]+)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern EXTRACT_METHOD_PATTERN = Pattern.compile(
            "\\bextract\\s+(method|function)\\s+([a-z][a-zA-Z0-9]*)\\s+from\\s+([a-z][a-zA-Z0-9]*)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern EXTRACT_CLASS_PATTERN = Pattern.compile(
            "\\bextract\\s+(class|type|interface)\\s+([A-Z][a-zA-Z0-9]*)\\s+from\\s+([A-Z][a-zA-Z0-9]*)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern INLINE_METHOD_PATTERN = Pattern.compile(
            "\\binline\\s+(method|function)\\s+([a-z][a-zA-Z0-9]*)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern DELETE_DEAD_CODE_PATTERN = Pattern.compile(
            "\\bdelete\\s+(dead|unused|redundant|obsolete)\\s+(code|class|method|field|import)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SPLIT_LARGE_CLASS_PATTERN = Pattern.compile(
            "\\bsplit\\s+(large|big|monolithic|god)\\s+(class|type|interface)\\s+([A-Z][a-zA-Z0-9]*)",
            Pattern.CASE_INSENSITIVE
    );

    // --- General entity detection patterns ---

    private static final Pattern CLASS_NAME_PATTERN = Pattern.compile(
            "\\b([A-Z][a-zA-Z0-9]*(?:Controller|Service|Repository|Entity|Dto|Config|Helper|Util|Manager|Provider|Factory|Mapper|Validator|Handler|Processor|Builder|Adapter|Listener|Filter|Interceptor|Component|Bean|Model|Domain|Vo|Pojo|Form|View|Resource|Endpoint|Dao|DataAccess|Storage))\\b"
    );

    private static final Pattern METHOD_NAME_PATTERN = Pattern.compile(
            "\\b([a-z][a-zA-Z0-9]*\\(\\s*\\)|[a-z][a-zA-Z0-9]*By[A-Z][a-zA-Z0-9]*|[a-z][a-zA-Z0-9]*Exception)\\b"
    );

    private static final Pattern PACKAGE_NAME_PATTERN = Pattern.compile(
            "\\b([a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]*){2,})\\b"
    );

    // --- Deterministic execution order templates by refactoring type ---

    private static final Map<String, List<String>> EXECUTION_ORDER_TEMPLATES = buildExecutionOrderTemplates();

    // --- Deterministic validation checklist templates by refactoring type ---

    private static final Map<String, List<String>> VALIDATION_TEMPLATES = buildValidationTemplates();

    // --- Deterministic risk templates by refactoring type ---

    private static final Map<String, List<String>> RISK_TEMPLATES = buildRiskTemplates();

    public RefactoringAssistantService(
            TaskAnalysisService taskAnalysisService,
            ContextAssemblyService contextAssemblyService,
            ImpactAnalysisService impactAnalysisService,
            ImplementationPlanningService implementationPlanningService,
            TestImpactAnalysisService testImpactAnalysisService) {
        this.taskAnalysisService = taskAnalysisService;
        this.contextAssemblyService = contextAssemblyService;
        this.impactAnalysisService = impactAnalysisService;
        this.implementationPlanningService = implementationPlanningService;
        this.testImpactAnalysisService = testImpactAnalysisService;
    }

    /**
     * Analyzes a proposed refactoring task and produces a structured,
     * deterministic refactoring plan.
     *
     * @param task           the natural language refactoring request
     * @param repositoryName the repository name
     * @param branch         the git branch (optional, defaults to "main")
     * @return a structured refactoring assistant response
     * @throws IllegalArgumentException if the task is null or empty
     */
    public RefactoringAssistantResponse analyzeRefactoring(
            String task, String repositoryName, String branch) {
        if (task == null || task.trim().isEmpty()) {
            throw new IllegalArgumentException("Refactoring task description cannot be null or empty");
        }

        logger.info("Analyzing refactoring task: {} in repository: {}", task, repositoryName);

        String effectiveBranch = (branch != null && !branch.trim().isEmpty()) ? branch.trim() : "main";
        String normalizedTask = task.trim().toLowerCase();

        // Step 1: Invoke analyze_task
        TaskAnalysisResponse taskAnalysis = taskAnalysisService.analyze(task);

        // Step 2: Invoke assemble_context (non-critical)
        try {
            contextAssemblyService.assembleContext(task.trim(), repositoryName, effectiveBranch);
        } catch (Exception e) {
            logger.warn("Failed to assemble repository context: {}", e.getMessage());
        }

        // Step 3: Invoke analyze_impact (non-critical)
        ImpactAnalysisResponse impactAnalysis = null;
        try {
            impactAnalysis = impactAnalysisService.analyzeImpact(task.trim(), repositoryName, effectiveBranch);
        } catch (Exception e) {
            logger.warn("Failed to analyze impact: {}", e.getMessage());
        }

        // Step 4: Invoke implementation_plan (non-critical)
        ImplementationPlanningResponse implPlan = null;
        try {
            implPlan = implementationPlanningService.generatePlan(task.trim(), repositoryName, effectiveBranch);
        } catch (Exception e) {
            logger.warn("Failed to generate implementation plan: {}", e.getMessage());
        }

        // Step 5: Invoke test_impact_analysis (non-critical)
        TestImpactAnalysisResponse testImpact = null;
        try {
            testImpact = testImpactAnalysisService.analyzeTestImpact(task.trim(), repositoryName, effectiveBranch);
        } catch (Exception e) {
            logger.warn("Failed to analyze test impact: {}", e.getMessage());
        }

        // Step 6: Build the refactoring assistant response
        RefactoringAssistantResponse response = new RefactoringAssistantResponse();
        response.setOriginalTask(task.trim());

        // Determine refactoring type
        String refactoringType = detectRefactoringType(normalizedTask);
        response.setRefactoringType(refactoringType);

        // Identify affected classes
        List<String> affectedClasses = identifyAffectedClasses(
                task, normalizedTask, refactoringType, taskAnalysis, impactAnalysis);
        response.setAffectedClasses(affectedClasses);

        // Identify affected methods
        List<String> affectedMethods = identifyAffectedMethods(
                task, normalizedTask, refactoringType, impactAnalysis);
        response.setAffectedMethods(affectedMethods);

        // Identify affected packages
        List<String> affectedPackages = identifyAffectedPackages(
                task, normalizedTask, refactoringType);
        response.setAffectedPackages(affectedPackages);

        // Build dependencies involved
        List<String> dependenciesInvolved = buildDependencies(
                refactoringType, impactAnalysis, normalizedTask);
        response.setDependenciesInvolved(dependenciesInvolved);

        // Build suggested execution order
        List<String> executionOrder = buildExecutionOrder(
                refactoringType, normalizedTask, affectedClasses, affectedMethods);
        response.setSuggestedExecutionOrder(executionOrder);

        // Build validation checklist
        List<String> validationChecklist = buildValidationChecklist(
                refactoringType, normalizedTask);
        response.setValidationChecklist(validationChecklist);

        // Build recommended tests
        List<String> recommendedTests = buildRecommendedTests(
                refactoringType, normalizedTask, testImpact, impactedClasses(affectedClasses, impactAnalysis));
        response.setRecommendedTests(recommendedTests);

        // Build risks
        List<String> risks = buildRisks(
                refactoringType, normalizedTask, impactAnalysis, affectedClasses);
        response.setRisks(risks);

        // Determine confidence level
        String confidence = determineConfidence(
                refactoringType, normalizedTask, taskAnalysis, impactAnalysis);
        response.setConfidenceLevel(confidence);

        logger.info("Refactoring analysis complete: type={}, classes={}, methods={}, confidence={}",
                refactoringType, affectedClasses.size(), affectedMethods.size(), confidence);

        return response;
    }

    /**
     * Detects the refactoring type from the task description using pattern matching.
     */
    String detectRefactoringType(String normalizedTask) {
        if (RENAME_CLASS_PATTERN.matcher(normalizedTask).find() ||
                normalizedTask.contains("rename class") ||
                normalizedTask.contains("rename type") ||
                normalizedTask.contains("rename interface")) {
            return "Rename Class";
        }

        if (RENAME_METHOD_PATTERN.matcher(normalizedTask).find() ||
                normalizedTask.contains("rename method") ||
                normalizedTask.contains("rename function")) {
            return "Rename Method";
        }

        if (MOVE_CLASS_PATTERN.matcher(normalizedTask).find() ||
                (normalizedTask.contains("move class") && normalizedTask.contains("to"))) {
            return "Move Class";
        }

        if (MOVE_PACKAGE_PATTERN.matcher(normalizedTask).find() ||
                (normalizedTask.contains("move package") && normalizedTask.contains("to"))) {
            return "Move Package";
        }

        if (EXTRACT_METHOD_PATTERN.matcher(normalizedTask).find() ||
                normalizedTask.contains("extract method") ||
                normalizedTask.contains("extract function")) {
            return "Extract Method";
        }

        if (EXTRACT_CLASS_PATTERN.matcher(normalizedTask).find() ||
                normalizedTask.contains("extract class") ||
                normalizedTask.contains("extract type")) {
            return "Extract Class";
        }

        if (INLINE_METHOD_PATTERN.matcher(normalizedTask).find() ||
                normalizedTask.contains("inline method") ||
                normalizedTask.contains("inline function")) {
            return "Inline Method";
        }

        if (DELETE_DEAD_CODE_PATTERN.matcher(normalizedTask).find()) {
            return "Delete Dead Code";
        }

        if (SPLIT_LARGE_CLASS_PATTERN.matcher(normalizedTask).find() ||
                normalizedTask.contains("split class") ||
                normalizedTask.contains("split type")) {
            return "Split Large Class";
        }

        // Check for general refactoring keywords
        if (containsAny(normalizedTask, "refactor", "restructure", "reorganize",
                "clean up", "simplify", "improve", "optimize")) {
            return "General Refactoring";
        }

        return "General Refactoring";
    }

    /**
     * Identifies the classes affected by the proposed refactoring.
     */
    List<String> identifyAffectedClasses(
            String task, String normalizedTask, String refactoringType,
            TaskAnalysisResponse taskAnalysis, ImpactAnalysisResponse impactAnalysis) {
        Set<String> classes = new LinkedHashSet<>();

        // Extract class names from the refactoring-specific patterns
        extractPatternedClasses(normalizedTask, refactoringType, classes);

        // Extract class names from general entity detection
        Matcher classMatcher = CLASS_NAME_PATTERN.matcher(task);
        while (classMatcher.find()) {
            String className = classMatcher.group(1);
            if (className.length() >= 3) {
                classes.add(className);
            }
        }

        // Add detected entities from task analysis
        if (taskAnalysis.getDetectedEntities() != null) {
            for (String entity : taskAnalysis.getDetectedEntities()) {
                if (entity.contains("(Controller)") || entity.contains("(Service)")
                        || entity.contains("(Repository)") || entity.contains("(Entity/Model)")
                        || entity.contains("(DTO)") || entity.contains("(Configuration)")
                        || entity.contains("Class:")) {
                    String cleanName = entity;
                    int parenIdx = cleanName.indexOf(" (");
                    if (parenIdx > 0) {
                        cleanName = cleanName.substring(0, parenIdx);
                    }
                    if (cleanName.startsWith("Class: ")) {
                        cleanName = cleanName.substring("Class: ".length());
                    }
                    classes.add(cleanName);
                }
            }
        }

        // Add directly affected components from impact analysis
        if (impactAnalysis != null && impactAnalysis.getDirectlyAffectedComponents() != null) {
            for (ImpactedComponent component : impactAnalysis.getDirectlyAffectedComponents()) {
                String name = component.getComponentName();
                if (!name.startsWith("Endpoint:") && !name.startsWith("Package:")) {
                    classes.add(name);
                }
            }
        }

        // Add indirectly affected components from impact analysis
        if (impactAnalysis != null && impactAnalysis.getIndirectlyAffectedComponents() != null) {
            for (ImpactedComponent component : impactAnalysis.getIndirectlyAffectedComponents()) {
                String name = component.getComponentName();
                if ("Class".equals(component.getComponentType()) && !name.startsWith("Endpoint:")) {
                    classes.add(name);
                }
            }
        }

        // If still empty, derive defaults from refactoring type
        if (classes.isEmpty()) {
            classes.addAll(getDefaultAffectedClasses(refactoringType));
        }

        return new ArrayList<>(classes);
    }

    /**
     * Identifies the methods affected by the proposed refactoring.
     */
    List<String> identifyAffectedMethods(
            String task, String normalizedTask, String refactoringType,
            ImpactAnalysisResponse impactAnalysis) {
        Set<String> methods = new LinkedHashSet<>();

        // Extract method names from refactoring-specific patterns
        extractPatternedMethods(normalizedTask, refactoringType, methods);

        // Extract method names from general pattern matching
        Matcher methodMatcher = METHOD_NAME_PATTERN.matcher(task);
        while (methodMatcher.find()) {
            String methodName = methodMatcher.group(1);
            methods.add(methodName);
        }

        // Add methods from impact analysis
        if (impactAnalysis != null && impactAnalysis.getDirectlyAffectedComponents() != null) {
            for (ImpactedComponent component : impactAnalysis.getDirectlyAffectedComponents()) {
                if ("Method".equals(component.getComponentType())) {
                    methods.add(component.getComponentName());
                }
            }
        }

        return new ArrayList<>(methods);
    }

    /**
     * Identifies the packages affected by the proposed refactoring.
     */
    List<String> identifyAffectedPackages(
            String task, String normalizedTask, String refactoringType) {
        Set<String> packages = new LinkedHashSet<>();

        // For Move Package, extract source and target
        if ("Move Package".equals(refactoringType)) {
            Matcher matcher = MOVE_PACKAGE_PATTERN.matcher(normalizedTask);
            if (matcher.find()) {
                packages.add("Source: " + matcher.group(2));
                packages.add("Target: " + matcher.group(3));
            }
        }

        // For Move Class, extract target package
        if ("Move Class".equals(refactoringType)) {
            Matcher matcher = MOVE_CLASS_PATTERN.matcher(normalizedTask);
            if (matcher.find()) {
                packages.add("Target: " + matcher.group(3));
            }
        }

        // Extract package names from task
        Matcher pkgMatcher = PACKAGE_NAME_PATTERN.matcher(task);
        while (pkgMatcher.find()) {
            String pkg = pkgMatcher.group(1);
            if (!pkg.equals("com") && !pkg.equals("org") && !pkg.equals("io")
                    && !pkg.equals("net") && !pkg.startsWith("http")) {
                packages.add(pkg);
            }
        }

        return new ArrayList<>(packages);
    }

    /**
     * Extracts class names from refactoring-specific patterns.
     */
    private void extractPatternedClasses(String normalizedTask, String refactoringType, Set<String> classes) {
        switch (refactoringType) {
            case "Rename Class": {
                Matcher matcher = RENAME_CLASS_PATTERN.matcher(normalizedTask);
                if (matcher.find()) {
                    classes.add(matcher.group(2));
                    classes.add(matcher.group(3) + " (new name)");
                }
                break;
            }
            case "Move Class": {
                Matcher matcher = MOVE_CLASS_PATTERN.matcher(normalizedTask);
                if (matcher.find()) {
                    classes.add(matcher.group(2));
                }
                break;
            }
            case "Extract Class": {
                Matcher matcher = EXTRACT_CLASS_PATTERN.matcher(normalizedTask);
                if (matcher.find()) {
                    classes.add(matcher.group(2) + " (extracted)");
                    classes.add(matcher.group(3) + " (source)");
                }
                break;
            }
            case "Split Large Class": {
                Matcher matcher = SPLIT_LARGE_CLASS_PATTERN.matcher(normalizedTask);
                if (matcher.find()) {
                    classes.add(matcher.group(3));
                }
                break;
            }
            default:
                break;
        }
    }

    /**
     * Extracts method names from refactoring-specific patterns.
     */
    private void extractPatternedMethods(String normalizedTask, String refactoringType, Set<String> methods) {
        switch (refactoringType) {
            case "Rename Method": {
                Matcher matcher = RENAME_METHOD_PATTERN.matcher(normalizedTask);
                if (matcher.find()) {
                    methods.add(matcher.group(2));
                    methods.add(matcher.group(3) + " (new name)");
                }
                break;
            }
            case "Extract Method": {
                Matcher matcher = EXTRACT_METHOD_PATTERN.matcher(normalizedTask);
                if (matcher.find()) {
                    methods.add(matcher.group(2) + " (extracted)");
                    methods.add(matcher.group(3) + " (source)");
                }
                break;
            }
            case "Inline Method": {
                Matcher matcher = INLINE_METHOD_PATTERN.matcher(normalizedTask);
                if (matcher.find()) {
                    methods.add(matcher.group(2));
                }
                break;
            }
            default:
                break;
        }
    }

    /**
     * Builds the list of dependencies involved in the refactoring.
     */
    List<String> buildDependencies(
            String refactoringType, ImpactAnalysisResponse impactAnalysis, String normalizedTask) {
        Set<String> deps = new LinkedHashSet<>();

        // Add dependency impact from impact analysis
        if (impactAnalysis != null && impactAnalysis.getDependencyImpact() != null) {
            deps.addAll(impactAnalysis.getDependencyImpact());
        }

        // Type-specific dependencies
        switch (refactoringType) {
            case "Rename Class":
                deps.add("All import statements referencing the renamed class");
                deps.add("Configuration files referencing the class (e.g., Spring beans, XML config)");
                deps.add("Reflection-based references (e.g., Class.forName, Spring bean names)");
                break;
            case "Rename Method":
                deps.add("All callers of the renamed method across the codebase");
                deps.add("Override/implement relationships in subclasses");
                deps.add("Reflection-based method invocations");
                break;
            case "Move Class":
                deps.add("Import statements in all files referencing the moved class");
                deps.add("Package-private access relationships");
                deps.add("Module descriptors (module-info.java, OSGi, etc.)");
                break;
            case "Move Package":
                deps.add("All import statements referencing the moved package");
                deps.add("Module and build configuration files");
                deps.add("Package scanning configuration (Spring component scan, etc.)");
                break;
            case "Extract Method":
                deps.add("Original method's local variables and parameters");
                deps.add("Access to private fields of the containing class");
                break;
            case "Extract Class":
                deps.add("All references from the original class to the extracted class");
                deps.add("Dependency injection wiring for the new class");
                break;
            case "Inline Method":
                deps.add("All call sites of the method being inlined");
                deps.add("Method's own internal dependencies");
                break;
            case "Delete Dead Code":
                deps.add("Textual references (comments, documentation, configuration)");
                deps.add("Build configuration dependencies (explicit includes/excludes)");
                break;
            case "Split Large Class":
                deps.add("All callers of each responsibility in the original class");
                deps.add("Inter-dependencies between extracted classes");
                deps.add("Dependency injection wiring for all new classes");
                break;
            default:
                deps.add("Internal module dependencies");
                deps.add("Import statements across affected files");
                break;
        }

        return new ArrayList<>(deps);
    }

    /**
     * Builds a deterministic, ordered list of execution steps for the refactoring.
     */
    List<String> buildExecutionOrder(
            String refactoringType, String normalizedTask,
            List<String> affectedClasses, List<String> affectedMethods) {
        List<String> order = new ArrayList<>();
        int stepNumber = 1;

        // Get template steps for this refactoring type
        List<String> templateSteps = EXECUTION_ORDER_TEMPLATES.getOrDefault(
                refactoringType, EXECUTION_ORDER_TEMPLATES.get("General Refactoring"));

        for (String step : templateSteps) {
            order.add(stepNumber++ + ". " + step);
        }

        // Add specific entity references
        if (!affectedClasses.isEmpty()) {
            order.add(stepNumber++ + ". Review affected classes: " +
                    String.join(", ", affectedClasses));
        }

        if (!affectedMethods.isEmpty()) {
            order.add(stepNumber++ + ". Review affected methods: " +
                    String.join(", ", affectedMethods));
        }

        // Final verification
        if (!containsAny(normalizedTask, "delete", "dead", "remove")) {
            order.add(stepNumber + ". Run the full test suite to verify no regressions were introduced");
        } else {
            order.add(stepNumber + ". Verify that the deleted code is not referenced anywhere else in the codebase");
        }

        return order;
    }

    /**
     * Builds a validation checklist for the proposed refactoring.
     */
    List<String> buildValidationChecklist(String refactoringType, String normalizedTask) {
        List<String> checklist = new ArrayList<>();

        // Get template validation items for this refactoring type
        List<String> templateItems = VALIDATION_TEMPLATES.getOrDefault(
                refactoringType, VALIDATION_TEMPLATES.get("General Refactoring"));
        checklist.addAll(templateItems);

        // General validation items always included
        checklist.add("Verify that the code compiles without errors after the refactoring");
        checklist.add("Run the existing test suite to confirm no behavioral changes were introduced");

        // Additional validation based on keywords
        if (containsAny(normalizedTask, "spring", "bean", "autowired", "inject")) {
            checklist.add("Validate Spring bean wiring and dependency injection configuration");
        }

        if (containsAny(normalizedTask, "api", "endpoint", "rest", "controller")) {
            checklist.add("Verify API contracts remain unchanged");
        }

        if (containsAny(normalizedTask, "config", "property", "yml", "yaml")) {
            checklist.add("Validate configuration files for any reference changes");
        }

        return checklist;
    }

    /**
     * Builds the list of recommended tests for the refactoring.
     */
    List<String> buildRecommendedTests(
            String refactoringType, String normalizedTask,
            TestImpactAnalysisResponse testImpact, List<String> impactedClasses) {
        Set<String> tests = new LinkedHashSet<>();

        // Add affected class-specific tests
        if (testImpact != null && testImpact.getRelatedTestClasses() != null) {
            for (String testClass : testImpact.getRelatedTestClasses()) {
                if (!testClass.contains("Integration") && !testClass.contains("End-to-end")) {
                    tests.add(testClass);
                }
            }
        }

        // Derive test names from impacted classes
        for (String affectedClass : impactedClasses) {
            String cleanName = affectedClass;
            int parenIdx = cleanName.indexOf(" (");
            if (parenIdx > 0) {
                cleanName = cleanName.substring(0, parenIdx);
            }
            tests.add(cleanName + "Test");
        }

        // Type-specific test recommendations
        switch (refactoringType) {
            case "Rename Class":
            case "Rename Method":
                tests.add("Compilation test to verify all references are updated");
                break;
            case "Move Class":
            case "Move Package":
                tests.add("Package structure tests");
                tests.add("Class loading and reflection tests");
                break;
            case "Extract Method":
            case "Extract Class":
                tests.add("Unit tests for the extracted component in isolation");
                tests.add("Integration tests verifying the extracted component works with existing code");
                break;
            case "Inline Method":
                tests.add("Unit tests covering the inlined logic at call sites");
                break;
            case "Delete Dead Code":
                tests.add("Compilation test to verify no remaining references");
                break;
            case "Split Large Class":
                tests.add("Unit tests for each extracted class");
                tests.add("Integration tests for inter-class interactions");
                break;
            default:
                tests.add("Regression tests for all affected components");
                break;
        }

        // Add integration tests
        if (testImpact != null && testImpact.getRelatedTestClasses() != null) {
            for (String testClass : testImpact.getRelatedTestClasses()) {
                if (testClass.contains("Integration") || testClass.contains("End-to-end")) {
                    tests.add(testClass);
                }
            }
        }

        return new ArrayList<>(tests);
    }

    /**
     * Builds the list of risks associated with the refactoring.
     */
    List<String> buildRisks(
            String refactoringType, String normalizedTask,
            ImpactAnalysisResponse impactAnalysis, List<String> affectedClasses) {
        Set<String> risks = new LinkedHashSet<>();

        // Add risks from impact analysis
        if (impactAnalysis != null && impactAnalysis.getPotentialRisks() != null) {
            for (RiskItem risk : impactAnalysis.getPotentialRisks()) {
                risks.add(risk.getDescription());
            }
        }

        // Get template risks for this refactoring type
        List<String> templateRisks = RISK_TEMPLATES.getOrDefault(
                refactoringType, RISK_TEMPLATES.get("General Refactoring"));
        risks.addAll(templateRisks);

        // Risk based on number of affected classes
        if (affectedClasses.size() >= 5) {
            risks.add("High number of affected classes increases risk of incomplete refactoring");
        }

        // Security-related risk
        if (containsAny(normalizedTask, "security", "authentication", "authorization",
                "permission", "role", "user", "login")) {
            risks.add("Security-related refactoring requires thorough review to avoid introducing vulnerabilities");
        }

        return new ArrayList<>(risks);
    }

    /**
     * Determines the confidence level of the refactoring analysis.
     */
    String determineConfidence(
            String refactoringType, String normalizedTask,
            TaskAnalysisResponse taskAnalysis, ImpactAnalysisResponse impactAnalysis) {
        // Start with task analysis confidence
        ConfidenceLevel baseConfidence = taskAnalysis.getConfidenceLevel();

        // Adjust based on how well the refactoring type was detected
        boolean hasStrongTypeMatch = !"General Refactoring".equals(refactoringType);

        // Adjust based on impact analysis availability
        boolean hasImpactData = impactAnalysis != null
                && impactAnalysis.getDirectlyAffectedComponents() != null
                && !impactAnalysis.getDirectlyAffectedComponents().isEmpty();

        // High confidence if type is specific and impact data is available
        if (hasStrongTypeMatch && hasImpactData && baseConfidence != ConfidenceLevel.LOW) {
            return "High";
        }

        // Medium confidence if we have type match or impact data
        if (hasStrongTypeMatch || hasImpactData) {
            if (baseConfidence == ConfidenceLevel.LOW) {
                return "Low";
            }
            return "Medium";
        }

        // Default based on task analysis confidence
        return baseConfidence.name();
    }

    // --- Private helper methods ---

    private List<String> getDefaultAffectedClasses(String refactoringType) {
        List<String> defaults = new ArrayList<>();
        switch (refactoringType) {
            case "Rename Class":
                defaults.add("TargetClass (to be renamed)");
                break;
            case "Rename Method":
                defaults.add("ContainingClass (method owner)");
                break;
            case "Move Class":
                defaults.add("ClassToMove");
                break;
            case "Move Package":
                defaults.add("All classes in the source package");
                break;
            case "Extract Method":
                defaults.add("SourceClass (method container)");
                break;
            case "Extract Class":
                defaults.add("SourceClass (extraction origin)");
                defaults.add("ExtractedClass (new class)");
                break;
            case "Inline Method":
                defaults.add("SourceClass (method container)");
                break;
            case "Delete Dead Code":
                defaults.add("DeadCodeComponent (to be removed)");
                break;
            case "Split Large Class":
                defaults.add("LargeClass (to be split)");
                break;
            default:
                defaults.add("RefactoredComponent");
                break;
        }
        return defaults;
    }

    private List<String> impactedClasses(List<String> affectedClasses, ImpactAnalysisResponse impactAnalysis) {
        Set<String> classes = new LinkedHashSet<>(affectedClasses);

        if (impactAnalysis != null && impactAnalysis.getPrimaryTargets() != null) {
            classes.addAll(impactAnalysis.getPrimaryTargets());
        }

        return new ArrayList<>(classes);
    }

    private static Map<String, List<String>> buildExecutionOrderTemplates() {
        Map<String, List<String>> map = new LinkedHashMap<>();

        map.put("Rename Class", List.of(
                "Identify all references to the class across the codebase",
                "Update the class declaration with the new name",
                "Update all import statements referencing the renamed class",
                "Update all configuration files referencing the class name",
                "Update any reflection-based references (e.g., Class.forName, Spring bean names)",
                "Update documentation and comments referencing the class"
        ));

        map.put("Rename Method", List.of(
                "Identify all callers of the method across the codebase",
                "Identify all subclasses that override the method",
                "Rename the method declaration",
                "Update all method invocations",
                "Update any functional interface or lambda references",
                "Update any reflection-based method invocations"
        ));

        map.put("Move Class", List.of(
                "Identify the target package for the class",
                "Update the package declaration in the class file",
                "Move the source file to the new package directory",
                "Update all import statements in files referencing the moved class",
                "Verify package-private access is not broken",
                "Update build configuration if package scanning is used"
        ));

        map.put("Move Package", List.of(
                "Identify the target package path",
                "Move all files in the source package to the target package",
                "Update all import statements across the codebase",
                "Update package declarations in all moved files",
                "Update module descriptors and build configurations",
                "Update package scanning configuration (Spring component scan, etc.)"
        ));

        map.put("Extract Method", List.of(
                "Identify the code block to be extracted",
                "Determine input parameters and return value for the new method",
                "Create the new method with extracted code",
                "Replace the original code block with a call to the new method",
                "Rename the extracted method to be descriptive",
                "Add Javadoc or documentation to the new method"
        ));

        map.put("Extract Class", List.of(
                "Identify the set of related responsibilities to extract",
                "Design the new class interface and API",
                "Create the new class with extracted fields and methods",
                "Update the original class to delegate to the extracted class",
                "Configure dependency injection for the new class",
                "Update callers to use the new class directly where appropriate"
        ));

        map.put("Inline Method", List.of(
                "Identify all call sites of the method to be inlined",
                "Ensure method body can be safely inlined (no recursion, side effects)",
                "Replace each method call with the method body at each call site",
                "Remove the original method declaration",
                "Verify no duplicate code was introduced"
        ));

        map.put("Delete Dead Code", List.of(
                "Verify the code is truly unreferenced (no reflection, no future use)",
                "Remove the dead code declarations",
                "Remove any related imports that are no longer needed",
                "Update any comments or documentation referencing the removed code",
                "Remove any test code that only tested the dead code"
        ));

        map.put("Split Large Class", List.of(
                "Identify the distinct responsibilities in the class",
                "Design the extracted classes and their interfaces",
                "Create new classes for each identified responsibility",
                "Move relevant fields and methods to each new class",
                "Update the original class to delegate or compose with extracted classes",
                "Configure dependency injection for all new classes",
                "Update all callers to use the appropriate class for each responsibility"
        ));

        map.put("General Refactoring", List.of(
                "Analyze the current code structure and identify areas for improvement",
                "Review existing tests to understand the expected behavior",
                "Identify all callers and consumers of the refactored components",
                "Implement the refactoring changes gradually",
                "Update callers to use the new structure",
                "Run full test suite to verify no behavioral changes",
                "Update documentation if public APIs changed"
        ));

        return map;
    }

    private static Map<String, List<String>> buildValidationTemplates() {
        Map<String, List<String>> map = new LinkedHashMap<>();

        List<String> generalValidation = List.of(
                "Verify the behavior remains unchanged after refactoring",
                "Check for any compilation or import issues",
                "Perform code review of the refactoring changes"
        );

        map.put("Rename Class", concatLists(generalValidation, List.of(
                "Verify all references to the old class name have been updated",
                "Check configuration files for any remaining old class name references",
                "Verify logging statements and error messages use the new class name"
        )));

        map.put("Rename Method", concatLists(generalValidation, List.of(
                "Verify all method invocations have been updated",
                "Check for reflection-based invocations that may still use the old name",
                "Verify functional interface compatibility"
        )));

        map.put("Move Class", concatLists(generalValidation, List.of(
                "Verify package-private access is maintained",
                "Check that no import statements still reference the old package",
                "Update module-info.java if present"
        )));

        map.put("Move Package", concatLists(generalValidation, List.of(
                "Verify all imports have been updated across the codebase",
                "Check for any remaining references to the old package path",
                "Update build and deployment configurations"
        )));

        map.put("Extract Method", concatLists(generalValidation, List.of(
                "Verify the extracted method is called correctly at the original location",
                "Check that all necessary parameters are passed to the new method",
                "Verify the method naming is descriptive and follows conventions"
        )));

        map.put("Extract Class", concatLists(generalValidation, List.of(
                "Verify the extracted class is properly instantiated and wired",
                "Check that the original class correctly delegates to the new class",
                "Verify no circular dependencies were introduced"
        )));

        map.put("Inline Method", concatLists(generalValidation, List.of(
                "Verify no duplicate code blocks were introduced",
                "Check that inlined code does not cause side effects",
                "Verify the containing class still compiles correctly"
        )));

        map.put("Delete Dead Code", concatLists(generalValidation, List.of(
                "Verify the deleted code is truly unreferenced",
                "Check for any remaining imports related to the deleted code",
                "Verify no test code needs updating due to the deletion"
        )));

        map.put("Split Large Class", concatLists(generalValidation, List.of(
                "Verify each new class has a single, well-defined responsibility",
                "Check that inter-class dependencies are properly managed",
                "Verify dependency injection wiring for all new classes"
        )));

        map.put("General Refactoring", concatLists(generalValidation, List.of(
                "Verify existing tests still pass without modification",
                "Check for any unintended API changes"
        )));

        return map;
    }

    private static Map<String, List<String>> buildRiskTemplates() {
        Map<String, List<String>> map = new LinkedHashMap<>();

        map.put("Rename Class", List.of(
                "External consumers may reference the old class name and fail to compile",
                "Reflection-based instantiation may break if not updated",
                "Serialization/deserialization may break if class names are used in serialized form",
                "Build and CI/CD configurations may reference the old class name"
        ));

        map.put("Rename Method", List.of(
                "External callers of the method may break if they are not updated",
                "Reflection-based method invocations may fail",
                "Functional interface compatibility may be affected"
        ));

        map.put("Move Class", List.of(
                "Package-private access may break if the new package does not have access",
                "External consumers may have hardcoded import paths",
                "Module system (module-info.java) may need restructuring"
        ));

        map.put("Move Package", List.of(
                "Large-scale import changes may introduce compilation errors",
                "Package scanning configuration may need updates across all environments",
                "Version control history and blame annotations may be lost"
        ));

        map.put("Extract Method", List.of(
                "Extracted method may have unintended side effects on shared state",
                "Parameter passing may introduce subtle bugs if parameters are modified",
                "Method naming may not accurately reflect the extracted logic"
        ));

        map.put("Extract Class", List.of(
                "Dependency injection wiring may become complex",
                "Circular dependencies between the original and extracted class may occur",
                "Performance may be impacted by additional delegation"
        ));

        map.put("Inline Method", List.of(
                "Duplicate code blocks may be introduced at multiple call sites",
                "Inlined code may increase method complexity",
                "Behavioral changes if the inlined method had side effects"
        ));

        map.put("Delete Dead Code", List.of(
                "Code may appear unused but be accessed via reflection or dynamic loading",
                "Removed code may be referenced in documentation or configuration",
                "Historical context for future maintenance may be lost"
        ));

        map.put("Split Large Class", List.of(
                "Multiple new classes may increase overall codebase complexity short-term",
                "Inter-class dependencies may introduce circular references",
                "Dependency injection configuration may become significantly more complex"
        ));

        map.put("General Refactoring", List.of(
                "Refactoring may introduce behavioral changes in unexpected areas",
                "Existing test coverage may be insufficient to detect regressions"
        ));

        return map;
    }

    private static <T> List<T> concatLists(List<T> first, List<T> second) {
        List<T> result = new ArrayList<>(first);
        result.addAll(second);
        return result;
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