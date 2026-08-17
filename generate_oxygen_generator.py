#!/usr/bin/env python3
"""generate_oxygen_generator.py — textures for the AlyrionCore Oxygen Generator.

FULLY CUSTOM machine textures — nothing is reused or stretched from existing
blocks. Every texture is designed for its own face:

  * side       — machine housing: recessed service panel, screws, cooling ribs
  * control    — front instrument cluster: gauge, buttons, LED, hazard trim
                 (+ _lit while running)
  * top        — service hatch with hinges and screws
  * ring       — the raised vent ring/frame that holds the animated impeller
  * stack      — top exhaust/vent stack (pipe with ring bands)
  * pipe       — side feed pipe (cylindrical shading, teal energy stripe)
  * power_port — electrical terminal (socket + bolt contacts, teal glow)
  * water_port — pipe flange (bolts + water-blue bore)
  * fan        — impeller blade

All use the pack's meteoric palette (S0..SP steel + K1/K2/KG teal), are opaque
and top-left lit. The control _lit variant swaps in while the machine runs.

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


def _panel_bg():
    """Gentle panel metal: brighter top-left, darker bottom-right."""
    img = mc.new_img(16, 16, mc.hex2rgb(S2) + (255,))
    for y in range(16):
        for x in range(16):
            shade = (y / 15.0) * 0.55 + (x / 15.0) * 0.45
            c = S3 if shade < 0.38 else (S2 if shade < 0.72 else S1)
            img[y][x] = mc.hex2rgb(c) + (255,)
    return img


def _screw(img, x, y):
    img[y][x] = mc.hex2rgb(S4) + (255,)
    img[y + 1][x + 1] = mc.hex2rgb(S0) + (255,)


def side():
    """Machine housing side: recessed service panel with screws + cooling ribs."""
    img = _panel_bg()
    # recessed service panel
    for y in range(3, 14):
        for x in range(2, 14):
            img[y][x] = mc.hex2rgb(S1) + (255,)
    for x in range(2, 14):
        img[3][x] = mc.hex2rgb(O0) + (255,)   # shadow top-left inner edge
        img[13][x] = mc.hex2rgb(S3) + (255,)  # highlight bottom-right inner edge
    for y in range(3, 14):
        img[y][2] = mc.hex2rgb(O0) + (255,)
        img[y][13] = mc.hex2rgb(S3) + (255,)
    for (x, y) in ((3, 4), (12, 4), (3, 12), (12, 12)):
        _screw(img, x, y)
    # cooling ribs at the bottom
    for y in range(14, 16):
        for x in range(16):
            img[y][x] = mc.hex2rgb(S0 if (x + y) % 2 == 0 else S2) + (255,)
    return img


def control(lit=False):
    """Front instrument cluster (recessed panel face): gauge, buttons, LED,
    hazard trim. The vent ring covers the left-middle in the model, so the
    cluster sits right-of-center. Lit: needle + LED glow teal."""
    img = _panel_bg()
    # corner screws
    for (x, y) in ((1, 1), (14, 1), (1, 14), (14, 14)):
        _screw(img, x, y)
    # hazard chevrons, bottom-left
    for y in range(13, 16):
        for x in range(0, 7):
            img[y][x] = mc.hex2rgb(S0 if (x + y) % 2 == 0 else S1) + (255,)
    # small O2 label plaque, top-left (left of the vent ring)
    for y in range(2, 4):
        for x in range(2, 7):
            img[y][x] = mc.hex2rgb(S1) + (255,)
    img[2][3] = mc.hex2rgb(K2) + (255,)
    img[2][5] = mc.hex2rgb(K2) + (255,)
    img[3][3] = mc.hex2rgb(KG if lit else K2) + (255,)
    # main gauge, right side
    cx, cy, r = 11.0, 5.0, 2.9
    for y in range(16):
        for x in range(16):
            d = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5
            if d <= r - 0.5:
                img[y][x] = mc.hex2rgb(O0) + (255,)
            elif d <= r + 0.5:
                rim = S3 if (x + y) < 17 else S1
                img[y][x] = mc.hex2rgb(rim) + (255,)
    for (x, y) in ((10, 3), (11, 2), (12, 3)):
        img[y][x] = mc.hex2rgb(SP) + (255,)
    for (x, y) in ((11, 6), (12, 5), (12, 4)):
        img[y][x] = mc.hex2rgb(KG if lit else K2) + (255,)
    # buttons + status LED
    for (x, y) in ((13, 9), (14.5, 9)):
        for yy in range(2):
            for xx in range(2):
                img[int(y + yy)][int(x + xx)] = mc.hex2rgb(S3) + (255,)
        img[int(y + 1)][int(x + 1)] = mc.hex2rgb(S0) + (255,)
    for (x, y) in ((12, 11), (12, 12)):
        img[y][x] = mc.hex2rgb(KG if lit else K2) + (255,)
    if lit:
        for (x, y) in ((10, 8), (10, 9), (11, 9)):
            img[y][x] = mc.hex2rgb(K1) + (255,)
    return img


def top():
    """Service hatch on the machine roof: recessed hatch plate, hinges, screws."""
    img = _panel_bg()
    for y in range(3, 14):
        for x in range(3, 14):
            img[y][x] = mc.hex2rgb(S1) + (255,)
    for x in range(3, 14):
        img[3][x] = mc.hex2rgb(O0) + (255,)
        img[13][x] = mc.hex2rgb(S3) + (255,)
    for y in range(3, 14):
        img[y][3] = mc.hex2rgb(O0) + (255,)
        img[y][13] = mc.hex2rgb(S3) + (255,)
    for (x, y) in ((4, 4), (11, 4), (4, 11), (11, 11)):
        _screw(img, x, y)
    # hinges along the back edge
    for (x, y) in ((5, 13), (9, 13)):
        img[y][x] = mc.hex2rgb(S3) + (255,)
        img[y + 1][x] = mc.hex2rgb(S0) + (255,)
    return img


def ring():
    """Raised vent ring/frame around the impeller: beveled frame with screws
    and a dark inner lip."""
    img = _panel_bg()
    # outer bevel
    for x in range(16):
        img[0][x] = mc.hex2rgb(S3) + (255,)
        img[15][x] = mc.hex2rgb(S0) + (255,)
    for y in range(16):
        img[y][0] = mc.hex2rgb(S3) + (255,)
        img[y][15] = mc.hex2rgb(S0) + (255,)
    # dark inner lip (the hole side)
    for x in range(2, 14):
        img[2][x] = mc.hex2rgb(O0) + (255,)
        img[13][x] = mc.hex2rgb(O0) + (255,)
    for y in range(2, 14):
        img[y][2] = mc.hex2rgb(O0) + (255,)
        img[y][13] = mc.hex2rgb(O0) + (255,)
    for (x, y) in ((4, 4), (11, 4), (4, 11), (11, 11)):
        _screw(img, x, y)
    return img


def pipe():
    """Side feed pipe: cylinder with a teal energy stripe."""
    img = mc.new_img(16, 16, mc.hex2rgb(S2) + (255,))
    for y in range(16):
        for x in range(16):
            d = abs(x - 7.5) / 6.0
            c = S4 if d < 0.2 else (S3 if d < 0.5 else S1)
            img[y][x] = mc.hex2rgb(c) + (255,)
    # teal energy stripe down the pipe
    for y in range(16):
        img[y][6] = mc.hex2rgb(K1) + (255,)
        img[y][7] = mc.hex2rgb(K2) + (255,)
    img[2][7] = mc.hex2rgb(KG) + (255,)
    return img


def power_port():
    """Electrical input terminal (power side): dark plate, four bolt contacts
    and a central socket with the pack's teal energy glow."""
    img = mc.new_img(16, 16, mc.hex2rgb(S1) + (255,))
    for x in range(16):
        img[0][x] = mc.hex2rgb(S3) + (255,)
        img[15][x] = mc.hex2rgb(S0) + (255,)
    for y in range(16):
        img[y][0] = mc.hex2rgb(S3) + (255,)
        img[y][15] = mc.hex2rgb(S0) + (255,)
    for (x, y) in ((3, 3), (12, 3), (3, 12), (12, 12)):
        _screw(img, x, y)
    for y in range(5, 11):
        for x in range(5, 11):
            img[y][x] = mc.hex2rgb(O0) + (255,)
    for (x, y) in ((5, 5), (5, 10), (10, 5), (10, 10)):
        img[y][x] = mc.hex2rgb(S2) + (255,)
    img[7][7] = mc.hex2rgb(KG) + (255,)
    img[8][8] = mc.hex2rgb(K2) + (255,)
    return img


