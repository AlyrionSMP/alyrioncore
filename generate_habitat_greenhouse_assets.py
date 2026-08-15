#!/usr/bin/env python3
"""
Generate textures, models, blockstates, recipes, loot tables, tags, and localizations
for the Pressurized Habitats, Airlocks, and Martian Greenhouse System.
"""

import json
import math
import os
import random
from PIL import Image, ImageDraw

MOD_DIR = "/Users/lea/alyrioncore"
ASSETS_DIR = os.path.join(MOD_DIR, "src/main/resources/assets/alyrioncore")
DATA_DIR = os.path.join(MOD_DIR, "src/main/resources/data/alyrioncore")
MC_DATA_DIR = os.path.join(MOD_DIR, "src/main/resources/data/minecraft")

def ensure_dir(path):
    os.makedirs(path, exist_ok=True)

def write_json(path, data):
    ensure_dir(os.path.dirname(path))
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)
    print(f"Wrote: {path}")

# --- 1. TEXTURES ---

def create_airlock_textures():
    # 32x32 high-tech space habitat airlock door textures
    ensure_dir(os.path.join(ASSETS_DIR, "textures/block"))
    ensure_dir(os.path.join(ASSETS_DIR, "textures/item"))

    # Bottom Half
    img_bot = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw_b = ImageDraw.Draw(img_bot)
    
    # Outer frame: Dark titanium steel
    draw_b.rectangle([0, 0, 31, 31], fill=(42, 45, 52, 255))
    draw_b.rectangle([2, 0, 29, 30], fill=(68, 72, 82, 255))
    # Reinforced inset plating
    draw_b.rectangle([5, 3, 26, 28], fill=(95, 100, 112, 255))
    draw_b.rectangle([6, 4, 25, 27], fill=(80, 85, 96, 255))
    # Bevels and bolts
    for y in [4, 15, 27]:
        draw_b.point((4, y), fill=(180, 185, 195, 255))
        draw_b.point((27, y), fill=(180, 185, 195, 255))
    # Pneumatic locking bar across center
    draw_b.rectangle([6, 13, 25, 17], fill=(130, 135, 145, 255))
    draw_b.rectangle([8, 14, 23, 16], fill=(50, 52, 60, 255))
    # Rubber pressure seal gasket border
    draw_b.rectangle([3, 0, 4, 30], fill=(25, 25, 28, 255))
    draw_b.rectangle([27, 0, 28, 30], fill=(25, 25, 28, 255))

    img_bot.save(os.path.join(ASSETS_DIR, "textures/block/airlock_bottom.png"))

    # Top Half
    img_top = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw_t = ImageDraw.Draw(img_top)
    
    # Outer frame: Dark titanium steel
    draw_t.rectangle([0, 0, 31, 31], fill=(42, 45, 52, 255))
    draw_t.rectangle([2, 1, 29, 31], fill=(68, 72, 82, 255))
    # Reinforced inset plating
    draw_t.rectangle([5, 3, 26, 28], fill=(95, 100, 112, 255))
    draw_t.rectangle([6, 4, 25, 27], fill=(80, 85, 96, 255))
    # Reinforced airtight viewport glass window
    draw_t.rectangle([9, 6, 22, 17], fill=(30, 35, 45, 255))
    draw_t.rectangle([10, 7, 21, 16], fill=(40, 160, 200, 240))
    # Glass glare highlight
    draw_t.line([(11, 8), (17, 8)], fill=(180, 240, 255, 255))
    draw_t.line([(11, 9), (13, 9)], fill=(180, 240, 255, 255))
    # Pressure status indicator HUD LED (Green for SEALED)
    draw_t.rectangle([11, 21, 20, 24], fill=(20, 25, 30, 255))
    draw_t.rectangle([12, 22, 15, 23], fill=(40, 220, 90, 255)) # Green = SEALED
    draw_t.rectangle([16, 22, 19, 23], fill=(80, 30, 30, 255)) # Red = Unsealed / Venting

    img_top.save(os.path.join(ASSETS_DIR, "textures/block/airlock_top.png"))

    # Item texture
    img_item = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw_i = ImageDraw.Draw(img_item)
    draw_i.rectangle([7, 2, 24, 29], fill=(42, 45, 52, 255))
    draw_i.rectangle([9, 4, 22, 27], fill=(75, 80, 90, 255))
    # Viewport
    draw_i.rectangle([11, 6, 20, 13], fill=(40, 170, 210, 255))
    draw_i.line([(12, 7), (16, 7)], fill=(200, 245, 255, 255))
    # Status light
    draw_i.rectangle([13, 16, 18, 18], fill=(40, 220, 90, 255))
    # Handle / latch
    draw_i.rectangle([10, 20, 21, 23], fill=(120, 125, 135, 255))
    img_item.save(os.path.join(ASSETS_DIR, "textures/item/airlock.png"))

