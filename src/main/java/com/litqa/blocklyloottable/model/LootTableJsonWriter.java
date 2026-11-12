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

        Map<String, Object> count = new LinkedHashMap<>();
        count.put("min", entry.getMinCount());
        count.put("max", entry.getMaxCount());

        Map<String, Object> setCount = new LinkedHashMap<>();
        setCount.put("function", "minecraft:set_count");
        setCount.put("count", count);

        List<Object> functions = new ArrayList<>();
        functions.add(setCount);
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

    private static double number(Object value, double fallback) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return fallback;
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
