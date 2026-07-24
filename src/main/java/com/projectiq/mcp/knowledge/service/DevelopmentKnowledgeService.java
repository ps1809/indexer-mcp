package com.projectiq.mcp.knowledge.service;

import com.projectiq.mcp.analysis.dto.ArchitectureInsightsResponse;
import com.projectiq.mcp.analysis.dto.ArchitecturalDecisionResponse;
import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse;
import com.projectiq.mcp.analysis.dto.RepositoryEvolutionAnalysisResponse;
import com.projectiq.mcp.analysis.dto.RepositoryHealthResponse;
import com.projectiq.mcp.analysis.service.ArchitectureInsightsService;
import com.projectiq.mcp.analysis.service.ArchitecturalDecisionService;
import com.projectiq.mcp.analysis.service.CrossRepositoryAnalysisService;
import com.projectiq.mcp.analysis.service.RepositoryEvolutionAnalysisService;
import com.projectiq.mcp.analysis.service.RepositoryHealthService;
import com.projectiq.mcp.knowledge.dto.KnowledgeDomain;
import com.projectiq.mcp.knowledge.dto.KnowledgeReport;
import com.projectiq.mcp.knowledgegraph.dto.KnowledgeGraphReport;
import com.projectiq.mcp.knowledgegraph.service.RepositoryKnowledgeGraphService;
import com.projectiq.mcp.orchestration.dto.WorkflowResult;
import com.projectiq.mcp.orchestration.service.WorkflowOrchestratorService;
import com.projectiq.mcp.pipeline.service.IntelligentContextPipelineService;
import com.projectiq.mcp.recommendation.dto.RecommendationReport;
import com.projectiq.mcp.recommendation.service.RecommendationEngineService;
import com.projectiq.mcp.session.service.DevelopmentSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service that implements the Intelligent AI Development Knowledge Engine.
 * Consolidates all repository intelligence, workflow analysis, architectural
 * insights, knowledge graph relationships, validation results, recommendations,
 * and historical development sessions into a unified deterministic knowledge
 * report.
 *
 * <p>This service provides AI coding agents with a single entry point for
 * repository understanding, enabling richer development assistance without
 * re-running independent analyses or modifying repository code.</p>
 *
 * <p>All outputs are deterministic, stable, and based solely on indexed data.
 * No AI/LLM reasoning is used. No repository modification occurs.</p>
 */
@Service
public class DevelopmentKnowledgeService {

    private static final Logger logger = LoggerFactory.getLogger(DevelopmentKnowledgeService.class);

    private final RepositoryKnowledgeGraphService knowledgeGraphService;
    private final DevelopmentSessionService sessionService;
    private final WorkflowOrchestratorService workflowOrchestratorService;
    private final IntelligentContextPipelineService contextPipelineService;
    private final RecommendationEngineService recommendationEngineService;
    private final RepositoryEvolutionAnalysisService evolutionAnalysisService;
    private final CrossRepositoryAnalysisService crossRepositoryAnalysisService;
    private final ArchitectureInsightsService architectureInsightsService;
    private final ArchitecturalDecisionService architecturalDecisionService;
    private final RepositoryHealthService repositoryHealthService;

    public DevelopmentKnowledgeService(
            RepositoryKnowledgeGraphService knowledgeGraphService,
            DevelopmentSessionService sessionService,
            WorkflowOrchestratorService workflowOrchestratorService,
            IntelligentContextPipelineService contextPipelineService,
            RecommendationEngineService recommendationEngineService,
            RepositoryEvolutionAnalysisService evolutionAnalysisService,
            CrossRepositoryAnalysisService crossRepositoryAnalysisService,
            ArchitectureInsightsService architectureInsightsService,
            ArchitecturalDecisionService architecturalDecisionService,
            RepositoryHealthService repositoryHealthService) {
        this.knowledgeGraphService = knowledgeGraphService;
        this.sessionService = sessionService;
        this.workflowOrchestratorService = workflowOrchestratorService;
        this.contextPipelineService = contextPipelineService;
        this.recommendationEngineService = recommendationEngineService;
        this.evolutionAnalysisService = evolutionAnalysisService;
        this.crossRepositoryAnalysisService = crossRepositoryAnalysisService;
        this.architectureInsightsService = architectureInsightsService;
        this.architecturalDecisionService = architecturalDecisionService;
        this.repositoryHealthService = repositoryHealthService;
    }

