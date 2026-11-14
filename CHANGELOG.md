# Changelog

## 1.2.0.111-beta

### Changed

- Refined the Chinese and English source-dependency wording: source entity,
  immediate source entity, source player and provided damage source.
- Localized the pool branch labels (`if condition`, `entries`, `else if
  condition`, `else entries`) and the potion item-type dropdown.
- Replaced the mod element icons in all bundled themes with the new
  `blockly_loot_table.png` artwork.

## 1.2.0.109-beta

### Added

- Data Pack generator definitions for 1.16.5, 1.17.1, 1.18.2, 1.19.2,
  1.19.4, 1.20.1, 1.20.4, 1.20.6, 1.21.1, 1.21.4 and 1.21.8. Each definition
  is identical to the matching mod-generator definition and writes into the
  Data Pack `src/main/data/<namespace>/loot_tables|...` tree.

### Changed

- Raised the minor version from `1.1.x` to `1.2.0` once; the build number
  continues to increase normally.
- Expanded the 1.16-1.19.4 and 1.20-1.20.6 compatibility documents with Data
  Pack paths, generator mappings and the version-gated skip/warning behavior.

### Verified

- Prepared for the normal self-check/build pipeline; Data Pack resources and
  folder names are covered by self-check assertions.

## 1.1.1.106-beta

### Added

- Chest loot-table type with a searchable, localized selector for 57 vanilla
  chest and structure paths. Chest generation uses the selected path under
  `loot_table/chests/` on 1.21+ and rejects conditions that require the absent
  tool, block-state or explosion-radius context parameters.
- Entity sprinting and sneaking flag conditions.
- `LootTableConditionSafe` output for block/world/weather/time/random
  conditions. Nested loot-table entries accept only this output so entity-,
  item- and damage-dependent conditions cannot make the nested table context
  invalid at runtime.

### Changed

- Enchantment condition labels now consistently end with “on item”.
- The enchantment range defaults to Integer 1 through Infinity; the Infinity
  upper bound omits `max`.
- The full custom-name item producer uses inline inputs.

### Verified

- Self-check 603 checks; loot-table JSON and mutator JavaScript validation pass.

## 1.1.1.96-beta

### Fixed

- The potion `minecraft:item_name` component is now written as an escaped JSON
  string, matching the vanilla component codec. Previously the component was an
  inline object and could make the generated loot table fail to load.
- Removed the Fabric 1.21.11 generator definitions from the 2026.1 branch; the
  supported Fabric range now ends at 1.21.8.

### Verified

- Self-check 575 checks; potion JSON, generator resources and packaging pass.

## 1.1.1.92-beta

### Fixed

- Potion and tipped-arrow items no longer duplicate their selected effect. The
  producer writes `custom_potion_effects`/`custom_effects` only and no longer
  adds a base potion template alongside it.
- Potion and tipped-arrow items now write a translated item name instead of
  falling back to "Uncraftable Potion": `minecraft:item_name` on 1.20.5+, and a
  `display.Name` text component in the older NBT formats.

### Added

- Exact item-enchantment-level condition.
- Exact item-durability condition; both exact blocks remain alongside the
  0/Infinity range variants.

### Verified

- Self-check 575 checks; the potion formats and exact item predicates compile
  and generate the expected JSON.

## 1.1.1.88-beta

### Added

- Potion items now write a base potion template alongside custom effects, so
  vanilla clients show a usable potion name instead of "Uncraftable Potion".
- Added the `Infinity` number provider and a complex entity potion-effect range
  condition with duration and amplifier min/max sockets.
- Merged durability, enchantment-level and item-count range conditions; lower
  bound 0 and upper bound Infinity omit the corresponding JSON fields.
- Added simple and advanced `minecraft:set_name` item producers; the advanced
  form accepts and validates a complete JSON text component.
- Added a warning when a conditional nested loot table is used from a Block
  table, because its conditions see the breaking player as `this`.

### Changed

- Complex pools no longer insert a sample condition or item; the first branch is
  labelled `if condition`. Simple pools start with one simple item entry.

### Verified

- Self-check 568 checks; version-aware generation, potion formats and new item
  components compile without errors.

## 1.1.0.80-beta

### Added

- Added the full 1.16-1.19.4 compatibility matrix in
  `dsh_docs/loot_table_format/loot_table-1.16-1.19.4.md`.
- Added version-aware generation for the older Forge generators:
  - 1.16 falls back to static `set_nbt` enchantments and zero for score providers.
  - `set_contents` is skipped on 1.16-1.17 and uses `type` on 1.18-1.20.4.
  - 1.18+ nested loot tables use the `name` field; 1.21+ uses `value`.
  - 1.19-1.20.4 location predicates use `structure`; earlier versions use
    `feature`; 1.20.5+ uses `biomes`/`structures`.
  - 1.19.4+ damage predicates use `tags`; earlier versions use the old
    boolean fields.
  - Pre-1.21 enchant functions use `enchantments`/`treasure`; 1.21+ uses
    `options`.
  - Pre-1.21 count increase uses `looting_enchant`; 1.21+ uses
    `enchanted_count_increase`.
