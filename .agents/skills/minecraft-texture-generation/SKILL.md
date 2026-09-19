---
name: minecraft-texture-generation
description: "Use together with minecraft-texture-design when generating Minecraft textures with the bundled pure-Python scripts (fetch_reference, gen_block, gen_item, gen_pbr, gen_pack, preview) — CLI reference for every tool, the generate-preview-critique-fix loop, and the no-image analyze workflow for models that cannot view images."
compatibility: "Scripts are in scripts/ at the preset root, symlinked from scripts/ in each skill directory. Run as scripts/<name>.py from the skill root. In DSH, scripts are also injected as tools."
---

# Minecraft Texture Generation — Script Toolset

This skill is the *how* half of the texture toolset (the `minecraft-texture-design`
skill is the *why*). It documents the bundled pure-Python generator scripts and
the generate → preview → critique → fix loop.

The scripts are **pure Python 3 standard library** — no pip installs, no
Pillow, no numpy, no ImageMagick. They run on any stock Python 3, and the PNG
codec reads **every** common PNG variant (8/4/2/1-bit, palette, tRNS), so
vanilla textures from the wiki or assets mirrors decode without conversion.

## 0. Locating the scripts

The scripts ship inside this agent preset at the repo-root `scripts/`
directory. Each skill directory has a `scripts/` symlink back to it, so from
any skill you can run them directly:

    python3 scripts/gen_block.py ...

