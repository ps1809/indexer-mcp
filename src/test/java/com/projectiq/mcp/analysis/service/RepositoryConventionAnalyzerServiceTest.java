package com.projectiq.mcp.analysis.service;

import com.projectiq.mcp.analysis.dto.RepositoryConventionResponse;
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
class RepositoryConventionAnalyzerServiceTest {

    @Mock
    private IndexerRestClient indexerRestClient;

    private RepositoryConventionAnalyzerService service;

    @BeforeEach
    void setUp() {
        service = new RepositoryConventionAnalyzerService(indexerRestClient);
    }

    @Test
    void analyzeConventions_withStandardSpringBootProject_detectsAllConventions() {
        // Arrange
        RepositorySummaryResponse summary = createStandardSpringBootSummary();
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(summary);

        // Act
        RepositoryConventionResponse response = service.analyzeConventions("my-app", "main");

        // Assert
        assertEquals("my-app", response.getRepositoryName());
        assertEquals("main", response.getBranch());
        assertNotNull(response.getRepositoryOverview());
        assertNotNull(response.getNamingConventions());
        assertNotNull(response.getPackageConventions());
        assertNotNull(response.getArchitecturalConventions());
        assertNotNull(response.getAnnotationConventions());
        assertNotNull(response.getRestApiConventions());
        assertNotNull(response.getTestingConventions());
        assertNotNull(response.getProjectSpecificObservations());
        assertNotNull(response.getConfidenceLevel());

        // Verify naming conventions
        assertEquals("PascalCase (consistent)", response.getNamingConventions().getClassNamingConvention());
        assertEquals("{Name}Service", response.getNamingConventions().getServiceNamingPattern());
        assertEquals("{Name}Repository", response.getNamingConventions().getRepositoryNamingPattern());
        assertEquals("{Name}Controller", response.getNamingConventions().getControllerNamingPattern());

        // Verify package conventions
        assertNotNull(response.getPackageConventions().getModuleOrganization());
        assertNotNull(response.getPackageConventions().getPackageNamingStyle());

        // Verify architectural conventions
        assertNotNull(response.getArchitecturalConventions().getArchitecturalStyle());
        assertFalse(response.getArchitecturalConventions().getDetectedLayers().isEmpty());

        // Verify annotations
        assertFalse(response.getAnnotationConventions().getCommonAnnotations().isEmpty());

        // Verify project-specific observations
        assertFalse(response.getProjectSpecificObservations().isEmpty());
    }

    @Test
    void analyzeConventions_withMultiModuleRepository_detectsMultiModule() {
        // Arrange
        RepositorySummaryResponse summary = createMultiModuleSummary();
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(summary);

        // Act
        RepositoryConventionResponse response = service.analyzeConventions("multi-module-app", "develop");

        // Assert
        assertEquals("multi-module-app", response.getRepositoryName());
        assertEquals("develop", response.getBranch());
        assertNotNull(response.getPackageConventions().getModuleOrganization());
        assertFalse(response.getPackageConventions().getDetectedPackages().isEmpty());
        assertEquals(4, response.getPackageConventions().getDetectedPackages().size());
    }

    @Test
    void analyzeConventions_withMixedNamingConventions_detectsMixedNames() {
        // Arrange
        RepositorySummaryResponse summary = createMixedNamingSummary();
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(summary);

        // Act
        RepositoryConventionResponse response = service.analyzeConventions("mixed-app", "main");

        // Assert
        assertTrue(response.getNamingConventions().getClassNamingConvention().contains("PascalCase"));
        assertTrue(response.getNamingConventions().getMethodNamingConvention().contains("camelCase"));
    }

    @Test
    void analyzeConventions_withEmptyRepository_returnsLowConfidence() {
        // Arrange
        RepositorySummaryResponse summary = new RepositorySummaryResponse();
        summary.setRepositoryName("empty-repo");
        summary.setBranch("main");
        summary.setStatus("INDEXED");
        summary.setPackageCount(0);
        summary.setClassCount(0);
        summary.setMethodCount(0);
        summary.setFileCount(0);
        summary.setCommitCount(0);
        summary.setPackages(new ArrayList<>());

        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(summary);

        // Act
        RepositoryConventionResponse response = service.analyzeConventions("empty-repo", "main");

        // Assert
        assertEquals("LOW", response.getConfidenceLevel());
        assertNotNull(response.getNamingConventions());
        assertNotNull(response.getPackageConventions());
        assertNotNull(response.getArchitecturalConventions());
    }