- Potion item inputs are inline by default.

### Verified

- Self-check 554 checks; all 112 loottable JSON resources and the mutator
  script validate.

## 1.1.0.77-beta

### Added

- Added the **Potion effect** item component. It creates a potion, splash
  potion, lingering potion or tipped arrow with a selected effect, integer
  amplifier and integer duration.
- The generated potion data is version-specific: numeric `CustomPotionEffects`
  NBT on 1.20-1.20.1, namespaced `custom_potion_effects` NBT on 1.20.2-1.20.4,
  and the `minecraft:potion_contents` component on 1.20.5+.

### Verified

- Self-check 537 checks; all loottable JSON resources and the mutator script
  validate.

## 1.1.0.75-beta

### Fixed

- Advanced-pool `mcreator.statements` metadata now uses named objects. MCreator
  previously failed to load all three condition pools with
  `Expected BEGIN_OBJECT but was STRING at path $.statements[0]`.

### Added

- The nested `Loot table ..., weight ..., functions ...` entry now has a
  `conditions` value socket. Conditions are emitted alongside the nested table's
  `value`/`name`, weight and functions.

### Verified

- Self-check 530 checks; all 111 loottable JSON resources and the mutator script
  validate.

## 1.1.0.74-beta

### Changed

- Restored the monotonic build number above the previous `73` (the temporary
  `1.1.0.1` regression is superseded).
- Condition-aware pools now use a native `if` / `else if` / `else` mutator:
  the mutator list only contains optional `else if` and `else` rows, the base
  `if` branch stays in the block body, and `else` remains at the bottom.
- `All conditions` and `Any condition` start with two empty condition rows.
- The circular loot-table reference warning is localized instead of hardcoded
  Chinese.
- Added the Block condition `A and B are the same block`, defaulting to the
  provided blockstate and an empty block selector.

### Verified

- Self-check 528 checks; all 111 loottable JSON resources and the mutator script
  validate.

## 1.1.0.1-beta

### Fixed

- Condition-aware pool blocks can be dragged again. Their mutator now builds
  the condition and entry inputs on Blockly insertion markers as well as on
  normal blocks, so the drag preview no longer fails with
  `The insertion marker manager tried to create a marker but the result is
  missing an input`.

## 1.0.3.73-beta

### Changed

- `All conditions`, `Any condition` and `Not` now start with an empty input
  instead of an embedded example condition.
- The Block conditions and Entity conditions subcategories remain visible and
  their original toolbox assignments are restored.
- Fixed the block/entity NBT condition labels so they reference exactly the
  three block arguments (`tag`, block/entity, value); the extra `%4`
  placeholder made the toolbox categories fail to render in the editor.
- All A-type condition blocks now use the condition green, regardless of their
  original block, entity, world or item category colour.
- The three Scoreboard number providers now use the same blue as the rest of
  the Numbers category.

## 1.0.3.67-beta

### New Content

- Added three Scoreboard number providers under Numbers: entity context score,
  fixed player-name score, and fixed UUID score. Each writes
  `minecraft:score` with a literal scale.
- Added a warning whenever a nested loot-table entry or loot-table-contents
  block references another table, because direct or indirect self-reference is
  not detectable by the plugin and can crash the game or prevent world loading.

### Changed

- Every condition is now an A-type `LootTableCondition` value block. Pool and
  entry condition sockets accept one condition value instead of a statement
  chain.
- `All conditions` and `Any condition` are dynamic A-1 condition combiners at
  the root of the Conditions category. `Not` now accepts exactly one condition
  value and also lives at the Conditions root.
- The three alternatives pools now use dynamic `if` / `else if` condition and
  entry rows; `else` is a single bottom row.

## 1.0.3.61-beta

### New Content

- Added three deep-green alternatives pool blocks: one `if` branch, `if` +
  `else if`, and `if` + `else`. They generate a `minecraft:alternatives` entry
  with an optional final unconditional branch.
- Added two loot-table entries that call another loot table, with optional
  functions. The generated entry uses `value` on 1.21+ and `name` on older
  generators.
- Added an item component that fills a container from another loot table with
  `minecraft:set_contents`.
- Added a weight field to the compact item entry.

### Changed

- The boolean selector label no longer repeats the word "True/false" before the
  selector.
- Context dependencies use normal-size bold text with category colours.
- The equals sign in NBT comparisons is fixed; it is no longer an editable
  field.
