package com.projectiq.mcp.client.dto;

/**
 * DTO representing file type statistics for a repository.
 */
public class FileTypeStats {

    private String fileType;
    private long fileCount;
    private long totalLinesOfCode;

    public FileTypeStats() {
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
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

    @Override
    public String toString() {
        return "FileTypeStats{" +
                "fileType='" + fileType + '\'' +
                ", fileCount=" + fileCount +
                ", totalLinesOfCode=" + totalLinesOfCode +
                '}';
    }
}