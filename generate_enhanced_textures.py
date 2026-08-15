import os
from PIL import Image

def hex_to_rgb(h):
    h = h.lstrip('#')
    if len(h) == 6:
        return tuple(int(h[i:i+2], 16) for i in (0, 2, 4)) + (255,)
    elif len(h) == 8:
        return tuple(int(h[i:i+2], 16) for i in (0, 2, 4, 6))
    return (0, 0, 0, 255)

def create_image(grid, palette, out_path):
    img = Image.new('RGBA', (16, 16))
    pixels = img.load()
    for y in range(16):
        row = grid[y]
        for x in range(16):
            char = row[x]
            if char in palette:
                color = palette[char]
                if isinstance(color, str):
                    pixels[x, y] = hex_to_rgb(color)
                else:
                    pixels[x, y] = color
            else:
                pixels[x, y] = (0, 0, 0, 0)
    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    img.save(out_path)
    print(f"Generated enhanced texture: {out_path}")

# ==============================================================================
# 1. BLOCKS (19 Textures)
# ==============================================================================

# ------------------------------------------------------------------------------
# Block 1: martian_sand
# Pass 1: Warm ochre-orange hue shifting to deep rust shadow
# Pass 2: Smooth flowing sand dune ripples with grain clusters
# Pass 3: Specular quartz/ferric oxide dust flecks
# ------------------------------------------------------------------------------
sand_palette = {
    '0': '#DE7A3E', # Highlight dune crest
    '1': '#CB6731', # Light midtone
    '2': '#B85526', # Core midtone
    '3': '#A1441E', # Shadow slope
    '4': '#873418', # Deep valley shadow
    '5': '#E89052', # Specular ferric quartz grain
}
sand_grid = [
    "1221105012233221",
    "2332100122344322",
    "3432211223443323",
    "2332212334432232",
    "1222101233321231",
    "0110050122210120",
    "1001101233210011",
    "2112212343211122",
    "2223223443222232",
    "3334334432233433",
    "2334444321234432",
    "1233433210123321",
    "0122322105012210",
    "1012211001123211",
    "2123221112234322",
    "2234322223344322"
]

# ------------------------------------------------------------------------------
# Block 2: martian_regolith
# Pass 1: Weathered iron-rich basalt soil palette
# Pass 2: Fine gritty soil texture with irregular organic rock crumbs
# Pass 3: High-contrast silicate glints and micro-cavities
# ------------------------------------------------------------------------------
regolith_palette = {
    '0': '#C96232', # Highlight soil
    '1': '#B04F26', # Light brown-red
    '2': '#963E1C', # Base soil
    '3': '#7B2E15', # Dark soil
    '4': '#5E200F', # Deep crevice
    '5': '#D87A4A', # Quartz dust highlight
    '6': '#4A180B', # Cavity shadow
}
regolith_grid = [
    "2321234221012342",
    "3432346332123463",
    "2352234210502342",
    "1233123101111231",
    "0124212212220122",
    "1234323323431233",
    "2346334434642344",
    "3443235323433432",
    "2332123212322321",
    "1221012101211210",
    "2332123212322321",
    "3443234323433432",
    "4643346434644643",
    "3432234323433432",
    "2321123212322321",
    "1210012101211210"
]

# ------------------------------------------------------------------------------
# Block 3: coarse_martian_regolith
# Pass 1: Rich rocky red-brown soil with dark basalt gravel
# Pass 2: Distinct chunky pebbles and aggregate clusters embedded in dirt
# Pass 3: Ambient occlusion around pebbles with sharp top-left highlights
# ------------------------------------------------------------------------------
coarse_regolith_palette = {
    '0': '#C45D2E', # Highlight soil
    '1': '#A64620', # Midtone soil
    '2': '#873416', # Dark soil
    '3': '#65230E', # Base soil shadow
    '4': '#635350', # Basalt pebble highlight
    '5': '#453836', # Basalt pebble midtone
    '6': '#2E2322', # Basalt pebble shadow
    '7': '#D97545', # Silicate glint
}
coarse_regolith_grid = [
    "1210123214562121",
    "2321232145566322",
    "3272310155663231",
    "2123211266321245",
    "1012456212324556",
    "2145566323215566",
    "1455663212106632",
    "2556621012121231",
    "3663212723456212",
    "2321012324556632",
    "1211223115566321",
    "0145622016632120",
    "1455663101232171",
    "2556632121245622",
    "3663210124556632",
    "2321121215566321"
]

# ------------------------------------------------------------------------------
# Block 4: frost_dusted_regolith
# Pass 1: Contrast between ferric red and frosty icy cyan-white
# Pass 2: Organic frost rime creeping into crevices and coating ridges
# Pass 3: Glinting ice crystals with micro-highlights
# ------------------------------------------------------------------------------
frost_regolith_palette = {
    '0': '#EAF9FF', # Pure frost white
    '1': '#BFE4F5', # Soft cyan ice highlight
    '2': '#88B8D0', # Shadowed rime
    '3': '#B85526', # Red regolith light
    '4': '#963E1C', # Red regolith mid
    '5': '#6E2812', # Red regolith dark
    '6': '#4E1A0B', # Deep cavity
    '7': '#DCEFFA', # Sparkling ice glint
}
frost_regolith_grid = [
    "0171024340170125",
    "1001255451001256",
    "7122456542122455",
    "1245566543455543",
    "0124554340124434",
    "1012443410012345",
    "7101234571012456",
    "1212345612124554",
    "2324554323245434",
    "0171243401712345",
    "1001245510012456",
    "7122556671225545",
    "1245665412455434",
    "0124543401244345",
    "1012344510123456",
    "2123455621234545"
]

