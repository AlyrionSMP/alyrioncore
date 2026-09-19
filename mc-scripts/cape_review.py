#!/usr/bin/env python3
"""cape_review.py — build the review page for a cape, out of the cape texture itself.

Why this exists: a review page that mixes design sketches or abandoned
experiments in with the current cape is worse than no review page, because the
thing on screen is not the thing on disk. So the hero panels and the atlas strip
here are sliced OUT of the shipped 64x32 atlas by preview.cape_layers(), and
anything else has to be declared as an experiment, where it lands under a banner
that says, in the page, that it is not shipped.

Usage:
  python3 cape_review.py \
      --asset src/main/resources/assets/alyrioncore/textures/capes/pride.png \
      --out run/pride_preview.html \
      --experiment "flag's own stripe rhythm (NOT shipped):run/pride_flag_even_experiment.png"
"""

import argparse
import base64
import html
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import mcutil as mc
import preview as pv


def data_uri(path):
    with open(path, 'rb') as f:
        return 'data:image/png;base64,' + base64.b64encode(f.read()).decode('ascii')


def fig(src, caption, note=''):
    extra = '<div class="note">%s</div>' % html.escape(note) if note else ''
    return ('<figure><img src="%s" alt="%s">'
            '<figcaption>%s</figcaption>%s</figure>' % (src, html.escape(caption),
                                                        html.escape(caption), extra))


def main():
    ap = argparse.ArgumentParser(description='cape review page, generated from the cape texture')
    ap.add_argument('--asset', required=True, help='the shipped 64x32 cape atlas')
    ap.add_argument('--name', default='', help='cape name for the title (default: the file name)')
    ap.add_argument('--out', required=True, help='html file to write')
    ap.add_argument('--scale', type=int, default=16, help='scale for the face panels')
    ap.add_argument('--experiment', action='append', default=[],
                    help='"caption:png" for a render that is deliberately NOT the shipped cape')
    args = ap.parse_args()

    name = args.name or os.path.splitext(os.path.basename(args.asset))[0]
    atlas = mc.read_png(args.asset)
    worn, inner = pv.cape_layers(atlas)          # raises if the atlas is not 64x32

    faces_path = os.path.join(os.path.dirname(args.out), name + '_faces.png')
    atlas_path = os.path.join(os.path.dirname(args.out), name + '_atlas.png')
    mc.write_png(faces_path, pv.cape_sheet(worn, inner, args.scale))
    mc.write_scaled(atlas_path, atlas, scale=8)

    parts = [
        '<!doctype html><html><head><meta charset="utf-8">',
        '<title>%s cape</title>' % html.escape(name),
        '<style>',
        'body{background:#121216;color:#e8e8ee;font:14px/1.5 system-ui,sans-serif;margin:0;padding:24px}',
        'h1{font-size:20px;margin:0 0 4px}h2{font-size:15px;margin:28px 0 10px;color:#9fb4ff}',
        '.sub{color:#8b8b98;margin:0 0 8px}',
        'figure{display:inline-block;margin:0 16px 16px 0;vertical-align:top}',
        'img{image-rendering:pixelated;border:1px solid #2c2c34;display:block}',
        'figcaption{color:#a8a8b8;font-size:12px;padding-top:6px;max-width:340px}',
        '.note{color:#6f6f7c;font-size:11px;max-width:340px}',
        '.banner{background:#3a2216;border:1px solid #7a4a22;color:#ffcf9e;padding:10px 12px;'
        'margin:24px 0 12px;border-radius:4px;max-width:760px}',
        '.exp img{width:320px}',
        '</style></head><body>',
        '<h1>%s cape</h1>' % html.escape(name),
        '<p class="sub">every panel below is rendered from <code>%s</code> '
        '(64x32 atlas, %d bytes) unless a section says otherwise</p>'
        % (html.escape(args.asset), os.path.getsize(args.asset)),
        '<h2>the cape as shipped</h2>',
        '<figure class="faces"><img src="%s" alt="worn and inner face"><figcaption>left: worn face, '
        'cols 12&ndash;21 &mdash; what strangers see, and the exact region the store icon blits. '
        'right: inner face, cols 1&ndash;10 &mdash; the mirrored copy, so it reads the way the cape looks '
        'to someone standing in front of the wearer.</figcaption></figure>' % data_uri(faces_path),
        '<h2>what changed on disk</h2>',
        fig(data_uri(atlas_path), 'the whole 64x32 atlas at x8',
            'transparent pixels are the unused parts of the atlas; the top row carries the hems.'),
    ]

    if args.experiment:
        parts.append('<div class="banner"><b>Not shipped.</b> The panels below are experiments kept only '
                     'to explain decisions. They are not in the asset and nothing wears them.</div>')
        parts.append('<div class="exp">')
        for spec in args.experiment:
            caption, _, path = spec.partition(':')
            parts.append(fig(data_uri(path), caption))
        parts.append('</div>')

    parts.append('</body></html>')
    with open(args.out, 'w') as f:
        f.write('\n'.join(parts))
    print('wrote', args.out, '(faces + atlas sliced from the asset)')


if __name__ == '__main__':
    sys.exit(main())
