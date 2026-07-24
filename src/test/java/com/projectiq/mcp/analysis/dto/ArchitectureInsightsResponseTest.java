package com.projectiq.mcp.analysis.dto;

import com.projectiq.mcp.analysis.dto.ArchitectureInsightsResponse.ModuleRelationship;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArchitectureInsightsResponseTest {

    @Test
    void defaultConstructor_initializesEmptyLists() {
        ArchitectureInsightsResponse response = new ArchitectureInsightsResponse();
        assertNotNull(response.getDetectedLayers());
        assertNotNull(response.getModuleRelationships());
        assertNotNull(response.getCrossLayerDependencies());
        assertNotNull(response.getArchitecturalStrengths());
        assertNotNull(response.getPotentialConcerns());
        assertTrue(response.getDetectedLayers().isEmpty());
        assertTrue(response.getModuleRelationships().isEmpty());
        assertTrue(response.getCrossLayerDependencies().isEmpty());
        assertTrue(response.getArchitecturalStrengths().isEmpty());
        assertTrue(response.getPotentialConcerns().isEmpty());
    }

    @Test
    void setAndGetRepositoryName() {
        ArchitectureInsightsResponse response = new ArchitectureInsightsResponse();
        response.setRepositoryName("my-project");
        assertEquals("my-project", response.getRepositoryName());
    }

    @Test
    void setAndGetBranch() {
        ArchitectureInsightsResponse response = new ArchitectureInsightsResponse();
        response.setBranch("main");
        assertEquals("main", response.getBranch());
    }

    @Test
    void setAndGetRepositoryOverview() {
        ArchitectureInsightsResponse response = new ArchitectureInsightsResponse();
        response.setRepositoryOverview("Repository contains 5 packages");
        assertEquals("Repository contains 5 packages", response.getRepositoryOverview());
    }

    @Test
    void setAndGetArchitecturalStyle() {
        ArchitectureInsightsResponse response = new ArchitectureInsightsResponse();
        response.setArchitecturalStyle("Layered Architecture");
        assertEquals("Layered Architecture", response.getArchitecturalStyle());
    }

    @Test
    void setAndGetDetectedLayers() {
        ArchitectureInsightsResponse response = new ArchitectureInsightsResponse();
        List<String> layers = new ArrayList<>();
        layers.add("Controller (Presentation)");
        layers.add("Service (Business Logic)");
        response.setDetectedLayers(layers);
        assertEquals(2, response.getDetectedLayers().size());
        assertTrue(response.getDetectedLayers().contains("Controller (Presentation)"));
    }

    @Test
    void setDetectedLayers_withNull_createsEmptyList() {
        ArchitectureInsightsResponse response = new ArchitectureInsightsResponse();
        response.setDetectedLayers(null);
        assertNotNull(response.getDetectedLayers());
        assertTrue(response.getDetectedLayers().isEmpty());
    }

    @Test
    void setAndGetModuleRelationships() {
        ArchitectureInsightsResponse response = new ArchitectureInsightsResponse();
        List<ModuleRelationship> relationships = new ArrayList<>();
        relationships.add(new ModuleRelationship("controller", "service", "Layer Dependency"));
        response.setModuleRelationships(relationships);
        assertEquals(1, response.getModuleRelationships().size());
        assertEquals("controller", response.getModuleRelationships().get(0).getSourceModule());
    }

    @Test
    void setModuleRelationships_withNull_createsEmptyList() {
        ArchitectureInsightsResponse response = new ArchitectureInsightsResponse();
        response.setModuleRelationships(null);
        assertNotNull(response.getModuleRelationships());
        assertTrue(response.getModuleRelationships().isEmpty());
    }

    @Test
    void setAndGetDependencyFlow() {
        ArchitectureInsightsResponse response = new ArchitectureInsightsResponse();
        response.setDependencyFlow("Controller -> Service -> Repository");
        assertEquals("Controller -> Service -> Repository", response.getDependencyFlow());
    }

    @Test
    void setAndGetCrossLayerDependencies() {
        ArchitectureInsightsResponse response = new ArchitectureInsightsResponse();
        List<String> deps = new ArrayList<>();
        deps.add("Repository package present alongside Controller package");
        response.setCrossLayerDependencies(deps);
        assertEquals(1, response.getCrossLayerDependencies().size());
    }

    @Test
    void setCrossLayerDependencies_withNull_createsEmptyList() {
        ArchitectureInsightsResponse response = new ArchitectureInsightsResponse();
        response.setCrossLayerDependencies(null);
        assertNotNull(response.getCrossLayerDependencies());
        assertTrue(response.getCrossLayerDependencies().isEmpty());
    }

    @Test
    void setAndGetArchitecturalStrengths() {
        ArchitectureInsightsResponse response = new ArchitectureInsightsResponse();
        List<String> strengths = new ArrayList<>();
        strengths.add("Layered Architecture");
        response.setArchitecturalStrengths(strengths);
        assertEquals(1, response.getArchitecturalStrengths().size());
    }

    @Test
    void setArchitecturalStrengths_withNull_createsEmptyList() {
        ArchitectureInsightsResponse response = new ArchitectureInsightsResponse();
        response.setArchitecturalStrengths(null);
        assertNotNull(response.getArchitecturalStrengths());
        assertTrue(response.getArchitecturalStrengths().isEmpty());
    }

    @Test
    void setAndGetPotentialConcerns() {
        ArchitectureInsightsResponse response = new ArchitectureInsightsResponse();
        List<String> concerns = new ArrayList<>();
        concerns.add("No architectural layers detected");
        response.setPotentialConcerns(concerns);
        assertEquals(1, response.getPotentialConcerns().size());
    }

    @Test
    void setPotentialConcerns_withNull_createsEmptyList() {
        ArchitectureInsightsResponse response = new ArchitectureInsightsResponse();
        response.setPotentialConcerns(null);
        assertNotNull(response.getPotentialConcerns());
        assertTrue(response.getPotentialConcerns().isEmpty());
    }

    @Test
    void setAndGetConfidenceLevel() {
        ArchitectureInsightsResponse response = new ArchitectureInsightsResponse();
        response.setConfidenceLevel("HIGH");
        assertEquals("HIGH", response.getConfidenceLevel());
    }

    @Test
    void moduleRelationship_constructorAndGetters() {
        ModuleRelationship relationship = new ModuleRelationship(
                "controller", "service", "Layer Dependency");
        assertEquals("controller", relationship.getSourceModule());
        assertEquals("service", relationship.getTargetModule());
        assertEquals("Layer Dependency", relationship.getRelationshipType());
    }

    @Test
    void moduleRelationship_setters() {
        ModuleRelationship relationship = new ModuleRelationship();
        relationship.setSourceModule("service");
        relationship.setTargetModule("repository");
        relationship.setRelationshipType("Layer Dependency");
        assertEquals("service", relationship.getSourceModule());
        assertEquals("repository", relationship.getTargetModule());
        assertEquals("Layer Dependency", relationship.getRelationshipType());
    }

    @Test
    void moduleRelationship_defaultConstructor() {
        ModuleRelationship relationship = new ModuleRelationship();
        assertNull(relationship.getSourceModule());
        assertNull(relationship.getTargetModule());
        assertNull(relationship.getRelationshipType());
    }
}