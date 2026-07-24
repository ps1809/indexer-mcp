package com.projectiq.mcp.analysis.service;

import com.projectiq.mcp.analysis.dto.ArchitectureInsightsResponse;
import com.projectiq.mcp.analysis.dto.CodeChangeAnalysisResponse;
import com.projectiq.mcp.analysis.dto.DependencyChangePredictionResponse;
import com.projectiq.mcp.analysis.dto.RepositoryConventionResponse;
import com.projectiq.mcp.analysis.dto.RepositoryEvolutionAnalysisResponse;
import com.projectiq.mcp.analysis.dto.RepositoryEvolutionAnalysisResponse.ArchitectureEvolutionAnalysis;
import com.projectiq.mcp.analysis.dto.RepositoryEvolutionAnalysisResponse.ConventionConsistencyAnalysis;
import com.projectiq.mcp.analysis.dto.RepositoryEvolutionAnalysisResponse.DependencyEvolutionAnalysis;
import com.projectiq.mcp.analysis.dto.RepositoryEvolutionAnalysisResponse.MaintainabilityAnalysis;
import com.projectiq.mcp.analysis.dto.RepositoryEvolutionAnalysisResponse.ModuleExpansionAnalysis;
import com.projectiq.mcp.analysis.dto.RepositoryEvolutionAnalysisResponse.PackageGrowthAnalysis;
import com.projectiq.mcp.analysis.dto.RepositoryEvolutionAnalysisResponse.ScalabilityReadinessAnalysis;
import com.projectiq.mcp.analysis.dto.RepositoryEvolutionAnalysisResponse.TechnicalDebtIndicatorsAnalysis;
import com.projectiq.mcp.analysis.dto.RepositoryHealthResponse;
import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.dto.RepositorySummaryRequest;
import com.projectiq.mcp.client.dto.RepositorySummaryResponse;
import com.projectiq.mcp.pipeline.service.IntelligentContextPipelineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Service that performs deterministic Intelligent Repository Evolution Analysis.
 * Evaluates how proposed features or architectural changes will affect the
 * long-term evolution of the repository. Using indexed repository intelligence,
 * dependency graphs, architecture insights, historical workflow data, and
 * existing development patterns, the analyzer produces deterministic evolution
 * reports to help AI coding agents make decisions that preserve repository
 * maintainability and architectural consistency.
 *
 * <p>This service reuses {@link CodeChangeAnalysisService},
 * {@link DependencyChangePredictionService}, {@link RefactoringImpactSimulationService},
 * {@link ArchitectureInsightsService}, {@link RepositoryHealthService},
 * {@link RepositoryConventionAnalyzerService}, and
 * {@link IntelligentContextPipelineService} to gather the necessary analysis data.
 * All outputs are deterministic, stable, and free of duplicate entries.</p>
 *
 * <p>This service NEVER generates code, modifies the repository, performs
 * git operations, or uses any AI/LLM reasoning.</p>
 */
