package xyz.alyrion.alyrioncore.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class DryIceBlock extends Block {
    public static final MapCodec<DryIceBlock> CODEC = simpleCodec(DryIceBlock::new);

    public DryIceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Scientifically, solid CO2 (dry ice) sublimes directly into gas
        if (random.nextInt(3) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 1.0D + random.nextDouble() * 0.1D;
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.CLOUD, x, y, z, 0.0D, 0.02D, 0.0D);
        }
        if (random.nextInt(5) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.SNOWFLAKE, x, y, z, 0.0D, -0.01D, 0.0D);
        }
    }
}
