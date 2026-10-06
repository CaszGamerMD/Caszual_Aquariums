## 0.6.1
- Redesigned Mobitat with four short legs, a front door, and a 3D carry handle.
- Placed Mobitats render their stored residents as animated 10% scale previews while preserving stored variants.
- Breaking a Mobitat in Creative now drops the Mobitat with its stored residents intact.

## 0.6.0
- Unified Aquarium/Terrarium editor with Mobs and Decorate pages.
- Removed the legacy custom-model decoration workflow.
- Added unlimited item/block decorations with Ceiling, Body, and Floor anchors.
- Added per-decoration movement, XYZ rotation, scaling, and one-block-outside enclosure bounds.
- Decorations use vanilla placed block models when possible and act as movement obstacles for residents.
- Aquarium residents remain 100% scale; terrarium residents use 75% scale with faster natural walking animation.
- Mobs page supports selecting residents and retrieving them with the appropriate container.

## 0.5.0
- Added Mobitat: portable storage for up to five mobs of one type.
- Mob Nets can insert/extract Mobitat residents, including supported aquarium creatures.
- Filled Mobitats retain residents when placed or picked up; sneak-use places without unloading.
- Use a held Mobitat on compatible enclosures to transfer residents, or elsewhere to release them.
- Added Name Tag resident naming UI for placed Mobitats.
- Added varied aquarium/tube swim lanes and framed tank-to-tube mounts.

# 0.4.2

- Fixed the cause of position-dependent missing pipe water: multipart `apply` arrays were choosing glass OR water randomly. Glass, water and edging now use independent, simultaneous multipart entries for all 64 connection shapes.
- Removed every diagonal glass marking. All normal glass uses #dbdbdb and hostile glass uses #58307a, at 13/255 alpha (nearest to 5%).
- Restored translucent rendering for the uniform glass tint, including inventory models.
- Inset substrate side faces to 0.6 model units behind the outside planes, removing their coplanar overlap with corner edging. Kept full-width top faces for connected sand/gravel/powder floors.
- Retained joined frame geometry, two-sided pipe walls and water seals inside tank-to-pipe connections.

Replace the old linked-aquariums JAR with 0.4.2; do not install both versions.

# 0.4.1

- Replaced overlapping tank, pipe and inventory frame bars with the exposed surface of their geometric union, removing coincident faces at corners.
- Removed duplicate textured borders and moved glass away from frame planes.
- Added inward-facing pipe walls with a small physical separation from the outward-facing walls.
- Changed glass to vanilla-style cutout transparency so clear pixels cannot hide water behind them. Hostile glass retains purple highlights.
- Added water surfaces around narrow tube entrances, inside the adjacent aquarium rather than outside the pipe.
- Preserved gameplay, saved block IDs, connectivity, GUI, floors, fish and terrarium inhabitants.

Replace the 0.4.0 JAR with 0.4.1. Do not install both versions.

# 0.4.0

- Added dedicated 3D inventory icons: water-filled aquarium, grassy passive terrarium, and purple rocky hostile terrarium, with matching three-quarter-height pipes.
- Added dark physical outer frames that disappear at shared tank edges, plus visible continuous frames along pipes.
- Strengthened contained-water and glass visibility while keeping water geometry inside the glass; water panels meet without gaps.
- Fixed floor top faces being culled beneath stacked enclosure blocks; adjacent substrate remains continuous.
- Fixed invisible icon contents by drawing the interior before glass; removed invalid empty-face geometry in six-way pipe junctions.
- Replaced numbered decor selection with a clickable top-down grid, layer switching, panning, coordinate tooltips and markers for decor, substrate and shallow water.
- Kept grid selection stable when new blocks are added to the network and validated all cell selections on the server.
- Preserved existing block IDs, floors, decoration data, creature handling and no-spill behavior.

Replace the previous linked-aquariums JAR with 0.4.0; do not keep both installed.

# 0.3.0

- Added separate passive and hostile terrariums with dry interiors, connected glass, shared decor UI and matching zero-capacity pipes.
- Added shallow contained ground-water patches through water buckets.
- Added reusable Mob Nets for capture, transfer, retrieval and world release with original size/AI restoration.
- Set terrarium residents to one-third base scale; disabled attack AI and neutralized creeper fuses.
- Added five-block minimum flying cages, floor-level horizontal ground pipes and fourth-level-or-higher flying connections.
- Added lightly tinted hostile glass and sunlight protection for hostile residents.
- Added rescue to filled nets when residents are stranded, overcrowded or left in a cage too short for flight.
- Preserved all aquarium features and 0.2.0 decor behavior.

# 0.2.0

- Added tank management UI with decor rotation/position, module selection, and network creature selection with bucket insertion/removal.
- Added four rock/log shapes, trident poses, and occasionally opening decorative chests with bubbles.
- Accepted all placeable block items as harmless miniature decor, including pottable plants and modded block items.
- Added fish spawn eggs, peaceful axolotls, mini squid/glow squid, permanent baby turtles, and a 2% mini drowned chance on trident placement.
- Added reusable creature buckets, persistent item components/layouts, and automatic migration of earlier decorations.
- Preserved connected glass/floors, full contained tube water, zero tube capacity, unaided tube swimming, and no water spill on breaking.

# 0.1.1

- Full contained water in tubes, with no full-block water outside the glass.
- Connected glass textures remove borders at shared edges.
- Floor sections extend to the block edges so sand, gravel, and powder surfaces join without gaps.
- Managed fish swim through tube openings, including large salmon; routes avoid immediately turning back when another connection is available.
- Fish bucket components, including salmon size, are preserved when adding fish.
- Breaking aquarium and tube blocks leaves air instead of water.

Replace the old 0.1.0 jar with 0.1.1; do not keep both installed. Existing tank blocks keep their IDs, floors and decorations. Any water already spilled by the previous build is ordinary world water and must be removed separately.
