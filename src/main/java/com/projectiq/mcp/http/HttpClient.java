package com.projectiq.mcp.http;

import com.projectiq.mcp.http.exception.HttpClientException;
import com.projectiq.mcp.http.model.HttpResponse;
import java.util.Map;

/**
 * Generic HTTP client interface for executing HTTP requests.
 * This interface is completely generic and contains no business logic.
 */
public interface HttpClient {

    /**
     * Execute a GET request.
     *
     * @param url the target URL
     * @param responseClass the expected response body type
     * @param headers optional request headers
     * @return the deserialized HTTP response
     * @throws HttpClientException if an error occurs during the request
     */
    <T> HttpResponse<T> get(String url, Class<T> responseClass, Map<String, String> headers) throws HttpClientException;

    /**
     * Execute a POST request.
     *
     * @param url the target URL
     * @param requestBody the request body to serialize and send
     * @param responseClass the expected response body type
     * @param headers optional request headers
     * @return the deserialized HTTP response
     * @throws HttpClientException if an error occurs during the request
     */
    <T, R> HttpResponse<T> post(String url, R requestBody, Class<T> responseClass, Map<String, String> headers) throws HttpClientException;

    /**
     * Execute a PUT request.
     *
     * @param url the target URL
     * @param requestBody the request body to serialize and send
     * @param responseClass the expected response body type
     * @param headers optional request headers
     * @return the deserialized HTTP response
     * @throws HttpClientException if an error occurs during the request
     */
    <T, R> HttpResponse<T> put(String url, R requestBody, Class<T> responseClass, Map<String, String> headers) throws HttpClientException;

    /**
     * Execute a DELETE request.
     *
     * @param url the target URL
     * @param responseClass the expected response body type
     * @param headers optional request headers
     * @return the deserialized HTTP response
     * @throws HttpClientException if an error occurs during the request
     */
    <T> HttpResponse<T> delete(String url, Class<T> responseClass, Map<String, String> headers) throws HttpClientException;

}