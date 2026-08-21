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

---

## 6. "Reinforced blocks" (X-hits mechanic) — the pattern that works

Ship-block-reinforcement on top of ANY block without new textures:

- **Wrapper block + BlockEntity**: right-click with a plate replaces the target
  with one `reinforced_block` (blockstate property = tier) whose BE stores the
  ORIGINAL `BlockState` (via `NbtUtils.writeBlockState`) + remaining hits.
  Exclude air/fluids/unbreakable (`getDestroySpeed < 0`) and anything with a BE
  (chests/machines would lose their data). No BlockItem, no loot table.
- **Hit absorption** = cancel `BlockEvent.BreakEvent` (fires inside the
  patched `ServerPlayerGameMode.destroyBlock`, BEFORE removal, for both the
  STOP_DESTROY_BLOCK and the delayed-tick paths — verified via javap). Cancel →
  `CommonHooks.fireBlockBreak` sends a block update; ALSO send
  `ClientboundBlockDestructionPacket(id, pos, -1)` yourself to clear the crack
  (the delayed path never clears it). The client's local progress resets after
  each STOP, so holding the button re-cycles → "N hits".
- **Drops = original block only**: override `playerDestroy` — vanilla passes the
  PRE-removal BE capture, so read the original state and `Block.getDrops(orig,
  serverLevel, pos, be, player, tool)` → silk touch/fortune of the ORIGINAL
  block just work. Don't set `requiresCorrectToolForDrops` on the wrapper (it
  gates whether `playerDestroy` even runs). **Gotcha**: the correct-tool drop
  gate lives in `ServerPlayerGameMode.destroyBlock`
  (`player.hasCorrectToolForDrops(state)` before `playerDestroy`), NOT in the
  loot table — stone's loot table happily returns stone to a bare fist. So a
  wrapper that always runs `playerDestroy` and blindly spawns the original's
  `getDrops` drops stone without a pickaxe. Replicate the gate yourself:
  `if (player.hasCorrectToolForDrops(original)) { spawn drops }`.