def create_farmland_textures():
    # 32x32 Regolith Farmland
    # Dry Farmland
    img_dry = Image.new("RGBA", (32, 32), (0, 0, 0, 255))
    draw_d = ImageDraw.Draw(img_dry)
    random.seed(301)
    base_r, base_g, base_b = 135, 68, 38
    for y in range(32):
        for x in range(32):
            # Furrow ridge lines every 4 pixels
            is_ridge = (y % 4 == 0 or y % 4 == 1)
            offset = 12 if is_ridge else -14
            noise = random.randint(-8, 8)
            r = max(40, min(220, base_r + offset + noise))
            g = max(20, min(150, base_g + offset + noise))
            b = max(10, min(100, base_b + offset + noise))
            img_dry.putpixel((x, y), (r, g, b, 255))
    img_dry.save(os.path.join(ASSETS_DIR, "textures/block/regolith_farmland_dry.png"))

    # Moist Farmland (Deep rich terracotta / dark moisture sheen)
    img_moist = Image.new("RGBA", (32, 32), (0, 0, 0, 255))
    random.seed(302)
    m_r, m_g, m_b = 82, 38, 22
    for y in range(32):
        for x in range(32):
            is_ridge = (y % 4 == 0 or y % 4 == 1)
            offset = 10 if is_ridge else -12
            noise = random.randint(-6, 6)
            r = max(25, min(160, m_r + offset + noise))
            g = max(15, min(90, m_g + offset + noise))
            b = max(8, min(60, m_b + offset + noise))
            # Subtle moisture glint
            if random.random() < 0.04:
                r, g, b = r + 35, g + 35, b + 45
            img_moist.putpixel((x, y), (r, g, b, 255))
    img_moist.save(os.path.join(ASSETS_DIR, "textures/block/regolith_farmland_moist.png"))

    # Side texture
    img_side = Image.new("RGBA", (32, 32), (0, 0, 0, 255))
    random.seed(303)
    for y in range(32):
        for x in range(32):
            if y < 4:
                # Top tilled layer
                r = max(30, min(180, 110 + random.randint(-10, 10)))
                g = max(15, min(120, 55 + random.randint(-8, 8)))
                b = max(10, min(80, 30 + random.randint(-6, 6)))
            else:
                # Regolith subsoil
                r = max(40, min(200, 140 + random.randint(-12, 12)))
                g = max(20, min(130, 65 + random.randint(-10, 10)))
                b = max(10, min(90, 35 + random.randint(-8, 8)))
            img_side.putpixel((x, y), (r, g, b, 255))
    img_side.save(os.path.join(ASSETS_DIR, "textures/block/regolith_farmland_side.png"))

