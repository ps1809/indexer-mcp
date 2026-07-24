package com.projectiq.mcp.client;

import com.projectiq.mcp.client.dto.DependencyRequest;
import com.projectiq.mcp.client.dto.DependencyResponse;
import com.projectiq.mcp.client.dto.IndexerHealthResponse;
import com.projectiq.mcp.client.dto.RepositoryStatsRequest;
import com.projectiq.mcp.client.dto.RepositoryStatsResponse;
import com.projectiq.mcp.client.dto.RepositorySummaryRequest;
import com.projectiq.mcp.client.dto.RepositorySummaryResponse;
import com.projectiq.mcp.client.dto.SearchCodeRequest;
import com.projectiq.mcp.client.dto.SearchCodeResponse;
import com.projectiq.mcp.client.dto.RestApiRequest;
import com.projectiq.mcp.client.dto.RestApiResponse;
import com.projectiq.mcp.client.dto.SpringComponentRequest;
import com.projectiq.mcp.client.dto.SpringComponentResponse;
import com.projectiq.mcp.client.exception.IndexerClientException;
import com.projectiq.mcp.client.exception.IndexerConnectionException;
import com.projectiq.mcp.client.exception.IndexerHttpException;
import com.projectiq.mcp.client.exception.IndexerTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

/**
 * Implementation of {@link IndexerRestClient} using Spring's RestClient.
 * Handles HTTP communication with ProjectIQ Indexer including error handling.
 */
@Component
public class IndexerRestClientImpl implements IndexerRestClient {

    private static final Logger logger = LoggerFactory.getLogger(IndexerRestClientImpl.class);

    private final RestClient restClient;

    public IndexerRestClientImpl(RestClient indexerRestClient) {
        this.restClient = indexerRestClient;
    }

    @Override
    public IndexerHealthResponse checkHealth() {
        logger.debug("Checking Indexer health status");
        try {
            IndexerHealthResponse response = restClient.get()
                    .uri("/actuator/health")
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                    .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
                    .body(IndexerHealthResponse.class);

            if (response == null) {
                throw new IndexerClientException("Received null response from Indexer health endpoint");
            }

            logger.debug("Indexer health response: {}", response);
            return response;
        } catch (ResourceAccessException e) {
            throw handleResourceAccessException(e);
        } catch (RestClientException e) {
            String message = "Failed to deserialize response from Indexer: " + e.getMessage();
            logger.error(message, e);
            throw new IndexerClientException(message, e);
        }
    }

