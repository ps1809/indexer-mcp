package com.projectiq.mcp.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO representing a class summary within a package.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassSummary {

    private String className;
    private String fullyQualifiedName;
    private long methodCount;
    private long fieldCount;
    private String superClass;
    private String[] interfaces;

    public ClassSummary() {
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    public void setFullyQualifiedName(String fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
    }

    public long getMethodCount() {
        return methodCount;
    }

    public void setMethodCount(long methodCount) {
        this.methodCount = methodCount;
    }

    public long getFieldCount() {
        return fieldCount;
    }

    public void setFieldCount(long fieldCount) {
        this.fieldCount = fieldCount;
    }

    public String getSuperClass() {
        return superClass;
    }

    public void setSuperClass(String superClass) {
        this.superClass = superClass;
    }

    public String[] getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(String[] interfaces) {
        this.interfaces = interfaces;
    }

    @Override
    public String toString() {
        return "ClassSummary{" +
                "className='" + className + '\'' +
                ", fullyQualifiedName='" + fullyQualifiedName + '\'' +
                ", methodCount=" + methodCount +
                ", fieldCount=" + fieldCount +
                '}';
    }
}