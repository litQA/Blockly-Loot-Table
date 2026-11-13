package com.litqa.blocklyloottable.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Converts a Blockly XML document into a loot table model and validation report.
 *
 * <p>The parser deliberately treats unknown or malformed child blocks as
 * skipped entries with warnings. Context violations are errors because the
 * specification requires generation to stop for illegal condition scopes.</p>
 */
public final class BlocklyXmlParser {

    private static final Pattern NBT_NUMBER = Pattern.compile(
            "[-+]?(?:\\d+(?:\\.\\d*)?|\\.\\d+)(?:[eE][-+]?\\d+)?[bBsSlLfFdD]?");
    private static final Pattern NBT_TAG = Pattern.compile("[A-Za-z0-9_.+-]+");

    private BlocklyXmlParser() {
    }

    public record ParseResult(LootTableModel model, ValidationReport report) {
    }

    public static ParseResult parse(String xml, LootTableType type, ItemNameResolver resolver) {
        return parse(xml, type, resolver, "");
    }

    /**
     * @param providedBlock registry name of the element target block, used by
     *                      the provided blockstate dependency block
     */
    public static ParseResult parse(String xml, LootTableType type, ItemNameResolver resolver,
            String providedBlock) {
        LootTableType effectiveType = type == null ? LootTableType.BLOCK : type;
        ItemNameResolver effectiveResolver = resolver == null ? ItemNameResolver.IDENTITY : resolver;
        String effectiveProvidedBlock = providedBlock == null ? "" : providedBlock.trim();
        LootTableModel model = new LootTableModel(effectiveType);
        ValidationReport report = new ValidationReport();

        if (xml == null || xml.isBlank()) {
            report.addWarning(null, "The Blockly workspace is empty; an empty loot table will be generated.");
            return new ParseResult(model, report);
        }

        Document document;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            var builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new org.xml.sax.ErrorHandler() {
                @Override
                public void warning(org.xml.sax.SAXParseException exception) {
                    // Parse errors are reported through the validation report.
                }

                @Override
                public void error(org.xml.sax.SAXParseException exception) {
                    // Parse errors are reported through the validation report.
                }

                @Override
                public void fatalError(org.xml.sax.SAXParseException exception) throws org.xml.sax.SAXException {
                    throw exception;
                }
            });
            document = builder.parse(new InputSource(new StringReader(xml)));
        } catch (Exception exception) {
            report.addError(null, "The Blockly document is not valid XML: " + exception.getMessage());
            return new ParseResult(model, report);
        }

        Element root = document.getDocumentElement();
        List<Element> topBlocks = directChildren(root, "block");
        Element startBlock = null;
        int extraStarts = 0;
        for (Element block : topBlocks) {
            if ("loottable_start".equals(block.getAttribute("type"))) {
                if (startBlock == null) {
                    startBlock = block;
                } else {
                    extraStarts++;
                }
            }
        }
        if (extraStarts > 0) {
            report.addWarning("loottable_start",
                    "Only one loot table start block is allowed; " + extraStarts + " extra start block(s) were ignored.");
        }

        if (startBlock != null) {
            parsePoolChain(nextBlock(startBlock), effectiveType, effectiveResolver, effectiveProvidedBlock, model, report);
            for (Element block : topBlocks) {
                if (block != startBlock && "loottable_pool".equals(block.getAttribute("type"))) {
                    report.addWarning("loottable_pool",
                            "A pool block is not connected to the start block and was ignored.");
                }
            }
        } else {
            report.addWarning(null, "No loot table start block was found; top-level pools will still be read.");
            int poolIndex = 0;
            for (Element block : topBlocks) {
                if ("loottable_pool".equals(block.getAttribute("type"))) {
                    poolIndex++;
                    parsePool(block, poolIndex, effectiveType, effectiveResolver, effectiveProvidedBlock, model, report);
                }
            }
        }

        if (model.getPools().isEmpty()) {
            report.addWarning(null, "No valid loot table pools are available; an empty loot table will be generated.");
        }
        return new ParseResult(model, report);
    }

    private static int parsePoolChain(Element first, LootTableType type, ItemNameResolver resolver,
            String providedBlock, LootTableModel model, ValidationReport report) {
        int poolCount = 0;
        for (Element block = first; block != null; block = nextBlock(block)) {
            String blockType = block.getAttribute("type");
            if ("loottable_pool".equals(blockType)) {
                poolCount++;
                parsePool(block, poolCount, type, resolver, providedBlock, model, report);
            } else if (!"loottable_start".equals(blockType)) {
                report.addWarning(blockType, "Block " + blockType + " is not a pool and was skipped.");
            }
        }
        return poolCount;
    }

    private static void parsePool(Element block, int poolIndex, LootTableType type, ItemNameResolver resolver,
            String providedBlock, LootTableModel model, ValidationReport report) {
        LootTableModel.Pool pool = new LootTableModel.Pool();
        pool.setRolls(readWholeNumber(block, "rolls", 1, 0, "Pool " + poolIndex + " rolls", report));
        pool.setBonusRolls(readWholeNumber(block, "bonus_rolls", 0, 0,
                "Pool " + poolIndex + " bonus rolls", report));

        parseConditionChain(statementFirstBlock(block, "conditions"), type, resolver, providedBlock,
                pool.getConditions(), report);
        int entryCount = parseEntryChain(statementFirstBlock(block, "entries"), poolIndex, type, resolver,
                providedBlock, pool.getEntries(), report);
        if (entryCount == 0) {
            report.addWarning("loottable_pool",
                    "Pool " + poolIndex + " has no valid entries and was skipped.");
            return;
        }
        model.addPool(pool);
    }

    private static int parseEntryChain(Element first, int poolIndex, LootTableType type, ItemNameResolver resolver,
            String providedBlock, List<LootTableModel.Entry> entries, ValidationReport report) {
        int entryCount = 0;
        for (Element block = first; block != null; block = nextBlock(block)) {
            String blockType = block.getAttribute("type");
            if (!"loottable_entry_item".equals(blockType)
                    && !"loottable_entry_item_simple".equals(blockType)) {
                report.addWarning(blockType,
                        "Block " + blockType + " is not an item entry and was skipped in pool " + poolIndex + ".");
                continue;
            }
            LootTableModel.Entry entry = parseEntry(block, type, resolver, providedBlock, report);
            if (entry != null) {
                entries.add(entry);
                entryCount++;
            }
        }
        return entryCount;
    }

    private static LootTableModel.Entry parseEntry(Element block, LootTableType type, ItemNameResolver resolver,
            String providedBlock, ValidationReport report) {
        Element itemBlock = valueBlock(block, "item");
        if (itemBlock == null) {
            report.addWarning("loottable_entry_item",
                    "An item entry must use an item block; the entry was skipped.");
            return null;
        }

        ItemSpec itemSpec = parseItemProducer(itemBlock, resolver, report);
        if (itemSpec == null || itemSpec.item().isBlank()) {
            return null;
        }

        LootTableModel.Entry entry = new LootTableModel.Entry();
        entry.setItem(itemSpec.item());
        entry.setCount(parseCount(block, report));
        entry.setWeight(readWholeNumber(block, "weight", 1, 1, "Entry weight", report));
        entry.getFunctions().addAll(itemSpec.functions());

        parseConditionChain(statementFirstBlock(block, "conditions"), type, resolver, providedBlock,
                entry.getConditions(), report);
        parseFunctionChain(statementFirstBlock(block, "functions"), entry.getFunctions(), report);
        return entry;
    }

    private record ItemSpec(String item, List<LootTableModel.LootFunction> functions) {
    }

    /**
     * Parses an item-producing value block. Item producer blocks may be nested:
     * a function block wraps another item value and adds its loot function.
     */
    private static ItemSpec parseItemProducer(Element itemBlock, ItemNameResolver resolver,
            ValidationReport report) {
        String itemType = itemBlock.getAttribute("type");
        if ("loottable_item_selector".equals(itemType)) {
            String item = resolveItemSelector(itemBlock, resolver, report);
            return item.isBlank() ? null : new ItemSpec(item, new ArrayList<>());
        }

        Element baseBlock = valueBlock(itemBlock, "item");
        if (baseBlock == null) {
            report.addWarning(itemType, "An item function block has no base item and was skipped.");
            return null;
        }
        ItemSpec base = parseItemProducer(baseBlock, resolver, report);
        if (base == null) {
            return null;
        }

        LootTableModel.LootFunction function = switch (itemType) {
            case "loottable_item_with_enchantment" -> {
                List<LootTableModel.EnchantmentLevel> enchantments = parseEnchantmentRows(itemBlock, report);
                if (enchantments.isEmpty()) {
                    report.addWarning(itemType,
                            "An enchanted item has no enchantment rows and was skipped.");
                    yield null;
                }
                LootTableModel.LootFunction value = new LootTableModel.LootFunction(itemType);
                value.getProperties().put("enchantments", enchantments);
                yield value;
            }
            case "loottable_item_enchant_random" -> {
                List<String> options = parseEnchantmentOptions(itemBlock, report);
                if (options.isEmpty()) {
                    report.addWarning(itemType,
                            "The random-enchantment item has no enchantment list and was skipped.");
                    yield null;
                }
                LootTableModel.LootFunction value = new LootTableModel.LootFunction(itemType);
                value.getProperties().put("options", options);
                yield value;
            }
            case "loottable_item_enchant_random_any" -> {
                LootTableModel.NumberProvider min = nonNegative(
                        parseNumberInput(itemBlock, "min", 1, "Random level minimum", report),
                        itemType, "Random level minimum", report);
                LootTableModel.NumberProvider max = nonNegative(
                        parseNumberInput(itemBlock, "max", 3, "Random level maximum", report),
                        itemType, "Random level maximum", report);
                if (isSimpleNumber(min) && isSimpleNumber(max) && max.getValue() < min.getValue()) {
                    report.addWarning(itemType,
                            "Random level maximum was smaller than the minimum; the values were swapped.");
                    LootTableModel.NumberProvider swap = min;
                    min = max;
                    max = swap;
                }
                LootTableModel.LootFunction value = new LootTableModel.LootFunction(itemType);
                value.getProperties().put("min", min);
                value.getProperties().put("max", max);
                value.getProperties().put("treasure", readBoolean(itemBlock, "treasure", true,
                        "Include treasure enchantments", report));
                yield value;
            }
            case "loottable_item_enchant_levels" -> {
                LootTableModel.NumberProvider levels = nonNegative(
                        parseNumberInput(itemBlock, "levels", 1, "Enchanting level", report),
                        itemType, "Enchanting level", report);
                List<String> options = parseEnchantmentOptions(itemBlock, report);
                if (options.isEmpty()) {
                    report.addWarning(itemType,
                            "The leveled-enchantment item has no enchantment list and was skipped.");
                    yield null;
                }
                LootTableModel.LootFunction value = new LootTableModel.LootFunction(itemType);
                value.getProperties().put("levels", levels);
                value.getProperties().put("options", options);
                yield value;
            }
            case "loottable_item_enchant_levels_any" -> {
                LootTableModel.NumberProvider levels = nonNegative(
                        parseNumberInput(itemBlock, "levels", 1, "Enchanting level", report),
                        itemType, "Enchanting level", report);
                LootTableModel.LootFunction value = new LootTableModel.LootFunction(itemType);
                value.getProperties().put("levels", levels);
                value.getProperties().put("treasure", readBoolean(itemBlock, "treasure", true,
                        "Include treasure enchantments", report));
                yield value;
            }
            default -> {
                report.addWarning(itemType, "Unsupported item value block " + itemType + " was skipped.");
                yield null;
            }
        };
        if (function == null) {
            return null;
        }
        base.functions().add(function);
        return base;
    }

    private static String resolveItemSelector(Element itemBlock, ItemNameResolver resolver,
            ValidationReport report) {
        String rawItem = fieldValue(itemBlock, "item").trim();
        if (rawItem.isBlank()) {
            report.addWarning("loottable_entry_item", "An item entry has no selected item and was skipped.");
            return "";
        }
        String item = resolver.resolve(rawItem);
        if (item.isBlank()) {
            report.addWarning("loottable_entry_item",
                    "Item value '" + rawItem + "' could not be resolved to a registry name and was skipped.");
            return "";
        }
        if (item.startsWith("#")) {
            report.addWarning("loottable_entry_item",
                    "Item tags are not supported by this version of the plugin; entry '" + item + "' was skipped.");
            return "";
        }
        if ("minecraft:air".equals(item)) {
            report.addWarning("loottable_entry_item", "Air cannot be used as a loot table item and was skipped.");
            return "";
        }
        return item;
    }

    private static List<String> parseEnchantmentOptions(Element block, ValidationReport report) {
        int count = mutationInputCount(block);
        List<String> enchantments = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            String enchantment = normalizedEnchantment(fieldValue(block, "enchantment" + index));
            if (!enchantment.isBlank()) {
                enchantments.add(enchantment);
            }
        }
        if (enchantments.isEmpty()) {
            report.addWarning(block.getAttribute("type"), "Enchantment list is empty.");
        }
        return enchantments;
    }

    private static String normalizedEnchantment(String raw) {
        String enchantment = raw == null ? "" : raw.trim();
        if (enchantment.isBlank()) {
            return "";
        }
        enchantment = enchantment.toLowerCase(Locale.ROOT);
        return enchantment.contains(":") ? enchantment : "minecraft:" + enchantment;
    }

    private static boolean parseTimeCheckCondition(Element block, String blockType,
            LootTableModel.Condition condition, ValidationReport report) {
        LootTableModel.NumberProvider minimum = nonNegative(
                parseNumberInput(block, "min", 0, false, "Time minimum", report),
                blockType, "Time minimum", report);
        LootTableModel.NumberProvider maximum = nonNegative(
                parseNumberInput(block, "max", 12000, false, "Time maximum", report),
                blockType, "Time maximum", report);
        condition.getProperties().put("min", minimum);
        condition.getProperties().put("max", maximum);

        if ("loottable_time_day".equals(blockType)) {
            condition.getProperties().put("period", 24000L);
        } else if ("loottable_time_periodic".equals(blockType)) {
            LootTableModel.NumberProvider period = nonNegative(
                    parseNumberInput(block, "period", 24000, false, "Time period", report),
                    blockType, "Time period", report);
            if (!isSimpleNumber(period)) {
                report.addWarning(blockType,
                        "The time-check period must be a constant number in Minecraft 1.21.1; the condition was skipped.");
                return false;
            }
            long value = (long) period.getValue();
            if (value <= 0) {
                report.addWarning(blockType, "The time-check period must be greater than zero.");
                return false;
            }
            condition.getProperties().put("period", value);
        }
        return true;
    }

    private static boolean parseWorldCondition(Element block, String blockType,
            LootTableModel.Condition condition, ValidationReport report) {
        switch (blockType) {
            case "loottable_world_dimension" -> {
                String dimension = dimensionValue(fieldValue(block, "dimension"));
                if (dimension.isBlank()) {
                    report.addWarning(blockType, "The dimension condition has no dimension selected.");
                    return false;
                }
                condition.getProperties().put("dimension", dimension);
            }
            case "loottable_world_biome" -> {
                String biome = worldValue(fieldValue(block, "biome"));
                if (biome.isBlank()) {
                    report.addWarning(blockType, "The biome condition has no biome selected.");
                    return false;
                }
                condition.getProperties().put("biome", biome);
            }
            case "loottable_world_biome_tag" -> {
                String tag = fieldValue(block, "tag").trim();
                if (tag.isBlank()) {
                    report.addWarning(blockType, "The biome tag condition has no tag.");
                    return false;
                }
                condition.getProperties().put("tag", tag);
            }
            case "loottable_world_smokey" -> condition.getProperties().put("smokey", true);
            case "loottable_world_structure" -> {
                String structure = worldValue(fieldValue(block, "structure"));
                if (structure.isBlank()) {
                    report.addWarning(blockType, "The structure condition has no structure selected.");
                    return false;
                }
                condition.getProperties().put("structure", structure);
            }
            case "loottable_world_structure_tag" -> {
                String tag = fieldValue(block, "tag").trim();
                if (tag.isBlank()) {
                    report.addWarning(blockType, "The structure tag condition has no tag.");
                    return false;
                }
                condition.getProperties().put("tag", tag);
            }
            case "loottable_world_light" -> {
                int level = (int) Math.max(0, Math.min(15,
                        readNumber(block, "value", 0, "Light level", report)));
                condition.getProperties().put("min", level);
                condition.getProperties().put("max", level);
            }
            case "loottable_world_light_range" -> {
                int minimum = (int) Math.max(0, Math.min(15,
                        readNumber(block, "min", 0, "Light level minimum", report)));
                int maximum = (int) Math.max(minimum, Math.min(15,
                        readNumber(block, "max", 15, "Light level maximum", report)));
                condition.getProperties().put("min", minimum);
                condition.getProperties().put("max", maximum);
            }
            case "loottable_world_position_x", "loottable_world_position_y",
                    "loottable_world_position_z" -> {
                condition.getProperties().put("value",
                        readNumber(block, "value", 0, "Coordinate", report));
            }
            case "loottable_world_position_x_range", "loottable_world_position_y_range",
                    "loottable_world_position_z_range" -> {
                double minimum = readNumber(block, "min", 0, "Coordinate minimum", report);
                double maximum = readNumber(block, "max", 0, "Coordinate maximum", report);
                if (minimum > maximum) {
                    report.addWarning(blockType, "The coordinate range was reversed and has been swapped.");
                    double swap = minimum;
                    minimum = maximum;
                    maximum = swap;
                }
                condition.getProperties().put("min", minimum);
                condition.getProperties().put("max", maximum);
            }
            case "loottable_world_block_tag" -> {
                String tag = fieldValue(block, "tag").trim();
                if (tag.isBlank()) {
                    report.addWarning(blockType, "The block tag condition has no tag.");
                    return false;
                }
                condition.getProperties().put("tag", tag);
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    private static String worldValue(String raw) {
        String value = raw == null ? "" : raw.trim();
        if (value.isBlank()) {
            return "";
        }
        value = value.toLowerCase(Locale.ROOT);
        return value.contains(":") ? value : "minecraft:" + value;
    }

    private static String dimensionValue(String raw) {
        String value = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        return switch (value) {
            case "surface", "overworld", "minecraft:surface" -> "minecraft:overworld";
            case "nether", "the_nether", "minecraft:nether" -> "minecraft:the_nether";
            case "end", "the_end", "minecraft:end" -> "minecraft:the_end";
            case "" -> "";
            default -> value.contains(":") ? value : "minecraft:" + value;
        };
    }

    private static void parseConditionChain(Element first, LootTableType type, ItemNameResolver resolver,
            String providedBlock, List<LootTableModel.Condition> conditions, ValidationReport report) {
        for (Element block = first; block != null; block = nextBlock(block)) {
            LootTableModel.Condition condition = parseCondition(block, type, resolver, providedBlock, report);
            if (condition != null) {
                conditions.add(condition);
            }
        }
    }

    /**
     * Parses one condition block, including nested inverted conditions.
     *
     * @return condition or {@code null} when the block is unknown or illegal
     */
    private static LootTableModel.Condition parseCondition(Element block, LootTableType type,
            ItemNameResolver resolver, String providedBlock, ValidationReport report) {
        String blockType = block.getAttribute("type");
        ConditionDefinition definition = ConditionScopeRegistry.get(blockType);
        if (definition == null) {
            report.addWarning(blockType, "Unknown condition block " + blockType + " was skipped.");
            return null;
        }
        if (!definition.isAllowed(type)) {
            report.addError(blockType, definition.invalidScopeMessage(type));
            return null;
        }

        LootTableModel.Condition condition = new LootTableModel.Condition(blockType, definition.jsonType());
        switch (blockType) {
            case "loottable_random_chance" -> {
                double chance = readNumber(block, "chance", 0.5, "Random chance", report);
                if (chance < 0 || chance > 1) {
                    report.addWarning(blockType,
                            "Random chance must be between 0 and 1; the value was clamped into that range.");
                    chance = Math.max(0, Math.min(1, chance));
                }
                condition.getProperties().put("chance", chance);
            }
            case "loottable_weather_check" -> {
                condition.getProperties().put("raining", readBoolean(block, "raining", false,
                        "Weather check raining", report));
                condition.getProperties().put("thundering", readBoolean(block, "thundering", false,
                        "Weather check thundering", report));
            }
            case "loottable_block_state_boolean", "loottable_block_state_integer", "loottable_block_state_enum" -> {
                String resolvedBlock = parseBlockValue(block, "block", providedBlock, resolver, report);
                if (resolvedBlock.isBlank()) {
                    return null;
                }
                String property = fieldValue(block, "property").trim();
                String value = fieldValue(block, "value").trim();
                if (property.isBlank()) {
                    report.addWarning(blockType,
                            "Block state property has no property name and was skipped.");
                    return null;
                }
                if (value.isBlank()) {
                    report.addWarning(blockType, "Block state property has no comparison value and was skipped.");
                    return null;
                }
                if ("loottable_block_state_boolean".equals(blockType)) {
                    value = value.toLowerCase(java.util.Locale.ROOT);
                }
                condition.getProperties().put("block", resolvedBlock);
                condition.getProperties().put("property", property);
                condition.getProperties().put("value", value);
            }
            case "loottable_entity_has_potion" -> {
                String entity = parseEntityValue(block, "entity", type, report);
                if (entity.isBlank()) {
                    return null;
                }
                String effect = fieldValue(block, "potion").trim();
                if (effect.isBlank()) {
                    report.addWarning(blockType, "Potion condition has no effect selected and was skipped.");
                    return null;
                }
                if (!effect.contains(":")) {
                    effect = "minecraft:" + effect;
                }
                condition.getProperties().put("entity", entity);
                condition.getProperties().put("effect", effect);
            }
            case "loottable_block_nbt_logic", "loottable_block_nbt_number", "loottable_block_nbt_text" -> {
                if (!parseBlockNbtCondition(block, blockType, providedBlock, resolver, condition, report)) {
                    return null;
                }
            }
            case "loottable_entity_nbt_logic", "loottable_entity_nbt_number", "loottable_entity_nbt_text" -> {
                if (!parseEntityNbtCondition(block, blockType, type, condition, report)) {
                    return null;
                }
            }
            case "loottable_item_has_enchantment_min", "loottable_item_has_enchantment_range" -> {
                if (!parseItemEnchantmentCondition(block, blockType, type, condition, report)) {
                    return null;
                }
            }
            case "loottable_time_check", "loottable_time_day", "loottable_time_periodic" -> {
                if (!parseTimeCheckCondition(block, blockType, condition, report)) {
                    return null;
                }
            }
            case "loottable_world_dimension", "loottable_world_biome", "loottable_world_biome_tag",
                    "loottable_world_smokey", "loottable_world_structure",
                    "loottable_world_structure_tag", "loottable_world_light",
                    "loottable_world_light_range", "loottable_world_position_x",
                    "loottable_world_position_y", "loottable_world_position_z",
                    "loottable_world_position_x_range", "loottable_world_position_y_range",
                    "loottable_world_position_z_range", "loottable_world_block_tag" -> {
                if (!parseWorldCondition(block, blockType, condition, report)) {
                    return null;
                }
            }
            case "loottable_logic_and", "loottable_logic_or" -> {
                if (!parseLogicTerms(block, blockType, type, resolver, providedBlock, condition, report)) {
                    return null;
                }
            }
            case "loottable_logic_not" -> {
                if (!parseLogicTerms(block, blockType, type, resolver, providedBlock, condition, report)) {
                    return null;
                }
                if (condition.getTerms().size() == 1) {
                    condition.setTerm(condition.getTerms().get(0));
                } else {
                    LootTableModel.Condition allOf = new LootTableModel.Condition(
                            "loottable_logic_and", "minecraft:all_of");
                    allOf.getTerms().addAll(condition.getTerms());
                    condition.setTerm(allOf);
                }
            }
            default -> {
                // Killed by player and survives explosion carry no extra fields.
            }
        }
        return condition;
    }

    private static boolean parseLogicTerms(Element block, String blockType, LootTableType type,
            ItemNameResolver resolver, String providedBlock, LootTableModel.Condition condition,
            ValidationReport report) {
        Element first = statementFirstBlock(block, "conditions");
        if (first == null) {
            report.addWarning(blockType, "Logic condition has no nested condition and was skipped.");
            return false;
        }
        parseConditionChain(first, type, resolver, providedBlock, condition.getTerms(), report);
        if (condition.getTerms().isEmpty()) {
            report.addWarning(blockType, "Logic condition has no valid nested condition and was skipped.");
            return false;
        }
        return true;
    }

    private static boolean parseBlockNbtCondition(Element block, String blockType, String providedBlock,
            ItemNameResolver resolver, LootTableModel.Condition condition, ValidationReport report) {
        String resolvedBlock = parseBlockValue(block, "block", providedBlock, resolver, report);
        if (resolvedBlock.isBlank()) {
            return false;
        }
        String tag = fieldValue(block, "tag").trim();
        if (tag.isBlank()) {
            report.addWarning(blockType, "Block NBT condition has no NBT tag and was skipped.");
            return false;
        }
        String value = nbtComparisonValue(block, blockType, report);
        if (value == null) {
            return false;
        }
        condition.getProperties().put("block", resolvedBlock);
        condition.getProperties().put("nbt", "{" + quoteNbtKey(tag) + ":" + value + "}");
        return true;
    }

    private static boolean parseEntityNbtCondition(Element block, String blockType, LootTableType type,
            LootTableModel.Condition condition, ValidationReport report) {
        String entity = parseEntityValue(block, "entity", type, report);
        if (entity.isBlank()) {
            return false;
        }
        String tag = fieldValue(block, "tag").trim();
        if (tag.isBlank()) {
            report.addWarning(blockType, "Entity NBT condition has no NBT tag and was skipped.");
            return false;
        }
        String value = nbtComparisonValue(block, blockType, report);
        if (value == null) {
            return false;
        }
        condition.getProperties().put("entity", entity);
        condition.getProperties().put("nbt", "{" + quoteNbtKey(tag) + ":" + value + "}");
        return true;
    }

    private static boolean parseItemEnchantmentCondition(Element block, String blockType, LootTableType type,
            LootTableModel.Condition condition, ValidationReport report) {
        String enchantment = fieldValue(block, "enhancement").trim();
        if (enchantment.isBlank()) {
            report.addWarning(blockType, "Enchantment condition has no enchantment selected and was skipped.");
            return false;
        }
        enchantment = enchantment.toLowerCase(java.util.Locale.ROOT);
        if (!enchantment.contains(":")) {
            enchantment = "minecraft:" + enchantment;
        }

        int minimum = readEnchantmentLevelInput(block, "min", 1, "Minimum enchantment level", report);
        Integer maximum = null;
        if ("loottable_item_has_enchantment_range".equals(blockType)) {
            maximum = readEnchantmentLevelInput(block, "max", 3, "Maximum enchantment level", report);
            if (maximum < minimum) {
                report.addWarning(blockType,
                        "Maximum enchantment level is lower than the minimum; the bounds were swapped.");
                int swap = minimum;
                minimum = maximum;
                maximum = swap;
            }
        }

        Element itemBlock = valueBlock(block, "item");
        if (itemBlock == null) {
            condition.getProperties().put("mode", "tool");
        } else if ("loottable_entity_mainhand_item".equals(itemBlock.getAttribute("type"))) {
            String entity = parseEntityValue(itemBlock, "entity", type, report);
            if (entity.isBlank()) {
                return false;
            }
            condition.getProperties().put("mode", "entity");
            condition.getProperties().put("entity", entity);
        } else {
            report.addWarning(itemBlock.getAttribute("type"),
                    "Enchantment condition only supports a main-hand item source in this version; the condition was skipped.");
            return false;
        }

        condition.getProperties().put("enchantment", enchantment);
        condition.getProperties().put("minLevel", minimum);
        if (maximum != null) {
            condition.getProperties().put("maxLevel", maximum);
        }
        return true;
    }

    private static List<LootTableModel.EnchantmentLevel> parseEnchantmentRows(Element block,
            ValidationReport report) {
        int count = mutationInputCount(block);
        List<LootTableModel.EnchantmentLevel> enchantments = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            String enchantment = normalizedEnchantment(fieldValue(block, "enchantment" + index));
            if (enchantment.isBlank()) {
                continue;
            }
            LootTableModel.NumberProvider level;
            Element levelBlock = valueBlock(block, "level" + index);
            if (levelBlock != null) {
                level = parseNumberValue(levelBlock, 1, false,
                        "Enchantment level", report);
            } else {
                // Keep old workspaces readable when the former numeric field was used.
                level = LootTableModel.NumberProvider.number(
                        readEnchantmentLevelField(block, index, report), false);
            }
            enchantments.add(new LootTableModel.EnchantmentLevel(enchantment,
                    normalizeEnchantmentLevel(level, block.getAttribute("type"), report)));
        }
        return enchantments;
    }

    private static LootTableModel.NumberProvider normalizeEnchantmentLevel(
            LootTableModel.NumberProvider level, String blockType, ValidationReport report) {
        if (!isSimpleNumber(level)) {
            return level;
        }
        double value = level.getValue();
        int normalized = (int) value;
        if (value != normalized) {
            report.addWarning(blockType,
                    "Enchantment level '" + value + "' is not an integer; the fractional part was removed.");
        }
        if (normalized < 1) {
            report.addWarning(blockType, "Enchantment level cannot be lower than 1; the minimum was used.");
            normalized = 1;
        }
        if (normalized > 255) {
            report.addWarning(blockType, "Enchantment level cannot exceed 255; the value was clamped.");
            normalized = 255;
        }
        return "constant".equals(level.getType())
                ? LootTableModel.NumberProvider.constant(normalized, false)
                : LootTableModel.NumberProvider.number(normalized, false);
    }

    private static int mutationInputCount(Element block) {
        for (Element mutation : directChildren(block, "mutation")) {
            String inputs = mutation.getAttribute("inputs");
            if (!inputs.isBlank()) {
                try {
                    return Math.max(1, Math.min(64, Integer.parseInt(inputs)));
                } catch (NumberFormatException ignored) {
                    return 1;
                }
            }
        }
        return 1;
    }

    private static int readEnchantmentLevelField(Element block, int index, ValidationReport report) {
        String raw = fieldValue(block, "level" + index).trim();
        if (raw.isBlank()) {
            return 1;
        }
        try {
            double number = Double.parseDouble(raw);
            int level = (int) number;
            if (number != level) {
                report.addWarning("loottable_item_with_enchantment",
                        "Enchantment level '" + raw + "' is not an integer; the fractional part was removed.");
            }
            if (level < 1) {
                report.addWarning("loottable_item_with_enchantment",
                        "Enchantment level cannot be lower than 1; the minimum was used.");
                level = 1;
            }
            if (level > 255) {
                report.addWarning("loottable_item_with_enchantment",
                        "Enchantment level cannot exceed 255; the value was clamped.");
                level = 255;
            }
            return level;
        } catch (NumberFormatException exception) {
            report.addWarning("loottable_item_with_enchantment",
                    "Enchantment level '" + raw + "' is not a number; the default value was used.");
            return 1;
        }
    }

    private static int readEnchantmentLevelInput(Element block, String inputName, int fallback, String label,
            ValidationReport report) {
        Element value = valueBlock(block, inputName);
        if (value == null) {
            return fallback;
        }
        String valueType = value.getAttribute("type");
        if (!"loottable_integer".equals(valueType) && !"loottable_integer_provider".equals(valueType)) {
            report.addWarning(valueType,
                    label + " must use an Integer block; the default value was used.");
            return fallback;
        }
        String raw = fieldValue(value, "value").trim();
        if (raw.isBlank()) {
            report.addWarning(valueType, label + " is empty; the default value was used.");
            return fallback;
        }
        try {
            double number = Double.parseDouble(raw);
            int level = (int) number;
            if (number != level) {
                report.addWarning(valueType,
                        label + " value '" + raw + "' is not an integer; the fractional part was removed.");
            }
            if (level < 1) {
                report.addWarning(valueType, label + " cannot be lower than 1; the minimum was used.");
                level = 1;
            }
            if (level > 255) {
                report.addWarning(valueType, label + " cannot exceed 255; the value was clamped.");
                level = 255;
            }
            return level;
        } catch (NumberFormatException exception) {
            report.addWarning(valueType,
                    label + " value '" + raw + "' is not a number; the default value was used.");
            return fallback;
        }
    }

    /**
     * Converts the comparison field of an NBT condition to an SNBT value.
     *
     * @return SNBT value or {@code null} when the value is invalid
     */
    private static String nbtComparisonValue(Element block, String blockType, ValidationReport report) {
        String raw = fieldValue(block, "value").trim();
        if (raw.isBlank()) {
            report.addWarning(blockType, "NBT condition has no comparison value and was skipped.");
            return null;
        }
        if (blockType.endsWith("_logic")) {
            String normalized = raw.toLowerCase(Locale.ROOT);
            if ("true".equals(normalized) || "1".equals(normalized)) {
                return "1b";
            }
            if ("false".equals(normalized) || "0".equals(normalized)) {
                return "0b";
            }
            report.addWarning(blockType, "NBT logic value '" + raw + "' is not true or false and was skipped.");
            return null;
        }
        if (blockType.endsWith("_number")) {
            if (!NBT_NUMBER.matcher(raw).matches()) {
                report.addWarning(blockType, "NBT number value '" + raw + "' is not valid and was skipped.");
                return null;
            }
            return raw;
        }
        return quoteNbtText(raw);
    }

    private static String quoteNbtKey(String key) {
        return NBT_TAG.matcher(key).matches() ? key : quoteNbtText(key);
    }

    private static String quoteNbtText(String value) {
        StringBuilder escaped = new StringBuilder(value.length() + 2);
        escaped.append('"');
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '\\' -> escaped.append("\\\\");
                case '"' -> escaped.append("\\\"");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> escaped.append(character);
            }
        }
        return escaped.append('"').toString();
    }

    private static LootTableModel.NumberProvider parseCount(Element block, ValidationReport report) {
        Element countBlock = valueBlock(block, "count");
        if (countBlock == null) {
            return LootTableModel.NumberProvider.number(1, false);
        }
        return nonNegative(parseNumberValue(countBlock, 1, false, "Entry count", report),
                "loottable_entry_item", "Entry count", report);
    }

    private static void parseFunctionChain(Element first, List<LootTableModel.LootFunction> functions,
            ValidationReport report) {
        for (Element block = first; block != null; block = nextBlock(block)) {
            LootTableModel.LootFunction function = parseFunction(block, report);
            if (function != null) {
                functions.add(function);
            }
        }
    }

    private static LootTableModel.LootFunction parseFunction(Element block, ValidationReport report) {
        String blockType = block.getAttribute("type");
        LootTableModel.LootFunction function = new LootTableModel.LootFunction(blockType);
        switch (blockType) {
            case "loottable_function_set_count_add" -> {
                LootTableModel.NumberProvider count = nonNegative(
                        parseNumberInput(block, "count", 1, "Added count", report),
                        blockType, "Added count", report);
                function.getProperties().put("count", count);
            }
            case "loottable_function_apply_bonus_uniform" -> {
                function.getProperties().put("enchantment", parseEnchantmentField(block, "minecraft:fortune",
                        "Bonus enchantment", report));
                function.getProperties().put("bonus", readPlainIntegerInput(block, "bonus", 1,
                        "Bonus multiplier", report));
            }
            case "loottable_function_apply_bonus_binomial" -> {
                function.getProperties().put("enchantment", parseEnchantmentField(block, "minecraft:fortune",
                        "Bonus enchantment", report));
                function.getProperties().put("extra", readPlainIntegerInput(block, "extra", 1,
                        "Extra rolls", report));
                function.getProperties().put("probability", readPlainFloatInput(block, "probability", 0.5,
                        "Bonus probability", report, 0.0, 1.0));
            }
            case "loottable_function_apply_bonus_ore" -> function.getProperties().put("enchantment",
                    parseEnchantmentField(block, "minecraft:fortune", "Ore bonus enchantment", report));
            case "loottable_function_enchanted_count_increase" -> {
                function.getProperties().put("enchantment", parseEnchantmentField(block, "minecraft:looting",
                        "Count increase enchantment", report));
                function.getProperties().put("count", parseCountIncreaseInput(block, report));
            }
            case "loottable_function_limit_count" -> {
                LootTableModel.NumberProvider limit = nonNegative(
                        parseNumberInput(block, "limit", 9, "Count limit", report),
                        blockType, "Count limit", report);
                function.getProperties().put("limit", limit);
            }
            default -> {
                report.addWarning(blockType, "Unknown loot function " + blockType + " was skipped.");
                return null;
            }
        }
        return function;
    }

    private static String parseEnchantmentField(Element block, String fallback, String label,
            ValidationReport report) {
        String raw = fieldValue(block, "enchantment").trim();
        if (raw.isBlank()) {
            return fallback;
        }
        String enchantment = normalizedEnchantment(raw);
        if (enchantment.isBlank()) {
            report.addWarning(block.getAttribute("type"), label + " is empty; the default was used.");
            return fallback;
        }
        return enchantment;
    }

    private static int readPlainIntegerInput(Element block, String inputName, int fallback, String label,
            ValidationReport report) {
        LootTableModel.NumberProvider provider = valueBlock(block, inputName) == null
                ? LootTableModel.NumberProvider.number(fallback, false)
                : parseNumberValue(valueBlock(block, inputName), fallback, false, label, report);
        if (!isSimpleNumber(provider)) {
            report.addWarning(block.getAttribute("type"),
                    label + " must be a plain number in Minecraft 1.21.1; the default was used.");
            return fallback;
        }
        double raw = provider.getValue();
        int value = (int) raw;
        if (raw != value) {
            report.addWarning(block.getAttribute("type"),
                    label + " must be an integer; the fractional part was removed.");
        }
        if (value < 0) {
            report.addWarning(block.getAttribute("type"),
                    label + " cannot be negative; the value was clamped to 0.");
            return 0;
        }
        return value;
    }

    private static double readPlainFloatInput(Element block, String inputName, double fallback, String label,
            ValidationReport report, double minimum, double maximum) {
        LootTableModel.NumberProvider provider = valueBlock(block, inputName) == null
                ? LootTableModel.NumberProvider.number(fallback, true)
                : parseNumberValue(valueBlock(block, inputName), fallback, true, label, report);
        if (!isSimpleNumber(provider)) {
            report.addWarning(block.getAttribute("type"),
                    label + " must be a plain number in Minecraft 1.21.1; the default was used.");
            return fallback;
        }
        double value = provider.getValue();
        if (value < minimum || value > maximum) {
            report.addWarning(block.getAttribute("type"),
                    label + " must be between " + minimum + " and " + maximum + "; the value was clamped.");
            value = Math.max(minimum, Math.min(maximum, value));
        }
        return value;
    }

    private static LootTableModel.NumberProvider parseCountIncreaseInput(Element block, ValidationReport report) {
        Element value = valueBlock(block, "count");
        if (value == null) {
            return LootTableModel.NumberProvider.uniform(
                    LootTableModel.NumberProvider.number(0, false),
                    LootTableModel.NumberProvider.number(1, false));
        }
        return nonNegative(parseNumberValue(value, 0, false, "Count increase", report),
                block.getAttribute("type"), "Count increase", report);
    }

    private static LootTableModel.NumberProvider parseNumberInput(Element block, String inputName, double fallback,
            String label, ValidationReport report) {
        return parseNumberInput(block, inputName, fallback, false, label, report);
    }

    private static LootTableModel.NumberProvider parseNumberInput(Element block, String inputName, double fallback,
            boolean fallbackFloating, String label, ValidationReport report) {
        Element valueBlock = valueBlock(block, inputName);
        if (valueBlock == null) {
            return LootTableModel.NumberProvider.number(fallback, fallbackFloating);
        }
        return parseNumberValue(valueBlock, fallback, fallbackFloating, label, report);
    }

    private static LootTableModel.NumberProvider parseNumberValue(Element valueBlock, double fallback,
            String label, ValidationReport report) {
        return parseNumberValue(valueBlock, fallback, false, label, report);
    }

    private static LootTableModel.NumberProvider parseNumberValue(Element valueBlock, double fallback,
            boolean fallbackFloating, String label, ValidationReport report) {
        String valueType = valueBlock.getAttribute("type");
        String raw;
        switch (valueType) {
            case "loottable_integer", "loottable_integer_provider" -> {
                raw = fieldValue(valueBlock, "value");
                return integerNumber(valueBlock, raw, fallback, fallbackFloating, label, report,
                        "loottable_integer_provider".equals(valueType));
            }
            case "loottable_float", "loottable_float_provider" -> {
                raw = fieldValue(valueBlock, "value");
                return floatingNumber(valueBlock, raw, fallback, fallbackFloating, label, report,
                        "loottable_float_provider".equals(valueType));
            }
            case "loottable_uniform" -> {
                LootTableModel.NumberProvider min = nonNegative(
                        parseNumberInput(valueBlock, "min", 1, "Uniform minimum", report),
                        valueType, "Uniform minimum", report);
                LootTableModel.NumberProvider max = nonNegative(
                        parseNumberInput(valueBlock, "max", 1, "Uniform maximum", report),
                        valueType, "Uniform maximum", report);
                if (isSimpleNumber(min) && isSimpleNumber(max) && max.getValue() < min.getValue()) {
                    report.addWarning(valueType,
                            "Uniform maximum was smaller than the minimum; the values were swapped.");
                    LootTableModel.NumberProvider swap = min;
                    min = max;
                    max = swap;
                }
                return LootTableModel.NumberProvider.uniform(min, max);
            }
            case "loottable_binomial" -> {
                LootTableModel.NumberProvider trials = integerTrials(
                        parseNumberInput(valueBlock, "n", 1, "Binomial trials", report), report);
                LootTableModel.NumberProvider probability = probability(
                        parseNumberInput(valueBlock, "p", 1.0, true, "Binomial probability", report),
                        report);
                return LootTableModel.NumberProvider.binomial(trials, probability);
            }
            case "math_number" -> {
                raw = fieldValue(valueBlock, "NUM");
                return raw.contains(".")
                        ? floatingNumber(valueBlock, raw, fallback, fallbackFloating, label, report, false)
                        : integerNumber(valueBlock, raw, fallback, fallbackFloating, label, report, false);
            }
            default -> {
                report.addWarning(valueType,
                        label + " only supports a number or number provider block; the default was used.");
                return LootTableModel.NumberProvider.number(fallback, fallbackFloating);
            }
        }
    }

    private static LootTableModel.NumberProvider integerNumber(Element block, String raw, double fallback,
            boolean fallbackFloating, String label, ValidationReport report, boolean provider) {
        String valueType = block.getAttribute("type");
        if (raw.isBlank()) {
            return provider
                    ? LootTableModel.NumberProvider.constant(fallback, fallbackFloating)
                    : LootTableModel.NumberProvider.number(fallback, fallbackFloating);
        }
        try {
            double value = Double.parseDouble(raw);
            if (!Double.isFinite(value)) {
                throw new NumberFormatException("not finite");
            }
            long truncated = (long) value;
            if (value != truncated) {
                report.addWarning(valueType, label + " value '" + raw
                        + "' is not an integer; the fractional part was removed.");
            }
            return provider
                    ? LootTableModel.NumberProvider.constant(truncated, false)
                    : LootTableModel.NumberProvider.number(truncated, false);
        } catch (NumberFormatException exception) {
            report.addWarning(valueType,
                    label + " value '" + raw + "' is not a number; the default was used.");
            return provider
                    ? LootTableModel.NumberProvider.constant(fallback, fallbackFloating)
                    : LootTableModel.NumberProvider.number(fallback, fallbackFloating);
        }
    }

    private static LootTableModel.NumberProvider floatingNumber(Element block, String raw, double fallback,
            boolean fallbackFloating, String label, ValidationReport report, boolean provider) {
        String valueType = block.getAttribute("type");
        if (raw.isBlank()) {
            return provider
                    ? LootTableModel.NumberProvider.constant(fallback, true)
                    : LootTableModel.NumberProvider.number(fallback, true);
        }
        try {
            double value = Double.parseDouble(raw);
            if (!Double.isFinite(value)) {
                throw new NumberFormatException("not finite");
            }
            return provider
                    ? LootTableModel.NumberProvider.constant(value, true)
                    : LootTableModel.NumberProvider.number(value, true);
        } catch (NumberFormatException exception) {
            report.addWarning(valueType,
                    label + " value '" + raw + "' is not a number; the default was used.");
            return provider
                    ? LootTableModel.NumberProvider.constant(fallback, true)
                    : LootTableModel.NumberProvider.number(fallback, true);
        }
    }

    private static LootTableModel.NumberProvider integerTrials(LootTableModel.NumberProvider provider,
            ValidationReport report) {
        if (provider != null && "number".equals(provider.getType())) {
            double value = provider.getValue();
            long truncated = (long) value;
            if (value != truncated) {
                report.addWarning("loottable_binomial", "Binomial trials value '" + value
                        + "' is not an integer; the fractional part was removed.");
            }
            return LootTableModel.NumberProvider.number(truncated, false);
        }
        return provider;
    }

    private static LootTableModel.NumberProvider probability(LootTableModel.NumberProvider provider,
            ValidationReport report) {
        if (provider != null && "number".equals(provider.getType())) {
            if (!provider.isFloating()) {
                report.addWarning("loottable_binomial",
                        "Binomial probability should use a Float block; the integer value was converted to a float.");
            }
            return LootTableModel.NumberProvider.number(provider.getValue(), true);
        }
        return provider;
    }

    private static boolean isSimpleNumber(LootTableModel.NumberProvider provider) {
        return provider != null
                && ("number".equals(provider.getType()) || "constant".equals(provider.getType()));
    }

    private static LootTableModel.NumberProvider nonNegative(LootTableModel.NumberProvider provider,
            String blockType, String label, ValidationReport report) {
        if (isSimpleNumber(provider) && provider.getValue() < 0) {
            report.addWarning(blockType, label + " cannot be negative; the value was clamped to 0.");
            return "constant".equals(provider.getType())
                    ? LootTableModel.NumberProvider.constant(0, provider.isFloating())
                    : LootTableModel.NumberProvider.number(0, provider.isFloating());
        }
        return provider;
    }

    private static String fieldValue(Element block, String name) {
        for (Element field : directChildren(block, "field")) {
            if (name.equals(field.getAttribute("name"))) {
                return field.getTextContent() == null ? "" : field.getTextContent().trim();
            }
        }
        return "";
    }

    private static String parseBlockValue(Element block, String inputName, String providedBlock,
            ItemNameResolver resolver, ValidationReport report) {
        Element valueBlock = valueBlock(block, inputName);
        if (valueBlock == null) {
            if (!providedBlock.isBlank()) {
                return providedBlock;
            }
            report.addError(block.getAttribute("type"), "A block value is required for this condition.");
            return "";
        }

        String valueType = valueBlock.getAttribute("type");
        String rawBlock;
        switch (valueType) {
            case "loottable_provided_blockstate", "blockstate_from_deps" -> rawBlock = providedBlock;
            case "loottable_block_selector", "blockstate_selector" -> rawBlock = fieldValue(valueBlock, "block");
            default -> {
                report.addWarning(valueType, "Block value " + valueType + " is not supported and was skipped.");
                return "";
            }
        }

        if (rawBlock.isBlank()) {
            if ("loottable_provided_blockstate".equals(valueType) || "blockstate_from_deps".equals(valueType)) {
                report.addError(valueType,
                        "Provided blockstate requires a target block in the loot table settings.");
            } else {
                report.addWarning(valueType, "Block selector has no selected block and was skipped.");
            }
            return "";
        }

        String resolved = resolver.resolve(rawBlock);
        if (resolved.isBlank() || resolved.startsWith("#")) {
            report.addWarning(valueType,
                    "Block value '" + rawBlock + "' is not a concrete block and was skipped.");
            return "";
        }
        return resolved;
    }

    private static String parseEntityValue(Element block, String inputName, LootTableType type,
            ValidationReport report) {
        Element valueBlock = valueBlock(block, inputName);
        if (valueBlock == null) {
            return "this";
        }

        String valueType = valueBlock.getAttribute("type");
        return switch (valueType) {
            case "loottable_entity_dependency", "entity_from_deps" -> {
                if (type == LootTableType.BLOCK) {
                    report.addWarning(valueType,
                            "Block loot tables only provide the target entity when a player breaks the block; "
                                    + "this condition may not match for non-player drops.");
                }
                yield "this";
            }
            case "loottable_source_entity_dependency", "source_entity_from_deps" -> {
                yield type == LootTableType.BLOCK ? "this" : "attacker";
            }
            case "loottable_immediate_source_entity_dependency", "immediate_source_entity_from_deps" -> {
                yield type == LootTableType.BLOCK ? "this" : "direct_attacker";
            }
            case "loottable_source_player_dependency", "source_player_from_deps" -> {
                yield type == LootTableType.BLOCK ? "this" : "attacking_player";
            }
            default -> {
                report.addWarning(valueType, "Entity value " + valueType + " is not supported and was skipped.");
                yield "";
            }
        };
    }

    private static Element valueBlock(Element block, String name) {
        for (Element value : directChildren(block, "value")) {
            if (name.equals(value.getAttribute("name"))) {
                Element connected = directChild(value, "block");
                return connected != null ? connected : directChild(value, "shadow");
            }
        }
        return null;
    }

    private static Element statementFirstBlock(Element block, String name) {
        for (Element statement : directChildren(block, "statement")) {
            if (name.equals(statement.getAttribute("name"))) {
                return directChild(statement, "block");
            }
        }
        return null;
    }

    private static Element nextBlock(Element block) {
        Element next = directChild(block, "next");
        return next == null ? null : directChild(next, "block");
    }

    private static Element directChild(Element parent, String tagName) {
        for (Element child : directChildren(parent, tagName)) {
            return child;
        }
        return null;
    }

    private static List<Element> directChildren(Element parent, String tagName) {
        List<Element> result = new ArrayList<>();
        NodeList children = parent.getChildNodes();
        for (int index = 0; index < children.getLength(); index++) {
            Node node = children.item(index);
            if (node.getNodeType() == Node.ELEMENT_NODE && tagName.equals(node.getNodeName())) {
                result.add((Element) node);
            }
        }
        return result;
    }

    private static double readNumber(Element block, String field, double fallback, String label,
            ValidationReport report) {
        String text = fieldValue(block, field);
        if (text.isBlank()) {
            return fallback;
        }
        try {
            double value = Double.parseDouble(text);
            if (!Double.isFinite(value)) {
                throw new NumberFormatException("not finite");
            }
            return value;
        } catch (NumberFormatException exception) {
            report.addWarning(block.getAttribute("type"),
                    label + " value '" + text + "' is not a number; the default value was used.");
            return fallback;
        }
    }

    private static double readWholeNumber(Element block, String field, double fallback, double minimum,
            String label, ValidationReport report) {
        double value = readNumber(block, field, fallback, label, report);
        double rounded = Math.rint(value);
        if (Math.abs(value - rounded) > 1.0e-9) {
            report.addWarning(block.getAttribute("type"),
                    label + " value '" + value + "' is not a whole number and was rounded.");
        }
        if (rounded < minimum) {
            report.addWarning(block.getAttribute("type"),
                    label + " cannot be lower than " + formatNumber(minimum) + "; the minimum was used.");
            rounded = minimum;
        }
        return rounded;
    }

    private static boolean readBoolean(Element block, String field, boolean fallback, String label,
            ValidationReport report) {
        String text = fieldValue(block, field);
        if (text.isBlank()) {
            return fallback;
        }
        if ("true".equalsIgnoreCase(text) || "1".equals(text)) {
            return true;
        }
        if ("false".equalsIgnoreCase(text) || "0".equals(text)) {
            return false;
        }
        report.addWarning(block.getAttribute("type"),
                label + " value '" + text + "' is not a boolean; the default value was used.");
        return fallback;
    }

    private static String formatNumber(double value) {
        if (value == Math.rint(value)) {
            return Long.toString((long) value);
        }
        return Double.toString(value);
    }
}
