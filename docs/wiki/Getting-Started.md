[Home](Home.md) · [Getting started](Getting-Started.md) · [Aquariums](Aquariums-and-Swim-Tubes.md) · [Terrariums](Terrariums-and-Pipes.md) · [Mobitat](Mobitat-and-Mob-Nets.md) · [Decorating](Decorating-and-Editor.md) · [Fish Editor](Tropical-Fish-Editor.md) · [Recipes](Crafting-Recipes.md) · [Configuration](Configuration-and-Limits.md) · [Troubleshooting](Troubleshooting.md)

---

# Getting Started

## Installation

1. Use **Minecraft Java Edition 26.2**.
2. Install **Fabric Loader 0.19.5 or newer**, **Fabric API 0.158.0+26.2 or newer**, and **Java 25 or newer**.
3. Install the matching **`linked-aquariums-0.7.1.jar`** release/build in your `mods` folder. Do not install a `-sources.jar` file or keep old versions alongside it.
4. For multiplayer, both the **server and each client** must have the mod and Fabric API installed.
5. Find its blocks/items under **Functional Blocks** in the creative inventory.

For developers: clone the [repository](../../), run `./gradlew build` (Linux/macOS) or `gradlew.bat build` (Windows), then use `build/libs/linked-aquariums-0.7.1.jar`.

## Your first aquarium

1. Craft or obtain **Aquarium** blocks; each one comes with its own contained water.
2. Place Aquarium blocks touching one another on a **face** (not only at a corner). Shared glass faces open to make one habitat, including vertically.
3. Attach **Swim Tubes** to extend routes. Tanks and tubes connect in all six directions.
4. Use a **cod, salmon, tropical fish, pufferfish or axolotl bucket** on an Aquarium tank to add a resident, or other supported sources described in the [aquarium guide](Aquariums-and-Swim-Tubes.md).
5. With an empty hand, **right-click a tank** to open the management editor. Use **Mobs** for inhabitant selection/retrieval and **Decorate** for display items.
6. Use an **empty or water bucket** to collect a creature; some special creatures come out in an **Aquarium Creature Bucket**.

**Capacity:** By default four linked Aquarium blocks have room for four creatures, however many tubes connect them.

## Your first terrarium

1. Craft either **Passive Terrarium** (oak-plank recipe) or **Hostile Terrarium** (iron-ingot recipe).
2. Build a connected chamber out of the same type. Only matching pipes connect to it.
3. Craft a **Mob Net** and right-click a supported mob to capture it.
4. Right-click the correct terrarium with the filled net to move the mob inside.
5. Retrieve the resident with an empty net, or select one under **Mobs** in the editor. Use the filled net against an ordinary block with room beside it to release the mob.

**Flying mobs need a continuous chamber at least five blocks tall.** Ground creatures use the lowest tank level.

## Optional workstations

- **Mobitat:** Holds five captured mobs of the same type and can be carried, placed or used to transfer mobs.
- **Tropical Fish Editor:** Insert a Tropical Fish Bucket, customize pattern and color, then remove with an empty bucket.

Next: [Aquariums](Aquariums-and-Swim-Tubes.md) · [Terrariums](Terrariums-and-Pipes.md) · [Recipes](Crafting-Recipes.md)
