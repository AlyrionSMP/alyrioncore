#!/usr/bin/env python3
"""audit_model.py — detector script for vanilla JSON block models.

Implements the hard-won checks from the minecraft-model-design skill so you
never eyeball a model again. Pure stdlib.

Checks:
  1. Z-FIGHTING  — pairs of faces on the same axis/plane with the same facing
     sign and overlapping rectangles in the two free axes flicker in-game.
  2. BOUNDS      — element coordinates must stay within 0..16 (outside = clips
     into neighbor blocks and z-fights their faces).
  3. UV AUDIT    — per-face uv windows must match the face's projected size
     (full 16x16 on a thin face = mush; small offsets read as the wrong
     region). Expected origin follows the vanilla FaceInfo convention.
  4. BLOCKSTATE  — for blockstate files, prints the facing -> y rotation map
     so you can verify `y = rotationDegrees(-y)` against the furnace.

Usage:
  python3 audit_model.py models/block/machine.json
  python3 audit_model.py models/block/            # whole folder
Exit code 0 = zero findings, 1 = findings.
"""

import argparse
import json
import os
import sys

TOL_STRETCH = 2.0    # uv window size mismatch beyond this = stretch bug
TOL_OFFSET = 3.0     # uv origin mismatch beyond this = wrong region

SIGN = {'north': -1, 'south': 1, 'west': -1, 'east': 1, 'down': -1, 'up': 1}
# expected uv origin/size per face direction, from vanilla FaceInfo:
#   north u=16-x v=16-y | south u=x v=16-y | west u=z v=16-y
#   east  u=16-z v=16-y | up   u=x v=z     | down u=x v=16-z
FACES = ('north', 'south', 'west', 'east', 'up', 'down')


def expected_uv(direction, x0, y0, z0, x1, y1, z1):
    """Return (u0, v0, width, height) the face's projection should use."""
    if direction == 'north':
        return (16 - x1, 16 - y1, x1 - x0, y1 - y0)
    if direction == 'south':
        return (x0, 16 - y1, x1 - x0, y1 - y0)
    if direction == 'west':
        return (z0, 16 - y1, z1 - z0, y1 - y0)
    if direction == 'east':
        return (16 - z1, 16 - y1, z1 - z0, y1 - y0)
    if direction == 'up':
        return (x0, z0, x1 - x0, z1 - z0)
    if direction == 'down':
        return (x0, 16 - z1, x1 - x0, z1 - z0)
    raise ValueError(direction)


def face_rect(direction, x0, y0, z0, x1, y1, z1):
    """Face as (min_a, min_b, max_a, max_b) in its two free axes (a,b order)."""
    if direction in ('north', 'south'):        # fixed z; free axes x, y
        z = z0 if direction == 'north' else z1
        return ('z', z, 'x', x0, 'y', y0, x1, y1)
    if direction in ('west', 'east'):          # fixed x; free axes z, y
        x = x0 if direction == 'west' else x1
        return ('x', x, 'z', z0, 'y', y0, z1, y1)
    # up / down: fixed y; free axes x, z
    y = y0 if direction == 'down' else y1
    return ('y', y, 'x', x0, 'z', z0, x1, z1)


def overlaps(a0, a1, b0, b1):
    """Rectangle overlap with a tiny epsilon so touching edges don't count."""
    return max(a0, b0) < min(a1, b1) - 1e-6


