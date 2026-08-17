#!/usr/bin/env python3
"""generate_create_recipes.py — convert AlyrionCore's crafting-table recipes to
Create machine recipes where that makes sense, keeping crafting-table fallbacks
for when Create is not installed.

Decisions (matching Create's own conventions):
  * Complex machines        -> create:mechanical_crafting (Create crafts these
                               in the Mechanical Crafter; Rocketnautics' own
                               thrusters are mechanical_crafting too)
  * Block compression       -> create:compacting (basin + press; Create
                               compacts 9 snow blocks -> ice, so 9 ingots ->
                               block is the same idiom)
  * Chemistry / reactions   -> create:mixing (basin + mixer: gunpowder from
                               sulfur + coal, cryo-packed ice from dry ice)
  * Grinding                -> create:milling (hematite ore -> red dye)
  * Kept as crafting-table: tools/weapons, armor (space helmet), fluid hose,
                               unpacking (block -> 9), masonry 2x2 (basalt,
                               coarse regolith, regolith from sand), torches,
                               spyglass, water bucket from ice, furnace and
                               stonecutting recipes.

Every converted recipe is gated on `create` being loaded; the originals are
kept as `<name>_from_crafting.json` fallbacks gated on `create` NOT being
loaded, so the mod still works standalone. Rocketnautics compat parts get no
fallback (Rocketnautics itself requires Create).

Run:  python3 generate_create_recipes.py
"""

import json
import os

ROOT = os.path.dirname(os.path.abspath(__file__))
DATA_DIR = os.path.join(ROOT, "src", "main", "resources", "data", "alyrioncore")
RECIPE_DIR = os.path.join(DATA_DIR, "recipe")


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)
    print("wrote", os.path.relpath(path, ROOT))


def cond_mod(modid):
    return {"type": "neoforge:mod_loaded", "modid": modid}


def cond_not(cond):
    return {"type": "neoforge:not", "value": cond}


def cond_create():
    return [cond_mod("create")]


def cond_not_create():
    return [cond_not(cond_mod("create"))]


# ---------------------------------------------------------------------------
# Create recipe emitters
# ---------------------------------------------------------------------------
def mechanical_crafting(name, pattern, key, result_id, count=1, conditions=None):
    r = {
        "type": "create:mechanical_crafting",
        "accept_mirrored": False,
        "category": "misc",
        "key": key,
        "pattern": pattern,
        "result": {"count": count, "id": result_id},
    }
    if conditions:
        r["neoforge:conditions"] = conditions
    write_json(os.path.join(RECIPE_DIR, f"{name}.json"), r)


def compacting(name, item, amount, results, conditions=None):
    r = {
        "type": "create:compacting",
        "ingredients": [{"item": item} for _ in range(amount)],
        "results": results,
    }
    if conditions:
        r["neoforge:conditions"] = conditions
    write_json(os.path.join(RECIPE_DIR, f"{name}.json"), r)


def mixing(name, item_ingredients, fluid_ingredients, results, conditions=None):
    r = {
        "type": "create:mixing",
        "ingredients": [{"item": i} for i in item_ingredients]
        + [{"type": "neoforge:single", "amount": a, "fluid": f}
           for a, f in fluid_ingredients],
        "results": results,
    }
    if conditions:
        r["neoforge:conditions"] = conditions
    write_json(os.path.join(RECIPE_DIR, f"{name}.json"), r)


def milling(name, item, results, processing_time=50, conditions=None):
    r = {
        "type": "create:milling",
        "ingredients": [{"item": item}],
        "processing_time": processing_time,
        "results": results,
    }
    if conditions:
        r["neoforge:conditions"] = conditions
    write_json(os.path.join(RECIPE_DIR, f"{name}.json"), r)


# ---------------------------------------------------------------------------
# Crafting-table fallback emitter (the original recipe + not-create condition)
# ---------------------------------------------------------------------------
def shaped_fallback(name, pattern, key, result_id, count=1, category="misc"):
    r = {
        "type": "minecraft:crafting_shaped",
        "category": category,
        "key": key,
        "pattern": pattern,
        "result": {"count": count, "id": result_id},
        "neoforge:conditions": cond_not_create(),
    }
    write_json(os.path.join(RECIPE_DIR, f"{name}_from_crafting.json"), r)


def shapeless_fallback(name, ingredients, result_id, count=1, category="misc"):
    r = {
        "type": "minecraft:crafting_shapeless",
        "category": category,
        "ingredients": [{"item": i} for i in ingredients],
        "result": {"count": count, "id": result_id},
        "neoforge:conditions": cond_not_create(),
    }
    write_json(os.path.join(RECIPE_DIR, f"{name}_from_crafting.json"), r)


