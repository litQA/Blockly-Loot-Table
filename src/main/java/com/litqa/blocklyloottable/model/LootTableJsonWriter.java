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
            List<Object> conditions = conditionsToJson(pool.getConditions());
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
            List<Object> conditions = conditionsToJson(entry.getConditions());
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
            case "loottable_item_with_enchantment" -> {
                Object value = function.getProperties().get("enchantments");
                if (!(value instanceof List<?> entries) || entries.isEmpty()) {
                    return null;
                }
                Map<String, Object> enchantments = new LinkedHashMap<>();
                for (Object entry : entries) {
                    if (entry instanceof LootTableModel.EnchantmentLevel enchantment) {
                        enchantments.put(enchantment.getEnchantment(),
                                numberProviderToJson(enchantment.getLevel()));
                    }
                }
                if (enchantments.isEmpty()) {
                    return null;
                }
                Map<String, Object> json = new LinkedHashMap<>();
                json.put("function", "minecraft:set_enchantments");
                json.put("enchantments", enchantments);
                json.put("add", false);
                return json;
            }
            case "loottable_item_enchant_random" -> {
                List<String> options = stringList(function.getProperties().get("options"));
                if (options.isEmpty()) {
                    return null;
                }
                Map<String, Object> json = new LinkedHashMap<>();
                json.put("function", "minecraft:enchant_randomly");
                json.put("options", options);
                return json;
            }
            case "loottable_item_enchant_random_any" -> {
                Map<String, Object> json = new LinkedHashMap<>();
                json.put("function", "minecraft:enchant_with_levels");
                json.put("levels", numberProviderToJson(LootTableModel.NumberProvider.uniform(
                        provider(function.getProperties().get("min"), 1),
                        provider(function.getProperties().get("max"), 3))));
                if (!bool(function.getProperties().get("treasure"), true)) {
                    json.put("options", "#minecraft:on_random_loot");
                }
                return json;
            }
            case "loottable_item_enchant_levels" -> {
                List<String> options = stringList(function.getProperties().get("options"));
                if (options.isEmpty()) {
                    return null;
                }
                Map<String, Object> json = new LinkedHashMap<>();
                json.put("function", "minecraft:enchant_with_levels");
                json.put("levels", numberProviderToJson(
                        provider(function.getProperties().get("levels"), 1)));
                json.put("options", options);
                return json;
            }
            case "loottable_item_enchant_levels_any" -> {
                Map<String, Object> json = new LinkedHashMap<>();
                json.put("function", "minecraft:enchant_with_levels");
                json.put("levels", numberProviderToJson(
                        provider(function.getProperties().get("levels"), 1)));
                if (!bool(function.getProperties().get("treasure"), true)) {
                    json.put("options", "#minecraft:on_random_loot");
                }
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
            case "loottable_function_apply_bonus_uniform" -> {
                Map<String, Object> parameters = new LinkedHashMap<>();
                parameters.put("bonusMultiplier", intProperty(function.getProperties().get("bonus"), 1));
                return applyBonusJson(function, "minecraft:uniform_bonus_count", parameters);
            }
            case "loottable_function_apply_bonus_binomial" -> {
                Map<String, Object> parameters = new LinkedHashMap<>();
                parameters.put("extra", intProperty(function.getProperties().get("extra"), 1));
                parameters.put("probability", JsonText.floating(
                        doubleProperty(function.getProperties().get("probability"), 0.5)));
                return applyBonusJson(function, "minecraft:binomial_with_bonus_count", parameters);
            }
            case "loottable_function_apply_bonus_ore" -> {
                return applyBonusJson(function, "minecraft:ore_drops", null);
            }
            case "loottable_function_enchanted_count_increase" -> {
                Map<String, Object> json = new LinkedHashMap<>();
                json.put("function", "minecraft:enchanted_count_increase");
                json.put("enchantment", string(function.getProperties().get("enchantment")));
                json.put("count", numberProviderToJson(provider(function.getProperties().get("count"), 0)));
                return json;
            }
            case "loottable_function_limit_count" -> {
                Map<String, Object> json = new LinkedHashMap<>();
                json.put("function", "minecraft:limit_count");
                json.put("limit", numberProviderToJson(provider(function.getProperties().get("limit"), 9)));
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
            case "loottable_item_has_enchantment_min", "loottable_item_has_enchantment_range" -> {
                String enchantment = string(condition.getProperties().get("enchantment"));
                if (enchantment.isBlank()) {
                    return null;
                }
                int minimum = (int) Math.max(1, Math.min(255,
                        number(condition.getProperties().get("minLevel"),
                                number(condition.getProperties().get("level"), 1))));
                Map<String, Object> levels = new LinkedHashMap<>();
                levels.put("min", minimum);
                if (condition.getProperties().containsKey("maxLevel")) {
                    int maximum = (int) Math.max(minimum, Math.min(255,
                            number(condition.getProperties().get("maxLevel"), minimum)));
                    levels.put("max", maximum);
                }
                Map<String, Object> enchantmentPredicate = new LinkedHashMap<>();
                enchantmentPredicate.put("enchantments", List.of(enchantment));
                enchantmentPredicate.put("levels", levels);
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
            case "loottable_time_check", "loottable_time_day", "loottable_time_periodic" -> {
                LootTableModel.NumberProvider minimum = provider(
                        condition.getProperties().get("min"), 0);
                LootTableModel.NumberProvider maximum = provider(
                        condition.getProperties().get("max"), 12000);
                Map<String, Object> value = new LinkedHashMap<>();
                value.put("min", numberProviderToJson(minimum));
                value.put("max", numberProviderToJson(maximum));
                json.put("value", value);
                if (condition.getProperties().get("period") instanceof Number period) {
                    json.put("period", period.longValue());
                }
            }
            case "loottable_world_dimension" -> {
                String dimension = string(condition.getProperties().get("dimension"));
                if (dimension.isBlank()) {
                    return null;
                }
                json.put("condition", "minecraft:location_check");
                json.put("predicate", locationPredicate("dimension", dimension));
            }
            case "loottable_world_biome" -> {
                String biome = string(condition.getProperties().get("biome"));
                if (biome.isBlank()) {
                    return null;
                }
                json.put("condition", "minecraft:location_check");
                json.put("predicate", locationPredicate("biomes", biome));
            }
            case "loottable_world_biome_tag" -> {
                String tag = string(condition.getProperties().get("tag"));
                if (tag.isBlank()) {
                    return null;
                }
                json.put("condition", "minecraft:location_check");
                json.put("predicate", locationPredicate("biomes", tagged(tag)));
            }
            case "loottable_world_structure" -> {
                String structure = string(condition.getProperties().get("structure"));
                if (structure.isBlank()) {
                    return null;
                }
                json.put("condition", "minecraft:location_check");
                json.put("predicate", locationPredicate("structures", structure));
            }
            case "loottable_world_structure_tag" -> {
                String tag = string(condition.getProperties().get("tag"));
                if (tag.isBlank()) {
                    return null;
                }
                json.put("condition", "minecraft:location_check");
                json.put("predicate", locationPredicate("structures", tagged(tag)));
            }
            case "loottable_world_smokey" -> {
                json.put("condition", "minecraft:location_check");
                json.put("predicate", locationPredicate("smokey", true));
            }
            case "loottable_world_light", "loottable_world_light_range" -> {
                Map<String, Object> light = new LinkedHashMap<>();
                int minimum = (int) Math.max(0, Math.min(15,
                        number(condition.getProperties().get("min"), 0)));
                int maximum = (int) Math.max(minimum, Math.min(15,
                        number(condition.getProperties().get("max"), minimum)));
                if ("loottable_world_light".equals(condition.getBlockType())) {
                    light.put("light", minimum);
                } else {
                    Map<String, Object> range = new LinkedHashMap<>();
                    range.put("min", minimum);
                    range.put("max", maximum);
                    light.put("light", range);
                }
                json.put("condition", "minecraft:location_check");
                json.put("predicate", locationPredicate("light", light));
            }
            case "loottable_world_position_x", "loottable_world_position_y",
                    "loottable_world_position_z", "loottable_world_position_x_range",
                    "loottable_world_position_y_range", "loottable_world_position_z_range" -> {
                String blockType = condition.getBlockType();
                String axis = blockType.contains("_x") ? "x" : blockType.contains("_y") ? "y" : "z";
                Map<String, Object> position = new LinkedHashMap<>();
                if (blockType.endsWith("_range")) {
                    Map<String, Object> range = new LinkedHashMap<>();
                    range.put("min", number(condition.getProperties().get("min"), 0));
                    range.put("max", number(condition.getProperties().get("max"), 0));
                    position.put(axis, range);
                } else {
                    position.put(axis, number(condition.getProperties().get("value"), 0));
                }
                json.put("condition", "minecraft:location_check");
                json.put("predicate", locationPredicate("position", position));
            }
            case "loottable_world_block_tag" -> {
                String tag = string(condition.getProperties().get("tag"));
                if (tag.isBlank()) {
                    return null;
                }
                Map<String, Object> blocks = new LinkedHashMap<>();
                blocks.put("blocks", tagged(tag));
                json.put("condition", "minecraft:location_check");
                json.put("predicate", locationPredicate("block", blocks));
            }
            case "loottable_logic_and", "loottable_logic_or" -> {
                List<Object> terms = conditionTermsToJson(condition.getTerms());
                if (terms.isEmpty()) {
                    return null;
                }
                json.put("terms", terms);
            }
            case "loottable_logic_not" -> {
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

    private static Map<String, Object> locationPredicate(String key, Object value) {
        Map<String, Object> predicate = new LinkedHashMap<>();
        predicate.put(key, value);
        return predicate;
    }

    private static String tagged(String tag) {
        String value = tag == null ? "" : tag.trim();
        return value.startsWith("#") ? value : "#" + value;
    }

    private static List<Object> conditionsToJson(List<LootTableModel.Condition> input) {
        List<Object> conditions = new ArrayList<>();
        for (LootTableModel.Condition condition : input) {
            Map<String, Object> conditionJson = conditionToJson(condition);
            if (conditionJson == null) {
                continue;
            }
            if ("minecraft:location_check".equals(conditionJson.get("condition"))
                    && conditionJson.get("predicate") instanceof Map<?, ?> predicate) {
                Map<String, Object> combined = findLocationPredicate(conditions);
                if (combined != null && deepMerge(combined, predicate)) {
                    continue;
                }
            }
            conditions.add(conditionJson);
        }
        return conditions;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> findLocationPredicate(List<Object> conditions) {
        for (Object condition : conditions) {
            if (condition instanceof Map<?, ?> map
                    && "minecraft:location_check".equals(map.get("condition"))
                    && map.get("predicate") instanceof Map<?, ?> predicate) {
                return (Map<String, Object>) predicate;
            }
        }
        return null;
    }

    private static boolean deepMerge(Map<String, Object> target, Map<?, ?> source) {
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            String key = String.valueOf(entry.getKey());
            Object incoming = entry.getValue();
            Object existing = target.get(key);
            if (existing == null) {
                target.put(key, incoming);
            } else if (existing instanceof Map<?, ?> existingMap
                    && incoming instanceof Map<?, ?> incomingMap) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nested = (Map<String, Object>) existingMap;
                if (!deepMerge(nested, incomingMap)) {
                    return false;
                }
            } else if (!existing.equals(incoming)) {
                return false;
            }
        }
        return true;
    }

    private static List<Object> conditionTermsToJson(List<LootTableModel.Condition> conditions) {
        List<Object> terms = new ArrayList<>();
        if (conditions != null) {
            for (LootTableModel.Condition condition : conditions) {
                Map<String, Object> json = conditionToJson(condition);
                if (json != null) {
                    terms.add(json);
                }
            }
        }
        return terms;
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

    private static Map<String, Object> applyBonusJson(LootTableModel.LootFunction function, String formula,
            Map<String, Object> parameters) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("function", "minecraft:apply_bonus");
        json.put("enchantment", string(function.getProperties().get("enchantment")));
        json.put("formula", formula);
        if (parameters != null) {
            json.put("parameters", parameters);
        }
        return json;
    }

    private static int intProperty(Object value, int fallback) {
        LootTableModel.NumberProvider provider = provider(value, fallback);
        return (int) provider.getValue();
    }

    private static double doubleProperty(Object value, double fallback) {
        return provider(value, fallback).getValue();
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

    private static List<String> stringList(Object value) {
        List<String> result = new ArrayList<>();
        if (value instanceof List<?> values) {
            for (Object item : values) {
                String text = string(item);
                if (!text.isBlank()) {
                    result.add(text);
                }
            }
        }
        return result;
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