- The manual path field is half as wide.
- Container, enchantment and other multi-row B/C blocks now right-align each
  row's item and count/value inputs.
- The container item component defaults to a shulker box.

## 1.0.3.55-beta

### New Content

- Added a **Conditions** parent category with block, entity, item, damage and
  world condition subcategories.
- Added a condition-free **Pool with rolls** block for loot tables that do not
  need a pool condition chain.
- Added a bold, colour-coded context dependency summary to the editor header.

### Changed

- Block-state boolean, integer and enum conditions now use text input blocks
  for the property name and typed input blocks for the comparison value. The
  equality operator is fixed and no longer editable.
- World conditions now use the native earth-yellow colour. The Minecraft
  components category uses the native red colour. Loot table and pool blocks
  use the deep-green colour.
- Exact damage-type conditions are skipped with a clear warning because
  Minecraft 1.21.1 has no exact damage-type predicate.
- `Killed by player or pets` now checks the tamed-mob NBT marker `Tame:1b`
  instead of matching individual pet entity types.

## 1.0.3.52-beta

### New Content

- Added item predicate conditions for item identity, durability bounds, item
  count bounds and custom logic/number/text NBT values.
- Added **Damage conditions** with damage-type and damage-tag blocks, plus a
  reusable **Source damage** value block.
- Added `Killed by player or pets` as an `any_of` approximation of the missing
  vanilla aggregate condition.
- Added an **Item components** category for all A-type item producers.
- NBT tag names are now text value inputs, and block/entity NBT values use
  proper logic, number and text value inputs.

### Changed

- Container rows now default to an empty item selector and Integer 1, use
  expanded layout and right-align both inputs.
- Removed the leading `Item` from the empty item selector and `Empty text` from
  the empty text block.
- Same-level item predicates merge into one `match_tool` or
  `entity_properties.equipment.mainhand` when their keys do not conflict.

### Internal

- Extended the self-check to 420 checks for item predicates, damage conditions,
  merged predicates, NBT value inputs and category placement.

## 1.0.3.45-beta

### New Content

- Added boolean, number and text custom-NBT item value blocks. They emit
  `minecraft:set_custom_data` on 1.20.5+ and `minecraft:set_nbt` on older
  generators.
- Added an expandable item-with-contents block. Each row has an item socket and
  a count provider; the block emits `minecraft:set_contents`.
- Added a true/false selector to the Logic category and an empty text block to
  Minecraft components.

### Changed

- Moved the A-1 item producers from the item-condition list to **Loot tables**.
- Moved **Random chance** to the Logic category and gave it the native logic
  colour.
- Moved the main-hand item block to **Entity conditions**.

### Fixed

- String-aware NBT and container serialization for 1.20.5+ item components.
- Container counts reject floating-point literals instead of emitting an
  invalid `set_contents` entry.
- Fixed the boolean and empty-text labels so Blockly no longer rejects their
  field arguments. This also restores the default-value blocks used by the
  NBT and container producers in **Loot tables**.
- Removed the now-empty **Loot conditions** category.

### Internal

- Extended the self-check to 401 checks for NBT serialization, item containers,
  category placement, placeholder integrity and category removals.

## 1.0.3.41-beta

### Changed

- Changed the **Functions** category and all six function blocks to the purple
  colour 280.
- Removed the word “Simple” from the compact item entry label; it is now
  `Item %1, count %2`.

### Documentation

- Added an English method reference for the 1.21-1.21.8 loot-table format.
- Added a 1.20-1.20.6 compatibility report covering the 1.20, 1.20.2 and
  1.20.5 changes.

### Internal

- Extended the self-check to 373 checks with function colour and label
  assertions.

## 1.0.3.38-beta

### New Content

- Added a **Functions** toolbox category and moved **Add value** into it.
- Added five function blocks: uniform enchantment bonus, binomial enchantment
  bonus, ore-drop enchantment bonus, enchanted count increase and limit count.
- Added a simple item entry with only item and count sockets.
- Added minimum/maximum level sockets to the random enchantment item with the
  treasure checkbox.

### Changed

- **Block is tagged** now appears in the Block conditions category.
- The structure-tag block is labelled **Current structure is tagged**.
- The random-any enchantment item now emits `minecraft:enchant_with_levels` with
  a uniform level range, because 1.21.1 `enchant_randomly` has no level bounds.

### Internal

- Extended the headless self-check to 366 checks, covering the simple entry,
  function JSON, random-range generation and category placement.

## 1.0.3.32-beta

### New Content

- **Item with enchantments** rows now take an A-type Number/NumberProvider
  level socket, so enchantment levels can use integer, float, Uniform, Binomial
  and nested providers.
