package com.litqa.blocklyloottable.model;

/**
 * One validation issue produced while parsing the Blockly document.
 *
 * @param severity issue severity
 * @param blockType Blockly machine name related to the issue, or {@code null}
 * @param message  user-facing message
 */
public record Issue(Severity severity, String blockType, String message) {

    public enum Severity {
        ERROR,
        WARNING
    }

    public boolean isError() {
        return severity == Severity.ERROR;
    }
}
