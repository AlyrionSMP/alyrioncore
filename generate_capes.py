try:
    from PIL import Image
except ImportError:
    Image = None

def hex_to_rgb(h):
    h = h.lstrip('#')
    return tuple(int(h[i:i+2], 16) for i in (0, 2, 4))

def create_cape_2_year():
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    pixels = img.load()

    bg_main = hex_to_rgb("#4195DC")
    bg_dark = hex_to_rgb("#2F7ABF")
    gold = hex_to_rgb("#F7CA3B")
    white = hex_to_rgb("#FFFFFF")
    pink = hex_to_rgb("#F472B6")
    red = hex_to_rgb("#EF4444")
    cake_sponge = hex_to_rgb("#D79B65")
    cake_sponge_dark = hex_to_rgb("#B87B48")
    cake_icing = hex_to_rgb("#FFFBEB")
    flame = hex_to_rgb("#F97316")

    grid = [[bg_main for _ in range(10)] for _ in range(16)]

    # Border
    for yr in range(16):
        grid[yr][0] = bg_dark
        grid[yr][9] = bg_dark
    for xr in range(10):
        grid[0][xr] = bg_dark
        grid[15][xr] = bg_dark

    # Festive sparkles / confetti (no text)
    grid[1][2] = gold; grid[1][7] = gold
    grid[2][1] = white; grid[2][8] = white
    grid[3][2] = pink; grid[3][7] = pink
    grid[13][1] = gold; grid[13][8] = gold
    grid[14][2] = white; grid[14][7] = white

    # Triple Candles (yr=3..5)
    # Candle 1 (xr=2)
    grid[3][2] = gold; grid[4][2] = flame; grid[5][2] = red
    # Candle 2 (xr=4..5)
    grid[2][4] = gold; grid[3][4] = flame; grid[4][4] = gold; grid[5][4] = gold
    grid[2][5] = gold; grid[3][5] = flame; grid[4][5] = gold; grid[5][5] = gold
    # Candle 3 (xr=7)
    grid[3][7] = gold; grid[4][7] = flame; grid[5][7] = pink

    # Top Cake Tier (yr=6..8, xr=2..7)
    # Strawberries & icing top
    grid[6][2] = cake_icing; grid[6][3] = red; grid[6][4] = cake_icing; grid[6][5] = cake_icing; grid[6][6] = red; grid[6][7] = cake_icing
    # Sponge with icing drips
    grid[7][2] = cake_sponge; grid[7][3] = cake_icing; grid[7][4] = cake_sponge; grid[7][5] = cake_sponge; grid[7][6] = cake_icing; grid[7][7] = cake_sponge
    # Cream layer
    for xr in range(2, 8):
        grid[8][xr] = cake_icing

    # Bottom Cake Tier (yr=9..11, xr=1..8)
    for xr in range(1, 9):
        grid[9][xr] = cake_sponge if xr % 2 == 0 else cake_icing
        grid[10][xr] = cake_sponge if xr % 2 != 0 else cake_sponge_dark

    # Golden Cake Platter / Stand (yr=11..12, xr=1..8)
    for xr in range(1, 9):
        grid[11][xr] = gold
    grid[12][3] = gold; grid[12][4] = gold; grid[12][5] = gold; grid[12][6] = gold

    # Apply to OUTSIDE face (UV 1..10):
    for yr in range(16):
        y = 1 + yr
        for xr in range(10):
            u = 10 - xr
            pixels[u, y] = (*grid[yr][xr], 255)
            pixels[12 + xr, y] = (*grid[yr][xr], 255)

    for x in range(1, 21): pixels[x, 0] = (*bg_dark, 255)
    for y in range(1, 17):
        pixels[0, y] = (*bg_dark, 255)
        pixels[11, y] = (*bg_dark, 255)

    img.save("src/main/resources/assets/alyrioncore/textures/capes/2_year_celebration.png")
    print("Saved 2_year_celebration.png")

