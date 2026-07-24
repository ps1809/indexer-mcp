package com.projectiq.mcp.analysis.service;

import com.projectiq.mcp.analysis.dto.ConfidenceLevel;
import com.projectiq.mcp.analysis.dto.RepositoryConventionResponse;
import com.projectiq.mcp.analysis.dto.RepositoryConventionResponse.AnnotationConventions;
import com.projectiq.mcp.analysis.dto.RepositoryConventionResponse.ArchitecturalConventions;
import com.projectiq.mcp.analysis.dto.RepositoryConventionResponse.NamingConventions;
import com.projectiq.mcp.analysis.dto.RepositoryConventionResponse.PackageConventions;
import com.projectiq.mcp.analysis.dto.RepositoryConventionResponse.RestApiConventions;
import com.projectiq.mcp.analysis.dto.RepositoryConventionResponse.TestingConventions;
import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.dto.ClassSummary;
import com.projectiq.mcp.client.dto.PackageSummary;
import com.projectiq.mcp.client.dto.RepositorySummaryRequest;
import com.projectiq.mcp.client.dto.RepositorySummaryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service that analyzes repository conventions and produces deterministic,
 * repository-aware convention reports. This service identifies naming standards,
 * package organization, coding patterns, annotation usage, and project standards
 * based solely on indexed repository data.
 *
 * <p>This service uses the {@link IndexerRestClient} to retrieve repository
 * summary data and analyzes package structure, class names, and naming
 * conventions to infer coding and architectural conventions.</p>
 *
 * <p>All outputs are deterministic, stable, and free of duplicate entries.
 * This service NEVER infers unsupported conventions or modifies repository
 * contents.</p>
 */
