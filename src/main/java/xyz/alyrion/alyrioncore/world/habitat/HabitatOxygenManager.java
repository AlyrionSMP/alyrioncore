package xyz.alyrion.alyrioncore.world.habitat;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Tracks how much oxygen each sealed room currently holds, so pressurization is a
 * gradual process instead of instant.
 *
 * <p><b>The rule:</b> filling a sealed room takes <b>0.5 seconds per interior block</b>
 * per running oxygen generator — two generators double the speed, three triple it, etc.
 * A room becomes breathable only once it is 100% filled. When no generator is running
 * the oxygen slowly drains again (a full room loses breathability almost immediately,
 * but a partially filled room keeps its progress, so restarting the generator refills
 * it faster than starting from scratch).
 *
 * <p><b>Implementation notes:</b> rooms are identified by a <i>deterministic anchor</i>
 * (the minimum interior cell of the flood fill, see {@link HabitatSealManager}), so the
 * same room maps to the same entry no matter which cell was queried. State is advanced
 * <i>lazily</i>: instead of ticking every room every tick, the elapsed game time since
 * the last query is multiplied by the fill rate on the next query — a room with a
 * running generator fills at the correct speed even while nobody is inside. Only the
 * server advances the state ({@code !level.isClientSide}); in an integrated server the
 * client shares this JVM and would otherwise double-count the fill.
 */
public final class HabitatOxygenManager {

    /** Interior blocks oxygenated per second by ONE running generator
     *  (0.5 s per block; generators stack linearly). */
    public static final float OXYGEN_PER_GENERATOR_PER_SECOND = 2.0f;

    /** Interior blocks of oxygen lost per second from a sealed room with no running
     *  generator (same rate as one generator refills, so a full room goes unbreathable
     *  within a tick of losing power while a partial fill is preserved for a faster
     *  refill later). */
    public static final float OXYGEN_DRAIN_PER_SECOND = 2.0f;

    /** A room's identity: its dimension plus the deterministic flood-fill anchor. */
    private record RoomKey(ResourceKey<Level> dimension, long anchor) {
    }

    private static final class RoomState {
        int volume;          // interior blocks (air cells) of the sealed room
        float oxygen;        // blocks of oxygen currently held, 0..volume
        long lastUpdateTick; // game time of the last state advancement
    }

    private static final ConcurrentMap<RoomKey, RoomState> ROOMS = new ConcurrentHashMap<>();

    private HabitatOxygenManager() {
    }

    /**
     * Advance the room's oxygen by the time elapsed since its last update and return
     * the fraction (0..1) of the room that is currently filled. Callers should only
     * pass room info from a sealed scan ({@code roomKey != 0, volume > 0}).
     *
     * @param level      the level the room is in (dimension keys the room entry;
     *                   {@code isClientSide} reads without advancing the state)
     * @param roomKey    deterministic room anchor (min interior cell)
     * @param volume     interior block count from the latest seal scan
     * @param generators number of running oxygen generators found inside the room
     * @return the current fill fraction, 0..1
     */
    public static float fillFraction(Level level, long roomKey, int volume, int generators) {
        RoomState room = ROOMS.computeIfAbsent(new RoomKey(level.dimension(), roomKey), k -> {
            RoomState s = new RoomState();
            s.volume = volume;
            s.lastUpdateTick = level.getGameTime();
            return s;
        });
        // Adopt the latest scan: walls can move while a room stays sealed, changing volume.
        room.volume = volume;

        if (!level.isClientSide) {
            long now = level.getGameTime();
            long elapsed = Math.max(0L, now - room.lastUpdateTick);
            if (elapsed > 0) {
                float seconds = elapsed / 20f;
                float delta = (generators > 0
                        ? generators * OXYGEN_PER_GENERATOR_PER_SECOND
                        : -OXYGEN_DRAIN_PER_SECOND) * seconds;
                room.oxygen = Math.max(0f, Math.min(room.volume, room.oxygen + delta));
                room.lastUpdateTick = now;
            }
        }
        return room.volume > 0 ? Math.min(1f, room.oxygen / room.volume) : 1f;
    }

    /** Vent a breached room back to vacuum — its oxygen is gone and refilling starts fresh. */
    public static void onBreach(Level level, long roomKey) {
        ROOMS.remove(new RoomKey(level.dimension(), roomKey));
    }

    /** A new server session started: drop room state left over from a previous world. */
    public static void onServerStarted() {
        ROOMS.clear();
    }
}
