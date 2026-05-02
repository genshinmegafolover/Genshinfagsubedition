# Morphscale MVP Plan

## Summary
- Build a Purpur/Paper plugin named `Morphscale` with command `/morphscale` and permission `morphscale.admin`.
- Create a plugin-managed flat world `morphscale_dimension` that behaves as the new dimension for gameplay.
- Add a 3x3x1 water-pool portal with particles, usable both ways.
- Add two brewed potions: shrinking and growing, with three size stages: `small` = 0.5, `normal` = 1.0, `large` = 2.0.

## Key Changes
- Rename the template package/plugin metadata to `dev.local.morphscale.MorphscalePlugin`, `pluginId=morphscale`, `pluginCommand=morphscale`.
- On plugin enable, create/load `morphscale_dimension` using a custom flat chunk generator: simple grass/dirt plane, no structures, fixed safe spawn.
- Portal behavior:
  - Detect valid 3x3 water source pools one block deep.
  - Keep water in place and show portal particles.
  - Entering from the main world teleports to the flat dimension.
  - Entering from the flat dimension returns to the player’s last origin portal, or main-world spawn if none is known.
- Potion recipes:
  - Awkward Potion + `AMETHYST_SHARD` = shrinking potion.
  - Awkward Potion + `SLIME_BALL` = growing potion.
  - Potions are tagged with plugin persistent data and handled on consume.

## Size Behavior
- Potion steps are clamped across `small -> normal -> large`.
  - Growing: small to normal, normal to large, large refreshes large.
  - Shrinking: large to normal, normal to small, small refreshes small.
- In the normal world:
  - Drinking a size potion starts a 3-minute unstable effect.
  - Player gets nausea/headache visuals.
  - Scale jitters every 5-10 seconds by tenths around normal, then returns.
  - If the player enters `morphscale_dimension` during this window, the target size stabilizes for 15 minutes.
- In `morphscale_dimension`:
  - Drinking applies the target size immediately for 15 minutes.
- When any stable effect ends, player returns to `normal` size.

## Attribute Profile
Use transient attribute modifiers so effects can be removed cleanly.

- `small`: `scale=0.5`, movement speed +25%, jump strength +15%, gravity -15%, step height -30%, block/entity reach -35%, attack damage -35%, attack speed +25%, block break speed +15%, safe fall distance +25%, fall damage multiplier -25%, max health -20%.
- `normal`: remove Morphscale modifiers.
- `large`: `scale=2.0`, movement speed -20%, jump strength -5%, gravity +10%, step height +100%, block/entity reach +60%, attack damage +75%, attack speed -35%, attack knockback +0.75, knockback resistance +0.35, block break speed +25%, safe fall distance +50%, fall damage multiplier +25%, max health +50%.

## Commands
- `/morphscale reload` reloads config.
- `/morphscale doctor` shows plugin/server/world status.
- `/morphscale size set <small|normal|large> [player]` immediately sets debug size.
- `/morphscale size reset [player]` returns to normal.
- `/morphscale potion give <grow|shrink> [player] [amount]` gives test potions.
- `/morphscale world tp [player]` teleports to the flat dimension for testing.

## Test Plan
- Run `.\gradlew.bat build`.
- Start/copy into the local Purpur server and confirm `/morphscale doctor`.
- Brew both potions and verify vanilla potion recipes are not overwritten.
- Build a 3x3x1 water portal, confirm particles, teleport to flat world, and return.
- Drink potion in main world: nausea plus unstable scale jitter for 3 minutes, then reset.
- Drink potion in main world and enter dimension before expiry: stabilize to target size for 15 minutes.
- Drink potion inside dimension: immediate stable size for 15 minutes.
- Verify all debug commands and that disabling/reloading removes Morphscale modifiers.

## Assumptions
- “New dimension” means a plugin-managed Bukkit/Paper world, not a datapack dimension.
- Portal visual is water plus particles, not end-portal blocks.
- Effect expiry always returns the player to normal size.
- Command is stored lowercase as `/morphscale`; players can think of it as Morphscale.
