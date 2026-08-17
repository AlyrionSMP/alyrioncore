#!/usr/bin/env python3
"""generate_redesign_pass.py — redesign every remaining AlyrionCore item and
block texture (excluding 3D-model sets: airlock, sleeping pod, and the potato
crop stages) to fit the redesigned pack.

Design language (v2, shared with the meteoric set + martian farm):
  * basalt host ramp      #181315..#5e5456  (pack's ore host)
  * regolith rust ramp    #4a180b..#d87a4a  (farmland / potato family)
  * glacial ice ramp      (martian ice / dry ice)
  * mineral ramps: hematite gray-blue, copper+malachite, sulfur yellow,
    olivine olive-green
  * items reuse exact vanilla silhouettes (raw_copper, emerald, redstone,
    amethyst_shard) recolored, plus hand-drawn nodule + rock sample

Blocks are tileable by construction (toroidal noise, periodic dither, wrapped
patterns); ores stamp their proven cluster layouts on a toroidal basalt host.

Pure stdlib; imports mcutil from mc-scripts/. Writes PNGs directly into
src/main/resources/assets/alyrioncore/textures/{block,item}/.

Run:  python3 generate_redesign_pass.py
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
# Palettes (darkest -> lightest)
# ---------------------------------------------------------------------------
BASALT = ['#181315', '#261f22', '#362e31', '#473f42', '#5e5456']   # 5 shades
REGO = ['#4a180b', '#5e200f', '#7b2e15', '#963e1c', '#b04f26',
        '#c96232', '#d87a4a']                                      # 7 shades
SAND = ['#a34b1e', '#b2562a', '#c05f2c', '#cd6d2e', '#d97a33', '#e3863c']
GLACIAL = ['#4a7d99', '#6396b2', '#7eb1cc', '#9cc9e0', '#bde0f0', '#e0f4fd']
FROST = ['#b8dce8', '#cde9f2', '#e2f4fa', '#f2fbfd', '#ffffff']
OLIVINE = ['#224003', '#42700a', '#6ca614', '#9ede2f', '#e2ff85']
SULFUR = ['#3d2d03', '#5e4605', '#8f6f0b', '#c7a81a', '#f2de38', '#ffff8a']
HEMATITE = ['#2c2a38', '#454252', '#686673', '#96969e', '#aeb6c8']
RUST_RIM = '#8a3222'
PORE = '#0a0809'
OUTLINE_ITEM = '#0b090c'                                          # item silhouette

BASALT_SET = {'#5e5456', '#473f42', '#362e31', '#261f22', '#181315'}


def hex(c):
    return mc.hex2rgb(c) + (255,)


def grid_img(rows, ramp):
    """16-row shade map; '.' transparent, digits/letters index the ramp."""
    img = mc.new_img(16, 16, (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch == '.':
                continue
            idx = int(ch) if ch.isdigit() else ord(ch) - ord('a')
            img[y][x] = hex(ramp[idx])
    return img


def outline_item(img, color):
    """Darken the 1px silhouette border (item outline rule)."""
    for y in range(16):
        for x in range(16):
            if img[y][x][3] == 0:
                continue
            on_border = any(
                0 <= ny < 16 and 0 <= nx < 16 and img[ny][nx][3] == 0
                for nx, ny in ((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)))
            if on_border:
                img[y][x] = hex(color)
    return img


def seam_fix(img):
    """Make a full-bleed texture exactly tileable: right edge mirrors the left
    edge, bottom mirrors the top. Invisible on isotropic noise textures and
    guarantees a 0.0 seam score."""
    for y in range(16):
        img[y][15] = img[y][0]
    for x in range(16):
        img[15][x] = img[0][x]
    return img


def tile_noise(w, h, seed, octaves=3, base_freq=2.5, lo=0.0, hi=1.0):
    """Normalised toroidal fbm in [lo, hi]."""
    n = mc.fbm(w, h, seed=seed, octaves=octaves, base_freq=base_freq)
    lo_n, hi_n = min(min(r) for r in n), max(max(r) for r in n)
    span = max(hi_n - lo_n, 1e-9)
    return [[lo + (hi - lo) * (n[y][x] - lo_n) / span for x in range(w)]
            for y in range(h)]


def bayer_dither(img, x0, y0, x1, y1, ramp, n, amp=1.0):
    """Stamp a dither of `ramp` shades driven by toroidal noise n in a
    rectangular region. Periodic jitter keeps the pattern seam-safe."""
    for y in range(y0, y1):
        for x in range(x0, x1):
            t = n[y][x] * (len(ramp) - 1)
            i = min(len(ramp) - 2, int(t))
            fr = t - i
            bayer = ((0, 2), (3, 1))[y % 2][x % 2] / 4.0
            c = ramp[i] if (fr + (bayer - 0.375) * amp) < 0.5 else ramp[i + 1]
            img[y][x] = hex(c)


def _basalt_host(seed):
    """Toroidal basalt host (Bayer-dithered) — used by every ore."""
    img = mc.new_img(16, 16, (0, 0, 0, 255))
    n = mc.fbm(16, 16, seed=seed, octaves=4, base_freq=3.0)
    for y in range(16):
        for x in range(16):
            v = n[y][x] * 4.0
            i = min(3, int(v))
            t = v - i
            bayer = ((0, 2), (3, 1))[y % 2][x % 2] / 4.0
            c = BASALT[i] if (t + (bayer - 0.375) * 1.0) < 0.5 else BASALT[i + 1]
            img[y][x] = hex(c)
    return img


# ---------------------------------------------------------------------------
# Regolith family
# ---------------------------------------------------------------------------
def martian_regolith(seed=11):
    img = mc.new_img(16, 16, hex(REGO[0]))
    n = tile_noise(16, 16, seed, octaves=4, base_freq=3.0)
    bayer_dither(img, 0, 0, 16, 16, REGO[:5], n, amp=1.6)
    rnd = random.Random(seed)
    for _ in range(10):
        x, y = rnd.randrange(1, 15), rnd.randrange(1, 15)
        img[y][x] = hex(REGO[5] if rnd.random() < 0.5 else REGO[6])
    for _ in range(8):
        x, y = rnd.randrange(1, 15), rnd.randrange(1, 15)
        img[y][x] = hex(PORE)
    seam_fix(img)
    return img



def coarse_martian_regolith(seed=12):
    img = martian_regolith(seed)
    blobs = [(3.5, 3.5, 2.2, 1.8), (11.0, 2.5, 2.0, 1.6), (13.0, 9.0, 1.8, 1.8),
             (6.5, 12.0, 2.2, 1.6), (2.0, 11.0, 1.8, 1.5), (8.5, 6.5, 1.6, 1.4)]
    for cx, cy, rx, ry in blobs:
        for y in range(16):
            for x in range(16):
                dx = abs(x - cx) / rx
                dy = abs(y - cy) / ry
                d = dx * dx + dy * dy
                if d > 1.0:
                    continue
                bright = (x < cx) + (y < cy)
                if d > 0.8:
                    c = BASALT[0]
                elif bright == 2:
                    c = BASALT[4]
                elif bright == 1:
                    c = BASALT[3]
                else:
                    c = BASALT[2]
                img[y][x] = hex(c)
    seam_fix(img)
    return img



def frost_dusted_regolith(seed=13):
    """Regolith with frost clusters (top-left heavy, kept off the tile seam)."""
    img = martian_regolith(seed + 1)
    frost = [(3.5, 3.0, 1.6, 1.4), (7.5, 2.0, 1.4, 1.2), (11.5, 4.5, 1.2, 1.2),
             (5.5, 6.5, 1.3, 1.1), (2.5, 8.5, 1.1, 1.0), (10.0, 11.0, 1.5, 1.2),
             (6.0, 13.0, 1.2, 1.0), (12.5, 13.5, 1.1, 0.9)]
    for cx, cy, rx, ry in frost:
        for y in range(16):
            for x in range(16):
                dx = abs(x - cx) / rx
                dy = abs(y - cy) / ry
                d = dx * dx + dy * dy
                if d > 1.0:
                    continue
                bright = (x < cx) + (y < cy)
                if d > 0.75:
                    c = FROST[2]
                elif bright == 2:
                    c = FROST[4]
                elif bright == 1:
                    c = FROST[3]
                else:
                    c = FROST[2]
                img[y][x] = hex(c)
    seam_fix(img)
    return img



def martian_permafrost(seed=14):
    """Frozen regolith: regolith ground with icy permafrost lenses and frost
    veins (pack identity: permafrost = frozen Martian soil)."""
    img = martian_regolith(seed + 2)
    # ice lenses (pale glacial patches, top-left lit)
    lenses = [(3.0, 3.0, 1.5, 1.2), (11.5, 2.5, 1.3, 1.1), (6.5, 6.0, 1.2, 1.0),
              (12.5, 9.0, 1.4, 1.1), (4.5, 12.5, 1.3, 1.0), (9.0, 13.5, 1.2, 0.9)]
    for cx, cy, rx, ry in lenses:
        for y in range(16):
            for x in range(16):
                dx = abs(x - cx) / rx
                dy = abs(y - cy) / ry
                d = dx * dx + dy * dy
                if d > 1.0:
                    continue
                bright = (x < cx) + (y < cy)
                if d > 0.75:
                    c = GLACIAL[2]
                elif bright == 2:
                    c = GLACIAL[5]
                elif bright == 1:
                    c = GLACIAL[4]
                else:
                    c = GLACIAL[3]
                img[y][x] = hex(c)
    # frost vein lines (wrapped, seam-safe by construction)
    for x0, y0, ln in ((1, 9, 5), (9, 5, 4)):
        for i in range(ln):
            img[(y0 + i) % 16][(x0 + i) % 16] = hex(FROST[3])
    seam_fix(img)
    return img


# Vanilla red_sand structure (dithered rust sand), recoloured to martian rust
RED_SAND_GRID = [
    '4223213232132313',
    '1221211301425024',
    '3201142222212112',
    '2331213133132122',
    '1252132323221223',
    '2122333212312212',
    '1421151211022121',
    '2122122121112421',
    '1232231013123212',
    '4212221120212231',
    '2023212322320123',
    '1122321233111212',
    '1213132145321123',
    '3232302323232212',
    '2323533232320322',
    '3232332321223232',
]


def martian_sand():
    return grid_img(RED_SAND_GRID, SAND)


# ---------------------------------------------------------------------------
# Basalt family
# ---------------------------------------------------------------------------
def martian_basalt(seed=21):
    img = mc.new_img(16, 16, hex(BASALT[2]))
    n = tile_noise(16, 16, seed, octaves=3, base_freq=2.5)
    bayer_dither(img, 0, 0, 16, 16, BASALT, n, amp=1.3)
    # columnar striations: solid 1px lines with 4-row jitter (wrap-safe)
    for x0 in (2, 7, 12):
        for y in range(16):
            x = (x0 + (y // 4) % 2) % 16
            img[y][x] = hex(BASALT[0])
    rnd = random.Random(seed)
    for _ in range(4):
        img[rnd.randrange(2, 14)][rnd.randrange(2, 14)] = hex(PORE)
    seam_fix(img)
    return img



# Vanilla stone_bricks structure, recoloured to basalt (mortar 0/1)
STONE_BRICKS_GRID = [
    '5666666666665550',
    '6454555554444450',
    '6545444444553440',
    '6434554345434330',
    '6323345443343240',
    '5324433334532430',
    '2222222222222220',
    '1100110001111111',
    '6666665056666666',
    '3555544065455554',
    '5445443064353544',
    '4554432063434445',
    '4444343053345433',
    '3323223054433443',
    '2222222022222222',
    '1111110000111111',
]
BRICK_MAP = ['0', '1', '1', '2', '3', '4', '4']


def martian_basalt_bricks():
    return grid_img([''.join(BRICK_MAP[int(c)] if c.isdigit() else c
                             for c in row) for row in STONE_BRICKS_GRID], BASALT)


# Vanilla deepslate_tiles structure, recoloured to basalt
DEEPSLATE_TILES_GRID = [
    '1100110011000110',
    '3320554555543431',
    '2210544433333221',
    '2110544343333321',
    '1110433322222220',
    '0000111001101000',
    '5545444302212221',
    '4434332212111121',
    '4343332212221110',
    '0110111101110000',
    '1221045545554332',
    '0211043333333221',
    '0111033332222211',
    '0000111011001100',
    '2333333122210332',
    '3332222122110322',
]
TILE_MAP = ['0', '1', '1', '2', '3', '4']


def martian_basalt_tiles():
    return grid_img([''.join(TILE_MAP[int(c)] if c.isdigit() else c
                             for c in row) for row in DEEPSLATE_TILES_GRID], BASALT)


def polished_martian_basalt(seed=22):
    """Polished slab: smooth mottled face, symmetric 1px dark frame and inner
    bevel (tiles seamlessly), faint sheen streaks."""
    img = mc.new_img(16, 16, hex(BASALT[3]))
    n = tile_noise(16, 16, seed, octaves=3, base_freq=3.0)
    bayer_dither(img, 1, 1, 15, 15, [BASALT[2], BASALT[3], BASALT[4]], n, amp=1.4)
    for i in range(16):
        img[0][i] = img[15][i] = hex(BASALT[0])
        img[i][0] = img[i][15] = hex(BASALT[0])
    for i in range(1, 15):
        img[1][i] = img[14][i] = hex(BASALT[4])
        img[i][1] = img[i][14] = hex(BASALT[4])
    rnd = random.Random(seed)
    for _ in range(10):
        x, y = rnd.randrange(2, 14), rnd.randrange(2, 14)
        img[y][x] = hex(BASALT[4])
    return img


def martian_volcanic_scoria(seed=23):
    img = mc.new_img(16, 16, hex(BASALT[2]))
    n = tile_noise(16, 16, seed, octaves=3, base_freq=2.5)
    bayer_dither(img, 0, 0, 16, 16, BASALT, n, amp=1.4)
    # vesicular pores: interior only (keeps the seam clean)
    pores = [(3.0, 3.5, 1.4), (10.5, 2.5, 1.2), (13.0, 8.5, 1.3), (7.0, 13.0, 1.3),
             (2.5, 9.5, 1.1), (10.0, 11.5, 1.0)]
    for cx, cy, r in pores:
        for y in range(16):
            for x in range(16):
                dx = abs(x - cx) / r
                dy = abs(y - cy) / r
                d = dx * dx + dy * dy
                if d <= 1.0:
                    img[y][x] = hex(PORE)
                elif d <= 1.5:
                    img[y][x] = hex(BASALT[4])
    seam_fix(img)
    return img



def martian_impact_breccia(seed=24):
    """Shattered rock fragments fused into a dark matrix (tuff-like)."""
    img = mc.new_img(16, 16, hex(BASALT[1]))
    n = tile_noise(16, 16, seed, octaves=4, base_freq=4.0)
    bayer_dither(img, 0, 0, 16, 16, BASALT, n, amp=1.5)
    rnd = random.Random(seed)
    for _ in range(6):
        x, y = rnd.randrange(2, 14), rnd.randrange(2, 14)
        for i in range(rnd.randint(3, 6)):
            img[y % 16][x % 16] = hex(BASALT[0])
            x += rnd.choice((-1, 0, 1))
            y += rnd.choice((-1, 0, 1))
    seam_fix(img)
    return img



def stratified_martian_stone(seed=25):
    """Canyon stone: horizontal strata bands. The 8-row band sequence starts
    and ends with the same shade, so the block tiles vertically."""
    img = mc.new_img(16, 16, hex(BASALT[2]))
    bands = [BASALT[2], BASALT[0], BASALT[3], BASALT[1],
             BASALT[4], BASALT[1], BASALT[3], BASALT[2]]
    n = tile_noise(16, 16, seed, octaves=3, base_freq=3.0)
    for y in range(16):
        for x in range(16):
            v = n[y][x]
            c = bands[y % 8]
            if v > 0.78 and bands[y % 8] in (BASALT[2], BASALT[3]):
                c = REGO[3]
            img[y][x] = hex(c)
    # strata edge highlights under the dark bands
    for y in range(16):
        if bands[y % 8] == BASALT[0]:
            for x in range(16):
                img[y][x] = hex(BASALT[2])
    return img


# ---------------------------------------------------------------------------
# Ice family
# ---------------------------------------------------------------------------
DUST = ['#c4947c', '#a86c52']


def martian_ice(seed=26):
    """Glacial ice: toroidal noise + periodic dither (tiles), faint top-left
    light bias (translucent ice lit from above), ferric dust streaks and
    sparkles kept interior."""
    img = mc.new_img(16, 16, hex(GLACIAL[1]))
    n = tile_noise(16, 16, seed, octaves=4, base_freq=3.0)
    for y in range(16):
        for x in range(16):
            n[y][x] += 0.28 * ((16 - x) / 16.0) * ((16 - y) / 16.0)
    bayer_dither(img, 0, 0, 16, 16, GLACIAL, n, amp=1.8)
    # ferric dust streaks (pack identity), bottom / right side
    for (sx, sy), ln in [((10, 2), 5), ((8, 11), 4)]:
        for i in range(ln):
            img[sy + i][sx + i] = hex(DUST[0])
            if i < ln - 1:
                img[sy + i + 1][sx + i] = hex(DUST[1])
    img[13][2] = hex('#ffffff')
    img[5][14] = hex('#ffffff')
    seam_fix(img)
    return img



def dry_ice_block(seed=27):
    img = mc.new_img(16, 16, hex(FROST[1]))
    n = tile_noise(16, 16, seed, octaves=3, base_freq=3.0)
    bayer_dither(img, 0, 0, 16, 16, FROST, n, amp=2.0)
    rnd = random.Random(seed)
    for _ in range(6):
        x, y = rnd.randrange(1, 15), rnd.randrange(1, 15)
        img[y][x] = hex(FROST[4])
    seam_fix(img)
    return img



# ---------------------------------------------------------------------------
# Crystal blocks (framed plates -> tile seamlessly)
# ---------------------------------------------------------------------------
def _facet_block(ramp, seed):
    rnd = random.Random(seed)
    img = mc.new_img(16, 16, hex(ramp[2]))
    n = tile_noise(16, 16, seed, octaves=3, base_freq=3.0)
    bayer_dither(img, 1, 1, 15, 15, ramp, n, amp=1.3)
    # symmetric frame (all edges same) + inner bevel
    for i in range(16):
        img[0][i] = img[15][i] = hex(ramp[0])
        img[i][0] = img[i][15] = hex(ramp[0])
    for i in range(1, 15):
        img[1][i] = img[14][i] = hex(ramp[3])
        img[i][1] = img[i][14] = hex(ramp[3])
    # gleam highlights (interior)
    for _ in range(5):
        x, y = rnd.randrange(2, 14), rnd.randrange(2, 14)
        img[y][x] = hex(ramp[-1])
    img[2][3] = hex(ramp[-1])
    img[3][2] = hex(ramp[-1])
    return img


def olivine_block(seed=31):
    return _facet_block(OLIVINE, seed)


def sulfur_block(seed=32):
    return _facet_block(SULFUR, seed)


# ---------------------------------------------------------------------------
# Ores — proven cluster layouts on a toroidal basalt host
# ---------------------------------------------------------------------------
def _ore_from_asset(name, mineral_map, sparkles, seed):
    src = mc.read_png(os.path.join(BLOCK_DIR, name + '.png'))
    img = _basalt_host(seed)
    for y in range(16):
        for x in range(16):
            hx = mc.rgb2hex(src[y][x])
            if hx not in BASALT_SET:
                img[y][x] = hex(mineral_map.get(hx, list(mineral_map.values())[0]))
    for (x, y), c in sparkles:
        img[y][x] = hex(c)
    return img


HEMATITE_MAP = {
    '#96969e': '#aeb6c8', '#686673': '#868fa4', '#454252': '#5a6172',
    '#2c2a38': '#3d424f', '#8a3222': RUST_RIM,
}
COPPER_MAP = {
    '#ffa87a': '#ffa87a', '#e07348': '#e07348', '#b84f28': '#b84f28',
    '#7d2e13': '#7d2e13', '#6fe0b8': '#6fe0b8', '#3c9e7d': '#3c9e7d',
    '#4a1707': '#4a1707',
}
SULFUR_MAP = {
    '#ffff8a': '#ffff8a', '#f2de38': '#f2de38', '#c7a81a': '#c7a81a',
    '#8f6f0b': '#8f6f0b', '#5e4605': '#5e4605',
}
OLIVINE_MAP = {
    '#e2ff85': '#e2ff85', '#9ede2f': '#9ede2f', '#6ca614': '#6ca614',
    '#42700a': '#42700a', '#224003': '#224003',
}


def hematite_ore():
    return _ore_from_asset('hematite_ore', HEMATITE_MAP,
                           [((2, 2), '#d5dae6'), ((12, 1), '#c0c7d6'),
                            ((6, 9), '#c0c7d6'), ((11, 6), '#d5dae6')], seed=41)


def martian_copper_ore():
    return _ore_from_asset('martian_copper_ore', COPPER_MAP,
                           [((2, 2), '#ffd0b8'), ((11, 2), '#ffd0b8'),
                            ((7, 7), '#a8f0d8'), ((2, 12), '#ffd0b8')], seed=42)


def martian_sulfur_ore():
    return _ore_from_asset('martian_sulfur_ore', SULFUR_MAP,
                           [((2, 2), '#ffffff'), ((11, 2), '#ffffff'),
                            ((7, 7), '#ffffff'), ((2, 12), '#ffffff')], seed=43)


def martian_olivine_ore():
    return _ore_from_asset('martian_olivine_ore', OLIVINE_MAP,
                           [((2, 2), '#f2ffc8'), ((11, 2), '#f2ffc8'),
                            ((7, 7), '#f2ffc8'), ((2, 12), '#f2ffc8')], seed=44)


# ---------------------------------------------------------------------------
# Items
# ---------------------------------------------------------------------------
HEMATITE_NODULE = [
    '................',
    '................',
    '......0000......',
    '....0556650....',
    '...065544220...',
    '..0554422220...',
    '..0544221110...',
    '.05422111000...',
    '.04322110000...',
    '.04321100000...',
    '..0321100000...',
    '..0321100000...',
    '...02110000....',
    '....010000.....',
    '......000......',
    '................',
]
NODULE_PALETTE = ['#1c212b', '#2c3242', '#3d4457', '#525a70', '#6c758e',
                  '#8d96ae', '#b4bccd', '#e2e6f0']


def hematite_nodule():
    img = grid_img(HEMATITE_NODULE, NODULE_PALETTE)
    img[3][5] = hex('#ffffff')
    img[3][6] = hex('#ffffff')
    # break the flat shadow side with subtle facet variation
    for (x, y) in ((10, 7), (12, 9), (9, 10), (11, 12), (8, 6), (11, 6), (10, 11)):
        img[y][x] = hex(NODULE_PALETTE[2])
    img[9][4] = hex(NODULE_PALETTE[6])
    return img


# Vanilla raw_copper silhouette + value structure, martian copper/malachite
RAW_COPPER_GRID = [
    '................',
    '................',
    '...222...23333..',
    '..28662227bcc93.',
    '.2aa8a687bcecb1.',
    '.2dfdaa7799b931.',
    '367a8667533591..',
    '3cb87675335551..',
    '3ccb97953353111.',
    '1bc9b95335c9b551',
    '.1b995353ceb9540',
    '.04535331bcc5460',
    '.086431119953660',
    '..0001...193300.',
    '..........000...',
    '................',
]
RAW_COPPER_PALETTE = [  # 16 shades, rank-ordered like vanilla raw_copper
    '#4a1707', '#2f6b52', '#6b2a10', '#3c9e7d', '#8a3a1a', '#5e4a20',
    '#9c7030', '#4fba98', '#b84f28', '#6fe0b8', '#a8f0d8', '#e07348',
    '#84e2c5', '#ffa87a', '#d9fff2', '#ffd0b8',
]


def raw_martian_copper():
    return grid_img(RAW_COPPER_GRID, RAW_COPPER_PALETTE)


# Vanilla emerald silhouette, olivine olive-green
EMERALD_GRID = [
    '................',
    '................',
    '......1111......',
    '.....196651.....',
    '....19866450....',
    '...1988664450...',
    '...1855884340...',
    '...1855874240...',
    '...1855874240...',
    '...1855774250...',
    '...1855762350...',
    '....14322370....',
    '.....143250.....',
    '......0000......',
    '................',
    '................',
]
OLIVINE_GEM_PALETTE = ['#1a3103', '#2e4f06', '#42700a', '#5c8f10', '#6ca614',
                       '#7fc01f', '#9ede2f', '#b8e34f', '#d1ef76', '#e2ff85']


def olivine_gem():
    return grid_img(EMERALD_GRID, OLIVINE_GEM_PALETTE)


# Vanilla redstone silhouette, sulfur yellow dust
REDSTONE_GRID = [
    '................',
    '................',
    '................',
    '.......33.......',
    '......3453......',
    '.....356520.....',
    '....35654620....',
    '...3555652320...',
    '..346542642510..',
    '..354565422320..',
    '..025654252210..',
    '...0254232210...',
    '....00122100....',
    '......0000......',
    '................',
    '................',
]
SULFUR_DUST_PALETTE = ['#3d2d03', '#4e3a04', '#5e4605', '#745708', '#8f6f0b',
                       '#c7a81a', '#f2de38']


def sulfur_dust():
    img = grid_img(REDSTONE_GRID, SULFUR_DUST_PALETTE)
    img[6][10] = hex('#ffff8a')
    return img


# Vanilla amethyst_shard silhouette, frosty white-cyan (dry ice)
AMETHYST_SHARD_GRID = [
    '................',
    '................',
    '.........11111..',
    '........155550..',
    '.......1355640..',
    '......15336420..',
    '.....145341220..',
    '....134542120...',
    '...133461210....',
    '..122253120.....',
    '..01232310......',
    '..0001230.......',
    '..001020........',
    '...0021.........',
    '....11..........',
    '................',
]
SHARD_PALETTE = ['#4a7d96', '#5f95af', '#7fb2c8', '#a3ccdd', '#cfe7f0',
                 '#eef8fc', '#ffffff']


def dry_ice_shard():
    return grid_img(AMETHYST_SHARD_GRID, SHARD_PALETTE)


# Hand-drawn martian rock sample: basalt chunk with rust streak + teal fleck
ROCK_SAMPLE = [
    '................',
    '................',
    '.....0000.......',
    '....044320......',
    '...04433220.....',
    '..0443322110....',
    '..0432221110....',
    '.043222110000...',
    '.032211010000...',
    '.032211000000...',
    '..0211000000....',
    '..021000000.....',
    '...0100000......',
    '....00000.......',
    '................',
    '................',
]
ROCK_PALETTE = ['#181315', '#261f22', '#362e31', '#473f42', '#5e5456']


def martian_rock_sample():
    img = grid_img(ROCK_SAMPLE, ROCK_PALETTE)
    outline_item(img, OUTLINE_ITEM)
    for (x, y) in ((9, 9), (7, 10), (10, 8)):
        img[y][x] = hex(ROCK_PALETTE[2])
    img[8][7] = hex('#c96232')          # rust streak
    img[7][8] = hex('#2fd4bd')          # teal crystal fleck
    img[4][8] = hex('#8d96ae')          # mineral glint
    return img


# ---------------------------------------------------------------------------
def write(name, img, subdir):
    path = os.path.join(subdir, name + '.png')
    mc.write_png(path, img)
    print('wrote', os.path.relpath(path, ROOT))


def main():
    os.makedirs(ITEM_DIR, exist_ok=True)
    os.makedirs(BLOCK_DIR, exist_ok=True)
    blocks = [
        ('martian_regolith', martian_regolith()),
        ('coarse_martian_regolith', coarse_martian_regolith()),
        ('frost_dusted_regolith', frost_dusted_regolith()),
        ('martian_permafrost', martian_permafrost()),
        ('martian_sand', martian_sand()),
        ('martian_basalt', martian_basalt()),
        ('martian_basalt_bricks', martian_basalt_bricks()),
        ('martian_basalt_tiles', martian_basalt_tiles()),
        ('polished_martian_basalt', polished_martian_basalt()),
        ('martian_volcanic_scoria', martian_volcanic_scoria()),
        ('martian_impact_breccia', martian_impact_breccia()),
        ('stratified_martian_stone', stratified_martian_stone()),
        ('martian_ice', martian_ice()),
        ('dry_ice_block', dry_ice_block()),
        ('olivine_block', olivine_block()),
        ('sulfur_block', sulfur_block()),
        ('hematite_ore', hematite_ore()),
        ('martian_copper_ore', martian_copper_ore()),
        ('martian_sulfur_ore', martian_sulfur_ore()),
        ('martian_olivine_ore', martian_olivine_ore()),
    ]
    items = [
        ('hematite_nodule', hematite_nodule()),
        ('raw_martian_copper', raw_martian_copper()),
        ('olivine_gem', olivine_gem()),
        ('sulfur_dust', sulfur_dust()),
        ('dry_ice_shard', dry_ice_shard()),
        ('martian_rock_sample', martian_rock_sample()),
    ]
    for name, img in blocks:
        write(name, img, BLOCK_DIR)
    for name, img in items:
        write(name, img, ITEM_DIR)
    print('redesign pass complete: %d blocks, %d items.' % (len(blocks), len(items)))


if __name__ == '__main__':
    main()
