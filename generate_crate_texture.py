#!/usr/bin/env python3
"""Generate the AlyrionCore 'crate' item texture (16x16).

A wooden shipping crate with iron corner brackets, matching vanilla's
palette language (oak planks + iron). Pure stdlib PNG writer, same style
as the repo's other generate_* scripts.
"""
import struct
import zlib

W = H = 16

# (r, g, b, a)
TRANSPARENT = (0, 0, 0, 0)
# Oak plank ramp (vanilla-ish)
PLANK_DARK = (86, 60, 34, 255)
PLANK_MID = (108, 76, 43, 255)
PLANK_LIGHT = (130, 94, 56, 255)
PLANK_HIGHLIGHT = (152, 112, 68, 255)
# Iron bracket ramp
IRON_DARK = (54, 54, 58, 255)
IRON_MID = (80, 80, 86, 255)
IRON_LIGHT = (122, 122, 130, 255)

OUTLINE = (43, 29, 16, 255)


def base_texture():
    px = [[TRANSPARENT] * W for _ in range(H)]
    for y in range(1, 15):
        for x in range(1, 15):
            # Horizontal planks with subtle per-row shading and grain noise
            if y % 4 == 0:
                c = PLANK_DARK
            else:
                c = PLANK_MID if (x * 7 + y * 3) % 5 == 0 else (
                    PLANK_HIGHLIGHT if (x + y) % 7 == 0 else PLANK_LIGHT)
            px[y][x] = c
    return px


def add_frame(px):
    """Dark outline around the whole crate."""
    for i in range(16):
        px[0][i] = OUTLINE
        px[15][i] = OUTLINE
        px[i][0] = OUTLINE
        px[i][15] = OUTLINE


def add_diagonal_brace(px):
    """X-brace across the front, like vanilla cargo crates."""
    for i in range(2, 14):
        px[i][i] = PLANK_DARK if i % 3 else PLANK_MID
        px[i][15 - i] = PLANK_DARK if i % 3 else PLANK_MID
        # slight highlight offset for depth
        px[min(i + 1, 14)][i] = PLANK_MID


def add_corner_brackets(px):
    """Iron L-brackets in all four corners."""
    def bracket(cx, cy, dx, dy):
        for o in range(4):
            px[cy + dy * 0][cx + dx * o] = IRON_MID
            px[cy + dy * o][cx + dx * 0] = IRON_MID
        px[cy][cx] = IRON_LIGHT
        px[cy + dy * 1][cx + dx * 1] = IRON_DARK

    bracket(1, 1, 1, 1)
    bracket(14, 1, -1, 1)
    bracket(1, 14, 1, -1)
    bracket(14, 14, -1, -1)


def write_png(path, pixels):
    raw = b""
    for row in pixels:
        raw += b"\x00" + b"".join(struct.pack("4B", *p) for p in row)

    def chunk(tag, data):
        c = tag + data
        return struct.pack(">I", len(data)) + c + struct.pack(">I", zlib.crc32(c))

    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", W, H, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(raw, 9))
    png += chunk(b"IEND", b"")

    with open(path, "wb") as f:
        f.write(png)


def main():
    out = "src/main/resources/assets/alyrioncore/textures/item/crate.png"
    px = base_texture()
    add_frame(px)
    add_diagonal_brace(px)
    add_corner_brackets(px)
    write_png(out, px)
    print(f"wrote {out}")


if __name__ == "__main__":
    main()
