#!/usr/bin/env python3
"""
Generate complete assets, models, textures, recipes, loot tables, and lang for Mars Sleeping Pod.
"""

import json
import os
from PIL import Image, ImageDraw

MOD_DIR = "/Users/lea/alyrioncore"
DATA_DIR = os.path.join(MOD_DIR, "src/main/resources/data/alyrioncore")
MINECRAFT_DATA_DIR = os.path.join(MOD_DIR, "src/main/resources/data/minecraft")
ASSETS_DIR = os.path.join(MOD_DIR, "src/main/resources/assets/alyrioncore")

TEXTURE_BLOCK_DIR = os.path.join(ASSETS_DIR, "textures/block")
TEXTURE_ITEM_DIR = os.path.join(ASSETS_DIR, "textures/item")

def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)
    print(f"Wrote JSON: {path}")

def save_texture_32(img_data, filepath):
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    for y in range(32):
        for x in range(32):
            c = img_data[y][x]
            if len(c) == 3:
                c = (c[0], c[1], c[2], 255)
            img.putpixel((x, y), c)
    os.makedirs(os.path.dirname(filepath), exist_ok=True)
    img.save(filepath, "PNG")
    print(f"Saved Texture: {filepath}")

# ----------------- 1. TEXTURES -----------------

def make_sleeping_pod_casing():
    # 32x32 aerospace titanium white plating with dark gunmetal seams, cyan LED indicators, and hazard accents
    W_HI  = (240, 245, 252) # Pure titanium highlight
    W_MID = (215, 222, 232) # Base aerospace plating
    W_SHD = (175, 185, 198) # Shaded white bevel
    D_HI  = (90, 100, 115)  # Gunmetal border highlight
    D_MID = (55, 62, 75)    # Gunmetal seam/panel
    D_DRK = (35, 40, 50)    # Deep shadow line
    CY_HI = (120, 255, 255) # Luminous cyan LED
    CY_MD = (30, 200, 230)  # Cyan glow
    HZ_YL = (255, 205, 40)  # Hazard yellow
    HZ_BK = (40, 42, 48)    # Hazard black
    
    grid = [[W_MID for _ in range(32)] for _ in range(32)]
    
    # Outer beveled borders
    for x in range(32):
        grid[0][x] = W_HI
        grid[1][x] = W_HI
        grid[30][x] = W_SHD
        grid[31][x] = D_DRK
    for y in range(32):
        grid[y][0] = W_HI
        grid[y][1] = W_HI
        grid[y][30] = W_SHD
        grid[y][31] = D_DRK
        
    # Panel subdivisions (aerospace modular hull tiles)
    for x in range(2, 30):
        grid[15][x] = D_DRK
        grid[16][x] = D_MID
        grid[17][x] = W_HI
    for y in range(2, 30):
        grid[y][15] = D_DRK
        grid[y][16] = D_MID
        grid[y][17] = W_HI
        
    # Rivet / bolt details at panel corners
    bolts = [(4, 4), (4, 13), (4, 19), (4, 28), (13, 4), (13, 13), (13, 19), (13, 28),
             (19, 4), (19, 13), (19, 19), (19, 28), (28, 4), (28, 13), (28, 19), (28, 28)]
    for bx, by in bolts:
        grid[by][bx] = D_MID
        grid[by-1][bx] = D_DRK
        grid[by][bx+1] = W_HI
        
    # Status LED strips
    for x in range(6, 12):
        grid[6][x] = CY_HI
        grid[7][x] = CY_MD
        grid[24][x] = CY_HI
        grid[25][x] = CY_MD
        
    # Hazard stripe bar on top-right panel
    for i in range(20, 28):
        for j in range(5, 9):
            grid[j][i] = HZ_YL if (i + j) % 4 < 2 else HZ_BK
            
    return grid

def make_sleeping_pod_base():
    # 32x32 heavy reinforced graphite steel, rubber shock absorbers, and mesh intake vents
    M_HI  = (110, 120, 135)
    M_MID = (75, 82, 95)
    M_SHD = (50, 56, 66)
    M_DRK = (30, 34, 42)
    VT_BK = (20, 22, 28)
    VT_GR = (60, 68, 78)
    COP_H = (220, 140, 80)
    COP_M = (180, 100, 50)
    
    grid = [[M_MID for _ in range(32)] for _ in range(32)]
    
    # Outer frame
    for x in range(32):
        grid[0][x] = M_HI
        grid[31][x] = M_DRK
    for y in range(32):
        grid[y][0] = M_HI
        grid[y][31] = M_DRK
        
    # Central ventilation mesh grill
    for y in range(6, 26):
        for x in range(6, 26):
            if y % 2 == 0:
                grid[y][x] = VT_BK if x % 2 == 0 else VT_GR
            else:
                grid[y][x] = VT_GR if x % 2 == 0 else M_DRK
                
    # Reinforced corner mounting brackets with copper thermal conduits
    for i in range(2, 6):
        grid[2][i] = COP_H; grid[3][i] = COP_M
        grid[29][i] = COP_H; grid[30][i] = COP_M
        grid[2][31-i] = COP_H; grid[3][31-i] = COP_M
        grid[29][31-i] = COP_H; grid[30][31-i] = COP_M

    return grid