    @Test
    void analyzeConventions_withNullSummary_returnsLowConfidence() {
        // Arrange
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(null);

        // Act
        RepositoryConventionResponse response = service.analyzeConventions("null-repo", "main");

        // Assert
        assertEquals("LOW", response.getConfidenceLevel());
        assertNotNull(response.getRepositoryOverview());
        assertTrue(response.getRepositoryOverview().contains("not available"));
    }

    @Test
    void analyzeConventions_withMissingMetadata_stillReturnsResponse() {
        // Arrange
        RepositorySummaryResponse summary = new RepositorySummaryResponse();
        summary.setRepositoryName("minimal-repo");
        summary.setBranch("main");
        summary.setStatus("INDEXED");
        summary.setPackageCount(1);
        summary.setClassCount(5);
        summary.setMethodCount(10);
        summary.setFileCount(5);
        summary.setCommitCount(0);
        summary.setLastIndexedDate(null);

        List<PackageSummary> packages = new ArrayList<>();
        PackageSummary pkg = new PackageSummary();
        pkg.setPackageName("com.example");
        pkg.setClassCount(5);
        pkg.setMethodCount(10);
        List<ClassSummary> classes = new ArrayList<>();
        ClassSummary cls1 = new ClassSummary();
        cls1.setClassName("MyService");
        classes.add(cls1);
        ClassSummary cls2 = new ClassSummary();
        cls2.setClassName("MyRepository");
        classes.add(cls2);
        pkg.setClasses(classes);
        packages.add(pkg);
        summary.setPackages(packages);

        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(summary);

        // Act
        RepositoryConventionResponse response = service.analyzeConventions("minimal-repo", "main");

        // Assert
        assertNotNull(response);
        assertEquals("MEDIUM", response.getConfidenceLevel());
        assertNotNull(response.getNamingConventions());
    }

    @Test
    void analyzeConventions_withClientException_returnsLowConfidence() {
        // Arrange
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenThrow(new RuntimeException("Connection failed"));

        // Act
        RepositoryConventionResponse response = service.analyzeConventions("failing-repo", "main");

        // Assert
        assertEquals("LOW", response.getConfidenceLevel());
        assertNotNull(response.getRepositoryOverview());
    }

    @Test
    void analyzeConventions_defaultBranchUsedWhenBranchNull() {
        // Arrange
        RepositorySummaryResponse summary = createStandardSpringBootSummary();
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(summary);

        // Act
        RepositoryConventionResponse response = service.analyzeConventions("my-app", null);

        // Assert
        assertEquals("main", response.getBranch());
    }

    @Test
    void analyzeConventions_deterministicOutput() {
        // Arrange
        RepositorySummaryResponse summary = createStandardSpringBootSummary();
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(summary);

        // Act
        RepositoryConventionResponse response1 = service.analyzeConventions("my-app", "main");
        RepositoryConventionResponse response2 = service.analyzeConventions("my-app", "main");

        // Assert - both calls should produce identical results
        assertEquals(response1.getNamingConventions().getClassNamingConvention(),
                response2.getNamingConventions().getClassNamingConvention());
        assertEquals(response1.getNamingConventions().getServiceNamingPattern(),
                response2.getNamingConventions().getServiceNamingPattern());
        assertEquals(response1.getArchitecturalConventions().getArchitecturalStyle(),
                response2.getArchitecturalConventions().getArchitecturalStyle());
        assertEquals(response1.getPackageConventions().getModuleOrganization(),
                response2.getPackageConventions().getModuleOrganization());
    }

    @Test
    void buildRepositoryOverview_withValidSummary_returnsFormattedOverview() {
        // Arrange
        RepositorySummaryResponse summary = new RepositorySummaryResponse();
        summary.setRepositoryName("test-repo");
        summary.setBranch("main");
        summary.setStatus("INDEXED");
        summary.setPackageCount(5);
        summary.setClassCount(20);
        summary.setMethodCount(100);
        summary.setFileCount(30);
        summary.setCommitCount(50);
        summary.setLastIndexedDate("2024-01-15T10:30:00Z");

        // Act
        String overview = service.buildRepositoryOverview(summary);

        // Assert
        assertTrue(overview.contains("test-repo"));
        assertTrue(overview.contains("main"));
        assertTrue(overview.contains("indexed"));
        assertTrue(overview.contains("5 packages"));
        assertTrue(overview.contains("20 classes"));
        assertTrue(overview.contains("50"));
    }