# ------------------------------------------------------------------------------
# Block 5: martian_permafrost
# Pass 1: Subsurface icy frozen soil palette (muddy blue-gray & rust)
# Pass 2: Crystalline ice veins branching through compressed frozen regolith
# Pass 3: Translucent icy sheen and sharp frozen fractures
# ------------------------------------------------------------------------------
permafrost_palette = {
    '0': '#D4F0FC', # Bright ice fracture
    '1': '#9DC8DE', # Blue ice vein light
    '2': '#6996AE', # Blue ice vein shadow
    '3': '#7D4C3A', # Frozen dirt highlight
    '4': '#5E3426', # Frozen dirt core
    '5': '#422217', # Frozen dirt shadow
    '6': '#29140D', # Deep frozen crevice
    '7': '#BBE4F7', # Ice glint
}
permafrost_grid = [
    "3445170124563445",
    "4551701245634551",
    "5617012344545617",
    "6101234556556101",
    "1712456344541712",
    "0124563455650124",
    "1234554563451234",
    "2345634454562345",
    "3456345565633456",
    "4563445170124563",
    "5634551701245634",
    "6344517012456344",
    "3455101245633455",
    "4561712344544561",
    "5610124556555610",
    "6171245634456171"
]

# ------------------------------------------------------------------------------
# Block 6: martian_basalt
# Pass 1: Rich volcanic basalt palette (dark charcoal with warm purple-ferric undertone)
# Pass 2: Columnar cooling joints, fine mineral grain clusters
# Pass 3: Specular mineral flecks (olivine/pyroxene micro-crystals)
# ------------------------------------------------------------------------------
basalt_palette = {
    '0': '#5E5456', # Highlight grain
    '1': '#473F42', # Light midtone
    '2': '#362E31', # Core basalt
    '3': '#261F22', # Shadow basalt
    '4': '#181315', # Deep crevice
    '5': '#726769', # Feldspar/pyroxene glint
}
basalt_grid = [
    "1221050123321221",
    "2332101234432332",
    "3443212344323443",
    "2332101233212332",
    "1221050122101221",
    "2332101233212332",
    "3443212344323443",
    "2332101233212332",
    "1221050122101221",
    "2332101233212332",
    "3443212344323443",
    "2332101233212332",
    "1221050122101221",
    "2332101233212332",
    "3443212344323443",
    "2332101233212332"
]

# ------------------------------------------------------------------------------
# Block 7: polished_martian_basalt
# Pass 1: High-contrast polished stone with dark glossy reflections
# Pass 2: Clean chamfered tile border with light source from top-left
# Pass 3: Subtle diagonal specular sheen and glossy mirror reflections
# ------------------------------------------------------------------------------
polished_basalt_palette = {
    '0': '#7D7275', # Bevel top highlight
    '1': '#665B5E', # Bevel left highlight
    '2': '#4E4447', # Polished face light
    '3': '#3D3437', # Polished face base
    '4': '#2D2527', # Polished face shadow
    '5': '#1C1618', # Bevel bottom/right shadow
    '6': '#918689', # Specular mirror sheen
}
polished_basalt_grid = [
    "0000000000000001",
    "1622222222222235",
    "1262222222222345",
    "1226222222223445",
    "1222622222234445",
    "1222262222344445",
    "1222226223444445",
    "1222222634444445",
    "1222222364444445",
    "1222223446444445",
    "1222234444644445",
    "1222344444464445",
    "1223444444446445",
    "1234444444444645",
    "1344444444444465",
    "5555555555555555"
]

# ------------------------------------------------------------------------------
# Block 8: martian_basalt_bricks
# Pass 1: Architectural masonry with warm-tinted dark volcanic mortar
# Pass 2: 2x2 brick layout with staggered bond and stone texture variation
# Pass 3: Chiseled brick edges, corner chips, and ambient shadow cavities
# ------------------------------------------------------------------------------
basalt_bricks_palette = {
    '0': '#6B6063', # Brick top highlight
    '1': '#544A4D', # Brick face light
    '2': '#3E3538', # Brick face midtone
    '3': '#2C2427', # Brick face shadow
    '4': '#161113', # Mortar / deep cavity
    '5': '#7F7477', # Chiseled edge glint
}
basalt_bricks_grid = [
    "0000000400000004",
    "0512233405112234",
    "1122333411223334",
    "2233333422333334",
    "3333333433333334",
    "3333333433333334",
    "4444444444444444",
    "0004000000040004",
    "0534051122340534",
    "1234112233341234",
    "2334223333342334",
    "3334333333343334",
    "3334333333343334",
    "4444444444444444",
    "0000000400000004",
    "3333333433333334"
]

# ------------------------------------------------------------------------------
# Block 9: martian_basalt_tiles
# Pass 1: Fine geometric square floor tiles
# Pass 2: 4-tile grid (8x8 each) with clean cross-joints
# Pass 3: Subtle diagonal brushed polish and corner highlights
# ------------------------------------------------------------------------------
basalt_tiles_palette = {
    '0': '#72676A', # Tile top highlight
    '1': '#594F52', # Tile light
    '2': '#42393C', # Tile mid
    '3': '#2F2629', # Tile shadow
    '4': '#171214', # Grout line
    '5': '#887C80', # Specular corner
}
basalt_tiles_grid = [
    "0000000400000004",
    "0511223405112234",
    "0122233401222334",
    "0122333401223334",
    "0223333402233334",
    "0233333402333334",
    "0333333403333334",
    "4444444444444444",
    "0000000400000004",
    "0511223405112234",
    "0122233401222334",
    "0122333401223334",
    "0223333402233334",
    "0233333402333334",
    "0333333403333334",
    "4444444444444444"
]

