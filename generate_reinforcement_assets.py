#!/usr/bin/env python3
"""Generate all reinforcement assets for AlyrionCore.

Reinforcement plates reinforce any breakable, BE-free, non-fluid block. The
reinforced block keeps the original block's look (rendered by a BlockEntity
renderer) plus a protruding riveted-plate frame (the blockstate model) on the
air-facing sides.

This script is the source of truth for:

  - textures/block/reinforced_overlay_<tier>.png        (riveted plate, 16x16)
  - textures/item/<tier>_reinforcement_plate.png        (same art, item copy)
  - models/block/reinforced_shell_<tier>.json           (plate-frame shell)
  - blockstates/reinforced_block.json                   (tier variants)
  - models/item/<tier>_reinforcement_plate.json
  - data/alyrioncore/recipe/<tier>_reinforcement_plate.json   (2x2 -> 8 plates)

Run from the repo root:
    python3 generate_reinforcement_assets.py
"""

import json
import os

ROOT = os.path.dirname(os.path.abspath(__file__))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "alyrioncore")
DATA = os.path.join(ROOT, "src", "main", "resources", "data", "alyrioncore")

# --- Tier definitions -------------------------------------------------------
# name, hits, ingredient (recipe), palette (darkest -> lightest), rivet highlight
TIERS = {
    "iron": {
        "hits": 3,
        "ingredient": "minecraft:iron_ingot",
        "palette": ["#9d9c9c", "#b1b0b0", "#dcdcdc", "#ececec", "#f2f2f2"],
        "rivet": "#f2f2f2",
    },
    "diamond": {
        "hits": 10,
        "ingredient": "minecraft:diamond",
        "palette": ["#0ebabd", "#15c2c6", "#3de0e5", "#70fbf0", "#9efeeb"],
        "rivet": "#9efeeb",
    },
    "meteoric_iron": {
        "hits": 30,
        "ingredient": "alyrioncore:meteoric_iron_ingot",
        # indexed from the mod's meteoric_iron_ingot.png (steel-blue + teal flecks)
        "palette": ["#1a2c42", "#2b425e", "#40607e", "#8db4cb", "#dbeef5"],
        "rivet": "#2fd4bd",  # the meteoric teal accent
    },
    "netherite": {
        "hits": 100,
        "ingredient": "minecraft:netherite_ingot",
        "palette": ["#241e1f", "#31292a", "#3c3232", "#4d494d", "#5a575a"],
        "rivet": "#5a575a",
    },
}

RING = {0, 1, 14, 15}          # the 2px border ring
RIVET_CELLS = [2, 6, 10, 14]   # 2x2 rivet cells along the ring, every 4px


def hex_rgb(h):
    h = h.lstrip("#")
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16))


def lerp(a, b, t):
    return a + (b - a) * t


def mix(c1, c2, t):
    return tuple(int(lerp(c1[i], c2[i], t)) for i in range(3))


# --- Plate texture ----------------------------------------------------------
def draw_plate(palette, rivet_hex):
    """16x16 riveted reinforcement plate: 2px beveled ring with 2x2 rivets
    every 4px, brushed-metal center panel. Light from the top-left."""
    s = [hex_rgb(c) for c in palette]
    darkest, dark, mid, light, bright = s[0], s[1], s[2], s[3], s[4]
    rivet = hex_rgb(rivet_hex)
    img = [[(0, 0, 0, 0) for _ in range(16)] for _ in range(16)]

    for y in range(16):
        for x in range(16):
            if x in RING or y in RING:
                # 2-tone bevel: outer 1px catches the light / deepest shadow
                if y <= 1:      # top: lit
                    c = bright if y == 0 else mix(light, mid, 0.35)
                elif x <= 1:    # left: lit
                    c = bright if x == 0 else mix(light, mid, 0.35)
                elif y >= 14:   # bottom: shadowed
                    c = darkest if y == 15 else dark
                else:           # right: shadowed
                    c = darkest if x == 15 else dark
                img[y][x] = (*c, 255)
            elif x in (2, 13) or y in (2, 13):
                img[y][x] = (*darkest, 255)          # 1px inset panel line
            else:
                img[y][x] = (*mid, 255)

    # brushed-metal grain in the panel (subtle diagonal streaks)
    for y in range(3, 13):
        for x in range(3, 13):
            if (x + y) % 3 == 1:
                img[y][x] = (*mix(mid, light, 0.5), 255)
            elif (x + y) % 6 == 4:
                img[y][x] = (*mix(mid, dark, 0.45), 255)

    # 2x2 rivets on the ring: top-left highlight, bottom-right shadow
    def rivet_at(x0, y0):
        img[y0][x0] = (*rivet, 255)
        img[y0][x0 + 1] = (*mix(rivet, mid, 0.4), 255)
        img[y0 + 1][x0] = (*mix(rivet, mid, 0.4), 255)
        img[y0 + 1][x0 + 1] = (*darkest, 255)

    for cx in RIVET_CELLS:
        rivet_at(cx, 0)      # top edge
        rivet_at(cx, 14)     # bottom edge
        rivet_at(0, cx)      # left edge
        rivet_at(14, cx)     # right edge
    return img