def audit_model(model, path, findings, verbose):
    elements = model.get('elements') or []
    if not elements:
        return
    faces = []
    for ei, el in enumerate(elements):
        c = el.get('from', [0, 0, 0])
        d = el.get('to', [16, 16, 16])
        x0, y0, z0 = float(c[0]), float(c[1]), float(c[2])
        x1, y1, z1 = float(d[0]), float(d[1]), float(d[2])
        # bounds check
        for name, lo, hi in (('x', x0, x1), ('y', y0, y1), ('z', z0, z1)):
            if lo < -0.001 or hi > 16.001:
                findings.append('%s: element %d %s range %.2f..%.2f OUTSIDE 0..16'
                                % (path, ei, name, lo, hi))
        el_faces = el.get('faces') or {}
        for direction, spec in el_faces.items():
            if direction not in FACES:
                findings.append('%s: element %d unknown face %r' % (path, ei, direction))
                continue
            rect = face_rect(direction, x0, y0, z0, x1, y1, z1)
            faces.append((ei, direction, rect, spec))
            # UV audit
            uv = spec.get('uv')
            if uv:
                u0, v0, u1, v1 = (float(v) for v in uv)
                exp_u0, exp_v0, ew, eh = expected_uv(direction, x0, y0, z0, x1, y1, z1)
                aw, ah = u1 - u0, v1 - v0
                if abs(aw - ew) > TOL_STRETCH or abs(ah - eh) > TOL_STRETCH:
                    findings.append(
                        '%s: element %d %s UV STRETCH window %.1fx%.1f but face is '
                        '%.1fx%.1f (expected uv ~[%.1f,%.1f,%.1f,%.1f])'
                        % (path, ei, direction, aw, ah, ew, eh, exp_u0, exp_v0,
                           exp_u0 + ew, exp_v0 + eh))
                elif abs(u0 - exp_u0) > TOL_OFFSET or abs(v0 - exp_v0) > TOL_OFFSET:
                    findings.append(
                        '%s: element %d %s UV OFFSET window starts [%.1f,%.1f] but '
                        'projection is [%.1f,%.1f] (wrong region)'
                        % (path, ei, direction, u0, v0, exp_u0, exp_v0))
                elif verbose:
                    print('  ok %s element %d %s uv [%.1f,%.1f,%.1f,%.1f]'
                          % (path, ei, direction, u0, v0, u1, v1))
            else:
                if verbose:
                    print('  note %s element %d %s has no explicit uv (auto-map)'
                          % (path, ei, direction))
    # z-fighting: same fixed axis + same plane + same facing sign + overlap
    # face rect tuple: (axis, plane, a_axis, a0, b_axis, b0, a1, b1)
    for i in range(len(faces)):
        ei, di, ri, si = faces[i]
        for j in range(i + 1, len(faces)):
            ej, dj, rj, sj = faces[j]
            if ri[0] != rj[0] or abs(ri[1] - rj[1]) > 1e-6:
                continue
            if SIGN[di] != SIGN[dj]:
                continue
            if overlaps(ri[3], ri[6], rj[3], rj[6]) and overlaps(ri[5], ri[7], rj[5], rj[7]):
                findings.append(
                    '%s: Z-FIGHT elements %d(%s) and %d(%s) — same %s=%.2f, same '
                    'facing, overlapping faces' % (path, ei, di, ej, dj, ri[0], ri[1]))


def audit_blockstate(bs, path, findings):
    variants = bs.get('variants') or {}
    if not variants:
        return
    print('== %s (blockstate) facing -> y ==' % path)
    for key, spec in variants.items():
        if isinstance(spec, list):
            spec = spec[0]
        print('  %-28s y=%s' % (key or '(default)', spec.get('y', 0)))


def main():
    ap = argparse.ArgumentParser(description='audit vanilla JSON block models')
    ap.add_argument('target', help='model JSON file or directory')
    ap.add_argument('-v', '--verbose', action='store_true')
    args = ap.parse_args()

    files = []
    if os.path.isdir(args.target):
        for root, _, names in os.walk(args.target):
            for n in sorted(names):
                if n.endswith('.json'):
                    files.append(os.path.join(root, n))
    else:
        files = [args.target]

    findings = []
    for path in files:
        try:
            data = json.load(open(path))
        except Exception as exc:
            findings.append('%s: unreadable JSON: %s' % (path, exc))
            continue
        if 'elements' in data:
            audit_model(data, path, findings, args.verbose)
        elif 'variants' in data or 'multipart' in data:
            audit_blockstate(data, path, findings)
        else:
            findings.append('%s: neither a model (elements) nor a blockstate' % path)

    print()
    if findings:
        print('FINDINGS (%d):' % len(findings))
        for f in findings:
            print('  [!] ' + f)
        return 1
    print('no findings — model audit clean')
    return 0


if __name__ == '__main__':
    sys.exit(main())
