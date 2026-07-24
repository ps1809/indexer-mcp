package com.projectiq.mcp.client.service;

import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.dto.BuildContextRequest;
import com.projectiq.mcp.client.dto.ClassInfo;
import com.projectiq.mcp.client.dto.ClassRequest;
import com.projectiq.mcp.client.dto.ClassResponse;
import com.projectiq.mcp.client.dto.ContextBuildError;
import com.projectiq.mcp.client.dto.DependencyInfo;
import com.projectiq.mcp.client.dto.DependencyRequest;
import com.projectiq.mcp.client.dto.DependencyResponse;
import com.projectiq.mcp.client.dto.MethodInfo;
import com.projectiq.mcp.client.dto.MethodRequest;
import com.projectiq.mcp.client.dto.MethodResponse;
import com.projectiq.mcp.client.dto.RelatedFile;
import com.projectiq.mcp.client.dto.RelatedFileRequest;
import com.projectiq.mcp.client.dto.RelatedFileResponse;
import com.projectiq.mcp.client.dto.RepositoryContext;
import com.projectiq.mcp.client.dto.RepositoryStatsRequest;
import com.projectiq.mcp.client.dto.RepositoryStatsResponse;
import com.projectiq.mcp.client.dto.RepositorySummaryRequest;
import com.projectiq.mcp.client.dto.RepositorySummaryResponse;
import com.projectiq.mcp.client.dto.RestApiRequest;
import com.projectiq.mcp.client.dto.RestApiResponse;
import com.projectiq.mcp.client.dto.RestEndpointInfo;
import com.projectiq.mcp.client.dto.SearchCodeRequest;
import com.projectiq.mcp.client.dto.SearchCodeResponse;
import com.projectiq.mcp.client.dto.SearchResult;
import com.projectiq.mcp.client.dto.SpringComponentInfo;
import com.projectiq.mcp.client.dto.SpringComponentRequest;
import com.projectiq.mcp.client.dto.SpringComponentResponse;
import com.projectiq.mcp.client.exception.IndexerClientException;
import com.projectiq.mcp.client.exception.IndexerConnectionException;
import com.projectiq.mcp.client.exception.IndexerHttpException;
import com.projectiq.mcp.client.exception.IndexerTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service responsible for building a unified repository context by aggregating
 * information from multiple ProjectIQ Indexer endpoints.
 */
