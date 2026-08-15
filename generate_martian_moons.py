#!/usr/bin/env python3
"""
Generate Phobos and Deimos universe_planets JSON definitions and high-res celestial textures
for Create: Cosmonautics / Rocketnautics integration in Alyrion SMP.
"""

import json
import os
import math
import random
from PIL import Image, ImageDraw

MOD_DIR = "/Users/lea/alyrioncore"
ALYRION_DATA = os.path.join(MOD_DIR, "src/main/resources/data/alyrioncore/universe_planets")
ROCKET_DATA = os.path.join(MOD_DIR, "src/main/resources/data/rocketnautics/universe_planets")
ALYRION_TEXTURES = os.path.join(MOD_DIR, "src/main/resources/assets/alyrioncore/textures/planet")
ROCKET_TEXTURES = os.path.join(MOD_DIR, "src/main/resources/assets/rocketnautics/textures/planet")

def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)
    print(f"Wrote: {path}")

# --- 1. CELESTIAL BODY DEFINITIONS ---

def make_phobos_json():
    return {
        "parent": "mars",
        "name": "phobos",
        "radius": 11000.0,
        "acceleration_at_surface": 0.0057,
        "position": {
            "type": "circular_orbit_period",
            "orbit_axis": {
                "x": 0.0,
                "y": 1.0,
                "z": 0.0
            },
            "period_seconds": 382.3
        },
        "rotation": {
            "type": "tidal_lock",
            "rotations_per_orbit": 1.0
        },
        "planet_extras": {
            "is_star": False,
            "has_clouds": False,
            "light_source_name": "sol"
        },
        "planet_texture": {
            "type": "resloc",
            "texture": "alyrioncore:textures/planet/phobos.png"
        },
        "priority": 1000,
        "disabled": False
    }

def make_deimos_json():
    return {
        "parent": "mars",
        "name": "deimos",
        "radius": 6200.0,
        "acceleration_at_surface": 0.003,
        "position": {
            "type": "circular_orbit_period",
            "orbit_axis": {
                "x": 0.0,
                "y": 1.0,
                "z": 0.0
            },
            "period_seconds": 1516.1
        },
        "rotation": {
            "type": "tidal_lock",
            "rotations_per_orbit": 1.0
        },
        "planet_extras": {
            "is_star": False,
            "has_clouds": False,
            "light_source_name": "sol"
        },
        "planet_texture": {
            "type": "resloc",
            "texture": "alyrioncore:textures/planet/deimos.png"
        },
        "priority": 1000,
        "disabled": False
    }

# --- 2. TEXTURES ---

def generate_phobos_texture(size=64):
    # 64x64 equirectangular/spherical projection texture of Phobos (Stickney crater, grooves, carbonaceous chondrite)
    random.seed(42)
    img = Image.new("RGBA", (size, size), (0, 0, 0, 255))
    
    # Base dark carbonaceous asteroid palette
    # Low albedo ~0.07 with subtle reddish-brown dust
    base_r, base_g, base_b = 62, 58, 54
    
    pixels = []
    for y in range(size):
        row = []
        for x in range(size):
            # Perlin-like noise simulation using sinusoids
            n1 = math.sin(x * 0.25) * math.cos(y * 0.25) * 12
            n2 = math.sin(x * 0.55 + y * 0.45) * 8
            n3 = math.cos(x * 0.12 - y * 0.18) * 15
            noise = n1 + n2 + n3 + random.uniform(-4, 4)
            
            r = int(max(20, min(120, base_r + noise)))
            g = int(max(18, min(115, base_g + noise * 0.95)))
            b = int(max(16, min(110, base_b + noise * 0.90)))
            row.append((r, g, b, 255))
        pixels.append(row)
        
    # Large impact crater: Stickney (centered around x=22, y=30, radius=14)
    stickney_cx, stickney_cy, stickney_r = 22, 30, 14
    for y in range(size):
        for x in range(size):
            dx = x - stickney_cx
            dy = y - stickney_cy
            dist = math.sqrt(dx*dx + dy*dy)
            if dist < stickney_r:
                factor = dist / stickney_r
                if factor < 0.7:
                    # Deep floor shadow
                    darken = (1.0 - factor) * 35
                    r, g, b, a = pixels[y][x]
                    pixels[y][x] = (int(max(15, r - darken)), int(max(15, g - darken)), int(max(15, b - darken)), 255)
                else:
                    # Raised rim highlight
                    brighten = (1.0 - abs(factor - 0.85) / 0.15) * 45
                    r, g, b, a = pixels[y][x]
                    pixels[y][x] = (int(min(220, r + brighten)), int(min(215, g + brighten)), int(min(210, b + brighten)), 255)

    # Stickney radiating grooves (linear fracture chains)
    for angle in [0.2, 0.45, 0.8, -0.3, -0.6, 1.2, 1.6]:
        for step in range(5, 32):
            gx = int(stickney_cx + math.cos(angle) * step)
            gy = int(stickney_cy + math.sin(angle) * step)
            if 0 <= gx < size and 0 <= gy < size:
                r, g, b, a = pixels[gy][gx]
                pixels[gy][gx] = (int(max(10, r - 22)), int(max(10, g - 22)), int(max(10, b - 22)), 255)
                if gy+1 < size:
                    pr, pg, pb, _ = pixels[gy+1][gx]
                    pixels[gy+1][gx] = (int(min(200, pr + 18)), int(min(195, pg + 18)), int(min(190, pb + 18)), 255)

    # Other distinct craters: Hall, Limtoc
    small_craters = [(45, 18, 6), (52, 42, 5), (12, 50, 4), (36, 12, 4)]
    for cx, cy, cr in small_craters:
        for y in range(size):
            for x in range(size):
                dx = x - cx
                dy = y - cy
                dist = math.sqrt(dx*dx + dy*dy)
                if dist < cr:
                    if dist < cr * 0.6:
                        r, g, b, _ = pixels[y][x]
                        pixels[y][x] = (int(max(15, r - 25)), int(max(15, g - 25)), int(max(15, b - 25)), 255)
                    else:
                        r, g, b, _ = pixels[y][x]
                        pixels[y][x] = (int(min(220, r + 30)), int(min(215, g + 30)), int(min(210, b + 30)), 255)

    for y in range(size):
        for x in range(size):
            img.putpixel((x, y), pixels[y][x])
    return img