# ------------------------------------------------------------------------------
# Block 10: stratified_martian_stone
# Pass 1: Striated sedimentary mineral layers (hematite ochre, ironstone, basalt)
# Pass 2: Distinct horizontal geological bands with undulating natural fault lines
# Pass 3: Weathering cracks, dust deposits, and mineral seams
# ------------------------------------------------------------------------------
stratified_palette = {
    '0': '#DE8A52', # Ochre sandstone highlight
    '1': '#C46F38', # Ochre sandstone mid
    '2': '#9E4E24', # Rust clay layer
    '3': '#783517', # Ironstone layer
    '4': '#542614', # Dark shale band
    '5': '#402422', # Basaltic sedimentary band
    '6': '#2E1918', # Deep fault crevice
    '7': '#EDB07E', # Fine gypsum seam
}
stratified_grid = [
    "0010701100100701",
    "1121112211211122",
    "2232233322322333",
    "3343344433433444",
    "4464466644644666",
    "5555555555555555",
    "0100701101007011",
    "1211112212111122",
    "2322233323222333",
    "3433344434333444",
    "4644466646444666",
    "0070100100701001",
    "1111211211112112",
    "2223322322233223",
    "3344433433444334",
    "5556655655566556"
]

# ------------------------------------------------------------------------------
# Block 11: martian_volcanic_scoria
# Pass 1: Dark purplish-charcoal volcanic slag palette with oxidized ferric rims
# Pass 2: Dense bubbly vesicles (gas pockets) with organic porous depth
# Pass 3: Sharp vesicular cavity rims and glassy highlights
# ------------------------------------------------------------------------------
scoria_palette = {
    '0': '#735E62', # Highlight scoria rim
    '1': '#574347', # Light volcanic matrix
    '2': '#403033', # Core matrix
    '3': '#2D2022', # Cavity shadow
    '4': '#170E10', # Deep gas bubble void
    '5': '#8A4836', # Oxidized rust vesicle edge
    '6': '#8F7A7F', # Glassy basalt reflection
}
scoria_grid = [
    "1206124432120612",
    "2044314443204431",
    "0444432321044443",
    "6444321051644432",
    "1343205443134320",
    "2120644444212064",
    "3204444443320444",
    "2044443212204444",
    "0444321206044432",
    "6443210544644321",
    "1321054443132105",
    "2106444432210644",
    "0544444321054444",
    "5444432120544443",
    "4443212061444321",
    "4321206123432120"
]

# ------------------------------------------------------------------------------
# Block 12: martian_impact_breccia
# Pass 1: Angular rock clasts (shocked quartz, basalt, ironstone) in melted matrix
# Pass 2: Distinct fractured polygonal clasts embedded at various angles
# Pass 3: Bright shocked crystal facets, impact melt glass veins
# ------------------------------------------------------------------------------
breccia_palette = {
    '0': '#D48657', # Shocked sandstone clast light
    '1': '#A65529', # Sandstone clast shadow
    '2': '#82777A', # Basalt clast light
    '3': '#52484B', # Basalt clast shadow
    '4': '#453538', # Melt matrix light
    '5': '#2E2224', # Melt matrix core
    '6': '#1A1113', # Matrix cavity / impact fracture
    '7': '#E6B08E', # Shocked quartz vein glint
}
breccia_grid = [
    "5422735654007156",
    "4222333540001115",
    "2223336600011166",
    "3333665411116654",
    "5665427356542735",
    "6540713356407133",
    "5400113654001136",
    "4001166540011665",
    "2711665427116654",
    "3366542733665427",
    "6554071365540713",
    "5400113354001133",
    "4001133640011336",
    "0113336501133365",
    "1333665413336654",
    "3366545533665455"
]

# ------------------------------------------------------------------------------
# Block 13: hematite_ore
# Pass 1: Dark basalt host rock + metallic ferric hematite spherules
# Pass 2: Spherical "blueberry" nodule clusters embedded in the stone
# Pass 3: Concentric metallic highlights and rich specular reflections
# ------------------------------------------------------------------------------
hematite_ore_palette = {
    'B0': '#5E5456', # Basalt highlight
    'B1': '#473F42', # Basalt light
    'B2': '#362E31', # Basalt base
    'B3': '#261F22', # Basalt shadow
    'B4': '#181315', # Basalt deep
    'H0': '#96969E', # Metallic hematite specular
    'H1': '#686673', # Hematite bright
    'H2': '#454252', # Hematite core
    'H3': '#2C2A38', # Hematite shadow
    'H4': '#8A3222', # Oxidation rim
}
hematite_ore_grid = [
    ["B1","B2","B2","B1","B0","B1","B2","B3","B3","B2","B1","B2","B2","B1","B0","B1"],
    ["B2","H0","H1","H4","B1","B0","H0","H1","H4","B2","B3","B4","B4","B3","B2","B3"],
    ["B3","H1","H2","H3","B2","B1","H1","H2","H3","B3","B4","B4","B3","B2","B3","B4"],
    ["B2","H4","H3","H3","B1","B0","H4","H3","H3","B2","B3","B3","B2","B1","B2","B3"],
    ["B1","B2","B2","B1","B0","B1","B2","B2","B1","B0","B1","B2","H0","H1","H4","B1"],
    ["B2","B3","B3","B2","B1","B0","B1","B2","B3","B3","B2","B1","H1","H2","H3","B2"],
    ["B3","B4","B4","B3","H0","H1","H4","B3","B4","B4","B3","B2","H4","H3","H3","B3"],
    ["B2","B3","B3","B2","H1","H2","H3","B2","B3","B3","B2","B1","B2","B3","B3","B2"],
    ["B1","B2","B2","B1","H4","H3","H3","B1","B2","B2","B1","B0","B1","B2","B2","B1"],
    ["B2","B3","B3","B2","B1","B0","B1","B2","H0","H1","H4","B1","B2","B3","B3","B2"],
    ["B3","H0","H1","H4","B2","B1","B2","B3","H1","H2","H3","B2","B3","B4","B4","B3"],
    ["B2","H1","H2","H3","B1","B0","B1","B2","H4","H3","H3","B1","B2","B3","B3","B2"],
    ["B1","H4","H3","H3","B0","B1","B2","B2","B1","B0","B1","B2","B2","B1","B0","B1"],
    ["B2","B3","B3","B2","B1","B0","B1","B2","B3","B3","B2","B1","B2","B3","B3","B2"],
    ["B3","B4","B4","B3","B2","B1","B2","B3","B4","B4","B3","B2","B3","B4","B4","B3"],
    ["B2","B3","B3","B2","B1","B0","B1","B2","B3","B3","B2","B1","B2","B3","B3","B2"]
]

