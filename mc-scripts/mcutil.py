"""mcutil.py — core toolkit for vanilla-style Minecraft texture generation.

Pure Python 3 standard library ONLY (zlib, struct, math, random, json, os, sys).
No Pillow, no numpy, no pip installs — runs on any stock Python 3.

Conventions
-----------
An *image* is a list of rows; each row is a list of (r, g, b, a) tuples with
values 0..255. 16x16 is the vanilla block-face size; everything in this toolkit
is tileable by construction (all noise is toroidal / wrap-aware).

Design rules baked in (from the vanilla-style research):
  * limited palettes — 2..5 shades per material ramp, hue-shifted
  * toroidal value noise / fBm for natural grain; never flat fills
  * no pure black or white; shadows keep hue, highlights desaturate slightly
  * 1px features (outlines, seams, cracks) — the vanilla/Compliance rule
  * generated textures are always tileable (wrap-aware sampling)
"""

import math
import random
import struct
import zlib

# ---------------------------------------------------------------------------
# Vanilla-inspired palettes (5-shade ramps, darkest -> lightest).
# These approximate the actual vanilla textures; the recipes in gen_block.py
# are the important part — swap in exact vanilla hexes when you have a
# reference texture to sample from (see the design skill).
# ---------------------------------------------------------------------------

PALETTES = {
    'stone':      ['#4b4b4b', '#5b5b5b', '#6c6c6c', '#7d7d7d', '#8e8e8e'],
    'deepslate':  ['#38373d', '#45434c', '#53505c', '#605d6b', '#6e6b7b'],
    'andesite':   ['#636363', '#717171', '#808080', '#8e8e8e', '#9d9d9d'],
    'diorite':    ['#b3b3b3', '#c2c2c2', '#d0d0d0', '#dfdfdf', '#ededed'],
    'granite':    ['#8a5a52', '#9b6a61', '#ac7b71', '#bd8b80', '#ce9c90'],
    'cobble':     ['#565656', '#666666', '#767676', '#868686', '#969696'],
    'moss':       ['#4a6a3a', '#5a7c49', '#6b8e58', '#7ca067', '#8db276'],
    'dirt':       ['#5f4028', '#6f4d31', '#7f5a3a', '#8f6743', '#9f744c'],
    'grass_top':  ['#4f8a3a', '#5f9c49', '#6fae58', '#7fc067', '#8fd276'],
    'sand':       ['#c2ac74', '#cdb887', '#d8c49a', '#e3d0ad', '#eedcc0'],
    'gravel':     ['#6d6a68', '#7c7976', '#8b8884', '#9a9792', '#a9a6a0'],
    'clay':       ['#8e8e96', '#9d9da5', '#acacb4', '#bbbbc3', '#cacad2'],
    'snow':       ['#c9d6e8', '#d6e0ee', '#e3eaf4', '#f0f4fa', '#fdfeff'],
    'oak_planks': ['#6a4e2e', '#795c37', '#886a40', '#977849', '#a68652'],
    'spruce_planks': ['#4d3823', '#5b442b', '#695033', '#775c3b', '#856843'],
    'birch_planks': ['#b5ad8e', '#c3bb9d', '#d1c9ac', '#dfd7bb', '#ede5ca'],
    'jungle_planks': ['#7a5a44', '#8a6a4e', '#9a7a58', '#aa8a62', '#ba9a6c'],
    'acacia_planks': ['#8a4a38', '#9c5843', '#ae664e', '#c07459', '#d28264'],
    'dark_oak_planks': ['#3e2d1a', '#4b3820', '#584326', '#654e2c', '#725932'],
    'mangrove_planks': ['#6a3a38', '#7a4744', '#8a5450', '#9a615c', '#aa6e68'],
    'cherry_planks': ['#c98a80', '#d79a90', '#e5aaa0', '#f3bab0', '#ffcac0'],
    'crimson_planks': ['#5e2a3c', '#6e3448', '#7e3e54', '#8e4860', '#9e526c'],
    'warped_planks': ['#2c5c66', '#366c76', '#407c86', '#4a8c96', '#549ca6'],
    'bamboo_planks': ['#9a8c4a', '#aa9c58', '#baac66', '#cabc74', '#cacc82'],
    'oak_bark':   ['#4e3d2a', '#5c4a35', '#6a5740', '#78644b', '#867156'],
    'spruce_bark': ['#3a3026', '#463c30', '#52483a', '#5e5444', '#6a604e'],
    'birch_bark': ['#c2c2bc', '#d0d0ca', '#deded8', '#ecece6', '#fafaf4'],
    'netherrack': ['#5e2426', '#6e2e30', '#7e383a', '#8e4244', '#9e4c4e'],
    'soul_sand':  ['#4a403c', '#574c48', '#645854', '#716460', '#7e706c'],
    'brick':      ['#7e3c32', '#8e4a3e', '#9e584a', '#ae6656', '#be7462'],
    'nether_brick': ['#341f22', '#3f2729', '#4a2f30', '#553737', '#603f3e'],
    'iron':       ['#b5b5bd', '#c4c4cc', '#d3d3db', '#e2e2ea', '#f1f1f9'],
    'gold':       ['#9a7020', '#b0802a', '#c69034', '#dca03e', '#f2b048'],
    'copper':     ['#8a4a34', '#9c5840', '#ae664c', '#c07458', '#d28264'],
    'diamond':    ['#3fd8c8', '#5ce4d6', '#79f0e4', '#96fcf2', '#b3ffff'],
    'emerald':    ['#1f9e4c', '#2db45c', '#3bca6c', '#49e07c', '#57f68c'],
    'redstone':   ['#9e1a1a', '#b02424', '#c22e2e', '#d43838', '#e64242'],
    'lapis':      ['#1a3a8a', '#2446a0', '#2e52b6', '#385ecc', '#426ae2'],
    'coal':       ['#2a2a2a', '#353535', '#404040', '#4b4b4b', '#565656'],
    'gold_ore_gem': ['#8a6218', '#a07420', '#b68628', '#cc9830', '#e2aa38'],
    'netherite':  ['#3a3438', '#464046', '#524c54', '#5e5862', '#6a6470'],
    'amethyst':   ['#7a4ad8', '#8e5ee8', '#a272f8', '#b686ff', '#ca9aff'],
    'glowstone':  ['#8a6a1a', '#a08020', '#b69626', '#ccac2c', '#e2c232'],
    'quartz':     ['#d6d0c6', '#e0dacf', '#eae4d8', '#f4eee1', '#fef8ea'],
    'obsidian':   ['#100e16', '#1a1722', '#24202e', '#2e293a', '#383246'],
    'glass':      ['#b8d8e0', '#c8e4ea', '#d8f0f4', '#e8fcfe', '#f8ffff'],
    'wool':       ['#c2c2c2', '#d0d0d0', '#dedede', '#ececec', '#fafafa'],
}

