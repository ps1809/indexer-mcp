package com.projectiq.mcp.knowledgegraph.service;

import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.dto.ClassInfo;
import com.projectiq.mcp.client.dto.ClassRequest;
import com.projectiq.mcp.client.dto.ClassResponse;
import com.projectiq.mcp.client.dto.ClassType;
import com.projectiq.mcp.client.dto.DependencyInfo;
import com.projectiq.mcp.client.dto.DependencyRequest;
import com.projectiq.mcp.client.dto.DependencyResponse;
import com.projectiq.mcp.client.dto.DependencyType;
import com.projectiq.mcp.client.dto.MethodInfo;
import com.projectiq.mcp.client.dto.MethodRequest;
import com.projectiq.mcp.client.dto.MethodResponse;
import com.projectiq.mcp.client.dto.PackageSummary;
import com.projectiq.mcp.client.dto.RepositorySummaryRequest;
import com.projectiq.mcp.client.dto.RepositorySummaryResponse;
import com.projectiq.mcp.client.dto.RestApiRequest;
import com.projectiq.mcp.client.dto.RestApiResponse;
import com.projectiq.mcp.client.dto.RestEndpointInfo;
import com.projectiq.mcp.client.dto.SpringComponentInfo;
import com.projectiq.mcp.client.dto.SpringComponentRequest;
import com.projectiq.mcp.client.dto.SpringComponentResponse;
import com.projectiq.mcp.knowledgegraph.dto.KnowledgeGraph;
import com.projectiq.mcp.knowledgegraph.dto.KnowledgeGraphReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryKnowledgeGraphServiceTest {

    @Mock
    private IndexerRestClient indexerRestClient;

    private RepositoryKnowledgeGraphService service;

    @BeforeEach
    void setUp() {
        service = new RepositoryKnowledgeGraphService(indexerRestClient);
    }

    @Test
    void buildKnowledgeGraph_withValidRepository_returnsGraph() {
        // Arrange
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(createSampleSummary());
        when(indexerRestClient.findClass(any(ClassRequest.class)))
                .thenReturn(createSampleClassResponse());
        when(indexerRestClient.findMethod(any(MethodRequest.class)))
                .thenReturn(createSampleMethodResponse());
        when(indexerRestClient.findSpringComponent(any(SpringComponentRequest.class)))
                .thenReturn(createSampleSpringComponentResponse());
        when(indexerRestClient.findRestApi(any(RestApiRequest.class)))
                .thenReturn(createSampleRestApiResponse());
        when(indexerRestClient.findDependency(any(DependencyRequest.class)))
                .thenReturn(createSampleDependencyResponse());

        // Act
        KnowledgeGraph graph = service.buildKnowledgeGraph("test-repo", "main");

        // Assert
        assertNotNull(graph);
        assertEquals("test-repo", graph.getRepositoryName());
        assertEquals("main", graph.getBranch());
        assertTrue(graph.getNodeCount() > 0);
        assertTrue(graph.getEdgeCount() > 0);
    }

    @Test
    void buildKnowledgeGraph_withNullBranch_defaultsToMain() {
        // Arrange
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(createSampleSummary());
        when(indexerRestClient.findClass(any(ClassRequest.class)))
                .thenReturn(createSampleClassResponse());
        when(indexerRestClient.findMethod(any(MethodRequest.class)))
                .thenReturn(createSampleMethodResponse());
        when(indexerRestClient.findSpringComponent(any(SpringComponentRequest.class)))
                .thenReturn(createSampleSpringComponentResponse());
        when(indexerRestClient.findRestApi(any(RestApiRequest.class)))
                .thenReturn(createSampleRestApiResponse());
        when(indexerRestClient.findDependency(any(DependencyRequest.class)))
                .thenReturn(createSampleDependencyResponse());

        // Act
        KnowledgeGraph graph = service.buildKnowledgeGraph("test-repo", null);

        // Assert
        assertEquals("main", graph.getBranch());
    }

    @Test
    void buildKnowledgeGraph_withEmptyRepository_returnsGraphWithRepoNode() {
        // Arrange
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(null);
        when(indexerRestClient.findClass(any(ClassRequest.class)))
                .thenReturn(null);
        when(indexerRestClient.findMethod(any(MethodRequest.class)))
                .thenReturn(null);
        when(indexerRestClient.findSpringComponent(any(SpringComponentRequest.class)))
                .thenReturn(null);
        when(indexerRestClient.findRestApi(any(RestApiRequest.class)))
                .thenReturn(null);
        when(indexerRestClient.findDependency(any(DependencyRequest.class)))
                .thenReturn(null);

        // Act
        KnowledgeGraph graph = service.buildKnowledgeGraph("empty-repo", "main");

        // Assert
        assertNotNull(graph);
        assertEquals(1, graph.getNodeCount()); // Just the repository node
        assertEquals(0, graph.getEdgeCount());
    }

    @Test
    void generateKnowledgeGraphReport_withValidRepository_returnsReport() {
        // Arrange
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(createSampleSummary());
        when(indexerRestClient.findClass(any(ClassRequest.class)))
                .thenReturn(createSampleClassResponse());
        when(indexerRestClient.findMethod(any(MethodRequest.class)))
                .thenReturn(createSampleMethodResponse());
        when(indexerRestClient.findSpringComponent(any(SpringComponentRequest.class)))
                .thenReturn(createSampleSpringComponentResponse());
        when(indexerRestClient.findRestApi(any(RestApiRequest.class)))
                .thenReturn(createSampleRestApiResponse());
        when(indexerRestClient.findDependency(any(DependencyRequest.class)))
                .thenReturn(createSampleDependencyResponse());

        // Act
        KnowledgeGraphReport report = service.generateKnowledgeGraphReport("test-repo", "main");

        // Assert
        assertNotNull(report);
        assertEquals("test-repo", report.getRepositoryName());
        assertNotNull(report.getGraphStatistics());
        assertTrue(report.getGraphStatistics().getTotalNodes() > 0);
        assertTrue(report.getGraphStatistics().getTotalEdges() > 0);
        assertNotNull(report.getTraversalSummary());
    }

    @Test
    void traverseFromEntity_withExistingEntity_returnsSubgraph() {
        // Arrange
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(createSampleSummary());
        when(indexerRestClient.findClass(any(ClassRequest.class)))
                .thenReturn(createSampleClassResponse());
        when(indexerRestClient.findMethod(any(MethodRequest.class)))
                .thenReturn(createSampleMethodResponse());
        when(indexerRestClient.findSpringComponent(any(SpringComponentRequest.class)))
                .thenReturn(createSampleSpringComponentResponse());
        when(indexerRestClient.findRestApi(any(RestApiRequest.class)))
                .thenReturn(createSampleRestApiResponse());
        when(indexerRestClient.findDependency(any(DependencyRequest.class)))
                .thenReturn(createSampleDependencyResponse());

        // Act
        KnowledgeGraphReport report = service.traverseFromEntity("test-repo", "test-repo", "main");

        // Assert
        assertNotNull(report);
        assertEquals("test-repo", report.getRepositoryName());
        assertNotNull(report.getTraversalSummary());
        assertTrue(report.getTraversalSummary().contains("test-repo"));
    }

    @Test
    void traverseFromEntity_withUnknownEntity_returnsErrorReport() {
        // Arrange
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(createSampleSummary());
        when(indexerRestClient.findClass(any(ClassRequest.class)))
                .thenReturn(createSampleClassResponse());
        when(indexerRestClient.findMethod(any(MethodRequest.class)))
                .thenReturn(createSampleMethodResponse());
        when(indexerRestClient.findSpringComponent(any(SpringComponentRequest.class)))
                .thenReturn(createSampleSpringComponentResponse());
        when(indexerRestClient.findRestApi(any(RestApiRequest.class)))
                .thenReturn(createSampleRestApiResponse());
        when(indexerRestClient.findDependency(any(DependencyRequest.class)))
                .thenReturn(createSampleDependencyResponse());

        // Act
        KnowledgeGraphReport report = service.traverseFromEntity("test-repo", "NonExistentEntity12345", "main");

        // Assert
        assertNotNull(report);
        assertTrue(report.getTraversalSummary().contains("not found"));
        assertEquals(0, report.getGraphStatistics().getTotalNodes());
    }

    @Test
    void generateKnowledgeGraphReport_withEmptyRepository_returnsEmptyReport() {
        // Arrange
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(null);
        when(indexerRestClient.findClass(any(ClassRequest.class)))
                .thenReturn(null);
        when(indexerRestClient.findMethod(any(MethodRequest.class)))
                .thenReturn(null);
        when(indexerRestClient.findSpringComponent(any(SpringComponentRequest.class)))
                .thenReturn(null);
        when(indexerRestClient.findRestApi(any(RestApiRequest.class)))
                .thenReturn(null);
        when(indexerRestClient.findDependency(any(DependencyRequest.class)))
                .thenReturn(null);

        // Act
        KnowledgeGraphReport report = service.generateKnowledgeGraphReport("empty-repo", "main");

        // Assert
        assertNotNull(report);
        assertEquals(1, report.getGraphStatistics().getTotalNodes()); // Just repo node
        assertEquals(0, report.getGraphStatistics().getTotalEdges());
    }

    @Test
    void buildKnowledgeGraph_returnsDeterministicOutput() {
        // Arrange
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(createSampleSummary());
        when(indexerRestClient.findClass(any(ClassRequest.class)))
                .thenReturn(createSampleClassResponse());
        when(indexerRestClient.findMethod(any(MethodRequest.class)))
                .thenReturn(createSampleMethodResponse());
        when(indexerRestClient.findSpringComponent(any(SpringComponentRequest.class)))
                .thenReturn(createSampleSpringComponentResponse());
        when(indexerRestClient.findRestApi(any(RestApiRequest.class)))
                .thenReturn(createSampleRestApiResponse());
        when(indexerRestClient.findDependency(any(DependencyRequest.class)))
                .thenReturn(createSampleDependencyResponse());

        // Act
        KnowledgeGraph graph1 = service.buildKnowledgeGraph("test-repo", "main");
        KnowledgeGraph graph2 = service.buildKnowledgeGraph("test-repo", "main");

        // Assert
        assertEquals(graph1.getNodeCount(), graph2.getNodeCount());
        assertEquals(graph1.getEdgeCount(), graph2.getEdgeCount());
    }

    // --- Helper methods ---

    private RepositorySummaryResponse createSampleSummary() {
        RepositorySummaryResponse response = new RepositorySummaryResponse();
        response.setRepositoryName("test-repo");
        response.setBranch("main");
        response.setStatus("INDEXED");
        response.setPackageCount(2);
        response.setClassCount(3);
        response.setMethodCount(5);
        response.setFileCount(10);

        List<PackageSummary> packages = new ArrayList<>();
        PackageSummary pkg1 = new PackageSummary();
        pkg1.setPackageName("com.test.controller");
        List<com.projectiq.mcp.client.dto.ClassSummary> classes1 = new ArrayList<>();
        com.projectiq.mcp.client.dto.ClassSummary cls1 = new com.projectiq.mcp.client.dto.ClassSummary();
        cls1.setClassName("UserController");
        cls1.setFullyQualifiedName("com.test.controller.UserController");
        classes1.add(cls1);
        pkg1.setClasses(classes1);
        packages.add(pkg1);

        PackageSummary pkg2 = new PackageSummary();
        pkg2.setPackageName("com.test.service");
        List<com.projectiq.mcp.client.dto.ClassSummary> classes2 = new ArrayList<>();
        com.projectiq.mcp.client.dto.ClassSummary cls2 = new com.projectiq.mcp.client.dto.ClassSummary();
        cls2.setClassName("UserService");
        cls2.setFullyQualifiedName("com.test.service.UserService");
        classes2.add(cls2);
        pkg2.setClasses(classes2);
        packages.add(pkg2);

        response.setPackages(packages);
        return response;
    }

    private ClassResponse createSampleClassResponse() {
        ClassResponse response = new ClassResponse();
        response.setRepositoryName("test-repo");
        response.setTotalResults(2);
        List<ClassInfo> classes = new ArrayList<>();

        ClassInfo ci1 = new ClassInfo();
        ci1.setClassName("UserController");
        ci1.setPackageName("com.test.controller");
        ci1.setFullyQualifiedName("com.test.controller.UserController");
        ci1.setClassType(ClassType.CLASS);
        classes.add(ci1);

        ClassInfo ci2 = new ClassInfo();
        ci2.setClassName("UserService");
        ci2.setPackageName("com.test.service");
        ci2.setFullyQualifiedName("com.test.service.UserService");
        ci2.setClassType(ClassType.CLASS);
        classes.add(ci2);

        response.setClasses(classes);
        return response;
    }

    private MethodResponse createSampleMethodResponse() {
        MethodResponse response = new MethodResponse();
        response.setRepositoryName("test-repo");
        response.setTotalResults(2);
        List<MethodInfo> methods = new ArrayList<>();

        MethodInfo mi1 = new MethodInfo();
        mi1.setMethodName("getUser");
        mi1.setFullyQualifiedName("com.test.controller.UserController.getUser");
        mi1.setDeclaringClass("com.test.controller.UserController");
        mi1.setReturnType("User");
        methods.add(mi1);

        MethodInfo mi2 = new MethodInfo();
        mi2.setMethodName("createUser");
        mi2.setFullyQualifiedName("com.test.service.UserService.createUser");
        mi2.setDeclaringClass("com.test.service.UserService");
        mi2.setReturnType("User");
        methods.add(mi2);

        response.setMethods(methods);
        return response;
    }

    private SpringComponentResponse createSampleSpringComponentResponse() {
        SpringComponentResponse response = new SpringComponentResponse();
        response.setRepositoryName("test-repo");
        response.setTotalResults(1);
        List<SpringComponentInfo> components = new ArrayList<>();

        SpringComponentInfo comp = new SpringComponentInfo();
        comp.setName("userService");
        comp.setClassName("com.test.service.UserService");
        comp.setComponentType("@Service");
        components.add(comp);

        response.setComponents(components);
        return response;
    }

    private RestApiResponse createSampleRestApiResponse() {
        RestApiResponse response = new RestApiResponse();
        response.setRepositoryName("test-repo");
        response.setTotalResults(1);
        List<RestEndpointInfo> endpoints = new ArrayList<>();

        RestEndpointInfo endpoint = new RestEndpointInfo();
        endpoint.setEndpointPath("/api/users");
        endpoint.setHttpMethod("GET");
        endpoint.setControllerName("com.test.controller.UserController");
        endpoints.add(endpoint);

        response.setEndpoints(endpoints);
        return response;
    }

    private DependencyResponse createSampleDependencyResponse() {
        DependencyResponse response = new DependencyResponse();
        response.setRepositoryName("test-repo");
        response.setTotalResults(1);
        List<DependencyInfo> dependencies = new ArrayList<>();

        DependencyInfo dep = new DependencyInfo();
        dep.setName("spring-boot-starter-web");
        dep.setGroupId("org.springframework.boot");
        dep.setArtifactId("spring-boot-starter-web");
        dep.setVersion("3.2.0");
        dep.setType(DependencyType.MAVEN);
        dep.setScope("compile");
        dependencies.add(dep);

        response.setDependencies(dependencies);
        return response;
    }
}