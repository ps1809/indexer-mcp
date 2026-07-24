package com.projectiq.mcp.client.dto;

import java.util.Arrays;
import java.util.List;

/**
 * Request DTO for searching classes in ProjectIQ Indexer.
 */
public class ClassRequest {

    private String repositoryName;
    private String className;
    private String packageName;
    private List<String> classTypes;
    private String branch;

    public ClassRequest() {
    }

    public ClassRequest(String repositoryName, String className) {
        this.repositoryName = repositoryName;
        this.className = className;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<String> getClassTypes() {
        return classTypes;
    }

    public void setClassTypes(List<String> classTypes) {
        this.classTypes = classTypes;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    /**
     * Adds a single class type to the list.
     * If the current list is immutable, it will be replaced with a mutable copy.
     */
    public void addClassType(String classType) {
        if (this.classTypes == null) {
            this.classTypes = new java.util.ArrayList<>();
        } else if (!(this.classTypes instanceof java.util.ArrayList)) {
            this.classTypes = new java.util.ArrayList<>(this.classTypes);
        }
        this.classTypes.add(classType);
    }

    @Override
    public String toString() {
        return "ClassRequest{" +
                "repositoryName='" + repositoryName + '\'' +
                ", className='" + className + '\'' +
                ", packageName='" + packageName + '\'' +
                ", classTypes=" + classTypes +
                ", branch='" + branch + '\'' +
                '}';
    }
}