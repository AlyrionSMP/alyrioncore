package xyz.alyrion.alyrioncore.world.habitat;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import xyz.alyrion.alyrioncore.block.AirlockBlock;
import xyz.alyrion.alyrioncore.block.OxygenGeneratorBlock;
import xyz.alyrion.alyrioncore.block.OxygenGeneratorBlockEntity;
import xyz.alyrion.alyrioncore.block.SleepingPodBlock;
import xyz.alyrion.alyrioncore.compat.VacuumAtmosphere;
import xyz.alyrion.alyrioncore.registry.ModBlocks;

import java.util.ArrayDeque;
import java.util.Queue;

public class HabitatSealManager {

    private static final int MAX_ROOM_VOLUME = 6144; // Up to 6,144 blocks volume for habitats & greenhouses

    /** A seal check result: is the cell inside a sealed room, and does that room
     *  contain at least one powered (charged) oxygen generator? */
    public record SealResult(boolean sealed, boolean oxygen) {
        public static final SealResult OPEN_AIR = new SealResult(false, false);
        public static final SealResult SEALED = new SealResult(true, false);
        /** Non-vacuum dimensions: nothing to pressurize, always breathable. */
        public static final SealResult PRESSURIZED = new SealResult(true, true);
    }

    private static final ConcurrentMap<Long, SealResult> SEAL_CACHE = new ConcurrentHashMap<>();
    private static long lastCacheClearTick = 0;

    // Where the last failed flood fill escaped (diagnostics: tells players where the leak is).
    private static BlockPos lastLeakPos = null;
    private static Direction lastLeakDir = null;

    /** Block the most recent failed seal check escaped through, if any. */
    public static BlockPos getLastLeakPos() {
        return lastLeakPos;
    }

    /** Direction the most recent failed seal check escaped in. */
    public static Direction getLastLeakDir() {
        return lastLeakDir;
    }

    /** Full habitat state: sealed AND supplied by a powered oxygen generator.
     *  On non-vacuum dimensions there is nothing to pressurize — always breathable. */
    public static SealResult sealState(Level level, BlockPos pos) {
        if (!VacuumAtmosphere.isVacuum(level, pos.getY())) {
            return new SealResult(true, true); // Overworld & co. are naturally pressurized
        }

        long gameTime = level.getGameTime();
        if (gameTime - lastCacheClearTick > 40) {
            SEAL_CACHE.clear();
            lastCacheClearTick = gameTime;
        }

        long posKey = pos.asLong();
        SealResult cached = SEAL_CACHE.get(posKey);
        if (cached != null) {
            return cached;
        }

        SealResult result = runFloodFill(level, pos);
        SEAL_CACHE.put(posKey, result);
        return result;
    }

    public static boolean isPositionSealed(Level level, BlockPos pos) {
        return sealState(level, pos).sealed();
    }

    private static SealResult runFloodFill(Level level, BlockPos startPos) {
        // Fast pre-checks
        if (level.canSeeSky(startPos)) {
            lastLeakPos = startPos;
            lastLeakDir = Direction.UP; // open to the sky directly above
            return SealResult.OPEN_AIR;
        }

        BlockState startState = level.getBlockState(startPos);
        if (isAirtight(startState, level, startPos)) {
            // The queried cell is itself solid (player embedded / standing on a machine).
            // The surrounding room isn't scanned here, but a powered generator right at
            // the feet still counts as the room's oxygen source.
            boolean oxygen = startState.getBlock() instanceof OxygenGeneratorBlock
                    && generatorPowered(level, startPos, startState);
            return new SealResult(true, oxygen);
        }

        LongSet visited = new LongOpenHashSet();
        Queue<BlockPos> queue = new ArrayDeque<>();
        boolean hasOxygen = false;

        visited.add(startPos.asLong());
        queue.add(startPos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.relative(dir);
                long neighborKey = neighbor.asLong();

                if (visited.contains(neighborKey)) {
                    continue;
                }

                if (neighbor.getY() >= level.getMaxBuildHeight() || neighbor.getY() <= level.getMinBuildHeight()) {
                    lastLeakPos = current;
                    lastLeakDir = dir; // escaped into the exosphere or void
                    return SealResult.OPEN_AIR;
                }

                BlockState state = level.getBlockState(neighbor);

                // A powered oxygen generator inside the sealed volume pressurizes the room.
                // Checked before the general airtight test so the machine itself (a solid
                // full cube, and therefore "airtight") still registers as the O2 source.
                if (state.getBlock() instanceof OxygenGeneratorBlock) {
                    if (generatorPowered(level, neighbor, state)) {
                        hasOxygen = true;
                    }
                    continue;
                }

                if (isAirtight(state, level, neighbor)) {
                    // Reached airtight boundary block (wall/window/closed airlock).
                    // Checked BEFORE sky exposure: a roof or wall that happens to be
                    // the topmost block in its column ("can see sky") still seals.
                    continue;
                }

                if (level.canSeeSky(neighbor)) {
                    lastLeakPos = neighbor;
                    lastLeakDir = dir; // hole: a non-airtight cell exposed to open vacuum sky
                    return SealResult.OPEN_AIR;
                }

                visited.add(neighborKey);
                queue.add(neighbor);

                if (visited.size() > MAX_ROOM_VOLUME) {
                    lastLeakPos = current;
                    lastLeakDir = dir;
                    return SealResult.OPEN_AIR; // Room volume exceeds maximum limit; considered unsealed/open world
                }
            }
        }

