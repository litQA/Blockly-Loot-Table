# Migration and Limitation Report

This report records the design decisions and known limitations of the Blockly
Loot Table 2026.1 implementation.

## Scope

The plugin adds a configuration element backed by a Blockly editor. The element
is converted to a standard loot table JSON document by the generator template
for the active generator. No native generator definition, FreeMarker template or
workspace metadata is modified.

## Fixed Issues

### 1.0.3.67-beta

- Converted every condition block from a statement-chain block to an A-type
  `LootTableCondition` value block. Pool and item-entry condition inputs now
  take one condition value.
- Reworked `All conditions` and `Any condition` into dynamic A-1 combiners and
  `Not` into a single-value A block, all placed at the Conditions root.
- Reworked all three alternatives pools to add/remove `if` and `else if`
  condition-entry rows dynamically; the unconditional `else` row remains last.
- Added Scoreboard `minecraft:score` number providers. The `scale` value is a
  literal float, so non-constant providers are rejected with a warning.
- Added a warning for any nested loot-table reference because the plugin cannot
  inspect other files to detect a reference cycle.

### 1.0.3.61-beta

- Added version-aware nested loot-table entries. Minecraft 1.21+ uses the
  `value` field; older generators receive `name`.
- Added alternatives pools and a loot-table contents item component.
- Fixed compact item entry weight output, container defaults, fixed NBT
  comparison labels, and right alignment for expandable block rows.

### 1.0.3.55-beta

- Grouped all condition categories under a **Conditions** parent category while
  preserving each subcategory colour.
- Added a condition-free pool block and aligned the loot-table/pool, world and
  Minecraft-components category colours with the native palette.
- Block-state property conditions now take text and typed value inputs; the
  fixed equality operator is no longer a selectable field.
- Exact damage-type conditions are skipped with the warning
  `当前minecraft版本不支持这个条件，它将被跳过`.
- `Killed by player or pets` now matches the `Tame:1b` entity NBT marker.
- The editor dependency summary is bold, larger and colour-coded by dependency
  type.

### 1.0.3.52-beta

- Added item predicate conditions for item type, durability, count and custom
  NBT values. Same-level predicates are merged into one `match_tool` or one
  main-hand `entity_properties` predicate when the fields do not conflict.
- Added **Damage conditions** and the **Source damage** value block. Minecraft
  1.21.1 has no exact damage-type predicate, so damage types are mapped to the
  closest vanilla damage tag. Types without a mapping are skipped with a
  warning.
- Added `Killed by player or pets`. Vanilla 1.21.1 has no single condition for
  this test, so the plugin emits an `any_of` of player-kill and common pet
  entity types.
- Added the **Item components** category and moved all A-type item producers
  there. NBT tags are now text inputs; NBT values use typed value inputs.
- Container rows now default to an empty item selector and Integer 1, use an
  expanded layout and align both row sockets to the right.

### 1.0.3.45-beta

- Added custom-NBT item producers for boolean, number and text values.
  `set_custom_data` is used on 1.20.5+; `set_nbt` is used on older generators.
  NBT is static SNBT, so number NBT accepts only constant values.
- Added an expandable item-with-contents producer. It writes `set_contents` with
  `type` before 1.20.5 and with `component` from 1.20.5. Floating-point literal
  counts are rejected because container counts must be integral.
- Moved A-1 item producers to **Loot tables**, moved **Random chance** to
  **Logic**, and moved the main-hand item block to **Entity conditions**.
- Fixed the boolean and empty-text labels to keep their `%1` field placeholder.
  Without it Blockly rejected those blocks and the toolboxes that referenced
  them could render empty or overlap.
- Removed the empty **Loot conditions** category.
- Limitation: custom NBT is limited to a flat tag name and a scalar value. Nested
  compounds, lists and dynamic number providers are not supported.

### 1.0.3.41-beta

- The **Functions** category and every function block now use the purple
  colour 280.
- The compact item entry label no longer contains “Simple”.
- Added the 1.21-1.21.8 method reference and a 1.20-1.20.6 compatibility report.
  The report concludes that the methods emitted by this plugin do not break
  across 1.20-1.20.6; only the data-pack folder changes from `loot_tables` to
  `loot_table` in 1.21.

### 1.0.3.38-beta