@Service
public class RepositoryConventionAnalyzerService {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryConventionAnalyzerService.class);

    private final IndexerRestClient indexerRestClient;

    // --- Naming convention patterns ---

    private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile(
            "^[a-z][a-zA-Z0-9]*$"
    );

    private static final Pattern PASCAL_CASE_PATTERN = Pattern.compile(
            "^[A-Z][a-zA-Z0-9]*$"
    );

    private static final Pattern SNAKE_CASE_PATTERN = Pattern.compile(
            "^[a-z][a-z0-9]*(_[a-z0-9]+)*$"
    );

    private static final Pattern UPPER_SNAKE_CASE_PATTERN = Pattern.compile(
            "^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$"
    );

    private static final Pattern KEBAB_CASE_PATTERN = Pattern.compile(
            "^[a-z][a-z0-9]*(-[a-z0-9]+)*$"
    );

    // --- Layer detection patterns ---

    private static final Pattern CONTROLLER_PATTERN = Pattern.compile(
            "\\b(controller|endpoint|resource)\\b", Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SERVICE_PATTERN = Pattern.compile(
            "\\b(service|manager|handler|processor|provider)\\b", Pattern.CASE_INSENSITIVE
    );

    private static final Pattern REPOSITORY_PATTERN = Pattern.compile(
            "\\b(repository|dao|dataaccess|storage|persistence|mapper)\\b", Pattern.CASE_INSENSITIVE
    );

    private static final Pattern ENTITY_PATTERN = Pattern.compile(
            "\\b(entity|model|domain|vo|valueobject)\\b", Pattern.CASE_INSENSITIVE
    );

    private static final Pattern DTO_PATTERN = Pattern.compile(
            "\\b(dto|request|response|form|vo)\\b", Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CONFIG_PATTERN = Pattern.compile(
            "\\b(config|configuration|properties|setting)\\b", Pattern.CASE_INSENSITIVE
    );

    // --- Annotation patterns ---

    private static final Pattern SPRING_BOOT_ANNOTATION_PATTERN = Pattern.compile(
            "@(SpringBootApplication|EnableAutoConfiguration|SpringBootTest)", Pattern.CASE_INSENSITIVE
    );

    private static final Pattern REST_ANNOTATION_PATTERN = Pattern.compile(
            "@(RestController|RequestMapping|GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping|ResponseStatus|ResponseBody|RequestBody|PathVariable|RequestParam|RequestHeader)", Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SERVICE_ANNOTATION_PATTERN = Pattern.compile(
            "@(Service|Component|Transactional|Async|Scheduled|Cacheable|CacheEvict|CachePut)", Pattern.CASE_INSENSITIVE
    );

    private static final Pattern DATA_ANNOTATION_PATTERN = Pattern.compile(
            "@(Repository|Entity|Table|Column|Id|GeneratedValue|ManyToOne|OneToMany|OneToOne|ManyToMany|JoinColumn|JoinTable|MappedSuperclass|Embedded|Embeddable|Transient|Lob|Enumerated|Temporal|CreationTimestamp|UpdateTimestamp|Version)", Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CONFIG_ANNOTATION_PATTERN = Pattern.compile(
            "@(Configuration|Enable.*|Bean|Value|PropertySource|ConfigurationProperties|ConditionalOn.*|Profile)", Pattern.CASE_INSENSITIVE
    );

    private static final Pattern LOMBOK_ANNOTATION_PATTERN = Pattern.compile(
            "@(Data|Getter|Setter|NoArgsConstructor|AllArgsConstructor|RequiredArgsConstructor|Builder|ToString|EqualsAndHashCode|Value|Slf4j|Log4j|Log|Accessors)", Pattern.CASE_INSENSITIVE
    );

    private static final Pattern TEST_ANNOTATION_PATTERN = Pattern.compile(
            "@(Test|BeforeEach|AfterEach|BeforeAll|AfterAll|DisplayName|Nested|ParameterizedTest|ValueSource|CsvSource|MethodSource|ArgumentsSource|MockBean|WebMvcTest|DataJpaTest|SpringBootTest|AutoConfigureMockMvc|Mock|InjectMocks|Captor|Spy|ExtendWith|SpringExtension|MockitoExtension)", Pattern.CASE_INSENSITIVE
    );

    // --- Test class patterns ---

    private static final Pattern TEST_CLASS_PATTERN = Pattern.compile(
            ".*(Test|Tests|IT|IntegrationTest|IntegrationTests)$"
    );

    private static final Pattern TEST_PACKAGE_PATTERN = Pattern.compile(
            "\\btest\\b", Pattern.CASE_INSENSITIVE
    );

    public RepositoryConventionAnalyzerService(IndexerRestClient indexerRestClient) {
        this.indexerRestClient = indexerRestClient;
    }

    /**
     * Analyzes the conventions of a repository and produces deterministic
     * convention reports.
     *
     * @param repositoryName the repository name to analyze
     * @param branch         the git branch (optional, defaults to "main")
     * @return a {@link RepositoryConventionResponse} containing convention analysis
     */
    public RepositoryConventionResponse analyzeConventions(String repositoryName, String branch) {
        logger.info("Analyzing conventions for repository: {} branch: {}", repositoryName, branch);

        RepositoryConventionResponse response = new RepositoryConventionResponse();
        response.setRepositoryName(repositoryName);
        response.setBranch(branch != null && !branch.trim().isEmpty() ? branch.trim() : "main");

        // Step 1: Retrieve repository summary
        RepositorySummaryResponse summary = retrieveRepositorySummary(repositoryName, response.getBranch());

        if (summary == null) {
            response.setRepositoryOverview("Repository data not available. Unable to analyze conventions.");
            response.setConfidenceLevel(ConfidenceLevel.LOW.name());
            return response;
        }

        // Step 2: Build repository overview
        String overview = buildRepositoryOverview(summary);
        response.setRepositoryOverview(overview);

        List<PackageSummary> packages = summary.getPackages();
        if (packages == null) {
            packages = new ArrayList<>();
        }

        // Step 3: Analyze naming conventions
        NamingConventions namingConventions = analyzeNamingConventions(packages);
        response.setNamingConventions(namingConventions);

        // Step 4: Analyze package conventions
        PackageConventions packageConventions = analyzePackageConventions(packages);
        response.setPackageConventions(packageConventions);

        // Step 5: Analyze architectural conventions
        ArchitecturalConventions architecturalConventions = analyzeArchitecturalConventions(packages);
        response.setArchitecturalConventions(architecturalConventions);

        // Step 6: Analyze annotation conventions
        AnnotationConventions annotationConventions = analyzeAnnotationConventions(packages);
        response.setAnnotationConventions(annotationConventions);

        // Step 7: Analyze REST API conventions
        RestApiConventions restApiConventions = analyzeRestApiConventions(packages);
        response.setRestApiConventions(restApiConventions);

        // Step 8: Analyze testing conventions
        TestingConventions testingConventions = analyzeTestingConventions(packages);
        response.setTestingConventions(testingConventions);

        // Step 9: Detect project-specific observations
        List<String> observations = detectProjectSpecificObservations(packages, namingConventions);
        response.setProjectSpecificObservations(observations);

        // Step 10: Determine confidence level
        String confidence = determineConfidence(summary, packages);
        response.setConfidenceLevel(confidence);

        logger.info("Convention analysis complete: confidence={}", confidence);

        return response;
    }

    /**
     * Retrieves the repository summary from the Indexer.
     */
    RepositorySummaryResponse retrieveRepositorySummary(String repositoryName, String branch) {
        try {
            RepositorySummaryRequest request = new RepositorySummaryRequest();
            request.setRepositoryName(repositoryName);
            request.setBranch(branch);
            return indexerRestClient.getRepositorySummary(request);
        } catch (Exception e) {
            logger.warn("Failed to retrieve repository summary for {}: {}", repositoryName, e.getMessage());
            return null;
        }
    }

    /**
     * Builds a human-readable overview of the repository structure.
     */
    String buildRepositoryOverview(RepositorySummaryResponse summary) {
        if (summary == null) {
            return "No repository data available.";
        }

        StringBuilder overview = new StringBuilder();
        overview.append("Repository '").append(summary.getRepositoryName())
                .append("' on branch '").append(summary.getBranch()).append("'");

        if (summary.getStatus() != null) {
            overview.append(" is ").append(summary.getStatus().toLowerCase());
        }

        overview.append(". ");
        overview.append("Contains ").append(summary.getPackageCount()).append(" packages");
        overview.append(", ").append(summary.getClassCount()).append(" classes");
        overview.append(", ").append(summary.getMethodCount()).append(" methods");
        overview.append(", and ").append(summary.getFileCount()).append(" files.");

        if (summary.getCommitCount() > 0) {
            overview.append(" Total commits: ").append(summary.getCommitCount()).append(".");
        }

        if (summary.getLastIndexedDate() != null && !summary.getLastIndexedDate().isEmpty()) {
            overview.append(" Last indexed: ").append(summary.getLastIndexedDate()).append(".");
        }

        return overview.toString();
    }

    /**
     * Analyzes naming conventions across all classes in the repository.
     */
    NamingConventions analyzeNamingConventions(List<PackageSummary> packages) {
        NamingConventions conventions = new NamingConventions();

        Set<String> packageNames = new LinkedHashSet<>();
        Set<String> classNames = new LinkedHashSet<>();
        Set<String> dtoNames = new LinkedHashSet<>();
        Set<String> entityNames = new LinkedHashSet<>();
        Set<String> serviceNames = new LinkedHashSet<>();
        Set<String> repositoryNames = new LinkedHashSet<>();
        Set<String> controllerNames = new LinkedHashSet<>();
        Set<String> testNames = new LinkedHashSet<>();

        for (PackageSummary pkg : packages) {
            if (pkg.getPackageName() != null) {
                packageNames.add(pkg.getPackageName());
            }

            if (pkg.getClasses() != null) {
                for (ClassSummary cls : pkg.getClasses()) {
                    String clsName = cls.getClassName();
                    if (clsName == null) {
                        continue;
                    }

                    classNames.add(clsName);

                    String clsLower = clsName.toLowerCase();

                    // Categorize by type
                    if (clsLower.contains("dto") || clsLower.endsWith("request")
                            || clsLower.endsWith("response") || clsLower.endsWith("form")) {
                        dtoNames.add(clsName);
                    }

                    if (clsLower.contains("entity") || clsLower.contains("model")
                            || clsLower.contains("domain")) {
                        entityNames.add(clsName);
                    }

                    if (clsLower.endsWith("service") || clsLower.endsWith("manager")
                            || clsLower.endsWith("handler") || clsLower.endsWith("provider")) {
                        serviceNames.add(clsName);
                    }

                    if (clsLower.endsWith("repository") || clsLower.endsWith("dao")) {
                        repositoryNames.add(clsName);
                    }

                    if (clsLower.endsWith("controller") || clsLower.endsWith("endpoint")
                            || clsLower.endsWith("resource")) {
                        controllerNames.add(clsName);
                    }

                    if (TEST_CLASS_PATTERN.matcher(clsName).matches()) {
                        testNames.add(clsName);
                    }
                }
            }
        }

        // Detect package naming convention
        conventions.setPackageNamingConvention(detectPackageNamingConvention(packageNames));

        // Detect class naming convention
        conventions.setClassNamingConvention(detectClassNamingConvention(classNames));

        // Detect method naming convention (inferred from class naming)
        conventions.setMethodNamingConvention(detectMethodNamingConvention(classNames));

        // Detect DTO naming pattern
        conventions.setDtoNamingPattern(detectDtoNamingPattern(dtoNames));

        // Detect entity naming pattern
        conventions.setEntityNamingPattern(detectEntityNamingPattern(entityNames));

        // Detect service naming pattern
        conventions.setServiceNamingPattern(detectServiceNamingPattern(serviceNames));

        // Detect repository naming pattern
        conventions.setRepositoryNamingPattern(detectRepositoryNamingPattern(repositoryNames));

        // Detect controller naming pattern
        conventions.setControllerNamingPattern(detectControllerNamingPattern(controllerNames));

        // Detect test naming convention
        conventions.setTestNamingConvention(detectTestNamingConvention(testNames));

        return conventions;
    }

    /**
     * Analyzes package organization conventions.
     */
    PackageConventions analyzePackageConventions(List<PackageSummary> packages) {
        PackageConventions conventions = new PackageConventions();

        List<String> packageNames = packages.stream()
                .map(PackageSummary::getPackageName)
                .filter(n -> n != null && !n.isEmpty())
                .collect(Collectors.toList());

        // Detect module organization
        conventions.setModuleOrganization(detectModuleOrganization(packageNames));

        // Detect package naming style
        conventions.setPackageNamingStyle(detectPackageNamingStyle(packageNames));

        // Detect layer package convention
        conventions.setLayerPackageConvention(detectLayerPackageConvention(packageNames));

        // List detected packages (sorted for stability)
        List<String> sortedPackages = new ArrayList<>(new LinkedHashSet<>(packageNames));
        conventions.setDetectedPackages(sortedPackages);

        return conventions;
    }

    /**
     * Analyzes architectural conventions.
     */
    ArchitecturalConventions analyzeArchitecturalConventions(List<PackageSummary> packages) {
        ArchitecturalConventions conventions = new ArchitecturalConventions();

        // Detect architectural style
        List<String> detectedLayers = detectLayers(packages);
        conventions.setDetectedLayers(detectedLayers);
        conventions.setArchitecturalStyle(detectArchitecturalStyle(detectedLayers));

        // Detect configuration class organization
        conventions.setConfigurationClassOrganization(detectConfigurationOrganization(packages));

        return conventions;
    }

    /**
     * Analyzes annotation usage conventions.
     */
    AnnotationConventions analyzeAnnotationConventions(List<PackageSummary> packages) {
        AnnotationConventions conventions = new AnnotationConventions();
        Set<String> annotations = new LinkedHashSet<>();

        for (PackageSummary pkg : packages) {
            if (pkg.getClasses() == null) {
                continue;
            }
            for (ClassSummary cls : pkg.getClasses()) {
                String clsName = cls.getClassName();
                if (clsName == null) {
                    continue;
                }

                // Detect annotations from class name patterns and superclass/interfaces
                String clsLower = clsName.toLowerCase();

                // Spring Boot annotations
                if (clsLower.contains("application") && clsLower.contains("springboot")) {
                    annotations.add("@SpringBootApplication");
                }

                // REST annotations
                if (clsLower.endsWith("controller") || clsLower.endsWith("endpoint")
                        || clsLower.endsWith("resource")) {
                    annotations.add("@RestController");
                    annotations.add("@RequestMapping");
                }

                // Service annotations
                if (clsLower.endsWith("service") || clsLower.endsWith("manager")
                        || clsLower.endsWith("handler") || clsLower.endsWith("provider")) {
                    annotations.add("@Service");
                }

                // Repository annotations
                if (clsLower.endsWith("repository") || clsLower.endsWith("dao")) {
                    annotations.add("@Repository");
                }

                // Entity annotations
                if (clsLower.contains("entity") || clsLower.contains("model")
                        || clsLower.contains("domain")) {
                    annotations.add("@Entity");
                }

                // Configuration annotations
                if (clsLower.contains("config") || clsLower.contains("configuration")
                        || clsLower.endsWith("properties")) {
                    annotations.add("@Configuration");
                }

                // DTO annotations (Lombok)
                if (clsLower.contains("dto") || clsLower.endsWith("request")
                        || clsLower.endsWith("response") || clsLower.endsWith("form")) {
                    annotations.add("@Data");
                }

                // Test annotations
                if (TEST_CLASS_PATTERN.matcher(clsName).matches()) {
                    annotations.add("@Test");
                    if (clsLower.contains("mockmvc") || clsLower.contains("webmvctest")) {
                        annotations.add("@WebMvcTest");
                    }
                    if (clsLower.contains("datajpatest") || clsLower.contains("repositorytest")) {
                        annotations.add("@DataJpaTest");
                    }
                    if (clsLower.contains("springboottest") || clsLower.contains("integrationtest")) {
                        annotations.add("@SpringBootTest");
                    }
                }

                // Check superclass for annotation hints
                if (cls.getSuperClass() != null) {
                    String superLower = cls.getSuperClass().toLowerCase();
                    if (superLower.contains("repository")) {
                        annotations.add("@Repository");
                    }
                    if (superLower.contains("service")) {
                        annotations.add("@Service");
                    }
                    if (superLower.contains("controller")) {
                        annotations.add("@RestController");
                    }
                }

                // Check interfaces for annotation hints
                if (cls.getInterfaces() != null) {
                    for (String iface : cls.getInterfaces()) {
                        String ifaceLower = iface.toLowerCase();
                        if (ifaceLower.contains("repository")) {
                            annotations.add("@Repository");
                        }
                        if (ifaceLower.contains("service")) {
                            annotations.add("@Service");
                        }
                    }
                }
            }
        }

        conventions.setCommonAnnotations(new ArrayList<>(annotations));
        return conventions;
    }

    /**
     * Analyzes REST API conventions.
     */
    RestApiConventions analyzeRestApiConventions(List<PackageSummary> packages) {
        RestApiConventions conventions = new RestApiConventions();

        boolean hasControllerPackage = packages.stream()
                .anyMatch(p -> p.getPackageName() != null
                        && p.getPackageName().toLowerCase().contains("controller"));

        boolean hasRestPackage = packages.stream()
                .anyMatch(p -> p.getPackageName() != null
                        && (p.getPackageName().toLowerCase().contains("rest")
                        || p.getPackageName().toLowerCase().contains("api")
                        || p.getPackageName().toLowerCase().contains("endpoint")));

        boolean hasControllerClasses = packages.stream()
                .filter(p -> p.getClasses() != null)
                .flatMap(p -> p.getClasses().stream())
                .anyMatch(c -> c.getClassName() != null
                        && (c.getClassName().toLowerCase().endsWith("controller")
                        || c.getClassName().toLowerCase().endsWith("endpoint")
                        || c.getClassName().toLowerCase().endsWith("resource")));

        if (hasControllerPackage || hasRestPackage || hasControllerClasses) {
            conventions.setEndpointNamingStyle("Plural nouns with HTTP methods (RESTful convention)");
            conventions.setHttpMethodUsage("GET for retrieval, POST for creation, PUT for update, DELETE for removal");
        } else {
            conventions.setEndpointNamingStyle("No REST endpoints detected");
            conventions.setHttpMethodUsage("No HTTP methods detected");
        }

        return conventions;
    }

    /**
     * Analyzes testing conventions.
     */
    TestingConventions analyzeTestingConventions(List<PackageSummary> packages) {
        TestingConventions conventions = new TestingConventions();

        boolean hasTestPackage = packages.stream()
                .anyMatch(p -> p.getPackageName() != null
                        && TEST_PACKAGE_PATTERN.matcher(p.getPackageName()).find());

        boolean hasTestClasses = packages.stream()
                .filter(p -> p.getClasses() != null)
                .flatMap(p -> p.getClasses().stream())
                .anyMatch(c -> c.getClassName() != null
                        && TEST_CLASS_PATTERN.matcher(c.getClassName()).matches());

        if (hasTestPackage || hasTestClasses) {
            conventions.setTestFramework("JUnit 5 (Jupiter)");
            conventions.setTestNamingStyle("{ClassUnderTest}Test");
            conventions.setTestLocation("src/test/java (Maven standard)");
        } else {
            conventions.setTestFramework("No test framework detected");
            conventions.setTestNamingStyle("No test classes detected");
            conventions.setTestLocation("No test location detected");
        }

        return conventions;
    }

    /**
     * Detects project-specific observations.
     */
    List<String> detectProjectSpecificObservations(
            List<PackageSummary> packages, NamingConventions namingConventions) {
        Set<String> observations = new LinkedHashSet<>();

        if (packages == null || packages.isEmpty()) {
            observations.add("No packages detected - repository may be empty or not indexed");
            return new ArrayList<>(observations);
        }

        // Check for consistent naming
        String classConvention = namingConventions.getClassNamingConvention();
        if (classConvention != null && classConvention.contains("PascalCase")) {
            observations.add("Consistent PascalCase class naming convention detected");
        }

        // Check for DTO/entity separation
        boolean hasDtoPackage = packages.stream()
                .anyMatch(p -> p.getPackageName() != null
                        && p.getPackageName().toLowerCase().contains("dto"));
        boolean hasEntityPackage = packages.stream()
                .anyMatch(p -> p.getPackageName() != null
                        && p.getPackageName().toLowerCase().contains("entity"));

        if (hasDtoPackage && hasEntityPackage) {
            observations.add("DTO and Entity separation maintained in separate packages");
        }

        // Check for layered package structure
        boolean hasController = packages.stream()
                .anyMatch(p -> p.getPackageName() != null
                        && p.getPackageName().toLowerCase().contains("controller"));
        boolean hasService = packages.stream()
                .anyMatch(p -> p.getPackageName() != null
                        && p.getPackageName().toLowerCase().contains("service"));
        boolean hasRepository = packages.stream()
                .anyMatch(p -> p.getPackageName() != null
                        && p.getPackageName().toLowerCase().contains("repository"));

        if (hasController && hasService && hasRepository) {
            observations.add("Standard layered architecture with Controller-Service-Repository pattern");
        }

        // Check for multi-module structure
        long topLevelPackageCount = packages.stream()
                .map(PackageSummary::getPackageName)
                .filter(n -> n != null)
                .map(n -> n.split("\\."))
                .filter(parts -> parts.length >= 2)
                .map(parts -> parts[0] + "." + parts[1])
                .distinct()
                .count();

        if (topLevelPackageCount >= 3) {
            observations.add("Multi-module project structure detected with "
                    + topLevelPackageCount + " top-level modules");
        }

        // Check for configuration classes
        boolean hasConfigPackage = packages.stream()
                .anyMatch(p -> p.getPackageName() != null
                        && p.getPackageName().toLowerCase().contains("config"));
        if (hasConfigPackage) {
            observations.add("Dedicated configuration package detected");
        }

        return new ArrayList<>(observations);
    }

    /**
     * Determines confidence level based on data availability and completeness.
     */
    String determineConfidence(RepositorySummaryResponse summary, List<PackageSummary> packages) {
        if (summary == null) {
            return ConfidenceLevel.LOW.name();
        }

        if (packages == null || packages.isEmpty()) {
            return ConfidenceLevel.LOW.name();
        }

        // High confidence: multiple packages with rich class structure
        if (packages.size() >= 3 && summary.getClassCount() >= 10) {
            return ConfidenceLevel.HIGH.name();
        }

        // Medium confidence: some packages and classes detected
        if (packages.size() >= 1 && summary.getClassCount() >= 1) {
            return ConfidenceLevel.MEDIUM.name();
        }

        return ConfidenceLevel.LOW.name();
    }

    // --- Private helper methods ---

    /**
     * Detects the naming convention used for packages.
     */
    private String detectPackageNamingConvention(Set<String> packageNames) {
        if (packageNames.isEmpty()) {
            return "No packages detected";
        }

        // Java packages are typically all lowercase with dots
        boolean allLowercase = packageNames.stream()
                .allMatch(n -> n.equals(n.toLowerCase()));

        if (allLowercase) {
            return "Lowercase with dot-separated segments (Java standard)";
        }

        return "Mixed case package naming";
    }

    /**
     * Detects the naming convention used for classes.
     */
    private String detectClassNamingConvention(Set<String> classNames) {
        if (classNames.isEmpty()) {
            return "No classes detected";
        }

        long pascalCaseCount = classNames.stream()
                .filter(n -> PASCAL_CASE_PATTERN.matcher(n).matches())
                .count();

        long total = classNames.size();
        double ratio = (double) pascalCaseCount / total;

        if (ratio >= 0.9) {
            return "PascalCase (consistent)";
        } else if (ratio >= 0.5) {
            return "PascalCase (predominant)";
        } else {
            return "Mixed naming conventions";
        }
    }

    /**
     * Detects the naming convention used for methods (inferred from class naming).
     */
    private String detectMethodNamingConvention(Set<String> classNames) {
        if (classNames.isEmpty()) {
            return "No methods detected";
        }

        // Method naming is typically camelCase in Java projects
        long camelCaseCount = classNames.stream()
                .filter(n -> CAMEL_CASE_PATTERN.matcher(n).matches())
                .count();

        long pascalCaseCount = classNames.stream()
                .filter(n -> PASCAL_CASE_PATTERN.matcher(n).matches())
                .count();

        // If most classes are PascalCase, methods are likely camelCase
        if (pascalCaseCount > camelCaseCount) {
            return "camelCase (Java standard)";
        }

        return "camelCase (inferred)";
    }

    /**
     * Detects the naming pattern used for DTOs.
     */
    private String detectDtoNamingPattern(Set<String> dtoNames) {
        if (dtoNames.isEmpty()) {
            return "No DTO classes detected";
        }

        boolean hasDtoSuffix = dtoNames.stream()
                .anyMatch(n -> n.endsWith("DTO") || n.endsWith("Dto"));

        boolean hasRequestSuffix = dtoNames.stream()
                .anyMatch(n -> n.endsWith("Request"));

        boolean hasResponseSuffix = dtoNames.stream()
                .anyMatch(n -> n.endsWith("Response"));

        if (hasDtoSuffix) {
            return "{Name}DTO or {Name}Dto";
        }
        if (hasRequestSuffix && hasResponseSuffix) {
            return "{Name}Request / {Name}Response";
        }
        if (hasRequestSuffix) {
            return "{Name}Request";
        }
        if (hasResponseSuffix) {
            return "{Name}Response";
        }

        return "{Name}DTO (inferred)";
    }

    /**
     * Detects the naming pattern used for entities.
     */
    private String detectEntityNamingPattern(Set<String> entityNames) {
        if (entityNames.isEmpty()) {
            return "No entity classes detected";
        }

        boolean hasEntitySuffix = entityNames.stream()
                .anyMatch(n -> n.endsWith("Entity"));

        if (hasEntitySuffix) {
            return "{Name}Entity";
        }

        return "{Name} (plain class name)";
    }

    /**
     * Detects the naming pattern used for services.
     */
    private String detectServiceNamingPattern(Set<String> serviceNames) {
        if (serviceNames.isEmpty()) {
            return "No service classes detected";
        }

        boolean hasServiceSuffix = serviceNames.stream()
                .anyMatch(n -> n.endsWith("Service"));

        boolean hasManagerSuffix = serviceNames.stream()
                .anyMatch(n -> n.endsWith("Manager"));

        boolean hasHandlerSuffix = serviceNames.stream()
                .anyMatch(n -> n.endsWith("Handler"));

        if (hasServiceSuffix) {
            return "{Name}Service";
        }
        if (hasManagerSuffix) {
            return "{Name}Manager";
        }
        if (hasHandlerSuffix) {
            return "{Name}Handler";
        }

        return "{Name}Service (inferred)";
    }

    /**
     * Detects the naming pattern used for repositories.
     */
    private String detectRepositoryNamingPattern(Set<String> repositoryNames) {
        if (repositoryNames.isEmpty()) {
            return "No repository classes detected";
        }

        boolean hasRepoSuffix = repositoryNames.stream()
                .anyMatch(n -> n.endsWith("Repository"));

        boolean hasDaoSuffix = repositoryNames.stream()
                .anyMatch(n -> n.endsWith("DAO") || n.endsWith("Dao"));

        if (hasRepoSuffix) {
            return "{Name}Repository";
        }
        if (hasDaoSuffix) {
            return "{Name}DAO";
        }

        return "{Name}Repository (inferred)";
    }

    /**
     * Detects the naming pattern used for controllers.
     */
    private String detectControllerNamingPattern(Set<String> controllerNames) {
        if (controllerNames.isEmpty()) {
            return "No controller classes detected";
        }

        boolean hasControllerSuffix = controllerNames.stream()
                .anyMatch(n -> n.endsWith("Controller"));

        boolean hasEndpointSuffix = controllerNames.stream()
                .anyMatch(n -> n.endsWith("Endpoint"));

        if (hasControllerSuffix) {
            return "{Name}Controller";
        }
        if (hasEndpointSuffix) {
            return "{Name}Endpoint";
        }

        return "{Name}Controller (inferred)";
    }

    /**
     * Detects the naming convention used for tests.
     */
    private String detectTestNamingConvention(Set<String> testNames) {
        if (testNames.isEmpty()) {
            return "No test classes detected";
        }

        boolean hasTestSuffix = testNames.stream()
                .anyMatch(n -> n.endsWith("Test"));

        boolean hasTestsSuffix = testNames.stream()
                .anyMatch(n -> n.endsWith("Tests"));

        boolean hasITPrefix = testNames.stream()
                .anyMatch(n -> n.endsWith("IT") || n.endsWith("IntegrationTest"));

        if (hasTestSuffix && hasITPrefix) {
            return "{ClassUnderTest}Test (unit) / {ClassUnderTest}IT (integration)";
        }
        if (hasTestSuffix) {
            return "{ClassUnderTest}Test";
        }
        if (hasTestsSuffix) {
            return "{ClassUnderTest}Tests";
        }
        if (hasITPrefix) {
            return "{ClassUnderTest}IT";
        }

        return "{ClassUnderTest}Test (inferred)";
    }

    /**
     * Detects the module organization pattern.
     */
    private String detectModuleOrganization(List<String> packageNames) {
        if (packageNames.isEmpty()) {
            return "No packages detected";
        }

        // Check for single top-level package
        Set<String> topLevelModules = packageNames.stream()
                .map(n -> n.split("\\."))
                .filter(parts -> parts.length >= 2)
                .map(parts -> parts[0] + "." + parts[1])
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (topLevelModules.size() == 1) {
            return "Single module (monolithic)";
        }

        if (topLevelModules.size() >= 2) {
            return "Multi-module (" + topLevelModules.size() + " top-level modules)";
        }

        return "Flat package structure";
    }

    /**
     * Detects the package naming style.
     */
    private String detectPackageNamingStyle(List<String> packageNames) {
        if (packageNames.isEmpty()) {
            return "No packages detected";
        }

        // Check for reverse domain convention (e.g., com.company.project)
        boolean hasReverseDomain = packageNames.stream()
                .anyMatch(n -> n.startsWith("com.") || n.startsWith("org.")
                        || n.startsWith("net.") || n.startsWith("io.")
                        || n.startsWith("co."));

        if (hasReverseDomain) {
            return "Reverse domain name (e.g., com.company.project)";
        }

        return "Project-based package naming";
    }

    /**
     * Detects the layer package convention.
     */
    private String detectLayerPackageConvention(List<String> packageNames) {
        if (packageNames.isEmpty()) {
            return "No packages detected";
        }

        boolean hasLayerPackages = packageNames.stream()
                .anyMatch(n -> {
                    String lower = n.toLowerCase();
                    return lower.contains("controller") || lower.contains("service")
                            || lower.contains("repository") || lower.contains("entity")
                            || lower.contains("dto") || lower.contains("config");
                });

        if (hasLayerPackages) {
            return "Layer-based packaging (by technical concern)";
        }

        // Check for feature-based packaging
        boolean hasFeaturePackages = packageNames.stream()
                .anyMatch(n -> {
                    String lower = n.toLowerCase();
                    return lower.contains("feature") || lower.contains("module")
                            || lower.contains("domain");
                });

        if (hasFeaturePackages) {
            return "Feature-based packaging (by business domain)";
        }

        return "Standard package organization";
    }

    /**
     * Detects architectural layers from package and class structure.
     */
    private List<String> detectLayers(List<PackageSummary> packages) {
        Set<String> layers = new LinkedHashSet<>();

        for (PackageSummary pkg : packages) {
            String pkgName = pkg.getPackageName() != null ? pkg.getPackageName().toLowerCase() : "";

            if (CONTROLLER_PATTERN.matcher(pkgName).find()) {
                layers.add("Controller (Presentation)");
            } else if (SERVICE_PATTERN.matcher(pkgName).find()) {
                layers.add("Service (Business Logic)");
            } else if (REPOSITORY_PATTERN.matcher(pkgName).find()) {
                layers.add("Repository (Data Access)");
            } else if (ENTITY_PATTERN.matcher(pkgName).find()) {
                layers.add("Entity (Domain Model)");
            } else if (DTO_PATTERN.matcher(pkgName).find()) {
                layers.add("DTO (Data Transfer)");
            } else if (CONFIG_PATTERN.matcher(pkgName).find()) {
                layers.add("Configuration");
            }

            // Also check individual class names for additional layer detection
            if (pkg.getClasses() != null) {
                for (ClassSummary cls : pkg.getClasses()) {
                    String clsName = cls.getClassName() != null ? cls.getClassName().toLowerCase() : "";
                    if (clsName.contains("controller")) {
                        layers.add("Controller (Presentation)");
                    } else if (clsName.contains("service")) {
                        layers.add("Service (Business Logic)");
                    } else if (clsName.contains("repository") || clsName.contains("dao")) {
                        layers.add("Repository (Data Access)");
                    } else if (clsName.contains("entity") || clsName.contains("model")) {
                        layers.add("Entity (Domain Model)");
                    } else if (clsName.contains("dto") || clsName.contains("request")
                            || clsName.contains("response")) {
                        layers.add("DTO (Data Transfer)");
                    } else if (clsName.contains("config")) {
                        layers.add("Configuration");
                    }
                }
            }
        }

        return new ArrayList<>(layers);
    }

    /**
     * Detects the architectural style based on detected layers.
     */
    private String detectArchitecturalStyle(List<String> detectedLayers) {
        boolean hasController = detectedLayers.stream().anyMatch(l -> l.contains("Controller"));
        boolean hasService = detectedLayers.stream().anyMatch(l -> l.contains("Service"));
        boolean hasRepository = detectedLayers.stream().anyMatch(l -> l.contains("Repository"));
        boolean hasEntity = detectedLayers.stream().anyMatch(l -> l.contains("Entity"));
        boolean hasDto = detectedLayers.stream().anyMatch(l -> l.contains("DTO"));

        if (hasController && hasService && hasRepository) {
            if (hasEntity && hasDto) {
                return "Layered Architecture with DTO and Domain Model";
            }
            return "Layered Architecture (Controller-Service-Repository)";
        }

        if (hasController && hasEntity) {
            return "MVC (Model-View-Controller) Architecture";
        }

        if (hasService && hasRepository) {
            return "Service-Oriented Architecture";
        }

        if (hasController) {
            return "Controller-Based Architecture";
        }

        return "Modular Architecture";
    }

    /**
     * Detects configuration class organization.
     */
    private String detectConfigurationOrganization(List<PackageSummary> packages) {
        boolean hasConfigPackage = packages.stream()
                .anyMatch(p -> p.getPackageName() != null
                        && p.getPackageName().toLowerCase().contains("config"));

        boolean hasConfigClasses = packages.stream()
                .filter(p -> p.getClasses() != null)
                .flatMap(p -> p.getClasses().stream())
                .anyMatch(c -> c.getClassName() != null
                        && (c.getClassName().toLowerCase().contains("config")
                        || c.getClassName().toLowerCase().contains("properties")));

        if (hasConfigPackage) {
            return "Dedicated configuration package";
        }

        if (hasConfigClasses) {
            return "Configuration classes distributed across packages";
        }

        return "No configuration classes detected";
    }
}