#!/usr/bin/env python3
"""generate_meteoric_toolset.py — regenerate the AlyrionCore meteoric iron set.

Redesign identity (v2):
  * Meteoric nickel-iron: teal-slate steel ramp (dark cosmic body, icy specular)
  * Dark fusion-crust outlines on every item (1px, reads as a space-weathered rim)
  * Carbonized meteorite-rod handles (charcoal with a faint violet cast)
  * Small glowing teal starfall-crystal inclusions set into blades/heads

Every tool and the ingot reuse the exact vanilla silhouette AND value structure
from the 1.21 masters (iron sword/pickaxe/axe/shovel/hoe/ingot) — same object,
same angle, same proportions (Jappa checklist) — recolored to the meteoric ramp
and given crystal inclusions. The raw block / iron block are procedural and
tile (toroidal nodule placement / wrapped milled bands). The ore reuses the
pack's proven embedded-chunk layout (basalt host + fusion-crusted nickel-iron
chunks) recolored to the new palette.

Pure stdlib; imports mcutil from mc-scripts/. Writes PNGs directly into
src/main/resources/assets/alyrioncore/textures/{block,item}/.

Run:  python3 generate_meteoric_toolset.py
"""

import os
import random
import sys

ROOT = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(ROOT, 'mc-scripts'))
import mcutil as mc

TEX = os.path.join(ROOT, 'src', 'main', 'resources', 'assets', 'alyrioncore', 'textures')
ITEM_DIR = os.path.join(TEX, 'item')
BLOCK_DIR = os.path.join(TEX, 'block')

# ---------------------------------------------------------------------------
# Meteoric palette
# ---------------------------------------------------------------------------
# Steel (teal-slate nickel-iron), darkest -> lightest:
O0 = '#0e1624'   # fusion crust / outline
S0 = '#1a2c42'   # deep shadow
S1 = '#2b425e'   # shadow
S2 = '#40607e'   # midtone
S3 = '#5d85a4'   # light
S4 = '#8db4cb'   # bright
SP = '#dbeef5'   # specular (icy teal-white)

# Starfall crystal (teal):
K1 = '#15998b'   # crystal dark
K2 = '#2fd4bd'   # crystal bright
KG = '#a8ffe9'   # crystal glint

# Carbonized meteorite rod (handle), darkest -> lightest:
C0 = '#0c0912'
C1 = '#191420'
C2 = '#2b2336'
C3 = '#413553'

# Basalt host rock for the ore (kept identical to the pack's other martian ores):
BASALT = ['#5e5456', '#473f42', '#362e31', '#261f22', '#181315']  # light -> dark

LETTERS = {
    'o': O0, 'a': S0, 'b': S1, 'c': S2, 'd': S3, 'e': S4, 'f': SP,
    '0': C0, '1': C1, '2': C2, '3': C3,
}