# --- Shell model ------------------------------------------------------------
# Vanilla per-face UV convention (u, v axes):
#   north(-z) u=16-x v=16-y | south(+z) u=x v=16-y | west(-x) u=z v=16-y
#   east(+x) u=16-z v=16-y  | up(+y) u=x v=z       | down(-y) u=x v=16-z
FACE_AXIS = {
    "north": (2, 1),   # (u axis index, sign)
    "south": (2, 1),
    "west": (0, 1),
    "east": (0, -1),
    "up": (0, 1),
    "down": (0, 1),
}
# For each face: axis of the plane, fixed plane coordinate, and which of the
# two free axes maps to u (the other maps to v), plus v axis + flip.
# face -> (plane_axis, plane_val, u_axis, u_flip, v_axis, v_flip)
FACE_META = {
    # plane axis 0=x 1=y 2=z ; u/v axis + flip per the convention table
    "north": (2, 0.0, 0, -1, 1, -1),   # u=16-x, v=16-y
    "south": (2, 16.0, 0, 1, 1, -1),   # u=x,   v=16-y
    "west": (0, 0.0, 2, 1, 1, -1),     # u=z,   v=16-y
    "east": (0, 16.0, 2, -1, 1, -1),   # u=16-z, v=16-y
    "up": (1, 16.0, 0, 1, 2, 1),       # u=x,   v=z
    "down": (1, 0.0, 0, 1, 2, -1),     # u=x,   v=16-z
}
OUT = {"north": -0.1, "south": 0.1, "west": -0.1, "east": 0.1, "up": 0.1, "down": -0.1}


def uv_window(face, frm, to):
    """Compute the uv window [u1, v1, u2, v2] for a face of an element."""
    plane_axis, plane, u_axis, u_flip, v_axis, v_flip = FACE_META[face]
    lo = [frm[0], frm[1], frm[2]]
    hi = [to[0], to[1], to[2]]
    # the face lies in the plane: the two free axes are the others
    u0, u1 = lo[u_axis], hi[u_axis]
    v0, v1 = lo[v_axis], hi[v_axis]
    if u_flip < 0:
        u0, u1 = 16 - u1, 16 - u0
    if v_flip < 0:
        v0, v1 = 16 - v1, 16 - v0
    w = [min(u0, u1), min(v0, v1), max(u0, u1), max(v0, v1)]
    # clamp into the 0..16 sprite and keep a non-degenerate window
    for i in range(4):
        w[i] = max(0.0, min(16.0, round(w[i], 3)))
    if w[2] - w[0] < 0.001:
        w[2] = min(16.0, w[0] + 0.001)
    if w[3] - w[1] < 0.001:
        w[3] = min(16.0, w[1] + 0.001)
    return w


def element(frm, to):
    el = {"from": [round(v, 3) for v in frm], "to": [round(v, 3) for v in to], "faces": {}}
    for face, meta in FACE_META.items():
        el["faces"][face] = {"uv": uv_window(face, frm, to), "texture": "#plate"}
    return el


