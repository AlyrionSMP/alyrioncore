#!/usr/bin/env python3
"""inspect_jar.py — reverse-engineer a mod jar without source.

Wraps the unzip/strings/javap workflow from the minecraft-mod-compat skill in
one tool. Pure stdlib (zipfile); `strings` and constant-pool scans are
implemented in Python; `javap` is used only when a JDK is available.

Commands:
  classes  <jar> [filter]    list .class entries (optionally filtered)
  strings  <jar> [filter]    printable ASCII strings across all class files
  javap    <jar> <class-path> [javap args...]
                             extract one class and disassemble it (-c -p)
  has-id   <jar> <id>        ghost-item check: which classes reference an id
                             (e.g. "rocketnautics:fluid_hose")

Usage:
  python3 inspect_jar.py strings create.jar pipe
  python3 inspect_jar.py classes create.jar FluidPropagator
  python3 inspect_jar.py javap create.jar 'com/simibubi/create/foundation/fluid/FluidPropagator.class'
  python3 inspect_jar.py has-id mod.jar rocketnautics:fluid_hose
"""

import argparse
import os
import re
import shutil
import subprocess
import sys
import tempfile
import zipfile

ASCII_RUN = re.compile(rb'[\x20-\x7e]{4,}')
CLASS = re.compile(r'^[^/]+/[^/]+\.class$')


def read_entries(jar):
    with zipfile.ZipFile(jar) as z:
        return [(i.filename, z.read(i.filename)) for i in z.infolist()]


def cmd_strings(jar, filt):
    hits = 0
    for name, data in read_entries(jar):
        if not name.endswith('.class'):
            continue
        for m in ASCII_RUN.finditer(data):
            s = m.group().decode('ascii')
            if filt and filt.lower() not in s.lower():
                continue
            print('%s: %s' % (name, s))
            hits += 1
    print('--- %d string hits ---' % hits)
    return 0 if hits else 1


def cmd_classes(jar, filt):
    names = [n for n, _ in read_entries(jar) if n.endswith('.class')]
    if filt:
        names = [n for n in names if filt.lower() in n.lower()]
    for n in names:
        print(n)
    print('--- %d classes ---' % len(names))
    return 0


def find_javap():
    """Locate javap: JAVA_HOME, workspace .tools/jre, then PATH."""
    jh = os.environ.get('JAVA_HOME')
    for cand in ([os.path.join(jh, 'bin', 'javap')] if jh else []) + \
                [os.path.expanduser('~/.tools/jre/bin/javap'), 'javap']:
        if shutil.which(cand):
            return cand
    return None


def cmd_javap(jar, class_path, extra):
    javap = find_javap()
    if not javap:
        print('javap not found (no JAVA_HOME / ~/.tools/jre / PATH) — use '
              '`strings` or `has-id` instead', file=sys.stderr)
        return 2
    with zipfile.ZipFile(jar) as z:
        try:
            data = z.read(class_path)
        except KeyError:
            print('class %r not in %s — list with `classes`' % (class_path, jar),
                  file=sys.stderr)
            return 2
    with tempfile.TemporaryDirectory() as tmp:
        out = os.path.join(tmp, class_path)
        os.makedirs(os.path.dirname(out), exist_ok=True)
        with open(out, 'wb') as f:
            f.write(data)
        argv = [javap, '-c', '-p', '-classpath', tmp, class_path[:-6]] + extra
        return subprocess.call(argv)


def cmd_has_id(jar, rid):
    hits = 0
    needle = rid.encode('ascii')
    for name, data in read_entries(jar):
        if not name.endswith('.class'):
            continue
        if needle in data:
            print('%s references %r' % (name, rid))
            hits += 1
    if not hits:
        print('NO class references %r — likely a ghost item / unregistered id; '
              'do not write recipes that output it' % rid)
        return 1
    print('--- %d classes reference %r ---' % (hits, rid))
    return 0


def main():
    ap = argparse.ArgumentParser(description='reverse-engineer a mod jar')
    sub = ap.add_subparsers(dest='cmd', required=True)

    p = sub.add_parser('classes')
    p.add_argument('jar')
    p.add_argument('filter', nargs='?', default='')

    p = sub.add_parser('strings')
    p.add_argument('jar')
    p.add_argument('filter', nargs='?', default='')

    p = sub.add_parser('javap')
    p.add_argument('jar')
    p.add_argument('class_path', help='path inside the jar, e.g. com/foo/Bar.class')
    p.add_argument('extra', nargs='*', default=[])

    p = sub.add_parser('has-id')
    p.add_argument('jar')
    p.add_argument('id', help='resource id, e.g. rocketnautics:fluid_hose')

    args = ap.parse_args()
    if args.cmd == 'classes':
        return cmd_classes(args.jar, args.filter)
    if args.cmd == 'strings':
        return cmd_strings(args.jar, args.filter)
    if args.cmd == 'javap':
        return cmd_javap(args.jar, args.class_path, args.extra)
    if args.cmd == 'has-id':
        return cmd_has_id(args.jar, args.id)
    return 2


if __name__ == '__main__':
    sys.exit(main())
