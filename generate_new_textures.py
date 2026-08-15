#!/usr/bin/env python3
"""
Generate handcrafted 16x16 pixel art textures for new AlyrionCore blocks and tools.
Applies JAPPA design principles:
- Hue-shifted shading (cool shadows, warm highlights)
- Clear cluster volumes and beveling
- Darkened 1-pixel item silhouettes
"""

import os
from PIL import Image

TEXTURE_DIR = "/Users/lea/alyrioncore/src/main/resources/assets/alyrioncore/textures"
BLOCK_DIR = os.path.join(TEXTURE_DIR, "block")
ITEM_DIR = os.path.join(TEXTURE_DIR, "item")

def save_texture(img_data, filepath):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            c = img_data[y][x]
            if len(c) == 3:
                c = (c[0], c[1], c[2], 255)
            img.putpixel((x, y), c)
    os.makedirs(os.path.dirname(filepath), exist_ok=True)
    img.save(filepath, "PNG")
    print(f"Saved: {filepath}")

# 1. Meteoric Iron Block (Smooth cosmic gunmetal steel with beveled border and nickel sheen)
def make_meteoric_iron_block():
    H1 = (130, 140, 155) # Highlight
    H0 = (105, 115, 130) # High-mid
    M  = (80, 88, 102)   # Base metal
    D0 = (58, 64, 76)    # Shadow
    D1 = (38, 42, 52)    # Deep border
    
    grid = [[M for _ in range(16)] for _ in range(16)]
    # Border
    for x in range(16):
        grid[0][x] = H1
        grid[15][x] = D1
    for y in range(16):
        grid[y][0] = H1
        grid[y][15] = D1
        
    # Bevel corners
    grid[0][15] = H0
    grid[15][0] = D0
    
    # Inner plate bevel
    for x in range(2, 14):
        grid[2][x] = H0
        grid[13][x] = D0
    for y in range(2, 14):
        grid[y][2] = H0
        grid[y][13] = D0
        
    # Plate face with subtle cosmic grain
    pattern = [
        [M, M, H0, M, M, M, D0, M, M, H0, M, M],
        [M, H0, M, M, M, M, M, M, M, M, M, D0],
        [M, M, M, M, D0, M, M, H0, M, M, M, M],
        [H0, M, M, M, M, M, M, M, M, D0, M, M],
        [M, M, D0, M, M, H0, M, M, M, M, M, H0],
        [M, M, M, M, M, M, M, D0, M, M, M, M],
        [D0, M, M, H0, M, M, M, M, M, H0, M, M],
        [M, M, M, M, M, D0, M, M, M, M, M, D0],
        [M, H0, M, M, M, M, M, H0, M, M, M, M],
        [M, M, M, D0, M, M, M, M, M, D0, M, M],
    ]
    for y in range(3, 13):
        for x in range(3, 13):
            grid[y][x] = pattern[y-3][x-3]
            
    # Central rivet accents
    grid[3][3] = H1
    grid[3][12] = H1
    grid[12][3] = D0
    grid[12][12] = D1
    
    return grid

# 2. Raw Meteoric Iron Block (Clustered fusion-crusted nickel-iron nodules)
def make_raw_meteoric_iron_block():
    H1 = (120, 132, 148)
    H0 = (90, 100, 115)
    M  = (65, 72, 85)
    D0 = (45, 50, 60)
    D1 = (28, 32, 40)
    CR = (175, 110, 65) # Metallic rust streak
    
    grid = [[D0 for _ in range(16)] for _ in range(16)]
    nodules = [
        (2, 2, 5, 5), (8, 1, 6, 5), (1, 8, 6, 6), (8, 7, 7, 7), (2, 14, 5, 2), (13, 13, 3, 3)
    ]
    for ox, oy, w, h in nodules:
        for y in range(oy, min(16, oy+h)):
            for x in range(ox, min(16, ox+w)):
                grid[y][x] = M
                if x == ox or y == oy:
                    grid[y][x] = H0
                if x == ox + w - 1 or y == oy + h - 1:
                    grid[y][x] = D1
        grid[oy][ox] = H1
        
    grid[4][10] = CR
    grid[5][11] = CR
    grid[11][4] = CR
    return grid

