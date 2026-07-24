package com.projectiq.mcp.analysis.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectiq.mcp.analysis.dto.ArchitecturalDecisionResponse.Alternative;
import com.projectiq.mcp.analysis.dto.ArchitecturalDecisionResponse.DependencyImplications;
import com.projectiq.mcp.analysis.dto.ArchitecturalDecisionResponse.ImpactAssessment;
import com.projectiq.mcp.analysis.dto.ArchitecturalDecisionResponse.MaintainabilityAssessment;
import com.projectiq.mcp.analysis.dto.ArchitecturalDecisionResponse.ScalabilityAssessment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArchitecturalDecisionResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testDefaultConstructor() {
        ArchitecturalDecisionResponse response = new ArchitecturalDecisionResponse();
        assertNotNull(response.getAlternatives());
        assertNotNull(response.getArchitecturalRisks());
        assertNotNull(response.getRepositoryImpact());
        assertNotNull(response.getDependencyImplications());
        assertNotNull(response.getScalabilityAssessment());
        assertNotNull(response.getMaintainabilityAssessment());
        assertTrue(response.getAlternatives().isEmpty());
    }

    @Test
    void testSettersAndGetters() {
        ArchitecturalDecisionResponse response = new ArchitecturalDecisionResponse();
        response.setDecisionId("decision-1");
        response.setDecisionCategory("New Service vs Existing Service");
        response.setRequestDescription("Should we create a new service?");
        response.setRepositoryName("my-repo");
        response.setRecommendedApproach("Create New Service");
        response.setDecisionRationale("Better separation of concerns");

        assertEquals("decision-1", response.getDecisionId());
        assertEquals("New Service vs Existing Service", response.getDecisionCategory());
        assertEquals("Should we create a new service?", response.getRequestDescription());
        assertEquals("my-repo", response.getRepositoryName());
        assertEquals("Create New Service", response.getRecommendedApproach());
        assertEquals("Better separation of concerns", response.getDecisionRationale());
    }

    @Test
    void testAlternative() {
        Alternative alt = new Alternative("Composition", "Use composition pattern",
                List.of("Flexibility", "Testability"),
                List.of("More code", "Complexity"));
        alt.setSuitabilityScore(9);
        alt.setComplexityLevel("Medium");
        alt.setMaintainabilityRating("Excellent");

        assertEquals("Composition", alt.getName());
        assertEquals("Use composition pattern", alt.getDescription());
        assertEquals(2, alt.getPros().size());
        assertEquals(2, alt.getCons().size());
        assertEquals(9, alt.getSuitabilityScore());
        assertEquals("Medium", alt.getComplexityLevel());
        assertEquals("Excellent", alt.getMaintainabilityRating());
    }

    @Test
    void testImpactAssessment() {
        ImpactAssessment ia = new ImpactAssessment();
        ia.setOverallImpact("Medium");
        ia.setFilesAffected(5);
        ia.setClassesAffected(10);
        ia.setArchitecturalConsistencyImpact("Good");
        ia.setConventionAlignment("Aligned");

        assertEquals("Medium", ia.getOverallImpact());
        assertEquals(5, ia.getFilesAffected());
        assertEquals(10, ia.getClassesAffected());
        assertEquals("Good", ia.getArchitecturalConsistencyImpact());
        assertEquals("Aligned", ia.getConventionAlignment());
    }

    @Test
    void testDependencyImplications() {
        DependencyImplications di = new DependencyImplications();
        di.setNewDependenciesRequired(2);
        di.setExistingDependenciesAffected(3);
        di.setDependencyComplexity("Low");
        di.setCircularDependencyRisk("Low");

        assertEquals(2, di.getNewDependenciesRequired());
        assertEquals(3, di.getExistingDependenciesAffected());
        assertEquals("Low", di.getDependencyComplexity());
        assertEquals("Low", di.getCircularDependencyRisk());
    }

    @Test
    void testScalabilityAssessment() {
        ScalabilityAssessment sa = new ScalabilityAssessment();
        sa.setHorizontalScalability("Good");
        sa.setVerticalScalability("Excellent");
        sa.setPerformanceImplication("Neutral");
        sa.setResourceUtilizationEstimate("Moderate");

        assertEquals("Good", sa.getHorizontalScalability());
        assertEquals("Excellent", sa.getVerticalScalability());
        assertEquals("Neutral", sa.getPerformanceImplication());
        assertEquals("Moderate", sa.getResourceUtilizationEstimate());
    }

    @Test
    void testMaintainabilityAssessment() {
        MaintainabilityAssessment ma = new MaintainabilityAssessment();
        ma.setCodeComplexity("Low");
        ma.setTestability("Excellent");
        ma.setReusability("Good");
        ma.setLongTermMaintainability("Excellent");

        assertEquals("Low", ma.getCodeComplexity());
        assertEquals("Excellent", ma.getTestability());
        assertEquals("Good", ma.getReusability());
        assertEquals("Excellent", ma.getLongTermMaintainability());
    }

    @Test
    void testDecisionCategoryConstants() {
        assertEquals("New Service vs Existing Service", ArchitecturalDecisionResponse.DecisionCategory.NEW_SERVICE_VS_EXISTING);
        assertEquals("New Module vs Existing Module", ArchitecturalDecisionResponse.DecisionCategory.NEW_MODULE_VS_EXISTING);
        assertEquals("Extend API vs Create API", ArchitecturalDecisionResponse.DecisionCategory.EXTEND_API_VS_CREATE_API);
        assertEquals("Event-Driven vs Synchronous", ArchitecturalDecisionResponse.DecisionCategory.EVENT_DRIVEN_VS_SYNCHRONOUS);
        assertEquals("Composition vs Inheritance", ArchitecturalDecisionResponse.DecisionCategory.COMPOSITION_VS_INHERITANCE);
        assertEquals("Configuration vs Code", ArchitecturalDecisionResponse.DecisionCategory.CONFIGURATION_VS_CODE);
        assertEquals("Shared Component vs Dedicated Component", ArchitecturalDecisionResponse.DecisionCategory.SHARED_VS_DEDICATED);
        assertEquals("Package Organization", ArchitecturalDecisionResponse.DecisionCategory.PACKAGE_ORGANIZATION);
    }

    @Test
    void testJsonSerialization() throws Exception {
        ArchitecturalDecisionResponse response = new ArchitecturalDecisionResponse();
        response.setDecisionId("test-1");
        response.setDecisionCategory("Composition vs Inheritance");
        response.setRepositoryName("my-repo");
        response.setRecommendedApproach("Composition");

        Alternative alt = new Alternative("Composition", "Use composition",
                List.of("Flexibility"), List.of("More code"));
        alt.setSuitabilityScore(9);
        response.setAlternatives(List.of(alt));

        response.getRepositoryImpact().setOverallImpact("Low");
        response.getArchitecturalRisks().add("Over-engineering risk");

        String json = objectMapper.writeValueAsString(response);
        assertNotNull(json);
        assertTrue(json.contains("test-1"));
        assertTrue(json.contains("Composition"));
        assertTrue(json.contains("Over-engineering risk"));

        ArchitecturalDecisionResponse deserialized = objectMapper.readValue(json, ArchitecturalDecisionResponse.class);
        assertEquals("test-1", deserialized.getDecisionId());
        assertEquals(1, deserialized.getAlternatives().size());
        assertEquals(1, deserialized.getArchitecturalRisks().size());
    }
}