def create_cape_season_8():
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    pixels = img.load()

    bg_red = hex_to_rgb("#A31621")
    bg_dark = hex_to_rgb("#6B0D15")
    gold_main = hex_to_rgb("#F5BE28")
    gold_light = hex_to_rgb("#FFE27A")
    gold_dark = hex_to_rgb("#B8860B")
    white = hex_to_rgb("#FFFFFF")

    grid = [[bg_red for _ in range(10)] for _ in range(16)]

    # Gold borders
    for yr in range(16):
        grid[yr][0] = gold_dark
        grid[yr][9] = gold_dark
    for xr in range(10):
        grid[0][xr] = gold_dark
        grid[15][xr] = gold_dark

    # Gold corner trims
    grid[1][1] = gold_light; grid[1][8] = gold_light
    grid[14][1] = gold_light; grid[14][8] = gold_light

    # Crown / Crest at top (yr=2..4)
    grid[2][2] = gold_light; grid[2][4] = gold_light; grid[2][5] = gold_light; grid[2][7] = gold_light
    grid[3][2] = gold_main;  grid[3][3] = gold_light; grid[3][4] = gold_main;  grid[3][5] = gold_main;  grid[3][6] = gold_light; grid[3][7] = gold_main
    for xr in range(2, 8):
        grid[4][xr] = gold_dark

    # Royal Golden Shield with Diamond in center (no text!) (yr=5..10)
    grid[5][3] = gold_light; grid[5][4] = gold_light; grid[5][5] = gold_light; grid[5][6] = gold_light
    grid[6][2] = gold_light; grid[6][3] = gold_main;  grid[6][4] = white;      grid[6][5] = white;      grid[6][6] = gold_main;  grid[6][7] = gold_light
    grid[7][2] = gold_main;  grid[7][3] = white;      grid[7][4] = gold_light; grid[7][5] = gold_light; grid[7][6] = white;      grid[7][7] = gold_main
    grid[8][2] = gold_main;  grid[8][3] = gold_main;  grid[8][4] = white;      grid[8][5] = white;      grid[8][6] = gold_main;  grid[8][7] = gold_main
    grid[9][3] = gold_dark;  grid[9][4] = gold_main;  grid[9][5] = gold_main;  grid[9][6] = gold_dark
    grid[10][4] = gold_dark; grid[10][5] = gold_dark

    # Golden laurels below (yr=11..13)
    grid[11][1] = gold_dark; grid[11][8] = gold_dark
    grid[12][2] = gold_main; grid[12][3] = gold_light; grid[12][4] = gold_main; grid[12][5] = gold_main; grid[12][6] = gold_light; grid[12][7] = gold_main
    grid[13][3] = gold_dark; grid[13][4] = gold_main;  grid[13][5] = gold_main; grid[13][6] = gold_dark

    for yr in range(16):
        y = 1 + yr
        for xr in range(10):
            u = 10 - xr
            pixels[u, y] = (*grid[yr][xr], 255)
            pixels[12 + xr, y] = (*grid[yr][xr], 255)

    for x in range(1, 21): pixels[x, 0] = (*gold_dark, 255)
    for y in range(1, 17):
        pixels[0, y] = (*gold_dark, 255)
        pixels[11, y] = (*gold_dark, 255)

    img.save("src/main/resources/assets/alyrioncore/textures/capes/season_8.png")
    print("Saved season_8.png")

def create_cape_stars():
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    pixels = img.load()

    bg_space = hex_to_rgb("#090B14")
    bg_space2 = hex_to_rgb("#0F1424")
    # All stars strictly white / soft-white shades
    white_bright = hex_to_rgb("#FFFFFF")
    white_soft = hex_to_rgb("#E2E8F0")
    white_dim = hex_to_rgb("#94A3B8")

    metal = hex_to_rgb("#E2E8F0")
    metal_dark = hex_to_rgb("#64748B")
    solar_blue = hex_to_rgb("#2563EB")
    solar_light = hex_to_rgb("#60A5FA")
    beacon_red = hex_to_rgb("#EF4444")

    grid = [[bg_space if (xr + yr) % 3 != 0 else bg_space2 for xr in range(10)] for yr in range(16)]

    # Border
    for yr in range(16):
        grid[yr][0] = bg_space
        grid[yr][9] = bg_space
    for xr in range(10):
        grid[0][xr] = bg_space
        grid[15][xr] = bg_space

    # ALL WHITE Stars of varying brightness across the cape
    grid[1][1] = white_bright
    grid[1][5] = white_dim
    grid[1][8] = white_soft
    grid[2][3] = white_bright
    grid[3][8] = white_dim
    grid[4][1] = white_soft
    grid[5][8] = white_bright
    grid[6][1] = white_dim
    grid[10][1] = white_bright
    grid[11][8] = white_soft
    grid[12][2] = white_dim
    grid[13][7] = white_bright
    grid[14][1] = white_soft
    grid[14][5] = white_bright
    grid[14][8] = white_dim

    # Research Satellite in the center (yr=6..9, xr=1..8)
    # Left Solar Panel: xr=1..3
    grid[7][1] = solar_light; grid[7][2] = solar_blue; grid[7][3] = metal_dark
    grid[8][1] = solar_blue;  grid[8][2] = solar_light; grid[8][3] = metal_dark

    # Central Core: xr=4..5
    grid[6][4] = beacon_red;    grid[6][5] = metal_dark   # antenna beacon
    grid[7][4] = white_bright;  grid[7][5] = metal        # satellite body
    grid[8][4] = metal;         grid[8][5] = metal_dark
    grid[9][4] = white_soft;    grid[9][5] = metal_dark   # sensor / lens

    # Right Solar Panel: xr=6..8
    grid[7][6] = metal_dark;  grid[7][7] = solar_blue;  grid[7][8] = solar_light
    grid[8][6] = metal_dark;  grid[8][7] = solar_light; grid[8][8] = solar_blue

    for yr in range(16):
        y = 1 + yr
        for xr in range(10):
            u = 10 - xr
            pixels[u, y] = (*grid[yr][xr], 255)
            pixels[12 + xr, y] = (*grid[yr][xr], 255)

    for x in range(1, 21): pixels[x, 0] = (*bg_space, 255)
    for y in range(1, 17):
        pixels[0, y] = (*bg_space, 255)
        pixels[11, y] = (*bg_space, 255)

    img.save("src/main/resources/assets/alyrioncore/textures/capes/stars.png")
    print("Saved stars.png")

