# Phase 1 Implementation Plan and Status

This document tracks the first-phase MVP from `user_docs/start.md` against the
current source tree.

## Delivered in phase 1

- [x] Java plugin skeleton, `plugin.json`, Gradle build and local wrapper.
- [x] `Blockly Loot Table` mod element type registration.
- [x] Block / Entity selector and context dependency label.
- [x] Automatic and manual loot table path.
- [x] Blockly editor type, categories and pool/entry/condition blocks.
- [x] `ConditionScopeRegistry` as a single source of truth.
- [x] Blockly warning markers for illegal condition scopes.
- [x] Compile notes panel and save-time validation.
- [x] Generation-time validation that blocks invalid JSON.
- [x] Standard loot table JSON output with pool rolls, bonus rolls, conditions,
  weighted item entries and count ranges.
- [x] Version-aware data folder (`loot_tables` vs `loot_table`).
- [x] `random_sequence` for 1.20 and newer.
- [x] Two templates: ore block with chance, entity rare drop with player kill.
- [x] README, changelog, migration report and manual test checklist.

## Validation mapping to phase 1 acceptance

1. Element creation and Block/Entity selection: implemented.
2. Block table with diamond pool and `random_chance 0.5`: implemented and
   covered by `selfCheck`.
3. Entity table with rotten flesh count range and `killed_by_player`: implemented
   and covered by `selfCheck`.
4. Entity table with `block_state_property`: editor error, save block and
   generation block implemented.
5. Block table with `killed_by_player`: same validation path.
6. README includes install, build, examples and manual test steps.

## Phase 2 candidates

- `table_bonus`
- `alternative`
- `location_check`
- `random_chance_with_looting`
- `killed_by_player_or_pets`
- Richer error summary panel
- Range editors for rolls and bonus rolls

## Phase 3 candidates

- `match_tool`
- `entity_properties`
- `damage_source_properties`
- Complete `block_state_property` property selector
- Predicate JSON fragment fallback input
- Preset template library
- Native block/entity loot table reference integration
