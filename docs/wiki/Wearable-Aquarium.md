# Wearable Aquarium (0.8.4)

**Placed-first loading:** Craft the Wearable Aquarium, place it on the ground like a block and add residents by right-clicking its standing humanoid glass model. **Do not fill it while holding it** anymore.

- Any Cod, Salmon, Tropical Fish or Pufferfish Bucket loads one fish (**1 of 4 slots**).
- Use a filled **Mob Net containing a Guardian** to add a Guardian (**2 of 4 slots**). First catch a Guardian by right-clicking the wild Guardian with an empty Mob Net.
- A regular empty bucket removes the most recently inserted **fish**. An empty Mob Net removes the most recently inserted **Guardian**.
- Break the placed tank to pick up the Wearable Aquarium **with all occupants saved** (including in Creative).
- Equip the recovered item into Caszual Additions' **chest cosmetic slot** to wear it. The placed form and worn form share the same resident data.
- Regular fish continue swimming inside the model, including into the head.
- **Guardians stay inside the head** in both placed and worn forms. One Guardian is centered; two Guardians sit evenly spaced side by side, like eyes, and face outward. They no longer wander through the torso. The narrow frame and saved fish data are unchanged.

**Capacity examples:** four fish; two fish and one Guardian; or two Guardians. Custom tropical fish colors and Guardian Mob Net entity data persist through placement, pickup and use. Guardian data is stored on the item and does not spawn Guardians in the world.

## Previous versions

Introduced in Linked Aquariums **0.8.0**, for use with the Caszual Additions chest cosmetic slot.

## Use

1. Craft it from **four Aquarium blocks**, **four Glass blocks** and **one Iron Chestplate**.
2. Hold the wearable in your **main hand** and one of **Cod Bucket, Salmon Bucket, Tropical Fish Bucket, or Pufferfish Bucket** in your **offhand**.
3. Right-click air to load that fish; you get an empty bucket back. The wearable holds **four fish in total**.
4. To retrieve a fish, hold an **empty bucket** in the offhand and right-click air while holding the wearable. Fish come out **last in, first out**.
5. Equip the wearable in the **chest cosmetic slot** of compatible Caszual Additions. It replaces your player's appearance with a hollow, humanoid, framed-glass aquarium. All saved fish swim inside the torso.

Each fish is stored as its original bucket stack, so tropical fish variants and custom bucket data survive insertion, cosmetic equip/unequip and retrieval. There are no free-roaming world entities, altered hitboxes, armor stats or breathing benefits.

**Compatibility:** Minecraft Java 26.2, Linked Aquariums 0.8.0+, and a Caszual Additions build with the Wearable Aquarium renderer. Without Caszual Additions, the item stores fish but cannot transform the player. Only vanilla fish bucket types are supported; axolotls, squid and turtles are excluded. Current fish visuals are stylized miniatures rendered in the torso, not realistic fish traveling through limbs.
