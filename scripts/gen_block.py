#!/usr/bin/env python3
"""gen_block.py — vanilla-style Minecraft block texture generator.

Pure stdlib (uses mcutil.py). Recipes encode the vanilla design language:

  * stone / rock families: low-frequency fBm grain mapped onto a 5-shade
    hue-shifted ramp + sparse 1px cracks + a few darker blotches.
  * ores: the stone base with gem crystal clusters embedded in it — every
    crystal is a small bright core, a mid casing, a darker seam where it meets
    the stone, and (for shiny gems) a 1px white sparkle. No floating gems.
  * wood: planks are 2-3 strips with vertical grain and a 1px dark seam; logs
    get vertical bark grain and concentric top rings.
  * sand / gravel / dirt: heavy dithering between adjacent ramp shades (sand is
    the canonical dithered vanilla texture); gravel mixes pebble blotches.
  * glass: translucent tint, 1px frame (light top-left, dark bottom-right),
    a couple of diagonal shine pixels.
  * bricks: offset courses, per-brick value variation, 1px mortar, top-edge
    highlight per brick.

Usage:  python3 gen_block.py <recipe> [--seed N] [--out DIR] [--scale N] ...
        python3 gen_block.py list          # list recipes + palettes
        python3 gen_block.py preview ...   # see preview.py for mosaic/wall tools
"""

import argparse
import os
import random
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import mcutil as mc
from mcutil import PALETTES

# Ore gems: (gem ramp, casing ramp, sparkle bool, deep casing hex)
ORES = {
    'coal':      (PALETTES['coal'], PALETTES['stone'], False, '#1c1c1c'),
    'iron':      (PALETTES['diorite'], PALETTES['stone'], False, '#9c9c9c'),
    'gold':      (PALETTES['gold_ore_gem'], PALETTES['stone'], True, '#6e4c14'),
    'copper':    (PALETTES['copper'], PALETTES['stone'], False, '#5e2c20'),
    'diamond':   (PALETTES['diamond'], PALETTES['stone'], True, '#1f9c90'),
    'emerald':   (PALETTES['emerald'], PALETTES['stone'], True, '#11652f'),
    'redstone':  (PALETTES['redstone'], PALETTES['stone'], False, '#5e0e0e'),
    'lapis':     (PALETTES['lapis'], PALETTES['stone'], False, '#0f2454'),
    'netherite': (PALETTES['netherite'], PALETTES['stone'], True, '#221e24'),
    'amethyst':  (PALETTES['amethyst'], PALETTES['stone'], True, '#4a2a8a'),
    'quartz':    (PALETTES['quartz'], PALETTES['netherrack'], False, '#b0a896'),
}

# ---------------------------------------------------------------------------
# Recipes
# ---------------------------------------------------------------------------

def _stone_base(seed, ramp=None, crack_count=3, blotch=True):
    ramp = ramp or PALETTES['stone']
    n = mc.fbm(16, 16, seed=seed, octaves=4, base_freq=2.5)
    img = mc.noise_to_ramp(n, ramp, dither=True, seed=seed)
    mc.cracks(img, seed + 7, count=crack_count, color=(30, 28, 26, 255))
    if blotch:
        b = mc.fbm(16, 16, seed=seed + 13, octaves=3, base_freq=1.5)
        blobs = mc.blotches(b, 0.62, 0.72, [ramp[0], ramp[1]], seed=seed)
        for y in range(16):
            for x in range(16):
                if blobs[y][x][3] == 255:
                    img[y][x] = mc.mix(img[y][x], blobs[y][x], 0.6)
    return img


def stone(seed):
    return _stone_base(seed)


def deepslate(seed):
    return _stone_base(seed, ramp=PALETTES['deepslate'], crack_count=4)


def andesite(seed):
    img = _stone_base(seed, ramp=PALETTES['andesite'], crack_count=1, blotch=False)
    n = mc.value_noise(16, 16, seed=seed + 5, freq=5.0)
    specks = mc.blotches(n, 0.35, 0.65, [PALETTES['andesite'][3]], seed=seed)
    for y in range(16):
        for x in range(16):
            if specks[y][x][3] == 255 and (x + y) % 2 == 0:
                img[y][x] = mc.mix(img[y][x], specks[y][x], 0.7)
    return img


