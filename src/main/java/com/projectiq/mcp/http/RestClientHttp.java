package com.projectiq.mcp.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectiq.mcp.http.config.HttpClientProperties;
import com.projectiq.mcp.http.exception.HttpClientConnectionException;
import com.projectiq.mcp.http.exception.HttpClientException;
import com.projectiq.mcp.http.exception.HttpClientSerializationException;
import com.projectiq.mcp.http.exception.HttpClientTimeoutException;
import com.projectiq.mcp.http.model.HttpResponse;
import java.io.IOException;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.ResourceAccessException;

/**
 * Generic HTTP client implementation using Spring's RestClient.
 * This implementation contains no business logic and is fully reusable.
 */
public class RestClientHttp implements HttpClient {

    private static final Logger log = LoggerFactory.getLogger(RestClientHttp.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final HttpClientProperties properties;

    public RestClientHttp(RestClient restClient, ObjectMapper objectMapper, HttpClientProperties properties) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public <T> HttpResponse<T> get(String url, Class<T> responseClass, Map<String, String> headers) throws HttpClientException {
        try {
            logRequest("GET", url, null);

            org.springframework.http.ResponseEntity<byte[]> entity = restClient.method(HttpMethod.GET)
                .uri(url)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::doHandleError)
                .onStatus(HttpStatusCode::is5xxServerError, this::doHandleError)
                .toEntity(byte[].class);

            int statusCode = entity.getStatusCode().value();
            HttpHeaders responseHeaders = entity.getHeaders();
            byte[] bodyBytes = entity.getBody();

            Map<String, Object> headersMap = convertHeaders(responseHeaders);
            T body = bodyBytes != null ? deserialize(new String(bodyBytes), responseClass) : null;

            return new HttpResponse<>(body, statusCode, headersMap);
        } catch (ResourceAccessException e) {
            throw handleResourceAccessException(e);
        } catch (HttpClientSerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new HttpClientException("Unexpected error on GET " + url + ": " + e.getMessage(), e);
        }
    }

    @Override
    public <T, R> HttpResponse<T> post(String url, R requestBody, Class<T> responseClass, Map<String, String> headers) throws HttpClientException {
        try {
            String jsonBody = serialize(requestBody);
            logRequest("POST", url, jsonBody);

            org.springframework.http.ResponseEntity<byte[]> entity = restClient.method(HttpMethod.POST)
                .uri(url)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .body(jsonBody)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::doHandleError)
                .onStatus(HttpStatusCode::is5xxServerError, this::doHandleError)
                .toEntity(byte[].class);

            int statusCode = entity.getStatusCode().value();
            HttpHeaders responseHeaders = entity.getHeaders();
            byte[] bodyBytes = entity.getBody();

            Map<String, Object> headersMap = convertHeaders(responseHeaders);
            T body = bodyBytes != null ? deserialize(new String(bodyBytes), responseClass) : null;

            return new HttpResponse<>(body, statusCode, headersMap);
        } catch (ResourceAccessException e) {
            throw handleResourceAccessException(e);
        } catch (HttpClientSerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new HttpClientException("Unexpected error on POST " + url + ": " + e.getMessage(), e);
        }
    }

    @Override
    public <T, R> HttpResponse<T> put(String url, R requestBody, Class<T> responseClass, Map<String, String> headers) throws HttpClientException {
        try {
            String jsonBody = serialize(requestBody);
            logRequest("PUT", url, jsonBody);

            org.springframework.http.ResponseEntity<byte[]> entity = restClient.method(HttpMethod.PUT)
                .uri(url)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .body(jsonBody)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::doHandleError)
                .onStatus(HttpStatusCode::is5xxServerError, this::doHandleError)
                .toEntity(byte[].class);

            int statusCode = entity.getStatusCode().value();
            HttpHeaders responseHeaders = entity.getHeaders();
            byte[] bodyBytes = entity.getBody();

            Map<String, Object> headersMap = convertHeaders(responseHeaders);
            T body = bodyBytes != null ? deserialize(new String(bodyBytes), responseClass) : null;

            return new HttpResponse<>(body, statusCode, headersMap);
        } catch (ResourceAccessException e) {
            throw handleResourceAccessException(e);
        } catch (HttpClientSerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new HttpClientException("Unexpected error on PUT " + url + ": " + e.getMessage(), e);
        }
    }

    @Override
    public <T> HttpResponse<T> delete(String url, Class<T> responseClass, Map<String, String> headers) throws HttpClientException {
        try {
            logRequest("DELETE", url, null);

            org.springframework.http.ResponseEntity<byte[]> entity = restClient.method(HttpMethod.DELETE)
                .uri(url)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::doHandleError)
                .onStatus(HttpStatusCode::is5xxServerError, this::doHandleError)
                .toEntity(byte[].class);

            int statusCode = entity.getStatusCode().value();
            HttpHeaders responseHeaders = entity.getHeaders();
            byte[] bodyBytes = entity.getBody();

            Map<String, Object> headersMap = convertHeaders(responseHeaders);
            T body = bodyBytes != null ? deserialize(new String(bodyBytes), responseClass) : null;

            return new HttpResponse<>(body, statusCode, headersMap);
        } catch (ResourceAccessException e) {
            throw handleResourceAccessException(e);
        } catch (HttpClientSerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new HttpClientException("Unexpected error on DELETE " + url + ": " + e.getMessage(), e);
        }
    }

    private void doHandleError(org.springframework.http.HttpRequest request, ClientHttpResponse response) throws IOException {
        HttpStatusCode statusCode = response.getStatusCode();
        if (statusCode.is4xxClientError()) {
            String body = new String(response.getBody().readAllBytes());
            throw new com.projectiq.mcp.http.exception.HttpClientResponseException(
                statusCode.value(), "Client error " + statusCode.value() + ": " + body);
        } else if (statusCode.is5xxServerError()) {
            String body = new String(response.getBody().readAllBytes());
            throw new com.projectiq.mcp.http.exception.HttpClientResponseException(
                statusCode.value(), "Server error " + statusCode.value() + ": " + body);
        }
    }

    private Map<String, Object> convertHeaders(HttpHeaders httpHeaders) {
        Map<String, Object> result = new HashMap<>();
        if (httpHeaders != null) {
            for (String key : httpHeaders.keySet()) {
                List<String> values = httpHeaders.get(key);
                if (values != null && !values.isEmpty()) {
                    result.put(key, values.get(0));
                }
            }
        }
        return result;
    }

    private <T> T deserialize(String json, Class<T> clazz) throws HttpClientSerializationException {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new HttpClientSerializationException("Failed to deserialize response: " + e.getMessage(), e);
        }
    }

    private String serialize(Object requestBody) throws HttpClientSerializationException {
        if (requestBody == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new HttpClientSerializationException("Failed to serialize request body: " + e.getMessage(), e);
        }
    }

    private HttpClientException handleResourceAccessException(ResourceAccessException e) {
        Throwable cause = e.getCause();
        if (cause instanceof java.net.SocketTimeoutException) {
            return new HttpClientTimeoutException("Read timeout: " + e.getMessage(), e);
        }
        if (cause instanceof ConnectException) {
            return new HttpClientConnectionException("Connection refused: " + e.getMessage(), e);
        }
        return new HttpClientConnectionException("Connection error: " + e.getMessage(), e);
    }

    private void logRequest(String method, String url, String body) {
        if (properties.isLoggingEnabled()) {
            log.debug("{} {}", method, url);
            if (body != null) {
                log.debug("Request body: {}", body);
            }
        }
    }
}