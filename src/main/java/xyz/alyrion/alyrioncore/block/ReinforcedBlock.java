package xyz.alyrion.alyrioncore.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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
                // / onExplosionHit); these values are only fallbacks when the
                // block entity is unavailable.
                .strength(50.0F, 3600000.0F)
                .sound(SoundType.METAL);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TIER);
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
     * When an explosion does break the reinforcement, hand the whole per-block
     * explosion handling to the protected block: it drops the ORIGINAL block's
     * explosion loot (dirt drops dirt, ores drop nothing without a tool, ...),
     * fires its {@code wasExploded} hook and removes the block — no reinforced
     * block, no plates.
     */
    @Override
    public void onExplosionHit(BlockState state, Level level, BlockPos pos, Explosion explosion,
                               BiConsumer<ItemStack, BlockPos> dropConsumer) {
        if (level.getBlockEntity(pos) instanceof ReinforcedBlockEntity be) {
            BlockState original = be.getOriginalState();
            if (!original.isAir()) {
                original.onExplosionHit(level, pos, explosion, dropConsumer);
                return;
            }
        }
        super.onExplosionHit(state, level, pos, explosion, dropConsumer);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        // Creative pick gives the ORIGINAL block, not the reinforcement.
        if (level.getBlockEntity(pos) instanceof ReinforcedBlockEntity be) {
            BlockState original = be.getOriginalState();
            if (!original.isAir()) {
                return original.getBlock().getCloneItemStack(level, pos, original);
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