    @Override
    public boolean isReachable() {
        logger.debug("Checking if Indexer is reachable");
        try {
            restClient.get()
                    .uri("/actuator/health")
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                    .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
                    .toEntity(String.class);

            logger.debug("Indexer is reachable");
            return true;
        } catch (ResourceAccessException e) {
            logger.warn("Indexer is not reachable: {}", e.getMessage());
            return false;
        } catch (RestClientException e) {
            logger.warn("Indexer communication error during reachability check: {}", e.getMessage());
            return false;
        } catch (IndexerClientException e) {
            logger.warn("Indexer returned error during reachability check: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public RepositorySummaryResponse getRepositorySummary(RepositorySummaryRequest request) {
        logger.debug("Fetching repository summary for repository: {}, branch: {}", 
                request.getRepositoryName(), request.getBranch());
        
        String url = "/api/v1/indexer/" + request.getRepositoryName() + "/summary";
        if (request.getBranch() != null && !request.getBranch().isEmpty()) {
            url += "?branch=" + request.getBranch();
        }

        try {
            RepositorySummaryResponse response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                    .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
                    .body(RepositorySummaryResponse.class);

            if (response == null) {
                throw new IndexerClientException("Received null response from Indexer repository summary endpoint");
            }

            logger.debug("Indexer repository summary: {}", response);
            return response;
        } catch (ResourceAccessException e) {
            throw handleResourceAccessException(e);
        } catch (RestClientException e) {
            String message = "Failed to deserialize response from Indexer repository summary endpoint: " + e.getMessage();
            logger.error(message, e);
            throw new IndexerClientException(message, e);
        }
    }

    @Override
    public RepositoryStatsResponse getRepositoryStatistics(RepositoryStatsRequest request) {
        logger.debug("Fetching repository statistics for repository: {}, branch: {}", 
                request.getRepositoryName(), request.getBranch());
        
        String url = "/api/v1/indexer/" + request.getRepositoryName() + "/stats";
        if (request.getBranch() != null && !request.getBranch().isEmpty()) {
            url += "?branch=" + request.getBranch();
        }

        try {
            RepositoryStatsResponse response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                    .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
                    .body(RepositoryStatsResponse.class);

            if (response == null) {
                throw new IndexerClientException("Received null response from Indexer repository statistics endpoint");
            }

            logger.debug("Indexer repository statistics: {}", response);
            return response;
        } catch (ResourceAccessException e) {
            throw handleResourceAccessException(e);
        } catch (RestClientException e) {
            String message = "Failed to deserialize response from Indexer repository statistics endpoint: " + e.getMessage();
            logger.error(message, e);
            throw new IndexerClientException(message, e);
        }
    }

    @Override
    public SearchCodeResponse searchCode(SearchCodeRequest request) {
        logger.debug("Searching code for repository: {}, query: {}", 
                request.getRepositoryName(), request.getQuery());
        
        StringBuilder urlBuilder = new StringBuilder("/api/v1/indexer/");
        urlBuilder.append(request.getRepositoryName()).append("/search?q=");
        
        if (request.getQuery() != null && !request.getQuery().isEmpty()) {
            urlBuilder.append(request.getQuery());
        }
        
        if (request.getBranch() != null && !request.getBranch().isEmpty()) {
            urlBuilder.append("&branch=").append(request.getBranch());
        }
        
        if (request.getPackageName() != null && !request.getPackageName().isEmpty()) {
            urlBuilder.append("&packageName=").append(request.getPackageName());
        }
        
        if (request.getMaxResults() != null) {
            urlBuilder.append("&maxResults=").append(request.getMaxResults());
        }

        String url = urlBuilder.toString();

        try {
            SearchCodeResponse response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                    .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
                    .body(SearchCodeResponse.class);

            if (response == null) {
                throw new IndexerClientException("Received null response from Indexer search endpoint");
            }

            logger.debug("Indexer search results: {} total", response.getTotalResults());
            return response;
        } catch (ResourceAccessException e) {
            throw handleResourceAccessException(e);
        } catch (RestClientException e) {
            String message = "Failed to deserialize response from Indexer search endpoint: " + e.getMessage();
            logger.error(message, e);
            throw new IndexerClientException(message, e);
        }
    }

    @Override
    public SpringComponentResponse findSpringComponent(SpringComponentRequest request) {
        logger.debug("Finding Spring components for repository: {}, types: {}", 
                request.getRepositoryName(), request.getComponentTypes());
        
        StringBuilder urlBuilder = new StringBuilder("/api/v1/indexer/");
        urlBuilder.append(request.getRepositoryName()).append("/spring-component?types=");
        
        if (request.getComponentTypes() != null && !request.getComponentTypes().isEmpty()) {
            urlBuilder.append(String.join(",", request.getComponentTypes()));
        }
        
        if (request.getBranch() != null && !request.getBranch().isEmpty()) {
            urlBuilder.append("&branch=").append(request.getBranch());
        }
        
        if (request.getPackageName() != null && !request.getPackageName().isEmpty()) {
            urlBuilder.append("&packageName=").append(request.getPackageName());
        }

        String url = urlBuilder.toString();

        try {
            SpringComponentResponse response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                    .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
                    .body(SpringComponentResponse.class);

            if (response == null) {
                throw new IndexerClientException("Received null response from Indexer spring component endpoint");
            }

            logger.debug("Indexer Spring component results: {} total", response.getTotalResults());
            return response;
        } catch (ResourceAccessException e) {
            throw handleResourceAccessException(e);
        } catch (RestClientException e) {
            String message = "Failed to deserialize response from Indexer spring component endpoint: " + e.getMessage();
            logger.error(message, e);
            throw new IndexerClientException(message, e);
        }
    }

    @Override
    public RestApiResponse findRestApi(RestApiRequest request) {
        logger.debug("Finding REST API endpoints for repository: {}, methods: {}", 
                request.getRepositoryName(), request.getHttpMethods());
        
        StringBuilder urlBuilder = new StringBuilder("/api/v1/indexer/");
        urlBuilder.append(request.getRepositoryName()).append("/rest-api?types=");
        
        if (request.getHttpMethods() != null && !request.getHttpMethods().isEmpty()) {
            urlBuilder.append(String.join(",", request.getHttpMethods()));
        }
        
        if (request.getBranch() != null && !request.getBranch().isEmpty()) {
            urlBuilder.append("&branch=").append(request.getBranch());
        }
        
        if (request.getPackageName() != null && !request.getPackageName().isEmpty()) {
            urlBuilder.append("&packageName=").append(request.getPackageName());
        }

        String url = urlBuilder.toString();

        try {
            RestApiResponse response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                    .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
                    .body(RestApiResponse.class);

            if (response == null) {
                throw new IndexerClientException("Received null response from Indexer REST API endpoint");
            }

            logger.debug("Indexer REST API results: {} total", response.getTotalResults());
            return response;
        } catch (ResourceAccessException e) {
            throw handleResourceAccessException(e);
        } catch (RestClientException e) {
            String message = "Failed to deserialize response from Indexer REST API endpoint: " + e.getMessage();
            logger.error(message, e);
            throw new IndexerClientException(message, e);
        }
    }

    @Override
    public DependencyResponse findDependency(DependencyRequest request) {
        logger.debug("Finding dependencies for repository: {}, types: {}", 
                request.getRepositoryName(), request.getDependencyTypes());
        
        StringBuilder urlBuilder = new StringBuilder("/api/v1/indexer/");
        urlBuilder.append(request.getRepositoryName()).append("/dependency?types=");
        
        if (request.getDependencyTypes() != null && !request.getDependencyTypes().isEmpty()) {
            urlBuilder.append(String.join(",", request.getDependencyTypes()));
        }
        
        if (request.getBranch() != null && !request.getBranch().isEmpty()) {
            urlBuilder.append("&branch=").append(request.getBranch());
        }
        
        if (request.getPackageName() != null && !request.getPackageName().isEmpty()) {
            urlBuilder.append("&packageName=").append(request.getPackageName());
        }
        
        if (request.getSearchPattern() != null && !request.getSearchPattern().isEmpty()) {
            urlBuilder.append("&searchPattern=").append(request.getSearchPattern());
        }

        String url = urlBuilder.toString();

        try {
            DependencyResponse response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                    .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
                    .body(DependencyResponse.class);

            if (response == null) {
                throw new IndexerClientException("Received null response from Indexer dependency endpoint");
            }

            logger.debug("Indexer dependency results: {} total", response.getTotalResults());
            return response;
        } catch (ResourceAccessException e) {
            throw handleResourceAccessException(e);
        } catch (RestClientException e) {
            String message = "Failed to deserialize response from Indexer dependency endpoint: " + e.getMessage();
            logger.error(message, e);
            throw new IndexerClientException(message, e);
        }
    }

    private void handleClientError(HttpRequest request, ClientHttpResponse response) throws IOException {
        int statusCode = response.getStatusCode().value();
        String body = new String(response.getBody().readAllBytes());
        String message = String.format("Indexer returned client error: HTTP %d - %s", statusCode, body);
        logger.warn(message);
        throw new IndexerHttpException(message, statusCode);
    }

    private void handleServerError(HttpRequest request, ClientHttpResponse response) throws IOException {
        int statusCode = response.getStatusCode().value();
        String body = new String(response.getBody().readAllBytes());
        String message = String.format("Indexer returned server error: HTTP %d - %s", statusCode, body);
        logger.error(message);
        throw new IndexerHttpException(message, statusCode);
    }

    private IndexerClientException handleResourceAccessException(ResourceAccessException e) {
        Throwable cause = e.getCause();

        if (cause instanceof SocketTimeoutException) {
            String message = "Connection to Indexer timed out";
            logger.error(message, e);
            return new IndexerTimeoutException(message, e);
        }

        if (cause instanceof ConnectException) {
            String message = "Connection to Indexer refused";
            logger.error(message, e);
            return new IndexerConnectionException(message, e);
        }

        String message = "Failed to communicate with Indexer: " + e.getMessage();
        logger.error(message, e);
        return new IndexerClientException(message, e);
    }
}