def make_sleeping_pod_interior():
    # 32x32 dark navy cryogenic padded stasis mattress with quilted memory cushion texture & pillow
    P_HI  = (70, 95, 135)   # Quilt highlight
    P_MID = (45, 65, 98)    # Stasis cushion navy
    P_SHD = (30, 45, 72)    # Quilt seam shadow
    P_DRK = (18, 28, 48)    # Deep stitch line
    CY_AC = (80, 220, 240)  # Cryo capillary thread
    
    grid = [[P_MID for _ in range(32)] for _ in range(32)]
    
    # Diamond quilted mattress pattern
    for y in range(32):
        for x in range(32):
            val = (x + y) % 8
            val2 = (x - y) % 8
            if val == 0 or val2 == 0:
                grid[y][x] = P_DRK
            elif val == 1 or val2 == 1:
                grid[y][x] = P_SHD
            elif val == 4 and val2 == 4:
                grid[y][x] = CY_AC # Cryo gel node
            else:
                grid[y][x] = P_MID
                
    # Soft pillow / headrest area on upper section (y: 2 to 12)
    for y in range(2, 12):
        for x in range(4, 28):
            grid[y][x] = (85, 115, 160) if y < 6 else (60, 85, 125)
    for x in range(4, 28):
        grid[2][x] = (120, 155, 205)
        grid[11][x] = (35, 50, 80)
        
    return grid

def make_sleeping_pod_screen():
    # 32x32 holographic bio-monitor & Mars atmospheric telemetry HUD
    BG    = (15, 22, 35)    # Glass monitor screen dark
    CY_HI = (160, 255, 255) # Bright cyan HUD text
    CY_MD = (40, 200, 235)  # Cyan readout
    GN_HI = (120, 255, 140) # Green vitals / ECG
    GN_MD = (40, 200, 70)   # Green normal state
    OR_HI = (255, 160, 40)  # Mars warning / coordinate orange
    
    grid = [[BG for _ in range(32)] for _ in range(32)]
    
    # Screen bezel
    for x in range(32):
        grid[0][x] = (60, 75, 95)
        grid[31][x] = (30, 40, 55)
    for y in range(32):
        grid[y][0] = (60, 75, 95)
        grid[y][31] = (30, 40, 55)
        
    # ECG heart rate / pulse wave line
    ecg_y = [10, 10, 10, 10, 10, 8, 13, 5, 16, 7, 10, 10, 10, 10, 10, 10, 10, 8, 14, 5, 16, 7, 10, 10, 10, 10, 10, 10]
    for i, y in enumerate(ecg_y):
        x = i + 2
        if 0 <= x < 32 and 0 <= y < 32:
            grid[y][x] = GN_HI
            if y+1 < 32: grid[y+1][x] = GN_MD
            
    # Oxygen Bar (O2: 100%)
    for x in range(4, 28):
        grid[19][x] = (30, 60, 80)
        grid[20][x] = CY_HI if x < 26 else (40, 70, 90)
        grid[21][x] = CY_MD if x < 26 else (30, 55, 75)
        
    # Mars atmospheric sync & temperature indicators
    for x in range(4, 14):
        grid[25][x] = OR_HI
        grid[26][x] = (180, 100, 20)
    for x in range(18, 28):
        grid[25][x] = CY_HI
        grid[26][x] = CY_MD
        
    # Header status dots: [READY] [STASIS ONLINE]
    grid[3][4] = GN_HI; grid[3][5] = GN_HI
    grid[3][8] = CY_HI; grid[3][9] = CY_HI
    grid[3][12] = CY_HI; grid[3][13] = CY_HI
    return grid

def make_sleeping_pod_glass():
    # 32x32 translucent aerospace cyan tinted canopy glass with glare reflections
    # RGBA colors with alpha ~90-130
    BASE_GL = (50, 180, 230, 85)
    EDGE_GL = (80, 210, 255, 130)
    SPEC_GL = (220, 245, 255, 180)
    SPEC_MD = (140, 230, 255, 140)
    
    grid = [[BASE_GL for _ in range(32)] for _ in range(32)]
    
    # Outer edge refraction
    for x in range(32):
        grid[0][x] = EDGE_GL
        grid[31][x] = EDGE_GL
    for y in range(32):
        grid[y][0] = EDGE_GL
        grid[y][31] = EDGE_GL
        
    # Diagonal specular glint sheen (aerospace canopy reflection)
    for i in range(4, 28):
        if 0 <= i < 32:
            grid[i][i] = SPEC_GL
            if i+1 < 32: grid[i+1][i] = SPEC_MD
            if i-1 >= 0: grid[i-1][i] = SPEC_MD
            
    for i in range(16, 30):
        grid[i-12][i] = SPEC_MD
        
    return grid

