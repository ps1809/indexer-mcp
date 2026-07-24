package com.projectiq.mcp.analysis.service;

import com.projectiq.mcp.analysis.dto.ArchitectureInsightsResponse;
import com.projectiq.mcp.analysis.dto.ArchitectureInsightsResponse.ModuleRelationship;
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
class ArchitectureInsightsServiceTest {

    @Mock
    private IndexerRestClient indexerRestClient;

    private ArchitectureInsightsService service;

    @BeforeEach
    void setUp() {
        service = new ArchitectureInsightsService(indexerRestClient);
    }

    @Test
    void analyzeArchitecture_withStandardSpringBootProject_detectsLayeredArchitecture() {
        // Arrange
        RepositorySummaryResponse summary = createStandardSpringBootSummary();
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(summary);

        // Act
        ArchitectureInsightsResponse response = service.analyzeArchitecture("my-app", "main");

        // Assert
        assertEquals("my-app", response.getRepositoryName());
        assertEquals("main", response.getBranch());
        assertNotNull(response.getRepositoryOverview());
        assertTrue(response.getDetectedLayers().contains("Controller (Presentation)"));
        assertTrue(response.getDetectedLayers().contains("Service (Business Logic)"));
        assertTrue(response.getDetectedLayers().contains("Repository (Data Access)"));
        assertTrue(response.getDetectedLayers().contains("Entity (Domain Model)"));
        assertTrue(response.getDetectedLayers().contains("DTO (Data Transfer)"));
        assertTrue(response.getArchitecturalStyle().contains("Layered Architecture"));
        assertFalse(response.getModuleRelationships().isEmpty());
        assertNotNull(response.getDependencyFlow());
        assertFalse(response.getArchitecturalStrengths().isEmpty());
        assertNotNull(response.getConfidenceLevel());
    }

    @Test
    void analyzeArchitecture_withMultiModuleRepository_detectsModuleRelationships() {
        // Arrange
        RepositorySummaryResponse summary = createMultiModuleSummary();
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(summary);

        // Act
        ArchitectureInsightsResponse response = service.analyzeArchitecture("multi-module-app", "develop");

        // Assert
        assertEquals("multi-module-app", response.getRepositoryName());
        assertEquals("develop", response.getBranch());
        assertFalse(response.getModuleRelationships().isEmpty());

        // Verify at least one relationship is detected
        boolean hasRelationship = response.getModuleRelationships().stream()
                .anyMatch(r -> r.getRelationshipType() != null);
        assertTrue(hasRelationship);
    }

    @Test
    void analyzeArchitecture_withLayeredArchitecture_detectsCorrectStyle() {
        // Arrange
        RepositorySummaryResponse summary = createLayeredArchitectureSummary();
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(summary);

        // Act
        ArchitectureInsightsResponse response = service.analyzeArchitecture("layered-app", "main");

        // Assert
        assertTrue(response.getArchitecturalStyle().contains("Layered Architecture"));
        assertTrue(response.getDetectedLayers().contains("Controller (Presentation)"));
        assertTrue(response.getDetectedLayers().contains("Service (Business Logic)"));
        assertTrue(response.getDetectedLayers().contains("Repository (Data Access)"));
    }

    @Test
    void analyzeArchitecture_withMissingPackageData_returnsLowConfidence() {
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
        ArchitectureInsightsResponse response = service.analyzeArchitecture("empty-repo", "main");

        // Assert
        assertEquals("LOW", response.getConfidenceLevel());
        assertTrue(response.getDetectedLayers().isEmpty());
        assertTrue(response.getModuleRelationships().isEmpty());
    }

    @Test
    void analyzeArchitecture_withEmptyRepository_returnsLowConfidence() {
        // Arrange
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(null);

        // Act
        ArchitectureInsightsResponse response = service.analyzeArchitecture("non-existent", "main");

        // Assert
        assertEquals("LOW", response.getConfidenceLevel());
        assertEquals("Unknown", response.getArchitecturalStyle());
        assertNotNull(response.getRepositoryOverview());
    }

    @Test
    void analyzeArchitecture_withNullBranch_defaultsToMain() {
        // Arrange
        RepositorySummaryResponse summary = createStandardSpringBootSummary();
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(summary);

        // Act
        ArchitectureInsightsResponse response = service.analyzeArchitecture("my-app", null);

        // Assert
        assertEquals("main", response.getBranch());
    }

