package com.litqa.blocklyloottable.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Headless smoke test for the parser and JSON writer. Run with
 * {@code gradlew selfCheck}.
 */
public final class LootTableSelfCheck {

    private static int checks;

    private LootTableSelfCheck() {
    }

    public static void main(String[] args) {
        blocklyTranslations();
        itemMapping();
        validBlockTable();
        validEntityTable();
        invalidEntityBlockState();
        invalidBlockKilledByPlayer();
        unknownConditionIsSkipped();
        malformedXmlDoesNotThrow();
        emptyDocumentProducesEmptyTable();
        System.out.println("SELF CHECK PASSED (" + checks + " checks)");
    }

    private static void blocklyTranslations() {
        assertTranslationKeys("texts.properties");
        assertTranslationKeys("texts_zh_CN.properties");
    }

    private static void assertTranslationKeys(String fileName) {
        Properties properties = new Properties();
        try (InputStream input = LootTableSelfCheck.class.getResourceAsStream("/lang/" + fileName)) {
            if (input == null) {
                throw new AssertionError("Missing language resource: " + fileName);
            }
            properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new AssertionError("Could not read language resource: " + fileName, exception);
        }

        for (String key : List.of(
                "blockly.search_loottable",
                "blockly.templates.loottable",
                "blockly.templates.loottable.export",
                "blockly.templates.loottable.import",
                "blockly.templates.loottable.export_failed.message",
                "blockly.templates.loottable.export_failed.title",
                "blockly.templates.loottable.import_failed.message",
                "blockly.templates.loottable.import_failed.title")) {
            check(properties.containsKey(key), fileName + " contains " + key);
        }
    }

    private static void itemMapping() {
        Map<String, String> mapping = new LinkedHashMap<>();
        mapping.put("Blocks.DIAMOND_ORE", "minecraft:diamond_ore");
        mapping.put("Items.ROTTEN_FLESH", "minecraft:rotten_flesh");
        mapping.put("Blocks.OAK_LOG", "minecraft:oak_log");

        BlocklyXmlParser.ParseResult result = BlocklyXmlParser.parse(blockTableXml(
                "<block type=\"loottable_entry_item\">"
                        + "<field name=\"item\">Blocks.DIAMOND_ORE</field>"
                        + "<field name=\"min_count\">1</field>"
                        + "<field name=\"max_count\">1</field>"
                        + "<field name=\"weight\">1</field>"
                        + "</block>", ""), LootTableType.BLOCK, mapping::get);
        check(!result.report().hasErrors(), "valid block table has no errors");
        check(result.model().getPools().size() == 1, "block table has one pool");
        check(result.model().getPools().get(0).getEntries().size() == 1, "block table has one entry");
        check("minecraft:diamond_ore".equals(result.model().getPools().get(0).getEntries().get(0).getItem()),
                "item selector resolves to a registry name");

        String json = LootTableJsonWriter.write(result.model(), "test", "blocks/diamond_ore", true);
        check(json.contains("\"type\": \"minecraft:block\""), "JSON root type is block");
        check(json.contains("\"name\": \"minecraft:diamond_ore\""), "JSON item name is correct");
        check(json.contains("\"random_sequence\": \"test:blocks/diamond_ore\""), "JSON random sequence is written");
    }

    private static void validBlockTable() {
        BlocklyXmlParser.ParseResult result = BlocklyXmlParser.parse(blockTableXml(
                entry("Blocks.DIAMOND_ORE", 1, 1, 1),
                chain(condition("loottable_random_chance", "<field name=\"chance\">0.5</field>"),
                        condition("loottable_survives_explosion", ""),
                        condition("loottable_weather_check",
                                "<field name=\"raining\">TRUE</field><field name=\"thundering\">FALSE</field>"))),
                LootTableType.BLOCK, value -> value);
        check(!result.report().hasErrors(), "valid block conditions have no errors");
        String json = LootTableJsonWriter.write(result.model(), "test", "blocks/ore", false);
        check(json.contains("\"condition\": \"minecraft:random_chance\""), "random chance condition exists");
        check(json.contains("\"chance\": 0.5"), "random chance value exists");
        check(json.contains("\"condition\": \"minecraft:survives_explosion\""), "survives explosion exists");
        check(json.contains("\"raining\": true"), "weather check raining exists");
        check(json.contains("\"thundering\": false"), "weather check thundering exists");
    }

    private static void validEntityTable() {
        BlocklyXmlParser.ParseResult result = BlocklyXmlParser.parse(entityTableXml(
                entry("Items.ROTTEN_FLESH", 1, 2, 10),
                condition("loottable_killed_by_player", "")), LootTableType.ENTITY, value -> value);
        check(!result.report().hasErrors(), "valid entity conditions have no errors");
        String json = LootTableJsonWriter.write(result.model(), "test", "entities/zombie", true);
        check(json.contains("\"type\": \"minecraft:entity\""), "JSON root type is entity");
        check(json.contains("\"condition\": \"minecraft:killed_by_player\""), "killed by player exists");
        check(json.contains("\"max\": 2"), "count range exists");
    }

