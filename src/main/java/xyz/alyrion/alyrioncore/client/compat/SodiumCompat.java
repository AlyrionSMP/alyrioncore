package xyz.alyrion.alyrioncore.client.compat;

import xyz.alyrion.alyrioncore.AlyrionCore;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Soft integration with Sodium.
 *
 * <p><b>Why this exists:</b> Sodium's "Fog Occlusion" option is a render-distance optimisation —
 * {@code RenderSectionManager#getSearchDistance()} clamps Sodium's chunk search distance to the
 * distance the current fog actually hides ({@code getEffectiveRenderDistance()}). Mars dust storms
 * drive the fog end down to a handful of blocks (and ATMOSPHERICS' per-biome values down to ~6), so
 * under that optimisation Sodium stops loading and rendering whole chunk sections around the player:
 * terrain visibly pops out and back in close by. It is the option, not the fog, that is wrong here —
 * the fog is meant to hide the world.
 *
 * <p>ATMOSPHERICS patches the same setting for its own fog, but does it by rewriting
 * {@code config/sodium-options.json} once at startup, which does not survive Sodium writing the file
 * back. This class flips the <em>live</em> option instead — {@code SodiumClientMod.options()} is a
 * plain public mutable field tree, read per frame by the chunk renderer — and restores the player's
 * own value as soon as visibility recovers or the storm ends.
 *
 * <p>Reflection on purpose: Sodium's option classes are internal and move between releases, and
 * Sodium is an optional client-side mod we must not require at runtime.
 */
public final class SodiumCompat {

    private static final String OPTIONS_HOLDER = "net.caffeinemc.mods.sodium.client.SodiumClientMod";

    private static boolean resolveFailed;
    private static Method optionsMethod;
    private static Field performanceField;
    private static Field fogOcclusionField;

    /** The player's own value, remembered while we override it. */
    private static Boolean suppressedFrom;

    private SodiumCompat() {
    }

    /** True while we are overriding the player's option. */
    public static boolean isSuppressing() {
        return suppressedFrom != null;
    }

    /**
     * Suspends Sodium's fog occlusion while {@code suppress} is true, then puts the player's own
     * value back. Cheap and safe to call every tick.
     */
    public static void setFogOcclusionSuspended(boolean suppress) {
        if (!suppress) {
            if (suppressedFrom == null) {
                return;
            }
            Boolean original = suppressedFrom;
            suppressedFrom = null;
            Object performance = livePerformance();
            if (performance != null) {
                write(fogOcclusionField, performance, original);
            }
            return;
        }

        if (suppressedFrom != null) {
            return; // already suspended
        }
        Object performance = livePerformance();
        if (performance == null || fogOcclusionField == null) {
            return;
        }
        Object current = read(fogOcclusionField, performance);
        if (!(current instanceof Boolean enabled) || !enabled) {
            return; // already off (or unreadable) — nothing to suspend
        }
        suppressedFrom = enabled;
        write(fogOcclusionField, performance, Boolean.FALSE);
        AlyrionCore.LOGGER.info(
                "Sodium fog occlusion suspended: Mars visibility is below Sodium's search distance");
    }

    /** The live {@code SodiumOptions.PerformanceSettings} instance, or null if Sodium is not ready. */
    private static Object livePerformance() {
        if (!resolve()) {
            return null;
        }
        Object options;
        try {
            options = optionsMethod.invoke(null);
        } catch (Throwable t) {
            return null;
        }
        if (options == null) {
            return null; // Sodium's client config is not initialised yet
        }
        if (performanceField == null) {
            performanceField = publicField(options.getClass(), "performance");
        }
        Object performance = read(performanceField, options);
        if (performance != null && fogOcclusionField == null) {
            fogOcclusionField = publicField(performance.getClass(), "useFogOcclusion");
        }
        return performance;
    }

    private static boolean resolve() {
        if (optionsMethod != null) {
            return true;
        }
        if (resolveFailed) {
            return false;
        }
        try {
            optionsMethod = Class.forName(OPTIONS_HOLDER).getMethod("options");
            return true;
        } catch (Throwable t) {
            resolveFailed = true;
            return false;
        }
    }

    private static Field publicField(Class<?> owner, String name) {
        try {
            return owner.getField(name);
        } catch (Throwable t) {
            return null;
        }
    }

    private static Object read(Field field, Object holder) {
        if (field == null || holder == null) {
            return null;
        }
        try {
            return field.get(holder);
        } catch (Throwable t) {
            return null;
        }
    }

    private static void write(Field field, Object holder, Object value) {
        if (field == null || holder == null) {
            return;
        }
        try {
            field.set(holder, value);
        } catch (Throwable t) {
            AlyrionCore.LOGGER.warn("Could not update Sodium fog occlusion: {}", t.toString());
        }
    }
}
