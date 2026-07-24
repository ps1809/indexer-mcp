package com.projectiq.mcp.analysis.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO containing a deterministic repository evolution analysis report.
 * Evaluates how proposed features or architectural changes will affect the
 * long-term evolution of the repository across architecture, package growth,
 * module expansion, dependency evolution, convention consistency, maintainability,
 * technical debt indicators, and scalability readiness.
 *
 * <p>All collections use stable ordering. No duplicate entries are produced.
 * This DTO is serialized to JSON for the MCP tool response.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RepositoryEvolutionAnalysisResponse {

    private String repositoryName;
    private String branch;
    private String proposedChange;
    private String proposedChangeSummary;
    private String architecturalImpact;
    private String maintainabilityAssessment;
    private List<String> technicalDebtIndicators;
    private String conventionCompliance;
    private String scalabilityConsiderations;
    private List<String> longTermRisks;
    private List<String> recommendedRepositoryPractices;
    private Integer repositoryEvolutionScore;

    // --- Analysis Categories ---

    private ArchitectureEvolutionAnalysis architectureEvolution;
    private PackageGrowthAnalysis packageGrowth;
    private ModuleExpansionAnalysis moduleExpansion;
    private DependencyEvolutionAnalysis dependencyEvolution;
    private ConventionConsistencyAnalysis conventionConsistency;
    private MaintainabilityAnalysis maintainability;
    private TechnicalDebtIndicatorsAnalysis technicalDebtAnalysis;
    private ScalabilityReadinessAnalysis scalabilityReadiness;

    public RepositoryEvolutionAnalysisResponse() {
        this.technicalDebtIndicators = new ArrayList<>();
        this.longTermRisks = new ArrayList<>();
        this.recommendedRepositoryPractices = new ArrayList<>();
    }

    // --- Getters and Setters ---

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

    public String getProposedChange() {
        return proposedChange;
    }

    public void setProposedChange(String proposedChange) {
        this.proposedChange = proposedChange;
    }

    public String getProposedChangeSummary() {
        return proposedChangeSummary;
    }

    public void setProposedChangeSummary(String proposedChangeSummary) {
        this.proposedChangeSummary = proposedChangeSummary;
    }

    public String getArchitecturalImpact() {
        return architecturalImpact;
    }

    public void setArchitecturalImpact(String architecturalImpact) {
        this.architecturalImpact = architecturalImpact;
    }

    public String getMaintainabilityAssessment() {
        return maintainabilityAssessment;
    }

    public void setMaintainabilityAssessment(String maintainabilityAssessment) {
        this.maintainabilityAssessment = maintainabilityAssessment;
    }

    public List<String> getTechnicalDebtIndicators() {
        return technicalDebtIndicators;
    }

    public void setTechnicalDebtIndicators(List<String> technicalDebtIndicators) {
        this.technicalDebtIndicators = technicalDebtIndicators != null ? new ArrayList<>(technicalDebtIndicators) : new ArrayList<>();
    }

    public String getConventionCompliance() {
        return conventionCompliance;
    }

    public void setConventionCompliance(String conventionCompliance) {
        this.conventionCompliance = conventionCompliance;
    }

    public String getScalabilityConsiderations() {
        return scalabilityConsiderations;
    }

    public void setScalabilityConsiderations(String scalabilityConsiderations) {
        this.scalabilityConsiderations = scalabilityConsiderations;
    }

    public List<String> getLongTermRisks() {
        return longTermRisks;
    }

    public void setLongTermRisks(List<String> longTermRisks) {
        this.longTermRisks = longTermRisks != null ? new ArrayList<>(longTermRisks) : new ArrayList<>();
    }

    public List<String> getRecommendedRepositoryPractices() {
        return recommendedRepositoryPractices;
    }

    public void setRecommendedRepositoryPractices(List<String> recommendedRepositoryPractices) {
        this.recommendedRepositoryPractices = recommendedRepositoryPractices != null ? new ArrayList<>(recommendedRepositoryPractices) : new ArrayList<>();
    }

    public Integer getRepositoryEvolutionScore() {
        return repositoryEvolutionScore;
    }

    public void setRepositoryEvolutionScore(Integer repositoryEvolutionScore) {
        this.repositoryEvolutionScore = repositoryEvolutionScore;
    }

    public ArchitectureEvolutionAnalysis getArchitectureEvolution() {
        return architectureEvolution;
    }

    public void setArchitectureEvolution(ArchitectureEvolutionAnalysis architectureEvolution) {
        this.architectureEvolution = architectureEvolution;
    }

    public PackageGrowthAnalysis getPackageGrowth() {
        return packageGrowth;
    }

    public void setPackageGrowth(PackageGrowthAnalysis packageGrowth) {
        this.packageGrowth = packageGrowth;
    }

    public ModuleExpansionAnalysis getModuleExpansion() {
        return moduleExpansion;
    }

    public void setModuleExpansion(ModuleExpansionAnalysis moduleExpansion) {
        this.moduleExpansion = moduleExpansion;
    }

    public DependencyEvolutionAnalysis getDependencyEvolution() {
        return dependencyEvolution;
    }

    public void setDependencyEvolution(DependencyEvolutionAnalysis dependencyEvolution) {
        this.dependencyEvolution = dependencyEvolution;
    }

    public ConventionConsistencyAnalysis getConventionConsistency() {
        return conventionConsistency;
    }

    public void setConventionConsistency(ConventionConsistencyAnalysis conventionConsistency) {
        this.conventionConsistency = conventionConsistency;
    }

    public MaintainabilityAnalysis getMaintainability() {
        return maintainability;
    }

    public void setMaintainability(MaintainabilityAnalysis maintainability) {
        this.maintainability = maintainability;
    }

    public TechnicalDebtIndicatorsAnalysis getTechnicalDebtAnalysis() {
        return technicalDebtAnalysis;
    }

    public void setTechnicalDebtAnalysis(TechnicalDebtIndicatorsAnalysis technicalDebtAnalysis) {
        this.technicalDebtAnalysis = technicalDebtAnalysis;
    }

    public ScalabilityReadinessAnalysis getScalabilityReadiness() {
        return scalabilityReadiness;
    }

    public void setScalabilityReadiness(ScalabilityReadinessAnalysis scalabilityReadiness) {
        this.scalabilityReadiness = scalabilityReadiness;
    }

    // --- Nested Analysis DTOs ---

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ArchitectureEvolutionAnalysis {
        private String summary;
        private List<String> architecturalDrifts;
        private Integer architectureScore;

        public ArchitectureEvolutionAnalysis() {
            this.architecturalDrifts = new ArrayList<>();
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public List<String> getArchitecturalDrifts() {
            return architecturalDrifts;
        }

        public void setArchitecturalDrifts(List<String> architecturalDrifts) {
            this.architecturalDrifts = architecturalDrifts != null ? new ArrayList<>(architecturalDrifts) : new ArrayList<>();
        }

        public Integer getArchitectureScore() {
            return architectureScore;
        }

        public void setArchitectureScore(Integer architectureScore) {
            this.architectureScore = architectureScore;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PackageGrowthAnalysis {
        private String summary;
        private Integer estimatedNewPackages;
        private Integer estimatedPackageDensity;

        public PackageGrowthAnalysis() {
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public Integer getEstimatedNewPackages() {
            return estimatedNewPackages;
        }

        public void setEstimatedNewPackages(Integer estimatedNewPackages) {
            this.estimatedNewPackages = estimatedNewPackages;
        }

        public Integer getEstimatedPackageDensity() {
            return estimatedPackageDensity;
        }

        public void setEstimatedPackageDensity(Integer estimatedPackageDensity) {
            this.estimatedPackageDensity = estimatedPackageDensity;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ModuleExpansionAnalysis {
        private String summary;
        private Integer estimatedNewClasses;
        private Integer moduleCohesionScore;

        public ModuleExpansionAnalysis() {
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public Integer getEstimatedNewClasses() {
            return estimatedNewClasses;
        }

        public void setEstimatedNewClasses(Integer estimatedNewClasses) {
            this.estimatedNewClasses = estimatedNewClasses;
        }

        public Integer getModuleCohesionScore() {
            return moduleCohesionScore;
        }

        public void setModuleCohesionScore(Integer moduleCohesionScore) {
            this.moduleCohesionScore = moduleCohesionScore;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DependencyEvolutionAnalysis {
        private String summary;
        private Integer estimatedNewDependencies;
        private Boolean circularDependencyRisk;

        public DependencyEvolutionAnalysis() {
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public Integer getEstimatedNewDependencies() {
            return estimatedNewDependencies;
        }

        public void setEstimatedNewDependencies(Integer estimatedNewDependencies) {
            this.estimatedNewDependencies = estimatedNewDependencies;
        }

        public Boolean getCircularDependencyRisk() {
            return circularDependencyRisk;
        }

        public void setCircularDependencyRisk(Boolean circularDependencyRisk) {
            this.circularDependencyRisk = circularDependencyRisk;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ConventionConsistencyAnalysis {
        private String summary;
        private Integer conventionScore;
        private List<String> deviations;

        public ConventionConsistencyAnalysis() {
            this.deviations = new ArrayList<>();
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public Integer getConventionScore() {
            return conventionScore;
        }

        public void setConventionScore(Integer conventionScore) {
            this.conventionScore = conventionScore;
        }

        public List<String> getDeviations() {
            return deviations;
        }

        public void setDeviations(List<String> deviations) {
            this.deviations = deviations != null ? new ArrayList<>(deviations) : new ArrayList<>();
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MaintainabilityAnalysis {
        private String summary;
        private Integer maintainabilityScore;
        private List<String> complexityConcerns;

        public MaintainabilityAnalysis() {
            this.complexityConcerns = new ArrayList<>();
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public Integer getMaintainabilityScore() {
            return maintainabilityScore;
        }

        public void setMaintainabilityScore(Integer maintainabilityScore) {
            this.maintainabilityScore = maintainabilityScore;
        }

        public List<String> getComplexityConcerns() {
            return complexityConcerns;
        }

        public void setComplexityConcerns(List<String> complexityConcerns) {
            this.complexityConcerns = complexityConcerns != null ? new ArrayList<>(complexityConcerns) : new ArrayList<>();
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TechnicalDebtIndicatorsAnalysis {
        private String summary;
        private Integer technicalDebtScore;
        private List<String> debtIndicators;

        public TechnicalDebtIndicatorsAnalysis() {
            this.debtIndicators = new ArrayList<>();
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public Integer getTechnicalDebtScore() {
            return technicalDebtScore;
        }

        public void setTechnicalDebtScore(Integer technicalDebtScore) {
            this.technicalDebtScore = technicalDebtScore;
        }

        public List<String> getDebtIndicators() {
            return debtIndicators;
        }

        public void setDebtIndicators(List<String> debtIndicators) {
            this.debtIndicators = debtIndicators != null ? new ArrayList<>(debtIndicators) : new ArrayList<>();
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ScalabilityReadinessAnalysis {
        private String summary;
        private Integer scalabilityScore;
        private List<String> scalabilityConcerns;

        public ScalabilityReadinessAnalysis() {
            this.scalabilityConcerns = new ArrayList<>();
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public Integer getScalabilityScore() {
            return scalabilityScore;
        }

        public void setScalabilityScore(Integer scalabilityScore) {
            this.scalabilityScore = scalabilityScore;
        }

        public List<String> getScalabilityConcerns() {
            return scalabilityConcerns;
        }

        public void setScalabilityConcerns(List<String> scalabilityConcerns) {
            this.scalabilityConcerns = scalabilityConcerns != null ? new ArrayList<>(scalabilityConcerns) : new ArrayList<>();
        }
    }
}