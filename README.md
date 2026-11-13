# Blockly Loot Table (MCreator 2026.1)

A Java plugin for MCreator 2026.1 that adds a Blockly based loot table element.
The element builds `pools -> entries -> conditions` visually and generates
standard Minecraft loot table JSON. It does not generate Java and does not
modify MCreator's native loot table element or generator templates.

## Scope

Supported condition blocks:

- `random_chance`
- `killed_by_player` (Entity loot tables only)
- `survives_explosion` (Block loot tables only)
- `weather_check`
- block-state boolean, integer and enum comparisons (Block loot tables only)
- entity potion-effect predicate (Block tables test the player who breaks the
  block; Entity tables test the selected entity)
- block and entity NBT logic, number and text comparisons
- item enchantment conditions with an item input, minimum level and optional
  maximum level
- enchanted-item value blocks with dynamic enchantment rows and NumberProvider
  levels
- time checks and location checks for dimension, biome, biome tag, structure,
  structure tag, smokey, light level, block tag and x/y/z position
- nested `and`, `or` and `not` logic conditions

Same-level location checks are merged into one `minecraft:location_check`
predicate when their keys do not conflict, so dimension, biome, position and
block-tag tests become a single AND predicate.

The **Minecraft components** category provides the empty item selector,
provided-blockstate, block selector, event/target entity, source entity,
immediate source entity, source player and entity main-hand item blocks. The
provided blockstate resolves to the block named by the effective loot table
path, so an element with manual path `mymod:blocks/custom_ore` supplies
`mymod:custom_ore` even when the selected target differs.
In Entity tables, source entity is the indirect damage source (`attacker`),
immediate source entity is `direct_attacker`, and source player is
`attacking_player`. In Block tables all three source aliases refer to the player
who broke the block (`this`). The main-hand item block takes an entity input and
can feed the enchantment conditions.

The `loottable_start` block is hidden from the toolbox and cannot be deleted.
Each loot table has exactly one start block; extra start blocks are ignored with
a warning.

Pools support `rolls`, `bonus_rolls`, conditions and weighted item entries.
Entries require an **Item selector** input, a number count input, a weight, a
condition chain and a function chain. The item input also accepts the
**Item with enchantment** value block and the four randomized enchantment item
variants. **Item with enchantments** writes `minecraft:set_enchantments` with
dynamic rows whose levels accept a bare number or a number provider. The four
other variants write `minecraft:enchant_randomly` or
`minecraft:enchant_with_levels`; their enchantment list rows use MCreator's
`enchantments` datalist, and the treasure checkbox omits `options` when checked
so treasure enchantments are eligible, while an unchecked box restricts to
`#minecraft:on_random_loot`.

The **Loot tables** category also contains A-1 item producers for custom NBT and
container contents. The three NBT blocks add boolean, number or text SNBT to the
item's `tag`. On 1.20.5+ they emit `minecraft:set_custom_data`; older generators
receive `minecraft:set_nbt`. Number NBT accepts only a constant value, because
the tag must be static. **Item with contents** is an expandable container block
with one item socket and one Number/NumberProvider count socket per row. It
emits `minecraft:set_contents` with `type` before 1.20.5 and with `component`
from 1.20.5 onward. A floating-point literal count is rejected so the generated
container remains valid.

`Random chance` now lives in the **Logic** category and uses the native logic
colour. The main-hand item block is in **Entity conditions**. The item producer
blocks were moved out of the item-condition list into **Loot tables**.

The count input accepts a bare number or a number provider. The **Numbers**
category contains the terminal `Integer` and `Float` blocks, the constant
`Integer provider` and `Float provider` blocks, and the `Uniform` and `Binomial`
provider blocks. `Uniform` and `Binomial` can be nested inside one another, and
their sockets accept either a bare number or a provider.

The **Functions** category, colour 280, contains `Add value` and five
loot-function blocks: `Add enchantment bonus` (uniform), `Add binomial
enchantment bonus`, `Apply ore bonus`, `Increase count by enchantment level`
and `Limit final count`. These append `minecraft:apply_bonus`,
`minecraft:enchanted_count_increase` and `minecraft:limit_count` functions to
the entry function chain. The three `apply_bonus` numeric parameters are
literal numbers in 1.21.1; their sockets reject number providers with a
warning.

The compact item entry is a shorter B-shaped entry with only item and count
sockets. It is useful when no conditions or functions are needed.

Bare integer and float blocks serialize as bare JSON numbers. The two provider
constant blocks serialize as `{"type":"minecraft:constant","value":...}`.
Integer blocks truncate fractional input and report a warning; float defaults
display as `1.0`. Incompatible block types cannot be connected because pool,
entry, condition and function connections use distinct Blockly types.