def main():
    # ------------------------------------------------------------------
    # 1. Complex machines -> Mechanical Crafter
    # ------------------------------------------------------------------
    mechanical_crafting(
        "sleeping_pod",
        ["GGG", "IBI", "III"],
        {"G": {"item": "minecraft:glass"},
         "I": {"item": "minecraft:iron_ingot"},
         "B": {"tag": "minecraft:beds"}},
        "alyrioncore:sleeping_pod",
        conditions=cond_create(),
    )
    shaped_fallback(
        "sleeping_pod",
        ["GGG", "IBI", "III"],
        {"G": {"item": "minecraft:glass"},
         "I": {"item": "minecraft:iron_ingot"},
         "B": {"tag": "minecraft:beds"}},
        "alyrioncore:sleeping_pod",
        category="misc",
    )

    mechanical_crafting(
        "airlock",
        ["MIM", "MGM", "MRM"],
        {"M": {"item": "alyrioncore:meteoric_iron_ingot"},
         "I": {"item": "minecraft:iron_ingot"},
         "G": {"item": "minecraft:glass_pane"},
         "R": {"item": "minecraft:redstone"}},
        "alyrioncore:airlock",
        conditions=cond_create(),
    )
    shaped_fallback(
        "airlock",
        ["MIM", "MGM", "MRM"],
        {"M": {"item": "alyrioncore:meteoric_iron_ingot"},
         "I": {"item": "minecraft:iron_ingot"},
         "G": {"item": "minecraft:glass_pane"},
         "R": {"item": "minecraft:redstone"}},
        "alyrioncore:airlock",
        category="misc",
    )

    mechanical_crafting(
        "oxygen_generator",
        ["MGM", "GMG", "MRM"],
        {"M": {"item": "alyrioncore:meteoric_iron_ingot"},
         "G": {"item": "minecraft:glass_pane"},
         "R": {"item": "minecraft:redstone"}},
        "alyrioncore:oxygen_generator",
        conditions=cond_create(),
    )
    shaped_fallback(
        "oxygen_generator",
        ["MGM", "GMG", "MRM"],
        {"M": {"item": "alyrioncore:meteoric_iron_ingot"},
         "G": {"item": "minecraft:glass_pane"},
         "R": {"item": "minecraft:redstone"}},
        "alyrioncore:oxygen_generator",
        category="misc",
    )

    # Rocketnautics engine parts — mechanical crafting (no fallback: the space
    # mod itself requires Create, so a crafting fallback would be dead code).
    rn = [cond_mod("rocketnautics"), cond_mod("create")]
    mechanical_crafting(
        "engine_nozzle",
        ["III", "ICI", "ICI"],
        {"I": {"item": "minecraft:iron_ingot"},
         "C": {"item": "minecraft:copper_ingot"}},
        "rocketnautics:engine_nozzle",
        conditions=rn,
    )
    mechanical_crafting(
        "engine_pipes",
        ["ICI", "ICI", "ICI"],
        {"I": {"item": "minecraft:iron_ingot"},
         "C": {"item": "minecraft:copper_ingot"}},
        "rocketnautics:engine_pipes",
        conditions=rn,
    )
    mechanical_crafting(
        "thruster_mount",
        ["III", "O O", "III"],
        {"I": {"item": "minecraft:iron_ingot"},
         "O": {"item": "minecraft:obsidian"}},
        "rocketnautics:thruster_mount",
        conditions=rn,
    )
    mechanical_crafting(
        "hose_anchor",
        ["III", "ICI", "III"],
        {"I": {"item": "minecraft:iron_ingot"},
         "C": {"item": "minecraft:copper_ingot"}},
        "rocketnautics:hose_anchor",
        conditions=rn,
    )

    # ------------------------------------------------------------------
    # 2. Block compression -> Basin + Press (9 -> 1, 4 -> 1)
    # ------------------------------------------------------------------
    comp = [
        ("meteoric_iron_block", "alyrioncore:meteoric_iron_ingot", 9,
         [{"id": "alyrioncore:meteoric_iron_block"}]),
        ("raw_meteoric_iron_block", "alyrioncore:raw_meteoric_iron", 9,
         [{"id": "alyrioncore:raw_meteoric_iron_block"}]),
        ("olivine_block", "alyrioncore:olivine_gem", 9,
         [{"id": "alyrioncore:olivine_block"}]),
        ("sulfur_block", "alyrioncore:sulfur_dust", 9,
         [{"id": "alyrioncore:sulfur_block"}]),
        ("raw_copper_block_from_martian_copper", "alyrioncore:raw_martian_copper", 9,
         [{"id": "minecraft:raw_copper_block"}]),
        ("dry_ice_block_from_shards", "alyrioncore:dry_ice_shard", 4,
         [{"id": "alyrioncore:dry_ice_block"}]),
        ("packed_ice_from_martian_ice", "alyrioncore:martian_ice", 4,
         [{"id": "minecraft:packed_ice"}]),
        ("iron_ingot_from_hematite_reduction", "alyrioncore:hematite_nodule", 4,
         [{"id": "minecraft:raw_iron"}]),
    ]
    for name, item, amount, results in comp:
        compacting(name, item, amount, results, conditions=cond_create())
    # crafting fallbacks (the original 2x2 / 3x3 patterns)
    shaped_fallback("meteoric_iron_block", ["###", "###", "###"],
                    {"#": {"item": "alyrioncore:meteoric_iron_ingot"}},
                    "alyrioncore:meteoric_iron_block", category="building")
    shaped_fallback("raw_meteoric_iron_block", ["###", "###", "###"],
                    {"#": {"item": "alyrioncore:raw_meteoric_iron"}},
                    "alyrioncore:raw_meteoric_iron_block", category="building")
    shaped_fallback("olivine_block", ["###", "###", "###"],
                    {"#": {"item": "alyrioncore:olivine_gem"}},
                    "alyrioncore:olivine_block", category="building")
    shaped_fallback("sulfur_block", ["###", "###", "###"],
                    {"#": {"item": "alyrioncore:sulfur_dust"}},
                    "alyrioncore:sulfur_block", category="building")
    shaped_fallback("raw_copper_block_from_martian_copper", ["###", "###", "###"],
                    {"#": {"item": "alyrioncore:raw_martian_copper"}},
                    "minecraft:raw_copper_block", category="building")
    shaped_fallback("dry_ice_block_from_shards", ["##", "##"],
                    {"#": {"item": "alyrioncore:dry_ice_shard"}},
                    "alyrioncore:dry_ice_block", category="building")
    shaped_fallback("packed_ice_from_martian_ice", ["##", "##"],
                    {"#": {"item": "alyrioncore:martian_ice"}},
                    "minecraft:packed_ice", category="building")
    shaped_fallback("iron_ingot_from_hematite_reduction", ["##", "##"],
                    {"#": {"item": "alyrioncore:hematite_nodule"}},
                    "minecraft:raw_iron", category="misc")

    # ------------------------------------------------------------------
    # 3. Chemistry -> Basin + Mixer
    # ------------------------------------------------------------------
    mixing("gunpowder_from_sulfur_and_coal",
           ["alyrioncore:sulfur_dust", "minecraft:coal", "minecraft:sugar"],
           [], [{"count": 2, "id": "minecraft:gunpowder"}],
           conditions=cond_create())
    mixing("gunpowder_from_sulfur_and_charcoal",
           ["alyrioncore:sulfur_dust", "minecraft:charcoal", "minecraft:sugar"],
           [], [{"count": 2, "id": "minecraft:gunpowder"}],
           conditions=cond_create())
    mixing("gunpowder_from_sulfur_and_bonemeal",
           ["alyrioncore:sulfur_dust", "minecraft:coal", "minecraft:bone_meal"],
           [], [{"count": 2, "id": "minecraft:gunpowder"}],
           conditions=cond_create())
    mixing("packed_ice_from_dry_ice_and_water",
           ["alyrioncore:dry_ice_shard"] * 4,
           [(1000, "minecraft:water")],
           [{"count": 2, "id": "minecraft:packed_ice"}],
           conditions=cond_create())

    shapeless_fallback("gunpowder_from_sulfur_and_coal",
                       ["alyrioncore:sulfur_dust", "minecraft:coal", "minecraft:sugar"],
                       "minecraft:gunpowder", count=2)
    shapeless_fallback("gunpowder_from_sulfur_and_charcoal",
                       ["alyrioncore:sulfur_dust", "minecraft:charcoal", "minecraft:sugar"],
                       "minecraft:gunpowder", count=2)
    shapeless_fallback("gunpowder_from_sulfur_and_bonemeal",
                       ["alyrioncore:sulfur_dust", "minecraft:coal", "minecraft:bone_meal"],
                       "minecraft:gunpowder", count=2)
    shapeless_fallback("packed_ice_from_dry_ice_and_water",
                       ["alyrioncore:dry_ice_shard"] * 4 + ["minecraft:water_bucket"],
                       "minecraft:packed_ice", count=2, category="building")

    # ------------------------------------------------------------------
    # 4. Grinding -> Millstone
    # ------------------------------------------------------------------
    milling("red_dye_from_hematite", "alyrioncore:hematite_nodule",
            [{"count": 2, "id": "minecraft:red_dye"}],
            conditions=cond_create())
    shapeless_fallback("red_dye_from_hematite",
                       ["alyrioncore:hematite_nodule"],
                       "minecraft:red_dye", count=2)

    print("Create recipe conversion generated.")


if __name__ == "__main__":
    main()
