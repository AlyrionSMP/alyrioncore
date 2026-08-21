package xyz.alyrion.alyrioncore.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import xyz.alyrion.alyrioncore.AlyrionCore;
import xyz.alyrion.alyrioncore.block.ReinforcedBlockEntity;

/**
 * Draws the ORIGINAL block model inside the reinforced block's plate frame,
 * plus the damage crack overlay.
 *
 * The frame is the blockstate model (chunk-culled, so plates only show on
 * air-facing sides); this renderer supplies the block's own look so the
 * reinforced block is never just a new texture — it is the original block
 * wrapped in plates. The crack overlay is one of 8 stage models (registered as
 * additional models) chosen from the block entity's crack stage, which the
 * server updates on the BE ONLY — the block itself is never replaced.
 */
public class ReinforcedBlockEntityRenderer implements BlockEntityRenderer<ReinforcedBlockEntity> {

    private static final ModelResourceLocation[] CRACK_MODELS = new ModelResourceLocation[8];

    static {
        for (int i = 0; i < 8; i++) {
            CRACK_MODELS[i] = ModelResourceLocation.standalone(
                    ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, "block/reinforced_crack_" + i));
        }
    }

    private final RandomSource random = RandomSource.create();

    public ReinforcedBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    /** The additional model to register for a crack stage (0..7). */
    public static ModelResourceLocation crackModel(int stage) {
        return CRACK_MODELS[Mth.clamp(stage, 0, 7)];
    }

    @Override
    public void render(ReinforcedBlockEntity blockEntity, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light, int overlay) {
        BlockState original = blockEntity.getOriginalState();
        if (original.isAir() || !(blockEntity.getLevel() instanceof ClientLevel level)) {
            return;
        }

        // The protected block's own model, with world culling + lighting; the
        // pose is already at the block position, quads are in 0..16 block space.
        RenderType renderType = ItemBlockRenderTypes.getRenderType(original, false);
        VertexConsumer consumer = buffers.getBuffer(renderType);
        pose.pushPose();
        Minecraft.getInstance().getBlockRenderer()
                .renderBatched(original, blockEntity.getBlockPos(), level, pose, consumer, true, this.random);
        pose.popPose();

        // Cumulative damage crack overlay (server-synced stage 0..7).
        int stage = Mth.clamp(blockEntity.getCrackStage(), 0, 7);
        if (stage > 0) {
            BakedModel crack = Minecraft.getInstance().getModelManager().getModel(CRACK_MODELS[stage]);
            BlockState state = blockEntity.getBlockState();
            VertexConsumer crackBuffer = buffers.getBuffer(RenderType.cutout());
            Minecraft.getInstance().getBlockRenderer().getModelRenderer()
                    .renderModel(pose.last(), crackBuffer, state, crack, 1.0F, 1.0F, 1.0F, light, overlay);
        }
    }
}
