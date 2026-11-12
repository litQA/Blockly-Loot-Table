package com.litqa.blocklyloottable.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Collected validation issues for one loot table document or element.
 */
public final class ValidationReport {

    private final List<Issue> issues = new ArrayList<>();

    public void addError(String blockType, String message) {
        issues.add(new Issue(Issue.Severity.ERROR, blockType, message));
    }

    public void addWarning(String blockType, String message) {
        issues.add(new Issue(Issue.Severity.WARNING, blockType, message));
    }

    public void merge(ValidationReport other) {
        if (other != null) {
            issues.addAll(other.issues);
        }
    }

    public boolean hasErrors() {
        return issues.stream().anyMatch(Issue::isError);
    }

    public boolean isEmpty() {
        return issues.isEmpty();
    }

    public List<Issue> getIssues() {
        return List.copyOf(issues);
    }

    public List<String> getErrorMessages() {
        return issues.stream().filter(Issue::isError).map(Issue::message).toList();
    }

    public List<String> getWarningMessages() {
        return issues.stream().filter(issue -> !issue.isError()).map(Issue::message).toList();
    }

    /**
     * Groups error messages by Blockly block type for workspace warning markers.
     *
     * @return ordered map of block type to joined error text
     */
    public Map<String, String> getErrorMessagesByBlockType() {
        Map<String, String> grouped = new LinkedHashMap<>();
        for (Issue issue : issues) {
            if (!issue.isError()) {
                continue;
            }
            String key = issue.blockType() == null || issue.blockType().isBlank() ? "_general" : issue.blockType();
            grouped.merge(key, issue.message(), (left, right) -> left + "\n" + right);
        }
        return grouped;
    }

    public String summary() {
        List<String> errors = getErrorMessages();
        if (!errors.isEmpty()) {
            return String.join(" | ", errors);
        }
        List<String> warnings = getWarningMessages();
        return String.join(" | ", warnings);
    }
}
