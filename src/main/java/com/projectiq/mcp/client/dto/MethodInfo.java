package com.projectiq.mcp.client.dto;

import java.util.List;

/**
 * DTO representing Java method metadata from ProjectIQ Indexer.
 */
public class MethodInfo {

    private String methodName;
    private String fullyQualifiedName;
    private String declaringClass;
    private String packageName;
    private String returnType;
    private List<MethodParameter> parameters;
    private String visibility;
    private Boolean staticFlag;
    private Boolean abstractFlag;
    private List<String> annotations;
    private String sourceFileLocation;

    public MethodInfo() {
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    public void setFullyQualifiedName(String fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
    }

    public String getDeclaringClass() {
        return declaringClass;
    }

    public void setDeclaringClass(String declaringClass) {
        this.declaringClass = declaringClass;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public List<MethodParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<MethodParameter> parameters) {
        this.parameters = parameters;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public Boolean isStatic() {
        return staticFlag;
    }

    public void setStaticFlag(Boolean staticFlag) {
        this.staticFlag = staticFlag;
    }

    public Boolean isAbstract() {
        return abstractFlag;
    }

    public void setAbstractFlag(Boolean abstractFlag) {
        this.abstractFlag = abstractFlag;
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
        return "MethodInfo{" +
                "methodName='" + methodName + '\'' +
                ", fullyQualifiedName='" + fullyQualifiedName + '\'' +
                ", declaringClass='" + declaringClass + '\'' +
                ", packageName='" + packageName + '\'' +
                ", returnType='" + returnType + '\'' +
                ", parameters=" + parameters +
                ", visibility='" + visibility + '\'' +
                ", staticFlag=" + staticFlag +
                ", abstractFlag=" + abstractFlag +
                ", annotations=" + annotations +
                ", sourceFileLocation='" + sourceFileLocation + '\'' +
                '}';
    }
}