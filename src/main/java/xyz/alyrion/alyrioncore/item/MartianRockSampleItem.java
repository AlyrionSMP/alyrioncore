package xyz.alyrion.alyrioncore.item;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import xyz.alyrion.alyrioncore.registry.ModBlocks;
import xyz.alyrion.alyrioncore.registry.ModItems;

public class MartianRockSampleItem extends Item {
    public MartianRockSampleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        level.playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_BREAK,
                SoundSource.PLAYERS,
                0.9F,
                1.1F + (level.random.nextFloat() * 0.3F)
        );

        if (!level.isClientSide) {
            RandomSource random = level.random;
            int mineralCount = 1 + random.nextInt(3); // 1 to 3 minerals

            for (int i = 0; i < mineralCount; i++) {
                int roll = random.nextInt(100);
                ItemStack yield;
                if (roll < 35) {
                    yield = new ItemStack(ModItems.HEMATITE_NODULE.get(), 1 + random.nextInt(2));
                } else if (roll < 60) {
                    yield = new ItemStack(ModItems.SULFUR_DUST.get(), 1 + random.nextInt(2));
                } else if (roll < 80) {
                    yield = new ItemStack(ModItems.RAW_MARTIAN_COPPER.get(), 1);
                } else if (roll < 93) {
                    yield = new ItemStack(ModItems.RAW_METEORIC_IRON.get(), 1);
                } else {
                    yield = new ItemStack(ModItems.OLIVINE_GEM.get(), 1);
                }

                if (!player.getInventory().add(yield)) {
                    player.drop(yield, false);
                }
            }

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, ModBlocks.STRATIFIED_MARTIAN_STONE.get().defaultBlockState()),
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        15, 0.2, 0.2, 0.2, 0.1
                );
            }

            if (!player.getAbilities().instabuild) {
                itemstack.shrink(1);
            }
        }

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}