def diorite(seed):
    return _stone_base(seed, ramp=PALETTES['diorite'], crack_count=1, blotch=False)


def granite(seed):
    img = _stone_base(seed, ramp=PALETTES['granite'], crack_count=1, blotch=False)
    n = mc.value_noise(16, 16, seed=seed + 5, freq=6.0)
    specks = mc.blotches(n, 0.3, 0.7, [PALETTES['granite'][0], PALETTES['granite'][1]], seed=seed)
    for y in range(16):
        for x in range(16):
            if specks[y][x][3] == 255:
                img[y][x] = mc.mix(img[y][x], specks[y][x], 0.8)
    return img


def cobble(seed):
    """Individual stones: irregular blobs, 1px darker mortar gaps, each stone
    shaded brighter toward its top-left (vanilla top-left light rule)."""
    rnd = random.Random(seed)
    img = mc.new_img(16, 16, mc.hex2rgb(PALETTES['cobble'][0]) + (255,))
    mortar = (38, 36, 34, 255)
    stones = []
    for _ in range(9):
        cx, cy = rnd.uniform(3, 13), rnd.uniform(3, 13)
        rx, ry = rnd.uniform(1.8, 3.6), rnd.uniform(1.4, 3.0)
        stones.append((cx, cy, rx, ry))
    # draw mortar as the background by punching stone blobs
    mask = [[False] * 16 for _ in range(16)]
    for (cx, cy, rx, ry) in stones:
        for y in range(16):
            for x in range(16):
                dx = (x - cx) / rx
                dy = (y - cy) / ry
                if dx * dx + dy * dy <= 1.0:
                    mask[y][x] = True
    for y in range(16):
        for x in range(16):
            if not mask[y][x]:
                img[y][x] = mortar
    for i, (cx, cy, rx, ry) in enumerate(stones):
        s = rnd.choice(range(3, 6))
        ramp = PALETTES['cobble'][:s + 1] if s < len(PALETTES['cobble']) else PALETTES['cobble']
        for y in range(16):
            for x in range(16):
                dx = (x - cx) / rx
                dy = (y - cy) / ry
                if dx * dx + dy * dy <= 1.0:
                    # value falls off from top-left toward bottom-right
                    t = 0.25 + 0.5 * ((x - cx) / rx + (y - cy) / ry) * 0.5
                    t = max(0.0, min(1.0, t))
                    idx = int(t * (len(ramp) - 1))
                    img[y][x] = mc.hex2rgb(ramp[idx]) + (255,)
    # 1px dark seam around the stones (inner outline)
    mc.outline_mask(img, mask, (52, 50, 48, 255), inside=True)
    return img


def planks(wood='oak', seed=0):
    """2 vertical planks with grain, 1px dark seams, per-plank value variation."""
    ramp = PALETTES[wood + '_planks']
    rnd = random.Random(seed)
    img = mc.new_img(16, 16, mc.hex2rgb(ramp[2]) + (255,))
    seam = (34, 26, 16, 255)
    split = rnd.choice((7, 8, 9))
    offsets = [rnd.uniform(-0.15, 0.15), rnd.uniform(-0.1, 0.1)]
    for plank_i, (x0, x1) in enumerate(((0, split), (split + 1, 15))):
        grain = mc.fbm(16, 16, seed=seed + plank_i, octaves=3, base_freq=1.2)
        for y in range(16):
            for x in range(x0, x1 + 1):
                # vertical grain: stretch noise horizontally (sample with low x-freq)
                g = grain[y][min(x, 15)]
                t = 0.25 + 0.5 * g + offsets[plank_i]
                t = max(0.0, min(1.0, t))
                idx = int(t * (len(ramp) - 1))
                img[y][x] = mc.hex2rgb(ramp[idx]) + (255,)
        # plank seam
        for y in range(16):
            img[y][split] = seam
            img[y][split + 1] = seam
    return img


