package com.projectiq.mcp.validation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A single validation finding produced by the Intelligent Validation Pipeline.
 * Each finding has a deterministic category, severity, message, and optional details.
 * Findings are produced in stable order with no duplicates.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "category",
        "severity",
        "message",
        "details",
        "blocking"
})
public class ValidationFinding {

    private ValidationCategory category;
    private ValidationSeverity severity;
    private String message;
    private String details;
    private boolean blocking;

    public ValidationFinding() {
    }

    public ValidationFinding(ValidationCategory category, ValidationSeverity severity,
                             String message, String details, boolean blocking) {
        this.category = category;
        this.severity = severity;
        this.message = message;
        this.details = details;
        this.blocking = blocking;
    }

    public ValidationCategory getCategory() {
        return category;
    }

    public void setCategory(ValidationCategory category) {
        this.category = category;
    }

    public ValidationSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(ValidationSeverity severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public boolean isBlocking() {
        return blocking;
    }

    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }
}