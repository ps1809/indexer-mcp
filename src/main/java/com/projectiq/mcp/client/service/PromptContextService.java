package com.projectiq.mcp.client.service;

import com.projectiq.mcp.client.dto.ClassInfo;
import com.projectiq.mcp.client.dto.ContextBuildError;
import com.projectiq.mcp.client.dto.DependencyInfo;
import com.projectiq.mcp.client.dto.DevelopmentContext;
import com.projectiq.mcp.client.dto.MethodInfo;
import com.projectiq.mcp.client.dto.PromptContext;
import com.projectiq.mcp.client.dto.PromptContext.RepositoryConventionsInfo;
import com.projectiq.mcp.client.dto.PromptContext.RepositorySummaryInfo;
import com.projectiq.mcp.client.dto.RelatedFile;
import com.projectiq.mcp.client.dto.RepositorySummaryResponse;
import com.projectiq.mcp.client.dto.RestEndpointInfo;
import com.projectiq.mcp.client.dto.SpringComponentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service responsible for transforming {@link DevelopmentContext} into an
 * AI-ready structured {@link PromptContext}. Preserves only relevant repository
 * information, removes duplicate and unnecessary data, and produces
 * deterministic output optimized for downstream AI consumption.
 */
@Service
public class PromptContextService {

    private static final Logger logger = LoggerFactory.getLogger(PromptContextService.class);

    /**
     * Transforms a {@link DevelopmentContext} into a concise, structured
     * {@link PromptContext} suitable for AI coding agents.
     *
     * @param developmentContext the development context to transform
     * @return a structured prompt context with deterministic ordering
     */
    public PromptContext createPromptContext(DevelopmentContext developmentContext) {
        logger.info("Creating prompt context for task: {}", developmentContext.getTask());

        PromptContext promptContext = new PromptContext();

        // Copy basic metadata
        promptContext.setTask(developmentContext.getTask());
        promptContext.setRepositoryName(developmentContext.getRepositoryName());
        promptContext.setBranch(developmentContext.getBranch());
        promptContext.setBuildTimestamp(developmentContext.getBuildTimestamp());

        // Transform repository summary
        promptContext.setRepositorySummary(transformRepositorySummary(developmentContext.getRepositorySummary()));

        // Extract and sort relevant packages (deduplicated)
        promptContext.setRelevantPackages(extractRelevantPackages(developmentContext));

        // Copy and sort relevant classes
        promptContext.setRelevantClasses(sortClasses(developmentContext.getRelevantClasses()));

        // Copy and sort relevant methods
        promptContext.setRelevantMethods(sortMethods(developmentContext.getRelevantMethods()));

        // Copy and sort Spring components
        promptContext.setSpringComponents(sortSpringComponents(developmentContext.getSpringComponents()));

        // Copy and sort REST APIs
        promptContext.setRestApis(sortRestApis(developmentContext.getRestApis()));

        // Copy and sort related files
        promptContext.setRelatedFiles(sortRelatedFiles(developmentContext.getRelatedFiles()));

        // Copy and sort required dependencies
        promptContext.setRequiredDependencies(sortDependencies(developmentContext.getDependencies()));

        // Extract repository conventions
        promptContext.setRepositoryConventions(extractRepositoryConventions(developmentContext));

        // Copy errors
        if (developmentContext.hasErrors()) {
            promptContext.setErrors(new ArrayList<>(developmentContext.getErrors()));
        }

        logger.debug("Prompt context created: {}", promptContext);
        return promptContext;
    }

    /**
     * Transforms a {@link RepositorySummaryResponse} into a simplified
     * {@link RepositorySummaryInfo} for AI consumption.
     */
    private RepositorySummaryInfo transformRepositorySummary(RepositorySummaryResponse summary) {
        if (summary == null) {
            return null;
        }
        RepositorySummaryInfo info = new RepositorySummaryInfo();
        info.setName(summary.getRepositoryName());
        info.setBranch(summary.getBranch());
        info.setDescription(summary.getStatus());
        info.setFileCount(summary.getFileCount());
        info.setClassCount(summary.getClassCount());
        info.setMethodCount(summary.getMethodCount());
        info.setCommitCount(summary.getCommitCount());
        info.setPackageCount(summary.getPackageCount());
        return info;
    }

    /**
     * Extracts unique package names from classes in the development context.
     * Packages are sorted deterministically.
     */
    private List<String> extractRelevantPackages(DevelopmentContext context) {
        Set<String> packageNames = new HashSet<>();

        // Extract packages from classes
        if (context.getRelevantClasses() != null) {
            for (ClassInfo ci : context.getRelevantClasses()) {
                String packageName = extractPackageName(ci.getFullyQualifiedName());
                if (packageName != null && !packageName.isEmpty()) {
                    packageNames.add(packageName);
                }
            }
        }

        // Extract packages from methods
        if (context.getRelevantMethods() != null) {
            for (MethodInfo mi : context.getRelevantMethods()) {
                String packageName = extractPackageName(mi.getDeclaringClass());
                if (packageName != null && !packageName.isEmpty()) {
                    packageNames.add(packageName);
                }
            }
        }

        // Extract packages from Spring components
        if (context.getSpringComponents() != null) {
            for (SpringComponentInfo sci : context.getSpringComponents()) {
                String packageName = extractPackageName(sci.getClassName());
                if (packageName != null && !packageName.isEmpty()) {
                    packageNames.add(packageName);
                }
            }
        }

        // Extract packages from repository summary packages
        if (context.getRepositorySummary() != null && context.getRepositorySummary().getPackages() != null) {
            for (var pkg : context.getRepositorySummary().getPackages()) {
                if (pkg.getPackageName() != null && !pkg.getPackageName().isEmpty()) {
                    packageNames.add(pkg.getPackageName());
                }
            }
        }

        List<String> sortedPackages = new ArrayList<>(packageNames);
        sortedPackages.sort(Comparator.naturalOrder());
        return sortedPackages;
    }

