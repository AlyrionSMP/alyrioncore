package xyz.alyrion.alyrioncore.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class MartianSandBlock extends FallingBlock {
    public static final MapCodec<MartianSandBlock> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.INT.fieldOf("dust_color").forGetter(MartianSandBlock::getDustColor),
                    propertiesCodec()
            ).apply(instance, MartianSandBlock::new)
    );

    private final int dustColor;

    public MartianSandBlock(int dustColor, BlockBehaviour.Properties properties) {
        super(properties);
        this.dustColor = dustColor;
    }

    @Override
    protected MapCodec<? extends FallingBlock> codec() {
        return CODEC;
    }

    public int getDustColor() {
        return this.dustColor;
    }
}
