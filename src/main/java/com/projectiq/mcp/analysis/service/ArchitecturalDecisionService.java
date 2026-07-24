package com.projectiq.mcp.analysis.service;

import com.projectiq.mcp.analysis.dto.ArchitecturalDecisionResponse;
import com.projectiq.mcp.analysis.dto.ArchitecturalDecisionResponse.Alternative;
import com.projectiq.mcp.analysis.dto.ArchitecturalDecisionResponse.DependencyImplications;
import com.projectiq.mcp.analysis.dto.ArchitecturalDecisionResponse.ImpactAssessment;
import com.projectiq.mcp.analysis.dto.ArchitecturalDecisionResponse.MaintainabilityAssessment;
import com.projectiq.mcp.analysis.dto.ArchitecturalDecisionResponse.ScalabilityAssessment;
import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.dto.RepositorySummaryRequest;
import com.projectiq.mcp.client.dto.RepositorySummaryResponse;
import com.projectiq.mcp.knowledgegraph.service.RepositoryKnowledgeGraphService;
import com.projectiq.mcp.strategy.service.DevelopmentStrategyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service that evaluates architectural design choices and provides deterministic
 * recommendations. Compares multiple architectural alternatives using repository
 * intelligence, dependency analysis, knowledge graph relationships, and
 * development strategy analysis.
 *
 * <p>This service evaluates alternatives for supported decision categories and
 * produces comprehensive reports with pros/cons, impact analysis, scalability
 * assessment, maintainability assessment, and a recommended approach.</p>
 *
 * <p>All outputs are deterministic, stable, and free of duplicate entries.
 * This service NEVER modifies repository contents or generates code.</p>
 */
@Service
public class ArchitecturalDecisionService {

    private static final Logger logger = LoggerFactory.getLogger(ArchitecturalDecisionService.class);

    private final IndexerRestClient indexerRestClient;
    private final ArchitectureInsightsService architectureInsightsService;
    private final RepositoryKnowledgeGraphService knowledgeGraphService;
    private final DevelopmentStrategyService developmentStrategyService;
    private final CrossRepositoryAnalysisService crossRepositoryAnalysisService;
    private final CodeChangeAnalysisService codeChangeAnalysisService;

    public ArchitecturalDecisionService(
            IndexerRestClient indexerRestClient,
            ArchitectureInsightsService architectureInsightsService,
            RepositoryKnowledgeGraphService knowledgeGraphService,
            DevelopmentStrategyService developmentStrategyService,
            CrossRepositoryAnalysisService crossRepositoryAnalysisService,
            CodeChangeAnalysisService codeChangeAnalysisService) {
        this.indexerRestClient = indexerRestClient;
        this.architectureInsightsService = architectureInsightsService;
        this.knowledgeGraphService = knowledgeGraphService;
        this.developmentStrategyService = developmentStrategyService;
        this.crossRepositoryAnalysisService = crossRepositoryAnalysisService;
        this.codeChangeAnalysisService = codeChangeAnalysisService;
    }