def grid_img(rows, overrides=None):
    """Render a 16-row shade map. '.' = transparent; letters -> LETTERS.
    overrides: {(x, y): hex} applied last (crystal inclusions, pits)."""
    img = mc.new_img(16, 16, (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch == '.':
                continue
            img[y][x] = mc.hex2rgb(LETTERS[ch]) + (255,)
    for (x, y), hx in (overrides or {}).items():
        img[y][x] = mc.hex2rgb(hx) + (255,)
    return img


# ---------------------------------------------------------------------------
# Tools — vanilla silhouettes + value structure, meteoric recolour
# ---------------------------------------------------------------------------
SWORD = [
    '.............aaa',
    '............affo',
    '...........afdfo',
    '..........afdfo.',
    '.........afdeo..',
    '........afdeo...',
    '..aa...aedeo....',
    '..aba.aedeo.....',
    '...acoeceo......',
    '...accbeo.......',
    '....abao........',
    '...12oaao.......',
    '..130.ooao......',
    'aa20....oo......',
    'abo.............',
    'ooo.............',
]

PICKAXE = [
    '................',
    '................',
    '......aaaaa.....',
    '.....afeddea12..',
    '......aooodd30..',
    '..........1edo..',
    '.........120deo.',
    '........130.odo.',
    '.......120..odo.',
    '......130...oeo.',
    '.....120....ofo.',
    '....130......o..',
    '...120..........',
    '..130...........',
    '..00............',
    '................',
]

AXE = [
    '................',
    '.........aa.....',
    '........affa....',
    '.......afdea....',
    '......afddd12...',
    '......ofedcd0...',
    '.......oo1dcdo..',
    '........120ddo..',
    '.......130.oo...',
    '......120.......',
    '.....120........',
    '....130.........',
    '...120..........',
    '..130...........',
    '..00............',
    '................',
]

SHOVEL = [
    '................',
    '................',
    '...........aao..',
    '..........affdo.',
    '.........afedfo.',
    '........afedefo.',
    '.........1defo..',
    '........130fo...',
    '.......130.o....',
    '......120.......',
    '.....130........',
    '....120.........',
    '..1130..........',
    '..130...........',
    '...00...........',
    '................',
]

HOE = [
    '................',
    '.......aaa......',
    '......afeea.....',
    '.......oodea12..',
    '.........odd3o..',
    '..........1edo..',
    '.........12oo...',
    '........13o.....',
    '.......12o......',
    '......13o.......',
    '.....12o........',
    '....13o.........',
    '...12o..........',
    '..13o...........',
    '..oo............',
    '................',
]

INGOT = [
    '................',
    '................',
    '..........bb....',
    '.......bbbddc...',
    '....bbbdeeeedc..',
    '.bbbdeeeeeeeedc.',
    'bfeeeeeeeeeeffec',
    'bdfeeeeeefffecda',
    'bddfeefffeccccda',
    'bdddffecccccddda',
    'bcddecccccddcaa.',
    '.bcdecccccaaa...',
    '..bcdcbaaa......',
    '...bbaa.........',
    '................',
    '................',
]


def meteoric_sword():
    img = grid_img(SWORD)
    # starfall crystal inlaid in the blade base
    for (x, y), hx in (((9, 4), K1), ((10, 4), K2), ((9, 5), K2), ((10, 5), KG)):
        img[y][x] = mc.hex2rgb(hx) + (255,)
    return img


def meteoric_pickaxe():
    img = grid_img(PICKAXE)
    # crystal core in the head socket
    for (x, y), hx in (((7, 3), K2), ((8, 3), KG), ((7, 4), K1)):
        img[y][x] = mc.hex2rgb(hx) + (255,)
    # fusion pit on the right head band
    img[4][9] = mc.hex2rgb(S0) + (255,)
    return img


def meteoric_axe():
    img = grid_img(AXE)
    # crystal cluster on the blade face
    for (x, y), hx in (((8, 4), K1), ((9, 4), K2), ((8, 5), KG)):
        img[y][x] = mc.hex2rgb(hx) + (255,)
    # fusion pit on the lower blade
    img[6][10] = mc.hex2rgb(S0) + (255,)
    return img


def meteoric_shovel():
    img = grid_img(SHOVEL)
    # crystal cluster in the scoop
    for (x, y), hx in (((11, 4), K2), ((12, 4), K1), ((11, 5), KG)):
        img[y][x] = mc.hex2rgb(hx) + (255,)
    # fusion pit on the scoop rim
    img[3][13] = mc.hex2rgb(S0) + (255,)
    return img


def meteoric_hoe():
    img = grid_img(HOE)
    # crystal cluster in the hoe head
    for (x, y), hx in (((9, 3), K2), ((9, 4), K1), ((10, 3), KG)):
        img[y][x] = mc.hex2rgb(hx) + (255,)
    # fusion pit beside it
    img[4][10] = mc.hex2rgb(S0) + (255,)
    return img


def meteoric_ingot():
    img = grid_img(INGOT)
    # tiny crystal vein across the ingot top face
    for (x, y), hx in (((9, 6), K1), ((10, 6), K2), ((11, 6), KG)):
        img[y][x] = mc.hex2rgb(hx) + (255,)
    return img


# ---------------------------------------------------------------------------
# Raw meteoric iron (item) — hand-drawn fusion-crusted chunk
# 'o' crust / 'a'..'f' steel / 'K' crystal / '.' empty
# ---------------------------------------------------------------------------
RAW_IRON = [
    '................',
    '................',
    '.....ooooo......',
    '...ooddddo......',
    '..oedddccdbo....',
    '.oeeddcdcdcbbo..',
    '.oeddcKdccdbbbo.',
    'oeddcdcccdcdbbo.',
    'oedcdcKccdcdbo..',
    'oeddccdcdbbboo..',
    '.occdcddbbbooo..',
    '.ocbbbddbbbooo..',
    '..obbdbbboo.....',
    '...ooooooo......',
    '................',
    '................',
]

RAW_IRON_CRATERS = {
    (4, 8): S0, (11, 5): S0, (5, 11): S0, (11, 8): S1,
}

RAW_IRON_CRYSTAL = {
    (8, 6): K2, (8, 8): KG, (6, 7): K1,
}


def meteoric_raw_iron():
    img = mc.new_img(16, 16, (0, 0, 0, 0))
    raw_letters = dict(LETTERS, K=K2, G=KG)
    for y, row in enumerate(RAW_IRON):
        for x, ch in enumerate(row):
            if ch == '.':
                continue
            img[y][x] = mc.hex2rgb(raw_letters[ch]) + (255,)
    for (x, y), hx in RAW_IRON_CRATERS.items():
        img[y][x] = mc.hex2rgb(hx) + (255,)
    for (x, y), hx in RAW_IRON_CRYSTAL.items():
        img[y][x] = mc.hex2rgb(hx) + (255,)
    # bright top-left facet gleam
    img[5][3] = mc.hex2rgb(SP) + (255,)
    img[6][4] = mc.hex2rgb(SP) + (255,)
    return img


# ---------------------------------------------------------------------------
# Blocks (16x16, tileable)
# ---------------------------------------------------------------------------
# Vanilla iron block structure (subtle brushed bands + bevel frame), recoloured
# to the meteoric ramp — polished metal, no plank-like groove lines.
IRON_BLOCK_GRID = [
    '3223333333322223',
    '2aaaaa9998777772',
    '2777777777777771',
    '0555555566644440',
    '2aaaaaa999777771',
    '2777777777777771',
    '0555556666444440',
    '2aa9999977777771',
    '2777777777777771',
    '0555555556664440',
    '2aaaaaa999977771',
    '2777777777777771',
    '0555555666644440',
    '2aaaa99997777771',
    '2777777777777771',
    '2222222112111111',
]
# grid char (0..a) -> meteoric shade: frame S2, dark bands S3, bright S4, gleam SP
IRON_MAP = 'ccccddeeeff'


def meteoric_iron_block(seed=7):
    """Polished cosmic metal plate: vanilla iron block's brushed banding and
    bevel frame in teal-slate steel, with the meteoric family's crystal
    flecks and fusion pits. Reads as metal, not wood."""
    img = mc.new_img(16, 16, (0, 0, 0, 255))
    for y, row in enumerate(IRON_BLOCK_GRID):
        for x, ch in enumerate(row):
            img[y][x] = mc.hex2rgb(LETTERS[IRON_MAP[int(ch, 36)]]) + (255,)
    # family details (interior, off the frame): crystal flecks + fusion pits
    for (x, y), hx in (((4, 5), K2), ((11, 9), K2), ((8, 8), K1),
                       ((5, 4), KG), ((9, 6), S0), ((13, 5), S0)):
        img[y][x] = mc.hex2rgb(hx) + (255,)
    return img


def raw_meteoric_iron_block(seed=7):
    """Clustered fusion-crusted nickel-iron nodules on a fine-grained mottled
    crust matrix.

    Matrix: 2-shade S0/S1 jittered dither over toroidal noise — the pattern
    has period 2 on a 16-wide grid, so it continues seamlessly across tile
    seams (like vanilla sand's checker), and the strong jitter keeps shade
    patches microscopic. Sparse S2 dust islands and the nodules sit on top.
    Nodules are top-left lit with dark crust rims and never touch the tile
    edges; small teal crystal flecks sit in the crust."""
    img = mc.new_img(16, 16, mc.hex2rgb(S0) + (255,))
    n = mc.fbm(16, 16, seed=seed, octaves=3, base_freq=2.5)
    for y in range(16):
        for x in range(16):
            t = n[y][x]
            bayer = ((0, 2), (3, 1))[y % 2][x % 2] / 4.0
            c = S1 if (t + (bayer - 0.375) * 2.0) >= 0.5 else S0
            img[y][x] = mc.hex2rgb(c) + (255,)
    for y in range(16):
        for x in range(16):
            if n[y][x] > 0.74:
                img[y][x] = mc.hex2rgb(S2) + (255,)
    blobs = [
        (5.0, 4.5, 3.2, 2.8), (11.0, 5.5, 2.6, 2.4), (7.5, 10.0, 3.6, 3.0),
        (4.0, 11.5, 2.6, 2.2), (11.5, 11.0, 2.4, 2.6), (8.5, 3.0, 2.4, 2.0),
    ]
    for cx, cy, rx, ry in blobs:
        for y in range(16):
            for x in range(16):
                dx = abs(x - cx) / rx
                dy = abs(y - cy) / ry
                d = dx * dx + dy * dy
                if d > 1.0:
                    continue
                bright = (x < cx) + (y < cy)          # 0..2, top-left lit
                if d > 0.78:
                    c = S1                            # crust rim
                elif d > 0.55:
                    c = S2
                elif bright == 2:
                    c = S4
                elif bright == 1:
                    c = S3
                else:
                    c = S2
                img[y][x] = mc.hex2rgb(c) + (255,)
    # crystal flecks (interior)
    for (x, y), hx in (((5, 4), K2), ((10, 3), K2), ((12, 11), K2), ((7, 12), K2),
                       ((3, 8), K2), ((6, 4), KG), ((11, 12), K1)):
        img[y][x] = mc.hex2rgb(hx) + (255,)
    return img


# Meteoric ore: tileable basalt host + fusion-crusted nickel-iron chunks.
# The chunk layout is the pack's original embedded-chunk design (decoded from
# the committed asset): four chunks, none touching the tile edges, so tiling
# is governed by the toroidal basalt noise. Chunks recoloured to the v2
# palette and given crystal flecks.
ORE_BASALT = ['#5e5456', '#473f42', '#362e31', '#261f22', '#181315']  # light->dark
ORE_CHUNKS = [
    '................',
    '..ooo.....ooo...',
    '.ofedo...ofedo..',
    '.oedco...oedco..',
    '.odcbo...odcbo..',
    '..ooo.....ooo...',
    '......ooo.......',
    '.....ofedo......',
    '.....oedco......',
    '.....odcbo......',
    '......ooo.......',
    '.ooo............',
    '.ofeo...........',
    '.oedo...........',
    '..oo............',
    '................',
]

ORE_CRYSTAL = {
    (3, 2): K2, (4, 2): KG, (3, 3): K1,        # top-left chunk
    (11, 2): K2, (12, 2): KG, (11, 3): K1,     # top-right chunk
    (7, 7): K2, (8, 7): KG,                    # centre chunk
    (2, 12): K2, (3, 12): KG, (2, 13): K1,     # bottom-left chunk
}


def meteoric_iron_ore(seed=7):
    """Basalt host from toroidal noise (Bayer-dithered, seam-safe); embedded
    chunks stamped on top; crystal flecks set into the chunk faces."""
    n = mc.fbm(16, 16, seed=seed, octaves=4, base_freq=3.0)
    img = mc.new_img(16, 16, (0, 0, 0, 255))
    ramp = [mc.hex2rgb(h) for h in ORE_BASALT]
    for y in range(16):
        for x in range(16):
            v = n[y][x] * 4.0
            i = min(3, int(v))
            t = v - i
            # periodic Bayer jitter (keeps the dither seam-safe)
            bayer = ((0, 2), (3, 1))[y % 2][x % 2] / 4.0
            c = ramp[i] if (t + (bayer - 0.375)) < 0.5 else ramp[i + 1]
            img[y][x] = c + (255,)
    for y in range(16):
        for x in range(16):
            ch = ORE_CHUNKS[y][x]
            if ch != '.':
                img[y][x] = mc.hex2rgb(LETTERS[ch]) + (255,)
    for (x, y), hx in ORE_CRYSTAL.items():
        img[y][x] = mc.hex2rgb(hx) + (255,)
    return img


# ---------------------------------------------------------------------------
def write(name, img, subdir):
    path = os.path.join(subdir, name + '.png')
    mc.write_png(path, img)
    print('wrote', os.path.relpath(path, ROOT))


def main():
    os.makedirs(ITEM_DIR, exist_ok=True)
    os.makedirs(BLOCK_DIR, exist_ok=True)
    write('meteoric_iron_sword', meteoric_sword(), ITEM_DIR)
    write('meteoric_iron_pickaxe', meteoric_pickaxe(), ITEM_DIR)
    write('meteoric_iron_axe', meteoric_axe(), ITEM_DIR)
    write('meteoric_iron_shovel', meteoric_shovel(), ITEM_DIR)
    write('meteoric_iron_hoe', meteoric_hoe(), ITEM_DIR)
    write('meteoric_iron_ingot', meteoric_ingot(), ITEM_DIR)
    write('raw_meteoric_iron', meteoric_raw_iron(), ITEM_DIR)
    write('meteoric_iron_block', meteoric_iron_block(), BLOCK_DIR)
    write('raw_meteoric_iron_block', raw_meteoric_iron_block(), BLOCK_DIR)
    write('meteoric_iron_ore', meteoric_iron_ore(), BLOCK_DIR)
    print('meteoric toolset regenerated.')


if __name__ == '__main__':
    main()
