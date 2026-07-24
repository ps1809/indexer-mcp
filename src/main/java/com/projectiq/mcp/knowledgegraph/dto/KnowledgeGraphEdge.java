package com.projectiq.mcp.knowledgegraph.dto;

import java.util.Objects;

/**
 * Represents a directed edge (relationship) between two nodes in the repository knowledge graph.
 */
public class KnowledgeGraphEdge {

    private String id;
    private String sourceId;
    private String targetId;
    private RelationshipType relationshipType;
    private String label;
    private int weight;

    public KnowledgeGraphEdge() {
    }

    public KnowledgeGraphEdge(String id, String sourceId, String targetId,
                              RelationshipType relationshipType, String label) {
        this.id = id;
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.relationshipType = relationshipType;
        this.label = label;
        this.weight = 1;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public RelationshipType getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(RelationshipType relationshipType) {
        this.relationshipType = relationshipType;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KnowledgeGraphEdge that = (KnowledgeGraphEdge) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "KnowledgeGraphEdge{" +
                "id='" + id + '\'' +
                ", sourceId='" + sourceId + '\'' +
                ", targetId='" + targetId + '\'' +
                ", relationshipType=" + relationshipType +
                ", label='" + label + '\'' +
                ", weight=" + weight +
                '}';
    }
}