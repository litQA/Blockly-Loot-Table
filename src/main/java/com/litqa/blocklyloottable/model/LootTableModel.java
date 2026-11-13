package com.litqa.blocklyloottable.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parsed loot table document independent of MCreator UI classes.
 */
public final class LootTableModel {

    private final LootTableType type;
    private final List<Pool> pools = new ArrayList<>();

    public LootTableModel(LootTableType type) {
        this.type = type == null ? LootTableType.BLOCK : type;
    }

    public LootTableType getType() {
        return type;
    }

    public List<Pool> getPools() {
        return pools;
    }

    public void addPool(Pool pool) {
        if (pool != null) {
            pools.add(pool);
        }
    }

    public static final class Pool {

        private double rolls = 1;
        private double bonusRolls = 0;
        private final List<Condition> conditions = new ArrayList<>();
        private final List<Entry> entries = new ArrayList<>();

        public double getRolls() {
            return rolls;
        }

        public void setRolls(double rolls) {
            this.rolls = rolls;
        }

        public double getBonusRolls() {
            return bonusRolls;
        }

        public void setBonusRolls(double bonusRolls) {
            this.bonusRolls = bonusRolls;
        }

        public List<Condition> getConditions() {
            return conditions;
        }

        public List<Entry> getEntries() {
            return entries;
        }
    }

    public static final class Entry {

        private String item = "";
        private NumberProvider count = NumberProvider.number(1, false);
        private double weight = 1;
        private final List<Condition> conditions = new ArrayList<>();
        private final List<LootFunction> functions = new ArrayList<>();

        public String getItem() {
            return item;
        }

        public void setItem(String item) {
            this.item = item == null ? "" : item;
        }

        public NumberProvider getCount() {
            return count;
        }

        public void setCount(NumberProvider count) {
            this.count = count == null ? NumberProvider.number(1, false) : count;
        }

        public double getWeight() {
            return weight;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }

        public List<Condition> getConditions() {
            return conditions;
        }

        public List<LootFunction> getFunctions() {
            return functions;
        }
    }

    /**
     * A number provider used by the item count and the set_count functions.
     */
    public static final class NumberProvider {

        private final String type;
        private final double value;
        private final boolean floating;
        private final NumberProvider min;
        private final NumberProvider max;
        private final NumberProvider n;
        private final NumberProvider p;

        private NumberProvider(String type, double value, boolean floating,
                NumberProvider min, NumberProvider max, NumberProvider n, NumberProvider p) {
            this.type = type;
            this.value = value;
            this.floating = floating;
            this.min = min;
            this.max = max;
            this.n = n;
            this.p = p;
        }

        /**
         * A bare JSON number. This is the inline form accepted by vanilla
         * number-provider codecs for fields that are themselves providers.
         */
        public static NumberProvider number(double value, boolean floating) {
            return new NumberProvider("number", value, floating, null, null, null, null);
        }

        /**
         * The explicit {@code minecraft:constant} number provider object.
         */
        public static NumberProvider constant(double value, boolean floating) {
            return new NumberProvider("constant", value, floating, null, null, null, null);
        }

        public static NumberProvider uniform(double min, double max) {
            return uniform(number(min, false), number(max, false));
        }

        public static NumberProvider uniform(NumberProvider min, NumberProvider max) {
            return new NumberProvider("uniform", 0, false,
                    min == null ? number(1, false) : min,
                    max == null ? number(1, false) : max,
                    null, null);
        }

        public static NumberProvider binomial(NumberProvider n, NumberProvider p) {
            return new NumberProvider("binomial", 0, false, null, null,
                    n == null ? number(1, false) : n,
                    p == null ? number(1, true) : p);
        }

        public String getType() {
            return type;
        }

        public double getValue() {
            return value;
        }

        public boolean isFloating() {
            return floating;
        }

        public NumberProvider getMin() {
            return min;
        }

        public NumberProvider getMax() {
            return max;
        }

        public NumberProvider getN() {
            return n;
        }

        public NumberProvider getP() {
            return p;
        }
    }

    /**
     * One loot function attached to an item entry.
     */
    public static final class LootFunction {

        private final String blockType;
        private final Map<String, Object> properties = new LinkedHashMap<>();

        public LootFunction(String blockType) {
            this.blockType = blockType == null ? "" : blockType;
        }

        public String getBlockType() {
            return blockType;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }
    }

    public static final class EnchantmentLevel {

        private final String enchantment;
        private final int minLevel;
        private final Integer maxLevel;

        public EnchantmentLevel(String enchantment, int minLevel, Integer maxLevel) {
            this.enchantment = enchantment == null ? "" : enchantment;
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
        }

        public String getEnchantment() {
            return enchantment;
        }

        public int getMinLevel() {
            return minLevel;
        }

        public Integer getMaxLevel() {
            return maxLevel;
        }
    }

    public static final class Condition {

        private final String blockType;
        private final String jsonType;
        private final Map<String, Object> properties = new LinkedHashMap<>();
        private final List<Condition> terms = new ArrayList<>();
        private Condition term;

        public Condition(String blockType, String jsonType) {
            this.blockType = blockType == null ? "" : blockType;
            this.jsonType = jsonType == null ? "" : jsonType;
        }

        public String getBlockType() {
            return blockType;
        }

        public String getJsonType() {
            return jsonType;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }

        public List<Condition> getTerms() {
            return terms;
        }

        public Condition getTerm() {
            return term;
        }

        public void setTerm(Condition term) {
            this.term = term;
        }
    }
}
