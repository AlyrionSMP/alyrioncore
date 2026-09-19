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
    """Martian cape (64x32), redesigned to match the look of moon.png: deep space
    with stars, a rust Mars globe, a dithered 4-tone cratered Martian surface and
    a little green Martian standing on the dunes. Same 10x16 design grid and
    mirrored layout as moon.png. Pure stdlib (mcutil)."""
    import os
    import sys
    sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), 'mc-scripts'))
    import mcutil as mc

    C = {
        '0': "#060810",   # deep space (same as moon.png)
        '*': "#FFFFFF",   # bright star
        '+': "#94A3B8",   # dim star
        # Mars globe
        'W': "#FFF7E8",   # polar cap
        'p': "#F08C48",   # planet light surface
        'P': "#C1440E",   # planet mid
        'r': "#8A2E0C",   # planet dark maria
        'R': "#5F1E06",   # planet deep shadow
        # Martian surface (4-tone rust ramp, moon-style spacing)
        'L': "#F5A95E",   # light rust
        'N': "#DA6E1F",   # mid rust
        'D': "#96381A",   # dark rust
        'S': "#471604",   # shadow rust
        # Little Martian
        'A': "#86EFAC",   # alien highlight
        'a': "#22C55E",   # alien mid green
        'z': "#15803D",   # alien shadow / feet
        'e': "#064E3B",   # alien eye
        'w': "#FFFFFF",   # eye shine
        'm': "#065F46",   # alien mouth
    }

    grid = [
        "0*000+0000",   # 0  sky: stars
        "000000Wpp0",   # 1  Mars globe: polar cap + light surface
        "00*000rppP",   # 2  dark maria, mid surface
        "0000+0rpRr",   # 3  maria + deep shadow
        "0+00000Pr0",   # 4  globe bottom
        "00000000*0",   # 5  sky: star
        "000+000000",   # 6  sky: dim star
        "SDNDNDS000",   # 7  cratered horizon (sky gap right, like moon)
        "DNLLLNDDDS",   # 8  light dune ridge
        "NLSSSDNDSD",   # 9  distant crater (light rim, dark bowl)
        "DNDSDNSDDN",   # 10 mid ground
        "NDSDDSDSND",   # 11
        "LNDNDNLNLN",   # 12
        "NNDDNNLLNN",   # 13
        "DNNDNDNNDN",   # 14
        "SDDNNNDSSS",   # 15 dark base
    ]

    # Little Martian (5 wide x 6 tall, xr2..6, yr10..15): big head, big eyes
    # with shine, small smile, tiny body. All rows are exactly 10 chars,
    # centered on xr4.
    alien = {
        10: "...AAa....",
        11: "..AAaaa...",
        12: "..weawe...",
        13: "..eeaee...",
        14: "..ammaa...",
        15: "..zaaaz...",
    }
    for yr, row in alien.items():
        assert len(row) == 10, (yr, row)
        cells = list(grid[yr])
        for xr, ch in enumerate(row):
            if ch != '.':
                cells[xr] = ch
        grid[yr] = "".join(cells)

    # Same 64x32 mapping as moon.png: 10x16 design grid mirrored about the seam.
    width = 64
    height = 32
    img = [[(0, 0, 0, 0)] * width for _ in range(height)]
    for yr in range(16):
        y = 1 + yr
        for xr in range(10):
            rgb = mc.hex2rgb(C[grid[yr][xr]])
            img[y][10 - xr] = rgb + (255,)   # mirrored left copy (cape back)
            img[y][12 + xr] = rgb + (255,)   # right copy

    # Top edge (y=0): space left, rust seam right (moon-style)
    for x in range(1, 11):
        img[0][x] = mc.hex2rgb(C['0']) + (255,)
    for x in range(11, 21):
        img[0][x] = mc.hex2rgb(C['D']) + (255,)

    # Side edges + center seam: space above the horizon, rust below
    for y in range(1, 17):
        c = mc.hex2rgb(C['0']) if y <= 8 else mc.hex2rgb(C['D'])
        img[y][0] = c + (255,)
        img[y][11] = c + (255,)

    mc.write_png("src/main/resources/assets/alyrioncore/textures/capes/marsian.png", img)
    print("Saved 64x32 marsian.png (Moon-Style Mars with little Martian)")

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
    """Progress Pride flag (Daniel Quasar, 2018) worn hoist-up on the cape's 10x16 art area.

    Geometry is lifted from the flag's own SVG (Wikimedia Commons, viewBox
    0 0 6000 3810): five nested chevrons whose hoist extents are 1577 (black), 1209
    (brown), 844 (light blue), 477 (pink) and 111 (white) units, each arm bending at
    +1764 to an apex on the flag's centre line, over six equal 635-unit rainbow
    stripes. The chevrons are painted outer-to-inner, which is why the flag reads as
    one big white wedge with the trans pink and light blue hugging it and the black
    and brown wrapping the outside - not as four thin stripes stacked on a white bar.

    DESIGN samples that geometry onto the cape grid (16 rows hoist -> fly, 10 cols
    wearer's left -> right) by hand and de-aliased, because at 10 columns a naive
    sample collapses the thin bands into speckle. Each band's arm is a 1px diagonal
    stepping one column per row and closes on a flat 2px tip on the centre seam
    (pink row 4, light blue 5, brown 6, black 7, with the white wedge closing above
    them), so the chevron's point is a clean stack of tips instead of the old single
    misaligned black spike. The whole chevron is then set one row higher than a
    strict sample would place it: at this size the flag's own proportions leave the
    white wedge crowding the hem side, and one row up clears it.

    Six stripes never divide 10 columns evenly, so the widths are 1/2/2/2/2/1 for
    red/orange/yellow/green/blue/violet - the four middle stripes land equal at 2px
    and the red and violet read as clipped by the cape's edge, rather than the
    flag's own 2/1/2/2/1/2 rhythm whose two 1px stripes read as squeezed.

    Faces: cols 12..21 carry the flag as worn - the same region the store blits as the
    cape icon - cols 1..10 mirror it for the cloth's inner side, cols 11/0 repeat the
    design edge each side folds onto and row 0 carries the top/bottom hems, every
    edge pixel shaded 15% down so the flag reads as cloth around the cape slab.
    Pure stdlib (mcutil)."""
    import os
    import sys
    sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), 'mc-scripts'))
    import mcutil as mc

    # canonical Progress flag palette (the SVG's own hexes); K is the flag's black,
    # kept in the pack's near-black (AlyrionCore uses #0A0A0D for black cloth)
    COL = {
        'R': "#E40303", 'O': "#FF8C00", 'Y': "#FFED00", 'G': "#008026",
        'B': "#004DFF", 'V': "#750787",                       # 6 rainbow stripes
        'K': "#0A0A0D", 'N': "#613915",                       # chevron: black, brown
        'L': "#74D7EE", 'I': "#FFAFC8", 'W': "#FFFFFF",       # chevron: trans flag
    }

    # 16 rows = hoist (shoulders) -> fly (hem), 10 cols = red (left) -> violet (right)
    DESIGN = (
        "IWWWWWWWWI",
        "LIWWWWWWIL",
        "NLIWWWWILN",
        "KNLIWWILNK",
        "RKNLIILNKV",
        "ROKNLLNKBV",
        "ROOKNNKBBV",
        "ROOYKKGBBV",
        "ROOYYGGBBV",
        "ROOYYGGBBV",
        "ROOYYGGBBV",
        "ROOYYGGBBV",
        "ROOYYGGBBV",
        "ROOYYGGBBV",
        "ROOYYGGBBV",
        "ROOYYGGBBV",
    )

    # guard the chevron: every band pixel must mirror across the 4/5 seam (the rainbow
    # stripes legitimately do not - that is how a band pixel differs from a stripe one)
    # and every row must be full width, so the misaligned tip cannot creep back in
    BANDS = "NLIWK"
    for yr, row in enumerate(DESIGN):
        assert len(row) == 10, (yr, row)
        for x in range(5):
            a, b = row[x], row[9 - x]
            if a in BANDS or b in BANDS:
                assert a == b, 'row %d lost its chevron symmetry: %s' % (yr, row)

    # the slab's edge faces are the same cloth, one shade darker (reads as cloth depth)
    def hem(char):
        return tuple(int(c * 0.85) for c in mc.hex2rgb(COL[char])) + (255,)

    img = [[(0, 0, 0, 0)] * 64 for _ in range(32)]
    for yr, row in enumerate(DESIGN):                      # texture rows 1..16
        y = 1 + yr
        for xr, char in enumerate(row):
            rgb = mc.hex2rgb(COL[char]) + (255,)
            img[y][12 + xr] = rgb                          # worn face (store icon blit)
            img[y][10 - xr] = rgb                          # inner face, mirrored
        img[y][11] = hem(row[0])                           # side face folding onto col 12
        img[y][0] = hem(row[9])                            # side face folding onto col 21

    # row 0: the slab's top face is UV'd to cols 1..10 (cape top hem, so it mirrors the
    # inner face) while its bottom face takes cols 11..20 (cape bottom hem)
    for xr, char in enumerate(DESIGN[0]):
        img[0][10 - xr] = hem(char)
    for xr, char in enumerate(DESIGN[15]):
        img[0][11 + xr] = hem(char)

    mc.write_png("src/main/resources/assets/alyrioncore/textures/capes/pride.png", img)
    print("Saved pride.png (Progress Pride flag)")

if __name__ == "__main__":
    create_cape_2_year()
    create_cape_season_8()
    create_cape_stars()
    create_cape_moon()
    create_cape_marsian()
    create_cape_grim()
    create_cape_pride()
