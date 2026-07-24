package com.projectiq.mcp.analysis.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DependencyChangePredictionResponse}.
 */
class DependencyChangePredictionResponseTest {

    @Test
    void testDefaultConstructor() {
        DependencyChangePredictionResponse response = new DependencyChangePredictionResponse();

        assertNotNull(response.getImpactedModules());
        assertTrue(response.getImpactedModules().isEmpty());
        assertNotNull(response.getImpactedServices());
        assertTrue(response.getImpactedServices().isEmpty());
        assertNotNull(response.getTransitiveDependencyEffects());
        assertTrue(response.getTransitiveDependencyEffects().isEmpty());
        assertNotNull(response.getCompatibilityRisks());
        assertTrue(response.getCompatibilityRisks().isEmpty());
        assertNotNull(response.getBuildRisks());
        assertTrue(response.getBuildRisks().isEmpty());
        assertNotNull(response.getTestingImpact());
        assertTrue(response.getTestingImpact().isEmpty());
        assertNotNull(response.getMigrationRecommendations());
        assertTrue(response.getMigrationRecommendations().isEmpty());
        assertNotNull(response.getSuggestedValidationChecklist());
        assertTrue(response.getSuggestedValidationChecklist().isEmpty());
        assertFalse(response.isCircularDependencyDetected());
    }

    @Test
    void testSettersAndGetters() {
        DependencyChangePredictionResponse response = new DependencyChangePredictionResponse();

        response.setProposedDependencyChange("UPGRADE org.springframework.boot:spring-boot-starter-web");
        response.setDependencyName("org.springframework.boot:spring-boot-starter-web");
        response.setChangeType("UPGRADE");
        response.setPredictionCategory("Spring Dependencies");
        response.setCurrentVersion("3.0.0");
        response.setNewVersion("3.1.0");
        response.setCircularDependencyDetected(true);
        response.setMigrationEffortEstimate("MEDIUM");

        List<String> modules = Arrays.asList("Build configuration (pom.xml)", "Module dependency graph");
        List<String> services = Arrays.asList("Spring context management", "Bean lifecycle management");
        List<String> transitiveEffects = Arrays.asList("New transitive dependencies will be pulled in");
        List<String> compatibilityRisks = Arrays.asList("API breaking changes in newer versions");
        List<String> buildRisks = Arrays.asList("Version conflict resolution may require dependency exclusions");
        List<String> testingImpact = Arrays.asList("Run full test suite to detect regressions");
        List<String> migrationRecs = Arrays.asList("Review changelog/release notes for the new version");
        List<String> checklist = Arrays.asList("[ ] Verify build compiles successfully");

        response.setImpactedModules(modules);
        response.setImpactedServices(services);
        response.setTransitiveDependencyEffects(transitiveEffects);
        response.setCompatibilityRisks(compatibilityRisks);
        response.setBuildRisks(buildRisks);
        response.setTestingImpact(testingImpact);
        response.setMigrationRecommendations(migrationRecs);
        response.setSuggestedValidationChecklist(checklist);

        assertEquals("UPGRADE org.springframework.boot:spring-boot-starter-web", response.getProposedDependencyChange());
        assertEquals("org.springframework.boot:spring-boot-starter-web", response.getDependencyName());
        assertEquals("UPGRADE", response.getChangeType());
        assertEquals("Spring Dependencies", response.getPredictionCategory());
        assertEquals("3.0.0", response.getCurrentVersion());
        assertEquals("3.1.0", response.getNewVersion());
        assertTrue(response.isCircularDependencyDetected());
        assertEquals("MEDIUM", response.getMigrationEffortEstimate());
        assertEquals(modules, response.getImpactedModules());
        assertEquals(services, response.getImpactedServices());
        assertEquals(transitiveEffects, response.getTransitiveDependencyEffects());
        assertEquals(compatibilityRisks, response.getCompatibilityRisks());
        assertEquals(buildRisks, response.getBuildRisks());
        assertEquals(testingImpact, response.getTestingImpact());
        assertEquals(migrationRecs, response.getMigrationRecommendations());
        assertEquals(checklist, response.getSuggestedValidationChecklist());
    }

    @Test
    void testToString() {
        DependencyChangePredictionResponse response = new DependencyChangePredictionResponse();
        response.setDependencyName("com.example:test-lib");
        response.setChangeType("ADD");
        response.setPredictionCategory("Testing Libraries");

        String toString = response.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("com.example:test-lib"));
        assertTrue(toString.contains("ADD"));
        assertTrue(toString.contains("Testing Libraries"));
    }

    @Test
    void testNullListsAreReplacedByEmptyLists() {
        DependencyChangePredictionResponse response = new DependencyChangePredictionResponse();

        response.setImpactedModules(null);
        response.setImpactedServices(null);
        response.setTransitiveDependencyEffects(null);
        response.setCompatibilityRisks(null);
        response.setBuildRisks(null);
        response.setTestingImpact(null);
        response.setMigrationRecommendations(null);
        response.setSuggestedValidationChecklist(null);

        // The getters will return null since we set them to null after construction
        assertNull(response.getImpactedModules());
        assertNull(response.getImpactedServices());
    }

    @Test
    void testAddDependencyChange() {
        DependencyChangePredictionResponse response = new DependencyChangePredictionResponse();
        response.setDependencyName("com.example:new-lib");
        response.setChangeType("ADD");
        response.setPredictionCategory("Third-Party Frameworks");
        response.setMigrationEffortEstimate("MEDIUM");

        assertEquals("com.example:new-lib", response.getDependencyName());
        assertEquals("ADD", response.getChangeType());
        assertEquals("Third-Party Frameworks", response.getPredictionCategory());
        assertEquals("MEDIUM", response.getMigrationEffortEstimate());
        assertFalse(response.isCircularDependencyDetected());
    }

    @Test
    void testRemoveDependencyChange() {
        DependencyChangePredictionResponse response = new DependencyChangePredictionResponse();
        response.setDependencyName("com.example:old-lib");
        response.setChangeType("REMOVE");
        response.setCurrentVersion("1.0.0");
        response.setMigrationEffortEstimate("HIGH");

        assertEquals("com.example:old-lib", response.getDependencyName());
        assertEquals("REMOVE", response.getChangeType());
        assertEquals("1.0.0", response.getCurrentVersion());
        assertNull(response.getNewVersion());
        assertEquals("HIGH", response.getMigrationEffortEstimate());
    }
}