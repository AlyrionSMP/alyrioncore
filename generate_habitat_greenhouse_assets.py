#!/usr/bin/env python3
"""
Generate textures, models, blockstates, recipes, loot tables, tags, and localizations
for the Pressurized Habitats, Airlocks, and Martian Greenhouse System.
"""

import json
import os
import random
from PIL import Image, ImageDraw

MOD_DIR = "/Users/lea/alyrioncore"
ASSETS_DIR = os.path.join(MOD_DIR, "src/main/resources/assets/alyrioncore")
DATA_DIR = os.path.join(MOD_DIR, "src/main/resources/data/alyrioncore")
MC_DATA_DIR = os.path.join(MOD_DIR, "src/main/resources/data/minecraft")

def ensure_dir(path):
    os.makedirs(path, exist_ok=True)

def write_json(path, data):
    ensure_dir(os.path.dirname(path))
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)
    print(f"Wrote: {path}")

# --- 1. TEXTURES ---

def create_airlock_textures():
    # 32x32 high-tech space habitat airlock door textures
    ensure_dir(os.path.join(ASSETS_DIR, "textures/block"))
    ensure_dir(os.path.join(ASSETS_DIR, "textures/item"))

    # Bottom Half
    img_bot = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw_b = ImageDraw.Draw(img_bot)
    
    # Outer frame: Dark titanium steel
    draw_b.rectangle([0, 0, 31, 31], fill=(42, 45, 52, 255))
    draw_b.rectangle([2, 0, 29, 30], fill=(68, 72, 82, 255))
    # Reinforced inset plating
    draw_b.rectangle([5, 3, 26, 28], fill=(95, 100, 112, 255))
    draw_b.rectangle([6, 4, 25, 27], fill=(80, 85, 96, 255))
    # Bevels and bolts
    for y in [4, 15, 27]:
        draw_b.point((4, y), fill=(180, 185, 195, 255))
        draw_b.point((27, y), fill=(180, 185, 195, 255))
    # Pneumatic locking bar across center
    draw_b.rectangle([6, 13, 25, 17], fill=(130, 135, 145, 255))
    draw_b.rectangle([8, 14, 23, 16], fill=(50, 52, 60, 255))
    # Rubber pressure seal gasket border
    draw_b.rectangle([3, 0, 4, 30], fill=(25, 25, 28, 255))
    draw_b.rectangle([27, 0, 28, 30], fill=(25, 25, 28, 255))

    img_bot.save(os.path.join(ASSETS_DIR, "textures/block/airlock_bottom.png"))

    # Top Half
    img_top = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw_t = ImageDraw.Draw(img_top)
    
    # Outer frame: Dark titanium steel
    draw_t.rectangle([0, 0, 31, 31], fill=(42, 45, 52, 255))
    draw_t.rectangle([2, 1, 29, 31], fill=(68, 72, 82, 255))
    # Reinforced inset plating
    draw_t.rectangle([5, 3, 26, 28], fill=(95, 100, 112, 255))
    draw_t.rectangle([6, 4, 25, 27], fill=(80, 85, 96, 255))
    # Reinforced airtight viewport glass window
    draw_t.rectangle([9, 6, 22, 17], fill=(30, 35, 45, 255))
    draw_t.rectangle([10, 7, 21, 16], fill=(40, 160, 200, 240))
    # Glass glare highlight
    draw_t.line([(11, 8), (17, 8)], fill=(180, 240, 255, 255))
    draw_t.line([(11, 9), (13, 9)], fill=(180, 240, 255, 255))
    # Pressure status indicator HUD LED (Green for SEALED)
    draw_t.rectangle([11, 21, 20, 24], fill=(20, 25, 30, 255))
    draw_t.rectangle([12, 22, 15, 23], fill=(40, 220, 90, 255)) # Green = SEALED
    draw_t.rectangle([16, 22, 19, 23], fill=(80, 30, 30, 255)) # Red = Unsealed / Venting

    img_top.save(os.path.join(ASSETS_DIR, "textures/block/airlock_top.png"))

    # Item texture
    img_item = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw_i = ImageDraw.Draw(img_item)
    draw_i.rectangle([7, 2, 24, 29], fill=(42, 45, 52, 255))
    draw_i.rectangle([9, 4, 22, 27], fill=(75, 80, 90, 255))
    # Viewport
    draw_i.rectangle([11, 6, 20, 13], fill=(40, 170, 210, 255))
    draw_i.line([(12, 7), (16, 7)], fill=(200, 245, 255, 255))
    # Status light
    draw_i.rectangle([13, 16, 18, 18], fill=(40, 220, 90, 255))
    # Handle / latch
    draw_i.rectangle([10, 20, 21, 23], fill=(120, 125, 135, 255))
    img_item.save(os.path.join(ASSETS_DIR, "textures/item/airlock.png"))

