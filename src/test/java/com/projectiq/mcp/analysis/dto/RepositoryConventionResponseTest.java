package com.projectiq.mcp.analysis.dto;

import com.projectiq.mcp.analysis.dto.RepositoryConventionResponse.AnnotationConventions;
import com.projectiq.mcp.analysis.dto.RepositoryConventionResponse.ArchitecturalConventions;
import com.projectiq.mcp.analysis.dto.RepositoryConventionResponse.NamingConventions;
import com.projectiq.mcp.analysis.dto.RepositoryConventionResponse.PackageConventions;
import com.projectiq.mcp.analysis.dto.RepositoryConventionResponse.RestApiConventions;
import com.projectiq.mcp.analysis.dto.RepositoryConventionResponse.TestingConventions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link RepositoryConventionResponse} and its nested DTOs.
 */
class RepositoryConventionResponseTest {

    @Test
    void testDefaultConstructor() {
        RepositoryConventionResponse response = new RepositoryConventionResponse();
        assertNotNull(response.getProjectSpecificObservations());
        assertTrue(response.getProjectSpecificObservations().isEmpty());
        assertNull(response.getRepositoryName());
        assertNull(response.getBranch());
        assertNull(response.getRepositoryOverview());
        assertNull(response.getNamingConventions());
        assertNull(response.getPackageConventions());
        assertNull(response.getArchitecturalConventions());
        assertNull(response.getAnnotationConventions());
        assertNull(response.getRestApiConventions());
        assertNull(response.getTestingConventions());
        assertNull(response.getConfidenceLevel());
    }

    @Test
    void testSettersAndGetters() {
        RepositoryConventionResponse response = new RepositoryConventionResponse();

        response.setRepositoryName("test-repo");
        response.setBranch("main");
        response.setRepositoryOverview("Test overview");
        response.setConfidenceLevel("HIGH");

        assertEquals("test-repo", response.getRepositoryName());
        assertEquals("main", response.getBranch());
        assertEquals("Test overview", response.getRepositoryOverview());
        assertEquals("HIGH", response.getConfidenceLevel());
    }

    @Test
    void testNamingConventions() {
        NamingConventions naming = new NamingConventions();

        naming.setPackageNamingConvention("Lowercase with dot-separated segments (Java standard)");
        naming.setClassNamingConvention("PascalCase (consistent)");
        naming.setMethodNamingConvention("camelCase (Java standard)");
        naming.setDtoNamingPattern("{Name}DTO");
        naming.setEntityNamingPattern("{Name} (plain class name)");
        naming.setServiceNamingPattern("{Name}Service");
        naming.setRepositoryNamingPattern("{Name}Repository");
        naming.setControllerNamingPattern("{Name}Controller");
        naming.setTestNamingConvention("{ClassUnderTest}Test");

        assertEquals("Lowercase with dot-separated segments (Java standard)", naming.getPackageNamingConvention());
        assertEquals("PascalCase (consistent)", naming.getClassNamingConvention());
        assertEquals("camelCase (Java standard)", naming.getMethodNamingConvention());
        assertEquals("{Name}DTO", naming.getDtoNamingPattern());
        assertEquals("{Name} (plain class name)", naming.getEntityNamingPattern());
        assertEquals("{Name}Service", naming.getServiceNamingPattern());
        assertEquals("{Name}Repository", naming.getRepositoryNamingPattern());
        assertEquals("{Name}Controller", naming.getControllerNamingPattern());
        assertEquals("{ClassUnderTest}Test", naming.getTestNamingConvention());
    }

    @Test
    void testPackageConventions() {
        PackageConventions pkg = new PackageConventions();

        pkg.setModuleOrganization("Single module (monolithic)");
        pkg.setPackageNamingStyle("Reverse domain name (e.g., com.company.project)");
        pkg.setLayerPackageConvention("Layer-based packaging (by technical concern)");
        pkg.setDetectedPackages(Arrays.asList("com.projectiq.mcp.controller", "com.projectiq.mcp.service"));

        assertEquals("Single module (monolithic)", pkg.getModuleOrganization());
        assertEquals("Reverse domain name (e.g., com.company.project)", pkg.getPackageNamingStyle());
        assertEquals("Layer-based packaging (by technical concern)", pkg.getLayerPackageConvention());
        assertEquals(2, pkg.getDetectedPackages().size());
        assertEquals("com.projectiq.mcp.controller", pkg.getDetectedPackages().get(0));
    }

    @Test
    void testPackageConventionsDefaultList() {
        PackageConventions pkg = new PackageConventions();
        assertNotNull(pkg.getDetectedPackages());
        assertTrue(pkg.getDetectedPackages().isEmpty());
    }

    @Test
    void testPackageConventionsNullList() {
        PackageConventions pkg = new PackageConventions();
        pkg.setDetectedPackages(null);
        assertNotNull(pkg.getDetectedPackages());
        assertTrue(pkg.getDetectedPackages().isEmpty());
    }

