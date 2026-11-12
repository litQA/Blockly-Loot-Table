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

/**
 * Converts a Blockly XML document into a loot table model and validation report.
 *
 * <p>The parser deliberately treats unknown or malformed child blocks as
 * skipped entries with warnings. Context violations are errors because the
 * specification requires generation to stop for illegal condition scopes.</p>
 */
public final class BlocklyXmlParser {

    private BlocklyXmlParser() {
    }

    public record ParseResult(LootTableModel model, ValidationReport report) {
    }

    public static ParseResult parse(String xml, LootTableType type, ItemNameResolver resolver) {
        LootTableType effectiveType = type == null ? LootTableType.BLOCK : type;
        ItemNameResolver effectiveResolver = resolver == null ? ItemNameResolver.IDENTITY : resolver;
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
        for (Element block : topBlocks) {
            if ("loottable_start".equals(block.getAttribute("type"))) {
                startBlock = block;
                break;
            }
        }

        int poolCount;
        if (startBlock != null) {
            poolCount = parsePoolChain(nextBlock(startBlock), effectiveType, effectiveResolver, model, report);
        } else {
            report.addWarning(null, "No loot table start block was found; top-level pools will still be read.");
            poolCount = 0;
            for (Element block : topBlocks) {
                if ("loottable_pool".equals(block.getAttribute("type"))) {
                    poolCount++;
                    parsePool(block, poolCount, effectiveType, effectiveResolver, model, report);
                }
            }
        }

        if (poolCount == 0) {
            report.addWarning(null, "No loot table pools are defined; an empty loot table will be generated.");
        }
        return new ParseResult(model, report);
    }

    private static int parsePoolChain(Element first, LootTableType type, ItemNameResolver resolver,
            LootTableModel model, ValidationReport report) {
        int poolCount = 0;
        for (Element block = first; block != null; block = nextBlock(block)) {
            String blockType = block.getAttribute("type");
            if ("loottable_pool".equals(blockType)) {
                poolCount++;
                parsePool(block, poolCount, type, resolver, model, report);
            } else if (!"loottable_start".equals(blockType)) {
                report.addWarning(blockType, "Block " + blockType + " is not a pool and was skipped.");
            }
        }
        return poolCount;
    }

    private static void parsePool(Element block, int poolIndex, LootTableType type, ItemNameResolver resolver,
            LootTableModel model, ValidationReport report) {
        LootTableModel.Pool pool = new LootTableModel.Pool();
        pool.setRolls(readWholeNumber(block, "rolls", 1, 0, "Pool " + poolIndex + " rolls", report));
        pool.setBonusRolls(readWholeNumber(block, "bonus_rolls", 0, 0,
                "Pool " + poolIndex + " bonus rolls", report));

        parseConditionChain(statementFirstBlock(block, "conditions"), type, resolver, pool.getConditions(), report);
        int entryCount = parseEntryChain(statementFirstBlock(block, "entries"), poolIndex, type, resolver,
                pool.getEntries(), report);
        if (entryCount == 0) {
            report.addWarning("loottable_pool",
                    "Pool " + poolIndex + " has no valid entries; it will be generated with an empty entry list.");
        }
        model.addPool(pool);
    }

    private static int parseEntryChain(Element first, int poolIndex, LootTableType type, ItemNameResolver resolver,
            List<LootTableModel.Entry> entries, ValidationReport report) {
        int entryCount = 0;
        for (Element block = first; block != null; block = nextBlock(block)) {
            String blockType = block.getAttribute("type");
            if (!"loottable_entry_item".equals(blockType)) {
                report.addWarning(blockType,
                        "Block " + blockType + " is not an item entry and was skipped in pool " + poolIndex + ".");
                continue;
            }
            LootTableModel.Entry entry = parseEntry(block, type, resolver, report);
            if (entry != null) {
                entries.add(entry);
                entryCount++;
            }
        }
        return entryCount;
    }