def create_farmland_textures():
    # 32x32 Regolith Farmland
    # Dry Farmland
    img_dry = Image.new("RGBA", (32, 32), (0, 0, 0, 255))
    draw_d = ImageDraw.Draw(img_dry)
    random.seed(301)
    base_r, base_g, base_b = 135, 68, 38
    for y in range(32):
        for x in range(32):
            # Furrow ridge lines every 4 pixels
            is_ridge = (y % 4 == 0 or y % 4 == 1)
            offset = 12 if is_ridge else -14
            noise = random.randint(-8, 8)
            r = max(40, min(220, base_r + offset + noise))
            g = max(20, min(150, base_g + offset + noise))
            b = max(10, min(100, base_b + offset + noise))
            img_dry.putpixel((x, y), (r, g, b, 255))
    img_dry.save(os.path.join(ASSETS_DIR, "textures/block/regolith_farmland_dry.png"))

    # Moist Farmland (Deep rich terracotta / dark moisture sheen)
    img_moist = Image.new("RGBA", (32, 32), (0, 0, 0, 255))
    random.seed(302)
    m_r, m_g, m_b = 82, 38, 22
    for y in range(32):
        for x in range(32):
            is_ridge = (y % 4 == 0 or y % 4 == 1)
            offset = 10 if is_ridge else -12
            noise = random.randint(-6, 6)
            r = max(25, min(160, m_r + offset + noise))
            g = max(15, min(90, m_g + offset + noise))
            b = max(8, min(60, m_b + offset + noise))
            # Subtle moisture glint
            if random.random() < 0.04:
                r, g, b = r + 35, g + 35, b + 45
            img_moist.putpixel((x, y), (r, g, b, 255))
    img_moist.save(os.path.join(ASSETS_DIR, "textures/block/regolith_farmland_moist.png"))

    # Side texture
    img_side = Image.new("RGBA", (32, 32), (0, 0, 0, 255))
    random.seed(303)
    for y in range(32):
        for x in range(32):
            if y < 4:
                # Top tilled layer
                r = max(30, min(180, 110 + random.randint(-10, 10)))
                g = max(15, min(120, 55 + random.randint(-8, 8)))
                b = max(10, min(80, 30 + random.randint(-6, 6)))
            else:
                # Regolith subsoil
                r = max(40, min(200, 140 + random.randint(-12, 12)))
                g = max(20, min(130, 65 + random.randint(-10, 10)))
                b = max(10, min(90, 35 + random.randint(-8, 8)))
            img_side.putpixel((x, y), (r, g, b, 255))
    img_side.save(os.path.join(ASSETS_DIR, "textures/block/regolith_farmland_side.png"))

