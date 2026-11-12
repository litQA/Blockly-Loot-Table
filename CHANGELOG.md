# Changelog

## 1.0.1.8-beta

### Bug Fixes

- Added all Blockly toolbar localization keys required by MCreator 2026.1.
  The missing `blockly.search_loottable` key caused a null string paint
  exception on every toolbar repaint and made the new-element editor appear
  unresponsive.
- Moved Blockly workspace validation off the Swing event dispatch thread and
  debounced validation requests so the initial workspace load cannot block the
  UI for the WebView execution timeout.
- Extended the headless self-check to cover the toolbar localization contract.

## 1.0.1.7-beta

## New Content

- Blockly Loot Table mod element with Block and Entity contexts.
- Blockly editor type, block definitions and four toolbox categories for pools,
  entries and condition blocks.
- Condition scope registry covering `random_chance`, `killed_by_player`,
  `survives_explosion`, `weather_check`, `inverted` and
  `block_state_property`.
- Blockly warning markers, compile notes and save-time validation for illegal
  condition scopes.
- Version-aware JSON generation: `loot_tables` before 1.21, `loot_table` from
  1.21, and `random_sequence` from 1.20.
- Two built-in Blockly templates: block ore drop with chance and entity rare
  drop with player kill.
- Generator definitions for Forge, NeoForge and Fabric 2026.1 generator names.

## Known Limitations

- The condition set is the first-phase subset documented in
  `MIGRATION-REPORT.md`.
- Item tags and nested predicate editors are not implemented yet.
