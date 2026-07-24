package com.projectiq.mcp.client.dto;

import java.util.Arrays;
import java.util.List;

/**
 * Request DTO for searching methods in ProjectIQ Indexer.
 */
public class MethodRequest {

    private String repositoryName;
    private String methodName;
    private String packageName;
    private List<String> methodTypes;
    private String branch;

    public MethodRequest() {
    }

    public MethodRequest(String repositoryName, String methodName) {
        this.repositoryName = repositoryName;
        this.methodName = methodName;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<String> getMethodTypes() {
        return methodTypes;
    }

    public void setMethodTypes(List<String> methodTypes) {
        this.methodTypes = methodTypes;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    /**
     * Adds a single method type to the list.
     * If the current list is immutable, it will be replaced with a mutable copy.
     */
    public void addMethodType(String methodType) {
        if (this.methodTypes == null) {
            this.methodTypes = new java.util.ArrayList<>();
        } else if (!(this.methodTypes instanceof java.util.ArrayList)) {
            this.methodTypes = new java.util.ArrayList<>(this.methodTypes);
        }
        this.methodTypes.add(methodType);
    }

    @Override
    public String toString() {
        return "MethodRequest{" +
                "repositoryName='" + repositoryName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", packageName='" + packageName + '\'' +
                ", methodTypes=" + methodTypes +
                ", branch='" + branch + '\'' +
                '}';
    }
}