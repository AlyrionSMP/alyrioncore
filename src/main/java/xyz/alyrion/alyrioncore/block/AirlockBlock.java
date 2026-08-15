package xyz.alyrion.alyrioncore.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.phys.BlockHitResult;

public class AirlockBlock extends DoorBlock {

    public static final MapCodec<AirlockBlock> CODEC = simpleCodec(AirlockBlock::new);

    public AirlockBlock(BlockBehaviour.Properties properties) {
        super(BlockSetType.IRON, properties);
    }

    @Override
    public MapCodec<AirlockBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        // Toggle open/closed state on right click with pneumatic airlock sound
        state = state.cycle(OPEN);
        level.setBlock(pos, state, 10);
        float pitch = state.getValue(OPEN) ? 0.85F : 1.15F;
        level.playSound(player, pos, state.getValue(OPEN) ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, pitch);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public static boolean isAirtight(BlockState state) {
        return !state.getValue(OPEN);
    }
}
