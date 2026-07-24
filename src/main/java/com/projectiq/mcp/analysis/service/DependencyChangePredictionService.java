package com.projectiq.mcp.analysis.service;

import com.projectiq.mcp.analysis.dto.DependencyChangePredictionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service that performs deterministic Intelligent Dependency Change Prediction.
 * Evaluates the downstream impact of introducing, removing, or modifying project
 * dependencies using repository intelligence, architecture analysis, and
 * dependency relationships. Produces a deterministic dependency impact report
 * before any implementation begins.
 *
 * <p>This service reuses {@link CodeChangeAnalysisService} patterns for consistency
 * and relies on deterministic rule-based analysis. All outputs are stable,
 * predictable, and free of AI/LLM reasoning.</p>
 *
 * <p>This service NEVER generates code, modifies the repository, performs
 * git operations, or uses any AI/LLM reasoning.</p>
 */
@Service
public class DependencyChangePredictionService {

    private static final Logger logger = LoggerFactory.getLogger(DependencyChangePredictionService.class);

    // --- Prediction Categories ---

    private static final List<String> PREDICTION_CATEGORIES = List.of(
            "Build Dependencies",
            "Runtime Dependencies",
            "Spring Dependencies",
            "Database Drivers",
            "Testing Libraries",
            "Logging Frameworks",
            "Security Libraries",
            "Third-Party Frameworks"
    );

    // --- Change Types ---

    private static final String CHANGE_TYPE_ADD = "ADD";
    private static final String CHANGE_TYPE_REMOVE = "REMOVE";
    private static final String CHANGE_TYPE_UPGRADE = "UPGRADE";
    private static final String CHANGE_TYPE_DOWNGRADE = "DOWNGRADE";
    private static final String CHANGE_TYPE_MODIFY = "MODIFY";

    // --- Migration effort estimates ---

    private static final String EFFORT_LOW = "LOW";
    private static final String EFFORT_MEDIUM = "MEDIUM";
    private static final String EFFORT_HIGH = "HIGH";

    // --- Deterministic patterns for dependency name extraction ---

    private static final Pattern DEPENDENCY_PATTERN = Pattern.compile(
            "\\b([a-zA-Z][a-zA-Z0-9_.-]+:[a-zA-Z][a-zA-Z0-9_.-]+)\\b"
    );

    private static final Pattern VERSION_PATTERN = Pattern.compile(
            "\\b(\\d+\\.\\d+\\.\\d+(?:\\.\\w+)?(?:-[A-Za-z]+)?)\\b"
    );

    // --- Category keyword maps for deterministic classification ---

    private static final Map<String, List<String>> CATEGORY_KEYWORDS = buildCategoryKeywords();
    private static final Map<String, List<String>> CATEGORY_MODULES = buildCategoryModules();
    private static final Map<String, List<String>> CATEGORY_SERVICES = buildCategoryServices();
    private static final Map<String, List<String>> CATEGORY_TRANSITIVE_EFFECTS = buildTransitiveEffects();
    private static final Map<String, List<String>> CATEGORY_COMPATIBILITY_RISKS = buildCompatibilityRisks();
    private static final Map<String, List<String>> CATEGORY_BUILD_RISKS = buildBuildRisks();
    private static final Map<String, List<String>> CATEGORY_TESTING_IMPACT = buildTestingImpact();
    private static final Map<String, List<String>> CATEGORY_MIGRATION_RECOMMENDATIONS = buildMigrationRecommendations();
    private static final Map<String, List<String>> CATEGORY_VALIDATION_CHECKLIST = buildValidationChecklist();
    private static final Map<String, String> CATEGORY_EFFORT = buildCategoryEffort();

    // --- Known circular dependency pairs (deterministic) ---

    private static final Set<String> KNOWN_CIRCULAR_PATTERNS = Set.of(
            "spring-boot-starter-web:spring-boot-starter-tomcat",
            "hibernate-core:javax.persistence-api",
            "jackson-core:jackson-databind",
            "log4j:log4j-core",
            "slf4j-api:logback-classic",
            "spring-security-core:spring-security-config"
    );

    // --- Known dependency categories for deterministic classification ---

    private static final List<String> MAVEN_BUILD_KEYWORDS = List.of(
            "maven", "build", "plugin", "compiler", "surefire", "failsafe",
            "shade", "assembly", "war", "jar", "source", "javadoc", "gpg",
            "nexus", "deploy", "release", "checkstyle", "pmd", "spotbugs",
            "jacoco", "cobertura", "coveralls", "sonar"
    );

    private static final List<String> SPRING_KEYWORDS = List.of(
            "spring", "spring-boot", "spring-cloud", "spring-security",
            "spring-data", "spring-web", "spring-mvc", "spring-jpa",
            "spring-test", "spring-actuator", "spring-kafka",
            "spring-rabbit", "spring-integration", "spring-batch"
    );

    private static final List<String> DATABASE_KEYWORDS = List.of(
            "jdbc", "mysql", "postgresql", "h2", "h2database", "hibernate",
            "jpa", "flyway", "liquibase", "mongodb", "redis", "cassandra",
            "elasticsearch", "oracle", "sqlserver", "mariadb", "sqlite",
            "r2dbc", "jooq", "mybatis", "dynamodb"
    );

    private static final List<String> TESTING_KEYWORDS = List.of(
            "junit", "testng", "mockito", "assertj", "hamcrest", "selenium",
            "cucumber", "spock", "testcontainers", "wiremock", "rest-assured",
            "spring-test", "archunit", "pact", "karate", "gatling", "jmeter"
    );

    private static final List<String> LOGGING_KEYWORDS = List.of(
            "log4j", "logback", "slf4j", "logging", "logstash", "fluentd",
            "log4j2", "tinylog", "java-util-logging"
    );

