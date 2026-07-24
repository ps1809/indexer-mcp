package com.projectiq.mcp.knowledgegraph.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single node (entity) in the repository knowledge graph.
 */
public class KnowledgeGraphNode {

    private String id;
    private String name;
    private EntityType entityType;
    private String qualifiedName;
    private List<String> labels;
    private int connectionCount;

    public KnowledgeGraphNode() {
        this.labels = new ArrayList<>();
    }

    public KnowledgeGraphNode(String id, String name, EntityType entityType, String qualifiedName) {
        this();
        this.id = id;
        this.name = name;
        this.entityType = entityType;
        this.qualifiedName = qualifiedName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public int getConnectionCount() {
        return connectionCount;
    }

    public void setConnectionCount(int connectionCount) {
        this.connectionCount = connectionCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KnowledgeGraphNode that = (KnowledgeGraphNode) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "KnowledgeGraphNode{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", entityType=" + entityType +
                ", qualifiedName='" + qualifiedName + '\'' +
                ", connectionCount=" + connectionCount +
                '}';
    }
}