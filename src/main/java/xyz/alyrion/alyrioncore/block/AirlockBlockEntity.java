package xyz.alyrion.alyrioncore.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import xyz.alyrion.alyrioncore.registry.ModBlockEntities;

/**
 * Drives the client-side open/close animation for the pressurized airlock.
 * The hatch leaf is rendered by {@link xyz.alyrion.alyrioncore.client.renderer.AirlockBlockEntityRenderer}
 * and opens like a real airlock: it pops out of the frame, then glides sideways.
 */
public class AirlockBlockEntity extends BlockEntity {

    /** Progress per tick while opening/closing (0.0 = fully closed, 1.0 = fully open).
     *  Deliberately slow — a pressurized airlock glides, it does not swing. */
    private static final float ANIM_SPEED = 0.08F;

    private float animProgress;

    public AirlockBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AIRLOCK.get(), pos, state);
        this.animProgress = state.getValue(DoorBlock.OPEN) ? 1.0F : 0.0F;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AirlockBlockEntity be) {
        if (!level.isClientSide) {
            return;
        }
        float target = state.getValue(DoorBlock.OPEN) ? 1.0F : 0.0F;
        if (be.animProgress < target) {
            be.animProgress = Math.min(target, be.animProgress + ANIM_SPEED);
        } else if (be.animProgress > target) {
            be.animProgress = Math.max(target, be.animProgress - ANIM_SPEED);
        }
    }

    /** Raw animation progress, extrapolated toward the current target for smooth frame interpolation. */
    public float getAnimProgress(float partialTick) {
        boolean open = this.getBlockState().getValue(DoorBlock.OPEN);
        float target = open ? 1.0F : 0.0F;
        float p = animProgress;
        if (p < target) {
            p = Math.min(target, p + ANIM_SPEED * partialTick);
        } else if (p > target) {
            p = Math.max(target, p - ANIM_SPEED * partialTick);
        }
        return p;
    }

    /** Smoothstep-eased animation progress for a natural pneumatic swing. */
    public float getEasedProgress(float partialTick) {
        float t = getAnimProgress(partialTick);
        return t * t * (3.0F - 2.0F * t);
    }
}