    private static final List<String> SECURITY_KEYWORDS = List.of(
            "spring-security", "oauth", "jwt", "jjwt", "keycloak", "bcrypt",
            "shiro", "pac4j", "caffeine", "bouncycastle", "tink", "vault"
    );

    public DependencyChangePredictionService() {
        // No dependencies needed - fully deterministic service
    }

    /**
     * Predicts the downstream impact of a proposed dependency change.
     * Analyzes the dependency name, change type, current and new versions,
     * and produces a comprehensive deterministic dependency impact report.
     *
     * @param dependencyName  the name of the dependency (e.g., "com.example:my-lib")
     * @param changeType      the type of change (ADD, REMOVE, UPGRADE, DOWNGRADE, MODIFY)
     * @param currentVersion  the current version (optional, may be null for ADD)
     * @param newVersion      the new/target version (optional, may be null for REMOVE)
     * @param repositoryName  the repository name (optional, defaults to "unknown")
     * @return a structured dependency change prediction response
     * @throws IllegalArgumentException if dependency name is null or empty
     */
    public DependencyChangePredictionResponse predictDependencyChange(
            String dependencyName, String changeType,
            String currentVersion, String newVersion,
            String repositoryName) {

        // Validate required inputs
        if (dependencyName == null || dependencyName.trim().isEmpty()) {
            throw new IllegalArgumentException("Dependency name cannot be null or empty");
        }

        if (changeType == null || changeType.trim().isEmpty()) {
            throw new IllegalArgumentException("Change type cannot be null or empty");
        }

        String normalizedName = dependencyName.trim().toLowerCase();
        String trimmedChangeType = changeType.trim();

        // Validate change type BEFORE normalization
        if (!isValidChangeTypeRaw(trimmedChangeType)) {
            throw new IllegalArgumentException("Invalid change type: " + changeType
                    + ". Valid types: ADD, REMOVE, UPGRADE, DOWNGRADE, MODIFY");
        }

        String normalizedChangeType = normalizeChangeType(trimmedChangeType);

        logger.info("Predicting dependency change: {} {} {} -> {} in repository: {}",
                normalizedChangeType, normalizedName,
                currentVersion != null ? currentVersion : "none",
                newVersion != null ? newVersion : "none",
                repositoryName != null ? repositoryName : "unknown");

        // Build response
        DependencyChangePredictionResponse response = new DependencyChangePredictionResponse();

        // Set base information
        String effectiveRepositoryName = (repositoryName != null && !repositoryName.trim().isEmpty())
                ? repositoryName.trim() : "unknown";

        response.setProposedDependencyChange(normalizedChangeType + " " + dependencyName.trim());
        response.setDependencyName(dependencyName.trim());
        response.setChangeType(normalizedChangeType);
        response.setCurrentVersion(currentVersion);
        response.setNewVersion(newVersion);

        // Classify the dependency into a prediction category
        String category = classifyDependency(normalizedName, normalizedChangeType);
        response.setPredictionCategory(category);

        // Detect circular dependency patterns
        boolean circularDetected = detectCircularDependency(normalizedName, normalizedChangeType);
        response.setCircularDependencyDetected(circularDetected);

        // Determine impacted modules
        response.setImpactedModules(determineImpactedModules(category, normalizedName, normalizedChangeType));

        // Determine impacted services
        response.setImpactedServices(determineImpactedServices(category, normalizedName, normalizedChangeType));

        // Determine transitive dependency effects
        response.setTransitiveDependencyEffects(determineTransitiveEffects(category, normalizedName, normalizedChangeType));

        // Determine compatibility risks
        response.setCompatibilityRisks(determineCompatibilityRisks(category, normalizedName, normalizedChangeType, currentVersion, newVersion));

        // Determine build risks
        response.setBuildRisks(determineBuildRisks(category, normalizedName, normalizedChangeType));

        // Determine testing impact
        response.setTestingImpact(determineTestingImpact(category, normalizedName, normalizedChangeType));

        // Determine migration recommendations
        response.setMigrationRecommendations(determineMigrationRecommendations(category, normalizedName, normalizedChangeType));

        // Determine migration effort estimate
        response.setMigrationEffortEstimate(determineMigrationEffort(category, normalizedName, normalizedChangeType));

        // Generate suggested validation checklist
        response.setSuggestedValidationChecklist(generateValidationChecklist(
                category, normalizedName, normalizedChangeType, currentVersion, newVersion));

        logger.info("Dependency change prediction complete: name={}, type={}, category={}, effort={}",
                dependencyName, normalizedChangeType, category, response.getMigrationEffortEstimate());

        return response;
    }

    /**
     * Predicts dependency change from a full change description string.
     * Parses the description to extract dependency name, change type, and versions.
     *
     * @param changeDescription the natural language dependency change description
     * @param repositoryName    the repository name (optional)
     * @return a structured dependency change prediction response
     * @throws IllegalArgumentException if change description is null or empty,
     *                                  or if dependency name cannot be extracted
     */
    public DependencyChangePredictionResponse predictDependencyChangeFromDescription(
            String changeDescription, String repositoryName) {

        if (changeDescription == null || changeDescription.trim().isEmpty()) {
            throw new IllegalArgumentException("Change description cannot be null or empty");
        }

        logger.info("Predicting dependency change from description: {}", changeDescription);

        String normalizedDesc = changeDescription.trim().toLowerCase();

        // Extract dependency name
        String dependencyName = extractDependencyName(changeDescription.trim());
        if (dependencyName == null) {
            throw new IllegalArgumentException("Unable to extract dependency name from: " + changeDescription);
        }

        // Determine change type from description
        String changeType = determineChangeTypeFromDescription(normalizedDesc);

        // Extract versions
        String currentVersion = extractCurrentVersion(changeDescription.trim(), normalizedDesc);
        String newVersion = extractNewVersion(changeDescription.trim(), normalizedDesc);

        return predictDependencyChange(dependencyName, changeType, currentVersion, newVersion, repositoryName);
    }