def make_sleeping_pod_item_icon():
    # 32x32 pixel art item icon for the Sleeping Pod
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    
    # Draw isometric futuristic capsule
    # Base shadow
    d.polygon([(4, 20), (16, 26), (28, 20), (16, 14)], fill=(25, 30, 40, 220))
    # Titanium Hull Sled
    d.polygon([(4, 18), (16, 24), (28, 18), (28, 21), (16, 27), (4, 21)], fill=(50, 58, 72, 255))
    # Main White Shell
    d.polygon([(5, 14), (16, 19), (27, 14), (27, 18), (16, 24), (5, 18)], fill=(215, 225, 238, 255))
    # Headboard console (at back-left)
    d.polygon([(5, 9), (13, 13), (13, 17), (5, 13)], fill=(180, 195, 215, 255))
    d.polygon([(5, 9), (10, 7), (18, 11), (13, 13)], fill=(235, 242, 252, 255))
    # Screen on headboard
    d.polygon([(7, 11), (11, 13), (11, 15), (7, 13)], fill=(30, 210, 240, 255))
    # Stasis Bed Mattress inside
    d.polygon([(11, 16), (16, 18), (24, 14), (19, 12)], fill=(40, 60, 95, 255))
    # Translucent Glass Canopy Dome
    d.polygon([(9, 10), (19, 15), (27, 11), (17, 6)], fill=(80, 210, 245, 140))
    # Canopy highlight shine
    d.line([(12, 9), (21, 13)], fill=(240, 255, 255, 200), width=1)
    # Status LED
    d.ellipse([(5, 17), (7, 19)], fill=(40, 240, 120, 255))
    
    filepath = os.path.join(TEXTURE_ITEM_DIR, "sleeping_pod.png")
    os.makedirs(os.path.dirname(filepath), exist_ok=True)
    img.save(filepath, "PNG")
    print(f"Saved Item Icon: {filepath}")

# ----------------- 2. 3D BLOCK MODELS (JSON) -----------------