def shell_elements():
    """28 elements: per-face 2px frames (24) + 4 vertical corner posts."""
    els = []
    for face, out in OUT.items():
        plane_axis = FACE_META[face][0]
        plane = FACE_META[face][1]
        frm = [0.0, 0.0, 0.0]
        to = [16.0, 16.0, 16.0]
        frm[plane_axis] = min(plane, plane + out)
        to[plane_axis] = max(plane, plane + out)
        # four strips along the face's edges (2 units wide)
        free = [a for a in (0, 1, 2) if a != plane_axis]
        a0, a1 = free
        for edge, sign in ((0, -1), (1, 1)):      # low edge / high edge along a0
            s = [0.0, 0.0, 0.0]
            e = [16.0, 16.0, 16.0]
            s[a0] = 0.0 if sign < 0 else 14.0
            e[a0] = 2.0 if sign < 0 else 16.0
            els.append(element(
                [frm[i] if i == plane_axis else s[i] for i in range(3)],
                [to[i] if i == plane_axis else e[i] for i in range(3)],
            ))
            s = [0.0, 0.0, 0.0]
            e = [16.0, 16.0, 16.0]
            s[a1] = 0.0 if sign < 0 else 14.0
            e[a1] = 2.0 if sign < 0 else 16.0
            s[a0], e[a0] = 2.0, 14.0
            els.append(element(
                [frm[i] if i == plane_axis else s[i] for i in range(3)],
                [to[i] if i == plane_axis else e[i] for i in range(3)],
            ))
    # four vertical corner posts (fill the corner notches left by the frames)
    for xs in ((-0.1, 0.0), (16.0, 16.1)):
        for zs in ((-0.1, 0.0), (16.0, 16.1)):
            els.append(element((xs[0], -0.1, zs[0]), (xs[1], 16.1, zs[1])))
    return els


def shell_model(tier, tex):
    return {
        "textures": {"particle": tex, "plate": tex},
        "elements": shell_elements(),
    }


# --- Crack overlay (8 damage stages) ---------------------------------------
# Crack fissures radiate from the block's edges/corners and grow with every
# absorbed hit; each stage draws the first N pixels of each path plus
# missing-chunk holes, so the damage reads as a continuous progression from
# pristine (stage 0) to nearly shattered (stage 7).
CRACK_PATHS = [
    ((3, 0), (1, 1)),      # 0 top edge -> down-right
    ((12, 0), (-1, 1)),    # 1 top edge -> down-left
    ((7, 15), (1, -1)),    # 2 bottom edge -> up-right
    ((0, 5), (1, 1)),      # 3 left edge -> down-right
    ((15, 11), (-1, -1)),  # 4 right edge -> up-left
    ((0, 0), (1, 1)),      # 5 top-left corner -> down-right
    ((15, 15), (-1, -1)),  # 6 bottom-right corner -> up-left
    ((4, 8), (1, 1)),      # 7 mid face -> down-right
    ((12, 7), (-1, 1)),    # 8 mid face -> down-left
    ((15, 0), (-1, 1)),    # 9 top-right corner -> down-left
]
STAGE_PATHS = {
    0: [],
    1: [(0, 2), (1, 2), (2, 2)],
    2: [(0, 4), (1, 4), (2, 4), (3, 3), (4, 3)],
    3: [(0, 6), (1, 6), (2, 6), (3, 5), (4, 5), (5, 4), (6, 4)],
    4: [(0, 8), (1, 8), (2, 8), (3, 7), (4, 7), (5, 6), (6, 6), (7, 4)],
    5: [(0, 10), (1, 10), (2, 10), (3, 9), (4, 9), (5, 8), (6, 8), (7, 6), (8, 4)],
    6: [(0, 12), (1, 12), (2, 12), (3, 11), (4, 11), (5, 10), (6, 10), (7, 8), (8, 6), (9, 5)],
    7: [(0, 16), (1, 16), (2, 16), (3, 15), (4, 15), (5, 13), (6, 13), (7, 11), (8, 9), (9, 8)],
}
STAGE_HOLES = {
    0: [], 1: [], 2: [],
    3: [(7, 4, 2)],
    4: [(9, 11, 2)],
    5: [(4, 10, 2), (12, 3, 2)],
    6: [(7, 4, 3), (11, 12, 2), (2, 8, 2)],
    7: [(3, 6, 3), (9, 4, 3), (13, 10, 2), (6, 12, 2)],
}
CRACK_CORE = (22, 22, 22, 255)
CRACK_EDGE = (230, 230, 230, 255)
HOLE_FILL = (16, 16, 16, 255)
HOLE_RIM = (8, 8, 8, 255)


