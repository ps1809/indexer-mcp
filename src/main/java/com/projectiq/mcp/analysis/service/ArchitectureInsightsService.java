package com.projectiq.mcp.analysis.service;

import com.projectiq.mcp.analysis.dto.ArchitectureInsightsResponse;
import com.projectiq.mcp.analysis.dto.ArchitectureInsightsResponse.ModuleRelationship;
import com.projectiq.mcp.analysis.dto.ConfidenceLevel;
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
 * Service that analyzes repository architecture and produces deterministic,
 * repository-aware architecture insights. This service identifies architectural
 * layers, module relationships, dependency flow, and architectural patterns
 * based solely on indexed repository data.
 *
 * <p>This service uses the {@link IndexerRestClient} to retrieve repository
 * summary data and analyzes package structure, class types, and naming
 * conventions to infer architectural characteristics.</p>
 *
 * <p>All outputs are deterministic, stable, and free of duplicate entries.
 * This service NEVER infers unsupported architectures or patterns.</p>
 */
@Service
public class ArchitectureInsightsService {

    private static final Logger logger = LoggerFactory.getLogger(ArchitectureInsightsService.class);

    private final IndexerRestClient indexerRestClient;

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

    // --- Pattern detection constants ---

    private static final String LAYERED_ARCHITECTURE = "Layered Architecture";
    private static final String MVC_PATTERN = "MVC (Model-View-Controller)";
    private static final String REPOSITORY_PATTERN_NAME = "Repository Pattern";
    private static final String SERVICE_LAYER_PATTERN = "Service Layer Pattern";
    private static final String BUILDER_PATTERN = "Builder Pattern";
    private static final String FACTORY_PATTERN = "Factory Pattern";
    private static final String STRATEGY_PATTERN = "Strategy Pattern";
    private static final String OBSERVER_PATTERN = "Observer Pattern";

    public ArchitectureInsightsService(IndexerRestClient indexerRestClient) {
        this.indexerRestClient = indexerRestClient;
    }