# 3. Olivine Block (Radiant vibrant translucent green crystal gemstone)
def make_olivine_block():
    H2 = (195, 245, 120) # Bright lime specular
    H1 = (145, 220, 75)  # Bright peridot
    M  = (90, 185, 50)   # Rich emerald green
    D0 = (55, 140, 40)   # Deep forest green
    D1 = (30, 95, 28)    # Crystal shadow border
    
    grid = [[M for _ in range(16)] for _ in range(16)]
    # Border facet
    for x in range(16):
        grid[0][x] = H1
        grid[15][x] = D1
    for y in range(16):
        grid[y][0] = H1
        grid[y][15] = D1
        
    # Geometric facet diagonals
    for i in range(1, 15):
        grid[i][i] = H0 = (120, 205, 60)
        grid[i][15-i] = D0
        
    grid[3][3] = H2
    grid[3][4] = H2
    grid[4][3] = H2
    grid[7][7] = H2
    grid[8][8] = H1
    
    # Internal crystalline refractions
    grid[4][11] = H1
    grid[11][4] = D0
    grid[12][12] = D1
    return grid

# 4. Sulfur Block (Vibrant canary brimstone crystalline mineral block)
def make_sulfur_block():
    H2 = (255, 250, 150)
    H1 = (245, 225, 75)
    M  = (220, 190, 40)
    D0 = (175, 140, 25)
    D1 = (130, 95, 15)
    
    grid = [[M for _ in range(16)] for _ in range(16)]
    for x in range(16):
        grid[0][x] = H1
        grid[15][x] = D1
    for y in range(16):
        grid[y][0] = H1
        grid[y][15] = D1
        
    # Volcanic layered crystalline ridges
    for x in range(2, 14):
        grid[4][x] = H1 if x % 3 != 0 else H2
        grid[5][x] = M
        grid[9][x] = D0 if x % 2 == 0 else M
        grid[10][x] = H0 = (235, 205, 55)
        
    grid[2][2] = H2
    grid[2][3] = H2
    grid[7][8] = H2
    grid[13][13] = D1
    return grid

# --- TOOLS ---
# Tool palettes:
# Stick / Handle: Wood tones
ST_H = (145, 105, 65)
ST_M = (115, 78, 45)
ST_D = (80, 52, 28)
ST_O = (45, 28, 14) # Handle outline

# Meteoric Iron Blade / Head: Cosmic Nickel-Steel
MT_SP = (210, 225, 245) # Specular white-blue
MT_H  = (150, 168, 190) # Bright steel
MT_M  = (105, 118, 138) # Mid gunmetal
MT_D  = (65, 75, 92)    # Dark cosmic steel
MT_O  = (38, 42, 54)    # Silhouette outline

T = (0, 0, 0, 0) # Transparent

def make_meteoric_sword():
    grid = [[T for _ in range(16)] for _ in range(16)]
    # Tip
    grid[1][14] = MT_O
    grid[2][13] = MT_SP
    grid[2][14] = MT_O
    grid[3][12] = MT_H
    grid[3][13] = MT_O
    grid[4][11] = MT_SP
    grid[4][12] = MT_O
    
    # Blade Spine & Edge
    for i in range(5, 11):
        grid[i][15-i] = MT_H   # Highlight edge
        grid[i][16-i] = MT_M   # Full blade
        grid[i-1][16-i] = MT_SP# Glint
        grid[i+1][15-i] = MT_D # Shadow bevel
        grid[i][14-i] = MT_O   # Left outline
        grid[i][17-i] = MT_O   # Right outline
        
    # Crossguard
    grid[9][6] = MT_O; grid[9][7] = MT_H; grid[9][8] = MT_O
    grid[10][5] = MT_O; grid[10][6] = MT_H; grid[10][7] = MT_M; grid[10][8] = MT_D; grid[10][9] = MT_O
    grid[11][6] = MT_O; grid[11][7] = MT_D; grid[11][8] = MT_O
    
    # Handle
    grid[11][4] = ST_O; grid[11][5] = ST_H
    grid[12][3] = ST_O; grid[12][4] = ST_M; grid[12][5] = ST_O
    grid[13][2] = ST_O; grid[13][3] = ST_D; grid[13][4] = ST_O
    
    # Pommel
    grid[14][1] = MT_O; grid[14][2] = MT_H; grid[14][3] = MT_O
    grid[15][1] = MT_O; grid[15][2] = MT_D; grid[15][3] = MT_O
    return grid

