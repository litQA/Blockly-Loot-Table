package com.litqa.blocklyloottable.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Dependency free JSON serializer used for generated loot table documents.
 */
public final class JsonText {

    /**
     * A pre-validated JSON fragment. Only use this for values that have already
     * been parsed successfully by the plugin.
     */
    public record RawJson(String json) {
    }

    private JsonText() {
    }

    public static String write(Object value) {
        StringBuilder builder = new StringBuilder();
        writeValue(builder, value, 0);
        builder.append('\n');
        return builder.toString();
    }

    /**
     * Renders a value as compact JSON for components that store JSON as text.
     */
    public static String inline(Object value) {
        StringBuilder builder = new StringBuilder();
        writeCompactValue(builder, value);
        return builder.toString();
    }

    private static void writeValue(StringBuilder builder, Object value, int depth) {
        if (value == null) {
            builder.append("null");
        } else if (value instanceof Map<?, ?> map) {
            writeObject(builder, map, depth);
        } else if (value instanceof List<?> list) {
            writeArray(builder, list, depth);
        } else if (value instanceof RawJson raw) {
            builder.append(raw.json());
        } else if (value instanceof String text) {
            writeString(builder, text);
        } else if (value instanceof Number number) {
            builder.append(formatNumber(number));
        } else if (value instanceof Boolean bool) {
            builder.append(bool);
        } else {
            writeString(builder, String.valueOf(value));
        }
    }

    private static void writeCompactValue(StringBuilder builder, Object value) {
        if (value == null) {
            builder.append("null");
        } else if (value instanceof Map<?, ?> map) {
            builder.append('{');
            int index = 0;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (index++ > 0) {
                    builder.append(',');
                }
                writeString(builder, String.valueOf(entry.getKey()));
                builder.append(':');
                writeCompactValue(builder, entry.getValue());
            }
            builder.append('}');
        } else if (value instanceof List<?> list) {
            builder.append('[');
            for (int index = 0; index < list.size(); index++) {
                if (index > 0) {
                    builder.append(',');
                }
                writeCompactValue(builder, list.get(index));
            }
            builder.append(']');
        } else if (value instanceof RawJson raw) {
            builder.append(raw.json());
        } else if (value instanceof String text) {
            writeString(builder, text);
        } else if (value instanceof Number number) {
            builder.append(formatNumber(number));
        } else if (value instanceof Boolean bool) {
            builder.append(bool);
        } else {
            writeString(builder, String.valueOf(value));
        }
    }

    private static void writeObject(StringBuilder builder, Map<?, ?> map, int depth) {
        if (map.isEmpty()) {
            builder.append("{}");
            return;
        }
        builder.append("{\n");
        int index = 0;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            indent(builder, depth + 1);
            writeString(builder, String.valueOf(entry.getKey()));
            builder.append(": ");
            writeValue(builder, entry.getValue(), depth + 1);
            if (++index < map.size()) {
                builder.append(',');
            }
            builder.append('\n');
        }
        indent(builder, depth);
        builder.append('}');
    }

    private static void writeArray(StringBuilder builder, List<?> list, int depth) {
        if (list.isEmpty()) {
            builder.append("[]");
            return;
        }
        builder.append("[\n");
        for (int index = 0; index < list.size(); index++) {
            indent(builder, depth + 1);
            writeValue(builder, list.get(index), depth + 1);
            if (index + 1 < list.size()) {
                builder.append(',');
            }
            builder.append('\n');
        }
        indent(builder, depth);
        builder.append(']');
    }

    private static void writeString(StringBuilder builder, String text) {
        builder.append('"');
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            switch (character) {
                case '"' -> builder.append("\\\"");
                case '\\' -> builder.append("\\\\");
                case '\b' -> builder.append("\\b");
                case '\f' -> builder.append("\\f");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> {
                    if (character < 0x20) {
                        builder.append(String.format("\\u%04x", (int) character));
                    } else {
                        builder.append(character);
                    }
                }
            }
        }
        builder.append('"');
    }

    /**
     * Returns a number that always renders with a decimal point. This keeps
     * float-valued JSON fields visibly float-shaped even when the value is whole.
     */
    public static Number floating(double value) {
        if (!Double.isFinite(value)) {
            value = 0;
        }
        BigDecimal decimal = BigDecimal.valueOf(value);
        if (decimal.scale() <= 0) {
            decimal = decimal.setScale(1);
        }
        return decimal;
    }

    private static String formatNumber(Number number) {
        if (number instanceof BigDecimal decimal) {
            return decimal.toPlainString();
        }
        if (number instanceof Byte || number instanceof Short || number instanceof Integer || number instanceof Long) {
            return number.toString();
        }
        double value = number.doubleValue();
        if (!Double.isFinite(value)) {
            return "0";
        }
        if (value == Math.rint(value) && Math.abs(value) < 9.0e15) {
            return Long.toString((long) value);
        }
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private static void indent(StringBuilder builder, int depth) {
        builder.append("  ".repeat(Math.max(depth, 0)));
    }
}
