package com.projectiq.mcp.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Response DTO representing the repository summary from ProjectIQ Indexer.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepositorySummaryResponse {

    private String repositoryName;
    private String branch;
    private String status;
    private long commitCount;
    private long packageCount;
    private long classCount;
    private long methodCount;
    private long fileCount;
    private String lastIndexedDate;
    private List<PackageSummary> packages;

    public RepositorySummaryResponse() {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCommitCount() {
        return commitCount;
    }

    public void setCommitCount(long commitCount) {
        this.commitCount = commitCount;
    }

    public long getPackageCount() {
        return packageCount;
    }

    public void setPackageCount(long packageCount) {
        this.packageCount = packageCount;
    }

    public long getClassCount() {
        return classCount;
    }

    public void setClassCount(long classCount) {
        this.classCount = classCount;
    }

    public long getMethodCount() {
        return methodCount;
    }

    public void setMethodCount(long methodCount) {
        this.methodCount = methodCount;
    }

    public long getFileCount() {
        return fileCount;
    }

    public void setFileCount(long fileCount) {
        this.fileCount = fileCount;
    }

    public String getLastIndexedDate() {
        return lastIndexedDate;
    }

    public void setLastIndexedDate(String lastIndexedDate) {
        this.lastIndexedDate = lastIndexedDate;
    }

    public List<PackageSummary> getPackages() {
        return packages;
    }

    public void setPackages(List<PackageSummary> packages) {
        this.packages = packages;
    }

    public boolean isIndexed() {
        return "INDEXED".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "RepositorySummaryResponse{" +
                "repositoryName='" + repositoryName + '\'' +
                ", branch='" + branch + '\'' +
                ", status='" + status + '\'' +
                ", commitCount=" + commitCount +
                ", packageCount=" + packageCount +
                ", classCount=" + classCount +
                ", methodCount=" + methodCount +
                ", fileCount=" + fileCount +
                ", lastIndexedDate='" + lastIndexedDate + '\'' +
                '}';
    }
}