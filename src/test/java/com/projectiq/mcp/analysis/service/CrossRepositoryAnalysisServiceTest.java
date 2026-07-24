package com.projectiq.mcp.analysis.service;

import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse;
import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse.RepositorySummary;
import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.dto.DependencyInfo;
import com.projectiq.mcp.client.dto.DependencyRequest;
import com.projectiq.mcp.client.dto.DependencyResponse;
import com.projectiq.mcp.client.dto.PackageSummary;
import com.projectiq.mcp.client.dto.ClassSummary;
import com.projectiq.mcp.client.dto.RestApiRequest;
import com.projectiq.mcp.client.dto.RestApiResponse;
import com.projectiq.mcp.client.dto.RestEndpointInfo;
import com.projectiq.mcp.client.dto.RepositorySummaryRequest;
import com.projectiq.mcp.client.dto.RepositorySummaryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for CrossRepositoryAnalysisService.
 */
@ExtendWith(MockitoExtension.class)
class CrossRepositoryAnalysisServiceTest {

    @Mock
    private IndexerRestClient indexerRestClient;

    @Mock
    private ArchitectureInsightsService architectureInsightsService;

    @Mock
    private RepositoryConventionAnalyzerService conventionAnalyzerService;

    @Mock
    private RepositoryHealthService healthService;

    private CrossRepositoryAnalysisService service;

    @BeforeEach
    void setUp() {
        service = new CrossRepositoryAnalysisService(
                indexerRestClient, architectureInsightsService,
                conventionAnalyzerService, healthService);
    }

    @Test
    void testAnalyzeCrossRepository_EmptyList() {
        CrossRepositoryAnalysisResponse response = service.analyzeCrossRepository(new ArrayList<>());
        assertNotNull(response);
        assertNotNull(response.getAnalysisId());
        assertTrue(response.getRepositories().isEmpty());
    }

    @Test
    void testAnalyzeCrossRepository_NullList() {
        CrossRepositoryAnalysisResponse response = service.analyzeCrossRepository(null);
        assertNotNull(response);
        assertNotNull(response.getAnalysisId());
        assertTrue(response.getRepositories().isEmpty());
    }

    @Test
    void testAnalyzeCrossRepository_SingleRepository() {
        String repoName = "test-repo";
        RepositorySummaryResponse summaryResponse = createMockSummaryResponse(repoName);
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(summaryResponse);

        var archResponse = createMockArchitectureResponse();
        when(architectureInsightsService.analyzeArchitecture(anyString(), anyString()))
                .thenReturn(archResponse);

        CrossRepositoryAnalysisResponse response = service.analyzeCrossRepository(List.of(repoName));

        assertNotNull(response);
        assertEquals(1, response.getRepositories().size());
        assertEquals(repoName, response.getRepositories().get(0).getRepositoryName());
        assertNotNull(response.getCommonArchitecture());
        assertNotNull(response.getSharedComponents());
        assertNotNull(response.getDependencyComparison());
        assertNotNull(response.getArchitecturalDifferences());
        assertNotNull(response.getRiskAssessment());
    }

    @Test
    void testAnalyzeCrossRepository_MultipleRepositories() {
        String repo1 = "repo1";
        String repo2 = "repo2";

        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(createMockSummaryResponse(repo1))
                .thenReturn(createMockSummaryResponse(repo2));

        var archResponse = createMockArchitectureResponse();
        when(architectureInsightsService.analyzeArchitecture(anyString(), anyString()))
                .thenReturn(archResponse);

        CrossRepositoryAnalysisResponse response = service.analyzeCrossRepository(List.of(repo1, repo2));

        assertNotNull(response);
        assertEquals(2, response.getRepositories().size());
        assertNotNull(response.getCommonArchitecture());
    }

    @Test
    void testAnalyzeCrossRepository_WithInvalidRepository() {
        String validRepo = "valid-repo";
        String invalidRepo = "invalid-repo";

        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenThrow(new RuntimeException("Connection failed"))
                .thenReturn(createMockSummaryResponse(validRepo));

        var archResponse = createMockArchitectureResponse();
        when(architectureInsightsService.analyzeArchitecture(eq(validRepo), anyString()))
                .thenReturn(archResponse);

        CrossRepositoryAnalysisResponse response = service.analyzeCrossRepository(List.of(invalidRepo, validRepo));

        assertNotNull(response);
        // Only valid repo should be in results
        assertEquals(1, response.getRepositories().size());
        assertEquals(validRepo, response.getRepositories().get(0).getRepositoryName());
        // Risk should be present since < 2 repos available
        assertNotNull(response.getRiskAssessment());
    }

    @Test
    void testRetrieveAllSummaries_HandlesExceptions() {
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenThrow(new RuntimeException("Error"));

        Map<String, RepositorySummaryResponse> summaries = service.retrieveAllSummaries(List.of("repo1", "repo2"));
        assertTrue(summaries.isEmpty());
    }

    @Test
    void testBuildRepositorySummaries() {
        when(architectureInsightsService.analyzeArchitecture(anyString(), anyString()))
                .thenReturn(createMockArchitectureResponse());

        Map<String, RepositorySummaryResponse> summaries = Map.of(
                "repo1", createMockSummaryResponse("repo1")
        );

        List<RepositorySummary> results = service.buildRepositorySummaries(summaries);
        assertEquals(1, results.size());
        assertEquals("repo1", results.get(0).getRepositoryName());
        assertEquals("main", results.get(0).getBranch());
    }