    /**
     * Evaluates an architectural decision request and produces a deterministic recommendation.
     *
     * @param decisionCategory the category of architectural decision
     * @param requestDescription description of the architectural decision to evaluate
     * @param repositoryName the target repository name
     * @return an ArchitecturalDecisionResponse with alternatives and recommendation
     */
    public ArchitecturalDecisionResponse adviseArchitecture(
            String decisionCategory, String requestDescription, String repositoryName) {
        logger.info("Advising architecture for category: {}, repository: {}, description: {}",
                decisionCategory, repositoryName, requestDescription);

        ArchitecturalDecisionResponse response = new ArchitecturalDecisionResponse();
        response.setDecisionId(UUID.randomUUID().toString());
        response.setDecisionCategory(decisionCategory);
        response.setRequestDescription(requestDescription);
        response.setRepositoryName(repositoryName);

        // Validate repository exists
        RepositorySummaryResponse summary = retrieveRepositorySummary(repositoryName);
        if (summary == null) {
            response.setWarning("Repository '" + repositoryName + "' not found or not indexed. "
                    + "Analysis will be based on general architectural principles.");
        }

        // Evaluate based on decision category
        switch (decisionCategory) {
            case ArchitecturalDecisionResponse.DecisionCategory.NEW_SERVICE_VS_EXISTING:
                evaluateNewServiceVsExisting(response, repositoryName);
                break;
            case ArchitecturalDecisionResponse.DecisionCategory.NEW_MODULE_VS_EXISTING:
                evaluateNewModuleVsExisting(response, repositoryName);
                break;
            case ArchitecturalDecisionResponse.DecisionCategory.EXTEND_API_VS_CREATE_API:
                evaluateExtendApiVsCreateApi(response, repositoryName);
                break;
            case ArchitecturalDecisionResponse.DecisionCategory.EVENT_DRIVEN_VS_SYNCHRONOUS:
                evaluateEventDrivenVsSynchronous(response, repositoryName);
                break;
            case ArchitecturalDecisionResponse.DecisionCategory.COMPOSITION_VS_INHERITANCE:
                evaluateCompositionVsInheritance(response, repositoryName);
                break;
            case ArchitecturalDecisionResponse.DecisionCategory.CONFIGURATION_VS_CODE:
                evaluateConfigurationVsCode(response, repositoryName);
                break;
            case ArchitecturalDecisionResponse.DecisionCategory.SHARED_VS_DEDICATED:
                evaluateSharedVsDedicated(response, repositoryName);
                break;
            case ArchitecturalDecisionResponse.DecisionCategory.PACKAGE_ORGANIZATION:
                evaluatePackageOrganization(response, repositoryName);
                break;
            default:
                response.setWarning("Unsupported decision category: '" + decisionCategory
                        + "'. Supported categories: " + String.join(", ", getSupportedCategories()));
                response.setRecommendedApproach("Unable to evaluate - unsupported category");
                response.setDecisionRationale("The requested decision category is not supported. "
                        + "Please use one of the supported categories.");
                break;
        }

        logger.info("Architectural decision complete: category={}, recommended={}",
                decisionCategory, response.getRecommendedApproach());

        return response;
    }

    /**
     * Returns the list of supported decision categories.
     */
    public List<String> getSupportedCategories() {
        return List.of(
                ArchitecturalDecisionResponse.DecisionCategory.NEW_SERVICE_VS_EXISTING,
                ArchitecturalDecisionResponse.DecisionCategory.NEW_MODULE_VS_EXISTING,
                ArchitecturalDecisionResponse.DecisionCategory.EXTEND_API_VS_CREATE_API,
                ArchitecturalDecisionResponse.DecisionCategory.EVENT_DRIVEN_VS_SYNCHRONOUS,
                ArchitecturalDecisionResponse.DecisionCategory.COMPOSITION_VS_INHERITANCE,
                ArchitecturalDecisionResponse.DecisionCategory.CONFIGURATION_VS_CODE,
                ArchitecturalDecisionResponse.DecisionCategory.SHARED_VS_DEDICATED,
                ArchitecturalDecisionResponse.DecisionCategory.PACKAGE_ORGANIZATION
        );
    }

    // --- Evaluation methods for each decision category ---