        // Cache all interior coordinates in the sealed room (same O2 state as the whole room)
        SealResult result = new SealResult(true, hasOxygen);
        for (long key : visited) {
            SEAL_CACHE.put(key, result);
        }

        return result;
    }

    /** True if the oxygen generator at this position currently has both stored
     *  FE and water (i.e. it is actually running and producing oxygen).
     *  On the server this reads the block entity exactly; on the client the BE
     *  state may lag behind the server, so the synced ACTIVE blockstate (which
     *  mirrors {@code energy > 0 && water > 0}) is used as a fallback. */
    private static boolean generatorPowered(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof OxygenGeneratorBlockEntity gen) {
            return gen.isRunning();
        }
        return state.getValue(OxygenGeneratorBlock.ACTIVE);
    }

    public static boolean isAirtight(BlockState state, BlockGetter level, BlockPos pos) {
        if (state.isAir()) {
            return false;
        }

        if (state.getBlock() instanceof AirlockBlock) {
            return AirlockBlock.isAirtight(state);
        }

        if (state.getBlock() instanceof SleepingPodBlock) {
            return true;
        }

        if (state.getBlock() instanceof DoorBlock) {
            return state.is(Blocks.IRON_DOOR) && !state.getValue(DoorBlock.OPEN);
        }

        if (state.getBlock() instanceof TrapDoorBlock) {
            return state.is(Blocks.IRON_TRAPDOOR) && !state.getValue(TrapDoorBlock.OPEN);
        }

        if (state.is(Blocks.GLASS) || state.is(Blocks.TINTED_GLASS) || state.is(BlockTags.IMPERMEABLE)) {
            return true;
        }

        // Standard solid building blocks (stone, iron, terracotta, concrete, etc.)
        return state.isSolidRender(level, pos) || state.isCollisionShapeFullBlock(level, pos);
    }

    public static void onBlockBreak(ServerLevel level, BlockPos pos, BlockState brokenState) {
        if (!VacuumAtmosphere.isVacuum(level, pos.getY())) {
            return;
        }

        // Check if broken block was adjacent to a sealed interior
        boolean wasSealed = false;
        for (Direction dir : Direction.values()) {
            BlockPos adj = pos.relative(dir);
            SealResult cached = SEAL_CACHE.get(adj.asLong());
            if (cached != null && cached.sealed()) {
                wasSealed = true;
                break;
            }
        }

        if (wasSealed) {
            // Depressurization Particle Burst & Audio
            double px = pos.getX() + 0.5;
            double py = pos.getY() + 0.5;
            double pz = pos.getZ() + 0.5;

            level.sendParticles(ParticleTypes.POOF, px, py, pz, 18, 0.4, 0.4, 0.4, 0.15);
            level.sendParticles(ParticleTypes.CLOUD, px, py, pz, 12, 0.3, 0.3, 0.3, 0.12);
            level.sendParticles(ParticleTypes.SNOWFLAKE, px, py, pz, 15, 0.5, 0.5, 0.5, 0.20);

            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.5F, 1.4F);
            level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 0.8F, 1.8F);

            // Invalidate cache
            SEAL_CACHE.clear();
        }
    }
}