- Added a **Functions** category and five loot-function blocks. `apply_bonus`
  uses `uniform_bonus_count`, `binomial_with_bonus_count` or `ore_drops`;
  `enchanted_count_increase` and `limit_count` cover the remaining requested
  functions.
- Added a simple item entry with only item and count sockets; it still emits the
  normal base `minecraft:set_count` but has no conditions or functions.
- Moved **Block is tagged** into the Block conditions category and renamed
  **Structure is tagged** to **Current structure is tagged**.
- The random-any enchantment item now exposes minimum and maximum levels and
  emits `minecraft:enchant_with_levels` with a uniform provider. Vanilla
  `enchant_randomly` has no level bounds, so this is the closest valid mapping.
- Limitation: `apply_bonus` multipliers, extra rolls and probability are literal
  numbers in 1.21.1. Their sockets therefore accept `Number` only; a provider is
  rejected with a warning and the default is used.
- Limitation: `enchanted_count_increase.count` and `limit_count.limit` accept
  NumberProvider values because their vanilla codecs support them.

### 1.0.3.32-beta

- **Item with enchantments** now stores each row level as a NumberProvider. The
  row level socket accepts terminal numbers or provider blocks and writes the
  provider object under `minecraft:set_enchantments`.
- Added four A-1 enchantment item producers. The random-any and level-any
  variants expose a treasure checkbox. 1.21.1 has no `treasure` JSON field, so
  checked omits `options` and unchecked writes
  `options: "#minecraft:on_random_loot"`.
- Added time and location condition blocks. Same-level `minecraft:location_check`
  predicates are merged when their keys do not conflict; conflicting predicates
  remain separate.
- Limitation: `time_check.period` is a plain long in 1.21.1. The periodic block
  accepts a number input, but a non-constant provider is rejected with a warning
  and the condition is skipped.
- Limitation: `locations` merging is shallow at the predicate level. Two
  conditions that both write `block` (for example block NBT and a block tag)
  remain separate instead of being combined incorrectly.

### 1.0.3.24-beta

- Removed the preset-template selector and the template document class. The
  editor now starts from the fixed empty workspace.
- Split the target control: Block tables use an icon selector backed by
  `ElementUtil.loadBlocks`, while Entity tables use a text button that opens the
  native `DataListSelectorDialog` entity list.
- **Provided blockstate** now derives from the effective block loot path. For a
  manual path such as `mymod:blocks/custom_ore`, the provided block is
  `mymod:custom_ore`; this keeps it aligned with custom paths.
- Replaced the multi-enchantment condition with an **Item with enchantment**
  A-1 value block that outputs an `MCItem` and writes `minecraft:set_enchantments`
  when used in an entry item slot.
- Removed the legacy number, inverted, block-state-property, single-level
  enchantment and uniform-function blocks from the parser, writer, registry and
  toolbox.
- Limitation: the enchanted-item rows set enchantment levels with
  `minecraft:set_enchantments`; they do not add random per-row level ranges.

### 1.0.3.21-beta

- The editor target is now an icon selector. Manual path mode disables the
  selector and enables path editing; automatic mode disables the field and
  regenerates the path. This also removes the old free-form target text field.
- Entity automatic paths normalize registry mappings such as
  `minecraft:entities/EntityType.ZOMBIE` to `minecraft:entities/zombie`.
- Added nested `and`, `or` and `not` conditions. `not` with one term emits
  `inverted`; with multiple terms it emits `inverted(all_of(...))`, matching the
  requested all-false semantics.
- Item entries now require an **Item selector** data input. The empty selector is
  available in **Minecraft components**, so entries no longer embed a selector
  field directly.
- Added a multi-enchantment condition using a `simpleRepeatingInputMixin`-style
  mutator. Rows are added/removed dynamically and each row has an enchantment
  id plus a minimum level.
- Removed the legacy inverted, number, uniform-function, single-level
  enchantment and block-state-property blocks. They are not parseable in this
  version and are not present in the toolbox. Rebuild those parts with the
  current blocks.
- Limitation: the multi-enchantment block groups all rows into one
  `minecraft:enchantments` predicate list; it does not expose independent
  per-row level ranges or arbitrary nested item predicates.

### 1.0.3.16-beta

