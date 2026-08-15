#!/usr/bin/env python3
"""
Generate complete Crashed Soviet and US Mars Probe structures for Minecraft 1.21.1 / NeoForge:
- Generates binary NBT structure templates (.nbt)
- Template pools (Jigsaw)
- Structure definitions
- Structure sets
- Chest loot tables with atmospheric logs and scientific artifacts
"""

import os
import gzip
import struct
import json

TAG_END = 0
TAG_BYTE = 1
TAG_SHORT = 2
TAG_INT = 3
TAG_LONG = 4
TAG_FLOAT = 5
TAG_DOUBLE = 6
TAG_BYTE_ARRAY = 7
TAG_STRING = 8
TAG_LIST = 9
TAG_COMPOUND = 10
TAG_INT_ARRAY = 11
TAG_LONG_ARRAY = 12

class NBTTag:
    def __init__(self, tag_type, value):
        self.tag_type = tag_type
        self.value = value

def write_tag(f, name, tag):
    f.write(struct.pack('>B', tag.tag_type))
    if name is not None:
        name_bytes = name.encode('utf-8')
        f.write(struct.pack('>H', len(name_bytes)))
        f.write(name_bytes)
    write_payload(f, tag)

def write_payload(f, tag):
    t = tag.tag_type
    v = tag.value
    if t == TAG_BYTE:
        f.write(struct.pack('>b', v))
    elif t == TAG_SHORT:
        f.write(struct.pack('>h', v))
    elif t == TAG_INT:
        f.write(struct.pack('>i', v))
    elif t == TAG_LONG:
        f.write(struct.pack('>q', v))
    elif t == TAG_FLOAT:
        f.write(struct.pack('>f', v))
    elif t == TAG_DOUBLE:
        f.write(struct.pack('>d', v))
    elif t == TAG_STRING:
        str_bytes = v.encode('utf-8')
        f.write(struct.pack('>H', len(str_bytes)))
        f.write(str_bytes)
    elif t == TAG_LIST:
        elem_type, elem_list = v
        f.write(struct.pack('>B', elem_type))
        f.write(struct.pack('>i', len(elem_list)))
        for elem in elem_list:
            write_payload(f, elem)
    elif t == TAG_COMPOUND:
        for k, subtag in v.items():
            write_tag(f, k, subtag)
        f.write(struct.pack('>B', TAG_END))
    elif t == TAG_INT_ARRAY:
        f.write(struct.pack('>i', len(v)))
        for x in v:
            f.write(struct.pack('>i', x))

def NBT_Byte(val): return NBTTag(TAG_BYTE, val)
def NBT_Int(val): return NBTTag(TAG_INT, val)
def NBT_String(val): return NBTTag(TAG_STRING, val)
def NBT_Compound(val): return NBTTag(TAG_COMPOUND, val)
def NBT_List(elem_type, val): return NBTTag(TAG_LIST, (elem_type, val))

class StructureBuilder:
    def __init__(self, size_x, size_y, size_z):
        self.size_x = size_x
        self.size_y = size_y
        self.size_z = size_z
        self.palette = [] # list of (name, properties_dict)
        self.palette_map = {}
        self.blocks = [] # list of (x, y, z, state_idx, nbt_dict)

    def get_state(self, name, props=None):
        props_tuple = tuple(sorted(props.items())) if props else ()
        key = (name, props_tuple)
        if key not in self.palette_map:
            idx = len(self.palette)
            self.palette_map[key] = idx
            self.palette.append((name, props or {}))
        return self.palette_map[key]

    def set_block(self, x, y, z, name, props=None, nbt=None):
        if 0 <= x < self.size_x and 0 <= y < self.size_y and 0 <= z < self.size_z:
            state = self.get_state(name, props)
            self.blocks.append((x, y, z, state, nbt))

    def save(self, filepath):
        os.makedirs(os.path.dirname(filepath), exist_ok=True)
        
        # Build Palette NBT
        palette_tags = []
        for name, props in self.palette:
            comp = {"Name": NBT_String(name)}
            if props:
                prop_comp = {k: NBT_String(v) for k, v in props.items()}
                comp["Properties"] = NBT_Compound(prop_comp)
            palette_tags.append(NBT_Compound(comp))

        # Build Blocks NBT
        block_tags = []
        for x, y, z, state, nbt_data in self.blocks:
            b_comp = {
                "pos": NBT_List(TAG_INT, [NBT_Int(x), NBT_Int(y), NBT_Int(z)]),
                "state": NBT_Int(state)
            }
            if nbt_data:
                b_comp["nbt"] = nbt_data
            block_tags.append(NBT_Compound(b_comp))

        root = NBT_Compound({
            "DataVersion": NBT_Int(3955),
            "size": NBT_List(TAG_INT, [NBT_Int(self.size_x), NBT_Int(self.size_y), NBT_Int(self.size_z)]),
            "palette": NBT_List(TAG_COMPOUND, palette_tags),
            "blocks": NBT_List(TAG_COMPOUND, block_tags),
            "entities": NBT_List(TAG_COMPOUND, [])
        })

        with gzip.open(filepath, 'wb') as f:
            write_tag(f, "", root)
        print(f"Saved structure NBT: {filepath} ({len(self.blocks)} blocks)")


