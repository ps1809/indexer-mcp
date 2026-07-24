package com.projectiq.mcp.analysis.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO containing deterministic architectural decision recommendations.
 * Evaluates architectural alternatives and provides pros/cons, impact analysis,
 * and a recommended approach based on repository intelligence.
 *
 * <p>All collections use stable ordering. No duplicate entries are produced.
 * This DTO is serialized to JSON for the MCP tool response.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArchitecturalDecisionResponse {

    private String decisionId;
    private String decisionCategory;
    private String requestDescription;
    private String repositoryName;
    private List<Alternative> alternatives;
    private String recommendedApproach;
    private String decisionRationale;
    private ImpactAssessment repositoryImpact;
    private DependencyImplications dependencyImplications;
    private ScalabilityAssessment scalabilityAssessment;
    private MaintainabilityAssessment maintainabilityAssessment;
    private List<String> architecturalRisks;
    private String warning;

    public ArchitecturalDecisionResponse() {
        this.alternatives = new ArrayList<>();
        this.architecturalRisks = new ArrayList<>();
        this.repositoryImpact = new ImpactAssessment();
        this.dependencyImplications = new DependencyImplications();
        this.scalabilityAssessment = new ScalabilityAssessment();
        this.maintainabilityAssessment = new MaintainabilityAssessment();
    }

    public String getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(String decisionId) {
        this.decisionId = decisionId;
    }

    public String getDecisionCategory() {
        return decisionCategory;
    }

    public void setDecisionCategory(String decisionCategory) {
        this.decisionCategory = decisionCategory;
    }

    public String getRequestDescription() {
        return requestDescription;
    }

    public void setRequestDescription(String requestDescription) {
        this.requestDescription = requestDescription;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public List<Alternative> getAlternatives() {
        return alternatives;
    }

    public void setAlternatives(List<Alternative> alternatives) {
        this.alternatives = alternatives != null ? new ArrayList<>(alternatives) : new ArrayList<>();
    }

    public String getRecommendedApproach() {
        return recommendedApproach;
    }

    public void setRecommendedApproach(String recommendedApproach) {
        this.recommendedApproach = recommendedApproach;
    }

    public String getDecisionRationale() {
        return decisionRationale;
    }

    public void setDecisionRationale(String decisionRationale) {
        this.decisionRationale = decisionRationale;
    }

    public ImpactAssessment getRepositoryImpact() {
        return repositoryImpact;
    }

    public void setRepositoryImpact(ImpactAssessment repositoryImpact) {
        this.repositoryImpact = repositoryImpact;
    }

    public DependencyImplications getDependencyImplications() {
        return dependencyImplications;
    }

    public void setDependencyImplications(DependencyImplications dependencyImplications) {
        this.dependencyImplications = dependencyImplications;
    }

    public ScalabilityAssessment getScalabilityAssessment() {
        return scalabilityAssessment;
    }

    public void setScalabilityAssessment(ScalabilityAssessment scalabilityAssessment) {
        this.scalabilityAssessment = scalabilityAssessment;
    }

    public MaintainabilityAssessment getMaintainabilityAssessment() {
        return maintainabilityAssessment;
    }

    public void setMaintainabilityAssessment(MaintainabilityAssessment maintainabilityAssessment) {
        this.maintainabilityAssessment = maintainabilityAssessment;
    }

    public List<String> getArchitecturalRisks() {
        return architecturalRisks;
    }

    public void setArchitecturalRisks(List<String> architecturalRisks) {
        this.architecturalRisks = architecturalRisks != null ? new ArrayList<>(architecturalRisks) : new ArrayList<>();
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    // --- Inner DTOs ---

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Alternative {
        private String name;
        private String description;
        private List<String> pros;
        private List<String> cons;
        private int suitabilityScore;
        private String complexityLevel;
        private String maintainabilityRating;

        public Alternative() {
            this.pros = new ArrayList<>();
            this.cons = new ArrayList<>();
        }

        public Alternative(String name, String description, List<String> pros, List<String> cons) {
            this.name = name;
            this.description = description;
            this.pros = pros != null ? new ArrayList<>(pros) : new ArrayList<>();
            this.cons = cons != null ? new ArrayList<>(cons) : new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getPros() {
            return pros;
        }

        public void setPros(List<String> pros) {
            this.pros = pros != null ? new ArrayList<>(pros) : new ArrayList<>();
        }

        public List<String> getCons() {
            return cons;
        }

        public void setCons(List<String> cons) {
            this.cons = cons != null ? new ArrayList<>(cons) : new ArrayList<>();
        }

        public int getSuitabilityScore() {
            return suitabilityScore;
        }

        public void setSuitabilityScore(int suitabilityScore) {
            this.suitabilityScore = suitabilityScore;
        }

        public String getComplexityLevel() {
            return complexityLevel;
        }

        public void setComplexityLevel(String complexityLevel) {
            this.complexityLevel = complexityLevel;
        }

        public String getMaintainabilityRating() {
            return maintainabilityRating;
        }

        public void setMaintainabilityRating(String maintainabilityRating) {
            this.maintainabilityRating = maintainabilityRating;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ImpactAssessment {
        private String overallImpact;
        private int filesAffected;
        private int classesAffected;
        private String architecturalConsistencyImpact;
        private String conventionAlignment;

        public ImpactAssessment() {
        }

        public String getOverallImpact() {
            return overallImpact;
        }

        public void setOverallImpact(String overallImpact) {
            this.overallImpact = overallImpact;
        }

        public int getFilesAffected() {
            return filesAffected;
        }

        public void setFilesAffected(int filesAffected) {
            this.filesAffected = filesAffected;
        }

        public int getClassesAffected() {
            return classesAffected;
        }

        public void setClassesAffected(int classesAffected) {
            this.classesAffected = classesAffected;
        }

        public String getArchitecturalConsistencyImpact() {
            return architecturalConsistencyImpact;
        }

        public void setArchitecturalConsistencyImpact(String architecturalConsistencyImpact) {
            this.architecturalConsistencyImpact = architecturalConsistencyImpact;
        }

        public String getConventionAlignment() {
            return conventionAlignment;
        }

        public void setConventionAlignment(String conventionAlignment) {
            this.conventionAlignment = conventionAlignment;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DependencyImplications {
        private int newDependenciesRequired;
        private int existingDependenciesAffected;
        private String dependencyComplexity;
        private String circularDependencyRisk;

        public DependencyImplications() {
        }

        public int getNewDependenciesRequired() {
            return newDependenciesRequired;
        }

        public void setNewDependenciesRequired(int newDependenciesRequired) {
            this.newDependenciesRequired = newDependenciesRequired;
        }

        public int getExistingDependenciesAffected() {
            return existingDependenciesAffected;
        }

        public void setExistingDependenciesAffected(int existingDependenciesAffected) {
            this.existingDependenciesAffected = existingDependenciesAffected;
        }

        public String getDependencyComplexity() {
            return dependencyComplexity;
        }

        public void setDependencyComplexity(String dependencyComplexity) {
            this.dependencyComplexity = dependencyComplexity;
        }

        public String getCircularDependencyRisk() {
            return circularDependencyRisk;
        }

        public void setCircularDependencyRisk(String circularDependencyRisk) {
            this.circularDependencyRisk = circularDependencyRisk;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ScalabilityAssessment {
        private String horizontalScalability;
        private String verticalScalability;
        private String performanceImplication;
        private String resourceUtilizationEstimate;

        public ScalabilityAssessment() {
        }

        public String getHorizontalScalability() {
            return horizontalScalability;
        }

        public void setHorizontalScalability(String horizontalScalability) {
            this.horizontalScalability = horizontalScalability;
        }

        public String getVerticalScalability() {
            return verticalScalability;
        }

        public void setVerticalScalability(String verticalScalability) {
            this.verticalScalability = verticalScalability;
        }

        public String getPerformanceImplication() {
            return performanceImplication;
        }

        public void setPerformanceImplication(String performanceImplication) {
            this.performanceImplication = performanceImplication;
        }

        public String getResourceUtilizationEstimate() {
            return resourceUtilizationEstimate;
        }

        public void setResourceUtilizationEstimate(String resourceUtilizationEstimate) {
            this.resourceUtilizationEstimate = resourceUtilizationEstimate;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MaintainabilityAssessment {
        private String codeComplexity;
        private String testability;
        private String reusability;
        private String longTermMaintainability;

        public MaintainabilityAssessment() {
        }

        public String getCodeComplexity() {
            return codeComplexity;
        }

        public void setCodeComplexity(String codeComplexity) {
            this.codeComplexity = codeComplexity;
        }

        public String getTestability() {
            return testability;
        }

        public void setTestability(String testability) {
            this.testability = testability;
        }

        public String getReusability() {
            return reusability;
        }

        public void setReusability(String reusability) {
            this.reusability = reusability;
        }

        public String getLongTermMaintainability() {
            return longTermMaintainability;
        }

        public void setLongTermMaintainability(String longTermMaintainability) {
            this.longTermMaintainability = longTermMaintainability;
        }
    }

    /**
     * Supported decision categories.
     */
    public static final class DecisionCategory {
        public static final String NEW_SERVICE_VS_EXISTING = "New Service vs Existing Service";
        public static final String NEW_MODULE_VS_EXISTING = "New Module vs Existing Module";
        public static final String EXTEND_API_VS_CREATE_API = "Extend API vs Create API";
        public static final String EVENT_DRIVEN_VS_SYNCHRONOUS = "Event-Driven vs Synchronous";
        public static final String COMPOSITION_VS_INHERITANCE = "Composition vs Inheritance";
        public static final String CONFIGURATION_VS_CODE = "Configuration vs Code";
        public static final String SHARED_VS_DEDICATED = "Shared Component vs Dedicated Component";
        public static final String PACKAGE_ORGANIZATION = "Package Organization";

        private DecisionCategory() {
        }
    }
}