[Home](Home.md) · [Getting started](Getting-Started.md) · [Aquariums](Aquariums-and-Swim-Tubes.md) · [Terrariums](Terrariums-and-Pipes.md) · [Mobitat](Mobitat-and-Mob-Nets.md) · [Decorating](Decorating-and-Editor.md) · [Fish Editor](Tropical-Fish-Editor.md) · [Recipes](Crafting-Recipes.md) · [Configuration](Configuration-and-Limits.md) · [Troubleshooting](Troubleshooting.md)

---

# Aquariums and Swim Tubes

## Connecting tanks

An **Aquarium** is a one-block, already-water-filled glass habitat. Adjacent tank blocks merge into a larger chamber when they share a face; external faces stay closed. The frame/contained-water visuals adapt to neighbors. Connected horizontal floors appear continuous. Breaking enclosure components does **not** dump ordinary water into the world.

**Swim Tubes** connect to tanks and one another, can branch in all six directions, and carry aquatic residents between parts of the network. Water stays inside the tube geometry. Tubes contribute **no capacity**; only Aquarium blocks count. Aquarium, passive terrarium and hostile terrarium networks never merge.

## Add inhabitants

Right-click an **Aquarium tank block** with the relevant item.

| Item or method | Resident | Notes |
|---|---|---|
| Cod Bucket | Cod | Regular Minecraft fish |
| Salmon Bucket | Salmon | Regular Minecraft fish |
| Tropical Fish Bucket | Tropical fish | Preserves variant and bucket data |
| Pufferfish Bucket | Pufferfish | Kept deflated in the tank |
| Axolotl Bucket | Axolotl | Managed behavior prevents hunting other tank fish |
| Supported aquatic spawn egg | Corresponding creature | Only eligible supported types; drowned eggs are excluded |
| Ink Sac | Small squid | Reduced-size visual |
| Glow Ink Sac | Small glow squid | Reduced-size visual |
| Turtle Scute | Baby turtle | Kept in the baby stage |
| Trident placed as aquarium decor | Rare miniature drowned | **2% chance**, only if the network has space |
| Filled Aquarium Creature Bucket | Stored special creature | Carries saved creature data from retrieval |

All supported aquarium residents occupy **one slot each**, including special inhabitants.

## How creatures move

Inhabitants are real saved entities whose AI is controlled inside the aquarium. They wander among connected modules and swim through tubes using varied positions/lanes rather than always taking one central track. Decorative objects are treated as obstacles by the movement controller. Some very dense layouts may still need adjustment if a resident seems blocked; see [Troubleshooting](Troubleshooting.md).

## Remove inhabitants

- With an **empty or water bucket**, right-click an Aquarium or tube to retrieve a nearby network inhabitant.
- To choose a specific creature, open the **Mobs** editor page, select it, put the correct empty bucket into **Input**, and click **Pick up selected**. Take the filled result from **Out**.
- Fish and axolotls use regular bucket forms. Squid, turtles and drowned use the mod's **Aquarium Creature Bucket**, obtainable by retrieval (not crafting).

If a structure is broken/split and no longer holds its previous population, overflow creatures are returned as dropped filled containers. Pick them up before normal item despawn.

## Capacity example

`4 Aquarium blocks + 12 Swim Tubes = 4 residents` with the default `fish-per-tank-block=1` setting. A server can increase the per-tank capacity to 16 in [Configuration](Configuration-and-Limits.md).

See [Decorating and Editor](Decorating-and-Editor.md) for floors, decorative blocks and the network management interface.

## Squid size (0.8.1+)

Ink Sacs and Glow Ink Sacs place squid and glow squid as managed aquarium residents. Both are rendered at **25% of their original scale** while inside the aquarium. Their previous size is restored when retrieved from the tank.

## Sealed water effects (0.8.6+)

Tank and swim-tube water is still a **real full-height contained fluid** so aquarium inhabitants can breathe, navigate, and swim through pipes. This update disables unwanted vanilla **drip particles** below sealed pipes and stops nearby outside mobs from making **false water-entry splashes** when their collision box briefly overlaps a pipe or tank. Genuine open water still behaves normally, and the fish/water simulation is unchanged.
