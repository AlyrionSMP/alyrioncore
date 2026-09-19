#!/usr/bin/env python3
"""preview.py — see your textures with your own eyes.

The whole quality loop depends on LOOKING at output: generate -> preview ->
critique -> fix. This module renders the review images AND, crucially, turns
any texture into structured text so a model WITHOUT image input can still
critique it precisely:

  * mosaic   — a grid of scaled textures side by side (compare variants)
  * wall     — the canonical 3x3 tiling wall (block textures MUST tile)
  * scaled   — one texture scaled up for inspection
  * cape     — both faces of a 64x32 cape atlas, read OUT of the atlas
  * analyze  — THE no-image critique report: ASCII luma/alpha maps, palette,
               light check, outline check, banding, flat patches, tiling
  * ascii    — just the ASCII map (luma / alpha / hex)
  * palette  — print a color report (hex, count, coverage)
  * info     — size / color count / alpha stats

Usage:
  python3 preview.py mosaic --files a.png b.png c.png --scale 8 --out m.png
  python3 preview.py wall --file stone.png --scale 8 --out wall.png
  python3 preview.py scaled --file stone.png --scale 10 --out big.png
  python3 preview.py cape --file capes/pride.png --scale 16 --out faces.png
  python3 preview.py analyze --file stone.png          # full text report
  python3 preview.py ascii --file stone.png --mode luma
  python3 preview.py palette --file stone.png
  python3 preview.py info --file stone.png
"""

import argparse
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import mcutil as mc


def cape_layers(img):
    """Split a 64x32 cape atlas into the two faces worth reviewing, as 10x16 grids.

    Reading them back out of the atlas (rather than out of a design grid) is the
    whole point: a review image then cannot show something the shipped texture
    does not contain.

      * worn  — cols 12..21, the face strangers see; CapeCosmeticRenderer
                .drawStoreIcon blits this exact region for the store icon
      * inner — cols 1..10, already the mirrored copy, so it reads the way the
                cape looks to someone standing in front of the wearer
    """
    w, h = len(img[0]), len(img)
    if (w, h) != (64, 32):
        raise SystemExit('cape: expected a 64x32 cape atlas, got %dx%d' % (w, h))
    worn = [[img[y][12 + x] for x in range(10)] for y in range(1, 17)]
    inner = [[img[y][1 + x] for x in range(10)] for y in range(1, 17)]
    return worn, inner


def cape_sheet(worn, inner, scale=16, gap=3, bg=(18, 18, 22, 255)):
    """The two faces side by side at nearest-neighbour scale, gutter between them."""
    cw, chh = 10 * scale, 16 * scale
    g = gap * scale
    img = mc.new_img(2 * cw + g, chh, bg)
    for panel, ox in ((worn, 0), (inner, cw + g)):
        for y in range(16):
            for x in range(10):
                px = panel[y][x]
                for sy in range(scale):
                    for sx in range(scale):
                        mc.set_px(img, ox + x * scale + sx, y * scale + sy, px)
    return img


def main():
    ap = argparse.ArgumentParser(description='texture review images + text analysis')
    sub = ap.add_subparsers(dest='cmd', required=True)

    p = sub.add_parser('mosaic')
    p.add_argument('--files', nargs='+', required=True)
    p.add_argument('--scale', type=int, default=8)
    p.add_argument('--out', default='mosaic.png')

    p = sub.add_parser('wall')
    p.add_argument('--file', required=True)
    p.add_argument('--scale', type=int, default=8)
    p.add_argument('--out', default='wall.png')

    p = sub.add_parser('scaled')
    p.add_argument('--file', required=True)
    p.add_argument('--scale', type=int, default=10)
    p.add_argument('--out', default='scaled.png')

    p = sub.add_parser('cape')
    p.add_argument('--file', required=True, help='64x32 cape atlas')
    p.add_argument('--scale', type=int, default=16)
    p.add_argument('--gap', type=int, default=3, help='gutter between faces, in atlas pixels')
    p.add_argument('--out', default='cape_faces.png')

    p = sub.add_parser('analyze')
    p.add_argument('--file', required=True)
    p.add_argument('--name', default='')

    p = sub.add_parser('ascii')
    p.add_argument('--file', required=True)
    p.add_argument('--mode', choices=('luma', 'alpha', 'hex'), default='luma')

    p = sub.add_parser('palette')
    p.add_argument('--file', required=True)

    p = sub.add_parser('info')
    p.add_argument('--file', required=True)

    args = ap.parse_args()

    if args.cmd == 'mosaic':
        imgs = [mc.read_png(f) for f in args.files]
        mc.write_mosaic(args.out, imgs, scale=args.scale)
        print('wrote', args.out, '(%d textures)' % len(imgs))
    elif args.cmd == 'wall':
        img = mc.read_png(args.file)
        mc.write_wall(args.out, img, scale=args.scale)
        print('wrote', args.out, '(3x3 tiling wall)')
    elif args.cmd == 'scaled':
        img = mc.read_png(args.file)
        mc.write_scaled(args.out, img, scale=args.scale)
        print('wrote', args.out)
    elif args.cmd == 'cape':
        worn, inner = cape_layers(mc.read_png(args.file))
        mc.write_png(args.out, cape_sheet(worn, inner, args.scale, args.gap))
        print('wrote %s (worn face | inner face, x%d)' % (args.out, args.scale))
    elif args.cmd == 'analyze':
        print(mc.analyze(mc.read_png(args.file), args.name))
    elif args.cmd == 'ascii':
        img = mc.read_png(args.file)
        if args.mode == 'luma':
            print(mc.ascii_luma(img))
        elif args.mode == 'alpha':
            print(mc.ascii_alpha(img))
        else:
            print(mc.labeled_grid(img))
    elif args.cmd == 'palette':
        print(mc.palette_report(mc.read_png(args.file)))
    elif args.cmd == 'info':
        img = mc.read_png(args.file)
        w, h = len(img[0]), len(img)
        opaque = sum(1 for row in img for px in row if px[3] > 0)
        alpha_lo = sum(1 for row in img for px in row if 0 < px[3] < 255)
        colors = {mc.rgb2hex(px) for row in img for px in row if px[3] > 0}
        print('size: %dx%d' % (w, h))
        print('opaque px: %d (%.0f%%)' % (opaque, 100.0 * opaque / (w * h)))
        print('semi-transparent px: %d' % alpha_lo)
        print('distinct colors: %d' % len(colors))
    return 0


if __name__ == '__main__':
    sys.exit(main())