@Service
public class RepositoryEvolutionAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryEvolutionAnalysisService.class);

    private final CodeChangeAnalysisService codeChangeAnalysisService;
    private final DependencyChangePredictionService dependencyChangePredictionService;
    private final RefactoringImpactSimulationService refactoringImpactSimulationService;
    private final ArchitectureInsightsService architectureInsightsService;
    private final RepositoryHealthService repositoryHealthService;
    private final RepositoryConventionAnalyzerService repositoryConventionAnalyzerService;
    private final IntelligentContextPipelineService intelligentContextPipelineService;
    private final IndexerRestClient indexerRestClient;

    // --- Evolution analysis patterns ---

    private static final Pattern NEW_PACKAGE_PATTERN = Pattern.compile(
            "\\b(package|module|namespace|subsystem)\\b", Pattern.CASE_INSENSITIVE
    );

    private static final Pattern NEW_CLASS_PATTERN = Pattern.compile(
            "\\b(class|interface|enum|record|component|service|controller|repository|entity|dto)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern NEW_DEPENDENCY_PATTERN = Pattern.compile(
            "\\b(dependency|library|framework|artifact|maven|gradle|npm|pip|import|require)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern ARCHITECTURE_CHANGE_PATTERN = Pattern.compile(
            "\\b(architecture|layer|tier|pattern|restructure|refactor|migrate|reorganize)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SCALABILITY_PATTERN = Pattern.compile(
            "\\b(scalab|perform|concurr|parallel|distribut|cache|async|reactive|event-driven|load)\\b",
            Pattern.CASE_INSENSITIVE
    );

    public RepositoryEvolutionAnalysisService(
            CodeChangeAnalysisService codeChangeAnalysisService,
            DependencyChangePredictionService dependencyChangePredictionService,
            RefactoringImpactSimulationService refactoringImpactSimulationService,
            ArchitectureInsightsService architectureInsightsService,
            RepositoryHealthService repositoryHealthService,
            RepositoryConventionAnalyzerService repositoryConventionAnalyzerService,
            IntelligentContextPipelineService intelligentContextPipelineService,
            IndexerRestClient indexerRestClient) {
        this.codeChangeAnalysisService = codeChangeAnalysisService;
        this.dependencyChangePredictionService = dependencyChangePredictionService;
        this.refactoringImpactSimulationService = refactoringImpactSimulationService;
        this.architectureInsightsService = architectureInsightsService;
        this.repositoryHealthService = repositoryHealthService;
        this.repositoryConventionAnalyzerService = repositoryConventionAnalyzerService;
        this.intelligentContextPipelineService = intelligentContextPipelineService;
        this.indexerRestClient = indexerRestClient;
    }

    /**
     * Analyzes the long-term repository evolution impact of a proposed change.
     *
     * @param repositoryName the repository name to analyze
     * @param branch         the git branch (optional, defaults to "main")
     * @param proposedChange a description of the proposed enhancement or change
     * @return a {@link RepositoryEvolutionAnalysisResponse} containing the evolution report
     */
    public RepositoryEvolutionAnalysisResponse analyzeEvolution(
            String repositoryName, String branch, String proposedChange) {
        logger.info("Analyzing repository evolution for: {} branch: {} change: {}",
                repositoryName, branch, proposedChange);

        RepositoryEvolutionAnalysisResponse response = new RepositoryEvolutionAnalysisResponse();
        response.setRepositoryName(repositoryName);
        response.setBranch(branch != null && !branch.trim().isEmpty() ? branch.trim() : "main");
        response.setProposedChange(proposedChange);

        // Step 1: Retrieve repository summary
        RepositorySummaryResponse summary = retrieveRepositorySummary(repositoryName, response.getBranch());

        if (summary == null) {
            response.setProposedChangeSummary("Repository data not available. Unable to analyze evolution.");
            response.setRepositoryEvolutionScore(0);
            return response;
        }

        // Step 2: Build proposed change summary
        String changeSummary = buildChangeSummary(proposedChange, summary);
        response.setProposedChangeSummary(changeSummary);

        // Step 3: Analyze architecture evolution
        ArchitectureEvolutionAnalysis archEvolution = analyzeArchitectureEvolution(
                repositoryName, response.getBranch(), proposedChange, summary);
        response.setArchitectureEvolution(archEvolution);
        response.setArchitecturalImpact(archEvolution.getSummary());

        // Step 4: Analyze package growth
        PackageGrowthAnalysis pkgGrowth = analyzePackageGrowth(proposedChange, summary);
        response.setPackageGrowth(pkgGrowth);

        // Step 5: Analyze module expansion
        ModuleExpansionAnalysis moduleExpansion = analyzeModuleExpansion(proposedChange, summary);
        response.setModuleExpansion(moduleExpansion);

        // Step 6: Analyze dependency evolution
        DependencyEvolutionAnalysis depEvolution = analyzeDependencyEvolution(
                repositoryName, response.getBranch(), proposedChange, summary);
        response.setDependencyEvolution(depEvolution);

        // Step 7: Analyze convention consistency
        ConventionConsistencyAnalysis conventionAnalysis = analyzeConventionConsistency(
                repositoryName, response.getBranch());
        response.setConventionConsistency(conventionAnalysis);
        response.setConventionCompliance(conventionAnalysis.getSummary());

        // Step 8: Analyze maintainability
        MaintainabilityAnalysis maintAnalysis = analyzeMaintainability(
                repositoryName, response.getBranch(), proposedChange, summary);
        response.setMaintainability(maintAnalysis);
        response.setMaintainabilityAssessment(maintAnalysis.getSummary());

        // Step 9: Analyze technical debt indicators
        TechnicalDebtIndicatorsAnalysis debtAnalysis = analyzeTechnicalDebtIndicators(
                repositoryName, response.getBranch(), proposedChange, summary);
        response.setTechnicalDebtAnalysis(debtAnalysis);
        response.setTechnicalDebtIndicators(debtAnalysis.getDebtIndicators());

        // Step 10: Analyze scalability readiness
        ScalabilityReadinessAnalysis scalabilityAnalysis = analyzeScalabilityReadiness(
                proposedChange, summary);
        response.setScalabilityReadiness(scalabilityAnalysis);
        response.setScalabilityConsiderations(scalabilityAnalysis.getSummary());

        // Step 11: Identify long-term risks
        List<String> risks = identifyLongTermRisks(
                archEvolution, depEvolution, debtAnalysis, scalabilityAnalysis);
        response.setLongTermRisks(risks);

        // Step 12: Generate recommended practices
        List<String> recommendations = generateRecommendedPractices(
                archEvolution, conventionAnalysis, maintAnalysis, debtAnalysis, scalabilityAnalysis);
        response.setRecommendedRepositoryPractices(recommendations);

        // Step 13: Calculate repository evolution score
        int evolutionScore = calculateEvolutionScore(
                archEvolution, pkgGrowth, moduleExpansion, depEvolution,
                conventionAnalysis, maintAnalysis, debtAnalysis, scalabilityAnalysis);
        response.setRepositoryEvolutionScore(evolutionScore);

        logger.info("Repository evolution analysis complete: score={}, risks={}, recommendations={}",
                evolutionScore, risks.size(), recommendations.size());

        return response;
    }

    /**
     * Retrieves the repository summary from the Indexer.
     */
    RepositorySummaryResponse retrieveRepositorySummary(String repositoryName, String branch) {
        try {
            RepositorySummaryRequest request = new RepositorySummaryRequest();
            request.setRepositoryName(repositoryName);
            request.setBranch(branch);
            return indexerRestClient.getRepositorySummary(request);
        } catch (Exception e) {
            logger.warn("Failed to retrieve repository summary for {}: {}", repositoryName, e.getMessage());
            return null;
        }
    }

    /**
     * Builds a summary of the proposed change in context of the repository.
     */
    String buildChangeSummary(String proposedChange, RepositorySummaryResponse summary) {
        StringBuilder sb = new StringBuilder();
        sb.append("Proposed change '").append(proposedChange).append("' ");
        if (summary != null) {
            sb.append("applied to repository '").append(summary.getRepositoryName())
                    .append("' containing ").append(summary.getPackageCount())
                    .append(" packages and ").append(summary.getClassCount())
                    .append(" classes.");
        } else {
            sb.append("applied to the repository.");
        }
        return sb.toString();
    }

    /**
     * Analyzes architecture evolution impact.
     */
    ArchitectureEvolutionAnalysis analyzeArchitectureEvolution(
            String repositoryName, String branch, String proposedChange,
            RepositorySummaryResponse summary) {
        ArchitectureEvolutionAnalysis analysis = new ArchitectureEvolutionAnalysis();

        // Get architecture insights
        ArchitectureInsightsResponse archInsights = null;
        try {
            archInsights = architectureInsightsService.analyzeArchitecture(repositoryName, branch);
        } catch (Exception e) {
            logger.warn("Failed to get architecture insights: {}", e.getMessage());
        }

        List<String> drifts = new ArrayList<>();
        int score = 50; // neutral baseline

        if (archInsights != null) {
            String style = archInsights.getArchitecturalStyle();
            List<String> layers = archInsights.getDetectedLayers();
            List<String> concerns = archInsights.getPotentialConcerns();

            // Check if proposed change affects architecture
            boolean affectsArchitecture = ARCHITECTURE_CHANGE_PATTERN.matcher(proposedChange).find();

            if (affectsArchitecture) {
                drifts.add("Proposed change may alter the existing '" + style + "' architectural style");
                score -= 15;
            }

            // Check for layer violations
            if (proposedChange.toLowerCase().contains("controller")
                    && proposedChange.toLowerCase().contains("repository")) {
                drifts.add("Direct controller-to-repository access may bypass service layer");
                score -= 10;
            }

            // Check for existing concerns
            if (concerns != null && !concerns.isEmpty()) {
                for (String concern : concerns) {
                    if (concern.toLowerCase().contains("layer") || concern.toLowerCase().contains("architecture")) {
                        drifts.add("Existing architectural concern: " + concern);
                        score -= 5;
                    }
                }
            }

            // Positive indicators
            if (layers != null && layers.size() >= 3) {
                score += 10; // Strong layered structure
            }

            if (style != null && !style.contains("Unknown") && !style.contains("Modular")) {
                score += 5; // Recognizable architecture
            }
        } else {
            drifts.add("Unable to retrieve architecture insights for drift detection");
            score -= 10;
        }

        // Check for new package introduction
        if (NEW_PACKAGE_PATTERN.matcher(proposedChange).find()) {
            drifts.add("New package/module introduction may affect package hierarchy");
            score -= 5;
        }

        analysis.setSummary(buildArchitectureSummary(score, drifts));
        analysis.setArchitecturalDrifts(drifts);
        analysis.setArchitectureScore(clampScore(score));

        return analysis;
    }

    /**
     * Analyzes package growth impact.
     */
    PackageGrowthAnalysis analyzePackageGrowth(
            String proposedChange, RepositorySummaryResponse summary) {
        PackageGrowthAnalysis analysis = new PackageGrowthAnalysis();

        int estimatedNewPackages = 0;
        int packageDensity = 0;

        // Estimate new packages from proposed change
        if (NEW_PACKAGE_PATTERN.matcher(proposedChange).find()) {
            estimatedNewPackages = 1;
            if (proposedChange.toLowerCase().contains("multiple")
                    || proposedChange.toLowerCase().contains("several")
                    || proposedChange.toLowerCase().contains("various")) {
                estimatedNewPackages = 3;
            }
        }

        // Calculate current package density (classes per package)
        if (summary != null && summary.getPackageCount() > 0) {
            packageDensity = (int) (summary.getClassCount() / summary.getPackageCount());
        }

        String summaryText;
        if (estimatedNewPackages > 0) {
            summaryText = "Proposed change may introduce approximately "
                    + estimatedNewPackages + " new package(s). "
                    + "Current package density is " + packageDensity + " classes per package.";
        } else {
            summaryText = "Proposed change is not expected to introduce new packages. "
                    + "Current package density is " + packageDensity + " classes per package.";
        }

        analysis.setSummary(summaryText);
        analysis.setEstimatedNewPackages(estimatedNewPackages);
        analysis.setEstimatedPackageDensity(packageDensity);

        return analysis;
    }

    /**
     * Analyzes module expansion impact.
     */
    ModuleExpansionAnalysis analyzeModuleExpansion(
            String proposedChange, RepositorySummaryResponse summary) {
        ModuleExpansionAnalysis analysis = new ModuleExpansionAnalysis();

        int estimatedNewClasses = 0;
        int cohesionScore = 50;

        // Estimate new classes from proposed change
        java.util.regex.Matcher classMatcher = NEW_CLASS_PATTERN.matcher(proposedChange);
        Set<String> classTypes = new LinkedHashSet<>();
        while (classMatcher.find()) {
            classTypes.add(classMatcher.group().toLowerCase());
        }

        if (!classTypes.isEmpty()) {
            estimatedNewClasses = classTypes.size();
            // Each unique class type mentioned suggests a new class
            if (proposedChange.toLowerCase().contains("multiple")
                    || proposedChange.toLowerCase().contains("several")) {
                estimatedNewClasses = Math.max(estimatedNewClasses, 3);
            }
        }

        // Calculate module cohesion based on package density
        if (summary != null && summary.getPackageCount() > 0) {
            int density = (int) (summary.getClassCount() / summary.getPackageCount());
            if (density <= 5) {
                cohesionScore = 80; // Good cohesion
            } else if (density <= 10) {
                cohesionScore = 60; // Moderate cohesion
            } else if (density <= 20) {
                cohesionScore = 40; // Low cohesion
            } else {
                cohesionScore = 20; // Poor cohesion
            }
        }

        String summaryText;
        if (estimatedNewClasses > 0) {
            summaryText = "Proposed change may introduce approximately "
                    + estimatedNewClasses + " new class(es). "
                    + "Module cohesion score is " + cohesionScore + "/100.";
        } else {
            summaryText = "Proposed change is not expected to introduce new classes. "
                    + "Module cohesion score is " + cohesionScore + "/100.";
        }

        analysis.setSummary(summaryText);
        analysis.setEstimatedNewClasses(estimatedNewClasses);
        analysis.setModuleCohesionScore(cohesionScore);

        return analysis;
    }

    /**
     * Analyzes dependency evolution impact.
     */
    DependencyEvolutionAnalysis analyzeDependencyEvolution(
            String repositoryName, String branch, String proposedChange,
            RepositorySummaryResponse summary) {
        DependencyEvolutionAnalysis analysis = new DependencyEvolutionAnalysis();

        int estimatedNewDependencies = 0;
        boolean circularDependencyRisk = false;

        // Estimate new dependencies from proposed change
        if (NEW_DEPENDENCY_PATTERN.matcher(proposedChange).find()) {
            estimatedNewDependencies = 1;
            if (proposedChange.toLowerCase().contains("multiple")
                    || proposedChange.toLowerCase().contains("several")
                    || proposedChange.toLowerCase().contains("integration")) {
                estimatedNewDependencies = 3;
            }
        }

        // Check for circular dependency risk
        if (proposedChange.toLowerCase().contains("circular")
                || proposedChange.toLowerCase().contains("bidirectional")) {
            circularDependencyRisk = true;
        }

        // Check if change introduces cross-layer dependencies
        if (proposedChange.toLowerCase().contains("controller")
                && proposedChange.toLowerCase().contains("repository")) {
            circularDependencyRisk = true;
        }

        String summaryText;
        if (estimatedNewDependencies > 0) {
            summaryText = "Proposed change may introduce approximately "
                    + estimatedNewDependencies + " new dependency(ies). "
                    + "Circular dependency risk: " + (circularDependencyRisk ? "Yes" : "No") + ".";
        } else {
            summaryText = "Proposed change is not expected to introduce new dependencies. "
                    + "Circular dependency risk: " + (circularDependencyRisk ? "Yes" : "No") + ".";
        }

        analysis.setSummary(summaryText);
        analysis.setEstimatedNewDependencies(estimatedNewDependencies);
        analysis.setCircularDependencyRisk(circularDependencyRisk);

        return analysis;
    }

    /**
     * Analyzes convention consistency impact.
     */
    ConventionConsistencyAnalysis analyzeConventionConsistency(
            String repositoryName, String branch) {
        ConventionConsistencyAnalysis analysis = new ConventionConsistencyAnalysis();

        List<String> deviations = new ArrayList<>();
        int score = 70; // default moderate score

        try {
            RepositoryConventionResponse conventionResponse =
                    repositoryConventionAnalyzerService.analyzeConventions(repositoryName, branch);

            if (conventionResponse != null) {
                List<String> observations = conventionResponse.getProjectSpecificObservations();
                if (observations != null && !observations.isEmpty()) {
                    for (String obs : observations) {
                        if (obs.toLowerCase().contains("inconsistency")
                                || obs.toLowerCase().contains("deviation")
                                || obs.toLowerCase().contains("violation")) {
                            deviations.add("Convention observation: " + obs);
                        }
                    }
                    score = Math.max(10, 70 - (observations.size() * 5));
                } else {
                    score = 90; // No observations
                }

                String confidence = conventionResponse.getConfidenceLevel();
                if (confidence != null) {
                    if (confidence.equalsIgnoreCase("HIGH")) {
                        score = Math.max(score, 85);
                    } else if (confidence.equalsIgnoreCase("MEDIUM")) {
                        score = Math.max(score, 60);
                    } else {
                        score = Math.min(score, 40);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to get convention analysis: {}", e.getMessage());
            deviations.add("Unable to retrieve convention analysis");
            score = 30;
        }

        String summaryText;
        if (deviations.isEmpty()) {
            summaryText = "Repository conventions are consistent. Convention score: " + score + "/100.";
        } else {
            summaryText = "Repository has " + deviations.size() + " convention deviation(s). "
                    + "Convention score: " + score + "/100.";
        }

        analysis.setSummary(summaryText);
        analysis.setConventionScore(score);
        analysis.setDeviations(deviations);

        return analysis;
    }

    /**
     * Analyzes maintainability impact.
     */
    MaintainabilityAnalysis analyzeMaintainability(
            String repositoryName, String branch, String proposedChange,
            RepositorySummaryResponse summary) {
        MaintainabilityAnalysis analysis = new MaintainabilityAnalysis();

        List<String> concerns = new ArrayList<>();
        int score = 60; // default

        // Check repository health
        try {
            RepositoryHealthResponse health =
                    repositoryHealthService.analyzeHealth(repositoryName, branch);
            if (health != null) {
                int healthScore = health.getHealthScore();
                score = healthScore;
                List<String> risks = health.getPotentialRisks();
                if (risks != null) {
                    for (String risk : risks) {
                        if (risk.toLowerCase().contains("complex")
                                || risk.toLowerCase().contains("maintain")) {
                            concerns.add("Existing maintainability concern: " + risk);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to get repository health: {}", e.getMessage());
        }

        // Analyze proposed change complexity
        if (proposedChange.length() > 200) {
            concerns.add("Proposed change description is verbose, indicating potential complexity");
            score -= 10;
        }

        if (NEW_PACKAGE_PATTERN.matcher(proposedChange).find()
                && NEW_CLASS_PATTERN.matcher(proposedChange).find()) {
            concerns.add("Proposed change introduces both new packages and classes, "
                    + "which may increase maintenance overhead");
            score -= 10;
        }

        // Check for architectural impact
        if (ARCHITECTURE_CHANGE_PATTERN.matcher(proposedChange).find()) {
            concerns.add("Architectural changes may require significant maintenance effort");
            score -= 10;
        }

        score = clampScore(score);

        String summaryText;
        if (concerns.isEmpty()) {
            summaryText = "Maintainability assessment: Good. Score: " + score + "/100.";
        } else {
            summaryText = "Maintainability assessment: " + concerns.size()
                    + " concern(s) identified. Score: " + score + "/100.";
        }

        analysis.setSummary(summaryText);
        analysis.setMaintainabilityScore(score);
        analysis.setComplexityConcerns(concerns);

        return analysis;
    }

    /**
     * Analyzes technical debt indicators.
     */
    TechnicalDebtIndicatorsAnalysis analyzeTechnicalDebtIndicators(
            String repositoryName, String branch, String proposedChange,
            RepositorySummaryResponse summary) {
        TechnicalDebtIndicatorsAnalysis analysis = new TechnicalDebtIndicatorsAnalysis();

        List<String> indicators = new ArrayList<>();
        int score = 80; // low debt = high score

        // Check for indicators in proposed change
        if (proposedChange.toLowerCase().contains("workaround")
                || proposedChange.toLowerCase().contains("hack")
                || proposedChange.toLowerCase().contains("temporary")
                || proposedChange.toLowerCase().contains("quick fix")) {
            indicators.add("Proposed change uses workaround patterns that may increase technical debt");
            score -= 20;
        }

        if (proposedChange.toLowerCase().contains("deprecated")
                || proposedChange.toLowerCase().contains("legacy")) {
            indicators.add("Proposed change interacts with deprecated or legacy components");
            score -= 15;
        }

        if (proposedChange.toLowerCase().contains("duplicate")
                || proposedChange.toLowerCase().contains("copy")) {
            indicators.add("Proposed change may introduce code duplication");
            score -= 15;
        }

        // Check repository health for debt indicators
        try {
            RepositoryHealthResponse health =
                    repositoryHealthService.analyzeHealth(repositoryName, branch);
            if (health != null) {
                List<String> risks = health.getPotentialRisks();
                if (risks != null) {
                    for (String risk : risks) {
                        if (risk.toLowerCase().contains("debt")
                                || risk.toLowerCase().contains("deprecated")
                                || risk.toLowerCase().contains("legacy")) {
                            indicators.add("Existing technical debt: " + risk);
                            score -= 10;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to get repository health for debt analysis: {}", e.getMessage());
        }

        // Large repositories may have higher debt
        if (summary != null && summary.getClassCount() > 500) {
            indicators.add("Large repository with " + summary.getClassCount()
                    + " classes may have accumulated technical debt");
            score -= 5;
        }

        score = clampScore(score);

        String summaryText;
        if (indicators.isEmpty()) {
            summaryText = "No significant technical debt indicators detected. Score: " + score + "/100.";
        } else {
            summaryText = indicators.size() + " technical debt indicator(s) identified. Score: " + score + "/100.";
        }

        analysis.setSummary(summaryText);
        analysis.setTechnicalDebtScore(score);
        analysis.setDebtIndicators(indicators);

        return analysis;
    }

    /**
     * Analyzes scalability readiness.
     */
    ScalabilityReadinessAnalysis analyzeScalabilityReadiness(
            String proposedChange, RepositorySummaryResponse summary) {
        ScalabilityReadinessAnalysis analysis = new ScalabilityReadinessAnalysis();

        List<String> concerns = new ArrayList<>();
        int score = 50; // neutral

        // Check if proposed change addresses scalability
        boolean addressesScalability = SCALABILITY_PATTERN.matcher(proposedChange).find();

        if (addressesScalability) {
            score += 20; // Positive: change considers scalability
        }

        // Check for scalability risks
        if (proposedChange.toLowerCase().contains("singleton")
                || proposedChange.toLowerCase().contains("static")) {
            concerns.add("Use of singleton/static patterns may limit scalability");
            score -= 10;
        }

        if (proposedChange.toLowerCase().contains("synchronized")
                || proposedChange.toLowerCase().contains("lock")
                || proposedChange.toLowerCase().contains("blocking")) {
            concerns.add("Blocking/synchronized operations may impact scalability");
            score -= 10;
        }

        if (proposedChange.toLowerCase().contains("database")
                && proposedChange.toLowerCase().contains("join")) {
            concerns.add("Complex database joins may impact horizontal scalability");
            score -= 5;
        }

        // Check repository size as scalability indicator
        if (summary != null) {
            if (summary.getPackageCount() > 50) {
                concerns.add("Large number of packages (" + summary.getPackageCount()
                        + ") may indicate scalability challenges");
                score -= 5;
            }
            if (summary.getClassCount() > 1000) {
                concerns.add("Large codebase with " + summary.getClassCount()
                        + " classes may require modularization for scalability");
                score -= 5;
            }
        }

        score = clampScore(score);

        String summaryText;
        if (concerns.isEmpty()) {
            if (addressesScalability) {
                summaryText = "Proposed change considers scalability. Readiness score: " + score + "/100.";
            } else {
                summaryText = "No scalability concerns detected. Readiness score: " + score + "/100.";
            }
        } else {
            summaryText = concerns.size() + " scalability concern(s) identified. Readiness score: " + score + "/100.";
        }

        analysis.setSummary(summaryText);
        analysis.setScalabilityScore(score);
        analysis.setScalabilityConcerns(concerns);

        return analysis;
    }

    /**
     * Identifies long-term risks based on all analysis categories.
     */
    List<String> identifyLongTermRisks(
            ArchitectureEvolutionAnalysis archEvolution,
            DependencyEvolutionAnalysis depEvolution,
            TechnicalDebtIndicatorsAnalysis debtAnalysis,
            ScalabilityReadinessAnalysis scalabilityAnalysis) {
        List<String> risks = new ArrayList<>();

        // Architecture risks
        if (archEvolution.getArchitectureScore() != null && archEvolution.getArchitectureScore() < 40) {
            risks.add("Architecture drift risk: Low architecture score ("
                    + archEvolution.getArchitectureScore() + "/100)");
        }
        if (archEvolution.getArchitecturalDrifts() != null && !archEvolution.getArchitecturalDrifts().isEmpty()) {
            risks.add("Architectural drift detected with "
                    + archEvolution.getArchitecturalDrifts().size() + " drift indicator(s)");
        }

        // Dependency risks
        if (Boolean.TRUE.equals(depEvolution.getCircularDependencyRisk())) {
            risks.add("Circular dependency risk may increase maintenance complexity over time");
        }
        if (depEvolution.getEstimatedNewDependencies() != null && depEvolution.getEstimatedNewDependencies() > 2) {
            risks.add("Multiple new dependencies (" + depEvolution.getEstimatedNewDependencies()
                    + ") may increase supply chain risk");
        }

        // Technical debt risks
        if (debtAnalysis.getTechnicalDebtScore() != null && debtAnalysis.getTechnicalDebtScore() < 50) {
            risks.add("High technical debt score (" + debtAnalysis.getTechnicalDebtScore()
                    + "/100) indicates growing maintenance burden");
        }

        // Scalability risks
        if (scalabilityAnalysis.getScalabilityScore() != null && scalabilityAnalysis.getScalabilityScore() < 40) {
            risks.add("Low scalability readiness (" + scalabilityAnalysis.getScalabilityScore()
                    + "/100) may limit future growth");
        }

        return risks;
    }

    /**
     * Generates recommended repository practices based on analysis.
     */
    List<String> generateRecommendedPractices(
            ArchitectureEvolutionAnalysis archEvolution,
            ConventionConsistencyAnalysis conventionAnalysis,
            MaintainabilityAnalysis maintAnalysis,
            TechnicalDebtIndicatorsAnalysis debtAnalysis,
            ScalabilityReadinessAnalysis scalabilityAnalysis) {
        List<String> recommendations = new ArrayList<>();

        // Architecture recommendations
        if (archEvolution.getArchitectureScore() != null && archEvolution.getArchitectureScore() < 60) {
            recommendations.add("Consider documenting and enforcing architectural boundaries");
        }
        if (archEvolution.getArchitecturalDrifts() != null && !archEvolution.getArchitecturalDrifts().isEmpty()) {
            recommendations.add("Review and address architectural drift indicators");
        }

        // Convention recommendations
        if (conventionAnalysis.getConventionScore() != null && conventionAnalysis.getConventionScore() < 60) {
            recommendations.add("Improve convention consistency across the repository");
        }

        // Maintainability recommendations
        if (maintAnalysis.getMaintainabilityScore() != null && maintAnalysis.getMaintainabilityScore() < 50) {
            recommendations.add("Prioritize refactoring to improve maintainability");
        }
        if (maintAnalysis.getComplexityConcerns() != null && !maintAnalysis.getComplexityConcerns().isEmpty()) {
            recommendations.add("Address complexity concerns to reduce maintenance overhead");
        }

        // Technical debt recommendations
        if (debtAnalysis.getTechnicalDebtScore() != null && debtAnalysis.getTechnicalDebtScore() < 60) {
            recommendations.add("Create a technical debt reduction plan");
        }

        // Scalability recommendations
        if (scalabilityAnalysis.getScalabilityScore() != null && scalabilityAnalysis.getScalabilityScore() < 50) {
            recommendations.add("Evaluate scalability requirements and plan for future growth");
        }

        // General recommendations
        recommendations.add("Maintain consistent coding conventions across all modules");
        recommendations.add("Document architectural decisions and evolution rationale");

        return recommendations;
    }

    /**
     * Calculates the overall repository evolution score.
     */
    int calculateEvolutionScore(
            ArchitectureEvolutionAnalysis archEvolution,
            PackageGrowthAnalysis pkgGrowth,
            ModuleExpansionAnalysis moduleExpansion,
            DependencyEvolutionAnalysis depEvolution,
            ConventionConsistencyAnalysis conventionAnalysis,
            MaintainabilityAnalysis maintAnalysis,
            TechnicalDebtIndicatorsAnalysis debtAnalysis,
            ScalabilityReadinessAnalysis scalabilityAnalysis) {
        int totalScore = 0;
        int categories = 0;

        if (archEvolution.getArchitectureScore() != null) {
            totalScore += archEvolution.getArchitectureScore();
            categories++;
        }
        if (conventionAnalysis.getConventionScore() != null) {
            totalScore += conventionAnalysis.getConventionScore();
            categories++;
        }
        if (maintAnalysis.getMaintainabilityScore() != null) {
            totalScore += maintAnalysis.getMaintainabilityScore();
            categories++;
        }
        if (debtAnalysis.getTechnicalDebtScore() != null) {
            totalScore += debtAnalysis.getTechnicalDebtScore();
            categories++;
        }
        if (scalabilityAnalysis.getScalabilityScore() != null) {
            totalScore += scalabilityAnalysis.getScalabilityScore();
            categories++;
        }
        if (moduleExpansion.getModuleCohesionScore() != null) {
            totalScore += moduleExpansion.getModuleCohesionScore();
            categories++;
        }

        if (categories > 0) {
            return clampScore(totalScore / categories);
        }
        return 50;
    }

    // --- Private helper methods ---

    private String buildArchitectureSummary(int score, List<String> drifts) {
        if (drifts.isEmpty()) {
            return "Architecture evolution is well-aligned. Score: " + score + "/100.";
        }
        return "Architecture evolution has " + drifts.size()
                + " drift indicator(s). Score: " + score + "/100.";
    }

    private int clampScore(int score) {
        return Math.max(0, Math.min(100, score));
    }
}