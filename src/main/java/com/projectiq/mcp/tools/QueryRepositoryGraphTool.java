package com.projectiq.mcp.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.projectiq.mcp.knowledgegraph.dto.KnowledgeGraphReport;
import com.projectiq.mcp.knowledgegraph.service.RepositoryKnowledgeGraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP tool for querying the repository knowledge graph.
 * Allows AI agents to traverse and understand repository relationships.
 */
@Component
public class QueryRepositoryGraphTool {

    private static final Logger logger = LoggerFactory.getLogger(QueryRepositoryGraphTool.class);

    private final RepositoryKnowledgeGraphService knowledgeGraphService;
    private final ObjectMapper objectMapper;

    public QueryRepositoryGraphTool(RepositoryKnowledgeGraphService knowledgeGraphService) {
        this.knowledgeGraphService = knowledgeGraphService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Executes the query_repository_graph tool.
     *
     * @param repositoryName the repository name (required)
     * @param entityName     the entity name to traverse from (optional)
     * @param branch         the branch name (optional)
     * @return JSON string with graph analysis results
     */
    @Tool(description = "Query the repository knowledge graph to discover relationships " +
            "between classes, methods, APIs, dependencies, and other repository entities. " +
            "Returns connected entities, relationship graph, dependency paths, " +
            "architectural relationships, indirect dependencies, critical nodes, " +
            "and graph statistics.")
    public String queryRepositoryGraph(String repositoryName, String entityName, String branch) {
        if (repositoryName == null || repositoryName.isEmpty()) {
            try {
                return objectMapper.writeValueAsString(
                        new ErrorResponse("INVALID_ARGUMENT", "repositoryName is required"));
            } catch (Exception e) {
                return "{\"error\":{\"code\":\"SERIALIZATION_ERROR\",\"message\":\"Failed to serialize error response\"}}";
            }
        }

        try {
            KnowledgeGraphReport report;
            if (entityName != null && !entityName.trim().isEmpty()) {
                logger.info("Querying knowledge graph from entity '{}' in repository: {}",
                        entityName, repositoryName);
                report = knowledgeGraphService.traverseFromEntity(
                        repositoryName, entityName.trim(), branch);
            } else {
                logger.info("Querying knowledge graph for repository: {}", repositoryName);
                report = knowledgeGraphService.generateKnowledgeGraphReport(
                        repositoryName, branch);
            }

            return objectMapper.writeValueAsString(report);

        } catch (Exception e) {
            logger.error("Unexpected error while querying knowledge graph: {}", e.getMessage());
            try {
                return objectMapper.writeValueAsString(
                        new ErrorResponse("INTERNAL_ERROR",
                                "Internal error: " + e.getMessage()));
            } catch (Exception jacksonEx) {
                return "{\"error\":{\"code\":\"INTERNAL_ERROR\",\"message\":\"Internal error\"}}";
            }
        }
    }

    /**
     * Error response DTO.
     */
    public static class ErrorResponse {
        private String code;
        private String message;

        public ErrorResponse() {
        }

        public ErrorResponse(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}