package com.projectiq.mcp.pipeline.dto;

import java.util.Objects;

/**
 * Represents a single item of context within the intelligent context pipeline.
 * Each item has a source type, priority level, content, and a deduplication key.
 */
public class ContextItem implements Comparable<ContextItem> {

    private ContextSourceType sourceType;
    private ContextPriority priority;
    private String content;
    private String deduplicationKey;

    public ContextItem() {
    }

    public ContextItem(ContextSourceType sourceType, ContextPriority priority, String content) {
        this.sourceType = sourceType;
        this.priority = priority;
        this.content = content;
        this.deduplicationKey = content != null ? content.trim() : "";
    }

    public ContextItem(ContextSourceType sourceType, ContextPriority priority, String content, String deduplicationKey) {
        this.sourceType = sourceType;
        this.priority = priority;
        this.content = content;
        this.deduplicationKey = deduplicationKey != null ? deduplicationKey : (content != null ? content.trim() : "");
    }

    public ContextSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(ContextSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public ContextPriority getPriority() {
        return priority;
    }

    public void setPriority(ContextPriority priority) {
        this.priority = priority;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        this.deduplicationKey = content != null ? content.trim() : "";
    }

    public String getDeduplicationKey() {
        return deduplicationKey;
    }

    public void setDeduplicationKey(String deduplicationKey) {
        this.deduplicationKey = deduplicationKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContextItem that = (ContextItem) o;
        return Objects.equals(deduplicationKey, that.deduplicationKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deduplicationKey);
    }

    @Override
    public int compareTo(ContextItem other) {
        // Sort by priority: HIGH first, then MEDIUM, then LOW
        int thisOrdinal = this.priority != null ? this.priority.ordinal() : ContextPriority.LOW.ordinal();
        int otherOrdinal = other.priority != null ? other.priority.ordinal() : ContextPriority.LOW.ordinal();
        int priorityCompare = Integer.compare(thisOrdinal, otherOrdinal);
        if (priorityCompare != 0) {
            return priorityCompare;
        }
        // Within same priority, sort by source type name for determinism
        String thisSource = this.sourceType != null ? this.sourceType.name() : "";
        String otherSource = other.sourceType != null ? other.sourceType.name() : "";
        int sourceCompare = thisSource.compareTo(otherSource);
        if (sourceCompare != 0) {
            return sourceCompare;
        }
        // Finally sort by content for determinism
        String thisContent = this.content != null ? this.content : "";
        String otherContent = other.content != null ? other.content : "";
        return thisContent.compareTo(otherContent);
    }
}