#!/usr/bin/env python3
"""generate_oxygen_generator.py — textures for the AlyrionCore Oxygen Generator.

The machine is the heart of every pressurized habitat: a meteoric-iron chassis
(reusing the block-of-meteoric-iron plate so it matches the pack's metal), a
teal coolant tank on top, a front dial with a status LED, and a small impeller
blade for the animated fan (rendered by the block entity renderer).

Palette = the pack's meteoric nickel-iron ramp (S0..SP) + starfall teal crystal
(K1/K2/KG) — same identity as the meteoric toolset. All textures are opaque and
top-left lit. The _lit variants (tank, dial) swap in while the machine runs.

Pure stdlib; imports mcutil from mc-scripts/. Writes PNGs directly into
src/main/resources/assets/alyrioncore/textures/block/.

Run:  python3 generate_oxygen_generator.py
"""

import os
import sys

ROOT = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(ROOT, 'mc-scripts'))
import mcutil as mc

TEX = os.path.join(ROOT, 'src', 'main', 'resources', 'assets', 'alyrioncore', 'textures')
BLOCK_DIR = os.path.join(TEX, 'block')

# ---------------------------------------------------------------------------
# Meteoric palette (identical to generate_meteoric_toolset.py)
# ---------------------------------------------------------------------------
O0 = '#0e1624'   # fusion crust / deep face
S0 = '#1a2c42'   # deep shadow
S1 = '#2b425e'   # shadow
S2 = '#40607e'   # midtone
S3 = '#5d85a4'   # light
S4 = '#8db4cb'   # bright
SP = '#dbeef5'   # specular (icy teal-white)
K1 = '#15998b'   # crystal dark
K2 = '#2fd4bd'   # crystal bright
KG = '#a8ffe9'   # crystal glint

# The block-of-meteoric-iron plate grid + mapping (proven, tileable)
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
IRON_MAP = 'ccccddeeeff'
IRON_LETTERS = {'c': S2, 'd': S3, 'e': S4, 'f': SP}


def casing():
    """The meteoric metal plate — the machine is built from the same material
    as the block of meteoric iron, so the family reads at a glance."""
    img = mc.new_img(16, 16, (0, 0, 0, 255))
    for y, row in enumerate(IRON_BLOCK_GRID):
        for x, ch in enumerate(row):
            img[y][x] = mc.hex2rgb(IRON_LETTERS[IRON_MAP[int(ch, 36)]]) + (255,)
    # family details (interior, off the frame): crystal flecks + fusion pits
    for (x, y), hx in (((4, 5), K2), ((11, 9), K2), ((8, 8), K1),
                       ((5, 4), KG), ((9, 6), S0), ((13, 5), S0)):
        img[y][x] = mc.hex2rgb(hx) + (255,)
    return img


def tank(lit=False):
    """Opaque frosted-glass tank with teal coolant. Metal clamps top/bottom,
    liquid shaded brighter at the top-left, glass shine streaks, and a dark
    edge on the right. The lit variant glows (K2/KG dominate)."""
    img = mc.new_img(16, 16, (0, 0, 0, 255))
    # metal clamps
    for x in range(16):
        img[0][x] = mc.hex2rgb(S3 if x < 8 else S2) + (255,)
        img[1][x] = mc.hex2rgb(S2) + (255,)
        img[14][x] = mc.hex2rgb(S1) + (255,)
        img[15][x] = mc.hex2rgb(S0) + (255,)
    # liquid body rows 2..13, top-left lit
    for y in range(2, 14):
        for x in range(16):
            t = (y - 2) / 11.0   # 0 top .. 1 bottom
            l = x / 15.0         # 0 left .. 1 right
            shade = t * 0.62 + l * 0.38
            if lit:
                c = KG if shade < 0.16 else (K2 if shade < 0.52 else K1)
            else:
                c = K2 if shade < 0.34 else K1
            img[y][x] = mc.hex2rgb(c) + (255,)
    # glass edge: bright left, dark right
    for y in range(2, 14):
        img[y][0] = mc.hex2rgb(SP if lit else S4) + (255,)
        img[y][15] = mc.hex2rgb(S1) + (255,)
    # glass shine streaks (top-left)
    for (x, y) in ((2, 3), (3, 4), (4, 5), (2, 6), (11, 3), (12, 4)):
        img[y][x] = mc.hex2rgb(KG if lit else SP) + (255,)
    # lit: a few bubbles / glow patches mid-tank
    if lit:
        for (x, y) in ((6, 7), (9, 9), (7, 11), (10, 6)):
            img[y][x] = mc.hex2rgb(KG) + (255,)
    return img


def dial(lit=False):
    """Front status dial on a raised bezel: dark instrument face, specular tick
    marks, a teal needle pointing up-right, and a small LED. Lit: needle and LED
    glow (KG) and the rim warms up."""
    img = mc.new_img(16, 16, mc.hex2rgb(S2) + (255,))  # bezel metal
    cx, cy, r = 8.0, 8.0, 6.0
    for y in range(16):
        for x in range(16):
            d = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5
            if d <= r:
                img[y][x] = mc.hex2rgb(O0) + (255,)          # instrument face
            elif d <= r + 1.0:
                rim = S3 if (x + y) < 16 else S1             # lit top-left rim
                img[y][x] = mc.hex2rgb(rim) + (255,)
    # tick marks at the top of the face
    for (x, y) in ((6, 3), (8, 3), (10, 3)):
        img[y][x] = mc.hex2rgb(SP) + (255,)
    # needle: center -> upper right
    for (x, y) in ((8, 7), (9, 6), (10, 5), (9, 5)):
        img[y][x] = mc.hex2rgb(KG if lit else K2) + (255,)
    # status LED, bottom-left of the face
    for (x, y) in ((5, 12), (6, 12), (5, 13)):
        img[y][x] = mc.hex2rgb(KG if lit else K2) + (255,)
    return img


def fan():
    """Impeller blade texture: diagonal metal gradient (bright top-left, dark
    bottom-right), a teal edge on the lower-right, specular glints up-left."""
    img = mc.new_img(16, 16, mc.hex2rgb(S2) + (255,))
    for y in range(16):
        for x in range(16):
            v = x + y
            if v < 10:
                c = S4
            elif v < 17:
                c = S3
            else:
                c = S1
            img[y][x] = mc.hex2rgb(c) + (255,)
    for (x, y) in ((12, 12), (13, 13), (14, 14), (12, 13), (13, 14), (13, 12), (14, 13)):
        img[y][x] = mc.hex2rgb(K2) + (255,)
    for (x, y) in ((3, 3), (4, 4), (2, 5), (5, 3)):
        img[y][x] = mc.hex2rgb(SP) + (255,)
    return img


def write(name, img):
    path = os.path.join(BLOCK_DIR, name + '.png')
    mc.write_png(path, img)
    print('wrote', os.path.relpath(path, ROOT))


def main():
    os.makedirs(BLOCK_DIR, exist_ok=True)
    write('oxygen_generator_casing', casing())
    write('oxygen_generator_tank', tank(lit=False))
    write('oxygen_generator_tank_lit', tank(lit=True))
    write('oxygen_generator_dial', dial(lit=False))
    write('oxygen_generator_dial_lit', dial(lit=True))
    write('oxygen_generator_fan', fan())
    print('oxygen generator textures regenerated.')


if __name__ == '__main__':
    main()