# ---------------------------------------------------------------------------
# Color helpers
# ---------------------------------------------------------------------------

def hex2rgb(h):
    h = h.lstrip('#')
    if len(h) == 3:
        h = ''.join(c * 2 for c in h)
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16))


def rgb2hex(c):
    return '#%02x%02x%02x' % tuple(int(v) for v in c[:3])


def clamp(v, lo=0, hi=255):
    return lo if v < lo else hi if v > hi else v


def lerp(a, b, t):
    return a + (b - a) * t


def mix(c1, c2, t):
    """Blend two (r,g,b[,a]) colors. Shorter tuples are padded with 255 so a
    3-tuple and a 4-tuple can be blended (result keeps the longer length)."""
    n = max(len(c1), len(c2))
    pad = lambda c: c + (255,) * (n - len(c))  # noqa: E731
    a, b = pad(c1), pad(c2)
    return tuple(int(round(lerp(a[i], b[i], t))) for i in range(n))


def rgb2hsv(c):
    r, g, b = (v / 255.0 for v in c[:3])
    mx, mn = max(r, g, b), min(r, g, b)
    d = mx - mn
    h = 0.0
    if d:
        if mx == r:
            h = ((g - b) / d) % 6
        elif mx == g:
            h = (b - r) / d + 2
        else:
            h = (r - g) / d + 4
        h *= 60.0
    s = 0.0 if mx == 0 else d / mx
    return (h, s, mx)


def hsv2rgb(hsv):
    h, s, v = hsv
    h = h % 360.0
    c = v * s
    x = c * (1 - abs((h / 60.0) % 2 - 1))
    m = v - c
    if h < 60:
        r, g, b = c, x, 0
    elif h < 120:
        r, g, b = x, c, 0
    elif h < 180:
        r, g, b = 0, c, x
    elif h < 240:
        r, g, b = 0, x, c
    elif h < 300:
        r, g, b = x, 0, c
    else:
        r, g, b = c, 0, x
    return (int(round((r + m) * 255)), int(round((g + m) * 255)), int(round((b + m) * 255)))


def ramp(base_hex, steps=5, hue_shift=6.0, sat_dark=0.06, sat_light=-0.12, val_step=0.16):
    """Build a hue-shifted color ramp from a midtone.

    The base sits in the middle of the ramp. Shadows go darker AND slightly more
    saturated (hue nudged by -hue_shift), highlights go brighter AND slightly
    less saturated (hue nudged by +hue_shift). This is the vanilla pattern:
    straight value-only ramps look dull, hue shifting keeps ramps lively.

    Returns a list of hex strings, darkest first. steps must be odd so the base
    lands exactly in the middle.
    """
    if steps % 2 == 0:
        steps += 1
    h, s, v = rgb2hsv(hex2rgb(base_hex))
    mid = steps // 2
    out = []
    for i in range(steps):
        t = (i - mid) / max(mid, 1)
        sh = (h + hue_shift * t) % 360.0
        ss = clamp(s + (sat_dark if t < 0 else sat_light) * abs(t), 0, 1)
        sv = clamp(v + val_step * t, 0.02, 1.0)
        out.append(rgb2hex(hsv2rgb((sh, ss, sv))))
    return out


