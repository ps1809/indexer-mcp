package com.projectiq.mcp.analysis.dto;

import com.projectiq.mcp.analysis.dto.RepositoryEvolutionAnalysisResponse.ArchitectureEvolutionAnalysis;
import com.projectiq.mcp.analysis.dto.RepositoryEvolutionAnalysisResponse.ConventionConsistencyAnalysis;
import com.projectiq.mcp.analysis.dto.RepositoryEvolutionAnalysisResponse.DependencyEvolutionAnalysis;
import com.projectiq.mcp.analysis.dto.RepositoryEvolutionAnalysisResponse.MaintainabilityAnalysis;
import com.projectiq.mcp.analysis.dto.RepositoryEvolutionAnalysisResponse.ModuleExpansionAnalysis;
import com.projectiq.mcp.analysis.dto.RepositoryEvolutionAnalysisResponse.PackageGrowthAnalysis;
import com.projectiq.mcp.analysis.dto.RepositoryEvolutionAnalysisResponse.ScalabilityReadinessAnalysis;
import com.projectiq.mcp.analysis.dto.RepositoryEvolutionAnalysisResponse.TechnicalDebtIndicatorsAnalysis;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryEvolutionAnalysisResponseTest {

    @Test
    void testDefaultConstructor() {
        RepositoryEvolutionAnalysisResponse response = new RepositoryEvolutionAnalysisResponse();
        assertNotNull(response.getTechnicalDebtIndicators());
        assertNotNull(response.getLongTermRisks());
        assertNotNull(response.getRecommendedRepositoryPractices());
        assertTrue(response.getTechnicalDebtIndicators().isEmpty());
        assertTrue(response.getLongTermRisks().isEmpty());
        assertTrue(response.getRecommendedRepositoryPractices().isEmpty());
    }

    @Test
    void testSettersAndGetters() {
        RepositoryEvolutionAnalysisResponse response = new RepositoryEvolutionAnalysisResponse();
        response.setRepositoryName("test-repo");
        response.setBranch("main");
        response.setProposedChange("Add new service");
        response.setProposedChangeSummary("Summary of change");
        response.setArchitecturalImpact("Moderate impact");
        response.setMaintainabilityAssessment("Good");
        response.setConventionCompliance("Compliant");
        response.setScalabilityConsiderations("Scalable");
        response.setRepositoryEvolutionScore(75);

        List<String> debtIndicators = Arrays.asList("Indicator 1", "Indicator 2");
        response.setTechnicalDebtIndicators(debtIndicators);

        List<String> risks = Arrays.asList("Risk 1", "Risk 2");
        response.setLongTermRisks(risks);

        List<String> practices = Arrays.asList("Practice 1", "Practice 2");
        response.setRecommendedRepositoryPractices(practices);

        assertEquals("test-repo", response.getRepositoryName());
        assertEquals("main", response.getBranch());
        assertEquals("Add new service", response.getProposedChange());
        assertEquals("Summary of change", response.getProposedChangeSummary());
        assertEquals("Moderate impact", response.getArchitecturalImpact());
        assertEquals("Good", response.getMaintainabilityAssessment());
        assertEquals("Compliant", response.getConventionCompliance());
        assertEquals("Scalable", response.getScalabilityConsiderations());
        assertEquals(Integer.valueOf(75), response.getRepositoryEvolutionScore());
        assertEquals(2, response.getTechnicalDebtIndicators().size());
        assertEquals(2, response.getLongTermRisks().size());
        assertEquals(2, response.getRecommendedRepositoryPractices().size());
    }

    @Test
    void testNestedAnalysisObjects() {
        RepositoryEvolutionAnalysisResponse response = new RepositoryEvolutionAnalysisResponse();

        // ArchitectureEvolutionAnalysis
        ArchitectureEvolutionAnalysis archAnalysis = new ArchitectureEvolutionAnalysis();
        archAnalysis.setSummary("Architecture is stable");
        archAnalysis.setArchitectureScore(80);
        archAnalysis.setArchitecturalDrifts(Arrays.asList("Drift 1"));
        response.setArchitectureEvolution(archAnalysis);

        assertNotNull(response.getArchitectureEvolution());
        assertEquals("Architecture is stable", response.getArchitectureEvolution().getSummary());
        assertEquals(Integer.valueOf(80), response.getArchitectureEvolution().getArchitectureScore());
        assertEquals(1, response.getArchitectureEvolution().getArchitecturalDrifts().size());

        // PackageGrowthAnalysis
        PackageGrowthAnalysis pkgGrowth = new PackageGrowthAnalysis();
        pkgGrowth.setSummary("Package growth expected");
        pkgGrowth.setEstimatedNewPackages(2);
        pkgGrowth.setEstimatedPackageDensity(10);
        response.setPackageGrowth(pkgGrowth);

        assertNotNull(response.getPackageGrowth());
        assertEquals(Integer.valueOf(2), response.getPackageGrowth().getEstimatedNewPackages());
        assertEquals(Integer.valueOf(10), response.getPackageGrowth().getEstimatedPackageDensity());

        // ModuleExpansionAnalysis
        ModuleExpansionAnalysis moduleExp = new ModuleExpansionAnalysis();
        moduleExp.setSummary("Module expansion expected");
        moduleExp.setEstimatedNewClasses(5);
        moduleExp.setModuleCohesionScore(70);
        response.setModuleExpansion(moduleExp);

        assertNotNull(response.getModuleExpansion());
        assertEquals(Integer.valueOf(5), response.getModuleExpansion().getEstimatedNewClasses());
        assertEquals(Integer.valueOf(70), response.getModuleExpansion().getModuleCohesionScore());

        // DependencyEvolutionAnalysis
        DependencyEvolutionAnalysis depEvo = new DependencyEvolutionAnalysis();
        depEvo.setSummary("Dependency evolution expected");
        depEvo.setEstimatedNewDependencies(3);
        depEvo.setCircularDependencyRisk(true);
        response.setDependencyEvolution(depEvo);

        assertNotNull(response.getDependencyEvolution());
        assertEquals(Integer.valueOf(3), response.getDependencyEvolution().getEstimatedNewDependencies());
        assertTrue(response.getDependencyEvolution().getCircularDependencyRisk());

        // ConventionConsistencyAnalysis
        ConventionConsistencyAnalysis convCons = new ConventionConsistencyAnalysis();
        convCons.setSummary("Conventions are consistent");
        convCons.setConventionScore(85);
        convCons.setDeviations(Arrays.asList("Deviation 1"));
        response.setConventionConsistency(convCons);

        assertNotNull(response.getConventionConsistency());
        assertEquals(Integer.valueOf(85), response.getConventionConsistency().getConventionScore());
        assertEquals(1, response.getConventionConsistency().getDeviations().size());

        // MaintainabilityAnalysis
        MaintainabilityAnalysis maint = new MaintainabilityAnalysis();
        maint.setSummary("Maintainability is good");
        maint.setMaintainabilityScore(75);
        maint.setComplexityConcerns(Arrays.asList("Concern 1"));
        response.setMaintainability(maint);

        assertNotNull(response.getMaintainability());
        assertEquals(Integer.valueOf(75), response.getMaintainability().getMaintainabilityScore());
        assertEquals(1, response.getMaintainability().getComplexityConcerns().size());

        // TechnicalDebtIndicatorsAnalysis
        TechnicalDebtIndicatorsAnalysis debt = new TechnicalDebtIndicatorsAnalysis();
        debt.setSummary("Low debt");
        debt.setTechnicalDebtScore(80);
        debt.setDebtIndicators(Arrays.asList("Indicator 1"));
        response.setTechnicalDebtAnalysis(debt);

        assertNotNull(response.getTechnicalDebtAnalysis());
        assertEquals(Integer.valueOf(80), response.getTechnicalDebtAnalysis().getTechnicalDebtScore());
        assertEquals(1, response.getTechnicalDebtAnalysis().getDebtIndicators().size());

        // ScalabilityReadinessAnalysis
        ScalabilityReadinessAnalysis scalability = new ScalabilityReadinessAnalysis();
        scalability.setSummary("Scalable");
        scalability.setScalabilityScore(70);
        scalability.setScalabilityConcerns(Arrays.asList("Concern 1"));
        response.setScalabilityReadiness(scalability);

        assertNotNull(response.getScalabilityReadiness());
        assertEquals(Integer.valueOf(70), response.getScalabilityReadiness().getScalabilityScore());
        assertEquals(1, response.getScalabilityReadiness().getScalabilityConcerns().size());
    }

    @Test
    void testNullListsAreHandled() {
        RepositoryEvolutionAnalysisResponse response = new RepositoryEvolutionAnalysisResponse();
        response.setTechnicalDebtIndicators(null);
        response.setLongTermRisks(null);
        response.setRecommendedRepositoryPractices(null);

        assertNotNull(response.getTechnicalDebtIndicators());
        assertNotNull(response.getLongTermRisks());
        assertNotNull(response.getRecommendedRepositoryPractices());
        assertTrue(response.getTechnicalDebtIndicators().isEmpty());
        assertTrue(response.getLongTermRisks().isEmpty());
        assertTrue(response.getRecommendedRepositoryPractices().isEmpty());
    }

    @Test
    void testArchitectureEvolutionAnalysisDefaults() {
        ArchitectureEvolutionAnalysis analysis = new ArchitectureEvolutionAnalysis();
        assertNotNull(analysis.getArchitecturalDrifts());
        assertTrue(analysis.getArchitecturalDrifts().isEmpty());
    }

    @Test
    void testConventionConsistencyAnalysisDefaults() {
        ConventionConsistencyAnalysis analysis = new ConventionConsistencyAnalysis();
        assertNotNull(analysis.getDeviations());
        assertTrue(analysis.getDeviations().isEmpty());
    }

    @Test
    void testMaintainabilityAnalysisDefaults() {
        MaintainabilityAnalysis analysis = new MaintainabilityAnalysis();
        assertNotNull(analysis.getComplexityConcerns());
        assertTrue(analysis.getComplexityConcerns().isEmpty());
    }

    @Test
    void testTechnicalDebtIndicatorsAnalysisDefaults() {
        TechnicalDebtIndicatorsAnalysis analysis = new TechnicalDebtIndicatorsAnalysis();
        assertNotNull(analysis.getDebtIndicators());
        assertTrue(analysis.getDebtIndicators().isEmpty());
    }

    @Test
    void testScalabilityReadinessAnalysisDefaults() {
        ScalabilityReadinessAnalysis analysis = new ScalabilityReadinessAnalysis();
        assertNotNull(analysis.getScalabilityConcerns());
        assertTrue(analysis.getScalabilityConcerns().isEmpty());
    }
}