def build_soviet_probe():
    # 9x7x9 crater and probe footprint
    sb = StructureBuilder(9, 7, 9)
    
    # 1. Impact Crater depression in ground (y=0 to 1)
    for x in range(9):
        for z in range(9):
            dist_sq = (x - 4)**2 + (z - 4)**2
            if dist_sq <= 16:
                # Crater floor: scorched impact breccia and scoria
                if dist_sq <= 6:
                    sb.set_block(x, 0, z, "alyrioncore:martian_impact_breccia")
                else:
                    sb.set_block(x, 0, z, "alyrioncore:martian_volcanic_scoria")
                # Crater rim ejecta
                if 8 <= dist_sq <= 16:
                    sb.set_block(x, 1, z, "alyrioncore:coarse_martian_regolith")

    # Scattered heat shield fragments
    sb.set_block(1, 1, 3, "minecraft:iron_bars")
    sb.set_block(2, 1, 7, "minecraft:iron_bars")
    sb.set_block(7, 1, 2, "alyrioncore:martian_impact_breccia")
    sb.set_block(7, 1, 6, "minecraft:copper_block")

    # 2. Soviet Spherical / Conical Lander Core (Center at x=4, z=4, y=1 to 4)
    # Base ring / heatshield
    sb.set_block(3, 1, 4, "minecraft:weathered_copper")
    sb.set_block(5, 1, 4, "minecraft:weathered_copper")
    sb.set_block(4, 1, 3, "minecraft:weathered_copper")
    sb.set_block(4, 1, 5, "minecraft:weathered_copper")
    sb.set_block(4, 1, 4, "minecraft:blast_furnace", {"facing": "north", "lit": "false"}) # RTG Power Core

    # Main Capsule Body
    sb.set_block(4, 2, 4, "minecraft:copper_block")
    sb.set_block(3, 2, 4, "minecraft:weathered_copper")
    sb.set_block(5, 2, 4, "minecraft:weathered_copper")
    sb.set_block(4, 2, 3, "minecraft:iron_block")
    
    # Instrument Bay / Chest on North side
    chest_nbt = NBT_Compound({
        "id": NBT_String("minecraft:chest"),
        "LootTable": NBT_String("alyrioncore:chests/crashed_soviet_probe")
    })
    sb.set_block(4, 2, 5, "minecraft:chest", {"facing": "south"}, chest_nbt)

    # Upper Dome / Capsule Top
    sb.set_block(4, 3, 4, "minecraft:lightning_rod", {"facing": "up"})
    sb.set_block(3, 3, 4, "minecraft:daylight_detector") # Solar petal
    sb.set_block(5, 3, 4, "minecraft:daylight_detector") # Solar petal
    sb.set_block(4, 3, 3, "minecraft:chain", {"axis": "y"}) # Deployment tether

    # Parabolic Antenna Mast
    sb.set_block(4, 4, 4, "minecraft:iron_bars")
    sb.set_block(4, 5, 4, "minecraft:lightning_rod", {"facing": "up"})
    sb.set_block(4, 6, 4, "minecraft:end_rod", {"facing": "up"}) # Telemetry transmitter tip

    # Fallen parachute / foil shroud nearby
    sb.set_block(2, 1, 2, "minecraft:red_carpet")
    sb.set_block(2, 1, 3, "minecraft:red_carpet")
    sb.set_block(3, 1, 2, "minecraft:yellow_carpet")
    sb.set_block(1, 1, 2, "minecraft:chain", {"axis": "x"})

    return sb