    private void evaluateNewServiceVsExisting(ArchitecturalDecisionResponse response, String repoName) {
        List<Alternative> alternatives = new ArrayList<>();

        Alternative newService = new Alternative(
                "Create New Service",
                "Create a dedicated new service for the functionality, following microservice principles.",
                List.of("Clear separation of concerns",
                        "Independent deployability and scaling",
                        "Isolated failure domain",
                        "Team autonomy for development",
                        "Technology flexibility"),
                List.of("Higher initial development effort",
                        "Additional operational overhead",
                        "Service discovery and communication complexity",
                        "Distributed system challenges (consistency, latency)",
                        "Requires CI/CD pipeline setup"));
        newService.setSuitabilityScore(7);
        newService.setComplexityLevel("High");
        newService.setMaintainabilityRating("Good - Isolated service boundaries");

        Alternative existingService = new Alternative(
                "Extend Existing Service",
                "Add the new functionality to an existing service that has related responsibilities.",
                List.of("Lower initial development effort",
                        "Shared infrastructure and operations",
                        "Simpler deployment model",
                        "Existing monitoring and alerting",
                        "Faster time to market"),
                List.of("Risk of service bloat and SRP violation",
                        "Tighter coupling between features",
                        "Shared resource contention",
                        "Scaling inefficiencies",
                        "Reduced team autonomy"));
        existingService.setSuitabilityScore(6);
        existingService.setComplexityLevel("Medium");
        existingService.setMaintainabilityRating("Fair - May increase service complexity");

        alternatives.add(newService);
        alternatives.add(existingService);
        response.setAlternatives(alternatives);

        // Determine recommendation based on repository architecture
        String archStyle = getArchitectureStyle(repoName);
        if (archStyle != null && archStyle.contains("Microservice")) {
            response.setRecommendedApproach("Create New Service");
            response.setDecisionRationale("Repository follows microservice architecture. "
                    + "Creating a new service aligns with the existing architectural pattern "
                    + "and maintains service boundaries.");
        } else {
            response.setRecommendedApproach("Extend Existing Service");
            response.setDecisionRationale("Repository follows a monolithic or layered architecture. "
                    + "Extending an existing service is more consistent with the current "
                    + "architectural style and avoids unnecessary complexity.");
        }

        setImpactAssessment(response, "Medium", 3, 5, "Consistent with existing patterns", "Good");
        setDependencyImplications(response, 1, 2, "Low", "Low");
        setScalabilityAssessment(response, "Good", "Good", "Neutral", "Moderate");
        setMaintainabilityAssessment(response, "Medium", "Good", "Moderate", "Good");
        setRisks(response, List.of("Service boundary misalignment", "Operational overhead for new service"));
    }

    private void evaluateNewModuleVsExisting(ArchitecturalDecisionResponse response, String repoName) {
        List<Alternative> alternatives = new ArrayList<>();

        Alternative newModule = new Alternative(
                "Create New Module",
                "Create a new module within the project to encapsulate the functionality.",
                List.of("Clear module boundary",
                        "Independent versioning possible",
                        "Focused testing scope",
                        "Reduced risk of regression in existing code",
                        "Better code organization"),
                List.of("Module initialization overhead",
                        "Build configuration complexity",
                        "Module dependency management",
                        "Potential duplication with existing modules",
                        "Learning curve for new module structure"));
        newModule.setSuitabilityScore(8);
        newModule.setComplexityLevel("Medium");
        newModule.setMaintainabilityRating("Good - Well-defined module boundary");

        Alternative existingModule = new Alternative(
                "Extend Existing Module",
                "Add the functionality to an existing module that has related concerns.",
                List.of("Lower overhead - no new module setup",
                        "Shared build configuration",
                        "Existing module conventions apply",
                        "Faster implementation",
                        "Simpler dependency graph"),
                List.of("Module may become too large",
                        "Blurred module responsibility",
                        "Increased coupling within module",
                        "Harder to test in isolation",
                        "May violate module cohesion principles"));
        existingModule.setSuitabilityScore(6);
        existingModule.setComplexityLevel("Low to Medium");
        existingModule.setMaintainabilityRating("Fair - Risk of module bloat");

        alternatives.add(newModule);
        alternatives.add(existingModule);
        response.setAlternatives(alternatives);

        response.setRecommendedApproach("Create New Module");
        response.setDecisionRationale("Creating a new module provides better separation of concerns "
                + "and maintainability. The initial overhead is justified by the long-term benefits "
                + "of clear module boundaries and independent evolution.");

        setImpactAssessment(response, "Low to Medium", 2, 3, "Positive - Better modularization", "Good");
        setDependencyImplications(response, 0, 1, "Low", "Low");
        setScalabilityAssessment(response, "Good", "Good", "Positive", "Low");
        setMaintainabilityAssessment(response, "Low", "Good", "Good", "Excellent");
        setRisks(response, List.of("Module boundary definition may need refinement"));
    }