def log(wood='oak', seed=0):
    """Bark side + top rings, both tileable."""
    bark = PALETTES[wood + '_bark']
    img = mc.new_img(16, 16, mc.hex2rgb(bark[2]) + (255,))
    n = mc.fbm(16, 16, seed=seed, octaves=3, base_freq=1.5)
    for y in range(16):
        for x in range(16):
            g = n[y][x] + 0.2 * (mc._hash2(x, 0, seed) - 0.5)
            g = max(0.0, min(1.0, g))
            idx = int(g * (len(bark) - 1))
            img[y][x] = mc.hex2rgb(bark[idx]) + (255,)
    # vertical bark ridges: every 4th column slightly darker/lighter
    for x in range(0, 16, 4):
        for y in range(16):
            img[y][x] = mc.hex2rgb(bark[1]) + (255,)
    return img


def log_top(wood='oak', seed=0):
    """Concentric rings viewed from above."""
    ramp = PALETTES[wood + '_bark']
    img = mc.new_img(16, 16, mc.hex2rgb(ramp[3]) + (255,))
    for y in range(16):
        for x in range(16):
            d = math_dist(x, y, 7.5, 7.5)
            ring = int(d)
            # alternate ring bands with a little noise so they aren't perfect circles
            n = mc._hash2(x, y, seed)
            shade = ramp[1] if ring % 2 == 0 else ramp[3]
            img[y][x] = mc.mix(mc.hex2rgb(shade), mc.hex2rgb(ramp[2]), 0.3 + 0.3 * n) + (255,)
    return img


def math_dist(x, y, cx, cy):
    import math
    return math.sqrt((x - cx) ** 2 + (y - cy) ** 2)


def ore(gem='diamond', seed=0):
    """Stone base + embedded crystal clusters. Crystals are bright-core blobs
    with a darker casing seam against the stone and optional sparkle."""
    base = _stone_base(seed, ramp=PALETTES['stone'])
    gem_ramp, casing, sparkle, deep = ORES[gem]
    rnd = random.Random(seed + 99)
    n = mc.fbm(16, 16, seed=seed + 3, octaves=3, base_freq=2.0)
    for cluster in range(rnd.randint(3, 5)):
        cx, cy = rnd.uniform(2, 14), rnd.uniform(2, 14)
        r = rnd.uniform(1.4, 2.6)
        mask = [[False] * 16 for _ in range(16)]
        for y in range(16):
            for x in range(16):
                dx = (x - cx) / r
                dy = (y - cy) / r
                d2 = dx * dx + dy * dy
                if d2 <= 1.0:
                    mask[y][x] = True
        for y in range(16):
            for x in range(16):
                if not mask[y][x]:
                    continue
                dx = (x - cx) / r
                dy = (y - cy) / r
                d2 = dx * dx + dy * dy
                if d2 <= 0.18:            # bright core
                    base[y][x] = mc.hex2rgb(gem_ramp[4]) + (255,)
                elif d2 <= 0.5:            # mid facet
                    base[y][x] = mc.hex2rgb(gem_ramp[2]) + (255,)
                elif d2 <= 0.8:            # outer facet
                    base[y][x] = mc.hex2rgb(gem_ramp[0]) + (255,)
                else:                      # deep casing seam vs stone
                    base[y][x] = mc.hex2rgb(deep) + (255,)
        mc.outline_mask(base, mask, mc.hex2rgb(deep) + (255,), inside=True)
        if sparkle:
            sx, sy = int(cx - r * 0.3), int(cy - r * 0.3)
            if 0 <= sx < 16 and 0 <= sy < 16:
                base[sy][sx] = (255, 255, 255, 255)
    return base


def sand(seed):
    """The canonical dithered texture: 2-3 shades checker-dithered, very low
    contrast — vanilla sand is basically dithering between two tans."""
    ramp = PALETTES['sand']
    img = mc.new_img(16, 16, mc.hex2rgb(ramp[2]) + (255,))
    mc.checker_dither(img, mc.hex2rgb(ramp[1]), mc.hex2rgb(ramp[2]))
    n = mc.value_noise(16, 16, seed=seed, freq=3.0)
    for y in range(16):
        for x in range(16):
            if n[y][x] > 0.72:
                img[y][x] = mc.hex2rgb(ramp[3]) + (255,)
            elif n[y][x] < 0.28:
                img[y][x] = mc.hex2rgb(ramp[1]) + (255,)
    return img