def generate_deimos_texture(size=64):
    # 64x64 texture of Deimos (smooth thick regolith blanket, Swift & Voltaire craters, reddish-grey albedo)
    random.seed(101)
    img = Image.new("RGBA", (size, size), (0, 0, 0, 255))
    
    # Base lighter reddish-grey palette with smooth powdery regolith mantling
    base_r, base_g, base_b = 85, 78, 72
    
    pixels = []
    for y in range(size):
        row = []
        for x in range(size):
            n1 = math.sin(x * 0.18) * math.cos(y * 0.18) * 14
            n2 = math.sin(x * 0.4 - y * 0.3) * 7
            n3 = math.cos(x * 0.08 + y * 0.14) * 10
            noise = n1 + n2 + n3 + random.uniform(-3, 3)
            
            r = int(max(30, min(145, base_r + noise + 6))) # slightly warmer red
            g = int(max(25, min(138, base_g + noise)))
            b = int(max(22, min(130, base_b + noise - 4)))
            row.append((r, g, b, 255))
        pixels.append(row)
        
    # Smooth regolith dust flow streams
    for y in range(size):
        for x in range(size):
            wave = math.sin(y * 0.15 + x * 0.1) * 8
            r, g, b, _ = pixels[y][x]
            pixels[y][x] = (int(max(20, min(240, r + wave))), int(max(20, min(230, g + wave))), int(max(20, min(220, b + wave))), 255)

    # Craters: Swift (x=24, y=26, r=8) & Voltaire (x=46, y=36, r=7) with smooth filled floors and bright ejecta
    deimos_craters = [(24, 26, 8), (46, 36, 7), (14, 44, 4), (40, 14, 4)]
    for cx, cy, cr in deimos_craters:
        for y in range(size):
            for x in range(size):
                dx = x - cx
                dy = y - cy
                dist = math.sqrt(dx*dx + dy*dy)
                if dist < cr:
                    if dist < cr * 0.5:
                        r, g, b, _ = pixels[y][x]
                        pixels[y][x] = (int(max(25, r - 18)), int(max(25, g - 18)), int(max(25, b - 18)), 255)
                    else:
                        r, g, b, _ = pixels[y][x]
                        pixels[y][x] = (int(min(240, r + 35)), int(min(235, g + 32)), int(min(230, b + 30)), 255)

    for y in range(size):
        for x in range(size):
            img.putpixel((x, y), pixels[y][x])
    return img

def main():
    print("Generating universe_planets for Phobos and Deimos...")
    # Write to alyrioncore
    write_json(os.path.join(ALYRION_DATA, "phobos.json"), make_phobos_json())
    write_json(os.path.join(ALYRION_DATA, "deimos.json"), make_deimos_json())
    
    # Write to rocketnautics
    write_json(os.path.join(ROCKET_DATA, "phobos.json"), make_phobos_json())
    write_json(os.path.join(ROCKET_DATA, "deimos.json"), make_deimos_json())

    print("Generating celestial textures for Phobos and Deimos...")
    phobos_img = generate_phobos_texture(64)
    deimos_img = generate_deimos_texture(64)

    os.makedirs(ALYRION_TEXTURES, exist_ok=True)
    os.makedirs(ROCKET_TEXTURES, exist_ok=True)

    phobos_img.save(os.path.join(ALYRION_TEXTURES, "phobos.png"), "PNG")
    phobos_img.save(os.path.join(ROCKET_TEXTURES, "phobos.png"), "PNG")
    print(f"Saved: {os.path.join(ALYRION_TEXTURES, 'phobos.png')}")

    deimos_img.save(os.path.join(ALYRION_TEXTURES, "deimos.png"), "PNG")
    deimos_img.save(os.path.join(ROCKET_TEXTURES, "deimos.png"), "PNG")
    print(f"Saved: {os.path.join(ALYRION_TEXTURES, 'deimos.png')}")

    print("Successfully generated all Phobos & Deimos configurations and textures!")

if __name__ == "__main__":
    main()
