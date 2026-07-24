package com.projectiq.mcp.pipeline.dto;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ContextPackage}.
 */
class ContextPackageTest {

    @Test
    void testDefaultConstructor() {
        ContextPackage pkg = new ContextPackage();
        assertNotNull(pkg.getRelevantClasses());
        assertNotNull(pkg.getRelevantMethods());
        assertNotNull(pkg.getRelatedApis());
        assertNotNull(pkg.getDependencies());
        assertNotNull(pkg.getConfiguration());
        assertNotNull(pkg.getRisks());
        assertNotNull(pkg.getConventions());
        assertNotNull(pkg.getArchitectureInsights());
        assertNotNull(pkg.getWarnings());
        assertTrue(pkg.getRelevantClasses().isEmpty());
    }

    @Test
    void testSettersAndGetters() {
        ContextPackage pkg = new ContextPackage();
        pkg.setWorkflowSummary("Test workflow");
        pkg.setRepositorySummary("Test repo");
        pkg.setSuggestedImplementationFocus("Focus on X");
        pkg.setTotalContextItems(10);
        pkg.setHighPriorityCount(5);
        pkg.setMediumPriorityCount(3);
        pkg.setLowPriorityCount(2);
        pkg.setProcessingTimeMillis(100L);

        assertEquals("Test workflow", pkg.getWorkflowSummary());
        assertEquals("Test repo", pkg.getRepositorySummary());
        assertEquals("Focus on X", pkg.getSuggestedImplementationFocus());
        assertEquals(10, pkg.getTotalContextItems());
        assertEquals(5, pkg.getHighPriorityCount());
        assertEquals(3, pkg.getMediumPriorityCount());
        assertEquals(2, pkg.getLowPriorityCount());
        assertEquals(100L, pkg.getProcessingTimeMillis());
    }

    @Test
    void testAddMethods() {
        ContextPackage pkg = new ContextPackage();
        pkg.addRelevantClass("ClassA");
        pkg.addRelevantMethod("MethodA");
        pkg.addRelatedApi("GET /api/test");
        pkg.addDependency("spring-core");
        pkg.addConfiguration("config1");
        pkg.addRisk("risk1");
        pkg.addConvention("conv1");
        pkg.addArchitectureInsight("insight1");
        pkg.addWarning("warning1");

        assertEquals(1, pkg.getRelevantClasses().size());
        assertEquals(1, pkg.getRelevantMethods().size());
        assertEquals(1, pkg.getRelatedApis().size());
        assertEquals(1, pkg.getDependencies().size());
        assertEquals(1, pkg.getConfiguration().size());
        assertEquals(1, pkg.getRisks().size());
        assertEquals(1, pkg.getConventions().size());
        assertEquals(1, pkg.getArchitectureInsights().size());
        assertEquals(1, pkg.getWarnings().size());
    }

    @Test
    void testNullSafety() {
        ContextPackage pkg = new ContextPackage();
        pkg.setRelevantClasses(null);
        pkg.setRelevantMethods(null);
        pkg.setRelatedApis(null);
        pkg.setDependencies(null);
        pkg.setConfiguration(null);
        pkg.setRisks(null);
        pkg.setConventions(null);
        pkg.setArchitectureInsights(null);
        pkg.setWarnings(null);

        assertNotNull(pkg.getRelevantClasses());
        assertNotNull(pkg.getRelevantMethods());
        assertNotNull(pkg.getRelatedApis());
        assertNotNull(pkg.getDependencies());
        assertNotNull(pkg.getConfiguration());
        assertNotNull(pkg.getRisks());
        assertNotNull(pkg.getConventions());
        assertNotNull(pkg.getArchitectureInsights());
        assertNotNull(pkg.getWarnings());
    }
}