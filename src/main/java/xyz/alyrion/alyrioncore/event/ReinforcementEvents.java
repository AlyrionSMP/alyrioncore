package xyz.alyrion.alyrioncore.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import xyz.alyrion.alyrioncore.AlyrionCore;
import xyz.alyrion.alyrioncore.block.ReinforcedBlock;
import xyz.alyrion.alyrioncore.block.ReinforcedBlockEntity;
import xyz.alyrion.alyrioncore.block.ReinforcementTier;
import xyz.alyrion.alyrioncore.registry.ModBlocks;

/**
 * The two halves of the reinforcement mechanic:
 *
 * <ul>
 *   <li><b>Apply</b> — right-clicking a breakable block with a plate replaces
 *       it with the {@code reinforced_block} wrapper storing the original state
 *       and the tier's hit budget.</li>
 *   <li><b>Absorb</b> — every time a player completes a full mining cycle on a
 *       reinforced block, {@link BlockEvent.BreakEvent} fires first: one hit is
 *       consumed (crack reset, metal clang, sparks) and the break is cancelled.
 *       On the final hit the event is allowed through and the original block's
 *       loot drops.</li>
 * </ul>
 *
 * Creative players break reinforced blocks instantly (and nothing drops), and
 * blocks that cannot be represented (air, fluids, unbreakable blocks, blocks
 * with block entities such as chests/machines) cannot be reinforced at all.
 */
@EventBusSubscriber(modid = AlyrionCore.MODID)
public class ReinforcementEvents {

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        BlockState state = event.getState();
        if (!(state.getBlock() instanceof ReinforcedBlock)) {
            return;
        }
        Player player = event.getPlayer();
        if (player.getAbilities().instabuild) {
            return; // creative: instant break, vanilla no-drop behavior
        }

        LevelAccessor level = event.getLevel();
        BlockPos pos = event.getPos();
        if (!(level.getBlockEntity(pos) instanceof ReinforcedBlockEntity be)) {
            return; // no block entity (desync): let the block break, nothing drops
        }

        int hits = be.getHitsRemaining();
        if (hits > 1) {
            be.setHitsRemaining(hits - 1);
            // Advance the crack stage on the BE ONLY — the blockstate and the
            // block are never touched, so nothing can be "replaced".
            int newStage = ReinforcedBlock.crackStageFor(
                    state.getValue(ReinforcedBlock.TIER).getHits(), hits - 1);
            be.setCrackStage(newStage);
            be.setChanged();
            event.setCanceled(true);
            AlyrionCore.LOGGER.info("[reinforce] hit absorbed at {} hits {} stage {}", pos, hits - 1, newStage);

            ReinforcementTier tier = state.getValue(ReinforcedBlock.TIER);
            level.playSound(null, pos, tier.getHitSound(), SoundSource.BLOCKS, 1.0F, 0.9F);
            if (level instanceof ServerLevel serverLevel) {
                // One tick after the break event (past the client's destroy
                // prediction) re-push the BE data so the client's re-created
                // empty BE gets the original state + new crack stage back.
                serverLevel.scheduleTick(pos, ModBlocks.REINFORCED_BLOCK.get(), 2);
                serverLevel.sendParticles(ParticleTypes.CRIT,
                        pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5,
                        8, 0.25, 0.25, 0.25, 0.05);
            }
            // Clear the crack overlay (also covers the server-side "delayed
            // destroy" path, which never resets it on its own) so the next
            // mining cycle starts fresh while the player keeps holding.
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(
                        new ClientboundBlockDestructionPacket(serverPlayer.getId(), pos, -1));
            }
        }
        // Final hit: let the break proceed; ReinforcedBlock.playerDestroy drops
        // the original block's loot (no reinforced block, no plates).
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        ReinforcementTier tier = ReinforcementTier.fromItem(stack.getItem());
        if (tier == null) {
            return;
        }

        Player player = event.getEntity();
        if (player.isSpectator()) {
            return;
        }

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState target = level.getBlockState(pos);
        if (!canReinforce(level, pos, target, player)) {
            return;
        }

        // Hammer-swing while bolting the plate on (visible to other players
        // through the server's swing broadcast).
        player.swing(event.getHand());

        if (event.getSide().isServer()) {
            BlockState reinforced = ModBlocks.REINFORCED_BLOCK.get().defaultBlockState()
                    .setValue(ReinforcedBlock.TIER, tier);
            level.setBlock(pos, reinforced, 3);
            if (level.getBlockEntity(pos) instanceof ReinforcedBlockEntity be) {
                be.setOriginalState(target);
                be.setHitsRemaining(tier.getHits());
                be.setChanged();
            }
            // Re-broadcast so every client receives the final block entity data.
            level.sendBlockUpdated(pos, reinforced, reinforced, 3);
            level.playSound(null, pos, tier.getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.CRIT,
                        pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                        10, 0.3, 0.3, 0.3, 0.05);
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        // Cancel on both sides so the target block's own interaction (levers,
        // buttons, GUIs...) never runs while bolting a plate on.
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
    }

    /**
     * What may be reinforced: any breakable, non-fluid block without a block
     * entity (chests/machines keep their data and are excluded so they are
     * never destroyed by the replacement), that is not already reinforced.
     */
    private static boolean canReinforce(Level level, BlockPos pos, BlockState target, Player player) {
        if (target.isAir() || !target.getFluidState().isEmpty()) {
            return false;
        }
        if (target.getBlock() instanceof ReinforcedBlock || target.getBlock() instanceof GameMasterBlock) {
            return false;
        }
        if (target.getDestroySpeed(level, pos) < 0.0F) {
            return false; // unbreakable: bedrock, barrier, ...
        }
        if (level.getBlockEntity(pos) != null) {
            return false;
        }
        return level.mayInteract(player, pos);
    }
}
