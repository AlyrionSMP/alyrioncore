#!/usr/bin/env python3
"""gen_item.py — vanilla-style Minecraft item sprite generator.

Vanilla item rules (Blockbench style guide + Compliance):
  * everything fits a 16x16 grid
  * start from the midtone shape, then a SIGNIFICANTLY darker 1px outline
  * light comes from the top-left: highlight top-left, shadow bottom-right
  * few shades per color; surface properties last (shine, cracks, roughness)
  * a couple of strong value steps read better at 16x16 than smooth gradients

Pure stdlib (uses mcutil.py). Presets cover common items; complex items
(swords, tools, armor) should be hand-drawn pixel by pixel — use this module's
primitives and the look-critique-fix loop from the generation skill.

Usage:  python3 gen_item.py <preset> [--seed N] [--out DIR]
        python3 gen_item.py list
"""

import argparse
import os
import random
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import mcutil as mc

ITEM_PALETTES = {
    'diamond': ['#1f9c90', '#3fd8c8', '#5ce4d6', '#79f0e4', '#ffffff'],
    'emerald': ['#11652f', '#1f9e4c', '#2db45c', '#3bca6c', '#b8ffd0'],
    'gold':    ['#6e4c14', '#9a7020', '#b0802a', '#c69034', '#ffe9a8'],
    'iron':    ['#6e6e76', '#b5b5bd', '#c4c4cc', '#d3d3db', '#ffffff'],
    'copper':  ['#5e2c20', '#8a4a34', '#9c5840', '#ae664c', '#ffd0b8'],
    'redstone':['#5e0e0e', '#9e1a1a', '#b02424', '#c22e2e', '#ffb0b0'],
    'lapis':   ['#0f2454', '#1a3a8a', '#2446a0', '#2e52b6', '#b0c8ff'],
    'emerald_item': ['#11652f', '#1f9e4c', '#2db45c', '#3bca6c', '#b8ffd0'],
    'stick':   ['#5c4526', '#7a5f38', '#8a6d42', '#9a7b4c', '#c9b28a'],
    'meteor':  ['#101a2a', '#1d3147', '#2b4866', '#4a6f94', '#7fb0d0'],
    'obsidian':['#0b0912', '#131020', '#1b172e', '#231e3c', '#2b254a'],
}


# The vanilla iron pickaxe silhouette, decoded from the 1.21 master and
# verified pixel-identical against it. H = head (iron), W = handle (wood),
# '.' = transparent. Same object, same angle, same proportions as vanilla —
# the Jappa checklist rule for custom tools.
PICKAXE = [
    '................',
    '................',
    '......HHHHH.....',
    '.....HHHHHHHWW..',
    '......HHHHHHWW..',
    '..........WHHH..',
    '.........WWWHHH.',
    '........WWW.HHH.',
    '.......WWW..HHH.',
    '......WWW...HHH.',
    '.....WWW....HHH.',
    '....WWW......H..',
    '...WWW..........',
    '..WWW...........',
    '..WW............',
    '................',
]


def _region_bbox(rows, ch):
    xs = [x for y, row in enumerate(rows) for x, c in enumerate(row) if c == ch]
    ys = [y for y, row in enumerate(rows) for x, c in enumerate(row) if c == ch]
    if not xs:
        return None
    return (min(xs), min(ys), max(xs), max(ys))


def _column_extremes(rows, ch):
    """per-column (top, bottom) and per-row (left, right) extremes of a region."""
    col_top, col_bot, row_left, row_right = {}, {}, {}, {}
    for y, row in enumerate(rows):
        for x, c in enumerate(row):
            if c != ch:
                continue
            col_top.setdefault(x, y)
            col_bot[x] = y
            row_left.setdefault(y, x)
            row_right[y] = x
    return col_top, col_bot, row_left, row_right


