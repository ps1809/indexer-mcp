package com.projectiq.mcp.analysis.service;

import com.projectiq.mcp.analysis.dto.ArchitectureInsightsResponse;
import com.projectiq.mcp.analysis.dto.CodeChangeAnalysisResponse;
import com.projectiq.mcp.analysis.dto.DependencyChangePredictionResponse;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse.ImpactedComponent;
import com.projectiq.mcp.analysis.dto.RefactoringAssistantResponse;
import com.projectiq.mcp.analysis.dto.RefactoringImpactSimulationResponse;
import com.projectiq.mcp.pipeline.service.IntelligentContextPipelineService;
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
 * Service that simulates proposed refactoring operations before implementation.
 * Predicts repository-wide effects of structural code changes using indexed
 * repository intelligence, dependency analysis, architecture insights, and
 * workflow context, enabling AI coding agents to safely plan refactoring
 * activities without modifying source code.
 *
 * <p>This service reuses {@link CodeChangeAnalysisService},
 * {@link DependencyChangePredictionService}, {@link RefactoringAssistantService},
 * {@link ImpactAnalysisService}, {@link ArchitectureInsightsService}, and
 * {@link IntelligentContextPipelineService} to gather the necessary analysis data.
 * All outputs are deterministic, stable, and free of duplicate entries.</p>
 *
 * <p>This service NEVER generates code, modifies the repository, performs
 * git operations, or uses any AI/LLM reasoning.</p>
 */
@Service
public class RefactoringImpactSimulationService {

    private static final Logger logger = LoggerFactory.getLogger(RefactoringImpactSimulationService.class);

    private final CodeChangeAnalysisService codeChangeAnalysisService;
    private final DependencyChangePredictionService dependencyChangePredictionService;
    private final RefactoringAssistantService refactoringAssistantService;
    private final ImpactAnalysisService impactAnalysisService;
    private final ArchitectureInsightsService architectureInsightsService;
    private final IntelligentContextPipelineService intelligentContextPipelineService;

    // --- Supported refactoring types ---

    private static final Set<String> SUPPORTED_REFACTORINGS = Set.of(
            "Rename Class",
            "Rename Method",
            "Move Class",
            "Move Package",
            "Extract Interface",
            "Extract Service",
            "Split Class",
            "Merge Classes",
            "Delete Dead Code"
    );

    // --- Refactoring detection patterns ---

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

    private static final Pattern EXTRACT_INTERFACE_PATTERN = Pattern.compile(
            "\\bextract\\s+(interface|contract)\\s+([A-Z][a-zA-Z0-9]*)\\s+from\\s+([A-Z][a-zA-Z0-9]*)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern EXTRACT_SERVICE_PATTERN = Pattern.compile(
            "\\bextract\\s+service\\s+([A-Z][a-zA-Z0-9]*)\\s+from\\s+([A-Z][a-zA-Z0-9]*)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SPLIT_CLASS_PATTERN = Pattern.compile(
            "\\bsplit\\s+(class|type)\\s+([A-Z][a-zA-Z0-9]*)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern MERGE_CLASSES_PATTERN = Pattern.compile(
            "\\bmerge\\s+(classes|types)\\s+([A-Z][a-zA-Z0-9]*)\\s+and\\s+([A-Z][a-zA-Z0-9]*)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern DELETE_DEAD_CODE_PATTERN = Pattern.compile(
            "\\bdelete\\s+(dead|unused|redundant|obsolete)\\s+(code|class|method|field|import)",
            Pattern.CASE_INSENSITIVE
    );

    // --- Entity detection patterns ---

    private static final Pattern CLASS_NAME_PATTERN = Pattern.compile(
            "\\b([A-Z][a-zA-Z0-9]*(?:Controller|Service|Repository|Entity|Dto|Config|Helper|Util|Manager|Provider|Factory|Mapper|Validator|Handler|Processor|Builder|Adapter|Listener|Filter|Interceptor|Component|Bean|Model|Domain|Vo|Pojo|Form|View|Resource|Endpoint|Dao|DataAccess|Storage))\\b"
    );

    private static final Pattern METHOD_NAME_PATTERN = Pattern.compile(
            "\\b([a-z][a-zA-Z0-9]*\\(\\s*\\)|[a-z][a-zA-Z0-9]*By[A-Z][a-zA-Z0-9]*|[a-z][a-zA-Z0-9]*Exception)\\b"
    );

    private static final Pattern PACKAGE_NAME_PATTERN = Pattern.compile(
            "\\b([a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]*){2,})\\b"
    );

    // --- Deterministic broken reference templates by refactoring type ---

    private static final Map<String, List<String>> BROKEN_REFERENCE_TEMPLATES = buildBrokenReferenceTemplates();

    // --- Deterministic architectural effect templates by refactoring type ---

    private static final Map<String, List<String>> ARCHITECTURAL_EFFECT_TEMPLATES = buildArchitecturalEffectTemplates();

    // --- Deterministic testing impact templates by refactoring type ---

    private static final Map<String, List<String>> TESTING_IMPACT_TEMPLATES = buildTestingImpactTemplates();

    // --- Deterministic risk templates by refactoring type ---

    private static final Map<String, List<String>> RISK_TEMPLATES = buildRiskTemplates();

    // --- Deterministic implementation sequence templates by refactoring type ---

