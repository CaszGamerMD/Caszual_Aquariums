[Home](Home.md) · [Getting started](Getting-Started.md) · [Aquariums](Aquariums-and-Swim-Tubes.md) · [Terrariums](Terrariums-and-Pipes.md) · [Mobitat](Mobitat-and-Mob-Nets.md) · [Decorating](Decorating-and-Editor.md) · [Fish Editor](Tropical-Fish-Editor.md) · [Recipes](Crafting-Recipes.md) · [Configuration](Configuration-and-Limits.md) · [Troubleshooting](Troubleshooting.md)

---

# Configuration, Capacity and Compatibility

## Requirements

| Component | Minimum |
|---|---|
| Minecraft | **Java Edition 26.2** |
| Fabric Loader | **0.19.5** |
| Fabric API | **0.158.0+26.2** |
| Java | **25** |

Install on **both client and server** for multiplayer. This is not a client-only cosmetic mod.

## Enclosure capacity

After first start, edit `config/linked-aquariums.properties`:

```properties
fish-per-tank-block=1
```

Values are **1–16**; restart the game/server after changing. The setting controls the number of residents per **tank** block in linked networks, including terrariums despite its historical `fish-` name. **Pipes and tubes count as zero**.

Examples with `fish-per-tank-block=1`:

- 1 tank + any number of connecting pipes = **1** resident.
- 5 connected tanks + 20 pipes = **5** residents.
- 10 connected tanks = **10** residents.

The mod scans networks of up to **4,096 modules**. An unloaded chunk or an over-limit network makes the scan incomplete and prevents adding new residents until the entire relevant network is loaded and within the limit. Population enforcement also pauses for incomplete networks.

## World and data behavior

- Aquarium water is self-contained and does not spill when modules are removed.
- Passive/hostile terrariums remain dry except for optional shallow **decorative** ground water.
- Tank connections form only between compatible modules sharing a **face**.
- Saved fish bucket variants, captured mob NBT, floor choices and individual decorations are preserved by their respective systems.
- When a completed network has too little room after a break or split, excess creatures are dropped as filled buckets or nets. Pick them up before despawn.
- Mobitat items preserve their stored inhabitants across placement/pickup.

## Known compatibility boundaries

- Aquarium inhabitant types are limited to the explicitly supported vanilla types in the mod's `Inhabitants` logic.
- Land terrariums can accept additional supported modded mob entities, but their special renderers or behavior are **not guaranteed** to be compatible.
- Generic modded block item decorations may render differently depending on the other mod.
- Source-side GameTests cover several behaviors, but custom shaders/alternate rendering pipelines still need real gameplay verification.

For build and tests see [TESTING.md](../../TESTING.md). For symptoms and workarounds see [Troubleshooting](Troubleshooting.md).