    /**
     * Queries the development knowledge engine for a unified knowledge report.
     *
     * @param query          the natural language knowledge query
     * @param repositoryName the repository name
     * @param branch         the git branch (optional, defaults to "main")
     * @return a unified {@link KnowledgeReport} with all relevant intelligence
     * @throws IllegalArgumentException if query or repositoryName is null/empty
     */
    public KnowledgeReport queryKnowledge(String query, String repositoryName, String branch) {
        long startTime = System.currentTimeMillis();
        logger.info("Querying development knowledge: query='{}' repository='{}' branch='{}'",
                query, repositoryName, branch);

        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query cannot be null or empty");
        }
        if (repositoryName == null || repositoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("Repository name cannot be null or empty");
        }

        String effectiveBranch = (branch != null && !branch.trim().isEmpty()) ? branch.trim() : "main";

        KnowledgeReport report = new KnowledgeReport();
        report.setRepositoryName(repositoryName.trim());
        report.setBranch(effectiveBranch);
        report.setQuery(query.trim());

        // Determine the knowledge domain from the query
        KnowledgeDomain domain = KnowledgeDomain.fromQuery(query);
        report.setKnowledgeDomain(domain.getDisplayName());

        try {
            // Build the report based on the detected domain
            switch (domain) {
                case REPOSITORY_STRUCTURE:
                    buildRepositoryStructureKnowledge(report, repositoryName, effectiveBranch);
                    break;
                case ARCHITECTURE:
                    buildArchitectureKnowledge(report, repositoryName, effectiveBranch);
                    break;
                case DEPENDENCIES:
                    buildDependenciesKnowledge(report, repositoryName, effectiveBranch);
                    break;
                case REST_APIS:
                    buildRestApisKnowledge(report, repositoryName, effectiveBranch);
                    break;
                case SPRING_COMPONENTS:
                    buildSpringComponentsKnowledge(report, repositoryName, effectiveBranch);
                    break;
                case KNOWLEDGE_GRAPH:
                    buildKnowledgeGraphKnowledge(report, repositoryName, effectiveBranch);
                    break;
                case DEVELOPMENT_SESSIONS:
                    buildSessionsKnowledge(report, repositoryName, effectiveBranch);
                    break;
                case WORKFLOW_INTELLIGENCE:
                    buildWorkflowIntelligence(report, repositoryName, effectiveBranch);
                    break;
                case VALIDATION_RESULTS:
                    buildValidationKnowledge(report, repositoryName, effectiveBranch);
                    break;
                case REPOSITORY_EVOLUTION:
                    buildEvolutionKnowledge(report, repositoryName, effectiveBranch);
                    break;
                case ARCHITECTURAL_DECISIONS:
                    buildArchitecturalDecisionsKnowledge(report, repositoryName, effectiveBranch);
                    break;
                case CROSS_REPOSITORY_INSIGHTS:
                    buildCrossRepositoryKnowledge(report, repositoryName, effectiveBranch);
                    break;
                case ALL:
                default:
                    buildFullKnowledgeReport(report, repositoryName, effectiveBranch);
                    break;
            }

            // Build unified summary
            report.setUnifiedSummary(buildUnifiedSummary(report));

            // Build metadata
            report.setMetadata(buildReportMetadata(repositoryName, effectiveBranch));

        } catch (Exception e) {
            logger.error("Failed to generate knowledge report: {}", e.getMessage(), e);
            report.setStatus("ERROR");
            report.setErrorMessage("Failed to generate knowledge report: " + e.getMessage());
        }

        report.setGenerationDurationMillis(System.currentTimeMillis() - startTime);
        logger.info("Knowledge report generated in {}ms for domain: {}",
                report.getGenerationDurationMillis(), domain.getDisplayName());

