# Verification — 0.4.2

This is a resource-only patch. Every compiled Java class is byte-identical to the 0.4.1 package (which retained the 0.4.0 classes). Gameplay and saved data handling are unchanged.

Final asset checks passed:

- All 64 swim-tube connection masks select glass, water and edging simultaneously, with no random `apply` arrays.
- Every glass PNG is uniform: normal RGBA (219,219,219,13), hostile RGBA (88,48,122,13), with no diagonal pixels.
- All 19 substrate meshes retain full-width top faces, inset side faces, and no same-plane overlap with any of the 64 tank frame configurations.
- Joined frames remain free of duplicate overlapping faces, pipe walls remain two-sided, water remains bounded and reaches open pipe ends, all six connection water seals stay inside adjoining tanks, and terrarium pipes remain dry.
- Packaged resources match the validated source assets; JAR/ZIP integrity and unchanged compiled Java bytes were checked.

Run `python3 tools/validate_assets.py` to reproduce the resource checks. No fresh in-game render test was run for 0.4.2. The bundled client smoke test is available through `./gradlew runClientGameTest`; shader/renderer compatibility remains unverified.

## Earlier verification history

# Verification — 0.4.1

This is a resource-only update. The release JAR retains every compiled Java class from the tested 0.4.0 release byte-for-byte; only resources and version metadata changed. No gameplay or entity code was rebuilt or modified.

`python3 tools/validate_assets.py` passed for the final package's source assets. It checks all 64 tank/pipe frame combinations for same-plane overlapping faces, both sides of all 128 normal/hostile pipe shells, water containment and connected endpoints, all six tank-side water seals, binary-alpha glass textures, model references, uncullable floors, and dry terrarium assets.

Before workspace recovery, the revised frame/cutout/two-sided-wall models passed the Minecraft client smoke test, including four camera positions around a vertical bend and branch, and were visually inspected in the straight-pipe and inventory views. The final addition of the tank-side water seals was geometry-checked but has not had a fresh in-game visual check. Shader packs and alternate renderers remain unverified.

## Previous gameplay verification

## 0.4.0

Built for Minecraft Java Edition 26.2, Fabric Loader 0.19.5, Fabric API 0.158.0+26.2, JDK 25, Gradle 9.5.1 and Loom 1.17.21.

Thirty-one enclosure integration tests passed in an actual isolated Minecraft test server (thirty-two required tests total, including Fabric's infrastructure test):

- Tank/tube connectivity, zero tube capacity, and splitting networks.
- Opening/resealing glass faces and updating tube-to-tank rings.
- Fish survival and rescue as buckets after removing their tank.
- Overcrowding recovery without fish loss or duplication.
- Salmon swimming into a tube unaided.
- Survival bucket insertion, full-tank refusal without consuming the bucket, and fish retrieval.
- Concrete powder floor storage, rock decoration, item consumption, and removal while retaining the floor.
- A large salmon added from a bucket retains its size and enters a tube without being pushed.
- A cod traverses a tube to the next tank and remains in water.
- Tank and tube removal leave air with no fluid, checked again after twenty ticks. Tube fluid reports full height.

Additional server checks passed:

- Decor UI places exactly one item, rotates, varies and moves it, returns it on removal, and refuses changes when Output is occupied.
- Fish UI adds and removes selected fish with correct bucket returns.
- Glow ink creates a scaled, peaceful squid; a creature bucket restores it with a fresh UUID.
- Fish eggs work and non-aquatic eggs are rejected.
- Axolotls do not attack fish during the test and turtles remain babies.
- Earlier rock decorations migrate exactly once and generic blocks create one miniature item display.
- Chest opening and closing selects the correct display states; decor item/layout saves and loads; removing the tank removes its display.
- Mini drowned are scaled, peaceful, and obey capacity.

Terrarium server checks passed:

- Passive tank/pipe networks exclude hostile modules and aquariums; pipes have zero capacity and no fluid.
- Hostile/passive mismatch and insufficient bird cage height keep the mob in the net; five-block cages accept birds.
- Ground entrances use the lowest layer; flying entrances use the fourth level or higher in either direction.
- A one-third-size cow walks unaided through a dry pipe into a second terrarium.
- Nets preserve custom names, refresh UUIDs, and restore original scale, AI and gravity on world release.
- Water bucket insertion/retrieval returns correct buckets, refuses upper layers, and never spills water.
- Terrariums share the decor insertion/rotation UI and resident selection with net transfers.
- A zombie stays healthy and unburned in daylight inside a hostile terrarium.
- Captured ignited creepers remain alive without exploding or damaging the enclosure.
- Removed cages rescue residents into exactly one filled net.
- Shortening a bird cage below five blocks returns the bird to a net.
- Full terrariums refuse insertion without consuming or emptying the filled net.

`python3 tools/validate_assets.py` checks all sixteen connected-glass edge masks, nineteen continuous floor meshes, sixty-four tube-water configurations, containment within the tube glass volume, continuous water at branch joins, and the invisible default-fluid sprite. The custom fluid uses a registered no-op fluid renderer; visible water comes from bounded aquarium meshes instead of full-block fluid faces.

Dry passive/hostile tank and pipe model references are also checked, along with bottom-only contained ground-water parts.

The 0.4.0 client smoke test runs Minecraft with Fabric/Indigo, opens a new test world, places all three enclosure types with stacked floors and pipes, opens the real management screen, clicks a grid cell with the mouse, switches layer with +, returns Home, and switches to the residents page. It also renders all six inventory models and captures screenshots. Screenshots were visually inspected for visible floors under stacked tanks, continuous outer borders, contained water, pipe frames, readable UI and distinct icon contents. Shader packs and alternate renderers have not been tested; compatibility with other mods remains unverified.

Two additional server tests verify spatial cell selection, rejecting pipe/empty cells, placing decor only in the clicked block, preserving selection after expansion, and changing layers/Home correctly.

The expanded asset validator also checks every model element has valid faces, all nineteen floors omit occlusion culling, twelve conditional outer edges per tank, sixty-four pipe frames, six dedicated icons, and seamless water-panel coordinates.

A local adjustment to Loom's optional Unix-domain-socket probe was needed in this environment. It is confined to the local build-tool cache and is absent from the distributed project and mod.