def gravel(seed):
    """Pebble mix: blotches of 2-3 gray-brown tones with 1px darker seams."""
    rnd = random.Random(seed)
    img = mc.new_img(16, 16, mc.hex2rgb(PALETTES['gravel'][2]) + (255,))
    tones = [PALETTES['gravel'][1], PALETTES['gravel'][2], PALETTES['gravel'][3]]
    for _ in range(14):
        cx, cy = rnd.uniform(1, 15), rnd.uniform(1, 15)
        r = rnd.uniform(0.7, 1.8)
        col = mc.hex2rgb(rnd.choice(tones)) + (255,)
        mc.ellipse(img, cx, cy, r, r * rnd.uniform(0.7, 1.2), col)
    mc.cracks(img, seed + 1, count=2, color=(50, 48, 46, 255), max_len=5)
    return img


def dirt(seed):
    img = _stone_base(seed, ramp=PALETTES['dirt'], crack_count=2, blotch=True)
    return img


def grass_top(seed):
    """Green tufted top: base green, short darker/lighter tufts, a few 1-2px
    blades. Vanilla relies on the biome colormap for hue; keep the value range
    narrow here so tinting works."""
    img = mc.new_img(16, 16, mc.hex2rgb(PALETTES['grass_top'][2]) + (255,))
    n = mc.fbm(16, 16, seed=seed, octaves=3, base_freq=2.0)
    for y in range(16):
        for x in range(16):
            v = n[y][x]
            if v < 0.35:
                img[y][x] = mc.hex2rgb(PALETTES['grass_top'][1]) + (255,)
            elif v > 0.68:
                img[y][x] = mc.hex2rgb(PALETTES['grass_top'][3]) + (255,)
    # short blades: vertical 1-2px tufts
    rnd = random.Random(seed + 5)
    for _ in range(10):
        x = rnd.randrange(16)
        y = rnd.randrange(16)
        hgt = rnd.choice((1, 2))
        col = mc.hex2rgb(rnd.choice((PALETTES['grass_top'][3], PALETTES['grass_top'][4]))) + (255,)
        for dy in range(hgt):
            img[(y - dy) % 16][x] = col
    return img


def glass(seed=0):
    """Translucent pane: very low alpha tint, 1px frame lighter on top-left
    and darker on bottom-right (the classic vanilla glass look), a few shine
    pixels. Tiles because the frame wraps the full 16x16 edge."""
    tint = mc.hex2rgb(PALETTES['glass'][1])
    img = mc.new_img(16, 16, (tint[0], tint[1], tint[2], 26))
    light = (235, 250, 252, 150)
    dark = (120, 150, 160, 150)
    for i in range(16):
        img[0][i] = light
        img[i][0] = light
        img[15][i] = dark
        img[i][15] = dark
    img[1][1] = (255, 255, 255, 200)
    img[2][1] = (255, 255, 255, 160)
    img[1][2] = (255, 255, 255, 160)
    img[14][14] = (100, 130, 140, 160)
    return img


def bricks(seed=0, ramp=None, mortar=(188, 180, 168, 255)):
    ramp = ramp or PALETTES['brick']
    rnd = random.Random(seed)
    img = mc.new_img(16, 16, mc.hex2rgb(ramp[2]) + (255,))
    for y in range(16):
        row = y // 4
        offset = 0 if row % 2 == 0 else 4
        for x in range(16):
            if (y % 4) == 3 or ((x + offset) % 8) == 7:
                img[y][x] = mortar
    # per-brick value variation + top-edge highlight
    for row in range(4):
        offset = 0 if row % 2 == 0 else 4
        for bx in range(2):
            x0 = bx * 8 + offset
            if x0 >= 16:
                continue
            x1 = min(x0 + 6, 15)
            y0, y1 = row * 4, row * 4 + 2
            v = rnd.uniform(-1, 1)
            for y in range(y0, y1 + 1):
                for x in range(x0, x1 + 1):
                    if img[y][x] == mortar:
                        continue
                    t = 0.5 + 0.12 * v
                    base = mc.hex2rgb(ramp[2])
                    img[y][x] = mc.mix(base, mc.hex2rgb(ramp[2 + int(v)]) if v > 0 else mc.hex2rgb(ramp[1]), abs(v) * 0.8) + (255,)
            # top edge highlight
            for x in range(x0, x1 + 1):
                if img[y0][x] != mortar:
                    img[y0][x] = mc.hex2rgb(ramp[3]) + (255,)
    return img


