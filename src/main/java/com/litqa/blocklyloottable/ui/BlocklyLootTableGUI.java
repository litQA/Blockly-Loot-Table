package com.litqa.blocklyloottable.ui;

import com.litqa.blocklyloottable.BlocklyLootTablePlugin;
import com.litqa.blocklyloottable.blockly.LootTableTemplates;
import com.litqa.blocklyloottable.element.types.BlocklyLootTable;
import com.litqa.blocklyloottable.model.Issue;
import com.litqa.blocklyloottable.model.JsonText;
import com.litqa.blocklyloottable.model.LootTableType;
import com.litqa.blocklyloottable.model.ValidationReport;
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
import net.mcreator.workspace.Workspace;
import net.mcreator.workspace.elements.ModElement;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
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

/**
 * Blockly based loot table editor.
 */
public class BlocklyLootTableGUI extends ModElementGUI<BlocklyLootTable> {

    private final MCreator mcreatorRef;

    private final JComboBox<String> lootTableType = new JComboBox<>(new String[]{"Block", "Entity"});
    private final VTextField target = new VTextField(30);
    private final JButton selectTarget = new JButton();
    private final JCheckBox manualPath = new JCheckBox();
    private final VTextField path = new VTextField(42);
    private final JLabel dependencyLabel = new JLabel();
    private final JComboBox<String> templateChoice = new JComboBox<>();
    private final JButton applyTemplate = new JButton();

    private final CompileNotesPanel compileNotesPanel = new CompileNotesPanel();
    private final JLabel statusLabel = new JLabel();

    private BlocklyPanel blocklyPanel;
    private boolean refreshing;

    public BlocklyLootTableGUI(MCreator mcreator, ModElement modElement, boolean editingMode) {
        super(mcreator, modElement, editingMode);
        this.mcreatorRef = mcreator;
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
        target.setToolTipText(L10N.t("elementgui.blockly_loot_table.target.tooltip"));
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
        selectTarget.addActionListener(event -> openTargetSelector());
        applyTemplate.addActionListener(event -> applySelectedTemplate());
        target.getDocument().addDocumentListener(new SimpleDocumentListener(this::onTargetChanged));
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

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0;
        header.add(L10N.label("elementgui.blockly_loot_table.type"), constraints);

        constraints.gridx = 1;
        constraints.weightx = 0.12;
        header.add(lootTableType, constraints);

        constraints.gridx = 2;
        constraints.weightx = 0;
        header.add(L10N.label("elementgui.blockly_loot_table.target"), constraints);

        constraints.gridx = 3;
        constraints.weightx = 0.5;
        header.add(target, constraints);

        constraints.gridx = 4;
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.NONE;
        selectTarget.setText(L10N.t("elementgui.blockly_loot_table.select_target"));
        header.add(selectTarget, constraints);

        constraints.gridx = 5;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        manualPath.setText(L10N.t("elementgui.blockly_loot_table.manual_path"));
        header.add(manualPath, constraints);

        constraints.gridy = 1;
        constraints.gridx = 0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0;
        header.add(L10N.label("elementgui.blockly_loot_table.path"), constraints);

        constraints.gridx = 1;
        constraints.gridwidth = 3;
        constraints.weightx = 0.5;
        header.add(path, constraints);

        constraints.gridx = 4;
        constraints.gridwidth = 1;
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.NONE;
        header.add(L10N.label("elementgui.blockly_loot_table.template"), constraints);

        constraints.gridx = 5;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0.2;
        templateChoice.removeAllItems();
        templateChoice.addItem(L10N.t("elementgui.blockly_loot_table.template.empty"));
        templateChoice.addItem(L10N.t("elementgui.blockly_loot_table.template.ore"));
        templateChoice.addItem(L10N.t("elementgui.blockly_loot_table.template.entity"));
        header.add(templateChoice, constraints);

        constraints.gridx = 6;
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.NONE;
        applyTemplate.setText(L10N.t("elementgui.blockly_loot_table.apply_template"));
        header.add(applyTemplate, constraints);

        constraints.gridy = 2;
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
        refreshing = true;
        try {
            target.setText("");
        } finally {
            refreshing = false;
        }
        updateDependencies();
        updateAutomaticPath();
        refreshValidation();
    }

