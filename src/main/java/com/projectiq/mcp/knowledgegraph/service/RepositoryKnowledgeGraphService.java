package com.projectiq.mcp.knowledgegraph.service;

import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.dto.ClassInfo;
import com.projectiq.mcp.client.dto.ClassRequest;
import com.projectiq.mcp.client.dto.ClassResponse;
import com.projectiq.mcp.client.dto.ClassType;
import com.projectiq.mcp.client.dto.DependencyInfo;
import com.projectiq.mcp.client.dto.DependencyRequest;
import com.projectiq.mcp.client.dto.DependencyResponse;
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
import com.projectiq.mcp.knowledgegraph.dto.EntityType;
import com.projectiq.mcp.knowledgegraph.dto.KnowledgeGraph;
import com.projectiq.mcp.knowledgegraph.dto.KnowledgeGraphEdge;
import com.projectiq.mcp.knowledgegraph.dto.KnowledgeGraphNode;
import com.projectiq.mcp.knowledgegraph.dto.KnowledgeGraphReport;
import com.projectiq.mcp.knowledgegraph.dto.RelationshipType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service that builds and traverses a deterministic repository knowledge graph.
 * Connects all indexed entities and their relationships to provide a unified,
 * queryable semantic representation of the repository.
 *
 * <p>All outputs are deterministic, stable, and based solely on indexed data.</p>
 */