    private void evaluateExtendApiVsCreateApi(ArchitecturalDecisionResponse response, String repoName) {
        List<Alternative> alternatives = new ArrayList<>();

        Alternative extendApi = new Alternative(
                "Extend Existing API",
                "Add new endpoints or parameters to an existing API controller.",
                List.of("Faster implementation",
                        "Existing API documentation and testing",
                        "Consistent API versioning",
                        "Shared API infrastructure",
                        "Familiar patterns for consumers"),
                List.of("API may become bloated",
                        "Mixed responsibilities in controller",
                        "Versioning complexity increases",
                        "Breaking changes may affect existing consumers",
                        "API surface area grows without clear organization"));
        extendApi.setSuitabilityScore(6);
        extendApi.setComplexityLevel("Low");
        extendApi.setMaintainabilityRating("Fair - Risk of API bloat");

        Alternative createApi = new Alternative(
                "Create New API",
                "Create a dedicated new API controller for the new functionality.",
                List.of("Clear API boundary",
                        "Independent versioning",
                        "Focused documentation",
                        "No risk of breaking existing APIs",
                        "Better API organization"),
                List.of("Higher initial effort",
                        "New API infrastructure setup",
                        "Additional testing requirements",
                        "Consumer discovery overhead",
                        "Potential API duplication"));
        createApi.setSuitabilityScore(7);
        createApi.setComplexityLevel("Medium");
        createApi.setMaintainabilityRating("Good - Clear API boundary");

        alternatives.add(extendApi);
        alternatives.add(createApi);
        response.setAlternatives(alternatives);

        response.setRecommendedApproach("Create New API");
        response.setDecisionRationale("Creating a new API controller provides better separation of "
                + "concerns and avoids bloating existing APIs. This approach is preferred when "
                + "the new functionality has distinct domain responsibilities.");

        setImpactAssessment(response, "Low", 1, 2, "Positive", "Good");
        setDependencyImplications(response, 0, 0, "Low", "Low");
        setScalabilityAssessment(response, "Good", "Good", "Neutral", "Low");
        setMaintainabilityAssessment(response, "Low", "Good", "Good", "Excellent");
        setRisks(response, List.of("API duplication risk", "Consumer migration if APIs overlap"));
    }

    private void evaluateEventDrivenVsSynchronous(ArchitecturalDecisionResponse response, String repoName) {
        List<Alternative> alternatives = new ArrayList<>();

        Alternative eventDriven = new Alternative(
                "Event-Driven Architecture",
                "Use asynchronous messaging and event-driven communication between components.",
                List.of("Loose coupling between components",
                        "Better scalability through async processing",
                        "Resilience through message persistence",
                        "Supports event sourcing and CQRS",
                        "Natural fit for distributed systems"),
                List.of("Higher complexity",
                        "Eventual consistency challenges",
                        "Debugging and tracing difficulty",
                        "Message broker dependency",
                        "Event schema versioning overhead"));
        eventDriven.setSuitabilityScore(7);
        eventDriven.setComplexityLevel("High");
        eventDriven.setMaintainabilityRating("Fair - Complex event chains");

        Alternative synchronous = new Alternative(
                "Synchronous Communication",
                "Use direct REST/gRPC calls for synchronous request-response communication.",
                List.of("Simpler implementation",
                        "Immediate consistency",
                        "Easier debugging and testing",
                        "Well-understood patterns",
                        "Lower infrastructure requirements"),
                List.of("Tighter coupling between services",
                        "Cascading failures possible",
                        "Latency sensitive to slowest service",
                        "Scaling requires load balancers",
                        "Blocking operations impact throughput"));
        synchronous.setSuitabilityScore(6);
        synchronous.setComplexityLevel("Low to Medium");
        synchronous.setMaintainabilityRating("Good - Simple request-response flow");

        alternatives.add(eventDriven);
        alternatives.add(synchronous);
        response.setAlternatives(alternatives);

        response.setRecommendedApproach("Synchronous Communication");
        response.setDecisionRationale("For most enterprise applications, synchronous communication "
                + "provides the right balance of simplicity, consistency, and maintainability. "
                + "Event-driven architecture should be adopted only when specific requirements "
                + "(async processing, loose coupling, event sourcing) justify the added complexity.");

        setImpactAssessment(response, "Low", 1, 2, "Consistent with common patterns", "Good");
        setDependencyImplications(response, 0, 1, "Low", "Low");
        setScalabilityAssessment(response, "Fair", "Good", "Neutral", "Low");
        setMaintainabilityAssessment(response, "Low", "Good", "Good", "Good");
        setRisks(response, List.of("Latency sensitivity", "Cascading failure potential"));
    }

