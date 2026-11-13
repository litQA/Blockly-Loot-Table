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
        validBlockStateConditions();
        validEntityPotionCondition();
        validNbtConditions();
        validItemEnchantmentCondition();
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
                "blockly.block.loottable_block_state_boolean",
                "blockly.block.loottable_block_state_integer",
                "blockly.block.loottable_block_state_enum",
                "blockly.block.loottable_entity_has_potion",
                "blockly.block.loottable_immediate_source_entity_dependency",
                "blockly.block.loottable_source_player_dependency",
                "blockly.block.loottable_entity_mainhand_item",
                "blockly.block.loottable_item_has_enchantment",
                "blockly.block.loottable_block_nbt_logic",
                "blockly.block.loottable_block_nbt_number",
                "blockly.block.loottable_block_nbt_text",
                "blockly.block.loottable_entity_nbt_logic",
                "blockly.block.loottable_entity_nbt_number",
                "blockly.block.loottable_entity_nbt_text",
                "blockly.block.loottable_number",
                "blockly.block.loottable_integer",
                "blockly.block.loottable_float",
                "blockly.block.loottable_integer_provider",
                "blockly.block.loottable_float_provider",
                "blockly.block.loottable_uniform",
                "blockly.block.loottable_binomial",
                "blockly.block.loottable_function_set_count_uniform",
                "blockly.block.loottable_function_set_count_add")) {
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
                        + "<value name=\"count\"><block type=\"loottable_integer\">"
                        + "<field name=\"value\">1</field></block></value>"
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
                entry("Blocks.DIAMOND_ORE", 1, 1),
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
                        "<field name=\"property\">waterlogged</field>"
                                + "<value name=\"block\"><block type=\"loottable_block_selector\">"
                                + "<field name=\"block\">Blocks.OAK_LOG</field></block></value>"
                                + "<field name=\"operator\">=</field>"
                                + "<field name=\"value\">true</field>")), LootTableType.BLOCK, resolver);
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
                        "<field name=\"property\">age</field>"
                                + "<value name=\"block\"><block type=\"loottable_provided_blockstate\"></block></value>"
                                + "<field name=\"operator\">=</field>"
                                + "<field name=\"value\">3</field>")), LootTableType.BLOCK, resolver,
                "minecraft:oak_log");
        check(!integerResult.report().hasErrors(), "provided blockstate condition has no errors");
        String integerJson = LootTableJsonWriter.write(integerResult.model(), "test", "blocks/log", false);
        check(integerJson.contains("\"block\": \"minecraft:oak_log\""),
                "provided blockstate uses the element target block");
        check(integerJson.contains("\"age\": \"3\""), "integer block state property value exists");

        BlocklyXmlParser.ParseResult enumResult = BlocklyXmlParser.parse(blockTableXml(
                entry("Blocks.DIAMOND_ORE", 1, 1),
                condition("loottable_block_state_enum",
                        "<field name=\"property\">half</field>"
                                + "<value name=\"block\"><block type=\"loottable_block_selector\">"
                                + "<field name=\"block\">Blocks.STONE</field></block></value>"
                                + "<field name=\"operator\">=</field>"
                                + "<field name=\"value\">upper</field>")), LootTableType.BLOCK, resolver);
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
                        "<field name=\"tag\">CustomName</field>"
                                + "<value name=\"block\"><block type=\"loottable_provided_blockstate\"></block></value>"
                                + "<field name=\"operator\">=</field>"
                                + "<field name=\"value\">true</field>")),
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
                        "<field name=\"tag\">Level</field>"
                                + "<value name=\"entity\"><block type=\"loottable_source_entity_dependency\"></block></value>"
                                + "<field name=\"operator\">=</field><field name=\"value\">3</field>")),
                LootTableType.ENTITY, value -> value);
        check(!entityResult.report().hasErrors(), "entity NBT condition has no errors");
        String entityJson = LootTableJsonWriter.write(entityResult.model(), "test", "entities/zombie", false);
        check(entityJson.contains("\"condition\": \"minecraft:entity_properties\""),
                "entity NBT condition uses entity_properties");
        check(entityJson.contains("\"entity\": \"attacker\""), "entity NBT uses the source entity");
        check(entityJson.contains("\"nbt\": \"{Level:3}\""), "entity number NBT value is SNBT");

        BlocklyXmlParser.ParseResult textResult = BlocklyXmlParser.parse(entityTableXml(
                entry("Items.ROTTEN_FLESH", 1, 1),
                condition("loottable_entity_nbt_text",
                        "<field name=\"tag\">Owner</field>"
                                + "<field name=\"value\">Steve</field>")),
                LootTableType.ENTITY, value -> value);
        check(!textResult.report().hasErrors(), "entity text NBT condition has no errors");
        String textJson = LootTableJsonWriter.write(textResult.model(), "test", "entities/zombie", false);
        check(textJson.contains("\"nbt\": \"{Owner:\\\"Steve\\\"}\""), "entity text NBT value is quoted");
    }

    private static void validItemEnchantmentCondition() {
        BlocklyXmlParser.ParseResult result = BlocklyXmlParser.parse(entityTableXml(
                entry("Items.ROTTEN_FLESH", 1, 1),
                condition("loottable_item_has_enchantment",
                        "<value name=\"item\"><block type=\"loottable_entity_mainhand_item\">"
                                + "<value name=\"entity\"><block type=\"loottable_source_entity_dependency\"></block></value>"
                                + "</block></value>"
                                + "<field name=\"enhancement\">minecraft:SHARPNESS</field>"
                                + "<field name=\"level\">3</field>")),
                LootTableType.ENTITY, value -> value);
        check(!result.report().hasErrors(), "item enchantment condition has no errors");
        String json = LootTableJsonWriter.write(result.model(), "test", "entities/zombie", false);
        check(json.contains("\"condition\": \"minecraft:entity_properties\""),
                "main-hand enchantment uses entity_properties");
        check(json.contains("\"entity\": \"attacker\""), "main-hand enchantment uses the source entity");
        check(json.contains("\"equipment\""), "main-hand enchantment uses equipment");
        check(json.contains("\"mainhand\""), "main-hand enchantment uses the mainhand slot");
        check(json.contains("\"minecraft:enchantments\": [\n"), "enchantment predicate list exists");
        check(json.contains("\"enchantments\": [\n") && json.contains("\"minecraft:sharpness\""),
                "enchantment id is lower-case and uses the official plural list");
        check(json.contains("\"levels\": {") && json.contains("\"min\": 3"),
                "enchantment level is a minimum range object");
        check(!json.contains("\"enchantment\":"), "enchantment predicate does not use a singular field");

        BlocklyXmlParser.ParseResult toolResult = BlocklyXmlParser.parse(blockTableXml(
                entry("Blocks.STONE", 1, 1),
                condition("loottable_item_has_enchantment",
                        "<field name=\"enhancement\">minecraft:fortune</field>"
                                + "<field name=\"level\">2</field>")),
                LootTableType.BLOCK, value -> value);
        check(!toolResult.report().hasErrors(), "tool enchantment condition has no errors");
        String toolJson = LootTableJsonWriter.write(toolResult.model(), "test", "blocks/stone", false);
        check(toolJson.contains("\"condition\": \"minecraft:match_tool\""),
                "tool enchantment uses match_tool");
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
        check(resourceText("/loottable/loottable_inverted.json").contains("\"check\": \"LootTableCondition\""),
                "inverted terms only accept conditions");
        check(resourceText("/loottable/$loottable_components.json").contains("\"color\": 120"),
                "minecraft components category exists");
        check(resourceText("/loottable/$loottable_items.json").contains("\"color\": 350"),
                "item category exists");
        check(resourceText("/loottable/$loottable_numbers.json").contains("\"color\": 230"),
                "number category exists");
        check(resourceText("/loottable/loottable_entity_has_potion.json").contains("\"colour\": 195"),
                "entity condition uses the native entity color");
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
        check(!resourceText("/loottable/loottable_function_set_count_uniform.json")
                        .contains("\"toolbox_id\""),
                "legacy uniform function is hidden from the toolbox");
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

    private static void invalidEntityBlockState() {
        BlocklyXmlParser.ParseResult result = BlocklyXmlParser.parse(entityTableXml(
                entry("Items.ROTTEN_FLESH", 1, 1),
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

    private static String entry(String item, int count, int weight) {
        return "<block type=\"loottable_entry_item\">"
                + "<field name=\"item\">" + item + "</field>"
                + "<value name=\"count\"><block type=\"loottable_integer\">"
                + "<field name=\"value\">" + count + "</field></block></value>"
                + "<field name=\"weight\">" + weight + "</field>"
                + "</block>";
    }

    private static String entryWithCount(String item, String countXml, int weight) {
        return "<block type=\"loottable_entry_item\">"
                + "<field name=\"item\">" + item + "</field>"
                + countXml
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

