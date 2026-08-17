#!/usr/bin/env python3
"""generate_martian_farm_assets.py — regenerate the AlyrionCore martian farm
set: martian potato + baked martian potato items, and the regolith farmland
block (dry, moist, side).

Design (v2):
  * Items reuse the exact vanilla potato / baked-potato silhouettes AND value
    structure from the 1.21 masters (same object, same angle, same
    proportions — Jappa checklist), recoloured to the pack's Martian
    terracotta/regolith ramp (deep rust shadows, warm rust body, amber gleam).
  * Farmland dry/moist reuse the vanilla farmland tilled-furrow structure in
    regolith colours (moisture darkens the soil and adds glints).
  * The side uses the pack's martian_regolith pixel-for-pixel below a 2px
    tilled top layer, so it is seamless with adjacent regolith blocks
    (vanilla farmland side == dirt).

Pure stdlib; imports mcutil from mc-scripts/. Writes PNGs directly into
src/main/resources/assets/alyrioncore/textures/{block,item}/.

Run:  python3 generate_martian_farm_assets.py
"""

import os
import sys

ROOT = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(ROOT, 'mc-scripts'))
import mcutil as mc

TEX = os.path.join(ROOT, 'src', 'main', 'resources', 'assets', 'alyrioncore', 'textures')
ITEM_DIR = os.path.join(TEX, 'item')
BLOCK_DIR = os.path.join(TEX, 'block')

# ---------------------------------------------------------------------------
# Palettes (darkest -> lightest), indexed from the pack's regolith family
# ---------------------------------------------------------------------------
# Martian potato skin: terracotta rust
POTATO = ['#3c1106', '#551c0c', '#6b2512', '#7f2d15', '#963e1c',
          '#b04f26', '#c96232', '#d87a4a', '#e89a6a']
# Baked martian potato: roasted rust skin + amber split interior
BAKED = ['#3a1105', '#5c1f0c', '#7a2e13', '#8d3a18', '#a04a24',
         '#b35a2c', '#c8703a', '#d89a45', '#e8b255', '#f7d97a']
# Regolith farmland dry
FARM_DRY = ['#3f1508', '#4a180b', '#5e200f', '#7b2e15', '#963e1c', '#b04f26']
# Regolith farmland moist (moisture-darkened soil, groove lines stay visible)
FARM_MOIST = ['#230c03', '#2f1205', '#3d1807', '#52220b', '#5e200f', '#7b2e15']


def grid_img(rows, ramp):
    """Render a 16-row shade map; '.' = transparent, letters index the ramp."""
    img = mc.new_img(16, 16, (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch == '.':
                continue
            idx = int(ch) if ch.isdigit() else ord(ch) - ord('a')
            img[y][x] = mc.hex2rgb(ramp[idx]) + (255,)
    return img


# ---------------------------------------------------------------------------
# Items — vanilla silhouettes + value structure, martian recolour
# ---------------------------------------------------------------------------
POTATO_GRID = [
    '................',
    '................',
    '................',
    '................',
    '........1111....',
    '.....11136541...',
    '....1346656651..',
    '...16777666550..',
    '..157887666520..',
    '..167886665330..',
    '..05476645330...',
    '..0345655300....',
    '...0244300......',
    '....0000........',
    '................',
    '................',
]

BAKED_GRID = [
    '................',
    '................',
    '................',
    '.......11111....',
    '.....11488661...',
    '....1886333331..',
    '...18633799751..',
    '...163799973361.',
    '..1437999738660.',
    '..1839975388620.',
    '..1637533886330.',
    '..135538846320..',
    '...0536866300...',
    '....0244300.....',
    '.....0000.......',
    '................',
]


def martian_potato():
    return grid_img(POTATO_GRID, POTATO)


def baked_martian_potato():
    return grid_img(BAKED_GRID, BAKED)


# ---------------------------------------------------------------------------
# Farmland blocks — vanilla tilled-furrow structure, regolith recolour
# ---------------------------------------------------------------------------
FARMLAND_DRY_GRID = [
    'eecacceeeecaceff',
    'feeaecefeeecaeef',
    'efeaceeeffecaefe',
    'ffcacceffeecccff',
    'fecbacceffecacef',
    'eeacaceeefcaccff',
    'feecaecedecaecef',
    'feecaeefeceacefc',
    'ffeaceeefccaceff',
    'efeacceffecaceff',
    'feccacfefeccaeef',
    'eebcecffffecacef',
    'fecacefffeecabee',
    'ffeaceefefeaccee',
    'efecaefeffeacefe',
    'efccaceefecacffe',
]

FARMLAND_MOIST_GRID = [
    'ccbabbccccbabcdd',
    'dccacbcdcccbaccd',
    'cdcabcccddcbacdc',
    'ddbabbcddccbbbdd',
    'dcbeabbcddcbabcd',
    'ccababcccdbabbdd',
    'ddbcacbcfcbacbcd',
    'dccbaccdcbcabcdb',
    'ddcabcccdbbabcdd',
    'cdcabbcddcbabcdd',
    'dcbbabdcdcbbaccd',
    'ccebcbddddcbabcd',
    'dcbabcdddccbaecc',
    'ddcabccdcdcabbcc',
    'cdcbacdcddcabcdc',
    'cdbbabccdcbabddc',
]

def regolith_farmland_dry():
    return grid_img(FARMLAND_DRY_GRID, FARM_DRY)


def regolith_farmland_moist():
    return grid_img(FARMLAND_MOIST_GRID, FARM_MOIST)


def regolith_farmland_side():
    """Pack regolith, pixel-identical to martian_regolith (vanilla rule:
    farmland side == base dirt). Blends seamlessly with adjacent regolith."""
    reg_path = os.path.join(BLOCK_DIR, 'martian_regolith.png')
    return mc.read_png(reg_path)


# ---------------------------------------------------------------------------
def write(name, img, subdir):
    path = os.path.join(subdir, name + '.png')
    mc.write_png(path, img)
    print('wrote', os.path.relpath(path, ROOT))


def main():
    os.makedirs(ITEM_DIR, exist_ok=True)
    os.makedirs(BLOCK_DIR, exist_ok=True)
    write('martian_potato', martian_potato(), ITEM_DIR)
    write('baked_martian_potato', baked_martian_potato(), ITEM_DIR)
    write('regolith_farmland_dry', regolith_farmland_dry(), BLOCK_DIR)
    write('regolith_farmland_moist', regolith_farmland_moist(), BLOCK_DIR)
    write('regolith_farmland_side', regolith_farmland_side(), BLOCK_DIR)
    print('martian farm set regenerated.')


if __name__ == '__main__':
    main()