def make_meteoric_pickaxe():
    grid = [[T for _ in range(16)] for _ in range(16)]
    # Handle diagonal
    for i in range(4, 15):
        grid[i][i] = ST_M
        grid[i][i-1] = ST_D
        grid[i-1][i] = ST_H
        
    # Outlines for stick
    for i in range(4, 15):
        if i+1 < 16 and grid[i+1][i-1] == T: grid[i+1][i-1] = ST_O
        if i+1 < 16 and grid[i-1][i+1] == T: grid[i-1][i+1] = ST_O
    grid[15][15] = ST_O
    grid[15][14] = ST_O
    grid[14][15] = ST_O

    # Pickaxe Head Arch
    # Top-right pick tip
    grid[1][14] = MT_O; grid[1][13] = MT_SP; grid[2][12] = MT_H; grid[3][11] = MT_H; grid[4][10] = MT_M
    grid[0][14] = MT_O; grid[1][15] = MT_O; grid[2][14] = MT_O; grid[3][13] = MT_O; grid[4][12] = MT_O
    grid[2][11] = MT_O; grid[3][10] = MT_O; grid[4][9] = MT_D; grid[5][8] = MT_D
    
    # Center socket
    grid[3][3] = MT_H; grid[3][4] = MT_SP; grid[4][3] = MT_H; grid[4][4] = MT_M; grid[4][5] = MT_M
    grid[2][3] = MT_O; grid[2][4] = MT_O; grid[3][5] = MT_O
    
    # Bottom-left pick tip
    grid[14][1] = MT_O; grid[13][1] = MT_SP; grid[12][2] = MT_H; grid[11][3] = MT_H; grid[10][4] = MT_M
    grid[14][0] = MT_O; grid[15][1] = MT_O; grid[14][2] = MT_O; grid[13][3] = MT_O; grid[12][4] = MT_O
    grid[11][2] = MT_O; grid[10][3] = MT_O; grid[9][4] = MT_D; grid[8][5] = MT_D
    return grid

def make_meteoric_axe():
    grid = [[T for _ in range(16)] for _ in range(16)]
    # Handle
    for i in range(5, 15):
        grid[i][i] = ST_M
        grid[i][i-1] = ST_D
        grid[i-1][i] = ST_H
    grid[15][14] = ST_O; grid[14][15] = ST_O; grid[15][15] = ST_O
    
    # Axe Head
    head_pixels = [
        (1, 8, MT_O), (1, 9, MT_O), (1, 10, MT_O), (1, 11, MT_O),
        (2, 7, MT_O), (2, 8, MT_SP), (2, 9, MT_SP), (2, 10, MT_H), (2, 11, MT_H), (2, 12, MT_O),
        (3, 6, MT_O), (3, 7, MT_H), (3, 8, MT_H), (3, 9, MT_M), (3, 10, MT_M), (3, 11, MT_D), (3, 12, MT_O),
        (4, 5, MT_O), (4, 6, MT_H), (4, 7, MT_M), (4, 8, MT_M), (4, 9, MT_D), (4, 10, MT_O),
        (5, 5, MT_O), (5, 6, MT_M), (5, 7, MT_D), (5, 8, MT_D), (5, 9, MT_O),
        (6, 6, MT_O), (6, 7, MT_D), (6, 8, MT_O),
        (7, 7, MT_O),
        # Back hook
        (3, 4, MT_O), (3, 5, MT_H), (4, 3, MT_O), (4, 4, MT_D), (5, 4, MT_O)
    ]
    for y, x, color in head_pixels:
        grid[y][x] = color
    return grid

