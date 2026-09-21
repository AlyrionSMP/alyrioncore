package xyz.alyrion.alyrioncore.store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central registry of every item pack sold in the Alyrion store.
 *
 * Mirrors {@code CosmeticsRegistry}: register a pack here and it automatically
 * shows up in the store's "Item Packs" tab and becomes purchasable through the
 * generic networking stack. Item ids are resolved lazily on first access so
 * this stays loadable before registries are frozen.
 */
public final class ItemPacksRegistry {

    private static final Map<String, ItemPackDefinition> PACKS = new LinkedHashMap<>();

    private ItemPacksRegistry() {
    }

    private static void register(ItemPackDefinition pack) {
        if (PACKS.containsKey(pack.id())) {
            throw new IllegalStateException("Duplicate item pack id: " + pack.id());
        }
        PACKS.put(pack.id(), pack);
    }

    /** Register all item packs. Ids are stable command keys: never rename an id after release. */
    private static void init() {
        // --- Train Starter Kit: everything needed for one basic Create train ---
        // Contents verified against the Create 6 recipes/ponder text shipped in the
        // pack (create-1.21.1-6.0.10.jar): what a train actually consumes is a Track,
        // a Train Station (the only place trains can be assembled or taken apart),
        // Train Casings (bogeys), Super Glue (carriage bodies), a chest or barrel of
        // fuel and Train Controls. The Steam Engine is NOT a train part — it is the
        // stationary boiler generator (fluid tanks + heat + a shaft for kinetic
        // output), so it and its fluid tanks belong to a boiler kit, not this one.
        register(new ItemPackDefinition(
                "train_starter_kit",
                "Train Starter Kit",
                "A working basic Create train: tracks, bogeys, controls, glue, fuel & seats.",
                50,
                List.of(
                        // In Create 6 a Train Track is a sequenced assembly (sleepers -> two
                        // nugget deploying steps -> pressing). Handing over finished tracks
                        // skips the whole line; 192 blocks is a 3-chunk stretch of track.
                        ItemPackDefinition.PackEntry.of("create:track", 192),
                        // Assembly AND disassembly happen at a station only, so a basic
                        // line wants a station at each end.
                        ItemPackDefinition.PackEntry.of("create:track_station", 2),
                        // Create 6 has no bogey item (nothing crafts create:small_bogey /
                        // create:large_bogey): using a Train Casing on a track creates one
                        // bogey, and clicking the track again cycles the style. Six casings
                        // are three carriages' worth (two bogeys each).
                        ItemPackDefinition.PackEntry.of("create:railway_casing", 6),
                        // Every train needs Train Controls on board; the second one lets a
                        // train depart a terminus station in either direction.
                        ItemPackDefinition.PackEntry.of("create:controls", 2),
                        // The carriage body is anything you glue onto the bogeys — without
                        // Super Glue the blocks simply fall off the train.
                        ItemPackDefinition.PackEntry.of("create:super_glue", 2),
                        // Trains only burn fuel they find in an assembled chest or barrel
                        // (fuel in vaults is ignored), and the chests double as the
                        // carriage body, so no separate crate is needed.
                        ItemPackDefinition.PackEntry.of("minecraft:chest", 2),
                        ItemPackDefinition.PackEntry.of("minecraft:coal", 64),
                        ItemPackDefinition.PackEntry.of("create:white_seat", 4),
                        ItemPackDefinition.PackEntry.of("create:wrench", 1),
                        // Schedules drive a train with nobody at the Controls; the driver
                        // must be a mob (or a Steam 'n' Rails conductor) sitting there.
                        ItemPackDefinition.PackEntry.of("create:schedule", 2)
                ),
                ItemPackDefinition.Delivery.CRATE,
                ItemPackDefinition.resolveStack("create:controls", 1)
        ));

        // --- Aeronautics Starter Kit: everything needed for one first flying
        // contraption. Contents follow the mods' own ponder scenes in the pack
        // (Create: Aeronautics 1.3.2 bundle = aeronautics + simulated + offroad):
        // "physics_assembler_intro" assembles a block group into a Simulated
        // Contraption (selected with Super Glue or Honey Glue), "portable_engine"
        // burns fuel into rotation, "propeller_bearing_size/thrust" turns a bearing
        // plus sail-like blocks (tag create:windmill_sails) into thrust, and the
        // gyroscopic bearing is the self-stabilizing one used for helicopters.
        register(new ItemPackDefinition(
                "aeronautics_starter_kit",
                "Aeronautics Starter Kit",
                "Everything for a first flying contraption: assembler, engine, propellers & sails.",
                75,
                List.of(
                        // The heart: assembles the blocks into a physics contraption,
                        // and any assembler can take one apart again.
                        ItemPackDefinition.PackEntry.of("simulated:physics_assembler", 1),
                        // Select the block group to assemble — Honey Glue is the
                        // Simulated-native tool (Super Glue also works, and Honey Glue
                        // will attach to overlapping Super Glue but not the reverse).
                        ItemPackDefinition.PackEntry.of("simulated:honey_glue", 1),
                        ItemPackDefinition.PackEntry.of("create:super_glue", 1),
                        // Rotational force from burning fuel; the red one is the base
                        // recipe, every other colour is a dye variant of it.
                        ItemPackDefinition.PackEntry.of("simulated:red_portable_engine", 1),
                        ItemPackDefinition.PackEntry.of("minecraft:coal", 64),
                        // Fuel only goes in by automated means, so the engine needs a
                        // funnel fed from a container that rides along.
                        ItemPackDefinition.PackEntry.of("create:andesite_funnel", 1),
                        ItemPackDefinition.PackEntry.of("minecraft:chest", 1),
                        // Thrust. A bearing attaches to the block in front of it and any
                        // structure with at least two sail-like blocks counts as a
                        // propeller; the gyroscopic one keeps itself upright, which is
                        // what makes a first hover stay stable.
                        ItemPackDefinition.PackEntry.of("aeronautics:gyroscopic_propeller_bearing", 1),
                        ItemPackDefinition.PackEntry.of("aeronautics:propeller_bearing", 1),
                        // Ready-made propellers, for when a one-block propeller on a
                        // shaft is enough (the andesite one is the base style).
                        ItemPackDefinition.PackEntry.of("aeronautics:andesite_propeller", 2),
                        // Propeller blades and wings: moving sails generate lift, which
                        // is how a contraption stays in the air once it is fast enough.
                        ItemPackDefinition.PackEntry.of("create:white_sail", 16),
                        ItemPackDefinition.PackEntry.of("create:sail_frame", 4),
                        // Symmetric sails make no lift, only drag — rudders and
                        // stabilizers for steering.
                        ItemPackDefinition.PackEntry.of("simulated:white_symmetric_sail", 4),
                        // Drivetrain, so the engine can reach the bearing at any angle.
                        ItemPackDefinition.PackEntry.of("create:shaft", 8),
                        ItemPackDefinition.PackEntry.of("create:cogwheel", 2),
                        ItemPackDefinition.PackEntry.of("create:large_cogwheel", 1),
                        ItemPackDefinition.PackEntry.of("create:gearbox", 1),
                        ItemPackDefinition.PackEntry.of("create:andesite_casing", 4),
                        // Somewhere to sit, and the two tools the ponder scenes rely on:
                        // the wrench reverses a propeller's thrust, the goggles read
                        // Thrust and Airflow off it.
                        ItemPackDefinition.PackEntry.of("create:white_seat", 1),
                        ItemPackDefinition.PackEntry.of("create:wrench", 1),
                        ItemPackDefinition.PackEntry.of("aeronautics:aviators_goggles", 1)
                ),
                ItemPackDefinition.Delivery.CRATE,
                ItemPackDefinition.resolveStack("simulated:physics_assembler", 1)
        ));

        // --- Traveler's Kit: a complete field loadout for heading out. Chainmail
        // has no crafting recipe in vanilla — it only comes from mob drops, loot
        // and trades — so the kit is the reliable way to hand a full set over.
        register(new ItemPackDefinition(
                "travelers_kit",
                "Traveler's Kit",
                "Chainmail armour, an enchanted iron sword, a bow with arrows & an iron backpack.",
                40,
                List.of(
                        ItemPackDefinition.PackEntry.of("minecraft:chainmail_helmet", 1),
                        ItemPackDefinition.PackEntry.of("minecraft:chainmail_chestplate", 1),
                        ItemPackDefinition.PackEntry.of("minecraft:chainmail_leggings", 1),
                        ItemPackDefinition.PackEntry.of("minecraft:chainmail_boots", 1),
                        // Sharpness I + Unbreaking II, applied from the datapack
                        // enchantment registry when the pack is built.
                        ItemPackDefinition.PackEntry.enchanted("minecraft:iron_sword", 1,
                                Map.of("minecraft:sharpness", 1, "minecraft:unbreaking", 2)),
                        ItemPackDefinition.PackEntry.of("minecraft:bow", 1),
                        ItemPackDefinition.PackEntry.of("minecraft:arrow", 16),
                        // Sophisticated Backpacks, the backpack mod in the pack.
                        ItemPackDefinition.PackEntry.of("sophisticatedbackpacks:iron_backpack", 1)
                ),
                ItemPackDefinition.Delivery.CRATE,
                ItemPackDefinition.resolveStack("sophisticatedbackpacks:iron_backpack", 1)
        ));

        // --- Super Glue: one cheap utility item, so it skips the crate ---
        // Create's Super Glue is what turns a loose pile of blocks into a group that
        // moves as one contraption (train carriages, flying machines, sliding doors).
        // The recipe yields a single item (2 slimeballs + 1 iron nugget + 1 iron
        // plate), so one per purchase matches what a crafter would get anyway, and
        // the stack is handed straight to the inventory: a crate around a single item
        // is just one extra right-click before it can be used.
        register(new ItemPackDefinition(
                "super_glue",
                "Super Glue",
                "One Create Super Glue for grouping blocks into contraptions, given straight to your inventory.",
                5,
                List.of(ItemPackDefinition.PackEntry.of("create:super_glue", 1)),
                ItemPackDefinition.Delivery.DIRECT,
                ItemPackDefinition.resolveStack("create:super_glue", 1)
        ));

        if (xyz.alyrion.alyrioncore.compat.OpacCompat.isOpacInstalled()) {
            register(ItemPackDefinition.claimChunks(
                    "claim_chunk_1",
                    "Additional Claim Chunk",
                    "1 additional chunk claim in Open Parties and Claims.",
                    2,
                    1,
                    ItemPackDefinition.resolveStack("minecraft:filled_map", 1)
            ));
            register(ItemPackDefinition.claimChunks(
                    "claim_chunk_10",
                    "10 Additional Claim Chunks",
                    "10 additional chunk claims in Open Parties and Claims.",
                    18,
                    10,
                    ItemPackDefinition.resolveStack("minecraft:filled_map", 10)
            ));
        }
    }

    /** Ensure the registry is loaded (idempotent). Safe once mod registries are frozen. */
    public static void ensureLoaded() {
        if (PACKS.isEmpty()) {
            init();
        }
    }

    public static ItemPackDefinition fromId(String id) {
        ensureLoaded();
        if (id == null) return null;
        ItemPackDefinition pack = PACKS.get(id);
        if (pack != null && pack.delivery() == ItemPackDefinition.Delivery.CLAIM_CHUNKS && !xyz.alyrion.alyrioncore.compat.OpacCompat.isOpacInstalled()) {
            return null;
        }
        return pack;
    }

    public static boolean isRegistered(String id) {
        return fromId(id) != null;
    }

    public static List<ItemPackDefinition> all() {
        ensureLoaded();
        List<ItemPackDefinition> list = new ArrayList<>();
        for (ItemPackDefinition pack : PACKS.values()) {
            if (pack.delivery() != ItemPackDefinition.Delivery.CLAIM_CHUNKS || xyz.alyrion.alyrioncore.compat.OpacCompat.isOpacInstalled()) {
                list.add(pack);
            }
        }
        return Collections.unmodifiableList(list);
    }
}