# ------------------------------------------------------------------------------
# Block 14: meteoric_iron_ore
# Pass 1: Basalt host + bright nickel-iron metallic chunks with Widmanstätten sheen
# Pass 2: Heavy angular metallic deposits embedded in fractured host rock
# Pass 3: High-contrast steel specular highlights and dark fusion crust border
# ------------------------------------------------------------------------------
meteoric_ore_palette = {
    'B0': '#5E5456', 'B1': '#473F42', 'B2': '#362E31', 'B3': '#261F22', 'B4': '#181315',
    'M0': '#FFFFFF', # Brilliant specular
    'M1': '#E0E3EB', # Bright steel
    'M2': '#A9B0BF', # Steel midtone
    'M3': '#6D7585', # Shadow steel
    'M4': '#3D424F', # Deep nickel shadow
    'M5': '#241D26', # Fusion crust
}
meteoric_ore_grid = [
    ["B1","B2","B2","B1","B0","B1","B2","B3","B3","B2","B1","B2","B2","B1","B0","B1"],
    ["B2","B3","M5","M5","M5","B0","B1","B2","B3","B4","M5","M5","M5","B3","B2","B3"],
    ["B3","M5","M0","M1","M2","M5","B2","B3","B4","M5","M0","M1","M2","M5","B3","B4"],
    ["B2","M5","M1","M2","M3","M5","B1","B0","B1","M5","M1","M2","M3","M5","B2","B3"],
    ["B1","M5","M2","M3","M4","M5","B2","B2","B1","M5","M2","M3","M4","M5","B0","B1"],
    ["B2","B3","M5","M5","M5","B1","B0","B1","B2","B3","M5","M5","M5","B1","B2","B3"],
    ["B3","B4","B4","B3","B2","B1","M5","M5","M5","B4","B4","B3","B2","B3","B4","B4"],
    ["B2","B3","B3","B2","B1","M5","M0","M1","M2","M5","B3","B2","B1","B2","B3","B3"],
    ["B1","B2","B2","B1","B0","M5","M1","M2","M3","M5","B2","B1","B0","B1","B2","B2"],
    ["B2","B3","B3","B2","B1","M5","M2","M3","M4","M5","B3","B2","B1","B2","B3","B3"],
    ["B3","B4","B4","B3","B2","B1","M5","M5","M5","B2","B3","B4","B4","B3","B2","B3"],
    ["B2","M5","M5","M5","B0","B1","B2","B3","B2","B1","B2","B3","B3","B2","B1","B2"],
    ["B1","M5","M0","M1","M5","B2","B2","B1","B0","B1","B2","B2","B1","B0","B1","B2"],
    ["B2","M5","M1","M2","M5","B1","B0","B1","B2","B3","B3","B2","B1","B2","B3","B3"],
    ["B3","B2","M5","M5","B2","B1","B2","B3","B4","B4","B3","B2","B3","B4","B4","B3"],
    ["B2","B3","B3","B2","B1","B0","B1","B2","B3","B3","B2","B1","B2","B3","B3","B2"]
]

