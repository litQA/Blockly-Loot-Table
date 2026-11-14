package com.litqa.blocklyloottable.model;

import com.litqa.blocklyloottable.util.RegistryNames;
import com.litqa.blocklyloottable.util.VersionCompat;

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
        validBlockStateConditions();
        validEntityPotionCondition();
        validNbtConditions();
        validItemEnchantmentCondition();
        validLogicConditions();
        validScoreboardProviders();
        validConditionValueBlocks();
        validEnchantedItem();
        validItemDataAndContainers();
        validEnchantItemFunctions();
        validFunctionBlocks();
        validWorldConditions();
        validLegacyVersionFormats();
        validSetNameAndPotionBase();
        validPotionRangeCondition();
        validItemConditionsAndDamage();
        validNestedPoolsAndEntries();
        entityAutoPath();
        manualPathProvidedBlock();
        validBlockSourceAliases();
        validCountFunctions();
        integerTruncation();
        strictConnectionChecks();
        invalidEntityBlockState();
        invalidBlockKilledByPlayer();
        validEntityPotionOnBlock();
        duplicateStartIsWarned();
        startBlockIsHidden();
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
                "blockly.templates.loottable.import_failed.title",
                "blockly.block.loottable_provided_blockstate",
                "blockly.block.loottable_block_selector",
                "blockly.block.loottable_entity_dependency",
                "blockly.block.loottable_source_entity_dependency",
                "blockly.category.loottable_components",
                "blockly.category.loottable_items",
                "blockly.category.loottable_numbers",
                "blockly.category.loottable_logic",
                "blockly.block.loottable_block_state_boolean",
                "blockly.block.loottable_block_state_integer",
                "blockly.block.loottable_block_state_enum",
                "blockly.block.loottable_entity_has_potion",
                "blockly.block.loottable_immediate_source_entity_dependency",
                "blockly.block.loottable_source_player_dependency",
                "blockly.block.loottable_entity_mainhand_item",
                "blockly.block.loottable_item_selector",
                "blockly.block.loottable_item_with_enchantment",
                "blockly.block.loottable_item_with_enchantment.level",
                "blockly.block.loottable_item_with_enchantment.mutator.container",
                "blockly.block.loottable_item_with_enchantment.mutator.input",
                "blockly.block.loottable_item_enchant_random",
                "blockly.block.loottable_item_enchant_random_any",
                "blockly.block.loottable_item_enchant_levels",
                "blockly.block.loottable_item_enchant_levels_any",
                "blockly.block.loottable_item_enchant_list.mutator.container",
                "blockly.block.loottable_item_enchant_list.mutator.input",
                "blockly.block.loottable_time_check",
                "blockly.block.loottable_time_day",
                "blockly.block.loottable_time_periodic",
                "blockly.block.loottable_world_dimension",
                "blockly.block.loottable_world_biome",
                "blockly.block.loottable_world_biome_tag",
                "blockly.block.loottable_world_smokey",
                "blockly.block.loottable_world_structure",
                "blockly.block.loottable_world_structure_tag",
                "blockly.block.loottable_world_light",
                "blockly.block.loottable_world_light_range",
                "blockly.block.loottable_world_position_x",
                "blockly.block.loottable_world_position_y",
                "blockly.block.loottable_world_position_z",
                "blockly.block.loottable_world_position_x_range",
                "blockly.block.loottable_world_position_y_range",
                "blockly.block.loottable_world_position_z_range",
                "blockly.block.loottable_world_block_tag",
                "blockly.block.loottable_logic_and",
                "blockly.block.loottable_logic_or",
                "blockly.block.loottable_logic_not",
                "blockly.block.loottable_item_has_enchantment_min",
                "blockly.block.loottable_item_has_enchantment_range",
                "blockly.block.loottable_block_nbt_logic",
                "blockly.block.loottable_block_nbt_number",
                "blockly.block.loottable_block_nbt_text",
                "blockly.block.loottable_entity_nbt_logic",
                "blockly.block.loottable_entity_nbt_number",
                "blockly.block.loottable_entity_nbt_text",
                "blockly.block.loottable_integer",
                "blockly.block.loottable_float",
                "blockly.block.loottable_integer_provider",
                "blockly.block.loottable_float_provider",
                "blockly.block.loottable_uniform",
                "blockly.block.loottable_binomial",
                "blockly.category.loottable_functions",
                "blockly.block.loottable_entry_item_simple",
                "blockly.block.loottable_pool_if",
                "blockly.block.loottable_pool_if_else_if",
                "blockly.block.loottable_pool_if_else",
                "blockly.block.loottable_entry_loot_table",
                "blockly.block.loottable_entry_loot_table_simple",
                "blockly.block.loottable_item_contents_loot",
                "blockly.block.loottable_item_potion",
                "blockly.block.loottable_function_apply_bonus_uniform",
                "blockly.block.loottable_function_apply_bonus_binomial",
                "blockly.block.loottable_function_apply_bonus_ore",
                "blockly.block.loottable_function_enchanted_count_increase",
                "blockly.block.loottable_function_limit_count",
                "blockly.block.loottable_function_set_count_add",
                "blockly.block.loottable_boolean",
                "blockly.block.loottable_block_same",
                "blockly.block.loottable_block_same.tooltip",
                "blockly.block.loottable_pool_mutator.do",
                "blockly.block.loottable_empty_text")) {
            check(properties.containsKey(key), fileName + " contains " + key);
        }
        check(properties.getProperty("blockly.block.loottable_boolean").contains("%1"),
                fileName + " keeps the boolean field placeholder");
        check(properties.getProperty("blockly.block.loottable_empty_text").contains("%1"),
                fileName + " keeps the empty-text field placeholder");
        check(!properties.getProperty("elementgui.blockly_loot_table.warning.circular_reference", "").isBlank(),
                fileName + " has a localized circular-reference warning");
        for (String nbtBlock : new String[]{
                "loottable_block_nbt_logic",
                "loottable_block_nbt_number",
                "loottable_block_nbt_text",
                "loottable_entity_nbt_logic",
                "loottable_entity_nbt_number",
                "loottable_entity_nbt_text"}) {
            String label = properties.getProperty("blockly.block." + nbtBlock, "");
            check(label.contains("%3") && !label.contains("%4"),
                    fileName + " matches the three NBT block arguments");
        }
        check(properties.containsKey("blockly.category.loottable_conditions"),
                fileName + " exposes the conditions parent category");
        check(LootTableSelfCheck.class.getResourceAsStream("/loottable/$loottable_conditions.json") != null,
                "conditions parent category resource exists");
    }

    private static void itemMapping() {
        Map<String, String> mapping = new LinkedHashMap<>();
        mapping.put("Blocks.DIAMOND_ORE", "minecraft:diamond_ore");
        mapping.put("Items.ROTTEN_FLESH", "minecraft:rotten_flesh");
        mapping.put("Blocks.OAK_LOG", "minecraft:oak_log");

        BlocklyXmlParser.ParseResult result = BlocklyXmlParser.parse(blockTableXml(
                entry("Blocks.DIAMOND_ORE", 1, 1), ""), LootTableType.BLOCK, mapping::get);
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
                entry("Blocks.DIAMOND_ORE", 1, 1),
                allOf(condition("loottable_random_chance", "<field name=\"chance\">0.5</field>"),
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

        String simpleXml = blockTableXml(entry("Blocks.DIAMOND_ORE", 1, 1), "")
                .replace("type=\"loottable_pool\"", "type=\"loottable_pool_simple\"");
        BlocklyXmlParser.ParseResult simpleResult = BlocklyXmlParser.parse(
                simpleXml, LootTableType.BLOCK, value -> value);
        check(!simpleResult.report().hasErrors(), "simple pool has no errors");
        check(simpleResult.model().getPools().size() == 1, "simple pool is parsed");
    }

    private static void validEntityTable() {
        BlocklyXmlParser.ParseResult result = BlocklyXmlParser.parse(entityTableXml(
                entry("Items.ROTTEN_FLESH", 2, 10),
                condition("loottable_killed_by_player", "")), LootTableType.ENTITY, value -> value);
        check(!result.report().hasErrors(), "valid entity conditions have no errors");
        String json = LootTableJsonWriter.write(result.model(), "test", "entities/zombie", true);
        check(json.contains("\"type\": \"minecraft:entity\""), "JSON root type is entity");
        check(json.contains("\"condition\": \"minecraft:killed_by_player\""), "killed by player exists");
        check(json.contains("\"function\": \"minecraft:set_count\""), "count function exists");
        check(json.contains("\"count\": 2"), "bare count value is written");
    }

    private static void validBlockStateConditions() {
        ItemNameResolver resolver = value -> switch (value) {
            case "Blocks.OAK_LOG" -> "minecraft:oak_log";
            case "Blocks.STONE" -> "minecraft:stone";
            default -> value;
        };

        BlocklyXmlParser.ParseResult booleanResult = BlocklyXmlParser.parse(blockTableXml(
                entry("Blocks.DIAMOND_ORE", 1, 1),
                condition("loottable_block_state_boolean",
                        "<value name=\"property\"><block type=\"loottable_empty_text\"><field name=\"TEXT\">waterlogged</field></block></value>"
                                + "<value name=\"block\"><block type=\"loottable_block_selector\">"
                                + "<field name=\"block\">Blocks.OAK_LOG</field></block></value>"
                                + "<value name=\"value\"><block type=\"loottable_boolean\">"
                                + "<field name=\"BOOL\">TRUE</field></block></value>")), LootTableType.BLOCK, resolver);
        check(!booleanResult.report().hasErrors(), "boolean block state condition has no errors");
        String booleanJson = LootTableJsonWriter.write(booleanResult.model(), "test", "blocks/log", false);
        check(booleanJson.contains("\"condition\": \"minecraft:block_state_property\""),
                "boolean block state condition type exists");
        check(booleanJson.contains("\"block\": \"minecraft:oak_log\""),
                "block selector value resolves to a registry name");
        check(booleanJson.contains("\"waterlogged\": \"true\""),
                "boolean block state property value exists");

        BlocklyXmlParser.ParseResult integerResult = BlocklyXmlParser.parse(blockTableXml(
                entry("Blocks.DIAMOND_ORE", 1, 1),
                condition("loottable_block_state_integer",
                        "<value name=\"property\"><block type=\"loottable_empty_text\"><field name=\"TEXT\">age</field></block></value>"
                                + "<value name=\"block\"><block type=\"loottable_provided_blockstate\"></block></value>"
                                + "<value name=\"value\"><block type=\"loottable_integer\">"
                                + "<field name=\"value\">3</field></block></value>")), LootTableType.BLOCK, resolver,
                "minecraft:oak_log");
        check(!integerResult.report().hasErrors(), "provided blockstate condition has no errors");
        String integerJson = LootTableJsonWriter.write(integerResult.model(), "test", "blocks/log", false);
        check(integerJson.contains("\"block\": \"minecraft:oak_log\""),
                "provided blockstate uses the element target block");
        check(integerJson.contains("\"age\": \"3\""), "integer block state property value exists");

        BlocklyXmlParser.ParseResult enumResult = BlocklyXmlParser.parse(blockTableXml(
                entry("Blocks.DIAMOND_ORE", 1, 1),
                condition("loottable_block_state_enum",
                        "<value name=\"property\"><block type=\"loottable_empty_text\"><field name=\"TEXT\">half</field></block></value>"
                                + "<value name=\"block\"><block type=\"loottable_block_selector\">"
                                + "<field name=\"block\">Blocks.STONE</field></block></value>"
                                + "<value name=\"value\"><block type=\"loottable_empty_text\">"
                                + "<field name=\"TEXT\">upper</field></block></value>")), LootTableType.BLOCK, resolver);
        check(!enumResult.report().hasErrors(), "enum block state condition has no errors");
        String enumJson = LootTableJsonWriter.write(enumResult.model(), "test", "blocks/stone", false);
        check(enumJson.contains("\"half\": \"upper\""), "enum block state property value exists");
    }

    private static void validEntityPotionCondition() {
        BlocklyXmlParser.ParseResult result = BlocklyXmlParser.parse(entityTableXml(
                entry("Items.ROTTEN_FLESH", 1, 1),
                condition("loottable_entity_has_potion",
                        "<value name=\"entity\"><block type=\"loottable_source_entity_dependency\"></block></value>"
                                + "<field name=\"potion\">minecraft:fire_resistance</field>")),
                LootTableType.ENTITY, value -> value);
        check(!result.report().hasErrors(), "entity potion condition has no errors");
        String json = LootTableJsonWriter.write(result.model(), "test", "entities/zombie", false);
        check(json.contains("\"condition\": \"minecraft:entity_properties\""),
                "entity potion condition type exists");
        check(json.contains("\"entity\": \"attacker\""), "entity source dependency uses the attacker");
        check(json.contains("\"minecraft:fire_resistance\": {}"), "entity potion effect map exists");
    }

    private static void validNbtConditions() {
        BlocklyXmlParser.ParseResult blockResult = BlocklyXmlParser.parse(blockTableXml(
                entry("Blocks.STONE", 1, 1),
                condition("loottable_block_nbt_logic",
                        "<value name=\"tag\"><block type=\"loottable_empty_text\">"
                                + "<field name=\"TEXT\">CustomName</field></block></value>"
                                + "<value name=\"block\"><block type=\"loottable_provided_blockstate\"></block></value>"
                                + "<field name=\"operator\">=</field>"
                                + "<value name=\"value\"><block type=\"loottable_boolean\">"
                                + "<field name=\"BOOL\">TRUE</field></block></value>")),
                LootTableType.BLOCK, value -> value, "minecraft:oak_log");
        check(!blockResult.report().hasErrors(), "block NBT condition has no errors");
        String blockJson = LootTableJsonWriter.write(blockResult.model(), "test", "blocks/log", false);
        check(blockJson.contains("\"condition\": \"minecraft:location_check\""),
                "block NBT condition uses location_check");
        check(blockJson.contains("\"blocks\": [") && blockJson.contains("\"minecraft:oak_log\""),
                "block NBT condition uses the provided target block");
        check(blockJson.contains("\"nbt\": \"{CustomName:1b}\""), "block logic NBT value is SNBT");

        BlocklyXmlParser.ParseResult entityResult = BlocklyXmlParser.parse(entityTableXml(
                entry("Items.ROTTEN_FLESH", 1, 1),
                condition("loottable_entity_nbt_number",
                        "<value name=\"tag\"><block type=\"loottable_empty_text\">"
                                + "<field name=\"TEXT\">Level</field></block></value>"
                                + "<value name=\"entity\"><block type=\"loottable_source_entity_dependency\"></block></value>"
                                + "<field name=\"operator\">=</field>"
                                + "<value name=\"value\"><block type=\"loottable_float\">"
                                + "<field name=\"value\">3.0</field></block></value>")),
                LootTableType.ENTITY, value -> value);
        check(!entityResult.report().hasErrors(), "entity NBT condition has no errors");
        String entityJson = LootTableJsonWriter.write(entityResult.model(), "test", "entities/zombie", false);
        check(entityJson.contains("\"condition\": \"minecraft:entity_properties\""),
                "entity NBT condition uses entity_properties");
        check(entityJson.contains("\"entity\": \"attacker\""), "entity NBT uses the source entity");
        check(entityJson.contains("\"nbt\": \"{Level:3.0}\""), "entity number NBT value is SNBT");

        BlocklyXmlParser.ParseResult textResult = BlocklyXmlParser.parse(entityTableXml(
                entry("Items.ROTTEN_FLESH", 1, 1),
                condition("loottable_entity_nbt_text",
                        "<value name=\"tag\"><block type=\"loottable_empty_text\">"
                                + "<field name=\"TEXT\">Owner</field></block></value>"
                                + "<value name=\"value\"><block type=\"loottable_empty_text\">"
                                + "<field name=\"TEXT\">Steve</field></block></value>")),
                LootTableType.ENTITY, value -> value);
        check(!textResult.report().hasErrors(), "entity text NBT condition has no errors");
        String textJson = LootTableJsonWriter.write(textResult.model(), "test", "entities/zombie", false);
        check(textJson.contains("\"nbt\": \"{Owner:\\\"Steve\\\"}\""), "entity text NBT value is quoted");
    }

    private static void validItemEnchantmentCondition() {
        BlocklyXmlParser.ParseResult rangeResult = BlocklyXmlParser.parse(entityTableXml(
                entry("Items.ROTTEN_FLESH", 1, 1),
                condition("loottable_item_has_enchantment_range",
                        "<value name=\"item\"><block type=\"loottable_entity_mainhand_item\">"
                                + "<value name=\"entity\"><block type=\"loottable_source_entity_dependency\"></block></value>"
                                + "</block></value>"
                                + "<field name=\"enhancement\">minecraft:SHARPNESS</field>"
                                + "<value name=\"min\"><block type=\"loottable_integer\">"
                                + "<field name=\"value\">2</field></block></value>"
                                + "<value name=\"max\"><block type=\"loottable_integer\">"
                                + "<field name=\"value\">5</field></block></value>")),
                LootTableType.ENTITY, value -> value);
        check(!rangeResult.report().hasErrors(), "range enchantment condition has no errors");
        String rangeJson = LootTableJsonWriter.write(rangeResult.model(), "test", "entities/zombie", false);
        check(rangeJson.contains("\"condition\": \"minecraft:entity_properties\""),
                "main-hand enchantment uses entity_properties");
        check(rangeJson.contains("\"entity\": \"attacker\""),
                "main-hand enchantment uses the entity source");
        check(rangeJson.contains("\"equipment\"") && rangeJson.contains("\"mainhand\""),
                "main-hand enchantment uses the mainhand slot");
        check(rangeJson.contains("\"enchantments\": [\n")
                        && rangeJson.contains("\"minecraft:sharpness\""),
                "enchantment id is lower-case and uses the official plural list");
        check(rangeJson.contains("\"levels\": {") && rangeJson.contains("\"min\": 2"),
                "range enchantment has a minimum level");
        check(rangeJson.contains("\"max\": 5"), "range enchantment has a maximum level");
        check(!rangeJson.contains("\"enchantment\":"),
                "enchantment predicate does not use a singular field");

        BlocklyXmlParser.ParseResult blockResult = BlocklyXmlParser.parse(blockTableXml(
                entry("Blocks.STONE", 1, 1),
                condition("loottable_item_has_enchantment_min",
                        "<value name=\"item\"><block type=\"loottable_entity_mainhand_item\">"
                                + "<value name=\"entity\"><block type=\"loottable_source_entity_dependency\"></block></value>"
                                + "</block></value>"
                                + "<field name=\"enhancement\">minecraft:fortune</field>"
                                + "<value name=\"min\"><block type=\"loottable_integer\">"
                                + "<field name=\"value\">1</field></block></value>")),
                LootTableType.BLOCK, value -> value);
        check(!blockResult.report().hasErrors(), "block main-hand enchantment has no errors");
        check(!blockResult.report().summary().contains("unavailable"),
                "block source entity is available for the breaker");
        String blockJson = LootTableJsonWriter.write(blockResult.model(), "test", "blocks/stone", false);
        check(blockJson.contains("\"entity\": \"this\""),
                "block main-hand enchantment uses the breaker as this");
        check(blockJson.contains("\"equipment\"") && blockJson.contains("\"mainhand\""),
                "block main-hand enchantment uses the breaker equipment");
        check(blockJson.contains("\"min\": 1"), "minimum enchantment level is written");

        BlocklyXmlParser.ParseResult toolResult = BlocklyXmlParser.parse(blockTableXml(
                entry("Blocks.STONE", 1, 1),
                condition("loottable_item_has_enchantment_min",
                        "<field name=\"enhancement\">minecraft:fortune</field>"
                                + "<value name=\"min\"><block type=\"loottable_integer\">"
                                + "<field name=\"value\">2</field></block></value>")),
                LootTableType.BLOCK, value -> value);
        check(!toolResult.report().hasErrors(), "tool enchantment condition has no errors");
        String toolJson = LootTableJsonWriter.write(toolResult.model(), "test", "blocks/stone", false);
        check(toolJson.contains("\"condition\": \"minecraft:match_tool\""),
                "tool enchantment uses match_tool");
        check(toolJson.contains("\"min\": 2"), "tool enchantment has a minimum level");
    }

    private static void validNestedPoolsAndEntries() {
        String simpleEntry = "<block type=\"loottable_entry_item_simple\">"
                + "<value name=\"item\"><block type=\"loottable_item_selector\">"
                + "<field name=\"item\">Blocks.DIAMOND_ORE</field></block></value>"
                + "<value name=\"count\"><block type=\"loottable_integer\">"
                + "<field name=\"value\">2</field></block></value>"
                + "<field name=\"weight\">4</field></block>";
        BlocklyXmlParser.ParseResult simpleResult = BlocklyXmlParser.parse(blockTableXml(simpleEntry, ""),
                LootTableType.BLOCK, value -> value);
        check(!simpleResult.report().hasErrors(), "simple item entry has no errors");
        check(simpleResult.model().getPools().get(0).getEntries().get(0).getWeight() == 4,
                "simple item entry keeps its weight");

        String elsePool = """
                <xml xmlns="https://developers.google.com/blockly/xml">
                  <block type="loottable_start" deletable="false">
                    <next>
                      <block type="loottable_pool_if_else">
                        <mutation elseif="0" else="1"></mutation>
                        <field name="rolls">1</field>
                        <field name="bonus_rolls">0</field>
                        <value name="condition0">
                          <block type="loottable_random_chance"><field name="chance">0.5</field></block>
                        </value>
                        <statement name="entries0">
                          <block type="loottable_entry_item_simple">
                            <value name="item"><block type="loottable_item_selector">
                              <field name="item">Blocks.DIAMOND_ORE</field>
                            </block></value>
                            <value name="count"><block type="loottable_integer">
                              <field name="value">1</field>
                            </block></value>
                            <field name="weight">1</field>
                          </block>
                        </statement>
                        <statement name="entries_else">
                          <block type="loottable_entry_item_simple">
                            <value name="item"><block type="loottable_item_selector">
                              <field name="item">Blocks.STONE</field>
                            </block></value>
                            <value name="count"><block type="loottable_integer">
                              <field name="value">1</field>
                            </block></value>
                            <field name="weight">1</field>
                          </block>
                        </statement>
                      </block>
                    </next>
                  </block>
                </xml>
                """;
        BlocklyXmlParser.ParseResult elseResult = BlocklyXmlParser.parse(elsePool, LootTableType.BLOCK,
                value -> value);
        check(!elseResult.report().hasErrors(), "else pool has no errors");
        LootTableModel.Entry elseAlternatives = elseResult.model().getPools().get(0).getEntries().get(0);
        check("minecraft:alternatives".equals(elseAlternatives.getType()),
                "else pool emits an alternatives entry");
        check(elseAlternatives.getChildren().size() == 2,
                "else pool keeps its condition branch and else branch");
        check(elseAlternatives.getChildren().get(1).getConditions().isEmpty(),
                "else branch has no conditions");

        String alternativePool = """
                <xml xmlns="https://developers.google.com/blockly/xml">
                  <block type="loottable_start" deletable="false">
                    <next>
                      <block type="loottable_pool_if">
                        <mutation elseif="0" else="0"></mutation>
                        <field name="rolls">1</field>
                        <field name="bonus_rolls">0</field>
                        <value name="condition0">
                          <block type="loottable_random_chance"><field name="chance">0.5</field></block>
                        </value>
                        <statement name="entries0">
                          <block type="loottable_entry_loot_table">
                            <value name="table"><block type="loottable_empty_text">
                              <field name="TEXT">minecraft:entities/zombie</field>
                            </block></value>
                            <field name="weight">2</field>
                            <value name="conditions">
                              <block type="loottable_random_chance"><field name="chance">0.25</field></block>
                            </value>
                            <statement name="functions">
                              <block type="loottable_function_limit_count">
                                <value name="limit"><block type="loottable_integer">
                                  <field name="value">1</field>
                                </block></value>
                              </block>
                            </statement>
                          </block>
                        </statement>
                      </block>
                    </next>
                  </block>
                </xml>
                """;
        BlocklyXmlParser.ParseResult poolResult = BlocklyXmlParser.parse(alternativePool, LootTableType.BLOCK,
                value -> value);
        check(!poolResult.report().hasErrors(), "alternative pool has no errors");
        String poolJson = LootTableJsonWriter.write(poolResult.model(), "test", "blocks/choice", false);
        check(poolJson.contains("\"type\": \"minecraft:alternatives\""), "alternative pool uses alternatives");
        check(poolJson.contains("\"type\": \"minecraft:group\""), "alternative branch uses group");
        check(poolJson.contains("\"type\": \"minecraft:loot_table\""), "nested loot table entry is written");
        check(poolJson.contains("\"value\": \"minecraft:entities/zombie\""),
                "nested loot table uses the 1.21 value field");
        check(poolResult.report().summary().contains(BlocklyXmlParser.getCircularReferenceWarning()),
                "nested loot table warns about circular references");
        check(poolJson.contains("\"function\": \"minecraft:limit_count\""),
                "nested loot table functions are written");
        check(countOccurrences(poolJson, "\"condition\": \"minecraft:random_chance\"") >= 2,
                "nested loot table entry writes its conditions");

        VersionCompat.Features v1205 = new VersionCompat.Features(
                true, true, true, true, true, true, true, true, false);
        String legacyJson = LootTableJsonWriter.write(poolResult.model(), "test", "blocks/choice", false, v1205);
        check(legacyJson.contains("\"name\": \"minecraft:entities/zombie\"")
                        && !legacyJson.contains("\"value\": \"minecraft:entities/zombie\""),
                "nested loot table uses name before 1.21");

        String contentsEntry = entryWithItem(
                "<block type=\"loottable_item_contents_loot\">"
                        + "<value name=\"item\"><block type=\"loottable_item_selector\">"
                        + "<field name=\"item\">minecraft:shulker_box</field></block></value>"
                        + "<value name=\"table\"><block type=\"loottable_empty_text\">"
                        + "<field name=\"TEXT\">minecraft:chests/simple_dungeon</field></block></value>"
                        + "</block>",
                "<value name=\"count\"><block type=\"loottable_integer\">"
                        + "<field name=\"value\">1</field></block></value>",
                1);
        BlocklyXmlParser.ParseResult contentsResult = BlocklyXmlParser.parse(blockTableXml(contentsEntry, ""),
                LootTableType.BLOCK, value -> value);
        check(!contentsResult.report().hasErrors(), "loot-table contents item has no errors");
        String contentsJson = LootTableJsonWriter.write(contentsResult.model(), "test", "blocks/contents", false);
        check(contentsJson.contains("\"function\": \"minecraft:set_contents\""),
                "loot-table contents uses set_contents");
        check(contentsJson.contains("\"type\": \"minecraft:loot_table\"")
                        && contentsJson.contains("\"value\": \"minecraft:chests/simple_dungeon\""),
                "loot-table contents embeds a nested loot table");
        check(contentsJson.contains("\"name\": \"minecraft:shulker_box\""),
                "loot-table contents defaults the container item");

        String nbtBlock = resourceText("/loottable/loottable_block_nbt_logic.json");
        check(!nbtBlock.contains("\"operator\""), "NBT blocks do not expose an operator field");
        String container = resourceText("/loottable/loottable_item_container.json");
        check(container.contains("Blocks.SHULKER_BOX"), "container block defaults to a shulker box");
        String simpleResource = resourceText("/loottable/loottable_entry_item_simple.json");
        check(simpleResource.contains("\"name\": \"weight\""), "simple item entry exposes a weight field");
    }

    private static void entityAutoPath() {
        String path = RegistryNames.autoPath(null, LootTableType.ENTITY,
                "minecraft:entities/EntityType.ZOMBIE", "test");
        check("minecraft:entities/zombie".equals(path),
                "entity automatic path strips the EntityType mapping");
    }

    private static void manualPathProvidedBlock() {
        check("mymod:custom_ore".equals(
                        RegistryNames.blockFromLootTablePath("mymod:blocks/custom_ore")),
                "manual block path supplies the provided blockstate");
        check("".equals(RegistryNames.blockFromLootTablePath("mymod:entities/zombie")),
                "entity paths do not supply a provided blockstate");
    }

    private static void validBlockSourceAliases() {
        assertEntityAlias("loottable_source_entity_dependency", "this", "block source entity is this");
        assertEntityAlias("loottable_immediate_source_entity_dependency", "this",
                "block immediate source entity is this");
        assertEntityAlias("loottable_source_player_dependency", "this", "block source player is this");
        assertEntityAlias("loottable_source_entity_dependency", "attacker",
                "entity source entity is the attacker");
        assertEntityAlias("loottable_immediate_source_entity_dependency", "direct_attacker",
                "entity immediate source is the direct attacker");
        assertEntityAlias("loottable_source_player_dependency", "attacking_player",
                "entity source player is the attacking player");
    }

    private static void assertEntityAlias(String dependency, String expected, String message) {
        LootTableType type = "this".equals(expected) ? LootTableType.BLOCK : LootTableType.ENTITY;
        String xml = type == LootTableType.BLOCK
                ? blockTableXml(entry("Blocks.STONE", 1, 1), condition("loottable_entity_has_potion",
                        "<value name=\"entity\"><block type=\"" + dependency + "\"></block></value>"
                                + "<field name=\"potion\">minecraft:fire_resistance</field>"))
                : entityTableXml(entry("Items.ROTTEN_FLESH", 1, 1), condition("loottable_entity_has_potion",
                        "<value name=\"entity\"><block type=\"" + dependency + "\"></block></value>"
                                + "<field name=\"potion\">minecraft:fire_resistance</field>"));
        BlocklyXmlParser.ParseResult result = BlocklyXmlParser.parse(xml, type, value -> value);
        check(!result.report().hasErrors(), message + " has no errors");
        check(!result.report().summary().contains("unavailable"),
                message + " is available");
        String json = LootTableJsonWriter.write(result.model(), "test",
                type == LootTableType.BLOCK ? "blocks/stone" : "entities/zombie", false);
        check(json.contains("\"entity\": \"" + expected + "\""), message + " is written");
    }

    private static void validCountFunctions() {
        String countXml = "<value name=\"count\"><block type=\"loottable_uniform\">"
                + "<value name=\"min\"><block type=\"loottable_integer\">"
                + "<field name=\"value\">1</field></block></value>"
                + "<value name=\"max\"><block type=\"loottable_float_provider\">"
                + "<field name=\"value\">3.5</field></block></value>"
                + "</block></value>";
        String entryXml = entryWithFunctions(entryWithCount("Items.ROTTEN_FLESH", countXml, 1), chain(
                "<block type=\"loottable_function_set_count_add\">"
                        + "<value name=\"count\"><block type=\"loottable_integer_provider\">"
                        + "<field name=\"value\">2</field></block></value>"
                        + "</block>"));
        BlocklyXmlParser.ParseResult result = BlocklyXmlParser.parse(entityTableXml(entryXml, ""),
                LootTableType.ENTITY, value -> value);
        check(!result.report().hasErrors(), "count provider has no errors");
        check(result.model().getPools().get(0).getEntries().get(0).getFunctions().size() == 1,
                "add value function was parsed");
        String json = LootTableJsonWriter.write(result.model(), "test", "entities/zombie", false);
        check(json.contains("\"type\": \"minecraft:uniform\""), "uniform count provider is written");
        check(json.contains("\"min\": 1"), "bare integer minimum is written as a number");
        check(json.contains("\"max\": {"), "constant provider maximum is written as an object");
        check(json.contains("\"value\": 3.5"), "float provider maximum keeps its value");
        check(json.contains("\"add\": true"), "add value function is written");

        String nestedXml = "<value name=\"count\"><block type=\"loottable_uniform\">"
                + "<value name=\"min\"><block type=\"loottable_binomial\">"
                + "<value name=\"n\"><block type=\"loottable_integer\">"
                + "<field name=\"value\">2</field></block></value>"
                + "<value name=\"p\"><block type=\"loottable_float\">"
                + "<field name=\"value\">1</field></block></value>"
                + "</block></value>"
                + "<value name=\"max\"><block type=\"loottable_integer\">"
                + "<field name=\"value\">4</field></block></value>"
                + "</block></value>";
        BlocklyXmlParser.ParseResult nested = BlocklyXmlParser.parse(
                entityTableXml(entryWithCount("Items.ROTTEN_FLESH", nestedXml, 1), ""),
                LootTableType.ENTITY, value -> value);
        check(!nested.report().hasErrors(), "nested binomial provider has no errors");
        String nestedJson = LootTableJsonWriter.write(nested.model(), "test", "entities/zombie", false);
        check(nestedJson.contains("\"type\": \"minecraft:binomial\""), "binomial provider is nested");
        check(nestedJson.contains("\"n\": 2"), "binomial trials are a bare integer");
        check(nestedJson.contains("\"p\": 1.0"), "binomial probability keeps a float shape");
        check(nestedJson.contains("\"max\": 4"), "uniform maximum keeps a bare number");
    }

    private static void integerTruncation() {
        String countXml = "<value name=\"count\"><block type=\"loottable_integer\">"
                + "<field name=\"value\">2.9</field></block></value>";
        BlocklyXmlParser.ParseResult result = BlocklyXmlParser.parse(
                entityTableXml(entryWithCount("Items.ROTTEN_FLESH", countXml, 1), ""),
                LootTableType.ENTITY, value -> value);
        check(!result.report().hasErrors(), "integer truncation is a warning, not an error");
        check(result.report().summary().contains("fractional part was removed"),
                "integer truncation reports a warning");
        String json = LootTableJsonWriter.write(result.model(), "test", "entities/zombie", false);
        check(json.contains("\"count\": 2"), "integer count truncates the decimal part");
        check(!json.contains("2.9"), "integer count does not keep the decimal part");
        check(!json.contains("\"type\": \"minecraft:constant\""),
                "terminal integer is not wrapped in a constant provider");
    }

    private static void strictConnectionChecks() {
        String start = resourceText("/loottable/loottable_start.json");
        String pool = resourceText("/loottable/loottable_pool.json");
        String entryBlock = resourceText("/loottable/loottable_entry_item.json");
        check(start.contains("\"nextStatement\": \"LootTablePool\""), "start only accepts pools");
        check(pool.contains("\"previousStatement\": \"LootTablePool\"")
                        && pool.contains("\"nextStatement\": \"LootTablePool\""),
                "pools only chain to pools");
        check(pool.contains("\"check\": \"LootTableEntry\""), "pool entries only accept entries");
        check(pool.contains("\"check\": \"LootTableCondition\""), "pool conditions only accept conditions");
        check(entryBlock.contains("\"previousStatement\": \"LootTableEntry\"")
                        && entryBlock.contains("\"nextStatement\": \"LootTableEntry\""),
                "entries only chain to entries");
        check(entryBlock.contains("\"check\": \"LootTableCondition\""),
                "entry conditions only accept conditions");
        check(entryBlock.contains("\"check\": \"LootTableFunction\""),
                "entry functions only accept functions");
        check(!start.contains("\"nextStatement\": {") && !pool.contains("\"previousStatement\": {")
                        && !entryBlock.contains("\"previousStatement\": {"),
                "statement checks use Blockly string form");
        check(resourceText("/loottable/loottable_logic_not.json").contains("\"check\": \"LootTableCondition\""),
                "logic terms only accept conditions");
        check(resourceText("/loottable/$loottable_components.json").contains("\"color\": 350"),
                "minecraft components category is red");
        check(resourceText("/loottable/$loottable_items.json").contains("\"color\": 350"),
                "item category exists");
        check(resourceText("/loottable/$loottable_numbers.json").contains("\"color\": 230"),
                "number category exists");
        check(resourceText("/loottable/$loottable_logic.json").contains("\"color\": 210"),
                "logic category exists");
        check(resourceText("/loottable/$loottable_conditions.json").contains("\"color\": 120"),
                "conditions parent category exists");
        check(resourceText("/loottable/$loottable_blocks.json").contains("\"color\": 60")
                        && resourceText("/loottable/$loottable_blocks.json")
                        .contains("\"parent_category\": \"loottable_conditions\""),
                "block conditions category is visible");
        check(resourceText("/loottable/$loottable_entities.json").contains("\"color\": 195")
                        && resourceText("/loottable/$loottable_entities.json")
                        .contains("\"parent_category\": \"loottable_conditions\""),
                "entity conditions category is visible");
        check(resourceText("/loottable/$loottable_world.json").contains("\"color\": 35"),
                "world conditions use the earth-yellow colour");
        check(resourceText("/loottable/loottable_world_light.json").contains("\"colour\": 120"),
                "world condition blocks use the condition green");
        check(resourceText("/loottable/loottable_pool_simple.json").contains("\"colour\": 120"),
                "simple pools use the deep-green colour");
        check(resourceText("/loottable/$loottable_functions.json").contains("\"color\": 280"),
                "function category exists");
        for (String functionFile : new String[]{
                "loottable_function_set_count_add.json",
                "loottable_function_apply_bonus_uniform.json",
                "loottable_function_apply_bonus_binomial.json",
                "loottable_function_apply_bonus_ore.json",
                "loottable_function_enchanted_count_increase.json",
                "loottable_function_limit_count.json"}) {
            check(resourceText("/loottable/" + functionFile).contains("\"colour\": 280"),
                    functionFile + " uses the purple function colour");
        }
        check(resourceText("/lang/texts.properties")
                        .contains("loottable_entry_item_simple=Item %1, count %2"),
                "simple entry label omits Simple");
        check(resourceText("/loottable/loottable_function_set_count_add.json")
                        .contains("\"toolbox_id\": \"loottable_functions\""),
                "add value is in the function category");
        String simpleEntry = resourceText("/loottable/loottable_entry_item_simple.json");
        check(simpleEntry.contains("\"name\": \"item\"")
                        && simpleEntry.contains("\"name\": \"count\"")
                        && !simpleEntry.contains("LootTableCondition")
                        && !simpleEntry.contains("LootTableFunction"),
                "simple entry only has item and count inputs");
        String bonusUniform = resourceText("/loottable/loottable_function_apply_bonus_uniform.json");
        check(bonusUniform.contains("\"name\": \"bonus\"")
                        && bonusUniform.contains("\"check\": \"Number\"")
                        && bonusUniform.contains("\"toolbox_id\": \"loottable_functions\""),
                "uniform bonus uses a literal number multiplier");
        check(resourceText("/loottable/loottable_function_enchanted_count_increase.json")
                        .contains("\"NumberProvider\""),
                "enchanted count increase accepts a provider count");
        check(resourceText("/loottable/loottable_function_limit_count.json")
                        .contains("\"NumberProvider\""),
                "limit count accepts a provider limit");
        check(resourceText("/loottable/loottable_item_enchant_random_any.json")
                        .contains("\"name\": \"min\"")
                        && resourceText("/loottable/loottable_item_enchant_random_any.json")
                        .contains("\"name\": \"max\""),
                "random any enchant exposes a level range");
        check(resourceText("/loottable/loottable_world_block_tag.json")
                        .contains("\"colour\": 120")
                        && resourceText("/loottable/loottable_world_block_tag.json")
                        .contains("\"toolbox_id\": \"loottable_blocks\""),
                "block tag is a green block condition");
        check(resourceText("/lang/texts.properties").contains("Current structure is tagged"),
                "structure tag label is current structure");
        check(resourceText("/loottable/loottable_item_selector.json")
                        .contains("\"output\": \"MCItem\""),
                "item selector is an item value block");
        check(entryBlock.contains("\"name\": \"item\""),
                "item entry has an item input");
        check(resourceText("/loottable/loottable_entity_has_potion.json").contains("\"colour\": 120"),
                "condition blocks use the condition green");
        check(entryBlock.contains("\"colour\": 350"), "item entry uses the native item color");
        check(entryBlock.contains("\"NumberProvider\""), "item count accepts number providers");
        check(resourceText("/loottable/loottable_uniform.json")
                        .contains("\"output\": \"NumberProvider\""),
                "uniform is a number provider");
        check(resourceText("/loottable/loottable_uniform.json")
                        .contains("\"NumberProvider\""),
                "uniform inputs accept nested providers");
        check(resourceText("/loottable/loottable_binomial.json")
                        .contains("\"output\": \"NumberProvider\""),
                "binomial is a number provider");
        check(resourceText("/loottable/loottable_binomial.json")
                        .contains("\"NumberProvider\""),
                "binomial inputs accept nested providers");
        String enchantedItem = resourceText("/loottable/loottable_item_with_enchantment.json");
        check(enchantedItem.contains("\"output\": \"MCItem\""),
                "enchanted item is an item value block");
        check(enchantedItem.contains("\"mutator\": \"loottable_item_with_enchantment_mutator\""),
                "enchanted item uses its mutator");
        check(enchantedItem.contains("\"repeating_fields\""),
                "enchanted item has repeating fields");
        check(resourceText("/loottable/loottable_item_enchant_random.json")
                        .contains("\"output\": \"MCItem\""),
                "random enchant item is an item value block");
        check(resourceText("/loottable/loottable_item_enchant_random_any.json")
                        .contains("\"field_checkbox\""),
                "random any enchant item exposes a treasure checkbox");
        check(resourceText("/loottable/loottable_item_enchant_levels.json")
                        .contains("\"check\": [")
                        && resourceText("/loottable/loottable_item_enchant_levels.json")
                        .contains("\"NumberProvider\""),
                "level enchant item accepts provider levels");
        check(resourceText("/loottable/loottable_time_periodic.json")
                        .contains("\"name\": \"period\""),
                "periodic time check exposes a period input");
        check(resourceText("/loottable/$loottable_world.json").contains("\"color\""),
                "world condition category exists");
        check(resourceText("/loottable/loottable_integer.json").contains("\"output\": \"Number\""),
                "integer number has a number output");
        check(resourceText("/loottable/loottable_float.json").contains("\"output\": \"Number\""),
                "float number has a number output");
        check(resourceText("/loottable/loottable_integer_provider.json")
                        .contains("\"output\": \"NumberProvider\""),
                "integer provider has a provider output");
        check(resourceText("/loottable/loottable_float_provider.json")
                        .contains("\"output\": \"NumberProvider\""),
                "float provider has a provider output");
        check(resourceText("/loottable/loottable_float.json").contains("\"field_loottable_float\"")
                        && resourceText("/loottable/loottable_float.json").contains("\"text\": \"1.0\""),
                "float number defaults to a visible 1.0");
        check(resourceText("/loottable/loottable_float_provider.json").contains("\"field_loottable_float\"")
                        && resourceText("/loottable/loottable_float_provider.json").contains("\"text\": \"1.0\""),
                "float provider defaults to a visible 1.0");
        check(resourceText("/blockly/js/loottable_float_field.js")
                        .contains("fieldRegistry.register('field_loottable_float'"),
                "custom float field is registered");
        String enchantmentMin = resourceText("/loottable/loottable_item_has_enchantment_min.json");
        String enchantmentRange = resourceText("/loottable/loottable_item_has_enchantment_range.json");
        check(enchantmentMin.contains("\"name\": \"min\"")
                        && enchantmentMin.contains("\"check\": \"Number\""),
                "minimum enchantment block has an integer level socket");
        check(enchantmentRange.contains("\"name\": \"min\"")
                        && enchantmentRange.contains("\"name\": \"max\""),
                "range enchantment block has minimum and maximum sockets");
        check(enchantmentMin.contains("\"check\": \"MCItem\"")
                        && enchantmentRange.contains("\"check\": \"MCItem\""),
                "enchantment blocks have an item input");
        check(resourceText("/blockly/js/loottable_mutators.js")
                        .contains("loottable_item_with_enchantment_mutator"),
                "enchanted item mutator script is registered");
    }

    private static void validLogicConditions() {
        String logic = condition("loottable_logic_and",
                "<mutation inputs=\"2\"></mutation>"
                        + value("condition0",
                        condition("loottable_random_chance", "<field name=\"chance\">0.5</field>"))
                        + value("condition1", condition("loottable_survives_explosion", "")));
        BlocklyXmlParser.ParseResult result = BlocklyXmlParser.parse(blockTableXml(
                entry("Blocks.STONE", 1, 1), logic), LootTableType.BLOCK, value -> value);
        check(!result.report().hasErrors(), "logic blocks parse without errors");
        String json = LootTableJsonWriter.write(result.model(), "test", "blocks/stone", false);
        check(json.contains("\"condition\": \"minecraft:all_of\""), "and emits all_of");
        check(json.contains("\"terms\": ["), "and emits a term list");

        String not = condition("loottable_logic_not",
                value("condition",
                        condition("loottable_random_chance", "<field name=\"chance\">0.5</field>")));
        BlocklyXmlParser.ParseResult notResult = BlocklyXmlParser.parse(blockTableXml(
                entry("Blocks.STONE", 1, 1), not), LootTableType.BLOCK, value -> value);
        check(!notResult.report().hasErrors(), "single-term not parses without errors");
        String notJson = LootTableJsonWriter.write(notResult.model(), "test", "blocks/stone", false);
        check(notJson.contains("\"condition\": \"minecraft:inverted\""), "not emits inverted");
        check(notJson.contains("\"condition\": \"minecraft:random_chance\""),
                "not keeps its single condition term");
    }

    private static void validScoreboardProviders() {
        String entityScore = "<block type=\"loottable_score_entity\">"
                + "<value name=\"entity\"><block type=\"loottable_source_entity_dependency\"></block></value>"
                + "<value name=\"score\"><block type=\"loottable_empty_text\">"
                + "<field name=\"TEXT\">points</field></block></value>"
                + "<value name=\"scale\"><block type=\"loottable_float\">"
                + "<field name=\"value\">2.0</field></block></value></block>";
        BlocklyXmlParser.ParseResult entityResult = BlocklyXmlParser.parse(entityTableXml(
                entryWithCount("Items.ROTTEN_FLESH", value("count", entityScore), 1), ""),
                LootTableType.ENTITY, value -> value);
        check(!entityResult.report().hasErrors(), "entity scoreboard provider has no errors");
        String entityJson = LootTableJsonWriter.write(entityResult.model(), "test", "entities/zombie", false);
        check(entityJson.contains("\"type\": \"minecraft:score\"")
                        && entityJson.contains("\"target\": \"attacker\"")
                        && entityJson.contains("\"score\": \"points\"")
                        && entityJson.contains("\"scale\": 2.0"),
                "entity scoreboard provider writes context target and scale");

        String playerScore = "<block type=\"loottable_score_player\">"
                + "<value name=\"player\"><block type=\"loottable_empty_text\">"
                + "<field name=\"TEXT\">Steve</field></block></value>"
                + "<value name=\"score\"><block type=\"loottable_empty_text\">"
                + "<field name=\"TEXT\">points</field></block></value></block>";
        BlocklyXmlParser.ParseResult playerResult = BlocklyXmlParser.parse(blockTableXml(
                entryWithCount("Blocks.STONE", value("count", playerScore), 1), ""),
                LootTableType.BLOCK, value -> value);
        check(!playerResult.report().hasErrors(), "player scoreboard provider has no errors");
        String playerJson = LootTableJsonWriter.write(playerResult.model(), "test", "blocks/stone", false);
        check(playerJson.contains("\"type\": \"minecraft:fixed\"")
                        && playerJson.contains("\"name\": \"Steve\""),
                "player scoreboard provider writes a fixed name");

        String uuidScore = "<block type=\"loottable_score_uuid\">"
                + "<value name=\"uuid\"><block type=\"loottable_empty_text\">"
                + "<field name=\"TEXT\">00000000-0000-0000-0000-000000000000</field></block></value>"
                + "<value name=\"score\"><block type=\"loottable_empty_text\">"
                + "<field name=\"TEXT\">points</field></block></value></block>";
        BlocklyXmlParser.ParseResult uuidResult = BlocklyXmlParser.parse(blockTableXml(
                entryWithCount("Blocks.STONE", value("count", uuidScore), 1), ""),
                LootTableType.BLOCK, value -> value);
        check(!uuidResult.report().hasErrors(), "UUID scoreboard provider has no errors");
        String uuidJson = LootTableJsonWriter.write(uuidResult.model(), "test", "blocks/stone", false);
        check(uuidJson.contains("\"name\": \"00000000-0000-0000-0000-000000000000\""),
                "UUID scoreboard provider writes a fixed UUID name");
        check(resourceText("/loottable/$loottable_scoreboard.json").contains("\"parent_category\": \"loottable_numbers\"")
                        && resourceText("/loottable/$loottable_scoreboard.json").contains("\"color\": 230"),
                "scoreboard category is nested under numbers and uses the number blue");
    }

    private static void validConditionValueBlocks() {
        for (String file : List.of("loottable_random_chance.json", "loottable_block_nbt_logic.json",
                "loottable_entity_has_potion.json", "loottable_item_same.json",
                "loottable_damage_tag.json", "loottable_world_dimension.json",
                "loottable_logic_and.json", "loottable_logic_not.json")) {
            String json = resourceText("/loottable/" + file);
            check(json.contains("\"output\": \"LootTableCondition\""),
                    file + " exposes a condition value");
            check(!json.contains("\"previousStatement\"") && !json.contains("\"nextStatement\""),
                    file + " has no statement connectors");
        }
        check(resourceText("/loottable/loottable_entry_item.json").contains("\"name\": \"conditions\"")
                        && resourceText("/loottable/loottable_entry_item.json").contains("\"type\": \"input_value\""),
                "entry condition socket is a value input");
        check(resourceText("/loottable/loottable_logic_and.json").contains("\"toolbox_init\": [\"<mutation inputs=\\\"2\\\"></mutation>\"]")
                        && !resourceText("/loottable/loottable_logic_and.json").contains("loottable_random_chance"),
                "all conditions starts with two empty inputs");
        check(resourceText("/loottable/loottable_logic_not.json").contains("\"toolbox_init\": []")
                        && !resourceText("/loottable/loottable_logic_not.json").contains("loottable_random_chance"),
                "not starts with one empty input");
        check(resourceText("/loottable/loottable_logic_and.json").contains("\"colour\": 120")
                        && resourceText("/loottable/loottable_logic_not.json").contains("\"colour\": 120")
                        && resourceText("/loottable/loottable_boolean.json").contains("%{BKY_LOGIC_HUE}"),
                "condition combiners are green and the boolean selector keeps its logic colour");
        check(resourceText("/loottable/loottable_score_entity.json").contains("%{BKY_MATH_HUE}")
                        && resourceText("/loottable/loottable_score_player.json").contains("%{BKY_MATH_HUE}")
                        && resourceText("/loottable/loottable_score_uuid.json").contains("%{BKY_MATH_HUE}"),
                "score providers use the number blue");
        String sameBlock = condition("loottable_block_same",
                value("block", "<block type=\"loottable_provided_blockstate\"></block>")
                        + value("other", "<block type=\"loottable_block_selector\">"
                                + "<field name=\"block\">minecraft:oak_log</field></block>"));
        BlocklyXmlParser.ParseResult sameBlockResult = BlocklyXmlParser.parse(blockTableXml(
                entry("Blocks.STONE", 1, 1), sameBlock), LootTableType.BLOCK, value -> value,
                "minecraft:oak_log");
        check(!sameBlockResult.report().hasErrors(), "same-block condition parses without errors");
        String sameBlockJson = LootTableJsonWriter.write(sameBlockResult.model(), "test", "blocks/oak", false);
        check(sameBlockJson.contains("\"condition\": \"minecraft:block_state_property\"")
                        && sameBlockJson.contains("\"block\": \"minecraft:oak_log\""),
                "same-block condition writes a block-state predicate");

        String compactPoolIf = resourceText("/loottable/loottable_pool_if.json")
                .replaceAll("\\s+", "");
        check(compactPoolIf.contains("\"statements\":[{\"name\":\"entries0\"}]"),
                "advanced pool statement metadata uses named objects");

        String mutatorJs = resourceText("/blockly/js/loottable_mutators.js");
        check(mutatorJs.contains("loottable_condition_all_mutator")
                        && mutatorJs.contains("loottable_pool_mutator")
                        && mutatorJs.contains("loottable_pool_mutator_else_if")
                        && mutatorJs.contains("loottable_pool_mutator_else"),
                "condition and native-style pool mutators are registered");
        check(mutatorJs.contains("elseifCount_") && mutatorJs.contains("updatePoolBranches(this)"),
                "pool mutator follows the if/else-if/else shape");
    }

    private static void validEnchantedItem() {
        String enchanted = "<block type=\"loottable_item_with_enchantment\">"
                + "<mutation inputs=\"2\"></mutation>"
                + "<value name=\"item\"><block type=\"loottable_item_selector\">"
                + "<field name=\"item\">minecraft:diamond_sword</field></block></value>"
                + "<field name=\"enchantment0\">sharpness</field>"
                + "<value name=\"level0\"><block type=\"loottable_integer_provider\">"
                + "<field name=\"value\">2</field></block></value>"
                + "<field name=\"enchantment1\">minecraft:unbreaking</field>"
                + "<value name=\"level1\"><block type=\"loottable_integer_provider\">"
                + "<field name=\"value\">3</field></block></value>"
                + "</block>";
        String entryXml = entryWithItem(enchanted,
                "<value name=\"count\"><block type=\"loottable_integer\">"
                        + "<field name=\"value\">1</field></block></value>", 1);
        BlocklyXmlParser.ParseResult result = BlocklyXmlParser.parse(blockTableXml(
                entryXml, ""), LootTableType.BLOCK, value -> value);
        check(!result.report().hasErrors(), "enchanted item parses without errors");
        String json = LootTableJsonWriter.write(result.model(), "test", "blocks/stone", false);
        check(json.contains("\"function\": \"minecraft:set_enchantments\""),
                "enchanted item writes set_enchantments");
        check(json.contains("\"minecraft:sharpness\": {")
                        && json.contains("\"type\": \"minecraft:constant\"")
                        && json.contains("\"value\": 2"),
                "enchanted item writes the first provider level");
        check(json.contains("\"minecraft:unbreaking\": {")
                        && json.contains("\"value\": 3"),
                "enchanted item writes the second provider level");
        check(json.contains("\"add\": false"), "enchanted item replaces existing enchantments");
    }

    private static void validItemDataAndContainers() {
        String count = "<value name=\"count\"><block type=\"loottable_integer\">"
                + "<field name=\"value\">1</field></block></value>";
        String base = "<block type=\"loottable_item_selector\">"
                + "<field name=\"item\">minecraft:paper</field></block>";

        String nbtLogic = "<block type=\"loottable_item_nbt_logic\">"
                + "<value name=\"tag\"><block type=\"loottable_empty_text\">"
                + "<field name=\"TEXT\">CustomName</field></block></value>"
                + "<value name=\"value\"><block type=\"loottable_boolean\">"
                + "<field name=\"BOOL\">TRUE</field></block></value>"
                + "<value name=\"item\">" + base + "</value></block>";
        BlocklyXmlParser.ParseResult logicResult = BlocklyXmlParser.parse(blockTableXml(
                entryWithItem(nbtLogic, count, 1), ""), LootTableType.BLOCK, value -> value);
        check(!logicResult.report().hasErrors(), "boolean NBT item parses without errors");
        String modernNbt = LootTableJsonWriter.write(logicResult.model(), "test", "blocks/paper", false, true);
        check(modernNbt.contains("\"function\": \"minecraft:set_custom_data\""),
                "boolean NBT uses set_custom_data on 1.20.5+");
        check(modernNbt.contains("{CustomName:1b}"), "boolean NBT writes a byte tag");
        String legacyNbt = LootTableJsonWriter.write(logicResult.model(), "test", "blocks/paper", false, false);
        check(legacyNbt.contains("\"function\": \"minecraft:set_nbt\""),
                "boolean NBT uses set_nbt before 1.20.5");

        String nbtText = "<block type=\"loottable_item_nbt_text\">"
                + "<value name=\"tag\"><block type=\"loottable_empty_text\">"
                + "<field name=\"TEXT\">Note</field></block></value>"
                + "<value name=\"value\"><block type=\"loottable_empty_text\">"
                + "<field name=\"TEXT\">hello</field></block></value>"
                + "<value name=\"item\">" + base + "</value></block>";
        BlocklyXmlParser.ParseResult textResult = BlocklyXmlParser.parse(blockTableXml(
                entryWithItem(nbtText, count, 1), ""), LootTableType.BLOCK, value -> value);
        String textJson = LootTableJsonWriter.write(textResult.model(), "test", "blocks/paper", false, true);
        check(textJson.contains("{Note:\\\"hello\\\"}"), "text NBT writes an escaped quoted string");

        String container = "<block type=\"loottable_item_container\">"
                + "<mutation inputs=\"2\"></mutation>"
                + "<field name=\"component\">minecraft:bundle_contents</field>"
                + "<value name=\"item\">" + base + "</value>"
                + "<value name=\"item0\"><block type=\"loottable_item_selector\">"
                + "<field name=\"item\">minecraft:apple</field></block></value>"
                + "<value name=\"count0\"><block type=\"loottable_integer\">"
                + "<field name=\"value\">2</field></block></value>"
                + "<value name=\"item1\"><block type=\"loottable_item_selector\">"
                + "<field name=\"item\">minecraft:diamond</field></block></value>"
                + "<value name=\"count1\"><block type=\"loottable_float\">"
                + "<field name=\"value\">2.5</field></block></value></block>";
        BlocklyXmlParser.ParseResult containerResult = BlocklyXmlParser.parse(blockTableXml(
                entryWithItem(container, count, 1), ""), LootTableType.BLOCK, value -> value);
        check(containerResult.report().getErrorMessages().stream()
                        .anyMatch(message -> message.contains("integer literal")),
                "container float count is rejected");
        String containerJson = LootTableJsonWriter.write(
                containerResult.model(), "test", "blocks/bundle", false, true);
        check(containerJson.contains("\"function\": \"minecraft:set_contents\""),
                "container item writes set_contents");
        check(containerJson.contains("\"component\": \"minecraft:bundle_contents\""),
                "container item writes its component");
        check(containerJson.contains("\"name\": \"minecraft:apple\"")
                        && containerJson.contains("\"name\": \"minecraft:diamond\""),
                "container item writes its rows");
        check(containerJson.contains("\"count\": 2"), "container item writes the integer count");
        String legacyContainer = LootTableJsonWriter.write(
                containerResult.model(), "test", "blocks/bundle", false, false);
        check(legacyContainer.contains("\"type\": \"minecraft:bundle_contents\""),
                "container item uses the pre-1.20.5 type field");

        String potion = "<block type=\"loottable_item_potion\">"
                + "<field name=\"potion\">minecraft:speed</field>"
                + "<field name=\"item_type\">potion</field>"
                + "<value name=\"amplifier\"><block type=\"loottable_integer\">"
                + "<field name=\"value\">1</field></block></value>"
                + "<value name=\"duration\"><block type=\"loottable_integer\">"
                + "<field name=\"value\">300</field></block></value></block>";
        BlocklyXmlParser.ParseResult potionResult = BlocklyXmlParser.parse(blockTableXml(
                entryWithItem(potion, count, 1), ""), LootTableType.BLOCK, value -> value);
        check(!potionResult.report().hasErrors(), "potion item parses without errors");
        String modernPotion = LootTableJsonWriter.write(
                potionResult.model(), "test", "blocks/potion", false, true, true);
        check(modernPotion.contains("\"function\": \"minecraft:set_components\"")
                        && modernPotion.contains("\"minecraft:potion_contents\"")
                        && modernPotion.contains("\"id\": \"minecraft:speed\"")
                        && modernPotion.contains("\"amplifier\": 1")
                        && modernPotion.contains("\"duration\": 300"),
                "potion item uses potion_contents on 1.20.5+");
        String namespacedPotion = LootTableJsonWriter.write(
                potionResult.model(), "test", "blocks/potion", false, false, true);
        check(namespacedPotion.contains("\"function\": \"minecraft:set_nbt\"")
                        && namespacedPotion.contains("custom_potion_effects")
                        && namespacedPotion.contains("id:\\\"minecraft:speed\\\"")
                        && namespacedPotion.contains("amplifier:1")
                        && namespacedPotion.contains("duration:300"),
                "potion item uses namespaced NBT on 1.20.2-1.20.4");
        String legacyPotion = LootTableJsonWriter.write(
                potionResult.model(), "test", "blocks/potion", false, false, false);
        check(legacyPotion.contains("\"function\": \"minecraft:set_nbt\"")
                        && legacyPotion.contains("CustomPotionEffects")
                        && legacyPotion.contains("Id:1")
                        && legacyPotion.contains("Amplifier:1")
                        && legacyPotion.contains("Duration:300"),
                "potion item uses numeric NBT on 1.20-1.20.1");
        check(resourceText("/loottable/loottable_item_potion.json")
                        .contains("\"toolbox_id\": \"loottable_item_components\""),
                "potion item is in the item components category");

        String booleanJson = resourceText("/loottable/loottable_boolean.json");
        check(booleanJson.contains("%{BKY_LOGIC_HUE}")
                        && booleanJson.contains("\"toolbox_id\": \"loottable_logic\""),
                "boolean selector is in the logic category");
        String emptyTextJson = resourceText("/loottable/loottable_empty_text.json");
        check(emptyTextJson.contains("%{BKY_TEXTS_HUE}")
                        && emptyTextJson.contains("\"toolbox_id\": \"loottable_components\""),
                "empty text block is in the components category");
        String containerResource = resourceText("/loottable/loottable_item_container.json");
        check(containerResource.contains("loottable_item_container_mutator")
                        && containerResource.contains("\"toolbox_id\": \"loottable_item_components\""),
                "container item resource exposes its mutator");
        String randomChance = resourceText("/loottable/loottable_random_chance.json");
        check(randomChance.contains("\"colour\": 120")
                        && randomChance.contains("\"toolbox_id\": \"loottable_logic\""),
                "random chance uses the green condition colour");
        String mainhand = resourceText("/loottable/loottable_entity_mainhand_item.json");
        check(mainhand.contains("\"toolbox_id\": \"loottable_entities\""),
                "main-hand item is in the entity category");
        for (String file : new String[]{
                "loottable_block_nbt_logic.json",
                "loottable_block_nbt_number.json",
                "loottable_block_nbt_text.json",
                "loottable_block_state_boolean.json",
                "loottable_block_state_enum.json",
                "loottable_block_state_integer.json",
                "loottable_survives_explosion.json",
                "loottable_world_block_tag.json"}) {
            check(resourceText("/loottable/" + file).contains("\"toolbox_id\": \"loottable_blocks\""),
                    file + " is listed in block conditions");
        }
        for (String file : new String[]{
                "loottable_entity_has_potion.json",
                "loottable_entity_mainhand_item.json",
                "loottable_entity_nbt_logic.json",
                "loottable_entity_nbt_number.json",
                "loottable_entity_nbt_text.json",
                "loottable_killed_by_player.json",
                "loottable_killed_by_player_or_pets.json"}) {
            check(resourceText("/loottable/" + file).contains("\"toolbox_id\": \"loottable_entities\""),
                    file + " is listed in entity conditions");
        }
    }

    private static void validItemConditionsAndDamage() {
        String same = condition("loottable_item_same",
                "<value name=\"other\"><block type=\"loottable_item_selector\">"
                        + "<field name=\"item\">minecraft:paper</field></block></value>");
        String durability = condition("loottable_item_durability_at_least",
                "<value name=\"min\"><block type=\"loottable_integer\">"
                        + "<field name=\"value\">40</field></block></value>");
        String count = condition("loottable_item_count_at_most",
                "<value name=\"max\"><block type=\"loottable_integer\">"
                        + "<field name=\"value\">3</field></block></value>");
        BlocklyXmlParser.ParseResult itemResult = BlocklyXmlParser.parse(blockTableXml(
                entry("Blocks.STONE", 1, 1), allOf(same, durability, count)),
                LootTableType.BLOCK, value -> value);
        check(!itemResult.report().hasErrors(), "item predicate conditions have no errors");
        String itemJson = LootTableJsonWriter.write(itemResult.model(), "test", "blocks/stone", false);
        check(countOccurrences(itemJson, "\"condition\": \"minecraft:match_tool\"") == 1,
                "same-level item conditions merge into one match_tool");
        check(itemJson.contains("\"minecraft:paper\""), "same-item condition keeps its item");
        check(itemJson.contains("\"minecraft:damage\"")
                        && itemJson.contains("\"durability\"")
                        && itemJson.contains("\"min\": 40"),
                "durability condition writes the damage sub-predicate");
        check(itemJson.contains("\"count\"") && itemJson.contains("\"max\": 3"),
                "item count condition writes the count predicate");
        String legacyItemJson = LootTableJsonWriter.write(itemResult.model(), "test", "blocks/stone", false, false);
        check(legacyItemJson.contains("\"durability\"")
                        && !legacyItemJson.contains("\"minecraft:damage\""),
                "pre-1.20.5 item durability uses the top-level predicate");

        String damage = condition("loottable_damage_type", "");
        BlocklyXmlParser.ParseResult damageResult = BlocklyXmlParser.parse(blockTableXml(
                entry("Blocks.STONE", 1, 1), damage), LootTableType.BLOCK, value -> value);
        check(!damageResult.report().hasErrors(), "damage type condition is skipped without an error");
        check(damageResult.report().summary().contains("当前minecraft版本不支持这个条件，它将被跳过"),
                "unsupported damage type warning is shown");
        String damageJson = LootTableJsonWriter.write(damageResult.model(), "test", "blocks/stone", false);
        check(!damageJson.contains("\"condition\": \"minecraft:damage_source_properties\""),
                "unsupported damage type emits no condition code");

        String nbt = condition("loottable_item_has_nbt_text",
                "<value name=\"tag\"><block type=\"loottable_empty_text\">"
                        + "<field name=\"TEXT\">tagName</field></block></value>"
                        + "<value name=\"value\"><block type=\"loottable_empty_text\">"
                        + "<field name=\"TEXT\">x</field></block></value>");
        BlocklyXmlParser.ParseResult nbtResult = BlocklyXmlParser.parse(blockTableXml(
                entry("Blocks.STONE", 1, 1), nbt), LootTableType.BLOCK, value -> value);
        check(!nbtResult.report().hasErrors(), "item NBT condition has no errors");
        String nbtJson = LootTableJsonWriter.write(nbtResult.model(), "test", "blocks/stone", false);
        check(nbtJson.contains("\"minecraft:custom_data\"")
                        && nbtJson.contains("{tagName:\\\"x\\\"}"),
                "item NBT condition writes custom_data");
        String legacyNbtJson = LootTableJsonWriter.write(nbtResult.model(), "test", "blocks/stone", false, false);
        check(legacyNbtJson.contains("\"nbt\"")
                        && !legacyNbtJson.contains("\"minecraft:custom_data\""),
                "pre-1.20.5 item NBT uses the top-level nbt predicate");

        String pets = condition("loottable_killed_by_player_or_pets", "");
        BlocklyXmlParser.ParseResult petResult = BlocklyXmlParser.parse(entityTableXml(
                entry("Items.ROTTEN_FLESH", 1, 1), pets), LootTableType.ENTITY, value -> value);
        check(!petResult.report().hasErrors(), "killed by player or pets has no errors");
        String petJson = LootTableJsonWriter.write(petResult.model(), "test", "entities/zombie", false);
        check(petJson.contains("\"condition\": \"minecraft:any_of\"")
                        && petJson.contains("\"condition\": \"minecraft:killed_by_player\""),
                "killed by player or pets expands to any_of");
        check(petJson.contains("\"nbt\": \"{Tame:1b}\""),
                "killed by player or pets uses the tamed NBT predicate");

        String components = resourceText("/loottable/$loottable_item_components.json");
        String damageCategory = resourceText("/loottable/$loottable_damage_conditions.json");
        check(components.contains("\"color\": 350"), "item components category is item-colored");
        check(damageCategory.contains("\"color\": 320"), "damage conditions category is pink");
        String sourceDamage = resourceText("/loottable/loottable_source_damage.json");
        check(sourceDamage.contains("\"output\": \"DamageSource\"")
                        && sourceDamage.contains("\"toolbox_id\": \"loottable_components\""),
                "source damage block is available in components");
        String nbtProducer = resourceText("/loottable/loottable_item_nbt_text.json");
        check(nbtProducer.contains("\"name\": \"tag\"")
                        && nbtProducer.contains("\"type\": \"input_value\""),
                "item NBT producer uses a tag text input");
    }

    private static void validEnchantItemFunctions() {
        String base = "<block type=\"loottable_item_selector\">"
                + "<field name=\"item\">minecraft:book</field></block>";
        String random = "<block type=\"loottable_item_enchant_random\">"
                + "<mutation inputs=\"1\"></mutation>"
                + "<value name=\"item\">" + base + "</value>"
                + "<field name=\"enchantment0\">sharpness</field></block>";
        String randomAny = "<block type=\"loottable_item_enchant_random_any\">"
                + "<value name=\"item\">" + base + "</value>"
                + "<value name=\"min\"><block type=\"loottable_integer\">"
                + "<field name=\"value\">2</field></block></value>"
                + "<value name=\"max\"><block type=\"loottable_integer\">"
                + "<field name=\"value\">5</field></block></value>"
                + "<field name=\"treasure\">FALSE</field></block>";
        String levels = "<block type=\"loottable_item_enchant_levels\">"
                + "<mutation inputs=\"1\"></mutation>"
                + "<value name=\"item\">" + base + "</value>"
                + "<value name=\"levels\"><block type=\"loottable_integer_provider\">"
                + "<field name=\"value\">30</field></block></value>"
                + "<field name=\"enchantment0\">minecraft:unbreaking</field></block>";
        String levelsAny = "<block type=\"loottable_item_enchant_levels_any\">"
                + "<value name=\"item\">" + base + "</value>"
                + "<value name=\"levels\"><block type=\"loottable_integer\">"
                + "<field name=\"value\">30</field></block></value>"
                + "<field name=\"treasure\">TRUE</field></block>";

        for (String item : List.of(random, randomAny, levels, levelsAny)) {
            String entryXml = entryWithItem(item,
                    "<value name=\"count\"><block type=\"loottable_integer\">"
                            + "<field name=\"value\">1</field></block></value>", 1);
            BlocklyXmlParser.ParseResult result = BlocklyXmlParser.parse(blockTableXml(
                    entryXml, ""), LootTableType.BLOCK, value -> value);
            check(!result.report().hasErrors(), "enchant item function parses without errors");
            String json = LootTableJsonWriter.write(result.model(), "test", "blocks/book", false);
            check(json.contains("\"function\": \"minecraft:enchant_randomly\"")
                            || json.contains("\"function\": \"minecraft:enchant_with_levels\""),
                    "enchant item function writes a vanilla enchant function");
        }

        String randomJson = LootTableJsonWriter.write(BlocklyXmlParser.parse(blockTableXml(
                entryWithItem(random, "<value name=\"count\"><block type=\"loottable_integer\">"
                        + "<field name=\"value\">1</field></block></value>", 1), ""),
                LootTableType.BLOCK, value -> value).model(), "test", "blocks/book", false);
        check(randomJson.contains("\"options\": [")
                        && randomJson.contains("\"minecraft:sharpness\""),
                "enchant_randomly writes the enchantment list");

        String anyJson = LootTableJsonWriter.write(BlocklyXmlParser.parse(blockTableXml(
                entryWithItem(randomAny, "<value name=\"count\"><block type=\"loottable_integer\">"
                        + "<field name=\"value\">1</field></block></value>", 1), ""),
                LootTableType.BLOCK, value -> value).model(), "test", "blocks/book", false);
        check(anyJson.contains("\"function\": \"minecraft:enchant_with_levels\""),
                "random any enchant uses bounded levels");
        check(anyJson.contains("\"type\": \"minecraft:uniform\"")
                        && anyJson.contains("\"min\": 2")
                        && anyJson.contains("\"max\": 5"),
                "random any enchant writes the requested level range");
        check(anyJson.contains("\"options\": \"#minecraft:on_random_loot\""),
                "non-treasure random enchant restricts to the loot enchantment tag");

        String levelsJson = LootTableJsonWriter.write(BlocklyXmlParser.parse(blockTableXml(
                entryWithItem(levels, "<value name=\"count\"><block type=\"loottable_integer\">"
                        + "<field name=\"value\">1</field></block></value>", 1), ""),
                LootTableType.BLOCK, value -> value).model(), "test", "blocks/book", false);
        check(levelsJson.contains("\"function\": \"minecraft:enchant_with_levels\""),
                "enchant_with_levels function exists");
        check(levelsJson.contains("\"levels\": {")
                        && levelsJson.contains("\"type\": \"minecraft:constant\"")
                        && levelsJson.contains("\"value\": 30"),
                "enchant_with_levels writes its provider level");

        VersionCompat.Features v1205 = new VersionCompat.Features(
                true, true, true, true, true, true, true, true, false);
        String legacyRandom = writeItemEntry(random, v1205);
        check(legacyRandom.contains("\"enchantments\": [")
                        && !legacyRandom.contains("\"options\": ["),
                "pre-1.21 enchant_randomly uses the enchantments field");
        String legacyRandomAny = writeItemEntry(randomAny, v1205);
        check(legacyRandomAny.contains("\"treasure\": false")
                        && !legacyRandomAny.contains("\"options\""),
                "pre-1.21 any-enchant uses the treasure flag");
        String legacyLevels = writeItemEntry(levels, v1205);
        check(legacyLevels.contains("\"treasure\": true")
                        && !legacyLevels.contains("\"options\""),
                "pre-1.21 enchant_with_levels uses the treasure flag");
    }

    private static void validFunctionBlocks() {
        String simple = "<block type=\"loottable_entry_item_simple\">"
                + "<value name=\"item\"><block type=\"loottable_item_selector\">"
                + "<field name=\"item\">minecraft:stone</field></block></value>"
                + "<value name=\"count\"><block type=\"loottable_integer\">"
                + "<field name=\"value\">2</field></block></value></block>";
        BlocklyXmlParser.ParseResult simpleResult = BlocklyXmlParser.parse(blockTableXml(
                simple, ""), LootTableType.BLOCK, value -> value);
        check(!simpleResult.report().hasErrors(), "simple entry parses without errors");
        String simpleJson = LootTableJsonWriter.write(simpleResult.model(), "test", "blocks/stone", false);
        check(simpleJson.contains("\"name\": \"minecraft:stone\""), "simple entry resolves its item");
        check(simpleJson.contains("\"count\": 2"), "simple entry writes its count");
        check(!simpleJson.contains("\"conditions\""), "simple entry has no condition list");
        check(countOccurrences(simpleJson, "\"function\":") == 1,
                "simple entry writes only the base set_count function");

        String uniform = "<block type=\"loottable_function_apply_bonus_uniform\">"
                + "<field name=\"enchantment\">fortune</field>"
                + "<value name=\"bonus\"><block type=\"loottable_integer\">"
                + "<field name=\"value\">2</field></block></value></block>";
        String binomial = "<block type=\"loottable_function_apply_bonus_binomial\">"
                + "<field name=\"enchantment\">looting</field>"
                + "<value name=\"extra\"><block type=\"loottable_integer\">"
                + "<field name=\"value\">3</field></block></value>"
                + "<value name=\"probability\"><block type=\"loottable_float\">"
                + "<field name=\"value\">0.25</field></block></value></block>";
        String ore = "<block type=\"loottable_function_apply_bonus_ore\">"
                + "<field name=\"enchantment\">silk_touch</field></block>";
        String increase = "<block type=\"loottable_function_enchanted_count_increase\">"
                + "<field name=\"enchantment\">looting</field>"
                + "<value name=\"count\"><block type=\"loottable_uniform\">"
                + "<value name=\"min\"><block type=\"loottable_integer\">"
                + "<field name=\"value\">0</field></block></value>"
                + "<value name=\"max\"><block type=\"loottable_integer\">"
                + "<field name=\"value\">2</field></block></value></block></value></block>";
        String limit = "<block type=\"loottable_function_limit_count\">"
                + "<value name=\"limit\"><block type=\"loottable_integer\">"
                + "<field name=\"value\">5</field></block></value></block>";
        String entry = "<block type=\"loottable_entry_item\">"
                + "<value name=\"item\"><block type=\"loottable_item_selector\">"
                + "<field name=\"item\">minecraft:diamond_ore</field></block></value>"
                + "<value name=\"count\"><block type=\"loottable_integer\">"
                + "<field name=\"value\">1</field></block></value>"
                + "<field name=\"weight\">1</field><statement name=\"functions\">"
                + chain(uniform, binomial, ore, increase, limit)
                + "</statement></block>";
        BlocklyXmlParser.ParseResult result = BlocklyXmlParser.parse(blockTableXml(
                entry, ""), LootTableType.BLOCK, value -> value);
        check(!result.report().hasErrors(), "function blocks parse without errors");
        String json = LootTableJsonWriter.write(result.model(), "test", "blocks/diamond_ore", false);
        check(json.contains("\"formula\": \"minecraft:uniform_bonus_count\"")
                        && json.contains("\"bonusMultiplier\": 2"),
                "uniform bonus function writes its multiplier");
        check(json.contains("\"formula\": \"minecraft:binomial_with_bonus_count\"")
                        && json.contains("\"extra\": 3")
                        && json.contains("\"probability\": 0.25"),
                "binomial bonus function writes extra and probability");
        check(json.contains("\"formula\": \"minecraft:ore_drops\""),
                "ore bonus function uses the ore drops formula");
        check(json.contains("\"function\": \"minecraft:enchanted_count_increase\"")
                        && json.contains("\"enchantment\": \"minecraft:looting\"")
                        && json.contains("\"type\": \"minecraft:uniform\""),
                "enchanted count increase writes a provider count");
        check(json.contains("\"function\": \"minecraft:limit_count\"")
                        && json.contains("\"limit\": 5"),
                "limit count function writes its limit");
        VersionCompat.Features v1205 = new VersionCompat.Features(
                true, true, true, true, true, true, true, true, false);
        String legacyJson = LootTableJsonWriter.write(result.model(), "test",
                "blocks/diamond_ore", false, v1205);
        check(legacyJson.contains("\"function\": \"minecraft:looting_enchant\"")
                        && !legacyJson.contains("\"function\": \"minecraft:enchanted_count_increase\""),
                "pre-1.21 enchanted count increase uses looting_enchant");

        String badBonus = "<block type=\"loottable_function_apply_bonus_uniform\">"
                + "<field name=\"enchantment\">fortune</field>"
                + "<value name=\"bonus\"><block type=\"loottable_uniform\">"
                + "<value name=\"min\"><block type=\"loottable_integer\">"
                + "<field name=\"value\">1</field></block></value>"
                + "<value name=\"max\"><block type=\"loottable_integer\">"
                + "<field name=\"value\">2</field></block></value></block></value></block>";
        String badEntry = "<block type=\"loottable_entry_item\">"
                + "<value name=\"item\"><block type=\"loottable_item_selector\">"
                + "<field name=\"item\">minecraft:stone</field></block></value>"
                + "<value name=\"count\"><block type=\"loottable_integer\">"
                + "<field name=\"value\">1</field></block></value>"
                + "<field name=\"weight\">1</field><statement name=\"functions\">"
                + badBonus + "</statement></block>";
        BlocklyXmlParser.ParseResult badResult = BlocklyXmlParser.parse(blockTableXml(
                badEntry, ""), LootTableType.BLOCK, value -> value);
        check(badResult.report().summary().contains("plain number"),
                "apply bonus rejects provider multipliers");
    }

    private static void validWorldConditions() {
        String time = condition("loottable_time_day",
                "<value name=\"min\"><block type=\"loottable_integer\">"
                        + "<field name=\"value\">0</field></block></value>"
                        + "<value name=\"max\"><block type=\"loottable_integer\">"
                        + "<field name=\"value\">12000</field></block></value>");
        BlocklyXmlParser.ParseResult result = BlocklyXmlParser.parse(blockTableXml(
                entry("Blocks.STONE", 1, 1), allOf(time,
                        condition("loottable_world_dimension",
                                "<field name=\"dimension\">Surface</field>"),
                        condition("loottable_world_biome",
                                "<field name=\"biome\">desert</field>"),
                        condition("loottable_world_block_tag",
                                "<field name=\"tag\">minecraft:base_stone_overworld</field>"))),
                LootTableType.BLOCK, value -> value);
        check(!result.report().hasErrors(), "world conditions parse without errors");
        String json = LootTableJsonWriter.write(result.model(), "test", "blocks/stone", false);
        check(json.contains("\"condition\": \"minecraft:time_check\""), "time condition exists");
        check(json.contains("\"period\": 24000"), "time-of-day period exists");
        int locationCount = countOccurrences(json, "\"condition\": \"minecraft:location_check\"");
        check(locationCount == 1, "same-level location checks merge into one predicate");
        check(json.contains("\"dimension\": \"minecraft:overworld\""), "dimension predicate exists");
        check(json.contains("\"biomes\": \"minecraft:desert\""), "biome predicate exists");
        check(json.contains("\"blocks\": \"#minecraft:base_stone_overworld\""),
                "block tag predicate exists");

        String light = condition("loottable_world_light",
                "<field name=\"value\">7</field>");
        String position = condition("loottable_world_position_x_range",
                "<field name=\"min\">-10</field><field name=\"max\">10</field>");
        BlocklyXmlParser.ParseResult lightResult = BlocklyXmlParser.parse(blockTableXml(
                entry("Blocks.STONE", 1, 1), allOf(light, position)), LootTableType.BLOCK, value -> value);
        String lightJson = LootTableJsonWriter.write(lightResult.model(), "test", "blocks/stone", false);
        check(lightJson.contains("\"light\": {") && lightJson.contains("\"light\": 7"),
                "light level predicate exists");
        check(lightJson.contains("\"position\": {")
                        && lightJson.contains("\"x\": {")
                        && lightJson.contains("\"min\": -10")
                        && lightJson.contains("\"max\": 10"),
                "coordinate range predicate exists");
    }

    private static void validSetNameAndPotionBase() {
        String simpleName = "<block type=\"loottable_item_set_name\">"
                + "<value name=\"item\"><block type=\"loottable_item_selector\">"
                + "<field name=\"item\">minecraft:stone</field></block></value>"
                + "<value name=\"name\"><block type=\"loottable_empty_text\">"
                + "<field name=\"TEXT\">Named Item</field></block></value></block>";
        String simpleJson = writeItemEntry(simpleName, VersionCompat.Features.modern());
        check(simpleJson.contains("\"function\": \"minecraft:set_name\""), "simple set_name function");
        check(simpleJson.contains("\"text\": \"Named Item\""), "simple set_name text");

        String fullName = "<block type=\"loottable_item_set_name_full\">"
                + "<value name=\"item\"><block type=\"loottable_item_selector\">"
                + "<field name=\"item\">minecraft:stone</field></block></value>"
                + "<field name=\"name\">{\"text\":\"Full Name\",\"color\":\"gold\"}</field></block>";
        String fullJson = writeItemEntry(fullName, VersionCompat.Features.modern());
        check(fullJson.contains("\"function\": \"minecraft:set_name\""), "full set_name function");
        check(fullJson.contains("\"color\":\"gold\""), "full set_name raw component");

        String potion = "<block type=\"loottable_item_potion\">"
                + "<field name=\"potion\">minecraft:speed</field>"
                + "<field name=\"item_type\">potion</field>"
                + "<value name=\"amplifier\"><block type=\"loottable_integer\">"
                + "<field name=\"value\">0</field></block></value>"
                + "<value name=\"duration\"><block type=\"loottable_integer\">"
                + "<field name=\"value\">600</field></block></value></block>";
        String potionJson = writeItemEntry(potion, VersionCompat.Features.modern());
        check(potionJson.contains("\"potion\": \"minecraft:swiftness\""), "potion base template");
        check(potionJson.contains("\"custom_effects\": ["), "potion custom effects");
    }

    private static void validPotionRangeCondition() {
        String rangeCondition = condition("loottable_entity_has_potion_range",
                value("entity", "<block type=\"loottable_entity_dependency\"></block>")
                        + "<field name=\"potion\">minecraft:speed</field>"
                        + value("durationMin", "<block type=\"loottable_integer\">"
                                + "<field name=\"value\">10</field></block>")
                        + value("durationMax", "<block type=\"loottable_integer\">"
                                + "<field name=\"value\">20</field></block>")
                        + value("amplifierMin", "<block type=\"loottable_integer\">"
                                + "<field name=\"value\">1</field></block>")
                        + value("amplifierMax", "<block type=\"loottable_integer\">"
                                + "<field name=\"value\">2</field></block>"));
        BlocklyXmlParser.ParseResult result = BlocklyXmlParser.parse(
                entityTableXml(entry("minecraft:stone", 1, 1), rangeCondition),
                LootTableType.ENTITY, ItemNameResolver.IDENTITY);
        check(!result.report().hasErrors(), "potion range condition parses without errors");
        String json = LootTableJsonWriter.write(result.model(), "test", "entities/test", false);
        check(json.contains("\"duration\": {"), "potion range writes duration");
        check(json.contains("\"min\": 10"), "potion range writes duration min");
        check(json.contains("\"max\": 20"), "potion range writes duration max");
        check(json.contains("\"amplifier\": {"), "potion range writes amplifier");
    }

    private static void validLegacyVersionFormats() {
        String count = "<value name=\"count\"><block type=\"loottable_integer\">"
                + "<field name=\"value\">1</field></block></value>";
        String base = "<block type=\"loottable_item_selector\">"
                + "<field name=\"item\">minecraft:shulker_box</field></block>";

        String enchanted = "<block type=\"loottable_item_with_enchantment\">"
                + "<mutation inputs=\"1\"></mutation>"
                + "<value name=\"item\"><block type=\"loottable_item_selector\">"
                + "<field name=\"item\">minecraft:diamond_sword</field></block></value>"
                + "<field name=\"enchantment0\">sharpness</field>"
                + "<value name=\"level0\"><block type=\"loottable_integer_provider\">"
                + "<field name=\"value\">2</field></block></value></block>";
        BlocklyXmlParser.ParseResult enchantedResult = BlocklyXmlParser.parse(blockTableXml(
                entryWithItem(enchanted, count, 1), ""), LootTableType.BLOCK, value -> value);
        VersionCompat.Features v116 = new VersionCompat.Features(
                false, false, false, false, false, false, false, false, false);
        String v116Enchanted = LootTableJsonWriter.write(enchantedResult.model(),
                "test", "blocks/sword", false, v116);
        check(v116Enchanted.contains("\"function\": \"minecraft:set_nbt\"")
                        && v116Enchanted.contains("Enchantments:[")
                        && v116Enchanted.contains("lvl:2"),
                "1.16 enchantments use the legacy Enchantments NBT tag");
        VersionCompat.Features v117 = new VersionCompat.Features(
                false, false, true, true, false, false, false, false, false);
        check(LootTableJsonWriter.write(enchantedResult.model(), "test", "blocks/sword", false, v117)
                        .contains("\"function\": \"minecraft:set_enchantments\""),
                "1.17+ item enchantments use set_enchantments");

        String score = "<block type=\"loottable_score_player\">"
                + "<value name=\"player\"><block type=\"loottable_empty_text\">"
                + "<field name=\"TEXT\">Steve</field></block></value>"
                + "<value name=\"score\"><block type=\"loottable_empty_text\">"
                + "<field name=\"TEXT\">points</field></block></value>"
                + "<value name=\"scale\"><block type=\"loottable_float\">"
                + "<field name=\"value\">1.0</field></block></value></block>";
        BlocklyXmlParser.ParseResult scoreResult = BlocklyXmlParser.parse(blockTableXml(
                entryWithItem(base, "<value name=\"count\">" + score + "</value>", 1), ""),
                LootTableType.BLOCK, value -> value);
        String v116Score = LootTableJsonWriter.write(scoreResult.model(),
                "test", "blocks/shulker", false, v116);
        check(v116Score.contains("\"count\": 0")
                        && !v116Score.contains("\"type\": \"minecraft:score\""),
                "1.16 score providers fall back to zero");

        String container = "<block type=\"loottable_item_container\">"
                + "<mutation inputs=\"1\"></mutation>"
                + "<field name=\"component\">minecraft:container</field>"
                + "<value name=\"item\">" + base + "</value>"
                + "<value name=\"item0\"><block type=\"loottable_item_selector\">"
                + "<field name=\"item\">minecraft:apple</field></block></value>"
                + "<value name=\"count0\"><block type=\"loottable_integer\">"
                + "<field name=\"value\">2</field></block></value></block>";
        BlocklyXmlParser.ParseResult containerResult = BlocklyXmlParser.parse(blockTableXml(
                entryWithItem(container, count, 1), ""), LootTableType.BLOCK, value -> value);
        String v116Container = LootTableJsonWriter.write(containerResult.model(),
                "test", "blocks/shulker", false, v116);
        check(!v116Container.contains("\"function\": \"minecraft:set_contents\""),
                "1.16-1.17 skip set_contents because the function did not exist");
        VersionCompat.Features v118 = new VersionCompat.Features(
                false, false, true, true, true, false, false, false, false);
        String v118Container = LootTableJsonWriter.write(containerResult.model(),
                "test", "blocks/shulker", false, v118);
        check(v118Container.contains("\"type\": \"minecraft:container\""),
                "1.18-1.19 set_contents uses the type field");

        String damage = condition("loottable_damage_tag",
                "<value name=\"tag\"><block type=\"loottable_empty_text\">"
                        + "<field name=\"TEXT\">minecraft:is_projectile</field></block></value>");
        BlocklyXmlParser.ParseResult damageResult = BlocklyXmlParser.parse(blockTableXml(
                entry("Blocks.STONE", 1, 1), damage), LootTableType.BLOCK, value -> value);
        String v1192Damage = LootTableJsonWriter.write(damageResult.model(),
                "test", "blocks/stone", false, v118);
        check(v1192Damage.contains("\"is_projectile\": true")
                        && !v1192Damage.contains("\"tags\""),
                "pre-1.19.4 damage predicates use the old boolean fields");

        String structure = condition("loottable_world_structure",
                "<field name=\"structure\">village</field>");
        BlocklyXmlParser.ParseResult structureResult = BlocklyXmlParser.parse(blockTableXml(
                entry("Blocks.STONE", 1, 1), structure), LootTableType.BLOCK, value -> value);
        check(LootTableJsonWriter.write(structureResult.model(),
                        "test", "blocks/stone", false, v118).contains("\"feature\": \"minecraft:village\""),
                "pre-1.19 location predicates use feature");
        VersionCompat.Features v1192 = new VersionCompat.Features(
                false, false, true, true, true, false, true, false, false);
        check(LootTableJsonWriter.write(structureResult.model(),
                        "test", "blocks/stone", false, v1192).contains("\"structure\": \"minecraft:village\""),
                "1.19-1.20.4 location predicates use singular structure");
        VersionCompat.Features v1205 = new VersionCompat.Features(
                false, false, true, true, true, false, true, false, false);
        check(LootTableJsonWriter.write(structureResult.model(),
                        "test", "blocks/stone", false, v1205).contains("\"structure\": \"minecraft:village\""),
                "1.20-1.20.4 structure remains singular");
        VersionCompat.Features modern = VersionCompat.Features.modern();
        check(LootTableJsonWriter.write(structureResult.model(),
                        "test", "blocks/stone", false, modern).contains("\"structures\": \"minecraft:village\""),
                "1.20.5+ location predicates use plural structures");
    }

    private static int countOccurrences(String text, String needle) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(needle, index)) >= 0) {
            count++;
            index += needle.length();
        }
        return count;
    }

    private static void validEntityPotionOnBlock() {
        BlocklyXmlParser.ParseResult result = BlocklyXmlParser.parse(blockTableXml(
                entry("Blocks.STONE", 1, 1),
                condition("loottable_entity_has_potion",
                        "<value name=\"entity\"><block type=\"loottable_entity_dependency\"></block></value>"
                                + "<field name=\"potion\">minecraft:fire_resistance</field>")),
                LootTableType.BLOCK, value -> value);
        check(!result.report().hasErrors(), "block table with entity potion condition has no errors");
        check(result.report().summary().contains("only provide the target entity"),
                "block potion condition warns about the optional target entity dependency");
        String json = LootTableJsonWriter.write(result.model(), "test", "blocks/stone", false);
        check(json.contains("\"condition\": \"minecraft:entity_properties\""),
                "block potion condition uses entity_properties");
        check(json.contains("\"entity\": \"this\""), "block potion condition targets the breaker entity");
        check(json.contains("\"minecraft:fire_resistance\": {}"),
                "block potion condition writes the effect predicate");
    }

    private static void duplicateStartIsWarned() {
        BlocklyXmlParser.ParseResult result = BlocklyXmlParser.parse(
                "<xml xmlns=\"https://developers.google.com/blockly/xml\">"
                        + "<block type=\"loottable_start\"></block>"
                        + "<block type=\"loottable_start\"></block></xml>",
                LootTableType.BLOCK, ItemNameResolver.IDENTITY);
        check(!result.report().hasErrors(), "duplicate start blocks are a warning, not an error");
        check(result.report().summary().contains("extra start block"),
                "duplicate start blocks produce a warning");
    }

    private static void startBlockIsHidden() {
        String blockJson = resourceText("/loottable/loottable_start.json");
        check(blockJson.contains("\"mcreator\""), "start block keeps its Blockly metadata");
        check(!blockJson.contains("\"toolbox_id\""), "start block is hidden from the toolbox");

        String script = resourceText("/blockly/js/loottable_start.js");
        check(script.contains("setDeletable(false)"), "start block script disables deletion");
    }

    private static String resourceText(String path) {
        try (InputStream input = LootTableSelfCheck.class.getResourceAsStream(path)) {
            if (input == null) {
                throw new AssertionError("Missing resource: " + path);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new AssertionError("Could not read resource: " + path, exception);
        }
    }

    private static boolean resourceExists(String path) {
        try (InputStream input = LootTableSelfCheck.class.getResourceAsStream(path)) {
            return input != null;
        } catch (IOException exception) {
            throw new AssertionError("Could not inspect resource: " + path, exception);
        }
    }

    private static void invalidEntityBlockState() {
        BlocklyXmlParser.ParseResult result = BlocklyXmlParser.parse(entityTableXml(
                entry("Items.ROTTEN_FLESH", 1, 1),
                condition("loottable_block_state_boolean", "")), LootTableType.ENTITY, value -> value);
        check(result.report().hasErrors(), "entity table with block state property is an error");
        check(result.report().summary().contains("block_state"), "error names the block");
        check(result.report().summary().contains("Entity"), "error names the current type");
    }

    private static void invalidBlockKilledByPlayer() {
        BlocklyXmlParser.ParseResult result = BlocklyXmlParser.parse(blockTableXml(
                entry("Blocks.STONE", 1, 1),
                condition("loottable_killed_by_player", "")), LootTableType.BLOCK, value -> value);
        check(result.report().hasErrors(), "block table with killed by player is an error");
        check(result.report().summary().contains("killed_by_player"), "error names the block");
        check(result.report().summary().contains("Block"), "error names the current type");
    }

    private static void unknownConditionIsSkipped() {
        BlocklyXmlParser.ParseResult result = BlocklyXmlParser.parse(blockTableXml(
                entry("Blocks.STONE", 1, 1),
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
                """.formatted(startType, value("conditions", poolConditionsXml),
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
        return entryXml.substring(0, closing) + value("conditions", conditionsXml) + entryXml.substring(closing);
    }

    private static String writeItemEntry(String itemXml, VersionCompat.Features features) {
        String count = "<value name=\"count\"><block type=\"loottable_integer\">"
                + "<field name=\"value\">1</field></block></value>";
        BlocklyXmlParser.ParseResult result = BlocklyXmlParser.parse(blockTableXml(
                entryWithItem(itemXml, count, 1), ""), LootTableType.BLOCK, value -> value);
        check(!result.report().hasErrors(), "versioned item entry parses without errors");
        return LootTableJsonWriter.write(result.model(), "test", "blocks/versioned", false, features);
    }

    private static String entryWithFunctions(String entryXml, String functionsXml) {
        if (functionsXml == null || functionsXml.isBlank()) {
            return entryXml;
        }
        int closing = entryXml.lastIndexOf("</block>");
        if (closing < 0) {
            return entryXml;
        }
        return entryXml.substring(0, closing) + statement("functions", functionsXml) + entryXml.substring(closing);
    }

    private static String statement(String name, String blocks) {
        if (blocks == null || blocks.isBlank()) {
            return "";
        }
        return "<statement name=\"" + name + "\">" + blocks + "</statement>";
    }

    private static String value(String name, String blockXml) {
        if (blockXml == null || blockXml.isBlank()) {
            return "";
        }
        return "<value name=\"" + name + "\">" + blockXml + "</value>";
    }

    private static String entry(String item, int count, int weight) {
        return entryWithCount(item,
                "<value name=\"count\"><block type=\"loottable_integer\">"
                        + "<field name=\"value\">" + count + "</field></block></value>",
                weight);
    }

    private static String entryWithCount(String item, String countXml, int weight) {
        return "<block type=\"loottable_entry_item\">"
                + "<value name=\"item\"><block type=\"loottable_item_selector\">"
                + "<field name=\"item\">" + item + "</field></block></value>"
                + countXml
                + "<field name=\"weight\">" + weight + "</field>"
                + "</block>";
    }

    private static String entryWithItem(String itemXml, String countXml, int weight) {
        return "<block type=\"loottable_entry_item\">"
                + "<value name=\"item\">" + itemXml + "</value>"
                + countXml
                + "<field name=\"weight\">" + weight + "</field>"
                + "</block>";
    }

    private static String condition(String type, String fields) {
        return "<block type=\"" + type + "\">" + fields + "</block>";
    }

    private static String allOf(String... conditions) {
        StringBuilder xml = new StringBuilder("<block type=\"loottable_logic_and\">")
                .append("<mutation inputs=\"").append(conditions.length).append("\"></mutation>");
        for (int index = 0; index < conditions.length; index++) {
            xml.append(value("condition" + index, conditions[index]));
        }
        return xml.append("</block>").toString();
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