- Added four randomized enchantment item value blocks:
  **item with one of those enchantments at a random level**, **item with any
  enchantment at a random level, including treasure**, **item enchanted with
  levels, including those enchantments**, and **item enchanted with levels,
  including treasure**.
- Added **World conditions**: absolute time, time of day, periodic time,
  dimension, biome, biome tag, structure, structure tag, smokey, light level and
  range, x/y/z position and ranges, and block tag.
- Added automatic merging of same-level `minecraft:location_check` conditions
  into one predicate when their keys do not conflict.

### Changed

- The treasure checkbox maps to 1.21.1's `enchant_randomly` /
  `enchant_with_levels` `options` field: checked omits `options` (all
  enchantments including treasure); unchecked limits to
  `#minecraft:on_random_loot`.
- Time-check periods must be constant numbers in 1.21.1; a non-constant period
  produces a warning and skips that condition.

### Internal

- Extended the headless self-check to 329 checks, covering provider enchantment
  levels, all four enchant item blocks, time/world predicates and location
  predicate merging.

## 1.0.3.24-beta

### Breaking Changes

- Removed the preset-template selector and the multi-enchantment condition
  block. Existing tables that used the old condition must replace it with the
  new **Item with enchantment** value block.
- Removed the legacy `loottable_number`, `loottable_inverted`,
  `loottable_block_state_property`, old single-level enchantment and old uniform
  function blocks.

### New Content

- Added the **Item with enchantment** A-1 value block. It outputs `MCItem`, starts
  with one enchantment row, supports dynamic add/remove rows and emits
  `minecraft:set_enchantments`.
- Added nested `and`, `or` and `not` controls, the **Item selector** value block
  and the **Item selector** entry input.

### Changed

- The editor header now uses separate target controls: a block icon selector for
  Block tables and a native entity-selector text button for Entity tables.
- The template controls were removed from the header.
- **Provided blockstate** now follows the effective manual block path
  (`mymod:blocks/custom_ore` -> `mymod:custom_ore`).
- Entity automatic paths continue to normalize mapped names such as
  `minecraft:entities/EntityType.ZOMBIE` to `minecraft:entities/zombie`.

### Internal

- Updated the headless self-check to 255 checks, covering the removed legacy
  blocks, logic terms, item selector, enchanted item output and manual-path
  provided blockstate.

## 1.0.3.21-beta

### Breaking Changes

- Removed the legacy `loottable_inverted`, `loottable_number`,
  `loottable_function_set_count_uniform`, `loottable_item_has_enchantment` and
  `loottable_block_state_property` blocks. Existing workspaces must rebuild
  those parts with the current logic, number, enchantment or block-state
  blocks.

### New Content

- Added the **Logic** category with nested `and`, `or` and `not` conditions.
  `and`/`or` emit `all_of`/`any_of`; `not` with multiple children emits
  `inverted(all_of(...))`.
- Added the empty **Item selector** block to **Minecraft components**. Item
  entries now use an item input with the selector as the default.
- Added the multi-enchantment item condition with dynamic add/remove rows and
  per-row enchantment selector plus minimum level.

### Changed

- Reworked the editor header: the target text field and Select button are
  replaced by an icon selector, the path field moved next to **Manual path**, and
  manual/automatic mode now disables the opposite control.
- Entity automatic paths normalize mapped names such as
  `minecraft:entities/EntityType.ZOMBIE` to `minecraft:entities/zombie`.

### Internal

- Extended the headless self-check to 255 checks, covering logic terms, the item
  selector, the multi-enchantment mutator, automatic entity paths and strict
  statement connections.
- Added the block type and usage reference at `../dsh_docs/blockly_type.md`.

## 1.0.3.16-beta

### Fixed

- Block loot tables can now use **Source entity**, **Immediate source entity**
  and **Source player** to test the block breaker. These aliases resolve to
  `this`; they are no longer rejected or skipped.
- Block tables can now test the breaker's main-hand tool enchantment. The item
  input, equipment predicate and `match_tool` fallback work in both table types.

### New Content

- Added minimum-only and range enchantment condition blocks. Both take an item
  input, an enchantment selector and integer level inputs; the range block
  writes `levels` with both `min` and `max`. The older single-level condition is
  kept for existing workspaces and hidden from the toolbox.

### Internal

- Extended the headless self-check to 224 checks, including block source
  aliases, block main-hand enchantment output and the new level sockets.

## 1.0.3.14-beta

### Fixed

- `Float` and `Float provider` use a dedicated Blockly field that keeps an
  explicit decimal point. Their default value is now visibly `1.0` instead of
  `1`; loading an older whole-number value also normalizes it to `X.0`.

### Internal

- Extended the headless self-check to 193 checks, including the custom float
  field registration and the visible `1.0` defaults.

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