def create_cape_moon():
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    pixels = img.load()

    bg_space = hex_to_rgb("#060810")
    star_white = hex_to_rgb("#FFFFFF")
    star_dim = hex_to_rgb("#94A3B8")
    earth_deep_blue = hex_to_rgb("#1D4ED8")
    earth_cyan = hex_to_rgb("#38BDF8")
    earth_green = hex_to_rgb("#22C55E")
    earth_cloud = hex_to_rgb("#F8FAFC")

    lunar_light = hex_to_rgb("#C0C7D1")
    lunar_mid = hex_to_rgb("#8E99A8")
    lunar_dark = hex_to_rgb("#5A6270")
    lunar_shadow = hex_to_rgb("#363B44")

    grid = [[bg_space for _ in range(10)] for _ in range(16)]

    # White stars in top space half
    grid[1][1] = star_white
    grid[0][3] = star_dim
    grid[3][2] = star_white
    grid[5][1] = star_dim
    grid[4][5] = star_white

    # Earth in upper right (xr=6..9, yr=1..4)
    grid[1][7] = earth_deep_blue; grid[1][8] = earth_cloud
    grid[2][6] = earth_cyan;      grid[2][7] = earth_green;     grid[2][8] = earth_deep_blue; grid[2][9] = earth_cloud
    grid[3][6] = earth_deep_blue; grid[3][7] = earth_cloud;     grid[3][8] = earth_green;     grid[3][9] = earth_deep_blue
    grid[4][7] = earth_cyan;      grid[4][8] = earth_deep_blue

    # Lunar Cratered Horizon (yr=7..15)
    lunar_rows = [
        [lunar_dark, lunar_mid, lunar_light, lunar_mid, lunar_light, lunar_mid, lunar_dark, lunar_shadow, bg_space, bg_space],
        [lunar_mid, lunar_light, lunar_light, lunar_light, lunar_mid, lunar_light, lunar_mid, lunar_mid, lunar_dark, lunar_dark],
        [lunar_light, lunar_mid, lunar_shadow, lunar_shadow, lunar_mid, lunar_light, lunar_light, lunar_mid, lunar_mid, lunar_light],
        [lunar_mid, lunar_shadow, lunar_shadow, lunar_dark, lunar_mid, lunar_mid, lunar_dark, lunar_shadow, lunar_shadow, lunar_mid],
        [lunar_mid, lunar_dark, lunar_mid, lunar_light, lunar_light, lunar_dark, lunar_shadow, lunar_shadow, lunar_dark, lunar_light],
        [lunar_light, lunar_light, lunar_mid, lunar_mid, lunar_light, lunar_mid, lunar_dark, lunar_dark, lunar_mid, lunar_mid],
        [lunar_mid, lunar_shadow, lunar_dark, lunar_light, lunar_light, lunar_light, lunar_mid, lunar_mid, lunar_light, lunar_light],
        [lunar_dark, lunar_dark, lunar_mid, lunar_mid, lunar_light, lunar_mid, lunar_dark, lunar_dark, lunar_mid, lunar_dark],
        [lunar_shadow, lunar_dark, lunar_dark, lunar_mid, lunar_mid, lunar_mid, lunar_dark, lunar_shadow, lunar_shadow, lunar_shadow]
    ]

    for i, row in enumerate(lunar_rows):
        yr = 7 + i
        if yr < 16:
            for xr in range(10):
                grid[yr][xr] = row[xr]

    for yr in range(16):
        y = 1 + yr
        for xr in range(10):
            u = 10 - xr
            pixels[u, y] = (*grid[yr][xr], 255)
            pixels[12 + xr, y] = (*grid[yr][xr], 255)

    for x in range(1, 11): pixels[x, 0] = (*bg_space, 255)
    for x in range(11, 21): pixels[x, 0] = (*lunar_dark, 255)
    for y in range(1, 17):
        c = bg_space if y <= 8 else lunar_dark
        pixels[0, y] = (*c, 255)
        pixels[11, y] = (*c, 255)

    img.save("src/main/resources/assets/alyrioncore/textures/capes/moon.png")
    print("Saved moon.png")

