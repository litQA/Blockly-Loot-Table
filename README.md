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
- block identity comparisons for the provided blockstate and another block
- entity potion-effect predicate (Block tables test the player who breaks the
  block; Entity tables test the selected entity)
- block and entity NBT logic, number and text comparisons
- item identity, durability and count conditions, plus item NBT comparisons
- damage-tag conditions (exact damage-type conditions are skipped with a warning)
- `killed_by_player_or_pets` (an `any_of` approximation using the `Tame:1b`
  marker)
- item enchantment conditions with an item input, minimum level and optional
  maximum level
- enchanted-item value blocks with dynamic enchantment rows and NumberProvider
  levels
- time checks and location checks for dimension, biome, biome tag, structure,
  structure tag, smokey, light level, block tag and x/y/z position
- nested `and`, `or` and `not` condition value blocks

Every condition block is an A-type value block and uses the condition green.
Pool and entry conditions accept one `LootTableCondition` value directly; use
`All conditions` or `Any condition` to combine several conditions. The parent
**Conditions** category also contains `Not`, which negates one condition value.
`All conditions` and `Any condition` start with two empty value rows; `Not`
starts with one empty value row. They do not embed an example condition.

Item, damage, world, block and entity conditions are visible subcategories under
**Conditions**. The Block and Entity subcategories keep their native category
colors, while every individual condition block uses the condition green. The
**Loot table** category and pool blocks use deep green.

Same-level location checks are merged into one `minecraft:location_check`
predicate when their keys do not conflict, so dimension, biome, position and
block-tag tests become a single AND predicate. Item predicates are merged the
same way: same-level identity, durability, count and NBT tests become one
`minecraft:match_tool` or one `entity_properties.equipment.mainhand` predicate.

**Damage conditions** test the source damage with a vanilla damage tag.
Minecraft 1.21.1 has no exact damage-type predicate, so the exact damage-type
block is skipped with
`当前minecraft版本不支持这个条件，它将被跳过` and generates no condition.
**Killed by player or pets** expands to an `any_of` of the player-kill
condition and the `Tame:1b` entity NBT marker used by tamed mobs.

The **Minecraft components** category uses the native red colour and provides
the block state, block selector, entity/source dependencies, source damage and
empty text blocks. The **Item components** category uses the same red colour and
contains the item-only producers. The
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

Pools support `rolls`, `bonus_rolls`, one A-type condition value and weighted item
entries. The condition-free **Pool with rolls** block uses the same deep-green
colour. Three alternatives pool blocks provide the native `if` / `else if` /
`else` structure. The mutator panel exposes the optional `else if` and `else`
rows; the base `if` branch is part of the block body, `else if` rows can be added
or removed freely, and `else` is always last. They generate a
`minecraft:alternatives` entry and can nest any normal entry. Entries require an **Item selector** input, a
number count input, a weight, one condition value and a function chain. Two
additional entries call another loot table directly: the full variant includes
a condition value and a function chain, the compact variant includes neither. On 1.21+ the generated `minecraft:loot_table` entry uses
`value`; older generators receive `name`. The item input also accepts the
**Item with enchantment** value block and the four randomized enchantment item
variants. **Item with
enchantments** writes `minecraft:set_enchantments` with dynamic rows whose
levels accept a bare number or a number provider. The four other variants write
`minecraft:enchant_randomly` or `minecraft:enchant_with_levels`; their
enchantment list rows use MCreator's `enchantments` datalist. On 1.21+ the
treasure checkbox omits `options` when checked so treasure enchantments are
eligible, while an unchecked box restricts to `#minecraft:on_random_loot`.
Before 1.21 the same choice uses the legacy `treasure` boolean and the
`enchantments` list field. The list-restricted `enchant_with_levels` form cannot
enforce its list before 1.21.

