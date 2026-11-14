package com.litqa.blocklyloottable.util;

import net.mcreator.workspace.Workspace;

/**
 * Minecraft-version-dependent loot table details.
 */
public final class VersionCompat {

    /**
     * Feature switches for a concrete Minecraft version. Keeping these in one
     * record avoids sprinkling version comparisons through the JSON writer.
     */
    public record Features(boolean itemComponents, boolean namespacedPotionNbt,
            boolean setEnchantments, boolean scoreProvider, boolean containerType,
            boolean damageTags, boolean structureField, boolean pluralLocationFields,
            boolean modern121Format) {

        public static Features modern() {
            return new Features(true, true, true, true, true, true, true, true, true);
        }
    }

    private VersionCompat() {
    }

    public static Features features(Workspace workspace) {
        String version = minecraftVersion(workspace);
        return new Features(
                compareVersion(version, 1, 20, 5) >= 0,
                compareVersion(version, 1, 20, 2) >= 0,
                compareVersion(version, 1, 17, 0) >= 0,
                compareVersion(version, 1, 17, 0) >= 0,
                compareVersion(version, 1, 18, 0) >= 0,
                compareVersion(version, 1, 19, 4) >= 0,
                compareVersion(version, 1, 19, 0) >= 0,
                compareVersion(version, 1, 20, 5) >= 0,
                compareVersion(version, 1, 21, 0) >= 0);
    }

    /**
     * The {@code random_sequence} field was introduced in Minecraft 1.20.
     *
     * @param workspace current workspace
     * @return {@code true} when the generator targets 1.20 or newer
     */
    public static boolean supportsRandomSequence(Workspace workspace) {
        String version = minecraftVersion(workspace);
        if (version == null || version.isBlank()) {
            return false;
        }
        String[] parts = version.split("\\.");
        try {
            int major = Integer.parseInt(parts[0].replaceAll("[^0-9]", ""));
            int minor = parts.length > 1 ? Integer.parseInt(parts[1].replaceAll("[^0-9]", "")) : 0;
            return major > 1 || (major == 1 && minor >= 20);
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    /**
     * Item components replaced item NBT in Minecraft 1.20.5. Loot functions
     * that target item data therefore use a different JSON shape from that
     * version onward.
     *
     * @param workspace current workspace
     * @return {@code true} for Minecraft 1.20.5 or newer
     */
    public static boolean usesItemComponents(Workspace workspace) {
        return compareVersion(minecraftVersion(workspace), 1, 20, 5) >= 0;
    }

    /**
     * Potion effect NBT changed in 1.20.2 from numeric effect ids to namespaced
     * ids under {@code custom_potion_effects}.
     *
     * @param workspace current workspace
     * @return {@code true} for Minecraft 1.20.2 or newer
     */
    public static boolean usesNamespacedPotionNbt(Workspace workspace) {
        return compareVersion(minecraftVersion(workspace), 1, 20, 2) >= 0;
    }

    private static int compareVersion(String version, int majorTarget, int minorTarget, int patchTarget) {
        if (version == null || version.isBlank()) {
            return -1;
        }
        String[] parts = version.split("\\.");
        try {
            int major = Integer.parseInt(parts[0].replaceAll("[^0-9]", ""));
            int minor = parts.length > 1 ? Integer.parseInt(parts[1].replaceAll("[^0-9]", "")) : 0;
            int patch = parts.length > 2 ? Integer.parseInt(parts[2].replaceAll("[^0-9]", "")) : 0;
            if (major != majorTarget) {
                return Integer.compare(major, majorTarget);
            }
            if (minor != minorTarget) {
                return Integer.compare(minor, minorTarget);
            }
            return Integer.compare(patch, patchTarget);
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    public static String minecraftVersion(Workspace workspace) {
        if (workspace == null || workspace.getGenerator() == null) {
            return "";
        }
        try {
            return workspace.getGenerator().getGeneratorMinecraftVersion();
        } catch (RuntimeException exception) {
            return "";
        }
    }
}