def create_crop_and_food_textures():
    # Martian Potato Crop Stages (0 to 7) - 32x32 cross crop textures.
    # Procedurally painted potato plants instead of flat green plates:
    # layered pinnate compound leaves with ragged edges, lit/shadow shading
    # and crimson midrib veins, tapered stems, white flowers, and at maturity
    # lumpy tubers and a regolith soil mound so the plant reads as planted.
    import math

    # --- palette ------------------------------------------------------
    stem_hi = (96, 158, 58, 255)
    stem_lo = (38, 78, 28, 255)
    vein_crimson = (172, 54, 62, 255)      # alien crimson-veined leaves
    vein_dark = (122, 34, 42, 255)
    greens = [
        (34, 84, 30, 255),    # 0 deep shadow leaf (back layer)
        (46, 104, 36, 255),   # 1 shadow leaf
        (62, 130, 46, 255),   # 2 mid leaf
        (84, 158, 58, 255),   # 3 lit leaf
        (108, 184, 72, 255),  # 4 highlight leaf
    ]
    flower_white = (243, 243, 249, 255)
    flower_shade = (198, 202, 216, 255)
    flower_yellow = (247, 210, 50, 255)
    flower_core = (216, 152, 32, 255)
    tuber_hi = (228, 160, 98, 255)
    tuber_lo = (146, 80, 42, 255)
    tuber_eye = (102, 52, 26, 255)
    soil_hi = (130, 74, 42, 255)
    soil_lo = (64, 32, 17, 255)
    soil_peb = (152, 98, 58, 255)

    def lerp3(a, b, t):
        return (int(a[0] + (b[0] - a[0]) * t),
                int(a[1] + (b[1] - a[1]) * t),
                int(a[2] + (b[2] - a[2]) * t), 255)

    def draw_leaflet(buf, cx, cy, length, width, angle_deg, col_lit, col_shade, rng, vein=True):
        """One organic leaflet: pointed oval with a wavy jittered edge, lit/shadow
        shading, a crimson midrib and faint speckles."""
        a = math.radians(angle_deg)
        ca, sa = math.cos(a), math.sin(a)
        phase = rng.uniform(0.0, 6.283)
        wob = rng.uniform(0.05, 0.13)
        half = length * 0.5
        x0 = max(0, int(cx - half - width)); x1 = min(32, int(cx + half + width) + 1)
        y0 = max(0, int(cy - half - width)); y1 = min(32, int(cy + half + width) + 1)
        for x in range(x0, x1):
            for y in range(y0, y1):
                dx, dy = x - cx, y - cy
                u = dx * ca + dy * sa
                v = -dx * sa + dy * ca
                t = (u + half) / length
                if t < -0.05 or t > 1.05:
                    continue
                tc = max(0.0, min(1.0, t))
                prof = width * math.sin(math.pi * tc) * (0.80 + 0.26 * tc)
                prof *= 1.0 + wob * math.sin(tc * 16.5 + phase)
                if abs(v) > prof:
                    continue
                lite = 0.5 - 0.5 * (v / max(prof, 0.4))
                lite = max(0.0, min(1.0, lite))
                lite *= 1.0 - 0.15 * math.sin(math.pi * tc)  # softer at base & tip
                col = lerp3(col_shade, col_lit, lite)
                if vein and length >= 4.0 and abs(v) < 0.6 and 0.13 < tc < 0.97:
                    col = vein_crimson if tc < 0.82 else vein_dark
                elif rng.random() < 0.05:
                    f = 1.0 + (0.06 if rng.random() < 0.5 else -0.09)
                    col = (min(255, int(col[0] * f)), min(255, int(col[1] * f)),
                           min(255, int(col[2] * f)), 255)
                buf[y][x] = col

    def draw_stem(buf, x0, y0, x1, y1, r_base, r_tip, rng):
        """Tapered shaded stem with a ragged organic edge."""
        sx, sy = x1 - x0, y1 - y0
        seg = math.hypot(sx, sy)
        if seg < 0.01:
            return
        ux, uy = sx / seg, sy / seg
        nx, ny = -uy, ux
        rmax = max(r_base, r_tip) + 1
        xa = max(0, int(min(x0, x1) - rmax)); xb = min(32, int(max(x0, x1) + rmax) + 1)
        ya = max(0, int(min(y0, y1) - rmax)); yb = min(32, int(max(y0, y1) + rmax) + 1)
        for x in range(xa, xb):
            for y in range(ya, yb):
                dx, dy = x - x0, y - y0
                t = max(0.0, min(1.0, (dx * ux + dy * uy) / seg))
                signed = dx * nx + dy * ny
                rr = r_base + (r_tip - r_base) * t
                if abs(signed) > rr:
                    continue
                if abs(signed) > rr * 0.72 and rng.random() < 0.45:
                    continue  # ragged edge
                shade = 0.5 - 0.5 * (signed / max(rr, 0.3))
                col = lerp3(stem_lo, stem_hi, max(0.0, min(1.0, shade)))
                if t > 0.85:  # darker growing tip
                    col = lerp3(col, stem_lo, (t - 0.85) / 0.15 * 0.4)
                buf[y][x] = col

    def draw_compound_leaf(buf, x0, y0, x1, y1, col_lit, col_shade, rng, rachis=True):
        """Pinnate compound potato leaf: leaflet pairs + terminal leaflet
        arranged along a rachis that doubles as the stem."""
        seg = math.hypot(x1 - x0, y1 - y0)
        if seg < 2.5:
            return
        ang = math.degrees(math.atan2(y1 - y0, x1 - x0))
        n_pairs = max(1, min(3, int(seg / 5.0)))
        for i in range(n_pairs):
            t = (i + 1) / (n_pairs + 1)
            lx, ly = x0 + (x1 - x0) * t, y0 + (y1 - y0) * t
            llen = seg * rng.uniform(0.36, 0.5) * (1.0 - t * 0.28)
            for side in (-1, 1):
                la = ang + side * rng.uniform(44, 62)
                draw_leaflet(buf, lx, ly, llen, llen * 0.42, la, col_lit, col_shade, rng)
        tlen = seg * rng.uniform(0.5, 0.6)
        draw_leaflet(buf, x1, y1, tlen, tlen * 0.4, ang + rng.uniform(-10, 10),
                     col_lit, col_shade, rng)
        if rachis:
            draw_stem(buf, x0, y0, x1, y1, 1.05, 0.45, rng)

    def draw_flower(buf, fx, fy, rng):
        """Small 5-petal white flower with a golden center."""
        for k in range(5):
            ang = k * 2.39996 + rng.uniform(-0.18, 0.18)
            cx = fx + math.cos(ang) * 2.1
            cy = fy + math.sin(ang) * 2.1
            for x in range(max(0, int(cx - 2)), min(32, int(cx + 3))):
                for y in range(max(0, int(cy - 2)), min(32, int(cy + 3))):
                    if (x - cx) ** 2 + (y - cy) ** 2 <= 2.7:
                        buf[y][x] = flower_shade if rng.random() < 0.22 else flower_white
        for x in range(int(fx) - 1, int(fx) + 2):
            for y in range(int(fy) - 1, int(fy) + 2):
                if (x - fx) ** 2 + (y - fy) ** 2 <= 1.6:
                    buf[y][x] = flower_core if rng.random() < 0.35 else flower_yellow

    def draw_tuber(buf, cx, cy, rx, ry, rng):
        """Lumpy shaded potato tuber with dimple eyes."""
        for x in range(max(0, int(cx - rx - 1)), min(32, int(cx + rx + 2))):
            for y in range(max(0, int(cy - ry - 1)), min(32, int(cy + ry + 2))):
                dx, dy = (x - cx) / rx, (y - cy) / ry
                d = dx * dx + dy * dy
                jit = 0.05 * math.sin(x * 2.9 + y * 1.7)  # lumpy outline
                if d > 1.0 + jit:
                    continue
                s = 0.5 - 0.5 * ((dx + dy) * 0.6)
                col = lerp3(tuber_lo, tuber_hi, max(0.0, min(1.0, s)))
                if rng.random() < 0.07:
                    col = lerp3(col, tuber_hi, 0.35)
                buf[y][x] = col
        for _ in range(rng.randint(2, 4)):
            ex = int(cx + rng.uniform(-rx * 0.55, rx * 0.55))
            ey = int(cy + rng.uniform(-ry * 0.55, ry * 0.55))
            if 0 <= ex < 32 and 0 <= ey < 32:
                buf[ey][ex] = tuber_eye
                if ey - 1 >= 0:
                    buf[ey - 1][ex] = lerp3(tuber_eye, tuber_hi, 0.55)

    def draw_soil_mound(buf, cx, cy, w, h, rng):
        """Low regolith mound with pebbles under the plant base."""
        for x in range(max(0, int(cx - w)), min(32, int(cx + w) + 1)):
            for y in range(max(0, int(cy - h)), min(32, int(cy + h) + 1)):
                dx = (x - cx) / w
                top = cy - h * (1.0 - dx * dx) * 0.92
                if y < top:
                    continue
                if rng.random() < 0.14:
                    buf[y][x] = soil_peb if rng.random() < 0.55 else soil_lo
                else:
                    s = 0.5 - 0.5 * ((dx + (y - cy) / h) * 0.5)
                    buf[y][x] = lerp3(soil_lo, soil_hi, max(0.0, min(1.0, s)))

    def draw_root(buf, x0, y0, x1, y1, col):
        """Thin 1px root tendril."""
        seg = math.hypot(x1 - x0, y1 - y0)
        steps = max(2, int(seg * 2))
        for i in range(steps + 1):
            t = i / steps
            x = int(round(x0 + (x1 - x0) * t))
            y = int(round(y0 + (y1 - y0) * t))
            if 0 <= x < 32 and 0 <= y < 32:
                buf[y][x] = col

    BX, BY = 16, 31  # plant base (bottom centre of the cross texture)

    for stage in range(8):
        buf = [[(0, 0, 0, 0)] * 32 for _ in range(32)]
        rng = random.Random(500 + stage * 37)  # deterministic detail

        if stage == 0:
            # Tiny sprout: stalk with two cotyledon leaves
            draw_stem(buf, BX, BY, 16, 24, 1.1, 0.5, rng)
            for side in (-1, 1):
                draw_leaflet(buf, 16, 24, 3.6, 1.7, 60 * side + rng.uniform(-8, 8),
                             greens[2], greens[1], rng, vein=False)
            draw_soil_mound(buf, BX, BY - 1, 1.8, 1.2, rng)
        elif stage == 1:
            # Small seedling: taller stalk, two tiny compound leaves
            draw_stem(buf, BX, BY, 16, 21, 1.2, 0.5, rng)
            draw_compound_leaf(buf, 15.5, 25, 13, 20.5, greens[3], greens[1], rng)
            draw_compound_leaf(buf, 16.5, 25, 19, 20.5, greens[2], greens[0], rng)
            draw_leaflet(buf, 16, 20, 3.2, 1.5, -80 + rng.uniform(-6, 6),
                         greens[3], greens[1], rng, vein=False)
            draw_soil_mound(buf, BX, BY - 1, 2.4, 1.4, rng)
        elif stage == 2:
            # Small bush: three leafy stalks
            draw_compound_leaf(buf, 15, 31, 12.5, 17.5, greens[3], greens[1], rng)
            draw_compound_leaf(buf, 17, 31, 19.5, 17.0, greens[2], greens[0], rng)
            draw_compound_leaf(buf, 16, 31, 16, 18.5, greens[4], greens[1], rng)
            draw_soil_mound(buf, BX, BY - 1, 3.0, 1.5, rng)
        elif stage == 3:
            # Growing bush with side branches
            draw_compound_leaf(buf, 15, 31, 11.5, 15.5, greens[3], greens[1], rng)
            draw_compound_leaf(buf, 17, 31, 20.5, 15.0, greens[2], greens[0], rng)
            draw_compound_leaf(buf, 16, 31, 16, 15.5, greens[4], greens[1], rng)
            draw_compound_leaf(buf, 13.5, 20, 11.5, 17.5, greens[3], greens[0], rng)
            draw_compound_leaf(buf, 18.5, 20, 20.5, 17.5, greens[3], greens[0], rng)
            draw_soil_mound(buf, BX, BY - 1, 3.6, 1.6, rng)
        elif stage == 4:
            # Fuller bush: dark back layer + bright front layer
            draw_compound_leaf(buf, 15.5, 31, 13.5, 14.0, greens[1], greens[0], rng)
            draw_compound_leaf(buf, 16.5, 31, 18.5, 13.5, greens[1], greens[0], rng)
            draw_compound_leaf(buf, 15, 31, 11.0, 13.5, greens[3], greens[1], rng)
            draw_compound_leaf(buf, 17, 31, 21.0, 13.0, greens[3], greens[1], rng)
            draw_compound_leaf(buf, 16, 31, 16, 12.5, greens[4], greens[2], rng)
            draw_compound_leaf(buf, 13.0, 19, 11.0, 16.0, greens[2], greens[0], rng)
            draw_compound_leaf(buf, 19.0, 19, 21.0, 16.0, greens[2], greens[0], rng)
            draw_soil_mound(buf, BX, BY - 1, 4.2, 1.8, rng)
        elif stage == 5:
            # Flowering: denser canopy + first white flowers
            draw_compound_leaf(buf, 15.5, 31, 13.0, 12.5, greens[1], greens[0], rng)
            draw_compound_leaf(buf, 16.5, 31, 19.0, 12.0, greens[1], greens[0], rng)
            draw_compound_leaf(buf, 15, 31, 10.5, 12.0, greens[3], greens[1], rng)
            draw_compound_leaf(buf, 17, 31, 21.5, 11.5, greens[3], greens[1], rng)
            draw_compound_leaf(buf, 16, 31, 16, 11.0, greens[4], greens[2], rng)
            draw_compound_leaf(buf, 12.5, 18, 10.5, 15.0, greens[2], greens[0], rng)
            draw_compound_leaf(buf, 19.5, 18, 21.5, 15.0, greens[2], greens[0], rng)
            draw_flower(buf, 14.5, 10.5, rng)
            draw_flower(buf, 17.5, 10.0, rng)
            draw_soil_mound(buf, BX, BY - 1, 4.6, 1.9, rng)
        elif stage == 6:
            # Near mature: tall dense canopy, three flowers
            draw_compound_leaf(buf, 15.5, 31, 13.0, 11.5, greens[1], greens[0], rng)
            draw_compound_leaf(buf, 16.5, 31, 19.0, 11.0, greens[1], greens[0], rng)
            draw_compound_leaf(buf, 15, 31, 10.0, 11.0, greens[3], greens[1], rng)
            draw_compound_leaf(buf, 17, 31, 22.0, 10.5, greens[3], greens[1], rng)
            draw_compound_leaf(buf, 16, 31, 16, 9.5, greens[4], greens[2], rng)
            draw_compound_leaf(buf, 12.0, 17, 10.0, 13.5, greens[2], greens[0], rng)
            draw_compound_leaf(buf, 20.0, 17, 22.0, 13.5, greens[2], greens[0], rng)
            draw_compound_leaf(buf, 13.5, 15, 12.0, 11.5, greens[3], greens[1], rng)
            draw_compound_leaf(buf, 18.5, 15, 20.0, 11.5, greens[3], greens[1], rng)
            draw_flower(buf, 14.0, 9.0, rng)
            draw_flower(buf, 18.0, 8.5, rng)
            draw_flower(buf, 16.0, 8.0, rng)
            draw_soil_mound(buf, BX, BY - 1, 5.0, 2.0, rng)
        else:
            # Mature: dense canopy, flowers, visible tubers with roots
            # in a regolith mound - the harvest-ready plant
            draw_compound_leaf(buf, 15.5, 31, 12.5, 10.5, greens[1], greens[0], rng)
            draw_compound_leaf(buf, 16.5, 31, 19.5, 10.0, greens[1], greens[0], rng)
            draw_compound_leaf(buf, 15, 31, 9.5, 10.0, greens[3], greens[1], rng)
            draw_compound_leaf(buf, 17, 31, 22.5, 9.5, greens[3], greens[1], rng)
            draw_compound_leaf(buf, 16, 31, 16, 8.5, greens[4], greens[2], rng)
            draw_compound_leaf(buf, 11.5, 16, 9.5, 12.5, greens[2], greens[0], rng)
            draw_compound_leaf(buf, 20.5, 16, 22.5, 12.5, greens[2], greens[0], rng)
            draw_compound_leaf(buf, 13.0, 14, 11.5, 10.5, greens[3], greens[1], rng)
            draw_compound_leaf(buf, 19.0, 14, 20.5, 10.5, greens[3], greens[1], rng)
            draw_compound_leaf(buf, 14.5, 12, 13.5, 8.5, greens[2], greens[0], rng)
            draw_compound_leaf(buf, 17.5, 12, 18.5, 8.5, greens[2], greens[0], rng)
            # regolith mound, then roots and tubers resting on it
            draw_soil_mound(buf, BX, BY - 1, 6.0, 2.4, rng)
            draw_root(buf, 12.5, 28.5, 16, 30.5, tuber_lo)
            draw_root(buf, 20.5, 29.0, 16, 30.5, tuber_lo)
            draw_tuber(buf, 12.0, 29.0, 3.3, 2.2, rng)
            draw_tuber(buf, 20.5, 29.5, 2.8, 2.0, rng)
            draw_tuber(buf, 16.5, 30.0, 2.2, 1.5, rng)   # center, half-buried
            draw_soil_mound(buf, 16.5, 30.6, 1.8, 1.0, rng)  # embed the center tuber
            draw_flower(buf, 13.5, 8.0, rng)
            draw_flower(buf, 18.5, 7.5, rng)
            draw_flower(buf, 15.5, 6.5, rng)
            draw_flower(buf, 11.0, 13.0, rng)

        img = Image.new("RGBA", (32, 32))
        img.putdata([c for row in buf for c in row])
        img.save(os.path.join(ASSETS_DIR, f"textures/block/martian_potato_stage{stage}.png"))

    # Raw Martian Potato Item (32x32)
    img_raw = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw_raw = ImageDraw.Draw(img_raw)
    # Organic oblong potato shape with warm Martian terracotta skin
    draw_raw.ellipse([6, 8, 26, 24], fill=(175, 105, 55, 255))
    draw_raw.ellipse([8, 9, 24, 22], fill=(205, 130, 75, 255))
    draw_raw.ellipse([10, 10, 20, 18], fill=(225, 155, 95, 255))
    # Potato eyes / dimples
    for ex, ey in [(11, 13), (17, 12), (21, 16), (14, 19), (22, 19)]:
        draw_raw.point((ex, ey), fill=(110, 60, 30, 255))
        draw_raw.point((ex + 1, ey), fill=(140, 80, 45, 255))
    img_raw.save(os.path.join(ASSETS_DIR, "textures/item/martian_potato.png"))

    # Baked Martian Potato Item (32x32)
    img_baked = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw_baked = ImageDraw.Draw(img_baked)
    # Roasted golden potato with crispy split skin and steaming fluffy interior
    draw_baked.ellipse([5, 8, 27, 24], fill=(130, 70, 32, 255))
    draw_baked.ellipse([7, 9, 25, 22], fill=(185, 115, 55, 255))
    # Steaming center split
    draw_baked.ellipse([10, 12, 22, 19], fill=(245, 220, 130, 255))
    draw_baked.ellipse([12, 13, 20, 18], fill=(255, 245, 180, 255))
    # Steam wisps
    draw_baked.line([(14, 7), (13, 5), (15, 3)], fill=(220, 230, 240, 180))
    draw_baked.line([(18, 6), (19, 4), (17, 2)], fill=(220, 230, 240, 180))
    img_baked.save(os.path.join(ASSETS_DIR, "textures/item/baked_martian_potato.png"))

