package com.projectiq.mcp.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * DTO representing a package summary within a repository.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PackageSummary {

    private String packageName;
    private long classCount;
    private long methodCount;
    private List<ClassSummary> classes;

    public PackageSummary() {
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
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

    public List<ClassSummary> getClasses() {
        return classes;
    }

    public void setClasses(List<ClassSummary> classes) {
        this.classes = classes;
    }

    @Override
    public String toString() {
        return "PackageSummary{" +
                "packageName='" + packageName + '\'' +
                ", classCount=" + classCount +
                ", methodCount=" + methodCount +
                '}';
    }
}