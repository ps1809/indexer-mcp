package com.projectiq.mcp.orchestration.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of validating workflow step dependencies.
 * Contains validation status and any errors found.
 */
public class DependencyValidationResult {

    private final boolean valid;
    private final List<String> errors;
    private final List<String> warnings;

    public DependencyValidationResult(boolean valid, List<String> errors, List<String> warnings) {
        this.valid = valid;
        this.errors = Collections.unmodifiableList(new ArrayList<>(
                errors != null ? errors : Collections.emptyList()));
        this.warnings = Collections.unmodifiableList(new ArrayList<>(
                warnings != null ? warnings : Collections.emptyList()));
    }

    public boolean isValid() {
        return valid;
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public static DependencyValidationResult valid() {
        return new DependencyValidationResult(true, Collections.emptyList(), Collections.emptyList());
    }

    public static DependencyValidationResult invalid(List<String> errors) {
        return new DependencyValidationResult(false, errors, Collections.emptyList());
    }
}