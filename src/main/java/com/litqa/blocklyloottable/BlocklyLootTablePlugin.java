package com.litqa.blocklyloottable;

import com.litqa.blocklyloottable.element.PluginElementTypes;
import net.mcreator.blockly.data.BlocklyLoader;
import net.mcreator.plugin.JavaPlugin;
import net.mcreator.plugin.Plugin;
import net.mcreator.plugin.events.PreGeneratorsLoadingEvent;
import net.mcreator.ui.blockly.BlocklyEditorType;

/**
 * Java plugin entry point for Blockly Loot Table.
 */
public class BlocklyLootTablePlugin extends JavaPlugin {

    public static final BlocklyEditorType LOOT_TABLE_EDITOR =
            new BlocklyEditorType("loottable", "ltbl", "loottable_start");

    public BlocklyLootTablePlugin(Plugin plugin) {
        super(plugin);
        addListener(PreGeneratorsLoadingEvent.class, event -> {
            if (BlocklyLoader.INSTANCE.getBlockLoader(LOOT_TABLE_EDITOR) == null) {
                BlocklyLoader.INSTANCE.registerBlockLoader(LOOT_TABLE_EDITOR);
            }
            PluginElementTypes.load();
        });
        System.out.println("[Blockly Loot Table] Plugin loaded");
    }
}
