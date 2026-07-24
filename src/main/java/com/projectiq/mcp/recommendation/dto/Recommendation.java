package com.projectiq.mcp.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A single deterministic recommendation produced by the Intelligent Recommendation Engine.
 * Each recommendation has a category, priority, description, rationale, and optional
 * action items. Recommendations are immutable once generated and are ordered by priority.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "category",
        "priority",
        "title",
        "description",
        "rationale",
        "actionItems",
        "source"
})
public class Recommendation {

    private String id;
    private RecommendationCategory category;
    private RecommendationPriority priority;
    private String title;
    private String description;
    private String rationale;
    private java.util.List<String> actionItems;
    private String source;

    public Recommendation() {
        this.actionItems = new java.util.ArrayList<>();
    }

    /**
     * Constructs a recommendation with all required fields.
     *
     * @param id          unique identifier for the recommendation
     * @param category    the category of the recommendation
     * @param priority    the priority of the recommendation
     * @param title       a concise title for the recommendation
     * @param description a detailed description of the recommendation
     * @param rationale   the reasoning behind the recommendation
     */
    public Recommendation(String id, RecommendationCategory category, RecommendationPriority priority,
                          String title, String description, String rationale) {
        this();
        this.id = id;
        this.category = category;
        this.priority = priority;
        this.title = title;
        this.description = description;
        this.rationale = rationale;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RecommendationCategory getCategory() {
        return category;
    }

    public void setCategory(RecommendationCategory category) {
        this.category = category;
    }

    public RecommendationPriority getPriority() {
        return priority;
    }

    public void setPriority(RecommendationPriority priority) {
        this.priority = priority;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRationale() {
        return rationale;
    }

    public void setRationale(String rationale) {
        this.rationale = rationale;
    }

    public java.util.List<String> getActionItems() {
        return actionItems;
    }

    public void setActionItems(java.util.List<String> actionItems) {
        this.actionItems = actionItems != null ? new java.util.ArrayList<>(actionItems) : new java.util.ArrayList<>();
    }

    public void addActionItem(String actionItem) {
        this.actionItems.add(actionItem);
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}