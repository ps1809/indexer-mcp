package com.projectiq.mcp.knowledgegraph.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the complete repository knowledge graph with nodes and edges.
 */
public class KnowledgeGraph {

    private String repositoryName;
    private String branch;
    private List<KnowledgeGraphNode> nodes;
    private List<KnowledgeGraphEdge> edges;

    public KnowledgeGraph() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    public KnowledgeGraph(String repositoryName, String branch) {
        this();
        this.repositoryName = repositoryName;
        this.branch = branch;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public List<KnowledgeGraphNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<KnowledgeGraphNode> nodes) {
        this.nodes = nodes;
    }

    public List<KnowledgeGraphEdge> getEdges() {
        return edges;
    }

    public void setEdges(List<KnowledgeGraphEdge> edges) {
        this.edges = edges;
    }

    /**
     * Adds a node to the graph.
     *
     * @param node the node to add
     * @return true if the node was added successfully
     */
    public boolean addNode(KnowledgeGraphNode node) {
        if (node != null && !nodes.contains(node)) {
            return nodes.add(node);
        }
        return false;
    }

    /**
     * Adds an edge to the graph.
     *
     * @param edge the edge to add
     * @return true if the edge was added successfully
     */
    public boolean addEdge(KnowledgeGraphEdge edge) {
        if (edge != null && !edges.contains(edge)) {
            return edges.add(edge);
        }
        return false;
    }

    /**
     * Returns the total number of nodes in the graph.
     */
    public int getNodeCount() {
        return nodes != null ? nodes.size() : 0;
    }

    /**
     * Returns the total number of edges in the graph.
     */
    public int getEdgeCount() {
        return edges != null ? edges.size() : 0;
    }
}