    private static void invalidEntityBlockState() {
        BlocklyXmlParser.ParseResult result = BlocklyXmlParser.parse(entityTableXml(
                entry("Items.ROTTEN_FLESH", 1, 1, 1),
                condition("loottable_block_state_property",
                        "<field name=\"block\">Blocks.OAK_LOG</field>" +
                                "<field name=\"property\">axis</field>" +
                                "<field name=\"value\">x</field>")), LootTableType.ENTITY, value -> value);
        check(result.report().hasErrors(), "entity table with block state property is an error");
        check(result.report().summary().contains("block_state_property"), "error names the block");
        check(result.report().summary().contains("Entity"), "error names the current type");
    }

    private static void invalidBlockKilledByPlayer() {
        BlocklyXmlParser.ParseResult result = BlocklyXmlParser.parse(blockTableXml(
                entry("Blocks.STONE", 1, 1, 1),
                condition("loottable_killed_by_player", "")), LootTableType.BLOCK, value -> value);
        check(result.report().hasErrors(), "block table with killed by player is an error");
        check(result.report().summary().contains("killed_by_player"), "error names the block");
        check(result.report().summary().contains("Block"), "error names the current type");
    }

    private static void unknownConditionIsSkipped() {
        BlocklyXmlParser.ParseResult result = BlocklyXmlParser.parse(blockTableXml(
                entry("Blocks.STONE", 1, 1, 1),
                condition("loottable_future_condition", "")), LootTableType.BLOCK, value -> value);
        check(!result.report().hasErrors(), "unknown condition is a warning, not an error");
        check(!result.report().getWarningMessages().isEmpty(), "unknown condition produces a warning");
    }

    private static void malformedXmlDoesNotThrow() {
        BlocklyXmlParser.ParseResult result = BlocklyXmlParser.parse(
                "<xml><block", LootTableType.BLOCK, ItemNameResolver.IDENTITY);
        check(result.report().hasErrors(), "malformed XML produces an error");
    }

    private static void emptyDocumentProducesEmptyTable() {
        BlocklyXmlParser.ParseResult result = BlocklyXmlParser.parse("", LootTableType.BLOCK,
                ItemNameResolver.IDENTITY);
        check(!result.report().hasErrors(), "empty document is not an error");
        String json = LootTableJsonWriter.write(result.model(), "test", "blocks/empty", false);
        check(json.contains("\"pools\": []"), "empty document writes an empty pool list");
    }

    private static String blockTableXml(String entryXml, String conditionsXml) {
        return tableXml("loottable_start", entryXml, conditionsXml, "");
    }

    private static String entityTableXml(String entryXml, String conditionsXml) {
        return tableXml("loottable_start", entryXml, "", conditionsXml);
    }

    private static String tableXml(String startType, String entryXml, String poolConditionsXml,
            String entryConditionsXml) {
        return """
                <xml xmlns="https://developers.google.com/blockly/xml">
                  <block type="%s" deletable="false" x="40" y="40">
                    <next>
                      <block type="loottable_pool">
                        <field name="rolls">1</field>
                        <field name="bonus_rolls">0</field>
                        %s
                        <statement name="entries">
                          %s
                        </statement>
                      </block>
                    </next>
                  </block>
                </xml>
                """.formatted(startType, statement("conditions", poolConditionsXml),
                entryWithConditions(entryXml, entryConditionsXml));
    }

    private static String entryWithConditions(String entryXml, String conditionsXml) {
        if (conditionsXml == null || conditionsXml.isBlank()) {
            return entryXml;
        }
        int closing = entryXml.lastIndexOf("</block>");
        if (closing < 0) {
            return entryXml;
        }
        return entryXml.substring(0, closing) + statement("conditions", conditionsXml) + entryXml.substring(closing);
    }

    private static String statement(String name, String blocks) {
        if (blocks == null || blocks.isBlank()) {
            return "";
        }
        return "<statement name=\"" + name + "\">" + blocks + "</statement>";
    }

    private static String entry(String item, int minCount, int maxCount, int weight) {
        return "<block type=\"loottable_entry_item\">"
                + "<field name=\"item\">" + item + "</field>"
                + "<field name=\"min_count\">" + minCount + "</field>"
                + "<field name=\"max_count\">" + maxCount + "</field>"
                + "<field name=\"weight\">" + weight + "</field>"
                + "</block>";
    }

    private static String condition(String type, String fields) {
        return "<block type=\"" + type + "\">" + fields + "</block>";
    }

    private static String chain(String... blocks) {
        String result = blocks[blocks.length - 1];
        for (int index = blocks.length - 2; index >= 0; index--) {
            int closing = blocks[index].lastIndexOf("</block>");
            result = blocks[index].substring(0, closing) + "<next>" + result + "</next>"
                    + blocks[index].substring(closing);
        }
        return result;
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) {
            throw new AssertionError("Self check failed: " + message);
        }
    }
}

