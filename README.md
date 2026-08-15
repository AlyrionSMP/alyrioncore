# 🪐 AlyrionCore

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg?logo=minecraft)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1.186-orange.svg)](https://neoforged.net/)
[![Java](https://img.shields.io/badge/Java-21-blue.svg?logo=openjdk)](https://adoptium.net/)
[![License](https://img.shields.io/badge/License-All_Rights_Reserved-lightgrey.svg)]()

**AlyrionCore** is the flagship core mod for the **Alyrion SMP**, engineered to deliver an authentic, scientifically grounded Mars planetary dimension alongside a server-wide cosmetic reward economy, custom cape rendering engine, space mod interoperability, and extraterrestrial physics simulation.

From realistic 0.38g surface gravity and true-to-life forward Mie scattering blue sunsets to geological stratigraphy, volcanic fumaroles, solid $CO_2$ dry ice sublimation, and milestone-driven custom cosmetic capes, AlyrionCore transforms extraterrestrial exploration in Minecraft into a cohesive, high-polish experience.

---

## 📑 Table of Contents

- [Overview & Philosophy](#-overview--philosophy)
- [Key Features at a Glance](#-key-features-at-a-glance)
- [Cosmetic Store & Reward Progression System](#-cosmetic-store--reward-progression-system)
  - [In-Game Store GUI & Economy](#in-game-store-gui--economy)
  - [Custom Cape Collection](#custom-cape-collection)
  - [Progression Tasks & Milestones](#progression-tasks--milestones)
  - [3D Cape Rendering Engine & Motion Physics](#3d-cape-rendering-engine--motion-physics)
  - [Real-Time Multiplayer Synchronization](#real-time-multiplayer-synchronization)
  - [Developer & Testing Controls](#developer--testing-controls)
- [The Mars Dimension (`alyrioncore:mars`)](#-the-mars-dimension-alyrioncoremars)
  - [Planetary & Dimension Properties](#planetary--dimension-properties)
  - [Atmospheric Rendering & Celestial Optics](#atmospheric-rendering--celestial-optics)
  - [Space Mod & Celestial Orbit Interoperability](#space-mod--celestial-orbit-interoperability)
- [Planetary Physics: Authentic Gravity Simulation](#-planetary-physics-authentic-gravity-simulation)
- [Martian Biomes & Regional Geography](#-martian-biomes--regional-geography)
  - [Biome Catalog (6 Distinct Regions)](#biome-catalog-6-distinct-regions)
- [World Generation & Terrain Features](#-world-generation--terrain-features)
- [Geology, Blocks & Materials](#-geology-blocks--materials)
  - [Martian Regolith & Surface Soils](#martian-regolith--surface-soils)
  - [Stones, Volcanics & Decorative Masonry](#stones-volcanics--decorative-masonry)
  - [Extraterrestrial Ores & Minerals](#extraterrestrial-ores--minerals)
  - [Polar & Volatile Ices](#polar--volatile-ices)
- [Items & Resource Economy](#-items--resource-economy)
- [Creative Mode Integration](#-creative-mode-integration)
- [Client & Quality of Life Systems](#-client--quality-of-life-systems)
  - [Universal Rebindable Escape / Close Screen Action](#universal-rebindable-escape--close-screen-action)
  - [Cosmetic Store Hotkey & Commands](#cosmetic-store-hotkey--commands)
- [Tool Requirements & Mining Tags](#-tool-requirements--mining-tags)
- [Development, Setup & Building](#-development-setup--building)
- [License & Credits](#-license--credits)

---

## 🔬 Overview & Philosophy

AlyrionCore was designed with a dual directive: **bridge the gap between authentic planetary science and engaging sandbox gameplay**, while providing **integrated community rewards and progression systems for the Alyrion SMP**.

Rather than relying on generic fantasy tropes, the mod models real-world planetary phenomena discovered by NASA, ESA, and robotic exploration rovers (*Curiosity*, *Perseverance*, *Opportunity*):
- **Atmospheric Optics**: Suspended ferric oxide dust creates butterscotch daytime fog, while forward Mie scattering produces vivid blue sunrises and sunsets.
- **Physical Atmospheric Behavior**: Solid Carbon Dioxide ($CO_2$) sublimes directly into gas with animated vapor and snowflake particle emissions.
- **Accurate Surface Gravity**: Dynamic entity attribute simulation of Mars's surface acceleration ($3.72\ \text{m/s}^2 \approx 0.38g$).
- **Authentic Stratigraphy**: Hematite "blueberries", meteoric nickel-iron alloys, olivine crystal intrusions, volcanic basalt columns, fumarole patches, impact breccia, and stratified canyon stone.
- **Server Progression & Wardrobe**: Reward active survival playtime and milestone achievements (Spaceflight, Lunar Landing, Mars Touchdown, Dragon Egg) with custom wearable cosmetic capes rendered in 3D with synchronized multiplayer networking.

---

## ⚡ Key Features at a Glance

| Feature | Description | Implementation Details |
|---|---|---|
| **Cosmetic Store & Wardrobe** | Built-in GUI for browsing, purchasing, and equipping custom 3D capes. | Accessible via `/store`, `/cosmetics`, or `K` keybind |
| **Survival Playtime Economy** | Earn 1 Coin for every 1 hour (3600s) spent in survival/adventure mode. | Persistent client-side data tracker with live progress bar |
| **5 Hand-Crafted Capes** | Custom 64x32 cape textures celebrating server milestones, spaceflight, and Mars. | Dedicated `AlyrionCapeLayer` with real-time motion physics |
| **Milestone Tasks** | Unlock coins and exclusive capes by reaching Space, the Moon, Mars, or claiming the Dragon Egg. | Real-time event & player state evaluation |
| **Multiplayer Cape Sync** | Network synchronization packets broadcast equipped capes to all nearby players. | Custom C2S / S2C payload network pipeline |
| **Mars Dimension** | Full extraterrestrial dimension with 384-block world height ($Y = -64$ to $Y = 320$). | `alyrioncore:mars`, multi-noise terrain generator |
| **0.38g Planetary Gravity** | Living entities experience 38% of Earth gravity while on Mars. | NeoForge Attribute Modifier (`Attributes.GRAVITY`, $-62\%$) |
| **Authentic Blue Sunsets** | Forward Mie scattering calculations simulate true blue Martian twilights. | Custom `DimensionSpecialEffects` pipeline |
| **Butterscotch Day Fog** | Suspended ferric dust creates an amber-ochre haze under the Martian sun. | RGB-weighted fog color transformation |
| **6 Realistic Biomes** | *Vastitas Borealis*, *Valles Marineris*, *Tharsis*, *Planum Boreum*, *Noachis Terra*, and *Olympus Mons*. | Multi-noise spline generation & biome-specific features |
| **Volcanic & Crater Formations** | Basalt columns, volcanic fumaroles, scoria boulder clusters, impact craters, and canyon rock scatters. | Placed and configured feature worldgen library |
| **CO₂ Dry Ice Sublimation** | Dry ice blocks dynamically release visible sublimation vapor and frost particles. | Custom `DryIceBlock` with animated particle emission |
| **Rich Geological Catalog** | 16 unique blocks including volcanics, bricks, ores, breccia, and soils with custom pixel art. | Hand-crafted 16x16 textures, pickaxe/shovel tool tags |
| **Universal Escape Keybind** | Rebind the standard Escape key action (Pause/Close GUI) to mouse buttons or other keys. | Custom `ModKeyMappings` with screen event routing |
| **Rocketnautics Interop** | Celestial definitions, orbital parameters, and atmospheric drag integration. | `data/alyrioncore` and `data/rocketnautics` datapacks |

---

## 🎨 Cosmetic Store & Reward Progression System

AlyrionCore features an integrated cosmetic wardrobe and progression reward economy built directly into the client and server.

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                     ✦ ALYRION COSMETIC STORE & REWARDS ✦            Coins: ⛃ 15 │
├─────────────────────────────────────────────────────────────────────────────────┤
│ [ Store & Wardrobe ]       [ Tasks & Playtime ]       [ Dev Controls ]          │
├────────────────────────────────────────┬────────────────────────────────────────┤
│ ┌────────────────────────────────────┐ │ ┌────────────────────────────────────┐ │
│ │ 2 Year Celebration Cape   [★ FREE] │ │ │      The Martian Cape              │ │
│ ├────────────────────────────────────┤ │ │  ┌──────┐                           │ │
│ │ Season 8 Cape             [★ FREE] │ │ │  │ 2D   │ Martian rust dunes and   │ │
│ ├────────────────────────────────────┤ │ │  │ Cape │ Olympus Mons with a      │ │
│ │ Stars Cape             [✔ UNLOCKED]│ │ │  │ View │ green Martian.           │ │
│ ├────────────────────────────────────┤ │ │  └──────┘                           │ │
│ │ Moon Cape              [5 Coins]   │ │ │          Status: Unlocked          │ │
│ ├────────────────────────────────────┤ │ │         [  Equip Cape  ]           │ │
│ │ The Martian Cape       [5 Coins]   │ │ └────────────────────────────────────┘ │
│ └────────────────────────────────────┘                                          │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### In-Game Store GUI & Economy
- **Access**: Open via chat commands (`/store` or `/cosmetics`) or the dedicated hotkey (default: **`K`**).
- **Playtime Currency**: Players earn **1 Coin** for every **1 hour (3600 seconds)** spent actively in Survival or Adventure mode (creative and spectator modes do not accumulate playtime).
- **Progress Tracking**: The "Tasks & Playtime" tab displays total survival playtime down to the second, alongside a live progress bar tracking time toward the next coin reward.
- **Persistent Storage**: Progression and unlocked cosmetics are saved locally in `config/alyrion_cosmetics.json` and synchronized with active server sessions.

---

### Custom Cape Collection

All capes are authored in 64x32 Minecraft cape format with bespoke pixel art:

| Cape | Texture Identifier | Price | Unlock Condition | Description |
|---|---|---|---|---|
| **2 Year Celebration Cape** | `2_year_celebration.png` | **FREE** | Unlocked by default | Commemorates 2 years of the Alyrion SMP with a festive tiered birthday cake and candles. |
| **Season 8 Cape** | `season_8.png` | **FREE** | Unlocked by default | Royal crimson and gold themed cape featuring diamond-inlaid crest and golden laurels for Season 8. |
| **Stars Cape** | `stars.png` | **5 Coins** | Buy or complete *"Going to Space"* | Deep space starfield studded with pure-white stars and an orbiting high-tech research satellite. |
| **Moon Cape** | `moon.png` | **5 Coins** | Buy or complete *"Going to the Moon"* | Detailed lunar cratered surface overlooking the blue marble of planet Earth in deep space. |
| **The Martian Cape** | `marsian.png` | **5 Coins** | Buy or complete *"Going to Mars"* | Rust-ochre Martian dunes beneath Olympus Mons featuring a friendly green Martian explorer. |

---

### Progression Tasks & Milestones

Players can earn bonus coins and immediately unlock premium capes by accomplishing key survival exploration milestones:

```
                                  🏆 SURVIVAL MILESTONES
                                            │
        ┌───────────────────┬───────────────┴───────────────┬───────────────────┐
        │                   │                               │                   │
  🚀 Going to Space   🌕 Going to the Moon            🪐 Going to Mars    🐉 Dragon Egg
  Launch to Orbit     Touch down on Moon              Reach Martian soil  Hold Dragon Egg
   +5 Coins &          +5 Coins &                      +5 Coins &          +10 Coins
   Stars Cape          Moon Cape                       Martian Cape
```

1. **Going to Space** (`task_space`):
   - **Trigger**: Launch a rocket and enter space, orbit, or asteroid dimensions.
   - **Reward**: **+5 Coins** + immediate unlock of the **Stars Cape**.
2. **Going to the Moon** (`task_moon`):
   - **Trigger**: Touch down on the lunar surface.
   - **Reward**: **+5 Coins** + immediate unlock of the **Moon Cape**.
3. **Going to Mars** (`task_mars`):
   - **Trigger**: Touch down on the surface of Mars (`alyrioncore:mars` or Martian biomes).
   - **Reward**: **+5 Coins** + immediate unlock of **The Martian Cape**.
4. **Obtaining the Dragon Egg** (`task_dragon_egg`):
   - **Trigger**: Slay the Ender Dragon and hold the Dragon Egg in your main inventory or offhand.
   - **Reward**: **+10 Coins**.

---

### 3D Cape Rendering Engine & Motion Physics

AlyrionCore implements `AlyrionCapeLayer.java`, a custom player render layer attached to all player skins (both `default` / wide and `slim` / Alex models):

- **Authentic Cloak Physics**: Implements real-time interpolation of player velocity, body rotation, walking bobbing, and vertical descent momentum to calculate natural cape sway and trailing angles.
- **Crouch Adjustments**: Automatically offsets and angles the cape when the player sneaks.
- **Smart Elytra Handling**: Seamlessly hides the cosmetic cape when an Elytra is equipped in the chest slot, preventing visual clipping and model artifacts.

---

### Real-Time Multiplayer Synchronization

Capes are fully synchronized across multiplayer servers using NeoForge custom payload networking (`CosmeticNetworking.java`):

- **Client-to-Server (`c2s_equip_cape`)**: When a player equips or unequips a cape, the client notifies the server.
- **Server-to-Client (`s2c_sync_cape`)**: The server broadcasts the equipped cape ID to all clients tracking that entity.
- **Graceful Fallback**: Functions offline and in singleplayer environments without requiring a dedicated server daemon.

---

### Developer & Testing Controls

When `CosmeticConfig.DEV_MODE = true`, an additional **Dev Controls** tab is rendered in the Cosmetic Store screen:
- **Instant Task Triggers**: One-click completion buttons for Space, Moon, Mars, and Dragon Egg tasks.
- **Economy Acceleration**: Instant `+1 Hour Playtime (+1 Coin)` and `+10 Coins` testing buttons.
- **State Reset Tools**: `Reset All Tasks Progress` and `Reset All Cosmetic Unlocks`.

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
  - `bed_works: false` – Beds explode in the thin atmosphere / vacuum conditions.
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

### Space Mod & Celestial Orbit Interoperability

AlyrionCore includes pre-configured planetary definitions and physical parameters compatible with modern NeoForge space exploration mods (such as *Cosmonautics* / *Rocketnautics*):

- **Celestial Orbit**: Sol-centered circular orbit with period $823,200\text{ s}$ and rotation period $1,230\text{ s}$.
- **Atmospheric Composition**: Configured with `low_density` and `drowning` atmospheric hazards above $Y = 5,000$, requiring pressurized life support equipment.
- **Planetary Surface Rendering**: Built-in 3D celestial sphere texture sampler with accurate RGB palette mapping across all 6 Martian biomes.
- **Atmospheric Drag**: Multi-tier altitude drag curves ($0.2\times$ drag below $4,000\text{ m}$ tapering to $0.0$ at orbital insertion).

---

## 🪂 Planetary Physics: Authentic Gravity Simulation

Mars possesses a surface gravity of approximately **$3.72\ \text{m/s}^2$**, which corresponds to **~38% of Earth's standard gravity ($0.38g$)**.

### How It Works (`MarsPhysicsHandler.java`)

1. **Attribute Integration**: Utilizes NeoForge's entity attribute system (`Attributes.GRAVITY`).
2. **Mathematical Modifier**:
   - An `AttributeModifier` with value `-0.62` (`ADD_MULTIPLIED_BASE`) is applied to every `LivingEntity` within `alyrioncore:mars`.
   - $\text{Effective Gravity} = \text{Base Gravity} \times (1.0 - 0.62) = 0.38 \times \text{Base Gravity}$.
3. **Dynamic Application & Safety**:
   - Evaluated dynamically on `EntityTickEvent.Post`.
   - Automatically attaches transient modifiers when an entity arrives on Mars.
   - Automatically and cleanly strips the modifier when the entity teleports back to the Overworld or another dimension, preventing state pollution.

### Gameplay Impact
- **Higher, Looping Jumps**: Players and mobs jump significantly higher.
- **Extended Leap Distances**: Horizontal momentum carries players over wide canyons and crater rims.
- **Reduced Fall Velocity**: Slower terminal velocity and floatier descent mechanics.

---

## 🗺️ Martian Biomes & Regional Geography

AlyrionCore features **6 scientifically distinct Martian biomes**, each representing actual physiographic provinces and surface regions on Mars:

```
                   ┌────────────────────────────────────────────────────────┐
                   │             PLANUM BOREUM (Polar Cap)                  │
                   │      Solid CO₂ Dry Ice, Glacial Ice, Permafrost        │
                   └───────────────────────────┬────────────────────────────┘
                                               │
        ┌──────────────────────────────────────┼──────────────────────────────────────┐
        │                                      │                                      │
 ┌──────┴──────────────────────┐  ┌────────────┴────────────┐  ┌──────────────────────┴──────┐
 │     VASTITAS BOREALIS       │  │    VALLES MARINERIS     │  │  THARSIS VOLCANIC PLATEAU   │
 │ Smooth Lowland Plains       │  │ Deep Rift Grand Canyon  │  │ Basalt Fields & Volcanics   │
 │ Martian Sand & Regolith     │  │ Stratified Terraces     │  │ Scoria Deposits & Sulfur    │
 └─────────────────────────────┘  └─────────────────────────┘  └─────────────────────────────┘
        │                                      │                                      │
        └──────────────────────────────────────┼──────────────────────────────────────┘
                                               │
                   ┌───────────────────────────┴────────────────────────────┐
                   │               OLYMPUS MONS (Shield Volcano)            │
                   │    Stratospheric Peak, Basalt Columns, Fumaroles, Ores │
                   └───────────────────────────┬────────────────────────────┘
                                               │
                   ┌───────────────────────────┴────────────────────────────┐
                   │               NOACHIS TERRA (Highlands)                │
                   │     Impact Breccia, Heavy Craters, Meteoric Iron       │
                   └────────────────────────────────────────────────────────┘
```

### Biome Catalog (6 Distinct Regions)

#### 1. Vastitas Borealis (`alyrioncore:vastitas_borealis`)
- **Scientific Context**: The vast, smooth northern circumpolar lowlands created by ancient sediment deposits and sweeping sand drifts.
- **Terrain**: Expansive, gentle low-elevation plains.
- **Dominant Materials**: Fine Martian Sand, Martian Regolith, coarse dust drifts.
- **Climate Parameters**: `temperature: 0.0`, `humidity: -0.5`, `continentalness: -0.4`, `erosion: 0.4`.

#### 2. Valles Marineris (`alyrioncore:valles_marineris`)
- **Scientific Context**: The colossal grand canyon of Mars, stretching over 4,000 km long and up to 7 km deep, formed by ancient tectonic rifting.
- **Terrain**: Deep chasms, stepped canyon walls, sheer cliffs, and exposed sedimentary layers.
- **Dominant Materials**: Stratified Martian Stone, exposed mineral veins, deep basalt outcroppings, canyon rock scatters.
- **Climate Parameters**: `temperature: 0.2`, `humidity: -0.2`, `continentalness: -0.1`, `erosion: -0.7`, `weirdness: 0.7`.

#### 3. Tharsis Volcanic Plateau (`alyrioncore:tharsis_volcanic_plateau`)
- **Scientific Context**: The colossal volcanic plateau home to the Tharsis Montes shield volcanoes and vast basalt floodplains.
- **Terrain**: Elevated volcanic plateaus, basalt sheets, lava channels, and scoria deposits.
- **Dominant Materials**: Martian Basalt, Martian Volcanic Scoria, Martian Sulfur Ore.
- **Climate Parameters**: `temperature: 0.5`, `humidity: -0.6`, `continentalness: 0.4`, `erosion: 0.2`, `weirdness: -0.3`.

#### 4. Planum Boreum (`alyrioncore:planum_boreum`)
- **Scientific Context**: The permanent northern polar ice cap of Mars, consisting of stratified water ice and seasonal solid $CO_2$ dry ice frost.
- **Terrain**: Glacial ice sheets, frost-dusted ridges, and polar permafrost beds.
- **Dominant Materials**: Martian Glacial Ice, Martian Dry Ice (Solid $CO_2$), Martian Permafrost, Frost-Dusted Regolith.
- **Climate Parameters**: `temperature: -0.85`, `humidity: 0.4`, `continentalness: -0.5`, `erosion: 0.0`.

#### 5. Noachis Terra (`alyrioncore:noachis_terra`)
- **Scientific Context**: One of the oldest known landforms on Mars, heavily cratered from the Late Heavy Bombardment.
- **Terrain**: Rugged crater rims, ejecta blankets, boulder fields, and highland plateaus.
- **Dominant Materials**: Martian Impact Breccia, Coarse Martian Regolith, Hematite Ore, Meteoric Nickel-Iron Ore.
- **Climate Parameters**: `temperature: -0.2`, `humidity: -0.5`, `continentalness: 0.2`, `erosion: 0.6`, `weirdness: -0.6`.

#### 6. Olympus Mons (`alyrioncore:olympus_mons`)
- **Scientific Context**: The tallest volcano and highest mountain in the entire Solar System, standing nearly three times the height of Mount Everest.
- **Terrain**: Towering stratospheric peaks, steep stratified cliffs, high-altitude frost caps, and volcanic fumaroles.
- **Dominant Materials**: Stratified Stone cliffs, Volcanic Scoria, Basalt Columns, Frost-Dusted Regolith, rich Meteoric Iron and Copper deposits.
- **Climate Parameters**: `temperature: -0.5`, `humidity: -0.7`, `continentalness: 0.8`, `erosion: -0.6`, `weirdness: 0.5`.

---

## 🌋 World Generation & Terrain Features

AlyrionCore features an extensive library of configured and placed features generating custom extraterrestrial surface structures:

```
                                   Martian Worldgen Features
                                              │
    ┌────────────────────┬────────────────────┼────────────────────┬────────────────────┐
    │                    │                    │                    │                    │
Volcanic Basalt      Volcanic Scoria      Fumarole Gas         Impact Craters       Polar Dry Ice
Columns              Boulder Clusters     Geothermal Vents     & Boulders           Glacial Sheets
(Hexagonal Pillars)  (Porous Deposits)    (Sulfur Emitting)    (Breccia Scatters)   (Solid CO₂ Beds)
```

1. **Volcanic Basalt Columns** (`volcanic_basalt_columns`): Geometric pillars of dense basalt emerging on volcanic slopes and Olympus Mons.
2. **Volcanic Fumarole Patches** (`volcanic_fumarole_patch`): Geothermal vent patches with sulfur ore and scoria outcroppings.
3. **Scoria Boulder Clusters & Patches** (`scoria_boulder_cluster`, `volcanic_scoria_patch`): Vesicular volcanic rock fields around shield volcanoes.
4. **Impact Craters & Boulder Scatters** (`large_crater_scatter`, `small_crater_scatter`, `boulder_scatter`): Heavy meteorite impact scars strewn across Noachis Terra.
5. **Canyon Rock Scatters** (`canyon_rock_scatter`): Fallen rock piles and stratified debris decorating canyon floors in Valles Marineris.
6. **Polar Ice & Dry Ice Sheets** (`dry_ice_sheet_patch`, `water_ice_patch`, `ore_permafrost`): Vast sheets of solid $CO_2$ and ancient subterranean water ice at the poles.
7. **Ore Deposits**: Stratified vertical distributions of Hematite (upper and lower), Meteoric Iron (deep veins and rich high-altitude outcroppings), Native Copper, Volcanic Sulfur, and Olivine crystals.

---

## 🧱 Geology, Blocks & Materials

All blocks feature custom 16x16 pixel art textures authored specifically for AlyrionCore:

### Martian Regolith & Surface Soils

| Block | Identifier | Map Color | Hardness / Resistance | Sound Type | Primary Tool | Description |
|---|---|---|---|---|---|---|
| **Martian Sand** | `martian_sand` | Red (`#C05832`) | `0.5` / `0.5` | `SAND` | Shovel | Fine iron-oxidized falling sand (`FallingBlock`) with custom red-orange dust particles. |
| **Martian Regolith** | `martian_regolith` | Terracotta Orange | `0.8` / `0.8` | `GRAVEL` | Shovel | Fine-grained planetary soil covering the lowland plains of Mars. |
| **Coarse Martian Regolith** | `coarse_martian_regolith` | Terracotta Red | `0.9` / `0.9` | `GRAVEL` | Shovel | Denser, pebble-strewn regolith found in ancient cratered highlands. |
| **Frost-Dusted Regolith** | `frost_dusted_regolith` | Light Gray | `0.8` / `0.8` | `SNOW` | Shovel | Regolith blanketed with micro-crystals of water and $CO_2$ frost. |
| **Martian Permafrost** | `martian_permafrost` | Ice | `1.8` / `3.0` | `GLASS` | Pickaxe | Deep frozen soil cemented by subterranean water ice. |

---

### Stones, Volcanics & Decorative Masonry

| Block | Identifier | Map Color | Hardness / Resistance | Sound Type | Tool Required | Description |
|---|---|---|---|---|---|---|
| **Martian Basalt** | `martian_basalt` | Gray | `1.8` / `6.0` | `DEEPSLATE` | Pickaxe (Stone+) | Dense volcanic igneous rock forming the bulk of the Martian crust. |
| **Polished Martian Basalt** | `polished_martian_basalt` | Gray | `2.0` / `6.0` | `STONE` | Pickaxe (Stone+) | Smooth-cut basalt ideal for modern extraterrestrial habitat construction. |
| **Martian Basalt Bricks** | `martian_basalt_bricks` | Gray | `2.0` / `6.0` | `STONE` | Pickaxe (Stone+) | Refined basalt masonry bricks engineered for structural resilience. |
| **Martian Basalt Tiles** | `martian_basalt_tiles` | Gray | `2.0` / `6.0` | `STONE` | Pickaxe (Stone+) | Finished basalt tiling for habitat floors and high-tech interior architecture. |
| **Stratified Martian Stone** | `stratified_martian_stone` | Terracotta Brown | `1.5` / `6.0` | `STONE` | Pickaxe (Stone+) | Sedimentary canyon stone exhibiting distinct geological banding. |
| **Martian Volcanic Scoria** | `martian_volcanic_scoria` | Black | `1.4` / `4.0` | `BASALT` | Pickaxe (Stone+) | Highly vesicular, porous volcanic rock ejected by Martian shield volcanoes. |
| **Martian Impact Breccia** | `martian_impact_breccia` | Terracotta Gray | `2.2` / `8.0` | `TUFF` | Pickaxe (Iron+) | High-density rock composed of shattered mineral fragments fused by meteorite impacts. |

---

### Extraterrestrial Ores & Minerals

| Ore Block | Identifier | Hardness / Resistance | Tool Required | Primary Mineral / Item |
|---|---|---|---|---|
| **Martian Hematite Ore** | `hematite_ore` | `3.0` / `3.0` | Pickaxe (Stone+) | Yields **Hematite Nodules** ($Fe_2O_3$ concretions / "Martian Blueberries"). |
| **Meteoric Nickel-Iron Ore** | `meteoric_iron_ore` | `4.5` / `5.0` | Pickaxe (Iron+) | High-grade meteoric deposits; yields **Raw Meteoric Iron**. |
| **Martian Copper Ore** | `martian_copper_ore` | `3.0` / `3.0` | Pickaxe (Stone+) | Hydrothermal planetary copper veins; yields **Raw Martian Copper**. |
| **Martian Sulfur Ore** | `martian_sulfur_ore` | `2.5` / `3.0` | Pickaxe (Stone+) | Volcanic geothermal deposits; yields **Martian Sulfur Dust**. |
| **Martian Olivine Ore** | `martian_olivine_ore` | `3.5` / `4.0` | Pickaxe (Iron+) | Ultramafic mantle silicate crystals; yields **Olivine Crystals** (Peridot). |

---

### Polar & Volatile Ices

#### 1. Martian Glacial Ice (`martian_ice`)
- **Composition**: Dense, ancient water ice ($H_2O$) preserved in polar ice sheets and subterranean permafrost.
- **Properties**: Hardness `1.2`, Friction `0.98` (high slipperiness), Glass sound. Requires a pickaxe.

#### 2. Martian Dry Ice / Solid $CO_2$ (`dry_ice_block`)
- **Composition**: Compressed solid carbon dioxide ($CO_2$).
- **Friction**: `0.985` (Ultra-low surface friction).
- **Sublimation Particle Mechanics (`DryIceBlock.java`)**:
  - In Martian conditions, solid dry ice transitions directly from a solid into a gas without melting into liquid.
  - During block ticks (`animateTick`), the block emits:
    - `ParticleTypes.CLOUD` vapor particles rising directly off the upper surface.
    - `ParticleTypes.SNOWFLAKE` sub-zero micro-frost particles drifting around the block.

---

## 💎 Items & Resource Economy

| Item | Identifier | Category | Lore & Scientific Description |
|---|---|---|---|
| **Hematite Nodule** | `hematite_nodule` | Mineral | Known informally as **"Martian Blueberries"**—spherical iron oxide concretions formed in the presence of ancient groundwater. |
| **Raw Meteoric Iron** | `raw_meteoric_iron` | Metal | Unrefined fragments of extraterrestrial nickel-iron meteorites harvested from impact craters. |
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

AlyrionCore implements a universal keybinding handler:

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

### Cosmetic Store Hotkey & Commands

- **Key Mapping Name**: `key.alyrioncore.open_store` ("Open Cosmetic Store")
- **Default Key**: **`K`** (In-game context)
- **Chat Commands**: `/store` or `/cosmetics` opens the store and rewards screen directly from the chat prompt.

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

### Asset Generation Scripts
- `generate_enhanced_textures.py`: Regenerates pixel-art textures for all 19 blocks and 8 items with 3-pass shading and specular highlights.
- `generate_capes.py`: Regenerates all 64x32 custom capes with anti-aliased pixel art.

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
*This mod is made using AI. If you are not okay with that, feel free to apply as our unpaid full-time developer or artist.*
