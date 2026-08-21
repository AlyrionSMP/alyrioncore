package xyz.alyrion.alyrioncore.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import xyz.alyrion.alyrioncore.block.RegolithFarmlandBlock;
import xyz.alyrion.alyrioncore.registry.ModBlocks;
import xyz.alyrion.alyrioncore.world.farmland.FarmlandFertilizerSavedData;

import java.util.List;

public class FertilizerItem extends Item {

    public FertilizerItem(Properties properties) {
        super(properties);
    }

    public static boolean isFarmland(BlockState state) {
        if (state == null) return false;
        return state.is(Blocks.FARMLAND)
                || state.is(ModBlocks.REGOLITH_FARMLAND.get())
                || state.getBlock() instanceof FarmBlock
                || state.getBlock() instanceof RegolithFarmlandBlock;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);

        BlockPos targetFarmlandPos = null;
        if (isFarmland(clickedState)) {
            targetFarmlandPos = clickedPos;
        } else if (isFarmland(level.getBlockState(clickedPos.below()))) {
            // Player clicked on a crop or block directly above farmland
            targetFarmlandPos = clickedPos.below();
        }

        if (targetFarmlandPos == null) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        FarmlandFertilizerSavedData data = FarmlandFertilizerSavedData.get(serverLevel);
        Player player = context.getPlayer();

        if (data.isFertilized(targetFarmlandPos)) {
            if (player != null) {
                player.displayClientMessage(
                        Component.translatable("message.alyrioncore.farmland_already_fertilized"),
                        true
                );
            }
            return InteractionResult.FAIL;
        }

        // Apply fertilizer
        data.setFertilized(targetFarmlandPos, true);

        // Visual and auditory feedback
        serverLevel.sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                targetFarmlandPos.getX() + 0.5,
                targetFarmlandPos.getY() + 1.0,
                targetFarmlandPos.getZ() + 0.5,
                15,
                0.35,
                0.15,
                0.35,
                0.02
        );

        serverLevel.playSound(
                null,
                targetFarmlandPos,
                SoundEvents.BONE_MEAL_USE,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );

        if (player != null) {
            if (!player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
            player.displayClientMessage(
                    Component.translatable("message.alyrioncore.fertilizer_applied"),
                    true
            );
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.alyrioncore.fertilizer.tooltip_1"));
        tooltipComponents.add(Component.translatable("item.alyrioncore.fertilizer.tooltip_2"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