    // --- Dependency name extraction ---

    /**
     * Extracts a Maven-style dependency name (groupId:artifactId) from text.
     */
    String extractDependencyName(String text) {
        Matcher matcher = DEPENDENCY_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }

        // Fallback: look for common dependency name patterns
        String[] words = text.split("[\\s,;]+");
        for (String word : words) {
            String trimmed = word.trim().replaceAll("[\"'(),]", "");
            if (trimmed.contains(":") && trimmed.split(":").length == 2) {
                return trimmed;
            }
        }

        return null;
    }

    /**
     * Determines the change type from a natural language description.
     */
    String determineChangeTypeFromDescription(String normalizedDesc) {
        if (containsAny(normalizedDesc, "add", "adding", "introduce", "new dependency", "include")) {
            return CHANGE_TYPE_ADD;
        } else if (containsAny(normalizedDesc, "remove", "removing", "delete", "drop")) {
            return CHANGE_TYPE_REMOVE;
        } else if (containsAny(normalizedDesc, "upgrade", "upgrading", "update", "bump", "increase version")) {
            return CHANGE_TYPE_UPGRADE;
        } else if (containsAny(normalizedDesc, "downgrade", "downgrading", "rollback", "revert version")) {
            return CHANGE_TYPE_DOWNGRADE;
        } else if (containsAny(normalizedDesc, "modify", "change", "replace", "swap", "switch")) {
            return CHANGE_TYPE_MODIFY;
        } else {
            return CHANGE_TYPE_MODIFY;
        }
    }

    /**
     * Extracts the current (from) version from text.
     */
    String extractCurrentVersion(String originalText, String normalizedDesc) {
        // Look for patterns like "from X.Y.Z" or "current version X.Y.Z"
        java.util.regex.Matcher fromMatcher = java.util.regex.Pattern.compile(
                "(?:from|current|existing|old)\\s+version\\s+(\\d+\\.\\d+\\.\\d+(?:\\.\\w+)?(?:-[A-Za-z]+)?)"
        ).matcher(normalizedDesc);
        if (fromMatcher.find()) {
            return fromMatcher.group(1);
        }

        // Look for "from X.Y.Z" with explicit word
        fromMatcher = java.util.regex.Pattern.compile(
                "\\bfrom\\s+(\\d+\\.\\d+\\.\\d+(?:\\.\\w+)?(?:-[A-Za-z]+)?)"
        ).matcher(normalizedDesc);
        if (fromMatcher.find()) {
            return fromMatcher.group(1);
        }

        // Look for version pattern with "->" or "to" where it's the first version
        String[] parts = normalizedDesc.split("\\s+->\\s+|\\s+to\\s+");
        if (parts.length >= 2) {
            Matcher versionMatcher = VERSION_PATTERN.matcher(parts[0]);
            if (versionMatcher.find()) {
                return versionMatcher.group(1);
            }
        }

        return null;
    }

    /**
     * Extracts the new (to) version from text.
     */
    String extractNewVersion(String originalText, String normalizedDesc) {
        // Look for patterns like "to X.Y.Z" or "new version X.Y.Z"
        java.util.regex.Matcher toMatcher = java.util.regex.Pattern.compile(
                "(?:to|new|target|updated)\\s+version\\s+(\\d+\\.\\d+\\.\\d+(?:\\.\\w+)?(?:-[A-Za-z]+)?)"
        ).matcher(normalizedDesc);
        if (toMatcher.find()) {
            return toMatcher.group(1);
        }

        // Look for "to X.Y.Z" with explicit word
        toMatcher = java.util.regex.Pattern.compile(
                "\\bto\\s+(\\d+\\.\\d+\\.\\d+(?:\\.\\w+)?(?:-[A-Za-z]+)?)"
        ).matcher(normalizedDesc);
        if (toMatcher.find()) {
            return toMatcher.group(1);
        }

        // Look for version pattern with "->" or "to" where it's the second version
        String[] parts = normalizedDesc.split("\\s+->\\s+|\\s+to\\s+");
        if (parts.length >= 2) {
            Matcher versionMatcher = VERSION_PATTERN.matcher(parts[1]);
            if (versionMatcher.find()) {
                return versionMatcher.group(1);
            }
        }

        return null;
    }

    // --- Classification logic ---

    /**
     * Classifies a dependency into a prediction category based on its name and type.
     * Security Libraries are checked before Spring Dependencies to ensure
     * spring-security dependencies are correctly classified as Security Libraries.
     */
    String classifyDependency(String normalizedName, String changeType) {
        // Check Security Libraries FIRST (before Spring) to catch spring-security-* dependencies
        if (containsAny(normalizedName, SECURITY_KEYWORDS.toArray(new String[0]))) {
            return "Security Libraries";
        }

        // Check each category's keywords against the dependency name
        for (Map.Entry<String, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            if (containsAny(normalizedName, entry.getValue().toArray(new String[0]))) {
                return entry.getKey();
            }
        }

        // Additional deterministic classification based on name patterns
        if (normalizedName.startsWith("org.springframework") || normalizedName.startsWith("org.spring")) {
            return "Spring Dependencies";
        }

        if (normalizedName.contains("test") || normalizedName.contains("mock")
                || normalizedName.contains("assert") || normalizedName.contains("junit")) {
            return "Testing Libraries";
        }

        if (normalizedName.contains("log") || normalizedName.contains("log4j")
                || normalizedName.contains("slf4j") || normalizedName.contains("logback")) {
            return "Logging Frameworks";
        }

        if (normalizedName.contains("jdbc") || normalizedName.contains("driver")
                || normalizedName.contains("database") || normalizedName.contains("db")) {
            return "Database Drivers";
        }

        if (normalizedName.contains("maven") || normalizedName.contains("plugin")
                || normalizedName.contains("build")) {
            return "Build Dependencies";
        }

        // Default for dependencies that don't match specific categories
        return "Third-Party Frameworks";
    }

    /**
     * Detects if the dependency change could introduce a circular dependency.
     */
    boolean detectCircularDependency(String normalizedName, String changeType) {
        if (CHANGE_TYPE_REMOVE.equals(changeType)) {
            return false; // Removing a dependency cannot introduce circular dependencies
        }

        // Check against known circular patterns
        for (String pattern : KNOWN_CIRCULAR_PATTERNS) {
            if (normalizedName.contains(pattern.split(":")[0])
                    || normalizedName.contains(pattern.split(":")[1])) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determines the modules impacted by the dependency change.
     */
    List<String> determineImpactedModules(String category, String normalizedName, String changeType) {
        Set<String> modules = new LinkedHashSet<>();

        // Add category-specific modules
        List<String> categoryModules = CATEGORY_MODULES.getOrDefault(category, List.of());
        modules.addAll(categoryModules);

        // Add type-specific modules
        switch (changeType) {
            case CHANGE_TYPE_ADD:
                modules.add("Build configuration (pom.xml / build.gradle)");
                modules.add("Module dependency graph");
                modules.add("Dependency resolution tree");
                break;
            case CHANGE_TYPE_REMOVE:
                modules.add("Build configuration (pom.xml / build.gradle)");
                modules.add("Code referencing the removed dependency");
                modules.add("Module dependency graph");
                break;
            case CHANGE_TYPE_UPGRADE:
            case CHANGE_TYPE_DOWNGRADE:
                modules.add("Build configuration (pom.xml / build.gradle)");
                modules.add("Module dependency graph");
                modules.add("Dependency resolution tree");
                modules.add("Transitive dependency chain");
                break;
            case CHANGE_TYPE_MODIFY:
                modules.add("Build configuration (pom.xml / build.gradle)");
                modules.add("Module dependency graph");
                modules.add("Dependency resolution tree");
                modules.add("Transitive dependency chain");
                break;
            default:
                break;
        }

        return new ArrayList<>(modules);
    }

    /**
     * Determines the services impacted by the dependency change.
     */
    List<String> determineImpactedServices(String category, String normalizedName, String changeType) {
        Set<String> services = new LinkedHashSet<>();

        // Add category-specific services
        List<String> categoryServices = CATEGORY_SERVICES.getOrDefault(category, List.of());
        services.addAll(categoryServices);

        // Add change-type-specific services
        if (CHANGE_TYPE_REMOVE.equals(changeType)) {
            services.add("All services consuming the removed dependency need refactoring");
        }

        if (CHANGE_TYPE_UPGRADE.equals(changeType) || CHANGE_TYPE_DOWNGRADE.equals(changeType)) {
            services.add("Services using the dependency may need recompilation");
            services.add("Services depending on specific API behavior may need updates");
        }

        return new ArrayList<>(services);
    }

    /**
     * Determines the transitive dependency effects.
     */
    List<String> determineTransitiveEffects(String category, String normalizedName, String changeType) {
        Set<String> effects = new LinkedHashSet<>();

        // Add category-specific effects
        List<String> categoryEffects = CATEGORY_TRANSITIVE_EFFECTS.getOrDefault(category, List.of());
        effects.addAll(categoryEffects);

        // Add change-type-specific effects
        switch (changeType) {
            case CHANGE_TYPE_ADD:
                effects.add("New transitive dependencies will be pulled in");
                effects.add("May introduce version conflicts with existing dependencies");
                effects.add("Dependency tree depth may increase");
                break;
            case CHANGE_TYPE_REMOVE:
                effects.add("Transitive dependencies of the removed dependency are no longer available");
                effects.add("Other dependencies relying on those transitive deps may break");
                effects.add("Dependency tree may be simplified");
                break;
            case CHANGE_TYPE_UPGRADE:
                effects.add("Newer version may introduce updated transitive dependencies");
                effects.add("Older transitive dependencies may be excluded due to version conflicts");
                effects.add("Dependency convergence may be affected in multi-module projects");
                break;
            case CHANGE_TYPE_DOWNGRADE:
                effects.add("Older version brings older transitive dependencies");
                effects.add("May resolve existing version conflicts but introduce new ones");
                effects.add("Dependency convergence may be affected in multi-module projects");
                break;
            case CHANGE_TYPE_MODIFY:
                effects.add("Changed dependency may pull different transitive dependencies");
                effects.add("Version resolution strategy may need updating");
                break;
            default:
                break;
        }

        return new ArrayList<>(effects);
    }

    /**
     * Determines the compatibility risks associated with the dependency change.
     */
    List<String> determineCompatibilityRisks(String category, String normalizedName, String changeType,
                                             String currentVersion, String newVersion) {
        Set<String> risks = new LinkedHashSet<>();

        // Add category-specific risks
        List<String> categoryRisks = CATEGORY_COMPATIBILITY_RISKS.getOrDefault(category, List.of());
        risks.addAll(categoryRisks);

        // Add change-type-specific risks
        switch (changeType) {
            case CHANGE_TYPE_ADD:
                risks.add("New dependency may conflict with existing dependency versions");
                risks.add("License compatibility with existing dependencies should be verified");
                risks.add("API style/patterns may differ from existing codebase conventions");
                break;
            case CHANGE_TYPE_REMOVE:
                risks.add("Code relying on the removed dependency will fail to compile");
                risks.add("Removal may break other modules using transitive APIs");
                risks.add("Removal may require significant refactoring of consuming code");
                break;
            case CHANGE_TYPE_UPGRADE:
                risks.add("API breaking changes in newer versions may affect consuming code");
                risks.add("Deprecated APIs removed in newer versions require code changes");
                risks.add("Behavioral changes in upgraded dependency may cause regressions");
                if (currentVersion != null && newVersion != null) {
                    risks.add("Major version jump (" + currentVersion + " -> " + newVersion
                            + ") indicates potential breaking changes");
                }
                break;
            case CHANGE_TYPE_DOWNGRADE:
                risks.add("Older versions may contain known security vulnerabilities");
                risks.add("Features used in current version may not exist in older version");
                risks.add("Bug fixes present in current version will be lost");
                break;
            case CHANGE_TYPE_MODIFY:
                risks.add("Different dependency may have incompatible APIs");
                risks.add("Different configuration/initialization may be required");
                risks.add("Behavioral differences between old and new dependency need evaluation");
                break;
            default:
                break;
        }

        return new ArrayList<>(risks);
    }

    /**
     * Determines the build risks associated with the dependency change.
     */
    List<String> determineBuildRisks(String category, String normalizedName, String changeType) {
        Set<String> risks = new LinkedHashSet<>();

        // Add category-specific build risks
        List<String> categoryRisks = CATEGORY_BUILD_RISKS.getOrDefault(category, List.of());
        risks.addAll(categoryRisks);

        // Add change-type-specific build risks
        switch (changeType) {
            case CHANGE_TYPE_ADD:
                risks.add("Build time may increase due to additional dependency resolution");
                risks.add("New dependency may increase final artifact size");
                risks.add("May introduce dependency convergence issues in multi-module builds");
                break;
            case CHANGE_TYPE_REMOVE:
                risks.add("Build may fail if other modules implicitly depend on this dependency");
                risks.add("Removal may reduce build time and artifact size");
                break;
            case CHANGE_TYPE_UPGRADE:
            case CHANGE_TYPE_DOWNGRADE:
                risks.add("Version conflict resolution may require dependency exclusions");
                risks.add("Maven/Gradle dependency mediation may select unexpected version");
                risks.add("Build reproducibility may be affected by transitive version changes");
                break;
            case CHANGE_TYPE_MODIFY:
                risks.add("New dependency may have different build lifecycle requirements");
                risks.add("Build plugins may need reconfiguration for the new dependency");
                break;
            default:
                break;
        }

        return new ArrayList<>(risks);
    }

    /**
     * Determines the testing impact of the dependency change.
     */
    List<String> determineTestingImpact(String category, String normalizedName, String changeType) {
        Set<String> impacts = new LinkedHashSet<>();

        // Add category-specific testing impact
        List<String> categoryImpact = CATEGORY_TESTING_IMPACT.getOrDefault(category, List.of());
        impacts.addAll(categoryImpact);

        // Add change-type-specific testing impact
        switch (changeType) {
            case CHANGE_TYPE_ADD:
                impacts.add("Verify new dependency works correctly in all environments");
                impacts.add("Test integration with existing code that uses the new dependency");
                if (!"Testing Libraries".equals(category)) {
                    impacts.add("Write integration tests for the new dependency");
                }
                break;
            case CHANGE_TYPE_REMOVE:
                impacts.add("Re-run all tests for modules that used the removed dependency");
                impacts.add("Verify no regressions from refactored code paths");
                break;
            case CHANGE_TYPE_UPGRADE:
                impacts.add("Run full test suite to detect regressions from API changes");
                impacts.add("Verify behavior of upgraded dependency matches expectations");
                impacts.add("Run performance benchmarks if applicable");
                break;
            case CHANGE_TYPE_DOWNGRADE:
                impacts.add("Re-run tests that depend on the affected version behavior");
                impacts.add("Verify downgraded version maintains required functionality");
                break;
            case CHANGE_TYPE_MODIFY:
                impacts.add("Full regression test suite required for dependency replacement");
                impacts.add("End-to-end tests needed for all affected workflows");
                break;
            default:
                break;
        }

        return new ArrayList<>(impacts);
    }

    /**
     * Determines the migration recommendations for the dependency change.
     */
    List<String> determineMigrationRecommendations(String category, String normalizedName, String changeType) {
        Set<String> recommendations = new LinkedHashSet<>();

        // Add category-specific recommendations
        List<String> categoryRecs = CATEGORY_MIGRATION_RECOMMENDATIONS.getOrDefault(category, List.of());
        recommendations.addAll(categoryRecs);

        // Add change-type-specific recommendations
        switch (changeType) {
            case CHANGE_TYPE_ADD:
                recommendations.add("Review dependency scope (compile, runtime, test, provided)");
                recommendations.add("Check for existing transitive availability before adding explicitly");
                if (!"Testing Libraries".equals(category)) {
                    recommendations.add("Consider adding dependency management entry for version control");
                }
                break;
            case CHANGE_TYPE_REMOVE:
                recommendations.add("Search codebase for all usages of the dependency before removal");
                recommendations.add("Ensure no other modules transitively rely on the dependency");
                recommendations.add("Update import statements and remove unused code");
                break;
            case CHANGE_TYPE_UPGRADE:
                recommendations.add("Review changelog/release notes for the new version");
                recommendations.add("Check for deprecated or removed APIs in the new version");
                recommendations.add("Consider upgrading gradually across modules if multi-module project");
                recommendations.add("Update dependency management entry to new version");
                break;
            case CHANGE_TYPE_DOWNGRADE:
                recommendations.add("Document the reason for downgrading");
                recommendations.add("Check for security patches that may be missing in older version");
                recommendations.add("Update dependency management entry to old version");
                break;
            case CHANGE_TYPE_MODIFY:
                recommendations.add("Research replacement dependency for API compatibility");
                recommendations.add("Plan phased migration to minimize disruption");
                recommendations.add("Consider keeping both dependencies during migration period");
                break;
            default:
                break;
        }

        return new ArrayList<>(recommendations);
    }

    /**
     * Determines the migration effort estimate for the dependency change.
     * Change type takes precedence over category for REMOVE and MODIFY operations.
     */
    String determineMigrationEffort(String category, String normalizedName, String changeType) {
        // REMOVE and MODIFY always have HIGH effort regardless of category
        if (CHANGE_TYPE_REMOVE.equals(changeType) || CHANGE_TYPE_MODIFY.equals(changeType)) {
            return EFFORT_HIGH;
        }

        // Check for category-specific effort override
        String categoryEffort = CATEGORY_EFFORT.get(category);
        if (categoryEffort != null && !categoryEffort.isEmpty()) {
            return categoryEffort;
        }

        // Determine effort based on change type
        switch (changeType) {
            case CHANGE_TYPE_ADD:
                if ("Testing Libraries".equals(category) || "Logging Frameworks".equals(category)) {
                    return EFFORT_LOW;
                }
                return EFFORT_MEDIUM;
            case CHANGE_TYPE_UPGRADE:
                if ("Testing Libraries".equals(category) || "Logging Frameworks".equals(category)) {
                    return EFFORT_LOW;
                }
                return EFFORT_MEDIUM;
            case CHANGE_TYPE_DOWNGRADE:
                return EFFORT_MEDIUM;
            default:
                return EFFORT_MEDIUM;
        }
    }

    /**
     * Generates a suggested validation checklist for the dependency change.
     */
    List<String> generateValidationChecklist(String category, String normalizedName, String changeType,
                                             String currentVersion, String newVersion) {
        Set<String> checklist = new LinkedHashSet<>();

        // Base checklist items (always included)
        checklist.add("[ ] Verify build compiles successfully after dependency change");
        checklist.add("[ ] Run full test suite and verify all tests pass");
        checklist.add("[ ] Verify dependency convergence in multi-module projects");

        // Add category-specific checklist items
        List<String> categoryChecklist = CATEGORY_VALIDATION_CHECKLIST.getOrDefault(category, List.of());
        checklist.addAll(categoryChecklist);

        // Add change-type-specific checklist items
        switch (changeType) {
            case CHANGE_TYPE_ADD:
                checklist.add("[ ] Verify new dependency is scanned for known vulnerabilities");
                checklist.add("[ ] Verify license compatibility with project license");
                checklist.add("[ ] Verify dependency scope is appropriate");
                checklist.add("[ ] Verify no duplicate dependencies with different versions");
                break;
            case CHANGE_TYPE_REMOVE:
                checklist.add("[ ] Verify no code references the removed dependency");
                checklist.add("[ ] Verify no transitive dependencies implicitly require it");
                checklist.add("[ ] Remove unused import statements in affected files");
                break;
            case CHANGE_TYPE_UPGRADE:
                checklist.add("[ ] Review API differences between versions");
                checklist.add("[ ] Verify deprecated API replacements are applied");
                checklist.add("[ ] Verify new version aligns with other dependency versions");
                if (currentVersion != null && newVersion != null) {
                    checklist.add("[ ] Verify major version migration guide is followed");
                }
                break;
            case CHANGE_TYPE_DOWNGRADE:
                checklist.add("[ ] Verify downgraded version still satisfies feature requirements");
                checklist.add("[ ] Check security advisory database for downgraded version");
                break;
            case CHANGE_TYPE_MODIFY:
                checklist.add("[ ] Verify new dependency provides equivalent functionality");
                checklist.add("[ ] Update all import statements to use new dependency packages");
                checklist.add("[ ] Verify configuration changes required by new dependency");
                break;
            default:
                break;
        }

        // Final verification
        checklist.add("[ ] Run dependency tree analysis to verify resolved versions");
        checklist.add("[ ] Verify in CI/CD pipeline before merging");

        return new ArrayList<>(checklist);
    }

    // --- Normalization helpers ---

    String normalizeChangeType(String changeType) {
        String upper = changeType.toUpperCase();
        switch (upper) {
            case "ADD":
            case "ADDING":
            case "NEW":
            case "INTRODUCE":
                return CHANGE_TYPE_ADD;
            case "REMOVE":
            case "REMOVING":
            case "DELETE":
            case "DROP":
                return CHANGE_TYPE_REMOVE;
            case "UPGRADE":
            case "UPGRADING":
            case "UPDATE":
            case "BUMP":
                return CHANGE_TYPE_UPGRADE;
            case "DOWNGRADE":
            case "DOWNGRADING":
            case "ROLLBACK":
                return CHANGE_TYPE_DOWNGRADE;
            case "MODIFY":
            case "MODIFYING":
            case "CHANGE":
            case "REPLACE":
            case "SWAP":
            case "SWITCH":
                return CHANGE_TYPE_MODIFY;
            default:
                return CHANGE_TYPE_MODIFY;
        }
    }

    boolean isValidChangeType(String changeType) {
        return CHANGE_TYPE_ADD.equals(changeType)
                || CHANGE_TYPE_REMOVE.equals(changeType)
                || CHANGE_TYPE_UPGRADE.equals(changeType)
                || CHANGE_TYPE_DOWNGRADE.equals(changeType)
                || CHANGE_TYPE_MODIFY.equals(changeType);
    }

    /**
     * Validates the raw change type string BEFORE normalization.
     * This ensures that truly invalid types throw exceptions rather than
     * being silently normalized to MODIFY.
     */
    boolean isValidChangeTypeRaw(String changeType) {
        String upper = changeType.toUpperCase();
        switch (upper) {
            case "ADD":
            case "ADDING":
            case "NEW":
            case "INTRODUCE":
            case "REMOVE":
            case "REMOVING":
            case "DELETE":
            case "DROP":
            case "UPGRADE":
            case "UPGRADING":
            case "UPDATE":
            case "BUMP":
            case "DOWNGRADE":
            case "DOWNGRADING":
            case "ROLLBACK":
            case "MODIFY":
            case "MODIFYING":
            case "CHANGE":
            case "REPLACE":
            case "SWAP":
            case "SWITCH":
                return true;
            default:
                return false;
        }
    }

    // --- Static data initializers ---

    private static Map<String, List<String>> buildCategoryKeywords() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("Build Dependencies", List.of(
                "maven", "plugin", "compiler", "surefire", "failsafe", "shade",
                "assembly", "jacoco", "sonar", "checkstyle", "pmd", "spotbugs"
        ));
        map.put("Spring Dependencies", List.of(
                "spring-boot", "spring-cloud", "spring-security", "spring-data",
                "spring-web", "spring-mvc", "spring-jpa", "spring-test",
                "spring-actuator", "spring-kafka", "spring-batch"
        ));
        map.put("Database Drivers", List.of(
                "mysql-connector", "postgresql", "h2database", "hibernate-core",
                "flyway", "liquibase", "mongodb-driver", "jedis", "lettuce"
        ));
        map.put("Testing Libraries", List.of(
                "junit", "testng", "mockito", "assertj", "hamcrest",
                "cucumber", "testcontainers", "wiremock", "rest-assured"
        ));
        map.put("Logging Frameworks", List.of(
                "log4j", "logback", "slf4j", "log4j2", "tinylog"
        ));
        map.put("Security Libraries", List.of(
                "spring-security", "oauth2", "jjwt", "keycloak", "bcrypt",
                "shiro", "bouncycastle"
        ));
        return map;
    }

    private static Map<String, List<String>> buildCategoryModules() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("Build Dependencies", List.of(
                "Build configuration module",
                "Root POM / Parent POM",
                "Build plugin management"
        ));
        map.put("Runtime Dependencies", List.of(
                "Core application module",
                "Runtime classpath configuration"
        ));
        map.put("Spring Dependencies", List.of(
                "Spring Boot auto-configuration",
                "Spring context initialization",
                "Bean definition module"
        ));
        map.put("Database Drivers", List.of(
                "Persistence layer module",
                "Data source configuration",
                "Database migration module"
        ));
        map.put("Testing Libraries", List.of(
                "Test configuration module",
                "Test resource setup"
        ));
        map.put("Logging Frameworks", List.of(
                "Logging configuration module",
                "Application startup configuration"
        ));
        map.put("Security Libraries", List.of(
                "Security configuration module",
                "Authentication/Authorization module",
                "Security filter chain"
        ));
        map.put("Third-Party Frameworks", List.of(
                "Core application module",
                "Integration configuration module",
                "External API integration module"
        ));
        return map;
    }

    private static Map<String, List<String>> buildCategoryServices() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("Build Dependencies", List.of(
                "Build pipeline (CI/CD)",
                "Release management service"
        ));
        map.put("Runtime Dependencies", List.of(
                "Application runtime service",
                "Class loading service"
        ));
        map.put("Spring Dependencies", List.of(
                "Spring context management",
                "Bean lifecycle management",
                "Dependency injection service"
        ));
        map.put("Database Drivers", List.of(
                "Data access service",
                "Connection pool management",
                "Database migration service"
        ));
        map.put("Testing Libraries", List.of(
                "Test execution service",
                "Test reporting service"
        ));
        map.put("Logging Frameworks", List.of(
                "Logging service",
                "Application monitoring service"
        ));
        map.put("Security Libraries", List.of(
                "Authentication service",
                "Authorization service",
                "Token management service"
        ));
        map.put("Third-Party Frameworks", List.of(
                "Integration service",
                "External API client service",
                "Data transformation service"
        ));
        return map;
    }

    private static Map<String, List<String>> buildTransitiveEffects() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("Build Dependencies", List.of(
                "May pull in additional build plugins transitively",
                "Build plugin version alignment across modules"
        ));
        map.put("Spring Dependencies", List.of(
                "Spring Boot starters pull in transitive auto-configuration",
                "May introduce conflicting bean definitions via auto-configuration",
                "Spring version alignment across all Spring modules required"
        ));
        map.put("Database Drivers", List.of(
                "Database drivers may pull in network/security libraries",
                "ORM frameworks bring JPA provider dependencies",
                "Connection pool libraries include their own dependencies"
        ));
        map.put("Testing Libraries", List.of(
                "Test runners may pull in assertion and reporting libraries",
                "Integration test libraries include test container dependencies"
        ));
        map.put("Logging Frameworks", List.of(
                "Logging implementations pull in SLF4J binding dependencies",
                "Multiple logging implementations may cause classpath conflicts"
        ));
        map.put("Security Libraries", List.of(
                "Security frameworks pull in cryptography and encoding libraries",
                "OAuth/OIDC clients bring HTTP client dependencies"
        ));
        map.put("Third-Party Frameworks", List.of(
                "Third-party frameworks often bring extensive transitive dependency trees",
                "May include version dependencies that conflict with existing libraries"
        ));
        return map;
    }

    private static Map<String, List<String>> buildCompatibilityRisks() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("Build Dependencies", List.of(
                "Build plugin version incompatibility with Maven/Gradle version",
                "Plugin API changes may break custom build configurations"
        ));
        map.put("Spring Dependencies", List.of(
                "Spring Boot version must align with Spring Cloud version",
                "Spring dependency versions must be compatible within the release train",
                "Auto-configuration class names may change between versions"
        ));
        map.put("Database Drivers", List.of(
                "Database driver version must be compatible with database server version",
                "ORM version must be compatible with JPA specification version",
                "Migration tool version must be compatible with database dialect"
        ));
        map.put("Testing Libraries", List.of(
                "Test framework version must be compatible with JUnit Platform version",
                "Mockito version compatibility with JUnit version"
        ));
        map.put("Logging Frameworks", List.of(
                "Multiple logging implementations on classpath cause binding conflicts",
                "Log4j-to-SLF4J bridge version must align with Log4j version"
        ));
        map.put("Security Libraries", List.of(
                "Security library version must be compatible with framework version",
                "Token format/validation may change between major versions"
        ));
        map.put("Third-Party Frameworks", List.of(
                "Third-party library may require specific framework version",
                "API compatibility may break between minor/patch versions"
        ));
        return map;
    }

    private static Map<String, List<String>> buildBuildRisks() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("Build Dependencies", List.of(
                "Wrong plugin version may break the entire build pipeline",
                "Plugin configuration may be incompatible with new version"
        ));
        map.put("Spring Dependencies", List.of(
                "Spring Boot version changes affect dependency management BOM",
                "Spring dependency version override may break managed dependencies"
        ));
        map.put("Database Drivers", List.of(
                "Database driver version mismatch may cause runtime connection failures",
                "Flyway/Liquibase version must be compatible with database and migration scripts"
        ));
        map.put("Testing Libraries", List.of(
                "Test library version conflicts may cause test execution failures",
                "Test framework upgrades may require configuration changes"
        ));
        map.put("Logging Frameworks", List.of(
                "SLF4J binding conflicts cause build warnings or failures",
                "Logging framework version changes may affect log output format"
        ));
        map.put("Security Libraries", List.of(
                "Security library upgrades may require configuration updates",
                "Token validation logic changes may break existing tokens"
        ));
        map.put("Third-Party Frameworks", List.of(
                "Framework version changes may require build tool configuration updates",
                "New dependencies may increase build time and artifact size"
        ));
        return map;
    }

    private static Map<String, List<String>> buildTestingImpact() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("Build Dependencies", List.of(
                "Plugin behavioral changes may affect test execution",
                "Code coverage tools may produce different results"
        ));
        map.put("Spring Dependencies", List.of(
                "Spring context test configuration may need updates",
                "Bean definition changes affect integration test setup"
        ));
        map.put("Database Drivers", List.of(
                "Database integration tests need verification with new driver",
                "Testcontainers configuration may need updates"
        ));
        map.put("Testing Libraries", List.of(
                "Test framework API changes may break existing test code",
                "Assertion libraries may have different method signatures"
        ));
        map.put("Logging Frameworks", List.of(
                "Log verification tests may need updates for format changes",
                "Log level configuration may behave differently"
        ));
        map.put("Security Libraries", List.of(
                "Security test fixtures may need updates",
                "Authentication/authorization test flows may break"
        ));
        map.put("Third-Party Frameworks", List.of(
                "Integration tests must be updated for API changes",
                "Mock setup may need changes for new library versions"
        ));
        return map;
    }

    private static Map<String, List<String>> buildMigrationRecommendations() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("Build Dependencies", List.of(
                "Review build plugin documentation for breaking changes",
                "Verify plugin configuration syntax compatibility",
                "Test build locally before committing changes"
        ));
        map.put("Spring Dependencies", List.of(
                "Review Spring Boot release notes for breaking changes",
                "Check Spring dependency version compatibility matrix",
                "Verify auto-configuration class names and package changes",
                "Test application context loading after dependency change"
        ));
        map.put("Database Drivers", List.of(
                "Verify database compatibility with new driver version",
                "Review ORM release notes for schema generation changes",
                "Test database migration scripts with new driver version"
        ));
        map.put("Testing Libraries", List.of(
                "Review test framework migration guide",
                "Update test configuration if required",
                "Run all existing tests to verify compatibility"
        ));
        map.put("Logging Frameworks", List.of(
                "Ensure only one SLF4J binding is on the classpath",
                "Verify log configuration file format compatibility",
                "Check for Log4j -> Log4j2 migration requirements"
        ));
        map.put("Security Libraries", List.of(
                "Review security advisory for the new version",
                "Verify security configuration compatibility",
                "Test authentication and authorization flows thoroughly"
        ));
        map.put("Third-Party Frameworks", List.of(
                "Review framework migration guide and changelog",
                "Check for API breaking changes in the new version",
                "Plan phased migration for complex replacements",
                "Verify framework compatibility with project's Spring Boot version"
        ));
        return map;
    }

    private static Map<String, List<String>> buildValidationChecklist() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("Build Dependencies", List.of(
                "[ ] Verify build plugin works with current Maven/Gradle version",
                "[ ] Verify plugin configuration is compatible",
                "[ ] Test build execution with the new plugin version"
        ));
        map.put("Spring Dependencies", List.of(
                "[ ] Verify Spring application context loads without errors",
                "[ ] Verify auto-configuration classes are compatible",
                "[ ] Run Spring Boot integration tests"
        ));
        map.put("Database Drivers", List.of(
                "[ ] Verify database connection works with the new driver",
                "[ ] Run database migration scripts successfully",
                "[ ] Verify CRUD operations work correctly"
        ));
        map.put("Testing Libraries", List.of(
                "[ ] Verify all existing tests pass with the new library version",
                "[ ] Verify test configuration loads correctly",
                "[ ] Verify test reporting still works"
        ));
        map.put("Logging Frameworks", List.of(
                "[ ] Verify log output format is as expected",
                "[ ] Verify no SLF4J binding conflicts exist",
                "[ ] Verify log levels are configured correctly"
        ));
        map.put("Security Libraries", List.of(
                "[ ] Verify authentication flow works correctly",
                "[ ] Verify authorization rules are enforced",
                "[ ] Verify token generation and validation works"
        ));
        map.put("Third-Party Frameworks", List.of(
                "[ ] Verify integration points work with the new dependency",
                "[ ] Verify data serialization/deserialization is compatible",
                "[ ] Verify error handling integration is maintained"
        ));
        return map;
    }

    private static Map<String, String> buildCategoryEffort() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("Build Dependencies", EFFORT_MEDIUM);
        map.put("Runtime Dependencies", EFFORT_MEDIUM);
        map.put("Spring Dependencies", EFFORT_HIGH);
        map.put("Database Drivers", EFFORT_HIGH);
        map.put("Testing Libraries", EFFORT_LOW);
        map.put("Logging Frameworks", EFFORT_LOW);
        map.put("Security Libraries", EFFORT_HIGH);
        map.put("Third-Party Frameworks", EFFORT_MEDIUM);
        return map;
    }

    // --- Utility methods ---

    private boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }
}