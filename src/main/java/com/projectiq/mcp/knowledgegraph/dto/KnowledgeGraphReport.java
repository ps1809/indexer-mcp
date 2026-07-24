package com.projectiq.mcp.knowledgegraph.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a comprehensive knowledge graph report containing graph analysis data.
 */
public class KnowledgeGraphReport {

    private String repositoryName;
    private String branch;
    private List<String> connectedEntities;
    private List<GraphRelationship> relationshipGraph;
    private List<String> dependencyPaths;
    private List<String> architecturalRelationships;
    private List<String> indirectDependencies;
    private List<String> criticalNodes;
    private GraphStatistics graphStatistics;
    private String traversalSummary;

    public KnowledgeGraphReport() {
        this.connectedEntities = new ArrayList<>();
        this.relationshipGraph = new ArrayList<>();
        this.dependencyPaths = new ArrayList<>();
        this.architecturalRelationships = new ArrayList<>();
        this.indirectDependencies = new ArrayList<>();
        this.criticalNodes = new ArrayList<>();
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

    public List<String> getConnectedEntities() {
        return connectedEntities;
    }

    public void setConnectedEntities(List<String> connectedEntities) {
        this.connectedEntities = connectedEntities;
    }

    public List<GraphRelationship> getRelationshipGraph() {
        return relationshipGraph;
    }

    public void setRelationshipGraph(List<GraphRelationship> relationshipGraph) {
        this.relationshipGraph = relationshipGraph;
    }

    public List<String> getDependencyPaths() {
        return dependencyPaths;
    }

    public void setDependencyPaths(List<String> dependencyPaths) {
        this.dependencyPaths = dependencyPaths;
    }

    public List<String> getArchitecturalRelationships() {
        return architecturalRelationships;
    }

    public void setArchitecturalRelationships(List<String> architecturalRelationships) {
        this.architecturalRelationships = architecturalRelationships;
    }

    public List<String> getIndirectDependencies() {
        return indirectDependencies;
    }

    public void setIndirectDependencies(List<String> indirectDependencies) {
        this.indirectDependencies = indirectDependencies;
    }

    public List<String> getCriticalNodes() {
        return criticalNodes;
    }

    public void setCriticalNodes(List<String> criticalNodes) {
        this.criticalNodes = criticalNodes;
    }

    public GraphStatistics getGraphStatistics() {
        return graphStatistics;
    }

    public void setGraphStatistics(GraphStatistics graphStatistics) {
        this.graphStatistics = graphStatistics;
    }

    public String getTraversalSummary() {
        return traversalSummary;
    }

    public void setTraversalSummary(String traversalSummary) {
        this.traversalSummary = traversalSummary;
    }

    /**
     * Represents a single relationship entry in the graph.
     */
    public static class GraphRelationship {
        private String source;
        private String target;
        private String relationship;
        private String sourceType;
        private String targetType;

        public GraphRelationship() {
        }

        public GraphRelationship(String source, String target, String relationship,
                                 String sourceType, String targetType) {
            this.source = source;
            this.target = target;
            this.relationship = relationship;
            this.sourceType = sourceType;
            this.targetType = targetType;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public String getRelationship() {
            return relationship;
        }

        public void setRelationship(String relationship) {
            this.relationship = relationship;
        }

        public String getSourceType() {
            return sourceType;
        }

        public void setSourceType(String sourceType) {
            this.sourceType = sourceType;
        }

        public String getTargetType() {
            return targetType;
        }

        public void setTargetType(String targetType) {
            this.targetType = targetType;
        }
    }

    /**
     * Statistics about the knowledge graph.
     */
    public static class GraphStatistics {
        private int totalNodes;
        private int totalEdges;
        private int entityTypeCount;
        private int relationshipTypeCount;
        private int criticalNodeCount;
        private int indirectDependencyCount;
        private double averageConnectionsPerNode;

        public GraphStatistics() {
        }

        public int getTotalNodes() {
            return totalNodes;
        }

        public void setTotalNodes(int totalNodes) {
            this.totalNodes = totalNodes;
        }

        public int getTotalEdges() {
            return totalEdges;
        }

        public void setTotalEdges(int totalEdges) {
            this.totalEdges = totalEdges;
        }

        public int getEntityTypeCount() {
            return entityTypeCount;
        }

        public void setEntityTypeCount(int entityTypeCount) {
            this.entityTypeCount = entityTypeCount;
        }

        public int getRelationshipTypeCount() {
            return relationshipTypeCount;
        }

        public void setRelationshipTypeCount(int relationshipTypeCount) {
            this.relationshipTypeCount = relationshipTypeCount;
        }

        public int getCriticalNodeCount() {
            return criticalNodeCount;
        }

        public void setCriticalNodeCount(int criticalNodeCount) {
            this.criticalNodeCount = criticalNodeCount;
        }

        public int getIndirectDependencyCount() {
            return indirectDependencyCount;
        }

        public void setIndirectDependencyCount(int indirectDependencyCount) {
            this.indirectDependencyCount = indirectDependencyCount;
        }

        public double getAverageConnectionsPerNode() {
            return averageConnectionsPerNode;
        }

        public void setAverageConnectionsPerNode(double averageConnectionsPerNode) {
            this.averageConnectionsPerNode = averageConnectionsPerNode;
        }
    }
}