def pickaxe(head='meteor', handle='obsidian', seed=0,
            outline='#0c1118', craters=3, sparkle=True):
    """Meteor-iron pickaxe: vanilla silhouette, bevel-lit metal head with
    crater speckles and a sheen, dark obsidian handle, 1px darker outline.

    Implements the Blockbench item recipe in order:
    midtone shape -> darker outline -> highlight + shadow (top-left/bevel) ->
    palette detail -> surface properties (craters, sheen, sparkle).
    """
    head_ramp = ITEM_PALETTES[head]
    handle_ramp = ITEM_PALETTES[handle]
    outline_rgb = mc.hex2rgb(outline)
    img = mc.new_img(16, 16, (0, 0, 0, 0))
    mask = [[False] * 16 for _ in range(16)]

    hb = _region_bbox(PICKAXE, 'H')
    hx0, hy0, hx1, hy1 = hb
    ct, cb, rl, rr = _column_extremes(PICKAXE, 'H')
    band_mid = (hx0 + hx1) // 2

    def head_shade(x, y):
        if ct[x] == y:
            return head_ramp[1]                    # bevel top (dark edge)
        if cb[x] == y:
            return head_ramp[0]                    # bottom edge (shadow)
        if rr[y] == x:
            return head_ramp[0]                    # right edge (shadow)
        if rl[y] == x:
            return head_ramp[3]                    # left edge (light)
        if ct[x] + 1 == y:                         # bright band under bevel
            return head_ramp[4] if x <= band_mid else head_ramp[3]
        return head_ramp[2]                        # interior midtone

    for y in range(16):
        for x in range(16):
            c = PICKAXE[y][x]
            if c == '.':
                continue
            mask[y][x] = True
            if c == 'H':
                img[y][x] = mc.hex2rgb(head_shade(x, y)) + (255,)
            else:
                img[y][x] = mc.hex2rgb(handle_ramp[2]) + (255,)

    # handle: cylinder shading — light left edge, dark right edge, mid interior
    for y in range(16):
        wx = [x for x in range(16) if PICKAXE[y][x] == 'W']
        if not wx:
            continue
        for i, x in enumerate(wx):
            if i == 0:
                img[y][x] = mc.hex2rgb(handle_ramp[3]) + (255,)
            elif i == len(wx) - 1:
                img[y][x] = mc.hex2rgb(handle_ramp[0]) + (255,)
            else:
                img[y][x] = mc.hex2rgb(handle_ramp[2]) + (255,)

    # surface properties: meteor craters (darker pits), sheen, sparkle
    rnd = random.Random(seed)
    interior = [(x, y) for y in range(16) for x in range(16)
                if PICKAXE[y][x] == 'H'
                and ct[x] != y and cb[x] != y and rr[y] != x and rl[y] != x
                and ct[x] + 1 != y]
    rnd.shuffle(interior)
    for (x, y) in interior[:craters]:
        img[y][x] = mc.mix(mc.hex2rgb(head_ramp[0]), mc.hex2rgb(head_ramp[1]), 0.4) + (255,)
    for (x, y) in interior[craters:craters + 2]:
        img[y][x] = mc.hex2rgb(head_ramp[4]) + (255,)          # sheen specks
    if sparkle:
        for (x, y) in ((6, 3), (13, 10)):
            if PICKAXE[y][x] == 'H':
                img[y][x] = (255, 255, 255, 255)

    mc.outline_mask(img, mask, outline_rgb + (255,), inside=True)
    return img


def gem(seed=0, palette='diamond', shape='facet'):
    """Faceted gem sprite: kite/facet silhouette, top-left bright facet,
    bottom-right dark facet, dark outline, one sparkle pixel."""
    ramp = ITEM_PALETTES[palette]
    img = mc.new_img(16, 16, (0, 0, 0, 0))
    cx, cy = 8, 8
    mask = [[False] * 16 for _ in range(16)]
    # kite: top point, wide middle, bottom point
    for y in range(16):
        for x in range(16):
            dx = abs(x - cx) / 5.0
            top_half = (y - cy) / 6.0
            bottom_half = (y - cy) / 5.5
            half = top_half if y < cy else bottom_half
            if abs(half) <= 1.0 and dx <= (1.0 - abs(half)) * 1.0 + 0.2:
                mask[y][x] = True
    # facets: quadrants get different shades
    for y in range(16):
        for x in range(16):
            if not mask[y][x]:
                continue
            if x < cx and y < cy:
                c = ramp[3]      # top-left: bright
            elif x >= cx and y < cy:
                c = ramp[2]      # top-right: mid-bright
            elif x < cx and y >= cy:
                c = ramp[1]      # bottom-left: mid-dark
            else:
                c = ramp[0]      # bottom-right: darkest
            img[y][x] = mc.hex2rgb(c) + (255,)
    mc.outline_mask(img, mask, mc.hex2rgb(ramp[0]) + (255,), inside=True)
    img[6][6] = (255, 255, 255, 255)   # sparkle
    img[7][6] = (255, 255, 255, 255)
    return img


