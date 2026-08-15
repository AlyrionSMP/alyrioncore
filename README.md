# 🪐 AlyrionCore

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg?logo=minecraft)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1.186-orange.svg)](https://neoforged.net/)
[![Java](https://img.shields.io/badge/Java-21-blue.svg?logo=openjdk)](https://adoptium.net/)
[![License](https://img.shields.io/badge/License-All_Rights_Reserved-lightgrey.svg)]()

**AlyrionCore** is the foundational core mod for the **Alyrion SMP**, engineered to deliver a scientifically grounded, highly immersive Mars planetary dimension alongside server enhancements and planetary physics simulation.

From realistic 0.38g surface gravity and true-to-life forward Mie scattering blue sunsets to accurate geological stratigraphy, volcanology, and solid $CO_2$ dry ice sublimation, AlyrionCore transforms extraterrestrial exploration in Minecraft into an authentic scientific voyage.

---

## 📑 Table of Contents

- [Overview & Philosophy](#-overview--philosophy)
- [Key Features at a Glance](#-key-features-at-a-glance)
- [The Mars Dimension (`alyrioncore:mars`)](#-the-mars-dimension-alyrioncoremars)
  - [Planetary & Dimension Properties](#planetary--dimension-properties)
  - [Atmospheric Rendering & Celestial Optics](#atmospheric-rendering--celestial-optics)
- [Planetary Physics: Authentic Gravity Simulation](#-planetary-physics-authentic-gravity-simulation)
- [Martian Biomes & Regional Geography](#-martian-biomes--regional-geography)
- [Geology, Blocks & Materials](#-geology-blocks--materials)
  - [Martian Regolith & Surface Soils](#martian-regolith--surface-soils)
  - [Stones, Volcanics & Decorative Masonry](#stones-volcanics--decorative-masonry)
  - [Extraterrestrial Ores & Minerals](#extraterrestrial-ores--minerals)
  - [Polar & Volatile Ices](#polar--volatile-ices)
- [Items & Resource Economy](#-items--resource-economy)
- [Creative Mode Integration](#-creative-mode-integration)
- [Client & Quality of Life Systems](#-client--quality-of-life-systems)
  - [Universal Rebindable Escape / Close Screen Action](#universal-rebindable-escape--close-screen-action)
- [Tool Requirements & Mining Tags](#-tool-requirements--mining-tags)
- [Development, Setup & Building](#-development-setup--building)
- [License & Credits](#-license--credits)

---

## 🔬 Overview & Philosophy

AlyrionCore was designed with a clear directive: **bridge the gap between authentic planetary science and engaging sandbox gameplay**. 

Rather than relying on fantasy tropes, the mod models real-world geological features discovered by NASA, ESA, and planetary rovers (Curiosity, Perseverance, Opportunity):
- **Ferric Oxide Optics**: Ambient daylight is tinted by suspended hematite dust, while sunsets are blue due to forward Mie scattering.
- **Physical Atmospheric Behavior**: Solid Carbon Dioxide ($CO_2$) sublimes directly into gas without melting into liquid.
- **True Surface Gravity**: Accurate calculation of Mars's surface acceleration ($3.72\ \text{m/s}^2 \approx 0.38g$).
- **Authentic Geological Formations**: Hematite "blueberries", meteoric nickel-iron alloys, olivine crystal beds, impact breccia, and stratified canyon stone.

---

## ⚡ Key Features at a Glance

| Feature | Description | Implementation Details |
|---|---|---|
| **Mars Dimension** | Full extraterrestrial dimension with 384-block world height ($Y = -64$ to $Y = 320$). | `alyrioncore:mars`, multi-noise generator |
| **0.38g Planetary Gravity** | Living entities experience 38% of Earth gravity while on Mars. | NeoForge Attribute Modifier (`Attributes.GRAVITY`, $-62\%$) |
| **Authentic Blue Sunsets** | Forward Mie scattering calculations simulate true blue Martian twilights. | Custom `DimensionSpecialEffects` pipeline |
| **Butterscotch Day Fog** | Suspended ferric dust creates an amber-ochre haze under the Martian sun. | RGB-weighted fog color transformation |
| **5 Realistic Biomes** | Real Martian regions: *Vastitas Borealis*, *Valles Marineris*, *Tharsis*, *Planum Boreum*, *Noachis Terra*. | Scientifically mapped multi-noise parameter splines |
| **CO₂ Dry Ice Sublimation** | Dry ice blocks dynamically release visible sublimation vapor and frost particles into the air. | Custom `DryIceBlock` with animated particle emission |
| **Rich Geological Catalog** | 16 unique blocks including volcanics, bricks, ores, breccia, and soils. | Fully tagged with pickaxe/shovel tools & tiers |
| **Universal Escape Keybind** | Rebind the standard Escape key action (Pause/Close GUI) to mouse buttons or other keys. | Custom `ModKeyMappings` with screen event routing |
| **Dedicated Creative Tab** | Custom item group organizing all planetary samples, minerals, and blocks. | `alyrioncore:mars_tab` |

---

## 🚀 The Mars Dimension (`alyrioncore:mars`)

### Planetary & Dimension Properties

Mars is registered under the identifier `alyrioncore:mars` with a dedicated dimension type `alyrioncore:mars_type`.

```
ResourceKey: alyrioncore:mars
Dimension Type: alyrioncore:mars_type
Noise Generator: alyrioncore:mars_noise_settings
```

- **Height Range**: $Y = -64$ to $Y = 320$ (Total height: 384 blocks, Logical height: 384 blocks).
- **Coordinate Scale**: `1.0` (1:1 spatial mapping with other standard dimensions).
- **Celestial & Light Dynamics**:
  - `has_skylight: true` – Natural sunlight penetrates the thin atmosphere.
  - `ambient_light: 0.08` – Dim ambient planetary illumination.
  - `monster_spawn_light_level: [0, 7]` – Monsters spawn under darkness conditions.
  - `monster_spawn_block_light_limit: 0` – Controlled hostile mob spawning.
- **Habitation & Respawns**:
  - `bed_works: false` – Beds are unsafe and do not work in the vacuum/thin atmosphere.
  - `respawn_anchor_works: true` – Respawn Anchors function as stable planetary spawn beacons.
  - `has_raids: false` – Illager raids cannot initiate on Mars.
  - `ultrawarm: false`, `natural: false`.

---

### Atmospheric Rendering & Celestial Optics

The visual environment of Mars is handled by `MarsDimensionEffects.java`:

#### 1. Butterscotch Daylight Sky & Dust Scattering
Because fine iron oxide (ferric dust) is permanently suspended in the thin Martian troposphere, Rayleigh scattering is subdued and dust-induced scattering dominates. During daylight hours, the fog color is mathematically transformed into a characteristic butterscotch / rusty-amber tone:

$$\vec{C}_{\text{fog}} = \text{brightness} \times \begin{pmatrix} 0.85 \times (0.4 R_0 + 0.6) \\ 0.52 \times (0.4 G_0 + 0.6) \\ 0.32 \times (0.4 B_0 + 0.6) \end{pmatrix}$$

#### 2. Forward Mie Scattering Blue Sunset & Sunrise
On Earth, fine gas molecules scatter blue light in all directions (making the sky blue and sunsets red). On Mars, microscopic dust particles are comparable in size to the wavelength of visible light. This creates **forward Mie scattering**, allowing blue wavelengths to pass straight through the dusty horizon directly surrounding the Sun while red wavelengths scatter outward into the wider sky.

The mod computes exact angular solar color transitions:
- **Red component**: Subdued ($f_1 \times 0.25 + 0.15$)
- **Green component**: Balanced ($f_1^2 \times 0.55 + 0.35$)
- **Blue component**: Dominant vivid azure ($f_1^2 \times 0.95 + 0.55$)

#### 3. Cloudless Thin Atmosphere
Water-vapor clouds are disabled (`cloudHeight = Float.NaN`), representing the extremely dry, thin, and desiccated Martian atmosphere.

---

## 🪂 Planetary Physics: Authentic Gravity Simulation

Mars possesses a surface gravity of approximately **$3.72\ \text{m/s}^2$**, which corresponds to **~38% of Earth's standard gravity ($0.38g$)**.

### How It Works (`MarsPhysicsHandler.java`)

1. **Attribute Integration**: Utilizes NeoForge's entity attribute system (`Attributes.GRAVITY`).
2. **Mathematical Modifier**:
   - An `AttributeModifier` with value `-0.62` (`ADD_MULTIPLIED_BASE`) is applied to every `LivingEntity` within `alyrioncore:mars`.
   - $\text{Effective Gravity} = \text{Base Gravity} \times (1.0 - 0.62) = 0.38 \times \text{Base Gravity}$.
3. **Dynamic Application & Safety**:
   - Runs on `EntityTickEvent.Post`.
   - Automatically attaches transient modifiers when an entity arrives on Mars.
   - Automatically and cleanly strips the modifier when the entity teleports back to the Overworld or another dimension, preventing state pollution.

### Gameplay Impact
- **Higher, Looping Jumps**: Players and mobs jump significantly higher.
- **Extended Hang Time & Distances**: Horizontal leap distances are substantially extended.
- **Reduced Fall Velocity**: Slower terminal velocity and floatier descent mechanics.

---

## 🗺️ Martian Biomes & Regional Geography

AlyrionCore features **5 scientifically distinct Martian biomes**, each representing actual physiographic provinces and surface regions on Mars:

```
                  ┌─────────────────────────────────────────────────┐
                  │          PLANUM BOREUM (Polar Cap)             │
                  │   Dry Ice CO₂, Glacial Ice, Permafrost, Frost   │
                  └────────────────────────┬────────────────────────┘
                                           │
       ┌───────────────────────────────────┼───────────────────────────────────┐
       │                                   │                                   │
┌──────┴──────────────────────┐ ┌──────────┴──────────┐ ┌──────────────────────┴──────┐
│     VASTITAS BOREALIS       │ │   VALLES MARINERIS   │ │  THARSIS VOLCANIC PLATEAU  │
│ Flat Sedimentary Lowlands   │ │ Massive Rift Canyon  │ │ Basalt Shield Volcanos &   │
│ Martian Sand & Regolith     │ │ Stratified Terraces  │ │ Scoria Formations          │
└─────────────────────────────┘ └──────────────────────┘ └────────────────────────────┘
                                           │
                  ┌────────────────────────┴────────────────────────┐
                  │           NOACHIS TERRA (Highlands)            │
                  │ Impact Breccia, Cratered Basins, Hematite Ore   │
                  └─────────────────────────────────────────────────┘
```

### Biome Profiles

#### 1. Vastitas Borealis (`alyrioncore:vastitas_borealis`)
- **Scientific Context**: The vast, smooth northern circumpolar lowlands created by ancient sediment deposits and vast plains.
- **Terrain**: Expansive, low-elevation plains.
- **Dominant Materials**: Fine Martian Sand, Martian Regolith, coarse dust drifts.
- **Climate Parameters**: `temperature: 0.0`, `humidity: -0.5`, `continentalness: -0.2`, `erosion: 0.3`.

#### 2. Valles Marineris (`alyrioncore:valles_marineris`)
- **Scientific Context**: The grand canyon system of Mars, stretching over 4,000 km long and up to 7 km deep, formed by tectonic rifting.
- **Terrain**: Deep chasms, stepped cliffs, canyon walls, and exposed sedimentary layers.
- **Dominant Materials**: Stratified Martian Stone, exposed mineral veins, deep basalt outcroppings.
- **Climate Parameters**: `temperature: 0.2`, `humidity: -0.2`, `continentalness: 0.4`, `erosion: -0.7`, `depth: 0.6`.

#### 3. Tharsis Volcanic Plateau (`alyrioncore:tharsis_volcanic_plateau`)
- **Scientific Context**: The colossal volcanic plateau home to Olympus Mons and the Tharsis Montes shield volcanoes.
- **Terrain**: Elevated volcanic ridges, basalt fields, lava channels, and scoria deposits.
- **Dominant Materials**: Martian Basalt, Martian Volcanic Scoria, Martian Sulfur Ore.
- **Climate Parameters**: `temperature: 0.7`, `humidity: -0.8`, `continentalness: 0.8`, `erosion: 0.5`.

#### 4. Planum Boreum (`alyrioncore:planum_boreum`)
- **Scientific Context**: The permanent northern polar ice cap of Mars, consisting of stratified water ice and seasonal solid $CO_2$ frost.
- **Terrain**: Glacial ice sheets, frost-dusted ridges, and polar permafrost.
- **Dominant Materials**: Martian Glacial Ice, Martian Dry Ice Block, Martian Permafrost, Frost-Dusted Regolith.
- **Climate Parameters**: `temperature: -0.9`, `humidity: 0.4`, `continentalness: -0.6`, `erosion: 0.0`.

#### 5. Noachis Terra (`alyrioncore:noachis_terra`)
- **Scientific Context**: One of the most ancient landforms on Mars, heavily cratered from the Late Heavy Bombardment.
- **Terrain**: Rugged crater rims, ejecta blankets, and weathered highland plateaus.
- **Dominant Materials**: Martian Impact Breccia, Coarse Martian Regolith, Hematite Ore, Meteoric Nickel-Iron Ore.
- **Climate Parameters**: `temperature: -0.2`, `humidity: -0.6`, `continentalness: 0.2`, `erosion: 0.8`.

---

## 🧱 Geology, Blocks & Materials

### Martian Regolith & Surface Soils

| Block | Identifier | Map Color | Hardness / Resistance | Sound Type | Primary Tool | Description |
|---|---|---|---|---|---|---|
| **Martian Sand** | `martian_sand` | Red (`#C05832`) | `0.5` / `0.5` | `SAND` | Shovel | Fine iron-oxidized falling sand (`FallingBlock`) with custom dust particle color. |
| **Martian Regolith** | `martian_regolith` | Terracotta Orange | `0.8` / `0.8` | `GRAVEL` | Shovel | Fine-grained planetary soil covering the surface of Mars. |
| **Coarse Martian Regolith** | `coarse_martian_regolith` | Terracotta Red | `0.9` / `0.9` | `GRAVEL` | Shovel | Denser, pebble-strewn regolith found in highland areas. |
| **Frost-Dusted Regolith** | `frost_dusted_regolith` | Light Gray | `0.8` / `0.8` | `SNOW` | Shovel | Regolith blanketed with micro-crystals of water and $CO_2$ frost. |
| **Martian Permafrost** | `martian_permafrost` | Ice | `1.8` / `3.0` | `GLASS` | Pickaxe | Deep frozen soil cemented by subterranean water ice. |

---

### Stones, Volcanics & Decorative Masonry

| Block | Identifier | Map Color | Hardness / Resistance | Sound Type | Tool Required | Description |
|---|---|---|---|---|---|---|
| **Martian Basalt** | `martian_basalt` | Gray | `1.8` / `6.0` | `DEEPSLATE` | Pickaxe (Stone+) | Dense, dark volcanic igneous rock forming the Martian crust. |
| **Polished Martian Basalt** | `polished_martian_basalt` | Gray | `2.0` / `6.0` | `STONE` | Pickaxe (Stone+) | Smooth-cut basalt ideal for modern extraterrestrial habitats. |
| **Martian Basalt Bricks** | `martian_basalt_bricks` | Gray | `2.0` / `6.0` | `STONE` | Pickaxe (Stone+) | Refined basalt bricks designed for structural integrity. |
| **Martian Basalt Tiles** | `martian_basalt_tiles` | Gray | `2.0` / `6.0` | `STONE` | Pickaxe (Stone+) | Finished basalt tiling for floors and interior architecture. |
| **Stratified Martian Stone** | `stratified_martian_stone` | Terracotta Brown | `1.5` / `6.0` | `STONE` | Pickaxe (Stone+) | Sedimentary stone with visible geological layering. |
| **Martian Volcanic Scoria** | `martian_volcanic_scoria` | Black | `1.4` / `4.0` | `BASALT` | Pickaxe (Stone+) | Porous, lightweight, vesicular volcanic rock from shield volcanoes. |
| **Martian Impact Breccia** | `martian_impact_breccia` | Terracotta Gray | `2.2` / `8.0` | `TUFF` | Pickaxe (Iron+) | Ultra-dense rock composed of angular fragments fused by meteorite impacts. |

---

### Extraterrestrial Ores & Minerals

| Ore Block | Identifier | Hardness / Resistance | Tool Required | Primary Mineral / Item |
|---|---|---|---|---|
| **Martian Hematite Ore** | `hematite_ore` | `3.0` / `3.0` | Pickaxe (Stone+) | Yields **Hematite Nodules** ($Fe_2O_3$ concretions). |
| **Meteoric Nickel-Iron Ore** | `meteoric_iron_ore` | `4.5` / `5.0` | Pickaxe (Iron+) | High-grade meteoric deposits; yields **Raw Meteoric Iron**. |
| **Martian Copper Ore** | `martian_copper_ore` | `3.0` / `3.0` | Pickaxe (Stone+) | Native planetary copper veins; yields **Raw Martian Copper**. |
| **Martian Sulfur Ore** | `martian_sulfur_ore` | `2.5` / `3.0` | Pickaxe (Stone+) | Hydrothermal / volcanic deposits; yields **Martian Sulfur Dust**. |
| **Martian Olivine Ore** | `martian_olivine_ore` | `3.5` / `4.0` | Pickaxe (Iron+) | Ultramafic silicate crystals; yields **Olivine Crystals** (Peridot). |

---

### Polar & Volatile Ices

#### 1. Martian Glacial Ice (`martian_ice`)
- **Composition**: Dense, ancient water ice ($H_2O$) preserved in subterranean beds and polar caps.
- **Properties**: Hardness `1.2`, Friction `0.98` (high slipperiness), Glass sound. Requires a pickaxe.

#### 2. Martian Dry Ice / Solid $CO_2$ (`dry_ice_block`)
- **Composition**: Compressed solid carbon dioxide ($CO_2$).
- **Friction**: `0.985` (Ultra-low surface friction).
- **Sublimation Particle Mechanics (`DryIceBlock.java`)**:
  - In Martian conditions, solid dry ice transitions directly from a solid into a gas without turning into liquid.
  - During block ticks (`animateTick`), the block emits:
    - `ParticleTypes.CLOUD` vapor particles rising directly off the upper surface.
    - `ParticleTypes.SNOWFLAKE` sub-zero micro-frost particles drifting around the block.

---

## 💎 Items & Resource Economy

| Item | Identifier | Category | Lore & Scientific Description |
|---|---|---|---|
| **Hematite Nodule** | `hematite_nodule` | Mineral | Known informally as **"Martian Blueberries"**—small spherical iron oxide concretions formed in the presence of ancient liquid water. |
| **Raw Meteoric Iron** | `raw_meteoric_iron` | Metal | Unrefined chunks of extraterrestrial nickel-iron meteorites harvested from impact craters. |
| **Meteoric Iron Ingot** | `meteoric_iron_ingot` | Metal | Smelted and refined high-durability alloy ingot with superior structural resilience. |
| **Raw Martian Copper** | `raw_martian_copper` | Metal | Pure native copper extracted from Martian hydrothermal veins. |
| **Martian Sulfur Dust** | `sulfur_dust` | Chemical | Fine volcanic sulfur powder collected from geothermal vents and sulfur beds in Tharsis. |
| **Olivine Crystal** | `olivine_gem` | Gemstone | Translucent green magnesium iron silicate crystal ($(\text{Mg, Fe})_2\text{SiO}_4$) found in pristine igneous intrusions. |
| **Dry Ice Shard** | `dry_ice_shard` | Volatile | Sub-zero crystallized shard of solid $CO_2$ harvested from polar ice caps. |
| **Martian Rock Sample** | `martian_rock_sample` | Research | Geological sample of Martian rock collected for planetary analysis and scientific cataloging. |

---

## 🎨 Creative Mode Integration

AlyrionCore adds a dedicated Creative Mode Tab: **`AlyrionCore: Mars & Planetary Geology`** (`itemGroup.alyrioncore.mars`).

The tab icon features the **Martian Rock Sample** (`martian_rock_sample`) and organizes all planetary materials into logical groupings:
1. **Scientific Samples & Minerals**: Rock Sample, Hematite Nodule, Raw Meteoric Iron, Meteoric Ingot, Raw Copper, Sulfur Dust, Olivine Gem, Dry Ice Shard.
2. **Soils & Regolith**: Martian Sand, Regolith, Coarse Regolith, Frost-Dusted Regolith, Permafrost.
3. **Stones & Architectural Blocks**: Basalt, Polished Basalt, Basalt Bricks, Basalt Tiles, Stratified Stone, Scoria, Impact Breccia.
4. **Planetary Ores**: Hematite Ore, Meteoric Iron Ore, Copper Ore, Sulfur Ore, Olivine Ore.
5. **Polar Volatiles & Ices**: Glacial Ice, Dry Ice Block.

---

## ⌨️ Client & Quality of Life Systems

### Universal Rebindable Escape / Close Screen Action

In vanilla Minecraft, the `Escape` key is hardcoded into various GUI screens and cannot normally be reassigned to other keys or mouse buttons (such as side mouse buttons `Mouse 4` / `Mouse 5`). 

AlyrionCore implements a clean, universal keybinding handler:

- **Key Mapping Name**: `key.alyrioncore.escape` ("Escape (Pause / Close Screen)")
- **Category**: `key.categories.alyrioncore` ("AlyrionCore")
- **Default Key**: `GLFW_KEY_ESCAPE` (Universal conflict context)

#### How It Works (`ClientGameEvents.java`)
- **In-Game World**: Pressing your bound key/mouse button consumes the click and brings up the Game Pause menu.
- **Inside GUI Screens (Inventories, Containers, Chat, Mod Menus)**:
  - Listens on `ScreenEvent.KeyPressed.Pre` and `ScreenEvent.MouseButtonPressed.Pre`.
  - When the bound key or mouse button is pressed, the event sends a simulated Escape action to the active screen (`screen.keyPressed(GLFW_KEY_ESCAPE, ...)`) or cleanly invokes `screen.onClose()`, safely closing any open inventory or menu.
  - Prevents unintended mouse-click conflicts by consuming and canceling the raw event.

---

## ⛏️ Tool Requirements & Mining Tags

All blocks in AlyrionCore strictly follow standard Minecraft NeoForge data conventions:

### Shovel Mineable (`#minecraft:mineable/shovel`)
- `martian_sand`
- `martian_regolith`
- `coarse_martian_regolith`
- `frost_dusted_regolith`

### Pickaxe Mineable (`#minecraft:mineable/pickaxe`)
- `martian_basalt`, `polished_martian_basalt`, `martian_basalt_bricks`, `martian_basalt_tiles`
- `stratified_martian_stone`, `martian_volcanic_scoria`, `martian_impact_breccia`
- `hematite_ore`, `meteoric_iron_ore`, `martian_copper_ore`, `martian_sulfur_ore`, `martian_olivine_ore`
- `martian_ice`, `dry_ice_block`, `martian_permafrost`

### Mining Tiers
- **Stone Tool or Better (`#minecraft:needs_stone_tool`)**:
  - `hematite_ore`, `martian_copper_ore`, `martian_sulfur_ore`
  - `martian_basalt`, `polished_martian_basalt`, `martian_basalt_bricks`, `martian_basalt_tiles`
  - `stratified_martian_stone`, `martian_volcanic_scoria`
- **Iron Tool or Better (`#minecraft:needs_iron_tool`)**:
  - `meteoric_iron_ore`
  - `martian_olivine_ore`
  - `martian_impact_breccia`

---

## 🛠️ Development, Setup & Building

### Prerequisites
- **JDK 21** (Eclipse Temurin or OpenJDK recommended)
- **Gradle** (or use the included `./gradlew` wrapper)

### Common Gradle Commands

```bash
# Build the mod JAR
./gradlew build

# Launch the Minecraft Client in development environment
./gradlew runClient

# Launch the dedicated server environment
./gradlew runServer

# Run Data Generation (recipes, tags, models)
./gradlew runData
```

### Mod Metadata & Architecture
- **Mod ID**: `alyrioncore`
- **Mod Name**: AlyrionCore
- **Mod Version**: `1.0.0`
- **Group ID**: `xyz.alyrion.alyrioncore`
- **Supported NeoForge Version**: `21.1.186+`
- **Target Minecraft Version**: `1.21.1`

---

## 📄 License & Credits

- **Developer**: Alyrion Team
- **Mod ID**: `alyrioncore`
- **License**: All Rights Reserved

*Created with precision for the Alyrion SMP.*