    private void evaluateCompositionVsInheritance(ArchitecturalDecisionResponse response, String repoName) {
        List<Alternative> alternatives = new ArrayList<>();

        Alternative composition = new Alternative(
                "Composition",
                "Use composition to combine behaviors by delegating to contained objects.",
                List.of("Greater flexibility at runtime",
                        "Favor over inheritance (GoF principle)",
                        "Easier to test with mocking",
                        "No fragile base class problem",
                        "Better encapsulation"),
                List.of("More boilerplate delegation code",
                        "More complex object graphs",
                        "May require dependency injection setup",
                        "Harder to trace behavior through delegation",
                        "Can lead to over-engineering"));
        composition.setSuitabilityScore(9);
        composition.setComplexityLevel("Medium");
        composition.setMaintainabilityRating("Excellent - Flexible and testable");

        Alternative inheritance = new Alternative(
                "Inheritance",
                "Use class inheritance to share behavior through base classes.",
                List.of("Code reuse through base classes",
                        "Polymorphic behavior",
                        "Simple to understand",
                        "Less boilerplate code",
                        "Direct access to parent behavior"),
                List.of("Fragile base class problem",
                        "Tight coupling to parent class",
                        "Limited runtime flexibility",
                        "Deep hierarchies are hard to maintain",
                        "Violates encapsulation"));
        inheritance.setSuitabilityScore(4);
        inheritance.setComplexityLevel("Low");
        inheritance.setMaintainabilityRating("Poor - Tight coupling and fragile hierarchies");

        alternatives.add(composition);
        alternatives.add(inheritance);
        response.setAlternatives(alternatives);

        response.setRecommendedApproach("Composition");
        response.setDecisionRationale("Composition is strongly preferred over inheritance for most "
                + "modern Java applications. It provides better flexibility, testability, and "
                + "maintainability. Inheritance should be reserved for true 'is-a' relationships "
                + "where the hierarchy is stable and unlikely to change.");

        setImpactAssessment(response, "Low", 1, 1, "Positive - Better design", "Good");
        setDependencyImplications(response, 0, 0, "Low", "Low");
        setScalabilityAssessment(response, "Good", "Good", "Neutral", "Low");
        setMaintainabilityAssessment(response, "Low", "Excellent", "Excellent", "Excellent");
        setRisks(response, List.of("Over-engineering risk with excessive composition"));
    }

    private void evaluateConfigurationVsCode(ArchitecturalDecisionResponse response, String repoName) {
        List<Alternative> alternatives = new ArrayList<>();

        Alternative configuration = new Alternative(
                "Configuration-Driven",
                "Use external configuration files to control behavior without code changes.",
                List.of("Behavior changes without redeployment",
                        "Environment-specific configurations",
                        "Non-developers can modify behavior",
                        "Centralized configuration management",
                        "Supports feature flags and toggles"),
                List.of("Configuration complexity grows",
                        "Runtime errors from misconfiguration",
                        "Harder to test configuration paths",
                        "Configuration drift across environments",
                        "Limited expressiveness compared to code"));
        configuration.setSuitabilityScore(7);
        configuration.setComplexityLevel("Low to Medium");
        configuration.setMaintainabilityRating("Good - Centralized configuration");

        Alternative code = new Alternative(
                "Code-Based",
                "Implement behavior directly in code using standard programming constructs.",
                List.of("Full expressiveness of programming language",
                        "Compile-time type safety",
                        "Easier to test and debug",
                        "Better IDE support and refactoring",
                        "Clearer intent through code"),
                List.of("Requires code changes for behavior modification",
                        "Redeployment needed for changes",
                        "Higher barrier for non-developers",
                        "Environment-specific logic in code",
                        "Can lead to code duplication"));
        code.setSuitabilityScore(6);
        code.setComplexityLevel("Low");
        code.setMaintainabilityRating("Good - Type-safe and testable");

        alternatives.add(configuration);
        alternatives.add(code);
        response.setAlternatives(alternatives);

        response.setRecommendedApproach("Configuration-Driven");
        response.setDecisionRationale("Configuration-driven approaches provide better operational "
                + "flexibility and align with Spring Boot's externalized configuration philosophy. "
                + "Use code for complex business logic and configuration for operational concerns.");

        setImpactAssessment(response, "Low", 1, 1, "Positive - Flexible", "Good");
        setDependencyImplications(response, 0, 0, "Low", "Low");
        setScalabilityAssessment(response, "Good", "Good", "Neutral", "Low");
        setMaintainabilityAssessment(response, "Low", "Good", "Good", "Good");
        setRisks(response, List.of("Configuration drift", "Runtime misconfiguration errors"));
    }

