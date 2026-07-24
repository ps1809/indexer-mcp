package com.projectiq.mcp.analysis.service;

import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse;
import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse.ApiMatch;
import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse.ArchitecturalDifferences;
import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse.CommonArchitecture;
import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse.ComponentMatch;
import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse.ConventionComparison;
import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse.DependencyComparison;
import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse.DependencyEntry;
import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse.DependencyVersionDiff;
import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse.RepositorySummary;
import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse.ReuseOpportunities;
import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse.RiskAssessment;
import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse.SharedComponents;
import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse.SimilarApis;
import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.dto.DependencyInfo;
import com.projectiq.mcp.client.dto.DependencyRequest;
import com.projectiq.mcp.client.dto.DependencyResponse;
import com.projectiq.mcp.client.dto.PackageSummary;
import com.projectiq.mcp.client.dto.RestApiRequest;
import com.projectiq.mcp.client.dto.RestApiResponse;
import com.projectiq.mcp.client.dto.RestEndpointInfo;
import com.projectiq.mcp.client.dto.RepositorySummaryRequest;
import com.projectiq.mcp.client.dto.RepositorySummaryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service that performs intelligent cross-repository analysis.
 * Compares multiple indexed repositories to identify shared architecture,
 * reusable components, common patterns, dependency differences, and
 * potential code reuse opportunities.
 *
 * <p>This service uses existing analysis services (ArchitectureInsightsService,
 * RepositoryConventionAnalyzerService, RepositoryHealthService, etc.) to
 * gather per-repository data and then performs cross-repository comparison.</p>
 *
 * <p>All outputs are deterministic, stable, and free of duplicate entries.
 * This service NEVER modifies repository contents or performs git operations.</p>
 */
