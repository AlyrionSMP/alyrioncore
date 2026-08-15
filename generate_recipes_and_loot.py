#!/usr/bin/env python3
"""
Generate complete Minecraft 1.21.1 JSONs and textures for AlyrionCore:
- Blockstates, Models, Item Models
- Loot Tables for all blocks
- Crafting, Smelting, Blasting, and Stonecutting Recipes
- Textures for new blocks & tools
- Localization entries
"""

import json
import os
from PIL import Image, ImageDraw

MOD_DIR = "/Users/lea/alyrioncore"
DATA_DIR = os.path.join(MOD_DIR, "src/main/resources/data/alyrioncore")
ASSETS_DIR = os.path.join(MOD_DIR, "src/main/resources/assets/alyrioncore")

def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)

def generate_block_json(name):
    # Blockstate
    bs = {
        "variants": {
            "": {"model": f"alyrioncore:block/{name}"}
        }
    }
    write_json(os.path.join(ASSETS_DIR, "blockstates", f"{name}.json"), bs)

    # Block Model
    bm = {
        "parent": "minecraft:block/cube_all",
        "textures": {
            "all": f"alyrioncore:block/{name}"
        }
    }
    write_json(os.path.join(ASSETS_DIR, "models/block", f"{name}.json"), bm)

    # Item Model
    im = {
        "parent": f"alyrioncore:block/{name}"
    }
    write_json(os.path.join(ASSETS_DIR, "models/item", f"{name}.json"), im)

def generate_tool_model(name):
    im = {
        "parent": "minecraft:item/handheld",
        "textures": {
            "layer0": f"alyrioncore:item/{name}"
        }
    }
    write_json(os.path.join(ASSETS_DIR, "models/item", f"{name}.json"), im)

def generate_simple_loot(block_name):
    loot = {
        "type": "minecraft:block",
        "pools": [
            {
                "bonus_rolls": 0.0,
                "conditions": [
                    {"condition": "minecraft:survives_explosion"}
                ],
                "entries": [
                    {
                        "type": "minecraft:item",
                        "name": f"alyrioncore:{block_name}"
                    }
                ],
                "rolls": 1.0
            }
        ]
    }
    # Write to both loot_table/blocks and loot_tables/blocks for 100% compatibility
    write_json(os.path.join(DATA_DIR, "loot_table/blocks", f"{block_name}.json"), loot)
    write_json(os.path.join(DATA_DIR, "loot_tables/blocks", f"{block_name}.json"), loot)

def generate_ore_loot(block_name, drop_item, min_count=1, max_count=1):
    count_function = []
    if min_count != 1 or max_count != 1:
        count_function.append({
            "function": "minecraft:set_count",
            "count": {"min": float(min_count), "max": float(max_count), "type": "minecraft:uniform"}
        })

    loot = {
        "type": "minecraft:block",
        "pools": [
            {
                "bonus_rolls": 0.0,
                "entries": [
                    {
                        "type": "minecraft:alternatives",
                        "children": [
                            {
                                "type": "minecraft:item",
                                "conditions": [
                                    {
                                        "condition": "minecraft:match_tool",
                                        "predicate": {
                                            "predicates": {
                                                "minecraft:enchantments": [
                                                    {
                                                        "enchantments": "minecraft:silk_touch",
                                                        "levels": {"min": 1}
                                                    }
                                                ]
                                            }
                                        }
                                    }
                                ],
                                "name": f"alyrioncore:{block_name}"
                            },
                            {
                                "type": "minecraft:item",
                                "functions": count_function + [
                                    {
                                        "enchantment": "minecraft:fortune",
                                        "formula": "minecraft:ore_drops",
                                        "function": "minecraft:apply_bonus"
                                    },
                                    {
                                        "function": "minecraft:explosion_decay"
                                    }
                                ],
                                "name": drop_item
                            }
                        ]
                    }
                ],
                "rolls": 1.0
            }
        ]
    }
    write_json(os.path.join(DATA_DIR, "loot_table/blocks", f"{block_name}.json"), loot)
    write_json(os.path.join(DATA_DIR, "loot_tables/blocks", f"{block_name}.json"), loot)

# ----------------- RECIPES -----------------
def generate_smelting_and_blasting(name, input_item, output_item, exp=0.7, smelt_time=200):
    # Smelting
    s = {
        "type": "minecraft:smelting",
        "category": "misc",
        "cookingtime": smelt_time,
        "experience": exp,
        "ingredient": {"item": input_item},
        "result": {"id": output_item}
    }
    write_json(os.path.join(DATA_DIR, "recipe", f"{name}_from_smelting.json"), s)

    # Blasting
    b = {
        "type": "minecraft:blasting",
        "category": "misc",
        "cookingtime": smelt_time // 2,
        "experience": exp,
        "ingredient": {"item": input_item},
        "result": {"id": output_item}
    }
    write_json(os.path.join(DATA_DIR, "recipe", f"{name}_from_blasting.json"), b)

