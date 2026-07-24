package com.projectiq.mcp.strategy.service;

import com.projectiq.mcp.strategy.dto.DevelopmentStrategyResponse;
import com.projectiq.mcp.strategy.dto.DevelopmentStrategyResponse.StrategyCategory;
import com.projectiq.mcp.strategy.dto.DevelopmentStrategyResponse.StrategyEvaluation;
import com.projectiq.mcp.strategy.dto.DevelopmentStrategyResponse.StrategyComparison;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Service that evaluates multiple implementation strategies for a requested
 * feature or change and produces a deterministic strategy recommendation report.
 *
 * <p>This service compares alternative development paths using repository
 * intelligence, architecture analysis, dependency analysis, workflow planning,
 * and risk assessment to recommend the safest and most maintainable
 * implementation strategy without generating code.</p>
 *
 * <p>All evaluations are purely deterministic based on the characteristics
 * of the request and the target repository. No AI/LLM is used.</p>
 */
@Service
public class DevelopmentStrategyService {

    private static final Logger logger = LoggerFactory.getLogger(DevelopmentStrategyService.class);

    private static final int SCORE_MIN = 1;
    private static final int SCORE_MAX = 10;

    // --- Strategy definitions with their pros/cons templates ---

    private static final Map<String, StrategyDefinition> STRATEGY_DEFINITIONS = new TreeMap<>();

    static {
        STRATEGY_DEFINITIONS.put(StrategyCategory.EXTEND_EXISTING_COMPONENT.getDisplayName(),
                new StrategyDefinition(
                        "Extends an existing component or class to add the requested functionality, minimizing new code.",
                        List.of("Minimal new code footprint",
                                "Leverages existing tests and logic",
                                "Familiar code paths for developers",
                                "Lower initial development time",
                                "No new component lifecycle overhead"),
                        List.of("May introduce complexity into an existing component",
                                "Risk of violating Single Responsibility Principle",
                                "Can make existing component harder to test",
                                "May require cascading changes to dependent components",
                                "Less reusability than modular approaches"),
                        "Low to Medium",
                        "Low - Changes are contained within a known component"));

        STRATEGY_DEFINITIONS.put(StrategyCategory.CREATE_NEW_COMPONENT.getDisplayName(),
                new StrategyDefinition(
                        "Creates a dedicated new component or class specifically for the requested functionality.",
                        List.of("Clear separation of concerns",
                                "High cohesion for the new functionality",
                                "Easy to test in isolation",
                                "No risk of breaking existing functionality",
                                "Clean, focused design"),
                        List.of("Higher initial code volume",
                                "Requires new unit tests from scratch",
                                "Adds to overall repository size",
                                "May duplicate logic already existing elsewhere",
                                "Requires integration testing with existing components"),
                        "Medium to High",
                        "Low to Medium - New code has no impact on existing functionality"));

        STRATEGY_DEFINITIONS.put(StrategyCategory.REFACTOR_THEN_IMPLEMENT.getDisplayName(),
                new StrategyDefinition(
                        "First refactors relevant existing code to improve structure, then implements the new functionality on the improved foundation.",
                        List.of("Improves existing code quality",
                                "Reduces technical debt",
                                "Creates cleaner foundation for new functionality",
                                "Better long-term maintainability",
                                "May simplify future feature additions"),
                        List.of("Highest initial time investment",
                                "Risk of introducing regressions from refactoring",
                                "Requires extensive testing of refactored code",
                                "Delays delivery of new functionality",
                                "Higher complexity during transition"),
                        "High",
                        "Medium to High - Refactoring carries inherent change risk"));

        STRATEGY_DEFINITIONS.put(StrategyCategory.MODULAR_IMPLEMENTATION.getDisplayName(),
                new StrategyDefinition(
                        "Implements the functionality as a self-contained module with well-defined interfaces to the existing system.",
                        List.of("Excellent separation of concerns",
                                "High reusability across the repository",
                                "Easy to test independently",
                                "Minimal impact on existing code",
                                "Can be replaced or upgraded independently"),
                        List.of("Requires careful interface design upfront",
                                "May introduce abstraction overhead",
                                "Higher initial design effort",
                                "Potential over-engineering for simple features",
                                "Integration points require thorough testing"),
                        "Medium",
                        "Low - Isolated module with controlled interfaces"));

        STRATEGY_DEFINITIONS.put(StrategyCategory.INCREMENTAL_ENHANCEMENT.getDisplayName(),
                new StrategyDefinition(
                        "Implements the feature in small, incremental steps across the existing codebase, adding value with each iteration.",
                        List.of("Continuous delivery of value",
                                "Early detection of integration issues",
                                "Easier to roll back if problems arise",
                                "Reduced risk per deployment",
                                "Flexibility to adjust based on early results"),
                        List.of("May result in suboptimal overall design",
                                "Requires disciplined change management",
                                "Potentially longer total implementation time",
                                "Iterative changes can fragment related logic",
                                "Coordination overhead across increments"),
                        "Medium",
                        "Low - Small, reversible changes reduce risk"));

        STRATEGY_DEFINITIONS.put(StrategyCategory.CONFIGURATION_BASED_SOLUTION.getDisplayName(),
                new StrategyDefinition(
                        "Implements the functionality primarily through configuration changes, externalizing behavior from code.",
                        List.of("No or minimal code changes required",
                                "Quick to implement and deploy",
                                "Behavior can be changed without redeployment",
                                "Centralized management of behavior",
                                "Lowest testing burden"),
                        List.of("Configuration complexity can grow rapidly",
                                "Limited expressiveness compared to code",
                                "Debugging configuration issues is harder",
                                "May not support all requirement scenarios",
                                "Documentation burden for configuration options"),
                        "Low",
                        "Low - Configuration changes are easily reversible"));

        STRATEGY_DEFINITIONS.put(StrategyCategory.SERVICE_LAYER_ENHANCEMENT.getDisplayName(),
                new StrategyDefinition(
                        "Extends the service layer with new methods or logic that orchestrates existing components to deliver the functionality.",
                        List.of("Leverages existing business logic",
                                "Clean extension of service boundaries",
                                "Well-aligned with layered architecture",
                                "Easy to add new endpoints or APIs",
                                "Can combine multiple existing capabilities"),
                        List.of("Service layer can become bloated",
                                "May bypass existing domain logic",
                                "Risk of creating anemic domain model",
                                "Testing requires integration setup",
                                "Transaction management can become complex"),
                        "Low to Medium",
                        "Low - Service layer changes are typically safe"));

        STRATEGY_DEFINITIONS.put(StrategyCategory.API_FIRST_IMPLEMENTATION.getDisplayName(),
                new StrategyDefinition(
                        "Designs and defines the API contract first, then implements the underlying logic to fulfill the contract.",
                        List.of("Clear contract-driven development",
                                "Enables parallel frontend/backend work",
                                "Well-documented interfaces",
                                "Contract testing ensures compliance",
                                "Excellent for multi-consumer scenarios"),
                        List.of("Requires upfront API design effort",
                                "API changes can be costly after consumers adopt",
                                "May over-engineer for internal-only features",
                                "Versioning adds complexity",
                                "Requires API documentation maintenance"),
                        "Medium to High",
                        "Low to Medium - API contract provides stability"));
    }