The **Item components** category contains every A-1 item producer: the
enchanted-item variants, custom NBT and container contents, together with the
empty item selector value block. The three NBT blocks add boolean, number or
text SNBT to the item's `tag`. On 1.20.5+
they emit `minecraft:set_custom_data`; older generators receive
`minecraft:set_nbt`. NBT tag names are text inputs; the value socket accepts a
logic, number or text block. Number NBT accepts only a constant value because
the tag must be static. **Item with contents** is an expandable container block
with one item socket and one Number/NumberProvider count socket per row. Rows
default to an empty item selector and Integer 1, and both sockets are
right-aligned. It emits `minecraft:set_contents` with `type` on 1.18-1.20.4 and
with `component` from 1.20.5 onward; the function is skipped on 1.16-1.17,
where it did not yet exist. A floating-point literal count is rejected so the
generated container remains valid. **Potion effect** creates a potion, splash
potion, lingering potion or tipped arrow with a selected effect, amplifier and
duration. Amplifier and duration accept numbers or number providers but must
resolve to integer literals. Potion data is version-gated: numeric
`CustomPotionEffects` NBT through 1.20.1, namespaced `custom_potion_effects`
NBT on 1.20.2-1.20.4, and the `minecraft:potion_contents` component on 1.20.5+.
No base potion template is written, because vanilla concatenates the base
potion's effects with `custom_effects` and duplicates the effect. To keep a
readable item name, the producer writes a translated `item_name` component on
1.20.5+ (`item.minecraft.<item>.effect.<effect>`) whose value is an escaped
JSON string, or the equivalent
`display.Name` text component in the older NBT formats.

**Entity has potion (complex range)** adds duration and amplifier ranges on top
of the entity potion predicate. Bounds accept any Number/NumberProvider; a lower
bound of 0 omits `min`, and an upper bound of the Infinity block omits `max`.
The durability, enchantment-level and item-count range blocks use the same
0/Infinity omission rules. Exact durability and exact enchantment-level blocks
remain available alongside the ranges; the exact count block also remains. The
**Infinity** number block is rejected with a warning outside the range sockets
where it is meaningful.

**Item with custom name** has a simple text form and an advanced single-line JSON
text-component form. Both emit `minecraft:set_name`; the advanced field is
validated as JSON before generation. Item entries keep their normal colour, while
complex pools start without a sample condition or item; simple pools include one
simple item entry by default.

The three block-state condition blocks use the same input style as NBT
conditions. The property name is a text input block, the equality operator is
fixed, and the compared value is a typed input block: a boolean selector, an
integer value, or a text value such as `upper`. This keeps the generated
`block_state_property` values explicit and stable.

`Random chance` lives in the **Logic** category and uses the condition green.
The main-hand item block is in **Entity conditions**. Item producers live in
**Item components**; item-comparison blocks stay in **Item conditions**.

The count input accepts a bare number or a number provider. The **Numbers**
category contains the terminal `Integer` and `Float` blocks, the constant
`Integer provider` and `Float provider` blocks, and the `Uniform` and `Binomial`
provider blocks. `Uniform` and `Binomial` can be nested inside one another, and
their sockets accept either a bare number or a provider.

The **Scoreboard** subcategory uses the same number blue as **Numbers** and provides
three `minecraft:score` number providers: an entity score selected by the
entity value socket, a fixed player name, and a fixed UUID string. Each block
has a scoreboard objective name (default `scoreboard_name`) and a literal scale
value (default `1.0`). Because 1.21.1 reads the scale as a static float, a
provider scale is rejected with a warning.

Referencing another loot table through a nested-entry or loot-table-contents
block shows a localized warning: a direct or indirect self-reference can crash
the game or prevent the world from loading. The plugin cannot inspect other
files to prove a cycle, so the warning is advisory.

The **Functions** category, colour 280, contains `Add value` and five
loot-function blocks: `Add enchantment bonus` (uniform), `Add binomial
enchantment bonus`, `Apply ore bonus`, `Increase count by enchantment level`
and `Limit final count`. These append `minecraft:apply_bonus`,
`minecraft:enchanted_count_increase` and `minecraft:limit_count` functions to
the entry function chain. The three `apply_bonus` numeric parameters are
literal numbers; their sockets reject number providers with a warning. On
1.16-1.20.6 the count-increase block is emitted as the legacy
`minecraft:looting_enchant` function, which always uses Looting.

