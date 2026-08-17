# 🪐 AlyrionCore

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg?logo=minecraft)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1.186-orange.svg)](https://neoforged.net/)
[![Java](https://img.shields.io/badge/Java-21-blue.svg?logo=openjdk)](https://adoptium.net/)
[![License](https://img.shields.io/badge/License-All_Rights_Reserved-lightgrey.svg)]()

**AlyrionCore** is the flagship core mod for the **Alyrion SMP**, engineered to deliver an authentic, scientifically grounded Mars planetary dimension alongside a server-wide cosmetic reward economy, custom cape rendering engine, space mod interoperability, and extraterrestrial physics simulation.

From realistic 0.38g surface gravity and true-to-life forward Mie scattering blue sunsets to geological stratigraphy, volcanic fumaroles, solid $CO_2$ dry ice sublimation, and milestone-driven custom cosmetic capes, AlyrionCore transforms extraterrestrial exploration in Minecraft into a cohesive, high-polish experience. Beyond the raw planetary surface, the mod adds a **dynamic seasonal dust-weather system** (from midday dust devils to planet-encircling storms), **pressurized habitat construction and life support**, a **two-block Mars Sleeping Pod**, **greenhouse farming of Martian Potatoes**, a **full Meteoric Iron equipment tier**, the moons **Phobos & Deimos**, and **crashed probe wreck structures** with salvageable loot.

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
  - [Dynamic Mars Weather & Dust Storm System](#-dynamic-mars-weather--dust-storm-system)
  - [Habitat Construction & Life Support](#-habitat-construction--life-support)
  - [Sleeping Pods & Rest on Mars](#-sleeping-pods--rest-on-mars)
  - [Space Mod & Celestial Orbit Interoperability](#space-mod--celestial-orbit-interoperability)
  - [Martian Moons: Phobos & Deimos](#-martian-moons-phobos--deimos)
- [Planetary Physics: Authentic Gravity Simulation](#-planetary-physics-authentic-gravity-simulation)
- [Martian Biomes & Regional Geography](#-martian-biomes--regional-geography)
  - [Biome Catalog (6 Distinct Regions)](#biome-catalog-6-distinct-regions)
- [World Generation & Terrain Features](#-world-generation--terrain-features)
  - [Crashed Space Probe Structures](#-crashed-space-probe-structures)
- [Geology, Blocks & Materials](#-geology-blocks--materials)
  - [Martian Regolith & Surface Soils](#martian-regolith--surface-soils)
  - [Stones, Volcanics & Decorative Masonry](#stones-volcanics--decorative-masonry)
  - [Extraterrestrial Ores & Minerals](#extraterrestrial-ores--minerals)
  - [Resource & Storage Blocks](#resource--storage-blocks)
  - [Polar & Volatile Ices](#polar--volatile-ices)
- [Greenhouse Farming & Martian Agriculture](#-greenhouse-farming--martian-agriculture)
- [Items & Resource Economy](#-items--resource-economy)
  - [Meteoric Iron Equipment Tier](#meteoric-iron-equipment-tier)
  - [Interactive Items: Dry Ice Shards & Rock Samples](#interactive-items-dry-ice-shards--rock-samples)
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
- **Vanilla-Style Texture Redesign**: The entire block & item catalog (meteoric iron tier, martian farm, basalt/regolith/ice/ore families, mineral blocks, tools and materials) re-authored in JAPPA-style 16x16 pixel art with top-left lighting, tight hue-shifted palettes and seamless tiling — generated by pure-Python, seedable scripts.
- **Server Progression & Wardrobe**: Reward active survival playtime and milestone achievements (Spaceflight, Lunar Landing, Mars Touchdown, Dragon Egg) with custom wearable cosmetic capes rendered in 3D with synchronized multiplayer networking.

---

## ⚡ Key Features at a Glance

| Feature | Description | Implementation Details |
|---|---|---|
| **Cosmetic Store & Wardrobe** | Built-in GUI for browsing, purchasing, and equipping custom 3D capes. | Accessible via `/store`, `/cosmetics`, or `K` keybind |
| **Survival Playtime Economy** | Earn 1 Coin for every 1 hour (3600s) spent in survival/adventure mode. | Server-authoritative tracker persisted in the world save with live progress bar |
| **7 Hand-Crafted Capes** | Custom 64x32 cape textures celebrating server milestones, spaceflight, Mars, and player parties. | Dedicated `AlyrionCapeLayer` with real-time motion physics |
| **Milestone Tasks** | Unlock coins and exclusive capes by reaching Space, the Moon, Mars, claiming the Dragon Egg, slaying 10 players, or partying up with 4+ players. | Real-time event & player state evaluation |
| **Multiplayer Cape Sync** | Network synchronization packets broadcast equipped capes to all nearby players. | Custom C2S / S2C payload network pipeline |
| **Cosmetic Pets** | Buy and equip 3D pets that orbit your character — currently the **Satellite Pet**, a gold research satellite circling your head. | `SatellitePetModel` + `SatellitePetLayer` orbit renderer, dedicated **Pets** tab in the store with spinning 3D preview |
| **Mars Dimension** | Full extraterrestrial dimension with 384-block world height ($Y = -64$ to $Y = 320$). | `alyrioncore:mars`, multi-noise terrain generator |
| **0.38g Planetary Gravity** | Living entities experience 38% of Earth gravity while on Mars. | NeoForge Attribute Modifier (`Attributes.GRAVITY`, $-62\%$) |
| **Authentic Blue Sunsets** | Forward Mie scattering calculations simulate true blue Martian twilights. | Custom `DimensionSpecialEffects` pipeline |
| **Butterscotch Day Fog** | Suspended ferric dust creates an amber-ochre haze under the Martian sun. | RGB-weighted fog color transformation |
| **6 Realistic Biomes** | *Vastitas Borealis*, *Valles Marineris*, *Tharsis*, *Planum Boreum*, *Noachis Terra*, and *Olympus Mons*. | Multi-noise spline generation & biome-specific features |
| **Volcanic & Crater Formations** | Basalt columns, volcanic fumaroles, scoria boulder clusters, impact craters, and canyon rock scatters. | Placed and configured feature worldgen library |
| **Dynamic Mars Weather** | Seasonally driven state machine: Clear Skies → Dust Devils → Regional Storm → Global Planet-Encircling Dust Storm. | `MarsWeatherSavedData` + per-tick server simulation, synced via `s2c_mars_weather` payload |
| **Dust Devils** | Towering conical dust columns spawn near players during midday or storm activity. | Server-tracked `DustDevilInstance`s rendered with swirling particle vortices |
| **Storm-Aware Atmosphere** | Fog ramps into a dense ochre dust blackout and blue sunsets are suppressed during severe storms. | `MarsClientWeatherHandler` fog/color events + `MarsDimensionEffects` intensity blending |
| **Pressurized Habitats** | Build airtight habitats and greenhouses that provide breathable air on **any vacuum world** — Mars, the Moon, deep space. A sealed room stays breathable **only while a charged Oxygen Generator runs inside it**. | `HabitatSealManager` flood-fill seal detection + `VacuumAtmosphere` atmosphere-API compat, bulletproof per-tick air refill, FE-powered `OxygenGeneratorBlock` |
| **Animated Pressurized Airlock** | Two-block airtight door with a folding armored hatch, viewport window and status LED. | `AirlockBlockEntity` + `AirlockBlockEntityRenderer` with smoothstep pneumatic swing |
| **Mars Sleeping Pod** | Two-block tech bed that lets players sleep on Mars — even through raging dust storms. | Custom `SleepingPodBlock` with NeoForge bed hooks & dimension-aware sleep logic |
| **Greenhouse Farming** | Till Martian regolith into farmland and grow Martian Potatoes — but only inside sealed, lit greenhouses. | `RegolithFarmlandBlock`, `MartianPotatoCropBlock` + `HabitatSealManager` integration |
| **Meteoric Iron Tier** | Full tool & weapon set (sword, pickaxe, axe, shovel, hoe) forged from meteoric nickel-iron. | `ModToolTiers.METEORIC_IRON` — diamond harvest level, 650 durability |
| **Crashed Probe Structures** | Two jigsaw crash sites (Soviet & US probe) with salvageable scientific chest loot. | NBT structures + `crashed_probes` structure set |
| **Martian Moons** | Phobos & Deimos added as tidally locked moons of Mars with bespoke celestial textures. | `universe_planets/phobos.json` & `deimos.json` (Rocketnautics + AlyrionCore datapacks) |
| **CO₂ Dry Ice Sublimation** | Dry ice blocks dynamically release visible sublimation vapor and frost particles. | Custom `DryIceBlock` with animated particle emission |
| **Rich Geological Catalog** | 24 unique blocks including volcanics, bricks, ores, breccia, resource blocks, technology and soils with custom pixel art. | Hand-crafted 16x16 textures (full 2026 vanilla-style redesign), pickaxe/shovel tool tags |
| **Universal Escape Keybind** | Rebind the standard Escape key action (Pause/Close GUI) to mouse buttons or other keys. | Custom `ModKeyMappings` with screen event routing |
| **Rocketnautics Interop** | Celestial definitions, orbital parameters, and atmospheric drag integration. | `data/alyrioncore` and `data/rocketnautics` datapacks |

---

## 🎨 Cosmetic Store & Reward Progression System

AlyrionCore features an integrated cosmetic wardrobe and progression reward economy. The **server is fully authoritative**: coins, cape & pet unlocks, playtime and task progress are stored per-server inside the world save (keyed by player UUID) and only ever mutated by the server. The client renders a synchronized mirror of the state the server sends it.

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                     ✦ ALYRION COSMETIC STORE & REWARDS ✦            Coins: ⛃ 15 │
├─────────────────────────────────────────────────────────────────────────────────┤
│ [ Store & Wardrobe ]  [ Pets ]     [ Tasks & Playtime ]                        │
├────────────────────────────────────────┬────────────────────────────────────────┤
│ ┌────────────────────────────────────┐ │ ┌────────────────────────────────────┐ │
│ │ 2 Year Celebration Cape   [★ FREE] │ │ │      The Martian Cape              │ │
│ ├────────────────────────────────────┤ │ │  ┌──────┐                           │ │
│ │ Season 8 Cape             [★ FREE] │ │ │  │ 2D   │ Martian rust dunes and   │ │
│ ├────────────────────────────────────┤ │ │  │ Cape │ Olympus Mons with a      │ │
│ │ Stars Cape             [✔ UNLOCKED]│ │ │  │ View │ green Martian.           │ │
│ ├────────────────────────────────────┤ │ │  └──────┘                           │ │
│ │ Moon Cape              [5 Coins]   │ │ │          Status: Unlocked          │ │
│ ├────────────────────────────────────┤ │ │         [ Preview Only ]           │ │
│ │ The Martian Cape       [5 Coins]   │ │ └────────────────────────────────────┘ │
│ ├────────────────────────────────────┤                                          │
│ │ Grim Cape              [10 Coins]  │                                          │
│ ├────────────────────────────────────┤                                          │
│ │ Satellite Pet           [15 Coins] │                                          │
│ └────────────────────────────────────┘                                          │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### In-Game Store GUI & Economy
- **Access**: Open via chat commands (`/store` or `/cosmetics`) or the dedicated hotkey (default: **`K`**).
- **Playtime Currency**: Players earn **1 Coin** for every **1 hour (3600 seconds)** spent actively in Survival or Adventure mode (creative and spectator modes do not accumulate playtime). Playtime is counted by the **server**, so it works identically for every client and cannot be spoofed.
- **Progress Tracking**: The "Tasks & Playtime" tab displays total survival playtime down to the second, alongside a live progress bar tracking time toward the next coin reward. The GUI live-refreshes whenever the server syncs new state.
- **Three-Tab Layout**: The store is organized into **Store & Wardrobe** (capes), **Pets** (3D companion pets with a spinning in-GUI preview), and **Tasks & Playtime** tabs.
- **Equipping from the Wardrobe**: The large cape/pet preview panel is **display-only** — equip/unequip is done on the item cards in the list, keeping the preview clean.
- **Server-Side Persistent Storage**: All progression (coins, unlocked capes, equipped cape, playtime, PvP kills, unlocked pets, equipped pet, completed tasks) is owned by the server and persisted per-world in the `alyrion_cosmetics` saved data (inside the world folder, `data/alyrion_cosmetics.dat`). Each server/world has its **own independent** progression for every player UUID — nothing is stored in `config/` anymore. The client only holds a transient mirror that is re-synced on every login and wiped on logout.
- **PvP Kill Rewards**: Every direct player kill made in Survival/Adventure counts toward kill-based tasks. The **Grim Reaper** task (`task_kills`) awards **+5 Coins** and the **Grim Cape** after **10 player kills** — or the cape can simply be bought for **10 Coins** in the store.

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
| **Grim Cape** | `grim.png` | **10 Coins** | Buy or complete *"Grim Reaper"* (10 PvP kills) | Jet-black cape with a bleached skeleton head, earned by forging a grim reputation. |
| **Pride Cape** | `pride.png` | **Not Buyable** | Complete *"United We Stand"* (party of 4+ via Open Parties and Claims) | Vibrant rainbow cape earned by partying up with at least 4 players in an OPAC party. |

---

### Custom Pets Collection

The **Pets** tab lets players purchase and equip 3D companion pets that follow them around — server-validated and synced to every nearby player just like capes:

| Pet | Texture Identifier | Price | Unlock Condition | Description |
|---|---|---|---|---|
| **Satellite Pet** | `pets/satellite.png` | **15 Coins** | Buy in the Pets tab | A little gold research satellite that orbits above your head — twin blue solar wings, a tilted antenna dish and a blinking beacon light. |

#### How Pets Are Rendered (`SatellitePetModel` + `SatellitePetLayer`)
- **Box-geometry model**: Authored as vanilla `ModelPart` cubes (gold body, mirrored solar wings, mast + dish, separate beacon light) against a 128x32 texture atlas generated by `generate_satellite_pet.py`.
- **Orbital motion**: The pet circles the player's head with a gentle vertical bob (orbit radius ~0.94 blocks, ~2.35 blocks above the ground), self-spin, and a lazy sway — the dish always angles toward the player.
- **Blinking beacon**: The antenna light pulses on a fixed blink cycle, tinted and rendered with full brightness.
- **Hidden when invisible**: Invisible players don't render their pet.
- **Store preview**: A **spinning 3D satellite** rendered live inside the Pets tab's preview panel, with the pet's current status (Locked / Unlocked / Equipped), price and description.
- **Multiplayer**: Pets are only rendered client-side for players whose equipped pet the server has broadcast via `s2c_sync_pet` — the same server-authoritative model used for capes.

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
5. **Grim Reaper** (`task_kills`):
   - **Trigger**: Slay **10 players** in Survival mode.
   - **Reward**: **+5 Coins** + immediate unlock of the **Grim Cape**.
6. **United We Stand** (`task_party`):
   - **Trigger**: Be a member of an **Open Parties and Claims** party with at least **4 members**.
   - **Reward**: **+5 Coins** + immediate unlock of the **Pride Cape** (cannot be bought — task-only).

> **Note on the Pride Cape**: it is deliberately **not purchasable** in the store (the server rejects coin purchases and the store shows a disabled *Task Reward* state). It is only awarded by completing the *United We Stand* task. When OPAC isn't installed, the task simply never completes, so the cape harmlessly stays locked.

---

### 3D Cape Rendering Engine & Motion Physics

AlyrionCore implements `AlyrionCapeLayer.java`, a custom player render layer attached to all player skins (both `default` / wide and `slim` / Alex models):

- **Authentic Cloak Physics**: Implements real-time interpolation of player velocity, body rotation, walking bobbing, and vertical descent momentum to calculate natural cape sway and trailing angles.
- **Crouch Adjustments**: Automatically offsets and angles the cape when the player sneaks.
- **Smart Elytra Handling**: Seamlessly hides the cosmetic cape when an Elytra is equipped in the chest slot, preventing visual clipping and model artifacts.

---

### Real-Time Multiplayer Synchronization

All cosmetics state is synchronized using NeoForge custom payload networking (`CosmeticNetworking.java`). The client never decides anything — it requests, the server validates and persists, then the server broadcasts the result:

- **Client-to-Server (`c2s_equip_cape`)**: The client requests to equip/unequip a cape. The server validates that the cape is actually unlocked for that player before applying it.
- **Client-to-Server (`c2s_purchase_cape`)**: The client requests a purchase. The server checks the player's coin balance, deducts coins, unlocks the cape and equips it — all against the world's saved data.
- **Client-to-Server (`c2s_request_cosmetics`)**: Fallback sync request, used when the store is opened before the login sync arrives.
- **Client-to-Server (`c2s_equip_pet` / `c2s_purchase_pet`)**: Pet equivalents of the cape requests — the server validates pet ownership and deducts coins from the player's balance.
- **Server-to-Client (`s2c_sync_cosmetics`)**: The full authoritative state for a player (coins, playtime, PvP kills, unlocked capes, equipped cape, completed tasks, unlocked pets, equipped pet) — pushed on login and after every state change.
- **Server-to-Client (`s2c_sync_cape`)**: The server broadcasts a player's equipped cape ID to all clients tracking that entity, so everyone sees the correct capes.
- **Server-to-Client (`s2c_sync_pet`)**: The server broadcasts a player's equipped pet ID to all clients tracking that entity (and on login), so everyone sees the correct orbiting pets.
- **Server-to-Client (`s2c_play_sound`)**: Reward sounds are triggered by the server (e.g. a coin earned or a task completed) and played locally.
- **Server Tick Driver**: Playtime accumulation and milestone task detection run on the server tick (`ServerCosmeticsEvents`), so progression is identical for every client and persists in the world save.

---

### Developer & Admin Controls

Dev/test overrides are **ops-only server commands** (replacing the old client-side Dev Controls tab, which could edit progress on any world or server from the client):

- `/alyrioncosmetics coins` — anyone can view their own coins, playtime and cape count.
- `/alyrioncosmetics addcoins <player> <amount>` — grant coins (op level 2).
- `/alyrioncosmetics addplaytime <player> <seconds>` — add survival playtime, awarding any coins earned along the way (op level 2).
- `/alyrioncosmetics completetask <player> <task>` — instantly complete a task (`task_space`, `task_moon`, `task_mars`, `task_dragon_egg`, `task_kills`, `task_party`) (op level 2).
- `/alyrioncosmetics resettasks <player>` — reset a player's task progression (op level 2).
- `/alyrioncosmetics resetcosmetics <player>` — reset a player's cosmetic unlocks (op level 2).

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
  - `bed_works: false` – Beds explode on the open Martian surface — *unless* placed inside a **pressurized sealed habitat** (see [Habitat Construction & Life Support](#-habitat-construction--life-support)), or you use a **Mars Sleeping Pod** (see [Sleeping Pods & Rest on Mars](#-sleeping-pods--rest-on-mars)).
  - `respawn_anchor_works: true` – Respawn Anchors function as stable planetary spawn beacons.
  - `has_raids: false` – Illager raids cannot initiate on Mars.
  - `ultrawarm: false`, `natural: false`.

---

### Atmospheric Rendering & Celestial Optics

The visual environment of Mars is handled by `MarsDimensionEffects.java`:

#### 1. Butterscotch Daylight Sky & Dust Scattering
Because fine iron oxide (ferric dust) is permanently suspended in the thin Martian troposphere, Rayleigh scattering is subdued and dust-induced scattering dominates. During daylight hours, the fog color is mathematically transformed into a characteristic butterscotch / rusty-amber tone:

$$\vec{C}_{\text{fog}} = \text{brightness} \times \begin{pmatrix} 0.85 \times (0.4 R_0 + 0.6) \\ 0.52 \times (0.4 G_0 + 0.6) \\ 0.32 \times (0.4 B_0 + 0.6) \end{pmatrix}$$

This base tone is now **dynamically modulated by the live dust-storm intensity**: as storms intensify, the ferric-dust channel dims red the least and blue the most, and an overall dimming factor darkens the scene — see [Dynamic Mars Weather](#-dynamic-mars-weather--dust-storm-system).

#### 2. Forward Mie Scattering Blue Sunset & Sunrise
On Earth, fine gas molecules scatter blue light in all directions (making the sky blue and sunsets red). On Mars, microscopic dust particles are comparable in size to the wavelength of visible light. This creates **forward Mie scattering**, allowing blue wavelengths to pass straight through the dusty horizon directly surrounding the Sun while red wavelengths scatter outward into the wider sky.

The mod computes exact angular solar color transitions:
- **Red component**: Subdued ($f_1 \times 0.25 + 0.15$)
- **Green component**: Balanced ($f_1^2 \times 0.55 + 0.35$)
- **Blue component**: Dominant vivid azure ($f_1^2 \times 0.95 + 0.55$)

During severe dust storms (intensity > 0.65), the Sun is obscured behind the uniform ochre haze and the blue-sunset effect is suppressed entirely, as in a real Martian global dust event.

#### 3. Cloudless Thin Atmosphere
Water-vapor clouds are disabled (`cloudHeight = Float.NaN`), representing the extremely dry, thin, and desiccated Martian atmosphere.

---

### 🌪️ Dynamic Mars Weather & Dust Storm System

Mars gets a **living, seasonally correct weather system** simulated server-side in `MarsWeatherSavedData` and rendered client-side by `MarsClientWeatherHandler`.

### Weather States

| State | Identifier | Base Intensity | Max Wind | Description |
|---|---|---|---|---|
| **Clear Skies** | `clear` | `0.00` | `0.05` | Calm, thin-atmosphere conditions. |
| **Dust Devil Activity** | `dust_devils` | `0.20` | `0.30` | Midday thermal convection whips up small dust devils. |
| **Regional Dust Storm** | `regional_storm` | `0.65` | `0.75` | A large regional storm front; heavy airborne dust. |
| **Global Planet-Encircling Dust Storm** | `global_dust_storm` | `1.00` | `1.00` | A Mars-wide "apocalyptic" storm — near-total dust blackout. |

### Martian Season Simulation (The Sol Cycle)
- The Mars clock runs on **sols** (`sol` day-time controller), with 668 sols per Martian year.
- **Perihelion / Southern Summer (Sols 420–580, Ls 200°–300°)** — the *storm season*: elevated probabilities for regional storms (which can cascade into global storms) and a notable chance of planet-encircling dust storms.
- **Aphelion / Northern Summer** — calm skies dominate; global storms are extremely rare (2% chance) and regional storms decay quickly.
- Storm intensity and wind speed **interpolate smoothly** toward each state's targets, and wind direction slowly rotates over time.

### Dust Devils
- During midday (solar 3500–8500 ticks) or `dust_devils` states, the server spawns **towering conical dust columns** (18–30 blocks tall, 2.5–4.5 block radius) that wander with subtle drift and live 30–90 seconds.
- Client-side, each devil renders a rotating, widening vortex of rust-orange `DustParticleOptions` and Martian-regolith block particles within 100 blocks of the player.
- Global storms disperse localized convective columns — the whole sky becomes the storm instead.

### Storm-Aware Rendering
- **Ambient wind & dust particles** scale with smoothed storm intensity (up to 28 particles/tick, blown in the server wind direction).
- **Fog ramp**: `ViewportEvent.RenderFog` collapses the far plane from a clear view down to ~12–30 blocks during a global storm (dense Martian dust blackout), with regional storms at ~42–82 blocks.
- **Fog color**: `ComputeFogColor` blends the clear butterscotch tone into a deep, uniform apocalyptic ochre/terracotta as intensity rises.
- **Sunset suppression** and **fog dimming** inside `MarsDimensionEffects` (see above).

### Weather Command (`/marsweather`)
| Subcommand | Permission | Description |
|---|---|---|
| `/marsweather query` | OP (2) | Reports current state, intensity, sol / 668 and season (Perihelion vs Aphelion). |
| `/marsweather set <state> [duration]` | OP (2) | Forces a weather state (e.g. `global_dust_storm`) for a given duration in ticks (default 24000). |

Weather is broadcast to Mars players every 20 ticks over the `s2c_mars_weather` custom payload, re-sent on dimension change and login.

---

### 🏠 Habitat Construction & Life Support

The Martian surface is a vacuum: hostile to life without shelter. AlyrionCore adds a **sealed-habitat simulation** (`HabitatSealManager`) that detects airtight enclosed rooms and grants breathable air inside them.

### How Sealing Works
- A **flood-fill** is run from the player's position. If the fill reaches open sky or the build-height/void boundaries before being stopped by airtight blocks, the position is *unsealed*.
- Airtight boundary blocks are honored **before** any sky-exposure test, so **above-ground habitats and glass roofs/skylights seal correctly** (they are the sky surface, but still hold pressure).
- Sealing works on **every vacuum world**, not just Mars: the `VacuumAtmosphere` compat reads the space mod's public atmosphere API, so habitats on the **Moon** (`rocketnautics:moon`) and in **deep space** pressurize too — with no hardcoded dimension list and without touching the planet data. Without the space mod installed it falls back to Mars only.
- Rooms up to **6,144 blocks** of interior volume are supported (larger spaces are considered open-world).
- Results are cached and invalidated on block breaks (with a **depressurization burst**: `poof`/`cloud`/`snowflake` particles, hissing fire-extinguish audio and a muffled explosion sound when a sealed boundary is breached). The server log also records the exact leak position + direction whenever a seal check fails, for quick leak-finding.

### Airtight Materials
Any solid-render / full-collision block seals, plus:
- **Glass** & **tinted glass** and anything in `#minecraft:impermeable`
- **Iron doors** and **iron trapdoors** (only while closed)
- **Airlock blocks** (only while closed — see below)
- **Sleeping pods** (fully pressurized capsules)

### Breathable Air (`LivingBreatheEvent`)
- On vacuum worlds, a sealed room is breathable **only while at least one powered Oxygen Generator runs inside it** (see below). The seal check returns both *sealed* and *oxygenated* state; while oxygenated, air is granted via the breathe event *and* restored to maximum after every entity tick (server-side), so no other mod's breathing logic can override a powered habitat.
- A sealed room with an **unpowered / missing** generator is *not* breathable: your air drains and you drown — the generator is the difference between a habitat and a tomb.
- The client air bar is synchronized with the server result — no phantom "drowning" bubbles inside a powered habitat.
- Outside a seal — on the open surface — vacuum suffocation applies (creative is exempt).
- In-game feedback: an actionbar shows **"✔ Pressurized habitat detected — breathing"** when you enter a powered sealed room, **"⚠ Habitat sealed — no oxygen generator running!"** when the room is sealed but starved of power, and **"⚠ Habitat breached — depressurizing!"** the moment a sealed habitat loses pressure (airlock opened, wall broken).

### The Oxygen Generator (`oxygen_generator`)

The **heart of every habitat**: a meteoric-iron machine with a teal coolant tank, front dial and an animated spinning impeller (rendered by `OxygenGeneratorBlockEntityRenderer`). It is what makes a sealed room breathable.

- **Requires FE (Forge Energy)**: the block entity exposes a standard receive-only `IEnergyStorage` capability (16,000 FE buffer, up to 1,024 FE/t input). Any FE source can charge it — in the reference pack, **Create: Power Grid** supplies it through its **Device Connector / FE Inverter** (solar panel → battery → connector → generator). Jade shows the stored charge.
- **Always-on drain**: while it has stored FE the machine runs and consumes **4 FE/t** (~80 FE/s); a full buffer lasts ~3.3 minutes. Keep the grid charged or the habitat depressurizes.
- **Active state**: while running the block emits light level 8, the dial and tank glow (model switches to the `active` variant) and the vent fan spins; when the buffer runs dry it goes dark and still.
- **Airtight**: a full solid cube, so it can be built into walls and roofs and still seal — the seal scan finds it even when it's part of the boundary.
- Crafted from meteoric iron ingots + glass panes + redstone; drops itself (charge is lost on break).

### The Pressurized Airlock (`airlock`)

A **two-block-tall airtight door** (`AirlockBlock`, extends `DoorBlock`) that seals habitats while closed, featuring a fully animated hatch:

- **Static bulkhead frame**: The doorway is framed by a fixed titanium bulkhead model (jambs + sill/header) with hazard-stripe markings — the blockstate only carries `facing` and `half` variants, while the door leaf is drawn at runtime.
- **Animated hatch leaf**: The heavy armored door leaf is rendered by `AirlockBlockEntityRenderer` (block entity registered in `ModBlockEntities.AIRLOCK`). It swings **90° around its hinge with smoothstep pneumatic easing**, folding **inward against the jamb inside the block cell** (never swinging out into the room), and the upper half carries a **translucent viewport window**.
- **Status LED**: A status LED on the header — **green when sealed** (closed), **red when venting** (open), and **blinking while the hatch is mid-swing**.
- **Collision & sealing**: While closed the airlock is a full solid block (sealing the habitat); while open only the **2-px-thick bulkhead jambs** remain solid, leaving a walkable doorway opening between them.
- Right-clicking cycles open/closed with a **pneumatic hiss** (iron-door sounds at different pitches) and toggles **both door halves together** (a single click fully seals — no half-open leaks); opening an airlock breaks the seal, closing it restores it.
- Tagged `#minecraft:doors` and `#minecraft:mineable/pickaxe`; beacon base-compatible.

### Beds Inside Sealed Habitats
Vanilla beds would explode on Mars (`bed_works: false`) — but a bed inside a **pressurized sealed habitat** is safe: the explosion is intercepted and the player sleeps normally (with correct `isDayTime` recomputation and dust-storm night equivalence, see below).

---

### 🛏️ Sleeping Pods & Rest on Mars

The **Mars Sleeping Pod** (`sleeping_pod`) is a two-block technological bed that provides safe rest on the Martian surface:

- **Two-part construction**: foot + head blocks placed in the facing direction (like a bed), with custom models, glass casing and interior screen textures.
- **Sleeping rules**: usable day or night under the Martian clock — vanilla's cached `skyDarken` is unreliable on custom dimensions, so daylight is recomputed directly from the dimension clock (plus rain/thunder).
- **Dust storms = thunderstorms**: a regional or global dust storm darkens the sky enough that the pod may be used even during daylight — the local analogue of vanilla's thunderstorm sleeping rule.
- **Respawn anchoring**: sleeping in a pod sets your respawn point in the current dimension; safe stand-up position is computed like a bed.
- **Occupancy handling**: occupied pods refuse entry ("This sleeping pod is occupied") and both halves share occupancy state.
- A **vanilla bed inside a sealed habitat** receives the same treatment via `CanPlayerSleepEvent` / `CanContinueSleepingEvent` overrides.
- **Night-skip fix for custom dimensions**: Custom dimensions share the Overworld's time-of-day clock through `DerivedLevelData`, whose `setDayTime`/`setGameTime` are intentional no-ops (Mojang MC-190731: *"Sleep doesn't advance to day in custom dimensions"*) — vanilla sleep would wake players but never advance the night. On `SleepFinishedTimeEvent`, the mod now applies the computed morning time to the Overworld clock, which the Mars level reads back, so sleeping on Mars actually skips the night.

---

### Space Mod & Celestial Orbit Interoperability

AlyrionCore includes pre-configured planetary definitions and physical parameters compatible with modern NeoForge space exploration mods (such as *Cosmonautics* / *Rocketnautics*):

- **Celestial Orbit**: Sol-centered circular orbit with period $823,200\text{ s}$ and rotation period $1,230\text{ s}$.
- **Atmospheric Composition**: Configured with `low_density` and `drowning` atmospheric hazards above $Y = 5,000$, requiring pressurized life support equipment.
- **Planetary Surface Rendering**: Built-in 3D celestial sphere texture sampler with accurate RGB palette mapping across all 6 Martian biomes.
- **Atmospheric Drag**: Multi-tier altitude drag curves ($0.2\times$ drag below $4,000\text{ m}$ tapering to $0.0$ at orbital insertion).
- **Gravity Ownership**: `apply_gravity_correction_to_entities_in_dimension` is set to `false` in both datapacks — the mod's own `MarsPhysicsHandler` attribute system owns Martian gravity to avoid double-application with the space mod.
- **Vacuum-Aware Life Support**: `VacuumAtmosphere` reads the space mod's public atmosphere API (`DeepSpaceHelper`) so sealed habitats grant breathable air on any world flagged `drowning` (Mars, the Moon, deep space) — cooperative compat, not a priority fight over the breathe event.
- **FE-Powered Habitats**: the **Oxygen Generator** runs on standard Forge Energy (`IEnergyStorage`), so it plugs straight into **Create: Power Grid**'s electricity network through its **Device Connector / FE Inverter** (solar panel → battery → connector → generator) — no custom energy system, no adapter needed.

---

### 🌑 Martian Moons: Phobos & Deimos

Mars's two tiny, lumpy moons are added as **tidally locked celestial bodies** visible from space, with bespoke high-resolution textures:

| Moon | Radius | Surface Gravity | Orbital Period | Notes |
|---|---|---|---|---|
| **Phobos** | 11,000 m | 0.0057 m/s² | 382.3 s | Inner moon; orbits Mars faster than Mars rotates. |
| **Deimos** | 6,200 m | 0.003 m/s² | 1,516.1 s | Outer moon; tidally locked, rotates once per orbit. |

- Defined as children of Mars (`"parent": "mars"`) in both `data/alyrioncore/universe_planets/` and `data/rocketnautics/universe_planets/`.
- Custom textures live in `assets/alyrioncore/textures/planet/` (mirrored into `assets/rocketnautics/textures/planet/` for compatibility).
- Generated by `generate_martian_moons.py`.

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

### 🛰️ Crashed Space Probe Structures

Two hand-built jigsaw crash sites scatter across the Martian surface — relics of failed early exploration:

| Structure | NBT | Loot |
|---|---|---|
| **Crashed Soviet Probe** | `structure/crashed_soviet_probe.nbt` | Salvageable science chest |
| **Crashed US Probe** | `structure/crashed_us_probe.nbt` | Salvageable science chest |

- Registered as a `crashed_probes` structure set (spacing 18, separation 6) valid in **all 6 Martian biomes**.
- Chest loot includes spyglasses, gold/copper ingots, redstone, quartz, daylight detectors, Martian Rock Samples and Raw Meteoric Iron.
- NBT structures and jigsaw pools/configs generated by `generate_probe_structures.py`.

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

### Resource & Storage Blocks

| Block | Identifier | Hardness / Resistance | Tool Required | Notes |
|---|---|---|---|---|
| **Block of Meteoric Iron** | `meteoric_iron_block` | `5.0` / `6.0` | Pickaxe (Iron+) | Compacted meteoric nickel-iron; beacon base-compatible, `#minecraft:beacon_base_blocks`. |
| **Block of Raw Meteoric Iron** | `raw_meteoric_iron_block` | `4.5` / `5.0` | Pickaxe (Iron+) | Storage form for unrefined meteoric ore. |
| **Block of Olivine** | `olivine_block` | `4.0` / `5.0` | Pickaxe (Iron+) | Stored Peridot crystals; beacon base-compatible. |
| **Block of Sulfur** | `sulfur_block` | `2.0` / `3.0` | Pickaxe (Stone+) | Compacted volcanic sulfur; burns as an **excellent furnace fuel** (800 seconds / 80 items) and converts to Gunpowder. |

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

## 🌱 Greenhouse Farming & Martian Agriculture

Martian soil is surprisingly fertile in fiction — and in AlyrionCore, *if* you build the right infrastructure. Farming on Mars requires **pressurized greenhouses**, mimicking real-world Martian agriculture research.

### Martian Regolith Farmland (`regolith_farmland`)
- **Tilling**: Use a **Hoe** on `martian_regolith` or `martian_sand` to convert it into Regolith Farmland (`BlockToolModificationEvent`).
- **Moisture**: A standard 0–7 moisture property. Water within 4 blocks (or a Waterlog ticket) saturates it to level 7; without water it dries out and reverts to plain regolith.
- **Trampling**: Heavy entities can trample it back into regolith (vanilla farmland rules); crops growing on top keep it maintained.

### Martian Potato Crop (`martian_potato_crop`)
- An 8-stage crop (`stage0`–`stage7`) that grows on Regolith Farmland or vanilla Farmland.
- **The Martian greenhouse rule**: on Mars, the crop **only grows inside a pressurized sealed habitat** (checked via `HabitatSealManager`) with light level ≥ 9 (artificial greenhouse lighting). Exposed to the freezing vacuum, growth halts and the plant has a chance to wither into a dead bush.
- **Procedural alien crop visuals**: All 8 stages are procedurally painted by `generate_habitat_greenhouse_assets.py` — layered pinnate compound potato leaves with jittered organic edges, lit/shadow shading, **crimson midrib veins**, tapered stems, white flowers with yellow cores, and at maturity lumpy tubers emerging from a **regolith soil mound** so the plant reads as truly planted. Stage models render with a `cutout` render type for clean transparency.
- Harvest yields **Martian Potatoes**; on Earth-like dimensions it behaves like a normal crop.

### Martian Food
| Item | Identifier | Nutrition | Saturation | Notes |
|---|---|---|---|---|
| **Martian Potato** | `martian_potato` | 3 | 0.6 | Also the seed for the crop (`ItemNameBlockItem`). |
| **Baked Martian Potato** | `baked_martian_potato` | 6 | 0.8 | Smelted / smoked / campfire-cooked from raw Martian Potatoes. |

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
| **Dry Ice Shard** | `dry_ice_shard` | Volatile | Sub-zero crystallized shard of solid $CO_2$ harvested from polar ice caps. Use it to flash-freeze foes or freeze water into ice. |
| **Martian Rock Sample** | `martian_rock_sample` | Research | Geological sample of Martian rock. Use it to crush out 1–3 random minerals: Hematite Nodules, Sulfur Dust, Raw Copper, Raw Meteoric Iron or Olivine Crystals. |

---

### Meteoric Iron Equipment Tier

Forged from meteoric nickel-iron alloy, the **Meteoric Iron tier** (`ModToolTiers.METEORIC_IRON`) sits between Diamond and Netherite in utility:

| Stat | Value |
|---|---|
| Harvest Level | Diamond (`#minecraft:incorrect_for_diamond_tool`) |
| Durability | 650 |
| Mining Speed | 7.5 |
| Attack Damage Bonus | 2.5 |
| Enchantability | 16 |
| Repair Ingredient | `meteoric_iron_ingot` |

| Item | Identifier | Notes |
|---|---|---|
| **Meteoric Iron Sword** | `meteoric_iron_sword` | +3 damage, -2.4 speed. |
| **Meteoric Iron Pickaxe** | `meteoric_iron_pickaxe` | +1 damage, -2.8 speed. |
| **Meteoric Iron Axe** | `meteoric_iron_axe` | +6 damage, -3.1 speed. |
| **Meteoric Iron Shovel** | `meteoric_iron_shovel` | +1.5 damage, -3.0 speed. |
| **Meteoric Iron Hoe** | `meteoric_iron_hoe` | -2 damage, -1.0 speed. |

All five tools are registered in their respective `#minecraft:item` tags (`swords`, `pickaxes`, `axes`, `shovels`, `hoes`), and `meteoric_iron_ingot` / `olivine_gem` are valid **beacon payment items**.

### Interactive Items: Dry Ice Shards & Rock Samples

- **Dry Ice Shard** (`DryIceShardItem.java`):
  - **Use (right-click)**: a flash-freeze blast in a 5-block radius — freezes targets (+160 freeze ticks), slows them (Slowness II) and deals 3 freeze damage, with a snowflake particle burst.
  - **Use on water**: instantly freezes a water source block into ice.
  - Can be recombined into / split from Dry Ice Blocks via recipes.
- **Martian Rock Sample** (`MartianRockSampleItem.java`):
  - **Use (right-click)**: crushes the sample, yielding 1–3 minerals with weighted drops (Hematite Nodules 35%, Sulfur Dust 25%, Raw Copper 20%, Raw Meteoric Iron 13%, Olivine Crystal 7%), plus stone-break particles and amethyst chime audio.
  - Also used in the `smooth_stone_from_rock_sample` recipe.

---

## 🎨 Creative Mode Integration

AlyrionCore adds a dedicated Creative Mode Tab: **`AlyrionCore: Mars & Planetary Geology`** (`itemGroup.alyrioncore.mars`).

The tab icon features the **Martian Rock Sample** (`martian_rock_sample`) and organizes all planetary materials into logical groupings:
1. **Scientific Samples & Minerals**: Rock Sample, Hematite Nodule, Raw Meteoric Iron, Meteoric Ingot, Raw Copper, Sulfur Dust, Olivine Gem, Dry Ice Shard.
2. **Meteoric Equipment**: Sword, Pickaxe, Axe, Shovel, Hoe.
3. **Resource & Storage Blocks**: Block of Meteoric Iron, Block of Raw Meteoric Iron, Block of Olivine, Block of Sulfur.
4. **Soils & Regolith**: Martian Sand, Regolith, Coarse Regolith, Frost-Dusted Regolith, Permafrost.
5. **Stones & Architectural Blocks**: Basalt, Polished Basalt, Basalt Bricks, Basalt Tiles, Stratified Stone, Scoria, Impact Breccia.
6. **Planetary Ores**: Hematite Ore, Meteoric Iron Ore, Copper Ore, Sulfur Ore, Olivine Ore.
7. **Polar Volatiles & Ices**: Glacial Ice, Dry Ice Block.
8. **Technology, Habitat & Greenhouse**: Sleeping Pod, Pressurized Airlock, Oxygen Generator, Regolith Farmland, Martian Potato, Baked Martian Potato.

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
- `regolith_farmland`

### Pickaxe Mineable (`#minecraft:mineable/pickaxe`)
- `martian_basalt`, `polished_martian_basalt`, `martian_basalt_bricks`, `martian_basalt_tiles`
- `stratified_martian_stone`, `martian_volcanic_scoria`, `martian_impact_breccia`
- `hematite_ore`, `meteoric_iron_ore`, `martian_copper_ore`, `martian_sulfur_ore`, `martian_olivine_ore`
- `martian_ice`, `dry_ice_block`, `martian_permafrost`
- `meteoric_iron_block`, `raw_meteoric_iron_block`, `olivine_block`, `sulfur_block`
- `sleeping_pod`, `airlock`, `oxygen_generator`

### Mining Tiers
- **Stone Tool or Better (`#minecraft:needs_stone_tool`)**:
  - `hematite_ore`, `martian_copper_ore`, `martian_sulfur_ore`
  - `martian_basalt`, `polished_martian_basalt`, `martian_basalt_bricks`, `martian_basalt_tiles`
  - `stratified_martian_stone`, `martian_volcanic_scoria`
- **Iron Tool or Better (`#minecraft:needs_iron_tool`)**:
  - `meteoric_iron_ore`
  - `martian_olivine_ore`
  - `martian_impact_breccia`
  - `meteoric_iron_block`, `raw_meteoric_iron_block`, `olivine_block`
- **Any Pickaxe (no tier tag)**: `sulfur_block` drops correctly with any pickaxe.

> The **Meteoric Iron** equipment tier itself mines at the Diamond harvest level, so it can collect every AlyrionCore ore and block.

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
- `generate_capes.py`: Regenerates all 64x32 custom capes with anti-aliased pixel art (includes the Grim Cape and the Pride Cape's full-bleed rainbow).
- `generate_new_textures.py`: Generates the Meteoric Iron equipment tier, resource-block textures and the Martian Potato / Baked Martian Potato item art.
- `generate_sleeping_pod_assets.py`: Generates the two-block Sleeping Pod blockstates, multi-part models and interior/casing/glass textures.
- `generate_habitat_greenhouse_assets.py`: Generates the Airlock bulkhead frame models + blockstate (the animated hatch is rendered at runtime), the Regolith Farmland / crop-stage blockstates, models and textures, and the procedurally painted 8-stage Martian Potato plant textures.
- `generate_airlock_assets.py`: Generates the Airlock textures — titanium bulkhead frame, armored hatch leaf, viewport window, status LEDs (green/red) and the item icon.
- `generate_oxygen_generator.py`: Generates the Oxygen Generator machine textures — meteoric-iron casing plate, teal coolant tank (+ lit variant), front dial (+ lit), status-LED glow and the impeller blade for the animated fan.
- `generate_martian_moons.py`: Generates Phobos & Deimos `universe_planets` JSON and celestial sphere textures (AlyrionCore + Rocketnautics datapacks).
- `generate_probe_structures.py`: Generates the crashed Soviet/US probe NBT structures, jigsaw pools, structure/structure-set configs and chest loot.
- `generate_recipes_and_loot.py`: Generates the full recipe catalog and block/chest loot tables.
- `generate_satellite_pet.py`: Generates the 128x32 texture atlas (gold body, solar wings, dish, beacon) for the Satellite Pet 3D model.
- `generate_mstexture.py`: Std-lib-only PNG writer toolkit used to author pixel-art item textures (Meteoric Iron pickaxe artwork).
- `scratch_nbt.py`: Internal helper used for hand-authoring NBT structure payloads.

### Recipe Catalog (selection)
- **Meteoric Iron**: tools, storage blocks, raw-block packing, ingot smelting/blasting from ore and raw, iron nuggets from hematite nodules.
- **Greenhouse & Habitat**: Sleeping Pod, Airlock, Oxygen Generator, Regolith Farmland (via hoe-tilling), Gunpowder (sulfur + coal/charcoal/bonemeal), Torches from sulfur, Glass from Martian Sand, Terracotta from regolith, Water Bucket from Martian Ice, Packed Ice, Snow Block from dry ice, Spyglass from Olivine, Smooth Stone from Rock Samples.
- **Food**: Baked Martian Potato (smelting, smoking, campfire).
- **Masonry**: Polished basalt, bricks and tiles via crafting, smelting/blasting and stonecutting chains.

### Mod Metadata & Architecture
- **Mod ID**: `alyrioncore`
- **Mod Name**: AlyrionCore
- **Mod Version**: `1.0.0`
- **Group ID**: `xyz.alyrion.alyrioncore`
- **Supported NeoForge Version**: `21.1.186+`
- **Target Minecraft Version**: `1.21.1`
- **Optional Dependency — Open Parties and Claims (`openpartiesandclaims` ≥ `0.26.1`)**: declared as an *optional* NeoForge dependency so the mod loads fine without it. When present, `OpacCompat` resolves OPAC's server API reflectively (`OpenPACServerAPI` → `IPartyManagerAPI` → `IServerPartyAPI`) to power the *United We Stand* party task; with OPAC absent every party check simply returns `false`.

---

## 📄 License & Credits

- **Developer**: Alyrion Team
- **Mod ID**: `alyrioncore`
- **License**: All Rights Reserved

*Created with precision for the Alyrion SMP.*
*This mod is made using AI. If you are not okay with that, feel free to apply as our unpaid full-time developer or artist.*
