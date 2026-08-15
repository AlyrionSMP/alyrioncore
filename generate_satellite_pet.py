from PIL import Image

# Generates the 128x32 texture atlas for the Satellite pet 3D model.
# UV regions follow the vanilla ModelPart cube layout for the parts in
# SatellitePetModel.createBodyLayer().

W, H = 128, 32
img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
px = img.load()


def hex_to_rgb(h):
    h = h.lstrip("#")
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def fill(x0, y0, x1, y1, color):
    color = hex_to_rgb(color)
    for y in range(y0, y1):
        for x in range(x0, x1):
            px[x, y] = (*color, 255)


def fill_v(x0, y0, x1, y1, color, color2):
    """Fill region with vertical gradient from color (top) to color2 (bottom)."""
    c0 = hex_to_rgb(color)
    c1 = hex_to_rgb(color2)
    for y in range(y0, y1):
        t = (y - y0) / max(1, (y1 - y0 - 1))
        c = tuple(int(a + (b - a) * t) for a, b in zip(c0, c1))
        for x in range(x0, x1):
            px[x, y] = (*c, 255)


def rect(x0, y0, x1, y1, color):
    fill(x0, y0, x1, y1, color)


SILVER = "#B9C3CE"
SILVER_DARK = "#98A3B0"
SILVER_EDGE = "#7C8894"
GOLD = "#E8B840"
GOLD_DARK = "#C99724"
GOLD_DEEP = "#8A6A1F"
SOLAR = "#2E6FD8"
SOLAR_DARK = "#1D4E9E"
SOLAR_LIGHT = "#5B93E8"
BACKING = "#4A5460"
RED = "#E0392F"
RED_DARK = "#A32820"
LIGHT_CORE = "#FFF6D8"
LIGHT_RING = "#FFE9A8"
LIGHT_SIDE = "#FFD97A"
NAVY = "#0B0F1A"

# =============================== BODY ===============================
# Cube (w8 h8 d8), texOffs(0,0) -> atlas u 0..32, v 0..16
# Face regions:
#   DOWN (8,0)-(16,8) | UP (16,0)-(24,8)
#   WEST (0,8)-(8,16) | NORTH (8,8)-(16,16) | EAST (16,8)-(24,16) | SOUTH (24,8)-(32,16)

# UP face: gold trim border, silver center, corner rivets
rect(16, 0, 24, 8, SILVER)
rect(16, 0, 24, 1, GOLD)
rect(16, 0, 17, 8, GOLD)
rect(23, 0, 24, 8, GOLD)
rect(16, 7, 24, 8, GOLD)
for cx, cy in ((17, 1), (22, 1), (17, 6), (22, 6)):
    rect(cx, cy, cx + 1, cy + 1, GOLD_DARK)

# DOWN face: dark silver with a cross brace
fill(8, 0, 16, 8, SILVER_DARK)
rect(8, 0, 16, 1, SILVER_EDGE)
rect(8, 7, 16, 8, SILVER_EDGE)
rect(11, 0, 13, 8, SILVER_EDGE)
rect(8, 3, 16, 5, SILVER_EDGE)

# WEST face: silver, corner bolts
fill(0, 8, 8, 16, SILVER)
for cx, cy in ((0, 8), (7, 8), (0, 15), (7, 15)):
    rect(cx, cy, cx + 1, cy + 1, SILVER_EDGE)

# NORTH face (front): silver with round red sensor eye
fill(8, 8, 16, 16, SILVER)
fill(8, 8, 16, 9, SILVER_EDGE)
fill(8, 15, 16, 16, SILVER_EDGE)
# red sensor disc centered at (12, 12), radius 2.5
for y in range(10, 15):
    for x in range(10, 15):
        if (x - 12) ** 2 + (y - 12) ** 2 <= 6.25:
            px[x, y] = (*hex_to_rgb(RED), 255)
for y in range(11, 14):
    for x in range(11, 14):
        if (x - 12) ** 2 + (y - 12) ** 2 <= 2.25:
            px[x, y] = (*hex_to_rgb(RED_DARK), 255)
px[12, 12] = (*hex_to_rgb("#FF6B5E"), 255)

# EAST face: silver with vertical gold stripe
fill(16, 8, 24, 16, SILVER)
rect(16, 8, 24, 9, SILVER_EDGE)
rect(16, 15, 24, 16, SILVER_EDGE)
rect(19, 8, 21, 16, GOLD)

# SOUTH face: silver, horizontal vent lines
fill(24, 8, 32, 16, SILVER_DARK)
for y in range(9, 16, 2):
    rect(24, y, 32, y + 1, SILVER_EDGE)

