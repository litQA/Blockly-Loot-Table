# Changelog

## 1.0.3.11-beta

### Fixed

- The entity potion-effect condition is now allowed in Block loot tables so it
  can test the player who breaks the block. A target-entity input on a Block
  table reports a warning because the entity is only available for player drops;
  source-entity and source-player inputs are rejected and skipped in that
  context.
- Enchantment ids are normalized to lower case. The item enchantment predicate
  now emits the official 1.21 `EnchantmentPredicate` shape, with the
  `enchantments` list and a `levels` range object:
  `{"enchantments":["minecraft:vanishing_curse"],"levels":{"min":1}}`.

### Internal

- Extended the headless self-check to 190 checks, including the Block potion
  context, target-entity warning and the enchantment predicate shape.

## 1.0.3.7-beta

### Fixed

- Fixed number-provider JSON. Terminal `Integer` and `Float` blocks now write a
  bare JSON number, while `Integer provider` and `Float provider` write
  `{"type":"minecraft:constant","value":...}`. The previous build wrapped both
  forms as constant providers, so uniform bounds were invalid.
- Removed the old `Set count uniform` function from the toolbox. The item count
  socket now takes the `Uniform` provider directly.

### New Content

- Added a `Uniform` number-provider block with two nested number/provider
  sockets and a `Binomial` number-provider block with integer trials and float
  probability sockets.
- Added the `Add value` function block for the entry function chain; it writes a
  `minecraft:set_count` function with `"add": true`.
- Float blocks now default to `1.0` and preserve a decimal point in generated
  JSON, even when the value is whole.

### Internal

- Added recursive `uniform` and `binomial` number-provider models. Providers
  accept bare numbers, constant providers and nested provider blocks.
- Extended the headless self-check to 185 checks, including bare/constant
  serialization, nested providers, binomial type handling and count functions.

## 1.0.3.5-beta

### Fixed

- Fixed Blockly statement connections. `previousStatement` and
  `nextStatement` now use Blockly's string check form; the object form prevented
  pools, entries, conditions and functions from connecting.
- Removed the `Block to test` prefix from the block selector; only the selector
  field is shown.

### New Content

- Split the number block into `Integer`, `Float`, `Integer provider` and
  `Float provider`. Integer blocks truncate decimal input at parse time and add a
  validation warning; float blocks keep the decimal part.
- Entry counts and the `set_count uniform`/`set_count add` inputs accept both
  number values and number providers. Uniform and add count providers are
  serialized recursively.
- The default item count now uses the `Integer` block with value 1.

### Internal

- The number-provider model now stores nested minimum and maximum providers, so
  generated uniform and add functions can use provider-shaped JSON.
- Extended the headless self-check to 171 checks, including statement-check
  syntax, provider sockets, float uniform bounds and integer truncation.

## 1.0.3.2-beta

### New Content

- Added block and entity NBT logic, number and text comparisons. Block NBT uses
  `minecraft:location_check`; entity NBT uses `minecraft:entity_properties`.
- Added immediate-source-entity and source-player dependency blocks. Source
  entity now resolves to the indirect damage source (`attacker`), immediate
  source entity to `direct_attacker`, and source player to `attacking_player`.
- Added the entity main-hand item block and the item enchantment condition.
  Enchantment checks on a main-hand item emit an entity equipment predicate;
  checks without an item input fall back to `minecraft:match_tool`.
- Added item count value input plus the independent `set_count uniform` and
  `set_count add` loot-function blocks on the entry function chain.
- Added a `minecraft components` toolbox category, an item condition category,
  and a number category.
- Entity blocks now use the native entity color, item blocks the native item
  color, and NBT blocks the native logic, math and text hues.
- Added strict Blockly connection checks for pools, entries, conditions and
  functions. Incompatible statement blocks can no longer be stacked.

### Internal

- Replaced entry `min_count`/`max_count` fields with a typed number provider and
  a loot-function list in the model, parser and JSON writer.
- Extended the headless self-check to 151 checks covering the new conditions,
  dependencies, count functions, colors, categories and connection checks.

## 1.0.2.1-beta

### New Content

- The loot table start block is now hidden from the toolbox and cannot be
  deleted. Each loot table has exactly one entry point.
- Added block-state boolean, integer and enum comparison conditions.
- Added the entity potion-effect condition using `minecraft:entity_properties`.
- Added provided-blockstate, block-selector, event/target-entity and
  source-entity value blocks. The provided blockstate resolves to the block
  selected by the loot table element, and entity dependencies resolve to
  `this`, `attacker` or `attacking_player` based on the loot context.
- Extended the headless self-check to 80 checks covering the new block JSON,
  parser, writer, localization and duplicate-start protection.

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