def palette_report(img):
    """Count distinct colors and print a report (hex, count, coverage)."""
    counts = {}
    total = 0
    for row in img:
        for px in row:
            if px[3] == 0:
                continue
            counts[rgb2hex(px)] = counts.get(rgb2hex(px), 0) + 1
            total += 1
    lines = ['palette (%d colors, %d opaque px):' % (len(counts), total)]
    for hexc, n in sorted(counts.items(), key=lambda kv: -kv[1]):
        lines.append('  %s x%-4d %5.1f%%' % (hexc, n, 100.0 * n / max(total, 1)))
    return '\n'.join(lines)


def quantize(img, palette_hexes):
    """Map every pixel to its nearest palette color (palette locking)."""
    pal = [hex2rgb(c) for c in palette_hexes]
    out = []
    for row in img:
        newrow = []
        for px in row:
            if px[3] == 0:
                newrow.append(px)
                continue
            best, bd = pal[0], 1e9
            for c in pal:
                d = (c[0] - px[0]) ** 2 + (c[1] - px[1]) ** 2 + (c[2] - px[2]) ** 2
                if d < bd:
                    bd, best = d, c
            newrow.append((best[0], best[1], best[2], px[3]))
        out.append(newrow)
    return out


# ---------------------------------------------------------------------------
# Image primitives
# ---------------------------------------------------------------------------

def new_img(w, h, color=(0, 0, 0, 255)):
    return [[tuple(color) for _ in range(w)] for _ in range(h)]


def get_px(img, x, y):
    w, h = len(img[0]), len(img)
    return img[y % h][x % w]


def set_px(img, x, y, color):
    w, h = len(img[0]), len(img)
    img[y % h][x % w] = tuple(color)


def rect(img, x0, y0, x1, y1, color):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            if 0 <= y < len(img) and 0 <= x < len(img[0]):
                img[y][x] = tuple(color)


def ellipse(img, cx, cy, rx, ry, color):
    """Filled ellipse, pixel-snapped; safe to draw off-edge (clipped)."""
    for y in range(max(0, int(cy - ry) - 1), min(len(img), int(cy + ry) + 2)):
        for x in range(max(0, int(cx - rx) - 1), min(len(img[0]), int(cx + rx) + 2)):
            dx = (x - cx) / max(rx, 0.5)
            dy = (y - cy) / max(ry, 0.5)
            if dx * dx + dy * dy <= 1.0:
                img[y][x] = tuple(color)


def outline_mask(img, mask, color, inside=True):
    """Draw a 1px outline around the True region of `mask` (same shape as img).

    inside=True  -> outline hugs the inner edge of the region
    inside=False -> outline hugs the outer edge (one pixel beyond the region)
    """
    w, h = len(img[0]), len(img)
    for y in range(h):
        for x in range(w):
            if mask[y][x]:
                for dy, dx in ((0, 1), (0, -1), (1, 0), (-1, 0)):
                    ny, nx = y + dy, x + dx
                    if 0 <= ny < h and 0 <= nx < w:
                        if inside and not mask[ny][nx]:
                            img[y][x] = tuple(color)
                        if not inside and not mask[ny][nx]:
                            img[ny][nx] = tuple(color)


def checker_dither(img, c1, c2, region=None):
    """Classic 2x2 checkered dither between two colors (vanilla sand style).

    region: optional (x0, y0, x1, y1). Defaults to the whole image.
    Colors may be 3- or 4-tuples; output pixels are always RGBA.
    """
    c1 = tuple(c1) + (255,) * (4 - len(c1))
    c2 = tuple(c2) + (255,) * (4 - len(c2))
    x0, y0, x1, y1 = region or (0, 0, len(img[0]), len(img))
    for y in range(y0, y1):
        for x in range(x0, x1):
            img[y][x] = tuple(c1 if (x + y) % 2 == 0 else c2)


def bayer_dither(img, c1, c2, region=None, seed=0):
    """Ordered 2x2 Bayer dither; 50% mix, less regular than full checker."""
    c1 = tuple(c1) + (255,) * (4 - len(c1))
    c2 = tuple(c2) + (255,) * (4 - len(c2))
    rnd = random.Random(seed)
    bayer = [[0, 2], [3, 1]]
    x0, y0, x1, y1 = region or (0, 0, len(img[0]), len(img))
    for y in range(y0, y1):
        for x in range(x0, x1):
            img[y][x] = tuple(c1 if (bayer[y % 2][x % 2] + rnd.random() * 0.9) < 1.5 else c2)


# ---------------------------------------------------------------------------
# Tileable noise
# ---------------------------------------------------------------------------

def _hash2(ix, iy, seed):
    h = (ix * 374761393 + iy * 668265263 + seed * 974634011) & 0xffffffff
    h = (h ^ (h >> 13)) * 1274126177 & 0xffffffff
    h ^= h >> 16
    return (h & 0xffffff) / 0xffffff


def _smooth(t):
    return t * t * (3 - 2 * t)


