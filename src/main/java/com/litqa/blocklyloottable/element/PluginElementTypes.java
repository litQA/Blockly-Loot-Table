package com.litqa.blocklyloottable.element;

import com.litqa.blocklyloottable.element.types.BlocklyLootTable;
import com.litqa.blocklyloottable.ui.BlocklyLootTableGUI;
import net.mcreator.element.ModElementType;
import net.mcreator.element.ModElementTypeLoader;
import net.mcreator.generator.GeneratorFlavor;

/**
 * Registers the Blockly Loot Table mod element type.
 */
public final class PluginElementTypes {

    public static ModElementType<?> BLOCKLY_LOOT_TABLE;

    private PluginElementTypes() {
    }

    public static void load() {
        if (BLOCKLY_LOOT_TABLE != null) {
            return;
        }
        BLOCKLY_LOOT_TABLE = ModElementTypeLoader.register(new ModElementType<>(
                BlocklyLootTable.REGISTRY_NAME,
                null,
                BlocklyLootTableGUI::new,
                BlocklyLootTable.class
        ).coveredOn(GeneratorFlavor.FORGE, GeneratorFlavor.NEOFORGE, GeneratorFlavor.FABRIC));
        System.out.println("[Blockly Loot Table] Registered mod element type " + BlocklyLootTable.REGISTRY_NAME);
    }
}