def water_port():
    """Fluid input flange (water side): circular pipe flange with four bolts,
    a dark bore and a water-blue accent inside."""
    img = mc.new_img(16, 16, mc.hex2rgb(S2) + (255,))
    cx, cy = 8.0, 8.0
    for y in range(16):
        for x in range(16):
            d = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5
            if d <= 2.4:
                img[y][x] = mc.hex2rgb(O0) + (255,)
            elif d <= 5.4:
                img[y][x] = mc.hex2rgb(S3 if (x + y) < 17 else S1) + (255,)
            elif d <= 6.2:
                img[y][x] = mc.hex2rgb(S2) + (255,)
    for (x, y) in ((8, 2), (8, 13), (2, 8), (13, 8)):
        _screw(img, x, y)
    img[7][6] = mc.hex2rgb('#7aa7d9') + (255,)
    img[6][7] = mc.hex2rgb('#7aa7d9') + (255,)
    img[8][8] = mc.hex2rgb('#a8c6e6') + (255,)
    return img


def fan():
    """Impeller blade: diagonal metal gradient (bright top-left, dark
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
    write('oxygen_generator_side', side())
    write('oxygen_generator_control', control(lit=False))
    write('oxygen_generator_control_lit', control(lit=True))
    write('oxygen_generator_top', top())
    write('oxygen_generator_ring', ring())
    write('oxygen_generator_pipe', pipe())
    write('oxygen_generator_power_port', power_port())
    write('oxygen_generator_water_port', water_port())
    write('oxygen_generator_fan', fan())
    print('oxygen generator textures regenerated.')


if __name__ == '__main__':
    main()