# --- 2. MODELS & BLOCKSTATES ---

def make_airlock_models_and_blockstates():
    models_block = os.path.join(ASSETS_DIR, "models/block")
    models_item = os.path.join(ASSETS_DIR, "models/item")
    blockstates = os.path.join(ASSETS_DIR, "blockstates")
    ensure_dir(models_block)
    ensure_dir(models_item)
    ensure_dir(blockstates)

    # Airlock uses a static bulkhead frame model (the animated hatch leaf is a
    # BlockEntityRenderer), so the blockstate only needs facing + half variants.
    frame_bottom = {
        "ambientocclusion": False,
        "textures": {
            "frame": "alyrioncore:block/airlock_frame",
            "particle": "#frame"
        },
        "elements": [
            {
                "name": "left_jamb",
                "from": [0, 0, 0], "to": [16, 16, 2],
                "faces": {
                    "north": {"uv": [0, 0, 16, 16], "texture": "#frame"},
                    "south": {"uv": [0, 0, 16, 16], "texture": "#frame"},
                    "west": {"uv": [0, 0, 2, 16], "texture": "#frame"},
                    "east": {"uv": [14, 0, 16, 16], "texture": "#frame"},
                    "up": {"uv": [0, 0, 16, 2], "texture": "#frame"},
                    "down": {"uv": [0, 0, 16, 2], "texture": "#frame"}
                }
            },
            {
                "name": "right_jamb",
                "from": [0, 0, 14], "to": [16, 16, 16],
                "faces": {
                    "north": {"uv": [0, 0, 16, 16], "texture": "#frame"},
                    "south": {"uv": [0, 0, 16, 16], "texture": "#frame"},
                    "west": {"uv": [0, 0, 2, 16], "texture": "#frame"},
                    "east": {"uv": [14, 0, 16, 16], "texture": "#frame"},
                    "up": {"uv": [0, 0, 16, 2], "texture": "#frame"},
                    "down": {"uv": [0, 0, 16, 2], "texture": "#frame"}
                }
            },
            {
                "name": "sill",
                "from": [0, 0, 2], "to": [16, 2, 14],
                "faces": {
                    "north": {"uv": [0, 0, 16, 2], "texture": "#frame"},
                    "south": {"uv": [0, 0, 16, 2], "texture": "#frame"},
                    "west": {"uv": [0, 0, 12, 2], "texture": "#frame"},
                    "east": {"uv": [0, 0, 12, 2], "texture": "#frame"},
                    "up": {"uv": [0, 0, 16, 12], "texture": "#frame"},
                    "down": {"uv": [0, 0, 16, 12], "texture": "#frame"}
                }
            }
        ]
    }
    frame_top = json.loads(json.dumps(frame_bottom))
    frame_top["elements"] = frame_bottom["elements"][:2] + [{
        "name": "header",
        "from": [0, 14, 2], "to": [16, 16, 14],
        "faces": {
            "north": {"uv": [0, 0, 16, 2], "texture": "#frame"},
            "south": {"uv": [0, 0, 16, 2], "texture": "#frame"},
            "west": {"uv": [0, 0, 12, 2], "texture": "#frame"},
            "east": {"uv": [0, 0, 12, 2], "texture": "#frame"},
            "up": {"uv": [0, 0, 16, 12], "texture": "#frame"},
            "down": {"uv": [0, 0, 16, 12], "texture": "#frame"}
        }
    }]
    write_json(os.path.join(models_block, "airlock_frame_bottom.json"), frame_bottom)
    write_json(os.path.join(models_block, "airlock_frame_top.json"), frame_top)

    # Item model
    write_json(os.path.join(models_item, "airlock.json"), {
        "parent": "minecraft:item/generated",
        "textures": {
            "layer0": "alyrioncore:item/airlock"
        }
    })

    # Blockstate: static frame, facing + half only (hinge/open handled by renderer)
    variants = {}
    for facing, y_rot in [("east", 0), ("south", 90), ("west", 180), ("north", 270)]:
        variants[f"facing={facing},half=lower"] = {
            "model": "alyrioncore:block/airlock_frame_bottom", "y": y_rot
        }
        variants[f"facing={facing},half=upper"] = {
            "model": "alyrioncore:block/airlock_frame_top", "y": y_rot
        }
    write_json(os.path.join(blockstates, "airlock.json"), {"variants": variants})

