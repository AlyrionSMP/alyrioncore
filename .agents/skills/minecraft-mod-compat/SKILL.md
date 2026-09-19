---
name: minecraft-mod-compat
description: "Use when integrating a NeoForge 1.21.1 mod with other mods (Create, FE cables) or debugging a modpack: capabilities on every face, reverse-engineering jars without source, recipe conditions and fallbacks, startup crash triage, code gotchas, and the detector-script workflow. Pair with minecraft-model-design and minecraft-texture-design."
compatibility: "Scripts are in scripts/ at the preset root, symlinked from scripts/ in each skill directory. Run as scripts/<name>.py from the skill root. In DSH, scripts are also injected as tools."
---

# Minecraft Mod Integration & Compatibility (NeoForge 1.21.1)

Hard-won lessons from building AlyrionCore (NeoForge 1.21.1, Create 6.0.10
pack). Cross-mod compat, reverse engineering, recipe conditions, crash
triage, and code gotchas. 3D models/UVs live in `minecraft-model-design`;
textures live in `minecraft-texture-design`.

---

## 1. Capabilities: expose them on EVERY face

Gating `IEnergyStorage` / `IFluidHandler` to one face breaks real mods:

- Create pipes connect via `FluidPropagator.hasFluidCapability` →
  `Level.getCapability(Capabilities.FluidHandler.BLOCK, pos, side)`; a
  side-gated handler → no connection on other faces.
- Other FE mods' cables do the same with
  `Capabilities.EnergyStorage.BLOCK`.
- Fix: register both side-agnostic — `(be, side) -> (IFluidHandler) be`.
  Keep the model's power/water ports as *visual markers only*.
- Register on the MOD bus via `RegisterCapabilitiesEvent` +
  `event.registerBlockEntity(Cap.X.BLOCK, type, provider)`; the BE implements
  the interface directly. `@EventBusSubscriber(bus = MOD)` is deprecated but
  works.

## 2. Reverse-engineering another mod (no source needed)

Use the bundled helper (wraps unzip/strings/javap, pure stdlib):

    python3 scripts/inspect_jar.py strings create.jar pipe       # strings in class files
    python3 scripts/inspect_jar.py classes create.jar Pipe       # class list
    python3 scripts/inspect_jar.py javap create.jar 'com/simibubi/create/foundation/fluid/FluidPropagator.class'
    python3 scripts/inspect_jar.py has-id create.jar rocketnautics:fluid_hose   # ghost-item check

Manual equivalents: `unzip -l mod.jar`, `unzip -p mod.jar '*.class' | strings`,
`javap -c -p <class>` for readable bytecode (method calls, fields, constants).

Known chain (Create 6.0.10): pipe attachment →
`PipeConnection.determineSource` → `FluidPropagator.hasFluidCapability` /
`canConnectTo` → capability lookup; `shouldDrawRim` renders the connector.

**Recipe JSON formats**: Create's own recipes show the exact JSON for
compacting/mixing/milling/mechanical_crafting. Vanilla `Ingredient` has **no
count field** — multi-item inputs repeat entries (Create compacts 9 snow
blocks → ice exactly that way).

**Ghost items**: a mod can ship a lang entry + item model for an item it
never registers (`rocketnautics:fluid_hose`). A recipe outputting it fails
with "Unknown registry key". Verify registration by grepping the class
constant pools for the id **before** writing recipes that reference it
(`scripts/inspect_jar.py has-id`).

## 3. Recipe conditions & fallbacks

- `neoforge:conditions` on recipes and loot tables; `neoforge:not` =
  `{"type":"neoforge:not","value":{...}}` (verify the schema from
  `NotCondition`). Use for: "Create installed → machine recipe, Create absent
  → crafting fallback".
- Convert crafting→Create only where it makes sense:
  - mechanical crafting for complex devices,
  - **compacting as an ADDITIONAL path** (Create itself keeps crafting for
    storage blocks),
  - mixing for chemistry, milling for grinding,
  - keep tools/unpacking/masonry on the crafting table.

## 4. Startup crashes: find the FIRST error

- Crash-report headlines can be pure side effects (a Sodium "config missing"
  crash was caused by an earlier, unrelated failure).
- Read `logs/latest.log` / `debug.log` for the first ERROR.
- Real example: Create failed construction — `AllSoundEvents.prepare()`
  iterated its static map while a Create addon (Create: New Age) registered
  sounds into it during **parallel mod construction** (ForkJoinPool) →
  `ConcurrentModificationException`, intermittent.
- Fix: `config/fml.toml` → `maxThreads = 1` serializes mod construction
  (slower startup, kills the whole class of parallel-registration races).

## 5. Code gotchas (NeoForge 1.21.1 / Parchment)

- `BlockEntity.saveAdditional/loadAdditional` take `(CompoundTag,
  HolderLookup.Provider)` — NOT `HolderGetter<RegistryAccess>` (verify with
  `javap` on the neoform-compiled jar before trusting memory).
- `BlockBehaviour` already has a static `properties()` — don't name a helper
  that; call it `machineProperties()`.
- Client-vs-server: breathe/air events fire on both sides; keep the client
  air bar in sync or bubbles drain visually. `setAirSupply` every tick is the
  bulletproof refill; capability lookups need BE `getLevel()` non-null (fine
  client-side).
- **Stale jars** caused repeated "nothing changed" reports — rebuild +
  install + FULL game restart after every change.
- Build environment without a system JDK: use the workspace `.tools/jre`;
  if the sandbox blocks `posix_spawn`, set
  `JAVA_TOOL_OPTIONS=-Djdk.lang.Process.launchMechanism=fork`; keep
  `GRADLE_USER_HOME` in-workspace.

## 6. Workflow that works

- **Detector scripts instead of eyeballing**: coplanar-face detection
  (`scripts/audit_model.py`), UV dimension/region audits (`scripts/audit_model.py`),
  item/recipe registration checks (`scripts/inspect_jar.py has-id`). Run after every
  change; report zero findings.
- For visual/symbol changes, **present ASCII art for approval before
  building** — the user catches symmetry and pixel-level issues in ASCII
  instantly.
- Iterate in small commits; keep generators (pure-stdlib Python) as the
  source of truth for textures and JSON assets so changes are reproducible.
- Git hygiene: `git add -A` can sweep extracted third-party jars / scratch
  dirs into history — gitignore them and audit staged files.
