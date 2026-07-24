package com.projectiq.mcp.client.dto;

import java.util.List;

/**
 * DTO representing Java class metadata from ProjectIQ Indexer.
 */
public class ClassInfo {

    private String packageName;
    private String className;
    private String fullyQualifiedName;
    private ClassType classType;
    private String visibility;
    private String parentClass;
    private List<String> implementedInterfaces;
    private List<String> annotations;
    private String sourceFileLocation;

    public ClassInfo() {
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

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    public void setFullyQualifiedName(String fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
    }

    public ClassType getClassType() {
        return classType;
    }

    public void setClassType(ClassType classType) {
        this.classType = classType;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getParentClass() {
        return parentClass;
    }

    public void setParentClass(String parentClass) {
        this.parentClass = parentClass;
    }

    public List<String> getImplementedInterfaces() {
        return implementedInterfaces;
    }

    public void setImplementedInterfaces(List<String> implementedInterfaces) {
        this.implementedInterfaces = implementedInterfaces;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<String> annotations) {
        this.annotations = annotations;
    }

    public String getSourceFileLocation() {
        return sourceFileLocation;
    }

    public void setSourceFileLocation(String sourceFileLocation) {
        this.sourceFileLocation = sourceFileLocation;
    }

    @Override
    public String toString() {
        return "ClassInfo{" +
                "packageName='" + packageName + '\'' +
                ", className='" + className + '\'' +
                ", fullyQualifiedName='" + fullyQualifiedName + '\'' +
                ", classType=" + classType +
                ", visibility='" + visibility + '\'' +
                ", parentClass='" + parentClass + '\'' +
                ", implementedInterfaces=" + implementedInterfaces +
                ", annotations=" + annotations +
                ", sourceFileLocation='" + sourceFileLocation + '\'' +
                '}';
    }
}