def make_farmland_models_and_blockstates():
    models_block = os.path.join(ASSETS_DIR, "models/block")
    models_item = os.path.join(ASSETS_DIR, "models/item")
    blockstates = os.path.join(ASSETS_DIR, "blockstates")

    write_json(os.path.join(models_block, "regolith_farmland.json"), {
        "parent": "minecraft:block/template_farmland",
        "textures": {
            "dirt": "alyrioncore:block/regolith_farmland_side",
            "top": "alyrioncore:block/regolith_farmland_dry",
            "particle": "alyrioncore:block/regolith_farmland_dry"
        }
    })

    write_json(os.path.join(models_block, "regolith_farmland_moist.json"), {
        "parent": "minecraft:block/template_farmland",
        "textures": {
            "dirt": "alyrioncore:block/regolith_farmland_side",
            "top": "alyrioncore:block/regolith_farmland_moist",
            "particle": "alyrioncore:block/regolith_farmland_moist"
        }
    })

    write_json(os.path.join(models_item, "regolith_farmland.json"), {
        "parent": "alyrioncore:block/regolith_farmland"
    })

    # Blockstate moisture 0-6 = dry, 7 = moist
    variants = {}
    for m in range(7):
        variants[f"moisture={m}"] = {"model": "alyrioncore:block/regolith_farmland"}
    variants["moisture=7"] = {"model": "alyrioncore:block/regolith_farmland_moist"}
    write_json(os.path.join(blockstates, "regolith_farmland.json"), {"variants": variants})