def generate_foot_model():
    foot_model = {
        "credit": "Designed for AlyrionCore Mars Sleeping Pod",
        "render_type": "minecraft:translucent",
        "textures": {
            "casing": "alyrioncore:block/sleeping_pod_casing",
            "base": "alyrioncore:block/sleeping_pod_base",
            "interior": "alyrioncore:block/sleeping_pod_interior",
            "glass": "alyrioncore:block/sleeping_pod_glass",
            "particle": "alyrioncore:block/sleeping_pod_casing"
        },
        "elements": [
            # 1. Base Left & Right Sled Struts
            {
                "name": "sled_left",
                "from": [1, 0, 0],
                "to": [3, 2, 16],
                "faces": {
                    "north": {"uv": [0, 14, 2, 16], "texture": "#base"},
                    "south": {"uv": [0, 14, 2, 16], "texture": "#base"},
                    "west":  {"uv": [0, 0, 16, 2], "texture": "#base"},
                    "east":  {"uv": [0, 0, 16, 2], "texture": "#base"},
                    "down":  {"uv": [0, 0, 2, 16], "texture": "#base", "cullface": "down"}
                }
            },
            {
                "name": "sled_right",
                "from": [13, 0, 0],
                "to": [15, 2, 16],
                "faces": {
                    "north": {"uv": [14, 14, 16, 16], "texture": "#base"},
                    "south": {"uv": [14, 14, 16, 16], "texture": "#base"},
                    "west":  {"uv": [0, 0, 16, 2], "texture": "#base"},
                    "east":  {"uv": [0, 0, 16, 2], "texture": "#base"},
                    "down":  {"uv": [14, 0, 16, 16], "texture": "#base", "cullface": "down"}
                }
            },
            # 2. Foot Cross Brace & Shock Pads
            {
                "name": "foot_cross_mount",
                "from": [3, 0, 1],
                "to": [13, 1.5, 4],
                "faces": {
                    "north": {"uv": [3, 14, 13, 15.5], "texture": "#base"},
                    "south": {"uv": [3, 14, 13, 15.5], "texture": "#base"},
                    "down":  {"uv": [3, 1, 13, 4], "texture": "#base", "cullface": "down"}
                }
            },
            {
                "name": "foot_shock_pad_left",
                "from": [0.5, 0, 1],
                "to": [3.5, 2.2, 4.5],
                "faces": {
                    "north": {"uv": [0, 12, 3, 14.2], "texture": "#base"},
                    "south": {"uv": [0, 12, 3, 14.2], "texture": "#base"},
                    "west":  {"uv": [1, 12, 4.5, 14.2], "texture": "#base"},
                    "up":    {"uv": [0, 1, 3, 4.5], "texture": "#base"},
                    "down":  {"uv": [0, 1, 3, 4.5], "texture": "#base", "cullface": "down"}
                }
            },
            {
                "name": "foot_shock_pad_right",
                "from": [12.5, 0, 1],
                "to": [15.5, 2.2, 4.5],
                "faces": {
                    "north": {"uv": [13, 12, 16, 14.2], "texture": "#base"},
                    "south": {"uv": [13, 12, 16, 14.2], "texture": "#base"},
                    "east":  {"uv": [11.5, 12, 15, 14.2], "texture": "#base"},
                    "up":    {"uv": [13, 1, 16, 4.5], "texture": "#base"},
                    "down":  {"uv": [13, 1, 16, 4.5], "texture": "#base", "cullface": "down"}
                }
            },
            # 3. Outer Hull Chassis (Bottom Floor & Side Walls)
            {
                "name": "hull_bottom",
                "from": [1, 1.5, 0],
                "to": [15, 3.5, 16],
                "faces": {
                    "north": {"uv": [1, 13, 15, 15], "texture": "#casing"},
                    "south": {"uv": [1, 13, 15, 15], "texture": "#casing"},
                    "down":  {"uv": [1, 0, 15, 16], "texture": "#base"}
                }
            },
            {
                "name": "hull_wall_left",
                "from": [1, 3.5, 0],
                "to": [3, 8, 16],
                "faces": {
                    "north": {"uv": [1, 8, 3, 12.5], "texture": "#casing"},
                    "west":  {"uv": [0, 8, 16, 12.5], "texture": "#casing"},
                    "east":  {"uv": [0, 8, 16, 12.5], "texture": "#casing"}
                }
            },
            {
                "name": "hull_wall_right",
                "from": [13, 3.5, 0],
                "to": [15, 8, 16],
                "faces": {
                    "north": {"uv": [13, 8, 15, 12.5], "texture": "#casing"},
                    "west":  {"uv": [0, 8, 16, 12.5], "texture": "#casing"},
                    "east":  {"uv": [0, 8, 16, 12.5], "texture": "#casing"}
                }
            },
            {
                "name": "hull_foot_nose",
                "from": [1, 3.5, 0],
                "to": [15, 8.5, 3],
                "faces": {
                    "north": {"uv": [1, 7.5, 15, 12.5], "texture": "#casing"},
                    "west":  {"uv": [0, 7.5, 3, 12.5], "texture": "#casing"},
                    "east":  {"uv": [13, 7.5, 16, 12.5], "texture": "#casing"},
                    "south": {"uv": [1, 7.5, 15, 12.5], "texture": "#casing"},
                    "up":    {"uv": [1, 0, 15, 3], "texture": "#casing"}
                }
            },
            # 4. Chamfered Metallic Edge Rims
            {
                "name": "rim_left",
                "from": [1, 8, 0],
                "to": [3.5, 9, 16],
                "faces": {
                    "north": {"uv": [1, 7, 3.5, 8], "texture": "#casing"},
                    "west":  {"uv": [0, 7, 16, 8], "texture": "#casing"},
                    "east":  {"uv": [0, 7, 16, 8], "texture": "#casing"},
                    "up":    {"uv": [1, 0, 3.5, 16], "texture": "#casing"}
                }
            },
            {
                "name": "rim_right",
                "from": [12.5, 8, 0],
                "to": [15, 9, 16],
                "faces": {
                    "north": {"uv": [12.5, 7, 15, 8], "texture": "#casing"},
                    "west":  {"uv": [0, 7, 16, 8], "texture": "#casing"},
                    "east":  {"uv": [0, 7, 16, 8], "texture": "#casing"},
                    "up":    {"uv": [12.5, 0, 15, 16], "texture": "#casing"}
                }
            },
            # 5. Turbine Exhaust Coolant Port
            {
                "name": "turbine_vent",
                "from": [4, 4, 0],
                "to": [12, 7.5, 0.5],
                "faces": {
                    "north": {"uv": [4, 8, 12, 11.5], "texture": "#base"}
                }
            },
            # 6. Interior Padded Cryogenic Mattress
            {
                "name": "mattress",
                "from": [3, 3.5, 0],
                "to": [13, 5.5, 16],
                "faces": {
                    "north": {"uv": [3, 10, 13, 12], "texture": "#interior"},
                    "up":    {"uv": [3, 0, 13, 16], "texture": "#interior"}
                }
            },
            {
                "name": "foot_rest",
                "from": [3, 5.5, 2.5],
                "to": [13, 6.5, 4],
                "faces": {
                    "north": {"uv": [3, 9.5, 13, 10.5], "texture": "#interior"},
                    "south": {"uv": [3, 9.5, 13, 10.5], "texture": "#interior"},
                    "up":    {"uv": [3, 2.5, 13, 4], "texture": "#interior"}
                }
            },
            # 7. Curved Translucent Glass Canopy & Frame
            {
                "name": "canopy_front_arch",
                "from": [2, 8.5, 2.5],
                "to": [14, 13, 4],
                "faces": {
                    "north": {"uv": [2, 3, 14, 7.5], "texture": "#casing"},
                    "south": {"uv": [2, 3, 14, 7.5], "texture": "#casing"},
                    "west":  {"uv": [2.5, 3, 4, 7.5], "texture": "#casing"},
                    "east":  {"uv": [12, 3, 13.5, 7.5], "texture": "#casing"},
                    "up":    {"uv": [2, 2.5, 14, 4], "texture": "#casing"}
                }
            },
            {
                "name": "canopy_glass_front",
                "from": [3, 9, 2],
                "to": [13, 12, 2.8],
                "faces": {
                    "north": {"uv": [3, 4, 13, 7], "texture": "#glass"},
                    "south": {"uv": [3, 4, 13, 7], "texture": "#glass"},
                    "up":    {"uv": [3, 2, 13, 2.8], "texture": "#glass"}
                }
            },
            {
                "name": "canopy_glass_left",
                "from": [2.5, 8.5, 3],
                "to": [3.5, 12.5, 16],
                "faces": {
                    "west":  {"uv": [0, 3.5, 13, 7.5], "texture": "#glass"},
                    "east":  {"uv": [0, 3.5, 13, 7.5], "texture": "#glass"}
                }
            },
            {
                "name": "canopy_glass_right",
                "from": [12.5, 8.5, 3],
                "to": [13.5, 12.5, 16],
                "faces": {
                    "west":  {"uv": [3, 3.5, 16, 7.5], "texture": "#glass"},
                    "east":  {"uv": [3, 3.5, 16, 7.5], "texture": "#glass"}
                }
            },
            {
                "name": "canopy_glass_top",
                "from": [3.5, 12, 3],
                "to": [12.5, 13, 16],
                "faces": {
                    "up":    {"uv": [3.5, 3, 12.5, 16], "texture": "#glass"},
                    "down":  {"uv": [3.5, 3, 12.5, 16], "texture": "#glass"}
                }
            }
        ]
    }
    write_json(os.path.join(ASSETS_DIR, "models/block/sleeping_pod_foot.json"), foot_model)