def build_us_probe():
    # 9x7x9 structure footprint (Viking / Opportunity style)
    sb = StructureBuilder(9, 7, 9)

    # 1. Landing skid marks & dust trench
    for x in range(9):
        for z in range(9):
            dist_sq = (x - 4)**2 + (z - 4)**2
            if dist_sq <= 15:
                if dist_sq <= 5:
                    sb.set_block(x, 0, z, "alyrioncore:martian_sand")
                else:
                    sb.set_block(x, 0, z, "alyrioncore:frost_dusted_regolith")
                if 9 <= dist_sq <= 15:
                    sb.set_block(x, 1, z, "alyrioncore:martian_sand")

    # 2. Tripod Landing Legs (Anvils / Hoppers / Pistons)
    sb.set_block(2, 1, 2, "minecraft:hopper", {"facing": "down"})
    sb.set_block(6, 1, 2, "minecraft:hopper", {"facing": "down"})
    sb.set_block(4, 1, 6, "minecraft:hopper", {"facing": "down"})

    # Lander Central Chassis
    sb.set_block(4, 1, 4, "minecraft:lodestone") # Computer / Inertial Guidance Unit
    sb.set_block(3, 1, 4, "minecraft:smooth_quartz")
    sb.set_block(5, 1, 4, "minecraft:smooth_quartz")
    sb.set_block(4, 1, 3, "minecraft:smooth_quartz")
    sb.set_block(4, 1, 5, "minecraft:smooth_quartz")

    # 3. Solar Panels (Wings extending East-West)
    sb.set_block(2, 2, 4, "minecraft:daylight_detector")
    sb.set_block(1, 2, 4, "minecraft:daylight_detector")
    sb.set_block(6, 2, 4, "minecraft:daylight_detector")
    sb.set_block(7, 2, 4, "minecraft:daylight_detector")

    # Gold foil thermal insulation
    sb.set_block(3, 2, 3, "minecraft:raw_gold_block")
    sb.set_block(5, 2, 3, "minecraft:raw_gold_block")
    
    # Lander Core & Instrument Deck
    sb.set_block(4, 2, 4, "minecraft:observer", {"facing": "up"}) # Optical Surface Scanner
    
    # Scientific Bay / Chest
    chest_nbt = NBT_Compound({
        "id": NBT_String("minecraft:chest"),
        "LootTable": NBT_String("alyrioncore:chests/crashed_us_probe")
    })
    sb.set_block(4, 2, 5, "minecraft:chest", {"facing": "north"}, chest_nbt)

    # 4. High-Gain Dish Antenna & Mast
    sb.set_block(4, 3, 4, "minecraft:lightning_rod", {"facing": "up"})
    sb.set_block(4, 4, 4, "minecraft:iron_bars")
    sb.set_block(4, 5, 4, "minecraft:iron_bars")
    sb.set_block(4, 6, 4, "minecraft:lightning_rod", {"facing": "up"}) # Parabolic feed horn
    sb.set_block(3, 5, 4, "minecraft:iron_trapdoor", {"facing": "east", "half": "top", "open": "true"})
    sb.set_block(5, 5, 4, "minecraft:iron_trapdoor", {"facing": "west", "half": "top", "open": "true"})

    # Sample Arm & Debris
    sb.set_block(3, 2, 5, "minecraft:piston", {"facing": "south"})
    sb.set_block(7, 1, 6, "minecraft:iron_bars")
    sb.set_block(2, 1, 6, "alyrioncore:martian_rock_sample")

    return sb


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)


