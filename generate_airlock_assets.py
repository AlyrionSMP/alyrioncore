#!/usr/bin/env python3
"""Generate the new airlock model textures (16x16 block atlas + item icon)."""
import os
from PIL import Image, ImageDraw

TEX_DIR = "/Users/lea/alyrioncore/src/main/resources/assets/alyrioncore/textures"

def save(img, rel):
    path = os.path.join(TEX_DIR, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)
    print("Wrote", path)

# --- Frame: titanium bulkhead with hazard stripe + rivets ---
def frame_texture():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # base steel
    d.rectangle([0, 0, 15, 15], fill=(58, 62, 70, 255))
    # inset plating
    d.rectangle([1, 1, 14, 14], fill=(78, 83, 94, 255))
    # hazard stripe band across top
    for x in range(0, 16, 2):
        d.rectangle([x, 0, x + 1, 2], fill=(214, 180, 30, 255))
        d.rectangle([x + 1, 0, x + 2, 2], fill=(30, 30, 32, 255))
    # bottom hazard stripe
    for x in range(0, 16, 2):
        d.rectangle([x, 14, x + 1, 16], fill=(30, 30, 32, 255))
        d.rectangle([x + 1, 14, x + 2, 16], fill=(214, 180, 30, 255))
    # rivets
    for rx, ry in [(2, 4), (13, 4), (2, 11), (13, 11)]:
        d.ellipse([rx - 1, ry - 1, rx + 1, ry + 1], fill=(150, 156, 166, 255))
    # vertical seam lines
    d.line([(0, 2), (15, 2)], fill=(35, 37, 42, 255))
    d.line([(0, 13), (15, 13)], fill=(35, 37, 42, 255))
    save(img, "block/airlock_frame.png")

# --- Leaf bottom: armored door with hazard stripe + locking bars + handle ---
def leaf_bottom_texture():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # door face (this is mapped to a 12x12 area via UVs; design fills the whole 16x16)
    d.rectangle([0, 0, 15, 15], fill=(72, 77, 87, 255))
    d.rectangle([1, 1, 14, 14], fill=(86, 92, 102, 255))
    # hazard stripe at the bottom of the door
    for x in range(1, 15, 2):
        d.rectangle([x, 12, x + 1, 15], fill=(214, 180, 30, 255))
        d.rectangle([x + 1, 12, x + 2, 15], fill=(30, 30, 32, 255))
    # two horizontal locking bars
    d.rectangle([2, 6, 13, 8], fill=(44, 47, 53, 255))
    d.rectangle([3, 7, 12, 8], fill=(150, 156, 166, 255))
    d.rectangle([2, 9, 13, 11], fill=(44, 47, 53, 255))
    d.rectangle([3, 10, 12, 11], fill=(150, 156, 166, 255))
    # handle
    d.rectangle([6, 2, 9, 5], fill=(44, 47, 53, 255))
    d.rectangle([6, 3, 9, 5], fill=(158, 164, 174, 255))
    # rivets
    for rx, ry in [(1, 1), (14, 1), (1, 14), (14, 14)]:
        d.ellipse([rx - 1, ry - 1, rx + 1, ry + 1], fill=(120, 126, 136, 255))
    save(img, "block/airlock_leaf_bottom.png")

# --- Leaf top: armored door with viewport window frame ---
def leaf_top_texture():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 15], fill=(72, 77, 87, 255))
    d.rectangle([1, 1, 14, 14], fill=(86, 92, 102, 255))
    # hazard stripe at top of door
    for x in range(1, 15, 2):
        d.rectangle([x, 1, x + 1, 3], fill=(214, 180, 30, 255))
        d.rectangle([x + 1, 1, x + 2, 3], fill=(30, 30, 32, 255))
    # viewport frame
    d.rectangle([3, 5, 12, 12], fill=(40, 43, 49, 255))
    d.rectangle([4, 6, 11, 11], fill=(24, 26, 30, 255))
    # small "PRESSURE SEAL" indicator strip
    d.rectangle([3, 13, 12, 14], fill=(44, 47, 53, 255))
    for rx, ry in [(1, 1), (14, 1), (1, 14), (14, 14)]:
        d.ellipse([rx - 1, ry - 1, rx + 1, ry + 1], fill=(120, 126, 136, 255))
    save(img, "block/airlock_leaf_top.png")

# --- Window glass (translucent cyan with glare) ---
def window_texture():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 15], fill=(90, 190, 220, 180))
    d.rectangle([1, 1, 14, 14], fill=(110, 205, 235, 170))
    # glare diagonal
    d.line([(2, 2), (6, 6)], fill=(230, 250, 255, 210), width=1)
    d.line([(2, 3), (5, 6)], fill=(230, 250, 255, 180), width=1)
    d.rectangle([0, 0, 15, 15], outline=(150, 220, 240, 230), width=1)
    save(img, "block/airlock_window.png")

# --- Status LEDs ---
def led_texture(name, rgb):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([4, 4, 11, 11], fill=(20, 22, 26, 255))
    d.rectangle([5, 5, 10, 10], fill=rgb)
    d.rectangle([6, 6, 7, 9], fill=(min(rgb[0] + 90, 255), min(rgb[1] + 90, 255), min(rgb[2] + 90, 255), 255))
    save(img, f"block/airlock_led_{name}.png")

# --- Item icon: mini airlock door ---
def item_texture():
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # frame
    d.rectangle([5, 3, 26, 28], fill=(58, 62, 70, 255))
    d.rectangle([7, 5, 24, 26], fill=(86, 92, 102, 255))
    # viewport (top)
    d.rectangle([10, 7, 21, 16], fill=(40, 43, 49, 255))
    d.rectangle([11, 8, 20, 15], fill=(110, 205, 235, 200))
    # glare
    d.line([(12, 9), (16, 9)], fill=(230, 250, 255, 255))
    # hazard stripe band
    for x in range(7, 24, 3):
        d.rectangle([x, 20, x + 1, 24], fill=(214, 180, 30, 255))
        d.rectangle([x + 1, 20, x + 2, 24], fill=(30, 30, 32, 255))
    # handle
    d.rectangle([13, 25, 18, 26], fill=(150, 156, 166, 255))
    # status light
    d.ellipse([6, 4, 8, 6], fill=(40, 220, 90, 255))
    save(img, "item/airlock.png")

frame_texture()
leaf_bottom_texture()
leaf_top_texture()
window_texture()
led_texture("green", (40, 220, 90, 255))
led_texture("red", (225, 50, 45, 255))
item_texture()
print("Done.")