def generate_head_model():
    head_model = {
        "credit": "Designed for AlyrionCore Mars Sleeping Pod",
        "render_type": "minecraft:translucent",
        "textures": {
            "casing": "alyrioncore:block/sleeping_pod_casing",
            "base": "alyrioncore:block/sleeping_pod_base",
            "interior": "alyrioncore:block/sleeping_pod_interior",
            "screen": "alyrioncore:block/sleeping_pod_screen",
            "glass": "alyrioncore:block/sleeping_pod_glass",
            "particle": "alyrioncore:block/sleeping_pod_casing"
        },
        "elements": [
            # 1. Base Left & Right Sled Struts
            {
                "name": "sled_left",
                "from": [1, 0, 0],
                "to": [3, 2, 16],
                "faces": {
                    "north": {"uv": [0, 14, 2, 16], "texture": "#base"},
                    "south": {"uv": [0, 14, 2, 16], "texture": "#base"},
                    "west":  {"uv": [0, 0, 16, 2], "texture": "#base"},
                    "east":  {"uv": [0, 0, 16, 2], "texture": "#base"},
                    "down":  {"uv": [0, 0, 2, 16], "texture": "#base", "cullface": "down"}
                }
            },
            {
                "name": "sled_right",
                "from": [13, 0, 0],
                "to": [15, 2, 16],
                "faces": {
                    "north": {"uv": [14, 14, 16, 16], "texture": "#base"},
                    "south": {"uv": [14, 14, 16, 16], "texture": "#base"},
                    "west":  {"uv": [0, 0, 16, 2], "texture": "#base"},
                    "east":  {"uv": [0, 0, 16, 2], "texture": "#base"},
                    "down":  {"uv": [14, 0, 16, 16], "texture": "#base", "cullface": "down"}
                }
            },
            # 2. Head Shock Pads & Life Support Generator Base
            {
                "name": "head_shock_pad_left",
                "from": [0.5, 0, 11.5],
                "to": [3.5, 2.2, 15],
                "faces": {
                    "north": {"uv": [0, 12, 3, 14.2], "texture": "#base"},
                    "south": {"uv": [0, 12, 3, 14.2], "texture": "#base"},
                    "west":  {"uv": [11.5, 12, 15, 14.2], "texture": "#base"},
                    "up":    {"uv": [0, 11.5, 3, 15], "texture": "#base"},
                    "down":  {"uv": [0, 11.5, 3, 15], "texture": "#base", "cullface": "down"}
                }
            },
            {
                "name": "head_shock_pad_right",
                "from": [12.5, 0, 11.5],
                "to": [15.5, 2.2, 15],
                "faces": {
                    "north": {"uv": [13, 12, 16, 14.2], "texture": "#base"},
                    "south": {"uv": [13, 12, 16, 14.2], "texture": "#base"},
                    "east":  {"uv": [11.5, 12, 15, 14.2], "texture": "#base"},
                    "up":    {"uv": [13, 11.5, 16, 15], "texture": "#base"},
                    "down":  {"uv": [13, 11.5, 16, 15], "texture": "#base", "cullface": "down"}
                }
            },
            {
                "name": "life_support_generator_base",
                "from": [4, 0, 12.5],
                "to": [12, 3, 16],
                "faces": {
                    "north": {"uv": [4, 13, 12, 16], "texture": "#base"},
                    "south": {"uv": [4, 13, 12, 16], "texture": "#base"},
                    "down":  {"uv": [4, 12.5, 12, 16], "texture": "#base", "cullface": "down"}
                }
            },
            # 3. Outer Hull Floor & Side Walls
            {
                "name": "hull_bottom",
                "from": [1, 1.5, 0],
                "to": [15, 3.5, 16],
                "faces": {
                    "north": {"uv": [1, 13, 15, 15], "texture": "#casing"},
                    "south": {"uv": [1, 13, 15, 15], "texture": "#casing"},
                    "down":  {"uv": [1, 0, 15, 16], "texture": "#base"}
                }
            },
            {
                "name": "hull_wall_left",
                "from": [1, 3.5, 0],
                "to": [3, 8, 13],
                "faces": {
                    "west":  {"uv": [0, 8, 13, 12.5], "texture": "#casing"},
                    "east":  {"uv": [0, 8, 13, 12.5], "texture": "#casing"}
                }
            },
            {
                "name": "hull_wall_right",
                "from": [13, 3.5, 0],
                "to": [15, 8, 13],
                "faces": {
                    "west":  {"uv": [0, 8, 13, 12.5], "texture": "#casing"},
                    "east":  {"uv": [0, 8, 13, 12.5], "texture": "#casing"}
                }
            },
            {
                "name": "rim_left",
                "from": [1, 8, 0],
                "to": [3.5, 9, 13],
                "faces": {
                    "west":  {"uv": [0, 7, 13, 8], "texture": "#casing"},
                    "east":  {"uv": [0, 7, 13, 8], "texture": "#casing"},
                    "up":    {"uv": [1, 0, 3.5, 13], "texture": "#casing"}
                }
            },
            {
                "name": "rim_right",
                "from": [12.5, 8, 0],
                "to": [15, 9, 13],
                "faces": {
                    "west":  {"uv": [0, 7, 13, 8], "texture": "#casing"},
                    "east":  {"uv": [0, 7, 13, 8], "texture": "#casing"},
                    "up":    {"uv": [12.5, 0, 15, 13], "texture": "#casing"}
                }
            },
            # 4. Reinforced Headboard & Life Support Bulkhead Tower
            {
                "name": "headboard_bulkhead",
                "from": [1, 3.5, 13],
                "to": [15, 14, 16],
                "faces": {
                    "north": {"uv": [1, 2, 15, 12.5], "texture": "#casing"},
                    "south": {"uv": [1, 2, 15, 12.5], "texture": "#casing"},
                    "west":  {"uv": [13, 2, 16, 12.5], "texture": "#casing"},
                    "east":  {"uv": [0, 2, 3, 12.5], "texture": "#casing"},
                    "up":    {"uv": [1, 13, 15, 16], "texture": "#casing"}
                }
            },
            # 5. Interior Mattress & Soft Ergonomic Pillow
            {
                "name": "mattress",
                "from": [3, 3.5, 0],
                "to": [13, 5.5, 9],
                "faces": {
                    "up":    {"uv": [3, 0, 13, 9], "texture": "#interior"}
                }
            },
            {
                "name": "ergonomic_pillow",
                "from": [3.5, 5, 8],
                "to": [12.5, 7.5, 13],
                "faces": {
                    "north": {"uv": [3.5, 8.5, 12.5, 11], "texture": "#interior"},
                    "south": {"uv": [3.5, 8.5, 12.5, 11], "texture": "#interior"},
                    "west":  {"uv": [8, 8.5, 13, 11], "texture": "#interior"},
                    "east":  {"uv": [8, 8.5, 13, 11], "texture": "#interior"},
                    "up":    {"uv": [3.5, 2, 12.5, 7], "texture": "#interior"}
                }
            },
            # 6. High-Tech Holographic Monitor / Console & Oxygen Tanks
            {
                "name": "monitor_housing",
                "from": [2.5, 8.5, 11.5],
                "to": [13.5, 13.5, 13.5],
                "faces": {
                    "north": {"uv": [2.5, 2.5, 13.5, 7.5], "texture": "#casing"},
                    "west":  {"uv": [11.5, 2.5, 13.5, 7.5], "texture": "#casing"},
                    "east":  {"uv": [2.5, 2.5, 4.5, 7.5], "texture": "#casing"},
                    "up":    {"uv": [2.5, 11.5, 13.5, 13.5], "texture": "#casing"}
                }
            },
            {
                "name": "hud_screen",
                "from": [3.5, 9, 11.2],
                "to": [12.5, 13, 11.8],
                "faces": {
                    "north": {"uv": [3.5, 3, 12.5, 7], "texture": "#screen"},
                    "south": {"uv": [3.5, 3, 12.5, 7], "texture": "#screen"},
                    "up":    {"uv": [3.5, 11.2, 12.5, 11.8], "texture": "#screen"}
                }
            },
            {
                "name": "oxygen_tank_left",
                "from": [1.5, 8, 13.5],
                "to": [3.5, 15.5, 15.5],
                "faces": {
                    "north": {"uv": [1.5, 0.5, 3.5, 8], "texture": "#base"},
                    "south": {"uv": [1.5, 0.5, 3.5, 8], "texture": "#base"},
                    "west":  {"uv": [13.5, 0.5, 15.5, 8], "texture": "#base"},
                    "east":  {"uv": [0.5, 0.5, 2.5, 8], "texture": "#base"},
                    "up":    {"uv": [1.5, 13.5, 3.5, 15.5], "texture": "#base"}
                }
            },
            {
                "name": "oxygen_tank_right",
                "from": [12.5, 8, 13.5],
                "to": [14.5, 15.5, 15.5],
                "faces": {
                    "north": {"uv": [12.5, 0.5, 14.5, 8], "texture": "#base"},
                    "south": {"uv": [12.5, 0.5, 14.5, 8], "texture": "#base"},
                    "west":  {"uv": [13.5, 0.5, 15.5, 8], "texture": "#base"},
                    "east":  {"uv": [0.5, 0.5, 2.5, 8], "texture": "#base"},
                    "up":    {"uv": [12.5, 13.5, 14.5, 15.5], "texture": "#base"}
                }
            },
            {
                "name": "status_beacon",
                "from": [7, 14, 14],
                "to": [9, 15.5, 15.5],
                "faces": {
                    "north": {"uv": [7, 0.5, 9, 2], "texture": "#casing"},
                    "south": {"uv": [7, 0.5, 9, 2], "texture": "#casing"},
                    "west":  {"uv": [14, 0.5, 15.5, 2], "texture": "#casing"},
                    "east":  {"uv": [0.5, 0.5, 2, 2], "texture": "#casing"},
                    "up":    {"uv": [7, 14, 9, 15.5], "texture": "#casing"}
                }
            },
            # 7. Translucent Glass Canopy & Frame (Head Section)
            {
                "name": "canopy_rear_arch",
                "from": [2, 8.5, 10.5],
                "to": [14, 13, 12],
                "faces": {
                    "north": {"uv": [2, 3, 14, 7.5], "texture": "#casing"},
                    "south": {"uv": [2, 3, 14, 7.5], "texture": "#casing"},
                    "west":  {"uv": [10.5, 3, 12, 7.5], "texture": "#casing"},
                    "east":  {"uv": [4, 3, 5.5, 7.5], "texture": "#casing"},
                    "up":    {"uv": [2, 10.5, 14, 12], "texture": "#casing"}
                }
            },
            {
                "name": "canopy_glass_left",
                "from": [2.5, 8.5, 0],
                "to": [3.5, 12.5, 11],
                "faces": {
                    "west":  {"uv": [0, 3.5, 11, 7.5], "texture": "#glass"},
                    "east":  {"uv": [0, 3.5, 11, 7.5], "texture": "#glass"}
                }
            },
            {
                "name": "canopy_glass_right",
                "from": [12.5, 8.5, 0],
                "to": [13.5, 12.5, 11],
                "faces": {
                    "west":  {"uv": [5, 3.5, 16, 7.5], "texture": "#glass"},
                    "east":  {"uv": [5, 3.5, 16, 7.5], "texture": "#glass"}
                }
            },
            {
                "name": "canopy_glass_top",
                "from": [3.5, 12, 0],
                "to": [12.5, 13, 11],
                "faces": {
                    "up":    {"uv": [3.5, 0, 12.5, 11], "texture": "#glass"},
                    "down":  {"uv": [3.5, 0, 12.5, 11], "texture": "#glass"}
                }
            }
        ]
    }
    write_json(os.path.join(ASSETS_DIR, "models/block/sleeping_pod_head.json"), head_model)