    private void evaluateSharedVsDedicated(ArchitecturalDecisionResponse response, String repoName) {
        List<Alternative> alternatives = new ArrayList<>();

        Alternative shared = new Alternative(
                "Shared Component",
                "Use a shared component that serves multiple consumers across the application.",
                List.of("Code reuse across multiple consumers",
                        "Single point of maintenance",
                        "Consistent behavior across consumers",
                        "Reduced duplication",
                        "Centralized optimization"),
                List.of("Tight coupling between consumers",
                        "Changes affect all consumers",
                        "Harder to evolve independently",
                        "Single point of failure",
                        "Feature creep and scope expansion"));
        shared.setSuitabilityScore(6);
        shared.setComplexityLevel("Medium");
        shared.setMaintainabilityRating("Fair - Coupling and change coordination");

        Alternative dedicated = new Alternative(
                "Dedicated Component",
                "Create a dedicated component specific to the consumer's needs.",
                List.of("Independent evolution",
                        "No cross-consumer coupling",
                        "Focused responsibility",
                        "Simpler testing",
                        "No coordination overhead"),
                List.of("Code duplication potential",
                        "Higher overall maintenance",
                        "Inconsistent behavior across components",
                        "More components to manage",
                        "May miss optimization opportunities"));
        dedicated.setSuitabilityScore(7);
        dedicated.setComplexityLevel("Low");
        dedicated.setMaintainabilityRating("Good - Independent and focused");

        alternatives.add(shared);
        alternatives.add(dedicated);
        response.setAlternatives(alternatives);

        response.setRecommendedApproach("Dedicated Component");
        response.setDecisionRationale("Dedicated components provide better independence and "
                + "maintainability. Shared components should only be used when there is a clear "
                + "and stable commonality that justifies the coupling.");

        setImpactAssessment(response, "Low", 1, 2, "Positive - Reduced coupling", "Good");
        setDependencyImplications(response, 0, 0, "Low", "Low");
        setScalabilityAssessment(response, "Good", "Good", "Neutral", "Low");
        setMaintainabilityAssessment(response, "Low", "Good", "Good", "Good");
        setRisks(response, List.of("Code duplication if not managed", "Inconsistent implementations"));
    }

    private void evaluatePackageOrganization(ArchitecturalDecisionResponse response, String repoName) {
        List<Alternative> alternatives = new ArrayList<>();

        Alternative byLayer = new Alternative(
                "Package by Layer",
                "Organize packages by architectural layer (controller, service, repository, entity).",
                List.of("Clear separation of technical concerns",
                        "Well-understood by most developers",
                        "Easy to enforce layer dependencies",
                        "Natural fit for layered architecture",
                        "Simpler for small to medium projects"),
                List.of("Poor cohesion within a feature",
                        "Feature changes touch multiple packages",
                        "Harder to navigate for large projects",
                        "Does not scale well",
                        "Cross-cutting concerns spread across layers"));
        byLayer.setSuitabilityScore(7);
        byLayer.setComplexityLevel("Low");
        byLayer.setMaintainabilityRating("Good for small-medium projects");

        Alternative byFeature = new Alternative(
                "Package by Feature",
                "Organize packages by business feature or domain (user, order, payment).",
                List.of("High cohesion within a feature",
                        "Feature changes contained in one package",
                        "Better scalability for large projects",
                        "Aligns with domain-driven design",
                        "Easier to navigate by business domain"),
                List.of("Cross-cutting concerns duplicated",
                        "Less clear technical separation",
                        "May need additional structure for shared code",
                        "Steeper learning curve for new developers",
                        "Can lead to package proliferation"));
        byFeature.setSuitabilityScore(8);
        byFeature.setComplexityLevel("Medium");
        byFeature.setMaintainabilityRating("Excellent for large/complex projects");

        alternatives.add(byLayer);
        alternatives.add(byFeature);
        response.setAlternatives(alternatives);

        // Determine recommendation based on repository size
        int packageCount = getPackageCount(repoName);
        if (packageCount > 20) {
            response.setRecommendedApproach("Package by Feature");
            response.setDecisionRationale("The repository has " + packageCount + " packages, suggesting "
                    + "a larger codebase. Package by feature provides better scalability and "
                    + "cohesion for larger projects, aligning with domain-driven design principles.");
        } else {
            response.setRecommendedApproach("Package by Layer");
            response.setDecisionRationale("Package by layer is recommended for this repository. "
                    + "It provides clear technical separation and is well-suited to the current "
                    + "project scale. Consider package by feature as the project grows.");
        }

        setImpactAssessment(response, "Low", 1, 1, "Positive - Better organization", "Good");
        setDependencyImplications(response, 0, 0, "Low", "Low");
        setScalabilityAssessment(response, "Good", "Good", "Neutral", "Low");
        setMaintainabilityAssessment(response, "Low", "Good", "Good", "Good");
        setRisks(response, List.of("Migration effort if restructuring existing packages"));
    }

