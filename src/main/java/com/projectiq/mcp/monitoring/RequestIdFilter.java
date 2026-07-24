package com.projectiq.mcp.monitoring;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Servlet filter that generates a unique Request ID for every incoming MCP request.
 * The Request ID is set in the MDC context for log correlation and included
 * in the response header for debugging.
 */
@Component
@Order(1)
public class RequestIdFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RequestIdFilter.class);
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String requestId = RequestIdManager.generateRequestId();

        try {
            if (response instanceof HttpServletResponse httpResponse) {
                httpResponse.setHeader(REQUEST_ID_HEADER, requestId);
            }

            logger.info("Incoming request [{}]", requestId);

            chain.doFilter(request, response);

            logger.info("Request completed [{}]", requestId);
        } catch (Exception e) {
            logger.error("Request failed [{}]: {}", requestId, e.getMessage());
            throw e;
        } finally {
            RequestIdManager.clear();
        }
    }
}