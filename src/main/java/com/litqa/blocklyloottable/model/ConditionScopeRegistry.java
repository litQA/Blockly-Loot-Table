package com.litqa.blocklyloottable.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Single source of truth for condition block legality.
 *
 * <p>The editor warning pass, save validation and generation validation all
 * read this registry. Do not duplicate scope rules in Blockly JSON.</p>
 */
public final class ConditionScopeRegistry {

    private static final Map<String, ConditionDefinition> DEFINITIONS = new LinkedHashMap<>();

    static {
        register(new ConditionDefinition("loottable_random_chance", "minecraft:random_chance",
                ConditionScope.BLOCK_AND_ENTITY, "Random chance"));
        register(new ConditionDefinition("loottable_killed_by_player", "minecraft:killed_by_player",
                ConditionScope.ENTITY_ONLY, "Killed by player"));
        register(new ConditionDefinition("loottable_survives_explosion", "minecraft:survives_explosion",
                ConditionScope.BLOCK_ONLY, "Survives explosion"));
        register(new ConditionDefinition("loottable_weather_check", "minecraft:weather_check",
                ConditionScope.BLOCK_AND_ENTITY, "Weather check"));
        register(new ConditionDefinition("loottable_inverted", "minecraft:inverted",
                ConditionScope.BLOCK_AND_ENTITY, "Inverted condition"));
        register(new ConditionDefinition("loottable_block_state_property", "minecraft:block_state_property",
                ConditionScope.BLOCK_ONLY, "Block state property"));
        register(new ConditionDefinition("loottable_block_state_boolean", "minecraft:block_state_property",
                ConditionScope.BLOCK_ONLY, "Block state boolean property"));
        register(new ConditionDefinition("loottable_block_state_integer", "minecraft:block_state_property",
                ConditionScope.BLOCK_ONLY, "Block state integer property"));
        register(new ConditionDefinition("loottable_block_state_enum", "minecraft:block_state_property",
                ConditionScope.BLOCK_ONLY, "Block state enum property"));
        register(new ConditionDefinition("loottable_entity_has_potion", "minecraft:entity_properties",
                ConditionScope.ENTITY_ONLY, "Entity has potion effect"));
        register(new ConditionDefinition("loottable_block_nbt_logic", "minecraft:location_check",
                ConditionScope.BLOCK_ONLY, "Block logic NBT"));
        register(new ConditionDefinition("loottable_block_nbt_number", "minecraft:location_check",
                ConditionScope.BLOCK_ONLY, "Block number NBT"));
        register(new ConditionDefinition("loottable_block_nbt_text", "minecraft:location_check",
                ConditionScope.BLOCK_ONLY, "Block text NBT"));
        register(new ConditionDefinition("loottable_entity_nbt_logic", "minecraft:entity_properties",
                ConditionScope.ENTITY_ONLY, "Entity logic NBT"));
        register(new ConditionDefinition("loottable_entity_nbt_number", "minecraft:entity_properties",
                ConditionScope.ENTITY_ONLY, "Entity number NBT"));
        register(new ConditionDefinition("loottable_entity_nbt_text", "minecraft:entity_properties",
                ConditionScope.ENTITY_ONLY, "Entity text NBT"));
        register(new ConditionDefinition("loottable_item_has_enchantment", "minecraft:match_tool",
                ConditionScope.BLOCK_AND_ENTITY, "Item has enchantment"));
    }

    private ConditionScopeRegistry() {
    }

    private static void register(ConditionDefinition definition) {
        DEFINITIONS.put(definition.blockType(), definition);
    }

    /**
     * @param blockType Blockly machine name
     * @return definition or {@code null} when the block is not registered
     */
    public static ConditionDefinition get(String blockType) {
        return DEFINITIONS.get(blockType);
    }

    public static Collection<ConditionDefinition> all() {
        return DEFINITIONS.values();
    }
}
