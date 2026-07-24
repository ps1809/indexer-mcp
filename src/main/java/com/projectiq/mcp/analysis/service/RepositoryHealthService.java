package com.projectiq.mcp.analysis.service;

import com.projectiq.mcp.analysis.dto.ConfidenceLevel;
import com.projectiq.mcp.analysis.dto.RepositoryHealthResponse;
import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.dto.ClassSummary;
import com.projectiq.mcp.client.dto.PackageSummary;
import com.projectiq.mcp.client.dto.RepositorySummaryRequest;
import com.projectiq.mcp.client.dto.RepositorySummaryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service that analyzes repository health and produces deterministic,
 * repository-aware health assessments. This service evaluates repository
 * maintainability, complexity, architecture consistency, dependency health,
 * testing maturity, and documentation maturity based solely on indexed
 * repository data.
 *
 * <p>This service uses the {@link IndexerRestClient} to retrieve repository
 * summary data and analyzes package structure, class types, and naming
 * conventions to infer health characteristics.</p>
 *
 * <p>All outputs are deterministic, stable, and free of duplicate entries.
 * This service NEVER infers unsupported health metrics or modifies repository
 * contents.</p>
 */
@Service
public class RepositoryHealthService {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryHealthService.class);

    private final IndexerRestClient indexerRestClient;

    // --- Layer detection patterns (reused from ArchitectureInsightsService) ---

    private static final Pattern CONTROLLER_PATTERN = Pattern.compile(
            "\\b(controller|endpoint|resource)\\b", Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SERVICE_PATTERN = Pattern.compile(
            "\\b(service|manager|handler|processor|provider)\\b", Pattern.CASE_INSENSITIVE
    );

    private static final Pattern REPOSITORY_PATTERN = Pattern.compile(
            "\\b(repository|dao|dataaccess|storage|persistence|mapper)\\b", Pattern.CASE_INSENSITIVE
    );

    private static final Pattern ENTITY_PATTERN = Pattern.compile(
            "\\b(entity|model|domain|vo|valueobject)\\b", Pattern.CASE_INSENSITIVE
    );

    private static final Pattern DTO_PATTERN = Pattern.compile(
            "\\b(dto|request|response|form|vo)\\b", Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CONFIG_PATTERN = Pattern.compile(
            "\\b(config|configuration|properties|setting)\\b", Pattern.CASE_INSENSITIVE
    );

    // --- Test detection patterns ---

    private static final Pattern TEST_PATTERN = Pattern.compile(
            "\\b(test|spec|it|integrationtest|unittest)\\b", Pattern.CASE_INSENSITIVE
    );

    // --- Documentation detection patterns ---

    private static final Pattern DOC_PATTERN = Pattern.compile(
            "\\b(doc|documentation|readme|wiki|guide|manual)\\b", Pattern.CASE_INSENSITIVE
    );

    // --- Rating thresholds ---

    private static final int SCORE_EXCELLENT = 85;
    private static final int SCORE_GOOD = 65;
    private static final int SCORE_FAIR = 45;
    private static final int SCORE_POOR = 25;

    public RepositoryHealthService(IndexerRestClient indexerRestClient) {
        this.indexerRestClient = indexerRestClient;
    }

    /**
     * Analyzes the health of a repository and produces deterministic health metrics.
     *
     * @param repositoryName the repository name to analyze
     * @param branch         the git branch (optional, defaults to "main")
     * @return a {@link RepositoryHealthResponse} containing health analysis
     */
    public RepositoryHealthResponse analyzeHealth(String repositoryName, String branch) {
        logger.info("Analyzing repository health for: {} branch: {}", repositoryName, branch);

        RepositoryHealthResponse response = new RepositoryHealthResponse();
        response.setRepositoryName(repositoryName);
        response.setBranch(branch != null && !branch.trim().isEmpty() ? branch.trim() : "main");

        // Step 1: Retrieve repository summary
        RepositorySummaryResponse summary = retrieveRepositorySummary(repositoryName, response.getBranch());

        if (summary == null) {
            response.setRepositoryOverview("Repository data not available. Unable to analyze health.");
            response.setHealthScore(0);
            response.setMaintainabilityRating("Unknown");
            response.setComplexityRating("Unknown");
            response.setArchitectureConsistency("Unknown");
            response.setDependencyHealth("Unknown");
            response.setTestingMaturity("Unknown");
            response.setDocumentationMaturity("Unknown");
            response.setMaintainabilitySummary("Repository data could not be retrieved. Health analysis cannot be performed.");
            response.setConfidenceLevel(ConfidenceLevel.LOW.name());
            return response;
        }

        // Step 2: Build repository overview
        String overview = buildRepositoryOverview(summary);
        response.setRepositoryOverview(overview);

        // Step 3: Extract packages and classes
        List<PackageSummary> packages = summary.getPackages();
        if (packages == null) {
            packages = new ArrayList<>();
        }

        // Step 4: Analyze package organization
        int packageOrganizationScore = analyzePackageOrganization(packages);

        // Step 5: Analyze class distribution
        int classDistributionScore = analyzeClassDistribution(packages);

        // Step 6: Analyze Controller-Service-Repository balance
        int csrBalanceScore = analyzeCsrBalance(packages);

        // Step 7: Analyze configuration complexity
        int configComplexityScore = analyzeConfigurationComplexity(packages);

        // Step 8: Analyze dependency density
        int dependencyDensityScore = analyzeDependencyDensity(packages);

        // Step 9: Analyze REST API distribution
        int restApiDistributionScore = analyzeRestApiDistribution(packages);

        // Step 10: Analyze test coverage availability
        int testCoverageScore = analyzeTestCoverage(packages);

        // Step 11: Analyze documentation coverage
        int documentationScore = analyzeDocumentationCoverage(packages);

        // Step 12: Analyze repository size
        int sizeScore = analyzeRepositorySize(summary);

        // Step 13: Calculate overall health score
        int healthScore = calculateOverallHealthScore(
                packageOrganizationScore,
                classDistributionScore,
                csrBalanceScore,
                configComplexityScore,
                dependencyDensityScore,
                restApiDistributionScore,
                testCoverageScore,
                documentationScore,
                sizeScore
        );
        response.setHealthScore(healthScore);

        // Step 14: Determine ratings
        response.setMaintainabilityRating(determineRating(
                packageOrganizationScore, classDistributionScore, csrBalanceScore, sizeScore));
        response.setComplexityRating(determineComplexityRating(
                configComplexityScore, dependencyDensityScore, packages));
        response.setArchitectureConsistency(determineArchitectureConsistency(
                csrBalanceScore, restApiDistributionScore, packages));
        response.setDependencyHealth(determineDependencyHealth(dependencyDensityScore, packages));
        response.setTestingMaturity(determineTestingMaturity(testCoverageScore));
        response.setDocumentationMaturity(determineDocumentationMaturity(documentationScore));

        // Step 15: Build maintainability summary
        response.setMaintainabilitySummary(buildMaintainabilitySummary(
                healthScore, response.getMaintainabilityRating(), response.getComplexityRating(),
                response.getArchitectureConsistency(), response.getTestingMaturity()));

        // Step 16: Detect strengths
        List<String> strengths = detectStrengths(
                packages, packageOrganizationScore, csrBalanceScore,
                testCoverageScore, documentationScore, sizeScore);
        response.setStrengths(strengths);

        // Step 17: Detect observations
        List<String> observations = detectObservations(
                summary, packages, healthScore, testCoverageScore, documentationScore);
        response.setObservations(observations);

        // Step 18: Detect potential risks
        List<String> risks = detectPotentialRisks(
                packages, configComplexityScore, dependencyDensityScore,
                testCoverageScore, documentationScore);
        response.setPotentialRisks(risks);

        // Step 19: Detect suggested review areas
        List<String> reviewAreas = detectSuggestedReviewAreas(
                packages, csrBalanceScore, testCoverageScore,
                documentationScore, dependencyDensityScore);
        response.setSuggestedReviewAreas(reviewAreas);

        // Step 20: Determine confidence level
        String confidence = determineConfidence(summary, packages);
        response.setConfidenceLevel(confidence);

        logger.info("Repository health analysis complete: score={}, maintainability={}, confidence={}",
                healthScore, response.getMaintainabilityRating(), confidence);

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
     * Builds a human-readable overview of the repository structure.
     */
    String buildRepositoryOverview(RepositorySummaryResponse summary) {
        if (summary == null) {
            return "No repository data available.";
        }

        StringBuilder overview = new StringBuilder();
        overview.append("Repository '").append(summary.getRepositoryName())
                .append("' on branch '").append(summary.getBranch()).append("'");

        if (summary.getStatus() != null) {
            overview.append(" is ").append(summary.getStatus().toLowerCase());
        }

        overview.append(". ");
        overview.append("Contains ").append(summary.getPackageCount()).append(" packages");
        overview.append(", ").append(summary.getClassCount()).append(" classes");
        overview.append(", ").append(summary.getMethodCount()).append(" methods");
        overview.append(", and ").append(summary.getFileCount()).append(" files.");

        if (summary.getCommitCount() > 0) {
            overview.append(" Total commits: ").append(summary.getCommitCount()).append(".");
        }

        if (summary.getLastIndexedDate() != null && !summary.getLastIndexedDate().isEmpty()) {
            overview.append(" Last indexed: ").append(summary.getLastIndexedDate()).append(".");
        }

        return overview.toString();
    }

    // --- Scoring methods ---

    /**
     * Analyzes package organization quality.
     */
    int analyzePackageOrganization(List<PackageSummary> packages) {
        if (packages == null || packages.isEmpty()) {
            return 0;
        }

        int score = 0;

        // Score based on number of packages (well-organized repos have multiple packages)
        if (packages.size() >= 10) {
            score += 40;
        } else if (packages.size() >= 5) {
            score += 30;
        } else if (packages.size() >= 3) {
            score += 20;
        } else {
            score += 10;
        }

        // Score based on package naming depth (deeper = better organized)
        boolean hasDeepPackages = packages.stream()
                .anyMatch(p -> p.getPackageName() != null
                        && p.getPackageName().split("\\.").length >= 4);
        if (hasDeepPackages) {
            score += 20;
        }

        // Score based on package name quality (lowercase, dot-separated)
        boolean hasWellNamedPackages = packages.stream()
                .filter(p -> p.getPackageName() != null)
                .allMatch(p -> p.getPackageName().matches("^[a-z][a-z0-9]*(\\.[a-z][a-z0-9]*)*$"));
        if (hasWellNamedPackages) {
            score += 20;
        }

        // Score based on class-to-package ratio (balanced distribution)
        long totalClasses = packages.stream()
                .filter(p -> p.getClasses() != null)
                .mapToLong(p -> p.getClasses().size())
                .sum();
        if (totalClasses > 0 && packages.size() > 0) {
            double avgClassesPerPackage = (double) totalClasses / packages.size();
            if (avgClassesPerPackage >= 3 && avgClassesPerPackage <= 15) {
                score += 20;
            } else if (avgClassesPerPackage > 0) {
                score += 10;
            }
        }

        return Math.min(score, 100);
    }

    /**
     * Analyzes class distribution across packages.
     */
    int analyzeClassDistribution(List<PackageSummary> packages) {
        if (packages == null || packages.isEmpty()) {
            return 0;
        }

        int score = 50; // Start at neutral

        // Check for packages with too many classes (potential god packages)
        boolean hasLargePackages = packages.stream()
                .anyMatch(p -> p.getClasses() != null && p.getClasses().size() > 20);
        if (hasLargePackages) {
            score -= 20;
        }

        // Check for packages with too few classes (potential over-splitting)
        boolean hasTinyPackages = packages.stream()
                .anyMatch(p -> p.getClasses() != null && p.getClasses().size() == 1
                        && packages.size() > 3);
        if (hasTinyPackages) {
            score -= 10;
        }

        // Check for classes with too many methods (potential god classes)
        boolean hasLargeClasses = packages.stream()
                .filter(p -> p.getClasses() != null)
                .flatMap(p -> p.getClasses().stream())
                .anyMatch(c -> c.getMethodCount() > 20);
        if (hasLargeClasses) {
            score -= 20;
        }

        // Check for classes with too many fields
        boolean hasFieldHeavyClasses = packages.stream()
                .filter(p -> p.getClasses() != null)
                .flatMap(p -> p.getClasses().stream())
                .anyMatch(c -> c.getFieldCount() > 15);
        if (hasFieldHeavyClasses) {
            score -= 10;
        }

        // Bonus for balanced class sizes
        boolean hasBalancedClasses = packages.stream()
                .filter(p -> p.getClasses() != null)
                .flatMap(p -> p.getClasses().stream())
                .noneMatch(c -> c.getMethodCount() > 20 || c.getFieldCount() > 15);
        if (hasBalancedClasses) {
            score += 10;
        }

        return Math.max(0, Math.min(score, 100));
    }

    /**
     * Analyzes Controller-Service-Repository balance.
     */
    int analyzeCsrBalance(List<PackageSummary> packages) {
        if (packages == null || packages.isEmpty()) {
            return 0;
        }

        boolean hasController = hasLayer(packages, CONTROLLER_PATTERN);
        boolean hasService = hasLayer(packages, SERVICE_PATTERN);
        boolean hasRepository = hasLayer(packages, REPOSITORY_PATTERN);

        int score = 0;

        // All three layers present = well-structured
        if (hasController && hasService && hasRepository) {
            score += 50;
        } else if ((hasController && hasService) || (hasService && hasRepository)) {
            score += 30;
        } else if (hasController || hasService || hasRepository) {
            score += 15;
        }

        // Check for balanced class counts across layers
        long controllerCount = countClassesInLayer(packages, CONTROLLER_PATTERN);
        long serviceCount = countClassesInLayer(packages, SERVICE_PATTERN);
        long repositoryCount = countClassesInLayer(packages, REPOSITORY_PATTERN);

        if (hasController && hasService && hasRepository) {
            // Ideal: controllers <= services, services >= repositories
            if (controllerCount <= serviceCount && serviceCount >= repositoryCount) {
                score += 30;
            } else if (controllerCount <= serviceCount || serviceCount >= repositoryCount) {
                score += 15;
            }
        }

        // Bonus for having entity and DTO layers alongside CSR
        boolean hasEntity = hasLayer(packages, ENTITY_PATTERN);
        boolean hasDto = hasLayer(packages, DTO_PATTERN);
        if (hasController && hasService && hasRepository && hasEntity && hasDto) {
            score += 20;
        } else if (hasController && hasService && hasRepository && (hasEntity || hasDto)) {
            score += 10;
        }

        return Math.min(score, 100);
    }

    /**
     * Analyzes configuration complexity.
     */
    int analyzeConfigurationComplexity(List<PackageSummary> packages) {
        if (packages == null || packages.isEmpty()) {
            return 50; // Neutral - no config detected
        }

        boolean hasConfig = hasLayer(packages, CONFIG_PATTERN);
        if (!hasConfig) {
            return 80; // No config package = simple configuration
        }

        long configClassCount = countClassesInLayer(packages, CONFIG_PATTERN);

        // Few config classes = manageable
        if (configClassCount <= 3) {
            return 70;
        } else if (configClassCount <= 8) {
            return 50;
        } else {
            return 30; // Many config classes = complex configuration
        }
    }

    /**
     * Analyzes dependency density.
     */
    int analyzeDependencyDensity(List<PackageSummary> packages) {
        if (packages == null || packages.isEmpty()) {
            return 50; // Neutral
        }

        // Count total classes and methods
        long totalClasses = packages.stream()
                .filter(p -> p.getClasses() != null)
                .mapToLong(p -> p.getClasses().size())
                .sum();

        long totalMethods = packages.stream()
                .filter(p -> p.getClasses() != null)
                .flatMap(p -> p.getClasses().stream())
                .mapToLong(ClassSummary::getMethodCount)
                .sum();

        if (totalClasses == 0) {
            return 50;
        }

        // Calculate average methods per class (proxy for dependency density)
        double avgMethodsPerClass = (double) totalMethods / totalClasses;

        if (avgMethodsPerClass <= 5) {
            return 80; // Low density
        } else if (avgMethodsPerClass <= 10) {
            return 60; // Moderate density
        } else if (avgMethodsPerClass <= 20) {
            return 40; // High density
        } else {
            return 20; // Very high density
        }
    }

    /**
     * Analyzes REST API distribution.
     */
    int analyzeRestApiDistribution(List<PackageSummary> packages) {
        if (packages == null || packages.isEmpty()) {
            return 50; // Neutral
        }

        boolean hasController = hasLayer(packages, CONTROLLER_PATTERN);
        if (!hasController) {
            return 70; // No controllers = no REST API concerns
        }

        long controllerCount = countClassesInLayer(packages, CONTROLLER_PATTERN);

        // Few controllers = focused API surface
        if (controllerCount <= 3) {
            return 80;
        } else if (controllerCount <= 10) {
            return 60;
        } else {
            return 40; // Many controllers = potentially fragmented API
        }
    }

    /**
     * Analyzes test coverage availability based on indexed test classes.
     */
    int analyzeTestCoverage(List<PackageSummary> packages) {
        if (packages == null || packages.isEmpty()) {
            return 0;
        }

        // Count test classes (classes with Test, Spec, IT, etc. in name)
        long testClassCount = packages.stream()
                .filter(p -> p.getClasses() != null)
                .flatMap(p -> p.getClasses().stream())
                .filter(c -> c.getClassName() != null && TEST_PATTERN.matcher(c.getClassName()).find())
                .count();

        // Count total classes
        long totalClasses = packages.stream()
                .filter(p -> p.getClasses() != null)
                .mapToLong(p -> p.getClasses().size())
                .sum();

        if (totalClasses == 0) {
            return 0;
        }

        // Calculate test-to-class ratio
        double testRatio = (double) testClassCount / totalClasses;

        if (testRatio >= 0.5) {
            return 90; // Excellent test coverage
        } else if (testRatio >= 0.3) {
            return 70; // Good test coverage
        } else if (testRatio >= 0.1) {
            return 50; // Some test coverage
        } else if (testRatio > 0) {
            return 30; // Minimal test coverage
        } else {
            return 10; // No tests detected
        }
    }

    /**
     * Analyzes documentation coverage based on indexed documentation files.
     */
    int analyzeDocumentationCoverage(List<PackageSummary> packages) {
        if (packages == null || packages.isEmpty()) {
            return 0;
        }

        // Count documentation-related classes/packages
        boolean hasDocPackages = packages.stream()
                .anyMatch(p -> p.getPackageName() != null
                        && DOC_PATTERN.matcher(p.getPackageName()).find());

        long docClassCount = packages.stream()
                .filter(p -> p.getClasses() != null)
                .flatMap(p -> p.getClasses().stream())
                .filter(c -> c.getClassName() != null
                        && DOC_PATTERN.matcher(c.getClassName()).find())
                .count();

        if (hasDocPackages && docClassCount > 0) {
            return 70; // Documentation packages and classes present
        } else if (hasDocPackages || docClassCount > 0) {
            return 50; // Some documentation artifacts
        } else {
            return 20; // No documentation detected
        }
    }

    /**
     * Analyzes repository size.
     */
    int analyzeRepositorySize(RepositorySummaryResponse summary) {
        if (summary == null) {
            return 0;
        }

        long classCount = summary.getClassCount();
        long packageCount = summary.getPackageCount();
        long fileCount = summary.getFileCount();

        // Small repository
        if (classCount <= 10 && packageCount <= 3 && fileCount <= 20) {
            return 80;
        }

        // Medium repository
        if (classCount <= 50 && packageCount <= 10 && fileCount <= 100) {
            return 70;
        }

        // Large repository
        if (classCount <= 200 && packageCount <= 30 && fileCount <= 500) {
            return 60;
        }

        // Very large repository
        if (classCount <= 500 && packageCount <= 100 && fileCount <= 2000) {
            return 50;
        }

        // Extremely large repository
        return 40;
    }

    /**
     * Calculates the overall health score from individual component scores.
     */
    int calculateOverallHealthScore(
            int packageOrganizationScore,
            int classDistributionScore,
            int csrBalanceScore,
            int configComplexityScore,
            int dependencyDensityScore,
            int restApiDistributionScore,
            int testCoverageScore,
            int documentationScore,
            int sizeScore) {

        // Weighted average calculation
        double weightedScore =
                (packageOrganizationScore * 0.15) +
                (classDistributionScore * 0.10) +
                (csrBalanceScore * 0.15) +
                (configComplexityScore * 0.10) +
                (dependencyDensityScore * 0.10) +
                (restApiDistributionScore * 0.05) +
                (testCoverageScore * 0.15) +
                (documentationScore * 0.10) +
                (sizeScore * 0.10);

        return (int) Math.round(weightedScore);
    }

    // --- Rating methods ---

    /**
     * Determines the maintainability rating based on key scores.
     */
    String determineRating(int packageOrgScore, int classDistScore, int csrBalanceScore, int sizeScore) {
        int avgScore = (packageOrgScore + classDistScore + csrBalanceScore + sizeScore) / 4;

        if (avgScore >= SCORE_EXCELLENT) {
            return "Excellent";
        } else if (avgScore >= SCORE_GOOD) {
            return "Good";
        } else if (avgScore >= SCORE_FAIR) {
            return "Fair";
        } else if (avgScore >= SCORE_POOR) {
            return "Poor";
        } else {
            return "Very Poor";
        }
    }

    /**
     * Determines the complexity rating.
     */
    String determineComplexityRating(int configComplexityScore, int dependencyDensityScore,
                                     List<PackageSummary> packages) {
        int avgScore = (configComplexityScore + dependencyDensityScore) / 2;

        if (avgScore >= SCORE_EXCELLENT) {
            return "Low";
        } else if (avgScore >= SCORE_GOOD) {
            return "Moderate";
        } else if (avgScore >= SCORE_FAIR) {
            return "High";
        } else {
            return "Very High";
        }
    }

    /**
     * Determines the architecture consistency rating.
     */
    String determineArchitectureConsistency(int csrBalanceScore, int restApiDistributionScore,
                                            List<PackageSummary> packages) {
        int avgScore = (csrBalanceScore + restApiDistributionScore) / 2;

        if (avgScore >= SCORE_EXCELLENT) {
            return "Consistent";
        } else if (avgScore >= SCORE_GOOD) {
            return "Mostly Consistent";
        } else if (avgScore >= SCORE_FAIR) {
            return "Inconsistent";
        } else {
            return "Unstructured";
        }
    }

    /**
     * Determines the dependency health rating.
     */
    String determineDependencyHealth(int dependencyDensityScore, List<PackageSummary> packages) {
        if (dependencyDensityScore >= SCORE_EXCELLENT) {
            return "Healthy";
        } else if (dependencyDensityScore >= SCORE_GOOD) {
            return "Moderate";
        } else if (dependencyDensityScore >= SCORE_FAIR) {
            return "Concerning";
        } else {
            return "Critical";
        }
    }

    /**
     * Determines the testing maturity rating.
     */
    String determineTestingMaturity(int testCoverageScore) {
        if (testCoverageScore >= SCORE_EXCELLENT) {
            return "Mature";
        } else if (testCoverageScore >= SCORE_GOOD) {
            return "Developing";
        } else if (testCoverageScore >= SCORE_FAIR) {
            return "Minimal";
        } else if (testCoverageScore >= SCORE_POOR) {
            return "Limited";
        } else {
            return "None";
        }
    }

    /**
     * Determines the documentation maturity rating.
     */
    String determineDocumentationMaturity(int documentationScore) {
        if (documentationScore >= SCORE_EXCELLENT) {
            return "Comprehensive";
        } else if (documentationScore >= SCORE_GOOD) {
            return "Adequate";
        } else if (documentationScore >= SCORE_FAIR) {
            return "Minimal";
        } else if (documentationScore >= SCORE_POOR) {
            return "Limited";
        } else {
            return "None";
        }
    }

    /**
     * Builds a human-readable maintainability summary.
     */
    String buildMaintainabilitySummary(int healthScore, String maintainabilityRating,
                                        String complexityRating, String architectureConsistency,
                                        String testingMaturity) {
        StringBuilder summary = new StringBuilder();
        summary.append("Repository health score is ").append(healthScore).append("/100. ");
        summary.append("Maintainability is '").append(maintainabilityRating).append("'");
        summary.append(", complexity is '").append(complexityRating).append("'");
        summary.append(", architecture consistency is '").append(architectureConsistency).append("'");
        summary.append(", and testing maturity is '").append(testingMaturity).append("'.");

        if (healthScore >= SCORE_EXCELLENT) {
            summary.append(" The repository is well-structured and maintainable.");
        } else if (healthScore >= SCORE_GOOD) {
            summary.append(" The repository is in good shape with some areas for improvement.");
        } else if (healthScore >= SCORE_FAIR) {
            summary.append(" The repository has several areas that may need attention.");
        } else {
            summary.append(" The repository may have significant structural concerns.");
        }

        return summary.toString();
    }

    // --- Detection methods ---

    /**
     * Detects repository strengths.
     */
    List<String> detectStrengths(List<PackageSummary> packages, int packageOrgScore,
                                  int csrBalanceScore, int testCoverageScore,
                                  int documentationScore, int sizeScore) {
        Set<String> strengths = new LinkedHashSet<>();

        if (packages == null || packages.isEmpty()) {
            return new ArrayList<>(strengths);
        }

        if (packageOrgScore >= SCORE_GOOD) {
            strengths.add("Well-organized package structure with clear separation of concerns");
        }

        if (csrBalanceScore >= SCORE_GOOD) {
            strengths.add("Good Controller-Service-Repository layering detected");
        }

        if (testCoverageScore >= SCORE_GOOD) {
            strengths.add("Test classes detected indicating test coverage is present");
        }

        if (documentationScore >= SCORE_GOOD) {
            strengths.add("Documentation artifacts detected in the repository");
        }

        if (sizeScore >= SCORE_GOOD) {
            strengths.add("Repository size is manageable and well-contained");
        }

        // Check for multi-module structure
        boolean hasMultiModule = packages.stream()
                .anyMatch(p -> p.getPackageName() != null
                        && p.getPackageName().split("\\.").length >= 3);
        if (hasMultiModule && packageOrgScore >= SCORE_GOOD) {
            strengths.add("Multi-module package structure promotes modularity");
        }

        return new ArrayList<>(strengths);
    }

    /**
     * Detects observations about the repository.
     */
    List<String> detectObservations(RepositorySummaryResponse summary, List<PackageSummary> packages,
                                     int healthScore, int testCoverageScore, int documentationScore) {
        Set<String> observations = new LinkedHashSet<>();

        if (summary == null) {
            return new ArrayList<>(observations);
        }

        observations.add("Repository contains " + summary.getClassCount()
                + " classes across " + summary.getPackageCount() + " packages");

        if (summary.getCommitCount() > 0) {
            observations.add("Repository has " + summary.getCommitCount()
                    + " total commits in the indexed history");
        }

        if (packages != null && !packages.isEmpty()) {
            long totalMethods = packages.stream()
                    .filter(p -> p.getClasses() != null)
                    .flatMap(p -> p.getClasses().stream())
                    .mapToLong(ClassSummary::getMethodCount)
                    .sum();
            observations.add("Total of " + totalMethods + " methods across all classes");
        }

        if (testCoverageScore < SCORE_POOR) {
            observations.add("No test classes detected in the indexed data");
        } else if (testCoverageScore < SCORE_GOOD) {
            observations.add("Limited test coverage detected based on indexed classes");
        }

        if (documentationScore < SCORE_POOR) {
            observations.add("No documentation artifacts detected in the indexed data");
        }

        return new ArrayList<>(observations);
    }

    /**
     * Detects potential risks in the repository.
     */
    List<String> detectPotentialRisks(List<PackageSummary> packages, int configComplexityScore,
                                       int dependencyDensityScore, int testCoverageScore,
                                       int documentationScore) {
        Set<String> risks = new LinkedHashSet<>();

        if (packages == null || packages.isEmpty()) {
            risks.add("No packages detected - repository may be empty or not indexed");
            return new ArrayList<>(risks);
        }

        // Check for large packages (god packages)
        for (PackageSummary pkg : packages) {
            if (pkg.getClasses() != null && pkg.getClasses().size() > 20) {
                risks.add("Package '" + pkg.getPackageName() + "' contains "
                        + pkg.getClasses().size() + " classes - may indicate a god package");
            }
        }

        // Check for large classes (god classes)
        for (PackageSummary pkg : packages) {
            if (pkg.getClasses() != null) {
                for (ClassSummary cls : pkg.getClasses()) {
                    if (cls.getMethodCount() > 20) {
                        risks.add("Class '" + cls.getClassName() + "' has "
                                + cls.getMethodCount() + " methods - may indicate a god class");
                    }
                }
            }
        }

        if (configComplexityScore < SCORE_FAIR) {
            risks.add("High configuration complexity detected - may increase maintenance burden");
        }

        if (dependencyDensityScore < SCORE_FAIR) {
            risks.add("High dependency density detected - classes may have too many responsibilities");
        }

        if (testCoverageScore < SCORE_POOR) {
            risks.add("No test coverage detected - changes may introduce regressions");
        } else if (testCoverageScore < SCORE_FAIR) {
            risks.add("Low test coverage detected - critical paths may be untested");
        }

        if (documentationScore < SCORE_POOR) {
            risks.add("No documentation detected - knowledge may be concentrated in individual developers");
        }

        return new ArrayList<>(risks);
    }

    /**
     * Detects suggested review areas.
     */
    List<String> detectSuggestedReviewAreas(List<PackageSummary> packages, int csrBalanceScore,
                                              int testCoverageScore, int documentationScore,
                                              int dependencyDensityScore) {
        Set<String> reviewAreas = new LinkedHashSet<>();

        if (packages == null || packages.isEmpty()) {
            return new ArrayList<>(reviewAreas);
        }

        if (csrBalanceScore < SCORE_GOOD) {
            boolean hasController = hasLayer(packages, CONTROLLER_PATTERN);
            boolean hasService = hasLayer(packages, SERVICE_PATTERN);
            boolean hasRepository = hasLayer(packages, REPOSITORY_PATTERN);

            if (hasController && !hasService) {
                reviewAreas.add("Review Controller-Service separation - controllers may contain business logic");
            }
            if (hasService && !hasRepository) {
                reviewAreas.add("Review Service-Repository separation - services may contain data access logic");
            }
            if (!hasController && !hasService && !hasRepository) {
                reviewAreas.add("Review architectural layering - no clear Controller/Service/Repository structure");
            }
        }

        if (testCoverageScore < SCORE_FAIR) {
            reviewAreas.add("Review test coverage strategy - consider adding tests for critical paths");
        }

        if (documentationScore < SCORE_FAIR) {
            reviewAreas.add("Review documentation coverage - consider adding documentation for key components");
        }

        if (dependencyDensityScore < SCORE_FAIR) {
            reviewAreas.add("Review class responsibilities - consider splitting large classes");
        }

        // Check for packages needing modularization
        for (PackageSummary pkg : packages) {
            if (pkg.getClasses() != null && pkg.getClasses().size() > 15) {
                reviewAreas.add("Review package '" + pkg.getPackageName()
                        + "' for potential modularization");
                break;
            }
        }

        return new ArrayList<>(reviewAreas);
    }

    /**
     * Determines confidence level based on data availability and completeness.
     */
    String determineConfidence(RepositorySummaryResponse summary, List<PackageSummary> packages) {
        if (summary == null) {
            return ConfidenceLevel.LOW.name();
        }

        if (packages == null || packages.isEmpty()) {
            return ConfidenceLevel.LOW.name();
        }

        // High confidence: rich package structure with multiple layers
        if (packages.size() >= 5 && summary.getClassCount() >= 10) {
            return ConfidenceLevel.HIGH.name();
        }

        // Medium confidence: some packages and classes detected
        if (packages.size() >= 1 && summary.getClassCount() >= 1) {
            return ConfidenceLevel.MEDIUM.name();
        }

        return ConfidenceLevel.LOW.name();
    }

    // --- Private helper methods ---

    /**
     * Checks if any package matches the given layer pattern.
     */
    private boolean hasLayer(List<PackageSummary> packages, Pattern pattern) {
        for (PackageSummary pkg : packages) {
            String pkgName = pkg.getPackageName() != null ? pkg.getPackageName().toLowerCase() : "";
            if (pattern.matcher(pkgName).find()) {
                return true;
            }
            if (pkg.getClasses() != null) {
                for (ClassSummary cls : pkg.getClasses()) {
                    String clsName = cls.getClassName() != null ? cls.getClassName().toLowerCase() : "";
                    if (pattern.matcher(clsName).find()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Counts classes that match the given layer pattern.
     */
    private long countClassesInLayer(List<PackageSummary> packages, Pattern pattern) {
        long count = 0;
        for (PackageSummary pkg : packages) {
            String pkgName = pkg.getPackageName() != null ? pkg.getPackageName().toLowerCase() : "";
            boolean pkgMatches = pattern.matcher(pkgName).find();
            if (pkg.getClasses() != null) {
                for (ClassSummary cls : pkg.getClasses()) {
                    String clsName = cls.getClassName() != null ? cls.getClassName().toLowerCase() : "";
                    if (pkgMatches || pattern.matcher(clsName).find()) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
}