## Supported generators

Definitions are supplied for these MCreator 2026.1 generator folder names:

- Forge 1.16.5, 1.17.1, 1.18.2, 1.19.2, 1.19.4, 1.20.1
- NeoForge 1.20.1, 1.20.4, 1.20.6, 1.21.1, 1.21.4, 1.21.8
- Fabric 1.21.8

The 1.21+ generators write to `data/<namespace>/loot_table/`; earlier
generators write to `data/<namespace>/loot_tables/`. Definitions for Fabric
1.20.1, 1.21.1 and 1.21.11 are also included so the plugin follows the Fabric
generator plugin when it is available. Minecraft 26.x uses a different loot
table schema and must be handled by a separate `plugin-branch-26.x` source
branch.

## Build

The project is self-contained and requires JDK 21. The wrapper uses the public
Gradle 9.8.0 distribution URL by default and keeps all Gradle state under the
workspace `caches/gradle`.

```bat
set JAVA_HOME=<path-to-jdk-21>
gradlew.bat build
```

If the public Gradle download times out or cannot connect, use the workspace
local distribution explicitly through the offline fallback:

```powershell
.\tools\build-offline.ps1 build
```

The plugin archive is written to
`build/libs/blockly_loot_table-2026.1.zip`. The archive name is fixed; the
version and build number are injected into `plugin.json` from
`version.properties`.

Run the headless parser/JSON smoke test with:

```bat
gradlew.bat selfCheck
```

## Install

1. Enable **Java plugins** in MCreator Preferences.
2. Close MCreator.
3. Copy `blockly_loot_table-2026.1.zip` into the MCreator 2026.1 `plugins`
   folder.
4. Start MCreator and create a **Blockly Loot Table** mod element in a Java
   generator workspace.

## Usage

1. Choose `Block` or `Entity`.
2. For a Block table, select the target block with the icon selector next to the
   type. For an Entity table, the selector becomes a text button that opens
   MCreator's native entity selector.
3. The path is generated automatically as
   `<target namespace>:<blocks|entities>/<target path>`. Check **Manual path** to
   disable the selector and enter a path yourself; uncheck it to restore the
   automatic path. Manual block paths also drive **Provided blockstate**.
4. Build the pool/entry/condition chain below the fixed start block.
5. Save. Invalid condition scopes are marked in the Blockly workspace, shown in
   the compile notes panel, and block saving and generation.

The context dependencies shown in the editor are:

- Block: `provided blockstate, block selector, target entity (player breaker only), world`
- Entity: `event/target entity, source entity, immediate source entity, source player, main-hand item, world`

## Examples

### Block ore drop with a chance

```text
Loot table start
└── Pool (rolls 1, bonus rolls 0)
    ├── conditions: Random chance 0.5
    └── entries: Item diamond ore, count 1, weight 1
```

### Entity rare drop with a player kill

```text
Loot table start
└── Pool (rolls 1, bonus rolls 0)
    └── entries: Item rotten flesh, count 2, weight 1
        └── conditions: Killed by player
```

The template examples are documented here only for reference; the plugin no
longer ships a preset-template selector.

## Manual test checklist

- Plugin appears in MCreator 2026.1 and **Blockly Loot Table** is visible.
- Creating a new element opens the Blockly editor promptly, without toolbar
  paint exceptions or repeated WebView execution timeouts in `mcreator.log`.
- The element can be created, saved and reopened with its Blockly document.
- The start block is not in the toolbox and cannot be deleted.
- The header shows one target control next to the type: an icon button for Block
  tables and a native entity-selection button for Entity tables. Manual path
  disables the selector and enables path editing; automatic mode disables the
  path field and regenerates the path.
- Selecting a zombie creates `minecraft:entities/zombie`, not
  `minecraft:entities/EntityType.ZOMBIE`.
- A Block table with `random_chance` and a diamond ore entry saves.
- A Block table with `survives_explosion` saves.
- A Block table with boolean/integer/enum property comparisons saves, and the
  generated condition names the selected block and property value.
- A Block table using **Provided blockstate** emits the block derived from its
  effective path. With manual path `mymod:blocks/custom_ore`, the emitted block is
  `mymod:custom_ore`.
- An Entity table with `killed_by_player` saves.
- An Entity table with the potion condition and **Source entity** emits
  `minecraft:entity_properties`, `entity: "attacker"` and a non-empty
  `predicate.effects` map.
- A Block table with **Source entity**, **Immediate source entity** or **Source
  player** accepts the block and emits `entity: "this"` without an
  unavailable-parameter warning.