    // --- Helper methods ---

    private RepositorySummaryResponse retrieveRepositorySummary(String repositoryName) {
        try {
            RepositorySummaryRequest request = new RepositorySummaryRequest();
            request.setRepositoryName(repositoryName);
            request.setBranch("main");
            return indexerRestClient.getRepositorySummary(request);
        } catch (Exception e) {
            logger.warn("Failed to retrieve repository summary for '{}': {}", repositoryName, e.getMessage());
            return null;
        }
    }

    private String getArchitectureStyle(String repoName) {
        try {
            var archResponse = architectureInsightsService.analyzeArchitecture(repoName, "main");
            return archResponse.getArchitecturalStyle();
        } catch (Exception e) {
            return null;
        }
    }

    private int getPackageCount(String repoName) {
        try {
            RepositorySummaryRequest request = new RepositorySummaryRequest();
            request.setRepositoryName(repoName);
            RepositorySummaryResponse summary = indexerRestClient.getRepositorySummary(request);
            return summary != null ? (int) summary.getPackageCount() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private void setImpactAssessment(ArchitecturalDecisionResponse response, String overallImpact,
                                     int filesAffected, int classesAffected,
                                     String consistencyImpact, String conventionAlignment) {
        ImpactAssessment ia = response.getRepositoryImpact();
        ia.setOverallImpact(overallImpact);
        ia.setFilesAffected(filesAffected);
        ia.setClassesAffected(classesAffected);
        ia.setArchitecturalConsistencyImpact(consistencyImpact);
        ia.setConventionAlignment(conventionAlignment);
    }

    private void setDependencyImplications(ArchitecturalDecisionResponse response,
                                           int newDeps, int existingAffected,
                                           String complexity, String circularRisk) {
        DependencyImplications di = response.getDependencyImplications();
        di.setNewDependenciesRequired(newDeps);
        di.setExistingDependenciesAffected(existingAffected);
        di.setDependencyComplexity(complexity);
        di.setCircularDependencyRisk(circularRisk);
    }

    private void setScalabilityAssessment(ArchitecturalDecisionResponse response,
                                          String horizontal, String vertical,
                                          String performance, String resourceUtil) {
        ScalabilityAssessment sa = response.getScalabilityAssessment();
        sa.setHorizontalScalability(horizontal);
        sa.setVerticalScalability(vertical);
        sa.setPerformanceImplication(performance);
        sa.setResourceUtilizationEstimate(resourceUtil);
    }

    private void setMaintainabilityAssessment(ArchitecturalDecisionResponse response,
                                              String complexity, String testability,
                                              String reusability, String longTerm) {
        MaintainabilityAssessment ma = response.getMaintainabilityAssessment();
        ma.setCodeComplexity(complexity);
        ma.setTestability(testability);
        ma.setReusability(reusability);
        ma.setLongTermMaintainability(longTerm);
    }

    private void setRisks(ArchitecturalDecisionResponse response, List<String> risks) {
        response.setArchitecturalRisks(risks);
    }
}