# ------------------------------------------------------------------------------
# Block 15: martian_copper_ore
# Pass 1: Basalt host + raw native copper & green malachite/cuprite patina
# Pass 2: Branching dendrites of metallic copper with oxidized margins
# Pass 3: Specular bronze-gold highlights and vibrant turquoise accents
# ------------------------------------------------------------------------------
copper_ore_palette = {
    'B0': '#5E5456', 'B1': '#473F42', 'B2': '#362E31', 'B3': '#261F22', 'B4': '#181315',
    'C0': '#FFA87A', # Bright copper specular
    'C1': '#E07348', # Raw copper light
    'C2': '#B84F28', # Raw copper midtone
    'C3': '#7D2E13', # Raw copper shadow
    'P0': '#6FE0B8', # Bright malachite patina
    'P1': '#3C9E7D', # Dark malachite patina
}
copper_ore_grid = [
    ["B1","B2","B2","B1","B0","B1","B2","B3","B3","B2","B1","B2","B2","B1","B0","B1"],
    ["B2","P1","P0","C0","B1","B0","B1","B2","B3","B4","B4","B3","B2","B3","B2","B3"],
    ["B3","P0","C0","C1","C2","B2","B1","B2","B3","B4","B4","B3","P1","P0","B3","B4"],
    ["B2","C0","C1","C2","C3","P1","B0","B1","B2","B3","B3","P0","C0","C1","C2","B3"],
    ["B1","B2","C2","C3","P1","P0","C0","B2","B1","B0","B1","C0","C1","C2","C3","B1"],
    ["B2","B3","B1","P1","P0","C0","C1","C2","B3","B3","B2","C1","C2","C3","P1","B2"],
    ["B3","B4","B4","B3","C0","C1","C2","C3","B4","B4","B3","B2","P1","P0","B3","B3"],
    ["B2","B3","B3","B2","B1","C2","C3","P1","B3","B2","B1","B2","B3","B3","B2","B2"],
    ["B1","B2","B2","B1","B0","B1","P1","B1","B2","B2","B1","B0","B1","B2","B2","B1"],
    ["B2","B3","B3","B2","P1","P0","C0","B1","B2","B3","B3","B2","B1","B2","B3","B2"],
    ["B3","B4","B4","P0","C0","C1","C2","B3","B4","B4","B3","B2","B3","B4","B4","B3"],
    ["B2","B3","P1","C0","C1","C2","C3","B2","B3","B3","B2","B1","B2","B3","B3","B2"],
    ["B1","B2","B2","C1","C2","C3","P1","B1","B0","B1","B2","B2","B1","B0","B1","B2"],
    ["B2","B3","B3","B2","P1","P0","B1","B2","B3","B3","B2","B1","B2","B3","B3","B2"],
    ["B3","B4","B4","B3","B2","B1","B2","B3","B4","B4","B3","B2","B3","B4","B4","B3"],
    ["B2","B3","B3","B2","B1","B0","B1","B2","B3","B3","B2","B1","B2","B3","B3","B2"]
]

# ------------------------------------------------------------------------------
# Block 16: martian_sulfur_ore
# Pass 1: Basalt host + radiant crystalline native sulfur veins
# Pass 2: Prismatic crystal facets and yellow-amber deposit pockets
# Pass 3: Specular bright canary-yellow crystal highlights and amber shading
# ------------------------------------------------------------------------------
sulfur_ore_palette = {
    'B0': '#5E5456', 'B1': '#473F42', 'B2': '#362E31', 'B3': '#261F22', 'B4': '#181315',
    'S0': '#FFFF8A', # Prismatic bright specular
    'S1': '#F2DE38', # Sulfur light
    'S2': '#C7A81A', # Sulfur midtone
    'S3': '#8F6F0B', # Sulfur amber shadow
    'S4': '#5E4605', # Deep crystal crevice
}
sulfur_ore_grid = [
    ["B1","B2","B2","B1","B0","B1","B2","B3","B3","B2","B1","B2","B2","B1","B0","B1"],
    ["B2","S0","S1","B1","B0","B1","B2","B3","B4","B4","S0","S1","B3","B2","B3","B2"],
    ["B3","S1","S2","S3","B2","B1","B2","B3","B4","S0","S1","S2","S3","B3","B4","B3"],
    ["B2","B1","S2","S3","S4","B0","B1","B2","B3","S1","S2","S3","S4","B2","B3","B2"],
    ["B1","B2","B3","S3","S4","B2","B1","B0","B1","B2","S3","S4","B1","B0","B1","B2"],
    ["B2","B3","B3","B2","B1","B0","S0","S1","B3","B3","B2","B1","B2","B3","B3","B2"],
    ["B3","B4","B4","B3","B2","S0","S1","S2","S3","B4","B3","B2","B3","B4","B4","B3"],
    ["B2","B3","B3","B2","B1","S1","S2","S3","S4","B3","B2","B1","B2","B3","B3","B2"],
    ["B1","B2","B2","B1","B0","B2","S3","S4","B1","B0","B1","B2","B2","B1","B0","B1"],
    ["B2","B3","B3","B2","B1","B0","B1","B2","B3","B3","B2","B1","B2","B3","B3","B2"],
    ["B3","B4","S0","S1","B2","B1","B2","B3","S0","S1","B3","B2","B3","B4","B4","B3"],
    ["B2","S0","S1","S2","S3","B1","B2","S0","S1","S2","S3","B1","B2","B3","B3","B2"],
    ["B1","S1","S2","S3","S4","B0","B1","S1","S2","S3","S4","B0","B1","B2","B2","B1"],
    ["B2","B3","S3","S4","B1","B0","B1","B2","S3","S4","B2","B1","B2","B3","B3","B2"],
    ["B3","B4","B4","B3","B2","B1","B2","B3","B4","B4","B3","B2","B3","B4","B4","B3"],
    ["B2","B3","B3","B2","B1","B0","B1","B2","B3","B3","B2","B1","B2","B3","B3","B2"]
]

