package com.litqa.blocklyloottable.model;

/**
 * Resolves Blockly selector values such as {@code Blocks.STONE#0} to Minecraft
 * registry names. The editor supplies an MCreator-backed implementation and the
 * self-check supplies a deterministic test implementation.
 */
@FunctionalInterface
public interface ItemNameResolver {

    ItemNameResolver IDENTITY = value -> value == null ? "" : value.trim();

    /**
     * @param rawValue value stored in the Blockly field
     * @return registry name, tag name starting with {@code #}, or an empty string
     */
    String resolve(String rawValue);
}