def create_crop_and_food_textures():
    # Martian Potato Crop Stages (0 to 7) - 32x32 cross crop textures
    # Deep alien-adapted dark emerald & crimson-veined potato leaves
    stem_color = (65, 125, 45, 255)
    leaf_dark = (38, 90, 32, 255)
    leaf_light = (85, 165, 55, 255)
    flower_white = (240, 240, 245, 255)
    flower_yellow = (245, 210, 45, 255)
    potato_tuber = (195, 120, 65, 255)

    for stage in range(8):
        img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
        draw = ImageDraw.Draw(img)
        
        # Growth height and spread increases per stage
        max_h = 4 + stage * 3.5
        spread = 2 + stage * 1.6
        cx = 16

        # Central stems
        for y in range(int(32 - max_h), 32):
            draw.line([(cx - 1, y), (cx + 1, y)], fill=stem_color)

        # Foliage clusters
        if stage >= 1:
            draw.ellipse([cx - spread, 32 - max_h, cx + spread, 32 - max_h + spread * 1.4], fill=leaf_dark)
            draw.ellipse([cx - spread * 0.7, 32 - max_h + 2, cx + spread * 0.7, 32 - max_h + spread * 1.2], fill=leaf_light)

        if stage >= 3:
            # Side foliage branches
            draw.ellipse([cx - spread * 1.2, 32 - max_h + 6, cx - 1, 32 - max_h + 14], fill=leaf_dark)
            draw.ellipse([cx + 1, 32 - max_h + 6, cx + spread * 1.2, 32 - max_h + 14], fill=leaf_dark)
            draw.ellipse([cx - spread * 1.0, 32 - max_h + 7, cx - 2, 32 - max_h + 12], fill=leaf_light)
            draw.ellipse([cx + 2, 32 - max_h + 7, cx + spread * 1.0, 32 - max_h + 12], fill=leaf_light)

        if stage >= 5:
            # Lower thick bush layer
            draw.ellipse([cx - spread * 1.4, 20, cx + spread * 1.4, 30], fill=leaf_dark)
            draw.ellipse([cx - spread * 1.1, 21, cx + spread * 1.1, 28], fill=leaf_light)
            # Small white flowers with golden centers
            draw.rectangle([cx - 4, int(32 - max_h - 1), cx - 2, int(32 - max_h + 1)], fill=flower_white)
            draw.point((cx - 3, int(32 - max_h)), fill=flower_yellow)
            draw.rectangle([cx + 2, int(32 - max_h), cx + 4, int(32 - max_h + 2)], fill=flower_white)
            draw.point((cx + 3, int(32 - max_h + 1)), fill=flower_yellow)

        if stage == 7:
            # Mature root mounds with visible golden Martian potato tubers near base
            draw.ellipse([cx - 9, 27, cx - 3, 31], fill=potato_tuber)
            draw.point((cx - 6, 29), fill=(140, 80, 40, 255))
            draw.ellipse([cx + 3, 26, cx + 9, 31], fill=potato_tuber)
            draw.point((cx + 6, 28), fill=(140, 80, 40, 255))

        img.save(os.path.join(ASSETS_DIR, f"textures/block/martian_potato_stage{stage}.png"))

    # Raw Martian Potato Item (32x32)
    img_raw = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw_raw = ImageDraw.Draw(img_raw)
    # Organic oblong potato shape with warm Martian terracotta skin
    draw_raw.ellipse([6, 8, 26, 24], fill=(175, 105, 55, 255))
    draw_raw.ellipse([8, 9, 24, 22], fill=(205, 130, 75, 255))
    draw_raw.ellipse([10, 10, 20, 18], fill=(225, 155, 95, 255))
    # Potato eyes / dimples
    for ex, ey in [(11, 13), (17, 12), (21, 16), (14, 19), (22, 19)]:
        draw_raw.point((ex, ey), fill=(110, 60, 30, 255))
        draw_raw.point((ex + 1, ey), fill=(140, 80, 45, 255))
    img_raw.save(os.path.join(ASSETS_DIR, "textures/item/martian_potato.png"))

    # Baked Martian Potato Item (32x32)
    img_baked = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw_baked = ImageDraw.Draw(img_baked)
    # Roasted golden potato with crispy split skin and steaming fluffy interior
    draw_baked.ellipse([5, 8, 27, 24], fill=(130, 70, 32, 255))
    draw_baked.ellipse([7, 9, 25, 22], fill=(185, 115, 55, 255))
    # Steaming center split
    draw_baked.ellipse([10, 12, 22, 19], fill=(245, 220, 130, 255))
    draw_baked.ellipse([12, 13, 20, 18], fill=(255, 245, 180, 255))
    # Steam wisps
    draw_baked.line([(14, 7), (13, 5), (15, 3)], fill=(220, 230, 240, 180))
    draw_baked.line([(18, 6), (19, 4), (17, 2)], fill=(220, 230, 240, 180))
    img_baked.save(os.path.join(ASSETS_DIR, "textures/item/baked_martian_potato.png"))

# --- 2. MODELS & BLOCKSTATES ---

