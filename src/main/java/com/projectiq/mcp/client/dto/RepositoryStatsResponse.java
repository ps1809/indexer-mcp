package com.projectiq.mcp.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Response DTO representing repository statistics from ProjectIQ Indexer.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepositoryStatsResponse {

    private String repositoryName;
    private String branch;
    private String status;
    private long commitCount;
    private long packageCount;
    private long classCount;
    private long methodCount;
    private long fileCount;
    private long totalLinesOfCode;
    private String lastIndexedDate;
    private List<ContributorStats> contributors;
    private List<FileTypeStats> fileTypeStats;

    public RepositoryStatsResponse() {
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

    public long getTotalLinesOfCode() {
        return totalLinesOfCode;
    }

    public void setTotalLinesOfCode(long totalLinesOfCode) {
        this.totalLinesOfCode = totalLinesOfCode;
    }

    public String getLastIndexedDate() {
        return lastIndexedDate;
    }

    public void setLastIndexedDate(String lastIndexedDate) {
        this.lastIndexedDate = lastIndexedDate;
    }

    public List<ContributorStats> getContributors() {
        return contributors;
    }

    public void setContributors(List<ContributorStats> contributors) {
        this.contributors = contributors;
    }

    public List<FileTypeStats> getFileTypeStats() {
        return fileTypeStats;
    }

    public void setFileTypeStats(List<FileTypeStats> fileTypeStats) {
        this.fileTypeStats = fileTypeStats;
    }

    public boolean isIndexed() {
        return "INDEXED".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "RepositoryStatsResponse{" +
                "repositoryName='" + repositoryName + '\'' +
                ", branch='" + branch + '\'' +
                ", status='" + status + '\'' +
                ", commitCount=" + commitCount +
                ", packageCount=" + packageCount +
                ", classCount=" + classCount +
                ", methodCount=" + methodCount +
                ", fileCount=" + fileCount +
                ", totalLinesOfCode=" + totalLinesOfCode +
                ", lastIndexedDate='" + lastIndexedDate + '\'' +
                '}';
    }
}