package com.projectiq.mcp.analysis.service;

import com.projectiq.mcp.analysis.dto.ArchitecturalDecisionResponse;
import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.dto.RepositorySummaryRequest;
import com.projectiq.mcp.client.dto.RepositorySummaryResponse;
import com.projectiq.mcp.knowledgegraph.service.RepositoryKnowledgeGraphService;
import com.projectiq.mcp.strategy.service.DevelopmentStrategyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArchitecturalDecisionServiceTest {

    @Mock
    private IndexerRestClient indexerRestClient;

    @Mock
    private ArchitectureInsightsService architectureInsightsService;

    @Mock
    private RepositoryKnowledgeGraphService knowledgeGraphService;

    @Mock
    private DevelopmentStrategyService developmentStrategyService;

    @Mock
    private CrossRepositoryAnalysisService crossRepositoryAnalysisService;

    @Mock
    private CodeChangeAnalysisService codeChangeAnalysisService;

    private ArchitecturalDecisionService service;

    @BeforeEach
    void setUp() {
        service = new ArchitecturalDecisionService(
                indexerRestClient, architectureInsightsService,
                knowledgeGraphService, developmentStrategyService,
                crossRepositoryAnalysisService, codeChangeAnalysisService);
    }

    @Test
    void testAdviseArchitecture_NewServiceVsExisting() {
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(createMockSummary());

        ArchitecturalDecisionResponse response = service.adviseArchitecture(
                ArchitecturalDecisionResponse.DecisionCategory.NEW_SERVICE_VS_EXISTING,
                "Should we create a new payment service?",
                "my-repo");

        assertNotNull(response);
        assertNotNull(response.getDecisionId());
        assertEquals(ArchitecturalDecisionResponse.DecisionCategory.NEW_SERVICE_VS_EXISTING,
                response.getDecisionCategory());
        assertEquals(2, response.getAlternatives().size());
        assertNotNull(response.getRecommendedApproach());
        assertNotNull(response.getDecisionRationale());
        assertNotNull(response.getRepositoryImpact());
        assertNotNull(response.getDependencyImplications());
        assertNotNull(response.getScalabilityAssessment());
        assertNotNull(response.getMaintainabilityAssessment());
    }

    @Test
    void testAdviseArchitecture_NewModuleVsExisting() {
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(createMockSummary());

        ArchitecturalDecisionResponse response = service.adviseArchitecture(
                ArchitecturalDecisionResponse.DecisionCategory.NEW_MODULE_VS_EXISTING,
                "Should we create a new module?",
                "my-repo");

        assertNotNull(response);
        assertEquals(2, response.getAlternatives().size());
        assertEquals("Create New Module", response.getRecommendedApproach());
    }

    @Test
    void testAdviseArchitecture_ExtendApiVsCreateApi() {
        ArchitecturalDecisionResponse response = service.adviseArchitecture(
                ArchitecturalDecisionResponse.DecisionCategory.EXTEND_API_VS_CREATE_API,
                "Should we extend existing API or create new?",
                "my-repo");

        assertNotNull(response);
        assertEquals(2, response.getAlternatives().size());
        assertEquals("Create New API", response.getRecommendedApproach());
    }

    @Test
    void testAdviseArchitecture_EventDrivenVsSynchronous() {
        ArchitecturalDecisionResponse response = service.adviseArchitecture(
                ArchitecturalDecisionResponse.DecisionCategory.EVENT_DRIVEN_VS_SYNCHRONOUS,
                "Should we use event-driven or sync?",
                "my-repo");

        assertNotNull(response);
        assertEquals(2, response.getAlternatives().size());
        assertEquals("Synchronous Communication", response.getRecommendedApproach());
    }

    @Test
    void testAdviseArchitecture_CompositionVsInheritance() {
        ArchitecturalDecisionResponse response = service.adviseArchitecture(
                ArchitecturalDecisionResponse.DecisionCategory.COMPOSITION_VS_INHERITANCE,
                "Should we use composition or inheritance?",
                "my-repo");

        assertNotNull(response);
        assertEquals(2, response.getAlternatives().size());
        assertEquals("Composition", response.getRecommendedApproach());
    }

    @Test
    void testAdviseArchitecture_ConfigurationVsCode() {
        ArchitecturalDecisionResponse response = service.adviseArchitecture(
                ArchitecturalDecisionResponse.DecisionCategory.CONFIGURATION_VS_CODE,
                "Should we use config or code?",
                "my-repo");

        assertNotNull(response);
        assertEquals(2, response.getAlternatives().size());
        assertEquals("Configuration-Driven", response.getRecommendedApproach());
    }

    @Test
    void testAdviseArchitecture_SharedVsDedicated() {
        ArchitecturalDecisionResponse response = service.adviseArchitecture(
                ArchitecturalDecisionResponse.DecisionCategory.SHARED_VS_DEDICATED,
                "Should we use shared or dedicated?",
                "my-repo");

        assertNotNull(response);
        assertEquals(2, response.getAlternatives().size());
        assertEquals("Dedicated Component", response.getRecommendedApproach());
    }

    @Test
    void testAdviseArchitecture_PackageOrganization() {
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(createMockSummary());

        ArchitecturalDecisionResponse response = service.adviseArchitecture(
                ArchitecturalDecisionResponse.DecisionCategory.PACKAGE_ORGANIZATION,
                "How should we organize packages?",
                "my-repo");

        assertNotNull(response);
        assertEquals(2, response.getAlternatives().size());
        assertNotNull(response.getRecommendedApproach());
    }

    @Test
    void testAdviseArchitecture_UnsupportedCategory() {
        ArchitecturalDecisionResponse response = service.adviseArchitecture(
                "Unsupported Category",
                "Some description",
                "my-repo");

        assertNotNull(response);
        assertNotNull(response.getWarning());
        assertTrue(response.getWarning().contains("Unsupported decision category"));
        assertEquals("Unable to evaluate - unsupported category", response.getRecommendedApproach());
    }

    @Test
    void testAdviseArchitecture_MissingRepository() {
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenThrow(new RuntimeException("Not found"));

        ArchitecturalDecisionResponse response = service.adviseArchitecture(
                ArchitecturalDecisionResponse.DecisionCategory.COMPOSITION_VS_INHERITANCE,
                "Test decision",
                "non-existent-repo");

        assertNotNull(response);
        assertNotNull(response.getWarning());
        assertTrue(response.getWarning().contains("not found"));
        assertNotNull(response.getRecommendedApproach());
    }

    @Test
    void testGetSupportedCategories() {
        var categories = service.getSupportedCategories();
        assertEquals(8, categories.size());
        assertTrue(categories.contains(ArchitecturalDecisionResponse.DecisionCategory.NEW_SERVICE_VS_EXISTING));
        assertTrue(categories.contains(ArchitecturalDecisionResponse.DecisionCategory.PACKAGE_ORGANIZATION));
    }

    private RepositorySummaryResponse createMockSummary() {
        RepositorySummaryResponse summary = new RepositorySummaryResponse();
        summary.setRepositoryName("my-repo");
        summary.setBranch("main");
        summary.setPackageCount(10);
        summary.setClassCount(50);
        summary.setMethodCount(200);
        summary.setFileCount(30);
        summary.setCommitCount(100);
        return summary;
    }
}