# Linked Aquariums — Fabric Minecraft 26.2

> **0.8.0:** New [Wearable Aquarium](docs/wiki/Wearable-Aquarium.md) for the Caszual Additions chest cosmetic slot (four fish maximum). The older 0.4.2 instructions below are retained as historical reference.

> **Current 0.7.1 documentation:** Visit the [Caszual Aquariums Wiki](docs/wiki/Home.md) for the up-to-date feature guides and all crafting recipes. The older version 0.4.2 text below is retained as historical reference.

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

To release a mob back into the world, use the filled net on an ordinary block with enough empty space on the clicked side. Original AI, gravity, and base size are restored. Mob nets cannot scoop aquarium residents; use buckets for those.

Right-click a lowest-level terrarium block with a water bucket to add a shallow, contained ground-water patch and receive an empty bucket. Use an empty bucket to take it back. The patch is decorative water rather than a full fluid block, keeping the land enclosure dry and preventing water from spilling when broken. The same bucket operation is available through Place on the Decor page. All existing floors, harmless block decorations, model variations, and layout controls work in terrariums.

Height is measured per connected **tank chamber**, excluding pipes. The lowest tank layer is level 1. Ground mobs walk on that lowest layer and can enter horizontal pipes attached there. They cannot climb vertical pipes or enter raised connections. Flying residents require a chamber **at least five blocks tall**. They can move throughout that chamber and enter/exit pipes only at level **4 or higher**. Flying pipes can route vertically. A connected destination chamber must also be tall enough. Parrots, bees, bats, allays, phantoms, ghasts, happy ghasts, vexes, and blazes are recognized as flying residents. Flying movement controls/navigation are also recognized. Other modded flying mobs can be added through the `linked_aquariums:flying` entity-type tag; compatibility with custom entity code is unverified.

If removing blocks strands a resident or leaves too little capacity, the resident becomes a dropped filled Mob Net. Birds are also returned to nets when the chamber they occupy is shortened below five blocks. Pick up these nets before normal dropped-item despawn.

Terrarium recipes produce four tanks from glass in the aquarium pattern, with oak planks along the bottom for passive tanks or iron ingots for hostile tanks. Matching pipe recipes produce eight pipes from three glass across the top, three across the bottom, and one matching terrarium tank in the center.

## Removing blocks

External faces reseal when connections are removed. If a fish is stranded in a removed module, it becomes a dropped fish bucket. If a smaller tank or split network has too many fish, excess fish become dropped fish buckets within one second. Pick those buckets up: normal dropped-item despawn rules still apply.

Floors and decorations drop with the tank in survival. Explosions use normal survival/explosion loot rules. Creative breaking uses normal Minecraft no-drop behavior.

## Capacity setting

After first launch, edit `config/linked-aquariums.properties`:

```properties
fish-per-tank-block=1
```

Values from 1 to 16 are accepted. Restart the game or server after changing the setting. Tubes always contribute zero. Capacity enforcement pauses for networks that reach unloaded chunks or exceed the 4096-module scan limit. New fish cannot be added until the entire network is loaded and within that limit.

## Recipes

Aquarium, four blocks:

```
Glass       Glass       Glass
Glass       Empty       Glass
Iron nugget Iron nugget Iron nugget
```

Swim Tube, six blocks: three glass across the top row and three across the bottom row, with an empty middle row.

## Build from source

Install JDK 25. From this folder run `gradlew.bat build` on Windows or `./gradlew build` on Linux/macOS. The output is `build/libs/linked-aquariums-0.4.2.jar`; do not install the `-sources.jar`.

Run server integration tests with `./gradlew runGameTest`. Run the client smoke test and screenshots with `./gradlew runClientGameTest` on a machine with a graphical display. The test source set is separate and is not included in the distributed mod. This task runs an isolated Minecraft test server, not an existing world.

Verification is recorded in TESTING.md. Compatibility with other mods still needs play-testing. Aquariums do not support custom creature types. Terrarium nets accept supported modded Mob entities, but their custom behavior and rendering need compatibility testing. Generic modded block decorations are accepted.

## Glass appearance in 0.4.2

All diagonal glass markings are removed. Normal enclosure and pipe glass uses #dbdbdb; hostile glass uses #58307a. Both use 5% opacity (13/255, the nearest PNG alpha value). The dark outside edging remains. Tube glass and water are separate, always-enabled model layers. Floor tops remain seamless; their side faces are inset behind the edging.
