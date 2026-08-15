package xyz.alyrion.alyrioncore.world.habitat;

import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
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
import net.minecraft.world.level.block.state.BlockState;
import xyz.alyrion.alyrioncore.block.AirlockBlock;
import xyz.alyrion.alyrioncore.block.SleepingPodBlock;
import xyz.alyrion.alyrioncore.registry.ModBlocks;
import xyz.alyrion.alyrioncore.world.ModDimensions;

import java.util.ArrayDeque;
import java.util.Queue;

public class HabitatSealManager {

    private static final int MAX_ROOM_VOLUME = 6144; // Up to 6,144 blocks volume for habitats & greenhouses
    private static final Long2BooleanMap SEAL_CACHE = new Long2BooleanOpenHashMap();
    private static long lastCacheClearTick = 0;

    public static boolean isPositionSealed(ServerLevel level, BlockPos pos) {
        if (!level.dimension().equals(ModDimensions.MARS_LEVEL)) {
            return true; // Overworld and non-vacuum dimensions are naturally pressurized
        }

        long gameTime = level.getGameTime();
        if (gameTime - lastCacheClearTick > 40) {
            SEAL_CACHE.clear();
            lastCacheClearTick = gameTime;
        }

        long posKey = pos.asLong();
        if (SEAL_CACHE.containsKey(posKey)) {
            return SEAL_CACHE.get(posKey);
        }

        boolean sealed = runFloodFill(level, pos);
        SEAL_CACHE.put(posKey, sealed);
        return sealed;
    }

    private static boolean runFloodFill(ServerLevel level, BlockPos startPos) {
        // Fast pre-checks
        if (level.canSeeSky(startPos)) {
            return false;
        }

        BlockState startState = level.getBlockState(startPos);
        if (isAirtight(startState, level, startPos)) {
            return true;
        }

        LongSet visited = new LongOpenHashSet();
        Queue<BlockPos> queue = new ArrayDeque<>();

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
                    return false; // Escaped into the Martian exosphere or void
                }

                if (level.canSeeSky(neighbor)) {
                    return false; // Directly exposed to open vacuum sky
                }

                BlockState state = level.getBlockState(neighbor);
                if (isAirtight(state, level, neighbor)) {
                    // Reached airtight boundary block (wall/window/closed airlock). Do not expand through it.
                    continue;
                }

                visited.add(neighborKey);
                queue.add(neighbor);

                if (visited.size() > MAX_ROOM_VOLUME) {
                    return false; // Room volume exceeds maximum limit; considered unsealed/open world
                }
            }
        }

        // Cache all interior coordinates in the sealed room
        for (long key : visited) {
            SEAL_CACHE.put(key, true);
        }

        return true;
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
        if (!level.dimension().equals(ModDimensions.MARS_LEVEL)) {
            return;
        }

        // Check if broken block was adjacent to a sealed interior
        boolean wasSealed = false;
        for (Direction dir : Direction.values()) {
            BlockPos adj = pos.relative(dir);
            if (SEAL_CACHE.getOrDefault(adj.asLong(), false)) {
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