    private static final Map<String, List<String>> IMPLEMENTATION_SEQUENCE_TEMPLATES = buildImplementationSequenceTemplates();

    // --- Deterministic effort estimates by refactoring type ---

    private static final Map<String, String> EFFORT_ESTIMATES = buildEffortEstimates();

    public RefactoringImpactSimulationService(
            CodeChangeAnalysisService codeChangeAnalysisService,
            DependencyChangePredictionService dependencyChangePredictionService,
            RefactoringAssistantService refactoringAssistantService,
            ImpactAnalysisService impactAnalysisService,
            ArchitectureInsightsService architectureInsightsService,
            IntelligentContextPipelineService intelligentContextPipelineService) {
        this.codeChangeAnalysisService = codeChangeAnalysisService;
        this.dependencyChangePredictionService = dependencyChangePredictionService;
        this.refactoringAssistantService = refactoringAssistantService;
        this.impactAnalysisService = impactAnalysisService;
        this.architectureInsightsService = architectureInsightsService;
        this.intelligentContextPipelineService = intelligentContextPipelineService;
    }

    /**
     * Simulates a proposed refactoring operation and produces a deterministic
     * simulation report predicting the repository-wide impact.
     *
     * @param refactoringType  the type of refactoring (e.g., "Rename Class", "Move Class")
     * @param targetEntity     the target entity name (e.g., class name, method name)
     * @param sourceContext    additional context describing the refactoring (e.g., new name, target package)
     * @param repositoryName   the repository name
     * @param branch           the git branch (optional, defaults to "main")
     * @return a deterministic simulation report
     * @throws IllegalArgumentException if the refactoring type is unsupported or inputs are invalid
     */
    public RefactoringImpactSimulationResponse simulateRefactoring(
            String refactoringType, String targetEntity, String sourceContext,
            String repositoryName, String branch) {
        if (refactoringType == null || refactoringType.trim().isEmpty()) {
            throw new IllegalArgumentException("Refactoring type is required");
        }
        if (targetEntity == null || targetEntity.trim().isEmpty()) {
            throw new IllegalArgumentException("Target entity is required");
        }

        String normalizedType = refactoringType.trim();
        if (!SUPPORTED_REFACTORINGS.contains(normalizedType)) {
            throw new IllegalArgumentException("Unsupported refactoring type: " + normalizedType
                    + ". Supported types: " + String.join(", ", SUPPORTED_REFACTORINGS));
        }

        logger.info("Simulating refactoring: type={}, target={}, context={}, repository={}",
                normalizedType, targetEntity, sourceContext, repositoryName);

        String effectiveBranch = (branch != null && !branch.trim().isEmpty()) ? branch.trim() : "main";
        String effectiveContext = (sourceContext != null && !sourceContext.trim().isEmpty()) ? sourceContext.trim() : "";
        String effectiveTarget = targetEntity.trim();

        // Build a natural language task description for reuse services
        String taskDescription = buildTaskDescription(normalizedType, effectiveTarget, effectiveContext);

        // Step 1: Invoke code change analysis (non-critical)
        CodeChangeAnalysisResponse codeChangeAnalysis = null;
        try {
            codeChangeAnalysis = codeChangeAnalysisService.analyzeCodeChange(
                    taskDescription, repositoryName, effectiveBranch);
        } catch (Exception e) {
            logger.warn("Failed to analyze code change: {}", e.getMessage());
        }

        // Step 2: Invoke dependency change prediction (non-critical)
        DependencyChangePredictionResponse depPrediction = null;
        try {
            depPrediction = dependencyChangePredictionService.predictDependencyChangeFromDescription(
                    taskDescription, repositoryName);
        } catch (Exception e) {
            logger.warn("Failed to predict dependency changes: {}", e.getMessage());
        }

        // Step 3: Invoke refactoring assistant (non-critical)
        RefactoringAssistantResponse refactoringAssistant = null;
        try {
            refactoringAssistant = refactoringAssistantService.analyzeRefactoring(
                    taskDescription, repositoryName, effectiveBranch);
        } catch (Exception e) {
            logger.warn("Failed to analyze refactoring: {}", e.getMessage());
        }

        // Step 4: Invoke impact analysis (non-critical)
        ImpactAnalysisResponse impactAnalysis = null;
        try {
            impactAnalysis = impactAnalysisService.analyzeImpact(
                    taskDescription, repositoryName, effectiveBranch);
        } catch (Exception e) {
            logger.warn("Failed to analyze impact: {}", e.getMessage());
        }

        // Step 5: Invoke architecture insights (non-critical)
        ArchitectureInsightsResponse architectureInsights = null;
        try {
            architectureInsights = architectureInsightsService.analyzeArchitecture(
                    repositoryName, effectiveBranch);
        } catch (Exception e) {
            logger.warn("Failed to analyze architecture: {}", e.getMessage());
        }

        // Step 6: Invoke context pipeline (non-critical)
        try {
            intelligentContextPipelineService.buildContextPipeline(
                    taskDescription, repositoryName, effectiveBranch, "", "");
        } catch (Exception e) {
            logger.warn("Failed to build context pipeline: {}", e.getMessage());
        }

        // Step 7: Build the simulation response
        RefactoringImpactSimulationResponse response = new RefactoringImpactSimulationResponse();

        // Build refactoring summary
        String summary = buildRefactoringSummary(normalizedType, effectiveTarget, effectiveContext);
        response.setRefactoringSummary(summary);
        response.setRefactoringType(normalizedType);
        response.setTargetEntity(effectiveTarget);
        response.setSourceContext(effectiveContext);

        // Identify impacted files
        List<String> impactedFiles = identifyImpactedFiles(
                normalizedType, effectiveTarget, effectiveContext, codeChangeAnalysis, impactAnalysis);
        response.setImpactedFiles(impactedFiles);

        // Identify impacted classes
        List<String> impactedClasses = identifyImpactedClasses(
                normalizedType, effectiveTarget, effectiveContext, codeChangeAnalysis, impactAnalysis, refactoringAssistant);
        response.setImpactedClasses(impactedClasses);

        // Identify impacted methods
        List<String> impactedMethods = identifyImpactedMethods(
                normalizedType, effectiveTarget, effectiveContext, impactAnalysis);
        response.setImpactedMethods(impactedMethods);

        // Identify broken references
        List<String> brokenReferences = identifyBrokenReferences(
                normalizedType, effectiveTarget, effectiveContext);
        response.setBrokenReferences(brokenReferences);

        // Identify dependency changes
        List<String> dependencyChanges = identifyDependencyChanges(
                normalizedType, effectiveTarget, effectiveContext, depPrediction);
        response.setDependencyChanges(dependencyChanges);

        // Identify architectural effects
        List<String> architecturalEffects = identifyArchitecturalEffects(
                normalizedType, effectiveTarget, effectiveContext, architectureInsights);
        response.setArchitecturalEffects(architecturalEffects);

        // Identify testing impact
        List<String> testingImpact = identifyTestingImpact(
                normalizedType, effectiveTarget, effectiveContext, impactedClasses);
        response.setTestingImpact(testingImpact);

        // Build risk assessment
        List<String> riskAssessment = buildRiskAssessment(
                normalizedType, effectiveTarget, effectiveContext, impactedClasses, impactedFiles);
        response.setRiskAssessment(riskAssessment);

        // Build implementation sequence
        List<String> implementationSequence = buildImplementationSequence(
                normalizedType, effectiveTarget, effectiveContext, impactedClasses, impactedMethods);
        response.setSuggestedImplementationSequence(implementationSequence);

        // Estimate effort
        String effort = estimateEffort(normalizedType, impactedClasses, impactedFiles);
        response.setEstimatedEffort(effort);

        logger.info("Refactoring simulation complete: type={}, target={}, files={}, classes={}, methods={}",
                normalizedType, effectiveTarget, impactedFiles.size(), impactedClasses.size(), impactedMethods.size());

        return response;
    }