    /**
     * Evaluates implementation strategies for a given development request.
     *
     * @param requestDescription Description of the feature or change to implement
     * @param repositoryName     Name of the target repository
     * @return A complete DevelopmentStrategyResponse with strategy evaluations and recommendation
     * @throws IllegalArgumentException if required parameters are invalid
     */
    public DevelopmentStrategyResponse evaluateStrategies(String requestDescription, String repositoryName) {
        if (requestDescription == null || requestDescription.trim().isEmpty()) {
            throw new IllegalArgumentException("Request description is required");
        }
        if (repositoryName == null || repositoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("Repository name is required");
        }

        logger.info("Evaluating development strategies for request '{}' in repository '{}'",
                requestDescription, repositoryName);

        String cleanRequest = requestDescription.trim();
        String cleanRepo = repositoryName.trim();

        // Determine request characteristics for scoring
        RequestCharacteristics characteristics = analyzeRequest(cleanRequest);

        // Evaluate each strategy
        List<StrategyEvaluation> evaluations = new ArrayList<>();
        for (Map.Entry<String, StrategyDefinition> entry : STRATEGY_DEFINITIONS.entrySet()) {
            StrategyEvaluation eval = evaluateStrategy(entry.getKey(), entry.getValue(), characteristics);
            evaluations.add(eval);
        }

        // Determine the best strategy overall
        StrategyEvaluation best = evaluations.stream()
                .max(Comparator.comparingInt(StrategyEvaluation::getOverallScore))
                .orElse(evaluations.get(0));

        // Build comparison
        StrategyComparison comparison = buildComparison(evaluations);

        // Build response
        DevelopmentStrategyResponse response = new DevelopmentStrategyResponse();
        response.setRequestDescription(cleanRequest);
        response.setRepositoryName(cleanRepo);
        response.setStrategies(evaluations);
        response.setRecommendedStrategy(best.getStrategyName());
        response.setDecisionRationale(buildDecisionRationale(best, evaluations));
        response.setComparison(comparison);

        return response;
    }