# ------------------------------------------------------------------------------
# Block 17: martian_olivine_ore
# Pass 1: Basalt host + radiant emerald/peridot-green olivine crystals
# Pass 2: Orthorhombic crystal clusters embedded in dark host rock
# Pass 3: High-refraction luminous green gleam and deep olive shadows
# ------------------------------------------------------------------------------
olivine_ore_palette = {
    'B0': '#5E5456', 'B1': '#473F42', 'B2': '#362E31', 'B3': '#261F22', 'B4': '#181315',
    'O0': '#E2FF85', # Brilliant peridot specular
    'O1': '#9EDE2F', # Vivid olive green
    'O2': '#6CA614', # Rich olivine core
    'O3': '#42700A', # Deep forest olive
    'O4': '#224003', # Crystal cavity shadow
}
olivine_ore_grid = [
    ["B1","B2","B2","B1","B0","B1","B2","B3","B3","B2","B1","B2","B2","B1","B0","B1"],
    ["B2","O0","O1","B1","B0","B1","B2","B3","B4","B4","O0","O1","B3","B2","B3","B2"],
    ["B3","O1","O2","O3","B2","B1","B2","B3","B4","O0","O1","O2","O3","B3","B4","B3"],
    ["B2","B1","O2","O3","O4","B0","B1","B2","B3","O1","O2","O3","O4","B2","B3","B2"],
    ["B1","B2","B3","O3","O4","B2","B1","B0","B1","B2","O3","O4","B1","B0","B1","B2"],
    ["B2","B3","B3","B2","B1","B0","O0","O1","B3","B3","B2","B1","B2","B3","B3","B2"],
    ["B3","B4","B4","B3","B2","O0","O1","O2","O3","B4","B3","B2","B3","B4","B4","B3"],
    ["B2","B3","B3","B2","B1","O1","O2","O3","O4","B3","B2","B1","B2","B3","B3","B2"],
    ["B1","B2","B2","B1","B0","B2","O3","O4","B1","B0","B1","B2","B2","B1","B0","B1"],
    ["B2","B3","B3","B2","B1","B0","B1","B2","B3","B3","B2","B1","B2","B3","B3","B2"],
    ["B3","B4","O0","O1","B2","B1","B2","B3","O0","O1","B3","B2","B3","B4","B4","B3"],
    ["B2","O0","O1","O2","O3","B1","B2","O0","O1","O2","O3","B1","B2","B3","B3","B2"],
    ["B1","O1","O2","O3","O4","B0","B1","O1","O2","O3","O4","B0","B1","B2","B2","B1"],
    ["B2","B3","O3","O4","B1","B0","B1","B2","O3","O4","B2","B1","B2","B3","B3","B2"],
    ["B3","B4","B4","B3","B2","B1","B2","B3","B4","B4","B3","B2","B3","B4","B4","B3"],
    ["B2","B3","B3","B2","B1","B0","B1","B2","B3","B3","B2","B1","B2","B3","B3","B2"]
]

# ------------------------------------------------------------------------------
# Block 18: martian_ice
# Pass 1: Ancient glacial water ice with ferric dust infusion and crystalline depth
# Pass 2: Internal polygonal fracture planes, trapped micro-bubbles
# Pass 3: Translucent surface reflections, subtle glacial turquoise refraction
# ------------------------------------------------------------------------------
martian_ice_palette = {
    '0': '#E8FAFF', # Surface glint
    '1': '#BFE7F7', # Light glacial cyan
    '2': '#88C4DE', # Mid glacial ice
    '3': '#5A9BBF', # Deep ice body
    '4': '#3E7799', # Deep fracture shadow
    '5': '#C4947C', # Ancient ferric dust streak
    '6': '#A86C52', # Dark ferric dust streak
    '7': '#FFFFFF', # Specular fracture edge
}
martian_ice_grid = [
    "7011234321701123",
    "0123432112012342",
    "1234321012123431",
    "2343210701234321",
    "3432155621343210",
    "4321556632432107",
    "3215566321321070",
    "2105663212210701",
    "1070632112107012",
    "0701234321070123",
    "7012343211701234",
    "0123432107012343",
    "1234321070123432",
    "2343210701234321",
    "3432107012343210",
    "4321070123432107"
]

# ------------------------------------------------------------------------------
# Block 19: dry_ice_block
# Pass 1: Solid carbon dioxide (CO2) dry ice with frosty white-cyan facets
# Pass 2: Microcrystalline columnar structures and sublimating rime edges
# Pass 3: High-brightness frost sparkle and cold atmospheric translucency
# ------------------------------------------------------------------------------
dry_ice_palette = {
    '0': '#FFFFFF', # Pure dry ice white
    '1': '#E0F7FF', # Frosty surface light
    '2': '#B8E8F8', # Translucent cold rime
    '3': '#88D0EB', # Ice shadow
    '4': '#5CAFCF', # Deep crystal depth
    '5': '#F2FBFF', # Sublimation vapor glint
}
dry_ice_grid = [
    "0511234321051123",
    "5012343211501234",
    "1123432105112343",
    "1234321051123432",
    "2343210511234321",
    "3432105112343210",
    "4321051123432105",
    "3210511234321051",
    "2105112343210512",
    "1051123432105123",
    "0511234321051123",
    "5012343211501234",
    "1123432105112343",
    "1234321051123432",
    "2343210511234321",
    "3432105112343210"
]

# ==============================================================================
# 2. ITEMS (8 Textures)
# ==============================================================================

# ------------------------------------------------------------------------------
# Item 1: hematite_nodule
# Pass 1: Concentric spherical ferric nodule ("Martian Blueberry")
# Pass 2: High-contrast spherical shading with 1-pixel dark outline
# Pass 3: Specular metallic gleam and warm oxide rim
# ------------------------------------------------------------------------------
hematite_nodule_palette = {
    '.': (0,0,0,0),
    '#': '#1A1822', # Dark item outline
    '0': '#B8B8C4', # Specular gleam
    '1': '#8A8799', # Metallic surface
    '2': '#5E5B6E', # Core metallic body
    '3': '#3D3B4A', # Deep sphere shadow
    '4': '#8A3626', # Ferric oxide rim
}
hematite_nodule_grid = [
    "................",
    "................",
    "......####......",
    "....##0011##....",
    "...#00011122#...",
    "..#0001112223#..",
    "..#0111122233#..",
    ".#111122233344#.",
    ".#111222333444#.",
    ".#122223334444#.",
    "..#2223334444#..",
    "..#233334444#...",
    "...#3344444#....",
    "....##444##.....",
    "......###.......",
    "................"
]