    @Test
    void testCompareArchitectures() {
        var archResponse = createMockArchitectureResponse();
        when(architectureInsightsService.analyzeArchitecture(anyString(), anyString()))
                .thenReturn(archResponse);

        Map<String, RepositorySummaryResponse> summaries = Map.of(
                "repo1", createMockSummaryResponse("repo1"),
                "repo2", createMockSummaryResponse("repo2")
        );

        var commonArch = service.compareArchitectures(summaries);
        assertNotNull(commonArch);
        assertNotNull(commonArch.getSharedArchitecturalStyles());
        assertNotNull(commonArch.getCommonLayers());
        assertNotNull(commonArch.getSharedPatterns());
        assertTrue(commonArch.getArchitectureSimilarityScore() >= 0.0);
    }

    @Test
    void testIdentifySharedComponents() {
        PackageSummary pkg1 = new PackageSummary();
        pkg1.setPackageName("com.company.module1");
        ClassSummary cls1 = new ClassSummary();
        cls1.setClassName("UserService");
        pkg1.setClasses(List.of(cls1));

        PackageSummary pkg2 = new PackageSummary();
        pkg2.setPackageName("com.company.module2");

        RepositorySummaryResponse summary1 = createMockSummaryResponse("repo1");
        summary1.setPackages(List.of(pkg1));
        RepositorySummaryResponse summary2 = createMockSummaryResponse("repo2");
        summary2.setPackages(List.of(pkg2));

        Map<String, RepositorySummaryResponse> summaries = Map.of("repo1", summary1, "repo2", summary2);
        var sc = service.identifySharedComponents(summaries);

        assertNotNull(sc);
        assertNotNull(sc.getCommonClassNames());
        assertNotNull(sc.getCommonPackagePrefixes());
        assertNotNull(sc.getComponentMatches());
    }

    @Test
    void testCompareApis() {
        // Mock REST API response
        RestApiResponse apiResponse = new RestApiResponse();
        RestEndpointInfo endpoint = new RestEndpointInfo();
        endpoint.setEndpointPath("/api/users");
        endpoint.setHttpMethod("GET");
        apiResponse.setEndpoints(List.of(endpoint));
        when(indexerRestClient.findRestApi(any(RestApiRequest.class)))
                .thenReturn(apiResponse);

        Map<String, RepositorySummaryResponse> summaries = Map.of(
                "repo1", createMockSummaryResponse("repo1"),
                "repo2", createMockSummaryResponse("repo2")
        );

        var sa = service.compareApis(summaries);
        assertNotNull(sa);
        assertNotNull(sa.getSimilarEndpoints());
    }

    @Test
    void testCompareDependencies() {
        DependencyResponse depResponse = new DependencyResponse();
        DependencyInfo dep = new DependencyInfo();
        dep.setName("spring-boot");
        dep.setGroupId("org.springframework.boot");
        dep.setArtifactId("spring-boot-starter");
        dep.setVersion("3.0.0");
        depResponse.setDependencies(List.of(dep));
        when(indexerRestClient.findDependency(any(DependencyRequest.class)))
                .thenReturn(depResponse);

        Map<String, RepositorySummaryResponse> summaries = Map.of(
                "repo1", createMockSummaryResponse("repo1"),
                "repo2", createMockSummaryResponse("repo2")
        );

        var dc = service.compareDependencies(summaries);
        assertNotNull(dc);
        assertNotNull(dc.getCommonDependencies());
        assertNotNull(dc.getUniqueDependencies());
    }

    @Test
    void testRiskAssessment_LowRisk() {
        Map<String, RepositorySummaryResponse> summaries = Map.of(
                "repo1", createMockSummaryResponse("repo1")
        );

        var depComparison = new CrossRepositoryAnalysisResponse.DependencyComparison();
        var archDifferences = new CrossRepositoryAnalysisResponse.ArchitecturalDifferences();

        var ra = service.assessRisks(summaries, depComparison, archDifferences);
        assertNotNull(ra);
        assertNotNull(ra.getOverallRiskLevel());
    }

    private RepositorySummaryResponse createMockSummaryResponse(String repoName) {
        RepositorySummaryResponse summary = new RepositorySummaryResponse();
        summary.setRepositoryName(repoName);
        summary.setBranch("main");
        summary.setPackageCount(5);
        summary.setClassCount(20);
        summary.setMethodCount(100);
        summary.setFileCount(15);
        summary.setCommitCount(50);
        summary.setPackages(new ArrayList<>());
        return summary;
    }

    private com.projectiq.mcp.analysis.dto.ArchitectureInsightsResponse createMockArchitectureResponse() {
        var archResponse = new com.projectiq.mcp.analysis.dto.ArchitectureInsightsResponse();
        archResponse.setArchitecturalStyle("Layered Architecture");
        archResponse.setDetectedLayers(List.of("Controller", "Service", "Repository"));
        archResponse.setArchitecturalStrengths(List.of("Repository Pattern", "Service Layer Pattern"));
        archResponse.setConfidenceLevel("HIGH");
        return archResponse;
    }
}