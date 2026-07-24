package com.projectiq.mcp.analysis.service;

import com.projectiq.mcp.analysis.dto.DependencyChangePredictionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DependencyChangePredictionService}.
 */
class DependencyChangePredictionServiceTest {

    private DependencyChangePredictionService service;

    @BeforeEach
    void setUp() {
        service = new DependencyChangePredictionService();
    }

    // --- Add Dependency Tests ---

    @Test
    void testAddDependency() {
        DependencyChangePredictionResponse response = service.predictDependencyChange(
                "org.springframework.boot:spring-boot-starter-web", "ADD", null, "3.1.0", "test-repo");

        assertNotNull(response);
        assertEquals("org.springframework.boot:spring-boot-starter-web", response.getDependencyName());
        assertEquals("ADD", response.getChangeType());
        assertEquals("Spring Dependencies", response.getPredictionCategory());
        assertNull(response.getCurrentVersion());
        assertEquals("3.1.0", response.getNewVersion());
        assertFalse(response.getImpactedModules().isEmpty());
        assertFalse(response.getImpactedServices().isEmpty());
        assertFalse(response.getTransitiveDependencyEffects().isEmpty());
        assertFalse(response.getCompatibilityRisks().isEmpty());
        assertFalse(response.getBuildRisks().isEmpty());
        assertFalse(response.getTestingImpact().isEmpty());
        assertFalse(response.getMigrationRecommendations().isEmpty());
        assertFalse(response.getSuggestedValidationChecklist().isEmpty());
    }

    @Test
    void testAddTestingLibrary() {
        DependencyChangePredictionResponse response = service.predictDependencyChange(
                "org.mockito:mockito-core", "ADD", null, "5.0.0", "test-repo");

        assertNotNull(response);
        assertEquals("Testing Libraries", response.getPredictionCategory());
        assertEquals("LOW", response.getMigrationEffortEstimate());
    }

    @Test
    void testAddLoggingFramework() {
        DependencyChangePredictionResponse response = service.predictDependencyChange(
                "ch.qos.logback:logback-classic", "ADD", null, "1.4.0", "test-repo");

        assertNotNull(response);
        assertEquals("Logging Frameworks", response.getPredictionCategory());
        assertEquals("LOW", response.getMigrationEffortEstimate());
    }

    // --- Remove Dependency Tests ---

    @Test
    void testRemoveDependency() {
        DependencyChangePredictionResponse response = service.predictDependencyChange(
                "com.example:old-lib", "REMOVE", "1.0.0", null, "test-repo");

        assertNotNull(response);
        assertEquals("com.example:old-lib", response.getDependencyName());
        assertEquals("REMOVE", response.getChangeType());
        assertEquals("1.0.0", response.getCurrentVersion());
        assertNull(response.getNewVersion());
        assertEquals("HIGH", response.getMigrationEffortEstimate());
        assertFalse(response.isCircularDependencyDetected());
    }

    // --- Upgrade Dependency Tests ---

    @Test
    void testUpgradeDependency() {
        DependencyChangePredictionResponse response = service.predictDependencyChange(
                "org.springframework.boot:spring-boot-starter-web", "UPGRADE", "3.0.0", "3.1.0", "test-repo");

        assertNotNull(response);
        assertEquals("UPGRADE", response.getChangeType());
        assertEquals("3.0.0", response.getCurrentVersion());
        assertEquals("3.1.0", response.getNewVersion());
        assertFalse(response.getCompatibilityRisks().isEmpty());
    }

    @Test
    void testUpgradeTestingLibrary() {
        DependencyChangePredictionResponse response = service.predictDependencyChange(
                "org.junit.jupiter:junit-jupiter-api", "UPGRADE", "5.9.0", "5.10.0", "test-repo");

        assertNotNull(response);
        assertEquals("Testing Libraries", response.getPredictionCategory());
        assertEquals("LOW", response.getMigrationEffortEstimate());
    }

    // --- Downgrade Dependency Tests ---