def value_noise(w, h, seed=0, freq=4.0):
    """Tileable value noise in [0,1]. freq = number of lattice cells across.

    Toroidal by construction: lattice indices are taken mod ceil(freq), and
    interpolation wraps at the edges, so the output tiles seamlessly.
    """
    cells = max(2, int(math.ceil(freq)))
    grid = [[_hash2(ix % cells, iy % cells, seed) for ix in range(cells + 1)]
            for iy in range(cells + 1)]
    out = [[0.0] * w for _ in range(h)]
    for y in range(h):
        fy = y / h * freq
        iy = int(fy)
        ty = _smooth(fy - iy)
        for x in range(w):
            fx = x / w * freq
            ix = int(fx)
            tx = _smooth(fx - ix)
            a = lerp(grid[iy % cells][ix % cells], grid[iy % cells][(ix + 1) % cells], tx)
            b = lerp(grid[(iy + 1) % cells][ix % cells], grid[(iy + 1) % cells][(ix + 1) % cells], tx)
            out[y][x] = lerp(a, b, ty)
    return out


def fbm(w, h, seed=0, octaves=4, base_freq=2.0, lacunarity=2.0, gain=0.5):
    """Fractal value noise (fBm) in [0,1], tileable. Great for organic grain."""
    total, amp, freq, norm = 0.0, 1.0, base_freq, 0.0
    acc = [[0.0] * w for _ in range(h)]
    for _ in range(octaves):
        n = value_noise(w, h, seed, freq)
        for y in range(h):
            for x in range(w):
                acc[y][x] += n[y][x] * amp
        norm += amp
        amp *= gain
        freq *= lacunarity
        seed += 101
    for y in range(h):
        for x in range(w):
            acc[y][x] = acc[y][x] / norm
    return acc


def noise_to_ramp(noise, ramp_hexes, dither=True, seed=0):
    """Map a 0..1 noise field onto a color ramp, optionally dithering the
    transitions so shades blend instead of banding."""
    ramp_rgb = [hex2rgb(c) for c in ramp_hexes]
    steps = len(ramp_rgb)
    rnd = random.Random(seed)
    w, h = len(noise[0]), len(noise)
    img = [[(0, 0, 0, 255)] * w for _ in range(h)]
    for y in range(h):
        for x in range(w):
            v = noise[y][x] * (steps - 1)
            i = int(v)
            i = min(i, steps - 2)
            t = v - i
            if dither:
                # threshold dither: crisp step at a jittered boundary
                t = 1.0 if (t + (rnd.random() - 0.5) * 0.8) > 0.5 else 0.0
            img[y][x] = tuple(mix(ramp_rgb[i], ramp_rgb[i + 1], t)) + (255,)
    return img


def add_grain(img, seed=0, amount=0.5, colors=None):
    """Add subtle single-pixel grain by nudging each pixel toward a neighbor
    shade. Breaks banding and flat patches (the two big vanilla no-nos)."""
    rnd = random.Random(seed)
    w, h = len(img[0]), len(img)
    src = [row[:] for row in img]
    for y in range(h):
        for x in range(w):
            if rnd.random() > amount:
                continue
            px = src[y][x]
            if px[3] == 0:
                continue
            n = src[(y + (1 if rnd.random() < 0.5 else -1)) % h][x]
            img[y][x] = tuple(mix(px, n, 0.5))
    return img


def cracks(img, seed=0, count=3, color=(30, 28, 26, 255), max_len=8):
    """Draw 1px random-walk crack lines (stone/rock texture detail)."""
    rnd = random.Random(seed)
    w, h = len(img[0]), len(img)
    for _ in range(count):
        x = rnd.randrange(w)
        y = rnd.randrange(h)
        for _ in range(rnd.randint(3, max_len)):
            img[y % h][x % w] = tuple(color)
            x += rnd.choice((-1, 0, 1))
            y += rnd.choice((-1, 0, 1))


def blotches(noise, lo, hi, ramp_hexes, seed=0):
    """Map the band of noise in [lo,hi] onto the given ramp; elsewhere leave
    transparent. Used to scatter pebbles, crystal clusters, tufts, etc."""
    rnd = random.Random(seed)
    ramp_rgb = [hex2rgb(c) for c in ramp_hexes]
    w, h = len(noise[0]), len(noise)
    img = new_img(w, h, (0, 0, 0, 0))
    for y in range(h):
        for x in range(w):
            v = noise[y][x]
            if lo <= v <= hi:
                t = (v - lo) / max(hi - lo, 1e-9)
                idx = int(t * (len(ramp_rgb) - 1))
                img[y][x] = tuple(ramp_rgb[idx]) + (255,)
    return img


# ---------------------------------------------------------------------------
# PBR map helpers (labPBR / 1.20.5+ style)
# ---------------------------------------------------------------------------

def normal_from_height(height, strength=1.0):
    """Build a tangent-space normal map from a grayscale height image.

    Central differences with toroidal wrap (keeps the normal tileable).
    Flat surface -> (128, 128, 255); +X -> red up; +Y (screen down) -> green up.
    """
    w, h = len(height[0]), len(height)
    img = new_img(w, h, (128, 128, 255, 255))
    for y in range(h):
        for x in range(w):
            hl = height[y][(x - 1) % w]
            hr = height[y][(x + 1) % w]
            hu = height[(y - 1) % h][x]
            hd = height[(y + 1) % h][x]
            dx = (hr - hl) * 0.5 * strength
            dy = (hd - hu) * 0.5 * strength
            nx, ny, nz = -dx, -dy, 1.0
            inv = 1.0 / math.sqrt(nx * nx + ny * ny + nz * nz)
            img[y][x] = (int(round((nx * inv + 1) * 127.5)),
                         int(round((ny * inv + 1) * 127.5)),
                         int(round((nz * inv + 1) * 127.5)), 255)
    return img


