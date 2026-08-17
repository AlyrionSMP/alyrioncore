#!/usr/bin/env python3
"""fetch_reference.py — fetch a vanilla Minecraft texture for study, no fumbling.

The design skill's workflow starts with "fetch the vanilla reference and study
it". Doing that by hand is fiddly: wiki pages block plain downloads, the real
files live on the wiki as versioned names ("Iron Pickaxe JE3 BE2.png"), and
many are 4-bit indexed PNGs. This script does all of it:

  1. finds the right file on the Minecraft wiki (searches, prefers the newest
     Java Edition version of the requested texture),
  2. downloads and decodes it (any bit depth / color type — no ImageMagick),
  3. downsamples wiki renders (e.g. 160x160) to the 16x16 master grid,
  4. writes the 16x16 PNG and prints the full study pack: palette, ASCII
     luma/alpha maps, and the exact labeled hex grid.

Usage:
  python3 fetch_reference.py "Iron Pickaxe" --out ref_iron_pickaxe.png
  python3 fetch_reference.py "Stone" --out ref_stone.png --prefer JE
  python3 fetch_reference.py "grass_block_side" --out ref_grass.png
  python3 fetch_reference.py list "Iron"          # just list candidate files
"""

import argparse
import json
import os
import re
import sys
import urllib.request
import urllib.parse

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import mcutil as mc

WIKIS = [
    'https://minecraft.wiki',
    'https://minecraft.fandom.com',
]
UA = ('Mozilla/5.0 (X11; Linux x86_64; MinecraftTextureToolset/1.0) '
      'AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36')
HEADERS = {
    'User-Agent': UA,
    'Accept': 'text/html,application/xhtml+xml,application/json,*/*;q=0.8',
    'Accept-Language': 'en-US,en;q=0.9',
}


def api_get(wiki, params):
    params = dict(params, format='json')
    url = wiki + '/api.php?' + urllib.parse.urlencode(params)
    req = urllib.request.Request(url, headers=HEADERS)
    with urllib.request.urlopen(req, timeout=30) as r:
        return json.loads(r.read().decode('utf-8'))


def je_version(name):
    """Extract (JE number, BE number) from a wiki filename, else (0, 0)."""
    m = re.search(r'JE(\d+)', name)
    b = re.search(r'BE(\d+)', name)
    return (int(m.group(1)) if m else 0, int(b.group(1)) if b else 0)


def find_files(query, prefer='JE'):
    """Return candidate filenames across wikis, newest JE version first.

    Wiki APIs return filenames with underscores ("Iron_Pickaxe_JE3_BE2.png");
    normalize to spaces before matching the query.
    """
    seen = set()
    out = []
    for wiki in WIKIS:
        try:
            data = api_get(wiki, {'action': 'query', 'list': 'allimages',
                                  'aiprefix': query, 'ailimit': 100})
        except Exception as exc:
            print('  (wiki %s unavailable: %s)' % (wiki, exc), file=sys.stderr)
            continue
        for item in data.get('query', {}).get('allimages', []):
            name = item['name']
            if name in seen:
                continue
            seen.add(name)
            norm = name.replace('_', ' ').lower()
            if not norm.startswith(query.lower()):
                continue
            if name.lower().endswith(('.gif', '.svg')):
                continue
            out.append((name, wiki))
    # newest JE first; prefer names that contain the query + JE
    out.sort(key=lambda t: (-je_version(t[0])[0], t[0]))
    return out


def image_url(wiki, filename):
    data = api_get(wiki, {'action': 'query', 'titles': 'File:' + filename,
                          'prop': 'imageinfo', 'iiprop': 'url|size'})
    pages = data.get('query', {}).get('pages', {})
    for page in pages.values():
        ii = page.get('imageinfo') or []
        if ii:
            return ii[0]['url']
    return None


def download(url, dest):
    req = urllib.request.Request(url, headers=HEADERS)
    with urllib.request.urlopen(req, timeout=60) as r:
        data = r.read()
    with open(dest, 'wb') as f:
        f.write(data)
    return dest


def downsample_to_16(img):
    """Reduce any image to 16x16 by nearest sampling at cell centers."""
    h, w = len(img), len(img[0])
    if w == 16 and h == 16:
        return img
    sx = w / 16.0
    sy = h / 16.0
    out = [[(0, 0, 0, 0)] * 16 for _ in range(16)]
    for y in range(16):
        for x in range(16):
            out[y][x] = img[min(h - 1, int((y + 0.5) * sy))][min(w - 1, int((x + 0.5) * sx))]
    return out


