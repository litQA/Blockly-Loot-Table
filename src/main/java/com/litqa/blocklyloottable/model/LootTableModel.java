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
        private NumberProvider count = NumberProvider.constant(1);
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
            this.count = count == null ? NumberProvider.constant(1) : count;
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
        private final double min;
        private final double max;

        private NumberProvider(String type, double value, double min, double max) {
            this.type = type;
            this.value = value;
            this.min = min;
            this.max = max;
        }

        public static NumberProvider constant(double value) {
            return new NumberProvider("constant", value, 0, 0);
        }

        public static NumberProvider uniform(double min, double max) {
            return new NumberProvider("uniform", 0, min, max);
        }

        public String getType() {
            return type;
        }

        public double getValue() {
            return value;
        }

        public double getMin() {
            return min;
        }

        public double getMax() {
            return max;
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

    public static final class Condition {

        private final String blockType;
        private final String jsonType;
        private final Map<String, Object> properties = new LinkedHashMap<>();
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

        public Condition getTerm() {
            return term;
        }

        public void setTerm(Condition term) {
            this.term = term;
        }
    }
}
