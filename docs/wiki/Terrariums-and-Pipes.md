[Home](Home.md) · [Getting started](Getting-Started.md) · [Aquariums](Aquariums-and-Swim-Tubes.md) · [Terrariums](Terrariums-and-Pipes.md) · [Mobitat](Mobitat-and-Mob-Nets.md) · [Decorating](Decorating-and-Editor.md) · [Fish Editor](Tropical-Fish-Editor.md) · [Recipes](Crafting-Recipes.md) · [Configuration](Configuration-and-Limits.md) · [Troubleshooting](Troubleshooting.md)

---

# Terrariums and Pipes

## Two distinct habitats

| Feature | Passive Terrarium | Hostile Terrarium |
|---|---|---|
| Intended inhabitants | Supported non-monster mobs | Minecraft `MONSTER` category mobs, including some neutral mobs |
| Matching connector | Passive Terrarium Pipe | Hostile Terrarium Pipe |
| Interior | Dry land | Dry land |
| Glass | Neutral, very translucent | Subtle purple tint (`#58307a`) |
| Sunlight | Normal enclosure | Managed inhabitants protected from sunlight burning |

These are separate networks. They do not join aquariums or the opposite type of terrarium. Like aquariums, terrarium **tank blocks provide capacity**, and matching **pipes provide zero**. They connect on shared faces rather than diagonally.

## Capture and transfer

1. Craft a [Mob Net](Mobitat-and-Mob-Nets.md).
2. Right-click a supported mob with an **empty** Mob Net to capture it.
3. Use the **filled** Mob Net on the correct terrarium tank.
4. Right-click a terrarium with an **empty** net to retrieve a nearby resident, or open the **Mobs** editor page to select one.
5. Right-click an ordinary world block with the filled net to release the mob on the adjacent side if there is enough space.

Mob Nets preserve saved mob details such as names, age, variants and equipment. Bosses, fully aquatic exclusions and mobs carrying/riding other mobs are not supported. Monster-category neutrals (for example endermen and zombified piglins) use **hostile** tanks.

## Size, behavior and movement

Terrarium residents render at **75% of their normal base scale** while managed by the enclosure. Their attacks and normal AI are disabled; the controller moves them within permitted tanks/pipes. The original size and AI flags are restored on release.

**Ground mobs:** Walk on the chamber's **lowest tank layer**. They can enter horizontal pipes at that level, but cannot climb through vertical pipe paths or use raised ground links.

**Flying mobs:** Need a connected tank chamber **at least five tank blocks high**. They can move throughout that chamber and enter/exit pipe connections only at level **4 or higher** (counting the lowest tank as level 1). Flying connections may extend vertically if the destination chamber also qualifies.

Common supported flying residents include parrots, bees, bats, allays, phantoms, ghasts, happy ghasts, vexes and blazes. For modded flying entities there is a `linked_aquariums:flying` entity-type tag, but third-party behavior may require compatibility testing.

## Substrate and small water patches

Decorate the lower floor with sand, red sand, gravel or concrete powder. To add a contained **shallow ground-water patch**, use a Water Bucket on a lowest-level terrarium tank; use an empty bucket to remove it. This is a decorative patch, not a full flooding water source. The editor supports other safe decorative blocks and items as well.

## Rescuing residents

When tanks are removed, populations overflow, or a flying chamber becomes too short, the mod attempts to return affected inhabitants as **dropped filled Mob Nets**. Collect them before normal item despawn.

More: [Mobitat](Mobitat-and-Mob-Nets.md) · [Decorations](Decorating-and-Editor.md)