    @Test
    void testArchitecturalConventions() {
        ArchitecturalConventions arch = new ArchitecturalConventions();

        arch.setArchitecturalStyle("Layered Architecture (Controller-Service-Repository)");
        arch.setDetectedLayers(Arrays.asList("Controller (Presentation)", "Service (Business Logic)", "Repository (Data Access)"));
        arch.setConfigurationClassOrganization("Dedicated configuration package");

        assertEquals("Layered Architecture (Controller-Service-Repository)", arch.getArchitecturalStyle());
        assertEquals(3, arch.getDetectedLayers().size());
        assertEquals("Dedicated configuration package", arch.getConfigurationClassOrganization());
    }

    @Test
    void testArchitecturalConventionsDefaultList() {
        ArchitecturalConventions arch = new ArchitecturalConventions();
        assertNotNull(arch.getDetectedLayers());
        assertTrue(arch.getDetectedLayers().isEmpty());
    }

    @Test
    void testAnnotationConventions() {
        AnnotationConventions ann = new AnnotationConventions();

        ann.setCommonAnnotations(Arrays.asList("@RestController", "@Service", "@Repository"));

        assertEquals(3, ann.getCommonAnnotations().size());
        assertTrue(ann.getCommonAnnotations().contains("@RestController"));
    }

    @Test
    void testAnnotationConventionsDefaultList() {
        AnnotationConventions ann = new AnnotationConventions();
        assertNotNull(ann.getCommonAnnotations());
        assertTrue(ann.getCommonAnnotations().isEmpty());
    }

    @Test
    void testRestApiConventions() {
        RestApiConventions rest = new RestApiConventions();

        rest.setEndpointNamingStyle("Plural nouns with HTTP methods (RESTful convention)");
        rest.setHttpMethodUsage("GET for retrieval, POST for creation, PUT for update, DELETE for removal");

        assertEquals("Plural nouns with HTTP methods (RESTful convention)", rest.getEndpointNamingStyle());
        assertEquals("GET for retrieval, POST for creation, PUT for update, DELETE for removal", rest.getHttpMethodUsage());
    }

    @Test
    void testTestingConventions() {
        TestingConventions test = new TestingConventions();

        test.setTestFramework("JUnit 5 (Jupiter)");
        test.setTestNamingStyle("{ClassUnderTest}Test");
        test.setTestLocation("src/test/java (Maven standard)");

        assertEquals("JUnit 5 (Jupiter)", test.getTestFramework());
        assertEquals("{ClassUnderTest}Test", test.getTestNamingStyle());
        assertEquals("src/test/java (Maven standard)", test.getTestLocation());
    }

    @Test
    void testProjectSpecificObservations() {
        RepositoryConventionResponse response = new RepositoryConventionResponse();

        List<String> observations = Arrays.asList(
                "Consistent PascalCase class naming convention detected",
                "Standard layered architecture with Controller-Service-Repository pattern"
        );
        response.setProjectSpecificObservations(observations);

        assertEquals(2, response.getProjectSpecificObservations().size());
        assertEquals("Consistent PascalCase class naming convention detected",
                response.getProjectSpecificObservations().get(0));
    }

    @Test
    void testProjectSpecificObservationsNull() {
        RepositoryConventionResponse response = new RepositoryConventionResponse();
        response.setProjectSpecificObservations(null);
        assertNotNull(response.getProjectSpecificObservations());
        assertTrue(response.getProjectSpecificObservations().isEmpty());
    }

    @Test
    void testNestedDtoInResponse() {
        RepositoryConventionResponse response = new RepositoryConventionResponse();

        NamingConventions naming = new NamingConventions();
        naming.setClassNamingConvention("PascalCase (consistent)");
        response.setNamingConventions(naming);

        PackageConventions pkg = new PackageConventions();
        pkg.setModuleOrganization("Single module (monolithic)");
        response.setPackageConventions(pkg);

        ArchitecturalConventions arch = new ArchitecturalConventions();
        arch.setArchitecturalStyle("Layered Architecture");
        response.setArchitecturalConventions(arch);

        AnnotationConventions ann = new AnnotationConventions();
        ann.setCommonAnnotations(Arrays.asList("@Service"));
        response.setAnnotationConventions(ann);

        RestApiConventions rest = new RestApiConventions();
        rest.setEndpointNamingStyle("RESTful");
        response.setRestApiConventions(rest);

        TestingConventions test = new TestingConventions();
        test.setTestFramework("JUnit 5");
        response.setTestingConventions(test);

        assertNotNull(response.getNamingConventions());
        assertNotNull(response.getPackageConventions());
        assertNotNull(response.getArchitecturalConventions());
        assertNotNull(response.getAnnotationConventions());
        assertNotNull(response.getRestApiConventions());
        assertNotNull(response.getTestingConventions());

        assertEquals("PascalCase (consistent)", response.getNamingConventions().getClassNamingConvention());
        assertEquals("Single module (monolithic)", response.getPackageConventions().getModuleOrganization());
        assertEquals("Layered Architecture", response.getArchitecturalConventions().getArchitecturalStyle());
        assertEquals(1, response.getAnnotationConventions().getCommonAnnotations().size());
        assertEquals("RESTful", response.getRestApiConventions().getEndpointNamingStyle());
        assertEquals("JUnit 5", response.getTestingConventions().getTestFramework());
    }
}