def make_airlock_models_and_blockstates():
    models_block = os.path.join(ASSETS_DIR, "models/block")
    models_item = os.path.join(ASSETS_DIR, "models/item")
    blockstates = os.path.join(ASSETS_DIR, "blockstates")
    ensure_dir(models_block)
    ensure_dir(models_item)
    ensure_dir(blockstates)

    # Airlock uses a static bulkhead frame model (the animated hatch leaf is a
    # BlockEntityRenderer), so the blockstate only needs facing + half variants.
    frame_bottom = {
        "ambientocclusion": False,
        "textures": {
            "frame": "alyrioncore:block/airlock_frame",
            "particle": "#frame"
        },
        "elements": [
            {
                "name": "left_jamb",
                "from": [0, 0, 0], "to": [16, 16, 2],
                "faces": {
                    "north": {"uv": [0, 0, 16, 16], "texture": "#frame"},
                    "south": {"uv": [0, 0, 16, 16], "texture": "#frame"},
                    "west": {"uv": [0, 0, 2, 16], "texture": "#frame"},
                    "east": {"uv": [14, 0, 16, 16], "texture": "#frame"},
                    "up": {"uv": [0, 0, 16, 2], "texture": "#frame"},
                    "down": {"uv": [0, 0, 16, 2], "texture": "#frame"}
                }
            },
            {
                "name": "right_jamb",
                "from": [0, 0, 14], "to": [16, 16, 16],
                "faces": {
                    "north": {"uv": [0, 0, 16, 16], "texture": "#frame"},
                    "south": {"uv": [0, 0, 16, 16], "texture": "#frame"},
                    "west": {"uv": [0, 0, 2, 16], "texture": "#frame"},
                    "east": {"uv": [14, 0, 16, 16], "texture": "#frame"},
                    "up": {"uv": [0, 0, 16, 2], "texture": "#frame"},
                    "down": {"uv": [0, 0, 16, 2], "texture": "#frame"}
                }
            },
            {
                "name": "sill",
                "from": [0, 0, 2], "to": [16, 2, 14],
                "faces": {
                    "north": {"uv": [0, 0, 16, 2], "texture": "#frame"},
                    "south": {"uv": [0, 0, 16, 2], "texture": "#frame"},
                    "west": {"uv": [0, 0, 12, 2], "texture": "#frame"},
                    "east": {"uv": [0, 0, 12, 2], "texture": "#frame"},
                    "up": {"uv": [0, 0, 16, 12], "texture": "#frame"},
                    "down": {"uv": [0, 0, 16, 12], "texture": "#frame"}
                }
            }
        ]
    }
    frame_top = json.loads(json.dumps(frame_bottom))
    frame_top["elements"] = frame_bottom["elements"][:2] + [{
        "name": "header",
        "from": [0, 14, 2], "to": [16, 16, 14],
        "faces": {
            "north": {"uv": [0, 0, 16, 2], "texture": "#frame"},
            "south": {"uv": [0, 0, 16, 2], "texture": "#frame"},
            "west": {"uv": [0, 0, 12, 2], "texture": "#frame"},
            "east": {"uv": [0, 0, 12, 2], "texture": "#frame"},
            "up": {"uv": [0, 0, 16, 12], "texture": "#frame"},
            "down": {"uv": [0, 0, 16, 12], "texture": "#frame"}
        }
    }]
    write_json(os.path.join(models_block, "airlock_frame_bottom.json"), frame_bottom)
    write_json(os.path.join(models_block, "airlock_frame_top.json"), frame_top)

    # Item model
    write_json(os.path.join(models_item, "airlock.json"), {
        "parent": "minecraft:item/generated",
        "textures": {
            "layer0": "alyrioncore:item/airlock"
        }
    })

    # Blockstate: static frame, facing + half only (hinge/open handled by renderer)
    variants = {}
    for facing, y_rot in [("east", 0), ("south", 90), ("west", 180), ("north", 270)]:
        variants[f"facing={facing},half=lower"] = {
            "model": "alyrioncore:block/airlock_frame_bottom", "y": y_rot
        }
        variants[f"facing={facing},half=upper"] = {
            "model": "alyrioncore:block/airlock_frame_top", "y": y_rot
        }
    write_json(os.path.join(blockstates, "airlock.json"), {"variants": variants})

def make_farmland_models_and_blockstates():
    models_block = os.path.join(ASSETS_DIR, "models/block")
    models_item = os.path.join(ASSETS_DIR, "models/item")
    blockstates = os.path.join(ASSETS_DIR, "blockstates")

    write_json(os.path.join(models_block, "regolith_farmland.json"), {
        "parent": "minecraft:block/template_farmland",
        "textures": {
            "dirt": "alyrioncore:block/regolith_farmland_side",
            "top": "alyrioncore:block/regolith_farmland_dry",
            "particle": "alyrioncore:block/regolith_farmland_dry"
        }
    })

    write_json(os.path.join(models_block, "regolith_farmland_moist.json"), {
        "parent": "minecraft:block/template_farmland",
        "textures": {
            "dirt": "alyrioncore:block/regolith_farmland_side",
            "top": "alyrioncore:block/regolith_farmland_moist",
            "particle": "alyrioncore:block/regolith_farmland_moist"
        }
    })

    write_json(os.path.join(models_item, "regolith_farmland.json"), {
        "parent": "alyrioncore:block/regolith_farmland"
    })

    # Blockstate moisture 0-6 = dry, 7 = moist
    variants = {}
    for m in range(7):
        variants[f"moisture={m}"] = {"model": "alyrioncore:block/regolith_farmland"}
    variants["moisture=7"] = {"model": "alyrioncore:block/regolith_farmland_moist"}
    write_json(os.path.join(blockstates, "regolith_farmland.json"), {"variants": variants})