def netherrack(seed):
    return _stone_base(seed, ramp=PALETTES['netherrack'], crack_count=5, blotch=True)


def glowstone(seed):
    """Emissive-looking block: yellow blobs with bright cores and dark gaps."""
    rnd = random.Random(seed)
    img = mc.new_img(16, 16, mc.hex2rgb(PALETTES['glowstone'][0]) + (255,))
    n = mc.fbm(16, 16, seed=seed, octaves=3, base_freq=2.2)
    for y in range(16):
        for x in range(16):
            v = n[y][x]
            if v < 0.3:
                img[y][x] = mc.hex2rgb(PALETTES['glowstone'][0]) + (255,)
            elif v < 0.6:
                img[y][x] = mc.hex2rgb(PALETTES['glowstone'][2]) + (255,)
            elif v < 0.85:
                img[y][x] = mc.hex2rgb(PALETTES['glowstone'][3]) + (255,)
            else:
                img[y][x] = mc.hex2rgb(PALETTES['glowstone'][4]) + (255,)
    return img


# ---------------------------------------------------------------------------
# CLI
# ---------------------------------------------------------------------------

RECIPES = {
    'stone': stone, 'deepslate': deepslate, 'andesite': andesite,
    'diorite': diorite, 'granite': granite, 'cobble': cobble,
    'sand': sand, 'gravel': gravel, 'dirt': dirt, 'grass_top': grass_top,
    'glass': glass, 'bricks': bricks, 'netherrack': netherrack,
    'glowstone': glowstone, 'log': log, 'log_top': log_top,
}


def main():
    ap = argparse.ArgumentParser(description='vanilla-style Minecraft block texture generator')
    ap.add_argument('recipe', help='recipe name or "list"')
    ap.add_argument('--seed', type=int, default=0)
    ap.add_argument('--wood', default='oak', choices=[k.replace('_planks', '') for k in PALETTES if k.endswith('_planks')])
    ap.add_argument('--gem', default='diamond', choices=list(ORES.keys()))
    ap.add_argument('--out', default='.', help='output directory')
    args = ap.parse_args()

    if args.recipe == 'list':
        print('block recipes:', ', '.join(sorted(RECIPES)))
        print('ore recipes:', ', '.join(sorted(ORES)))
        print('woods:', ', '.join(sorted(k.replace('_planks', '') for k in PALETTES if k.endswith('_planks'))))
        print('palettes:', ', '.join(sorted(PALETTES)))
        return 0

    os.makedirs(args.out, exist_ok=True)
    if args.recipe == 'planks':
        img = planks(args.wood, args.seed)
        name = args.wood + '_planks'
    elif args.recipe == 'ore':
        img = ore(args.gem, args.seed)
        name = args.gem + '_ore'
    elif args.recipe == 'log':
        img = log(args.wood, args.seed)
        name = args.wood + '_log'
    elif args.recipe == 'log_top':
        img = log_top(args.wood, args.seed)
        name = args.wood + '_log_top'
    elif args.recipe in RECIPES:
        img = RECIPES[args.recipe](args.seed)
        name = args.recipe
    else:
        print('unknown recipe %r — try "list"' % args.recipe, file=sys.stderr)
        return 2

    path = os.path.join(args.out, name + '.png')
    mc.write_png(path, img)
    print('wrote', path)
    print(mc.palette_report(img))
    return 0


if __name__ == '__main__':
    sys.exit(main())