def generate_blockstate():
    # Rotations for facing:
    # In Minecraft standard HorizontalDirectionalBlock:
    # north = 0 deg, east = 90 deg, south = 180 deg, west = 270 deg
    # Both halves are additionally rotated +180 deg: the models put the closed ends
    # (foot nose / head bulkhead) on the inner sides, so we flip them to the outer
    # head/foot ends and let the glass canopies open toward each other in the middle.
    bs = {
        "variants": {
            "facing=north,part=foot": {"model": "alyrioncore:block/sleeping_pod_foot", "y": 180},
            "facing=east,part=foot":  {"model": "alyrioncore:block/sleeping_pod_foot", "y": 270},
            "facing=south,part=foot": {"model": "alyrioncore:block/sleeping_pod_foot", "y": 0},
            "facing=west,part=foot":  {"model": "alyrioncore:block/sleeping_pod_foot", "y": 90},

            "facing=north,part=head": {"model": "alyrioncore:block/sleeping_pod_head", "y": 180},
            "facing=east,part=head":  {"model": "alyrioncore:block/sleeping_pod_head", "y": 270},
            "facing=south,part=head": {"model": "alyrioncore:block/sleeping_pod_head", "y": 0},
            "facing=west,part=head":  {"model": "alyrioncore:block/sleeping_pod_head", "y": 90}
        }
    }
    write_json(os.path.join(ASSETS_DIR, "blockstates/sleeping_pod.json"), bs)