def generate_configs():
    BASE_DIR = "/Users/lea/alyrioncore/src/main/resources/data/alyrioncore"
    
    # 1. Template Pools (Jigsaw)
    soviet_pool = {
        "name": "alyrioncore:crashed_soviet_probe",
        "fallback": "minecraft:empty",
        "elements": [
            {
                "weight": 1,
                "element": {
                    "element_type": "minecraft:single_pool_element",
                    "location": "alyrioncore:crashed_soviet_probe",
                    "projection": "rigid",
                    "processors": "minecraft:empty"
                }
            }
        ]
    }
    write_json(os.path.join(BASE_DIR, "worldgen/template_pool/crashed_soviet_probe.json"), soviet_pool)

    us_pool = {
        "name": "alyrioncore:crashed_us_probe",
        "fallback": "minecraft:empty",
        "elements": [
            {
                "weight": 1,
                "element": {
                    "element_type": "minecraft:single_pool_element",
                    "location": "alyrioncore:crashed_us_probe",
                    "projection": "rigid",
                    "processors": "minecraft:empty"
                }
            }
        ]
    }
    write_json(os.path.join(BASE_DIR, "worldgen/template_pool/crashed_us_probe.json"), us_pool)

    # 2. Biome Tags for Structure Generation
    all_mars_biomes = [
        "alyrioncore:vastitas_borealis",
        "alyrioncore:valles_marineris",
        "alyrioncore:tharsis_volcanic_plateau",
        "alyrioncore:planum_boreum",
        "alyrioncore:noachis_terra",
        "alyrioncore:olympus_mons"
    ]
    tag_data = {
        "replace": False,
        "values": all_mars_biomes
    }
    write_json(os.path.join(BASE_DIR, "tags/worldgen/biome/has_structure/crashed_probes.json"), tag_data)
    # Also write to minecraft tags for maximum compatibility
    write_json("/Users/lea/alyrioncore/src/main/resources/data/minecraft/tags/worldgen/biome/has_structure/crashed_probes.json", tag_data)

    # 3. Structure Definitions
    soviet_structure = {
        "type": "minecraft:jigsaw",
        "biomes": "#alyrioncore:has_structure/crashed_probes",
        "step": "surface_structures",
        "terrain_adaptation": "beard_thin",
        "spawn_overrides": {},
        "start_pool": "alyrioncore:crashed_soviet_probe",
        "size": 1,
        "start_height": {
            "absolute": 0
        },
        "project_start_to_heightmap": "WORLD_SURFACE_WG",
        "max_distance_from_center": 80,
        "use_expansion_hack": False
    }
    write_json(os.path.join(BASE_DIR, "worldgen/structure/crashed_soviet_probe.json"), soviet_structure)

    us_structure = {
        "type": "minecraft:jigsaw",
        "biomes": "#alyrioncore:has_structure/crashed_probes",
        "step": "surface_structures",
        "terrain_adaptation": "beard_thin",
        "spawn_overrides": {},
        "start_pool": "alyrioncore:crashed_us_probe",
        "size": 1,
        "start_height": {
            "absolute": 0
        },
        "project_start_to_heightmap": "WORLD_SURFACE_WG",
        "max_distance_from_center": 80,
        "use_expansion_hack": False
    }
    write_json(os.path.join(BASE_DIR, "worldgen/structure/crashed_us_probe.json"), us_structure)

    # 4. Structure Set (Spawns both probes across Mars)
    structure_set = {
        "structures": [
            {
                "structure": "alyrioncore:crashed_soviet_probe",
                "weight": 1
            },
            {
                "structure": "alyrioncore:crashed_us_probe",
                "weight": 1
            }
        ],
        "placement": {
            "type": "minecraft:random_spread",
            "spacing": 18,
            "separation": 6,
            "salt": 7842109
        }
    }
    write_json(os.path.join(BASE_DIR, "worldgen/structure_set/crashed_probes.json"), structure_set)

    # 5. Loot Tables for Chests
    soviet_loot = {
        "type": "minecraft:chest",
        "pools": [
            {
                "rolls": {"min": 3, "max": 6, "type": "minecraft:uniform"},
                "entries": [
                    {"type": "minecraft:item", "name": "alyrioncore:raw_meteoric_iron", "weight": 20, "functions": [{"function": "minecraft:set_count", "count": {"min": 1, "max": 4}}]},
                    {"type": "minecraft:item", "name": "alyrioncore:meteoric_iron_ingot", "weight": 10, "functions": [{"function": "minecraft:set_count", "count": {"min": 1, "max": 2}}]},
                    {"type": "minecraft:item", "name": "alyrioncore:sulfur_dust", "weight": 25, "functions": [{"function": "minecraft:set_count", "count": {"min": 2, "max": 6}}]},
                    {"type": "minecraft:item", "name": "minecraft:copper_ingot", "weight": 25, "functions": [{"function": "minecraft:set_count", "count": {"min": 3, "max": 8}}]},
                    {"type": "minecraft:item", "name": "minecraft:redstone", "weight": 20, "functions": [{"function": "minecraft:set_count", "count": {"min": 4, "max": 12}}]},
                    {"type": "minecraft:item", "name": "alyrioncore:olivine_gem", "weight": 15, "functions": [{"function": "minecraft:set_count", "count": {"min": 1, "max": 2}}]},
                    {"type": "minecraft:item", "name": "minecraft:compass", "weight": 8},
                    {"type": "minecraft:item", "name": "minecraft:lightning_rod", "weight": 10}
                ]
            }
        ]
    }
    write_json(os.path.join(BASE_DIR, "loot_table/chests/crashed_soviet_probe.json"), soviet_loot)
    write_json(os.path.join(BASE_DIR, "loot_tables/chests/crashed_soviet_probe.json"), soviet_loot)

    us_loot = {
        "type": "minecraft:chest",
        "pools": [
            {
                "rolls": {"min": 3, "max": 6, "type": "minecraft:uniform"},
                "entries": [
                    {"type": "minecraft:item", "name": "minecraft:spyglass", "weight": 10},
                    {"type": "minecraft:item", "name": "minecraft:gold_ingot", "weight": 20, "functions": [{"function": "minecraft:set_count", "count": {"min": 2, "max": 5}}]},
                    {"type": "minecraft:item", "name": "minecraft:copper_ingot", "weight": 25, "functions": [{"function": "minecraft:set_count", "count": {"min": 3, "max": 8}}]},
                    {"type": "minecraft:item", "name": "minecraft:redstone", "weight": 25, "functions": [{"function": "minecraft:set_count", "count": {"min": 4, "max": 14}}]},
                    {"type": "minecraft:item", "name": "alyrioncore:martian_rock_sample", "weight": 25, "functions": [{"function": "minecraft:set_count", "count": {"min": 2, "max": 4}}]},
                    {"type": "minecraft:item", "name": "alyrioncore:raw_meteoric_iron", "weight": 15, "functions": [{"function": "minecraft:set_count", "count": {"min": 1, "max": 3}}]},
                    {"type": "minecraft:item", "name": "minecraft:daylight_detector", "weight": 10},
                    {"type": "minecraft:item", "name": "minecraft:quartz", "weight": 20, "functions": [{"function": "minecraft:set_count", "count": {"min": 3, "max": 8}}]}
                ]
            }
        ]
    }
    write_json(os.path.join(BASE_DIR, "loot_table/chests/crashed_us_probe.json"), us_loot)
    write_json(os.path.join(BASE_DIR, "loot_tables/chests/crashed_us_probe.json"), us_loot)


def main():
    print("Generating Soviet Mars Probe NBT...")
    soviet = build_soviet_probe()
    # Save to data/alyrioncore/structure/ and structures/
    soviet.save("/Users/lea/alyrioncore/src/main/resources/data/alyrioncore/structure/crashed_soviet_probe.nbt")
    soviet.save("/Users/lea/alyrioncore/src/main/resources/data/alyrioncore/structures/crashed_soviet_probe.nbt")

    print("Generating US Mars Probe NBT...")
    us = build_us_probe()
    us.save("/Users/lea/alyrioncore/src/main/resources/data/alyrioncore/structure/crashed_us_probe.nbt")
    us.save("/Users/lea/alyrioncore/src/main/resources/data/alyrioncore/structures/crashed_us_probe.nbt")

    print("Generating Structure Worldgen JSONs, Pools, Sets, and Loot Tables...")
    generate_configs()

    print("All crashed probe assets generated successfully!")

if __name__ == "__main__":
    main()