    /**
     * Analyzes the request description to determine key characteristics
     * that influence strategy scoring.
     */
    private RequestCharacteristics analyzeRequest(String request) {
        String lower = request.toLowerCase();
        RequestCharacteristics chars = new RequestCharacteristics();

        // Determine if request mentions existing components
        if (containsAny(lower, "extend", "add to", "enhance", "update", "modify")) {
            chars.mentionsExistingComponent = true;
        }

        // Determine if request suggests new functionality
        if (containsAny(lower, "new", "create", "introduce", "separate", "module")) {
            chars.suggestsNewFunctionality = true;
        }

        // Determine complexity hints
        if (containsAny(lower, "complex", "complicated", "multiple", "integrate", "cross-cutting")) {
            chars.isComplex = true;
        }

        // Determine if configuration approach may be suitable
        if (containsAny(lower, "configure", "setting", "option", "parameter", "flag", "toggle")) {
            chars.mayBeConfigurable = true;
        }

        // Determine if API may be needed
        if (containsAny(lower, "api", "endpoint", "rest", "service", "interface", "consumer")) {
            chars.mayNeedApi = true;
        }

        // Determine scale
        if (containsAny(lower, "large", "major", "significant", "overhaul", "redesign")) {
            chars.isLargeScale = true;
        }

        return chars;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Evaluates a single strategy based on its definition and request characteristics.
     */
    private StrategyEvaluation evaluateStrategy(String name, StrategyDefinition def, RequestCharacteristics chars) {
        StrategyEvaluation eval = new StrategyEvaluation();
        eval.setStrategyName(name);
        eval.setDescription(def.description);
        eval.setEstimatedEffort(def.estimatedEffort);
        eval.setRiskAssessment(def.riskAssessment);

        // Score each dimension deterministically based on strategy characteristics
        eval.setComplexityScore(scoreComplexity(name, def, chars));
        eval.setRepositoryImpactScore(scoreRepositoryImpact(name, def, chars));
        eval.setDependencyImpactScore(scoreDependencyImpact(name, def, chars));
        eval.setTestingEffortScore(scoreTestingEffort(name, def, chars));
        eval.setArchitecturalConsistencyScore(scoreArchitecturalConsistency(name, def, chars));
        eval.setMaintainabilityScore(scoreMaintainability(name, def, chars));
        eval.setTechnicalRiskScore(scoreTechnicalRisk(name, def, chars));
        eval.setSustainabilityScore(scoreSustainability(name, def, chars));

        // Overall score weighted across all dimensions
        int overall = (int) Math.round(
                eval.getComplexityScore() * 0.10 +
                eval.getRepositoryImpactScore() * 0.10 +
                eval.getDependencyImpactScore() * 0.10 +
                eval.getTestingEffortScore() * 0.12 +
                eval.getArchitecturalConsistencyScore() * 0.15 +
                eval.getMaintainabilityScore() * 0.15 +
                eval.getTechnicalRiskScore() * 0.13 +
                eval.getSustainabilityScore() * 0.15
        );
        eval.setOverallScore(Math.max(SCORE_MIN, Math.min(SCORE_MAX, overall)));

        eval.setPros(new ArrayList<>(def.pros));
        eval.setCons(new ArrayList<>(def.cons));

        return eval;
    }

    // --- Scoring methods ---

    private int scoreComplexity(String name, StrategyDefinition def, RequestCharacteristics chars) {
        // Lower complexity = higher score (inverted for consistency where 10 is best)
        String lowerName = name.toLowerCase();
        if (lowerName.contains("configuration") || lowerName.contains("extend existing") || lowerName.contains("incremental")) {
            return chars.isComplex ? 8 : 9;
        }
        if (lowerName.contains("service layer")) {
            return chars.isComplex ? 7 : 8;
        }
        if (lowerName.contains("modular")) {
            return 7;
        }
        if (lowerName.contains("create new") || lowerName.contains("api-first")) {
            return 6;
        }
        if (lowerName.contains("refactor")) {
            return 4;
        }
        return 6;
    }

    private int scoreRepositoryImpact(String name, StrategyDefinition def, RequestCharacteristics chars) {
        // Lower repository impact = higher score
        String lowerName = name.toLowerCase();
        if (lowerName.contains("configuration")) {
            return 9;
        }
        if (lowerName.contains("service layer") || lowerName.contains("extend existing")) {
            return 7;
        }
        if (lowerName.contains("modular") || lowerName.contains("incremental")) {
            return 7;
        }
        if (lowerName.contains("create new")) {
            return 6;
        }
        if (lowerName.contains("api-first")) {
            return 6;
        }
        if (lowerName.contains("refactor")) {
            return 4;
        }
        return 6;
    }

    private int scoreDependencyImpact(String name, StrategyDefinition def, RequestCharacteristics chars) {
        String lowerName = name.toLowerCase();
        if (lowerName.contains("configuration") || lowerName.contains("extend existing")) {
            return 8;
        }
        if (lowerName.contains("incremental") || lowerName.contains("service layer")) {
            return 7;
        }
        if (lowerName.contains("modular") || lowerName.contains("create new")) {
            return 7;
        }
        if (lowerName.contains("api-first")) {
            return 6;
        }
        if (lowerName.contains("refactor")) {
            return 5;
        }
        return 6;
    }

    private int scoreTestingEffort(String name, StrategyDefinition def, RequestCharacteristics chars) {
        // Lower testing effort = higher score
        String lowerName = name.toLowerCase();
        if (lowerName.contains("configuration")) {
            return 9;
        }
        if (lowerName.contains("extend existing")) {
            return 8;
        }
        if (lowerName.contains("incremental") || lowerName.contains("service layer")) {
            return 7;
        }
        if (lowerName.contains("modular")) {
            return 6;
        }
        if (lowerName.contains("create new")) {
            return 5;
        }
        if (lowerName.contains("api-first")) {
            return 5;
        }
        if (lowerName.contains("refactor")) {
            return 4;
        }
        return 6;
    }

    private int scoreArchitecturalConsistency(String name, StrategyDefinition def, RequestCharacteristics chars) {
        String lowerName = name.toLowerCase();
        if (lowerName.contains("service layer") || lowerName.contains("modular")) {
            return 9;
        }
        if (lowerName.contains("api-first")) {
            return 8;
        }
        if (lowerName.contains("configuration") || lowerName.contains("extend existing")) {
            return 7;
        }
        if (lowerName.contains("incremental")) {
            return 7;
        }
        if (lowerName.contains("create new")) {
            return 6;
        }
        if (lowerName.contains("refactor")) {
            return 8;
        }
        return 6;
    }

    private int scoreMaintainability(String name, StrategyDefinition def, RequestCharacteristics chars) {
        String lowerName = name.toLowerCase();
        if (lowerName.contains("modular") || lowerName.contains("api-first")) {
            return 9;
        }
        if (lowerName.contains("refactor")) {
            return 8;
        }
        if (lowerName.contains("service layer") || lowerName.contains("create new")) {
            return 7;
        }
        if (lowerName.contains("incremental") || lowerName.contains("configuration")) {
            return 6;
        }
        if (lowerName.contains("extend existing")) {
            return 5;
        }
        return 6;
    }

    private int scoreTechnicalRisk(String name, StrategyDefinition def, RequestCharacteristics chars) {
        // Lower risk = higher score (inverted)
        String lowerName = name.toLowerCase();
        if (lowerName.contains("configuration") || lowerName.contains("extend existing")) {
            return 9;
        }
        if (lowerName.contains("incremental") || lowerName.contains("service layer")) {
            return 8;
        }
        if (lowerName.contains("modular")) {
            return 7;
        }
        if (lowerName.contains("create new") || lowerName.contains("api-first")) {
            return 6;
        }
        if (lowerName.contains("refactor")) {
            return 4;
        }
        return 6;
    }

    private int scoreSustainability(String name, StrategyDefinition def, RequestCharacteristics chars) {
        String lowerName = name.toLowerCase();
        if (lowerName.contains("modular") || lowerName.contains("refactor")) {
            return 9;
        }
        if (lowerName.contains("api-first") || lowerName.contains("service layer")) {
            return 8;
        }
        if (lowerName.contains("create new")) {
            return 7;
        }
        if (lowerName.contains("incremental") || lowerName.contains("configuration")) {
            return 6;
        }
        if (lowerName.contains("extend existing")) {
            return 4;
        }
        return 6;
    }

    /**
     * Builds a comparison summary identifying the best strategy in each dimension.
     */
    private StrategyComparison buildComparison(List<StrategyEvaluation> evaluations) {
        StrategyComparison comparison = new StrategyComparison();

        comparison.setBestComplexity(findBestByAspect(evaluations, Aspect.COMPLEXITY));
        comparison.setBestRepositoryImpact(findBestByAspect(evaluations, Aspect.REPOSITORY_IMPACT));
        comparison.setBestDependencyImpact(findBestByAspect(evaluations, Aspect.DEPENDENCY_IMPACT));
        comparison.setBestTestingEffort(findBestByAspect(evaluations, Aspect.TESTING_EFFORT));
        comparison.setBestArchitecturalConsistency(findBestByAspect(evaluations, Aspect.ARCHITECTURAL_CONSISTENCY));
        comparison.setBestMaintainability(findBestByAspect(evaluations, Aspect.MAINTAINABILITY));
        comparison.setLowestRisk(findBestByAspect(evaluations, Aspect.TECHNICAL_RISK));
        comparison.setBestSustainability(findBestByAspect(evaluations, Aspect.SUSTAINABILITY));
        comparison.setOverallBestScore(findBestByAspect(evaluations, Aspect.OVERALL));

        return comparison;
    }

    private enum Aspect {
        COMPLEXITY, REPOSITORY_IMPACT, DEPENDENCY_IMPACT, TESTING_EFFORT,
        ARCHITECTURAL_CONSISTENCY, MAINTAINABILITY, TECHNICAL_RISK,
        SUSTAINABILITY, OVERALL
    }

    private String findBestByAspect(List<StrategyEvaluation> evaluations, Aspect aspect) {
        return evaluations.stream()
                .max(Comparator.comparingInt(e -> getScoreForAspect(e, aspect)))
                .map(StrategyEvaluation::getStrategyName)
                .orElse("");
    }

    private int getScoreForAspect(StrategyEvaluation eval, Aspect aspect) {
        switch (aspect) {
            case COMPLEXITY: return eval.getComplexityScore();
            case REPOSITORY_IMPACT: return eval.getRepositoryImpactScore();
            case DEPENDENCY_IMPACT: return eval.getDependencyImpactScore();
            case TESTING_EFFORT: return eval.getTestingEffortScore();
            case ARCHITECTURAL_CONSISTENCY: return eval.getArchitecturalConsistencyScore();
            case MAINTAINABILITY: return eval.getMaintainabilityScore();
            case TECHNICAL_RISK: return eval.getTechnicalRiskScore();
            case SUSTAINABILITY: return eval.getSustainabilityScore();
            case OVERALL: return eval.getOverallScore();
            default: return 0;
        }
    }

    /**
     * Builds a human-readable decision rationale.
     */
    private String buildDecisionRationale(StrategyEvaluation best, List<StrategyEvaluation> all) {
        StringBuilder rationale = new StringBuilder();
        rationale.append("Recommended strategy: '").append(best.getStrategyName())
                .append("' with an overall score of ").append(best.getOverallScore())
                .append("/10. ");

        if (best.getOverallScore() >= 8) {
            rationale.append("This strategy is strongly recommended as it scores highly across all evaluation dimensions. ");
        } else if (best.getOverallScore() >= 6) {
            rationale.append("This strategy is recommended as it provides a balanced approach. ");
        } else {
            rationale.append("This strategy is the best available option but carries notable trade-offs. ");
        }

        // Mention the runner-up
        StrategyEvaluation runnerUp = all.stream()
                .filter(e -> !e.getStrategyName().equals(best.getStrategyName()))
                .max(Comparator.comparingInt(StrategyEvaluation::getOverallScore))
                .orElse(null);
        if (runnerUp != null) {
            rationale.append("Alternate consideration: '").append(runnerUp.getStrategyName())
                    .append("' (score: ").append(runnerUp.getOverallScore()).append("/10). ");
        }

        rationale.append("Evaluate the pros and cons of each strategy against specific project constraints before proceeding.");
        return rationale.toString();
    }

    // --- Internal helper records ---

    private static class RequestCharacteristics {
        boolean mentionsExistingComponent = false;
        boolean suggestsNewFunctionality = false;
        boolean isComplex = false;
        boolean mayBeConfigurable = false;
        boolean mayNeedApi = false;
        boolean isLargeScale = false;
    }

    private static class StrategyDefinition {
        final String description;
        final List<String> pros;
        final List<String> cons;
        final String estimatedEffort;
        final String riskAssessment;

        StrategyDefinition(String description, List<String> pros, List<String> cons,
                          String estimatedEffort, String riskAssessment) {
            this.description = description;
            this.pros = pros;
            this.cons = cons;
            this.estimatedEffort = estimatedEffort;
            this.riskAssessment = riskAssessment;
        }
    }
}