    /**
     * Builds a natural language task description from the refactoring parameters.
     */
    private String buildTaskDescription(String refactoringType, String targetEntity, String sourceContext) {
        StringBuilder sb = new StringBuilder();
        sb.append(refactoringType).append(" ").append(targetEntity);
        if (!sourceContext.isEmpty()) {
            sb.append(" ").append(sourceContext);
        }
        return sb.toString();
    }

    /**
     * Builds a human-readable summary of the proposed refactoring.
     */
    String buildRefactoringSummary(String refactoringType, String targetEntity, String sourceContext) {
        StringBuilder summary = new StringBuilder();
        summary.append("Proposed Refactoring: ").append(refactoringType);
        summary.append(" on ").append(targetEntity);
        if (!sourceContext.isEmpty()) {
            summary.append(" (").append(sourceContext).append(")");
        }
        summary.append(". This simulation predicts the repository-wide impact ")
                .append("of the proposed refactoring without modifying any code.");
        return summary.toString();
    }

    /**
     * Identifies files that will be impacted by the proposed refactoring.
     */
    List<String> identifyImpactedFiles(
            String refactoringType, String targetEntity, String sourceContext,
            CodeChangeAnalysisResponse codeChangeAnalysis, ImpactAnalysisResponse impactAnalysis) {
        Set<String> files = new LinkedHashSet<>();

        // Add files from code change analysis
        if (codeChangeAnalysis != null && codeChangeAnalysis.getImpactedFiles() != null) {
            files.addAll(codeChangeAnalysis.getImpactedFiles());
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
        files.addAll(getTypeSpecificFiles(refactoringType, targetEntity));

        // If still empty, add default files
        if (files.isEmpty()) {
            files.add(targetEntity + ".java");
            files.add(targetEntity + "Test.java");
        }

        return new ArrayList<>(files);
    }

    /**
     * Identifies classes that will be impacted by the proposed refactoring.
     */
    List<String> identifyImpactedClasses(
            String refactoringType, String targetEntity, String sourceContext,
            CodeChangeAnalysisResponse codeChangeAnalysis, ImpactAnalysisResponse impactAnalysis,
            RefactoringAssistantResponse refactoringAssistant) {
        Set<String> classes = new LinkedHashSet<>();

        // Add target entity
        classes.add(targetEntity);

        // Add classes from code change analysis
        if (codeChangeAnalysis != null && codeChangeAnalysis.getImpactedClasses() != null) {
            classes.addAll(codeChangeAnalysis.getImpactedClasses());
        }

        // Add classes from refactoring assistant
        if (refactoringAssistant != null && refactoringAssistant.getAffectedClasses() != null) {
            classes.addAll(refactoringAssistant.getAffectedClasses());
        }

        // Add directly affected components from impact analysis
        if (impactAnalysis != null && impactAnalysis.getDirectlyAffectedComponents() != null) {
            for (ImpactedComponent component : impactAnalysis.getDirectlyAffectedComponents()) {
                String name = component.getComponentName();
                String type = component.getComponentType();
                if ("Class".equals(type) || "Configuration".equals(type) || "DTO".equals(type)) {
                    classes.add(name);
                }
            }
        }

        // Add indirectly affected components that are classes
        if (impactAnalysis != null && impactAnalysis.getIndirectlyAffectedComponents() != null) {
            for (ImpactedComponent component : impactAnalysis.getIndirectlyAffectedComponents()) {
                String name = component.getComponentName();
                String type = component.getComponentType();
                if ("Class".equals(type) || "DTO".equals(type)) {
                    classes.add(name);
                }
            }
        }

        // Add type-specific classes
        classes.addAll(getTypeSpecificClasses(refactoringType, targetEntity, sourceContext));

        return new ArrayList<>(classes);
    }

    /**
     * Identifies methods that will be impacted by the proposed refactoring.
     */
    List<String> identifyImpactedMethods(
            String refactoringType, String targetEntity, String sourceContext,
            ImpactAnalysisResponse impactAnalysis) {
        Set<String> methods = new LinkedHashSet<>();

        // Add methods from impact analysis
        if (impactAnalysis != null && impactAnalysis.getDirectlyAffectedComponents() != null) {
            for (ImpactedComponent component : impactAnalysis.getDirectlyAffectedComponents()) {
                if ("Method".equals(component.getComponentType())) {
                    methods.add(component.getComponentName());
                }
            }
        }

        // Add type-specific methods
        methods.addAll(getTypeSpecificMethods(refactoringType, targetEntity, sourceContext));

        return new ArrayList<>(methods);
    }

    /**
     * Identifies broken references that would result from the proposed refactoring.
     */
    List<String> identifyBrokenReferences(
            String refactoringType, String targetEntity, String sourceContext) {
        Set<String> references = new LinkedHashSet<>();

        // Get template broken references for this refactoring type
        List<String> templateRefs = BROKEN_REFERENCE_TEMPLATES.getOrDefault(refactoringType, List.of());
        for (String ref : templateRefs) {
            references.add(ref.replace("{target}", targetEntity));
        }

        // Add entity-specific broken references
        references.addAll(getEntitySpecificBrokenReferences(refactoringType, targetEntity, sourceContext));

        return new ArrayList<>(references);
    }

    /**
     * Identifies dependency changes that would result from the proposed refactoring.
     */
    List<String> identifyDependencyChanges(
            String refactoringType, String targetEntity, String sourceContext,
            DependencyChangePredictionResponse depPrediction) {
        Set<String> changes = new LinkedHashSet<>();

        // Add dependency changes from prediction service
        if (depPrediction != null) {
            if (depPrediction.getTransitiveDependencyEffects() != null) {
                changes.addAll(depPrediction.getTransitiveDependencyEffects());
            }
            if (depPrediction.getCompatibilityRisks() != null) {
                changes.addAll(depPrediction.getCompatibilityRisks());
            }
            if (depPrediction.getTestingImpact() != null) {
                changes.addAll(depPrediction.getTestingImpact());
            }
        }

        // Add type-specific dependency changes
        changes.addAll(getTypeSpecificDependencyChanges(refactoringType, targetEntity, sourceContext));

        return new ArrayList<>(changes);
    }

    /**
     * Identifies architectural effects of the proposed refactoring.
     */
    List<String> identifyArchitecturalEffects(
            String refactoringType, String targetEntity, String sourceContext,
            ArchitectureInsightsResponse architectureInsights) {
        Set<String> effects = new LinkedHashSet<>();

        // Add architectural effects from architecture insights
        if (architectureInsights != null && architectureInsights.getArchitecturalStrengths() != null) {
            for (String strength : architectureInsights.getArchitecturalStrengths()) {
                effects.add("Architectural strength affected: " + strength);
            }
        }
        if (architectureInsights != null && architectureInsights.getPotentialConcerns() != null) {
            for (String concern : architectureInsights.getPotentialConcerns()) {
                effects.add("Architectural concern relevant: " + concern);
            }
        }

        // Get template architectural effects for this refactoring type
        List<String> templateEffects = ARCHITECTURAL_EFFECT_TEMPLATES.getOrDefault(refactoringType, List.of());
        for (String effect : templateEffects) {
            effects.add(effect.replace("{target}", targetEntity));
        }

        return new ArrayList<>(effects);
    }

    /**
     * Identifies testing impact of the proposed refactoring.
     */
    List<String> identifyTestingImpact(
            String refactoringType, String targetEntity, String sourceContext,
            List<String> impactedClasses) {
        Set<String> impacts = new LinkedHashSet<>();

        // Get template testing impacts for this refactoring type
        List<String> templateImpacts = TESTING_IMPACT_TEMPLATES.getOrDefault(refactoringType, List.of());
        for (String impact : templateImpacts) {
            impacts.add(impact.replace("{target}", targetEntity));
        }

        // Add class-specific testing impacts
        for (String impactedClass : impactedClasses) {
            String cleanName = impactedClass;
            int parenIdx = cleanName.indexOf(" (");
            if (parenIdx > 0) {
                cleanName = cleanName.substring(0, parenIdx);
            }
            impacts.add(cleanName + "Test requires updates");
        }

        return new ArrayList<>(impacts);
    }

    /**
     * Builds a risk assessment for the proposed refactoring.
     */
    List<String> buildRiskAssessment(
            String refactoringType, String targetEntity, String sourceContext,
            List<String> impactedClasses, List<String> impactedFiles) {
        Set<String> risks = new LinkedHashSet<>();

        // Get template risks for this refactoring type
        List<String> templateRisks = RISK_TEMPLATES.getOrDefault(refactoringType, List.of());
        for (String risk : templateRisks) {
            risks.add(risk.replace("{target}", targetEntity));
        }

        // Risk based on number of impacted classes
        if (impactedClasses.size() >= 5) {
            risks.add("High number of impacted classes increases risk of incomplete refactoring");
        } else if (impactedClasses.size() >= 3) {
            risks.add("Multiple impacted classes require coordinated changes");
        }

        // Risk based on number of impacted files
        if (impactedFiles.size() >= 5) {
            risks.add("Wide file impact increases change collision risk");
        }

        return new ArrayList<>(risks);
    }

    /**
     * Builds a suggested implementation sequence for the proposed refactoring.
     */
    List<String> buildImplementationSequence(
            String refactoringType, String targetEntity, String sourceContext,
            List<String> impactedClasses, List<String> impactedMethods) {
        List<String> sequence = new ArrayList<>();
        int stepNumber = 1;

        // Get template sequence for this refactoring type
        List<String> templateSteps = IMPLEMENTATION_SEQUENCE_TEMPLATES.getOrDefault(
                refactoringType, IMPLEMENTATION_SEQUENCE_TEMPLATES.get("Rename Class"));
        for (String step : templateSteps) {
            sequence.add(stepNumber++ + ". " + step.replace("{target}", targetEntity));
        }

        // Add specific entity references
        if (!impactedClasses.isEmpty()) {
            sequence.add(stepNumber++ + ". Review and update impacted classes: " +
                    String.join(", ", impactedClasses));
        }

        if (!impactedMethods.isEmpty()) {
            sequence.add(stepNumber++ + ". Review and update impacted methods: " +
                    String.join(", ", impactedMethods));
        }

        // Final verification
        sequence.add(stepNumber + ". Run the full test suite to verify no regressions were introduced");

        return sequence;
    }

    /**
     * Estimates the implementation effort for the proposed refactoring.
     */
    String estimateEffort(String refactoringType, List<String> impactedClasses, List<String> impactedFiles) {
        String baseEffort = EFFORT_ESTIMATES.getOrDefault(refactoringType, "Medium");

        // Adjust based on scope
        int totalImpacted = impactedClasses.size() + impactedFiles.size();
        if (totalImpacted >= 8) {
            return "High";
        } else if (totalImpacted >= 4) {
            return "Medium";
        } else if (totalImpacted <= 1) {
            return "Low";
        }

        return baseEffort;
    }

    // --- Private helper methods ---

    private String deriveFileName(String componentName, String componentType) {
        if (componentName == null || componentName.trim().isEmpty()) {
            return null;
        }

        String cleanName = componentName.trim();
        if (cleanName.startsWith("Endpoint: ")) {
            cleanName = cleanName.substring("Endpoint: ".length());
        } else if (cleanName.startsWith("Package: ")) {
            return null;
        }

        switch (componentType) {
            case "Class":
            case "DTO":
                return cleanName + ".java";
            case "Configuration":
                return cleanName + ".java";
            case "REST API":
                return cleanName + ".java";
            case "Testing":
                return null;
            case "Documentation":
                return cleanName + ".md";
            default:
                return cleanName + ".java";
        }
    }

    private List<String> getTypeSpecificFiles(String refactoringType, String targetEntity) {
        Set<String> files = new LinkedHashSet<>();

        switch (refactoringType) {
            case "Rename Class":
                files.add(targetEntity + ".java");
                files.add(targetEntity + "Test.java");
                break;
            case "Rename Method":
                files.add(targetEntity + ".java");
                break;
            case "Move Class":
                files.add(targetEntity + ".java");
                files.add(targetEntity + "Test.java");
                break;
            case "Move Package":
                files.add("All files in source package");
                break;
            case "Extract Interface":
                files.add(targetEntity + ".java");
                files.add(targetEntity + "Impl.java");
                break;
            case "Extract Service":
                files.add(targetEntity + ".java");
                files.add(targetEntity + "Service.java");
                break;
            case "Split Class":
                files.add(targetEntity + ".java");
                files.add(targetEntity + "Part1.java");
                files.add(targetEntity + "Part2.java");
                break;
            case "Merge Classes":
                files.add(targetEntity + ".java");
                break;
            case "Delete Dead Code":
                files.add(targetEntity + ".java");
                break;
            default:
                break;
        }

        return new ArrayList<>(files);
    }

    private List<String> getTypeSpecificClasses(String refactoringType, String targetEntity, String sourceContext) {
        Set<String> classes = new LinkedHashSet<>();

        switch (refactoringType) {
            case "Rename Class":
                classes.add(targetEntity + " (to be renamed)");
                if (!sourceContext.isEmpty()) {
                    classes.add(sourceContext + " (new name)");
                }
                break;
            case "Rename Method":
                classes.add(targetEntity + " (containing class)");
                break;
            case "Move Class":
                classes.add(targetEntity + " (to be moved)");
                break;
            case "Move Package":
                classes.add("All classes in source package");
                break;
            case "Extract Interface":
                classes.add(targetEntity + " (interface)");
                classes.add(targetEntity + "Impl (implementation)");
                break;
            case "Extract Service":
                classes.add(targetEntity + " (extracted service)");
                if (!sourceContext.isEmpty()) {
                    classes.add(sourceContext + " (source class)");
                }
                break;
            case "Split Class":
                classes.add(targetEntity + " (to be split)");
                classes.add(targetEntity + "Part1 (extracted)");
                classes.add(targetEntity + "Part2 (extracted)");
                break;
            case "Merge Classes":
                classes.add(targetEntity + " (target)");
                if (!sourceContext.isEmpty()) {
                    classes.add(sourceContext + " (source)");
                }
                break;
            case "Delete Dead Code":
                classes.add(targetEntity + " (to be removed)");
                break;
            default:
                break;
        }

        return new ArrayList<>(classes);
    }

    private List<String> getTypeSpecificMethods(String refactoringType, String targetEntity, String sourceContext) {
        Set<String> methods = new LinkedHashSet<>();

        switch (refactoringType) {
            case "Rename Method":
                methods.add(targetEntity + "() (to be renamed)");
                if (!sourceContext.isEmpty()) {
                    methods.add(sourceContext + "() (new name)");
                }
                break;
            case "Extract Interface":
                methods.add("All public methods of " + targetEntity);
                break;
            case "Extract Service":
                methods.add("Business logic methods to be extracted");
                break;
            case "Split Class":
                methods.add("Methods to be distributed across new classes");
                break;
            case "Merge Classes":
                methods.add("Methods from source class to be merged");
                break;
            case "Delete Dead Code":
                methods.add(targetEntity + "() (to be removed)");
                break;
            default:
                break;
        }

        return new ArrayList<>(methods);
    }

    private List<String> getEntitySpecificBrokenReferences(
            String refactoringType, String targetEntity, String sourceContext) {
        Set<String> refs = new LinkedHashSet<>();

        switch (refactoringType) {
            case "Rename Class":
                refs.add("Import statements referencing " + targetEntity);
                refs.add("Configuration entries referencing " + targetEntity);
                refs.add("Reflection-based references to " + targetEntity);
                break;
            case "Rename Method":
                refs.add("All callers of " + targetEntity + "()");
                refs.add("Override declarations in subclasses");
                refs.add("Reflection-based method invocations");
                break;
            case "Move Class":
                refs.add("Import statements referencing " + targetEntity);
                refs.add("Package-private access relationships");
                break;
            case "Move Package":
                refs.add("All import statements referencing the source package");
                refs.add("Package scanning configuration");
                break;
            case "Extract Interface":
                refs.add("Direct references to " + targetEntity + " implementation");
                refs.add("Constructor invocations of " + targetEntity);
                break;
            case "Extract Service":
                refs.add("Direct field references to extracted logic");
                refs.add("Dependency injection wiring");
                break;
            case "Split Class":
                refs.add("All references to " + targetEntity);
                refs.add("Callers of each responsibility in the original class");
                break;
            case "Merge Classes":
                refs.add("Duplicate method signatures");
                refs.add("Conflicting field names");
                break;
            case "Delete Dead Code":
                refs.add("Textual references in comments and documentation");
                refs.add("Build configuration references");
                break;
            default:
                break;
        }

        return new ArrayList<>(refs);
    }

    private List<String> getTypeSpecificDependencyChanges(
            String refactoringType, String targetEntity, String sourceContext) {
        Set<String> changes = new LinkedHashSet<>();

        switch (refactoringType) {
            case "Rename Class":
                changes.add("All import statements referencing " + targetEntity + " need updating");
                changes.add("Configuration files referencing " + targetEntity + " need updating");
                break;
            case "Rename Method":
                changes.add("All callers of " + targetEntity + "() need updating");
                break;
            case "Move Class":
                changes.add("Import statements in all files referencing " + targetEntity + " need updating");
                changes.add("Package-private access may be broken");
                break;
            case "Move Package":
                changes.add("All import statements referencing the source package need updating");
                changes.add("Module descriptors and build configurations need updating");
                break;
            case "Extract Interface":
                changes.add("All references to concrete class need replacement with interface");
                changes.add("Dependency injection wiring needs interface-based configuration");
                break;
            case "Extract Service":
                changes.add("Original class loses extracted responsibilities");
                changes.add("New service class needs dependency injection wiring");
                break;
            case "Split Class":
                changes.add("All callers of " + targetEntity + " need to reference new classes");
                changes.add("Inter-dependencies between extracted classes need management");
                break;
            case "Merge Classes":
                changes.add("Consolidated imports from both source classes");
                changes.add("Unified dependency injection wiring");
                break;
            case "Delete Dead Code":
                changes.add("Remove related imports that are no longer needed");
                changes.add("Remove build configuration references");
                break;
            default:
                break;
        }

        return new ArrayList<>(changes);
    }

    private boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }

    // --- Template builders ---

    private static Map<String, List<String>> buildBrokenReferenceTemplates() {
        Map<String, List<String>> map = new LinkedHashMap<>();

        map.put("Rename Class", List.of(
                "Import statements referencing {target} will break",
                "Configuration entries referencing {target} will break",
                "Reflection-based references to {target} will break",
                "Serialization/deserialization using {target} class name will break"
        ));

        map.put("Rename Method", List.of(
                "All callers of {target}() will break",
                "Override declarations in subclasses will break",
                "Functional interface references to {target} will break",
                "Reflection-based method invocations will break"
        ));

        map.put("Move Class", List.of(
                "Import statements referencing {target} will break",
                "Package-private access relationships may break",
                "Module descriptors referencing the old package will break"
        ));

        map.put("Move Package", List.of(
                "All import statements referencing the source package will break",
                "Package scanning configuration will break",
                "Module descriptors referencing the old package path will break"
        ));

        map.put("Extract Interface", List.of(
                "Direct references to {target} implementation class will break",
                "Constructor invocations of {target} will need refactoring",
                "Static method references to {target} will break"
        ));

        map.put("Extract Service", List.of(
                "Direct field references to extracted logic in original class will break",
                "Dependency injection wiring for extracted responsibilities will break",
                "Method calls to extracted logic within the original class will break"
        ));

        map.put("Split Class", List.of(
                "All references to {target} will break",
                "Callers referencing specific responsibilities will need to target new classes",
                "Dependency injection wiring for {target} will break"
        ));

        map.put("Merge Classes", List.of(
                "Duplicate method signatures will cause compilation errors",
                "Conflicting field names will cause compilation errors",
                "References to either source class will need updating"
        ));

        map.put("Delete Dead Code", List.of(
                "Textual references in comments and documentation may reference removed code",
                "Build configuration may reference removed source files",
                "Any remaining imports for removed code will cause compilation errors"
        ));

        return map;
    }

