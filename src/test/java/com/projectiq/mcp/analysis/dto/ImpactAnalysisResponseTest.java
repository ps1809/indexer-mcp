package com.projectiq.mcp.analysis.dto;

import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse.ImpactedComponent;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse.RiskItem;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImpactAnalysisResponseTest {

    @Test
    void defaultConstructor_initializesEmptyLists() {
        ImpactAnalysisResponse response = new ImpactAnalysisResponse();
        assertNotNull(response.getPrimaryTargets());
        assertNotNull(response.getDirectlyAffectedComponents());
        assertNotNull(response.getIndirectlyAffectedComponents());
        assertNotNull(response.getDependencyImpact());
        assertNotNull(response.getPotentialRisks());
        assertTrue(response.getPrimaryTargets().isEmpty());
        assertTrue(response.getDirectlyAffectedComponents().isEmpty());
        assertTrue(response.getIndirectlyAffectedComponents().isEmpty());
        assertTrue(response.getDependencyImpact().isEmpty());
        assertTrue(response.getPotentialRisks().isEmpty());
    }

    @Test
    void setAndGetOriginalTask() {
        ImpactAnalysisResponse response = new ImpactAnalysisResponse();
        response.setOriginalTask("Add pagination to UserController");
        assertEquals("Add pagination to UserController", response.getOriginalTask());
    }

    @Test
    void setAndGetTaskType() {
        ImpactAnalysisResponse response = new ImpactAnalysisResponse();
        response.setTaskType("New Feature");
        assertEquals("New Feature", response.getTaskType());
    }

    @Test
    void addPrimaryTarget() {
        ImpactAnalysisResponse response = new ImpactAnalysisResponse();
        response.addPrimaryTarget("UserController");
        response.addPrimaryTarget("UserService");
        assertEquals(2, response.getPrimaryTargets().size());
        assertTrue(response.getPrimaryTargets().contains("UserController"));
        assertTrue(response.getPrimaryTargets().contains("UserService"));
    }

    @Test
    void addDirectlyAffectedComponent() {
        ImpactAnalysisResponse response = new ImpactAnalysisResponse();
        ImpactedComponent component = new ImpactedComponent(
                "UserController", "Class", "Directly referenced controller");
        response.addDirectlyAffectedComponent(component);
        assertEquals(1, response.getDirectlyAffectedComponents().size());
        assertEquals("UserController", response.getDirectlyAffectedComponents().get(0).getComponentName());
    }

    @Test
    void addIndirectlyAffectedComponent() {
        ImpactAnalysisResponse response = new ImpactAnalysisResponse();
        ImpactedComponent component = new ImpactedComponent(
                "UserService", "Class", "Service layer associated with controller");
        response.addIndirectlyAffectedComponent(component);
        assertEquals(1, response.getIndirectlyAffectedComponents().size());
        assertEquals("UserService", response.getIndirectlyAffectedComponents().get(0).getComponentName());
    }

    @Test
    void addDependencyImpact() {
        ImpactAnalysisResponse response = new ImpactAnalysisResponse();
        response.addDependencyImpact("New dependencies may be required");
        assertEquals(1, response.getDependencyImpact().size());
        assertTrue(response.getDependencyImpact().contains("New dependencies may be required"));
    }

    @Test
    void setAndGetEstimatedImplementationScope() {
        ImpactAnalysisResponse response = new ImpactAnalysisResponse();
        response.setEstimatedImplementationScope(ScopeLevel.MEDIUM);
        assertEquals(ScopeLevel.MEDIUM, response.getEstimatedImplementationScope());
    }

    @Test
    void setAndGetEstimatedTestingScope() {
        ImpactAnalysisResponse response = new ImpactAnalysisResponse();
        response.setEstimatedTestingScope(ScopeLevel.LARGE);
        assertEquals(ScopeLevel.LARGE, response.getEstimatedTestingScope());
    }

    @Test
    void addPotentialRisk() {
        ImpactAnalysisResponse response = new ImpactAnalysisResponse();
        RiskItem risk = new RiskItem(
                "Data migration errors", RiskLevel.HIGH, "Implement rollback procedures");
        response.addPotentialRisk(risk);
        assertEquals(1, response.getPotentialRisks().size());
        assertEquals("Data migration errors", response.getPotentialRisks().get(0).getDescription());
        assertEquals(RiskLevel.HIGH, response.getPotentialRisks().get(0).getRiskLevel());
    }

    @Test
    void setAndGetConfidenceLevel() {
        ImpactAnalysisResponse response = new ImpactAnalysisResponse();
        response.setConfidenceLevel(ConfidenceLevel.HIGH);
        assertEquals(ConfidenceLevel.HIGH, response.getConfidenceLevel());
    }

    @Test
    void impactedComponent_constructorAndGetters() {
        ImpactedComponent component = new ImpactedComponent(
                "UserController", "Class", "Directly referenced controller");
        assertEquals("UserController", component.getComponentName());
        assertEquals("Class", component.getComponentType());
        assertEquals("Directly referenced controller", component.getImpactReason());
    }

    @Test
    void impactedComponent_setters() {
        ImpactedComponent component = new ImpactedComponent();
        component.setComponentName("UserService");
        component.setComponentType("Class");
        component.setImpactReason("Service layer");
        assertEquals("UserService", component.getComponentName());
        assertEquals("Class", component.getComponentType());
        assertEquals("Service layer", component.getImpactReason());
    }

    @Test
    void riskItem_constructorAndGetters() {
        RiskItem risk = new RiskItem(
                "Data migration errors", RiskLevel.HIGH, "Implement rollback procedures");
        assertEquals("Data migration errors", risk.getDescription());
        assertEquals(RiskLevel.HIGH, risk.getRiskLevel());
        assertEquals("Implement rollback procedures", risk.getMitigation());
    }

    @Test
    void riskItem_setters() {
        RiskItem risk = new RiskItem();
        risk.setDescription("API changes may break clients");
        risk.setRiskLevel(RiskLevel.MEDIUM);
        risk.setMitigation("Maintain backward compatibility");
        assertEquals("API changes may break clients", risk.getDescription());
        assertEquals(RiskLevel.MEDIUM, risk.getRiskLevel());
        assertEquals("Maintain backward compatibility", risk.getMitigation());
    }

    @Test
    void setPrimaryTargets_replacesList() {
        ImpactAnalysisResponse response = new ImpactAnalysisResponse();
        List<String> targets = new ArrayList<>();
        targets.add("Target1");
        targets.add("Target2");
        response.setPrimaryTargets(targets);
        assertEquals(2, response.getPrimaryTargets().size());
    }

    @Test
    void setDirectlyAffectedComponents_replacesList() {
        ImpactAnalysisResponse response = new ImpactAnalysisResponse();
        List<ImpactedComponent> components = new ArrayList<>();
        components.add(new ImpactedComponent("Comp1", "Class", "Reason"));
        response.setDirectlyAffectedComponents(components);
        assertEquals(1, response.getDirectlyAffectedComponents().size());
    }

    @Test
    void setIndirectlyAffectedComponents_replacesList() {
        ImpactAnalysisResponse response = new ImpactAnalysisResponse();
        List<ImpactedComponent> components = new ArrayList<>();
        components.add(new ImpactedComponent("Comp1", "Class", "Reason"));
        response.setIndirectlyAffectedComponents(components);
        assertEquals(1, response.getIndirectlyAffectedComponents().size());
    }

    @Test
    void setDependencyImpact_replacesList() {
        ImpactAnalysisResponse response = new ImpactAnalysisResponse();
        List<String> impacts = new ArrayList<>();
        impacts.add("Impact1");
        response.setDependencyImpact(impacts);
        assertEquals(1, response.getDependencyImpact().size());
    }

    @Test
    void setPotentialRisks_replacesList() {
        ImpactAnalysisResponse response = new ImpactAnalysisResponse();
        List<RiskItem> risks = new ArrayList<>();
        risks.add(new RiskItem("Risk1", RiskLevel.LOW, "Mitigation"));
        response.setPotentialRisks(risks);
        assertEquals(1, response.getPotentialRisks().size());
    }
}