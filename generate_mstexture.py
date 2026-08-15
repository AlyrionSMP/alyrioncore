import struct
import zlib

def make_rgba_png(width, height, rgba_data):
    """Generates an RGBA PNG byte stream using only Python's standard library."""
    png = bytearray(b"\x89PNG\r\n\x1a\n")

    # IHDR Chunk (RGBA 8-bit)
    ihdr_data = struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)
    ihdr_crc = struct.pack(">I", zlib.crc32(b"IHDR" + ihdr_data) & 0xFFFFFFFF)
    png += struct.pack(">I", len(ihdr_data)) + b"IHDR" + ihdr_data + ihdr_crc

    # IDAT Chunk
    raw_scanlines = bytearray()
    row_bytes = width * 4
    for y in range(height):
        raw_scanlines.append(0)  # Filter byte: None
        raw_scanlines.extend(rgba_data[y * row_bytes : (y + 1) * row_bytes])

    compressed = zlib.compress(bytes(raw_scanlines), level=9)
    idat_crc = struct.pack(">I", zlib.crc32(b"IDAT" + compressed) & 0xFFFFFFFF)
    png += struct.pack(">I", len(compressed)) + b"IDAT" + compressed + idat_crc

    # IEND Chunk
    png += struct.pack(">I", 0) + b"IEND" + struct.pack(">I", zlib.crc32(b"IEND") & 0xFFFFFFFF)
    return bytes(png)


def scale_rgba(src_data, factor=16):
    """Upscales pixel art crisply (nearest neighbor)."""
    w, h = 16 * factor, 16 * factor
    out = bytearray(w * h * 4)
    for y in range(h):
        for x in range(w):
            si = ((y // factor) * 16 + (x // factor)) * 4
            di = (y * w + x) * 4
            out[di : di + 4] = src_data[si : si + 4]
    return w, h, out


def generate():
    # Palette (R, G, B, A)
    P = {
        ".": (0, 0, 0, 0),          # Transparent

        # Handle: Carbonized meteorite rod + alloy reinforcement
        "w": (118, 102, 114, 255),  # Handle light edge
        "W": (72, 60, 70, 255),     # Handle main body
        "s": (40, 32, 40, 255),     # Handle shadow

        # Joint / Reinforcement Ring
        "j": (95, 80, 110, 255),    # Joint bracket light
        "J": (45, 36, 55, 255),     # Joint bracket shadow

        # Pickaxe Head: Meteoric Nickel-Iron
        "0": (24, 20, 30, 255),     # Fusion crust (deepest outline)
        "1": (42, 38, 54, 255),     # Dark meteor stone shadow
        "2": (66, 75, 96, 255),     # Main meteoric iron body
        "3": (104, 118, 144, 255),  # Midtone steel
        "4": (160, 178, 205, 255),  # Polished nickel sheen
        "5": (225, 238, 255, 255),  # Sharp tip / specular highlight

        # Celestial Core (Starfall crystal embedded in the metal)
        "c": (50, 220, 200, 255),   # Glowing cyan crystal
        "C": (160, 255, 245, 255),  # Crystal glint
    }

    # Authentic 16x16 Minecraft Pickaxe Grid
    grid = [
        "................",  # 0
        ".......0544430..",  # 1  (Top curved head)
        ".....0543222340.",  # 2
        "....054210.02340",  # 3
        "...05320....0340",  # 4  (Left & right prongs)
        "..0420.jJ...024.",  # 5  (Mounting collar)
        "..030..Ws...030.",  # 6  (Left prong tip)
        "..00..Ws........",  # 7
        ".....Ws.........",  # 8  (Handle diagonal)
        "....Ws..........",  # 9
        "...Ws...........",  # 10
        "..Ws............",  # 11
        ".Ws.............",  # 12
        ".ws.............",  # 13 (Pommel grip)
        ".0..............",  # 14 (Pommel cap)
        "................",  # 15
    ]

    # Add a glowing cosmic star inclusion into the pickaxe head
    # Replace (x=10, y=2) and (x=11, y=2) with celestial star crystal
    row2 = list(grid[2])
    row2[9] = "c"
    row2[10] = "C"
    grid[2] = "".join(row2)

    # Build raw RGBA bytes
    rgba = bytearray()
    for row in grid:
        for char in row:
            rgba.extend(P[char])

    # Save 16x16 game texture
    with open("meteoric_iron_pickaxe.png", "wb") as f:
        f.write(make_rgba_png(16, 16, rgba))
    print("✓ Saved 16x16 texture: meteoric_iron_pickaxe.png")

    # Save 256x256 preview
    pw, ph, pdata = scale_rgba(rgba, factor=16)
    with open("meteoric_iron_pickaxe_preview.png", "wb") as f:
        f.write(make_rgba_png(pw, ph, pdata))
    print("✓ Saved 256x256 preview: meteoric_iron_pickaxe_preview.png")

if __name__ == "__main__":
    generate()