    private static Map<String, List<String>> buildArchitecturalEffectTemplates() {
        Map<String, List<String>> map = new LinkedHashMap<>();

        map.put("Rename Class", List.of(
                "No architectural pattern changes - class name update only",
                "Package structure remains unchanged"
        ));

        map.put("Rename Method", List.of(
                "No architectural pattern changes - method name update only",
                "Class interface contract changes"
        ));

        map.put("Move Class", List.of(
                "Package structure changes - class moves to new package",
                "Module dependency graph may change if moved across module boundaries"
        ));

        map.put("Move Package", List.of(
                "Package hierarchy restructured",
                "Module dependency graph may change significantly",
                "Package scanning and auto-configuration scope changes"
        ));

        map.put("Extract Interface", List.of(
                "Introduces abstraction layer between consumers and implementation",
                "Enables polymorphism and dependency inversion",
                "Improves testability through interface-based mocking"
        ));

        map.put("Extract Service", List.of(
                "Introduces new service layer component",
                "Reduces responsibility of the original class",
                "Improves separation of concerns"
        ));

        map.put("Split Class", List.of(
                "Single large class decomposed into multiple focused classes",
                "Improves adherence to Single Responsibility Principle",
                "May introduce new package structure for extracted classes"
        ));

        map.put("Merge Classes", List.of(
                "Consolidates related functionality into a single class",
                "Reduces class count but may increase class complexity",
                "May require interface extraction to maintain testability"
        ));

        map.put("Delete Dead Code", List.of(
                "Reduces codebase size and complexity",
                "Removes unused dependencies and imports",
                "Improves maintainability and reduces cognitive load"
        ));

        return map;
    }

