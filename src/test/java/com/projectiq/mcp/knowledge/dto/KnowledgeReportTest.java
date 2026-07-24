package com.projectiq.mcp.knowledge.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeReportTest {

    @Test
    void testDefaultConstructor() {
        KnowledgeReport report = new KnowledgeReport();
        assertEquals("SUCCESS", report.getStatus());
        assertTrue(report.getGeneratedAtMillis() > 0);
        assertNotNull(report.getComponentRelationships());
        assertNotNull(report.getActiveSessions());
        assertNotNull(report.getWorkflowSummaries());
        assertNotNull(report.getRisks());
        assertNotNull(report.getRecommendations());
        assertNotNull(report.getEvolutionInsights());
        assertTrue(report.getComponentRelationships().isEmpty());
    }

    @Test
    void testSettersAndGetters() {
        KnowledgeReport report = new KnowledgeReport();
        report.setRepositoryName("test-repo");
        report.setBranch("main");
        report.setQuery("Show me architecture");
        report.setKnowledgeDomain("Architecture");
        report.setRepositoryOverview("Test overview");
        report.setArchitectureSummary("Test architecture");
        report.setRepositoryHealth("Health: 85/100");
        report.setUnifiedSummary("Unified summary");
        report.setStatus("SUCCESS");
        report.setErrorMessage(null);
        report.setGenerationDurationMillis(150L);

        assertEquals("test-repo", report.getRepositoryName());
        assertEquals("main", report.getBranch());
        assertEquals("Show me architecture", report.getQuery());
        assertEquals("Architecture", report.getKnowledgeDomain());
        assertEquals("Test overview", report.getRepositoryOverview());
        assertEquals("Test architecture", report.getArchitectureSummary());
        assertEquals("Health: 85/100", report.getRepositoryHealth());
        assertEquals("Unified summary", report.getUnifiedSummary());
        assertEquals("SUCCESS", report.getStatus());
        assertNull(report.getErrorMessage());
        assertEquals(150L, report.getGenerationDurationMillis());
    }

    @Test
    void testListSetters() {
        KnowledgeReport report = new KnowledgeReport();

        List<String> components = Arrays.asList("Component1", "Component2");
        report.setComponentRelationships(components);
        assertEquals(2, report.getComponentRelationships().size());
        assertTrue(report.getComponentRelationships().contains("Component1"));

        List<String> sessions = Arrays.asList("Session1");
        report.setActiveSessions(sessions);
        assertEquals(1, report.getActiveSessions().size());

        List<String> workflows = Arrays.asList("Workflow1", "Workflow2");
        report.setWorkflowSummaries(workflows);
        assertEquals(2, report.getWorkflowSummaries().size());

        List<String> risks = Arrays.asList("Risk1");
        report.setRisks(risks);
        assertEquals(1, report.getRisks().size());

        List<String> recommendations = Arrays.asList("Rec1", "Rec2", "Rec3");
        report.setRecommendations(recommendations);
        assertEquals(3, report.getRecommendations().size());

        List<String> insights = Arrays.asList("Insight1");
        report.setEvolutionInsights(insights);
        assertEquals(1, report.getEvolutionInsights().size());
    }

    @Test
    void testNullListSetters() {
        KnowledgeReport report = new KnowledgeReport();
        report.setComponentRelationships(null);
        report.setActiveSessions(null);
        report.setWorkflowSummaries(null);
        report.setRisks(null);
        report.setRecommendations(null);
        report.setEvolutionInsights(null);

        assertNotNull(report.getComponentRelationships());
        assertNotNull(report.getActiveSessions());
        assertNotNull(report.getWorkflowSummaries());
        assertNotNull(report.getRisks());
        assertNotNull(report.getRecommendations());
        assertNotNull(report.getEvolutionInsights());
        assertTrue(report.getComponentRelationships().isEmpty());
    }

    @Test
    void testReportMetadata() {
        KnowledgeReport.ReportMetadata metadata = new KnowledgeReport.ReportMetadata();
        metadata.setTotalSessions(10);
        metadata.setActiveSessionCount(3);
        metadata.setCompletedSessionCount(7);
        metadata.setTotalWorkflowCount(5);
        metadata.setTotalRecommendations(15);
        metadata.setTotalRisks(2);
        metadata.setTotalComponentRelationships(20);
        metadata.setKnowledgeGraphSummary("50 nodes, 100 edges");

        assertEquals(10, metadata.getTotalSessions());
        assertEquals(3, metadata.getActiveSessionCount());
        assertEquals(7, metadata.getCompletedSessionCount());
        assertEquals(5, metadata.getTotalWorkflowCount());
        assertEquals(15, metadata.getTotalRecommendations());
        assertEquals(2, metadata.getTotalRisks());
        assertEquals(20, metadata.getTotalComponentRelationships());
        assertEquals("50 nodes, 100 edges", metadata.getKnowledgeGraphSummary());
    }

    @Test
    void testErrorStatus() {
        KnowledgeReport report = new KnowledgeReport();
        report.setStatus("ERROR");
        report.setErrorMessage("Something went wrong");
        assertEquals("ERROR", report.getStatus());
        assertEquals("Something went wrong", report.getErrorMessage());
    }

    @Test
    void testKnowledgeDomainFromQuery() {
        assertEquals(KnowledgeDomain.ALL, KnowledgeDomain.fromQuery(null));
        assertEquals(KnowledgeDomain.ALL, KnowledgeDomain.fromQuery(""));
        assertEquals(KnowledgeDomain.ARCHITECTURE, KnowledgeDomain.fromQuery("architecture"));
        assertEquals(KnowledgeDomain.ARCHITECTURE, KnowledgeDomain.fromQuery("Show me architecture"));
        assertEquals(KnowledgeDomain.DEPENDENCIES, KnowledgeDomain.fromQuery("dependencies"));
        assertEquals(KnowledgeDomain.DEVELOPMENT_SESSIONS, KnowledgeDomain.fromQuery("sessions"));
        assertEquals(KnowledgeDomain.KNOWLEDGE_GRAPH, KnowledgeDomain.fromQuery("knowledge graph"));
        assertEquals(KnowledgeDomain.ALL, KnowledgeDomain.fromQuery("unknown query"));
    }
}