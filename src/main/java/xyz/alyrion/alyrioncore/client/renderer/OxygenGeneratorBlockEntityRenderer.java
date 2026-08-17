package xyz.alyrion.alyrioncore.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import xyz.alyrion.alyrioncore.AlyrionCore;
import xyz.alyrion.alyrioncore.block.OxygenGeneratorBlock;
import xyz.alyrion.alyrioncore.block.OxygenGeneratorBlockEntity;

/**
 * Renders the Oxygen Generator's animated impeller: a small two-blade fan
 * spinning in front of the vent cowl. It spins while the machine has both FE
 * and water (ACTIVE=true) and stands still when either runs dry. The vent
 * center in the authored model is x=4.5/16, y=6.5/16, z=15.7/16; the spin
 * happens around the vent axis (Z).
 */
public class OxygenGeneratorBlockEntityRenderer implements BlockEntityRenderer<OxygenGeneratorBlockEntity> {

    public static final ModelResourceLocation FAN = model("block/oxygen_generator_fan");

    /** Degrees of fan rotation per tick while running (fast enough to blur). */
    private static final float SPIN_SPEED = 42.0F;

    public OxygenGeneratorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    private static ModelResourceLocation model(String path) {
        return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, path));
    }

    @Override
    public void render(OxygenGeneratorBlockEntity blockEntity, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light, int overlay) {
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof OxygenGeneratorBlock) || blockEntity.getLevel() == null) {
            return;
        }

        boolean active = state.getValue(OxygenGeneratorBlock.ACTIVE);
        float spin = active ? (blockEntity.getLevel().getGameTime() + partialTick) * SPIN_SPEED % 360.0F : 0.0F;
        float yaw = state.getValue(OxygenGeneratorBlock.FACING).get2DDataValue() * 90.0F;

        var modelManager = Minecraft.getInstance().getModelManager();
        BakedModel fan = modelManager.getModel(FAN);
        ModelBlockRenderer renderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();

        pose.pushPose();
        // Match the blockstate's facing rotation (model authored front = south/+z).
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));
        // Spin the impeller around the vent axis (block-space pivot: x=4.5, y=6.5, z=15.7).
        pose.translate(4.5 / 16.0 - 0.5, 6.5 / 16.0 - 0.5, 15.7 / 16.0 - 0.5);
        pose.mulPose(Axis.ZP.rotationDegrees(spin));
        pose.translate(-(4.5 / 16.0 - 0.5), -(6.5 / 16.0 - 0.5), -(15.7 / 16.0 - 0.5));
        pose.translate(-0.5, -0.5, -0.5);

        VertexConsumer buffer = buffers.getBuffer(RenderType.cutout());
        renderer.renderModel(pose.last(), buffer, state, fan, 1.0F, 1.0F, 1.0F, light, overlay);
        pose.popPose();
    }
}
