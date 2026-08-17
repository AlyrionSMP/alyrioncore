#!/usr/bin/env python3
"""gen_pbr.py — derive PBR maps (normal / specular / height / emissive) from an
albedo texture, in both labPBR (shader packs) and 1.20.5+ official styles.

File conventions:
  * <name>_n.png  — normal map (RGB = tangent-space normal; flat = 128,128,255)
  * <name>_s.png  — specular map (R = specular intensity, G = smoothness,
                     B = metalness) — same meaning in labPBR and official PBR
  * <name>_h.png  — height map (grayscale, for parallax / depth)
  * <name>_e.png  — emissive map (R = emissive intensity, G/B = 0)

Rules of thumb for vanilla-looking PBR:
  * rough materials (stone, sand): low smoothness, low spec, gentle normals
  * smooth/shiny (metal, glass, polished): high smoothness + spec, stronger
    normal variation; metals get high metalness (blue channel)
  * emissive (glowstone, redstone ore, magma): bright _e mask

Usage:
  python3 gen_pbr.py <albedo.png> [--out DIR] [--strength 1.0]
                     [--spec 48] [--smooth 96] [--metal 0]
                     [--emissive mask.png | --emissive-color #hex [--tolerance 40]]
                     [--height heightmap.png]
"""

import argparse
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import mcutil as mc


def main():
    ap = argparse.ArgumentParser(description='PBR map generator for Minecraft textures')
    ap.add_argument('albedo')
    ap.add_argument('--out', default='.')
    ap.add_argument('--strength', type=float, default=1.0, help='normal strength')
    ap.add_argument('--spec', type=int, default=48, help='specular intensity (0-255)')
    ap.add_argument('--smooth', type=int, default=96, help='smoothness (0-255)')
    ap.add_argument('--metal', type=int, default=0, help='metalness (0-255)')
    ap.add_argument('--emissive', help='emissive mask PNG (white = emissive)')
    ap.add_argument('--emissive-color', help='emissive by albedo color, e.g. #ffcc66')
    ap.add_argument('--tolerance', type=int, default=48)
    ap.add_argument('--height', help='explicit heightmap PNG (overrides luminance)')
    args = ap.parse_args()

    os.makedirs(args.out, exist_ok=True)
    albedo = mc.read_png(args.albedo)
    base = os.path.splitext(os.path.basename(args.albedo))[0]

    if args.height:
        height = mc.build_height(mc.read_png(args.height))
    else:
        height = mc.build_height(albedo)

    n = mc.normal_from_height(height, args.strength)
    mc.write_png(os.path.join(args.out, base + '_n.png'), n)

    s = mc.specular_map(albedo, spec=args.spec, smooth=args.smooth, metal=args.metal)
    mc.write_png(os.path.join(args.out, base + '_s.png'), s)

    # height map: quantize luminance to 8-bit grayscale (opaque)
    h_out = [[(int(round(v * 255)), int(round(v * 255)), int(round(v * 255)), 255)
              for v in row] for row in height]
    mc.write_png(os.path.join(args.out, base + '_h.png'), h_out)

    mask = None
    if args.emissive:
        em = mc.read_png(args.emissive)
        mask = [[em[y][x][0] > 127 for x in range(len(em[0]))] for y in range(len(em))]
    elif args.emissive_color:
        target = mc.hex2rgb(args.emissive_color)
        mask = [[False] * len(albedo[0]) for _ in range(len(albedo))]
        for y in range(len(albedo)):
            for x in range(len(albedo[0])):
                r, g, b, a = albedo[y][x]
                if a == 0:
                    continue
                if (abs(r - target[0]) <= args.tolerance and
                        abs(g - target[1]) <= args.tolerance and
                        abs(b - target[2]) <= args.tolerance):
                    mask[y][x] = True
    if mask is not None:
        e = mc.emissive_map(albedo, mask, intensity=255)
        mc.write_png(os.path.join(args.out, base + '_e.png'), e)
        lit = sum(1 for row in mask for v in row if v)
        print('emissive mask: %d/%d px lit' % (lit, len(mask) * len(mask[0])))
    else:
        print('no emissive mask — skipped _e.png (use --emissive or --emissive-color)')

    print('wrote %s_n.png, %s_s.png, %s_h.png' % (base, base, base))
    print('normal strength=%.1f spec=%d smooth=%d metal=%d' %
          (args.strength, args.spec, args.smooth, args.metal))
    return 0


if __name__ == '__main__':
    sys.exit(main())
