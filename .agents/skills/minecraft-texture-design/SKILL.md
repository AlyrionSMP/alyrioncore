---
name: minecraft-texture-design
description: "Use when creating, editing, or critiquing Minecraft textures (blocks, items, or entities) — the vanilla style guide: what \"vanilla-like\" means, the 16x16 grid and tiling, color ramps and hue shifting, shading craft and artifacts to avoid, material language, per-family recipes, the Jappa/Compliance checklist, and technical file conventions."
compatibility: "Scripts are in scripts/ at the preset root, symlinked from scripts/ in each skill directory. Run as scripts/<name>.py from the skill root. In DSH, scripts are also injected as tools."
---

# Minecraft Texture Design — Vanilla Style

Load this skill whenever you create, edit, or critique Minecraft textures
(blocks, items, or entities). It encodes how vanilla textures are actually
designed — the visual language JAPPA (Jasper Boerstra) established in the 1.14
Texture Update — so your output reads as "vanilla-like" instead of "generic
pixel art".

Pair it with the `minecraft-texture-generation` skill, which provides the
runnable generator scripts; this skill is the *why*, that one is the *how*.

---

## 1. What "vanilla style" means

The 1.14 Texture Update (Java 1.14 / Bedrock 1.10, 2018–2019) replaced most of
the original "programmer art" textures. JAPPA's goals were readability and
material honesty:

- **Every texture reads as what it is** — stone looks like stone, wood like
  wood, even at 16×16. A texture that needs explanation has failed.
- **Low noise** — grain is used sparingly and deliberately; noise adds no
  information and, at worst, makes a texture unrecognizable.
- **Muted, cohesive color** — vanilla stays low-to-mid saturation; nothing
  glows unless it is literally emissive (glowstone, redstone ore, magma).
- **Simplified forms** — the model carries the shape; the texture carries
  material detail. Detail density is uniform across the block.
- **"Flatness" is a resolution limit, not a design choice.** When redrawing at
  higher resolution, use the extra pixels to say more about the material —
  make a bucket look like a bucket — rather than faithfully copying the 16×16
  shape.

## 2. The canvas and the grid

- A block face is **16×16 px**; 1 px ≈ 6.25 cm. A block is 16×16×16.
- Item sprites are also 16×16, drawn as if the item floats in front of the
  camera (no perspective).