    private static LootTableModel.Entry parseEntry(Element block, LootTableType type, ItemNameResolver resolver,
            ValidationReport report) {
        String rawItem = fieldValue(block, "item");
        if (rawItem.isBlank()) {
            report.addWarning("loottable_entry_item", "An item entry has no selected item and was skipped.");
            return null;
        }

        String item = resolver.resolve(rawItem);
        if (item.isBlank()) {
            report.addWarning("loottable_entry_item",
                    "Item value '" + rawItem + "' could not be resolved to a registry name and was skipped.");
            return null;
        }
        if (item.startsWith("#")) {
            report.addWarning("loottable_entry_item",
                    "Item tags are not supported by this version of the plugin; entry '" + item + "' was skipped.");
            return null;
        }
        if ("minecraft:air".equals(item)) {
            report.addWarning("loottable_entry_item", "Air cannot be used as a loot table item and was skipped.");
            return null;
        }

        LootTableModel.Entry entry = new LootTableModel.Entry();
        entry.setItem(item);

        double minCount = readWholeNumber(block, "min_count", 1, 1, "Entry minimum count", report);
        double maxCount = readWholeNumber(block, "max_count", minCount, 1, "Entry maximum count", report);
        if (maxCount < minCount) {
            report.addWarning("loottable_entry_item",
                    "Entry maximum count was smaller than the minimum; the maximum was raised to the minimum.");
            maxCount = minCount;
        }
        entry.setMinCount(minCount);
        entry.setMaxCount(maxCount);
        entry.setWeight(readWholeNumber(block, "weight", 1, 1, "Entry weight", report));

        parseConditionChain(statementFirstBlock(block, "conditions"), type, resolver, entry.getConditions(), report);
        return entry;
    }

    private static void parseConditionChain(Element first, LootTableType type, ItemNameResolver resolver,
            List<LootTableModel.Condition> conditions, ValidationReport report) {
        for (Element block = first; block != null; block = nextBlock(block)) {
            LootTableModel.Condition condition = parseCondition(block, type, resolver, report);
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
            ItemNameResolver resolver, ValidationReport report) {
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
            case "loottable_block_state_property" -> {
                String rawBlock = fieldValue(block, "block");
                if (rawBlock.isBlank()) {
                    report.addWarning(blockType, "Block state property has no block and was skipped.");
                    return null;
                }
                String resolvedBlock = resolver.resolve(rawBlock);
                if (resolvedBlock.isBlank() || resolvedBlock.startsWith("#")) {
                    report.addWarning(blockType,
                            "Block state property block '" + rawBlock + "' is not a concrete block and was skipped.");
                    return null;
                }
                condition.getProperties().put("block", resolvedBlock);

                String property = fieldValue(block, "property");
                String value = fieldValue(block, "value");
                if (property.isBlank()) {
                    report.addWarning(blockType,
                            "Block state property has no property name; it will match any block state.");
                } else {
                    condition.getProperties().put("property", property);
                    condition.getProperties().put("value", value);
                }
            }
            case "loottable_inverted" -> {
                Element termBlock = statementFirstBlock(block, "term");
                if (termBlock == null) {
                    report.addWarning(blockType, "Inverted condition has no nested condition and was skipped.");
                    return null;
                }
                LootTableModel.Condition term = parseCondition(termBlock, type, resolver, report);
                if (term == null) {
                    report.addWarning(blockType, "Inverted condition has no valid nested condition and was skipped.");
                    return null;
                }
                condition.setTerm(term);
            }
            default -> {
                // Killed by player and survives explosion carry no extra fields.
            }
        }
        return condition;
    }

    private static String fieldValue(Element block, String name) {
        for (Element field : directChildren(block, "field")) {
            if (name.equals(field.getAttribute("name"))) {
                return field.getTextContent() == null ? "" : field.getTextContent().trim();
            }
        }
        return "";
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
