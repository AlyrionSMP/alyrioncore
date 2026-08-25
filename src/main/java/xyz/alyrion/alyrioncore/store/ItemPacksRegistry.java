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
        register(new ItemPackDefinition(
                "train_starter_kit",
                "Train Starter Kit",
                "A full basic Create train: tracks, bogeys, controls, seats & a steam engine.",
                50,
                List.of(
                        ItemPackDefinition.resolveStack("create:track", 192),
                        ItemPackDefinition.resolveStack("create:track_station", 2),
                        // Create 6: bogeys are not items — a Railway Casing placed
                        // on track becomes a Small Bogey (click to cycle size).
                        ItemPackDefinition.resolveStack("create:railway_casing", 6),
                        ItemPackDefinition.resolveStack("create:controls", 1),
                        ItemPackDefinition.resolveStack("create:white_seat", 4),
                        ItemPackDefinition.resolveStack("create:steam_engine", 1),
                        ItemPackDefinition.resolveStack("create:fluid_tank", 6),
                        ItemPackDefinition.resolveStack("create:wrench", 1),
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