# ------------------------------------------------------------------------------
# Item 2: raw_meteoric_iron
# Pass 1: Heavy extraterrestrial iron nugget with fusion crust
# Pass 2: Angular regmaglypts (thumbprint ablation craters)
# Pass 3: Bright nickel-iron specular glints and dark crust edges
# ------------------------------------------------------------------------------
raw_iron_palette = {
    '.': (0,0,0,0),
    '#': '#1B1721', # Dark outline
    '0': '#FFFFFF', # Brilliant specular
    '1': '#DCE0EB', # Bright nickel-steel
    '2': '#A2A9BA', # Steel midtone
    '3': '#697082', # Shadow steel
    '4': '#3D4352', # Dark fusion crust
}
raw_iron_grid = [
    "................",
    ".....####.......",
    "...##0011##.....",
    "..#00011222##...",
    ".#00112222333#..",
    ".#112200123344#.",
    "#1222001123444#.",
    "#1221112234433#.",
    "#2233222334334#.",
    ".#23333344434#..",
    ".#3444444334#...",
    "..#44444334#....",
    "...##4433##.....",
    ".....####.......",
    "................",
    "................"
]

# ------------------------------------------------------------------------------
# Item 3: meteoric_iron_ingot
# Pass 1: Clean forged metallic ingot with classic Minecraft isometric bevels
# Pass 2: High-contrast metallic luster with smooth gradient
# Pass 3: Crisp corner highlights and polished mirror edge
# ------------------------------------------------------------------------------
meteoric_ingot_palette = {
    '.': (0,0,0,0),
    '#': '#1E202B', # Outline
    '0': '#FFFFFF', # Specular gleam
    '1': '#E4E8F2', # Ingot top highlight
    '2': '#B8BFD1', # Ingot face light
    '3': '#838D9E', # Ingot face midtone
    '4': '#565E6E', # Ingot face shadow
    '5': '#383E4C', # Deep bevel shadow
}
meteoric_ingot_grid = [
    "................",
    "................",
    "....########....",
    "...#00111111#...",
    "..#0011122233#..",
    ".#001122223344#.",
    ".#112222333445#.",
    ".#122233344455#.",
    ".#223334444555#.",
    ".#233444455555#.",
    "..#344455555#...",
    "...#4555555#....",
    "....########....",
    "................",
    "................",
    "................"
]

# ------------------------------------------------------------------------------
# Item 4: raw_martian_copper
# Pass 1: Raw native copper nugget with verdigris patina accents
# Pass 2: Chunk-like organic silhouette with crystalline metallic facets
# Pass 3: Polished bronze-orange gleam and turquoise cuprite highlights
# ------------------------------------------------------------------------------
raw_copper_palette = {
    '.': (0,0,0,0),
    '#': '#2B120A', # Outline
    '0': '#FFA87A', # Copper specular
    '1': '#E07348', # Bright copper
    '2': '#B84F28', # Copper core
    '3': '#7D2E13', # Copper shadow
    '4': '#4A1707', # Deep shadow
    'P': '#6FE0B8', # Turquoise malachite
}
raw_copper_grid = [
    "................",
    "....####........",
    "..##0011##..##..",
    ".#00011222##PP#.",
    "#0011222P11PPP#.",
    "#1122001122P33#.",
    "#1220011223334#.",
    ".#22112233334#..",
    ".#2332233444#...",
    "..#33334444#....",
    "...##3444##.....",
    ".....####.......",
    "................",
    "................",
    "................",
    "................"
]

# ------------------------------------------------------------------------------
# Item 5: sulfur_dust
# Pass 1: Fine crystalline brimstone powder with golden glow
# Pass 2: Piled powder form with organic crystal granule scattering
# Pass 3: Specular yellow crystal facets and rich amber base
# ------------------------------------------------------------------------------
sulfur_dust_palette = {
    '.': (0,0,0,0),
    '#': '#382802', # Outline
    '0': '#FFFF8A', # Specular crystal
    '1': '#F5E038', # Bright sulfur
    '2': '#C9AA16', # Sulfur midtone
    '3': '#94750B', # Amber shadow
    '4': '#5E4804', # Deep shadow
}
sulfur_dust_grid = [
    "................",
    "................",
    "......##........",
    "....##00##......",
    "...#001100##....",
    "..#001111112#...",
    ".#01100112223#..",
    "#0110011222234#.",
    "#1111122223334#.",
    "#1222223333444#.",
    ".#22333334444#..",
    "..#334444444#...",
    "...#########....",
    "................",
    "................",
    "................"
]

# ------------------------------------------------------------------------------
# Item 6: olivine_gem
# Pass 1: Faceted brilliant-cut peridot/olivine crystal gem
# Pass 2: Symmetrical orthorhombic gemstone geometry with crisp facet edges
# Pass 3: Vibrant light refraction, lime-green highlights, deep emerald base
# ------------------------------------------------------------------------------
olivine_gem_palette = {
    '.': (0,0,0,0),
    '#': '#112102', # Gem outline
    '0': '#F0FFB0', # Pure refraction gleam
    '1': '#B0F038', # Light lime facet
    '2': '#78C414', # Core green
    '3': '#4E8A0A', # Shadow green
    '4': '#2D5704', # Deep emerald facet
}
olivine_gem_grid = [
    "................",
    "......####......",
    "....##0011##....",
    "...#00011122#...",
    "..#0001112223#..",
    ".#001111222334#.",
    ".#011122223344#.",
    ".#112222333444#.",
    ".#122233344444#.",
    "..#2233344444#..",
    "..#233444444#...",
    "...#3444444#....",
    "....##4444##....",
    "......####......",
    "................",
    "................"
]