    /**
     * Extracts the package name from a fully qualified class name.
     * For example, "com.example.UserController" returns "com.example".
     */
    private String extractPackageName(String fullyQualifiedName) {
        if (fullyQualifiedName == null || fullyQualifiedName.isEmpty()) {
            return null;
        }
        int lastDot = fullyQualifiedName.lastIndexOf('.');
        if (lastDot > 0) {
            return fullyQualifiedName.substring(0, lastDot);
        }
        return null;
    }

    /**
     * Extracts repository conventions from the development context.
     * Uses available metadata to infer conventions.
     */
    private RepositoryConventionsInfo extractRepositoryConventions(DevelopmentContext context) {
        RepositoryConventionsInfo conventions = new RepositoryConventionsInfo();

        // Infer package structure from classes
        if (context.getRelevantClasses() != null && !context.getRelevantClasses().isEmpty()) {
            String firstClass = context.getRelevantClasses().get(0).getFullyQualifiedName();
            String packageName = extractPackageName(firstClass);
            if (packageName != null) {
                conventions.setPackageStructure(packageName);
                // Infer naming conventions from package structure
                if (packageName.contains("controller") || packageName.contains("service")
                        || packageName.contains("repository") || packageName.contains("model")) {
                    conventions.setNamingConventions("Layered architecture with standard Spring conventions");
                } else {
                    conventions.setNamingConventions("Standard Java naming conventions");
                }
            }
        }

        // Infer framework version from dependencies
        if (context.getDependencies() != null) {
            for (DependencyInfo dep : context.getDependencies()) {
                if ("org.springframework.boot".equals(dep.getGroupId())) {
                    conventions.setFrameworkVersion("Spring Boot " + (dep.getVersion() != null ? dep.getVersion() : "3.x"));
                    break;
                }
            }
        }

        // Default build tool
        conventions.setBuildTool("Maven");

        // Default Java version
        conventions.setJavaVersion("21");

        return conventions;
    }

    /**
     * Sorts classes by fully qualified name for deterministic ordering.
     */
    private List<ClassInfo> sortClasses(List<ClassInfo> classes) {
        if (classes == null || classes.isEmpty()) {
            return new ArrayList<>();
        }
        List<ClassInfo> sorted = new ArrayList<>(classes);
        sorted.sort(Comparator.comparing(
                ClassInfo::getFullyQualifiedName,
                Comparator.nullsLast(Comparator.naturalOrder())
        ));
        return sorted;
    }

    /**
     * Sorts methods by declaring class and method name for deterministic ordering.
     */
    private List<MethodInfo> sortMethods(List<MethodInfo> methods) {
        if (methods == null || methods.isEmpty()) {
            return new ArrayList<>();
        }
        List<MethodInfo> sorted = new ArrayList<>(methods);
        sorted.sort(Comparator
                .comparing(MethodInfo::getDeclaringClass, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(MethodInfo::getMethodName, Comparator.nullsLast(Comparator.naturalOrder()))
        );
        return sorted;
    }

    /**
     * Sorts Spring components by name for deterministic ordering.
     */
    private List<SpringComponentInfo> sortSpringComponents(List<SpringComponentInfo> components) {
        if (components == null || components.isEmpty()) {
            return new ArrayList<>();
        }
        List<SpringComponentInfo> sorted = new ArrayList<>(components);
        sorted.sort(Comparator.comparing(
                SpringComponentInfo::getName,
                Comparator.nullsLast(Comparator.naturalOrder())
        ));
        return sorted;
    }

    /**
     * Sorts REST APIs by HTTP method and endpoint path for deterministic ordering.
     */
    private List<RestEndpointInfo> sortRestApis(List<RestEndpointInfo> apis) {
        if (apis == null || apis.isEmpty()) {
            return new ArrayList<>();
        }
        List<RestEndpointInfo> sorted = new ArrayList<>(apis);
        sorted.sort(Comparator
                .comparing(RestEndpointInfo::getHttpMethod, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(RestEndpointInfo::getEndpointPath, Comparator.nullsLast(Comparator.naturalOrder()))
        );
        return sorted;
    }

    /**
     * Sorts dependencies by groupId and artifactId for deterministic ordering.
     */
    private List<DependencyInfo> sortDependencies(List<DependencyInfo> dependencies) {
        if (dependencies == null || dependencies.isEmpty()) {
            return new ArrayList<>();
        }
        List<DependencyInfo> sorted = new ArrayList<>(dependencies);
        sorted.sort(Comparator
                .comparing(DependencyInfo::getGroupId, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(DependencyInfo::getArtifactId, Comparator.nullsLast(Comparator.naturalOrder()))
        );
        return sorted;
    }

    /**
     * Sorts related files by file path for deterministic ordering.
     */
    private List<RelatedFile> sortRelatedFiles(List<RelatedFile> files) {
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }
        List<RelatedFile> sorted = new ArrayList<>(files);
        sorted.sort(Comparator.comparing(
                RelatedFile::getFilePath,
                Comparator.nullsLast(Comparator.naturalOrder())
        ));
        return sorted;
    }
}