def luminance(img):
    """Grayscale height proxy: 0.299R + 0.587G + 0.114B (alpha-aware)."""
    return [[0.0] * len(img[0]) for _ in range(len(img))]


def build_height(img):
    w, h = len(img[0]), len(img)
    out = [[0.0] * w for _ in range(h)]
    for y in range(h):
        for x in range(w):
            r, g, b, a = img[y][x]
            if a == 0:
                out[y][x] = 0.5
            else:
                out[y][x] = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
    return out


def specular_map(img, spec=64, smooth=128, metal=0):
    """labPBR specular: R = specular intensity, G = smoothness, B = metalness.
    Also written as _s.png for 1.20.5+ official PBR (same channel meaning)."""
    w, h = len(img[0]), len(img)
    return [[(spec, smooth, metal, img[y][x][3]) for x in range(w)] for y in range(h)]


def emissive_map(img, mask, intensity=255):
    """Emissive map: R = emissive intensity, G/B = 0 (per labPBR and 1.20.5+).
    mask: same-shape grid of booleans, or None for all-opaque."""
    w, h = len(img[0]), len(img)
    return [[(intensity if (mask is None or mask[y][x]) else 0, 0, 0, 255)
             for x in range(w)] for y in range(h)]


# ---------------------------------------------------------------------------
# Minimal PNG codec (pure stdlib, no dependencies)
# ---------------------------------------------------------------------------

def _chunk(tag, data):
    return (struct.pack('>I', len(data)) + tag + data +
            struct.pack('>I', zlib.crc32(tag + data) & 0xffffffff))


def write_png(path, img):
    """Write an RGBA image (list of rows of (r,g,b,a)) as a PNG."""
    h = len(img)
    w = len(img[0])
    raw = bytearray()
    for row in img:
        raw.append(0)  # filter type 0
        for px in row:
            raw += bytes((px[0] & 255, px[1] & 255, px[2] & 255, px[3] & 255))
    sig = b'\x89PNG\r\n\x1a\n'
    ihdr = struct.pack('>IIBBBBB', w, h, 8, 6, 0, 0, 0)
    idat = zlib.compress(bytes(raw), 9)
    with open(path, 'wb') as f:
        f.write(sig + _chunk(b'IHDR', ihdr) + _chunk(b'IDAT', idat) + _chunk(b'IEND', b''))
    return path


def _paeth(a, b, c):
    p = a + b - c
    pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
    if pa <= pb and pa <= pc:
        return a
    if pb <= pc:
        return b
    return c