def create_cape_marsian():
    """Martian cape v2: green alien waving on the Martian surface under a dusty
    rust sky with Phobos & Deimos, Olympus Mons silhouette and dune terrain.
    The design is symmetric so the cape texture's mirrored half reads the same.
    Pure stdlib (uses mcutil) so it regenerates without Pillow."""
    import os
    import sys
    sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), 'mc-scripts'))
    import mcutil as mc

    C = {
        '0': "#4A1705",  # deep sky top
        '1': "#7A3114",  # sky
        '2': "#A84F2B",  # sky mid
        '3': "#C2501F",  # sky near horizon
        '4': "#D96434",  # horizon glow
        'm': "#EAD9B4",  # moon (Phobos / Deimos)
        'p': "#471808",  # mountain shadow
        'P': "#5E210D",  # mountain (Olympus Mons)
        'l': "#E07340",  # soil light
        'M': "#BA4B1F",  # soil mid
        'd': "#852A0B",  # soil dark
        'x': "#4E1603",  # soil deep
        'a': "#6EE787",  # alien light
        'A': "#34D353",  # alien skin
        'D': "#15803D",  # alien dark
        'e': "#061A0C",  # alien eye
        'g': "#DCFCE7",  # eye gleam
    }

    scene = [
        "1111111111",
        "112m2222m2",
        "2222222222",
        "2222222222",
        "2222222222",
        "222pppp222",
        "22ppppp222",
        "2ppppppp22",
        "2ppppppp22",
        "3444444443",
        "lMMllMMddd",
        "MlMMllMMdd",
        "MMlMMlMMdd",
        "lMMllMMddd",
        "dMMdddMMdx",
        "dddddddddx",
    ]

    # Green alien overlaying the scene: big head with 2x2 gleaming eyes, small
    # body, arms out — no outline/antenna so nothing reads as hat or hair.
    alien = {
        (3, 7): 'a', (4, 7): 'A', (5, 7): 'A', (6, 7): 'A', (7, 7): 'a',                # head top
        (2, 8): 'A', (3, 8): 'g', (4, 8): 'e', (5, 8): 'A', (6, 8): 'g', (7, 8): 'e', (8, 8): 'A',  # eyes (gleam + black)
        (2, 9): 'A', (3, 9): 'e', (4, 9): 'e', (5, 9): 'A', (6, 9): 'e', (7, 9): 'e', (8, 9): 'A',  # eyes (black)
        (2, 10): 'A', (3, 10): 'A', (4, 10): 'A', (5, 10): 'A', (6, 10): 'A', (7, 10): 'A', (8, 10): 'A',  # chin
        (3, 11): 'A', (4, 11): 'A', (5, 11): 'A', (6, 11): 'A',                         # chin taper
        (4, 12): 'D', (5, 12): 'D',                                                     # neck
        (2, 13): 'a', (3, 13): 'D', (4, 13): 'D', (5, 13): 'D', (6, 13): 'D', (7, 13): 'D', (8, 13): 'a',  # shoulders
        (1, 14): 'a', (2, 14): 'D', (3, 14): 'A', (4, 14): 'A', (5, 14): 'A', (6, 14): 'A', (7, 14): 'D', (8, 14): 'D', (9, 14): 'a',  # arms + hands
        (3, 15): 'D', (4, 15): 'A', (5, 15): 'A', (6, 15): 'D',                         # feet
    }

    grid = [list(row) for row in scene]
    for (xr, yr), ch in alien.items():
        grid[yr][xr] = ch

    img = [[(0, 0, 0, 0)] * 64 for _ in range(32)]
    for yr in range(16):
        y = 1 + yr
        for xr in range(10):
            rgb = mc.hex2rgb(C[grid[yr][xr]])
            img[y][10 - xr] = rgb + (255,)   # mirrored left copy (visible cape back)
            img[y][12 + xr] = rgb + (255,)   # right copy (inner faces)

    # Cape frame: top edge + the two 1px side seams (cape box edges)
    for x in range(1, 21):
        img[0][x] = mc.hex2rgb(C['0']) + (255,)
    for y in range(1, 17):
        img[y][0] = mc.hex2rgb(C['x']) + (255,)
        img[y][11] = mc.hex2rgb(C['x']) + (255,)

    mc.write_png("src/main/resources/assets/alyrioncore/textures/capes/marsian.png", img)
    print("Saved marsian.png")