    @Test
    void testDowngradeDependency() {
        DependencyChangePredictionResponse response = service.predictDependencyChange(
                "com.example:my-lib", "DOWNGRADE", "2.0.0", "1.0.0", "test-repo");

        assertNotNull(response);
        assertEquals("DOWNGRADE", response.getChangeType());
        assertEquals("2.0.0", response.getCurrentVersion());
        assertEquals("1.0.0", response.getNewVersion());
        assertEquals("MEDIUM", response.getMigrationEffortEstimate());
    }

    // --- Modify Dependency Tests ---

    @Test
    void testModifyDependency() {
        DependencyChangePredictionResponse response = service.predictDependencyChange(
                "com.example:old-lib", "MODIFY", "1.0.0", "com.example:new-lib:2.0.0", "test-repo");

        assertNotNull(response);
        assertEquals("MODIFY", response.getChangeType());
        assertEquals("HIGH", response.getMigrationEffortEstimate());
    }

    // --- Classification Tests ---

    @Test
    void testClassifySpringDependency() {
        DependencyChangePredictionResponse response = service.predictDependencyChange(
                "org.springframework.boot:spring-boot-starter-data-jpa", "ADD", null, "3.1.0", null);

        assertEquals("Spring Dependencies", response.getPredictionCategory());
    }

    @Test
    void testClassifyDatabaseDriver() {
        DependencyChangePredictionResponse response = service.predictDependencyChange(
                "org.postgresql:postgresql", "ADD", null, "42.5.0", null);

        assertEquals("Database Drivers", response.getPredictionCategory());
    }

    @Test
    void testClassifySecurityLibrary() {
        DependencyChangePredictionResponse response = service.predictDependencyChange(
                "org.springframework.security:spring-security-core", "UPGRADE", "6.0.0", "6.1.0", null);

        assertEquals("Security Libraries", response.getPredictionCategory());
    }

    @Test
    void testClassifyBuildDependency() {
        DependencyChangePredictionResponse response = service.predictDependencyChange(
                "org.apache.maven.plugins:maven-compiler-plugin", "UPGRADE", "3.10.0", "3.11.0", null);

        assertEquals("Build Dependencies", response.getPredictionCategory());
    }

    @Test
    void testClassifyThirdPartyFramework() {
        DependencyChangePredictionResponse response = service.predictDependencyChange(
                "com.google.guava:guava", "ADD", null, "32.0.0", null);

        assertEquals("Third-Party Frameworks", response.getPredictionCategory());
    }

    // --- Circular Dependency Detection Tests ---

    @Test
    void testCircularDependencyDetected() {
        DependencyChangePredictionResponse response = service.predictDependencyChange(
                "spring-boot-starter-web:spring-boot-starter-tomcat", "ADD", null, "3.1.0", null);

        assertTrue(response.isCircularDependencyDetected());
    }

    @Test
    void testCircularDependencyNotDetectedForRemove() {
        DependencyChangePredictionResponse response = service.predictDependencyChange(
                "spring-boot-starter-web:spring-boot-starter-tomcat", "REMOVE", "3.0.0", null, null);

        assertFalse(response.isCircularDependencyDetected());
    }

    // --- Description Parsing Tests ---

    @Test
    void testPredictFromDescriptionAdd() {
        DependencyChangePredictionResponse response = service.predictDependencyChangeFromDescription(
                "Add com.example:new-lib version 1.0.0", "test-repo");

        assertNotNull(response);
        assertEquals("com.example:new-lib", response.getDependencyName());
        assertEquals("ADD", response.getChangeType());
    }

    @Test
    void testPredictFromDescriptionUpgrade() {
        DependencyChangePredictionResponse response = service.predictDependencyChangeFromDescription(
                "Upgrade org.springframework.boot:spring-boot-starter-web from 3.0.0 to 3.1.0", "test-repo");

        assertNotNull(response);
        assertEquals("org.springframework.boot:spring-boot-starter-web", response.getDependencyName());
        assertEquals("UPGRADE", response.getChangeType());
        assertEquals("3.0.0", response.getCurrentVersion());
        assertEquals("3.1.0", response.getNewVersion());
    }

    @Test
    void testPredictFromDescriptionRemove() {
        DependencyChangePredictionResponse response = service.predictDependencyChangeFromDescription(
                "Remove com.example:old-lib", "test-repo");

        assertNotNull(response);
        assertEquals("com.example:old-lib", response.getDependencyName());
        assertEquals("REMOVE", response.getChangeType());
    }