    private void onTargetChanged() {
        if (refreshing) {
            return;
        }
        if (!manualPath.isSelected()) {
            updateAutomaticPath();
        }
        refreshValidation();
    }

    private void updateDependencies() {
        dependencyLabel.setText(L10N.t("elementgui.blockly_loot_table.dependencies") + " "
                + (currentType() == LootTableType.ENTITY
                ? "entity, source entity, world"
                : "blockstate, source entity, world"));
    }

    private void updateManualPathState() {
        boolean manual = manualPath.isSelected();
        target.setEnabled(!manual);
        selectTarget.setEnabled(!manual);
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
        String targetValue = target.getText() == null ? "" : target.getText().trim();
        String resolvedTarget = "";
        if (!targetValue.isEmpty() && workspace != null) {
            resolvedTarget = currentType() == LootTableType.ENTITY
                    ? RegistryNames.resolveEntity(workspace, targetValue)
                    : RegistryNames.resolveBlockOrItem(workspace, targetValue);
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
                target.setText(RegistryNames.resolveEntity(workspace, selected.getName()));
                if (!manualPath.isSelected()) {
                    updateAutomaticPath();
                }
                refreshValidation();
            }
        } else {
            MCItem selected = MCItemSelectorDialog.openSelectorDialog(mcreatorRef, ElementUtil::loadBlocks);
            if (selected != null) {
                target.setText(RegistryNames.resolveBlockOrItem(workspace, selected.getName()));
                if (!manualPath.isSelected()) {
                    updateAutomaticPath();
                }
                refreshValidation();
            }
        }
    }

    private void applySelectedTemplate() {
        int index = templateChoice.getSelectedIndex();
        if (index <= 0) {
            return;
        }
        String xml = LootTableTemplates.byIndex(index);
        if (xml == null) {
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                L10N.t("elementgui.blockly_loot_table.template.confirm"),
                L10N.t("elementgui.blockly_loot_table.template"),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        LootTableType templateType = LootTableTemplates.typeByIndex(index);
        refreshing = true;
        try {
            lootTableType.setSelectedItem(templateType == LootTableType.ENTITY ? "Entity" : "Block");
            target.setText("");
            if (!manualPath.isSelected()) {
                updateAutomaticPath();
            }
        } finally {
            refreshing = false;
        }
        updateDependencies();

        blocklyPanel.addTaskToRunAfterLoaded(() -> {
            blocklyPanel.executeLocalScript("workspace.clear();"
                    + "Blockly.Xml.domToWorkspace(Blockly.Xml.textToDom('" + escapeJsString(xml) + "'), workspace);"
                    + "workspace.clearUndo();");
            refreshValidation();
        });
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
        if (refreshing || blocklyPanel == null) {
            return;
        }
        String xml = blocklyPanel.getXML();
        if (xml == null || xml.isBlank()) {
            xml = BlocklyLootTable.XML_BASE;
        }
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
        element.lootTableType = currentType() == LootTableType.ENTITY ? "Entity" : "Block";
        element.target = target.getText() == null ? "" : target.getText().trim();
        element.manualPath = manualPath.isSelected();
        element.path = path.getText() == null ? "" : path.getText().trim();
        element.blocklyXml = xml;
        return element;
    }

    private LootTableType currentType() {
        return "Entity".equals(lootTableType.getSelectedItem()) ? LootTableType.ENTITY : LootTableType.BLOCK;
    }

    @Override
    public void openInEditingMode(BlocklyLootTable element) {
        lootTableType.setSelectedItem(element.getLootTableTypeEnum() == LootTableType.ENTITY ? "Entity" : "Block");
        target.setText(element.target == null ? "" : element.target);
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
        if (blocklyPanel != null) {
            blocklyPanel.close();
        }
        super.onViewClosed();
    }

    @Override
    public URI contextURL() throws URISyntaxException {
        return new URI("https://mcreator.net/wiki/how-make-loot-table");
    }

    private static String escapeJsString(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "\\r");
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