def create_cape_grim():
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    pixels = img.load()

    bg = hex_to_rgb("#0A0A0D")
    bg_alt = hex_to_rgb("#0D0D11")
    bg_deep = hex_to_rgb("#050507")
    bone = hex_to_rgb("#E5E1D8")
    bone_dark = hex_to_rgb("#B5B0A2")
    bone_shadow = hex_to_rgb("#6E6A5E")
    socket = hex_to_rgb("#07070A")

    grid = [[bg for _ in range(10)] for _ in range(16)]

    # Subtle shading: the hem of the cape is a touch lighter
    for yr in range(13, 16):
        for xr in range(10):
            grid[yr][xr] = bg_alt

    # Skeleton head (xr=2..7, yr=3..12)
    grid[3][3] = bone; grid[3][4] = bone; grid[3][5] = bone; grid[3][6] = bone
    grid[4][2] = bone_dark; grid[4][3] = bone; grid[4][4] = bone; grid[4][5] = bone; grid[4][6] = bone; grid[4][7] = bone_dark
    grid[5][2] = bone; grid[5][3] = socket; grid[5][4] = bone_dark; grid[5][5] = bone_dark; grid[5][6] = socket; grid[5][7] = bone
    grid[6][2] = bone_dark; grid[6][3] = socket; grid[6][4] = socket; grid[6][5] = socket; grid[6][6] = socket; grid[6][7] = bone_dark
    grid[7][2] = bone; grid[7][3] = bone_dark; grid[7][4] = socket; grid[7][5] = socket; grid[7][6] = bone_dark; grid[7][7] = bone
    grid[8][2] = bone; grid[8][3] = bone; grid[8][4] = socket; grid[8][5] = socket; grid[8][6] = bone; grid[8][7] = bone
    grid[9][2] = bone; grid[9][3] = bone; grid[9][4] = bone_dark; grid[9][5] = bone_dark; grid[9][6] = bone; grid[9][7] = bone
    grid[10][2] = bone; grid[10][3] = bone; grid[10][4] = bone; grid[10][5] = bone; grid[10][6] = bone; grid[10][7] = bone
    grid[11][2] = bone_dark; grid[11][3] = bone; grid[11][4] = bone_shadow; grid[11][5] = bone_shadow; grid[11][6] = bone; grid[11][7] = bone_dark
    grid[12][3] = bone_shadow; grid[12][4] = bone_dark; grid[12][5] = bone_dark; grid[12][6] = bone_shadow

    # Apply to OUTSIDE face (UV 1..10):
    for yr in range(16):
        y = 1 + yr
        for xr in range(10):
            u = 10 - xr
            pixels[u, y] = (*grid[yr][xr], 255)
            pixels[12 + xr, y] = (*grid[yr][xr], 255)

    for x in range(1, 21): pixels[x, 0] = (*bg_deep, 255)
    for y in range(1, 17):
        pixels[0, y] = (*bg_deep, 255)
        pixels[11, y] = (*bg_deep, 255)

    img.save("src/main/resources/assets/alyrioncore/textures/capes/grim.png")
    print("Saved grim.png")

def create_cape_pride():
    """Classic 6-stripe pride flag, all stripes EQUAL height (3px each),
    filling the full 18-row cape design area. Pure stdlib (uses mcutil)."""
    import os
    import sys
    sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), 'mc-scripts'))
    import mcutil as mc

    stripes = ["#E40303", "#FF8C00", "#FFED00", "#008026", "#24408E", "#732982"]

    img = [[(0, 0, 0, 0)] * 64 for _ in range(32)]
    y = 0
    for color in stripes:
        for _ in range(3):                      # every stripe is 3 rows
            for x in range(22):                 # cols 0..21: both halves plus seam
                img[y][x] = mc.hex2rgb(color) + (255,)
            y += 1

    mc.write_png("src/main/resources/assets/alyrioncore/textures/capes/pride.png", img)
    print("Saved pride.png")

if __name__ == "__main__":
    create_cape_2_year()
    create_cape_season_8()
    create_cape_stars()
    create_cape_moon()
    create_cape_marsian()
    create_cape_grim()
    create_cape_pride()
