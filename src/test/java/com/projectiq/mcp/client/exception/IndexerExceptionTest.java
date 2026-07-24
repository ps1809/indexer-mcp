package com.projectiq.mcp.client.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IndexerExceptionTest {

    @Test
    void indexerClientException_shouldCreateWithMessage() {
        IndexerClientException ex = new IndexerClientException("test message");
        assertEquals("test message", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void indexerClientException_shouldCreateWithMessageAndCause() {
        RuntimeException cause = new RuntimeException("cause");
        IndexerClientException ex = new IndexerClientException("test message", cause);
        assertEquals("test message", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void indexerConnectionException_shouldExtendClientException() {
        IndexerConnectionException ex = new IndexerConnectionException("connection refused");
        assertInstanceOf(IndexerClientException.class, ex);
        assertEquals("connection refused", ex.getMessage());
    }

    @Test
    void indexerConnectionException_shouldCreateWithCause() {
        Exception cause = new Exception("root cause");
        IndexerConnectionException ex = new IndexerConnectionException("connection refused", cause);
        assertEquals("connection refused", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void indexerTimeoutException_shouldExtendClientException() {
        IndexerTimeoutException ex = new IndexerTimeoutException("timed out");
        assertInstanceOf(IndexerClientException.class, ex);
        assertEquals("timed out", ex.getMessage());
    }

    @Test
    void indexerTimeoutException_shouldCreateWithCause() {
        Exception cause = new Exception("root cause");
        IndexerTimeoutException ex = new IndexerTimeoutException("timed out", cause);
        assertEquals("timed out", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void indexerHttpException_shouldExtendClientException() {
        IndexerHttpException ex = new IndexerHttpException("not found", 404);
        assertInstanceOf(IndexerClientException.class, ex);
        assertEquals("not found", ex.getMessage());
        assertEquals(404, ex.getStatusCode());
    }

    @Test
    void indexerHttpException_shouldCreateWithCause() {
        Exception cause = new Exception("root cause");
        IndexerHttpException ex = new IndexerHttpException("server error", 500, cause);
        assertEquals("server error", ex.getMessage());
        assertEquals(500, ex.getStatusCode());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void indexerHttpException_shouldStoreStatusCode() {
        IndexerHttpException ex400 = new IndexerHttpException("bad request", 400);
        IndexerHttpException ex503 = new IndexerHttpException("unavailable", 503);

        assertEquals(400, ex400.getStatusCode());
        assertEquals(503, ex503.getStatusCode());
    }
}