- **"As hard as the protected block"**: `BlockState.getDestroySpeed` is a per-
  state FIELD (set from `strength()`), NOT block-dispatched — you can't make a
  wrapper block's hardness dynamic by overriding it. Override
  `BlockBehaviour.getDestroyProgress(state, player, level, pos)` (protected,
  called by both server progress accumulation and the client's local progress)
  and delegate to `original.getDestroyProgress(player, level, pos)` — the
  original's hardness, tool multipliers and correct-tool divisor all apply,
  and client/server crack timing stays in sync because the BE is synced.
- **Explosions must match the protected block** (TNT breaks reinforcement iff
  it breaks the original): NeoForge patched `ExplosionDamageCalculator` to call
  the context-aware `IBlockStateExtension.getExplosionResistance(level, pos,
  explosion)` → block-side `getExplosionResistance(state, level, pos,
  explosion)` — override it and delegate to the original (the no-arg
  `Block.getExplosionResistance()` has NO position context, useless here).
  And `Block.onExplosionHit(state, level, pos, explosion, BiConsumer)` is the
  WHOLE per-block explosion handler — drops (explosion loot params), block
  removal (setBlock air) and `wasExploded` — delegate it to the original so
  the explosion drops the original's loot and removes the block.
- **Rendering**: blockstate model = a protruding riveted-plate frame
  (elements poke 0.1 past 16/-0.1; chunk culling drops the faces next to solid
  blocks → plates only on air-facing sides). A BESR then draws the ORIGINAL
  model inside via `BlockRenderDispatcher.renderBatched(state, pos, level,
  pose, consumer, solid=true, random)` with the pose already at the block pos
  (quads are 0..16 local; `solid=true` gives world culling + AO). Frame faces
  never z-fight the original model: the frame's inner faces face INWARD at the
  same planes the original's faces face OUTWARD, and overlaps are zero-area.
  `audit_model.py` flags the 0.1 protrusion as out-of-bounds — that flag is the
  intended design (an inset frame would be hidden behind the block's own face;
  a flush one would z-fight it).
- Creative players bypass hits (`player.getAbilities().instabuild` → don't
  cancel). `inspect_jar.py has-id` gives false "ghost item" warnings for
  DeferredRegister ids (namespace is split from the id string) — grep the
  class constant pool for the bare id instead.
- **The crack overlay must NEVER touch the block**: the ONLY thing that may
  change is what the renderer draws. `level.setBlock` to advance a cosmetic
  blockstate property is a trap: on the SERVER the chunk's `setBlockState`
  re-creates the block entity (`newBlockEntity`) when
  `getBlockEntity(pos, CHECK)` finds nothing usable, and even when it is kept,
  the client's destroy prediction has already dropped ITS block entity and
  re-created it empty — so the wrapper suddenly renders with no original block
  inside ("clear block, reinforced shell only") and its hardness falls back to
  the wrapper defaults. Keep the blockstate static (tier only) and drive the
  crack from the BE: `ReinforcedBlockEntity.crackStage` (0..7, in
  save/load + update tag), set in the absorb handler; the BESR renders one of 8
  registered additional models (full cube at ±0.12, cutout) on top of the
  original model. The absorb handler then schedules a 1-tick `tick` that ONLY
  re-sends `be.getUpdatePacket()` to every player — the destroy prediction
  drops the client's BE and re-creates it EMPTY, and this guaranteed re-send
  (past the prediction window, after the block-changed ack) restores the
  original state + crack stage. Keep the wrapper `strength()` hardness NORMAL
  (2.0) as a fallback so a momentarily-empty client BE never grinds at
  obsidian speed. And give the block an EMPTY lang value
  (`"block.alyrioncore.reinforced_block": ""`) so its name never shows — it is
  a barrier-like wrapper.


## 7. Time-based game state (the habitat oxygen fill)

"Filling a sealed room takes 0.5 s per block, generators stack linearly" —
how to implement gradual state without per-tick ticking:

- **Deterministic room identity**: the flood fill already visits every interior
  cell; the SET is identical for any query position in the same room, so the
  minimum interior cell (tracked as `min` of the visited `long` keys during the
  fill) is a stable room key. Map: `dimension × anchor → RoomState`.
- **Lazy elapsed-time updates instead of ticking**: store `lastUpdateTick`;
  on every query advance `oxygen += rate * (now - lastUpdateTick)/20`.
  A room with a running generator fills at the correct speed even while nobody
  is inside — the math catches up on the next query. Never loop rooms.
- **Server-only advancement**: in an INTEGRATED server the client shares this
  JVM (and its statics!). Breathe events fire on BOTH logical sides → the
  client would advance the same room entry a second time per tick and double
  the fill speed. Gate the mutation on `!level.isClientSide`; clients read
  (their air bar sees the last server value, ≤1 tick stale).
- **Semantics that make decay meaningful**: breathable = fill ≥ 100%. If you
  used "any oxygen = breathable", the fill time would only delay by the FIRST
  0.5 s. With full-or-nothing, decay (unpowered room drains at the same 2/s)
  costs breathability instantly while preserving the partial fill for a faster
  refill — power blips feel fair, breaches reset everything
  (`onBreach` removes the entry → refill from scratch).
- **Cache the seal, refresh the fill**: the seal scan itself is stable for a
  cache window, but the oxygen flag changes every tick — cache the room info
  (anchor/volume/generator count) alongside the seal and recompute the flag
  from the tracker on every hit. Generator counts are scan-time values, so
  they lag up to one cache window (2 s) after a generator dies — same
  staleness the old instant-flag had, so no regression.
- **Don't forget the sleeping pod**: an airtight START cell (player embedded /
  sleeping) early-returns without scanning — the sleeper's cell IS the pod, so
  pods must fall through to the flood fill or sleeping in a pod always drowns.
  Airtight start cells that AREN'T pods keep the early return (flooding from
  inside a thin wall escapes through the far side).
- Clear all room state on `ServerStartedEvent` (game bus) — dimension keys
  repeat across worlds, so stale oxygen would otherwise leak into a new world.
- **"It repressurized after I broke blocks!" — two separate traps**:
  1. *The break handler vented too eagerly.* "Adjacent to a sealed cell" is NOT
     a breach: breaking an interior block (chest, pillar, machine) or a redundant
     boundary (inner layer of a thick wall, floor over solid terrain, wall
     between two sealed rooms) leaves the room airtight. Fix: gate on
     `isAirtight(brokenState)` first (only boundary blocks can breach), then
     SIMULATE the removal by running the flood fill from the broken cell with a
     `startIsPassable` flag (the scan treats its start cell as air even though
     the block is still present during `BreakEvent`). Only if that simulated
     scan ESCAPES (OPEN_AIR) is the room genuinely open to vacuum → vent +
     particle burst + cache clear. A sealed result means nothing vents.
  2. *The room key moved.* The anchor is the min interior cell — breaking a
     block at the room's min-extreme adds a smaller cell and silently re-keys
     the room, dropping the fill to 0 even with no leak. Fix: when a scan finds
     a sealed room whose anchor has no entry, ADOPT oxygen from any known room
     whose anchor is still inside the current interior (the room grew, or two
     rooms merged through a hole) — carry the max fill, drop the old entries.
     Pass the flood fill's visited set to the tracker for the O(1) membership
     test. Resets then only happen on genuine breaches (vented) or when the
     room physically shrinks past its old min cell (you rebuilt it).
