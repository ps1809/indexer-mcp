package com.projectiq.mcp.analysis.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the complete result of analyzing the potential impact of a
 * proposed development task on the repository. Contains original task
 * information, affected components, scope estimation, risk assessment,
 * and confidence level.
 */
public class ImpactAnalysisResponse {

    private String originalTask;
    private String taskType;
    private List<String> primaryTargets;
    private List<ImpactedComponent> directlyAffectedComponents;
    private List<ImpactedComponent> indirectlyAffectedComponents;
    private List<String> dependencyImpact;
    private ScopeLevel estimatedImplementationScope;
    private ScopeLevel estimatedTestingScope;
    private List<RiskItem> potentialRisks;
    private ConfidenceLevel confidenceLevel;

    public ImpactAnalysisResponse() {
        this.primaryTargets = new ArrayList<>();
        this.directlyAffectedComponents = new ArrayList<>();
        this.indirectlyAffectedComponents = new ArrayList<>();
        this.dependencyImpact = new ArrayList<>();
        this.potentialRisks = new ArrayList<>();
    }

    public String getOriginalTask() {
        return originalTask;
    }

    public void setOriginalTask(String originalTask) {
        this.originalTask = originalTask;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public List<String> getPrimaryTargets() {
        return primaryTargets;
    }

    public void setPrimaryTargets(List<String> primaryTargets) {
        this.primaryTargets = primaryTargets;
    }

    public void addPrimaryTarget(String target) {
        if (this.primaryTargets == null) {
            this.primaryTargets = new ArrayList<>();
        }
        this.primaryTargets.add(target);
    }

    public List<ImpactedComponent> getDirectlyAffectedComponents() {
        return directlyAffectedComponents;
    }

    public void setDirectlyAffectedComponents(List<ImpactedComponent> directlyAffectedComponents) {
        this.directlyAffectedComponents = directlyAffectedComponents;
    }

    public void addDirectlyAffectedComponent(ImpactedComponent component) {
        if (this.directlyAffectedComponents == null) {
            this.directlyAffectedComponents = new ArrayList<>();
        }
        this.directlyAffectedComponents.add(component);
    }

    public List<ImpactedComponent> getIndirectlyAffectedComponents() {
        return indirectlyAffectedComponents;
    }

    public void setIndirectlyAffectedComponents(List<ImpactedComponent> indirectlyAffectedComponents) {
        this.indirectlyAffectedComponents = indirectlyAffectedComponents;
    }

    public void addIndirectlyAffectedComponent(ImpactedComponent component) {
        if (this.indirectlyAffectedComponents == null) {
            this.indirectlyAffectedComponents = new ArrayList<>();
        }
        this.indirectlyAffectedComponents.add(component);
    }

    public List<String> getDependencyImpact() {
        return dependencyImpact;
    }

    public void setDependencyImpact(List<String> dependencyImpact) {
        this.dependencyImpact = dependencyImpact;
    }

    public void addDependencyImpact(String impact) {
        if (this.dependencyImpact == null) {
            this.dependencyImpact = new ArrayList<>();
        }
        this.dependencyImpact.add(impact);
    }

    public ScopeLevel getEstimatedImplementationScope() {
        return estimatedImplementationScope;
    }

    public void setEstimatedImplementationScope(ScopeLevel estimatedImplementationScope) {
        this.estimatedImplementationScope = estimatedImplementationScope;
    }

    public ScopeLevel getEstimatedTestingScope() {
        return estimatedTestingScope;
    }

    public void setEstimatedTestingScope(ScopeLevel estimatedTestingScope) {
        this.estimatedTestingScope = estimatedTestingScope;
    }

    public List<RiskItem> getPotentialRisks() {
        return potentialRisks;
    }

    public void setPotentialRisks(List<RiskItem> potentialRisks) {
        this.potentialRisks = potentialRisks;
    }

    public void addPotentialRisk(RiskItem risk) {
        if (this.potentialRisks == null) {
            this.potentialRisks = new ArrayList<>();
        }
        this.potentialRisks.add(risk);
    }

    public ConfidenceLevel getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(ConfidenceLevel confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }

    /**
     * Represents a repository component that may be affected by the proposed change.
     */
    public static class ImpactedComponent {
        private String componentName;
        private String componentType;
        private String impactReason;

        public ImpactedComponent() {
        }

        public ImpactedComponent(String componentName, String componentType, String impactReason) {
            this.componentName = componentName;
            this.componentType = componentType;
            this.impactReason = impactReason;
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

        public String getImpactReason() {
            return impactReason;
        }

        public void setImpactReason(String impactReason) {
            this.impactReason = impactReason;
        }
    }

    /**
     * Represents a potential risk identified during impact analysis.
     */
    public static class RiskItem {
        private String description;
        private RiskLevel riskLevel;
        private String mitigation;

        public RiskItem() {
        }

        public RiskItem(String description, RiskLevel riskLevel, String mitigation) {
            this.description = description;
            this.riskLevel = riskLevel;
            this.mitigation = mitigation;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public RiskLevel getRiskLevel() {
            return riskLevel;
        }

        public void setRiskLevel(RiskLevel riskLevel) {
            this.riskLevel = riskLevel;
        }

        public String getMitigation() {
            return mitigation;
        }

        public void setMitigation(String mitigation) {
            this.mitigation = mitigation;
        }
    }
}