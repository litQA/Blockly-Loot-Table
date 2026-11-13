package com.litqa.blocklyloottable.element.types;

import com.litqa.blocklyloottable.model.BlocklyXmlParser;
import com.litqa.blocklyloottable.model.ItemNameResolver;
import com.litqa.blocklyloottable.model.LootTableJsonWriter;
import com.litqa.blocklyloottable.model.LootTableType;
import com.litqa.blocklyloottable.model.ValidationReport;
import com.litqa.blocklyloottable.util.RegistryNames;
import com.litqa.blocklyloottable.util.VersionCompat;
import net.mcreator.blockly.data.BlocklyXML;
import net.mcreator.element.GeneratableElement;
import net.mcreator.element.ModElementType;
import net.mcreator.workspace.Workspace;
import net.mcreator.workspace.elements.ModElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Persistent Blockly loot table configuration element.
 *
 * <p>The element stores the Blockly document and the output path. Generation
 * parses the document again and refuses to produce JSON while context errors
 * are present.</p>
 */
public class BlocklyLootTable extends GeneratableElement {

    public static final String REGISTRY_NAME = "blockly_loot_table";
    public static final String XML_BASE = "<xml xmlns=\"https://developers.google.com/blockly/xml\">"
            + "<block type=\"loottable_start\" deletable=\"false\" x=\"40\" y=\"40\"></block></xml>";

    /** Stored display name of the loot table type: {@code Block} or {@code Entity}. */
    public String lootTableType = "Block";
    /** Selected block or entity registry name used to build the automatic path. */
    public String target = "";
    /** When true the path field is user controlled and the target selector is disabled. */
    public boolean manualPath = false;
    /** Namespaced loot table path, for example {@code mymod:blocks/diamond_ore}. */
    public String path = "";
    /** Serialized Blockly workspace. */
    @BlocklyXML("loottable")
    public String blocklyXml = XML_BASE;

    public BlocklyLootTable(ModElement element) {
        super(element);
    }

    public LootTableType getLootTableTypeEnum() {
        return LootTableType.fromStored(lootTableType);
    }

    public String getDependencyLabel() {
        return getLootTableTypeEnum() == LootTableType.ENTITY
                ? "event/target entity, source entity, immediate source entity, source player, main-hand item, world"
                : "provided blockstate, block selector, target entity (player breaker only), world";
    }

    public String getTargetRegistryName() {
        Workspace workspace = getWorkspace();
        if (workspace == null || target == null || target.isBlank()) {
            return "";
        }
        String resolved = getLootTableTypeEnum() == LootTableType.ENTITY
                ? RegistryNames.resolveEntity(workspace, target)
                : RegistryNames.resolveBlockOrItem(workspace, target);
        return resolved.isBlank() ? target.trim() : resolved;
    }

    public String getAutomaticPath() {
        Workspace workspace = getWorkspace();
        String fallbackName = getModElement() == null ? "loot_table" : getModElement().getRegistryName();
        if (fallbackName == null || fallbackName.isBlank()) {
            fallbackName = "loot_table";
        }
        return RegistryNames.autoPath(workspace, getLootTableTypeEnum(), getTargetRegistryName(), fallbackName);
    }

    /**
     * @return the configured path when it validates, otherwise the automatic path
     */
    public String getEffectivePath() {
        String configured = path == null ? "" : path.trim();
        if (!configured.isEmpty()) {
            return configured;
        }
        return getAutomaticPath();
    }

    public String getLootTableNamespace() {
        return RegistryNames.namespace(getSafeNormalizedPath());
    }

    public String getLootTablePath() {
        return RegistryNames.pathPart(getSafeNormalizedPath());
    }

    /**
     * @return normalized output key used to detect duplicate generated files
     */
    public String getOutputKey() {
        return getSafeNormalizedPath();
    }

    /**
     * Validates the element for the editor, save and generation phases.
     *
     * @return report containing context errors, path errors and skipped-entry warnings
     */
    public ValidationReport getValidationReport() {
        return validate(parseDocument());
    }

    private BlocklyXmlParser.ParseResult parseDocument() {
        return BlocklyXmlParser.parse(blocklyXml, getLootTableTypeEnum(), createResolver(),
                getTargetRegistryName());
    }

