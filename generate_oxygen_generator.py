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
    """Front instrument area (upper tier face): buttons, status LED, O2 plaque
    and hazard trim. The gauge lives on its own raised pod element. Lit: LED
    and buttons glow teal."""
    img = _panel_bg()
    for (x, y) in ((1, 1), (14, 1), (1, 14), (14, 14)):
        _screw(img, x, y)
    # hazard chevrons, bottom-left
    for y in range(13, 16):
        for x in range(0, 7):
            img[y][x] = mc.hex2rgb(S0 if (x + y) % 2 == 0 else S1) + (255,)
    # small O2 label plaque, top-left
    for y in range(2, 4):
        for x in range(2, 7):
            img[y][x] = mc.hex2rgb(S1) + (255,)
    img[2][3] = mc.hex2rgb(K2) + (255,)
    img[2][5] = mc.hex2rgb(K2) + (255,)
    img[3][3] = mc.hex2rgb(KG if lit else K2) + (255,)
    # buttons + status LED, right of the vent/gauge area
    for (x, y) in ((12, 5), (12, 7)):
        for yy in range(2):
            for xx in range(2):
                img[int(y + yy)][int(x + xx)] = mc.hex2rgb(KG if lit else S3) + (255,)
        img[int(y + 1)][int(x + 1)] = mc.hex2rgb(S0) + (255,)
    for (x, y) in ((14, 10), (14, 11)):
        img[y][x] = mc.hex2rgb(KG if lit else K2) + (255,)
    if lit:
        for (x, y) in ((12, 9), (13, 9), (13, 10)):
            img[y][x] = mc.hex2rgb(K1) + (255,)
    return img


def gauge():
    """Raised gauge pod face: dark instrument dial with ticks and a teal
    needle, set in a beveled bezel."""
    img = mc.new_img(16, 16, mc.hex2rgb(S2) + (255,))
    cx, cy, r = 8.0, 8.0, 5.6
    for y in range(16):
        for x in range(16):
            d = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5
            if d <= r - 0.6:
                img[y][x] = mc.hex2rgb(O0) + (255,)
            elif d <= r + 0.6:
                img[y][x] = mc.hex2rgb(S3 if (x + y) < 17 else S1) + (255,)
    for (x, y) in ((7, 3), (8, 2), (9, 3)):
        img[y][x] = mc.hex2rgb(SP) + (255,)
    for (x, y) in ((8, 9), (9, 8), (10, 7), (9, 7)):
        img[y][x] = mc.hex2rgb(K2) + (255,)
    img[8][8] = mc.hex2rgb(KG) + (255,)
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


def port_frame():
    """The connector plate shared by BOTH input ports: beveled plate with four
    large corner bolts and a recessed inner rim. Identical on both sides —
    only the symbol (power / water) differs."""
    img = mc.new_img(16, 16, mc.hex2rgb(S1) + (255,))
    for x in range(16):
        img[0][x] = mc.hex2rgb(S3) + (255,)
        img[15][x] = mc.hex2rgb(S0) + (255,)
    for y in range(16):
        img[y][0] = mc.hex2rgb(S3) + (255,)
        img[y][15] = mc.hex2rgb(S0) + (255,)
    for (bx, by) in ((1, 1), (12, 1), (1, 12), (12, 12)):
        for y in range(by, by + 3):
            for x in range(bx, bx + 3):
                img[y][x] = mc.hex2rgb(S4) + (255,)
        img[by + 2][bx + 2] = mc.hex2rgb(S0) + (255,)
    # recessed inner rim
    for x in range(3, 13):
        img[3][x] = mc.hex2rgb(O0) + (255,)
        img[12][x] = mc.hex2rgb(O0) + (255,)
    for y in range(3, 13):
        img[y][3] = mc.hex2rgb(O0) + (255,)
        img[y][12] = mc.hex2rgb(O0) + (255,)
    return img


def _outlet(img):
    """Power-outlet symbol: a square face plate (10x10, centered in the port
    well) with two vertical slots and a round ground hole — the classic wall
    socket. Perfectly symmetric: 3-px margins on all sides, slots mirrored
    around the center column, ground hole dead center."""
    for y in range(3, 13):
        for x in range(3, 13):
            img[y][x] = mc.hex2rgb(S2) + (255,)
    # plate bevel: light top/left edges, dark bottom/right edges
    for x in range(3, 13):
        img[3][x] = mc.hex2rgb(S3) + (255,)
        img[12][x] = mc.hex2rgb(S1) + (255,)
    for y in range(3, 13):
        img[y][3] = mc.hex2rgb(S3) + (255,)
        img[y][12] = mc.hex2rgb(S1) + (255,)
    # two vertical slots (left x5-6, right x9-10, rows 5-9)
    for y in range(5, 10):
        for x in (5, 6, 9, 10):
            img[y][x] = mc.hex2rgb(O0) + (255,)
    # round ground hole (x7-8, rows 10-11)
    for y in range(10, 12):
        for x in (7, 8):
            img[y][x] = mc.hex2rgb(O0) + (255,)
    return img


def _droplet(img):
    """Water-blue droplet symbol for the water port."""
    drops = [(7, 4), (8, 4), (6, 5), (7, 5), (8, 5), (9, 5),
             (6, 6), (7, 6), (8, 6), (9, 6), (5, 7), (6, 7), (7, 7), (8, 7), (9, 7), (10, 7),
             (5, 8), (6, 8), (7, 8), (8, 8), (9, 8), (10, 8),
             (6, 9), (7, 9), (8, 9), (9, 9), (7, 10), (8, 10)]
    for (x, y) in drops:
        img[y][x] = mc.hex2rgb('#7aa7d9') + (255,)
    img[7][5] = mc.hex2rgb('#a8c6e6') + (255,)
    img[7][6] = mc.hex2rgb('#a8c6e6') + (255,)


def port_power():
    """Power input face: the shared connector plate with a lightning-bolt
    symbol in the recessed well."""
    img = port_frame()
    _outlet(img)
    return img


def port_water():
    """Water input face: the shared connector plate with a droplet symbol in
    the recessed well."""
    img = port_frame()
    _droplet(img)
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
    write('oxygen_generator_port_frame', port_frame())
    write('oxygen_generator_port_power', port_power())
    write('oxygen_generator_port_water', port_water())
    write('oxygen_generator_gauge', gauge())
    write('oxygen_generator_fan', fan())
    print('oxygen generator textures regenerated.')


if __name__ == '__main__':
    main()
