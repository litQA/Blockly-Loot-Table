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
- `inverted`
- block-state boolean, integer and enum comparisons (Block loot tables only)
- entity potion-effect predicate (Entity loot tables only)
- block and entity NBT logic, number and text comparisons
- item enchantment condition with an item input (Entity or tool context)
- legacy `block_state_property` (parseable for existing workspaces; hidden from the toolbox)

The **minecraft components** category provides the provided-blockstate, block
selector, event/target entity, source entity, immediate source entity, source
player and entity main-hand item blocks. The provided blockstate resolves to the
block selected by the loot table element. Source entity is the indirect damage
source (`attacker`), immediate source entity is `direct_attacker`, and source
player is `attacking_player`. The main-hand item block takes an entity input and
can feed the enchantment condition.

The `loottable_start` block is hidden from the toolbox and cannot be deleted.
Each loot table has exactly one start block; extra start blocks are ignored with
a warning.

Pools support `rolls`, `bonus_rolls`, conditions and weighted item entries.
Entries support an item selector, a number count input, a weight, a condition
chain and a function chain. The count input accepts a bare number or a number
provider. The **Numbers** category contains the terminal `Integer` and `Float`
blocks, the constant `Integer provider` and `Float provider` blocks, the
`Uniform` and `Binomial` provider blocks, and the `Add value` function block.
`Uniform` and `Binomial` can be nested inside one another, and their sockets
accept either a bare number or a provider. `Add value` appends a
`minecraft:set_count` function with `"add": true`; the old `Set count uniform`
function is kept only for existing workspaces and is hidden from the toolbox.

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
2. Select a block/entity with **Select...**, or type a registry name. Leave the
   target empty to derive the path from the element name.
3. The path is generated automatically as
   `<target namespace>:<blocks|entities>/<target path>`. Check **Manual path** to
   disable the target selector and enter a path yourself.
4. Build the pool/entry/condition chain below the fixed start block.
5. Use the template selector for the two built-in examples.
6. Save. Invalid condition scopes are marked in the Blockly workspace, shown in
   the compile notes panel, and block saving and generation.

The context dependencies shown in the editor are:

- Block: `provided blockstate, block selector, source entity, immediate source entity, source player, world`
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

The template selector creates both documents automatically.

## Manual test checklist

- Plugin appears in MCreator 2026.1 and **Blockly Loot Table** is visible.
- Creating a new element opens the Blockly editor promptly, without toolbar
  paint exceptions or repeated WebView execution timeouts in `mcreator.log`.
- The element can be created, saved and reopened with its Blockly document.
- The start block is not in the toolbox and cannot be deleted.
- A Block table with `random_chance` and a diamond ore entry saves.
- A Block table with `survives_explosion` saves.
- A Block table with boolean/integer/enum property comparisons saves, and the
  generated condition names the selected block and property value.
- A Block table using **Provided blockstate** emits the element target block,
  not an empty or placeholder block id.
- An Entity table with `killed_by_player` saves.
- An Entity table with the potion condition and **Source entity** emits
  `minecraft:entity_properties`, `entity: "attacker"` and a non-empty
  `predicate.effects` map.
- Block and entity NBT logic/number/text conditions emit `location_check` and
  `entity_properties` with the expected SNBT value.
- An entity main-hand item enchantment condition emits the
  `equipment.mainhand` predicate with the selected enchantment and level.
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
- An Entity table containing `block_state_property` (or a block-state
  comparison) shows a Blockly warning, shows an error in the compile notes
  panel, and saving is blocked.
- A Block table containing `killed_by_player` behaves the same way.
- Build output places the JSON in `loot_tables/` before 1.21 and `loot_table/`
  from 1.21 onward.
- Empty, partially filled and malformed workspaces still produce a valid empty
  or reduced table without a build crash.
- The generated JSON loads in the target Minecraft version.

## Notes and limitations

See `MIGRATION-REPORT.md` for the full limitation list. The main restrictions in
this first phase are the condition subset, concrete item entries only (item tags
are skipped with a warning), and basic (not nested) predicate editors.