# =============================== PANELS ===============================
# Cube (w12 h1 d5), texOffs(32,0) -> atlas u 32..66, v 0..6
# Face regions:
#   DOWN (37,0)-(49,5) | UP (49,0)-(61,5)
#   WEST (32,5)-(37,6) | NORTH (37,5)-(49,6) | EAST (49,5)-(54,6) | SOUTH (54,5)-(66,6)

# UP face: blue solar cell grid
rect(49, 0, 61, 5, SOLAR)
for y in range(0, 5):
    for x in range(49, 61):
        if (x - 49) % 3 == 0 or y % 2 == 0:
            px[x, y] = (*hex_to_rgb(SOLAR_DARK), 255)
px[50, 1] = (*hex_to_rgb(SOLAR_LIGHT), 255)
px[53, 3] = (*hex_to_rgb(SOLAR_LIGHT), 255)
px[56, 1] = (*hex_to_rgb(SOLAR_LIGHT), 255)
px[59, 3] = (*hex_to_rgb(SOLAR_LIGHT), 255)

# DOWN face: dark backing
rect(37, 0, 49, 5, BACKING)
rect(37, 0, 49, 1, "#5A6470")

# side edges: gray frame
rect(32, 5, 37, 6, SILVER_EDGE)
rect(37, 5, 49, 6, SILVER_EDGE)
rect(49, 5, 54, 6, SILVER_EDGE)
rect(54, 5, 66, 6, SILVER_EDGE)

# =============================== MAST ===============================
# Cube (w2 h5 d2), texOffs(80,8) -> atlas u 80..88, v 8..18
# Face regions:
#   DOWN (82,8)-(84,10) | UP (84,8)-(86,10)
#   WEST (80,13)-(82,18) | NORTH (82,13)-(84,18) | EAST (84,13)-(86,18) | SOUTH (86,13)-(88,18)

rect(82, 8, 84, 10, SILVER_DARK)
rect(84, 8, 86, 10, SILVER)
fill_v(80, 13, 82, 18, "#CBD3DB", "#B4BFC9")
fill_v(82, 13, 84, 18, "#AEB9C4", "#98A3B0")
fill_v(84, 13, 86, 18, "#AEB9C4", "#98A3B0")
fill_v(86, 13, 88, 18, "#8E99A5", "#78838F")

# =============================== DISH ===============================
# Cube (w7 h1 d7), texOffs(80,0) -> atlas u 80..108, v 0..8
# Face regions:
#   DOWN (87,0)-(94,7) | UP (94,0)-(101,7)
#   WEST (80,7)-(87,8) | NORTH (87,7)-(94,8) | EAST (94,7)-(101,8) | SOUTH (101,7)-(108,8)

# DOWN face: dark dish back
rect(87, 0, 94, 7, GOLD_DEEP)
for r, c in ((3, "#6E5417"), (2, "#7D5F1A"), (1, "#8A6A1F")):
    for y in range(7):
        for x in range(7):
            if (x - 3) ** 2 + (y - 3) ** 2 <= r * r:
                px[87 + x, y] = (*hex_to_rgb(c), 255)

# UP face: gold concentric rings
rect(94, 0, 101, 7, GOLD)
for r, c in ((3, GOLD_DARK), (2, GOLD), (1, GOLD_DARK)):
    for y in range(7):
        for x in range(7):
            if (x - 3) ** 2 + (y - 3) ** 2 <= r * r:
                px[94 + x, y] = (*hex_to_rgb(c), 255)

# side rim
rect(80, 7, 87, 8, GOLD)
rect(87, 7, 94, 8, GOLD)
rect(94, 7, 101, 8, GOLD)
rect(101, 7, 108, 8, GOLD_DARK)

# =============================== LIGHT ===============================
# Cube (w2 h2 d2), texOffs(110,0) -> atlas u 110..118, v 0..4
# Face regions:
#   DOWN (112,0)-(114,2) | UP (114,0)-(116,2)
#   WEST (110,2)-(112,4) | NORTH (112,2)-(114,4) | EAST (114,2)-(116,4) | SOUTH (116,2)-(118,4)

rect(112, 0, 114, 2, LIGHT_SIDE)
rect(114, 0, 116, 2, LIGHT_CORE)
rect(110, 2, 112, 4, LIGHT_SIDE)
rect(112, 2, 114, 4, LIGHT_SIDE)
rect(114, 2, 116, 4, LIGHT_CORE)
rect(116, 2, 118, 4, LIGHT_SIDE)

# =============================== BACKING ===============================
# Fill unused atlas space with dark navy so nothing renders magenta.
for y in range(H):
    for x in range(W):
        if px[x, y][3] == 0:
            px[x, y] = (*hex_to_rgb(NAVY), 255)

img.save("src/main/resources/assets/alyrioncore/textures/pets/satellite.png")
print("Saved satellite pet texture.")
