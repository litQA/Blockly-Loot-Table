# Migration and Limitation Report

This report records the design decisions and known limitations of the Blockly
Loot Table 2026.1 implementation.

## Scope

The plugin adds a configuration element backed by a Blockly editor. The element
is converted to a standard loot table JSON document by the generator template
for the active generator. No native generator definition, FreeMarker template or
workspace metadata is modified.

## Fixed Issues

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
   The function chain supports `set_count` uniform and add; other loot functions
   remain future work.

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