# ------------------------------------------------------------------------------
# Item 7: dry_ice_shard
# Pass 1: Prismatic shard of solid carbon dioxide
# Pass 2: Sharp crystalline dagger silhouette with frosted facet planes
# Pass 3: Glowing sublimation vapor glints and bright frosty highlights
# ------------------------------------------------------------------------------
dry_ice_shard_palette = {
    '.': (0,0,0,0),
    '#': '#2B586B', # Cold outline
    '0': '#FFFFFF', # Vapor glint
    '1': '#E0F8FF', # Bright frost
    '2': '#ADE2F5', # Ice body
    '3': '#6EBED9', # Ice shadow
    '4': '#4092AD', # Deep ice
}
dry_ice_shard_grid = [
    "................",
    ".........##.....",
    "........#00#....",
    ".......#0011#...",
    "......#00112#...",
    ".....#011223#...",
    "....#0112234#...",
    "...#01122344#...",
    "..#01122344#....",
    ".#01122344#.....",
    ".#1122344#......",
    "..#22344#.......",
    "...#344#........",
    "....##4#........",
    "......##........",
    "................"
]

# ------------------------------------------------------------------------------
# Item 8: martian_rock_sample
# Pass 1: Scientific planetary rock specimen with stratified mineral banding
# Pass 2: Chiseled geologist hand sample with crystalline inclusions
# Pass 3: Detailed diagnostic cross-section with mineral glints
# ------------------------------------------------------------------------------
rock_sample_palette = {
    '.': (0,0,0,0),
    '#': '#1E1410', # Outline
    '0': '#DE8A52', # Sandstone top
    '1': '#B85526', # Ironstone mid
    '2': '#783517', # Dark shale
    '3': '#5E5456', # Basalt highlight
    '4': '#362E31', # Basalt base
    '5': '#181315', # Basalt shadow
    'G': '#A2F250', # Olivine grain glint
    'S': '#FFFFFF', # Steel grain glint
}
rock_sample_grid = [
    "................",
    "......####......",
    "....##0000##....",
    "...#00000000#...",
    "..#0000000011#..",
    ".#000000011112#.",
    ".#111111111222#.",
    ".#11G111122222#.",
    ".#222222222233#.",
    ".#333333333344#.",
    "..#33S3334444#..",
    "..#444444445#...",
    "...#4444455#....",
    "....##555##.....",
    "......###.......",
    "................"
]

def main():
    base_block_dir = "src/main/resources/assets/alyrioncore/textures/block"
    base_item_dir = "src/main/resources/assets/alyrioncore/textures/item"

    # Blocks
    blocks = {
        'martian_sand': (sand_grid, sand_palette),
        'martian_regolith': (regolith_grid, regolith_palette),
        'coarse_martian_regolith': (coarse_regolith_grid, coarse_regolith_palette),
        'frost_dusted_regolith': (frost_regolith_grid, frost_regolith_palette),
        'martian_permafrost': (permafrost_grid, permafrost_palette),
        'martian_basalt': (basalt_grid, basalt_palette),
        'polished_martian_basalt': (polished_basalt_grid, polished_basalt_palette),
        'martian_basalt_bricks': (basalt_bricks_grid, basalt_bricks_palette),
        'martian_basalt_tiles': (basalt_tiles_grid, basalt_tiles_palette),
        'stratified_martian_stone': (stratified_grid, stratified_palette),
        'martian_volcanic_scoria': (scoria_grid, scoria_palette),
        'martian_impact_breccia': (breccia_grid, breccia_palette),
        'hematite_ore': (hematite_ore_grid, hematite_ore_palette),
        'meteoric_iron_ore': (meteoric_ore_grid, meteoric_ore_palette),
        'martian_copper_ore': (copper_ore_grid, copper_ore_palette),
        'martian_sulfur_ore': (sulfur_ore_grid, sulfur_ore_palette),
        'martian_olivine_ore': (olivine_ore_grid, olivine_ore_palette),
        'martian_ice': (martian_ice_grid, martian_ice_palette),
        'dry_ice_block': (dry_ice_grid, dry_ice_palette),
    }

    for name, (grid, palette) in blocks.items():
        path = os.path.join(base_block_dir, f"{name}.png")
        create_image(grid, palette, path)

    # Items
    items = {
        'hematite_nodule': (hematite_nodule_grid, hematite_nodule_palette),
        'raw_meteoric_iron': (raw_iron_grid, raw_iron_palette),
        'meteoric_iron_ingot': (meteoric_ingot_grid, meteoric_ingot_palette),
        'raw_martian_copper': (raw_copper_grid, raw_copper_palette),
        'sulfur_dust': (sulfur_dust_grid, sulfur_dust_palette),
        'olivine_gem': (olivine_gem_grid, olivine_gem_palette),
        'dry_ice_shard': (dry_ice_shard_grid, dry_ice_shard_palette),
        'martian_rock_sample': (rock_sample_grid, rock_sample_palette),
    }

    for name, (grid, palette) in items.items():
        path = os.path.join(base_item_dir, f"{name}.png")
        create_image(grid, palette, path)

if __name__ == '__main__':
    main()