- Block loot tables provide the target entity when a player breaks the block.
  Therefore the **Source entity**, **Immediate source entity** and **Source
  player** aliases resolve to `this` in Block tables instead of being skipped.
  Entity tables keep `attacker`, `direct_attacker` and `attacking_player`.
- Enchantment conditions can use the breaker's main-hand item in Block tables.
  They emit an `equipment.mainhand` predicate targeting `this`; a condition with
  no item input falls back to `minecraft:match_tool`.
- Added minimum-only and range enchantment conditions. The range condition
  writes `levels` with both `min` and `max`; the minimum condition writes
  `levels.min`. The legacy single-level block remains parseable but is hidden
  from the toolbox.
- Integer level sockets truncate fractional input with a warning, clamp levels
  to `1..255`, and swap reversed range bounds with a warning.

### 1.0.3.14-beta

- `Float` and `Float provider` no longer rely on Blockly's numeric field
  formatting, which renders `1.0` as `1`. A custom text field validates the
  numeric value, normalizes whole numbers to one decimal place, and keeps the
  parser's existing numeric handling unchanged.

### 1.0.3.11-beta

- The potion-effect condition is valid in both Block and Entity loot tables.
  Block tables can supply the target entity only when a player breaks the block;
  the editor and save validation report this as a warning, not an error. Source
  entity, immediate source entity and source-player values are skipped in Block
  contexts because the block loot context does not provide those parameters.
- Enchantment predicates now use lower-case enchantment ids and a `levels` range
  object. The predicate keeps the official `enchantments` list field because
  Minecraft 1.21.1 through 1.21.10 codecs require it; the singular
  `enchantment` field is not an `EnchantmentPredicate` field in those versions.
  Existing elements pick up the corrected output the next time they are
  generated.

### 1.0.3.7-beta

- Number blocks now distinguish terminal values from provider constants.
  `Integer` and `Float` write bare JSON numbers; `Integer provider` and
  `Float provider` write `minecraft:constant` provider objects. Existing
  workspaces with the old `loottable_number` block continue to load.
- `Uniform` and `Binomial` are number-provider blocks that can be nested through
  their sockets. `Add value` is the function-chain block that emits
  `minecraft:set_count` with `add: true`.
- The legacy `Set count uniform` function block remains parseable but is hidden
  from the toolbox. Reopen an old workspace and replace it with a `Uniform`
  provider in the item count socket.

### 1.0.3.5-beta

- Statement checks now use Blockly's string form instead of an object. The object
  form was rejected by Blockly, which made all statement blocks unable to connect.
- The single number block was split into `Integer`, `Float`, `Integer provider`
  and `Float provider`. Integer values are truncated with a warning when they
  contain a fractional part.
- Entry counts and `set_count uniform`/`set_count add` inputs accept number
  providers as well as number values. Existing workspaces that contain the
  legacy `loottable_number` block continue to load and generate.
- The block selector no longer displays the `Block to test` prefix.

### 1.0.3.2-beta

- Added block and entity NBT logic/number/text conditions. Block NBT is emitted
  as `minecraft:location_check` with a block predicate; entity NBT is emitted as
  `minecraft:entity_properties`.
- Added immediate-source-entity and source-player dependencies, the entity
  main-hand item block, and the item enchantment condition.
- Replaced the entry `min_count`/`max_count` fields with a typed count input and
  a function chain. Independent `set_count uniform` and `set_count add` function
  blocks are supported.
- Added strict Blockly connection checks so pools, entries, conditions and
  functions cannot be stacked across incompatible chains.
- Split component, item and number blocks into dedicated toolbox categories and
  matched the native entity, item, logic, math and text colors.
- Existing workspaces that used `min_count`/`max_count` load with the new count
  input absent, so their generated count defaults to 1. The old fields are
  ignored. Reopen and save those elements to add an explicit count.

### 1.0.2.1-beta

- The `loottable_start` block is hidden from the toolbox and forced to be
  non-deletable. A workspace with multiple start blocks keeps the first one and
  reports a warning for the rest.
- Added block-state boolean, integer and enum comparisons, plus the entity
  potion-effect predicate. The legacy `block_state_property` block remains
  parseable for existing workspaces but is hidden from the toolbox.
- Added provided-blockstate, block-selector, event/target-entity and
  source-entity value blocks. The provided blockstate is resolved from the loot
  table element target; entity dependencies resolve by context.