def make_meteoric_shovel():
    grid = [[T for _ in range(16)] for _ in range(16)]
    # Handle
    for i in range(5, 15):
        grid[i][i] = ST_M
        grid[i][i-1] = ST_D
        grid[i-1][i] = ST_H
    grid[15][14] = ST_O; grid[14][15] = ST_O; grid[15][15] = ST_O
    
    # Shovel Scoop
    scoop = [
        (1, 13, MT_O), (1, 14, MT_O),
        (2, 11, MT_O), (2, 12, MT_SP), (2, 13, MT_SP), (2, 14, MT_O),
        (3, 10, MT_O), (3, 11, MT_H), (3, 12, MT_H), (3, 13, MT_M), (3, 14, MT_O),
        (4, 9, MT_O), (4, 10, MT_H), (4, 11, MT_M), (4, 12, MT_D), (4, 13, MT_O),
        (5, 8, MT_O), (5, 9, MT_D), (5, 10, MT_D), (5, 11, MT_O),
        (6, 8, MT_O), (6, 9, MT_O)
    ]
    for y, x, color in scoop:
        grid[y][x] = color
    return grid

def make_meteoric_hoe():
    grid = [[T for _ in range(16)] for _ in range(16)]
    # Handle
    for i in range(5, 15):
        grid[i][i] = ST_M
        grid[i][i-1] = ST_D
        grid[i-1][i] = ST_H
    grid[15][14] = ST_O; grid[14][15] = ST_O; grid[15][15] = ST_O
    
    # Hoe Head
    hoe = [
        (1, 8, MT_O), (1, 9, MT_O), (1, 10, MT_O), (1, 11, MT_O),
        (2, 7, MT_O), (2, 8, MT_SP), (2, 9, MT_SP), (2, 10, MT_H), (2, 11, MT_H), (2, 12, MT_O),
        (3, 6, MT_O), (3, 7, MT_H), (3, 8, MT_M), (3, 9, MT_D), (3, 10, MT_O),
        (4, 5, MT_O), (4, 6, MT_D), (4, 7, MT_O)
    ]
    for y, x, color in hoe:
        grid[y][x] = color
    return grid

def main():
    print("Generating block textures...")
    save_texture(make_meteoric_iron_block(), os.path.join(BLOCK_DIR, "meteoric_iron_block.png"))
    save_texture(make_raw_meteoric_iron_block(), os.path.join(BLOCK_DIR, "raw_meteoric_iron_block.png"))
    save_texture(make_olivine_block(), os.path.join(BLOCK_DIR, "olivine_block.png"))
    save_texture(make_sulfur_block(), os.path.join(BLOCK_DIR, "sulfur_block.png"))

    print("Generating tool textures...")
    save_texture(make_meteoric_sword(), os.path.join(ITEM_DIR, "meteoric_iron_sword.png"))
    save_texture(make_meteoric_pickaxe(), os.path.join(ITEM_DIR, "meteoric_iron_pickaxe.png"))
    save_texture(make_meteoric_axe(), os.path.join(ITEM_DIR, "meteoric_iron_axe.png"))
    save_texture(make_meteoric_shovel(), os.path.join(ITEM_DIR, "meteoric_iron_shovel.png"))
    save_texture(make_meteoric_hoe(), os.path.join(ITEM_DIR, "meteoric_iron_hoe.png"))

    print("All textures generated successfully!")

if __name__ == "__main__":
    main()
