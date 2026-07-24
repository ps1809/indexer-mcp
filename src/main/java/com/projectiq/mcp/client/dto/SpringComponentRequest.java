package com.projectiq.mcp.client.dto;

import java.util.Arrays;
import java.util.List;

/**
 * Request DTO for searching Spring components in ProjectIQ Indexer.
 */
public class SpringComponentRequest {

    private String repositoryName;
    private String branch;
    private List<String> componentTypes;
    private String packageName;

    public SpringComponentRequest() {
    }

    public SpringComponentRequest(String repositoryName, List<String> componentTypes) {
        this.repositoryName = repositoryName;
        this.componentTypes = componentTypes;
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

    public List<String> getComponentTypes() {
        return componentTypes;
    }

    public void setComponentTypes(List<String> componentTypes) {
        this.componentTypes = componentTypes;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Adds a single component type to the list.
     */
    public void addComponentType(String componentType) {
        if (this.componentTypes == null) {
            this.componentTypes = new java.util.ArrayList<>();
        }
        this.componentTypes.add(componentType);
    }

    @Override
    public String toString() {
        return "SpringComponentRequest{" +
                "repositoryName='" + repositoryName + '\'' +
                ", branch='" + branch + '\'' +
                ", componentTypes=" + componentTypes +
                ", packageName='" + packageName + '\'' +
                '}';
    }
}