package com.projectiq.mcp.client.dto;

/**
 * DTO representing a single Spring component found in the Indexer.
 */
public class SpringComponentInfo {

    private String name;
    private String componentType;
    private String packageName;
    private String className;
    private String filePath;
    private Integer lineNumber;
    private String description;

    public SpringComponentInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "SpringComponentInfo{" +
                "name='" + name + '\'' +
                ", componentType='" + componentType + '\'' +
                ", packageName='" + packageName + '\'' +
                ", className='" + className + '\'' +
                ", filePath='" + filePath + '\'' +
                ", lineNumber=" + lineNumber +
                ", description='" + description + '\'' +
                '}';
    }
}