    private static Map<String, List<String>> buildTestingImpactTemplates() {
        Map<String, List<String>> map = new LinkedHashMap<>();

        map.put("Rename Class", List.of(
                "{target}Test requires class name reference updates",
                "All test imports referencing {target} need updating"
        ));

        map.put("Rename Method", List.of(
                "All test methods invoking {target}() need updating",
                "Test method names referencing {target} may need updating"
        ));

        map.put("Move Class", List.of(
                "{target}Test needs package declaration update",
                "All test imports referencing {target} need updating"
        ));

        map.put("Move Package", List.of(
                "All test classes in the package need package declaration updates",
                "Test configuration files may need package scan updates"
        ));

        map.put("Extract Interface", List.of(
                "Tests for {target} implementation need interface-based mocking",
                "New interface contract tests should be written",
                "Existing tests may need refactoring to use interface type"
        ));

        map.put("Extract Service", List.of(
                "New service class requires dedicated unit tests",
                "Original class tests need reduction in scope",
                "Integration tests between original and new service needed"
        ));

        map.put("Split Class", List.of(
                "Each extracted class requires dedicated unit tests",
                "Integration tests for inter-class interactions needed",
                "Original {target}Test needs significant restructuring"
        ));

        map.put("Merge Classes", List.of(
                "Consolidated test class needed for merged functionality",
                "Existing test classes for source classes need merging",
                "Duplicate test coverage needs elimination"
        ));

        map.put("Delete Dead Code", List.of(
                "Remove test classes that only tested the dead code",
                "Update any test utilities that referenced the removed code"
        ));

        return map;
    }