If the symlink is missing (e.g. the skill was installed without scripts),
find the preset root and copy the scripts into your working directory:

    find . -maxdepth 4 -name 'gen_block.py' 2>/dev/null   # locate the scripts dir
    cp <preset>/scripts/*.py .                             # copy them local

They must stay together — they import each other.

**Paths**: always write outputs to your working directory (default `--out .`).
Do not rely on `/tmp` — it is sandbox-isolated per agent step and will not
persist between commands.

## 1. The tools at a glance

| Tool | Purpose |
|---|---|
| `scripts/fetch_reference.py` | one-command vanilla reference: finds the right wiki file / assets-mirror master, decodes any PNG variant, downsamples to 16×16, prints the full study pack (palette + ASCII maps + exact hex grid) |
| `scripts/gen_block.py` | block textures: stone, ores, planks, logs, sand, gravel, dirt, grass, glass, bricks, netherrack, glowstone, cobble, granite, … |
| `scripts/gen_item.py` | item sprites: gems, ingots, planks, sticks, and the **pickaxe** recipe (vanilla silhouette + configurable head/handle materials) |
| `scripts/gen_pbr.py` | derive `_n/_s/_h/_e` maps from an albedo (labPBR and 1.20.5+ official) |
| `scripts/gen_pack.py` | assemble textures into a loadable resource pack `.zip` with `pack.mcmeta` + animations |
| `scripts/preview.py` | see your work: mosaic, 3×3 tiling wall, scaled view, palette report — and **analyze/ascii**, the no-image critique reports |
| `scripts/mcutil.py` | the library: tileable fBm noise, hue-shifted ramps, dithering, full PNG codec, pixel-analysis engine — import and extend |

Every generator takes `--seed N` (vary it to explore variants) and writes to
`--out DIR`.

## 2. Working WITHOUT image input (models that cannot view images)

The design skill's loop is *look → critique → fix*. If your model has no image
input, "look" means **precise pixel analysis**: turn the texture into
structured text and check it against the checklist mechanically.

    # the full critique report — ALWAYS run this before delivering
    python3 scripts/preview.py analyze --file meteor_pickaxe.png

It prints: size/opacity, the exact palette, bounding box, a light-direction
check (top-left vs bevel), an outline check (items), banding detection, flat
patches, tile-edge mismatch (blocks), plus ASCII luma and alpha maps. To study
a texture pixel-by-pixel:

    python3 scripts/preview.py ascii --file x.png --mode luma    # brightness map
    python3 scripts/preview.py ascii --file x.png --mode alpha   # silhouette map
    python3 scripts/preview.py ascii --file x.png --mode hex     # exact hex per pixel

Rules for the no-image loop:
1. Always run `analyze` (and `palette`) before handing anything over.
2. Treat every `WARNING:` line as a checklist failure to fix, not noise.
3. Verify shape fidelity against the vanilla reference: generate your item,
   fetch the vanilla master, and compare silhouettes (alpha maps must be
   identical) and palettes.
4. You can still produce preview PNGs for the user — but your own critique
   happens on the text report.

## 3. Quick start

    # study the vanilla reference first (one command)
    python3 scripts/fetch_reference.py "Iron Pickaxe" --out ref_iron_pickaxe.png

    # one of each family, same seed so they feel like one pack
    python3 scripts/gen_block.py stone   --seed 7 --out textures
    python3 scripts/gen_block.py ore     --gem diamond --seed 7 --out textures
    python3 scripts/gen_block.py planks  --wood oak --seed 7 --out textures
    python3 scripts/gen_block.py sand    --seed 7 --out textures
    python3 scripts/gen_item.py  gem     --palette diamond --out textures
    python3 scripts/gen_item.py  ingot   --palette gold --out textures

    # LOOK at them (mosaic + wall as PNGs, analyze as text)
    python3 scripts/preview.py mosaic --files textures/*.png --scale 6 --out textures/mosaic.png
    python3 scripts/preview.py wall   --file textures/diamond_ore.png --scale 6 --out textures/wall.png
    python3 scripts/preview.py analyze --file textures/diamond_ore.png

    # critique against the design skill's checklist, then iterate:
    # tweak --seed, or edit the recipe, regenerate, re-look.

## 4. `scripts/fetch_reference.py` reference

    python3 scripts/fetch_reference.py "Iron Pickaxe" --out ref.png   # normal use
    python3 scripts/fetch_reference.py "Iron Pickaxe" --list          # see candidate files
    python3 scripts/fetch_reference.py stone --out ref_stone.png      # ids work too
    python3 scripts/fetch_reference.py "Stone" --file "Stone JE6.png" # pin a wiki file
    python3 scripts/fetch_reference.py "Iron Pickaxe" --wiki          # force the wiki source

It prefers the true 16×16 PNG master from the community assets mirror
(`InventivetalentDev/minecraft-assets`, recent versions), falling back to the
Minecraft wiki (which may serve 160×160 renders or WebP). The output PNG is
always 16×16, and the printed report includes the exact hex grid so you can
copy the vanilla structure precisely.

## 5. `scripts/gen_block.py` reference

    python3 scripts/gen_block.py list                          # recipes, ores, woods, palettes
    python3 scripts/gen_block.py stone --seed 7 --out DIR
    python3 scripts/gen_block.py ore --gem diamond --seed 7    # gems: diamond emerald gold iron
                                                       #       coal copper redstone lapis
                                                       #       netherite amethyst quartz
    python3 scripts/gen_block.py planks --wood oak             # woods: oak spruce birch jungle
                                                       #        acacia dark_oak mangrove
                                                       #        cherry bamboo crimson warped
    python3 scripts/gen_block.py log --wood oak                # bark side
    python3 scripts/gen_block.py log_top --wood oak            # rings top face
    python3 scripts/gen_block.py <sand|gravel|dirt|grass_top|glass|bricks|netherrack|glowstone|cobble|granite|andesite|diorite|deepslate>

The recipes encode the vanilla rules from the design skill:
5-shade hue-shifted ramps, tileable fBm grain, 1px cracks/seams/outlines,
top-left light, embedded ore crystals with sparkle, checker-dithered sand.

To add a new material: edit the `PALETTES` dict in `scripts/mcutil.py` (5 shades,
darkest→lightest) and add a recipe function in `scripts/gen_block.py` following an
existing one; register it in `RECIPES`.

## 6. `scripts/gen_item.py` reference

    python3 scripts/gen_item.py list
    python3 scripts/gen_item.py gem     --palette diamond --out DIR   # faceted gem, sparkle
    python3 scripts/gen_item.py ingot   --palette gold --out DIR
    python3 scripts/gen_item.py plank   --wood oak --out DIR
    python3 scripts/gen_item.py stick   --out DIR
    python3 scripts/gen_item.py pickaxe --head meteor --handle obsidian --out DIR

The **pickaxe** recipe ships the vanilla iron-pickaxe silhouette (decoded from
the 1.21 master and verified pixel-identical — same object, same angle, same
proportions, per the Jappa checklist). It implements the Blockbench item
recipe: midtone shape → darker 1px outline → bevel highlight + shadow →
palette detail → surface properties (craters, sheen, sparkle). Head and handle
are any `ITEM_PALETTES` entries — `meteor` (teal-slate iron) and `obsidian`
are built in for meteoric looks; `iron`, `gold`, `diamond`, … also work.

Complex items beyond the presets are best drawn as an ASCII shape map: copy
the `PICKAXE` pattern in `scripts/gen_item.py` (region letters per pixel), add a
recipe that fills regions with ramps and shades them, and verify the silhouette
with `scripts/preview.py analyze` against the vanilla reference.

## 7. `scripts/gen_pbr.py` reference (PBR: labPBR + 1.20.5+ official)

    # default: rough material look
    python3 scripts/gen_pbr.py textures/stone.png --out textures

    # shiny metal
    python3 scripts/gen_pbr.py textures/iron_block.png --out textures \
        --spec 180 --smooth 220 --metal 255 --strength 1.4

    # emissive: mask by albedo color (glowstone)
    python3 scripts/gen_pbr.py textures/glowstone.png --out textures \
        --emissive-color '#e2c232' --tolerance 60

    # emissive from a hand-made mask (white = emissive)
    python3 scripts/gen_pbr.py textures/lava.png --out textures --emissive mask.png

Output: `<name>_n.png` (normal), `<name>_s.png` (specular: R=spec, G=smooth,
B=metal), `<name>_h.png` (height), `<name>_e.png` (emissive, only with a mask).
Normals are toroidal — they tile with the albedo.

Material presets (design-skill section 5):

| Material | `--spec` | `--smooth` | `--metal` |
|---|---|---|---|
| rough (stone, sand) | 32–64 | 64–110 | 0 |
| wood / organic | 40–70 | 110–150 | 0 |
| polished (quartz, glazed) | 90–140 | 190–230 | 0 |
| metal (iron, gold) | 150–220 | 200–240 | 255 |
| emissive | — | — | — (use `--emissive-*`) |

## 8. `scripts/gen_pack.py` reference (shipping)

    # block textures in textures/block/, items in textures/item/
    python3 scripts/gen_pack.py --name "My Pack" --block textures --out dist

    # with an atlas manifest and PBR-capable pack format
    python3 scripts/gen_pack.py --name "My Pack" --block textures --out dist \
        --pack-format 22 --atlas

Output: `dist/My_Pack.zip` — drop it into `resourcepacks/`. Animation: place
`<name>.png.mcmeta` files next to your PNGs (see design skill §9 for the JSON
shape); `scripts/gen_pack.py` picks them up automatically. pack_format: 15 = 1.20.1,
22 = 1.20.5+ (official PBR suffixes), 34 = 1.21.4.

## 9. `scripts/preview.py` reference (the quality loop)

    python3 scripts/preview.py mosaic  --files a.png b.png c.png --scale 6 --out m.png
    python3 scripts/preview.py wall    --file block.png --scale 6 --out wall.png   # tile check
    python3 scripts/preview.py scaled  --file block.png --scale 10 --out big.png
    python3 scripts/preview.py analyze --file block.png --name my_block   # full text report
    python3 scripts/preview.py ascii   --file block.png --mode luma       # luma|alpha|hex
    python3 scripts/preview.py palette --file block.png          # hex colors + coverage
    python3 scripts/preview.py info    --file block.png          # size, alpha, color count

Use `wall` for every block texture (a 3×3 wall shows seams and repeat
artifacts instantly), `palette` to verify the shade budget (≤5 shades per
material, no stray colors), and `analyze` before every delivery — it is the
mechanized checklist: light direction, outline, banding, flat patches,
tiling. Read the output files with image-capable tools when your model has
image input; otherwise treat the analyze report as your eyes. Critique
against the design skill §7 checklist; fix; regenerate.

## 10. Extending the toolset

- New palettes → edit `PALETTES` in `scripts/mcutil.py`.
- New block recipes → add a function to `scripts/gen_block.py` and register it in
  `RECIPES`.
- New item shapes → add an ASCII map + recipe to `scripts/gen_item.py` (see the
  `PICKAXE` pattern).
- New PBR behaviors → `mcutil.normal_from_height`, `mcutil.specular_map`,
  `mcutil.emissive_map` are the building blocks.
- Hand-drawn passes → generate a base with the scripts, then refine pixel by
  pixel in an editor (Aseprite, Photoshop, or even a script that reads/writes
  PNGs via `mcutil.read_png` / `write_png`).

## 11. Golden rules

1. Always run `scripts/preview.py analyze` before delivering — even without image
   input, the text report is your eyes. Treat every WARNING as a failure.
2. Verify custom items against the vanilla reference: silhouettes must match,
   palettes should be indexed from the reference.
3. Vary `--seed` and compare variants in one mosaic; pick the best, don't
   settle for the first.
4. A block texture that fails the 3×3 wall is unfinished, no matter how
   pretty.
5. Keep the shade budget tight; index colors from vanilla references.
6. The scripts produce *vanilla-style bases* — the final 10% (character,
   readability) is a deliberate pixel pass. Say so when you hand something
   over.