- Texture origin is **top-left, y down** (screen space).
- **Tiling is mandatory.** Block textures repeat seamlessly; a texture that
  shows a visible seam or repeat grid is broken. The canonical check is a 3×3
  wall of the same block (see the generation skill's `scripts/preview.py wall`).
- **No mixels**: never mix 16×16 detail with 8×8 or 32×32 detail in one
  texture. If there is no room for a detail at 16×16, drop the detail or
  redesign the texture.
- Coordinates that matter: a block's top face is seen from above, the side
  faces from the side — keep that in mind when a texture must work on multiple
  faces (e.g., logs: bark on sides, rings on top).

## 3. Color: ramps, hue shifting, and the vanilla palette

**Build every material from a color ramp** — a small set of shades of one hue,
ordered by brightness. Vanilla uses **2–5 shades per material**; more reads as
noise, fewer reads as flat.

- Start from the **midtone**, then add one shadow and one highlight. Add more
  shades only if needed. (Blockbench style guide: "start the ramp with the
  midtone… only one shadow and one highlight… afterwards, more shades".)
- **Hue-shift between shades** — do not just change value (a "straight ramp"
  looks dull). Vanilla shadows often shift hue toward the warm side and gain a
  little saturation; highlights shift cool and desaturate. The bundled
  `scripts/mcutil.py`'s `ramp()` implements this: `hue_shift=6`, shadows +6% saturation,
  highlights −12% saturation, value steps ≈16%.
- **Stay muted.** Vanilla textures sit at roughly 30–60% saturation. Pure
  black and pure white are almost never used; the darkest shadow keeps hue, the
  brightest highlight is off-white.
- **Palette budget**: index your colors first. When copying a vanilla texture,
  extract its exact palette (see `scripts/preview.py palette`) and reuse it — vanilla
  textures reuse colors across blocks, and that consistency is a huge part of
  the look.
- **Gradients read as noise at 16×16.** Between two shades, use dithering, not
  a smooth gradient.

Material families and their vanilla behavior:

| Family | Vanilla treatment |
|---|---|
| Stone / rock | 3–5 grays with subtle warm/cool variation, sparse 1px cracks, faint blotches |
| Ores | stone base + embedded crystal clusters: bright core → mid facet → dark casing → darker seam against stone; shiny gems get a 1px white sparkle |
| Wood (planks) | 2–3 strips with vertical grain, 1px dark seams, per-plank value variation |
| Wood (logs) | bark: vertical grain ridges; top: concentric rings |
| Sand | the canonical dithered texture — checkered dithering between 2 tans, very low contrast |
| Gravel | scattered pebble blotches in 2–3 gray-brown tones with 1px seams |
| Grass | base green + short 1–2px tufts; hue comes from the biome colormap, so keep the value range narrow |
| Glass | translucent tint + 1px frame (light top-left, dark bottom-right) + a few shine pixels |
| Bricks | offset courses, per-brick value variation, 1px mortar, top-edge highlight |
| Metal | strong value steps, specular streaks, bevel highlights — never dithered |
| Emissive | saturated core + bright center (glowstone, magma, redstone ore) |

## 4. Shading craft and the artifacts that break it

Vanilla shading is **deliberate**: every pixel is placed with intent.

Tools of the trade (all manual):
- **Anti-aliasing (AA)**: hand-placed intermediate pixels on shape borders to
  smooth jaggies.
- **Dithering**: interspersing pixels of two shades, typically in a 2×2
  checker pattern, to transition between clusters.
- **1px features**: outlines, seams, cracks, highlights are 1px — never 2px
  when 1px works (the Compliance rule).

Artifacts to avoid (each instantly reads as "amateur"):
- **Banding**: pixels lining up brightest→darkest in straight lines ("fat
  lines"), diagonal staircases, or corners ("hugging"). It reveals the grid.
- **Pillow shading**: concentric darkest→brightest rings that ignore the
  shape's actual lighting.
- **Pancake shading**: highlight on one side and shadow on the other,
  regardless of surface shape.
- **Over-dithering / inconsistent dithering**: too much surface area, or
  dithered in one place and not another for no reason.
- **Jaggies**: unpolished diagonal lines/curves lacking AA.
- **Flat patches**: large areas of one color look plasticky — break them up
  with grain, cracks, or dithering.
- **Noise as decoration**: random speckle that carries no material
  information.

Lighting convention: **light from the top-left**. Highlights sit top-left,
shadows bottom-right, consistently across the whole texture and across the
pack.

## 5. Material language

Define the material *before* shading, then shade accordingly (Compliance
rule #3):

- **Rough / matte** (stone, sand, dirt, concrete): low contrast between
  shades, dithering welcome, no hard highlights.
- **Smooth / shiny** (glass, glazed terracotta, polished blocks): high
  contrast highlights, no dithering, sharp light shapes.
- **Metal** (iron, gold, copper): strong value jumps, specular streaks,
  bevel edges; metal blocks often have a brushed/milled pattern.
- **Translucent** (glass, ice): low alpha, white-ish edge highlights, visible
  shine streaks.
- **Emissive** (glowstone, lava, redstone ore, sea lantern): saturated color,
  bright core, and — for PBR — an emissive mask.
- **Organic** (leaves, moss, vines): irregular clusters, two-tone value
  variation, no straight lines.

## 6. Recipe cheat-sheet per family

**Stone / rock** — low-frequency noise (2–3 octaves, base freq ≈2–3) mapped to
a 5-shade gray ramp with threshold dithering; 2–4 one-pixel cracks; a few
darker blotches. Cobblestone = separate stones (irregular ellipses) with 1px
mortar gaps, each stone shaded brighter toward its top-left.

**Ores** — take the stone base; scatter 3–5 crystal clusters (radius 1.4–2.6).
Each cluster: bright core, mid facet, dark outer facet, deep casing seam where
it meets stone; add one sparkle pixel for shiny gems. Gems must look
*embedded*, never painted on top.

**Planks** — split the 16×16 into 2–3 vertical planks; per plank, map vertical
grain noise onto the ramp with a small per-plank value offset; 1px dark seams
between planks. Birch is light and smooth, dark oak is deep and coarse, crimson
is red-purple with almost-black grain.

**Glass** — low alpha tint; 1px border frame (light top-left → dark
bottom-right); 2–3 diagonal shine pixels; keep most of the face nearly
transparent.

**Sand / gravel / dirt** — dither-heavy. Sand is checker-dithered tans with
very low contrast. Gravel is pebble blotches with dark seams. Dirt is a brown
ramp with cracks and blotches.

**Bricks** — courses of bricks with half-offset joints; each brick gets its
own value variation and a top-edge highlight; mortar is 1px and slightly
desaturated.

**Items** — fit 16×16. Recipe (Blockbench order): midtone shape → significantly
darker 1px outline → highlight + shadow (top-left light) → rest of palette →
surface properties. Items read via silhouette first, so the outline must
separate them from the background.

**Entities** — box UV: the top and front faces are brighter than the bottom and
back; shade each face as its own surface with the same top-left light.

## 7. The vanilla-compatibility checklist

Before calling a texture done, check (from the Compliance "Jappa" checklist):

1. Same object, same angle/perspective, similar proportions as vanilla.
2. Items have a **darker, 1px-thick outline**.
3. Lit from the **top-left**, shaded accordingly.
4. Each color area uses **only a few shades**.
5. **Same palette** as the vanilla texture (or the pack's palette).
6. **No 2px outlines** where 1px works; no extra colors added without reason.
7. **Material is clear**: rough/smooth/shiny/translucent reads correctly.
8. **Tiles**: 3×3 wall shows no seams, no repeat artifacts, no banding.
9. **No flat patches**, no over-dithering, no noise for noise's sake.
10. File is **PNG** (never JPG), 16×16 master, alpha preserved where needed.

## 8. The workflow (how to actually work)

1. **Brief** — texture name, family, size (16×16 default), any constraints.
2. **Reference** — fetch the vanilla texture with one command and study it:
   `python3 scripts/fetch_reference.py "Iron Pickaxe" --out ref.png` prints the
   palette, ASCII maps, and the exact hex grid — the whole study pack, no
   image input needed. Index the reference's colors and reuse them.
3. **Palette** — build the ramp(s): midtone → shadow(s) → highlight(s), hue
   shifted, muted. Keep ≤5 shades per material.
4. **Base** — block in the structure (stone base, plank layout, item shape)
   with 2–3 shades only. For items, encode the shape as an ASCII map (see the
   generation skill's `PICKAXE` pattern) so the silhouette is explicit and
   verifiable.
5. **Tile-check early** — render the 3×3 wall *before* polishing. Fix seams
   first; detail on top of a broken tile is wasted. (Items: skip tiling, do a
   silhouette check instead.)
6. **Detail & shade** — add grain, cracks, crystals, seams, highlights.
   Re-check tiling after major changes.
7. **PBR (optional)** — derive normal/specular/height/emissive maps (see the
   generation skill) if the pack targets shaders or 1.20.5+.
8. **Look with your own eyes, critique, fix** — if your model can view images,
   render mosaic + wall + scaled previews and read them. If it cannot, run
   `python3 scripts/preview.py analyze --file X` — the text report (palette, light
   check, outline check, banding, flat patches, tiling, ASCII maps) is the
   critique substrate. Compare item silhouettes against the vanilla reference
   (alpha maps must be identical). Minimum 2–3 critique passes before
   delivery; every `WARNING:` line is a fix, not noise.
9. **Deliver** — master at 1×; display/export scales are integer multiples
   only (never scale non-integer, never upscale with filters).

## 9. Technical file conventions (quick reference)

- Block textures: `assets/minecraft/textures/block/<name>.png`
- Item textures: `assets/minecraft/textures/item/<name>.png`
- Animation: `<name>.png.mcmeta` → `{"animation": {"frametime": N, "interpolate": bool, "frames": [...]}}` — frames are square cells stacked vertically in one PNG; frame 0 is the top cell; frametime is in game ticks (20/s).
- Grass/foliage tint comes from the biome colormap (`assets/minecraft/textures/colormap/grass.png`, `foliage.png`) applied to the texture — keep tintable textures' value range narrow.
- Atlas (1.20.5+): `assets/minecraft/atlases/blocks.json` with `sources` entries (`directory`, `single`, `filter`, …).
- Official PBR suffixes (1.20.5+): `_n` (normal), `_s` (specular: R=spec intensity, G=smoothness, B=metalness), `_h` (height), `_e` (emissive: R=intensity, G/B=0). labPBR shader packs use the same `_n/_s/_h/_e` suffixes.

## 10. Sources

- Blockbench — [Minecraft Style Guide](https://www.blockbench.net/wiki/guides/minecraft-style-guide/)
- Compliance — ["Acquiring More Pixels"](https://gist.github.com/Pomi108/2257f47eb42350ba39fc6ec32548448c) (vanilla-style upscaling rules)
- Pixel Joint — [The Pixel Art Tutorial](https://pixeljoint.com/forum/forum_posts.asp?TID=11299)
- Minecraft Wiki — [Texture Update](https://minecraft.fandom.com/wiki/Texture_Update)
- shaderLABS — [LabPBR Material Standard](https://wiki.shaderlabs.org/wiki/LabPBR_Material_Standard)
- Pablo's Minecraft Tools — [How to Make a Resource Pack: Textures](https://teste001d2044098.neocities.org/guides-textures)