    private static Map<String, List<String>> buildRiskTemplates() {
        Map<String, List<String>> map = new LinkedHashMap<>();

        map.put("Rename Class", List.of(
                "External consumers may reference the old class name and fail to compile",
                "Reflection-based instantiation may break if not updated",
                "Serialization/deserialization may break if class names are used in serialized form"
        ));

        map.put("Rename Method", List.of(
                "External callers of {target}() may break if not updated",
                "Reflection-based method invocations may fail",
                "Functional interface compatibility may be affected"
        ));

        map.put("Move Class", List.of(
                "Package-private access may break if the new package does not have access",
                "External consumers may have hardcoded import paths",
                "Module system may need restructuring"
        ));

        map.put("Move Package", List.of(
                "Large-scale import changes may introduce compilation errors",
                "Package scanning configuration may need updates across all environments",
                "Version control history and blame annotations may be lost"
        ));

        map.put("Extract Interface", List.of(
                "Existing consumers may need to be updated to use the interface type",
                "Factory or provider pattern may be needed for implementation selection",
                "Interface may not cover all use cases of the concrete class"
        ));

        map.put("Extract Service", List.of(
                "Extracted service may have unintended dependencies on the original class",
                "Transaction boundaries may need redefinition",
                "Circular dependencies between original and extracted service may occur"
        ));

        map.put("Split Class", List.of(
                "Multiple new classes may increase overall codebase complexity short-term",
                "Inter-class dependencies may introduce circular references",
                "Dependency injection configuration may become significantly more complex"
        ));

        map.put("Merge Classes", List.of(
                "Merged class may violate Single Responsibility Principle",
                "Merge may introduce duplicate or conflicting behavior",
                "Testing the merged class may become more complex"
        ));

        map.put("Delete Dead Code", List.of(
                "Code may appear unused but be accessed via reflection or dynamic loading",
                "Removed code may be referenced in documentation or configuration",
                "Historical context for future maintenance may be lost"
        ));

        return map;
    }

