package com.litqa.blocklyloottable.util;

import net.mcreator.workspace.Workspace;

/**
 * Minecraft-version-dependent loot table details.
 */
public final class VersionCompat {

    private VersionCompat() {
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
