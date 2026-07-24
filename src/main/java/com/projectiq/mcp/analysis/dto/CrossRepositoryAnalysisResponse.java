package com.projectiq.mcp.analysis.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Response DTO containing deterministic cross-repository analysis results.
 * Provides comparison data across multiple repositories including architecture,
 * dependencies, APIs, conventions, and reuse opportunities.
 *
 * <p>All collections use stable ordering. No duplicate entries are produced.
 * This DTO is serialized to JSON for the MCP tool response.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrossRepositoryAnalysisResponse {

    private List<RepositorySummary> repositories;
    private CommonArchitecture commonArchitecture;
    private SharedComponents sharedComponents;
    private SimilarApis similarApis;
    private DependencyComparison dependencyComparison;
    private ConventionComparison conventionComparison;
    private ReuseOpportunities reuseOpportunities;
    private ArchitecturalDifferences architecturalDifferences;
    private RiskAssessment riskAssessment;
    private String analysisId;

    public CrossRepositoryAnalysisResponse() {
        this.repositories = new ArrayList<>();
        this.commonArchitecture = new CommonArchitecture();
        this.sharedComponents = new SharedComponents();
        this.similarApis = new SimilarApis();
        this.dependencyComparison = new DependencyComparison();
        this.conventionComparison = new ConventionComparison();
        this.reuseOpportunities = new ReuseOpportunities();
        this.architecturalDifferences = new ArchitecturalDifferences();
        this.riskAssessment = new RiskAssessment();
    }

    public List<RepositorySummary> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<RepositorySummary> repositories) {
        this.repositories = repositories != null ? new ArrayList<>(repositories) : new ArrayList<>();
    }

    public CommonArchitecture getCommonArchitecture() {
        return commonArchitecture;
    }

    public void setCommonArchitecture(CommonArchitecture commonArchitecture) {
        this.commonArchitecture = commonArchitecture;
    }

    public SharedComponents getSharedComponents() {
        return sharedComponents;
    }

    public void setSharedComponents(SharedComponents sharedComponents) {
        this.sharedComponents = sharedComponents;
    }

    public SimilarApis getSimilarApis() {
        return similarApis;
    }

    public void setSimilarApis(SimilarApis similarApis) {
        this.similarApis = similarApis;
    }

    public DependencyComparison getDependencyComparison() {
        return dependencyComparison;
    }

    public void setDependencyComparison(DependencyComparison dependencyComparison) {
        this.dependencyComparison = dependencyComparison;
    }

    public ConventionComparison getConventionComparison() {
        return conventionComparison;
    }

    public void setConventionComparison(ConventionComparison conventionComparison) {
        this.conventionComparison = conventionComparison;
    }

    public ReuseOpportunities getReuseOpportunities() {
        return reuseOpportunities;
    }

    public void setReuseOpportunities(ReuseOpportunities reuseOpportunities) {
        this.reuseOpportunities = reuseOpportunities;
    }

    public ArchitecturalDifferences getArchitecturalDifferences() {
        return architecturalDifferences;
    }

    public void setArchitecturalDifferences(ArchitecturalDifferences architecturalDifferences) {
        this.architecturalDifferences = architecturalDifferences;
    }

    public RiskAssessment getRiskAssessment() {
        return riskAssessment;
    }

    public void setRiskAssessment(RiskAssessment riskAssessment) {
        this.riskAssessment = riskAssessment;
    }

    public String getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(String analysisId) {
        this.analysisId = analysisId;
    }

    // --- Inner DTOs ---

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RepositorySummary {
        private String repositoryName;
        private String branch;
        private String architecturalStyle;
        private int packageCount;
        private int classCount;
        private int methodCount;
        private int fileCount;
        private List<String> detectedLayers;
        private int commitCount;

        public RepositorySummary() {
            this.detectedLayers = new ArrayList<>();
        }

        public String getRepositoryName() {
            return repositoryName;
        }

        public void setRepositoryName(String repositoryName) {
            this.repositoryName = repositoryName;
        }

        public String getBranch() {
            return branch;
        }

        public void setBranch(String branch) {
            this.branch = branch;
        }

        public String getArchitecturalStyle() {
            return architecturalStyle;
        }

        public void setArchitecturalStyle(String architecturalStyle) {
            this.architecturalStyle = architecturalStyle;
        }

        public int getPackageCount() {
            return packageCount;
        }

        public void setPackageCount(int packageCount) {
            this.packageCount = packageCount;
        }

        public int getClassCount() {
            return classCount;
        }

        public void setClassCount(int classCount) {
            this.classCount = classCount;
        }

        public int getMethodCount() {
            return methodCount;
        }

        public void setMethodCount(int methodCount) {
            this.methodCount = methodCount;
        }

        public int getFileCount() {
            return fileCount;
        }

        public void setFileCount(int fileCount) {
            this.fileCount = fileCount;
        }

        public List<String> getDetectedLayers() {
            return detectedLayers;
        }

        public void setDetectedLayers(List<String> detectedLayers) {
            this.detectedLayers = detectedLayers != null ? new ArrayList<>(detectedLayers) : new ArrayList<>();
        }

        public int getCommitCount() {
            return commitCount;
        }

        public void setCommitCount(int commitCount) {
            this.commitCount = commitCount;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CommonArchitecture {
        private List<String> sharedArchitecturalStyles;
        private List<String> commonLayers;
        private List<String> sharedPatterns;
        private double architectureSimilarityScore;

        public CommonArchitecture() {
            this.sharedArchitecturalStyles = new ArrayList<>();
            this.commonLayers = new ArrayList<>();
            this.sharedPatterns = new ArrayList<>();
        }

        public List<String> getSharedArchitecturalStyles() {
            return sharedArchitecturalStyles;
        }

        public void setSharedArchitecturalStyles(List<String> sharedArchitecturalStyles) {
            this.sharedArchitecturalStyles = sharedArchitecturalStyles != null ? new ArrayList<>(sharedArchitecturalStyles) : new ArrayList<>();
        }

        public List<String> getCommonLayers() {
            return commonLayers;
        }

        public void setCommonLayers(List<String> commonLayers) {
            this.commonLayers = commonLayers != null ? new ArrayList<>(commonLayers) : new ArrayList<>();
        }

        public List<String> getSharedPatterns() {
            return sharedPatterns;
        }

        public void setSharedPatterns(List<String> sharedPatterns) {
            this.sharedPatterns = sharedPatterns != null ? new ArrayList<>(sharedPatterns) : new ArrayList<>();
        }

        public double getArchitectureSimilarityScore() {
            return architectureSimilarityScore;
        }

        public void setArchitectureSimilarityScore(double architectureSimilarityScore) {
            this.architectureSimilarityScore = architectureSimilarityScore;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SharedComponents {
        private List<String> commonClassNames;
        private List<String> commonPackagePrefixes;
        private List<String> commonAnnotations;
        private List<ComponentMatch> componentMatches;

        public SharedComponents() {
            this.commonClassNames = new ArrayList<>();
            this.commonPackagePrefixes = new ArrayList<>();
            this.commonAnnotations = new ArrayList<>();
            this.componentMatches = new ArrayList<>();
        }

        public List<String> getCommonClassNames() {
            return commonClassNames;
        }

        public void setCommonClassNames(List<String> commonClassNames) {
            this.commonClassNames = commonClassNames != null ? new ArrayList<>(commonClassNames) : new ArrayList<>();
        }

        public List<String> getCommonPackagePrefixes() {
            return commonPackagePrefixes;
        }

        public void setCommonPackagePrefixes(List<String> commonPackagePrefixes) {
            this.commonPackagePrefixes = commonPackagePrefixes != null ? new ArrayList<>(commonPackagePrefixes) : new ArrayList<>();
        }

        public List<String> getCommonAnnotations() {
            return commonAnnotations;
        }

        public void setCommonAnnotations(List<String> commonAnnotations) {
            this.commonAnnotations = commonAnnotations != null ? new ArrayList<>(commonAnnotations) : new ArrayList<>();
        }

        public List<ComponentMatch> getComponentMatches() {
            return componentMatches;
        }

        public void setComponentMatches(List<ComponentMatch> componentMatches) {
            this.componentMatches = componentMatches != null ? new ArrayList<>(componentMatches) : new ArrayList<>();
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ComponentMatch {
        private String componentName;
        private String componentType;
        private List<String> presentInRepositories;
        private String description;

        public ComponentMatch() {
            this.presentInRepositories = new ArrayList<>();
        }

        public ComponentMatch(String componentName, String componentType, List<String> presentInRepositories) {
            this.componentName = componentName;
            this.componentType = componentType;
            this.presentInRepositories = presentInRepositories != null ? new ArrayList<>(presentInRepositories) : new ArrayList<>();
        }

        public String getComponentName() {
            return componentName;
        }

        public void setComponentName(String componentName) {
            this.componentName = componentName;
        }

        public String getComponentType() {
            return componentType;
        }

        public void setComponentType(String componentType) {
            this.componentType = componentType;
        }

        public List<String> getPresentInRepositories() {
            return presentInRepositories;
        }

        public void setPresentInRepositories(List<String> presentInRepositories) {
            this.presentInRepositories = presentInRepositories != null ? new ArrayList<>(presentInRepositories) : new ArrayList<>();
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SimilarApis {
        private List<ApiMatch> similarEndpoints;
        private List<String> commonHttpMethods;
        private List<String> commonMediaTypes;
        private int totalSimilarEndpoints;

        public SimilarApis() {
            this.similarEndpoints = new ArrayList<>();
            this.commonHttpMethods = new ArrayList<>();
            this.commonMediaTypes = new ArrayList<>();
        }

        public List<ApiMatch> getSimilarEndpoints() {
            return similarEndpoints;
        }

        public void setSimilarEndpoints(List<ApiMatch> similarEndpoints) {
            this.similarEndpoints = similarEndpoints != null ? new ArrayList<>(similarEndpoints) : new ArrayList<>();
        }

        public List<String> getCommonHttpMethods() {
            return commonHttpMethods;
        }

        public void setCommonHttpMethods(List<String> commonHttpMethods) {
            this.commonHttpMethods = commonHttpMethods != null ? new ArrayList<>(commonHttpMethods) : new ArrayList<>();
        }

        public List<String> getCommonMediaTypes() {
            return commonMediaTypes;
        }

        public void setCommonMediaTypes(List<String> commonMediaTypes) {
            this.commonMediaTypes = commonMediaTypes != null ? new ArrayList<>(commonMediaTypes) : new ArrayList<>();
        }

        public int getTotalSimilarEndpoints() {
            return totalSimilarEndpoints;
        }

        public void setTotalSimilarEndpoints(int totalSimilarEndpoints) {
            this.totalSimilarEndpoints = totalSimilarEndpoints;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ApiMatch {
        private String path;
        private String httpMethod;
        private String description;
        private List<String> presentInRepositories;

        public ApiMatch() {
            this.presentInRepositories = new ArrayList<>();
        }

        public ApiMatch(String path, String httpMethod, List<String> presentInRepositories) {
            this.path = path;
            this.httpMethod = httpMethod;
            this.presentInRepositories = presentInRepositories != null ? new ArrayList<>(presentInRepositories) : new ArrayList<>();
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getHttpMethod() {
            return httpMethod;
        }

        public void setHttpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getPresentInRepositories() {
            return presentInRepositories;
        }

        public void setPresentInRepositories(List<String> presentInRepositories) {
            this.presentInRepositories = presentInRepositories != null ? new ArrayList<>(presentInRepositories) : new ArrayList<>();
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DependencyComparison {
        private List<DependencyEntry> commonDependencies;
        private List<DependencyEntry> uniqueDependencies;
        private List<DependencyVersionDiff> versionDifferences;
        private int totalCommonDependencies;
        private int totalUniqueDependencies;

        public DependencyComparison() {
            this.commonDependencies = new ArrayList<>();
            this.uniqueDependencies = new ArrayList<>();
            this.versionDifferences = new ArrayList<>();
        }

        public List<DependencyEntry> getCommonDependencies() {
            return commonDependencies;
        }

        public void setCommonDependencies(List<DependencyEntry> commonDependencies) {
            this.commonDependencies = commonDependencies != null ? new ArrayList<>(commonDependencies) : new ArrayList<>();
        }

        public List<DependencyEntry> getUniqueDependencies() {
            return uniqueDependencies;
        }

        public void setUniqueDependencies(List<DependencyEntry> uniqueDependencies) {
            this.uniqueDependencies = uniqueDependencies != null ? new ArrayList<>(uniqueDependencies) : new ArrayList<>();
        }

        public List<DependencyVersionDiff> getVersionDifferences() {
            return versionDifferences;
        }

        public void setVersionDifferences(List<DependencyVersionDiff> versionDifferences) {
            this.versionDifferences = versionDifferences != null ? new ArrayList<>(versionDifferences) : new ArrayList<>();
        }

        public int getTotalCommonDependencies() {
            return totalCommonDependencies;
        }

        public void setTotalCommonDependencies(int totalCommonDependencies) {
            this.totalCommonDependencies = totalCommonDependencies;
        }

        public int getTotalUniqueDependencies() {
            return totalUniqueDependencies;
        }

        public void setTotalUniqueDependencies(int totalUniqueDependencies) {
            this.totalUniqueDependencies = totalUniqueDependencies;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DependencyEntry {
        private String name;
        private String groupId;
        private String artifactId;
        private String type;
        private List<String> presentInRepositories;

        public DependencyEntry() {
            this.presentInRepositories = new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<String> getPresentInRepositories() {
            return presentInRepositories;
        }

        public void setPresentInRepositories(List<String> presentInRepositories) {
            this.presentInRepositories = presentInRepositories != null ? new ArrayList<>(presentInRepositories) : new ArrayList<>();
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DependencyVersionDiff {
        private String name;
        private String groupId;
        private String artifactId;
        private Map<String, String> versions;

        public DependencyVersionDiff() {
            this.versions = new LinkedHashMap<>();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public Map<String, String> getVersions() {
            return versions;
        }

        public void setVersions(Map<String, String> versions) {
            this.versions = versions != null ? new LinkedHashMap<>(versions) : new LinkedHashMap<>();
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ConventionComparison {
        private List<String> commonNamingConventions;
        private List<String> commonPackageConventions;
        private List<String> commonTestingConventions;
        private List<String> commonAnnotationConventions;
        private List<String> commonRestApiConventions;
        private List<String> commonArchitecturalConventions;
        private double conventionSimilarityScore;

        public ConventionComparison() {
            this.commonNamingConventions = new ArrayList<>();
            this.commonPackageConventions = new ArrayList<>();
            this.commonTestingConventions = new ArrayList<>();
            this.commonAnnotationConventions = new ArrayList<>();
            this.commonRestApiConventions = new ArrayList<>();
            this.commonArchitecturalConventions = new ArrayList<>();
        }

        public List<String> getCommonNamingConventions() {
            return commonNamingConventions;
        }

        public void setCommonNamingConventions(List<String> commonNamingConventions) {
            this.commonNamingConventions = commonNamingConventions != null ? new ArrayList<>(commonNamingConventions) : new ArrayList<>();
        }

        public List<String> getCommonPackageConventions() {
            return commonPackageConventions;
        }

        public void setCommonPackageConventions(List<String> commonPackageConventions) {
            this.commonPackageConventions = commonPackageConventions != null ? new ArrayList<>(commonPackageConventions) : new ArrayList<>();
        }

        public List<String> getCommonTestingConventions() {
            return commonTestingConventions;
        }

        public void setCommonTestingConventions(List<String> commonTestingConventions) {
            this.commonTestingConventions = commonTestingConventions != null ? new ArrayList<>(commonTestingConventions) : new ArrayList<>();
        }

        public List<String> getCommonAnnotationConventions() {
            return commonAnnotationConventions;
        }

        public void setCommonAnnotationConventions(List<String> commonAnnotationConventions) {
            this.commonAnnotationConventions = commonAnnotationConventions != null ? new ArrayList<>(commonAnnotationConventions) : new ArrayList<>();
        }

        public List<String> getCommonRestApiConventions() {
            return commonRestApiConventions;
        }

        public void setCommonRestApiConventions(List<String> commonRestApiConventions) {
            this.commonRestApiConventions = commonRestApiConventions != null ? new ArrayList<>(commonRestApiConventions) : new ArrayList<>();
        }

        public List<String> getCommonArchitecturalConventions() {
            return commonArchitecturalConventions;
        }

        public void setCommonArchitecturalConventions(List<String> commonArchitecturalConventions) {
            this.commonArchitecturalConventions = commonArchitecturalConventions != null ? new ArrayList<>(commonArchitecturalConventions) : new ArrayList<>();
        }

        public double getConventionSimilarityScore() {
            return conventionSimilarityScore;
        }

        public void setConventionSimilarityScore(double conventionSimilarityScore) {
            this.conventionSimilarityScore = conventionSimilarityScore;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReuseOpportunities {
        private List<String> potentialSharedLibraries;
        private List<String> extractableCommonServices;
        private List<String> sharedConfigurationCandidates;
        private List<String> reusableApiContracts;
        private int totalReuseOpportunities;

        public ReuseOpportunities() {
            this.potentialSharedLibraries = new ArrayList<>();
            this.extractableCommonServices = new ArrayList<>();
            this.sharedConfigurationCandidates = new ArrayList<>();
            this.reusableApiContracts = new ArrayList<>();
        }

        public List<String> getPotentialSharedLibraries() {
            return potentialSharedLibraries;
        }

        public void setPotentialSharedLibraries(List<String> potentialSharedLibraries) {
            this.potentialSharedLibraries = potentialSharedLibraries != null ? new ArrayList<>(potentialSharedLibraries) : new ArrayList<>();
        }

        public List<String> getExtractableCommonServices() {
            return extractableCommonServices;
        }

        public void setExtractableCommonServices(List<String> extractableCommonServices) {
            this.extractableCommonServices = extractableCommonServices != null ? new ArrayList<>(extractableCommonServices) : new ArrayList<>();
        }

        public List<String> getSharedConfigurationCandidates() {
            return sharedConfigurationCandidates;
        }

        public void setSharedConfigurationCandidates(List<String> sharedConfigurationCandidates) {
            this.sharedConfigurationCandidates = sharedConfigurationCandidates != null ? new ArrayList<>(sharedConfigurationCandidates) : new ArrayList<>();
        }

        public List<String> getReusableApiContracts() {
            return reusableApiContracts;
        }

        public void setReusableApiContracts(List<String> reusableApiContracts) {
            this.reusableApiContracts = reusableApiContracts != null ? new ArrayList<>(reusableApiContracts) : new ArrayList<>();
        }

        public int getTotalReuseOpportunities() {
            return totalReuseOpportunities;
        }

        public void setTotalReuseOpportunities(int totalReuseOpportunities) {
            this.totalReuseOpportunities = totalReuseOpportunities;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ArchitecturalDifferences {
        private List<String> differentArchitecturalStyles;
        private List<String> uniqueLayers;
        private List<String> uniquePatterns;
        private List<String> architectureGapDescription;
        private double differenceScore;

        public ArchitecturalDifferences() {
            this.differentArchitecturalStyles = new ArrayList<>();
            this.uniqueLayers = new ArrayList<>();
            this.uniquePatterns = new ArrayList<>();
            this.architectureGapDescription = new ArrayList<>();
        }

        public List<String> getDifferentArchitecturalStyles() {
            return differentArchitecturalStyles;
        }

        public void setDifferentArchitecturalStyles(List<String> differentArchitecturalStyles) {
            this.differentArchitecturalStyles = differentArchitecturalStyles != null ? new ArrayList<>(differentArchitecturalStyles) : new ArrayList<>();
        }

        public List<String> getUniqueLayers() {
            return uniqueLayers;
        }

        public void setUniqueLayers(List<String> uniqueLayers) {
            this.uniqueLayers = uniqueLayers != null ? new ArrayList<>(uniqueLayers) : new ArrayList<>();
        }

        public List<String> getUniquePatterns() {
            return uniquePatterns;
        }

        public void setUniquePatterns(List<String> uniquePatterns) {
            this.uniquePatterns = uniquePatterns != null ? new ArrayList<>(uniquePatterns) : new ArrayList<>();
        }

        public List<String> getArchitectureGapDescription() {
            return architectureGapDescription;
        }

        public void setArchitectureGapDescription(List<String> architectureGapDescription) {
            this.architectureGapDescription = architectureGapDescription != null ? new ArrayList<>(architectureGapDescription) : new ArrayList<>();
        }

        public double getDifferenceScore() {
            return differenceScore;
        }

        public void setDifferenceScore(double differenceScore) {
            this.differenceScore = differenceScore;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RiskAssessment {
        private List<String> risks;
        private List<String> incompatibilities;
        private List<String> integrationChallenges;
        private String overallRiskLevel;

        public RiskAssessment() {
            this.risks = new ArrayList<>();
            this.incompatibilities = new ArrayList<>();
            this.integrationChallenges = new ArrayList<>();
        }

        public List<String> getRisks() {
            return risks;
        }

        public void setRisks(List<String> risks) {
            this.risks = risks != null ? new ArrayList<>(risks) : new ArrayList<>();
        }

        public List<String> getIncompatibilities() {
            return incompatibilities;
        }

        public void setIncompatibilities(List<String> incompatibilities) {
            this.incompatibilities = incompatibilities != null ? new ArrayList<>(incompatibilities) : new ArrayList<>();
        }

        public List<String> getIntegrationChallenges() {
            return integrationChallenges;
        }

        public void setIntegrationChallenges(List<String> integrationChallenges) {
            this.integrationChallenges = integrationChallenges != null ? new ArrayList<>(integrationChallenges) : new ArrayList<>();
        }

        public String getOverallRiskLevel() {
            return overallRiskLevel;
        }

        public void setOverallRiskLevel(String overallRiskLevel) {
            this.overallRiskLevel = overallRiskLevel;
        }
    }
}