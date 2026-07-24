package com.projectiq.mcp.client.dto;

/**
 * DTO representing contributor statistics for a repository.
 */
public class ContributorStats {

    private String author;
    private long commitCount;
    private String lastActiveDate;

    public ContributorStats() {
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getCommitCount() {
        return commitCount;
    }

    public void setCommitCount(long commitCount) {
        this.commitCount = commitCount;
    }

    public String getLastActiveDate() {
        return lastActiveDate;
    }

    public void setLastActiveDate(String lastActiveDate) {
        this.lastActiveDate = lastActiveDate;
    }

    @Override
    public String toString() {
        return "ContributorStats{" +
                "author='" + author + '\'' +
                ", commitCount=" + commitCount +
                ", lastActiveDate='" + lastActiveDate + '\'' +
                '}';
    }
}