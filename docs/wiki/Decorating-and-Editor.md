[Home](Home.md) · [Getting started](Getting-Started.md) · [Aquariums](Aquariums-and-Swim-Tubes.md) · [Terrariums](Terrariums-and-Pipes.md) · [Mobitat](Mobitat-and-Mob-Nets.md) · [Decorating](Decorating-and-Editor.md) · [Fish Editor](Tropical-Fish-Editor.md) · [Recipes](Crafting-Recipes.md) · [Configuration](Configuration-and-Limits.md) · [Troubleshooting](Troubleshooting.md)

---

# Decorating and the Enclosure Editor

## Open the editor

**Right-click a tank with an empty hand.** The current editor starts with **Mobs** and **Decorate** choices and shares its controls across aquarium and terrarium habitats.

### Mobs

- View total current residents and network capacity.
- Choose a resident from the list; use page arrows if there are **more than five**.
- Place the appropriate empty retrieval container into **Input**: a bucket for aquatic creatures, or an empty Mob Net for terrarium mobs.
- Press **Pick up selected** and collect the result from **Out**. Empty Out before another operation.

### Decorate

- Insert an item in **Input** and click **Add item**.
- Choose an attachment anchor: **Ceiling**, **Body**, or **Floor**.
- Select an existing decoration from the numbered/pageable list, then **Remove** to return it to Out.
- Adjust **X/Y/Z position**, **X/Y/Z rotation**, and **scale** for the selected decoration. **Reset** restores the default position, orientation and size for its anchor.
- Position buttons move by **0.1 block**, rotation buttons change **15 degrees**, and scale controls move by **0.1** within **0.1×–4×**.
- Drag over the **preview area** or use angle buttons to adjust the viewing angle; the interface displays a selected item preview.

**Important:** In version 0.7.1 the editor's preview is **not yet a full 3D tank scene showing all decorations with the mobs hidden**. Its view is a guide/selected-item preview. Treat a full enclosure preview as a proposed enhancement, not a shipped feature.

## Placing decorations

You can add many individual decorations rather than only one per tank. Items and blocks are presented as visual displays. Where possible, block items use their **placed block model**, while non-block items use an item display. You can position them throughout the connected enclosure. Model faces extending beyond the interior of the joined tank volume are clipped away instead of appearing outside the glass.

To place a block/item directly, right-click an aquarium or terrarium with a supported decorative block item or trident. The editor offers finer manipulation. Decorations are **not active world blocks**: they don't grow, tick, damage inhabitants or act as usable inventories. The movement controller checks decoration bounds when moving creatures.

## Floors and special effects

For bottom-layer floors, right-click the tank with **sand, red sand, gravel or one of the sixteen concrete powder colors**. The powder stays decorative, without hardening. Connected floors share uninterrupted top surfaces.

On terrarium ground-level tanks, a Water Bucket adds a shallow decorative water patch; an empty bucket retrieves it.

Some early special decorations include stone/polished-blackstone buttons shown as rocks, wooden fences as log pieces, trident poses and miniature decorative chests. Existing legacy decorations are migrated to the newer display system where possible. Decorative chests are **not inventories**.

## Persistence and multiplayer

Each item's anchor, position, scale, rotations and source item are saved with the enclosure data. From 0.8.1, decorations are rendered as client-side clipped block/item model geometry; the saved data synchronizes to other players. Previously spawned legacy decoration display entities are automatically cleaned up. Breaking a tank returns its retained decor in normal survival loot paths.

**Rendering limits:** Standard baked block and item geometry is clipped to the tank interiors, including connected cells; special modded item renderers that bypass baked quads may not obey clipping. Check such unusual models manually. See [Troubleshooting](Troubleshooting.md).

Related: [Aquariums](Aquariums-and-Swim-Tubes.md) · [Terrariums](Terrariums-and-Pipes.md)

## 0.8.3 editor and clipping restoration

The editor uses the wider Mobs/Decorate screen with a live network preview, independent transform controls, and readable inventory slots. Decoration geometry is submitted from item baked quads, with every triangle clipped to the interior of the connected enclosure; normal Minecraft block and item graphics are visible when inside the tank, but discarded or cropped where outside the walls. Special-rendered modded items that cannot be reduced to baked quads can only be shown when fully contained.
