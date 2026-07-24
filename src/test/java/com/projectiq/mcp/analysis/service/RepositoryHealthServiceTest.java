package com.projectiq.mcp.analysis.service;

import com.projectiq.mcp.analysis.dto.RepositoryHealthResponse;
import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.dto.ClassSummary;
import com.projectiq.mcp.client.dto.PackageSummary;
import com.projectiq.mcp.client.dto.RepositorySummaryRequest;
import com.projectiq.mcp.client.dto.RepositorySummaryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryHealthServiceTest {

    @Mock
    private IndexerRestClient indexerRestClient;

    private RepositoryHealthService service;

    @BeforeEach
    void setUp() {
        service = new RepositoryHealthService(indexerRestClient);
    }

    @Test
    void analyzeHealth_withSmallRepository_returnsHealthMetrics() {
        // Arrange
        RepositorySummaryResponse summary = createSmallRepositorySummary();
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(summary);

        // Act
        RepositoryHealthResponse response = service.analyzeHealth("small-app", "main");

        // Assert
        assertEquals("small-app", response.getRepositoryName());
        assertEquals("main", response.getBranch());
        assertNotNull(response.getRepositoryOverview());
        assertTrue(response.getHealthScore() >= 0);
        assertTrue(response.getHealthScore() <= 100);
        assertNotNull(response.getMaintainabilityRating());
        assertNotNull(response.getComplexityRating());
        assertNotNull(response.getArchitectureConsistency());
        assertNotNull(response.getDependencyHealth());
        assertNotNull(response.getTestingMaturity());
        assertNotNull(response.getDocumentationMaturity());
        assertNotNull(response.getMaintainabilitySummary());
        assertNotNull(response.getStrengths());
        assertNotNull(response.getObservations());
        assertNotNull(response.getPotentialRisks());
        assertNotNull(response.getSuggestedReviewAreas());
        assertNotNull(response.getConfidenceLevel());
    }

    @Test
    void analyzeHealth_withLargeRepository_returnsHealthMetrics() {
        // Arrange
        RepositorySummaryResponse summary = createLargeRepositorySummary();
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(summary);

        // Act
        RepositoryHealthResponse response = service.analyzeHealth("large-app", "main");

        // Assert
        assertEquals("large-app", response.getRepositoryName());
        assertTrue(response.getHealthScore() >= 0);
        assertTrue(response.getHealthScore() <= 100);
        assertNotNull(response.getMaintainabilityRating());
        assertNotNull(response.getConfidenceLevel());
    }

    @Test
    void analyzeHealth_withMultiModuleRepository_returnsHealthMetrics() {
        // Arrange
        RepositorySummaryResponse summary = createMultiModuleRepositorySummary();
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(summary);

        // Act
        RepositoryHealthResponse response = service.analyzeHealth("multi-module-app", "develop");

        // Assert
        assertEquals("multi-module-app", response.getRepositoryName());
        assertEquals("develop", response.getBranch());
        assertTrue(response.getHealthScore() >= 0);
        assertNotNull(response.getStrengths());
        assertNotNull(response.getObservations());
    }

    @Test
    void analyzeHealth_withEmptyRepository_returnsLowConfidence() {
        // Arrange
        RepositorySummaryResponse summary = new RepositorySummaryResponse();
        summary.setRepositoryName("empty-repo");
        summary.setBranch("main");
        summary.setStatus("INDEXED");
        summary.setPackageCount(0);
        summary.setClassCount(0);
        summary.setMethodCount(0);
        summary.setFileCount(0);
        summary.setPackages(new ArrayList<>());
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(summary);

        // Act
        RepositoryHealthResponse response = service.analyzeHealth("empty-repo", "main");

        // Assert
        assertEquals("LOW", response.getConfidenceLevel());
        assertTrue(response.getHealthScore() >= 0);
        assertTrue(response.getHealthScore() <= 100);
        assertNotNull(response.getMaintainabilityRating());
    }

    @Test
    void analyzeHealth_withMissingMetadata_returnsLowConfidence() {
        // Arrange
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(null);

        // Act
        RepositoryHealthResponse response = service.analyzeHealth("non-existent", "main");

        // Assert
        assertEquals("LOW", response.getConfidenceLevel());
        assertEquals(0, response.getHealthScore());
        assertEquals("Unknown", response.getMaintainabilityRating());
        assertNotNull(response.getRepositoryOverview());
    }

    @Test
    void analyzeHealth_withNullBranch_defaultsToMain() {
        // Arrange
        RepositorySummaryResponse summary = createSmallRepositorySummary();
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(summary);

        // Act
        RepositoryHealthResponse response = service.analyzeHealth("small-app", null);

        // Assert
        assertEquals("main", response.getBranch());
    }

    @Test
    void analyzeHealth_withEmptyBranch_defaultsToMain() {
        // Arrange
        RepositorySummaryResponse summary = createSmallRepositorySummary();
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(summary);

        // Act
        RepositoryHealthResponse response = service.analyzeHealth("small-app", "");

        // Assert
        assertEquals("main", response.getBranch());
    }

    @Test
    void buildRepositoryOverview_withValidSummary_returnsFormattedOverview() {
        // Arrange
        RepositorySummaryResponse summary = createSmallRepositorySummary();

        // Act
        String overview = service.buildRepositoryOverview(summary);

        // Assert
        assertTrue(overview.contains("small-app"));
        assertTrue(overview.contains("main"));
        assertTrue(overview.contains("packages"));
        assertTrue(overview.contains("classes"));
        assertTrue(overview.contains("methods"));
    }

    @Test
    void buildRepositoryOverview_withNullSummary_returnsDefaultMessage() {
        // Act
        String overview = service.buildRepositoryOverview(null);

        // Assert
        assertEquals("No repository data available.", overview);
    }

    @Test
    void analyzePackageOrganization_withMultiplePackages_returnsScore() {
        // Arrange
        List<PackageSummary> packages = createStandardPackages();

        // Act
        int score = service.analyzePackageOrganization(packages);

        // Assert
        assertTrue(score > 0);
        assertTrue(score <= 100);
    }

    @Test
    void analyzePackageOrganization_withEmptyPackages_returnsZero() {
        // Act
        int score = service.analyzePackageOrganization(new ArrayList<>());

        // Assert
        assertEquals(0, score);
    }

    @Test
    void analyzePackageOrganization_withNullPackages_returnsZero() {
        // Act
        int score = service.analyzePackageOrganization(null);

        // Assert
        assertEquals(0, score);
    }

    @Test
    void analyzeClassDistribution_withBalancedClasses_returnsScore() {
        // Arrange
        List<PackageSummary> packages = createStandardPackages();

        // Act
        int score = service.analyzeClassDistribution(packages);

        // Assert
        assertTrue(score >= 0);
        assertTrue(score <= 100);
    }

    @Test
    void analyzeClassDistribution_withEmptyPackages_returnsZero() {
        // Act
        int score = service.analyzeClassDistribution(new ArrayList<>());

        // Assert
        assertEquals(0, score);
    }

    @Test
    void analyzeCsrBalance_withAllLayers_returnsScore() {
        // Arrange
        List<PackageSummary> packages = createStandardPackages();

        // Act
        int score = service.analyzeCsrBalance(packages);

        // Assert
        assertTrue(score > 0);
        assertTrue(score <= 100);
    }

    @Test
    void analyzeCsrBalance_withEmptyPackages_returnsZero() {
        // Act
        int score = service.analyzeCsrBalance(new ArrayList<>());

        // Assert
        assertEquals(0, score);
    }

    @Test
    void analyzeConfigurationComplexity_withConfigPackage_returnsScore() {
        // Arrange
        List<PackageSummary> packages = createStandardPackages();

        // Act
        int score = service.analyzeConfigurationComplexity(packages);

        // Assert
        assertTrue(score >= 0);
        assertTrue(score <= 100);
    }

    @Test
    void analyzeConfigurationComplexity_withNoConfig_returnsHighScore() {
        // Arrange
        List<PackageSummary> packages = new ArrayList<>();
        PackageSummary pkg = new PackageSummary();
        pkg.setPackageName("com.example.controller");
        packages.add(pkg);

        // Act
        int score = service.analyzeConfigurationComplexity(packages);

        // Assert
        assertTrue(score >= 70);
    }

    @Test
    void analyzeDependencyDensity_withStandardClasses_returnsScore() {
        // Arrange
        List<PackageSummary> packages = createStandardPackages();

        // Act
        int score = service.analyzeDependencyDensity(packages);

        // Assert
        assertTrue(score >= 0);
        assertTrue(score <= 100);
    }

    @Test
    void analyzeDependencyDensity_withEmptyPackages_returnsNeutral() {
        // Act
        int score = service.analyzeDependencyDensity(new ArrayList<>());

        // Assert
        assertEquals(50, score);
    }

    @Test
    void analyzeRestApiDistribution_withControllers_returnsScore() {
        // Arrange
        List<PackageSummary> packages = createStandardPackages();

        // Act
        int score = service.analyzeRestApiDistribution(packages);

        // Assert
        assertTrue(score >= 0);
        assertTrue(score <= 100);
    }

    @Test
    void analyzeRestApiDistribution_withNoControllers_returnsHighScore() {
        // Arrange
        List<PackageSummary> packages = new ArrayList<>();
        PackageSummary pkg = new PackageSummary();
        pkg.setPackageName("com.example.util");
        packages.add(pkg);

        // Act
        int score = service.analyzeRestApiDistribution(packages);

        // Assert
        assertTrue(score >= 70);
    }

    @Test
    void analyzeTestCoverage_withTestClasses_returnsScore() {
        // Arrange
        List<PackageSummary> packages = new ArrayList<>();
        PackageSummary pkg = new PackageSummary();
        pkg.setPackageName("com.example.test");
        List<ClassSummary> classes = new ArrayList<>();
        ClassSummary testClass = new ClassSummary();
        testClass.setClassName("UserServiceTest");
        classes.add(testClass);
        ClassSummary mainClass = new ClassSummary();
        mainClass.setClassName("UserService");
        classes.add(mainClass);
        pkg.setClasses(classes);
        packages.add(pkg);

        // Act
        int score = service.analyzeTestCoverage(packages);

        // Assert
        assertTrue(score > 0);
    }

    @Test
    void analyzeTestCoverage_withNoTestClasses_returnsLowScore() {
        // Arrange
        List<PackageSummary> packages = new ArrayList<>();
        PackageSummary pkg = new PackageSummary();
        pkg.setPackageName("com.example.service");
        List<ClassSummary> classes = new ArrayList<>();
        ClassSummary mainClass = new ClassSummary();
        mainClass.setClassName("UserService");
        classes.add(mainClass);
        pkg.setClasses(classes);
        packages.add(pkg);

        // Act
        int score = service.analyzeTestCoverage(packages);

        // Assert
        assertTrue(score <= 10);
    }

    @Test
    void analyzeTestCoverage_withEmptyPackages_returnsZero() {
        // Act
        int score = service.analyzeTestCoverage(new ArrayList<>());

        // Assert
        assertEquals(0, score);
    }

    @Test
    void analyzeDocumentationCoverage_withDocPackages_returnsScore() {
        // Arrange
        List<PackageSummary> packages = new ArrayList<>();
        PackageSummary pkg = new PackageSummary();
        pkg.setPackageName("com.example.doc");
        List<ClassSummary> classes = new ArrayList<>();
        ClassSummary docClass = new ClassSummary();
        docClass.setClassName("ReadmeGenerator");
        classes.add(docClass);
        pkg.setClasses(classes);
        packages.add(pkg);

        // Act
        int score = service.analyzeDocumentationCoverage(packages);

        // Assert
        assertTrue(score > 0);
    }

    @Test
    void analyzeDocumentationCoverage_withNoDocs_returnsLowScore() {
        // Arrange
        List<PackageSummary> packages = createStandardPackages();

        // Act
        int score = service.analyzeDocumentationCoverage(packages);

        // Assert
        assertTrue(score <= 20);
    }

    @Test
    void analyzeRepositorySize_withSmallRepo_returnsHighScore() {
        // Arrange
        RepositorySummaryResponse summary = createSmallRepositorySummary();

        // Act
        int score = service.analyzeRepositorySize(summary);

        // Assert
        assertTrue(score >= 70);
    }

    @Test
    void analyzeRepositorySize_withNullSummary_returnsZero() {
        // Act
        int score = service.analyzeRepositorySize(null);

        // Assert
        assertEquals(0, score);
    }

    @Test
    void calculateOverallHealthScore_withPerfectScores_returnsHighScore() {
        // Act
        int score = service.calculateOverallHealthScore(100, 100, 100, 100, 100, 100, 100, 100, 100);

        // Assert
        assertEquals(100, score);
    }

    @Test
    void calculateOverallHealthScore_withLowScores_returnsLowScore() {
        // Act
        int score = service.calculateOverallHealthScore(0, 0, 0, 0, 0, 0, 0, 0, 0);

        // Assert
        assertEquals(0, score);
    }

    @Test
    void determineConfidence_withNullSummary_returnsLow() {
        // Act
        String confidence = service.determineConfidence(null, new ArrayList<>());

        // Assert
        assertEquals("LOW", confidence);
    }

    @Test
    void determineConfidence_withEmptyPackages_returnsLow() {
        // Arrange
        RepositorySummaryResponse summary = new RepositorySummaryResponse();
        summary.setRepositoryName("test");

        // Act
        String confidence = service.determineConfidence(summary, new ArrayList<>());

        // Assert
        assertEquals("LOW", confidence);
    }

    @Test
    void determineConfidence_withRichData_returnsHigh() {
        // Arrange
        RepositorySummaryResponse summary = new RepositorySummaryResponse();
        summary.setRepositoryName("test");
        summary.setClassCount(20);
        List<PackageSummary> packages = createStandardPackages();

        // Act
        String confidence = service.determineConfidence(summary, packages);

        // Assert
        assertEquals("HIGH", confidence);
    }

    @Test
    void determineConfidence_withSomeData_returnsMedium() {
        // Arrange
        RepositorySummaryResponse summary = new RepositorySummaryResponse();
        summary.setRepositoryName("test");
        summary.setClassCount(5);
        List<PackageSummary> packages = new ArrayList<>();
        PackageSummary pkg = new PackageSummary();
        pkg.setPackageName("com.example");
        packages.add(pkg);

        // Act
        String confidence = service.determineConfidence(summary, packages);

        // Assert
        assertEquals("MEDIUM", confidence);
    }

    @Test
    void retrieveRepositorySummary_whenClientFails_returnsNull() {
        // Arrange
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenThrow(new RuntimeException("Connection failed"));

        // Act
        RepositorySummaryResponse result = service.retrieveRepositorySummary("test", "main");

        // Assert
        assertNull(result);
    }

    @Test
    void detectStrengths_withGoodScores_returnsStrengths() {
        // Arrange
        List<PackageSummary> packages = createStandardPackages();

        // Act
        List<String> strengths = service.detectStrengths(packages, 90, 80, 80, 70, 80);

        // Assert
        assertFalse(strengths.isEmpty());
    }

    @Test
    void detectStrengths_withEmptyPackages_returnsEmptyList() {
        // Act
        List<String> strengths = service.detectStrengths(new ArrayList<>(), 0, 0, 0, 0, 0);

        // Assert
        assertTrue(strengths.isEmpty());
    }

    @Test
    void detectPotentialRisks_withLargePackage_returnsRisk() {
        // Arrange
        List<PackageSummary> packages = new ArrayList<>();
        PackageSummary pkg = new PackageSummary();
        pkg.setPackageName("com.example.large");
        List<ClassSummary> classes = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            ClassSummary cls = new ClassSummary();
            cls.setClassName("Class" + i);
            classes.add(cls);
        }
        pkg.setClasses(classes);
        packages.add(pkg);

        // Act
        List<String> risks = service.detectPotentialRisks(packages, 80, 80, 80, 80);

        // Assert
        assertTrue(risks.stream().anyMatch(r -> r.contains("god package")));
    }

    @Test
    void detectPotentialRisks_withEmptyPackages_returnsRisk() {
        // Act
        List<String> risks = service.detectPotentialRisks(new ArrayList<>(), 80, 80, 80, 80);

        // Assert
        assertTrue(risks.stream().anyMatch(r -> r.contains("No packages detected")));
    }

    @Test
    void detectPotentialRisks_withNoTests_returnsRisk() {
        // Arrange
        List<PackageSummary> packages = createStandardPackages();

        // Act
        List<String> risks = service.detectPotentialRisks(packages, 80, 80, 10, 80);

        // Assert
        assertTrue(risks.stream().anyMatch(r -> r.contains("No test coverage")));
    }

    @Test
    void detectSuggestedReviewAreas_withMissingLayers_returnsReviewAreas() {
        // Arrange
        List<PackageSummary> packages = new ArrayList<>();
        PackageSummary pkg = new PackageSummary();
        pkg.setPackageName("com.example.controller");
        packages.add(pkg);

        // Act
        List<String> reviewAreas = service.detectSuggestedReviewAreas(packages, 30, 80, 80, 80);

        // Assert
        assertFalse(reviewAreas.isEmpty());
    }

    @Test
    void detectSuggestedReviewAreas_withEmptyPackages_returnsEmptyList() {
        // Act
        List<String> reviewAreas = service.detectSuggestedReviewAreas(new ArrayList<>(), 80, 80, 80, 80);

        // Assert
        assertTrue(reviewAreas.isEmpty());
    }

    @Test
    void buildMaintainabilitySummary_withHighScore_returnsPositiveSummary() {
        // Act
        String summary = service.buildMaintainabilitySummary(90, "Excellent", "Low", "Consistent", "Mature");

        // Assert
        assertTrue(summary.contains("90"));
        assertTrue(summary.contains("well-structured"));
    }

    @Test
    void buildMaintainabilitySummary_withLowScore_returnsConcernSummary() {
        // Act
        String summary = service.buildMaintainabilitySummary(20, "Very Poor", "Very High", "Unstructured", "None");

        // Assert
        assertTrue(summary.contains("20"));
        assertTrue(summary.contains("significant structural concerns"));
    }

    // --- Test data builders ---

    private RepositorySummaryResponse createSmallRepositorySummary() {
        RepositorySummaryResponse summary = new RepositorySummaryResponse();
        summary.setRepositoryName("small-app");
        summary.setBranch("main");
        summary.setStatus("INDEXED");
        summary.setPackageCount(3);
        summary.setClassCount(5);
        summary.setMethodCount(20);
        summary.setFileCount(10);
        summary.setCommitCount(10);
        summary.setLastIndexedDate("2024-01-15T10:30:00Z");
        summary.setPackages(createSmallPackages());
        return summary;
    }

    private RepositorySummaryResponse createLargeRepositorySummary() {
        RepositorySummaryResponse summary = new RepositorySummaryResponse();
        summary.setRepositoryName("large-app");
        summary.setBranch("main");
        summary.setStatus("INDEXED");
        summary.setPackageCount(20);
        summary.setClassCount(100);
        summary.setMethodCount(500);
        summary.setFileCount(150);
        summary.setCommitCount(200);
        summary.setLastIndexedDate("2024-06-15T10:30:00Z");
        summary.setPackages(createLargePackages());
        return summary;
    }

    private RepositorySummaryResponse createMultiModuleRepositorySummary() {
        RepositorySummaryResponse summary = new RepositorySummaryResponse();
        summary.setRepositoryName("multi-module-app");
        summary.setBranch("develop");
        summary.setStatus("INDEXED");
        summary.setPackageCount(8);
        summary.setClassCount(30);
        summary.setMethodCount(150);
        summary.setFileCount(50);
        summary.setCommitCount(100);
        summary.setLastIndexedDate("2024-03-01T08:00:00Z");

        List<PackageSummary> packages = new ArrayList<>();

        PackageSummary module1Controller = new PackageSummary();
        module1Controller.setPackageName("com.example.module1.controller");
        module1Controller.setClassCount(2);
        List<ClassSummary> ctrlClasses = new ArrayList<>();
        ClassSummary ctrl = new ClassSummary();
        ctrl.setClassName("Module1Controller");
        ctrl.setMethodCount(5);
        ctrlClasses.add(ctrl);
        module1Controller.setClasses(ctrlClasses);
        packages.add(module1Controller);

        PackageSummary module1Service = new PackageSummary();
        module1Service.setPackageName("com.example.module1.service");
        module1Service.setClassCount(3);
        List<ClassSummary> svcClasses = new ArrayList<>();
        ClassSummary svc = new ClassSummary();
        svc.setClassName("Module1Service");
        svc.setMethodCount(8);
        svcClasses.add(svc);
        module1Service.setClasses(svcClasses);
        packages.add(module1Service);

        PackageSummary module1Repo = new PackageSummary();
        module1Repo.setPackageName("com.example.module1.repository");
        module1Repo.setClassCount(2);
        List<ClassSummary> repoClasses = new ArrayList<>();
        ClassSummary repo = new ClassSummary();
        repo.setClassName("Module1Repository");
        repo.setMethodCount(4);
        repoClasses.add(repo);
        module1Repo.setClasses(repoClasses);
        packages.add(module1Repo);

        PackageSummary module2Controller = new PackageSummary();
        module2Controller.setPackageName("com.example.module2.controller");
        module2Controller.setClassCount(2);
        List<ClassSummary> ctrl2Classes = new ArrayList<>();
        ClassSummary ctrl2 = new ClassSummary();
        ctrl2.setClassName("Module2Controller");
        ctrl2.setMethodCount(5);
        ctrl2Classes.add(ctrl2);
        module2Controller.setClasses(ctrl2Classes);
        packages.add(module2Controller);

        PackageSummary module2Service = new PackageSummary();
        module2Service.setPackageName("com.example.module2.service");
        module2Service.setClassCount(3);
        List<ClassSummary> svc2Classes = new ArrayList<>();
        ClassSummary svc2 = new ClassSummary();
        svc2.setClassName("Module2Service");
        svc2.setMethodCount(8);
        svc2Classes.add(svc2);
        module2Service.setClasses(svc2Classes);
        packages.add(module2Service);

        PackageSummary module2Repo = new PackageSummary();
        module2Repo.setPackageName("com.example.module2.repository");
        module2Repo.setClassCount(2);
        List<ClassSummary> repo2Classes = new ArrayList<>();
        ClassSummary repo2 = new ClassSummary();
        repo2.setClassName("Module2Repository");
        repo2.setMethodCount(4);
        repo2Classes.add(repo2);
        module2Repo.setClasses(repo2Classes);
        packages.add(module2Repo);

        PackageSummary entityPkg = new PackageSummary();
        entityPkg.setPackageName("com.example.entity");
        entityPkg.setClassCount(3);
        List<ClassSummary> entityClasses = new ArrayList<>();
        ClassSummary entity = new ClassSummary();
        entity.setClassName("SharedEntity");
        entity.setMethodCount(3);
        entityClasses.add(entity);
        entityPkg.setClasses(entityClasses);
        packages.add(entityPkg);

        PackageSummary dtoPkg = new PackageSummary();
        dtoPkg.setPackageName("com.example.dto");
        dtoPkg.setClassCount(3);
        List<ClassSummary> dtoClasses = new ArrayList<>();
        ClassSummary dto = new ClassSummary();
        dto.setClassName("SharedDto");
        dto.setMethodCount(2);
        dtoClasses.add(dto);
        dtoPkg.setClasses(dtoClasses);
        packages.add(dtoPkg);

        summary.setPackages(packages);
        return summary;
    }

    private List<PackageSummary> createSmallPackages() {
        List<PackageSummary> packages = new ArrayList<>();

        PackageSummary controllerPkg = new PackageSummary();
        controllerPkg.setPackageName("com.example.controller");
        controllerPkg.setClassCount(1);
        List<ClassSummary> controllerClasses = new ArrayList<>();
        ClassSummary controllerClass = new ClassSummary();
        controllerClass.setClassName("HomeController");
        controllerClass.setMethodCount(3);
        controllerClasses.add(controllerClass);
        controllerPkg.setClasses(controllerClasses);
        packages.add(controllerPkg);

        PackageSummary servicePkg = new PackageSummary();
        servicePkg.setPackageName("com.example.service");
        servicePkg.setClassCount(2);
        List<ClassSummary> serviceClasses = new ArrayList<>();
        ClassSummary serviceClass = new ClassSummary();
        serviceClass.setClassName("HomeService");
        serviceClass.setMethodCount(5);
        serviceClasses.add(serviceClass);
        servicePkg.setClasses(serviceClasses);
        packages.add(servicePkg);

        PackageSummary repoPkg = new PackageSummary();
        repoPkg.setPackageName("com.example.repository");
        repoPkg.setClassCount(2);
        List<ClassSummary> repoClasses = new ArrayList<>();
        ClassSummary repoClass = new ClassSummary();
        repoClass.setClassName("HomeRepository");
        repoClass.setMethodCount(4);
        repoClasses.add(repoClass);
        repoPkg.setClasses(repoClasses);
        packages.add(repoPkg);

        return packages;
    }

    private List<PackageSummary> createLargePackages() {
        List<PackageSummary> packages = new ArrayList<>();

        String[] packageNames = {
                "com.example.controller", "com.example.service", "com.example.repository",
                "com.example.entity", "com.example.dto", "com.example.config",
                "com.example.util", "com.example.security", "com.example.event",
                "com.example.scheduler", "com.example.cache", "com.example.messaging",
                "com.example.validation", "com.example.exception", "com.example.mapper",
                "com.example.filter", "com.example.interceptor", "com.example.aspect",
                "com.example.client", "com.example.provider"
        };

        for (String pkgName : packageNames) {
            PackageSummary pkg = new PackageSummary();
            pkg.setPackageName(pkgName);
            pkg.setClassCount(5);
            List<ClassSummary> classes = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                ClassSummary cls = new ClassSummary();
                cls.setClassName(pkgName.substring(pkgName.lastIndexOf('.') + 1) + "Class" + i);
                cls.setMethodCount(5);
                classes.add(cls);
            }
            pkg.setClasses(classes);
            packages.add(pkg);
        }

        return packages;
    }

    private List<PackageSummary> createStandardPackages() {
        List<PackageSummary> packages = new ArrayList<>();

        PackageSummary controllerPkg = new PackageSummary();
        controllerPkg.setPackageName("com.example.controller");
        controllerPkg.setClassCount(3);
        List<ClassSummary> controllerClasses = new ArrayList<>();
        ClassSummary controllerClass = new ClassSummary();
        controllerClass.setClassName("UserController");
        controllerClass.setMethodCount(5);
        controllerClasses.add(controllerClass);
        controllerPkg.setClasses(controllerClasses);
        packages.add(controllerPkg);

        PackageSummary servicePkg = new PackageSummary();
        servicePkg.setPackageName("com.example.service");
        servicePkg.setClassCount(4);
        List<ClassSummary> serviceClasses = new ArrayList<>();
        ClassSummary serviceClass = new ClassSummary();
        serviceClass.setClassName("UserService");
        serviceClass.setMethodCount(8);
        serviceClasses.add(serviceClass);
        servicePkg.setClasses(serviceClasses);
        packages.add(servicePkg);

        PackageSummary repoPkg = new PackageSummary();
        repoPkg.setPackageName("com.example.repository");
        repoPkg.setClassCount(3);
        List<ClassSummary> repoClasses = new ArrayList<>();
        ClassSummary repoClass = new ClassSummary();
        repoClass.setClassName("UserRepository");
        repoClass.setMethodCount(4);
        repoClasses.add(repoClass);
        repoPkg.setClasses(repoClasses);
        packages.add(repoPkg);

        PackageSummary entityPkg = new PackageSummary();
        entityPkg.setPackageName("com.example.entity");
        entityPkg.setClassCount(5);
        List<ClassSummary> entityClasses = new ArrayList<>();
        ClassSummary entityClass = new ClassSummary();
        entityClass.setClassName("UserEntity");
        entityClass.setMethodCount(3);
        entityClasses.add(entityClass);
        entityPkg.setClasses(entityClasses);
        packages.add(entityPkg);

        PackageSummary dtoPkg = new PackageSummary();
        dtoPkg.setPackageName("com.example.dto");
        dtoPkg.setClassCount(3);
        List<ClassSummary> dtoClasses = new ArrayList<>();
        ClassSummary dtoClass = new ClassSummary();
        dtoClass.setClassName("UserRequest");
        dtoClass.setMethodCount(2);
        dtoClasses.add(dtoClass);
        dtoPkg.setClasses(dtoClasses);
        packages.add(dtoPkg);

        PackageSummary configPkg = new PackageSummary();
        configPkg.setPackageName("com.example.config");
        configPkg.setClassCount(2);
        List<ClassSummary> configClasses = new ArrayList<>();
        ClassSummary configClass = new ClassSummary();
        configClass.setClassName("AppConfig");
        configClass.setMethodCount(3);
        configClasses.add(configClass);
        configPkg.setClasses(configClasses);
        packages.add(configPkg);

        return packages;
    }
}