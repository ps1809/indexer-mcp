package com.projectiq.mcp.analysis.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse.ApiMatch;
import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse.ArchitecturalDifferences;
import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse.CommonArchitecture;
import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse.ComponentMatch;
import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse.ConventionComparison;
import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse.DependencyComparison;
import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse.DependencyEntry;
import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse.DependencyVersionDiff;
import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse.RepositorySummary;
import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse.ReuseOpportunities;
import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse.RiskAssessment;
import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse.SharedComponents;
import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse.SimilarApis;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CrossRepositoryAnalysisResponse DTO.
 */
class CrossRepositoryAnalysisResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testDefaultConstructor() {
        CrossRepositoryAnalysisResponse response = new CrossRepositoryAnalysisResponse();
        assertNotNull(response.getRepositories());
        assertNotNull(response.getCommonArchitecture());
        assertNotNull(response.getSharedComponents());
        assertNotNull(response.getSimilarApis());
        assertNotNull(response.getDependencyComparison());
        assertNotNull(response.getConventionComparison());
        assertNotNull(response.getReuseOpportunities());
        assertNotNull(response.getArchitecturalDifferences());
        assertNotNull(response.getRiskAssessment());
        assertTrue(response.getRepositories().isEmpty());
    }

    @Test
    void testSettersAndGetters() {
        CrossRepositoryAnalysisResponse response = new CrossRepositoryAnalysisResponse();
        response.setAnalysisId("test-123");

        RepositorySummary repo = new RepositorySummary();
        repo.setRepositoryName("test-repo");
        repo.setBranch("main");
        repo.setArchitecturalStyle("Layered Architecture");
        repo.setPackageCount(10);
        repo.setClassCount(50);
        repo.setMethodCount(200);
        repo.setFileCount(30);
        repo.setCommitCount(100);
        repo.setDetectedLayers(List.of("Controller", "Service"));

        response.setRepositories(List.of(repo));

        assertEquals("test-123", response.getAnalysisId());
        assertEquals(1, response.getRepositories().size());
        assertEquals("test-repo", response.getRepositories().get(0).getRepositoryName());
        assertEquals("main", response.getRepositories().get(0).getBranch());
        assertEquals("Layered Architecture", response.getRepositories().get(0).getArchitecturalStyle());
        assertEquals(10, response.getRepositories().get(0).getPackageCount());
        assertEquals(50, response.getRepositories().get(0).getClassCount());
        assertEquals(200, response.getRepositories().get(0).getMethodCount());
        assertEquals(30, response.getRepositories().get(0).getFileCount());
        assertEquals(100, response.getRepositories().get(0).getCommitCount());
        assertEquals(2, response.getRepositories().get(0).getDetectedLayers().size());
    }

    @Test
    void testCommonArchitecture() {
        CommonArchitecture ca = new CommonArchitecture();
        ca.setSharedArchitecturalStyles(List.of("Layered Architecture"));
        ca.setCommonLayers(List.of("Controller", "Service", "Repository"));
        ca.setSharedPatterns(List.of("Repository Pattern", "Service Layer Pattern"));
        ca.setArchitectureSimilarityScore(0.85);

        assertEquals(1, ca.getSharedArchitecturalStyles().size());
        assertEquals(3, ca.getCommonLayers().size());
        assertEquals(2, ca.getSharedPatterns().size());
        assertEquals(0.85, ca.getArchitectureSimilarityScore());
    }

    @Test
    void testSharedComponents() {
        SharedComponents sc = new SharedComponents();
        sc.setCommonClassNames(List.of("Application", "Config"));
        sc.setCommonPackagePrefixes(List.of("com.company", "org.framework"));
        sc.setCommonAnnotations(List.of("@Service", "@Repository"));

        ComponentMatch match = new ComponentMatch("UserService", "Class", List.of("repo1", "repo2"));
        match.setDescription("A shared service component");
        sc.setComponentMatches(List.of(match));

        assertEquals(2, sc.getCommonClassNames().size());
        assertEquals(2, sc.getCommonPackagePrefixes().size());
        assertEquals(2, sc.getCommonAnnotations().size());
        assertEquals(1, sc.getComponentMatches().size());
        assertEquals("UserService", sc.getComponentMatches().get(0).getComponentName());
        assertEquals("Class", sc.getComponentMatches().get(0).getComponentType());
        assertEquals("A shared service component", sc.getComponentMatches().get(0).getDescription());
    }

    @Test
    void testSimilarApis() {
        SimilarApis sa = new SimilarApis();
        ApiMatch match = new ApiMatch("/api/users", "GET", List.of("repo1", "repo2"));
        match.setDescription("User API endpoint");
        sa.setSimilarEndpoints(List.of(match));
        sa.setCommonHttpMethods(List.of("GET", "POST"));
        sa.setCommonMediaTypes(List.of("application/json"));
        sa.setTotalSimilarEndpoints(1);

        assertEquals(1, sa.getTotalSimilarEndpoints());
        assertEquals("/api/users", sa.getSimilarEndpoints().get(0).getPath());
        assertEquals("GET", sa.getSimilarEndpoints().get(0).getHttpMethod());
        assertEquals("User API endpoint", sa.getSimilarEndpoints().get(0).getDescription());
    }

    @Test
    void testDependencyComparison() {
        DependencyComparison dc = new DependencyComparison();

        DependencyEntry common = new DependencyEntry();
        common.setName("spring-boot-starter-web");
        common.setType("common");
        common.setPresentInRepositories(List.of("repo1", "repo2"));

        DependencyEntry unique = new DependencyEntry();
        unique.setName("custom-library");
        unique.setType("unique");
        unique.setPresentInRepositories(List.of("repo1"));

        dc.setCommonDependencies(List.of(common));
        dc.setUniqueDependencies(List.of(unique));
        dc.setTotalCommonDependencies(1);
        dc.setTotalUniqueDependencies(1);

        DependencyVersionDiff diff = new DependencyVersionDiff();
        diff.setName("spring-boot-starter-web");
        Map<String, String> versions = new LinkedHashMap<>();
        versions.put("repo1", "3.0.0");
        versions.put("repo2", "2.7.0");
        diff.setVersions(versions);
        dc.setVersionDifferences(List.of(diff));

        assertEquals(1, dc.getTotalCommonDependencies());
        assertEquals(1, dc.getTotalUniqueDependencies());
        assertEquals(1, dc.getVersionDifferences().size());
        assertEquals("3.0.0", dc.getVersionDifferences().get(0).getVersions().get("repo1"));
    }

    @Test
    void testConventionComparison() {
        ConventionComparison cc = new ConventionComparison();
        cc.setCommonNamingConventions(List.of("CamelCase", "PascalCase"));
        cc.setCommonPackageConventions(List.of("Layer-based packaging"));
        cc.setCommonTestingConventions(List.of("JUnit", "Mockito"));
        cc.setCommonAnnotationConventions(List.of("Spring Annotations"));
        cc.setCommonRestApiConventions(List.of("Spring MVC Annotations"));
        cc.setCommonArchitecturalConventions(List.of("MVC", "Layered"));
        cc.setConventionSimilarityScore(0.75);

        assertEquals(2, cc.getCommonNamingConventions().size());
        assertEquals(1, cc.getCommonPackageConventions().size());
        assertEquals(2, cc.getCommonTestingConventions().size());
        assertEquals(1, cc.getCommonAnnotationConventions().size());
        assertEquals(1, cc.getCommonRestApiConventions().size());
        assertEquals(2, cc.getCommonArchitecturalConventions().size());
        assertEquals(0.75, cc.getConventionSimilarityScore());
    }

    @Test
    void testReuseOpportunities() {
        ReuseOpportunities ro = new ReuseOpportunities();
        ro.setPotentialSharedLibraries(List.of("spring-boot", "common-utils"));
        ro.setExtractableCommonServices(List.of("UserService", "ConfigService"));
        ro.setSharedConfigurationCandidates(List.of("Package prefix: com.company"));
        ro.setReusableApiContracts(List.of("GET /api/users"));
        ro.setTotalReuseOpportunities(5);

        assertEquals(2, ro.getPotentialSharedLibraries().size());
        assertEquals(2, ro.getExtractableCommonServices().size());
        assertEquals(1, ro.getSharedConfigurationCandidates().size());
        assertEquals(1, ro.getReusableApiContracts().size());
        assertEquals(5, ro.getTotalReuseOpportunities());
    }

    @Test
    void testArchitecturalDifferences() {
        ArchitecturalDifferences ad = new ArchitecturalDifferences();
        ad.setDifferentArchitecturalStyles(List.of("Layered", "Microservice"));
        ad.setUniqueLayers(List.of("Controller", "Service"));
        ad.setUniquePatterns(List.of("Repository Pattern", "Factory Pattern"));
        ad.setArchitectureGapDescription(List.of("Different architectural styles detected"));
        ad.setDifferenceScore(0.6);

        assertEquals(2, ad.getDifferentArchitecturalStyles().size());
        assertEquals(2, ad.getUniqueLayers().size());
        assertEquals(2, ad.getUniquePatterns().size());
        assertEquals(1, ad.getArchitectureGapDescription().size());
        assertEquals(0.6, ad.getDifferenceScore());
    }

    @Test
    void testRiskAssessment() {
        RiskAssessment ra = new RiskAssessment();
        ra.setRisks(List.of("Dependency version mismatch detected"));
        ra.setIncompatibilities(List.of("Different architectural styles"));
        ra.setIntegrationChallenges(List.of("May require adapters"));
        ra.setOverallRiskLevel("MEDIUM");

        assertEquals(1, ra.getRisks().size());
        assertEquals(1, ra.getIncompatibilities().size());
        assertEquals(1, ra.getIntegrationChallenges().size());
        assertEquals("MEDIUM", ra.getOverallRiskLevel());
    }

    @Test
    void testSetRepositoriesWithNull() {
        CrossRepositoryAnalysisResponse response = new CrossRepositoryAnalysisResponse();
        response.setRepositories(null);
        assertNotNull(response.getRepositories());
        assertTrue(response.getRepositories().isEmpty());
    }

    @Test
    void testJsonSerialization() throws Exception {
        CrossRepositoryAnalysisResponse response = new CrossRepositoryAnalysisResponse();
        response.setAnalysisId("test-123");

        RepositorySummary repo = new RepositorySummary();
        repo.setRepositoryName("repo1");
        repo.setBranch("main");
        repo.setArchitecturalStyle("Layered");
        response.setRepositories(List.of(repo));

        String json = objectMapper.writeValueAsString(response);
        assertNotNull(json);
        assertTrue(json.contains("repo1"));
        assertTrue(json.contains("test-123"));
        assertTrue(json.contains("Layered"));

        // Deserialize back
        CrossRepositoryAnalysisResponse deserialized = objectMapper.readValue(json, CrossRepositoryAnalysisResponse.class);
        assertEquals("test-123", deserialized.getAnalysisId());
        assertEquals(1, deserialized.getRepositories().size());
        assertEquals("repo1", deserialized.getRepositories().get(0).getRepositoryName());
    }
}