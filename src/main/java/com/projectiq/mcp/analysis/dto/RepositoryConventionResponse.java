package com.projectiq.mcp.analysis.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Response DTO containing deterministic repository convention analysis.
 * Provides detailed insights into naming conventions, package organization,
 * architectural patterns, annotation usage, REST API conventions, testing
 * conventions, and project-specific observations.
 *
 * <p>All collections use stable ordering. No duplicate entries are produced.
 * This DTO is serialized to JSON for the MCP tool response.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RepositoryConventionResponse {

    private String repositoryName;
    private String branch;
    private String repositoryOverview;
    private NamingConventions namingConventions;
    private PackageConventions packageConventions;
    private ArchitecturalConventions architecturalConventions;
    private AnnotationConventions annotationConventions;
    private RestApiConventions restApiConventions;
    private TestingConventions testingConventions;
    private List<String> projectSpecificObservations;
    private String confidenceLevel;

    public RepositoryConventionResponse() {
        this.projectSpecificObservations = new ArrayList<>();
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

    public String getRepositoryOverview() {
        return repositoryOverview;
    }

    public void setRepositoryOverview(String repositoryOverview) {
        this.repositoryOverview = repositoryOverview;
    }

    public NamingConventions getNamingConventions() {
        return namingConventions;
    }

    public void setNamingConventions(NamingConventions namingConventions) {
        this.namingConventions = namingConventions;
    }

    public PackageConventions getPackageConventions() {
        return packageConventions;
    }

    public void setPackageConventions(PackageConventions packageConventions) {
        this.packageConventions = packageConventions;
    }

    public ArchitecturalConventions getArchitecturalConventions() {
        return architecturalConventions;
    }

    public void setArchitecturalConventions(ArchitecturalConventions architecturalConventions) {
        this.architecturalConventions = architecturalConventions;
    }

    public AnnotationConventions getAnnotationConventions() {
        return annotationConventions;
    }

    public void setAnnotationConventions(AnnotationConventions annotationConventions) {
        this.annotationConventions = annotationConventions;
    }

    public RestApiConventions getRestApiConventions() {
        return restApiConventions;
    }

    public void setRestApiConventions(RestApiConventions restApiConventions) {
        this.restApiConventions = restApiConventions;
    }

    public TestingConventions getTestingConventions() {
        return testingConventions;
    }

    public void setTestingConventions(TestingConventions testingConventions) {
        this.testingConventions = testingConventions;
    }

    public List<String> getProjectSpecificObservations() {
        return projectSpecificObservations;
    }

    public void setProjectSpecificObservations(List<String> projectSpecificObservations) {
        this.projectSpecificObservations = projectSpecificObservations != null
                ? new ArrayList<>(projectSpecificObservations) : new ArrayList<>();
    }

    public String getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(String confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }

    // --- Nested DTOs ---

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class NamingConventions {
        private String packageNamingConvention;
        private String classNamingConvention;
        private String methodNamingConvention;
        private String dtoNamingPattern;
        private String entityNamingPattern;
        private String serviceNamingPattern;
        private String repositoryNamingPattern;
        private String controllerNamingPattern;
        private String testNamingConvention;

        public NamingConventions() {
        }

        public String getPackageNamingConvention() {
            return packageNamingConvention;
        }

        public void setPackageNamingConvention(String packageNamingConvention) {
            this.packageNamingConvention = packageNamingConvention;
        }

        public String getClassNamingConvention() {
            return classNamingConvention;
        }

        public void setClassNamingConvention(String classNamingConvention) {
            this.classNamingConvention = classNamingConvention;
        }

        public String getMethodNamingConvention() {
            return methodNamingConvention;
        }

        public void setMethodNamingConvention(String methodNamingConvention) {
            this.methodNamingConvention = methodNamingConvention;
        }

        public String getDtoNamingPattern() {
            return dtoNamingPattern;
        }

        public void setDtoNamingPattern(String dtoNamingPattern) {
            this.dtoNamingPattern = dtoNamingPattern;
        }

        public String getEntityNamingPattern() {
            return entityNamingPattern;
        }

        public void setEntityNamingPattern(String entityNamingPattern) {
            this.entityNamingPattern = entityNamingPattern;
        }

        public String getServiceNamingPattern() {
            return serviceNamingPattern;
        }

        public void setServiceNamingPattern(String serviceNamingPattern) {
            this.serviceNamingPattern = serviceNamingPattern;
        }

        public String getRepositoryNamingPattern() {
            return repositoryNamingPattern;
        }

        public void setRepositoryNamingPattern(String repositoryNamingPattern) {
            this.repositoryNamingPattern = repositoryNamingPattern;
        }

        public String getControllerNamingPattern() {
            return controllerNamingPattern;
        }

        public void setControllerNamingPattern(String controllerNamingPattern) {
            this.controllerNamingPattern = controllerNamingPattern;
        }

        public String getTestNamingConvention() {
            return testNamingConvention;
        }

        public void setTestNamingConvention(String testNamingConvention) {
            this.testNamingConvention = testNamingConvention;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PackageConventions {
        private String moduleOrganization;
        private String packageNamingStyle;
        private String layerPackageConvention;
        private List<String> detectedPackages;

        public PackageConventions() {
            this.detectedPackages = new ArrayList<>();
        }

        public String getModuleOrganization() {
            return moduleOrganization;
        }

        public void setModuleOrganization(String moduleOrganization) {
            this.moduleOrganization = moduleOrganization;
        }

        public String getPackageNamingStyle() {
            return packageNamingStyle;
        }

        public void setPackageNamingStyle(String packageNamingStyle) {
            this.packageNamingStyle = packageNamingStyle;
        }

        public String getLayerPackageConvention() {
            return layerPackageConvention;
        }

        public void setLayerPackageConvention(String layerPackageConvention) {
            this.layerPackageConvention = layerPackageConvention;
        }

        public List<String> getDetectedPackages() {
            return detectedPackages;
        }

        public void setDetectedPackages(List<String> detectedPackages) {
            this.detectedPackages = detectedPackages != null
                    ? new ArrayList<>(detectedPackages) : new ArrayList<>();
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ArchitecturalConventions {
        private String architecturalStyle;
        private List<String> detectedLayers;
        private String configurationClassOrganization;

        public ArchitecturalConventions() {
            this.detectedLayers = new ArrayList<>();
        }

        public String getArchitecturalStyle() {
            return architecturalStyle;
        }

        public void setArchitecturalStyle(String architecturalStyle) {
            this.architecturalStyle = architecturalStyle;
        }

        public List<String> getDetectedLayers() {
            return detectedLayers;
        }

        public void setDetectedLayers(List<String> detectedLayers) {
            this.detectedLayers = detectedLayers != null
                    ? new ArrayList<>(detectedLayers) : new ArrayList<>();
        }

        public String getConfigurationClassOrganization() {
            return configurationClassOrganization;
        }

        public void setConfigurationClassOrganization(String configurationClassOrganization) {
            this.configurationClassOrganization = configurationClassOrganization;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AnnotationConventions {
        private List<String> commonAnnotations;

        public AnnotationConventions() {
            this.commonAnnotations = new ArrayList<>();
        }

        public List<String> getCommonAnnotations() {
            return commonAnnotations;
        }

        public void setCommonAnnotations(List<String> commonAnnotations) {
            this.commonAnnotations = commonAnnotations != null
                    ? new ArrayList<>(commonAnnotations) : new ArrayList<>();
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RestApiConventions {
        private String endpointNamingStyle;
        private String httpMethodUsage;

        public RestApiConventions() {
        }

        public String getEndpointNamingStyle() {
            return endpointNamingStyle;
        }

        public void setEndpointNamingStyle(String endpointNamingStyle) {
            this.endpointNamingStyle = endpointNamingStyle;
        }

        public String getHttpMethodUsage() {
            return httpMethodUsage;
        }

        public void setHttpMethodUsage(String httpMethodUsage) {
            this.httpMethodUsage = httpMethodUsage;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TestingConventions {
        private String testFramework;
        private String testNamingStyle;
        private String testLocation;

        public TestingConventions() {
        }

        public String getTestFramework() {
            return testFramework;
        }

        public void setTestFramework(String testFramework) {
            this.testFramework = testFramework;
        }

        public String getTestNamingStyle() {
            return testNamingStyle;
        }

        public void setTestNamingStyle(String testNamingStyle) {
            this.testNamingStyle = testNamingStyle;
        }

        public String getTestLocation() {
            return testLocation;
        }

        public void setTestLocation(String testLocation) {
            this.testLocation = testLocation;
        }
    }
}