def make_crop_and_food_models():
    models_block = os.path.join(ASSETS_DIR, "models/block")
    models_item = os.path.join(ASSETS_DIR, "models/item")
    blockstates = os.path.join(ASSETS_DIR, "blockstates")

    # Crop stage models (render_type: cutout so transparency is honored in-world)
    for stage in range(8):
        write_json(os.path.join(models_block, f"martian_potato_stage{stage}.json"), {
            "parent": "minecraft:block/crop",
            "textures": {
                "crop": f"alyrioncore:block/martian_potato_stage{stage}"
            },
            "render_type": "minecraft:cutout"
        })

    # Crop blockstate
    variants = {}
    for age in range(8):
        variants[f"age={age}"] = {"model": f"alyrioncore:block/martian_potato_stage{age}"}
    write_json(os.path.join(blockstates, "martian_potato_crop.json"), {"variants": variants})

    # Food item models
    write_json(os.path.join(models_item, "martian_potato.json"), {
        "parent": "minecraft:item/generated",
        "textures": {
            "layer0": "alyrioncore:item/martian_potato"
        }
    })

    write_json(os.path.join(models_item, "baked_martian_potato.json"), {
        "parent": "minecraft:item/generated",
        "textures": {
            "layer0": "alyrioncore:item/baked_martian_potato"
        }
    })