    @Test
    void analyzeArchitecture_withEmptyBranch_defaultsToMain() {
        // Arrange
        RepositorySummaryResponse summary = createStandardSpringBootSummary();
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(summary);

        // Act
        ArchitectureInsightsResponse response = service.analyzeArchitecture("my-app", "");

        // Assert
        assertEquals("main", response.getBranch());
    }

    @Test
    void buildRepositoryOverview_withValidSummary_returnsFormattedOverview() {
        // Arrange
        RepositorySummaryResponse summary = createStandardSpringBootSummary();

        // Act
        String overview = service.buildRepositoryOverview(summary);

        // Assert
        assertTrue(overview.contains("my-app"));
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
    void detectLayers_withStandardPackages_detectsAllLayers() {
        // Arrange
        List<PackageSummary> packages = createStandardPackages();

        // Act
        List<String> layers = service.detectLayers(packages);

        // Assert
        assertTrue(layers.contains("Controller (Presentation)"));
        assertTrue(layers.contains("Service (Business Logic)"));
        assertTrue(layers.contains("Repository (Data Access)"));
        assertTrue(layers.contains("Entity (Domain Model)"));
        assertTrue(layers.contains("DTO (Data Transfer)"));
        assertTrue(layers.contains("Configuration"));
    }

    @Test
    void detectLayers_withEmptyPackages_returnsEmptyList() {
        // Act
        List<String> layers = service.detectLayers(new ArrayList<>());

        // Assert
        assertTrue(layers.isEmpty());
    }

    @Test
    void detectArchitecturalStyle_withControllerServiceRepository_returnsLayered() {
        // Arrange
        List<String> layers = new ArrayList<>();
        layers.add("Controller (Presentation)");
        layers.add("Service (Business Logic)");
        layers.add("Repository (Data Access)");
        List<PackageSummary> packages = createStandardPackages();

        // Act
        String style = service.detectArchitecturalStyle(layers, packages);

        // Assert
        assertTrue(style.contains("Layered Architecture"));
    }

    @Test
    void detectArchitecturalStyle_withOnlyController_returnsControllerBased() {
        // Arrange
        List<String> layers = new ArrayList<>();
        layers.add("Controller (Presentation)");
        List<PackageSummary> packages = new ArrayList<>();

        // Act
        String style = service.detectArchitecturalStyle(layers, packages);

        // Assert
        assertEquals("Controller-Based Architecture", style);
    }

    @Test
    void detectArchitecturalStyle_withNoLayers_returnsModular() {
        // Arrange
        List<String> layers = new ArrayList<>();
        List<PackageSummary> packages = new ArrayList<>();

        // Act
        String style = service.detectArchitecturalStyle(layers, packages);

        // Assert
        assertEquals("Modular Architecture", style);
    }

    @Test
    void detectModuleRelationships_withMultiplePackages_detectsRelationships() {
        // Arrange
        List<PackageSummary> packages = createStandardPackages();

        // Act
        List<ModuleRelationship> relationships = service.detectModuleRelationships(packages);

        // Assert
        assertFalse(relationships.isEmpty());
    }

    @Test
    void detectModuleRelationships_withSinglePackage_returnsEmptyList() {
        // Arrange
        List<PackageSummary> packages = new ArrayList<>();
        PackageSummary pkg = new PackageSummary();
        pkg.setPackageName("com.example");
        packages.add(pkg);

        // Act
        List<ModuleRelationship> relationships = service.detectModuleRelationships(packages);

        // Assert
        assertTrue(relationships.isEmpty());
    }

    @Test
    void detectModuleRelationships_withNullPackages_returnsEmptyList() {
        // Act
        List<ModuleRelationship> relationships = service.detectModuleRelationships(null);

        // Assert
        assertTrue(relationships.isEmpty());
    }

    @Test
    void determineDependencyFlow_withAllLayers_returnsDownwardFlow() {
        // Arrange
        List<String> layers = new ArrayList<>();
        layers.add("Controller (Presentation)");
        layers.add("Service (Business Logic)");
        layers.add("Repository (Data Access)");

        // Act
        String flow = service.determineDependencyFlow(layers, new ArrayList<>());

        // Assert
        assertTrue(flow.contains("Controller"));
        assertTrue(flow.contains("Service"));
        assertTrue(flow.contains("Repository"));
    }

    @Test
    void determineDependencyFlow_withNoLayers_returnsNoFlow() {
        // Arrange
        List<String> layers = new ArrayList<>();

        // Act
        String flow = service.determineDependencyFlow(layers, new ArrayList<>());

        // Assert
        assertEquals("No detectable dependency flow", flow);
    }

    @Test
    void detectCrossLayerDependencies_withControllerAndRepository_returnsWarning() {
        // Arrange
        List<PackageSummary> packages = createStandardPackages();

        // Act
        List<String> deps = service.detectCrossLayerDependencies(packages);

        // Assert
        assertFalse(deps.isEmpty());
    }

    @Test
    void detectCrossLayerDependencies_withEmptyPackages_returnsEmptyList() {
        // Act
        List<String> deps = service.detectCrossLayerDependencies(new ArrayList<>());

        // Assert
        assertTrue(deps.isEmpty());
    }

    @Test
    void detectArchitecturalPatterns_withStandardPackages_detectsPatterns() {
        // Arrange
        List<PackageSummary> packages = createStandardPackages();
        List<String> layers = new ArrayList<>();
        layers.add("Controller (Presentation)");
        layers.add("Service (Business Logic)");
        layers.add("Repository (Data Access)");

        // Act
        List<String> patterns = service.detectArchitecturalPatterns(packages, layers, "Layered Architecture");

        // Assert
        assertTrue(patterns.contains("Layered Architecture"));
        assertTrue(patterns.contains("Repository Pattern"));
        assertTrue(patterns.contains("Service Layer Pattern"));
    }

    @Test
    void detectArchitecturalPatterns_withBuilderAndFactory_detectsPatterns() {
        // Arrange
        List<PackageSummary> packages = new ArrayList<>();
        PackageSummary pkg = new PackageSummary();
        pkg.setPackageName("com.example.util");
        List<ClassSummary> classes = new ArrayList<>();

        ClassSummary builderClass = new ClassSummary();
        builderClass.setClassName("UserBuilder");
        classes.add(builderClass);

        ClassSummary factoryClass = new ClassSummary();
        factoryClass.setClassName("ServiceFactory");
        classes.add(factoryClass);

        pkg.setClasses(classes);
        packages.add(pkg);

        List<String> layers = new ArrayList<>();

        // Act
        List<String> patterns = service.detectArchitecturalPatterns(packages, layers, "Modular Architecture");

        // Assert
        assertTrue(patterns.contains("Builder Pattern"));
        assertTrue(patterns.contains("Factory Pattern"));
    }

    @Test
    void detectArchitecturalPatterns_withStrategyAndListener_detectsPatterns() {
        // Arrange
        List<PackageSummary> packages = new ArrayList<>();
        PackageSummary pkg = new PackageSummary();
        pkg.setPackageName("com.example.pattern");
        List<ClassSummary> classes = new ArrayList<>();

        ClassSummary strategyClass = new ClassSummary();
        strategyClass.setClassName("PaymentStrategy");
        classes.add(strategyClass);

        ClassSummary listenerClass = new ClassSummary();
        listenerClass.setClassName("EventListener");
        classes.add(listenerClass);

        pkg.setClasses(classes);
        packages.add(pkg);

        List<String> layers = new ArrayList<>();

        // Act
        List<String> patterns = service.detectArchitecturalPatterns(packages, layers, "Modular Architecture");

        // Assert
        assertTrue(patterns.contains("Strategy Pattern"));
        assertTrue(patterns.contains("Observer Pattern"));
    }

    @Test
    void detectArchitecturalPatterns_withEmptyPackages_returnsEmptyList() {
        // Act
        List<String> patterns = service.detectArchitecturalPatterns(
                new ArrayList<>(), new ArrayList<>(), "Unknown");

        // Assert
        assertTrue(patterns.isEmpty());
    }

    @Test
    void detectPotentialConcerns_withNoLayers_returnsConcern() {
        // Arrange
        List<PackageSummary> packages = createStandardPackages();
        List<String> layers = new ArrayList<>();

        // Act
        List<String> concerns = service.detectPotentialConcerns(packages, layers);

        // Assert
        assertTrue(concerns.stream().anyMatch(c -> c.contains("No architectural layers detected")));
    }

    @Test
    void detectPotentialConcerns_withEmptyPackages_returnsConcern() {
        // Act
        List<String> concerns = service.detectPotentialConcerns(new ArrayList<>(), new ArrayList<>());

        // Assert
        assertTrue(concerns.stream().anyMatch(c -> c.contains("No packages detected")));
    }

    @Test
    void detectPotentialConcerns_withControllerNoService_returnsConcern() {
        // Arrange
        List<PackageSummary> packages = new ArrayList<>();
        PackageSummary pkg = new PackageSummary();
        pkg.setPackageName("com.example.controller");
        packages.add(pkg);

        List<String> layers = new ArrayList<>();
        layers.add("Controller (Presentation)");

        // Act
        List<String> concerns = service.detectPotentialConcerns(packages, layers);

        // Assert
        assertTrue(concerns.stream().anyMatch(c -> c.contains("Controllers detected without")));
    }

    @Test
    void detectPotentialConcerns_withLargePackage_returnsConcern() {
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

        List<String> layers = new ArrayList<>();

        // Act
        List<String> concerns = service.detectPotentialConcerns(packages, layers);

        // Assert
        assertTrue(concerns.stream().anyMatch(c -> c.contains("consider splitting")));
    }

    @Test
    void determineConfidence_withNullSummary_returnsLow() {
        // Act
        String confidence = service.determineConfidence(null, new ArrayList<>(), new ArrayList<>());

        // Assert
        assertEquals("LOW", confidence);
    }

    @Test
    void determineConfidence_withEmptyPackages_returnsLow() {
        // Arrange
        RepositorySummaryResponse summary = new RepositorySummaryResponse();
        summary.setRepositoryName("test");

        // Act
        String confidence = service.determineConfidence(summary, new ArrayList<>(), new ArrayList<>());

        // Assert
        assertEquals("LOW", confidence);
    }

    @Test
    void determineConfidence_withThreeLayersAndThreePackages_returnsHigh() {
        // Arrange
        RepositorySummaryResponse summary = new RepositorySummaryResponse();
        summary.setRepositoryName("test");
        List<PackageSummary> packages = createStandardPackages();
        List<String> layers = new ArrayList<>();
        layers.add("Controller (Presentation)");
        layers.add("Service (Business Logic)");
        layers.add("Repository (Data Access)");

        // Act
        String confidence = service.determineConfidence(summary, packages, layers);

        // Assert
        assertEquals("HIGH", confidence);
    }

    @Test
    void determineConfidence_withOneLayerAndOnePackage_returnsMedium() {
        // Arrange
        RepositorySummaryResponse summary = new RepositorySummaryResponse();
        summary.setRepositoryName("test");
        List<PackageSummary> packages = new ArrayList<>();
        PackageSummary pkg = new PackageSummary();
        pkg.setPackageName("com.example");
        packages.add(pkg);
        List<String> layers = new ArrayList<>();
        layers.add("Controller (Presentation)");

        // Act
        String confidence = service.determineConfidence(summary, packages, layers);

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

    // --- Test data builders ---

    private RepositorySummaryResponse createStandardSpringBootSummary() {
        RepositorySummaryResponse summary = new RepositorySummaryResponse();
        summary.setRepositoryName("my-app");
        summary.setBranch("main");
        summary.setStatus("INDEXED");
        summary.setPackageCount(6);
        summary.setClassCount(20);
        summary.setMethodCount(100);
        summary.setFileCount(30);
        summary.setCommitCount(50);
        summary.setLastIndexedDate("2024-01-15T10:30:00Z");
        summary.setPackages(createStandardPackages());
        return summary;
    }

    private RepositorySummaryResponse createMultiModuleSummary() {
        RepositorySummaryResponse summary = new RepositorySummaryResponse();
        summary.setRepositoryName("multi-module-app");
        summary.setBranch("develop");
        summary.setStatus("INDEXED");
        summary.setPackageCount(4);
        summary.setClassCount(15);
        summary.setMethodCount(80);
        summary.setFileCount(25);
        summary.setCommitCount(100);
        summary.setLastIndexedDate("2024-02-01T08:00:00Z");

        List<PackageSummary> packages = new ArrayList<>();

        PackageSummary module1 = new PackageSummary();
        module1.setPackageName("com.example.module1.controller");
        module1.setClassCount(2);
        packages.add(module1);

        PackageSummary module2 = new PackageSummary();
        module2.setPackageName("com.example.module1.service");
        module2.setClassCount(3);
        packages.add(module2);

        PackageSummary module3 = new PackageSummary();
        module3.setPackageName("com.example.module2.controller");
        module3.setClassCount(2);
        packages.add(module3);

        PackageSummary module4 = new PackageSummary();
        module4.setPackageName("com.example.module2.service");
        module4.setClassCount(3);
        packages.add(module4);

        summary.setPackages(packages);
        return summary;
    }

    private RepositorySummaryResponse createLayeredArchitectureSummary() {
        RepositorySummaryResponse summary = new RepositorySummaryResponse();
        summary.setRepositoryName("layered-app");
        summary.setBranch("main");
        summary.setStatus("INDEXED");
        summary.setPackageCount(3);
        summary.setClassCount(10);
        summary.setMethodCount(50);
        summary.setFileCount(15);
        summary.setCommitCount(30);
        summary.setLastIndexedDate("2024-03-01T12:00:00Z");

        List<PackageSummary> packages = new ArrayList<>();

        PackageSummary controllerPkg = new PackageSummary();
        controllerPkg.setPackageName("com.example.controller");
        controllerPkg.setClassCount(3);
        List<ClassSummary> controllerClasses = new ArrayList<>();
        ClassSummary controllerClass = new ClassSummary();
        controllerClass.setClassName("UserController");
        controllerClasses.add(controllerClass);
        controllerPkg.setClasses(controllerClasses);
        packages.add(controllerPkg);

        PackageSummary servicePkg = new PackageSummary();
        servicePkg.setPackageName("com.example.service");
        servicePkg.setClassCount(4);
        List<ClassSummary> serviceClasses = new ArrayList<>();
        ClassSummary serviceClass = new ClassSummary();
        serviceClass.setClassName("UserService");
        serviceClasses.add(serviceClass);
        servicePkg.setClasses(serviceClasses);
        packages.add(servicePkg);

        PackageSummary repoPkg = new PackageSummary();
        repoPkg.setPackageName("com.example.repository");
        repoPkg.setClassCount(3);
        List<ClassSummary> repoClasses = new ArrayList<>();
        ClassSummary repoClass = new ClassSummary();
        repoClass.setClassName("UserRepository");
        repoClasses.add(repoClass);
        repoPkg.setClasses(repoClasses);
        packages.add(repoPkg);

        summary.setPackages(packages);
        return summary;
    }

    private List<PackageSummary> createStandardPackages() {
        List<PackageSummary> packages = new ArrayList<>();

        PackageSummary controllerPkg = new PackageSummary();
        controllerPkg.setPackageName("com.example.controller");
        controllerPkg.setClassCount(3);
        List<ClassSummary> controllerClasses = new ArrayList<>();
        ClassSummary controllerClass = new ClassSummary();
        controllerClass.setClassName("UserController");
        controllerClasses.add(controllerClass);
        controllerPkg.setClasses(controllerClasses);
        packages.add(controllerPkg);

        PackageSummary servicePkg = new PackageSummary();
        servicePkg.setPackageName("com.example.service");
        servicePkg.setClassCount(4);
        List<ClassSummary> serviceClasses = new ArrayList<>();
        ClassSummary serviceClass = new ClassSummary();
        serviceClass.setClassName("UserService");
        serviceClasses.add(serviceClass);
        servicePkg.setClasses(serviceClasses);
        packages.add(servicePkg);

        PackageSummary repoPkg = new PackageSummary();
        repoPkg.setPackageName("com.example.repository");
        repoPkg.setClassCount(3);
        List<ClassSummary> repoClasses = new ArrayList<>();
        ClassSummary repoClass = new ClassSummary();
        repoClass.setClassName("UserRepository");
        repoClasses.add(repoClass);
        repoPkg.setClasses(repoClasses);
        packages.add(repoPkg);

        PackageSummary entityPkg = new PackageSummary();
        entityPkg.setPackageName("com.example.entity");
        entityPkg.setClassCount(5);
        List<ClassSummary> entityClasses = new ArrayList<>();
        ClassSummary entityClass = new ClassSummary();
        entityClass.setClassName("UserEntity");
        entityClasses.add(entityClass);
        entityPkg.setClasses(entityClasses);
        packages.add(entityPkg);

        PackageSummary dtoPkg = new PackageSummary();
        dtoPkg.setPackageName("com.example.dto");
        dtoPkg.setClassCount(3);
        List<ClassSummary> dtoClasses = new ArrayList<>();
        ClassSummary dtoClass = new ClassSummary();
        dtoClass.setClassName("UserRequest");
        dtoClasses.add(dtoClass);
        dtoPkg.setClasses(dtoClasses);
        packages.add(dtoPkg);

        PackageSummary configPkg = new PackageSummary();
        configPkg.setPackageName("com.example.config");
        configPkg.setClassCount(2);
        List<ClassSummary> configClasses = new ArrayList<>();
        ClassSummary configClass = new ClassSummary();
        configClass.setClassName("AppConfig");
        configClasses.add(configClass);
        configPkg.setClasses(configClasses);
        packages.add(configPkg);

        return packages;
    }
}