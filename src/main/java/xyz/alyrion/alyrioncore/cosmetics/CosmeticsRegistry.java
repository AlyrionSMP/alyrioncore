package xyz.alyrion.alyrioncore.cosmetics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central registry of every cosmetic in the game.
 *
 * This is the "add new cosmetics here" file: register a new
 * {@link CosmeticDefinition} and it automatically appears in the store tab of
 * its {@link CosmeticType}, becomes purchasable/equippable through the generic
 * data + networking stack, and renders through the type's renderer. No other
 * file needs to change.
 */
public final class CosmeticsRegistry {

    private static final List<CosmeticDefinition> ALL = new ArrayList<>();
    private static final Map<String, CosmeticDefinition> BY_ID = new HashMap<>();
    private static final Map<CosmeticType, List<CosmeticDefinition>> BY_TYPE = new java.util.EnumMap<>(CosmeticType.class);

    private CosmeticsRegistry() {
    }

    private static CosmeticDefinition register(String id, CosmeticType type, String displayName, String description,
                                               int price, boolean unlockedByDefault) {
        return register(new CosmeticDefinition(id, type, displayName, description, price, unlockedByDefault));
    }

    private static CosmeticDefinition register(String id, CosmeticType type, String displayName, String description,
                                               int price, boolean unlockedByDefault, boolean purchasable) {
        return register(new CosmeticDefinition(id, type, displayName, description, price, unlockedByDefault, purchasable));
    }

    private static CosmeticDefinition register(CosmeticDefinition def) {
        if (BY_ID.containsKey(def.getId())) {
            throw new IllegalStateException("Duplicate cosmetic id: " + def.getId());
        }
        ALL.add(def);
        BY_ID.put(def.getId(), def);
        BY_TYPE.computeIfAbsent(def.getType(), t -> new ArrayList<>()).add(def);
        return def;
    }

    /** Register all cosmetics. Ids are stable save-data keys: never rename an id after release. */
    private static void init() {
        // --- Capes ---
        register("2_year_celebration", CosmeticType.CAPE, "2 Year Celebration Cape",
                "Commemorating 2 years of Alyrion with a festive cake!", 0, true);
        register("season_8", CosmeticType.CAPE, "Season 8 Cape",
                "Exclusive crimson & gold cape celebrating Season 8!", 0, true);
        register("stars", CosmeticType.CAPE, "Stars Cape",
                "Deep space starry sky with an orbiting research satellite.", 5, false);
        register("moon", CosmeticType.CAPE, "Moon Cape",
                "Lunar surface overlooking planet Earth in deep space.", 5, false);
        register("marsian", CosmeticType.CAPE, "The Martian Cape",
                "Rust-ochre Martian dunes under the red planet, with a little green Martian.", 5, false);
        register("grim", CosmeticType.CAPE, "Grim Cape",
                "A black cape bearing a bleached skull, earned with 10 coins or 10 player kills.", 10, false);
        register("pride", CosmeticType.CAPE, "Pride Cape",
                "The Progress Pride flag: rainbow stripes behind the trans chevron, earned by partying up with at least 4 players (Open Parties and Claims).",
                0, false, false); // task-only: never purchasable
        register("chicken", CosmeticType.CAPE, "Chicken Cape",
                "A lively green cape sporting a clucking chicken face.", 5, false);
        register("trans_pride", CosmeticType.CAPE, "Trans Pride Cape",
                "Pastel blue, pink, and white stripes of the Transgender Pride flag.", 5, false);
        register("communism", CosmeticType.CAPE, "Communism Cape",
                "A bold red cape emblazoned with the golden hammer and sickle.", 5, false);
        register("ender", CosmeticType.CAPE, "End Cape",
                "A deep violet void cape adorned with an Ender Dragon crest.", 10, false);
        register("wither", CosmeticType.CAPE, "Wither Cape",
                "A dark, tattered ash cloak bearing the skeletal visage of the Wither.", 15, false);
        register("creeper", CosmeticType.CAPE, "Red Creeper Cape",
                "A jet-black cape bearing an ominous crimson Creeper face, earned by dying 100 times.", 5, false);
        register("zombie_villager", CosmeticType.CAPE, "Zombie Villager Cape",
                "A split-faced cape depicting the triumph of curing a Zombie Villager.", 5, false);
        register("overload", CosmeticType.CAPE, "Overload Cape",
                "A glitched black and magenta missing-texture cape for those who shattered the bank with 1,000 coins.", 50, false);
        register("palestine", CosmeticType.CAPE, "Palestine Cape",
                "A vibrant red, black, white, and green flag cape. Grants a +10% (1.1x) playtime coin multiplier while equipped!", 0, true);

        // --- Pets ---
        register("satellite", CosmeticType.PET, "Satellite Pet",
                "A little research satellite that orbits above your head!", 15, false);

        // --- Trails (demo category proving the generic framework) ---
        register("rocket_trail", CosmeticType.TRAIL, "Rocket Trail",
                "Leave a fiery rocket exhaust trail behind you as you run!", 8, false);
    }

    /** Ensure the registry is loaded (idempotent). */
    public static void ensureLoaded() {
        // Touching BY_ID triggers static init, which registers everything.
        if (ALL.isEmpty()) {
            init();
        }
    }

    public static CosmeticDefinition fromId(String id) {
        ensureLoaded();
        if (id == null) return null;
        return BY_ID.get(id);
    }

    public static boolean isRegistered(String id) {
        return fromId(id) != null;
    }

    public static List<CosmeticDefinition> all() {
        ensureLoaded();
        return Collections.unmodifiableList(ALL);
    }

    /** Cosmetics of one type, in store order. */
    public static List<CosmeticDefinition> getByType(CosmeticType type) {
        ensureLoaded();
        if (type == null) return Collections.emptyList();
        List<CosmeticDefinition> list = BY_TYPE.get(type);
        return list != null ? Collections.unmodifiableList(list) : Collections.emptyList();
    }
}