def make_crop_and_food_models():
    models_block = os.path.join(ASSETS_DIR, "models/block")
    models_item = os.path.join(ASSETS_DIR, "models/item")
    blockstates = os.path.join(ASSETS_DIR, "blockstates")

    # Crop stage models
    for stage in range(8):
        write_json(os.path.join(models_block, f"martian_potato_stage{stage}.json"), {
            "parent": "minecraft:block/crop",
            "textures": {
                "crop": f"alyrioncore:block/martian_potato_stage{stage}"
            }
        })

    # Crop blockstate
    variants = {}
    for age in range(8):
        variants[f"age={age}"] = {"model": f"alyrioncore:block/martian_potato_stage{age}"}
    write_json(os.path.join(blockstates, "martian_potato_crop.json"), {"variants": variants})

    # Food item models
    write_json(os.path.join(models_item, "martian_potato.json"), {
        "parent": "minecraft:item/generated",
        "textures": {
            "layer0": "alyrioncore:item/martian_potato"
        }
    })

    write_json(os.path.join(models_item, "baked_martian_potato.json"), {
        "parent": "minecraft:item/generated",
        "textures": {
            "layer0": "alyrioncore:item/baked_martian_potato"
        }
    })

# --- 3. RECIPES ---

def make_recipes():
    recipe_dir = os.path.join(DATA_DIR, "recipe")
    ensure_dir(recipe_dir)

    # Airlock Shaped Recipe
    write_json(os.path.join(recipe_dir, "airlock.json"), {
        "type": "minecraft:crafting_shaped",
        "pattern": [
            "MIM",
            "MGM",
            "MRM"
        ],
        "key": {
            "M": {"item": "alyrioncore:meteoric_iron_ingot"},
            "I": {"item": "minecraft:iron_ingot"},
            "G": {"item": "minecraft:glass_pane"},
            "R": {"item": "minecraft:redstone"}
        },
        "result": {
            "count": 1,
            "id": "alyrioncore:airlock"
        }
    })

    # Smelting Baked Martian Potato
    write_json(os.path.join(recipe_dir, "baked_martian_potato_smelting.json"), {
        "type": "minecraft:smelting",
        "ingredient": {"item": "alyrioncore:martian_potato"},
        "result": {"id": "alyrioncore:baked_martian_potato"},
        "experience": 0.35,
        "cookingtime": 200
    })

    # Smoking Baked Martian Potato
    write_json(os.path.join(recipe_dir, "baked_martian_potato_smoking.json"), {
        "type": "minecraft:smoking",
        "ingredient": {"item": "alyrioncore:martian_potato"},
        "result": {"id": "alyrioncore:baked_martian_potato"},
        "experience": 0.35,
        "cookingtime": 100
    })

    # Campfire Cooking
    write_json(os.path.join(recipe_dir, "baked_martian_potato_campfire.json"), {
        "type": "minecraft:campfire_cooking",
        "ingredient": {"item": "alyrioncore:martian_potato"},
        "result": {"id": "alyrioncore:baked_martian_potato"},
        "experience": 0.35,
        "cookingtime": 600
    })

# --- 4. LOOT TABLES ---