@Service
public class RepositoryContextBuilderService {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryContextBuilderService.class);

    private final IndexerRestClient indexerRestClient;

    public RepositoryContextBuilderService(IndexerRestClient indexerRestClient) {
        this.indexerRestClient = indexerRestClient;
    }

    /**
     * Builds a unified repository context for the given task.
     *
     * @param request the build context request containing task and repository info
     * @return the aggregated repository context
     */
    public RepositoryContext buildContext(BuildContextRequest request) {
        logger.info("Building context for task: {} in repository: {}", 
                request.getTask(), request.getRepositoryName());

        RepositoryContext context = new RepositoryContext();
        context.setTask(request.getTask());
        context.setRepositoryName(request.getRepositoryName());
        context.setBranch(request.getBranch());
        context.setBuildTimestamp(Instant.now().toString());

        // Extract keywords from task for searching
        String searchQuery = extractSearchQuery(request.getTask());

        // Fetch data from all endpoints, handling partial failures
        fetchRepositorySummary(context, request);
        fetchRepositoryStatistics(context, request);
        fetchSearchResults(context, request, searchQuery);
        fetchSpringComponents(context, request, searchQuery);
        fetchRestApis(context, request, searchQuery);
        fetchClasses(context, request, searchQuery);
        fetchMethods(context, request, searchQuery);
        fetchRelatedFiles(context, request, searchQuery);
        fetchDependencies(context, request, searchQuery);

        // Eliminate duplicates
        eliminateDuplicates(context);

        logger.info("Context built successfully: {}", context);
        return context;
    }

    /**
     * Extracts a search query from the task description.
     * Uses the task itself as the search query for MVP.
     */
    private String extractSearchQuery(String task) {
        if (task == null || task.isEmpty()) {
            return "";
        }
        // For MVP, use the task as-is; future versions can extract keywords
        return task;
    }

    private void fetchRepositorySummary(RepositoryContext context, BuildContextRequest request) {
        try {
            RepositorySummaryRequest summaryRequest = new RepositorySummaryRequest();
            summaryRequest.setRepositoryName(request.getRepositoryName());
            summaryRequest.setBranch(request.getBranch());

            RepositorySummaryResponse response = indexerRestClient.getRepositorySummary(summaryRequest);
            context.setRepositorySummary(response);
            logger.debug("Repository summary fetched successfully");
        } catch (IndexerConnectionException e) {
            addError(context, "repositorySummary", "INDEXER_UNREACHABLE", e.getMessage());
        } catch (IndexerTimeoutException e) {
            addError(context, "repositorySummary", "INDEXER_TIMEOUT", e.getMessage());
        } catch (IndexerHttpException e) {
            addError(context, "repositorySummary", "INDEXER_HTTP_ERROR", e.getMessage());
        } catch (IndexerClientException e) {
            addError(context, "repositorySummary", "INDEXER_ERROR", e.getMessage());
        } catch (Exception e) {
            addError(context, "repositorySummary", "INTERNAL_ERROR", e.getMessage());
        }
    }

    private void fetchRepositoryStatistics(RepositoryContext context, BuildContextRequest request) {
        try {
            RepositoryStatsRequest statsRequest = new RepositoryStatsRequest();
            statsRequest.setRepositoryName(request.getRepositoryName());
            statsRequest.setBranch(request.getBranch());

            RepositoryStatsResponse response = indexerRestClient.getRepositoryStatistics(statsRequest);
            context.setRepositoryStatistics(response);
            logger.debug("Repository statistics fetched successfully");
        } catch (IndexerConnectionException e) {
            addError(context, "repositoryStatistics", "INDEXER_UNREACHABLE", e.getMessage());
        } catch (IndexerTimeoutException e) {
            addError(context, "repositoryStatistics", "INDEXER_TIMEOUT", e.getMessage());
        } catch (IndexerHttpException e) {
            addError(context, "repositoryStatistics", "INDEXER_HTTP_ERROR", e.getMessage());
        } catch (IndexerClientException e) {
            addError(context, "repositoryStatistics", "INDEXER_ERROR", e.getMessage());
        } catch (Exception e) {
            addError(context, "repositoryStatistics", "INTERNAL_ERROR", e.getMessage());
        }
    }

    private void fetchSearchResults(RepositoryContext context, BuildContextRequest request, String searchQuery) {
        try {
            SearchCodeRequest searchRequest = new SearchCodeRequest();
            searchRequest.setRepositoryName(request.getRepositoryName());
            searchRequest.setBranch(request.getBranch());
            searchRequest.setQuery(searchQuery);

            SearchCodeResponse response = indexerRestClient.searchCode(searchRequest);
            if (response != null && response.getResults() != null) {
                context.setSearchResults(response.getResults());
            }
            logger.debug("Search results fetched successfully");
        } catch (IndexerConnectionException e) {
            addError(context, "searchCode", "INDEXER_UNREACHABLE", e.getMessage());
        } catch (IndexerTimeoutException e) {
            addError(context, "searchCode", "INDEXER_TIMEOUT", e.getMessage());
        } catch (IndexerHttpException e) {
            addError(context, "searchCode", "INDEXER_HTTP_ERROR", e.getMessage());
        } catch (IndexerClientException e) {
            addError(context, "searchCode", "INDEXER_ERROR", e.getMessage());
        } catch (Exception e) {
            addError(context, "searchCode", "INTERNAL_ERROR", e.getMessage());
        }
    }

    private void fetchSpringComponents(RepositoryContext context, BuildContextRequest request, String searchQuery) {
        try {
            SpringComponentRequest springRequest = new SpringComponentRequest();
            springRequest.setRepositoryName(request.getRepositoryName());
            springRequest.setBranch(request.getBranch());
            springRequest.setPackageName(searchQuery);

            SpringComponentResponse response = indexerRestClient.findSpringComponent(springRequest);
            if (response != null && response.getComponents() != null) {
                context.setSpringComponents(response.getComponents());
            }
            logger.debug("Spring components fetched successfully");
        } catch (IndexerConnectionException e) {
            addError(context, "springComponents", "INDEXER_UNREACHABLE", e.getMessage());
        } catch (IndexerTimeoutException e) {
            addError(context, "springComponents", "INDEXER_TIMEOUT", e.getMessage());
        } catch (IndexerHttpException e) {
            addError(context, "springComponents", "INDEXER_HTTP_ERROR", e.getMessage());
        } catch (IndexerClientException e) {
            addError(context, "springComponents", "INDEXER_ERROR", e.getMessage());
        } catch (Exception e) {
            addError(context, "springComponents", "INTERNAL_ERROR", e.getMessage());
        }
    }

    private void fetchRestApis(RepositoryContext context, BuildContextRequest request, String searchQuery) {
        try {
            RestApiRequest restRequest = new RestApiRequest();
            restRequest.setRepositoryName(request.getRepositoryName());
            restRequest.setBranch(request.getBranch());
            restRequest.setPackageName(searchQuery);

            RestApiResponse response = indexerRestClient.findRestApi(restRequest);
            if (response != null && response.getEndpoints() != null) {
                context.setRestApis(response.getEndpoints());
            }
            logger.debug("REST APIs fetched successfully");
        } catch (IndexerConnectionException e) {
            addError(context, "restApis", "INDEXER_UNREACHABLE", e.getMessage());
        } catch (IndexerTimeoutException e) {
            addError(context, "restApis", "INDEXER_TIMEOUT", e.getMessage());
        } catch (IndexerHttpException e) {
            addError(context, "restApis", "INDEXER_HTTP_ERROR", e.getMessage());
        } catch (IndexerClientException e) {
            addError(context, "restApis", "INDEXER_ERROR", e.getMessage());
        } catch (Exception e) {
            addError(context, "restApis", "INTERNAL_ERROR", e.getMessage());
        }
    }

    private void fetchClasses(RepositoryContext context, BuildContextRequest request, String searchQuery) {
        try {
            ClassRequest classRequest = new ClassRequest();
            classRequest.setRepositoryName(request.getRepositoryName());
            classRequest.setBranch(request.getBranch());
            classRequest.setClassName(searchQuery);

            ClassResponse response = indexerRestClient.findClass(classRequest);
            if (response != null && response.getClasses() != null) {
                context.setClasses(response.getClasses());
            }
            logger.debug("Classes fetched successfully");
        } catch (IndexerConnectionException e) {
            addError(context, "classes", "INDEXER_UNREACHABLE", e.getMessage());
        } catch (IndexerTimeoutException e) {
            addError(context, "classes", "INDEXER_TIMEOUT", e.getMessage());
        } catch (IndexerHttpException e) {
            addError(context, "classes", "INDEXER_HTTP_ERROR", e.getMessage());
        } catch (IndexerClientException e) {
            addError(context, "classes", "INDEXER_ERROR", e.getMessage());
        } catch (Exception e) {
            addError(context, "classes", "INTERNAL_ERROR", e.getMessage());
        }
    }

    private void fetchMethods(RepositoryContext context, BuildContextRequest request, String searchQuery) {
        try {
            MethodRequest methodRequest = new MethodRequest();
            methodRequest.setRepositoryName(request.getRepositoryName());
            methodRequest.setBranch(request.getBranch());
            methodRequest.setMethodName(searchQuery);

            MethodResponse response = indexerRestClient.findMethod(methodRequest);
            if (response != null && response.getMethods() != null) {
                context.setMethods(response.getMethods());
            }
            logger.debug("Methods fetched successfully");
        } catch (IndexerConnectionException e) {
            addError(context, "methods", "INDEXER_UNREACHABLE", e.getMessage());
        } catch (IndexerTimeoutException e) {
            addError(context, "methods", "INDEXER_TIMEOUT", e.getMessage());
        } catch (IndexerHttpException e) {
            addError(context, "methods", "INDEXER_HTTP_ERROR", e.getMessage());
        } catch (IndexerClientException e) {
            addError(context, "methods", "INDEXER_ERROR", e.getMessage());
        } catch (Exception e) {
            addError(context, "methods", "INTERNAL_ERROR", e.getMessage());
        }
    }

    private void fetchRelatedFiles(RepositoryContext context, BuildContextRequest request, String searchQuery) {
        try {
            RelatedFileRequest relatedRequest = new RelatedFileRequest();
            relatedRequest.setRepositoryName(request.getRepositoryName());
            relatedRequest.setBranch(request.getBranch());
            relatedRequest.setSearchTarget(searchQuery);
            relatedRequest.setTargetType(RelatedFileRequest.SearchTargetType.CLASS);

            RelatedFileResponse response = indexerRestClient.findRelatedFiles(relatedRequest);
            if (response != null && response.getRelatedFiles() != null) {
                context.setRelatedFiles(response.getRelatedFiles());
            }
            logger.debug("Related files fetched successfully");
        } catch (IndexerConnectionException e) {
            addError(context, "relatedFiles", "INDEXER_UNREACHABLE", e.getMessage());
        } catch (IndexerTimeoutException e) {
            addError(context, "relatedFiles", "INDEXER_TIMEOUT", e.getMessage());
        } catch (IndexerHttpException e) {
            addError(context, "relatedFiles", "INDEXER_HTTP_ERROR", e.getMessage());
        } catch (IndexerClientException e) {
            addError(context, "relatedFiles", "INDEXER_ERROR", e.getMessage());
        } catch (Exception e) {
            addError(context, "relatedFiles", "INTERNAL_ERROR", e.getMessage());
        }
    }

    private void fetchDependencies(RepositoryContext context, BuildContextRequest request, String searchQuery) {
        try {
            DependencyRequest depRequest = new DependencyRequest();
            depRequest.setRepositoryName(request.getRepositoryName());
            depRequest.setBranch(request.getBranch());
            depRequest.setSearchPattern(searchQuery);

            DependencyResponse response = indexerRestClient.findDependency(depRequest);
            if (response != null && response.getDependencies() != null) {
                context.setDependencies(response.getDependencies());
            }
            logger.debug("Dependencies fetched successfully");
        } catch (IndexerConnectionException e) {
            addError(context, "dependencies", "INDEXER_UNREACHABLE", e.getMessage());
        } catch (IndexerTimeoutException e) {
            addError(context, "dependencies", "INDEXER_TIMEOUT", e.getMessage());
        } catch (IndexerHttpException e) {
            addError(context, "dependencies", "INDEXER_HTTP_ERROR", e.getMessage());
        } catch (IndexerClientException e) {
            addError(context, "dependencies", "INDEXER_ERROR", e.getMessage());
        } catch (Exception e) {
            addError(context, "dependencies", "INTERNAL_ERROR", e.getMessage());
        }
    }

    /**
     * Eliminates duplicate entries from the context.
     */
    private void eliminateDuplicates(RepositoryContext context) {
        context.setSearchResults(deduplicateSearchResults(context.getSearchResults()));
        context.setSpringComponents(deduplicateSpringComponents(context.getSpringComponents()));
        context.setRestApis(deduplicateRestApis(context.getRestApis()));
        context.setClasses(deduplicateClasses(context.getClasses()));
        context.setMethods(deduplicateMethods(context.getMethods()));
        context.setRelatedFiles(deduplicateRelatedFiles(context.getRelatedFiles()));
        context.setDependencies(deduplicateDependencies(context.getDependencies()));
    }

    private List<SearchResult> deduplicateSearchResults(List<SearchResult> results) {
        if (results == null || results.isEmpty()) {
            return new ArrayList<>();
        }
        Set<String> seen = new HashSet<>();
        List<SearchResult> unique = new ArrayList<>();
        for (SearchResult result : results) {
            String key = result.getFilePath() + ":" + result.getLineNumber();
            if (seen.add(key)) {
                unique.add(result);
            }
        }
        return unique;
    }

    private List<SpringComponentInfo> deduplicateSpringComponents(List<SpringComponentInfo> components) {
        if (components == null || components.isEmpty()) {
            return new ArrayList<>();
        }
        Set<String> seen = new HashSet<>();
        List<SpringComponentInfo> unique = new ArrayList<>();
        for (SpringComponentInfo component : components) {
            String key = component.getName();
            if (key != null && seen.add(key)) {
                unique.add(component);
            }
        }
        return unique;
    }

    private List<RestEndpointInfo> deduplicateRestApis(List<RestEndpointInfo> apis) {
        if (apis == null || apis.isEmpty()) {
            return new ArrayList<>();
        }
        Set<String> seen = new HashSet<>();
        List<RestEndpointInfo> unique = new ArrayList<>();
        for (RestEndpointInfo api : apis) {
            String key = api.getHttpMethod() + ":" + api.getEndpointPath();
            if (seen.add(key)) {
                unique.add(api);
            }
        }
        return unique;
    }

    private List<ClassInfo> deduplicateClasses(List<ClassInfo> classes) {
        if (classes == null || classes.isEmpty()) {
            return new ArrayList<>();
        }
        Set<String> seen = new HashSet<>();
        List<ClassInfo> unique = new ArrayList<>();
        for (ClassInfo classInfo : classes) {
            String key = classInfo.getFullyQualifiedName();
            if (key != null && seen.add(key)) {
                unique.add(classInfo);
            }
        }
        return unique;
    }

    private List<MethodInfo> deduplicateMethods(List<MethodInfo> methods) {
        if (methods == null || methods.isEmpty()) {
            return new ArrayList<>();
        }
        Set<String> seen = new HashSet<>();
        List<MethodInfo> unique = new ArrayList<>();
        for (MethodInfo method : methods) {
            String key = method.getDeclaringClass() + "#" + method.getMethodName();
            if (seen.add(key)) {
                unique.add(method);
            }
        }
        return unique;
    }

    private List<RelatedFile> deduplicateRelatedFiles(List<RelatedFile> files) {
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }
        Set<String> seen = new HashSet<>();
        List<RelatedFile> unique = new ArrayList<>();
        for (RelatedFile file : files) {
            String key = file.getFilePath();
            if (key != null && seen.add(key)) {
                unique.add(file);
            }
        }
        return unique;
    }

    private List<DependencyInfo> deduplicateDependencies(List<DependencyInfo> dependencies) {
        if (dependencies == null || dependencies.isEmpty()) {
            return new ArrayList<>();
        }
        Set<String> seen = new HashSet<>();
        List<DependencyInfo> unique = new ArrayList<>();
        for (DependencyInfo dep : dependencies) {
            String key = dep.getGroupId() + ":" + dep.getArtifactId();
            if (seen.add(key)) {
                unique.add(dep);
            }
        }
        return unique;
    }

    private void addError(RepositoryContext context, String endpoint, String errorType, String message) {
        logger.warn("Error fetching {}: {} - {}", endpoint, errorType, message);
        context.addError(new ContextBuildError(endpoint, errorType, message));
    }
}