@Service
public class CrossRepositoryAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(CrossRepositoryAnalysisService.class);

    private final IndexerRestClient indexerRestClient;
    private final ArchitectureInsightsService architectureInsightsService;
    private final RepositoryConventionAnalyzerService conventionAnalyzerService;
    private final RepositoryHealthService healthService;

    public CrossRepositoryAnalysisService(
            IndexerRestClient indexerRestClient,
            ArchitectureInsightsService architectureInsightsService,
            RepositoryConventionAnalyzerService conventionAnalyzerService,
            RepositoryHealthService healthService) {
        this.indexerRestClient = indexerRestClient;
        this.architectureInsightsService = architectureInsightsService;
        this.conventionAnalyzerService = conventionAnalyzerService;
        this.healthService = healthService;
    }

    /**
     * Performs cross-repository analysis on the given list of repositories.
     *
     * @param repositories list of repository names to analyze
     * @return a deterministic CrossRepositoryAnalysisResponse with comparison data
     */
    public CrossRepositoryAnalysisResponse analyzeCrossRepository(List<String> repositories) {
        logger.info("Starting cross-repository analysis for {} repositories: {}", 
                repositories != null ? repositories.size() : 0, repositories);

        CrossRepositoryAnalysisResponse response = new CrossRepositoryAnalysisResponse();
        response.setAnalysisId(UUID.randomUUID().toString());

        if (repositories == null || repositories.isEmpty()) {
            return response;
        }

        // Step 1: Retrieve summaries for all repositories
        Map<String, RepositorySummaryResponse> summaries = retrieveAllSummaries(repositories);

        // Step 2: Build repository summaries
        List<RepositorySummary> repoSummaries = buildRepositorySummaries(summaries);
        response.setRepositories(repoSummaries);

        // Step 3: Compare architectures
        CommonArchitecture commonArchitecture = compareArchitectures(summaries);
        response.setCommonArchitecture(commonArchitecture);

        // Step 4: Identify shared components
        SharedComponents sharedComponents = identifySharedComponents(summaries);
        response.setSharedComponents(sharedComponents);

        // Step 5: Compare APIs
        SimilarApis similarApis = compareApis(summaries);
        response.setSimilarApis(similarApis);

        // Step 6: Compare dependencies
        DependencyComparison dependencyComparison = compareDependencies(summaries);
        response.setDependencyComparison(dependencyComparison);

        // Step 7: Compare conventions
        ConventionComparison conventionComparison = compareConventions(summaries);
        response.setConventionComparison(conventionComparison);

        // Step 8: Identify reuse opportunities
        ReuseOpportunities reuseOpportunities = identifyReuseOpportunities(
                commonArchitecture, sharedComponents, similarApis, dependencyComparison, conventionComparison);
        response.setReuseOpportunities(reuseOpportunities);

        // Step 9: Detect architectural differences
        ArchitecturalDifferences architecturalDifferences = detectArchitecturalDifferences(
                summaries);
        response.setArchitecturalDifferences(architecturalDifferences);

        // Step 10: Assess risks
        RiskAssessment riskAssessment = assessRisks(
                summaries, dependencyComparison, architecturalDifferences);
        response.setRiskAssessment(riskAssessment);

        logger.info("Cross-repository analysis complete for {} repositories. Analysis ID: {}",
                repositories.size(), response.getAnalysisId());

        return response;
    }

    /**
     * Retrieves repository summaries for all given repository names.
     */
    Map<String, RepositorySummaryResponse> retrieveAllSummaries(List<String> repositories) {
        Map<String, RepositorySummaryResponse> summaries = new LinkedHashMap<>();
        for (String repoName : repositories) {
            try {
                RepositorySummaryRequest request = new RepositorySummaryRequest();
                request.setRepositoryName(repoName);
                request.setBranch("main");
                RepositorySummaryResponse summary = indexerRestClient.getRepositorySummary(request);
                if (summary != null) {
                    summaries.put(repoName, summary);
                } else {
                    logger.warn("Repository summary returned null for: {}", repoName);
                }
            } catch (Exception e) {
                logger.warn("Failed to retrieve repository summary for '{}': {}", repoName, e.getMessage());
            }
        }
        return summaries;
    }

    /**
     * Builds a list of repository summaries from the retrieved data.
     */
    List<RepositorySummary> buildRepositorySummaries(Map<String, RepositorySummaryResponse> summaries) {
        List<RepositorySummary> results = new ArrayList<>();
        for (Map.Entry<String, RepositorySummaryResponse> entry : summaries.entrySet()) {
            RepositorySummaryResponse summary = entry.getValue();
            RepositorySummary rs = new RepositorySummary();
            rs.setRepositoryName(entry.getKey());
            rs.setBranch(summary.getBranch() != null ? summary.getBranch() : "main");
            rs.setPackageCount((int) summary.getPackageCount());
            rs.setClassCount((int) summary.getClassCount());
            rs.setMethodCount((int) summary.getMethodCount());
            rs.setFileCount((int) summary.getFileCount());
            rs.setCommitCount((int) summary.getCommitCount());

            // Detect architecture style
            try {
                var archResponse = architectureInsightsService.analyzeArchitecture(entry.getKey(), "main");
                rs.setArchitecturalStyle(archResponse.getArchitecturalStyle());
                rs.setDetectedLayers(archResponse.getDetectedLayers());
            } catch (Exception e) {
                logger.warn("Failed to get architecture for '{}': {}", entry.getKey(), e.getMessage());
                rs.setArchitecturalStyle("Unknown");
                rs.setDetectedLayers(new ArrayList<>());
            }

            results.add(rs);
        }
        return results;
    }

    /**
     * Compares architectures across all repositories.
     */
    CommonArchitecture compareArchitectures(Map<String, RepositorySummaryResponse> summaries) {
        CommonArchitecture ca = new CommonArchitecture();

        Set<String> allStyles = new LinkedHashSet<>();
        Set<String> allLayers = new LinkedHashSet<>();
        Set<String> allPatterns = new LinkedHashSet<>();

        for (String repoName : summaries.keySet()) {
            try {
                var archResponse = architectureInsightsService.analyzeArchitecture(repoName, "main");
                if (archResponse.getArchitecturalStyle() != null) {
                    allStyles.add(archResponse.getArchitecturalStyle());
                }
                if (archResponse.getDetectedLayers() != null) {
                    allLayers.addAll(archResponse.getDetectedLayers());
                }
                if (archResponse.getArchitecturalStrengths() != null) {
                    allPatterns.addAll(archResponse.getArchitecturalStrengths());
                }
            } catch (Exception e) {
                logger.warn("Failed to get architecture for '{}': {}", repoName, e.getMessage());
            }
        }

        ca.setSharedArchitecturalStyles(new ArrayList<>(allStyles));
        ca.setCommonLayers(new ArrayList<>(allLayers));
        ca.setSharedPatterns(new ArrayList<>(allPatterns));

        double styleScore = allStyles.size() <= 1 ? 1.0 : 1.0 / allStyles.size();
        double layerScore = allLayers.isEmpty() ? 0.0 : Math.min(1.0, allLayers.size() / 10.0);
        double patternScore = allPatterns.isEmpty() ? 0.0 : Math.min(1.0, allPatterns.size() / 10.0);
        ca.setArchitectureSimilarityScore(Math.round(((styleScore * 0.4) + (layerScore * 0.3) + (patternScore * 0.3)) * 100.0) / 100.0);

        return ca;
    }

    /**
     * Identifies shared components across repositories.
     */
    SharedComponents identifySharedComponents(Map<String, RepositorySummaryResponse> summaries) {
        SharedComponents sc = new SharedComponents();

        Map<String, Set<String>> classNameRepos = new LinkedHashMap<>();
        Map<String, Set<String>> packagePrefixRepos = new LinkedHashMap<>();

        for (Map.Entry<String, RepositorySummaryResponse> entry : summaries.entrySet()) {
            String repoName = entry.getKey();
            RepositorySummaryResponse summary = entry.getValue();

            if (summary.getPackages() != null) {
                for (PackageSummary pkg : summary.getPackages()) {
                    if (pkg.getPackageName() != null) {
                        String[] parts = pkg.getPackageName().split("\\.");
                        if (parts.length >= 2) {
                            String prefix = parts[0] + "." + parts[1];
                            packagePrefixRepos.computeIfAbsent(prefix, k -> new LinkedHashSet<>()).add(repoName);
                        }

                        if (pkg.getClasses() != null) {
                            for (var cls : pkg.getClasses()) {
                                if (cls.getClassName() != null) {
                                    classNameRepos.computeIfAbsent(cls.getClassName(), k -> new LinkedHashSet<>()).add(repoName);
                                }
                            }
                        }
                    }
                }
            }
        }

        List<String> commonClasses = classNameRepos.entrySet().stream()
                .filter(e -> e.getValue().size() == summaries.size())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        sc.setCommonClassNames(commonClasses);

        List<String> commonPrefixes = packagePrefixRepos.entrySet().stream()
                .filter(e -> e.getValue().size() == summaries.size())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        sc.setCommonPackagePrefixes(commonPrefixes);

        List<ComponentMatch> matches = new ArrayList<>();
        for (String className : commonClasses) {
            Set<String> repos = classNameRepos.get(className);
            if (repos != null && repos.size() >= 2) {
                ComponentMatch match = new ComponentMatch(className, "Class", new ArrayList<>(repos));
                matches.add(match);
            }
        }
        for (String prefix : commonPrefixes) {
            Set<String> repos = packagePrefixRepos.get(prefix);
            if (repos != null && repos.size() >= 2) {
                ComponentMatch match = new ComponentMatch(prefix, "Package", new ArrayList<>(repos));
                matches.add(match);
            }
        }
        sc.setComponentMatches(matches);

        return sc;
    }

    /**
     * Compares APIs across repositories.
     */
    SimilarApis compareApis(Map<String, RepositorySummaryResponse> summaries) {
        SimilarApis sa = new SimilarApis();

        Map<String, Set<String>> endpointRepos = new LinkedHashMap<>();

        for (String repoName : summaries.keySet()) {
            try {
                RestApiRequest request = new RestApiRequest();
                request.setRepositoryName(repoName);
                RestApiResponse apiResponse = indexerRestClient.findRestApi(request);

                if (apiResponse != null && apiResponse.getEndpoints() != null) {
                    for (RestEndpointInfo endpoint : apiResponse.getEndpoints()) {
                        if (endpoint.getEndpointPath() != null) {
                            String method = endpoint.getHttpMethod() != null ? endpoint.getHttpMethod() : "UNKNOWN";
                            String key = method + " " + endpoint.getEndpointPath();
                            endpointRepos.computeIfAbsent(key, k -> new LinkedHashSet<>()).add(repoName);
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to get REST APIs for '{}': {}", repoName, e.getMessage());
            }
        }

        List<ApiMatch> similarEndpoints = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : endpointRepos.entrySet()) {
            if (entry.getValue().size() >= 2) {
                String[] parts = entry.getKey().split(" ", 2);
                String method = parts.length > 0 ? parts[0] : "UNKNOWN";
                String path = parts.length > 1 ? parts[1] : entry.getKey();
                ApiMatch match = new ApiMatch(path, method, new ArrayList<>(entry.getValue()));
                similarEndpoints.add(match);
            }
        }

        sa.setSimilarEndpoints(similarEndpoints);
        sa.setTotalSimilarEndpoints(similarEndpoints.size());

        return sa;
    }

    /**
     * Compares dependencies across repositories.
     */
    DependencyComparison compareDependencies(Map<String, RepositorySummaryResponse> summaries) {
        DependencyComparison dc = new DependencyComparison();

        Map<String, Set<String>> depRepos = new LinkedHashMap<>();
        Map<String, Map<String, String>> depVersions = new LinkedHashMap<>();

        for (String repoName : summaries.keySet()) {
            try {
                DependencyRequest request = new DependencyRequest();
                request.setRepositoryName(repoName);
                DependencyResponse depResponse = indexerRestClient.findDependency(request);

                if (depResponse != null && depResponse.getDependencies() != null) {
                    for (DependencyInfo dep : depResponse.getDependencies()) {
                        String groupId = dep.getGroupId() != null ? dep.getGroupId() : "";
                        String artifactId = dep.getArtifactId() != null ? dep.getArtifactId() : "";
                        String version = dep.getVersion() != null ? dep.getVersion() : "";
                        String key = groupId + ":" + artifactId;

                        depRepos.computeIfAbsent(key, k -> new LinkedHashSet<>()).add(repoName);
                        depVersions.computeIfAbsent(key, k -> new LinkedHashMap<>()).put(repoName, version);

                        if (dep.getName() != null) {
                            depRepos.computeIfAbsent(dep.getName(), k -> new LinkedHashSet<>()).add(repoName);
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to get dependencies for '{}': {}", repoName, e.getMessage());
            }
        }

        List<DependencyEntry> commonDeps = new ArrayList<>();
        List<DependencyEntry> uniqueDeps = new ArrayList<>();
        List<DependencyVersionDiff> versionDiffs = new ArrayList<>();

        int repoCount = summaries.size();
        for (Map.Entry<String, Set<String>> entry : depRepos.entrySet()) {
            DependencyEntry depEntry = new DependencyEntry();
            depEntry.setName(entry.getKey());
            depEntry.setPresentInRepositories(new ArrayList<>(entry.getValue()));

            if (entry.getValue().size() == repoCount && repoCount > 1) {
                depEntry.setType("common");
                commonDeps.add(depEntry);

                Map<String, String> versions = depVersions.get(entry.getKey());
                if (versions != null && versions.size() > 1) {
                    Set<String> uniqueVersions = new LinkedHashSet<>(versions.values());
                    if (uniqueVersions.size() > 1) {
                        DependencyVersionDiff diff = new DependencyVersionDiff();
                        diff.setName(entry.getKey());
                        diff.setVersions(new LinkedHashMap<>(versions));
                        versionDiffs.add(diff);
                    }
                }
            } else if (entry.getValue().size() == 1) {
                depEntry.setType("unique");
                uniqueDeps.add(depEntry);
            }
        }

        dc.setCommonDependencies(commonDeps);
        dc.setUniqueDependencies(uniqueDeps);
        dc.setVersionDifferences(versionDiffs);
        dc.setTotalCommonDependencies(commonDeps.size());
        dc.setTotalUniqueDependencies(uniqueDeps.size());

        return dc;
    }

    /**
     * Compares conventions across repositories.
     */
    ConventionComparison compareConventions(Map<String, RepositorySummaryResponse> summaries) {
        ConventionComparison cc = new ConventionComparison();

        Set<String> namingConventions = new LinkedHashSet<>();
        Set<String> packageConventions = new LinkedHashSet<>();
        Set<String> testingConventions = new LinkedHashSet<>();
        Set<String> annotationConventions = new LinkedHashSet<>();
        Set<String> restApiConventions = new LinkedHashSet<>();
        Set<String> archConventions = new LinkedHashSet<>();

        for (String repoName : summaries.keySet()) {
            try {
                var convResponse = conventionAnalyzerService.analyzeConventions(repoName, "main");

                // Naming conventions - check for common naming patterns in class names
                if (summaryContainsConvention(repoName, "Controller")) {
                    namingConventions.add("PascalCase");
                }

                // Package conventions
                var summary = summaries.get(repoName);
                if (summary != null && summary.getPackages() != null) {
                    for (PackageSummary pkg : summary.getPackages()) {
                        if (pkg.getPackageName() != null) {
                            String pkgName = pkg.getPackageName().toLowerCase();
                            if (pkgName.contains("controller") || pkgName.contains("service") 
                                    || pkgName.contains("repository") || pkgName.contains("entity")) {
                                packageConventions.add("Layer-based packaging");
                            }
                            if (pkgName.contains("domain") || pkgName.contains("model")) {
                                packageConventions.add("Domain-based packaging");
                            }
                            if (pkgName.contains("feature") || pkg.getPackageName().contains("module")) {
                                packageConventions.add("Feature-based packaging");
                            }
                        }
                    }
                }

                // Testing conventions
                if (summaryContainsConvention(repoName, "Test")) {
                    testingConventions.add("JUnit");
                }
                if (summaryContainsConvention(repoName, "Mock")) {
                    testingConventions.add("Mockito");
                }

                // Annotation conventions
                if (summaryContainsConvention(repoName, "Service") 
                        || summaryContainsConvention(repoName, "Controller")
                        || summaryContainsConvention(repoName, "Repository")) {
                    annotationConventions.add("Spring Annotations");
                }

                // REST API conventions
                try {
                    RestApiRequest apiRequest = new RestApiRequest();
                    apiRequest.setRepositoryName(repoName);
                    RestApiResponse apiResponse = indexerRestClient.findRestApi(apiRequest);
                    if (apiResponse != null && apiResponse.getEndpoints() != null 
                            && !apiResponse.getEndpoints().isEmpty()) {
                        restApiConventions.add("Spring MVC Annotations");
                    }
                } catch (Exception e) {
                    // Ignore
                }

                // Architectural conventions
                try {
                    var archResponse = architectureInsightsService.analyzeArchitecture(repoName, "main");
                    String style = archResponse.getArchitecturalStyle();
                    if (style != null) {
                        if (style.contains("MVC") || style.contains("Controller")) {
                            archConventions.add("MVC");
                        }
                        if (style.contains("Layered")) {
                            archConventions.add("Layered");
                        }
                        if (style.contains("Hexagonal")) {
                            archConventions.add("Hexagonal");
                        }
                    }
                } catch (Exception e) {
                    // Ignore
                }
            } catch (Exception e) {
                logger.warn("Failed to get conventions for '{}': {}", repoName, e.getMessage());
            }
        }

        cc.setCommonNamingConventions(new ArrayList<>(namingConventions));
        cc.setCommonPackageConventions(new ArrayList<>(packageConventions));
        cc.setCommonTestingConventions(new ArrayList<>(testingConventions));
        cc.setCommonAnnotationConventions(new ArrayList<>(annotationConventions));
        cc.setCommonRestApiConventions(new ArrayList<>(restApiConventions));
        cc.setCommonArchitecturalConventions(new ArrayList<>(archConventions));

        double totalCategories = 6.0;
        double populatedCategories = 0.0;
        if (!namingConventions.isEmpty()) populatedCategories++;
        if (!packageConventions.isEmpty()) populatedCategories++;
        if (!testingConventions.isEmpty()) populatedCategories++;
        if (!annotationConventions.isEmpty()) populatedCategories++;
        if (!restApiConventions.isEmpty()) populatedCategories++;
        if (!archConventions.isEmpty()) populatedCategories++;
        cc.setConventionSimilarityScore(Math.round((populatedCategories / totalCategories) * 100.0) / 100.0);

        return cc;
    }

    /**
     * Helper to check if a repository contains classes with a given convention string.
     */
    private boolean summaryContainsConvention(String repoName, String convention) {
        try {
            RepositorySummaryRequest request = new RepositorySummaryRequest();
            request.setRepositoryName(repoName);
            RepositorySummaryResponse summary = indexerRestClient.getRepositorySummary(request);
            if (summary != null && summary.getPackages() != null) {
                for (PackageSummary pkg : summary.getPackages()) {
                    if (pkg.getClasses() != null) {
                        for (var cls : pkg.getClasses()) {
                            if (cls.getClassName() != null 
                                    && cls.getClassName().contains(convention)) {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }

    /**
     * Identifies reuse opportunities based on comparison data.
     */
    ReuseOpportunities identifyReuseOpportunities(
            CommonArchitecture ca, SharedComponents sc, SimilarApis sa,
            DependencyComparison dc, ConventionComparison cc) {
        ReuseOpportunities ro = new ReuseOpportunities();

        if (dc.getCommonDependencies() != null) {
            List<String> libs = dc.getCommonDependencies().stream()
                    .map(DependencyEntry::getName)
                    .filter(n -> n != null && !n.isEmpty())
                    .collect(Collectors.toList());
            ro.setPotentialSharedLibraries(libs);
        }

        List<String> commonServices = new ArrayList<>();
        if (sc.getComponentMatches() != null) {
            for (ComponentMatch match : sc.getComponentMatches()) {
                if ("Class".equals(match.getComponentType()) && match.getPresentInRepositories().size() >= 2) {
                    commonServices.add(match.getComponentName());
                }
            }
        }
        ro.setExtractableCommonServices(commonServices);

        List<String> configCandidates = new ArrayList<>();
        if (sc.getCommonPackagePrefixes() != null) {
            for (String prefix : sc.getCommonPackagePrefixes()) {
                configCandidates.add("Package prefix: " + prefix);
            }
        }
        ro.setSharedConfigurationCandidates(configCandidates);

        List<String> apiContracts = new ArrayList<>();
        if (sa.getSimilarEndpoints() != null) {
            for (ApiMatch match : sa.getSimilarEndpoints()) {
                apiContracts.add(match.getHttpMethod() + " " + match.getPath());
            }
        }
        ro.setReusableApiContracts(apiContracts);

        int total = libsCount(ro.getPotentialSharedLibraries()) 
                + commonServices.size() 
                + configCandidates.size() 
                + apiContracts.size();
        ro.setTotalReuseOpportunities(total);

        return ro;
    }

    private int libsCount(List<String> libs) {
        return libs != null ? libs.size() : 0;
    }

    /**
     * Detects architectural differences across repositories.
     */
    ArchitecturalDifferences detectArchitecturalDifferences(
            Map<String, RepositorySummaryResponse> summaries) {
        ArchitecturalDifferences ad = new ArchitecturalDifferences();

        Set<String> uniqueStyles = new LinkedHashSet<>();
        Set<String> uniqueLayers = new LinkedHashSet<>();
        Set<String> uniquePatterns = new LinkedHashSet<>();
        List<String> allStyles = new ArrayList<>();

        for (String repoName : summaries.keySet()) {
            try {
                var archResponse = architectureInsightsService.analyzeArchitecture(repoName, "main");
                if (archResponse.getArchitecturalStyle() != null) {
                    allStyles.add(archResponse.getArchitecturalStyle());
                    uniqueStyles.add(archResponse.getArchitecturalStyle());
                }
                if (archResponse.getDetectedLayers() != null) {
                    uniqueLayers.addAll(archResponse.getDetectedLayers());
                }
                if (archResponse.getArchitecturalStrengths() != null) {
                    uniquePatterns.addAll(archResponse.getArchitecturalStrengths());
                }
            } catch (Exception e) {
                logger.warn("Failed to get architecture for '{}': {}", repoName, e.getMessage());
            }
        }

        if (uniqueStyles.size() > 1) {
            ad.setDifferentArchitecturalStyles(new ArrayList<>(uniqueStyles));
        }

        ad.setUniqueLayers(new ArrayList<>(uniqueLayers));
        ad.setUniquePatterns(new ArrayList<>(uniquePatterns));

        List<String> gaps = new ArrayList<>();
        if (uniqueStyles.size() > 1) {
            gaps.add("Repositories use different architectural styles: " + String.join(", ", uniqueStyles));
        }
        if (allStyles.size() != summaries.size()) {
            gaps.add("Some repositories have undetermined architectural styles");
        }
        ad.setArchitectureGapDescription(gaps);

        double styleDiversity = uniqueStyles.size() > 1 ? (double) uniqueStyles.size() / summaries.size() : 0.0;
        double layerDiversity = uniqueLayers.isEmpty() ? 0.0 : Math.min(1.0, uniqueLayers.size() / 15.0);
        double patternDiversity = uniquePatterns.isEmpty() ? 0.0 : Math.min(1.0, uniquePatterns.size() / 15.0);
        ad.setDifferenceScore(Math.round(((styleDiversity * 0.4) + (layerDiversity * 0.3) + (patternDiversity * 0.3)) * 100.0) / 100.0);

        return ad;
    }

    /**
     * Assesses risks across repositories.
     */
    RiskAssessment assessRisks(
            Map<String, RepositorySummaryResponse> summaries,
            DependencyComparison dependencyComparison,
            ArchitecturalDifferences architecturalDifferences) {
        RiskAssessment ra = new RiskAssessment();
        List<String> risks = new ArrayList<>();
        List<String> incompatibilities = new ArrayList<>();
        List<String> integrationChallenges = new ArrayList<>();

        if (dependencyComparison.getVersionDifferences() != null
                && !dependencyComparison.getVersionDifferences().isEmpty()) {
            risks.add("Dependency version mismatches detected across repositories");
            for (DependencyVersionDiff diff : dependencyComparison.getVersionDifferences()) {
                risks.add("Version mismatch for '" + diff.getName() + "' across repositories");
                incompatibilities.add("Version mismatch: " + diff.getName());
            }
        }

        if (architecturalDifferences.getDifferentArchitecturalStyles() != null
                && !architecturalDifferences.getDifferentArchitecturalStyles().isEmpty()) {
            incompatibilities.add("Different architectural styles across repositories");
            integrationChallenges.add("Integrating repositories with different architectural styles may require adapters");
        }

        if (summaries.size() < 2) {
            risks.add("Insufficient repositories for meaningful comparison");
            integrationChallenges.add("At least 2 repositories required for cross-repository analysis");
        }

        String riskLevel;
        int totalRisks = risks.size() + incompatibilities.size() + integrationChallenges.size();
        if (totalRisks == 0) {
            riskLevel = "LOW";
        } else if (totalRisks <= 2) {
            riskLevel = "MEDIUM";
        } else {
            riskLevel = "HIGH";
        }

        ra.setRisks(risks);
        ra.setIncompatibilities(incompatibilities);
        ra.setIntegrationChallenges(integrationChallenges);
        ra.setOverallRiskLevel(riskLevel);

        return ra;
    }
}