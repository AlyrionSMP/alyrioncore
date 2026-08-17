#!/usr/bin/env python3
"""analyze_cosmonautics_crafting.py — find cosmonautics (Rocketnautics) items
that have no crafting/processing recipe, and note alternative acquisition
(loot tables) for each.

Pure stdlib. Extracts the mod jar's data+assets into research_cosmonautics/
(next to this script) if missing, then diffs lang items against recipe outputs.
Run:  python3 analyze_cosmonautics_crafting.py [--jar /path/to/cosmonautics.jar]
"""
import argparse
import json
import os
import zipfile
from collections import defaultdict

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.join(HERE, 'research_cosmonautics')
MODID = 'rocketnautics'

parser = argparse.ArgumentParser()
parser.add_argument('--jar', default=os.path.expanduser(
    '~/.minecraftx/instances/Alyrion-indev-8.0.0/mods/cosmonautics-26.08.307.jar'))
args = parser.parse_args()

# ---------------------------------------------------------------------------
# 0. Extract the jar (assets + data) if not already extracted
# ---------------------------------------------------------------------------
if not os.path.isdir(os.path.join(ROOT, 'assets', MODID)):
    os.makedirs(ROOT, exist_ok=True)
    with zipfile.ZipFile(args.jar) as z:
        for name in z.namelist():
            if name.startswith(('assets/' + MODID + '/', 'data/')):
                z.extract(name, ROOT)
    print(f'extracted {args.jar} -> {ROOT}')

# ---------------------------------------------------------------------------
# 1. Collect ALL user-facing items from the lang file
# ---------------------------------------------------------------------------
lang = {}
with open(os.path.join(ROOT, 'assets', MODID, 'lang', 'en_us.json'), encoding='utf-8') as f:
    lang = json.load(f)

lang_items = {}       # id -> display name
lang_blocks = {}      # block id -> display name
for k, v in lang.items():
    if k.startswith('item.' + MODID + '.'):
        rest = k[len('item.' + MODID + '.'):]
        if '.' not in rest:  # skip tooltip/summary/funny Ponder keys
            lang_items[rest] = v
    elif k.startswith('block.' + MODID + '.'):
        rest = k[len('block.' + MODID + '.'):]
        if '.' not in rest:
            lang_blocks[rest] = v

# Block items are obtainable items too
all_items = {}
for iid, name in lang_items.items():
    all_items[MODID + ':' + iid] = name
for bid, name in lang_blocks.items():
    all_items[MODID + ':' + bid] = name

print(f'lang items: {len(lang_items)}, blocks: {len(lang_blocks)}, total ids: {len(all_items)}')

# Cross-check: item models (catch items without lang entries)
model_items = set()
models_dir = os.path.join(ROOT, 'assets', MODID, 'models', 'item')
if os.path.isdir(models_dir):
    for fn in os.listdir(models_dir):
        if fn.endswith('.json'):
            model_items.add(MODID + ':' + fn[:-5])
no_lang_models = model_items - set(all_items.keys())
print(f'model files: {len(model_items)}; with models but NO lang entry: {sorted(no_lang_models)}')

# ---------------------------------------------------------------------------
# 2. Collect ALL recipe results (vanilla + Create recipe types)
# ---------------------------------------------------------------------------
recipe_dir = os.path.join(ROOT, 'data', MODID, 'recipe')
recipe_results = defaultdict(list)   # item id -> list of recipe files

def extract_result(result, recipe_path):
    """result may be a dict {id, count} / {item:{id,count}} / string / list."""
    if isinstance(result, str):
        recipe_results[result].append(recipe_path)
        return
    if isinstance(result, list):
        for entry in result:
            extract_result(entry, recipe_path)
        return
    if isinstance(result, dict):
        # vanilla: {"id": "...", "count": n}
        if 'id' in result and isinstance(result['id'], str) and ':' in result['id']:
            recipe_results[result['id']].append(recipe_path)
        # create: {"item": "mod:id"} or {"item": {"id": "mod:id", "count": n}}
        if 'item' in result:
            it = result['item']
            if isinstance(it, str):
                recipe_results[it].append(recipe_path)
            elif isinstance(it, dict) and 'id' in it:
                recipe_results[it['id']].append(recipe_path)

count = 0
recipe_types = defaultdict(int)   # recipe type -> file count
recipe_types_with_output = set()
for dirpath, _dirs, files in os.walk(recipe_dir):
    for fn in files:
        if not fn.endswith('.json'):
            continue
        path = os.path.join(dirpath, fn)
        rel = os.path.relpath(path, ROOT)
        try:
            with open(path, encoding='utf-8') as f:
                data = json.load(f)
        except Exception as e:
            print(f'  !! parse error {rel}: {e}')
            continue
        count += 1
        rtype = data.get('type', '?')
        recipe_types[rtype] += 1
        if 'result' in data or 'results' in data or 'output' in data:
            recipe_types_with_output.add(rtype)
        if 'result' in data:
            extract_result(data['result'], rel)
        if 'results' in data:
            extract_result(data['results'], rel)
        if 'output' in data:  # create mechanical_crafting etc.
            extract_result(data['output'], rel)
print(f'recipe files parsed: {count}, distinct recipe outputs: {len(recipe_results)}')
print('recipe types:')
for t, n in sorted(recipe_types.items()):
    mark = 'OUTPUT' if t in recipe_types_with_output else 'no-output'
    print(f'  {n:3d}  {t:60s} [{mark}]')

# ---------------------------------------------------------------------------
# 3. Diff: items with NO recipe at all
# ---------------------------------------------------------------------------
craftable = set(recipe_results.keys())
not_craftable = {iid: name for iid, name in all_items.items() if iid not in craftable}

# ---------------------------------------------------------------------------
# 4. Loot tables (alternative acquisition)
# ---------------------------------------------------------------------------
loot_dir = os.path.join(ROOT, 'data', MODID, 'loot_table')
looted_items = set()
if os.path.isdir(loot_dir):
    for dirpath, _dirs, files in os.walk(loot_dir):
        for fn in files:
            if not fn.endswith('.json'):
                continue
            path = os.path.join(dirpath, fn)
            rel = os.path.relpath(path, ROOT)
            try:
                with open(path, encoding='utf-8') as f:
                    data = json.load(f)
            except Exception:
                continue
            # walk everything looking for "name": "modid:item" / "id": "modid:item"
            def walk(node):
                if isinstance(node, dict):
                    for k, v in node.items():
                        if k in ('name', 'id') and isinstance(v, str) and v.startswith(MODID + ':'):
                            looted_items.add(v)
                        walk(v)
                elif isinstance(node, list):
                    for v in node:
                        walk(v)
            walk(data)

print(f'\nloot tables reference {len(looted_items)} distinct rocketnautics item ids')

# ---------------------------------------------------------------------------
# 5. Report
# ---------------------------------------------------------------------------
print('\n' + '=' * 78)
print(f'ITEMS WITH NO CRAFTING/PROCESSING RECIPE ({len(not_craftable)}):')
print('=' * 78)
for iid in sorted(not_craftable):
    name = not_craftable[iid]
    via_loot = 'loot' if iid in looted_items else 'NO-LOOT'
    print(f'  {iid:50s} {name[:44]:44s} [{via_loot}]')

# sanity: which recipe outputs are NOT in the item list (hidden/removed/other mod)
foreign = {iid for iid in craftable if iid not in all_items}
print(f'\nrecipe outputs outside the lang item list (cross-mod/hidden): {len(foreign)}')
for iid in sorted(foreign):
    print(f'  {iid}  (recipes: {recipe_results[iid][:3]})')