    @Test
    void testPredictFromDescriptionDowngrade() {
        DependencyChangePredictionResponse response = service.predictDependencyChangeFromDescription(
                "Downgrade com.example:my-lib from 2.0.0 to 1.0.0", "test-repo");

        assertNotNull(response);
        assertEquals("DOWNGRADE", response.getChangeType());
        assertEquals("2.0.0", response.getCurrentVersion());
        assertEquals("1.0.0", response.getNewVersion());
    }

    // --- Exception Handling Tests ---

    @Test
    void testNullDependencyNameThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                service.predictDependencyChange(null, "ADD", null, "1.0.0", "test-repo"));
    }

    @Test
    void testEmptyDependencyNameThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                service.predictDependencyChange("", "ADD", null, "1.0.0", "test-repo"));
    }

    @Test
    void testNullChangeTypeThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                service.predictDependencyChange("com.example:lib", null, null, "1.0.0", "test-repo"));
    }

    @Test
    void testInvalidChangeTypeThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                service.predictDependencyChange("com.example:lib", "INVALID", null, "1.0.0", "test-repo"));
    }

    @Test
    void testNullDescriptionThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                service.predictDependencyChangeFromDescription(null, "test-repo"));
    }

    @Test
    void testEmptyDescriptionThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                service.predictDependencyChangeFromDescription("", "test-repo"));
    }

    @Test
    void testDescriptionWithoutDependencyNameThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                service.predictDependencyChangeFromDescription("Add a new library", "test-repo"));
    }

    // --- Deterministic Output Tests ---

    @Test
    void testDeterministicOutputSameInput() {
        DependencyChangePredictionResponse response1 = service.predictDependencyChange(
                "org.springframework.boot:spring-boot-starter-web", "UPGRADE", "3.0.0", "3.1.0", "test-repo");

        DependencyChangePredictionResponse response2 = service.predictDependencyChange(
                "org.springframework.boot:spring-boot-starter-web", "UPGRADE", "3.0.0", "3.1.0", "test-repo");

        assertEquals(response1.getPredictionCategory(), response2.getPredictionCategory());
        assertEquals(response1.getMigrationEffortEstimate(), response2.getMigrationEffortEstimate());
        assertEquals(response1.getImpactedModules(), response2.getImpactedModules());
        assertEquals(response1.getImpactedServices(), response2.getImpactedServices());
        assertEquals(response1.getTransitiveDependencyEffects(), response2.getTransitiveDependencyEffects());
        assertEquals(response1.getCompatibilityRisks(), response2.getCompatibilityRisks());
        assertEquals(response1.getBuildRisks(), response2.getBuildRisks());
        assertEquals(response1.getTestingImpact(), response2.getTestingImpact());
        assertEquals(response1.getMigrationRecommendations(), response2.getMigrationRecommendations());
        assertEquals(response1.getSuggestedValidationChecklist(), response2.getSuggestedValidationChecklist());
    }

    // --- Normalization Tests ---

    @Test
    void testNormalizeChangeTypeVariants() {
        assertEquals("ADD", service.normalizeChangeType("add"));
        assertEquals("ADD", service.normalizeChangeType("ADDING"));
        assertEquals("ADD", service.normalizeChangeType("NEW"));
        assertEquals("ADD", service.normalizeChangeType("INTRODUCE"));

        assertEquals("REMOVE", service.normalizeChangeType("remove"));
        assertEquals("REMOVE", service.normalizeChangeType("REMOVING"));
        assertEquals("REMOVE", service.normalizeChangeType("DELETE"));
        assertEquals("REMOVE", service.normalizeChangeType("DROP"));

        assertEquals("UPGRADE", service.normalizeChangeType("upgrade"));
        assertEquals("UPGRADE", service.normalizeChangeType("UPGRADING"));
        assertEquals("UPGRADE", service.normalizeChangeType("UPDATE"));
        assertEquals("UPGRADE", service.normalizeChangeType("BUMP"));

        assertEquals("DOWNGRADE", service.normalizeChangeType("downgrade"));
        assertEquals("DOWNGRADE", service.normalizeChangeType("DOWNGRADING"));
        assertEquals("DOWNGRADE", service.normalizeChangeType("ROLLBACK"));

        assertEquals("MODIFY", service.normalizeChangeType("modify"));
        assertEquals("MODIFY", service.normalizeChangeType("MODIFYING"));
        assertEquals("MODIFY", service.normalizeChangeType("CHANGE"));
        assertEquals("MODIFY", service.normalizeChangeType("REPLACE"));
        assertEquals("MODIFY", service.normalizeChangeType("SWAP"));
        assertEquals("MODIFY", service.normalizeChangeType("SWITCH"));
    }

    // --- Version Extraction Tests ---

    @Test
    void testExtractCurrentVersion() {
        String version = service.extractCurrentVersion("Upgrade from 1.0.0 to 2.0.0", "upgrade from 1.0.0 to 2.0.0");
        assertEquals("1.0.0", version);
    }

    @Test
    void testExtractNewVersion() {
        String version = service.extractNewVersion("Upgrade from 1.0.0 to 2.0.0", "upgrade from 1.0.0 to 2.0.0");
        assertEquals("2.0.0", version);
    }

    @Test
    void testExtractCurrentVersionWithCurrentKeyword() {
        String version = service.extractCurrentVersion("Upgrade current version 1.0.0 to 2.0.0", "upgrade current version 1.0.0 to 2.0.0");
        assertEquals("1.0.0", version);
    }

    @Test
    void testExtractNewVersionWithNewKeyword() {
        String version = service.extractNewVersion("Upgrade to new version 2.0.0", "upgrade to new version 2.0.0");
        assertEquals("2.0.0", version);
    }

    // --- Validation Checklist Tests ---

    @Test
    void testValidationChecklistContainsBaseItems() {
        DependencyChangePredictionResponse response = service.predictDependencyChange(
                "com.example:lib", "ADD", null, "1.0.0", null);

        List<String> checklist = response.getSuggestedValidationChecklist();
        assertTrue(checklist.stream().anyMatch(item -> item.contains("build compiles successfully")));
        assertTrue(checklist.stream().anyMatch(item -> item.contains("full test suite")));
        assertTrue(checklist.stream().anyMatch(item -> item.contains("dependency convergence")));
    }

    @Test
    void testValidationChecklistForUpgrade() {
        DependencyChangePredictionResponse response = service.predictDependencyChange(
                "com.example:lib", "UPGRADE", "1.0.0", "2.0.0", null);

        List<String> checklist = response.getSuggestedValidationChecklist();
        assertTrue(checklist.stream().anyMatch(item -> item.contains("API differences")));
        assertTrue(checklist.stream().anyMatch(item -> item.contains("major version migration guide")));
    }

    @Test
    void testValidationChecklistForRemove() {
        DependencyChangePredictionResponse response = service.predictDependencyChange(
                "com.example:lib", "REMOVE", "1.0.0", null, null);

        List<String> checklist = response.getSuggestedValidationChecklist();
        assertTrue(checklist.stream().anyMatch(item -> item.contains("no code references")));
        assertTrue(checklist.stream().anyMatch(item -> item.contains("no transitive dependencies")));
    }

    // --- Migration Effort Tests ---

    @Test
    void testMigrationEffortForSpringDependencies() {
        DependencyChangePredictionResponse response = service.predictDependencyChange(
                "org.springframework.boot:spring-boot-starter-web", "UPGRADE", "3.0.0", "3.1.0", null);

        assertEquals("HIGH", response.getMigrationEffortEstimate());
    }

    @Test
    void testMigrationEffortForDatabaseDrivers() {
        DependencyChangePredictionResponse response = service.predictDependencyChange(
                "org.postgresql:postgresql", "UPGRADE", "42.5.0", "42.6.0", null);

        assertEquals("HIGH", response.getMigrationEffortEstimate());
    }

    @Test
    void testMigrationEffortForSecurityLibraries() {
        DependencyChangePredictionResponse response = service.predictDependencyChange(
                "org.springframework.security:spring-security-core", "UPGRADE", "6.0.0", "6.1.0", null);

        assertEquals("HIGH", response.getMigrationEffortEstimate());
    }
}