# Linked Aquariums — Fabric Minecraft 26.2

Version 0.4.2 (plain glass and deterministic pipe-water fix).

## Install

Use Minecraft **Java Edition 26.2**, Fabric Loader **0.19.5 or newer**, and Fabric API **0.158.0+26.2 or newer for 26.2**. Put `linked-aquariums-0.4.2.jar` in your instance's `mods` folder. Multiplayer requires the mod and Fabric API on both the server and clients.

## Build your aquarium

Place Aquarium blocks next to each other, including vertically. Every block is already filled with contained water. Shared walls open automatically; external walls remain glass. Find the enclosure blocks in the Functional Blocks creative tab. Aquarium icons show water, passive terrariums show grass, and hostile terrariums show rocks behind purple glass. Matching pipe icons are three quarters of a block tall.

Dark outer frames and glass textures join across neighboring modules, and adjacent floor sections reach the full block edges. Tube water is drawn inside the glass and fills the entire pipe. Breaking either module leaves air; contained water does not spill into the world.

Each connected Aquarium block supports **one inhabitant by default**. Swim Tubes connect tanks and allow fish to travel between them, but contribute **zero** capacity. Example: four tank blocks connected by twelve tubes still hold four inhabitants.

Connections use shared faces, not corners. Tubes automatically branch in any of the six directions. Glass rings seal the connection between the smaller pipe and the larger tank face.

## Controls

- Right-click a tank with a cod, salmon, tropical fish, or pufferfish bucket to add a real fish. Tropical fish variants and bucket data are preserved.
- Right-click a tank or tube with an empty bucket or water bucket to retrieve the nearest fish in its connected network.
- Right-click a tank with an empty hand to open its management UI. Switch between Decor and Mobs pages. On Decor, click a tank cell on the top-down grid to choose exactly where an item goes; on Mobs, use the arrows to select a network inhabitant.
- Right-click a bottom-layer tank with sand, red sand, gravel, or any of the sixteen concrete powder colors to add a floor. Powder stays decorative and does not become concrete.
- Right-click a tank with any placeable block item or a trident to add decoration. Each tank block stores one floor material and one decoration. Aquarium and Swim Tube items place modules rather than becoming decor.
- Sneak-right-click with an empty hand to remove decoration first, then floor material. Replacing a floor or decoration returns the previous item.

Every placeable block item is accepted, including modded pottable plants and blocks with item models. Generic decorations use a miniature rendering of their item; they do not become functional world blocks. Cactus, wither rose, fire-related blocks, and all other decorations cannot damage inhabitants, grow, or tick. Rendering supplied by other mods still needs compatibility testing.

Stone buttons and polished blackstone buttons each have four rock arrangements. All twelve vanilla wooden fence types become logs with four shapes: single log, stacked logs, branches, and a hollow log. Tridents have four poses. The UI rotates decorations in 45-degree steps and moves them across the selected module's floor. Its layout diagram indicates position and direction rather than showing a 3D preview. Other block decorations retain their own item model; the Model button only changes the special models.

Chest, trapped chest, and ender chest items become a small decorative chest. It occasionally opens for four seconds, emits bubbles when opening, then closes. The Model button alternates between two wooden styles. Decorative chests have no storage inventory.

The Decor grid shows one horizontal layer at a time, with north at the top. Use +/− to switch layers, arrow buttons below the grid to pan, and Home to return to the block you opened. Gold marks the selected cell; a white inner border marks the opened block. Pipes appear as connectors and cannot hold decor. Tan strips mark floors, pale center squares mark decorations, and blue strips mark terrarium water patches. Hover a cell to see its world coordinates.

In the UI, put your floor, decoration, or creature source in In and press Place/Add. Returned items appear in Out. Empty Output before the next insertion/removal. On the Mobs page, put an empty bucket or water bucket in Input and press Remove to retrieve the selected network inhabitant. Closing the UI returns remaining input/output items to your inventory or drops them nearby if necessary.

