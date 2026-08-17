package xyz.alyrion.alyrioncore.compat;

import net.minecraft.world.level.Level;
import xyz.alyrion.alyrioncore.world.ModDimensions;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Optional;
import java.util.SortedMap;

/**
 * Detects "vacuum worlds" — dimensions whose atmosphere is hostile and where
 * the habitat seal system grants breathable air inside sealed rooms.
 *
 * The source of truth is Rocketnautics' public atmosphere API
 * ({@code DeepSpaceHelper} / {@code PlanetDimensionData}): any dimension whose
 * atmosphere map carries the {@code DROWNING} flag at the queried altitude is
 * a vacuum world. This makes the habitat system work on Mars, the Moon and any
 * future vacuum dimension without hardcoding dimension IDs. When Rocketnautics
 * is not installed (or its API changes), it falls back to Mars only, so the
 * mod keeps working standalone.
 *
 * Accessed via reflection (handles cached in a static initializer) so
 * AlyrionCore has no hard dependency on Rocketnautics/Cosmonautics at compile
 * time and per-tick lookups stay cheap.
 */
public final class VacuumAtmosphere {

    private static Method IS_DEEP_SPACE;
    private static Method GET_DATA;
    private static Object DROWNING;
    private static boolean rocketnauticsPresent = false;

    static {
        try {
            Class<?> helper = Class.forName("dev.devce.rocketnautics.api.orbit.DeepSpaceHelper");
            IS_DEEP_SPACE = helper.getMethod("isDeepSpace", Level.class);
            GET_DATA = helper.getMethod("getDataForDimension", Level.class);
            Class<?> flagsCls = Class.forName("dev.devce.rocketnautics.api.orbit.AtmosphereFlags");
            DROWNING = Enum.valueOf(flagsCls.asSubclass(Enum.class), "DROWNING");
            rocketnauticsPresent = true;
        } catch (Throwable t) {
            // Rocketnautics not installed: fall back to Mars-only handling.
            rocketnauticsPresent = false;
        }
    }

    private VacuumAtmosphere() {
    }

    /** True when the given altitude in this dimension is hostile vacuum (no breathable air). */
    public static boolean isVacuum(Level level, double y) {
        if (!rocketnauticsPresent) {
            return level.dimension().equals(ModDimensions.MARS_LEVEL);
        }
        try {
            if ((Boolean) IS_DEEP_SPACE.invoke(null, level)) {
                return true; // deep space is always vacuum
            }
            Object opt = GET_DATA.invoke(null, level);
            if (!(opt instanceof Optional<?> dataOpt) || dataOpt.isEmpty()) {
                return false; // dimension has no planet atmosphere data -> breathable
            }
            Object data = dataOpt.get();
            Object map = data.getClass().getMethod("atmosphere").invoke(data);
            if (!(map instanceof SortedMap<?, ?>)) {
                return false;
            }
            @SuppressWarnings("rawtypes")
            SortedMap sorted = (SortedMap) map;   // fastutil's map is a SortedMap<Integer, EnumSet>
            SortedMap<?, ?> tail = sorted.tailMap((int) y);
            if (tail.isEmpty()) {
                return false;
            }
            Object flags = tail.firstEntry().getValue();
            return flags instanceof Collection<?> col && col.contains(DROWNING);
        } catch (Throwable t) {
            // Rocketnautics' API changed mid-flight: Mars is the only vacuum world.
            return level.dimension().equals(ModDimensions.MARS_LEVEL);
        }
    }
}
