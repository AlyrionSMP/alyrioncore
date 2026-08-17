# AlyrionCore Modding Notes

Hard-won lessons from building AlyrionCore (NeoForge 1.21.1, Create 6.0.10 pack) —
mostly **3D model design** (overlaps, UVs, animation) and **cross-mod compat**.
The 16×16 pixel-art rules (palettes, hue shift, dithering, tiling, item
silhouettes…) live in the `minecraft-texture-design` skill — not repeated here.

---

## 1. 3D block models (vanilla JSON `elements`)

### 1.1 Z-fighting: the #1 visual bug
Elements that touch at a shared plane with **same-facing** coplanar faces
**z-fight** (flicker). Back-to-back coplanar faces (opposite facing) are fine;
**adjacent** coplanar faces (sharing an edge) are fine.

- Detector: for every pair of faces — same axis, same plane coordinate, same
  facing sign (`north/west/down` = −1, `south/east/up` = +1), and overlapping
  rectangles in the two free axes → fight.
- Fix: inset one element 0.05–0.1 along the plane normal so its face sits
  clearly behind the other (hidden) — invisible, kills the flicker.
- **Cross-model fights**: a BlockEntityRenderer (BESR) part rendered *after*
  the blockstate model z-fights it too. The airlock door leaf overlapping the
  frame's sill/header did exactly this.
- Culled faces (`"cullface"`) don't render when adjacent blocks are solid —
  pairs that are culled in normal use can still fight when floating.

### 1.2 The 0–16 boundary makes "front details" tricky
Model element coords are 0–16; nothing may protrude past 16, and anything with
z < 16 renders **behind** the front face at z = 16. So a flat front face hides
every detail placed "on" it (my first generator model looked like a plain
block for exactly this reason).

- **Depth layering**: recess the body faces (e.g., front at z=15.6, sides at
  x=15.4 / x=0.6) and put features (ports, vent rings, gauge pods) flush at the
  boundary plane (z=16 / x=16 / x=0). Features then read as proud of the body.