        return report;
    }

    // ========== Domain-specific builders ==========

    private void buildRepositoryStructureKnowledge(KnowledgeReport report,
                                                    String repositoryName, String branch) {
        try {
            KnowledgeGraphReport kgReport = knowledgeGraphService.generateKnowledgeGraphReport(
                    repositoryName, branch);
            report.setRepositoryOverview(buildRepositoryOverview(kgReport));
            report.setComponentRelationships(kgReport.getConnectedEntities());
        } catch (Exception e) {
            logger.warn("Failed to build repository structure knowledge: {}", e.getMessage());
            report.setRepositoryOverview("Repository structure information unavailable: " + e.getMessage());
        }
    }

    private void buildArchitectureKnowledge(KnowledgeReport report,
                                             String repositoryName, String branch) {
        try {
            ArchitectureInsightsResponse archResponse = architectureInsightsService
                    .analyzeArchitecture(repositoryName, branch);
            report.setArchitectureSummary(buildArchitectureSummary(archResponse));
            report.setComponentRelationships(archResponse.getModuleRelationships() != null
                    ? archResponse.getModuleRelationships().stream()
                    .map(r -> r.getSourceModule() + " -> " + r.getTargetModule()
                            + " [" + r.getRelationshipType() + "]")
                    .collect(Collectors.toList())
                    : new ArrayList<>());
        } catch (Exception e) {
            logger.warn("Failed to build architecture knowledge: {}", e.getMessage());
            report.setArchitectureSummary("Architecture information unavailable: " + e.getMessage());
        }
    }

    private void buildDependenciesKnowledge(KnowledgeReport report,
                                             String repositoryName, String branch) {
        try {
            KnowledgeGraphReport kgReport = knowledgeGraphService.generateKnowledgeGraphReport(
                    repositoryName, branch);
            report.setRepositoryOverview("Dependencies: " + kgReport.getDependencyPaths().size()
                    + " direct dependency paths, " + kgReport.getIndirectDependencies().size()
                    + " indirect dependencies.");
            report.setComponentRelationships(kgReport.getDependencyPaths());
            report.getRisks().addAll(kgReport.getIndirectDependencies().stream()
                    .map(d -> "Indirect dependency: " + d)
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            logger.warn("Failed to build dependencies knowledge: {}", e.getMessage());
            report.setRepositoryOverview("Dependency information unavailable: " + e.getMessage());
        }
    }

    private void buildRestApisKnowledge(KnowledgeReport report,
                                         String repositoryName, String branch) {
        try {
            KnowledgeGraphReport kgReport = knowledgeGraphService.generateKnowledgeGraphReport(
                    repositoryName, branch);
            List<String> apiRelationships = kgReport.getRelationshipGraph().stream()
                    .filter(r -> "REST_API".equals(r.getSourceType())
                            || "REST_API".equals(r.getTargetType()))
                    .map(r -> r.getSource() + " --[" + r.getRelationship()
                            + "]--> " + r.getTarget())
                    .collect(Collectors.toList());
            report.setComponentRelationships(apiRelationships);
            report.setRepositoryOverview("REST APIs: " + apiRelationships.size() + " API relationships found.");
        } catch (Exception e) {
            logger.warn("Failed to build REST APIs knowledge: {}", e.getMessage());
            report.setRepositoryOverview("REST API information unavailable: " + e.getMessage());
        }
    }

    private void buildSpringComponentsKnowledge(KnowledgeReport report,
                                                 String repositoryName, String branch) {
        try {
            KnowledgeGraphReport kgReport = knowledgeGraphService.generateKnowledgeGraphReport(
                    repositoryName, branch);
            List<String> springComponents = kgReport.getRelationshipGraph().stream()
                    .filter(r -> "SPRING_COMPONENT".equals(r.getSourceType())
                            || "SPRING_COMPONENT".equals(r.getTargetType()))
                    .map(r -> r.getSource() + " --[" + r.getRelationship()
                            + "]--> " + r.getTarget())
                    .collect(Collectors.toList());
            report.setComponentRelationships(springComponents);
            report.setRepositoryOverview("Spring Components: " + springComponents.size()
                    + " component relationships found.");
        } catch (Exception e) {
            logger.warn("Failed to build Spring components knowledge: {}", e.getMessage());
            report.setRepositoryOverview("Spring component information unavailable: " + e.getMessage());
        }
    }

    private void buildKnowledgeGraphKnowledge(KnowledgeReport report,
                                               String repositoryName, String branch) {
        try {
            KnowledgeGraphReport kgReport = knowledgeGraphService.generateKnowledgeGraphReport(
                    repositoryName, branch);
            report.setRepositoryOverview(buildRepositoryOverview(kgReport));
            report.setComponentRelationships(kgReport.getConnectedEntities());
            if (kgReport.getGraphStatistics() != null) {
                report.setArchitectureSummary("Knowledge Graph: " + kgReport.getGraphStatistics().getTotalNodes()
                        + " nodes, " + kgReport.getGraphStatistics().getTotalEdges()
                        + " edges across " + kgReport.getGraphStatistics().getEntityTypeCount()
                        + " entity types.");
            }
            report.setRisks(kgReport.getCriticalNodes());
            report.setEvolutionInsights(kgReport.getArchitecturalRelationships());
        } catch (Exception e) {
            logger.warn("Failed to build knowledge graph knowledge: {}", e.getMessage());
            report.setRepositoryOverview("Knowledge graph information unavailable: " + e.getMessage());
        }
    }

    private void buildSessionsKnowledge(KnowledgeReport report,
                                         String repositoryName, String branch) {
        try {
            int totalSessions = sessionService.getTotalSessionCount();
            int activeSessions = sessionService.getActiveSessionCount();
            report.setRepositoryOverview("Development Sessions: " + totalSessions
                    + " total, " + activeSessions + " active.");
            report.setActiveSessions(List.of(
                    "Total sessions: " + totalSessions,
                    "Active sessions: " + activeSessions
            ));
        } catch (Exception e) {
            logger.warn("Failed to build sessions knowledge: {}", e.getMessage());
            report.setRepositoryOverview("Session information unavailable: " + e.getMessage());
        }
    }

    private void buildWorkflowIntelligence(KnowledgeReport report,
                                            String repositoryName, String branch) {
        try {
            WorkflowResult workflowResult = workflowOrchestratorService.orchestrate(
                    "Analyze repository intelligence", repositoryName, branch);
            report.setWorkflowSummaries(List.of(
                    "Workflow type: " + workflowResult.getWorkflowType(),
                    "Execution status: " + workflowResult.getExecutionStatus(),
                    "Completed steps: " + workflowResult.getCompletedSteps().size(),
                    "Repository insights: " + workflowResult.getRepositoryInsights().size(),
                    "Risks identified: " + workflowResult.getRisksIdentified().size()
            ));
            report.setRepositoryOverview("Workflow Intelligence: "
                    + workflowResult.getSummary());
            report.setRisks(workflowResult.getRisksIdentified());
            report.setRecommendations(workflowResult.getSuggestedNextActions());
        } catch (Exception e) {
            logger.warn("Failed to build workflow intelligence: {}", e.getMessage());
            report.setRepositoryOverview("Workflow intelligence unavailable: " + e.getMessage());
        }
    }

    private void buildValidationKnowledge(KnowledgeReport report,
                                           String repositoryName, String branch) {
        try {
            WorkflowResult workflowResult = workflowOrchestratorService.orchestrate(
                    "Validate repository", repositoryName, branch);
            report.setRepositoryOverview("Validation Results: "
                    + workflowResult.getSummary());
            report.setRisks(workflowResult.getRisksIdentified());
        } catch (Exception e) {
            logger.warn("Failed to build validation knowledge: {}", e.getMessage());
            report.setRepositoryOverview("Validation information unavailable: " + e.getMessage());
        }
    }

    private void buildEvolutionKnowledge(KnowledgeReport report,
                                          String repositoryName, String branch) {
        try {
            RepositoryEvolutionAnalysisResponse evolutionResponse =
                    evolutionAnalysisService.analyzeEvolution(
                            repositoryName, branch, "Analyze repository evolution");
            report.setEvolutionInsights(buildEvolutionInsights(evolutionResponse));
            report.setRepositoryOverview("Repository Evolution: "
                    + buildEvolutionSummary(evolutionResponse));
        } catch (Exception e) {
            logger.warn("Failed to build evolution knowledge: {}", e.getMessage());
            report.setRepositoryOverview("Evolution information unavailable: " + e.getMessage());
        }
    }

    private void buildArchitecturalDecisionsKnowledge(KnowledgeReport report,
                                                       String repositoryName, String branch) {
        try {
            ArchitecturalDecisionResponse decisionResponse =
                    architecturalDecisionService.adviseArchitecture(
                            "ARCHITECTURE_REVIEW", "Analyze architectural decisions", repositoryName);
            report.setArchitectureSummary("Architectural Decisions: "
                    + decisionResponse.getRecommendedApproach());
            if (decisionResponse.getArchitecturalRisks() != null) {
                report.setRisks(new ArrayList<>(decisionResponse.getArchitecturalRisks()));
            }
        } catch (Exception e) {
            logger.warn("Failed to build architectural decisions knowledge: {}", e.getMessage());
            report.setArchitectureSummary("Architectural decision information unavailable: " + e.getMessage());
        }
    }

    private void buildCrossRepositoryKnowledge(KnowledgeReport report,
                                                String repositoryName, String branch) {
        try {
            CrossRepositoryAnalysisResponse crossResponse =
                    crossRepositoryAnalysisService.analyzeCrossRepository(
                            Collections.singletonList(repositoryName));
            report.setRepositoryOverview("Cross-Repository Insights: "
                    + crossResponse.getRepositories().size() + " repositories analyzed.");
            if (crossResponse.getRiskAssessment() != null
                    && crossResponse.getRiskAssessment().getRisks() != null) {
                report.setRisks(new ArrayList<>(crossResponse.getRiskAssessment().getRisks()));
            }
        } catch (Exception e) {
            logger.warn("Failed to build cross-repository knowledge: {}", e.getMessage());
            report.setRepositoryOverview("Cross-repository information unavailable: " + e.getMessage());
        }
    }

    private void buildFullKnowledgeReport(KnowledgeReport report,
                                           String repositoryName, String branch) {
        // Build all knowledge domains into a single comprehensive report
        buildRepositoryStructureKnowledge(report, repositoryName, branch);
        buildArchitectureKnowledge(report, repositoryName, branch);
        buildSessionsKnowledge(report, repositoryName, branch);
        buildWorkflowIntelligence(report, repositoryName, branch);
        buildEvolutionKnowledge(report, repositoryName, branch);

        // Add health information
        try {
            RepositoryHealthResponse healthResponse = repositoryHealthService
                    .analyzeHealth(repositoryName, branch);
            report.setRepositoryHealth("Health Score: " + healthResponse.getHealthScore()
                    + "/100, Maintainability: " + healthResponse.getMaintainabilityRating());
            if (healthResponse.getPotentialRisks() != null) {
                report.getRisks().addAll(healthResponse.getPotentialRisks());
            }
        } catch (Exception e) {
            logger.warn("Failed to build health knowledge: {}", e.getMessage());
            report.setRepositoryHealth("Health information unavailable");
        }

        // Add recommendations
        try {
            RecommendationReport recReport = recommendationEngineService
                    .generateRecommendations("Knowledge Analysis", "REPOSITORY_ANALYSIS",
                            "Analyze repository", repositoryName, branch);
            if (recReport.getPrioritizedRecommendations() != null) {
                report.setRecommendations(recReport.getPrioritizedRecommendations().stream()
                        .map(r -> "[" + r.getPriority().name() + "] " + r.getTitle()
                                + ": " + r.getDescription())
                        .collect(Collectors.toList()));
            }
        } catch (Exception e) {
            logger.warn("Failed to build recommendations: {}", e.getMessage());
        }

        // Add knowledge graph insights
        try {
            KnowledgeGraphReport kgReport = knowledgeGraphService
                    .generateKnowledgeGraphReport(repositoryName, branch);
            report.getComponentRelationships().addAll(kgReport.getConnectedEntities());
            report.getRisks().addAll(kgReport.getCriticalNodes());
            report.getEvolutionInsights().addAll(kgReport.getArchitecturalRelationships());
        } catch (Exception e) {
            logger.warn("Failed to build knowledge graph insights: {}", e.getMessage());
        }
    }

    // ========== Helper methods ==========

    private String buildRepositoryOverview(KnowledgeGraphReport kgReport) {
        if (kgReport.getGraphStatistics() == null) {
            return "Repository overview unavailable";
        }
        return "Repository contains " + kgReport.getGraphStatistics().getTotalNodes()
                + " entities with " + kgReport.getGraphStatistics().getTotalEdges()
                + " relationships across " + kgReport.getGraphStatistics().getEntityTypeCount()
                + " entity types. Average connections per node: "
                + kgReport.getGraphStatistics().getAverageConnectionsPerNode()
                + ". Critical nodes: " + kgReport.getGraphStatistics().getCriticalNodeCount();
    }

    private String buildArchitectureSummary(ArchitectureInsightsResponse archResponse) {
        StringBuilder sb = new StringBuilder();
        sb.append("Architecture Style: ").append(archResponse.getArchitecturalStyle());
        if (archResponse.getDetectedLayers() != null && !archResponse.getDetectedLayers().isEmpty()) {
            sb.append(". Layers: ").append(String.join(", ", archResponse.getDetectedLayers()));
        }
        if (archResponse.getModuleRelationships() != null) {
            sb.append(". Module relationships: ").append(archResponse.getModuleRelationships().size());
        }
        return sb.toString();
    }

    private List<String> buildEvolutionInsights(RepositoryEvolutionAnalysisResponse evolutionResponse) {
        List<String> insights = new ArrayList<>();
        if (evolutionResponse.getMaintainabilityAssessment() != null) {
            insights.add("Maintainability: " + evolutionResponse.getMaintainabilityAssessment());
        }
        if (evolutionResponse.getTechnicalDebtIndicators() != null) {
            insights.add("Technical Debt: " + String.join("; ", evolutionResponse.getTechnicalDebtIndicators()));
        }
        if (evolutionResponse.getScalabilityConsiderations() != null) {
            insights.add("Scalability: " + evolutionResponse.getScalabilityConsiderations());
        }
        if (evolutionResponse.getArchitecturalImpact() != null) {
            insights.add("Architecture Impact: " + evolutionResponse.getArchitecturalImpact());
        }
        if (evolutionResponse.getConventionCompliance() != null) {
            insights.add("Convention Compliance: " + evolutionResponse.getConventionCompliance());
        }
        if (evolutionResponse.getLongTermRisks() != null) {
            for (String risk : evolutionResponse.getLongTermRisks()) {
                insights.add("Risk: " + risk);
            }
        }
        if (evolutionResponse.getRecommendedRepositoryPractices() != null) {
            for (String practice : evolutionResponse.getRecommendedRepositoryPractices()) {
                insights.add("Recommendation: " + practice);
            }
        }
        return insights;
    }

    private String buildEvolutionSummary(RepositoryEvolutionAnalysisResponse evolutionResponse) {
        StringBuilder sb = new StringBuilder();
        if (evolutionResponse.getMaintainabilityAssessment() != null) {
            sb.append("Maintainability: ").append(evolutionResponse.getMaintainabilityAssessment()).append(". ");
        }
        if (evolutionResponse.getArchitecturalImpact() != null) {
            sb.append("Architecture: ").append(evolutionResponse.getArchitecturalImpact()).append(". ");
        }
        if (evolutionResponse.getConventionCompliance() != null) {
            sb.append("Conventions: ").append(evolutionResponse.getConventionCompliance()).append(". ");
        }
        if (evolutionResponse.getRepositoryEvolutionScore() != null) {
            sb.append("Evolution Score: ").append(evolutionResponse.getRepositoryEvolutionScore()).append("/100.");
        }
        return sb.toString().trim();
    }

    private String buildUnifiedSummary(KnowledgeReport report) {
        StringBuilder sb = new StringBuilder();
        sb.append("Knowledge Report for repository '").append(report.getRepositoryName()).append("'");
        if (report.getBranch() != null) {
            sb.append(" on branch '").append(report.getBranch()).append("'");
        }
        sb.append(". Domain: ").append(report.getKnowledgeDomain()).append(". ");

        if (report.getRepositoryOverview() != null) {
            sb.append(report.getRepositoryOverview()).append(" ");
        }
        if (report.getArchitectureSummary() != null) {
            sb.append(report.getArchitectureSummary()).append(" ");
        }
        if (report.getRepositoryHealth() != null) {
            sb.append(report.getRepositoryHealth()).append(" ");
        }
        sb.append("Active sessions: ").append(report.getActiveSessions().size()).append(". ");
        sb.append("Workflow summaries: ").append(report.getWorkflowSummaries().size()).append(". ");
        sb.append("Risks identified: ").append(report.getRisks().size()).append(". ");
        sb.append("Recommendations: ").append(report.getRecommendations().size()).append(". ");
        sb.append("Evolution insights: ").append(report.getEvolutionInsights().size()).append(". ");
        sb.append("Component relationships: ").append(report.getComponentRelationships().size()).append(".");

        return sb.toString();
    }

    private KnowledgeReport.ReportMetadata buildReportMetadata(String repositoryName, String branch) {
        KnowledgeReport.ReportMetadata metadata = new KnowledgeReport.ReportMetadata();
        try {
            metadata.setTotalSessions(sessionService.getTotalSessionCount());
            metadata.setActiveSessionCount(sessionService.getActiveSessionCount());
        } catch (Exception e) {
            logger.warn("Failed to get session metadata: {}", e.getMessage());
        }
        try {
            KnowledgeGraphReport kgReport = knowledgeGraphService
                    .generateKnowledgeGraphReport(repositoryName, branch);
            if (kgReport.getGraphStatistics() != null) {
                metadata.setKnowledgeGraphSummary(
                        kgReport.getGraphStatistics().getTotalNodes() + " nodes, "
                                + kgReport.getGraphStatistics().getTotalEdges() + " edges");
            }
        } catch (Exception e) {
            logger.warn("Failed to get knowledge graph metadata: {}", e.getMessage());
        }
        return metadata;
    }
}