    private static Map<String, List<String>> buildImplementationSequenceTemplates() {
        Map<String, List<String>> map = new LinkedHashMap<>();

        map.put("Rename Class", List.of(
                "Identify all references to {target} across the codebase",
                "Update the class declaration with the new name",
                "Update all import statements referencing the renamed class",
                "Update all configuration files referencing the class name",
                "Update any reflection-based references",
                "Update documentation and comments referencing the class"
        ));

        map.put("Rename Method", List.of(
                "Identify all callers of {target}() across the codebase",
                "Identify all subclasses that override {target}()",
                "Rename the method declaration",
                "Update all method invocations",
                "Update any functional interface or lambda references",
                "Update any reflection-based method invocations"
        ));

        map.put("Move Class", List.of(
                "Identify the target package for {target}",
                "Update the package declaration in the class file",
                "Move the source file to the new package directory",
                "Update all import statements in files referencing {target}",
                "Verify package-private access is not broken",
                "Update build configuration if package scanning is used"
        ));

        map.put("Move Package", List.of(
                "Identify the target package path",
                "Move all files in the source package to the target package",
                "Update all import statements across the codebase",
                "Update package declarations in all moved files",
                "Update module descriptors and build configurations",
                "Update package scanning configuration"
        ));

        map.put("Extract Interface", List.of(
                "Identify the public API of {target} to be exposed via interface",
                "Create the new interface with selected method signatures",
                "Update {target} to implement the new interface",
                "Replace direct references to {target} with interface type",
                "Update dependency injection to use interface-based wiring",
                "Update factory or provider patterns if needed"
        ));

        map.put("Extract Service", List.of(
                "Identify the set of related responsibilities to extract from {target}",
                "Design the new service class interface and API",
                "Create the new service class with extracted fields and methods",
                "Update {target} to delegate to the new service",
                "Configure dependency injection for the new service",
                "Update callers to use the new service directly where appropriate"
        ));

        map.put("Split Class", List.of(
                "Identify the distinct responsibilities in {target}",
                "Design the extracted classes and their interfaces",
                "Create new classes for each identified responsibility",
                "Move relevant fields and methods to each new class",
                "Update {target} to delegate or compose with extracted classes",
                "Configure dependency injection for all new classes",
                "Update all callers to use the appropriate class for each responsibility"
        ));

        map.put("Merge Classes", List.of(
                "Identify overlapping and complementary functionality in both classes",
                "Resolve any naming conflicts between fields and methods",
                "Consolidate the source class into the target class",
                "Update all references to the source class to use the target class",
                "Remove the source class file",
                "Update dependency injection wiring for the merged class"
        ));

        map.put("Delete Dead Code", List.of(
                "Verify {target} is truly unreferenced (no reflection, no future use)",
                "Remove the dead code declarations",
                "Remove any related imports that are no longer needed",
                "Update any comments or documentation referencing the removed code",
                "Remove any test code that only tested the dead code"
        ));

        return map;
    }

    private static Map<String, String> buildEffortEstimates() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("Rename Class", "Low");
        map.put("Rename Method", "Low");
        map.put("Move Class", "Medium");
        map.put("Move Package", "High");
        map.put("Extract Interface", "Medium");
        map.put("Extract Service", "Medium");
        map.put("Split Class", "High");
        map.put("Merge Classes", "High");
        map.put("Delete Dead Code", "Low");
        return map;
    }
}