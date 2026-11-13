package com.litqa.blocklyloottable.util;

import com.litqa.blocklyloottable.model.LootTableType;
import net.mcreator.element.parts.EntityEntry;
import net.mcreator.element.parts.MItemBlock;
import net.mcreator.generator.GeneratorWrapper;
import net.mcreator.workspace.Workspace;
import net.mcreator.workspace.elements.ModElement;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Converts MCreator selector values and user-entered identifiers into Minecraft
 * registry names and validates loot table resource paths.
 */
public final class RegistryNames {

    private static final Pattern NAMESPACE = Pattern.compile("[a-z0-9_.-]+");
    private static final Pattern PATH_SEGMENT = Pattern.compile("[a-z0-9_.-]+");
    private static final Pattern RESOURCE_LOCATION = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_.-]+(/[a-z0-9_.-]+)*");

    private RegistryNames() {
    }

    public record PathCheck(boolean valid, String message, String normalized) {

        public static PathCheck valid(String normalized) {
            return new PathCheck(true, "", normalized);
        }

        public static PathCheck invalid(String message) {
            return new PathCheck(false, message, "");
        }
    }

    /**
     * Resolves a block or item selector value to a registry name.
     *
     * @param workspace current workspace
     * @param rawValue  selector value or manually entered identifier
     * @return registry name, tag prefixed with {@code #}, or an empty string
     */
    public static String resolveBlockOrItem(Workspace workspace, String rawValue) {
        return resolve(workspace, rawValue, false);
    }

    /**
     * Resolves an entity selector value to a registry name.
     *
     * @param workspace current workspace
     * @param rawValue  selector value or manually entered identifier
     * @return registry name or an empty string
     */
    public static String resolveEntity(Workspace workspace, String rawValue) {
        return resolve(workspace, rawValue, true);
    }

    private static String resolve(Workspace workspace, String rawValue, boolean entity) {
        if (rawValue == null) {
            return "";
        }
        String value = rawValue.trim();
        if (value.isEmpty()) {
            return "";
        }
        if (value.startsWith("#")) {
            return value;
        }
        if (value.startsWith("TAG:")) {
            String tag = value.substring("TAG:".length()).trim();
            if (tag.isEmpty()) {
                return "";
            }
            return tag.startsWith("#") ? tag : "#" + tag;
        }
        if (value.startsWith("CUSTOM:")) {
            return resolveCustomElement(workspace, value);
        }
        if (value.startsWith("EXTERNAL:")) {
            value = value.substring("EXTERNAL:".length()).trim();
        }
        if (RESOURCE_LOCATION.matcher(value).matches()) {
            return entity ? normalizeEntityName(value) : value;
        }
        if (workspace == null || workspace.getGenerator() == null) {
            return "";
        }

        try {
            String mapped = entity ?
                    new EntityEntry(workspace, value).getMappedValue(1) :
                    new MItemBlock(workspace, value).getMappedValue(1);
            if (mapped == null || mapped.isBlank()) {
                return "";
            }
            mapped = mapped.trim();
            if (mapped.startsWith("#")) {
                return mapped;
            }
            if (entity) {
                return normalizeEntityName(mapped);
            }
            if (mapped.contains(":")) {
                return mapped;
            }
            return "minecraft:" + mapped;
        } catch (RuntimeException exception) {
            return "";
        }
    }

    private static String resolveCustomElement(Workspace workspace, String value) {
        if (workspace == null || workspace.getGenerator() == null) {
            return "";
        }
        ModElement element = findCustomElement(workspace, value);
        if (element == null) {
            return "";
        }
        return workspace.getWorkspaceSettings().getModID() + ":" + element.getRegistryName();
    }

    private static ModElement findCustomElement(Workspace workspace, String value) {
        String plain = value.substring("CUSTOM:".length());
        int suffix = plain.indexOf('#');
        if (suffix >= 0) {
            plain = plain.substring(0, suffix);
        }
        int dot = plain.lastIndexOf('.');
        if (dot >= 0) {
            plain = plain.substring(0, dot);
        }
        return workspace.getModElementByName(plain);
    }

    /**
     * Normalizes entity mappings such as {@code minecraft:entities/EntityType.ZOMBIE}
     * into the registry id {@code minecraft:zombie}.
     */
    public static String normalizeEntityName(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim();
        if (normalized.isEmpty() || normalized.startsWith("#")) {
            return normalized;
        }
        String namespace = "minecraft";
        String path = normalized;
        int colon = normalized.indexOf(':');
        if (colon >= 0) {
            namespace = normalized.substring(0, colon);
            path = normalized.substring(colon + 1);
        }
        boolean mappedEntity = path.startsWith("entities/");
        if (mappedEntity) {
            path = path.substring("entities/".length());
        }
        if (mappedEntity || path.startsWith("EntityType.")) {
            int dot = path.lastIndexOf('.');
            if (dot >= 0) {
                path = path.substring(dot + 1);
            }
        }
        return namespace + ":" + path.toLowerCase(Locale.ENGLISH);
    }

    /**
     * Builds the automatic loot table path for a selected target.
     *
     * @param workspace    current workspace
     * @param type         loot table type
     * @param target       selected block or entity registry name
     * @param fallbackName element name used when no target is selected
     * @return namespaced loot table path
     */
    public static String autoPath(Workspace workspace, LootTableType type, String target, String fallbackName) {
        String modid = workspace == null ? "minecraft" : workspace.getWorkspaceSettings().getModID();
        String folder = type == null ? LootTableType.BLOCK.getDefaultPathFolder() : type.getDefaultPathFolder();
        String effectiveTarget = target == null ? "" : target.trim();
        if (effectiveTarget.isEmpty() || effectiveTarget.startsWith("#")) {
            return modid + ":" + folder + "/" + sanitizePathSegment(fallbackName);
        }
        if (type == LootTableType.ENTITY) {
            effectiveTarget = normalizeEntityName(effectiveTarget);
        }

        String namespace = modid;
        String path = effectiveTarget;
        int colon = effectiveTarget.indexOf(':');
        if (colon > 0) {
            namespace = effectiveTarget.substring(0, colon);
            path = effectiveTarget.substring(colon + 1);
        }
        if (path.startsWith(folder + "/")) {
            return namespace + ":" + path;
        }
        if (path.contains("/")) {
            return namespace + ":" + path;
        }
        return namespace + ":" + folder + "/" + path;
    }

    /**
     * Validates and normalizes a loot table resource path.
     *
     * @param rawPath user or automatic path
     * @param modid   fallback namespace
     * @return validation result with the normalized path
     */
    public static PathCheck checkPath(String rawPath, String modid) {
        if (rawPath == null || rawPath.trim().isEmpty()) {
            return PathCheck.invalid("Loot table path cannot be empty.");
        }
        String path = rawPath.trim();
        if (path.indexOf('\\') >= 0 || path.startsWith("/") || path.endsWith("/")) {
            return PathCheck.invalid("Loot table path must be a namespaced resource location with forward slashes.");
        }

        String namespace = modid == null || modid.isBlank() ? "minecraft" : modid;
        String resourcePath = path;
        int colon = path.indexOf(':');
        if (colon >= 0) {
            namespace = path.substring(0, colon);
            resourcePath = path.substring(colon + 1);
        }
        if (!NAMESPACE.matcher(namespace).matches()) {
            return PathCheck.invalid("Loot table namespace '" + namespace + "' is not a valid resource location namespace.");
        }
        if (resourcePath.isBlank()) {
            return PathCheck.invalid("Loot table path must contain at least one path segment.");
        }
        for (String segment : resourcePath.split("/", -1)) {
            if (segment.isEmpty() || ".".equals(segment) || "..".equals(segment)
                    || !PATH_SEGMENT.matcher(segment).matches()) {
                return PathCheck.invalid("Loot table path segment '" + segment
                        + "' is invalid. Use lowercase letters, digits, underscore, dot or dash.");
            }
        }
        return PathCheck.valid(namespace + ":" + resourcePath);
    }

    public static String namespace(String normalizedPath) {
        int colon = normalizedPath.indexOf(':');
        return colon < 0 ? "" : normalizedPath.substring(0, colon);
    }

    public static String pathPart(String normalizedPath) {
        int colon = normalizedPath.indexOf(':');
        return colon < 0 ? normalizedPath : normalizedPath.substring(colon + 1);
    }

    public static String sanitizePathSegment(String value) {
        if (value == null) {
            return "loot_table";
        }
        String sanitized = value.toLowerCase(Locale.ENGLISH).trim().replace(' ', '_')
                .replaceAll("[^a-z0-9_.-]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+|_+$", "");
        return sanitized.isEmpty() ? "loot_table" : sanitized;
    }
}
