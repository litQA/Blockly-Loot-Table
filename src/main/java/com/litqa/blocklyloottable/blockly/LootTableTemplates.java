package com.litqa.blocklyloottable.blockly;

import com.litqa.blocklyloottable.model.LootTableType;

/**
 * Built-in Blockly documents used by the template selector.
 */
public final class LootTableTemplates {

    private static final String XML_HEADER = "<xml xmlns=\"https://developers.google.com/blockly/xml\">";
    private static final String XML_FOOTER = "</xml>";

    private static final String ORE_BLOCK = XML_HEADER
            + "<block type=\"loottable_start\" deletable=\"false\" x=\"40\" y=\"40\"><next>"
            + "<block type=\"loottable_pool\">"
            + "<field name=\"rolls\">1</field>"
            + "<field name=\"bonus_rolls\">0</field>"
            + "<statement name=\"conditions\">"
            + "<block type=\"loottable_random_chance\"><field name=\"chance\">0.5</field></block>"
            + "</statement>"
            + "<statement name=\"entries\">"
            + "<block type=\"loottable_entry_item\">"
            + "<value name=\"item\"><block type=\"loottable_item_selector\">"
            + "<field name=\"item\">Blocks.DIAMOND_ORE</field></block></value>"
            + "<value name=\"count\"><block type=\"loottable_integer\">"
            + "<field name=\"value\">1</field></block></value>"
            + "<field name=\"weight\">1</field>"
            + "</block>"
            + "</statement>"
            + "</block>"
            + "</next></block>" + XML_FOOTER;

    private static final String ENTITY_RARE_DROP = XML_HEADER
            + "<block type=\"loottable_start\" deletable=\"false\" x=\"40\" y=\"40\"><next>"
            + "<block type=\"loottable_pool\">"
            + "<field name=\"rolls\">1</field>"
            + "<field name=\"bonus_rolls\">0</field>"
            + "<statement name=\"entries\">"
            + "<block type=\"loottable_entry_item\">"
            + "<value name=\"item\"><block type=\"loottable_item_selector\">"
            + "<field name=\"item\">Items.ROTTEN_FLESH</field></block></value>"
            + "<value name=\"count\"><block type=\"loottable_integer\">"
            + "<field name=\"value\">2</field></block></value>"
            + "<field name=\"weight\">1</field>"
            + "<statement name=\"conditions\">"
            + "<block type=\"loottable_killed_by_player\"></block>"
            + "</statement>"
            + "</block>"
            + "</statement>"
            + "</block>"
            + "</next></block>" + XML_FOOTER;

    private LootTableTemplates() {
    }

    /**
     * @param index template combo index
     * @return XML document, or {@code null} for the empty selection
     */
    public static String byIndex(int index) {
        return switch (index) {
            case 1 -> ORE_BLOCK;
            case 2 -> ENTITY_RARE_DROP;
            default -> null;
        };
    }

    public static LootTableType typeByIndex(int index) {
        return index == 2 ? LootTableType.ENTITY : LootTableType.BLOCK;
    }
}