def generate_item_model():
    # Item model uses the 2D item icon or 3D block model with display transforms
    item_model = {
        "parent": "minecraft:item/generated",
        "textures": {
            "layer0": "alyrioncore:item/sleeping_pod"
        }
    }
    write_json(os.path.join(ASSETS_DIR, "models/item/sleeping_pod.json"), item_model)

# ----------------- 3. RECIPES & LOOT -----------------

def generate_recipe():
    recipe = {
        "type": "minecraft:crafting_shaped",
        "category": "misc",
        "key": {
            "G": {
                "item": "minecraft:glass"
            },
            "I": {
                "item": "minecraft:iron_ingot"
            },
            "B": {
                "tag": "minecraft:beds"
            }
        },
        "pattern": [
            "GGG",
            "IBI",
            "III"
        ],
        "result": {
            "count": 1,
            "id": "alyrioncore:sleeping_pod"
        }
    }
    write_json(os.path.join(DATA_DIR, "recipe/sleeping_pod.json"), recipe)

def generate_loot():
    loot = {
        "type": "minecraft:block",
        "pools": [
            {
                "bonus_rolls": 0.0,
                "conditions": [
                    {
                        "condition": "minecraft:survives_explosion"
                    },
                    {
                        "block": "alyrioncore:sleeping_pod",
                        "condition": "minecraft:block_state_property",
                        "properties": {
                            "part": "foot"
                        }
                    }
                ],
                "entries": [
                    {
                        "type": "minecraft:item",
                        "name": "alyrioncore:sleeping_pod"
                    }
                ],
                "rolls": 1.0
            }
        ]
    }
    write_json(os.path.join(DATA_DIR, "loot_table/blocks/sleeping_pod.json"), loot)
    write_json(os.path.join(DATA_DIR, "loot_tables/blocks/sleeping_pod.json"), loot)