@Service
public class RepositoryKnowledgeGraphService {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryKnowledgeGraphService.class);

    private final IndexerRestClient indexerRestClient;

    public RepositoryKnowledgeGraphService(IndexerRestClient indexerRestClient) {
        this.indexerRestClient = indexerRestClient;
    }

    /**
     * Builds a complete repository knowledge graph for the given repository.
     *
     * @param repositoryName the repository name
     * @param branch         the branch (optional)
     * @return a complete {@link KnowledgeGraph} with nodes and edges
     */
    public KnowledgeGraph buildKnowledgeGraph(String repositoryName, String branch) {
        logger.info("Building knowledge graph for repository: {} branch: {}", repositoryName, branch);
        KnowledgeGraph graph = new KnowledgeGraph(repositoryName, branch != null ? branch : "main");

        KnowledgeGraphNode repoNode = new KnowledgeGraphNode(
                "repo:" + repositoryName,
                repositoryName,
                EntityType.REPOSITORY,
                repositoryName
        );
        repoNode.getLabels().add("Repository Root");
        graph.addNode(repoNode);

        // Load repository summary for packages and classes
        RepositorySummaryResponse summary = retrieveRepositorySummary(repositoryName, graph.getBranch());
        if (summary != null && summary.getPackages() != null) {
            for (PackageSummary pkg : summary.getPackages()) {
                String pkgName = pkg.getPackageName();
                if (pkgName == null || pkgName.isEmpty()) continue;

                String pkgId = "pkg:" + pkgName;
                KnowledgeGraphNode pkgNode = new KnowledgeGraphNode(
                        pkgId,
                        extractSimpleName(pkgName),
                        EntityType.PACKAGE,
                        pkgName
                );
                pkgNode.getLabels().add("Package");
                graph.addNode(pkgNode);

                graph.addEdge(new KnowledgeGraphEdge(
                        "e:" + UUID.randomUUID(),
                        repoNode.getId(), pkgId,
                        RelationshipType.CONTAINS,
                        "Repository contains package"
                ));

                if (pkg.getClasses() != null) {
                    for (com.projectiq.mcp.client.dto.ClassSummary cls : pkg.getClasses()) {
                        String clsName = cls.getClassName();
                        if (clsName == null || clsName.isEmpty()) continue;

                        String clsFqn = pkgName + "." + clsName;
                        String clsId = "cls:" + clsFqn;
                        EntityType entityType = determineEntityType(clsName, null);

                        KnowledgeGraphNode clsNode = new KnowledgeGraphNode(
                                clsId, clsName, entityType, clsFqn
                        );
                        clsNode.getLabels().add(entityType.name());
                        graph.addNode(clsNode);

                        graph.addEdge(new KnowledgeGraphEdge(
                                "e:" + UUID.randomUUID(),
                                pkgId, clsId,
                                RelationshipType.CONTAINS,
                                "Package contains " + entityType.name().toLowerCase()
                        ));
                    }
                }
            }
        }

        loadDetailedClasses(graph, repositoryName, branch);
        loadMethods(graph, repositoryName, branch);
        loadSpringComponents(graph, repositoryName, branch);
        loadRestApis(graph, repositoryName, branch);
        loadDependencies(graph, repositoryName, branch);
        updateAllConnectionCounts(graph);

        logger.info("Knowledge graph built: {} nodes, {} edges",
                graph.getNodeCount(), graph.getEdgeCount());
        return graph;
    }

    /**
     * Generates a comprehensive knowledge graph report.
     */
    public KnowledgeGraphReport generateKnowledgeGraphReport(String repositoryName, String branch) {
        logger.info("Generating knowledge graph report for: {} branch: {}", repositoryName, branch);
        KnowledgeGraph graph = buildKnowledgeGraph(repositoryName, branch);

        KnowledgeGraphReport report = new KnowledgeGraphReport();
        report.setRepositoryName(repositoryName);
        report.setBranch(graph.getBranch());

        List<String> connectedEntities = graph.getNodes().stream()
                .filter(n -> n.getConnectionCount() > 0)
                .map(n -> n.getName() + " (" + n.getEntityType().name() + ")")
                .collect(Collectors.toList());
        report.setConnectedEntities(connectedEntities);

        List<KnowledgeGraphReport.GraphRelationship> relationships = graph.getEdges().stream()
                .map(e -> {
                    String srcName = findNodeName(graph, e.getSourceId());
                    String tgtName = findNodeName(graph, e.getTargetId());
                    String srcType = findNodeType(graph, e.getSourceId());
                    String tgtType = findNodeType(graph, e.getTargetId());
                    return new KnowledgeGraphReport.GraphRelationship(
                            srcName, tgtName, e.getRelationshipType().name(),
                            srcType, tgtType
                    );
                })
                .collect(Collectors.toList());
        report.setRelationshipGraph(relationships);

        report.setDependencyPaths(findDependencyPaths(graph));
        report.setArchitecturalRelationships(findArchitecturalRelationships(graph));
        report.setIndirectDependencies(findIndirectDependencies(graph));

        List<String> criticalNodes = findCriticalNodes(graph);
        report.setCriticalNodes(criticalNodes);

        KnowledgeGraphReport.GraphStatistics stats = new KnowledgeGraphReport.GraphStatistics();
        stats.setTotalNodes(graph.getNodeCount());
        stats.setTotalEdges(graph.getEdgeCount());

        Set<String> entityTypes = graph.getNodes().stream()
                .map(n -> n.getEntityType().name())
                .collect(Collectors.toSet());
        stats.setEntityTypeCount(entityTypes.size());

        Set<String> relTypes = graph.getEdges().stream()
                .map(e -> e.getRelationshipType().name())
                .collect(Collectors.toSet());
        stats.setRelationshipTypeCount(relTypes.size());
        stats.setCriticalNodeCount(criticalNodes.size());
        stats.setIndirectDependencyCount(report.getIndirectDependencies().size());

        double avgConnections = graph.getNodeCount() > 0
                ? (double) graph.getEdgeCount() / graph.getNodeCount()
                : 0.0;
        stats.setAverageConnectionsPerNode(Math.round(avgConnections * 100.0) / 100.0);
        report.setGraphStatistics(stats);

        report.setTraversalSummary(buildTraversalSummary(graph, stats));
        return report;
    }

    /**
     * Traverses the knowledge graph starting from a specific entity.
     */
    public KnowledgeGraphReport traverseFromEntity(String repositoryName, String entityName, String branch) {
        logger.info("Traversing knowledge graph from entity '{}' in repository: {}",
                entityName, repositoryName);

        KnowledgeGraph fullGraph = buildKnowledgeGraph(repositoryName, branch);
        KnowledgeGraphNode startNode = findNodeByName(fullGraph, entityName);

        if (startNode == null) {
            KnowledgeGraphReport errorReport = new KnowledgeGraphReport();
            errorReport.setRepositoryName(repositoryName);
            errorReport.setBranch(branch != null ? branch : "main");
            errorReport.setTraversalSummary("Entity '" + entityName + "' not found in repository knowledge graph.");
            KnowledgeGraphReport.GraphStatistics emptyStats = new KnowledgeGraphReport.GraphStatistics();
            emptyStats.setTotalNodes(0);
            emptyStats.setTotalEdges(0);
            errorReport.setGraphStatistics(emptyStats);
            return errorReport;
        }

        Set<String> reachableNodeIds = new LinkedHashSet<>();
        Set<String> reachableEdgeIds = new LinkedHashSet<>();
        traverseReachable(startNode.getId(), fullGraph, reachableNodeIds, reachableEdgeIds, new HashSet<>());

        KnowledgeGraphReport report = new KnowledgeGraphReport();
        report.setRepositoryName(repositoryName);
        report.setBranch(fullGraph.getBranch());

        List<String> connectedEntities = new ArrayList<>();
        for (String nodeId : reachableNodeIds) {
            KnowledgeGraphNode node = findNodeById(fullGraph, nodeId);
            if (node != null) {
                connectedEntities.add(node.getName() + " (" + node.getEntityType().name() + ")");
            }
        }
        report.setConnectedEntities(connectedEntities);

        List<KnowledgeGraphReport.GraphRelationship> relationships = new ArrayList<>();
        for (String edgeId : reachableEdgeIds) {
            KnowledgeGraphEdge edge = findEdgeById(fullGraph, edgeId);
            if (edge != null) {
                String srcName = findNodeName(fullGraph, edge.getSourceId());
                String tgtName = findNodeName(fullGraph, edge.getTargetId());
                String srcType = findNodeType(fullGraph, edge.getSourceId());
                String tgtType = findNodeType(fullGraph, edge.getTargetId());
                relationships.add(new KnowledgeGraphReport.GraphRelationship(
                        srcName, tgtName, edge.getRelationshipType().name(),
                        srcType, tgtType
                ));
            }
        }
        report.setRelationshipGraph(relationships);

        List<String> dependencyPaths = new ArrayList<>();
        for (String edgeId : reachableEdgeIds) {
            KnowledgeGraphEdge edge = findEdgeById(fullGraph, edgeId);
            if (edge != null && edge.getRelationshipType() == RelationshipType.DEPENDS_ON) {
                String srcName = findNodeName(fullGraph, edge.getSourceId());
                String tgtName = findNodeName(fullGraph, edge.getTargetId());
                dependencyPaths.add(srcName + " -> " + tgtName);
            }
        }
        report.setDependencyPaths(dependencyPaths);

        List<String> criticalNodes = new ArrayList<>();
        for (String nodeId : reachableNodeIds) {
            KnowledgeGraphNode node = findNodeById(fullGraph, nodeId);
            if (node != null && node.getConnectionCount() > 2) {
                criticalNodes.add(node.getName() + " (" + node.getEntityType().name()
                        + ") - " + node.getConnectionCount() + " connections");
            }
        }
        report.setCriticalNodes(criticalNodes);

        KnowledgeGraphReport.GraphStatistics stats = new KnowledgeGraphReport.GraphStatistics();
        stats.setTotalNodes(reachableNodeIds.size());
        stats.setTotalEdges(reachableEdgeIds.size());
        Set<String> entityTypes = new HashSet<>();
        for (String nodeId : reachableNodeIds) {
            KnowledgeGraphNode node = findNodeById(fullGraph, nodeId);
            if (node != null) {
                entityTypes.add(node.getEntityType().name());
            }
        }
        stats.setEntityTypeCount(entityTypes.size());
        stats.setCriticalNodeCount(criticalNodes.size());

        double avgConnections = reachableNodeIds.size() > 0
                ? (double) reachableEdgeIds.size() / reachableNodeIds.size()
                : 0.0;
        stats.setAverageConnectionsPerNode(Math.round(avgConnections * 100.0) / 100.0);
        report.setGraphStatistics(stats);

        report.setTraversalSummary("Graph traversal from '" + entityName + "': found "
                + reachableNodeIds.size() + " connected entities with "
                + reachableEdgeIds.size() + " relationships.");

        return report;
    }

    // --- Private helper methods ---

    private RepositorySummaryResponse retrieveRepositorySummary(String repositoryName, String branch) {
        try {
            RepositorySummaryRequest request = new RepositorySummaryRequest();
            request.setRepositoryName(repositoryName);
            request.setBranch(branch);
            return indexerRestClient.getRepositorySummary(request);
        } catch (Exception e) {
            logger.warn("Failed to retrieve repository summary: {}", e.getMessage());
            return null;
        }
    }

    private void loadDetailedClasses(KnowledgeGraph graph, String repositoryName, String branch) {
        try {
            ClassRequest request = new ClassRequest(repositoryName, null);
            request.setBranch(branch);
            ClassResponse response = indexerRestClient.findClass(request);

            if (response != null && response.getClasses() != null) {
                for (ClassInfo ci : response.getClasses()) {
                    String fqn = ci.getFullyQualifiedName();
                    if (fqn == null || fqn.isEmpty()) continue;

                    String clsId = "cls:" + fqn;
                    KnowledgeGraphNode existingNode = findNodeById(graph, clsId);

                    if (existingNode == null) {
                        EntityType entityType = determineEntityType(ci.getClassName(), ci.getClassType());
                        KnowledgeGraphNode node = new KnowledgeGraphNode(
                                clsId, ci.getClassName(), entityType, fqn
                        );
                        if (ci.getParentClass() != null) {
                            node.getLabels().add("extends:" + ci.getParentClass());
                        }
                        if (ci.getImplementedInterfaces() != null) {
                            ci.getImplementedInterfaces().forEach(
                                    iface -> node.getLabels().add("implements:" + iface)
                            );
                        }
                        if (ci.getAnnotations() != null) {
                            ci.getAnnotations().forEach(
                                    ann -> node.getLabels().add("@" + ann)
                            );
                        }
                        graph.addNode(node);

                        String pkgName = ci.getPackageName();
                        if (pkgName != null && !pkgName.isEmpty()) {
                            String pkgId = "pkg:" + pkgName;
                            graph.addEdge(new KnowledgeGraphEdge(
                                    "e:" + UUID.randomUUID(),
                                    pkgId, clsId,
                                    RelationshipType.CONTAINS,
                                    "Package contains class"
                            ));
                        }
                    }

                    if (ci.getParentClass() != null && !ci.getParentClass().isEmpty()) {
                        String parentId = "cls:" + ci.getParentClass();
                        if (findNodeById(graph, parentId) != null) {
                            graph.addEdge(new KnowledgeGraphEdge(
                                    "e:" + UUID.randomUUID(),
                                    clsId, parentId,
                                    RelationshipType.EXTENDS,
                                    ci.getClassName() + " extends " + ci.getParentClass()
                            ));
                        }
                    }

                    if (ci.getImplementedInterfaces() != null) {
                        for (String iface : ci.getImplementedInterfaces()) {
                            String ifaceId = "cls:" + iface;
                            if (findNodeById(graph, ifaceId) != null) {
                                graph.addEdge(new KnowledgeGraphEdge(
                                        "e:" + UUID.randomUUID(),
                                        clsId, ifaceId,
                                        RelationshipType.IMPLEMENTS,
                                        ci.getClassName() + " implements " + iface
                                ));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to load detailed classes: {}", e.getMessage());
        }
    }

    private void loadMethods(KnowledgeGraph graph, String repositoryName, String branch) {
        try {
            MethodRequest request = new MethodRequest(repositoryName, null);
            request.setBranch(branch);
            MethodResponse response = indexerRestClient.findMethod(request);

            if (response != null && response.getMethods() != null) {
                for (MethodInfo mi : response.getMethods()) {
                    String methodFqn = mi.getFullyQualifiedName();
                    if (methodFqn == null || methodFqn.isEmpty()) continue;

                    String methodId = "method:" + methodFqn;
                    KnowledgeGraphNode methodNode = new KnowledgeGraphNode(
                            methodId,
                            mi.getMethodName(),
                            EntityType.METHOD,
                            methodFqn
                    );
                    methodNode.getLabels().add("Method");
                    if (mi.getVisibility() != null) {
                        methodNode.getLabels().add(mi.getVisibility());
                    }
                    graph.addNode(methodNode);

                    String declaringClass = mi.getDeclaringClass();
                    if (declaringClass != null && !declaringClass.isEmpty()) {
                        String clsId = "cls:" + declaringClass;
                        if (findNodeById(graph, clsId) != null) {
                            graph.addEdge(new KnowledgeGraphEdge(
                                    "e:" + UUID.randomUUID(),
                                    clsId, methodId,
                                    RelationshipType.CONTAINS,
                                    "Class contains method"
                            ));
                        } else {
                            String simpleClsName = declaringClass.contains(".")
                                    ? declaringClass.substring(declaringClass.lastIndexOf('.') + 1)
                                    : declaringClass;
                            KnowledgeGraphNode clsNode = new KnowledgeGraphNode(
                                    clsId, simpleClsName, EntityType.CLASS, declaringClass
                            );
                            clsNode.getLabels().add("CLASS");
                            graph.addNode(clsNode);
                            graph.addEdge(new KnowledgeGraphEdge(
                                    "e:" + UUID.randomUUID(),
                                    clsId, methodId,
                                    RelationshipType.CONTAINS,
                                    "Class contains method"
                            ));
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to load methods: {}", e.getMessage());
        }
    }

    private void loadSpringComponents(KnowledgeGraph graph, String repositoryName, String branch) {
        try {
            SpringComponentRequest request = new SpringComponentRequest();
            request.setRepositoryName(repositoryName);
            request.setBranch(branch);
            SpringComponentResponse response = indexerRestClient.findSpringComponent(request);

            if (response != null && response.getComponents() != null) {
                for (SpringComponentInfo comp : response.getComponents()) {
                    String compClassName = comp.getClassName();
                    if (compClassName == null || compClassName.isEmpty()) continue;

                    String compId = "spring:" + compClassName;
                    KnowledgeGraphNode compNode = new KnowledgeGraphNode(
                            compId,
                            compClassName,
                            EntityType.SPRING_COMPONENT,
                            compClassName
                    );
                    compNode.getLabels().add("Spring Component");
                    if (comp.getComponentType() != null) {
                        compNode.getLabels().add("@" + comp.getComponentType());
                    }
                    graph.addNode(compNode);

                    String clsId = "cls:" + compClassName;
                    KnowledgeGraphNode existingCls = findNodeById(graph, clsId);
                    if (existingCls != null) {
                        graph.addEdge(new KnowledgeGraphEdge(
                                "e:" + UUID.randomUUID(),
                                clsId, compId,
                                RelationshipType.CONFIGURES,
                                "Class is a Spring component"
                        ));
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to load Spring components: {}", e.getMessage());
        }
    }

    private void loadRestApis(KnowledgeGraph graph, String repositoryName, String branch) {
        try {
            RestApiRequest request = new RestApiRequest();
            request.setRepositoryName(repositoryName);
            request.setBranch(branch);
            RestApiResponse response = indexerRestClient.findRestApi(request);

            if (response != null && response.getEndpoints() != null) {
                for (RestEndpointInfo endpoint : response.getEndpoints()) {
                    String endpointPath = endpoint.getEndpointPath();
                    if (endpointPath == null) continue;

                    String method = endpoint.getHttpMethod() != null ? endpoint.getHttpMethod() : "GET";
                    String apiId = "api:" + method + ":" + endpointPath;
                    String apiName = method + " " + endpointPath;

                    KnowledgeGraphNode apiNode = new KnowledgeGraphNode(
                            apiId,
                            apiName,
                            EntityType.REST_API,
                            apiName
                    );
                    apiNode.getLabels().add("REST API");
                    apiNode.getLabels().add(method + " " + endpointPath);
                    graph.addNode(apiNode);

                    String controllerName = endpoint.getControllerName();
                    if (controllerName != null && !controllerName.isEmpty()) {
                        String clsId = "cls:" + controllerName;
                        KnowledgeGraphNode clsNode = findNodeById(graph, clsId);
                        if (clsNode != null) {
                            graph.addEdge(new KnowledgeGraphEdge(
                                    "e:" + UUID.randomUUID(),
                                    clsId, apiId,
                                    RelationshipType.EXPOSES,
                                    "Class exposes REST API endpoint"
                            ));
                            graph.addEdge(new KnowledgeGraphEdge(
                                    "e:" + UUID.randomUUID(),
                                    apiId, clsId,
                                    RelationshipType.REFERENCES,
                                    "API references declaring class"
                            ));
                        }

                        String springId = "spring:" + controllerName;
                        if (findNodeById(graph, springId) != null) {
                            graph.addEdge(new KnowledgeGraphEdge(
                                    "e:" + UUID.randomUUID(),
                                    springId, apiId,
                                    RelationshipType.EXPOSES,
                                    "Spring component exposes REST API"
                            ));
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to load REST APIs: {}", e.getMessage());
        }
    }

    private void loadDependencies(KnowledgeGraph graph, String repositoryName, String branch) {
        try {
            DependencyRequest request = new DependencyRequest();
            request.setRepositoryName(repositoryName);
            request.setBranch(branch);
            DependencyResponse response = indexerRestClient.findDependency(request);

            if (response != null && response.getDependencies() != null) {
                for (DependencyInfo dep : response.getDependencies()) {
                    String depName = dep.getName();
                    if (depName == null || depName.isEmpty()) continue;

                    String depId = "dep:" + depName;
                    KnowledgeGraphNode depNode = new KnowledgeGraphNode(
                            depId,
                            depName,
                            EntityType.DEPENDENCY,
                            depName
                    );
                    depNode.getLabels().add("Dependency");
                    if (dep.getType() != null) {
                        depNode.getLabels().add("type:" + dep.getType().name());
                    }
                    if (dep.getScope() != null) {
                        depNode.getLabels().add("scope:" + dep.getScope());
                    }
                    graph.addNode(depNode);

                    String repoId = "repo:" + repositoryName;
                    graph.addEdge(new KnowledgeGraphEdge(
                            "e:" + UUID.randomUUID(),
                            repoId, depId,
                            RelationshipType.DEPENDS_ON,
                            "Repository depends on " + depName
                    ));

                    graph.addEdge(new KnowledgeGraphEdge(
                            "e:" + UUID.randomUUID(),
                            depId, repoId,
                            RelationshipType.CONFIGURES,
                            depName + " configures repository"
                    ));
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to load dependencies: {}", e.getMessage());
        }
    }

    private EntityType determineEntityType(String className, ClassType classType) {
        if (classType != null) {
            switch (classType) {
                case CLASS:
                    return EntityType.CLASS;
                case INTERFACE:
                    return EntityType.INTERFACE;
                case ENUM:
                    return EntityType.CLASS;
                case RECORD:
                    return EntityType.CLASS;
                case ANNOTATION:
                    return EntityType.CONFIGURATION;
            }
        }
        if (className != null) {
            String lower = className.toLowerCase();
            if (lower.contains("entity") || lower.contains("model") || lower.contains("domain")) {
                return EntityType.DATABASE_ENTITY;
            }
            if (lower.contains("config") || lower.contains("properties")) {
                return EntityType.CONFIGURATION;
            }
            if (lower.contains("controller") || lower.contains("resource") || lower.contains("endpoint")) {
                return EntityType.REST_API;
            }
        }
        return EntityType.CLASS;
    }

    private String extractSimpleName(String qualifiedName) {
        if (qualifiedName == null) return "";
        int lastDot = qualifiedName.lastIndexOf('.');
        return lastDot >= 0 ? qualifiedName.substring(lastDot + 1) : qualifiedName;
    }

    private KnowledgeGraphNode findNodeById(KnowledgeGraph graph, String id) {
        if (graph.getNodes() == null) return null;
        return graph.getNodes().stream()
                .filter(n -> n.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private KnowledgeGraphNode findNodeByName(KnowledgeGraph graph, String name) {
        if (graph.getNodes() == null || name == null) return null;
        String lowerName = name.toLowerCase().trim();
        return graph.getNodes().stream()
                .filter(n -> n.getName() != null && n.getName().toLowerCase().contains(lowerName))
                .findFirst()
                .orElse(null);
    }

    private KnowledgeGraphEdge findEdgeById(KnowledgeGraph graph, String id) {
        if (graph.getEdges() == null) return null;
        return graph.getEdges().stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private String findNodeName(KnowledgeGraph graph, String nodeId) {
        KnowledgeGraphNode node = findNodeById(graph, nodeId);
        return node != null ? node.getName() : nodeId;
    }

    private String findNodeType(KnowledgeGraph graph, String nodeId) {
        KnowledgeGraphNode node = findNodeById(graph, nodeId);
        return node != null ? node.getEntityType().name() : "UNKNOWN";
    }

    private void updateAllConnectionCounts(KnowledgeGraph graph) {
        if (graph.getNodes() == null || graph.getEdges() == null) return;
        for (KnowledgeGraphNode node : graph.getNodes()) {
            int count = (int) graph.getEdges().stream()
                    .filter(e -> e.getSourceId().equals(node.getId())
                            || e.getTargetId().equals(node.getId()))
                    .count();
            node.setConnectionCount(count);
        }
    }

    private void traverseReachable(String startId, KnowledgeGraph graph,
                                    Set<String> visitedNodes, Set<String> visitedEdges,
                                    Set<String> processing) {
        if (visitedNodes.contains(startId) || processing.contains(startId)) return;
        processing.add(startId);
        visitedNodes.add(startId);

        if (graph.getEdges() != null) {
            for (KnowledgeGraphEdge edge : graph.getEdges()) {
                if (edge.getSourceId().equals(startId)) {
                    visitedEdges.add(edge.getId());
                    traverseReachable(edge.getTargetId(), graph, visitedNodes, visitedEdges, processing);
                } else if (edge.getTargetId().equals(startId)) {
                    visitedEdges.add(edge.getId());
                    traverseReachable(edge.getSourceId(), graph, visitedNodes, visitedEdges, processing);
                }
            }
        }
        processing.remove(startId);
    }

    private List<String> findDependencyPaths(KnowledgeGraph graph) {
        List<String> paths = new ArrayList<>();
        if (graph.getEdges() == null) return paths;

        for (KnowledgeGraphEdge edge : graph.getEdges()) {
            if (edge.getRelationshipType() == RelationshipType.DEPENDS_ON) {
                String src = findNodeName(graph, edge.getSourceId());
                String tgt = findNodeName(graph, edge.getTargetId());
                paths.add(src + " depends on " + tgt);
            }
        }
        return paths;
    }

    private List<String> findArchitecturalRelationships(KnowledgeGraph graph) {
        List<String> archRels = new ArrayList<>();
        if (graph.getEdges() == null) return archRels;

        for (KnowledgeGraphEdge edge : graph.getEdges()) {
            String srcType = findNodeType(graph, edge.getSourceId());
            String tgtType = findNodeType(graph, edge.getTargetId());
            if (!srcType.equals(tgtType)) {
                String src = findNodeName(graph, edge.getSourceId());
                String tgt = findNodeName(graph, edge.getTargetId());
                archRels.add(src + " [" + srcType + "] --[" + edge.getRelationshipType().name()
                        + "]--> " + tgt + " [" + tgtType + "]");
            }
        }
        return archRels;
    }

    private List<String> findIndirectDependencies(KnowledgeGraph graph) {
        List<String> indirect = new ArrayList<>();
        if (graph.getEdges() == null || graph.getNodes() == null) return indirect;

        Map<String, List<String>> adjacency = new HashMap<>();
        for (KnowledgeGraphEdge edge : graph.getEdges()) {
            adjacency.computeIfAbsent(edge.getSourceId(), k -> new ArrayList<>()).add(edge.getTargetId());
        }

        for (String source : adjacency.keySet()) {
            for (String intermediate : adjacency.get(source)) {
                List<String> targets = adjacency.get(intermediate);
                if (targets != null) {
                    for (String target : targets) {
                        if (!target.equals(source)
                                && !adjacency.getOrDefault(source, new ArrayList<>()).contains(target)) {
                            String srcName = findNodeName(graph, source);
                            String intName = findNodeName(graph, intermediate);
                            String tgtName = findNodeName(graph, target);
                            indirect.add(srcName + " -> " + intName + " -> " + tgtName + " (indirect)");
                        }
                    }
                }
            }
        }
        return indirect;
    }

    private List<String> findCriticalNodes(KnowledgeGraph graph) {
        List<String> critical = new ArrayList<>();
        if (graph.getNodes() == null) return critical;

        double avgConnections = graph.getNodeCount() > 0
                ? (double) graph.getEdgeCount() / graph.getNodeCount()
                : 0;

        for (KnowledgeGraphNode node : graph.getNodes()) {
            if (node.getConnectionCount() > avgConnections && node.getConnectionCount() > 1) {
                critical.add(node.getName() + " (" + node.getEntityType().name()
                        + ") - " + node.getConnectionCount() + " connections");
            }
        }
        return critical;
    }

    private String buildTraversalSummary(KnowledgeGraph graph, KnowledgeGraphReport.GraphStatistics stats) {
        StringBuilder sb = new StringBuilder();
        sb.append("Knowledge Graph for repository '").append(graph.getRepositoryName()).append("'");
        if (graph.getBranch() != null) {
            sb.append(" on branch '").append(graph.getBranch()).append("'");
        }
        sb.append(". ");
        sb.append("Graph contains ").append(stats.getTotalNodes()).append(" nodes");
        sb.append(" and ").append(stats.getTotalEdges()).append(" edges");
        sb.append(" across ").append(stats.getEntityTypeCount()).append(" entity types");
        sb.append(" with ").append(stats.getRelationshipTypeCount()).append(" relationship types. ");
        sb.append("Average connections per node: ").append(stats.getAverageConnectionsPerNode()).append(". ");
        sb.append("Critical nodes: ").append(stats.getCriticalNodeCount()).append(". ");
        sb.append("Indirect dependencies: ").append(stats.getIndirectDependencyCount()).append(".");
        return sb.toString();
    }
}