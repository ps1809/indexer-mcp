package com.projectiq.mcp.client.dto;

/**
 * Response DTO representing a single dependency found in the ProjectIQ Indexer.
 */
public class DependencyInfo {

    private String name;
    private String groupId;
    private String artifactId;
    private String version;
    private String scope;
    private DependencyType type;

    public DependencyInfo() {
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public DependencyType getType() {
        return type;
    }

    public void setType(DependencyType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "DependencyInfo{" +
                "name='" + name + '\'' +
                ", groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                ", scope='" + scope + '\'' +
                ", type=" + type +
                '}';
    }
}