def update_pickaxe_tag():
    pickaxe_path = os.path.join(MINECRAFT_DATA_DIR, "tags/block/mineable/pickaxe.json")
    with open(pickaxe_path, "r", encoding="utf-8") as f:
        data = json.load(f)
    if "alyrioncore:sleeping_pod" not in data["values"]:
        data["values"].append("alyrioncore:sleeping_pod")
    write_json(pickaxe_path, data)

def update_lang():
    lang_path = os.path.join(ASSETS_DIR, "lang/en_us.json")
    with open(lang_path, "r", encoding="utf-8") as f:
        data = json.load(f)
    data["block.alyrioncore.sleeping_pod"] = "Mars Sleeping Pod"
    data["block.alyrioncore.sleeping_pod.occupied"] = "This sleeping pod is occupied"
    write_json(lang_path, data)

def main():
    print("Generating Sleeping Pod textures...")
    save_texture_32(make_sleeping_pod_casing(), os.path.join(TEXTURE_BLOCK_DIR, "sleeping_pod_casing.png"))
    save_texture_32(make_sleeping_pod_base(), os.path.join(TEXTURE_BLOCK_DIR, "sleeping_pod_base.png"))
    save_texture_32(make_sleeping_pod_interior(), os.path.join(TEXTURE_BLOCK_DIR, "sleeping_pod_interior.png"))
    save_texture_32(make_sleeping_pod_screen(), os.path.join(TEXTURE_BLOCK_DIR, "sleeping_pod_screen.png"))
    save_texture_32(make_sleeping_pod_glass(), os.path.join(TEXTURE_BLOCK_DIR, "sleeping_pod_glass.png"))
    make_sleeping_pod_item_icon()

    print("Generating 3D Models & Blockstates...")
    generate_foot_model()
    generate_head_model()
    generate_blockstate()
    generate_item_model()

    print("Generating Recipe, Loot Tables, Tags, and Lang...")
    generate_recipe()
    generate_loot()
    update_pickaxe_tag()
    update_lang()

    print("All Sleeping Pod assets generated successfully!")

if __name__ == "__main__":
    main()
