package com.litqa.blocklyloottable.ui;

import com.litqa.blocklyloottable.BlocklyLootTablePlugin;
import com.litqa.blocklyloottable.element.types.BlocklyLootTable;
import com.litqa.blocklyloottable.model.Issue;
import com.litqa.blocklyloottable.model.JsonText;
import com.litqa.blocklyloottable.model.LootTableType;
import com.litqa.blocklyloottable.model.ValidationReport;
import com.litqa.blocklyloottable.util.ChestCollections;
import com.litqa.blocklyloottable.util.RegistryNames;
import net.mcreator.blockly.BlocklyCompileNote;
import net.mcreator.blockly.data.BlocklyLoader;
import net.mcreator.blockly.data.ToolboxType;
import net.mcreator.minecraft.DataListEntry;
import net.mcreator.minecraft.ElementUtil;
import net.mcreator.minecraft.MCItem;
import net.mcreator.ui.MCreator;
import net.mcreator.ui.blockly.BlocklyEditorToolbar;
import net.mcreator.ui.blockly.BlocklyPanel;
import net.mcreator.ui.blockly.CompileNotesPanel;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.ui.dialogs.DataListSelectorDialog;
import net.mcreator.ui.dialogs.MCItemSelectorDialog;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.modgui.ModElementGUI;
import net.mcreator.ui.validation.AggregatedValidationResult;
import net.mcreator.ui.validation.ValidationResult;
import net.mcreator.ui.validation.component.VTextField;
import net.mcreator.util.image.EmptyIcon;
import net.mcreator.util.image.ImageUtils;
import net.mcreator.workspace.Workspace;
import net.mcreator.workspace.elements.ModElement;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Blockly based loot table editor.
 */
public class BlocklyLootTableGUI extends ModElementGUI<BlocklyLootTable> {

    private final MCreator mcreatorRef;

    private final JComboBox<String> lootTableType = new JComboBox<>(new String[]{"Block", "Entity", "Chest"});
    private final JButton blockTargetSelector = new JButton();
    private final JButton entityTargetSelector = new JButton();
    private final JButton chestTargetSelector = new JButton();
    private final JCheckBox manualPath = new JCheckBox();
    private final VTextField path = new VTextField(15);
    private String targetValue = "";
    private final JLabel dependencyLabel = new JLabel();

    private final CompileNotesPanel compileNotesPanel = new CompileNotesPanel();
    private final JLabel statusLabel = new JLabel();