def _plot(img, x, y, c):
    if 0 <= x < 16 and 0 <= y < 16:
        img[y][x] = c


def _line(img, x0, y0, dx, dy, length, color):
    x, y = x0, y0
    for _ in range(length):
        _plot(img, x, y, color)
        x += dx
        y += dy


def draw_crack(stage):
    """16x16 crack overlay: transparent background, dark fissures with a lit
    top-left edge, and dark missing-chunk holes. Stage 0 is pristine."""
    img = [[(0, 0, 0, 0) for _ in range(16)] for _ in range(16)]
    for path_idx, length in STAGE_PATHS.get(stage, []):
        (x0, y0), (dx, dy) = CRACK_PATHS[path_idx]
        _line(img, x0 - 1, y0 - 1, dx, dy, length, CRACK_EDGE)   # lit edge
        _line(img, x0, y0, dx, dy, length, CRACK_CORE)           # fissure
    for (hx, hy, size) in STAGE_HOLES.get(stage, []):
        for y in range(hy, hy + size):
            for x in range(hx, hx + size):
                rim = x == hx or y == hy or x == hx + size - 1 or y == hy + size - 1
                _plot(img, x, y, HOLE_RIM if rim else HOLE_FILL)
    return img


def crack_model(stage):
    tex = "alyrioncore:block/reinforced_crack_%d" % stage
    return {
        "textures": {"particle": tex, "crack": tex},
        "elements": [{
            "from": [-0.12, -0.12, -0.12],
            "to": [16.12, 16.12, 16.12],
            "faces": {face: {"uv": [0.0, 0.0, 16.0, 16.0], "texture": "#crack"}
                      for face in ("north", "south", "west", "east", "up", "down")},
        }],
    }


# --- Writers ----------------------------------------------------------------
def write_json(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


def main():
    import sys
    scripts = os.path.join(ROOT, "mc-scripts")
    if scripts not in sys.path:
        sys.path.insert(0, scripts)
    from mcutil import write_png  # noqa: PLC0415 (pure-stdlib sibling helper)

    for tier, t in TIERS.items():
        img = draw_plate(t["palette"], t["rivet"])
        block_tex = "alyrioncore:block/reinforced_overlay_%s" % tier
        write_png(os.path.join(ASSETS, "textures", "block", "reinforced_overlay_%s.png" % tier), img)
        write_png(os.path.join(ASSETS, "textures", "item", "%s_reinforcement_plate.png" % tier), img)
        write_json(os.path.join(ASSETS, "models", "block", "reinforced_shell_%s.json" % tier),
                   shell_model(tier, block_tex))
        write_json(os.path.join(ASSETS, "models", "item", "%s_reinforcement_plate.json" % tier),
                   {"parent": "minecraft:item/generated",
                    "textures": {"layer0": "alyrioncore:item/%s_reinforcement_plate" % tier}})
        write_json(os.path.join(DATA, "recipe", "%s_reinforcement_plate.json" % tier),
                   {"type": "minecraft:crafting_shaped",
                    "category": "misc",
                    "pattern": ["##", "##"],
                    "key": {"#": {"item": t["ingredient"]}},
                    "result": {"id": "alyrioncore:%s_reinforcement_plate" % tier, "count": 8}})

    write_json(os.path.join(ASSETS, "blockstates", "reinforced_block.json"),
               {"variants": {
                   "tier=%s" % tier: {"model": "alyrioncore:block/reinforced_shell_%s" % tier}
                   for tier in TIERS
               }})

    # 8-stage crack overlay (cumulative damage shown by the BESR)
    for stage in range(8):
        write_png(os.path.join(ASSETS, "textures", "block", "reinforced_crack_%d.png" % stage),
                  draw_crack(stage))
        write_json(os.path.join(ASSETS, "models", "block", "reinforced_crack_%d.json" % stage),
                   crack_model(stage))
    print("wrote reinforcement assets for tiers:", ", ".join(TIERS))
    print("wrote 8 crack overlay stages")


if __name__ == "__main__":
    main()
