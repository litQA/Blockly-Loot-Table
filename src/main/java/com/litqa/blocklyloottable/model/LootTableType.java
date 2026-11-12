package com.litqa.blocklyloottable.model;

/**
 * The loot table context supported by the editor.
 */
public enum LootTableType {

    BLOCK("Block", "minecraft:block", "blocks"),
    ENTITY("Entity", "minecraft:entity", "entities");

    private final String displayName;
    private final String jsonType;
    private final String defaultPathFolder;

    LootTableType(String displayName, String jsonType, String defaultPathFolder) {
        this.displayName = displayName;
        this.jsonType = jsonType;
        this.defaultPathFolder = defaultPathFolder;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getJsonType() {
        return jsonType;
    }

    public String getDefaultPathFolder() {
        return defaultPathFolder;
    }

    /**
     * Resolves a stored display name or JSON type back to a supported type.
     *
     * @param value stored value, possibly {@code null}
     * @return the matching type, or {@link #BLOCK} for unknown and empty values
     */
    public static LootTableType fromStored(String value) {
        if (value == null) {
            return BLOCK;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return BLOCK;
        }
        if ("Entity".equalsIgnoreCase(normalized) || "minecraft:entity".equalsIgnoreCase(normalized)) {
            return ENTITY;
        }
        return BLOCK;
    }
}
