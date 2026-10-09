[Home](Home.md) · [Getting started](Getting-Started.md) · [Aquariums](Aquariums-and-Swim-Tubes.md) · [Terrariums](Terrariums-and-Pipes.md) · [Mobitat](Mobitat-and-Mob-Nets.md) · [Decorating](Decorating-and-Editor.md) · [Fish Editor](Tropical-Fish-Editor.md) · [Recipes](Crafting-Recipes.md) · [Configuration](Configuration-and-Limits.md) · [Troubleshooting](Troubleshooting.md)

---

# Crafting Recipes

These are **all nine shaped recipes** under `src/main/resources/data/linked_aquariums/recipe/` as of **0.7.1**. Rows are shown exactly as in the crafting JSON; `.` means an empty grid cell.

| Craftable | Pattern (top / middle / bottom) | Legend | Output |
|---|---|---|---|
| **Aquarium** | `GGG / G.G / III` | `G` Glass; `I` Iron Nugget | **4** |
| **Swim Tube** | `GGG / ... / GGG` | `G` Glass | **6** |
| **Passive Terrarium** | `GGG / G.G / III` | `G` Glass; `I` Oak Planks | **4** |
| **Hostile Terrarium** | `GGG / G.G / III` | `G` Glass; `I` Iron Ingot | **4** |
| **Passive Terrarium Pipe** | `GGG / .T. / GGG` | `G` Glass; `T` Passive Terrarium | **8** |
| **Hostile Terrarium Pipe** | `GGG / .T. / GGG` | `G` Glass; `T` Hostile Terrarium | **8** |
| **Mob Net** | `.SS / .SS / I..` | `S` String; `I` Stick | **1** |
| **Mobitat** | `IGI / GCG / IGI` | `I` Iron Nugget; `G` Glass; `C` Chest | **1** |
| **Tropical Fish Editor** | `GIG / GBG / GIG` | `G` Glass; `I` Iron Ingot; `B` Bucket | **1** |

## Special items without crafting recipes

**Aquarium Creature Bucket:** Not crafted. Retrieve supported special aquarium inhabitants (e.g., mini squid or baby turtle) with an empty bucket to obtain one.

**Filled Mob Net:** Not crafted as a filled variant. First craft an empty Mob Net, then use it on a supported mob.

**Filled Mobitat:** Not a separate recipe. Load creatures into an ordinary Mobitat, then preserve them by carrying or breaking it.

All recipes use vanilla Minecraft ingredients. For exact authoritative values see the [recipe files in the repository](../../tree/main/src/main/resources/data/linked_aquariums/recipe).