The compact item entry is a shorter B-shaped entry with item, count and weight
sockets. It is useful when no conditions or functions are needed.

The **Loot table contents** item component fills a shulker box or bundle from
another loot table. It emits `minecraft:set_contents` with a nested
`minecraft:loot_table` entry and defaults to a shulker box plus
`minecraft:chests/simple_dungeon`.

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
1.20.1, 1.21.1 and 1.21.8 are also included so the plugin follows the Fabric
generator plugin when it is available. Minecraft 26.x uses a different loot
table schema and must be handled by a separate `plugin-branch-26.x` source
branch.

The older generators use version-gated function and predicate fields. See
[`loot_table-1.16-1.19.4.md`](../dsh_docs/loot_table_format/loot_table-1.16-1.19.4.md)
for the complete 1.16-1.19.4 matrix. In brief: 1.16 has no
`set_enchantments` or `score`, 1.18 adds `set_contents` with `type`, 1.19
renames the location `feature` key to `structure`, and 1.19.4 replaces the old
damage booleans with damage tags. Nested loot-table entries use `name` before
1.21. The potion producer uses numeric `CustomPotionEffects` NBT before 1.20.2.

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
4. Build the pool/entry/condition value structure below the fixed start block.
5. Save. Invalid condition scopes are marked in the Blockly workspace, shown in
   the compile notes panel, and block saving and generation.

The context dependencies shown in the editor are bold, slightly larger and
colour-coded:

- Block: `blockstate` (yellow), `target entity` (teal, player breaker only),
  `source entity` (teal), `world` (earth yellow)
- Entity: `entity` (teal), `source entity` (teal), `world` (earth yellow),
  `damage source` (pink)

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
- A potion or tipped-arrow item with fire resistance at amplifier 10 shows one
  fire-resistance effect, not a base effect plus a custom effect, and its hover
  name is translated instead of "Uncraftable Potion".
- The exact enchantment-level and exact durability conditions emit matching
  `levels.min`/`levels.max` and durability bounds.
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
- Pool, entry and function blocks connect only when their statement types
  match. Conditions are A-type value blocks and connect to the single condition
  socket on a pool or entry.
- Condition-aware pool variants can be dragged from the toolbox and around the
  workspace; the drag preview keeps their condition and entry inputs.
- Opening the condition-pool mutator shows `else if` and `else` rows rather than
  a duplicate of the base `if` branch; `else if` can be added/removed and `else`
  stays at the bottom.
- The block selector displays only the selector field, without a
  `Block to test` prefix.
- Entity and item blocks use the native entity and item colors; all A-type
  condition blocks use the condition green; NBT value blocks use the native
  logic, math and text hues.
- The **Conditions**, **Minecraft components**, **Item components** and
  **Loot table** categories render their blocks without overlap or empty
  entries. Block and Entity condition subcategories are visible again.
- The Conditions root contains `All conditions`, `Any condition` and `Not`.
  `All conditions` and `Any condition` start with two empty value rows and add or
  remove rows dynamically; `Not` starts with one empty row. The Logic category
  contains only `random chance` and the true/false selector.
- The Block conditions category contains the provided-blockstate identity check:
  both inputs default to the current block, and a different second block emits
  an `all_of` requiring both block-state predicates.
- The Scoreboard providers use the Numbers blue and produce `minecraft:score`;
  the entity variant uses a context target, while the player-name and UUID
  variants use fixed names.
- Referencing another loot table shows the circular-reference warning in the
  active MCreator language.
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
registry lists rather than per-item compatibility filtering. Nested loot-table
entries with conditions in a Block table are evaluated against the breaking
player (`this`); entity-specific nested tables can therefore be skipped when the
player entity is absent or does not satisfy the condition. The generated 1.21
entry uses the version-correct `value` field.
