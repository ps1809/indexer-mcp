package com.projectiq.mcp.knowledge.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration of all knowledge domains supported by the Development Knowledge Engine.
 * Each domain represents a distinct area of repository intelligence that can be queried.
 */
public enum KnowledgeDomain {
    REPOSITORY_STRUCTURE("Repository Structure"),
    ARCHITECTURE("Architecture"),
    DEPENDENCIES("Dependencies"),
    REST_APIS("REST APIs"),
    SPRING_COMPONENTS("Spring Components"),
    KNOWLEDGE_GRAPH("Knowledge Graph"),
    DEVELOPMENT_SESSIONS("Development Sessions"),
    WORKFLOW_INTELLIGENCE("Workflow Intelligence"),
    VALIDATION_RESULTS("Validation Results"),
    REPOSITORY_EVOLUTION("Repository Evolution"),
    ARCHITECTURAL_DECISIONS("Architectural Decisions"),
    CROSS_REPOSITORY_INSIGHTS("Cross-Repository Insights"),
    ALL("All Domains");

    private final String displayName;

    private static final Map<String, KnowledgeDomain> KEYWORD_MAP = buildKeywordMap();

    KnowledgeDomain(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Builds a map of lowercase keywords to their corresponding KnowledgeDomain.
     * These are specific words that uniquely identify a domain.
     */
    private static Map<String, KnowledgeDomain> buildKeywordMap() {
        Map<String, KnowledgeDomain> map = new HashMap<>();
        // Add specific keywords that uniquely identify each domain
        map.put("architecture", ARCHITECTURE);
        map.put("dependencies", DEPENDENCIES);
        map.put("rest apis", REST_APIS);
        map.put("spring components", SPRING_COMPONENTS);
        map.put("knowledge graph", KNOWLEDGE_GRAPH);
        map.put("sessions", DEVELOPMENT_SESSIONS);
        map.put("workflow intelligence", WORKFLOW_INTELLIGENCE);
        map.put("validation results", VALIDATION_RESULTS);
        map.put("repository evolution", REPOSITORY_EVOLUTION);
        map.put("architectural decisions", ARCHITECTURAL_DECISIONS);
        map.put("cross repository insights", CROSS_REPOSITORY_INSIGHTS);
        map.put("repository structure", REPOSITORY_STRUCTURE);
        return map;
    }

    /**
     * Resolves a knowledge domain from a query string, case-insensitively.
     * Checks if the query contains the domain's enum name or display name as a substring.
     * Domains are checked in order of longest display name first to ensure
     * more specific matches take priority over general ones.
     *
     * @param query the query string
     * @return the matching KnowledgeDomain, or ALL if no match
     */
    public static KnowledgeDomain fromQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return ALL;
        }
        String normalized = query.trim().toLowerCase();

        // Sort domains by display name length (longest first) for priority matching
        KnowledgeDomain[] sorted = values();
        java.util.Arrays.sort(sorted, (a, b) -> {
            if (a == ALL) return 1;
            if (b == ALL) return -1;
            return Integer.compare(b.getDisplayName().length(), a.getDisplayName().length());
        });

        // First pass: check full domain name or display name as substring
        for (KnowledgeDomain domain : sorted) {
            if (domain == ALL) continue;
            String domainName = domain.name().toLowerCase().replace('_', ' ');
            String displayName = domain.getDisplayName().toLowerCase();

            if (normalized.contains(domainName) || normalized.contains(displayName)) {
                return domain;
            }
        }

        // Second pass: check specific keywords
        for (Map.Entry<String, KnowledgeDomain> entry : KEYWORD_MAP.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return ALL;
    }
}