- Elements *can* sit slightly outside 0–16 (they just render into the next
  block's space) but that clips into adjacent blocks and z-fights their faces.

### 1.3 Vanilla UV convention (verified from `FaceInfo`/`FaceBakery` bytecode)
When you specify `"uv"` per face, the mapping is:

| Face | u axis | v axis |
|---|---|---|
| north (−z) | 16 − x | 16 − y |
| south (+z) | x | 16 − y |
| west (−x) | z | 16 − y |
| east (+x) | 16 − z | 16 − y |
| up (+y) | x | z |
| down (−y) | x | 16 − z |

To re-verify: `javap -c net.minecraft.client.renderer.FaceInfo` → the per-direction
`VertexInfo` constants; `BlockFaceUV.getU/getV` assign uv corners to vertices
in order (v0=(u1,v1), v1=(u1,v2), v2=(u2,v2), v3=(u2,v1)).

- **Crop convention**: hand-authored models map each face's projection onto the
  texture (uv = the face's x/z extent, v = 16−y for vertical faces). Full 16×16
  uv on a thin face (1–2 px) = unreadable mush; 12×12 uv on a 3×12 face =
  squashed door art. Audit every face: uv window dimensions must match face
  dimensions, and the window should match the projection (a script can flag
  both). Small author offsets (1–3 px) read as "wrong region" — fix them.
- Mirror choices are intentional and fine (left sled shows the texture's left
  edge, etc.); only *offsets* and *stretches* are bugs.

### 1.4 Machine design: don't build monoliths from stretched plates
Repeated user feedback: a machine made of big cubes with one plate texture
stretched over each face reads as "stretched existing blocks". What reads as a
purpose-built machine:

- **Dedicated textures per face** (front control panel ≠ side panel ≠ roof),
  never the same decorative-block texture everywhere.
- **Real geometry variety**: stepped tiers (narrower upper tier on a wider
  base), rubber feet, overhanging top caps, recessed side panels, raised vent
  rings, layered ports (outer bolt plate + proud symbol boss), individual small
  elements instead of one big box.
- **Depth layering** (1.2) so features actually show.

### 1.5 BlockEntity renderers: animation
- Pose translations are in **BLOCKS** (1.0 = one block); model element coords
  are 0–16. Mixing them up made a door slide **14.5 blocks** — always convert.
- Two-phase "real airlock" open: pop out of the frame first (clear the wall
  plane so the sliding door never intersects adjacent blocks), then glide
  sideways. Close = reverse. Eases: `easeOutCubic` (snappy pop), `easeInOutCubic`
  (smooth glide); a slow linear progress + per-phase ease looks pneumatic.
- "Slide to the viewer's right" for any facing: in the authored frame, sign =
  `facing.getAxis() == Axis.Z ? +1 : -1`.
- Client-only animation state lives in the block entity (tick only on client),
  and the renderer reads blockstate (`OPEN`) + BE progress + partialTick.
- Register BESR models via `ModelEvent.RegisterAdditional`; render with
  `ModelBlockRenderer.renderModel(pose, buffer, state, model, …)`, fullbright
  for LEDs via `LightTexture.FULL_BRIGHT`.
- Blockstate `"y"` rotation = `rotationDegrees(-y)` (verify against the furnace:
  facing=east → y:90 maps a +z-front model to +x). Match the BESR's facing
  rotation to the blockstate or the leaf lands on the wrong side.

---

## 2. Textures feeding 3D (the model-side of textures)
- Design each machine face's texture for its face; a texture that is a whole
  "block face" stretches badly on thin elements. Reusing a *decorative block's*
  texture as machine casing was called out repeatedly.
- A texture can be 32×32 while UVs stay 0–16 (UVs scale with the image) — but
  check the *actual* PNG size matches what the model UVs assume.
- Symbol art at small sizes: a lightning bolt drawn as a slanted band reads as
  "thick diagonal line"; a proper bolt needs a horizontal top bar, hard zigzag
  steps, and a point. **Show the ASCII art to the user for approval before
  building** — they care about symmetry (mirrored margins) and will reject
  anything off by one pixel.

---

## 3. Cross-mod compat (the big ones)

### 3.1 Capabilities: expose them on EVERY face
Gating `IEnergyStorage` / `IFluidHandler` to one face breaks real mods:

- Create pipes connect via `FluidPropagator.hasFluidCapability` →
  `Level.getCapability(Capabilities.FluidHandler.BLOCK, pos, side)`; a
  side-gated handler → no connection on other faces.
- Other FE mods' cables do the same with `Capabilities.EnergyStorage.BLOCK`.
- Fix: register both side-agnostic (`(be, side) -> (IFluidHandler) be`). Keep
  the model's power/water ports as *visual markers only*.
- Register on the MOD bus via `RegisterCapabilitiesEvent` +
  `event.registerBlockEntity(Cap.X.BLOCK, type, provider)`; the BE implements
  the interface directly. `@EventBusSubscriber(bus = MOD)` is deprecated but
  works.

### 3.2 How to learn another mod's integration points (no source needed)
- `unzip -p mod.jar '*.class' | strings` finds strings; per-class via
  `unzip -l` + `unzip -p`. `javap -c -p` on any class gives readable bytecode
  (method calls, fields, constants).
- Create pipe attachment chain: `PipeConnection.determineSource` →
  `FluidPropagator.hasFluidCapability` / `canConnectTo` → capability lookup;
  `shouldDrawRim` renders the connector. Create 6.0.10's own recipes show exact
  JSON formats (compacting/mixing/milling/mechanical_crafting); vanilla
  `Ingredient` has **no count field** — multi-item inputs repeat entries
  (Create itself compacts 9 snow blocks → ice exactly that way).
- **Ghost items**: a mod can ship a lang entry + item model for an item it
  never registers (`rocketnautics:fluid_hose`). A recipe outputting it fails
  with "Unknown registry key". Verify registration by grepping the class
  constant pools for the id *before* writing recipes that reference it.

### 3.3 Recipe conditions & fallbacks
- `neoforge:conditions` on recipes and loot tables; `neoforge:not` =
  `{"type":"neoforge:not","value":{...}}` (verify schema from `NotCondition`).
  Use for "Create installed → machine recipe, Create absent → crafting
  fallback".
- Convert crafting→Create only where it makes sense: mechanical crafting for
  complex devices, **compacting as an ADDITIONAL path** (Create itself keeps
  crafting for storage blocks), mixing for chemistry, milling for grinding;
  keep tools/unpacking/masonry on the crafting table.

### 3.4 Startup crashes: find the FIRST error
- Crash-report headlines can be pure side effects (a Sodium "config missing"
  crash was caused by an earlier, unrelated failure).
- Read `logs/latest.log` / `debug.log` for the first ERROR. Here: Create failed
  construction — `AllSoundEvents.prepare()` iterated its static map while a
  Create addon (Create: New Age) registered sounds into it during **parallel
  mod construction** (ForkJoinPool) → `ConcurrentModificationException`,
  intermittent.
- Fix: `config/fml.toml` → `maxThreads = 1` serializes mod construction
  (slower startup, kills the whole class of parallel-registration races).

---

## 4. Code gotchas (NeoForge 1.21.1 / Parchment)
- `BlockEntity.saveAdditional/loadAdditional` take `(CompoundTag,
  HolderLookup.Provider)` — NOT `HolderGetter<RegistryAccess>` (verify with
  `javap` on the neoform-compiled jar before trusting memory).
- `BlockBehaviour` already has a static `properties()` — don't name a helper
  that; call it `machineProperties()`.
- Client-vs-server: breathe/air events fire on both sides; keep the client air
  bar in sync or bubbles drain visually. `setAirSupply` every tick is the
  bulletproof refill; capability lookups need BE `getLevel()` non-null (fine
  client-side).
- **Stale jars** caused repeated "nothing changed" reports: rebuild + install
  + FULL game restart after every change.
- Build environment: no system JDK → workspace `.tools/jre`; sandbox blocks
  `posix_spawn` → `JAVA_TOOL_OPTIONS=-Djdk.lang.Process.launchMechanism=fork`;
  `GRADLE_USER_HOME` in-workspace.

---

## 5. Workflow that works
- Write **detector scripts** instead of eyeballing: coplanar-face detection,
  UV dimension/region audits, item/recipe registration checks — run them after
  every model change and report zero findings.
- For visual/symbol changes, present **ASCII art for approval before building**
  (the user catches symmetry and pixel-level issues in ASCII instantly).
- Iterate in small commits; keep generators (pure-stdlib Python, `mc-scripts/`)
  as the source of truth for textures and JSON assets so changes are
  reproducible.
- Git hygiene: `git add -A` can sweep extracted third-party jars / scratch
  dirs into history — gitignore them and audit staged files.
