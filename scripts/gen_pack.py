#!/usr/bin/env python3
"""gen_pack.py — assemble finished textures into a loadable Minecraft Java
resource pack (zip), with correct folder layout, pack.mcmeta, animations, and
an optional atlas file.

Layout produced:
    <name>.zip
      pack.mcmeta
      pack.png                      (generated icon, optional)
      assets/minecraft/textures/block/<file>.png      (from --block DIR)
      assets/minecraft/textures/block/<file>.png.mcmeta  (from *.mcmeta next to
                                                          the PNG or --anim JSON)
      assets/minecraft/textures/item/<file>.png       (from --item DIR)
      assets/minecraft/atlases/blocks.json            (with --atlas)

Animation meta format (same as vanilla):
    {"animation": {"frametime": 2, "interpolate": false, "frames": [0,1,2,1]}}
Frame 0 is the TOP of the PNG strip; all frames must be square and stacked
vertically (16-wide, N*16 tall).

Usage:
  python3 gen_pack.py --name "My Pack" --block textures/block --item textures/item
                      --out dist --pack-format 22 [--atlas] [--icon]
"""

import argparse
import json
import os
import sys
import zipfile

ATLAS_BLOCKS = {
    "sources": [
        {"type": "directory", "source": "block", "prefix": "block/"},
        {"type": "directory", "source": "item", "prefix": "item/"},
    ]
}


def collect(dirpath, ext='.png'):
    out = {}
    if dirpath and os.path.isdir(dirpath):
        for fn in sorted(os.listdir(dirpath)):
            if fn.endswith(ext):
                out[fn] = os.path.join(dirpath, fn)
    return out


def main():
    ap = argparse.ArgumentParser(description='Minecraft Java resource pack builder')
    ap.add_argument('--name', default='Generated Pack')
    ap.add_argument('--block', help='dir of block textures')
    ap.add_argument('--item', help='dir of item textures')
    ap.add_argument('--out', default='dist')
    ap.add_argument('--pack-format', type=int, default=22,
                    help='22 = 1.20.5 (official PBR suffixes), 34 = 1.21.4, 15 = 1.20.1')
    ap.add_argument('--atlas', action='store_true', help='write atlases/blocks.json')
    ap.add_argument('--icon', action='store_true', help='generate a pack.png icon')
    args = ap.parse_args()

    os.makedirs(args.out, exist_ok=True)
    zip_path = os.path.join(args.out, args.name.replace(' ', '_') + '.zip')
    blocks = collect(args.block)
    items = collect(args.item)
    if not blocks and not items:
        print('nothing to pack: give --block and/or --item dirs', file=sys.stderr)
        return 2

    with zipfile.ZipFile(zip_path, 'w', zipfile.ZIP_DEFLATED) as z:
        meta = {'pack': {'pack_format': args.pack_format,
                         'description': args.name}}
        z.writestr('pack.mcmeta', json.dumps(meta, indent=2))
        if args.icon:
            # simple 64x64 icon: tiled 16px stone-ish patch with green top
            try:
                import mcutil as mc
                icon = mc.new_img(64, 64, (108, 108, 108, 255))
                for y in range(16):
                    for x in range(64):
                        icon[y][x] = (122, 168, 96, 255)  # grass strip
                mc.write_png('pack.png.tmp', icon)
                z.write('pack.png.tmp', 'pack.png')
                os.remove('pack.png.tmp')
            except Exception as exc:  # noqa
                print('icon skipped:', exc)
        for fn, path in blocks.items():
            z.write(path, 'assets/minecraft/textures/block/' + fn)
            mcmeta = path + '.mcmeta'
            if os.path.exists(mcmeta):
                z.write(mcmeta, 'assets/minecraft/textures/block/' + fn + '.mcmeta')
        for fn, path in items.items():
            z.write(path, 'assets/minecraft/textures/item/' + fn)
            mcmeta = path + '.mcmeta'
            if os.path.exists(mcmeta):
                z.write(mcmeta, 'assets/minecraft/textures/item/' + fn + '.mcmeta')
        if args.atlas:
            z.writestr('assets/minecraft/atlases/blocks.json',
                       json.dumps(ATLAS_BLOCKS, indent=2))

    print('wrote', zip_path)
    print('  block textures:', len(blocks), ' item textures:', len(items))
    print('  pack_format:', args.pack_format, '(java %s)' % {
        15: '1.20.1', 22: '1.20.5+ (PBR)', 34: '1.21.4'}.get(args.pack_format, '?'))
    return 0


if __name__ == '__main__':
    sys.exit(main())
