package xyz.alyrion.alyrioncore.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class DryIceShardItem extends Item {
    public DryIceShardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();

        if (state.is(Blocks.WATER)) {
            level.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
            level.playSound(null, pos, SoundEvents.SNOW_GOLEM_HURT, SoundSource.BLOCKS, 1.0F, 1.2F);
            if (player != null && !player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        level.playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_HURT_FREEZE,
                SoundSource.PLAYERS,
                0.8F,
                1.4F
        );

        if (!level.isClientSide) {
            // Flash-freeze blast in 5-block radius around the player looking direction
            AABB area = player.getBoundingBox().inflate(4.0);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());

            for (LivingEntity target : targets) {
                target.setTicksFrozen(target.getTicksRequiredToFreeze() + 160);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
                target.hurt(level.damageSources().freeze(), 3.0F);
            }

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.SNOWFLAKE,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        30, 1.2, 0.5, 1.2, 0.05
                );
            }

            if (!player.getAbilities().instabuild) {
                itemstack.shrink(1);
            }
        }

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}
