package com.projectiq.mcp.tools;

import com.projectiq.mcp.knowledge.dto.KnowledgeReport;
import com.projectiq.mcp.knowledge.service.DevelopmentKnowledgeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP Tool that queries the Intelligent AI Development Knowledge Engine.
 * Accepts natural language knowledge queries and returns unified deterministic
 * knowledge reports consolidating all repository intelligence, architectural
 * insights, workflow information, development sessions, recommendations,
 * and evolution insights.
 *
 * <p>This tool does NOT use AI/LLM reasoning. All outputs are deterministic
 * and based solely on indexed repository data.</p>
 */
@Component
public class QueryDevelopmentKnowledgeTool {

    private static final Logger logger = LoggerFactory.getLogger(QueryDevelopmentKnowledgeTool.class);

    private final DevelopmentKnowledgeService developmentKnowledgeService;

    public QueryDevelopmentKnowledgeTool(DevelopmentKnowledgeService developmentKnowledgeService) {
        this.developmentKnowledgeService = developmentKnowledgeService;
    }

    /**
     * Queries the development knowledge engine for a unified knowledge report.
     *
     * @param query          the natural language knowledge query (required)
     * @param repositoryName the repository name (required)
     * @param branch         the git branch (optional, defaults to "main")
     * @return a unified KnowledgeReport with all relevant intelligence
     */
    @Tool(description = """
            Queries the Intelligent AI Development Knowledge Engine for a unified knowledge report.
            
            Accepts natural language knowledge queries and returns deterministic knowledge reports
            consolidating all repository intelligence, architectural insights, workflow information,
            development sessions, recommendations, and evolution insights.
            
            Supports domain-specific queries such as:
            - Architecture: "Show me architecture"
            - Dependencies: "What are the dependencies?"
            - Development Sessions: "Show me sessions"
            - Knowledge Graph: "knowledge graph"
            - Workflow Intelligence: "workflow intelligence"
            - Repository Evolution: "repository evolution"
            - All Domains: "Show me everything" (default for unrecognized queries)
            
            All results are deterministic and based solely on indexed repository data.
            """)
    public KnowledgeReport queryDevelopmentKnowledge(
            String query,
            String repositoryName,
            String branch) {
        logger.info("QueryDevelopmentKnowledgeTool called: query='{}', repository='{}', branch='{}'",
                query, repositoryName, branch);

        try {
            return developmentKnowledgeService.queryKnowledge(query, repositoryName, branch);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid arguments for queryDevelopmentKnowledge: {}", e.getMessage());
            KnowledgeReport errorReport = new KnowledgeReport();
            errorReport.setStatus("ERROR");
            errorReport.setErrorMessage(e.getMessage());
            errorReport.setQuery(query);
            errorReport.setRepositoryName(repositoryName);
            errorReport.setBranch(branch);
            return errorReport;
        } catch (Exception e) {
            logger.error("Unexpected error in queryDevelopmentKnowledge: {}", e.getMessage(), e);
            KnowledgeReport errorReport = new KnowledgeReport();
            errorReport.setStatus("ERROR");
            errorReport.setErrorMessage("Unexpected error: " + e.getMessage());
            errorReport.setQuery(query);
            errorReport.setRepositoryName(repositoryName);
            errorReport.setBranch(branch);
            return errorReport;
        }
    }
}