- A Block table with the potion condition and **Event/target entity** emits
  `entity: "this"` and shows a warning that the entity is only present when a
  player breaks the block.
- A Block table using the breaker's main-hand item and an enchantment condition
  emits the `equipment.mainhand` predicate with `entity: "this"`.
- Block and entity NBT logic/number/text conditions emit `location_check` and
  `entity_properties` with the expected SNBT value.
- The enchantment condition emits the
  `equipment.mainhand` predicate with the selected enchantment and level.
  The enchantment id is lower case, and the predicate entry uses the official
  1.21 `enchantments` list with a `levels` range object, for example
  `{"enchantments":["minecraft:vanishing_curse"],"levels":{"min":1}}`.
- The minimum-only and range enchantment blocks accept Integer level inputs;
  the range block writes both `levels.min` and `levels.max`, and reversed
  bounds are swapped with a warning.
- **Item with enchantments** accepts a Number/NumberProvider level socket for
  every row and writes those providers under `minecraft:set_enchantments`.
- The four randomized enchantment item blocks are available in **Items**:
  random list, random any with treasure checkbox, level list, and level any with
  treasure checkbox. The random-any block also has minimum and maximum level
  sockets and writes a uniform `levels` provider. The unchecked case writes
  `options: "#minecraft:on_random_loot"`; the checked case omits `options`.
- The compact item entry has only item and count sockets and emits no condition
  or function arrays beyond the base `set_count`.
- The **Functions** category is present, uses the purple colour 280, and
  `Add value` is inside it.
- The five function blocks emit their expected JSON: uniform/binomial/ore
  `apply_bonus`, `enchanted_count_increase` and `limit_count`.
- A provider placed in an `apply_bonus` multiplier/extra/probability socket is
  rejected with a warning and the literal default is used.
- Time-check blocks write `minecraft:time_check`; daily time also writes
  `period: 24000`, and the periodic block accepts period/min/max number inputs.
- Dimension, biome, biome tag, structure, structure tag, smokey, light and
  position conditions at the same chain level merge into one
  `minecraft:location_check` predicate instead of repeating the condition.
- Block-tag conditions write `predicate.block.blocks` as `#<tag>` and appear in
  the Block conditions category.
- The structure-tag block is labelled **Current structure is tagged**.
- The item count socket accepts `Integer`, `Float`, `Integer provider`,
  `Float provider`, `Uniform` or `Binomial`; generated JSON uses a bare number
  for constants and a `minecraft:constant` object for provider constants.
- `Uniform` and `Binomial` can be nested in each other's sockets; binomial
  trials truncate to an integer and probability keeps a float shape.
- `Add value` on the entry function chain emits `minecraft:set_count` with the
  selected count and `"add": true`.
- A float block defaults to `1.0`; an integer block containing a decimal is
  truncated with a warning.
- Pool, entry, condition and function blocks connect when their types match;
  pulling a condition onto an entry chain is rejected by Blockly.
- The block selector displays only the selector field, without a
  `Block to test` prefix.
- Entity and item blocks use the native entity and item colors, and NBT blocks
  use the native logic, math and text hues.
- The **Logic**, **Minecraft components** and **Loot tables** categories render
  their blocks without overlap or empty entries, and **Loot conditions** is
  absent.
- The **Logic** category contains `and`, `or`, `not`, `random chance` and the
  true/false selector; `and`/`or`/`not` can be nested in each other. `and`
  emits `all_of`, `or` emits `any_of`, and `not` with multiple children emits
  `inverted(all_of(...))`.
- The entry item socket accepts the empty item selector and the **Item with
  enchantment** value block. The enchanted item block starts with one row, adds
  and removes rows dynamically, and emits `minecraft:set_enchantments` with the
  selected item and all rows.
- Removed legacy blocks are absent from the toolbox. A workspace containing
  `loottable_inverted`, `loottable_number`, the old uniform function, the old
  single-level enchantment block, the old multi-enchantment condition or
  `loottable_block_state_property` requires rebuilding that part of the table.
- A Block table containing `killed_by_player` shows a Blockly warning, a compile
  notes error, and saving is blocked.
- Build output places the JSON in `loot_tables/` before 1.21 and `loot_table/`
  from 1.21 onward.
- Empty, partially filled and malformed workspaces still produce a valid empty
  or reduced table without a build crash.
- The generated JSON loads in the target Minecraft version.

## Notes and limitations

See `MIGRATION-REPORT.md` for the full limitation list and
`../dsh_docs/blockly_type.md` for the block type/usage reference. The main
restrictions are the condition subset, concrete item entries only (item tags are
skipped with a warning), and enchantment options that use the 1.21 enchantment
registry lists rather than per-item compatibility filtering.
