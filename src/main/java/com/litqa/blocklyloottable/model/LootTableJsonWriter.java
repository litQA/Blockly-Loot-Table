package com.litqa.blocklyloottable.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Writes the standard Minecraft loot table JSON representation of a parsed
 * Blockly document.
 */
public final class LootTableJsonWriter {

    private LootTableJsonWriter() {
    }

    public static String write(LootTableModel model, String namespace, String path, boolean includeRandomSequence) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("type", model.getType().getJsonType());

        List<Object> pools = new ArrayList<>();
        for (LootTableModel.Pool pool : model.getPools()) {
            pools.add(poolToJson(pool));
        }
        root.put("pools", pools);

        if (includeRandomSequence) {
            String randomSequence = (namespace == null ? "" : namespace) + ":" + (path == null ? "" : path);
            root.put("random_sequence", randomSequence);
        }
        return JsonText.write(root);
    }

    private static Map<String, Object> poolToJson(LootTableModel.Pool pool) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("rolls", pool.getRolls());
        json.put("bonus_rolls", pool.getBonusRolls());

        if (!pool.getConditions().isEmpty()) {
            List<Object> conditions = new ArrayList<>();
            for (LootTableModel.Condition condition : pool.getConditions()) {
                Map<String, Object> conditionJson = conditionToJson(condition);
                if (conditionJson != null) {
                    conditions.add(conditionJson);
                }
            }
            if (!conditions.isEmpty()) {
                json.put("conditions", conditions);
            }
        }

        List<Object> entries = new ArrayList<>();
        for (LootTableModel.Entry entry : pool.getEntries()) {
            Map<String, Object> entryJson = entryToJson(entry);
            if (entryJson != null) {
                entries.add(entryJson);
            }
        }
        json.put("entries", entries);
        return json;
    }

    private static Map<String, Object> entryToJson(LootTableModel.Entry entry) {
        String item = entry.getItem();
        if (item == null || item.isBlank() || item.startsWith("#") || "minecraft:air".equals(item)) {
            return null;
        }

        Map<String, Object> json = new LinkedHashMap<>();
        json.put("type", "minecraft:item");
        json.put("name", item);
        json.put("weight", entry.getWeight());

        Map<String, Object> setCount = new LinkedHashMap<>();
        setCount.put("function", "minecraft:set_count");
        setCount.put("count", numberProviderToJson(entry.getCount()));

        List<Object> functions = new ArrayList<>();
        functions.add(setCount);
        for (LootTableModel.LootFunction function : entry.getFunctions()) {
            Map<String, Object> functionJson = functionToJson(function);
            if (functionJson != null) {
                functions.add(functionJson);
            }
        }
        json.put("functions", functions);

        if (!entry.getConditions().isEmpty()) {
            List<Object> conditions = new ArrayList<>();
            for (LootTableModel.Condition condition : entry.getConditions()) {
                Map<String, Object> conditionJson = conditionToJson(condition);
                if (conditionJson != null) {
                    conditions.add(conditionJson);
                }
            }
            if (!conditions.isEmpty()) {
                json.put("conditions", conditions);
            }
        }
        return json;
    }

    private static Object numberProviderToJson(LootTableModel.NumberProvider provider) {
        if (provider == null) {
            return 1;
        }
        return switch (provider.getType()) {
            case "number" -> numberValue(provider.getValue(), provider.isFloating());
            case "uniform" -> {
                Map<String, Object> json = new LinkedHashMap<>();
                json.put("type", "minecraft:uniform");
                json.put("min", numberProviderToJson(provider.getMin()));
                json.put("max", numberProviderToJson(provider.getMax()));
                yield json;
            }
            case "binomial" -> {
                Map<String, Object> json = new LinkedHashMap<>();
                json.put("type", "minecraft:binomial");
                json.put("n", numberProviderToJson(provider.getN()));
                json.put("p", numberProviderToJson(provider.getP()));
                yield json;
            }
            default -> {
                Map<String, Object> json = new LinkedHashMap<>();
                json.put("type", "minecraft:constant");
                json.put("value", numberValue(provider.getValue(), provider.isFloating()));
                yield json;
            }
        };
    }

    private static Object numberValue(double value, boolean floating) {
        return floating ? JsonText.floating(value) : compactNumber(value);
    }

    private static Map<String, Object> functionToJson(LootTableModel.LootFunction function) {
        if (function == null) {
            return null;
        }
        switch (function.getBlockType()) {
            case "loottable_function_set_count_uniform" -> {
                Map<String, Object> json = new LinkedHashMap<>();
                json.put("function", "minecraft:set_count");
                json.put("count", LootTableJsonWriter.numberProviderToJson(
                        LootTableModel.NumberProvider.uniform(
                                provider(function.getProperties().get("min"), 1),
                                provider(function.getProperties().get("max"), 1))));
                return json;
            }
            case "loottable_function_set_count_add" -> {
                Map<String, Object> json = new LinkedHashMap<>();
                json.put("function", "minecraft:set_count");
                json.put("count", LootTableJsonWriter.numberProviderToJson(
                        provider(function.getProperties().get("count"), 1)));
                json.put("add", true);
                return json;
            }
            default -> {
                return null;
            }
        }
    }

    private static Map<String, Object> conditionToJson(LootTableModel.Condition condition) {
        if (condition == null) {
            return null;
        }
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("condition", condition.getJsonType());

        switch (condition.getBlockType()) {
            case "loottable_random_chance" -> json.put("chance", number(condition.getProperties().get("chance"), 0.5));
            case "loottable_weather_check" -> {
                json.put("raining", bool(condition.getProperties().get("raining"), false));
                json.put("thundering", bool(condition.getProperties().get("thundering"), false));
            }
            case "loottable_block_state_property" -> {
                String block = string(condition.getProperties().get("block"));
                if (block.isBlank() || block.startsWith("#")) {
                    return null;
                }
                json.put("block", block);
                String property = string(condition.getProperties().get("property"));
                if (!property.isBlank()) {
                    Map<String, Object> properties = new LinkedHashMap<>();
                    properties.put(property, string(condition.getProperties().get("value")));
                    json.put("properties", properties);
                }
            }
            case "loottable_block_state_boolean", "loottable_block_state_integer",
                    "loottable_block_state_enum" -> {
                String block = string(condition.getProperties().get("block"));
                String property = string(condition.getProperties().get("property"));
                if (block.isBlank() || block.startsWith("#") || property.isBlank()) {
                    return null;
                }
                json.put("block", block);
                Map<String, Object> properties = new LinkedHashMap<>();
                properties.put(property, string(condition.getProperties().get("value")));
                json.put("properties", properties);
            }
            case "loottable_entity_has_potion" -> {
                String entity = string(condition.getProperties().get("entity"));
                String effect = string(condition.getProperties().get("effect"));
                if (entity.isBlank() || effect.isBlank()) {
                    return null;
                }
                json.put("entity", entity);
                Map<String, Object> effects = new LinkedHashMap<>();
                effects.put(effect, new LinkedHashMap<>());
                Map<String, Object> predicate = new LinkedHashMap<>();
                predicate.put("effects", effects);
                json.put("predicate", predicate);
            }
            case "loottable_block_nbt_logic", "loottable_block_nbt_number", "loottable_block_nbt_text" -> {
                String block = string(condition.getProperties().get("block"));
                String nbt = string(condition.getProperties().get("nbt"));
                if (block.isBlank() || nbt.isBlank()) {
                    return null;
                }
                Map<String, Object> blockPredicate = new LinkedHashMap<>();
                blockPredicate.put("blocks", List.of(block));
                blockPredicate.put("nbt", nbt);
                Map<String, Object> predicate = new LinkedHashMap<>();
                predicate.put("block", blockPredicate);
                json.put("condition", "minecraft:location_check");
                json.put("predicate", predicate);
            }
            case "loottable_entity_nbt_logic", "loottable_entity_nbt_number", "loottable_entity_nbt_text" -> {
                String entity = string(condition.getProperties().get("entity"));
                String nbt = string(condition.getProperties().get("nbt"));
                if (entity.isBlank() || nbt.isBlank()) {
                    return null;
                }
                json.put("entity", entity);
                Map<String, Object> predicate = new LinkedHashMap<>();
                predicate.put("nbt", nbt);
                json.put("predicate", predicate);
            }
            case "loottable_item_has_enchantment" -> {
                String enchantment = string(condition.getProperties().get("enchantment"));
                if (enchantment.isBlank()) {
                    return null;
                }
                int level = (int) Math.max(1, Math.min(255,
                        number(condition.getProperties().get("level"), 1)));
                Map<String, Object> enchantmentPredicate = new LinkedHashMap<>();
                enchantmentPredicate.put("enchantments", List.of(enchantment));
                enchantmentPredicate.put("levels", level);
                List<Object> enchantmentPredicates = new ArrayList<>();
                enchantmentPredicates.add(enchantmentPredicate);

                Map<String, Object> predicates = new LinkedHashMap<>();
                predicates.put("minecraft:enchantments", enchantmentPredicates);
                Map<String, Object> itemPredicate = new LinkedHashMap<>();
                itemPredicate.put("predicates", predicates);

                if ("entity".equals(string(condition.getProperties().get("mode")))) {
                    String entity = string(condition.getProperties().get("entity"));
                    if (entity.isBlank()) {
                        return null;
                    }
                    Map<String, Object> mainhand = new LinkedHashMap<>();
                    mainhand.put("predicates", predicates);
                    Map<String, Object> equipment = new LinkedHashMap<>();
                    equipment.put("mainhand", mainhand);
                    Map<String, Object> predicate = new LinkedHashMap<>();
                    predicate.put("equipment", equipment);
                    json.put("condition", "minecraft:entity_properties");
                    json.put("entity", entity);
                    json.put("predicate", predicate);
                } else {
                    json.put("predicate", itemPredicate);
                }
            }
            case "loottable_inverted" -> {
                Map<String, Object> term = conditionToJson(condition.getTerm());
                if (term == null) {
                    return null;
                }
                json.put("term", term);
            }
            default -> {
                // Conditions without parameters need only their type.
            }
        }
        return json;
    }

    private static Object compactNumber(double value) {
        if (value == Math.rint(value) && value >= Long.MIN_VALUE && value <= Long.MAX_VALUE) {
            return (long) value;
        }
        return value;
    }

    private static double number(Object value, double fallback) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return fallback;
    }

    private static LootTableModel.NumberProvider provider(Object value, double fallback) {
        if (value instanceof LootTableModel.NumberProvider provider) {
            return provider;
        }
        if (value instanceof Number number) {
            return LootTableModel.NumberProvider.number(number.doubleValue(), false);
        }
        return LootTableModel.NumberProvider.number(fallback, false);
    }

    private static boolean bool(Object value, boolean fallback) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        return fallback;
    }

    private static String string(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
