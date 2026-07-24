package com.projectiq.mcp.client.service;

import com.projectiq.mcp.client.dto.BuildContextRequest;
import com.projectiq.mcp.client.dto.ClassInfo;
import com.projectiq.mcp.client.dto.ContextBuildError;
import com.projectiq.mcp.client.dto.DependencyInfo;
import com.projectiq.mcp.client.dto.DevelopmentContext;
import com.projectiq.mcp.client.dto.MethodInfo;
import com.projectiq.mcp.client.dto.RelatedFile;
import com.projectiq.mcp.client.dto.RepositoryContext;
import com.projectiq.mcp.client.dto.RestEndpointInfo;
import com.projectiq.mcp.client.dto.SpringComponentInfo;
import com.projectiq.mcp.client.exception.IndexerClientException;
import com.projectiq.mcp.client.exception.IndexerConnectionException;
import com.projectiq.mcp.client.exception.IndexerHttpException;
import com.projectiq.mcp.client.exception.IndexerTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Service responsible for creating a development-focused context optimized
 * for implementation tasks. Reuses {@link RepositoryContextBuilderService}
 * and organizes the information for AI coding agents, removing redundant
 * information and ensuring deterministic output.
 */
@Service
public class DevelopmentContextService {

    private static final Logger logger = LoggerFactory.getLogger(DevelopmentContextService.class);

    private final RepositoryContextBuilderService contextBuilderService;

    public DevelopmentContextService(RepositoryContextBuilderService contextBuilderService) {
        this.contextBuilderService = contextBuilderService;
    }

    /**
     * Creates a development-focused context for the given task.
     *
     * @param request the build context request containing task and repository info
     * @return the development-optimized context
     * @throws IndexerConnectionException if the indexer is unreachable
     * @throws IndexerTimeoutException    if the request times out
     * @throws IndexerHttpException       if an HTTP error occurs
     * @throws IndexerClientException     if a client error occurs
     */
    public DevelopmentContext createDevelopmentContext(BuildContextRequest request) {
        logger.info("Creating development context for task: {} in repository: {}",
                request.getTask(), request.getRepositoryName());

        RepositoryContext repositoryContext = contextBuilderService.buildContext(request);
        return transformToDevelopmentContext(repositoryContext);
    }

    /**
     * Transforms a {@link RepositoryContext} into a development-optimized
     * {@link DevelopmentContext} by selecting relevant sections, removing
     * redundant information, sorting deterministically, and stripping
     * statistics/search results that are not needed for implementation.
     */
    private DevelopmentContext transformToDevelopmentContext(RepositoryContext repoContext) {
        DevelopmentContext devContext = new DevelopmentContext();

        // Copy basic metadata
        devContext.setTask(repoContext.getTask());
        devContext.setRepositoryName(repoContext.getRepositoryName());
        devContext.setBranch(repoContext.getBranch());
        devContext.setBuildTimestamp(Instant.now().toString());

        // Copy repository summary
        devContext.setRepositorySummary(repoContext.getRepositorySummary());

        // Transform and sort classes (exclude search results, use relevant classes)
        devContext.setRelevantClasses(sortClasses(repoContext.getClasses()));

        // Transform and sort methods
        devContext.setRelevantMethods(sortMethods(repoContext.getMethods()));

        // Transform and sort Spring components
        devContext.setSpringComponents(sortSpringComponents(repoContext.getSpringComponents()));

        // Transform and sort REST APIs
        devContext.setRestApis(sortRestApis(repoContext.getRestApis()));

        // Transform and sort dependencies
        devContext.setDependencies(sortDependencies(repoContext.getDependencies()));

        // Transform and sort related files
        devContext.setRelatedFiles(sortRelatedFiles(repoContext.getRelatedFiles()));

        // Copy errors
        if (repoContext.hasErrors()) {
            devContext.setErrors(new ArrayList<>(repoContext.getErrors()));
        }

        logger.debug("Development context created: {}", devContext);
        return devContext;
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