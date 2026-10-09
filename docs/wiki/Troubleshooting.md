[Home](Home.md) · [Getting started](Getting-Started.md) · [Aquariums](Aquariums-and-Swim-Tubes.md) · [Terrariums](Terrariums-and-Pipes.md) · [Mobitat](Mobitat-and-Mob-Nets.md) · [Decorating](Decorating-and-Editor.md) · [Fish Editor](Tropical-Fish-Editor.md) · [Recipes](Crafting-Recipes.md) · [Configuration](Configuration-and-Limits.md) · [Troubleshooting](Troubleshooting.md)

---

# Troubleshooting and Current Limitations

This page distinguishes **implemented behavior** from **requested or unverified enhancements** in the current `main` branch (0.7.1).

## I cannot insert another fish or mob

Count only **tank blocks**, not tubes/pipes. Default capacity is one inhabitant per tank. Check that the network connects through **faces**, is fully loaded, and has fewer than 4,096 modules. Check that a terrarium's **passive/hostile** type matches the mob category.

## My flying mob will not enter a terrarium

The current implementation requires a **five-block-high tank chamber**. A flying pipe opening must be on **level four or above**, and the destination chamber must also be tall enough. Pipes themselves don't count towards chamber height.

## The Mobitat won't accept a different mob

A Mobitat stores **up to five of one entity type**. Empty it completely before switching types; variants of one type may be stored together.

## The output slot seems blocked

The editor avoids overwriting an occupied **Out** slot. Take any existing item out before trying another creature/decor retrieval.

## A modded creature crashes or is invisible in the Mobitat/Fish Editor

Version 0.7.1 assigns synthetic IDs to render-only previews and guards incompatible third-party renderer failures. If a third-party renderer still has problems, identify the affected mod and file a reproducible issue with a log. The mod does **not** promise universal modded entity compatibility.

## Decorations stick outside the glass

The current implementation allows decor positions to extend roughly **one block beyond** enclosure limits and renders items/blocks as display entities. Perfect glass-edge clipping/masking has **not** been established. Reduce size or move an object inward when needed.

## A mob gets stuck against decoration

The code checks decoration collision bounds and abandons a blocked movement target. A more deliberate **back away/choose a new X or Z direction for two steps** behavior has been requested, but should **not** be documented as shipped without a subsequent implementation check. Try moving the decoration or leaving more room around it.

## Where is the full 3D tank editor preview?

The current screen contains a **selected-decoration/item preview and angle controls**, not a live no-mobs 3D render of the entire tank. A full-scene editing preview remains an enhancement request.

## Where are the wiki, changelog and tests?

- [Wiki index](Home.md)
- [Changelog](../../CHANGELOG.md)
- [Testing notes](../../TESTING.md)
- [GitHub Issues](https://github.com/CaszGamerMD/Caszual_Aquariums/issues)

When reporting a bug, include Minecraft/Fabric/mod versions, reproduction steps, and the relevant `latest.log` or crash report.
