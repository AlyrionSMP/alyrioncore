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
                        ItemPackDefinition.resolveStack("create:track", 192),
                        // Assembly AND disassembly happen at a station only, so a basic
                        // line wants a station at each end.
                        ItemPackDefinition.resolveStack("create:track_station", 2),
                        // Create 6 has no bogey item (nothing crafts create:small_bogey /
                        // create:large_bogey): using a Train Casing on a track creates one
                        // bogey, and clicking the track again cycles the style. Six casings
                        // are three carriages' worth (two bogeys each).
                        ItemPackDefinition.resolveStack("create:railway_casing", 6),
                        // Every train needs Train Controls on board; the second one lets a
                        // train depart a terminus station in either direction.
                        ItemPackDefinition.resolveStack("create:controls", 2),
                        // The carriage body is anything you glue onto the bogeys — without
                        // Super Glue the blocks simply fall off the train.
                        ItemPackDefinition.resolveStack("create:super_glue", 2),
                        // Trains only burn fuel they find in an assembled chest or barrel
                        // (fuel in vaults is ignored), and the chests double as the
                        // carriage body, so no separate crate is needed.
                        ItemPackDefinition.resolveStack("minecraft:chest", 2),
                        ItemPackDefinition.resolveStack("minecraft:coal", 64),
                        ItemPackDefinition.resolveStack("create:white_seat", 4),
                        ItemPackDefinition.resolveStack("create:wrench", 1),
                        // Schedules drive a train with nobody at the Controls; the driver
                        // must be a mob (or a Steam 'n' Rails conductor) sitting there.
                        ItemPackDefinition.resolveStack("create:schedule", 2)
                ),
                ItemPackDefinition.resolveStack("create:controls", 1)
        ));
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
        return PACKS.get(id);
    }

    public static boolean isRegistered(String id) {
        return fromId(id) != null;
    }

    public static List<ItemPackDefinition> all() {
        ensureLoaded();
        return Collections.unmodifiableList(new ArrayList<>(PACKS.values()));
    }
}
