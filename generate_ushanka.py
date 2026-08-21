#!/usr/bin/env python3
"""Generate the Soviet Ushanka hat textures.

Outputs:
  src/main/resources/assets/alyrioncore/textures/item/ushanka.png        (16x16 item icon)
  src/main/resources/assets/alyrioncore/textures/models/armor/ushanka.png (64x64 worn-model sheet)

The worn sheet matches the custom UshankaModel box UVs exactly:

  crown (9x5x9)      @ texOffs(0,0)   top (9,0) bottom (18,0) sides x0..36 y9..14
  back flap (8x8x1)  @ texOffs(0,16)  outer face (10,17) 8x8
  front flap (8x3x1) @ texOffs(20,16) outer face (21,17) 8x3  <- red star + band
  ear flaps (1x8x6)  @ texOffs(40,0)  outer face (40,6) 6x8 (left flap mirrors it)

Pure stdlib (mcutil). Run from the repo root:  python3 generate_ushanka.py
"""
import os
import sys

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), 'mc-scripts'))
import mcutil as mc


def hex(h):
    return tuple(int(h[i:i + 2], 16) for i in (1, 3, 5)) + (255,)


# --- Ushanka palette (pale winter fur, near-black band, bright red star) ---
# Deliberately lighter than typical brown skins so the hat reads as a hat.
FUR = hex("#A98F63")     # fur mid
FUR_L = hex("#D2B98C")   # fur light (sunlit crown)
FUR_D = hex("#6B5233")   # fur dark (shadows, ear-flap underside)
BAND = hex("#1F1A15")    # near-black cloth girdle
STAR = hex("#C62828")    # red star
STAR_L = hex("#E5544B")  # red star highlight
EMPTY = (0, 0, 0, 0)

# A 5-step fur ramp (darkest..lightest), high-contrast so the fur reads as
# pixel art, not TV static.
FUR_RAMP = [mc.hex2rgb(h) for h in mc.ramp("#A98F63", steps=5, val_step=0.22)]


def streak(x, y, seed, cw=1.5, ch=4.0):
    """Anisotropic value noise: changes fast across x, slow down y -> vertical
    fur strands instead of per-pixel static."""
    fx, fy = x / cw, y / ch
    ix, iy = int(fx), int(fy)
    tx, ty = mc._smooth(fx - ix), mc._smooth(fy - iy)
    a = mc.lerp(mc._hash2(ix, iy, seed), mc._hash2(ix + 1, iy, seed), tx)
    b = mc.lerp(mc._hash2(ix, iy + 1, seed), mc._hash2(ix + 1, iy + 1, seed), tx)
    return mc.lerp(a, b, ty)


def fur_px(x, y, seed=7, shade=0.0, light=0.0):
    """One fur pixel: quantized strand noise + rare tips/partings."""
    n = streak(x, y, seed)
    fine = mc._hash2(x, y, seed + 101)
    v = n * 0.7 + fine * 0.3
    idx = 0 if v < 0.25 else 1 if v < 0.45 else 2 if v < 0.7 else 3 if v < 0.88 else 4
    c = FUR_RAMP[idx]
    if light > 0:
        c = mc.mix(c, FUR_L, light)
    if shade > 0:
        c = mc.mix(c, FUR_D, shade)
    if fine > 0.94:
        c = mc.mix(c, FUR_L, 0.7)   # sunlit fur tip
    elif fine < 0.05:
        c = mc.mix(c, FUR_D, 0.7)   # dark parting line
    return tuple(c)[:3] + (255,)


def band_px(x, y, seed=11):
    """Dark cloth band with a horizontal weave and worn flecks."""
    n = mc._hash2(x, y, seed)
    c = mc.mix(BAND, (0, 0, 0), 0.25) if y % 2 else BAND
    if n > 0.9:
        c = mc.mix(c, FUR_D, 0.5)
    return tuple(c)[:3] + (255,)


def fill(img, x0, y0, x1, y1, fn):
    """Fill inclusive rect with fn(x, y) -> RGBA."""
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            img[y][x] = fn(x, y)


# --- 16x16 item icon (front-facing ushanka: fur crown, band, starred brow ---
# --- flap, and ear flaps hanging to the chin) ------------------------------
ICON = [
    "................",
    ".....FFFFFF.....",
    "...FFFFFFFFFF...",
    "..FFFFFFFFFFFF..",
    ".FFfFFFFFFFfFFF.",
    ".FfFFFFFFFFFFfF.",
    ".BBBBBBBBBBBBBB.",
    ".ddd..FRRF..ddd.",
    ".ddd.FRRRRF.ddd.",
    ".ddd..FRRF..ddd.",
    ".ddd.BBBBBB.ddd.",
    ".ddd........ddd.",
    ".dd..........dd.",
    "................",
    "................",
    "................",
]

PAL = {
    'f': FUR_L, 'F': FUR, 'd': FUR_D, 'B': BAND,
    'R': STAR, '.': EMPTY,
}

img = []
for y, row in enumerate(ICON):
    assert len(row) == 16, (y, row)
    line = []
    for ch in row:
        line.append(list(PAL[ch]) if PAL[ch] is not EMPTY else list(EMPTY))
    img.append(line)