def make_loot_tables():
    loot_dir = os.path.join(DATA_DIR, "loot_table/blocks")
    loot_tables_dir = os.path.join(DATA_DIR, "loot_tables/blocks")
    ensure_dir(loot_dir)
    ensure_dir(loot_tables_dir)

    # Airlock (only drops when lower half is broken)
    airlock_loot = {
        "type": "minecraft:block",
        "pools": [
            {
                "rolls": 1,
                "bonus_rolls": 0,
                "entries": [
                    {
                        "type": "minecraft:item",
                        "name": "alyrioncore:airlock",
                        "conditions": [
                            {
                                "condition": "minecraft:block_state_property",
                                "block": "alyrioncore:airlock",
                                "properties": {
                                    "half": "lower"
                                }
                            }
                        ]
                    }
                ],
                "conditions": [
                    {
                        "condition": "minecraft:survives_explosion"
                    }
                ]
            }
        ]
    }
    write_json(os.path.join(loot_dir, "airlock.json"), airlock_loot)
    write_json(os.path.join(loot_tables_dir, "airlock.json"), airlock_loot)

    # Regolith Farmland (drops martian regolith)
    farmland_loot = {
        "type": "minecraft:block",
        "pools": [
            {
                "rolls": 1,
                "bonus_rolls": 0,
                "entries": [
                    {
                        "type": "minecraft:item",
                        "name": "alyrioncore:martian_regolith"
                    }
                ],
                "conditions": [
                    {
                        "condition": "minecraft:survives_explosion"
                    }
                ]
            }
        ]
    }
    write_json(os.path.join(loot_dir, "regolith_farmland.json"), farmland_loot)
    write_json(os.path.join(loot_tables_dir, "regolith_farmland.json"), farmland_loot)

    # Martian Potato Crop
    crop_loot = {
        "type": "minecraft:block",
        "pools": [
            {
                "rolls": 1,
                "bonus_rolls": 0,
                "entries": [
                    {
                        "type": "minecraft:item",
                        "name": "alyrioncore:martian_potato"
                    }
                ]
            },
            {
                "rolls": 1,
                "bonus_rolls": 0,
                "entries": [
                    {
                        "type": "minecraft:item",
                        "name": "alyrioncore:martian_potato",
                        "functions": [
                            {
                                "function": "minecraft:apply_bonus",
                                "enchantment": "minecraft:fortune",
                                "formula": "minecraft:binomial_with_bonus_count",
                                "parameters": {
                                    "extra": 3,
                                    "probability": 0.5714286
                                }
                            }
                        ]
                    }
                ],
                "conditions": [
                    {
                        "condition": "minecraft:block_state_property",
                        "block": "alyrioncore:martian_potato_crop",
                        "properties": {
                            "age": "7"
                        }
                    }
                ]
            }
        ]
    }
    write_json(os.path.join(loot_dir, "martian_potato_crop.json"), crop_loot)
    write_json(os.path.join(loot_tables_dir, "martian_potato_crop.json"), crop_loot)

# --- 5. TAGS ---

def make_tags():
    # Pickaxe mineable
    pick_path = os.path.join(MC_DATA_DIR, "tags/block/mineable/pickaxe.json")
    if os.path.exists(pick_path):
        with open(pick_path, "r", encoding="utf-8") as f:
            data = json.load(f)
        vals = data.get("values", [])
        if "alyrioncore:airlock" not in vals:
            vals.append("alyrioncore:airlock")
        data["values"] = vals
        write_json(pick_path, data)

    # Shovel mineable
    shov_dir = os.path.join(MC_DATA_DIR, "tags/block/mineable")
    ensure_dir(shov_dir)
    shov_path = os.path.join(shov_dir, "shovel.json")
    vals = ["alyrioncore:regolith_farmland", "alyrioncore:martian_regolith", "alyrioncore:martian_sand"]
    if os.path.exists(shov_path):
        with open(shov_path, "r", encoding="utf-8") as f:
            data = json.load(f)
        for v in vals:
            if v not in data.get("values", []):
                data.setdefault("values", []).append(v)
    else:
        data = {"replace": False, "values": vals}
    write_json(shov_path, data)

    # Doors tag
    doors_dir = os.path.join(MC_DATA_DIR, "tags/block")
    ensure_dir(doors_dir)
    write_json(os.path.join(doors_dir, "doors.json"), {
        "replace": False,
        "values": ["alyrioncore:airlock"]
    })

# --- 6. LOCALIZATION ---

def update_lang():
    lang_path = os.path.join(ASSETS_DIR, "lang/en_us.json")
    with open(lang_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    data["block.alyrioncore.airlock"] = "Pressurized Habitat Airlock"
    data["block.alyrioncore.regolith_farmland"] = "Martian Regolith Farmland"
    data["block.alyrioncore.martian_potato_crop"] = "Martian Potato Crop"
    data["item.alyrioncore.martian_potato"] = "Martian Potato"
    data["item.alyrioncore.baked_martian_potato"] = "Baked Martian Potato"

    write_json(lang_path, data)

def main():
    print("Generating assets for Pressurized Habitats, Airlocks & Greenhouse System...")
    create_airlock_textures()
    create_farmland_textures()
    create_crop_and_food_textures()
    make_airlock_models_and_blockstates()
    make_farmland_models_and_blockstates()
    make_crop_and_food_models()
    make_recipes()
    make_loot_tables()
    make_tags()
    update_lang()
    print("All assets successfully generated!")

if __name__ == "__main__":
    main()