    @Test
    void buildRepositoryOverview_withNullSummary_returnsDefaultMessage() {
        // Act
        String overview = service.buildRepositoryOverview(null);

        // Assert
        assertEquals("No repository data available.", overview);
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
    void analyzeConventions_withTestClasses_detectsTestConvention() {
        // Arrange
        RepositorySummaryResponse summary = new RepositorySummaryResponse();
        summary.setRepositoryName("tested-app");
        summary.setBranch("main");
        summary.setStatus("INDEXED");
        summary.setPackageCount(2);
        summary.setClassCount(4);
        summary.setMethodCount(20);
        summary.setFileCount(6);
        summary.setCommitCount(10);
        summary.setLastIndexedDate("2024-04-01T12:00:00Z");

        List<PackageSummary> packages = new ArrayList<>();

        PackageSummary srcPkg = new PackageSummary();
        srcPkg.setPackageName("com.example.service");
        srcPkg.setClassCount(2);
        List<ClassSummary> srcClasses = new ArrayList<>();
        ClassSummary svcClass = new ClassSummary();
        svcClass.setClassName("UserService");
        srcClasses.add(svcClass);
        srcPkg.setClasses(srcClasses);
        packages.add(srcPkg);

        PackageSummary testPkg = new PackageSummary();
        testPkg.setPackageName("com.example.test");
        testPkg.setClassCount(2);
        List<ClassSummary> testClasses = new ArrayList<>();
        ClassSummary testClass = new ClassSummary();
        testClass.setClassName("UserServiceTest");
        testClass.setSuperClass("Object");
        testClasses.add(testClass);
        ClassSummary itClass = new ClassSummary();
        itClass.setClassName("UserServiceIT");
        testClasses.add(itClass);
        testPkg.setClasses(testClasses);
        packages.add(testPkg);

        summary.setPackages(packages);

        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(summary);

        // Act
        RepositoryConventionResponse response = service.analyzeConventions("tested-app", "main");

        // Assert
        assertNotNull(response.getTestingConventions());
        String testNaming = response.getTestingConventions().getTestNamingStyle();
        assertNotNull(testNaming);
        assertTrue(testNaming.contains("Test") || testNaming.contains("IT"));

        String testFramework = response.getTestingConventions().getTestFramework();
        assertNotNull(testFramework);
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

    private RepositorySummaryResponse createMixedNamingSummary() {
        RepositorySummaryResponse summary = new RepositorySummaryResponse();
        summary.setRepositoryName("mixed-app");
        summary.setBranch("main");
        summary.setStatus("INDEXED");
        summary.setPackageCount(2);
        summary.setClassCount(6);
        summary.setMethodCount(30);
        summary.setFileCount(10);
        summary.setCommitCount(20);
        summary.setLastIndexedDate("2024-03-01T12:00:00Z");

        List<PackageSummary> packages = new ArrayList<>();

        PackageSummary pkg1 = new PackageSummary();
        pkg1.setPackageName("com.example.controller");
        pkg1.setClassCount(3);
        List<ClassSummary> classes1 = new ArrayList<>();
        ClassSummary c1 = new ClassSummary();
        c1.setClassName("UserController");
        classes1.add(c1);
        ClassSummary c2 = new ClassSummary();
        c2.setClassName("OrderController");
        classes1.add(c2);
        ClassSummary c3 = new ClassSummary();
        c3.setClassName("ProductDto");
        classes1.add(c3);
        pkg1.setClasses(classes1);
        packages.add(pkg1);

        PackageSummary pkg2 = new PackageSummary();
        pkg2.setPackageName("com.example.config");
        pkg2.setClassCount(3);
        List<ClassSummary> classes2 = new ArrayList<>();
        ClassSummary c4 = new ClassSummary();
        c4.setClassName("AppConfig");
        classes2.add(c4);
        ClassSummary c5 = new ClassSummary();
        c5.setClassName("SecurityConfig");
        classes2.add(c5);
        ClassSummary c6 = new ClassSummary();
        c6.setClassName("DatabaseConfig");
        classes2.add(c6);
        pkg2.setClasses(classes2);
        packages.add(pkg2);

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