out = "src/main/resources/assets/alyrioncore/textures/item/ushanka.png"
mc.write_png(out, img)
print("Saved", out)


# --- 64x64 worn-model sheet matching UshankaModel's UV layout ---------------
W, H = 64, 64
sheet = mc.new_img(W, H, EMPTY)

# Crown: top/bottom faces + four side strips (y9..13, band on rows 12..13).
# Sides shade darker toward the girdle; top face is sunlit.
fill(sheet, 9, 0, 17, 8, lambda x, y: fur_px(x, y, seed=7, light=0.55))      # top (sunlit)
fill(sheet, 18, 0, 26, 8, lambda x, y: fur_px(x, y, seed=8, shade=0.5))      # bottom
for x0 in (0, 9, 18, 27):                                                     # four sides
    for y in range(9, 12):
        t = (y - 9) / 2.0
        fill(sheet, x0, y, x0 + 8, y,
             lambda x, y, t=t: fur_px(x, y, seed=9, light=0.3 * (1 - t), shade=0.35 * t))
    fill(sheet, x0, 12, x0 + 8, 13, band_px)                                  # girdle

# Back flap: outer face (10,17) 8x8 — band where it tucks under the crown,
# fur shading gently darker toward the hem (kept light so the hat silhouette
# reads as fur, not a black blob).
fill(sheet, 10, 17, 17, 18, band_px)
for y in range(19, 25):
    t = (y - 19) / 5.0
    fill(sheet, 10, y, 17, y, lambda x, y, t=t: fur_px(x, y, seed=12, shade=0.2 * t, light=0.15))
fill(sheet, 1, 16, 16, 16, lambda x, y: fur_px(x, y, seed=13, shade=0.3))     # top+bottom strips
fill(sheet, 0, 17, 0, 24, lambda x, y: fur_px(x, y, seed=13, shade=0.3))      # side strips
fill(sheet, 9, 17, 9, 24, lambda x, y: fur_px(x, y, seed=13, shade=0.3))
fill(sheet, 1, 17, 8, 24, lambda x, y: fur_px(x, y, seed=14, shade=0.55))     # inner face

# Front flap: outer face (21,17) 8x3 — fur, a 3-row red star, band hem.
fill(sheet, 21, 17, 28, 18, lambda x, y: fur_px(x, y, seed=15, light=0.25))
sheet[17][24] = STAR                                                          # star top point
sheet[17][25] = STAR_L
for x in range(22, 28):                                                       # star arms
    sheet[18][x] = STAR_L if x == 22 else STAR
sheet[19][23] = STAR                                                          # star bottom points
sheet[19][26] = STAR
fill(sheet, 21, 19, 22, 19, band_px)                                          # band hem
fill(sheet, 24, 19, 25, 19, band_px)
fill(sheet, 27, 19, 28, 19, band_px)
fill(sheet, 21, 16, 28, 16, lambda x, y: fur_px(x, y, seed=16, shade=0.2))    # top+bottom strips
fill(sheet, 29, 16, 36, 16, lambda x, y: fur_px(x, y, seed=16, shade=0.2))
fill(sheet, 20, 17, 20, 19, lambda x, y: fur_px(x, y, seed=16, shade=0.2))    # side strips
fill(sheet, 29, 17, 29, 19, lambda x, y: fur_px(x, y, seed=16, shade=0.2))
fill(sheet, 30, 17, 37, 19, lambda x, y: fur_px(x, y, seed=17, shade=0.55))   # inner face

# Ear flaps (shared region, left flap mirrors): outer face (40,6) 6x8 —
# band on the top rows where they button under the crown, fur shading gently
# darker toward the rounded tip (kept light to match the pale crown).
fill(sheet, 40, 6, 45, 7, band_px)
for y in range(8, 14):
    t = (y - 8) / 5.0
    fill(sheet, 40, y, 45, y, lambda x, y, t=t: fur_px(x, y, seed=18, shade=0.18 * t, light=0.1))
fill(sheet, 47, 6, 52, 13, lambda x, y: fur_px(x, y, seed=19, shade=0.55))    # inner face
fill(sheet, 46, 0, 46, 5, lambda x, y: fur_px(x, y, seed=20, shade=0.2))      # top/bottom strips
fill(sheet, 47, 0, 47, 5, lambda x, y: fur_px(x, y, seed=20, shade=0.2))
fill(sheet, 46, 6, 46, 13, lambda x, y: fur_px(x, y, seed=20, shade=0.2))     # front/back strips
fill(sheet, 53, 6, 53, 13, lambda x, y: fur_px(x, y, seed=20, shade=0.2))

out2 = "src/main/resources/assets/alyrioncore/textures/models/armor/ushanka.png"
mc.write_png(out2, sheet)
print("Saved", out2)

# --- verify with the analyzer's ASCII luma map ---
for name, im in (("ushanka icon", img),):
    print(f"\n{name} luma map:")
    chars = " .:-=+*#%@"
    for row in im:
        line = ""
        for px in row:
            if px[3] == 0:
                line += " "
            else:
                v = (0.299 * px[0] + 0.587 * px[1] + 0.114 * px[2]) / 255
                line += chars[min(9, int(v * 10))]
        print("  |" + line + "|")
