package com.litqa.blocklyloottable.model;

/**
 * Legal loot table contexts for a condition block.
 */
public enum ConditionScope {

    BLOCK_AND_ENTITY("Block and Entity"),
    ALL_CONTEXTS("Block, Entity and Chest"),
    BLOCK_ONLY("Block"),
    ENTITY_ONLY("Entity");

    private final String allowedContextLabel;

    ConditionScope(String allowedContextLabel) {
        this.allowedContextLabel = allowedContextLabel;
    }

    public String getAllowedContextLabel() {
        return allowedContextLabel;
    }

    public boolean allows(LootTableType type) {
        return switch (this) {
            case BLOCK_AND_ENTITY -> type == LootTableType.BLOCK || type == LootTableType.ENTITY;
            case ALL_CONTEXTS -> true;
            case BLOCK_ONLY -> type == LootTableType.BLOCK;
            case ENTITY_ONLY -> type == LootTableType.ENTITY;
        };
    }
}
