package com.projectiq.mcp.analysis.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Response DTO containing deterministic architecture insights for a repository.
 * Provides an architectural overview including layers, module relationships,
 * dependency flow, detected patterns, strengths, and concerns.
 *
 * <p>All collections use stable ordering. No duplicate entries are produced.
 * This DTO is serialized to JSON for the MCP tool response.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArchitectureInsightsResponse {

    private String repositoryName;
    private String branch;
    private String repositoryOverview;
    private String architecturalStyle;
    private List<String> detectedLayers;
    private List<ModuleRelationship> moduleRelationships;
    private String dependencyFlow;
    private List<String> crossLayerDependencies;
    private List<String> architecturalStrengths;
    private List<String> potentialConcerns;
    private String confidenceLevel;

    public ArchitectureInsightsResponse() {
        this.detectedLayers = new ArrayList<>();
        this.moduleRelationships = new ArrayList<>();
        this.crossLayerDependencies = new ArrayList<>();
        this.architecturalStrengths = new ArrayList<>();
        this.potentialConcerns = new ArrayList<>();
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

    public String getRepositoryOverview() {
        return repositoryOverview;
    }

    public void setRepositoryOverview(String repositoryOverview) {
        this.repositoryOverview = repositoryOverview;
    }

    public String getArchitecturalStyle() {
        return architecturalStyle;
    }

    public void setArchitecturalStyle(String architecturalStyle) {
        this.architecturalStyle = architecturalStyle;
    }

    public List<String> getDetectedLayers() {
        return detectedLayers;
    }

    public void setDetectedLayers(List<String> detectedLayers) {
        this.detectedLayers = detectedLayers != null ? new ArrayList<>(detectedLayers) : new ArrayList<>();
    }

    public List<ModuleRelationship> getModuleRelationships() {
        return moduleRelationships;
    }

    public void setModuleRelationships(List<ModuleRelationship> moduleRelationships) {
        this.moduleRelationships = moduleRelationships != null ? new ArrayList<>(moduleRelationships) : new ArrayList<>();
    }

    public String getDependencyFlow() {
        return dependencyFlow;
    }

    public void setDependencyFlow(String dependencyFlow) {
        this.dependencyFlow = dependencyFlow;
    }

    public List<String> getCrossLayerDependencies() {
        return crossLayerDependencies;
    }

    public void setCrossLayerDependencies(List<String> crossLayerDependencies) {
        this.crossLayerDependencies = crossLayerDependencies != null ? new ArrayList<>(crossLayerDependencies) : new ArrayList<>();
    }

    public List<String> getArchitecturalStrengths() {
        return architecturalStrengths;
    }

    public void setArchitecturalStrengths(List<String> architecturalStrengths) {
        this.architecturalStrengths = architecturalStrengths != null ? new ArrayList<>(architecturalStrengths) : new ArrayList<>();
    }

    public List<String> getPotentialConcerns() {
        return potentialConcerns;
    }

    public void setPotentialConcerns(List<String> potentialConcerns) {
        this.potentialConcerns = potentialConcerns != null ? new ArrayList<>(potentialConcerns) : new ArrayList<>();
    }

    public String getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(String confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }

    /**
     * Represents a relationship between two modules in the repository.
     * Captures the source module, target module, and the nature of the dependency.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ModuleRelationship {

        private String sourceModule;
        private String targetModule;
        private String relationshipType;

        public ModuleRelationship() {
        }

        public ModuleRelationship(String sourceModule, String targetModule, String relationshipType) {
            this.sourceModule = sourceModule;
            this.targetModule = targetModule;
            this.relationshipType = relationshipType;
        }

        public String getSourceModule() {
            return sourceModule;
        }

        public void setSourceModule(String sourceModule) {
            this.sourceModule = sourceModule;
        }

        public String getTargetModule() {
            return targetModule;
        }

        public void setTargetModule(String targetModule) {
            this.targetModule = targetModule;
        }

        public String getRelationshipType() {
            return relationshipType;
        }

        public void setRelationshipType(String relationshipType) {
            this.relationshipType = relationshipType;
        }
    }
}