    private ValidationReport validate(BlocklyXmlParser.ParseResult parseResult) {
        ValidationReport report = new ValidationReport();
        RegistryNames.PathCheck pathCheck = RegistryNames.checkPath(getEffectivePath(), workspaceModId());
        if (!pathCheck.valid()) {
            report.addError(null, "Loot table path is invalid: " + pathCheck.message());
        }

        if (target != null && !target.isBlank() && getTargetRegistryName().isBlank()) {
            report.addWarning(null, "The selected target could not be resolved. The path may be incorrect.");
        }

        report.merge(parseResult.report());

        String outputKey = getSafeNormalizedPath();
        if (!outputKey.isBlank()) {
            for (ModElement other : workspaceElements()) {
                if (other == getModElement() || other.equals(getModElement())) {
                    continue;
                }
                ModElementType<?> type = other.getType();
                if (type == null || !REGISTRY_NAME.equals(type.getRegistryName())) {
                    continue;
                }
                if (other.getGeneratableElement() instanceof BlocklyLootTable otherTable
                        && outputKey.equals(otherTable.getSafeNormalizedPath())) {
                    report.addError(null, "Another Blockly Loot Table element already writes to " + outputKey
                            + ". Change the path or remove the duplicate element.");
                }
            }
        }
        return report;
    }

    /**
     * @return error messages that must block generation
     */
    public List<String> getGenerationErrors() {
        return getValidationReport().getErrorMessages();
    }

    /**
     * Renders the standard loot table JSON.
     *
     * @return JSON document
     * @throws IllegalStateException when validation errors are present
     */
    public String getGeneratedJson() {
        BlocklyXmlParser.ParseResult parseResult = parseDocument();
        ValidationReport report = validate(parseResult);

        List<String> errors = report.getErrorMessages();
        if (!errors.isEmpty()) {
            throw new IllegalStateException("Blockly Loot Table generation blocked: " + String.join(" | ", errors));
        }
        for (String warning : report.getWarningMessages()) {
            System.out.println("[Blockly Loot Table] Warning: " + warning);
        }

        String outputKey = getSafeNormalizedPath();
        boolean randomSequence = VersionCompat.supportsRandomSequence(getWorkspace());
        String json = LootTableJsonWriter.write(parseResult.model(),
                RegistryNames.namespace(outputKey),
                RegistryNames.pathPart(outputKey),
                randomSequence);
        System.out.println("[Blockly Loot Table] Generated loot table " + outputKey);
        return json;
    }

    public ItemNameResolver createResolver() {
        Workspace workspace = getWorkspace();
        if (workspace == null) {
            return ItemNameResolver.IDENTITY;
        }
        return rawValue -> {
            String item = RegistryNames.resolveBlockOrItem(workspace, rawValue);
            if (!item.isBlank()) {
                return item;
            }
            return RegistryNames.resolveEntity(workspace, rawValue);
        };
    }

    private Workspace getWorkspace() {
        return getModElement() == null ? null : getModElement().getWorkspace();
    }

    private String workspaceModId() {
        Workspace workspace = getWorkspace();
        return workspace == null ? "minecraft" : workspace.getWorkspaceSettings().getModID();
    }

    private List<ModElement> workspaceElements() {
        Workspace workspace = getWorkspace();
        if (workspace == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(workspace.getModElements());
    }

    /**
     * Returns a valid path for template file naming. Invalid input falls back to
     * the automatic path so that template path evaluation cannot escape the data
     * directory; generation still stops because {@link #getGenerationErrors()} is
     * checked by the template.
     */
    private String getSafeNormalizedPath() {
        String configured = path == null ? "" : path.trim();
        if (!configured.isEmpty()) {
            RegistryNames.PathCheck check = RegistryNames.checkPath(configured, workspaceModId());
            if (check.valid()) {
                return check.normalized();
            }
        }
        String automatic = getAutomaticPath();
        RegistryNames.PathCheck automaticCheck = RegistryNames.checkPath(automatic, workspaceModId());
        if (automaticCheck.valid()) {
            return automaticCheck.normalized();
        }
        return workspaceModId() + ":loot_table/" + RegistryNames.sanitizePathSegment(
                getModElement() == null ? "loot_table" : getModElement().getName());
    }

    /**
     * @return normalized path suitable for diagnostics
     */
    public String getRequestedPath() {
        String configured = path == null ? "" : path.trim();
        return configured.isEmpty() ? getAutomaticPath() : configured;
    }
}
