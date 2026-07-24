package com.projectiq.mcp.analysis.dto;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryHealthResponseTest {

    @Test
    void defaultConstructor_initializesEmptyLists() {
        RepositoryHealthResponse response = new RepositoryHealthResponse();
        assertNotNull(response.getStrengths());
        assertNotNull(response.getObservations());
        assertNotNull(response.getPotentialRisks());
        assertNotNull(response.getSuggestedReviewAreas());
        assertTrue(response.getStrengths().isEmpty());
        assertTrue(response.getObservations().isEmpty());
        assertTrue(response.getPotentialRisks().isEmpty());
        assertTrue(response.getSuggestedReviewAreas().isEmpty());
    }

    @Test
    void setAndGetRepositoryName() {
        RepositoryHealthResponse response = new RepositoryHealthResponse();
        response.setRepositoryName("my-project");
        assertEquals("my-project", response.getRepositoryName());
    }

    @Test
    void setAndGetBranch() {
        RepositoryHealthResponse response = new RepositoryHealthResponse();
        response.setBranch("main");
        assertEquals("main", response.getBranch());
    }

    @Test
    void setAndGetRepositoryOverview() {
        RepositoryHealthResponse response = new RepositoryHealthResponse();
        response.setRepositoryOverview("Repository contains 10 packages");
        assertEquals("Repository contains 10 packages", response.getRepositoryOverview());
    }

    @Test
    void setAndGetHealthScore() {
        RepositoryHealthResponse response = new RepositoryHealthResponse();
        response.setHealthScore(75);
        assertEquals(75, response.getHealthScore());
    }

    @Test
    void setAndGetMaintainabilityRating() {
        RepositoryHealthResponse response = new RepositoryHealthResponse();
        response.setMaintainabilityRating("Good");
        assertEquals("Good", response.getMaintainabilityRating());
    }

    @Test
    void setAndGetComplexityRating() {
        RepositoryHealthResponse response = new RepositoryHealthResponse();
        response.setComplexityRating("Moderate");
        assertEquals("Moderate", response.getComplexityRating());
    }

    @Test
    void setAndGetArchitectureConsistency() {
        RepositoryHealthResponse response = new RepositoryHealthResponse();
        response.setArchitectureConsistency("Consistent");
        assertEquals("Consistent", response.getArchitectureConsistency());
    }

    @Test
    void setAndGetDependencyHealth() {
        RepositoryHealthResponse response = new RepositoryHealthResponse();
        response.setDependencyHealth("Healthy");
        assertEquals("Healthy", response.getDependencyHealth());
    }

    @Test
    void setAndGetTestingMaturity() {
        RepositoryHealthResponse response = new RepositoryHealthResponse();
        response.setTestingMaturity("Mature");
        assertEquals("Mature", response.getTestingMaturity());
    }

    @Test
    void setAndGetDocumentationMaturity() {
        RepositoryHealthResponse response = new RepositoryHealthResponse();
        response.setDocumentationMaturity("Adequate");
        assertEquals("Adequate", response.getDocumentationMaturity());
    }

    @Test
    void setAndGetMaintainabilitySummary() {
        RepositoryHealthResponse response = new RepositoryHealthResponse();
        response.setMaintainabilitySummary("Repository health is good");
        assertEquals("Repository health is good", response.getMaintainabilitySummary());
    }

    @Test
    void setAndGetStrengths() {
        RepositoryHealthResponse response = new RepositoryHealthResponse();
        List<String> strengths = new ArrayList<>();
        strengths.add("Well-organized package structure");
        response.setStrengths(strengths);
        assertEquals(1, response.getStrengths().size());
        assertTrue(response.getStrengths().contains("Well-organized package structure"));
    }

    @Test
    void setStrengths_withNull_createsEmptyList() {
        RepositoryHealthResponse response = new RepositoryHealthResponse();
        response.setStrengths(null);
        assertNotNull(response.getStrengths());
        assertTrue(response.getStrengths().isEmpty());
    }

    @Test
    void setAndGetObservations() {
        RepositoryHealthResponse response = new RepositoryHealthResponse();
        List<String> observations = new ArrayList<>();
        observations.add("Repository has 100 classes");
        response.setObservations(observations);
        assertEquals(1, response.getObservations().size());
    }

    @Test
    void setObservations_withNull_createsEmptyList() {
        RepositoryHealthResponse response = new RepositoryHealthResponse();
        response.setObservations(null);
        assertNotNull(response.getObservations());
        assertTrue(response.getObservations().isEmpty());
    }

    @Test
    void setAndGetPotentialRisks() {
        RepositoryHealthResponse response = new RepositoryHealthResponse();
        List<String> risks = new ArrayList<>();
        risks.add("No test coverage detected");
        response.setPotentialRisks(risks);
        assertEquals(1, response.getPotentialRisks().size());
    }

    @Test
    void setPotentialRisks_withNull_createsEmptyList() {
        RepositoryHealthResponse response = new RepositoryHealthResponse();
        response.setPotentialRisks(null);
        assertNotNull(response.getPotentialRisks());
        assertTrue(response.getPotentialRisks().isEmpty());
    }

    @Test
    void setAndGetSuggestedReviewAreas() {
        RepositoryHealthResponse response = new RepositoryHealthResponse();
        List<String> reviewAreas = new ArrayList<>();
        reviewAreas.add("Review test coverage strategy");
        response.setSuggestedReviewAreas(reviewAreas);
        assertEquals(1, response.getSuggestedReviewAreas().size());
    }

    @Test
    void setSuggestedReviewAreas_withNull_createsEmptyList() {
        RepositoryHealthResponse response = new RepositoryHealthResponse();
        response.setSuggestedReviewAreas(null);
        assertNotNull(response.getSuggestedReviewAreas());
        assertTrue(response.getSuggestedReviewAreas().isEmpty());
    }

    @Test
    void setAndGetConfidenceLevel() {
        RepositoryHealthResponse response = new RepositoryHealthResponse();
        response.setConfidenceLevel("HIGH");
        assertEquals("HIGH", response.getConfidenceLevel());
    }
}