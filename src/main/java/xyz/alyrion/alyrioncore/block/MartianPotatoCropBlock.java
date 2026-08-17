package xyz.alyrion.alyrioncore.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import xyz.alyrion.alyrioncore.compat.VacuumAtmosphere;
import xyz.alyrion.alyrioncore.registry.ModBlocks;
import xyz.alyrion.alyrioncore.registry.ModItems;
import xyz.alyrion.alyrioncore.world.habitat.HabitatSealManager;

public class MartianPotatoCropBlock extends CropBlock {

    public static final MapCodec<MartianPotatoCropBlock> CODEC = simpleCodec(MartianPotatoCropBlock::new);

    public MartianPotatoCropBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<MartianPotatoCropBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(ModBlocks.REGOLITH_FARMLAND.get()) || state.is(Blocks.FARMLAND);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.MARTIAN_POTATO.get();
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // On Mars, crops strictly require a pressurized sealed greenhouse and heating!
        if (VacuumAtmosphere.isVacuum(level, pos.getY())) {
            boolean isSealed = HabitatSealManager.isPositionSealed(level, pos);
            int lightLevel = level.getRawBrightness(pos, 0);

            if (!isSealed) {
                // Exposed to freezing vacuum: chance to wither into dead bush
                if (random.nextInt(12) == 0) {
                    level.setBlockAndUpdate(pos, Blocks.DEAD_BUSH.defaultBlockState());
                }
                return; // Growth halted in vacuum
            }

            if (lightLevel < 9) {
                return; // Growth halted without artificial greenhouse light/heating
            }
        }

        super.randomTick(state, level, pos, random);
    }
}