    private final Timer validationTimer;
    private final ExecutorService validationExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Blockly Loot Table validation");
        thread.setDaemon(true);
        return thread;
    });

    private BlocklyPanel blocklyPanel;
    private boolean refreshing;
    private volatile boolean blocklyReady;
    private volatile boolean closed;
    private boolean validationInFlight;
    private boolean validationPending;

    public BlocklyLootTableGUI(MCreator mcreator, ModElement modElement, boolean editingMode) {
        super(mcreator, modElement, editingMode);
        this.mcreatorRef = mcreator;
        validationTimer = new Timer(300, event -> runValidation());
        validationTimer.setRepeats(false);
        initGUI();
        super.finalizeGUI();
    }

    @Override
    protected void initGUI() {
        blocklyPanel = new BlocklyPanel(mcreator, BlocklyLootTablePlugin.LOOT_TABLE_EDITOR);
        blocklyPanel.setPreferredSize(new Dimension(900, 440));
        blocklyPanel.addTaskToRunAfterLoaded(() -> {
            if (BlocklyLoader.INSTANCE.getBlockLoader(BlocklyLootTablePlugin.LOOT_TABLE_EDITOR) != null) {
                BlocklyLoader.INSTANCE.getBlockLoader(BlocklyLootTablePlugin.LOOT_TABLE_EDITOR)
                        .loadBlocksAndCategoriesInPanel(blocklyPanel, ToolboxType.EMPTY);
            } else {
                compileNotesPanel.updateCompileNotes(List.of(new BlocklyCompileNote(
                        BlocklyCompileNote.Type.ERROR,
                        "The loot table Blockly block loader is not registered.")));
            }
            blocklyPanel.addChangeListener(event -> SwingUtilities.invokeLater(this::refreshValidation));
            if (!isEditingMode()) {
                blocklyPanel.setInitialXML(BlocklyLootTable.XML_BASE);
            }
            blocklyReady = true;
            refreshValidation();
        });

        JPanel header = new JPanel(new GridBagLayout());
        header.setOpaque(false);
        buildHeader(header);

        BlocklyEditorToolbar toolbar = new BlocklyEditorToolbar(mcreator,
                BlocklyLootTablePlugin.LOOT_TABLE_EDITOR, blocklyPanel);

        JPanel editor = new JPanel(new BorderLayout(0, 5));
        editor.setOpaque(false);
        editor.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(150, 150, 150)),
                L10N.t("elementgui.blockly_loot_table.editor")));
        JPanel workspacePanel = new JPanel(new GridLayout());
        workspacePanel.setOpaque(false);
        workspacePanel.add(PanelUtils.northAndCenterElement(toolbar, blocklyPanel));
        editor.add(workspacePanel, BorderLayout.CENTER);
        editor.add(compileNotesPanel, BorderLayout.SOUTH);

        JPanel footer = new JPanel(new BorderLayout(0, 3));
        footer.setOpaque(false);
        footer.add(statusLabel, BorderLayout.NORTH);
        footer.add(editor, BorderLayout.CENTER);

        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setOpaque(false);
        root.add(PanelUtils.northAndCenterElement(header, footer), BorderLayout.CENTER);

        addPage(L10N.t("elementgui.blockly_loot_table.page"), root);

        path.setValidator(this::validatePathField);
        path.enableRealtimeValidation();
        blockTargetSelector.setToolTipText(L10N.t("elementgui.blockly_loot_table.target.tooltip"));
        blockTargetSelector.setPreferredSize(new Dimension(38, 30));
        blockTargetSelector.setFocusPainted(false);
        entityTargetSelector.setToolTipText(L10N.t("elementgui.blockly_loot_table.target.tooltip"));
        entityTargetSelector.setPreferredSize(new Dimension(180, 30));
        entityTargetSelector.setFocusPainted(false);
        chestTargetSelector.setToolTipText(L10N.t("elementgui.blockly_loot_table.target.chest_tooltip"));
        chestTargetSelector.setPreferredSize(new Dimension(180, 30));
        chestTargetSelector.setFocusPainted(false);
        updateTargetSelectorState();
        path.setToolTipText(L10N.t("elementgui.blockly_loot_table.path.tooltip"));
        manualPath.setSelected(false);
        updateManualPathState();
        updateDependencies();
        updateAutomaticPath();
        refreshValidation();

        lootTableType.addActionListener(event -> onTypeChanged());
        manualPath.addActionListener(event -> {
            updateManualPathState();
            refreshValidation();
        });
        blockTargetSelector.addActionListener(event -> openTargetSelector());
        entityTargetSelector.addActionListener(event -> openTargetSelector());
        chestTargetSelector.addActionListener(event -> openTargetSelector());
        path.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            if (!refreshing) {
                refreshValidation();
            }
        }));
    }

    private void buildHeader(JPanel header) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(2, 4, 2, 4);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        constraints.gridy = 0;
        constraints.gridx = 0;
        constraints.gridwidth = 1;
        constraints.weightx = 0;
        header.add(L10N.label("elementgui.blockly_loot_table.type"), constraints);

        constraints.gridx = 1;
        constraints.weightx = 0;
        header.add(lootTableType, constraints);

        constraints.gridx = 2;
        constraints.fill = GridBagConstraints.NONE;
        header.add(blockTargetSelector, constraints);
        header.add(entityTargetSelector, constraints);
        header.add(chestTargetSelector, constraints);

        constraints.gridx = 3;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        manualPath.setText(L10N.t("elementgui.blockly_loot_table.manual_path"));
        header.add(manualPath, constraints);

        constraints.gridx = 5;
        constraints.gridwidth = 1;
        constraints.weightx = 0;
        header.add(L10N.label("elementgui.blockly_loot_table.path"), constraints);

        constraints.gridx = 6;
        constraints.weightx = 0.35;
        header.add(path, constraints);

        constraints.gridy = 1;
        constraints.gridx = 0;
        constraints.gridwidth = 7;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        dependencyLabel.setForeground(new Color(90, 90, 90));
        header.add(dependencyLabel, constraints);
    }

    private void onTypeChanged() {
        if (refreshing) {
            return;
        }
        setTargetValue("");
        updateTargetSelectorState();
        updateDependencies();
        updateAutomaticPath();
        refreshValidation();
    }

    private void setTargetValue(String value) {
        targetValue = value == null ? "" : value.trim();
        if (currentType() == LootTableType.ENTITY) {
            targetValue = RegistryNames.normalizeEntityName(targetValue);
        } else if (currentType() == LootTableType.CHEST && !targetValue.isBlank()) {
            Workspace workspace = mcreatorRef.getWorkspace();
            if (workspace != null) {
                targetValue = RegistryNames.autoPath(workspace, LootTableType.CHEST, targetValue, "");
            }
        }
        updateTargetIcon();
        updateEntityTargetText();
        updateChestTargetText();
        if (!manualPath.isSelected()) {
            updateAutomaticPath();
        }
        refreshValidation();
    }

    private void updateTargetSelectorState() {
        LootTableType type = currentType();
        boolean block = type == LootTableType.BLOCK;
        boolean entity = type == LootTableType.ENTITY;
        boolean chest = type == LootTableType.CHEST;
        blockTargetSelector.setVisible(block);
        entityTargetSelector.setVisible(entity);
        chestTargetSelector.setVisible(chest);
        updateTargetIcon();
        updateEntityTargetText();
        updateChestTargetText();
    }

    private void updateTargetIcon() {
        ImageIcon icon = new EmptyIcon.ImageIcon(24, 24);
        Workspace workspace = mcreatorRef.getWorkspace();
        if (!targetValue.isBlank() && workspace != null) {
            for (MCItem item : ElementUtil.loadBlocks(workspace)) {
                String resolved = RegistryNames.resolveBlockOrItem(workspace, item.getName());
                if (targetValue.equals(item.getName()) || targetValue.equals(resolved)) {
                    if (item.icon != null) {
                        icon = new ImageIcon(ImageUtils.resizeAA(item.icon.getImage(), 24, 24));
                    }
                    break;
                }
            }
        }
        blockTargetSelector.setIcon(icon);
        blockTargetSelector.setText("");
    }

    private void updateEntityTargetText() {
        if (currentType() != LootTableType.ENTITY) {
            entityTargetSelector.setText("");
            return;
        }
        String display = targetValue;
        Workspace workspace = mcreatorRef.getWorkspace();
        if (!targetValue.isBlank() && workspace != null) {
            for (DataListEntry entry : ElementUtil.loadAllEntities(workspace)) {
                String resolved = RegistryNames.resolveEntity(workspace, entry.getName());
                if (targetValue.equals(resolved) || targetValue.equals(entry.getName())) {
                    display = entry.getReadableName();
                    break;
                }
            }
        }
        entityTargetSelector.setText(display.isBlank()
                ? L10N.t("elementgui.blockly_loot_table.target.select_entity")
                : display);
        entityTargetSelector.setIcon(null);
    }

    private void updateChestTargetText() {
        if (currentType() != LootTableType.CHEST) {
            chestTargetSelector.setText("");
            return;
        }
        String display = targetValue;
        for (DataListEntry entry : ChestCollections.entries()) {
            if (targetValue.equals(entry.getName())) {
                display = entry.getReadableName();
                break;
            }
        }
        chestTargetSelector.setText(display.isBlank()
                ? L10N.t("elementgui.blockly_loot_table.target.select_chest")
                : display);
    }

    private void updateDependencies() {
        String prefix = L10N.t("elementgui.blockly_loot_table.dependencies");
        String block = dependencyName("#D4A017", "block");
        String entity = dependencyName("#2AA198", "entity");
        String targetEntity = dependencyName("#2AA198", "target_entity");
        String sourceEntity = dependencyName("#2AA198", "source_entity");
        String world = dependencyName("#A68A52", "world");
        String damage = dependencyName("#E05A9A", "damage");
        String dependencies = switch (currentType()) {
            case ENTITY -> entity + ", " + sourceEntity + ", " + world + ", " + damage;
            case CHEST -> targetEntity + ", " + world;
            case BLOCK -> block + ", " + targetEntity + ", " + sourceEntity + ", " + world;
        };
        dependencyLabel.setText("<html><b>" + prefix + " " + dependencies
                + "</b></html>");
    }

    private static String dependencyName(String color, String key) {
        return "<font color='" + color + "'>" + L10N.t("elementgui.blockly_loot_table.dep." + key) + "</font>";
    }

    private void updateManualPathState() {
        boolean manual = manualPath.isSelected();
        blockTargetSelector.setEnabled(!manual);
        entityTargetSelector.setEnabled(!manual);
        chestTargetSelector.setEnabled(!manual);
        path.setEditable(manual);
        if (!manual) {
            updateAutomaticPath();
        }
    }

    private void updateAutomaticPath() {
        if (manualPath.isSelected() || path == null) {
            return;
        }
        Workspace workspace = mcreatorRef.getWorkspace();
        String resolvedTarget = "";
        if (!targetValue.isEmpty() && workspace != null) {
            resolvedTarget = switch (currentType()) {
                case ENTITY -> RegistryNames.resolveEntity(workspace, targetValue);
                case CHEST -> targetValue;
                case BLOCK -> RegistryNames.resolveBlockOrItem(workspace, targetValue);
            };
            if (resolvedTarget.isBlank()) {
                resolvedTarget = targetValue;
            }
        }
        String fallbackName = modElement.getRegistryName();
        if (fallbackName == null || fallbackName.isBlank()) {
            fallbackName = modElement.getName();
        }
        String automatic = RegistryNames.autoPath(workspace, currentType(), resolvedTarget, fallbackName);
        if (!automatic.equals(path.getText())) {
            refreshing = true;
            try {
                path.setText(automatic);
            } finally {
                refreshing = false;
            }
        }
    }

    private void openTargetSelector() {
        Workspace workspace = mcreatorRef.getWorkspace();
        if (workspace == null) {
            return;
        }
        if (currentType() == LootTableType.ENTITY) {
            DataListEntry selected = DataListSelectorDialog.openSelectorDialog(mcreatorRef,
                    ElementUtil::loadAllEntities,
                    L10N.t("dialog.selector.title"),
                    L10N.t("dialog.selector.entity.message"));
            if (selected != null) {
                setTargetValue(RegistryNames.resolveEntity(workspace, selected.getName()));
            }
        } else if (currentType() == LootTableType.CHEST) {
            DataListEntry selected = DataListSelectorDialog.openSelectorDialog(mcreatorRef,
                    ignored -> ChestCollections.entries(),
                    L10N.t("elementgui.blockly_loot_table.target.chest_title"),
                    L10N.t("elementgui.blockly_loot_table.target.chest_message"));
            if (selected != null) {
                setTargetValue(selected.getName());
            }
        } else {
            MCItem selected = MCItemSelectorDialog.openSelectorDialog(mcreatorRef, ElementUtil::loadBlocks);
            if (selected != null) {
                setTargetValue(selected.getName());
            }
        }
    }

    private ValidationResult validatePathField() {
        if (!manualPath.isSelected()) {
            return ValidationResult.PASSED;
        }
        RegistryNames.PathCheck check = RegistryNames.checkPath(path.getText(),
                mcreatorRef.getWorkspace() == null ? "minecraft"
                        : mcreatorRef.getWorkspace().getWorkspaceSettings().getModID());
        if (!check.valid()) {
            return new ValidationResult(ValidationResult.Type.ERROR, check.message());
        }
        return ValidationResult.PASSED;
    }

    private void refreshValidation() {
        if (closed) {
            return;
        }
        if (SwingUtilities.isEventDispatchThread()) {
            validationTimer.restart();
        } else {
            SwingUtilities.invokeLater(validationTimer::restart);
        }
    }

    private void runValidation() {
        if (closed || refreshing || !blocklyReady || blocklyPanel == null) {
            return;
        }
        if (validationInFlight) {
            validationPending = true;
            return;
        }

        validationInFlight = true;
        validationExecutor.submit(() -> {
            String xml = null;
            try {
                xml = blocklyPanel.getXML();
            } catch (RuntimeException exception) {
                // The web view can be closing while a validation is queued.
            }
            String finalXml = xml == null || xml.isBlank() ? BlocklyLootTable.XML_BASE : xml;
            SwingUtilities.invokeLater(() -> {
                if (closed) {
                    validationInFlight = false;
                    return;
                }
                validationInFlight = false;
                boolean rerun = validationPending;
                validationPending = false;
                applyValidation(finalXml);
                if (rerun) {
                    refreshValidation();
                }
            });
        });
    }

    private void applyValidation(String xml) {
        BlocklyLootTable probe = buildElement(xml);
        ValidationReport report = probe.getValidationReport();
        compileNotesPanel.updateCompileNotes(report.getIssues().stream().map(this::toCompileNote).toList());
        updateStatus(report);
        applyBlocklyWarnings(report);
    }

    private BlocklyCompileNote toCompileNote(Issue issue) {
        return new BlocklyCompileNote(issue.isError()
                ? BlocklyCompileNote.Type.ERROR
                : BlocklyCompileNote.Type.WARNING, issue.message());
    }

    private void updateStatus(ValidationReport report) {
        long errors = report.getErrorMessages().size();
        long warnings = report.getWarningMessages().size();
        if (errors > 0) {
            statusLabel.setForeground(new Color(170, 30, 30));
            statusLabel.setText(L10N.t("elementgui.blockly_loot_table.status.errors", errors)
                    + " " + report.getErrorMessages().get(0));
        } else if (warnings > 0) {
            statusLabel.setForeground(new Color(170, 110, 0));
            statusLabel.setText(L10N.t("elementgui.blockly_loot_table.status.warnings", warnings)
                    + " " + report.getWarningMessages().get(0));
        } else {
            statusLabel.setForeground(new Color(30, 130, 30));
            statusLabel.setText(L10N.t("elementgui.blockly_loot_table.status.ok"));
        }
    }

    private void applyBlocklyWarnings(ValidationReport report) {
        try {
            String errorsJson = JsonText.write(report.getErrorMessagesByBlockType()).trim();
            String script = "(function(){var e=" + errorsJson + ";var ws=workspace;"
                    + "if(!ws)return;ws.getAllBlocks(false).forEach(function(b){"
                    + "b.setWarningText(e[b.type]||null);});})()";
            blocklyPanel.executeLocalScript(script);
        } catch (RuntimeException exception) {
            // The Blockly web view may not be loaded yet; the next change event retries.
        }
    }

    private BlocklyLootTable buildElement(String xml) {
        BlocklyLootTable element = new BlocklyLootTable(modElement);
        element.lootTableType = currentType().getDisplayName();
        element.target = targetValue;
        element.manualPath = manualPath.isSelected();
        element.path = path.getText() == null ? "" : path.getText().trim();
        element.blocklyXml = xml;
        return element;
    }

    private LootTableType currentType() {
        return switch (String.valueOf(lootTableType.getSelectedItem())) {
            case "Entity" -> LootTableType.ENTITY;
            case "Chest" -> LootTableType.CHEST;
            default -> LootTableType.BLOCK;
        };
    }

    @Override
    public void openInEditingMode(BlocklyLootTable element) {
        lootTableType.setSelectedItem(element.getLootTableTypeEnum().getDisplayName());
        targetValue = element.target == null ? "" : element.target.trim();
        if (element.getLootTableTypeEnum() == LootTableType.ENTITY) {
            targetValue = RegistryNames.normalizeEntityName(targetValue);
        } else if (element.getLootTableTypeEnum() == LootTableType.CHEST && !targetValue.isBlank()) {
            Workspace workspace = mcreatorRef.getWorkspace();
            if (workspace != null) {
                targetValue = RegistryNames.autoPath(workspace, LootTableType.CHEST, targetValue, "");
            }
        }
        updateTargetSelectorState();
        manualPath.setSelected(element.manualPath);
        path.setText(element.path == null ? "" : element.path);
        updateManualPathState();
        updateDependencies();
        blocklyPanel.addTaskToRunAfterLoaded(() -> {
            blocklyPanel.setInitialXML(element.blocklyXml == null || element.blocklyXml.isBlank()
                    ? BlocklyLootTable.XML_BASE
                    : element.blocklyXml);
            refreshValidation();
        });
    }

    @Override
    public BlocklyLootTable getElementFromGUI() {
        String xml = blocklyPanel == null ? BlocklyLootTable.XML_BASE : blocklyPanel.getXML();
        if (xml == null || xml.isBlank()) {
            xml = BlocklyLootTable.XML_BASE;
        }
        BlocklyLootTable element = buildElement(xml);
        if (!element.manualPath && (element.path == null || element.path.isBlank())) {
            element.path = element.getAutomaticPath();
        }
        return element;
    }

    @Override
    protected AggregatedValidationResult getAdditionalValidationResult(BlocklyLootTable element) {
        ValidationReport report = element.getValidationReport();
        if (report.hasErrors()) {
            return new AggregatedValidationResult.FAIL(report.summary());
        }
        return super.getAdditionalValidationResult(element);
    }

    @Override
    public void reloadDataLists() {
        super.reloadDataLists();
        updateDependencies();
    }

    @Override
    public void onViewClosed() {
        closed = true;
        validationTimer.stop();
        validationExecutor.shutdownNow();
        if (blocklyPanel != null) {
            blocklyPanel.close();
        }
        super.onViewClosed();
    }

    @Override
    public URI contextURL() throws URISyntaxException {
        return new URI("https://mcreator.net/wiki/how-make-loot-table");
    }

    private static final class SimpleDocumentListener implements DocumentListener {

        private final Runnable action;

        private SimpleDocumentListener(Runnable action) {
            this.action = action;
        }

        @Override
        public void insertUpdate(DocumentEvent event) {
            action.run();
        }

        @Override
        public void removeUpdate(DocumentEvent event) {
            action.run();
        }

        @Override
        public void changedUpdate(DocumentEvent event) {
            action.run();
        }
    }
}