# --- 3. RECIPES ---

def make_recipes():
    recipe_dir = os.path.join(DATA_DIR, "recipe")
    ensure_dir(recipe_dir)

    # Airlock Shaped Recipe
    write_json(os.path.join(recipe_dir, "airlock.json"), {
        "type": "minecraft:crafting_shaped",
        "pattern": [
            "MIM",
            "MGM",
            "MRM"
        ],
        "key": {
            "M": {"item": "alyrioncore:meteoric_iron_ingot"},
            "I": {"item": "minecraft:iron_ingot"},
            "G": {"item": "minecraft:glass_pane"},
            "R": {"item": "minecraft:redstone"}
        },
        "result": {
            "count": 1,
            "id": "alyrioncore:airlock"
        }
    })

    # Smelting Baked Martian Potato
    write_json(os.path.join(recipe_dir, "baked_martian_potato_smelting.json"), {
        "type": "minecraft:smelting",
        "ingredient": {"item": "alyrioncore:martian_potato"},
        "result": {"id": "alyrioncore:baked_martian_potato"},
        "experience": 0.35,
        "cookingtime": 200
    })

    # Smoking Baked Martian Potato
    write_json(os.path.join(recipe_dir, "baked_martian_potato_smoking.json"), {
        "type": "minecraft:smoking",
        "ingredient": {"item": "alyrioncore:martian_potato"},
        "result": {"id": "alyrioncore:baked_martian_potato"},
        "experience": 0.35,
        "cookingtime": 100
    })

    # Campfire Cooking
    write_json(os.path.join(recipe_dir, "baked_martian_potato_campfire.json"), {
        "type": "minecraft:campfire_cooking",
        "ingredient": {"item": "alyrioncore:martian_potato"},
        "result": {"id": "alyrioncore:baked_martian_potato"},
        "experience": 0.35,
        "cookingtime": 600
    })

# --- 4. LOOT TABLES ---