def read_png(path):
    """Decode any common PNG into an RGBA row grid.

    Supports bit depths 1/2/4/8, color types 0/2/3/4/6 (grayscale, RGB,
    palette, gray+alpha, RGBA), tRNS transparency, and filters 0-4.
    Vanilla Minecraft textures and wiki renders are frequently 4-bit palette
    PNGs — this reader handles them with no external tools.

    Non-interlaced only; Adam7 raises a clear error (convert with ImageMagick:
    `convert in.png PNG32:out.png`).
    """
    with open(path, 'rb') as f:
        data = f.read()
    if data[:8] != b'\x89PNG\r\n\x1a\n':
        raise ValueError('not a PNG')
    pos = 8
    w = h = bitd = ctype = interlace = None
    plte = b''
    trns = b''
    idat = b''
    while pos < len(data):
        (length,) = struct.unpack('>I', data[pos:pos + 4])
        tag = data[pos + 4:pos + 8]
        chunk = data[pos + 8:pos + 8 + length]
        pos += 12 + length
        if tag == b'IHDR':
            w, h, bitd, ctype, _, _, interlace = struct.unpack('>IIBBBBB', chunk)
        elif tag == b'PLTE':
            plte = chunk
        elif tag == b'tRNS':
            trns = chunk
        elif tag == b'IDAT':
            idat += chunk
        elif tag == b'IEND':
            break
    if interlace != 0:
        raise ValueError('interlaced (Adam7) PNG not supported — convert first, '
                         'e.g. ImageMagick: convert %s PNG32:out.png' % path)
    if ctype not in (0, 2, 3, 4, 6):
        raise ValueError('unsupported color type %d' % ctype)
    if ctype == 3 and not plte:
        raise ValueError('palette PNG missing PLTE chunk')
    if bitd not in (1, 2, 4, 8):
        raise ValueError('unsupported bit depth %d' % bitd)

    channels = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}[ctype]
    raw = zlib.decompress(idat)
    stride = (w * channels * bitd + 7) // 8   # packed bytes per scanline
    prev = bytearray(stride)
    rows = []
    pos = 0
    for y in range(h):
        ftype = raw[pos]
        pos += 1
        line = bytearray(raw[pos:pos + stride])
        pos += stride
        if ftype == 1:
            for i in range(channels, stride):
                line[i] = (line[i] + line[i - channels]) & 255
        elif ftype == 2:
            for i in range(stride):
                line[i] = (line[i] + prev[i]) & 255
        elif ftype == 3:
            for i in range(stride):
                a = line[i - channels] if i >= channels else 0
                line[i] = (line[i] + ((a + prev[i]) >> 1)) & 255
        elif ftype == 4:
            for i in range(stride):
                a = line[i - channels] if i >= channels else 0
                b = prev[i]
                c = prev[i - channels] if i >= channels else 0
                line[i] = (line[i] + _paeth(a, b, c)) & 255
        elif ftype != 0:
            raise ValueError('bad filter %d' % ftype)
        prev = line
        rows.append(line)

    # palette + transparency tables
    pal = None
    pal_alpha = None
    if ctype == 3:
        pal = [tuple(plte[i:i + 3]) for i in range(0, len(plte), 3)]
        if trns:
            pal_alpha = list(trns) + [255] * (len(pal) - len(trns))
    trns_gray_rgb = None
    if ctype in (0, 2) and trns:
        trns_gray_rgb = tuple(trns)  # 2 bytes for gray, 6 for RGB

    scale = 255.0 / (2 ** bitd - 1)

    def _bits(line, off, n):
        v = 0
        for i in range(n):
            b = off + i
            v = (v << 1) | ((line[b // 8] >> (7 - (b % 8))) & 1)
        return v

    out = []
    for line in rows:
        row = []
        for x in range(w):
            if bitd == 8:
                base = x * channels
                if ctype == 0:
                    v = line[base]
                    px = (v, v, v, 255)
                elif ctype == 2:
                    px = (line[base], line[base + 1], line[base + 2], 255)
                elif ctype == 3:
                    idx = line[base]
                    px = (pal[idx][0], pal[idx][1], pal[idx][2],
                          pal_alpha[idx] if pal_alpha else 255)
                elif ctype == 4:
                    v = line[base]
                    px = (v, v, v, line[base + 1])
                else:
                    px = (line[base], line[base + 1], line[base + 2], line[base + 3])
            else:
                off = x * channels * bitd
                raw_vals = [_bits(line, off + c * bitd, bitd) for c in range(channels)]
                if ctype == 3:
                    # palette INDEX is not a color sample — never scale it
                    idx = raw_vals[0]
                    px = (pal[idx][0], pal[idx][1], pal[idx][2],
                          pal_alpha[idx] if pal_alpha else 255)
                else:
                    vals = [int(round(v * scale)) for v in raw_vals]
                    if ctype == 0:
                        px = (vals[0], vals[0], vals[0], 255)
                    elif ctype == 2:
                        px = (vals[0], vals[1], vals[2], 255)
                    elif ctype == 4:
                        px = (vals[0], vals[0], vals[0], vals[1])
                    else:
                        px = (vals[0], vals[1], vals[2], vals[3])
            if trns_gray_rgb is not None:
                if ctype == 0 and px[0] == trns_gray_rgb[0]:
                    px = (px[0], px[1], px[2], 0)
                elif ctype == 2 and px[:3] == trns_gray_rgb:
                    px = (px[0], px[1], px[2], 0)
            row.append(px)
        out.append(row)
    return out


# ---------------------------------------------------------------------------
# Pixel analysis — "see" a texture without image input.
#
# The whole look-critique-fix loop depends on LOOKING. If the agent's model
# cannot view images (no multimodal input), these functions turn any texture
# into structured text: ASCII grids, palette reports, and automated checks for
# the exact artifacts the design skill warns about (banding, flat patches,
# missing outline, wrong lighting, broken tiling).
# ---------------------------------------------------------------------------

def luma(px):
    """Perceptual luminance of an RGBA pixel (0..255)."""
    return 0.299 * px[0] + 0.587 * px[1] + 0.114 * px[2]


def ascii_alpha(img, ch='#', dot='.'):
    """16-row alpha map: which pixels are opaque."""
    return '\n'.join(''.join(ch if px[3] > 0 else dot for px in row) for row in img)


def ascii_luma(img, chars='.:-=+*#%@'):
    """16-row luminance map; transparent pixels are spaces.

    Dark shades map to '.' and bright to '@' so dark textures (meteor iron,
    obsidian) stay readable — no level collapses into a space.
    """
    n = len(chars)
    lines = []
    for row in img:
        lines.append(''.join(' ' if px[3] == 0 else chars[min(n - 1, int(luma(px) * n // 256))]
                             for px in row))
    return '\n'.join(lines)


def labeled_grid(img, cell=6):
    """Every pixel as a hex code (transparent = '..'), for exact study."""
    lines = []
    for y, row in enumerate(img):
        lines.append('%2d %s' % (y, ' '.join(mc_rgb2hex(px) if px[3] > 0 else '..'
                                             for px in row)))
    return '\n'.join(lines)


def mc_rgb2hex(px):
    return '#%02x%02x%02x' % (px[0], px[1], px[2])


def bounding_box(img):
    """(x0, y0, x1, y1) of the opaque region, or None if empty."""
    h, w = len(img), len(img[0])
    xs, ys = [], []
    for y in range(h):
        for x in range(w):
            if img[y][x][3] > 0:
                xs.append(x)
                ys.append(y)
    if not xs:
        return None
    return (min(xs), min(ys), max(xs), max(ys))


def quad_luminance(img, box=None):
    """Mean luminance of the four quadrants of the opaque region.

    Vanilla light comes from the top-left: expect TL >= TR, TL >= BL and
    BL >= BR. Returns (means, verdict).
    """
    box = box or bounding_box(img)
    if box is None:
        return (None, 'empty image')
    x0, y0, x1, y1 = box
    mx = (x0 + x1) / 2.0
    my = (y0 + y1) / 2.0
    sums = {'TL': [0, 0], 'TR': [0, 0], 'BL': [0, 0], 'BR': [0, 0]}
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            px = img[y][x]
            if px[3] == 0:
                continue
            key = ('T' if y <= my else 'B') + ('L' if x <= mx else 'R')
            sums[key][0] += luma(px)
            sums[key][1] += 1
    means = {k: (v[0] / v[1] if v[1] else 0.0) for k, v in sums.items()}
    tl, tr, bl, br = means['TL'], means['TR'], means['BL'], means['BR']
    # Vanilla is top-left lit, but bevel-lit items (pickaxe heads, ingots)
    # legitimately put their brightest band mid-face. Only warn on a strong
    # inversion: bottom-right clearly brighter than top-left.
    inverted = (br > tl + 15) or (tr > tl + 15 and bl > tl + 15)
    if inverted:
        verdict = ('WARNING: lighting reads as bottom-right lit '
                   '(TL=%.0f TR=%.0f BL=%.0f BR=%.0f)' % (tl, tr, bl, br))
    else:
        verdict = 'consistent with top-left / bevel lighting'
    return (means, verdict)


def banding_detect(img, min_run=4, min_step=6):
    """Find horizontal/vertical runs of monotonically stepping shades of one
    hue — the 'fat lines' / staircase banding the design skill forbids.

    Returns a list of (orientation, index, start, end, direction, shades).
    """
    h, w = len(img), len(img[0])
    runs = []
    for orient in ('row', 'col'):
        length = h if orient == 'row' else w
        for i in range(length):
            seq = [luma(img[i][x]) if orient == 'row' else luma(img[y][i])
                   for x in range(w)] if orient == 'row' else \
                  [luma(img[y][i]) for y in range(h)]
            start = None
            prev = None
            prev_hue = None
            for j in range(length):
                px = img[i][j] if orient == 'row' else img[j][i]
                v = luma(px)
                if px[3] == 0:
                    start = None
                    prev = None
                    prev_hue = None
                    continue
                if prev is None:
                    start, prev = j, v
                    continue
                d = v - prev
                hue = rgb2hsv(px[:3])[0]
                same_hue = prev_hue is None or min(abs(hue - prev_hue), 360 - abs(hue - prev_hue)) < 25
                if abs(d) >= min_step and same_hue:
                    if start is None:
                        start = j - 1
                    prev = v
                    prev_hue = hue
                    if j == length - 1 and j - start + 1 >= min_run:
                        runs.append((orient, i, start, j, 'up' if d > 0 else 'down', ''))
                else:
                    if start is not None and j - start >= min_run:
                        runs.append((orient, i, start, j - 1,
                                     'up' if (prev - luma(img[i][start] if orient == 'row' else img[start][i])) > 0 else 'down', ''))
                    start = None
                    prev = v
                    prev_hue = hue
    return runs[:12]


def flat_patches(img, min_size=10):
    """Largest connected same-color opaque patches (BFS). Flags flat areas."""
    h, w = len(img), len(img[0])
    seen = [[False] * w for _ in range(h)]
    patches = []
    for y in range(h):
        for x in range(w):
            if seen[y][x] or img[y][x][3] == 0:
                continue
            color = img[y][x]
            stack = [(x, y)]
            seen[y][x] = True
            cells = []
            while stack:
                cx, cy = stack.pop()
                cells.append((cx, cy))
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    nx, ny = cx + dx, cy + dy
                    if 0 <= nx < w and 0 <= ny < h and not seen[ny][nx] \
                            and img[ny][nx] == color:
                        seen[ny][nx] = True
                        stack.append((nx, ny))
            patches.append((len(cells), mc_rgb2hex(color)))
    patches.sort(reverse=True)
    big = [p for p in patches if p[0] >= min_size]
    return patches[:5], big


def outline_analysis(img):
    """Item-outline check: is the silhouette border darker than the interior?

    Returns (border_median, interior_median, fraction_border_dark, verdict).
    """
    h, w = len(img), len(img[0])
    border, interior = [], []
    for y in range(h):
        for x in range(w):
            if img[y][x][3] == 0:
                continue
            on_border = any(
                0 <= ny < h and 0 <= nx < w and img[ny][nx][3] == 0
                for nx, ny in ((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)))
            (border if on_border else interior).append(luma(img[y][x]))
    if not interior:
        return (None, None, 0.0, 'no interior')
    border.sort()
    interior.sort()
    bm = border[len(border) // 2] if border else 0
    im = interior[len(interior) // 2]
    dark = sum(1 for v in border if v < im - 12) / max(len(border), 1)
    if im - bm >= 15:
        verdict = 'outline present (border ~%.0f vs interior ~%.0f)' % (bm, im)
    else:
        verdict = 'WARNING: no clear darker 1px outline (border ~%.0f vs interior ~%.0f)' % (bm, im)
    return (bm, im, dark, verdict)


def edge_mismatch(img):
    """Tileability proxy for full-bleed block textures: mean abs difference
    across the wrapped left/right and top/bottom edges. Low = tiles well."""
    h, w = len(img), len(img[0])
    lr = sum(abs(img[y][0][c] - img[y][w - 1][c]) for y in range(h) for c in range(3)) / (h * 3)
    tb = sum(abs(img[0][x][c] - img[h - 1][x][c]) for x in range(w) for c in range(3)) / (w * 3)
    return lr, tb


def analyze(img, name=''):
    """Full text report — the critique substrate for models without image input."""
    h, w = len(img), len(img[0])
    opaque = sum(1 for row in img for px in row if px[3] > 0)
    colors = sorted({mc_rgb2hex(px) for row in img for px in row if px[3] > 0})
    box = bounding_box(img)
    lines = []
    lines.append('== analyze%s ==' % (' ' + name if name else ''))
    lines.append('size %dx%d | opaque %d/%d (%d%%) | colors %d' %
                 (w, h, opaque, w * h, 100 * opaque // (w * h), len(colors)))
    lines.append('palette: %s' % (' '.join(colors) if colors else '(empty)'))
    if box:
        lines.append('bbox: (%d,%d)-(%d,%d)' % box)
        qm, qv = quad_luminance(img, box)
        if qm:
            lines.append('light TL=%.0f TR=%.0f BL=%.0f BR=%.0f -> %s' %
                         (qm['TL'], qm['TR'], qm['BL'], qm['BR'], qv))
        if opaque < w * h:  # sprite: check outline
            bm, im, dark, ov = outline_analysis(img)
            lines.append('outline: ' + ov)
    runs = banding_detect(img)
    lines.append('banding: %s' %
                 ('none' if not runs else
                  '; '.join('%s %s[%d:%d] %s' % (o, i, a, b, d) for o, i, a, b, d, _ in runs)))
    patches, big = flat_patches(img)
    if big:
        lines.append('FLAT PATCHES: %s' %
                     ', '.join('%dpx %s' % (n, c) for n, c in big[:3]))
    else:
        lines.append('flat patches: largest %dpx %s (ok)' %
                     (patches[0][0] if patches else 0, patches[0][1] if patches else '-'))
    if opaque == w * h:  # full-bleed block: tiling matters
        lr, tb = edge_mismatch(img)
        verdict = 'tiles well' if max(lr, tb) < 12 else 'WARNING: visible seam'
        lines.append('tile edges L/R=%.1f T/B=%.1f -> %s' % (lr, tb, verdict))
    lines.append('')
    lines.append('luma map:')
    lines.append(ascii_luma(img))
    lines.append('')
    lines.append('alpha map:')
    lines.append(ascii_alpha(img))
    return '\n'.join(lines)




# ---------------------------------------------------------------------------
# Previews
# ---------------------------------------------------------------------------

def _scale_nearest(img, scale):
    w, h = len(img[0]), len(img)
    out = new_img(w * scale, h * scale, (0, 0, 0, 255))
    for y in range(h):
        for x in range(w):
            for dy in range(scale):
                for dx in range(scale):
                    out[y * scale + dy][x * scale + dx] = img[y][x]
    return out


def write_mosaic(path, imgs, scale=8, gap=2, bg=(32, 32, 36, 255)):
    """Stack scaled images side by side (with a gap) into one PNG for review."""
    cols = max(1, int(round(math.sqrt(len(imgs)))))
    rows = (len(imgs) + cols - 1) // cols
    cell = scale * 16 + gap
    out = new_img(cols * cell + gap, rows * cell + gap, bg)
    for i, img in enumerate(imgs):
        sx, sy = (i % cols) * cell + gap, (i // cols) * cell + gap
        big = _scale_nearest(img, scale)
        for y in range(len(big)):
            for x in range(len(big[0])):
                px = big[y][x]
                if px[3] == 0:
                    out[sy + y][sx + x] = (60, 60, 64, 255)  # checker-ish empty
                else:
                    out[sy + y][sx + x] = px
    write_png(path, out)
    return path


def write_wall(path, img, scale=8):
    """3x3 tiling wall — the canonical tileability check for block textures."""
    out = new_img(16 * scale * 3, 16 * scale * 3, (40, 40, 44, 255))
    big = _scale_nearest(img, scale)
    for ty in range(3):
        for tx in range(3):
            for y in range(16 * scale):
                for x in range(16 * scale):
                    out[ty * 16 * scale + y][tx * 16 * scale + x] = big[y][x]
    write_png(path, out)
    return path


def write_scaled(path, img, scale=8):
    big = _scale_nearest(img, scale)
    write_png(path, big)
    return path