- The generated JSON schema is unchanged for existing elements, so no workspace
  migration is required.

### 1.0.1.8-beta

- The custom Blockly editor now registers the toolbar localization keys used by
  MCreator 2026.1. Without `blockly.search_loottable`, the toolbar painted a null
  string and flooded the event dispatch thread with exceptions.
- Validation reads the Blockly workspace on a background worker after a short
  debounce. It no longer calls `BlocklyPanel.getXML()` synchronously from the
  EDT while the initial workspace is still loading.
- Element data and generated loot table JSON are unchanged by this fix, so no
  workspace migration is required.

## Known Limitations

1. **Context validation is implemented in Java.**
   Blockly block definitions contain no loot table type information. The
   `ConditionScopeRegistry` is the single source of truth used by the editor
   warning pass, the save validation and the generation validation. Third-party
   blocks that add conditions must register their scope in this Java registry or
   they are treated as unknown and skipped with a warning.

2. **Condition and function subset.**
   The available conditions are `random_chance`, `killed_by_player`,
   `survives_explosion`, `weather_check`, `inverted`, the legacy
   `block_state_property` form, block-state boolean/integer/enum comparisons,
   the entity potion-effect predicate, block/entity NBT comparisons, and the item
   enchantment condition. `table_bonus`, `random_chance_with_looting`,
   `killed_by_player_or_pets`, `alternative`, `and`, full `entity_properties` and
   `damage_source_properties` remain future work. The potion condition exposes
   the `effects` predicate only, and NBT conditions compare one tag at a time.
   The item count accepts recursive number providers; the function chain supports
   `set_count` add, while other loot functions remain future work.

3. **Concrete item entries only.**
   The item selector uses MCreator's block/item list. Item tags are recognized
   when the selector returns a tag, but tag entries are skipped with a build
   warning because 1.21+ loot table entry schemas require a different tag form.

4. **Custom block variant handling is conservative.**
   Custom elements resolve through MCreator's registry names. Multi-state
   variants such as stairs, slabs and walls rely on MCreator's normal item
   mapping; a variant that cannot be resolved is skipped instead of producing an
   invalid item id.

5. **Manual path restrictions.**
   Manual paths must be lowercase namespaced resource locations with forward
   slashes. Traversal, backslashes, empty segments and absolute paths are
   rejected. Two elements writing the same normalized path are rejected to avoid
   silently overwriting a generated JSON file.

6. **Unknown blocks are skipped.**
   Unknown pool, entry or condition blocks produce a warning and are omitted.
   Illegal condition scopes are errors and block generation, as required by the
   plugin specification.

7. **No full predicate editor.**
   `inverted` supports one nested condition. There is no full predicate tree
   editor yet; deeper nesting is a phase-three feature.

8. **Generator matrix.**
   Definitions are supplied for the Forge, NeoForge and Fabric generator names
   listed in `README.md`. The first manual test gate covers the generators
   shipped with the local MCreator 2026.1 installation. Fabric 1.20.1, 1.21.1 and
   1.21.11 definitions are present for when the matching Fabric generator plugin
   is installed but are not shipped with 2026.1.

9. **Minecraft 26.x is out of scope for this branch.**
   The 26.x loot table schema changes `condition` to `type`, replaces condition
   lists with single predicates, renames `functions` to `modifier`, and changes
   number providers. Mixing 26.x with 2026.1 would violate the branch policy, so
   those generators must be supported by a separate `plugin-branch-26.x` source
   branch.

10. **Generated-file path conflict detection is element-level.**
    The plugin detects duplicate paths among Blockly Loot Table elements. It
    cannot detect a conflict with a file generated by an unrelated native
    element because the plugin does not read or modify MCreator's internal
    workspace files.

11. **No Gradle post-processor.**
    The plugin emits JSON through the element template and does not inject a
    Gradle task. There is therefore no configuration-cache hook or postprocessor
    revision to maintain in this phase.

12. **Blockly connection types are plugin-defined.**
    Pools, entries, conditions and functions use the plugin connection types
    `LootTablePool`, `LootTableEntry`, `LootTableCondition` and
    `LootTableFunction`. Third-party blocks must opt into the matching type to
    connect to these chains. NBT conditions compare one scalar tag at a time and
    do not provide a compound or list editor.
