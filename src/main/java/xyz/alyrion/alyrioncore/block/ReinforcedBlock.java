package xyz.alyrion.alyrioncore.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import xyz.alyrion.alyrioncore.AlyrionCore;

import java.util.function.BiConsumer;

/**
 * The wrapper block that replaces any reinforced block. It has no item of its
 * own and no loot table: breaking it (after the tier's worth of absorbed hits)
 * drops only what the ORIGINAL block would drop.
 *
 * Rendering is split in two:
 * <ul>
 *   <li>the blockstate model is a protruding riveted-plate frame (per tier),
 *       rendered by the chunk renderer so faces next to solid blocks are
 *       culled — the plates only show on air-facing sides;</li>
 *   <li>a BlockEntityRenderer draws the stored original block model inside the
 *       frame, so the reinforced block keeps the original block's look.</li>
 * </ul>
 */
public class ReinforcedBlock extends Block implements EntityBlock {

    public static final EnumProperty<ReinforcementTier> TIER =
            EnumProperty.create("tier", ReinforcementTier.class);

    public ReinforcedBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(TIER, ReinforcementTier.IRON));
    }

    public static Properties reinforcedProperties() {
        return Properties.of()
                .mapColor(MapColor.METAL)
                // Mining hardness AND explosion behavior come from the
                // protected block (see getDestroyProgress / getExplosionResistance
                // / onExplosionHit). The hardness here is only a fallback while
                // the block entity is unavailable — keep it NORMAL so a client
                // with a momentarily-empty BE doesn't grind at obsidian speed.
                .strength(2.0F, 3600000.0F)
                .sound(SoundType.METAL);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TIER);
    }

    /** Maps consumed hits to a 0..7 crack stage (0 = pristine, 7 = nearly broken). */
    public static int crackStageFor(int totalHits, int hitsRemaining) {
        int consumed = Math.max(0, totalHits - hitsRemaining);
        return consumed * 7 / Math.max(1, totalHits - 1);
    }

    /**
     * The absorb handler schedules this one tick AFTER the break event, purely
     * to re-push the block entity data to every player. The blockstate and the
     * block itself are NEVER touched — but the client's destroy prediction
     * drops its BE and re-creates it EMPTY, so this guaranteed re-send (past
     * the prediction window) restores the original state + crack stage. Without
     * it the client renders an empty wrapper (clear block) and falls back to
     * the wrapper hardness.
     */
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof ReinforcedBlockEntity be) {
            var packet = be.getUpdatePacket();
            if (packet != null) {
                for (ServerPlayer player : level.players()) {
                    player.connection.send(packet);
                }
            }
            AlyrionCore.LOGGER.info("[reinforce] BE re-sent at {} stage {} hits {}", pos, be.getCrackStage(), be.getHitsRemaining());
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ReinforcedBlockEntity(pos, state);
    }

    @Override
    public SoundType getSoundType(BlockState state) {
        return state.getValue(TIER).getSoundType();
    }

    /**
     * The wrapper mines exactly as hard as the block it protects: delegate the
     * destroy progress (hardness + the player's tool/effects) to the stored
     * original state, so every mining cycle takes the same effort — and obeys
     * the same tool rules — as mining the original block. Runs on both sides;
     * the client has the block entity synced, so crack timing matches.
     */
    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof ReinforcedBlockEntity be) {
            BlockState original = be.getOriginalState();
            if (!original.isAir()) {
                return original.getDestroyProgress(player, level, pos);
            }
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    /**
     * Explosions treat the reinforced block exactly like the block it protects:
     * same blast absorption (NeoForge's context-aware resistance hook), so TNT
     * destroys the reinforcement precisely when it would destroy the original.
     */
    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        if (level.getBlockEntity(pos) instanceof ReinforcedBlockEntity be) {
            BlockState original = be.getOriginalState();
            if (!original.isAir()) {
                return original.getBlock().getExplosionResistance(original, level, pos, explosion);
            }
        }
        return super.getExplosionResistance(state, level, pos, explosion);
    }

    /**
     * An explosion that would destroy the protected block absorbs ONE hit
     * instead of one-shotting the reinforcement: the hit budget decrements, the
     * crack overlay advances and the block stays — so TNT must hit the block
     * as many times as mining cycles would, before it finally breaks. On the
     * final hit the whole per-block explosion handling is handed to the
     * protected block: it drops the ORIGINAL block's explosion loot (dirt
     * drops dirt, ores drop nothing without a tool, ...), fires its
     * {@code wasExploded} hook and removes the block — no reinforced block, no
     * plates.
     */
    @Override
    public void onExplosionHit(BlockState state, Level level, BlockPos pos, Explosion explosion,
                               BiConsumer<ItemStack, BlockPos> dropConsumer) {
        if (level.getBlockEntity(pos) instanceof ReinforcedBlockEntity be) {
            BlockState original = be.getOriginalState();
            if (!original.isAir()) {
                int hits = be.getHitsRemaining();
                if (hits > 1) {
                    be.setHitsRemaining(hits - 1);
                    int newStage = crackStageFor(state.getValue(TIER).getHits(), hits - 1);
                    be.setCrackStage(newStage);
                    be.setChanged();
                    level.sendBlockUpdated(pos, state, state, 3);
                    level.playSound(null, pos, state.getValue(TIER).getHitSound(), SoundSource.BLOCKS, 1.0F, 0.9F);
                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.CRIT,
                                pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5,
                                8, 0.25, 0.25, 0.25, 0.05);
                    }
                    return;
                }
                original.onExplosionHit(level, pos, explosion, dropConsumer);
                return;
            }
        }
        super.onExplosionHit(state, level, pos, explosion, dropConsumer);
    }

    /**
     * Fire treats the reinforcement exactly like the block it protects: the
     * wrapper reports the ORIGINAL block's flammability, so reinforced wood
     * burns (and reinforced stone never does) just like the unprotected block.
     * The vanilla fire block keeps per-block odds in its own maps keyed by the
     * BLOCK, and NeoForge routes them through these context-aware hooks — both
     * spread (getFireSpreadSpeed) and burn-out (getFlammability) must delegate
     * to the original block or the metal wrapper would never catch fire.
     */
    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (level.getBlockEntity(pos) instanceof ReinforcedBlockEntity be) {
            BlockState original = be.getOriginalState();
            if (!original.isAir()) {
                return original.getBlock().getFlammability(original, level, pos, direction);
            }
        }
        return super.getFlammability(state, level, pos, direction);
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (level.getBlockEntity(pos) instanceof ReinforcedBlockEntity be) {
            BlockState original = be.getOriginalState();
            if (!original.isAir()) {
                return original.getBlock().getFireSpreadSpeed(original, level, pos, direction);
            }
        }
        return super.getFireSpreadSpeed(state, level, pos, direction);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        // Creative pick / Jade name: give the ORIGINAL block's item, named
        // "Reinforced <original>" so the HUD reads e.g. "Reinforced Sand".
        if (level.getBlockEntity(pos) instanceof ReinforcedBlockEntity be) {
            BlockState original = be.getOriginalState();
            if (!original.isAir()) {
                ItemStack stack = original.getBlock().getCloneItemStack(level, pos, original);
                if (!stack.isEmpty()) {
                    stack.set(DataComponents.CUSTOM_NAME, Component.translatable(
                            "block.alyrioncore.reinforced_block.name", original.getBlock().getName()));
                }
                return stack;
            }
        }
        return super.getCloneItemStack(level, pos, state);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              @Nullable BlockEntity blockEntity, ItemStack tool) {
        // Vanilla drop handling: our block has no loot table, so nothing drops
        // here — then we spawn the ORIGINAL block's loot (silk touch, fortune
        // and tool requirements of the original block all apply).
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (level instanceof ServerLevel serverLevel && blockEntity instanceof ReinforcedBlockEntity be) {
            BlockState original = be.getOriginalState();
            // Vanilla gates drops on the correct tool in ServerPlayerGameMode
            // (hasCorrectToolForDrops), NOT in the loot table — so a fist on
            // reinforced stone would otherwise still yield stone. Replicate the
            // exact gate the ORIGINAL block would have been subject to.
            if (!original.isAir() && player.hasCorrectToolForDrops(original)) {
                for (ItemStack drop : Block.getDrops(original, serverLevel, pos, blockEntity, player, tool)) {
                    Block.popResource(serverLevel, pos, drop);
                }
            }
        }
    }
}