def generate_shaped(name, pattern, key, result_id, count=1, category="misc"):
    r = {
        "type": "minecraft:crafting_shaped",
        "category": category,
        "key": {k: {"item": v} for k, v in key.items()},
        "pattern": pattern,
        "result": {
            "count": count,
            "id": result_id
        }
    }
    write_json(os.path.join(DATA_DIR, "recipe", f"{name}.json"), r)

def generate_shapeless(name, ingredients, result_id, count=1, category="misc"):
    r = {
        "type": "minecraft:crafting_shapeless",
        "category": category,
        "ingredients": [{"item": item} for item in ingredients],
        "result": {
            "count": count,
            "id": result_id
        }
    }
    write_json(os.path.join(DATA_DIR, "recipe", f"{name}.json"), r)

def generate_stonecutting(name, input_item, result_id, count=1):
    r = {
        "type": "minecraft:stonecutting",
        "count": count,
        "ingredient": {"item": input_item},
        "result": {"id": result_id}
    }
    write_json(os.path.join(DATA_DIR, "recipe", f"{name}_from_stonecutting.json"), r)

def main():
    print("Generating block models and blockstates...")
    new_blocks = ["meteoric_iron_block", "raw_meteoric_iron_block", "olivine_block", "sulfur_block"]
    for b in new_blocks:
        generate_block_json(b)

    print("Generating tool models...")
    tools = ["meteoric_iron_sword", "meteoric_iron_pickaxe", "meteoric_iron_axe", "meteoric_iron_shovel", "meteoric_iron_hoe"]
    for t in tools:
        generate_tool_model(t)

    print("Generating loot tables...")
    all_simple_blocks = [
        "martian_sand", "martian_regolith", "coarse_martian_regolith", "frost_dusted_regolith", "martian_permafrost",
        "martian_basalt", "polished_martian_basalt", "martian_basalt_bricks", "martian_basalt_tiles",
        "stratified_martian_stone", "martian_volcanic_scoria", "martian_impact_breccia",
        "meteoric_iron_block", "raw_meteoric_iron_block", "olivine_block", "sulfur_block"
    ]
    for b in all_simple_blocks:
        generate_simple_loot(b)

    # Ore and special block drops
    generate_ore_loot("hematite_ore", "alyrioncore:hematite_nodule", 1, 3)
    generate_ore_loot("meteoric_iron_ore", "alyrioncore:raw_meteoric_iron", 1, 2)
    generate_ore_loot("martian_copper_ore", "alyrioncore:raw_martian_copper", 1, 3)
    generate_ore_loot("martian_sulfur_ore", "alyrioncore:sulfur_dust", 2, 5)
    generate_ore_loot("martian_olivine_ore", "alyrioncore:olivine_gem", 1, 2)
    generate_ore_loot("dry_ice_block", "alyrioncore:dry_ice_shard", 3, 4)
    generate_ore_loot("martian_ice", "minecraft:packed_ice", 1, 1)

    print("Generating Smelting & Blasting recipes...")
    generate_smelting_and_blasting("meteoric_iron_ingot_from_raw", "alyrioncore:raw_meteoric_iron", "alyrioncore:meteoric_iron_ingot", 1.0)
    generate_smelting_and_blasting("meteoric_iron_ingot_from_ore", "alyrioncore:meteoric_iron_ore", "alyrioncore:meteoric_iron_ingot", 1.0)
    generate_smelting_and_blasting("copper_ingot_from_martian_raw", "alyrioncore:raw_martian_copper", "minecraft:copper_ingot", 0.7)
    generate_smelting_and_blasting("copper_ingot_from_martian_ore", "alyrioncore:martian_copper_ore", "minecraft:copper_ingot", 0.7)
    generate_smelting_and_blasting("iron_nugget_from_hematite_nodule", "alyrioncore:hematite_nodule", "minecraft:iron_nugget", 0.2, 100)
    generate_smelting_and_blasting("iron_ingot_from_hematite_ore", "alyrioncore:hematite_ore", "minecraft:iron_ingot", 0.7)
    generate_smelting_and_blasting("polished_martian_basalt", "alyrioncore:martian_basalt", "alyrioncore:polished_martian_basalt", 0.1)

    # General Smelting
    write_json(os.path.join(DATA_DIR, "recipe/glass_from_martian_sand.json"), {
        "type": "minecraft:smelting", "cookingtime": 200, "experience": 0.1,
        "ingredient": {"item": "alyrioncore:martian_sand"}, "result": {"id": "minecraft:glass"}
    })
    write_json(os.path.join(DATA_DIR, "recipe/terracotta_from_martian_regolith.json"), {
        "type": "minecraft:smelting", "cookingtime": 200, "experience": 0.1,
        "ingredient": {"item": "alyrioncore:martian_regolith"}, "result": {"id": "minecraft:terracotta"}
    })
    write_json(os.path.join(DATA_DIR, "recipe/smooth_stone_from_rock_sample.json"), {
        "type": "minecraft:smelting", "cookingtime": 150, "experience": 0.2,
        "ingredient": {"item": "alyrioncore:martian_rock_sample"}, "result": {"id": "minecraft:smooth_stone"}
    })

    print("Generating Storage & Compression recipes...")
    # 9 ingots -> 1 block & reverse
    generate_shaped("meteoric_iron_block", ["###", "###", "###"], {"#": "alyrioncore:meteoric_iron_ingot"}, "alyrioncore:meteoric_iron_block", 1, "building")
    generate_shapeless("meteoric_iron_ingot_from_block", ["alyrioncore:meteoric_iron_block"], "alyrioncore:meteoric_iron_ingot", 9, "misc")

    # Raw meteoric iron block & reverse
    generate_shaped("raw_meteoric_iron_block", ["###", "###", "###"], {"#": "alyrioncore:raw_meteoric_iron"}, "alyrioncore:raw_meteoric_iron_block", 1, "building")
    generate_shapeless("raw_meteoric_iron_from_block", ["alyrioncore:raw_meteoric_iron_block"], "alyrioncore:raw_meteoric_iron", 9, "misc")

    # Olivine block & reverse
    generate_shaped("olivine_block", ["###", "###", "###"], {"#": "alyrioncore:olivine_gem"}, "alyrioncore:olivine_block", 1, "building")
    generate_shapeless("olivine_gem_from_block", ["alyrioncore:olivine_block"], "alyrioncore:olivine_gem", 9, "misc")

    # Sulfur block & reverse
    generate_shaped("sulfur_block", ["###", "###", "###"], {"#": "alyrioncore:sulfur_dust"}, "alyrioncore:sulfur_block", 1, "building")
    generate_shapeless("sulfur_dust_from_block", ["alyrioncore:sulfur_block"], "alyrioncore:sulfur_dust", 9, "misc")

    # Dry Ice Block & reverse (2x2 grid)
    generate_shaped("dry_ice_block_from_shards", ["##", "##"], {"#": "alyrioncore:dry_ice_shard"}, "alyrioncore:dry_ice_block", 1, "building")
    generate_shapeless("dry_ice_shards_from_block", ["alyrioncore:dry_ice_block"], "alyrioncore:dry_ice_shard", 4, "misc")

    # Raw Martian Copper -> Vanilla Raw Copper Block
    generate_shaped("raw_copper_block_from_martian_copper", ["###", "###", "###"], {"#": "alyrioncore:raw_martian_copper"}, "minecraft:raw_copper_block", 1, "building")

    print("Generating Meteoric Iron Tools recipes...")
    generate_shaped("meteoric_iron_sword", ["#", "#", "/"], {"#": "alyrioncore:meteoric_iron_ingot", "/": "minecraft:stick"}, "alyrioncore:meteoric_iron_sword", 1, "equipment")
    generate_shaped("meteoric_iron_pickaxe", ["###", " / ", " / "], {"#": "alyrioncore:meteoric_iron_ingot", "/": "minecraft:stick"}, "alyrioncore:meteoric_iron_pickaxe", 1, "equipment")
    generate_shaped("meteoric_iron_axe", ["## ", "#/ ", " / "], {"#": "alyrioncore:meteoric_iron_ingot", "/": "minecraft:stick"}, "alyrioncore:meteoric_iron_axe", 1, "equipment")
    generate_shaped("meteoric_iron_shovel", ["#", "/", "/"], {"#": "alyrioncore:meteoric_iron_ingot", "/": "minecraft:stick"}, "alyrioncore:meteoric_iron_shovel", 1, "equipment")
    generate_shaped("meteoric_iron_hoe", ["## ", " / ", " / "], {"#": "alyrioncore:meteoric_iron_ingot", "/": "minecraft:stick"}, "alyrioncore:meteoric_iron_hoe", 1, "equipment")

    print("Generating Functional & Chemistry Resource Recipes...")
    # 1. Sulfur -> Gunpowder (Sulfur + Coal + Sugar/Bonemeal)
    generate_shapeless("gunpowder_from_sulfur_and_coal", ["alyrioncore:sulfur_dust", "minecraft:coal", "minecraft:sugar"], "minecraft:gunpowder", 2, "misc")
    generate_shapeless("gunpowder_from_sulfur_and_charcoal", ["alyrioncore:sulfur_dust", "minecraft:charcoal", "minecraft:sugar"], "minecraft:gunpowder", 2, "misc")
    generate_shapeless("gunpowder_from_sulfur_and_bonemeal", ["alyrioncore:sulfur_dust", "minecraft:coal", "minecraft:bone_meal"], "minecraft:gunpowder", 2, "misc")
    
    # 2. Sulfur -> Torches
    generate_shaped("torches_from_sulfur", ["#", "/"], {"#": "alyrioncore:sulfur_dust", "/": "minecraft:stick"}, "minecraft:torch", 4, "misc")

    # 3. Hematite -> Red Dye
    generate_shapeless("red_dye_from_hematite", ["alyrioncore:hematite_nodule"], "minecraft:red_dye", 2, "misc")

    # 4. Hematite -> Iron Ingot Reduction (4 nodules + 1 coal -> 1 iron ingot)
    generate_shaped("iron_ingot_from_hematite_reduction", ["##", "##"], {"#": "alyrioncore:hematite_nodule"}, "minecraft:raw_iron", 1, "misc")

    # 5. Olivine -> Spyglass (Optics)
    generate_shaped("spyglass_from_olivine", [" # ", " C ", " C "], {"#": "alyrioncore:olivine_gem", "C": "minecraft:copper_ingot"}, "minecraft:spyglass", 1, "equipment")

    # 6. Dry Ice Shard -> Packed Ice (Cryo Flash Freeze with water bucket)
    generate_shapeless("packed_ice_from_dry_ice_and_water", ["alyrioncore:dry_ice_shard", "alyrioncore:dry_ice_shard", "alyrioncore:dry_ice_shard", "alyrioncore:dry_ice_shard", "minecraft:water_bucket"], "minecraft:packed_ice", 2, "building")
    generate_shapeless("snow_block_from_dry_ice", ["alyrioncore:dry_ice_shard", "minecraft:snowball"], "minecraft:snow_block", 4, "building")

    # 7. Martian Ice -> Packed Ice / Water
    generate_shaped("packed_ice_from_martian_ice", ["##", "##"], {"#": "alyrioncore:martian_ice"}, "minecraft:packed_ice", 1, "building")
    generate_shapeless("water_bucket_from_martian_ice", ["alyrioncore:martian_ice", "minecraft:bucket"], "minecraft:water_bucket", 1, "misc")

    # 8. Soil crafting
    generate_shaped("coarse_martian_regolith_crafting", ["SR", "RS"], {"S": "alyrioncore:martian_sand", "R": "alyrioncore:martian_regolith"}, "alyrioncore:coarse_martian_regolith", 4, "building")
    generate_shaped("martian_regolith_from_sand", ["##", "##"], {"#": "alyrioncore:martian_sand"}, "alyrioncore:martian_regolith", 1, "building")

    # 9. Basalt Line
    generate_shaped("polished_martian_basalt_crafting", ["##", "##"], {"#": "alyrioncore:martian_basalt"}, "alyrioncore:polished_martian_basalt", 4, "building")
    generate_shaped("martian_basalt_bricks_crafting", ["##", "##"], {"#": "alyrioncore:polished_martian_basalt"}, "alyrioncore:martian_basalt_bricks", 4, "building")
    generate_shaped("martian_basalt_tiles_crafting", ["##", "##"], {"#": "alyrioncore:martian_basalt_bricks"}, "alyrioncore:martian_basalt_tiles", 4, "building")

    # 10. Stonecutting for Basalts
    generate_stonecutting("polished_basalt_from_basalt", "alyrioncore:martian_basalt", "alyrioncore:polished_martian_basalt")
    generate_stonecutting("basalt_bricks_from_basalt", "alyrioncore:martian_basalt", "alyrioncore:martian_basalt_bricks")
    generate_stonecutting("basalt_tiles_from_basalt", "alyrioncore:martian_basalt", "alyrioncore:martian_basalt_tiles")
    generate_stonecutting("basalt_bricks_from_polished", "alyrioncore:polished_martian_basalt", "alyrioncore:martian_basalt_bricks")
    generate_stonecutting("basalt_tiles_from_polished", "alyrioncore:polished_martian_basalt", "alyrioncore:martian_basalt_tiles")
    generate_stonecutting("basalt_tiles_from_bricks", "alyrioncore:martian_basalt_bricks", "alyrioncore:martian_basalt_tiles")

    print("Successfully generated all recipes, loot tables, blockstates, and models!")

if __name__ == "__main__":
    main()
