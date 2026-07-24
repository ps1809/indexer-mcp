package com.projectiq.mcp.client;

import com.projectiq.mcp.client.dto.ClassInfo;
import com.projectiq.mcp.client.dto.ClassRequest;
import com.projectiq.mcp.client.dto.ClassResponse;
import com.projectiq.mcp.client.dto.ClassType;
import com.projectiq.mcp.client.dto.IndexerHealthResponse;
import com.projectiq.mcp.client.dto.RepositoryStatsRequest;
import com.projectiq.mcp.client.dto.RepositoryStatsResponse;
import com.projectiq.mcp.client.dto.RepositorySummaryRequest;
import com.projectiq.mcp.client.dto.RepositorySummaryResponse;
import com.projectiq.mcp.client.exception.IndexerClientException;
import com.projectiq.mcp.client.exception.IndexerHttpException;
import com.projectiq.mcp.client.exception.IndexerTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class IndexerRestClientImplTest {

    private MockRestServiceServer mockServer;
    private IndexerRestClientImpl indexerRestClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://localhost:8081");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        indexerRestClient = new IndexerRestClientImpl(builder.build());
    }

    @Test
    void checkHealth_shouldReturnHealthResponse() {
        String responseBody = """
                {
                    "status": "UP",
                    "version": "1.0.0"
                }
                """;

        mockServer.expect(requestTo("http://localhost:8081/actuator/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        IndexerHealthResponse response = indexerRestClient.checkHealth();

        assertNotNull(response);
        assertEquals("UP", response.getStatus());
        assertEquals("1.0.0", response.getVersion());
        assertTrue(response.isHealthy());
        mockServer.verify();
    }

    @Test
    void checkHealth_shouldThrowIndexerHttpExceptionOn4xx() {
        mockServer.expect(requestTo("http://localhost:8081/actuator/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withBadRequest().body("Bad Request").contentType(MediaType.TEXT_PLAIN));

        IndexerHttpException exception = assertThrows(IndexerHttpException.class,
                () -> indexerRestClient.checkHealth());

        assertEquals(400, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("400"));
        mockServer.verify();
    }

    @Test
    void checkHealth_shouldThrowIndexerHttpExceptionOn5xx() {
        mockServer.expect(requestTo("http://localhost:8081/actuator/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError().body("Internal Server Error").contentType(MediaType.TEXT_PLAIN));

        IndexerHttpException exception = assertThrows(IndexerHttpException.class,
                () -> indexerRestClient.checkHealth());

        assertEquals(500, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("500"));
        mockServer.verify();
    }

    @Test
    void checkHealth_shouldThrowIndexerHttpExceptionOn404() {
        mockServer.expect(requestTo("http://localhost:8081/actuator/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withResourceNotFound().body("Not Found").contentType(MediaType.TEXT_PLAIN));

        IndexerHttpException exception = assertThrows(IndexerHttpException.class,
                () -> indexerRestClient.checkHealth());

        assertEquals(404, exception.getStatusCode());
        mockServer.verify();
    }

    @Test
    void getRepositorySummary_shouldThrowOnNullResponse() {
        mockServer.expect(requestTo("http://localhost:8081/api/v1/indexer/test-repo/summary"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withResourceNotFound().body("Not Found").contentType(MediaType.TEXT_PLAIN));

        RepositorySummaryRequest request = new RepositorySummaryRequest("test-repo", null);
        
        IndexerHttpException exception = assertThrows(IndexerHttpException.class,
                () -> indexerRestClient.getRepositorySummary(request));

        assertEquals(404, exception.getStatusCode());
        mockServer.verify();
    }

    @Test
    void getRepositorySummary_shouldThrowOnDeserializationError() {
        String invalidJson = "not valid json {{{";

        mockServer.expect(requestTo("http://localhost:8081/api/v1/indexer/test-repo/summary"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(invalidJson, MediaType.APPLICATION_JSON));

        RepositorySummaryRequest request = new RepositorySummaryRequest("test-repo", null);
        
        IndexerClientException exception = assertThrows(IndexerClientException.class,
                () -> indexerRestClient.getRepositorySummary(request));

        assertTrue(exception.getMessage().contains("deserialize"));
        mockServer.verify();
    }

    @Test
    void getRepositoryStatistics_shouldReturnStatsResponse() {
        String responseBody = """
                {
                    "repositoryName": "test-repo",
                    "branch": "main",
                    "status": "INDEXED",
                    "commitCount": 500,
                    "packageCount": 50,
                    "classCount": 200,
                    "methodCount": 1000,
                    "fileCount": 300,
                    "totalLinesOfCode": 25000,
                    "lastIndexedDate": "2024-01-15T10:30:00",
                    "contributors": [
                        {
                            "author": "John Doe",
                            "commitCount": 200,
                            "lastActiveDate": "2024-01-14"
                        },
                        {
                            "author": "Jane Smith",
                            "commitCount": 150,
                            "lastActiveDate": "2024-01-13"
                        }
                    ],
                    "fileTypeStats": [
                        {
                            "fileType": "java",
                            "fileCount": 200,
                            "totalLinesOfCode": 20000
                        },
                        {
                            "fileType": "xml",
                            "fileCount": 50,
                            "totalLinesOfCode": 3000
                        }
                    ]
                }
                """;

        mockServer.expect(requestTo("http://localhost:8081/api/v1/indexer/test-repo/stats?branch=main"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        RepositoryStatsRequest request = new RepositoryStatsRequest("test-repo", "main");
        RepositoryStatsResponse response = indexerRestClient.getRepositoryStatistics(request);

        assertNotNull(response);
        assertEquals("test-repo", response.getRepositoryName());
        assertEquals("main", response.getBranch());
        assertEquals("INDEXED", response.getStatus());
        assertEquals(500L, response.getCommitCount());
        assertEquals(50L, response.getPackageCount());
        assertEquals(200L, response.getClassCount());
        assertEquals(1000L, response.getMethodCount());
        assertEquals(300L, response.getFileCount());
        assertEquals(25000L, response.getTotalLinesOfCode());
        assertEquals("2024-01-15T10:30:00", response.getLastIndexedDate());
        assertTrue(response.isIndexed());
        assertEquals(2, response.getContributors().size());
        assertEquals("John Doe", response.getContributors().get(0).getAuthor());
        assertEquals(200L, response.getContributors().get(0).getCommitCount());
        assertEquals("java", response.getFileTypeStats().get(0).getFileType());
        assertEquals(200L, response.getFileTypeStats().get(0).getFileCount());
        mockServer.verify();
    }

    @Test
    void getRepositoryStatistics_shouldThrowOnHttpError() {
        mockServer.expect(requestTo("http://localhost:8081/api/v1/indexer/test-repo/stats"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError().body("Internal Server Error").contentType(MediaType.TEXT_PLAIN));

        RepositoryStatsRequest request = new RepositoryStatsRequest("test-repo", null);
        
        IndexerHttpException exception = assertThrows(IndexerHttpException.class,
                () -> indexerRestClient.getRepositoryStatistics(request));

        assertEquals(500, exception.getStatusCode());
        mockServer.verify();
    }

    @Test
    void getRepositoryStatistics_shouldThrowOnDeserializationError() {
        String invalidJson = "not valid json {{{";

        mockServer.expect(requestTo("http://localhost:8081/api/v1/indexer/test-repo/stats"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(invalidJson, MediaType.APPLICATION_JSON));

        RepositoryStatsRequest request = new RepositoryStatsRequest("test-repo", null);
        
        IndexerClientException exception = assertThrows(IndexerClientException.class,
                () -> indexerRestClient.getRepositoryStatistics(request));

        assertTrue(exception.getMessage().contains("deserialize"));
        mockServer.verify();
    }

    @Test
    void findClass_shouldReturnClassResponse() {
        String responseBody = """
                {
                    "repositoryName": "spring-framework",
                    "totalResults": 2,
                    "classes": [
                        {
                            "packageName": "org.springframework.web",
                            "className": "RestController",
                            "fullyQualifiedName": "org.springframework.web.RestController",
                            "classType": "ANNOTATION",
                            "visibility": "public",
                            "parentClass": null,
                            "implementedInterfaces": [],
                            "annotations": ["@Documented", "@Retention", "@Target"],
                            "sourceFileLocation": "spring-web/org/springframework/web/RestController.java"
                        },
                        {
                            "packageName": "com.example.controller",
                            "className": "RestController",
                            "fullyQualifiedName": "com.example.controller.RestController",
                            "classType": "CLASS",
                            "visibility": "public",
                            "parentClass": "ControllerSupport",
                            "implementedInterfaces": ["InitializingBean"],
                            "annotations": ["@RestController", "@RequestMapping"],
                            "sourceFileLocation": "src/main/java/com/example/controller/RestController.java"
                        }
                    ]
                }
                """;

        mockServer.expect(requestTo("http://localhost:8081/api/v1/indexer/spring-framework/class?q=RestController"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        ClassRequest request = new ClassRequest();
        request.setRepositoryName("spring-framework");
        request.setClassName("RestController");

        ClassResponse response = indexerRestClient.findClass(request);

        assertNotNull(response);
        assertEquals("spring-framework", response.getRepositoryName());
        assertEquals(2, response.getTotalResults());
        assertEquals(2, response.getClasses().size());

        ClassInfo classInfo = response.getClasses().get(0);
        assertEquals("org.springframework.web", classInfo.getPackageName());
        assertEquals("RestController", classInfo.getClassName());
        assertEquals("org.springframework.web.RestController", classInfo.getFullyQualifiedName());
        assertEquals(ClassType.ANNOTATION, classInfo.getClassType());
        assertEquals("public", classInfo.getVisibility());
        assertNull(classInfo.getParentClass());
        assertTrue(classInfo.getImplementedInterfaces().isEmpty());
        assertEquals(3, classInfo.getAnnotations().size());
        assertEquals("spring-web/org/springframework/web/RestController.java", classInfo.getSourceFileLocation());

        ClassInfo classInfo2 = response.getClasses().get(1);
        assertEquals("com.example.controller", classInfo2.getPackageName());
        assertEquals("RestController", classInfo2.getClassName());
        assertEquals("CLASS", classInfo2.getClassType().name());
        assertEquals("ControllerSupport", classInfo2.getParentClass());
        assertTrue(classInfo2.getImplementedInterfaces().contains("InitializingBean"));

        mockServer.verify();
    }

    @Test
    void findClass_shouldThrowOnHttpError() {
        mockServer.expect(requestTo("http://localhost:8081/api/v1/indexer/test-repo/class?q=Test"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError().body("Internal Server Error").contentType(MediaType.TEXT_PLAIN));

        ClassRequest request = new ClassRequest();
        request.setRepositoryName("test-repo");
        request.setClassName("Test");

        IndexerHttpException exception = assertThrows(IndexerHttpException.class,
                () -> indexerRestClient.findClass(request));

        assertEquals(500, exception.getStatusCode());
        mockServer.verify();
    }

    @Test
    void findClass_shouldThrowOnDeserializationError() {
        String invalidJson = "not valid json {{{";

        mockServer.expect(requestTo("http://localhost:8081/api/v1/indexer/test-repo/class?q=Test"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(invalidJson, MediaType.APPLICATION_JSON));

        ClassRequest request = new ClassRequest();
        request.setRepositoryName("test-repo");
        request.setClassName("Test");

        IndexerClientException exception = assertThrows(IndexerClientException.class,
                () -> indexerRestClient.findClass(request));

        assertTrue(exception.getMessage().contains("deserialize"));
        mockServer.verify();
    }

    @Test
    void findClass_shouldThrowOnNotFound() {
        mockServer.expect(requestTo("http://localhost:8081/api/v1/indexer/nonexistent/class?q=Test"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withResourceNotFound().body("Not Found").contentType(MediaType.TEXT_PLAIN));

        ClassRequest request = new ClassRequest();
        request.setRepositoryName("nonexistent");
        request.setClassName("Test");

        IndexerHttpException exception = assertThrows(IndexerHttpException.class,
                () -> indexerRestClient.findClass(request));

        assertEquals(404, exception.getStatusCode());
        mockServer.verify();
    }

    @Test
    void findClass_shouldHandleEmptyResults() {
        String responseBody = """
                {
                    "repositoryName": "empty-repo",
                    "totalResults": 0,
                    "classes": []
                }
                """;

        mockServer.expect(requestTo("http://localhost:8081/api/v1/indexer/empty-repo/class?q=NonExistent"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        ClassRequest request = new ClassRequest();
        request.setRepositoryName("empty-repo");
        request.setClassName("NonExistent");

        ClassResponse response = indexerRestClient.findClass(request);

        assertNotNull(response);
        assertEquals("empty-repo", response.getRepositoryName());
        assertEquals(0, response.getTotalResults());
        assertTrue(response.getClasses().isEmpty());
        mockServer.verify();
    }

    @Test
    void findClass_shouldHandleAllClassTypes() {
        String responseBody = """
                {
                    "repositoryName": "test-repo",
                    "totalResults": 5,
                    "classes": [
                        {
                            "packageName": "com.example",
                            "className": "MyClass",
                            "fullyQualifiedName": "com.example.MyClass",
                            "classType": "CLASS",
                            "visibility": "public",
                            "parentClass": "Object",
                            "implementedInterfaces": [],
                            "annotations": [],
                            "sourceFileLocation": "src/main/java/com/example/MyClass.java"
                        },
                        {
                            "packageName": "com.example",
                            "className": "MyInterface",
                            "fullyQualifiedName": "com.example.MyInterface",
                            "classType": "INTERFACE",
                            "visibility": "public",
                            "parentClass": null,
                            "implementedInterfaces": [],
                            "annotations": [],
                            "sourceFileLocation": "src/main/java/com/example/MyInterface.java"
                        },
                        {
                            "packageName": "com.example",
                            "className": "MyEnum",
                            "fullyQualifiedName": "com.example.MyEnum",
                            "classType": "ENUM",
                            "visibility": "public",
                            "parentClass": "Enum",
                            "implementedInterfaces": ["Serializable"],
                            "annotations": [],
                            "sourceFileLocation": "src/main/java/com/example/MyEnum.java"
                        },
                        {
                            "packageName": "com.example",
                            "className": "MyRecord",
                            "fullyQualifiedName": "com.example.MyRecord",
                            "classType": "RECORD",
                            "visibility": "public",
                            "parentClass": "Object",
                            "implementedInterfaces": [],
                            "annotations": [],
                            "sourceFileLocation": "src/main/java/com/example/MyRecord.java"
                        },
                        {
                            "packageName": "com.example",
                            "className": "MyAnnotation",
                            "fullyQualifiedName": "com.example.MyAnnotation",
                            "classType": "ANNOTATION",
                            "visibility": "public",
                            "parentClass": null,
                            "implementedInterfaces": [],
                            "annotations": [],
                            "sourceFileLocation": "src/main/java/com/example/MyAnnotation.java"
                        }
                    ]
                }
                """;

        mockServer.expect(requestTo("http://localhost:8081/api/v1/indexer/test-repo/class?q=My&types=CLASS,INTERFACE,ENUM,RECORD,ANNOTATION"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        ClassRequest request = new ClassRequest();
        request.setRepositoryName("test-repo");
        request.setClassName("My");
        request.setClassTypes(Arrays.asList("CLASS", "INTERFACE", "ENUM", "RECORD", "ANNOTATION"));

        ClassResponse response = indexerRestClient.findClass(request);

        assertNotNull(response);
        assertEquals(5, response.getTotalResults());
        assertEquals(ClassType.CLASS, response.getClasses().get(0).getClassType());
        assertEquals(ClassType.INTERFACE, response.getClasses().get(1).getClassType());
        assertEquals(ClassType.ENUM, response.getClasses().get(2).getClassType());
        assertEquals(ClassType.RECORD, response.getClasses().get(3).getClassType());
        assertEquals(ClassType.ANNOTATION, response.getClasses().get(4).getClassType());
        mockServer.verify();
    }
}