## Additional inhabitants

- Supported fish spawn eggs add fish directly inside a tank, subject to capacity. Non-aquatic eggs are rejected.
- An axolotl bucket or axolotl spawn egg adds an axolotl. Its aquarium controller disables hunting, so it does not eat fish.
- An ink sac adds a miniature squid; a glow ink sac adds a miniature glow squid.
- A turtle scute adds a baby turtle whose age is held at baby stage.
- Placing a trident has a 2% chance to add a miniature drowned, provided the network has capacity. It is peaceful and moves around the tank floor.

All supported inhabitants use one capacity slot each. Squid, turtles, and drowned are retrieved into an Aquarium Creature Bucket, preserving their entity data so they can be added again. Fish and axolotls use their normal buckets. Aquarium Creature Buckets are obtained through retrieval, not crafted.

Fish remain real Minecraft entities. A tank swimming controller guides them through connected cells instead of allowing vanilla AI to strand them against glass. Pufferfish stay deflated inside the aquarium.

## Passive and hostile terrariums

Both land enclosure types join only their own tank blocks and matching pipes. Aquariums, passive terrariums, and hostile terrariums remain three separate networks, even if their glass touches. Each tank block adds one resident slot by default; all pipes add zero. Terrariums contain air. Hostile glass has a subtle purple tint, and hostile residents are protected against sunlight burning.

Craft a Mob Net using four string in a 2x2 square at the upper right and a stick at the lower left. Right-click a supported land mob with an empty net to catch it. A filled net holds one mob, preserving its name, equipment, variants, age, and saved entity data. Vanilla bosses, fully aquatic mobs, and mounted mobs are excluded. Right-click the appropriate terrarium with the filled net to transfer the mob; the net becomes empty and reusable. A refused transfer keeps the mob in the net.

Mobs in the Minecraft `MONSTER` spawn category use hostile terrariums; other supported mobs use passive terrariums. This means neutral monsters such as endermen and zombified piglins still use hostile terrariums. Captured residents have their base scale set to **one third of their original size**. Their attack AI is disabled, creeper fuses are neutralized, and the enclosure controller moves them through valid cells. Right-click with an empty net to retrieve the nearest resident, or use the UI's Mobs page to select a particular resident across the network. On that page, Add uses a filled net in Input; Remove uses an empty net and puts the filled net in Output. Empty Output before the next operation.

To release a mob back into the world, use the filled net on an ordinary block with enough empty space on the clicked side. Original AI, gravity, and base size are restored. Mob needs a full 2 F‛2x footprint to spawn safely.

## Recipes

The seven craftable items are the three enclosures, three matching pipes, and the Mob Net. Aquariums use the vanilla ambient water sound. Terrariums are silent.

## Save data

Aquariums use a block entity to store floors, decorations, decoration layout, chest animation state, and prior rock migration state. Real fish stay as world entities. Terrarium residents are real entities with their preserved data while they are in the world. The Mob Net and Aquarium Creature Bucket store one entity's data in the item component link%d_aquariums:entity_dat``.

## Rendering

The analytic visual design uses continuous dark edging only on the outside of each connected structure. Shared walls are open. Floor tops reach the full block edges while the side faces sit inside the dark frame. Swim Tube water fills the contained interior of the pipe and the open endpoints that join tanks. The custom fluid itself has an invisible default sprite so full-block vanilla fluid faces cannot clip through the tube walls; the visible water is provided by the bounded tube meshes.

## Glass appearance in 0.4.2

All diagonal glass markings are removed. Normal enclosure and pipe glass uses #dbdbdb; hostile glass uses #58307a. Both use 5% opacity (13/255, the nearest PNG alpha value). The dark outside edging remains. Tube glass and water are separate, always-enabled model layers. Floor tops remain seamless; their side faces are inset behind the edging.
