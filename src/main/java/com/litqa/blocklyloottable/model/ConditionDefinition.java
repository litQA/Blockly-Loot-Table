package com.litqa.blocklyloottable.model;

/**
 * Static definition of one loot table condition block.
 *
 * @param blockType  Blockly block machine name
 * @param jsonType   Minecraft loot condition type
 * @param scope      loot table contexts in which the condition is legal
 * @param readableName human readable name used in validation messages
 */
public record ConditionDefinition(String blockType, String jsonType, ConditionScope scope, String readableName) {

    public boolean isAllowed(LootTableType type) {
        return scope.allows(type);
    }

    /**
     * Builds the context error message required by the plugin specification.
     *
     * @param type current loot table type
     * @return message naming the block, current type, legal type and a repair hint
     */
    public String invalidScopeMessage(LootTableType type) {
        String repair = switch (scope) {
            case BLOCK_ONLY -> "Change the loot table type to Block or remove this condition.";
            case ENTITY_ONLY -> "Change the loot table type to Entity or remove this condition.";
            case BLOCK_AND_ENTITY -> "Remove this condition.";
        };
        return readableName + " (" + blockType + ") is only available for "
                + scope.getAllowedContextLabel() + " loot tables; the current table type is "
                + type.getDisplayName() + ". " + repair;
    }
}