    /**
     * Analyzes the architecture of a repository and produces deterministic
     * architecture insights.
     *
     * @param repositoryName the repository name to analyze
     * @param branch         the git branch (optional, defaults to "main")
     * @return an {@link ArchitectureInsightsResponse} containing architectural analysis
     */
    public ArchitectureInsightsResponse analyzeArchitecture(String repositoryName, String branch) {
        logger.info("Analyzing architecture for repository: {} branch: {}", repositoryName, branch);

        ArchitectureInsightsResponse response = new ArchitectureInsightsResponse();
        response.setRepositoryName(repositoryName);
        response.setBranch(branch != null && !branch.trim().isEmpty() ? branch.trim() : "main");

        // Step 1: Retrieve repository summary
        RepositorySummaryResponse summary = retrieveRepositorySummary(repositoryName, response.getBranch());

        if (summary == null) {
            response.setRepositoryOverview("Repository data not available. Unable to analyze architecture.");
            response.setArchitecturalStyle("Unknown");
            response.setConfidenceLevel(ConfidenceLevel.LOW.name());
            return response;
        }

        // Step 2: Build repository overview
        String overview = buildRepositoryOverview(summary);
        response.setRepositoryOverview(overview);

        // Step 3: Detect architectural layers
        List<PackageSummary> packages = summary.getPackages();
        if (packages == null) {
            packages = new ArrayList<>();
        }

        List<String> detectedLayers = detectLayers(packages);
        response.setDetectedLayers(detectedLayers);

        // Step 4: Detect architectural style
        String style = detectArchitecturalStyle(detectedLayers, packages);
        response.setArchitecturalStyle(style);

        // Step 5: Detect module relationships
        List<ModuleRelationship> relationships = detectModuleRelationships(packages);
        response.setModuleRelationships(relationships);

        // Step 6: Determine dependency flow
        String dependencyFlow = determineDependencyFlow(detectedLayers, packages);
        response.setDependencyFlow(dependencyFlow);

        // Step 7: Detect cross-layer dependencies
        List<String> crossLayerDeps = detectCrossLayerDependencies(packages);
        response.setCrossLayerDependencies(crossLayerDeps);

        // Step 8: Detect architectural patterns
        List<String> patterns = detectArchitecturalPatterns(packages, detectedLayers, style);
        response.setArchitecturalStrengths(patterns);

        // Step 9: Detect potential concerns
        List<String> concerns = detectPotentialConcerns(packages, detectedLayers);
        response.setPotentialConcerns(concerns);

        // Step 10: Determine confidence level
        String confidence = determineConfidence(summary, packages, detectedLayers);
        response.setConfidenceLevel(confidence);

        logger.info("Architecture analysis complete: style={}, layers={}, patterns={}, confidence={}",
                style, detectedLayers.size(), patterns.size(), confidence);

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
     * Detects architectural layers from package and class structure.
     */
    List<String> detectLayers(List<PackageSummary> packages) {
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
     * Detects the architectural style based on detected layers and package structure.
     */
    String detectArchitecturalStyle(List<String> detectedLayers, List<PackageSummary> packages) {
        boolean hasController = detectedLayers.stream().anyMatch(l -> l.contains("Controller"));
        boolean hasService = detectedLayers.stream().anyMatch(l -> l.contains("Service"));
        boolean hasRepository = detectedLayers.stream().anyMatch(l -> l.contains("Repository"));
        boolean hasEntity = detectedLayers.stream().anyMatch(l -> l.contains("Entity"));
        boolean hasDto = detectedLayers.stream().anyMatch(l -> l.contains("DTO"));

        // Detect Layered Architecture: Controller -> Service -> Repository
        if (hasController && hasService && hasRepository) {
            if (hasEntity && hasDto) {
                return "Layered Architecture with DTO and Domain Model";
            }
            return "Layered Architecture (Controller-Service-Repository)";
        }

        // Detect MVC: Controllers + Models
        if (hasController && hasEntity) {
            return "MVC (Model-View-Controller) Architecture";
        }

        // Detect Hexagonal Architecture: multiple adapter/port packages
        boolean hasAdapterPackages = packages.stream()
                .anyMatch(p -> p.getPackageName() != null
                        && (p.getPackageName().toLowerCase().contains("adapter")
                        || p.getPackageName().toLowerCase().contains("port")
                        || p.getPackageName().toLowerCase().contains("inbound")
                        || p.getPackageName().toLowerCase().contains("outbound")));
        if (hasAdapterPackages && hasService && hasRepository) {
            return "Hexagonal Architecture (Ports and Adapters)";
        }

        // Detect Microservice conventions
        boolean hasServiceDiscovery = packages.stream()
                .anyMatch(p -> p.getPackageName() != null
                        && (p.getPackageName().toLowerCase().contains("discovery")
                        || p.getPackageName().toLowerCase().contains("gateway")
                        || p.getPackageName().toLowerCase().contains("client")));
        if (hasServiceDiscovery && hasController && hasService) {
            return "Microservice-Oriented Architecture";
        }

        // Service-based if we have services
        if (hasService && hasRepository) {
            return "Service-Oriented Architecture";
        }

        // Default based on what we found
        if (hasController) {
            return "Controller-Based Architecture";
        }

        return "Modular Architecture";
    }

    /**
     * Detects relationships between packages/modules.
     */
    List<ModuleRelationship> detectModuleRelationships(List<PackageSummary> packages) {
        List<ModuleRelationship> relationships = new ArrayList<>();
        Set<String> seenRelationships = new LinkedHashSet<>();

        if (packages == null || packages.size() < 2) {
            return relationships;
        }

        List<String> packageNames = packages.stream()
                .map(PackageSummary::getPackageName)
                .filter(n -> n != null && !n.isEmpty())
                .collect(Collectors.toList());

        // Detect relationships based on package naming hierarchy
        for (int i = 0; i < packageNames.size(); i++) {
            for (int j = 0; j < packageNames.size(); j++) {
                if (i == j) continue;

                String src = packageNames.get(i);
                String tgt = packageNames.get(j);

                // Detect parent-child module relationships
                if (isParentPackage(src, tgt)) {
                    String key = "parent:" + src + "->" + tgt;
                    if (seenRelationships.add(key)) {
                        String srcModule = extractModuleName(src);
                        String tgtModule = extractModuleName(tgt);
                        relationships.add(new ModuleRelationship(
                                srcModule, tgtModule, "Package Containment"
                        ));
                    }
                }

                // Detect typical layer-to-layer relationships
                if (isLayerDependency(src, tgt)) {
                    String key = "layer:" + src + "->" + tgt;
                    if (seenRelationships.add(key)) {
                        String srcModule = extractModuleName(src);
                        String tgtModule = extractModuleName(tgt);
                        relationships.add(new ModuleRelationship(
                                srcModule, tgtModule, "Layer Dependency"
                        ));
                    }
                }
            }
        }

        return relationships;
    }

    /**
     * Determines the overall dependency flow description.
     */
    String determineDependencyFlow(List<String> detectedLayers, List<PackageSummary> packages) {
        boolean hasController = detectedLayers.stream().anyMatch(l -> l.contains("Controller"));
        boolean hasService = detectedLayers.stream().anyMatch(l -> l.contains("Service"));
        boolean hasRepository = detectedLayers.stream().anyMatch(l -> l.contains("Repository"));

        if (hasController && hasService && hasRepository) {
            return "Controller -> Service -> Repository (Downward dependency flow)";
        }

        if (hasController && hasService) {
            return "Controller -> Service (Typical layered flow)";
        }

        if (hasService && hasRepository) {
            return "Service -> Repository (Data access flow)";
        }

        if (packages == null || packages.isEmpty()) {
            return "No detectable dependency flow";
        }

        // Default: packages depend on each other based on naming hierarchy
        if (packages.size() > 1) {
            return "Inter-package dependencies based on module hierarchy";
        }

        return "Single module with internal dependencies";
    }

    /**
     * Detects cross-layer dependencies that may indicate architectural concerns.
     */
    List<String> detectCrossLayerDependencies(List<PackageSummary> packages) {
        Set<String> crossLayerDeps = new LinkedHashSet<>();

        if (packages == null || packages.isEmpty()) {
            return new ArrayList<>(crossLayerDeps);
        }

        // Check for repositories importing controllers
        boolean hasRepoPackage = packages.stream()
                .anyMatch(p -> p.getPackageName() != null
                        && p.getPackageName().toLowerCase().contains("repository"));
        boolean hasControllerPackage = packages.stream()
                .anyMatch(p -> p.getPackageName() != null
                        && p.getPackageName().toLowerCase().contains("controller"));

        if (hasRepoPackage && hasControllerPackage) {
            crossLayerDeps.add("Repository package present alongside Controller package "
                    + "- verify repositories are not directly accessed by controllers");
        }

        // Check for DTOs in the same package as entities
        boolean hasDtoPackage = packages.stream()
                .anyMatch(p -> p.getPackageName() != null
                        && p.getPackageName().toLowerCase().contains("dto"));
        boolean hasEntityPackage = packages.stream()
                .anyMatch(p -> p.getPackageName() != null
                        && p.getPackageName().toLowerCase().contains("entity"));

        if (hasEntityPackage && hasDtoPackage) {
            crossLayerDeps.add("DTO and Entity packages both present "
                    + "- ensure DTOs are used for data transfer, not entities directly exposed");
        }

        // Check if configuration exists alongside multiple layers
        boolean hasConfigPackage = packages.stream()
                .anyMatch(p -> p.getPackageName() != null
                        && p.getPackageName().toLowerCase().contains("config"));

        if (hasConfigPackage && hasControllerPackage && hasServicePackage(packages)) {
            crossLayerDeps.add("Configuration package detected across layers "
                    + "- verify configuration is appropriately scoped");
        }

        return new ArrayList<>(crossLayerDeps);
    }

    /**
     * Detects architectural patterns based on class names and package structure.
     */
    List<String> detectArchitecturalPatterns(
            List<PackageSummary> packages, List<String> detectedLayers, String style) {
        Set<String> patterns = new LinkedHashSet<>();

        if (packages == null || packages.isEmpty()) {
            return new ArrayList<>(patterns);
        }

        // Check for Layered Architecture pattern
        boolean hasController = detectedLayers.stream().anyMatch(l -> l.contains("Controller"));
        boolean hasService = detectedLayers.stream().anyMatch(l -> l.contains("Service"));
        boolean hasRepository = detectedLayers.stream().anyMatch(l -> l.contains("Repository"));
        boolean hasEntity = detectedLayers.stream().anyMatch(l -> l.contains("Entity"));

        if (hasController && hasService && hasRepository) {
            patterns.add(LAYERED_ARCHITECTURE);
        }

        // Check for MVC pattern
        if (hasController && hasEntity) {
            patterns.add(MVC_PATTERN);
        }

        // Check for Repository Pattern
        if (hasRepository) {
            patterns.add(REPOSITORY_PATTERN_NAME);
        }

        // Check for Service Layer Pattern
        if (hasService) {
            patterns.add(SERVICE_LAYER_PATTERN);
        }

        // Check for Builder Pattern
        boolean hasBuilder = packages.stream()
                .filter(p -> p.getClasses() != null)
                .flatMap(p -> p.getClasses().stream())
                .anyMatch(c -> c.getClassName() != null && c.getClassName().endsWith("Builder"));
        if (hasBuilder) {
            patterns.add(BUILDER_PATTERN);
        }

        // Check for Factory Pattern
        boolean hasFactory = packages.stream()
                .filter(p -> p.getClasses() != null)
                .flatMap(p -> p.getClasses().stream())
                .anyMatch(c -> c.getClassName() != null
                        && (c.getClassName().endsWith("Factory")
                        || c.getClassName().endsWith("FactoryBean")));
        if (hasFactory) {
            patterns.add(FACTORY_PATTERN);
        }

        // Check for Strategy Pattern
        boolean hasStrategy = packages.stream()
                .filter(p -> p.getClasses() != null)
                .flatMap(p -> p.getClasses().stream())
                .anyMatch(c -> c.getClassName() != null && c.getClassName().endsWith("Strategy"));
        if (hasStrategy) {
            patterns.add(STRATEGY_PATTERN);
        }

        // Check for Observer/Listener Pattern
        boolean hasListener = packages.stream()
                .filter(p -> p.getClasses() != null)
                .flatMap(p -> p.getClasses().stream())
                .anyMatch(c -> c.getClassName() != null
                        && (c.getClassName().endsWith("Listener")
                        || c.getClassName().endsWith("Observer")
                        || c.getClassName().endsWith("EventHandler")));
        if (hasListener) {
            patterns.add(OBSERVER_PATTERN);
        }

        return new ArrayList<>(patterns);
    }

    /**
     * Detects potential architectural concerns.
     */
    List<String> detectPotentialConcerns(List<PackageSummary> packages, List<String> detectedLayers) {
        Set<String> concerns = new LinkedHashSet<>();

        if (packages == null || packages.isEmpty()) {
            concerns.add("No packages detected - repository may be empty or not indexed");
            return new ArrayList<>(concerns);
        }

        // No architectural layers detected
        if (detectedLayers.isEmpty()) {
            concerns.add("No architectural layers detected - the repository structure may be flat or unstructured");
        }

        // Only one layer detected
        if (detectedLayers.size() == 1) {
            concerns.add("Only a single architectural layer detected - the repository may lack separation of concerns");
        }

        // Missing service layer
        boolean hasService = detectedLayers.stream().anyMatch(l -> l.contains("Service"));
        boolean hasController = detectedLayers.stream().anyMatch(l -> l.contains("Controller"));
        if (hasController && !hasService) {
            concerns.add("Controllers detected without a corresponding Service layer "
                    + "- business logic may be mixed with presentation concerns");
        }

        // No repository layer
        boolean hasRepository = detectedLayers.stream().anyMatch(l -> l.contains("Repository"));
        if (hasService && !hasRepository) {
            concerns.add("Service layer detected without a Repository layer "
                    + "- data access logic may be mixed with business logic");
        }

        // Large package (many classes) could indicate missing modularization
        for (PackageSummary pkg : packages) {
            if (pkg.getClasses() != null && pkg.getClasses().size() > 20) {
                concerns.add("Package '" + pkg.getPackageName() + "' contains "
                        + pkg.getClasses().size() + " classes - consider splitting for better modularization");
            }
        }

        return new ArrayList<>(concerns);
    }

    /**
     * Determines confidence level based on data availability and completeness.
     */
    String determineConfidence(
            RepositorySummaryResponse summary, List<PackageSummary> packages, List<String> detectedLayers) {
        if (summary == null) {
            return ConfidenceLevel.LOW.name();
        }

        if (packages == null || packages.isEmpty()) {
            return ConfidenceLevel.LOW.name();
        }

        // High confidence: multiple layers detected with rich package structure
        if (detectedLayers.size() >= 3 && packages.size() >= 3) {
            return ConfidenceLevel.HIGH.name();
        }

        // Medium confidence: some layers and packages detected
        if (detectedLayers.size() >= 1 && packages.size() >= 1) {
            return ConfidenceLevel.MEDIUM.name();
        }

        return ConfidenceLevel.LOW.name();
    }

    // --- Private helper methods ---

    /**
     * Checks if childPackage is a sub-package of parentPackage.
     */
    private boolean isParentPackage(String parentPackage, String childPackage) {
        return childPackage.startsWith(parentPackage + ".")
                && !childPackage.equals(parentPackage);
    }

    /**
     * Extracts a readable module name from a fully qualified package name.
     */
    private String extractModuleName(String packageName) {
        if (packageName == null) {
            return "Unknown";
        }
        String[] parts = packageName.split("\\.");
        if (parts.length >= 2) {
            return parts[parts.length - 2] + "." + parts[parts.length - 1];
        }
        return packageName;
    }

    /**
     * Checks if src package is a typical layer dependency to tgt package.
     */
    private boolean isLayerDependency(String src, String tgt) {
        String srcLower = src.toLowerCase();
        String tgtLower = tgt.toLowerCase();

        // Controller -> Service
        if (srcLower.contains("controller") && tgtLower.contains("service")) {
            return true;
        }

        // Service -> Repository
        if (srcLower.contains("service") && tgtLower.contains("repository")) {
            return true;
        }

        // Service -> Entity
        if (srcLower.contains("service") && tgtLower.contains("entity")) {
            return true;
        }

        // Controller -> DTO
        if (srcLower.contains("controller") && tgtLower.contains("dto")) {
            return true;
        }

        return false;
    }

    /**
     * Checks if any package is a service-layer package.
     */
    private boolean hasServicePackage(List<PackageSummary> packages) {
        return packages.stream()
                .anyMatch(p -> p.getPackageName() != null
                        && p.getPackageName().toLowerCase().contains("service"));
    }
}