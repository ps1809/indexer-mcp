package com.projectiq.mcp.client.dto;

import java.util.Arrays;
import java.util.List;

/**
 * Request DTO for searching REST API endpoints in ProjectIQ Indexer.
 */
public class RestApiRequest {

    private String repositoryName;
    private String branch;
    private String packageName;
    private List<String> httpMethods;

    public RestApiRequest() {
    }

    public RestApiRequest(String repositoryName, List<String> httpMethods) {
        this.repositoryName = repositoryName;
        this.httpMethods = httpMethods;
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

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<String> getHttpMethods() {
        return httpMethods;
    }

    public void setHttpMethods(List<String> httpMethods) {
        this.httpMethods = httpMethods;
    }

    /**
     * Adds a single HTTP method to the list.
     * If the current list is immutable, it will be replaced with a mutable copy.
     */
    public void addHttpMethod(String httpMethod) {
        if (this.httpMethods == null) {
            this.httpMethods = new java.util.ArrayList<>();
        } else if (!(this.httpMethods instanceof java.util.ArrayList)) {
            // Convert immutable list to mutable ArrayList
            this.httpMethods = new java.util.ArrayList<>(this.httpMethods);
        }
        this.httpMethods.add(httpMethod);
    }

    @Override
    public String toString() {
        return "RestApiRequest{" +
                "repositoryName='" + repositoryName + '\'' +
                ", branch='" + branch + '\'' +
                ", packageName='" + packageName + '\'' +
                ", httpMethods=" + httpMethods +
                '}';
    }
}