# Primary source: the community assets mirror serves the TRUE 16x16 PNG masters
# from the vanilla jar (raw.githubusercontent.com — no bot protection, no WebP
# content negotiation). Wiki renders (160x160, sometimes WebP) are the fallback.
ASSETS_REPO = 'https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets'
ASSETS_VERSIONS = ['1.21.9', '1.21.4', '1.20.6', '1.20.5']
ASSETS_CATEGORIES = ['item', 'block', 'entity']


def slugify(query):
    return re.sub(r'[^A-Za-z0-9]+', '_', query.strip().lower()).strip('_')


def fetch_from_mirror(slug):
    """Try the assets mirror; return (img, source_description) or None."""
    for version in ASSETS_VERSIONS:
        for cat in ASSETS_CATEGORIES:
            url = '%s/%s/assets/minecraft/textures/%s/%s.png' % (
                ASSETS_REPO, version, cat, slug)
            try:
                req = urllib.request.Request(url, headers={'User-Agent': UA})
                with urllib.request.urlopen(req, timeout=30) as r:
                    data = r.read()
                if not data.startswith(b'\x89PNG'):
                    continue
                tmp = '_mirror_%s.png' % slug
                with open(tmp, 'wb') as f:
                    f.write(data)
                img = mc.read_png(tmp)
                os.remove(tmp)
                return (img, 'assets mirror %s (%s/%s)' % (version, cat, slug))
            except Exception:
                continue
    return None


def maybe_convert_webp(path):
    """If the wiki served WebP, convert with ImageMagick when available."""
    with open(path, 'rb') as f:
        head = f.read(12)
    if not head.startswith(b'RIFF') or b'WEBP' not in head:
        return path
    import shutil
    if shutil.which('convert') or shutil.which('magick'):
        out = path + '.png'
        conv = shutil.which('convert') or shutil.which('magick')
        if os.path.basename(conv) == 'magick':
            os.system('%s %s %s' % (conv, path, out))
        else:
            os.system('%s %s %s' % (conv, path, out))
        if os.path.exists(out):
            return out
    raise ValueError('wiki served WebP and no ImageMagick found to convert it — '
                     'use the assets mirror or download manually')


def main():
    ap = argparse.ArgumentParser(description='fetch and study a vanilla Minecraft texture')
    ap.add_argument('query', help='texture name or id, e.g. "Iron Pickaxe" or "stone"')
    ap.add_argument('--out', default='', help='output PNG path (default: ref_<query>.png)')
    ap.add_argument('--prefer', choices=('JE', 'BE'), default='JE')
    ap.add_argument('--list', action='store_true', help='list candidate wiki files only')
    ap.add_argument('--file', help='use this exact wiki filename (from --list)')
    ap.add_argument('--wiki', action='store_true', help='force the wiki source')
    args = ap.parse_args()

    out = args.out or ('ref_' + slugify(args.query) + '.png')

    if args.list:
        candidates = find_files(args.query, args.prefer)
        if not candidates:
            print('no wiki files found for %r' % args.query, file=sys.stderr)
            return 2
        for name, wiki in candidates[:25]:
            print('%-44s %s' % (name, wiki))
        return 0

    img = None
    source = None
    raw_path = None
    if not args.wiki:
        img, source = fetch_from_mirror(slugify(args.query))
        if img:
            print('source: %s (true 16x16 master)' % source)
        else:
            print('assets mirror miss for %r — falling back to the wiki' % args.query,
                  file=sys.stderr)

    if img is None:
        candidates = find_files(args.query, args.prefer)
        if not candidates:
            print('no reference found for %r — try a shorter query' % args.query,
                  file=sys.stderr)
            return 2
        if args.file:
            match = next((t for t in candidates if t[0] == args.file), None)
            if not match:
                print('file %r not in candidates' % args.file, file=sys.stderr)
                return 2
            filename, wiki = match
        else:
            filename, wiki = candidates[0]
        print('source: wiki file %s (from %s)' % (filename, wiki))
        url = image_url(wiki, filename)
        if not url:
            print('could not resolve image URL', file=sys.stderr)
            return 2
        raw_path = os.path.join(os.path.dirname(os.path.abspath(out)),
                                '_raw_' + slugify(filename))
        download(url, raw_path)
        raw_path = maybe_convert_webp(raw_path)
        img = mc.read_png(raw_path)

    img16 = downsample_to_16(img)
    print('master %dx%d' % (len(img16[0]), len(img16)))
    mc.write_png(out, img16)
    if raw_path and os.path.exists(raw_path):
        os.remove(raw_path)
    print('wrote', out)
    print()
    print(mc.analyze(img16, os.path.basename(out)))
    print()
    print('exact hex grid:')
    print(mc.labeled_grid(img16))
    return 0


if __name__ == '__main__':
    sys.exit(main())
