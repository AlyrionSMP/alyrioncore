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
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import xyz.alyrion.alyrioncore.AlyrionCore;
import xyz.alyrion.alyrioncore.block.AirlockBlock;
import xyz.alyrion.alyrioncore.block.AirlockBlockEntity;

/**
 * Renders the animated pressurized-airlock hatch leaf: a heavy armored door that
 * swings open/closed on its hinge with a smooth pneumatic ease, a translucent
 * viewport window, and a blinking status LED (green = sealed, red = venting).
 */
public class AirlockBlockEntityRenderer implements BlockEntityRenderer<AirlockBlockEntity> {

    public static final ModelResourceLocation LEAF_BOTTOM = model("block/airlock_leaf_bottom");
    public static final ModelResourceLocation LEAF_TOP = model("block/airlock_leaf_top");
    public static final ModelResourceLocation WINDOW = model("block/airlock_window");
    public static final ModelResourceLocation LED_GREEN = model("block/airlock_led_green");
    public static final ModelResourceLocation LED_RED = model("block/airlock_led_red");

    private static final float OPEN_ANGLE = 100.0F;

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
        DoorHingeSide hinge = state.getValue(DoorBlock.HINGE);
        Direction facing = state.getValue(DoorBlock.FACING);

        float yaw = switch (facing) {
            case SOUTH -> 90.0F;
            case WEST -> 180.0F;
            case NORTH -> 270.0F;
            default -> 0.0F; // EAST
        };

        float eased = blockEntity.getEasedProgress(partialTick);
        float swing = OPEN_ANGLE * eased * (hinge == DoorHingeSide.LEFT ? 1.0F : -1.0F);
        // Hinge pivot, in the authored (facing=east) frame: front face, on the leaf's hinged edge.
        float pivotZ = hinge == DoorHingeSide.LEFT ? 2.0F / 16.0F : 14.0F / 16.0F;

        var modelManager = Minecraft.getInstance().getModelManager();
        BakedModel leaf = modelManager.getModel(upper ? LEAF_TOP : LEAF_BOTTOM);
        ModelBlockRenderer renderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();

        pose.pushPose();
        // Match the blockstate's facing rotation (rotateY(-yaw) about the block center).
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));
        pose.translate(-0.5, -0.5, -0.5);
        // Swing the leaf around its vertical hinge.
        pose.translate(1.0, 0.0, pivotZ);
        pose.mulPose(Axis.YP.rotationDegrees(swing));
        pose.translate(-1.0, 0.0, -pivotZ);

        VertexConsumer leafBuffer = buffers.getBuffer(RenderType.cutout());
        renderer.renderModel(pose.last(), leafBuffer, state, leaf, 1.0F, 1.0F, 1.0F, light, overlay);

        if (upper) {
            VertexConsumer windowBuffer = buffers.getBuffer(RenderType.translucent());
            renderer.renderModel(pose.last(), windowBuffer, state, modelManager.getModel(WINDOW), 1.0F, 1.0F, 1.0F, light, overlay);
        }
        pose.popPose();

        // Status LED on the upper header (always visible, even while the leaf is moving).
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
}
