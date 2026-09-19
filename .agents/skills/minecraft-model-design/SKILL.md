---
name: minecraft-model-design
description: "Use when designing or auditing 3D block models for Minecraft mods (vanilla JSON elements, NeoForge): z-fighting detection and fixes, the 0-16 boundary and depth layering, the per-face UV convention and crop auditing, machine design principles, BlockEntityRenderer animation, and the model-side rules for textures feeding 3D. Pair with minecraft-mod-compat and minecraft-texture-design."
compatibility: "Scripts are in scripts/ at the preset root, symlinked from scripts/ in each skill directory. Run as scripts/<name>.py from the skill root. In DSH, scripts are also injected as tools."
---

# Minecraft 3D Model Design (vanilla JSON elements)

Hard-won lessons from building AlyrionCore (NeoForge 1.21.1). This skill is the
3D half of modding: block-model JSON (`elements`), UVs, machine design, and
BlockEntityRenderer (BESR) animation. The 16×16 pixel-art rules (palettes, hue
shift, dithering, tiling, item silhouettes) live in `minecraft-texture-design`;
cross-mod code lives in `minecraft-mod-compat`.

Run the bundled detector script after every model change:

    python3 scripts/audit_model.py models/block/machine.json      # one model
    python3 scripts/audit_model.py models/block/                  # a whole folder

and report **zero findings** before moving on — never eyeball this.

---

## 1. Z-fighting: the #1 visual bug

Elements that touch at a shared plane with **same-facing coplanar faces**
z-fight (flicker). Back-to-back coplanar faces (opposite facing) are fine;
adjacent coplanar faces (sharing only an edge) are fine.

**Detector rule** (what `scripts/audit_model.py` implements): a fight exists for every
pair of faces with — same axis, same plane coordinate, same facing sign
(`north/west/down` = −1, `south/east/up` = +1), and overlapping rectangles in
the two free axes.

**Fix**: inset one element 0.05–0.1 along the plane normal so its face sits
clearly behind the other (hidden) — invisible, kills the flicker.

**Cross-model fights**: a BlockEntityRenderer part rendered *after* the
blockstate model z-fights it too. The airlock door leaf overlapping the
frame's sill/header did exactly this.

**Culled faces** (`"cullface"`) don't render when adjacent blocks are solid —
pairs that are culled in normal use can still fight when floating, so audit
them too.

## 2. The 0–16 boundary and "front details"

Element coords are 0–16; nothing may protrude past 16, and anything with
z < 16 renders **behind** the front face at z = 16. So a flat front face hides
every detail placed "on" it — a generator model looked like a plain block for
exactly this reason.

- **Depth layering**: recess the body faces (e.g., front at z=15.6, sides at
  x=15.4 / x=0.6) and put features (ports, vent rings, gauge pods) flush at the
  boundary plane (z=16 / x=16 / x=0). Features then read as proud of the body.
- Elements *can* sit slightly outside 0–16 (they just render into the next
  block's space) but that clips into adjacent blocks and z-fights their faces
  — keep everything inside.

## 3. Vanilla UV convention (verified from `FaceInfo`/`FaceBakery`)

When you specify `"uv"` per face, the mapping is:

| Face | u axis | v axis |
|---|---|---|
| north (−z) | 16 − x | 16 − y |
| south (+z) | x | 16 − y |
| west (−x) | z | 16 − y |
| east (+x) | 16 − z | 16 − y |
| up (+y) | x | z |
| down (−y) | x | 16 − z |

Re-verify with `javap -c net.minecraft.client.renderer.FaceInfo` (the
per-direction `VertexInfo` constants) and `BlockFaceUV.getU/getV` (v0=(u1,v1),
v1=(u1,v2), v2=(u2,v2), v3=(u2,v1)).

**Crop convention** (what `scripts/audit_model.py` flags): hand-authored models map
each face's projection onto the texture — uv = the face's x/z extent, v =
16−y for vertical faces.

- Full 16×16 uv on a thin face (1–2 px) = unreadable mush.
- 12×12 uv on a 3×12 face = squashed door art.
- **The uv window dimensions must match the face dimensions**, and the window
  should match the projection. Audit every face.
- Small author offsets (1–3 px) read as "wrong region" — fix them.
- Mirror choices are intentional and fine (e.g. left sled shows the texture's
  left edge); only *offsets* and *stretches* are bugs.

## 4. Machine design: don't build monoliths from stretched plates

A machine made of big cubes with one plate texture stretched over each face
reads as "stretched existing blocks". What reads as purpose-built:

- **Dedicated textures per face** — front control panel ≠ side panel ≠ roof.
  Never the same decorative-block texture everywhere.
- **Real geometry variety**: stepped tiers (narrower upper tier on a wider
  base), rubber feet, overhanging top caps, recessed side panels, raised vent
  rings, layered ports (outer bolt plate + proud symbol boss), individual
  small elements instead of one big box.
- **Depth layering** (section 2) so features actually show.

## 5. Textures feeding 3D

- Design each machine face's texture **for its face**; a texture that is a
  whole "block face" stretches badly on thin elements. Reusing a decorative
  block's texture as machine casing gets called out in review.
- A texture can be 32×32 while UVs stay 0–16 (UVs scale with the image) — but
  check the *actual* PNG size matches what the model UVs assume.
- **Symbol art at small sizes**: a lightning bolt drawn as a slanted band reads
  as "thick diagonal line"; a proper bolt needs a horizontal top bar, hard
  zigzag steps, and a point. **Show ASCII art to the user for approval before
  building** — they care about symmetry (mirrored margins) and reject anything
  off by one pixel. Use the texture toolset's `scripts/preview.py ascii --mode hex`
  to present exact pixels.

## 6. BlockEntityRenderer (BESR) animation

- Pose translations are in **BLOCKS** (1.0 = one block); model element coords
  are 0–16. Mixing them up made a door slide **14.5 blocks** — always convert.
- Two-phase "real airlock" open: pop out of the frame first (clear the wall
  plane so the sliding door never intersects adjacent blocks), then glide
  sideways. Close = reverse.
- Eases: `easeOutCubic` (snappy pop), `easeInOutCubic` (smooth glide); a slow
  linear progress + per-phase ease looks pneumatic.
- "Slide to the viewer's right" for any facing: in the authored frame, sign =
  `facing.getAxis() == Axis.Z ? +1 : -1`.
- Client-only animation state lives in the block entity (tick only on
  client); the renderer reads blockstate (`OPEN`) + BE progress + partialTick.
- Register BESR models via `ModelEvent.RegisterAdditional`; render with
  `ModelBlockRenderer.renderModel(pose, buffer, state, model, …)`, fullbright
  for LEDs via `LightTexture.FULL_BRIGHT`.
- Blockstate `"y"` rotation = `rotationDegrees(-y)` (verify against the
  furnace: facing=east → y:90 maps a +z-front model to +x). Match the BESR's
  facing rotation to the blockstate or the leaf lands on the wrong side.

## 7. Workflow

1. Author the model with depth layering and per-face textures from the start.
2. Run `scripts/audit_model.py` after **every** change: z-fighting, out-of-bounds
   elements, UV dimension/region mismatches. Zero findings required.
3. For symbols/faces: present ASCII art for approval before building.
4. Keep generators (pure-stdlib Python) as the source of truth for textures
   and JSON assets so everything is reproducible.
