[Home](Home.md) · [Getting started](Getting-Started.md) · [Aquariums](Aquariums-and-Swim-Tubes.md) · [Terrariums](Terrariums-and-Pipes.md) · [Mobitat](Mobitat-and-Mob-Nets.md) · [Decorating](Decorating-and-Editor.md) · [Fish Editor](Tropical-Fish-Editor.md) · [Recipes](Crafting-Recipes.md) · [Configuration](Configuration-and-Limits.md) · [Troubleshooting](Troubleshooting.md)

---

# Mobitat and Mob Nets

## Mob Net

A **Mob Net** is a reusable capture tool made from four string and one stick. Use an **empty net on a supported mob** to store it, then use the filled net to add it to a matching terrarium, put it in a Mobitat, or release it on an ordinary block. A successful terrarium transfer/release returns an empty net.

Captured mobs retain their individual saved data, including names, variants, age and equipment. Mob Nets are not the normal way to retrieve aquarium inhabitants; **buckets** are used in the Aquarium editor.

## Mobitat: pocket-size mob storage

The **Mobitat** is a placeable, portable block that stores **up to five inhabitants of exactly one mob type**. Mix variants of that type, but not different entity types. It is crafted using a chest, glass and iron nuggets ([recipe](Crafting-Recipes.md)).

Its model has **four small legs, a front door and a carry handle**. When placed, its saved residents appear as animated miniature previews at approximately **10% scale**. **Since 0.8.5, the miniatures slowly roam freely inside the block**, with movement scaled to match their tiny appearance. Ground mobs walk along the interior floor; flying and swimming residents can drift vertically. Residents turn and animate along their actual path rather than twitching in fixed positions. Their movement is only visual and never changes the stored mobs or uses world entity AI. Their entity variants are preserved in preview.

## Using a placed Mobitat

- **Right-click with a filled Mob Net:** add that mob if there is room and it matches the stored type.
- **Right-click with an empty Mob Net:** remove one stored resident into a filled net.
- **Right-click empty-handed:** open the Mobitat interface.
- To **rename** a specific resident, supply a **renamed Name Tag** in the interface and press the corresponding **Name** button.

Mobitats can also carry supported aquarium creature types where mob-net capture is possible.

## Using a Mobitat as an item

- **Use a filled Mobitat on a compatible enclosure:** transfer stored inhabitants into that habitat, respecting capacity and type.
- **Use it against another suitable world block:** attempt to release stored mobs into the world.
- **Sneak-use when placing it:** place the Mobitat without emptying its stored contents.
- **Break a placed Mobitat:** recover the item with its inhabitants; the mod also handles Creative break retention for filled Mobitats.

If the receiving habitat cannot accept a mob (wrong type, full, or unsuitable), that resident stays stored. For a given Mobitat, all five slots share one species/type.

## Common questions

**Can I carry an entire terrarium in one item?** Not its blocks, but up to five same-type residents.

**Can I rename a resident inside?** Yes, with an already-renamed Name Tag.

**Do variants get replaced by generic mobs?** Stored mob data and preview rendering are designed to retain variants. Other mods' custom entity renderers can still have compatibility issues.

More: [Terrariums](Terrariums-and-Pipes.md) · [Recipes](Crafting-Recipes.md)