def make_loot_tables():
    loot_dir = os.path.join(DATA_DIR, "loot_table/blocks")
    loot_tables_dir = os.path.join(DATA_DIR, "loot_tables/blocks")
    ensure_dir(loot_dir)
    ensure_dir(loot_tables_dir)

    # Airlock (only drops when lower half is broken)
    airlock_loot = {
        "type": "minecraft:block",
        "pools": [
            {
                "rolls": 1,
                "bonus_rolls": 0,
                "entries": [
                    {
                        "type": "minecraft:item",
                        "name": "alyrioncore:airlock",
                        "conditions": [
                            {
                                "condition": "minecraft:block_state_property",
                                "block": "alyrioncore:airlock",
                                "properties": {
                                    "half": "lower"
                                }
                            }
                        ]
                    }
                ],
                "conditions": [
                    {
                        "condition": "minecraft:survives_explosion"
                    }
                ]
            }
        ]
    }
    write_json(os.path.join(loot_dir, "airlock.json"), airlock_loot)
    write_json(os.path.join(loot_tables_dir, "airlock.json"), airlock_loot)

    # Regolith Farmland (drops martian regolith)
    farmland_loot = {
        "type": "minecraft:block",
        "pools": [
            {
                "rolls": 1,
                "bonus_rolls": 0,
                "entries": [
                    {
                        "type": "minecraft:item",
                        "name": "alyrioncore:martian_regolith"
                    }
                ],
                "conditions": [
                    {
                        "condition": "minecraft:survives_explosion"
                    }
                ]
            }
        ]
    }
    write_json(os.path.join(loot_dir, "regolith_farmland.json"), farmland_loot)
    write_json(os.path.join(loot_tables_dir, "regolith_farmland.json"), farmland_loot)

    # Martian Potato Crop
    crop_loot = {
        "type": "minecraft:block",
        "pools": [
            {
                "rolls": 1,
                "bonus_rolls": 0,
                "entries": [
                    {
                        "type": "minecraft:item",
                        "name": "alyrioncore:martian_potato"
                    }
                ]
            },
            {
                "rolls": 1,
                "bonus_rolls": 0,
                "entries": [
                    {
                        "type": "minecraft:item",
                        "name": "alyrioncore:martian_potato",
                        "functions": [
                            {
                                "function": "minecraft:apply_bonus",
                                "enchantment": "minecraft:fortune",
                                "formula": "minecraft:binomial_with_bonus_count",
                                "parameters": {
                                    "extra": 3,
                                    "probability": 0.5714286
                                }
                            }
                        ]
                    }
                ],
                "conditions": [
                    {
                        "condition": "minecraft:block_state_property",
                        "block": "alyrioncore:martian_potato_crop",
                        "properties": {
                            "age": "7"
                        }
                    }
                ]
            }
        ]
    }
    write_json(os.path.join(loot_dir, "martian_potato_crop.json"), crop_loot)
    write_json(os.path.join(loot_tables_dir, "martian_potato_crop.json"), crop_loot)

# --- 5. TAGS ---

def make_tags():
    # Pickaxe mineable
    pick_path = os.path.join(MC_DATA_DIR, "tags/block/mineable/pickaxe.json")
    if os.path.exists(pick_path):
        with open(pick_path, "r", encoding="utf-8") as f:
            data = json.load(f)
        vals = data.get("values", [])
        if "alyrioncore:airlock" not in vals:
            vals.append("alyrioncore:airlock")
        data["values"] = vals
        write_json(pick_path, data)

    # Shovel mineable
    shov_dir = os.path.join(MC_DATA_DIR, "tags/block/mineable")
    ensure_dir(shov_dir)
    shov_path = os.path.join(shov_dir, "shovel.json")
    vals = ["alyrioncore:regolith_farmland", "alyrioncore:martian_regolith", "alyrioncore:martian_sand"]
    if os.path.exists(shov_path):
        with open(shov_path, "r", encoding="utf-8") as f:
            data = json.load(f)
        for v in vals:
            if v not in data.get("values", []):
                data.setdefault("values", []).append(v)
    else:
        data = {"replace": False, "values": vals}
    write_json(shov_path, data)

    # Doors tag
    doors_dir = os.path.join(MC_DATA_DIR, "tags/block")
    ensure_dir(doors_dir)
    write_json(os.path.join(doors_dir, "doors.json"), {
        "replace": False,
        "values": ["alyrioncore:airlock"]
    })

# --- 6. LOCALIZATION ---

def update_lang():
    lang_path = os.path.join(ASSETS_DIR, "lang/en_us.json")
    with open(lang_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    data["block.alyrioncore.airlock"] = "Pressurized Habitat Airlock"
    data["block.alyrioncore.regolith_farmland"] = "Martian Regolith Farmland"
    data["block.alyrioncore.martian_potato_crop"] = "Martian Potato Crop"
    data["item.alyrioncore.martian_potato"] = "Martian Potato"
    data["item.alyrioncore.baked_martian_potato"] = "Baked Martian Potato"

    write_json(lang_path, data)

def main():
    print("Generating assets for Pressurized Habitats, Airlocks & Greenhouse System...")
    create_airlock_textures()
    create_farmland_textures()
    create_crop_and_food_textures()
    make_airlock_models_and_blockstates()
    make_farmland_models_and_blockstates()
    make_crop_and_food_models()
    make_recipes()
    make_loot_tables()
    make_tags()
    update_lang()
    print("All assets successfully generated!")

if __name__ == "__main__":
    main()