def ingot(seed=0, metal='gold'):
    """Ingot: rounded bar with bevel; top face bright, side dark, top-left shine."""
    ramp = ITEM_PALETTES[metal]
    img = mc.new_img(16, 16, (0, 0, 0, 0))
    # body (rounded rect)
    for y in range(4, 12):
        for x in range(3, 13):
            edge = (y in (4, 11) or x in (3, 12))
            img[y][x] = mc.hex2rgb(ramp[1] if edge else ramp[2]) + (255,)
    # top face (bright)
    for y in range(4, 7):
        for x in range(4, 12):
            img[y][x] = mc.hex2rgb(ramp[3]) + (255,)
    # bevel highlight top-left
    img[4][4] = mc.hex2rgb(ramp[4]) + (255,)
    img[4][5] = mc.hex2rgb(ramp[4]) + (255,)
    img[5][4] = mc.hex2rgb(ramp[4]) + (255,)
    # dark bottom edge
    for x in range(4, 12):
        img[11][x] = mc.hex2rgb(ramp[0]) + (255,)
    return img


def plank_item(seed=0, wood='oak'):
    """A single plank item: midtone board, dark 1px border, faint grain."""
    ramp = mc.PALETTES[wood + '_planks']
    img = mc.new_img(16, 16, (0, 0, 0, 0))
    for y in range(2, 14):
        for x in range(2, 14):
            img[y][x] = mc.hex2rgb(ramp[2]) + (255,)
    for i in range(16):
        img[2][i] = mc.hex2rgb(ramp[0]) + (255,)
        img[i][2] = mc.hex2rgb(ramp[0]) + (255,)
        img[13][i] = mc.hex2rgb(ramp[0]) + (255,)
        img[i][13] = mc.hex2rgb(ramp[0]) + (255,)
    # top-left edge light
    for i in range(3, 13):
        img[3][i] = mc.hex2rgb(ramp[3]) + (255,)
        img[i][3] = mc.hex2rgb(ramp[3]) + (255,)
    # grain
    rnd = random.Random(seed)
    for _ in range(6):
        x = rnd.randint(4, 12)
        y = rnd.randint(4, 12)
        img[y][x] = mc.hex2rgb(ramp[1]) + (255,)
    return img


def stick(seed=0):
    """Diagonal stick with darker outline."""
    ramp = ITEM_PALETTES['stick']
    img = mc.new_img(16, 16, (0, 0, 0, 0))
    for i in range(14):
        x, y = 1 + i, 13 - i
        if 0 <= x < 16 and 0 <= y < 16:
            img[y][x] = mc.hex2rgb(ramp[2]) + (255,)
            if x > 0:
                img[y][x - 1] = mc.mix(mc.hex2rgb(ramp[2]), mc.hex2rgb(ramp[0]), 0.6) + (255,)
    return img


PRESETS = {'gem': gem, 'ingot': ingot, 'plank': plank_item, 'stick': stick,
           'pickaxe': pickaxe}


def main():
    ap = argparse.ArgumentParser(description='vanilla-style Minecraft item sprite generator')
    ap.add_argument('preset', help='preset name or "list"')
    ap.add_argument('--seed', type=int, default=0)
    ap.add_argument('--palette', default='diamond',
                    choices=sorted(ITEM_PALETTES))
    ap.add_argument('--head', default='meteor',
                    choices=sorted(ITEM_PALETTES),
                    help='pickaxe head material palette (default: meteor)')
    ap.add_argument('--handle', default='obsidian',
                    choices=sorted(ITEM_PALETTES),
                    help='pickaxe handle material palette (default: obsidian)')
    ap.add_argument('--wood', default='oak',
                    choices=sorted(k.replace('_planks', '') for k in mc.PALETTES if k.endswith('_planks')))
    ap.add_argument('--out', default='.')
    args = ap.parse_args()

    if args.preset == 'list':
        print('item presets:', ', '.join(sorted(PRESETS)))
        print('palettes:', ', '.join(sorted(ITEM_PALETTES)))
        return 0

    os.makedirs(args.out, exist_ok=True)
    if args.preset == 'gem':
        img = gem(args.seed, args.palette)
        name = args.palette + '_gem'
    elif args.preset == 'ingot':
        img = ingot(args.seed, args.palette)
        name = args.palette + '_ingot'
    elif args.preset == 'plank':
        img = plank_item(args.seed, args.wood)
        name = args.wood + '_plank'
    elif args.preset == 'stick':
        img = stick(args.seed)
        name = 'stick'
    elif args.preset == 'pickaxe':
        img = pickaxe(args.head, args.handle, args.seed)
        name = args.head + '_pickaxe'
    else:
        print('unknown preset %r — try "list"' % args.preset, file=sys.stderr)
        return 2

    path = os.path.join(args.out, name + '.png')
    mc.write_png(path, img)
    print('wrote', path)
    print(mc.palette_report(img))
    return 0


if __name__ == '__main__':
    sys.exit(main())
