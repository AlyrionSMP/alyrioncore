package xyz.alyrion.alyrioncore.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import xyz.alyrion.alyrioncore.AlyrionCore;
import xyz.alyrion.alyrioncore.block.AirlockBlock;
import xyz.alyrion.alyrioncore.block.AirlockBlockEntity;

/**
 * Renders the animated pressurized-airlock hatch: a heavy armored door that
 * OPENS LIKE A REAL AIRLOCK — first it pops OUT of the frame (clearing the
 * wall so it never intersects the blocks beside the doorway), then it glides
 * sideways to the right with a smooth pneumatic ease. Closing reverses the
 * motion: slide back left, then seat back into the frame.
 *
 * The door carries a translucent viewport window; a status LED on the upper
 * header (green = sealed, red = venting) blinks while the door is moving.
 */
public class AirlockBlockEntityRenderer implements BlockEntityRenderer<AirlockBlockEntity> {

    public static final ModelResourceLocation LEAF_BOTTOM = model("block/airlock_leaf_bottom");
    public static final ModelResourceLocation LEAF_TOP = model("block/airlock_leaf_top");
    public static final ModelResourceLocation WINDOW = model("block/airlock_window");
    public static final ModelResourceLocation LED_GREEN = model("block/airlock_led_green");
    public static final ModelResourceLocation LED_RED = model("block/airlock_led_red");

    /** Fraction of the animation spent popping the door out of the frame. */
    private static final float OUT_FRACTION = 0.30F;
    /** How far the door lifts out of the frame (block units) before sliding. */
    private static final float OUT_DISTANCE = 0.65F;
    /** How far the door slides sideways (block units) to fully clear the doorway. */
    private static final float SLIDE_DISTANCE = 14.5F;

    public AirlockBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    private static ModelResourceLocation model(String path) {
        return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, path));
    }

    @Override
    public void render(AirlockBlockEntity blockEntity, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof AirlockBlock)) {
            return;
        }

        boolean upper = state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER;
        boolean open = state.getValue(DoorBlock.OPEN);
        Direction facing = state.getValue(DoorBlock.FACING);

        float yaw = switch (facing) {
            case SOUTH -> 90.0F;
            case WEST -> 180.0F;
            case NORTH -> 270.0F;
            default -> 0.0F; // EAST
        };
        // Slide to the viewer's right: the door's own +z (authored) is the right-hand
        // side for facings along Z, the left-hand side for facings along X.
        float slideSign = facing.getAxis() == Direction.Axis.Z ? 1.0F : -1.0F;

        float eased = blockEntity.getEasedProgress(partialTick);
        float progress = blockEntity.getAnimProgress(partialTick);
        // Two-phase airlock motion: pop OUT to clear the wall, then glide RIGHT.
        float outT = Mth.clamp(progress / OUT_FRACTION, 0.0F, 1.0F);
        float slideT = Mth.clamp((progress - OUT_FRACTION) / (1.0F - OUT_FRACTION), 0.0F, 1.0F);
        float out = easeOutCubic(outT) * OUT_DISTANCE;
        float slide = easeInOutCubic(slideT) * SLIDE_DISTANCE * slideSign;

        var modelManager = Minecraft.getInstance().getModelManager();
        BakedModel leaf = modelManager.getModel(upper ? LEAF_TOP : LEAF_BOTTOM);
        ModelBlockRenderer renderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();

        pose.pushPose();
        // Match the blockstate's facing rotation (rotateY(-yaw) about the block center).
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));
        pose.translate(-0.5, -0.5, -0.5);
        // Slide the door in the authored frame: out of the frame (+x), then right (+z).
        pose.translate(out, 0.0F, slide);

        VertexConsumer leafBuffer = buffers.getBuffer(RenderType.cutout());
        renderer.renderModel(pose.last(), leafBuffer, state, leaf, 1.0F, 1.0F, 1.0F, light, overlay);

        if (upper) {
            VertexConsumer windowBuffer = buffers.getBuffer(RenderType.translucent());
            renderer.renderModel(pose.last(), windowBuffer, state, modelManager.getModel(WINDOW), 1.0F, 1.0F, 1.0F, light, overlay);
        }
        pose.popPose();

        // Status LED on the upper header (always visible, even while the door is moving).
        if (upper) {
            float moving = eased > 0.05F && eased < 0.95F ? 1.0F : 0.0F;
            boolean blinkOn = (blockEntity.getLevel().getGameTime() / 3) % 2 == 0;
            if (moving == 0.0F || blinkOn) {
                BakedModel led = modelManager.getModel(open ? LED_RED : LED_GREEN);
                pose.pushPose();
                pose.translate(0.5, 0.5, 0.5);
                pose.mulPose(Axis.YP.rotationDegrees(-yaw));
                pose.translate(-0.5, -0.5, -0.5);
                VertexConsumer ledBuffer = buffers.getBuffer(RenderType.cutout());
                renderer.renderModel(pose.last(), ledBuffer, state, led, 1.0F, 1.0F, 1.0F, LightTexture.FULL_BRIGHT, overlay);
                pose.popPose();
            }
        }
    }

    /** Snappy pneumatic pop: fast out, gentle settle. */
    private static float easeOutCubic(float t) {
        return 1.0F - (1.0F - t) * (1.0F - t) * (1.0F - t);
    }

    /** Smooth symmetric glide for the sideways travel. */
    private static float easeInOutCubic(float t) {
        return t < 0.5F ? 4.0F * t * t * t : 1.0F - (float) Math.pow(-2.0F * t + 2.0F, 3.0F) / 2.0F;
    }
}
