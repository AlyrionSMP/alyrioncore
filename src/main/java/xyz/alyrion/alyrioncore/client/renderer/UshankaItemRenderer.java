package xyz.alyrion.alyrioncore.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import xyz.alyrion.alyrioncore.item.UshankaItem;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Renders the ushanka as its real 3D armor model everywhere the item appears
 * (inventory, hotbar, ground, item frames) — the same technique vanilla uses
 * for skulls and shields — instead of a flat 16x16 sprite.
 *
 * The model is drawn with the same texture the armor layer uses on the player,
 * tilted slightly so the crown faces the viewer.
 */
@OnlyIn(Dist.CLIENT)
public class UshankaItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static UshankaItemRenderer instance;

    public UshankaItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    public static UshankaItemRenderer getInstance() {
        if (instance == null) {
            instance = new UshankaItemRenderer(null, null);
        }
        return instance;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext,
                             PoseStack poseStack, MultiBufferSource buffer,
                             int packedLight, int packedOverlay) {
        poseStack.pushPose();
        // Vanilla's skull-item path (SkullBlockRenderer.renderSkull with direction=null):
        // ItemRenderer has already applied display transforms and translated -0.5 to center the cell.
        // translate(+0.5, 0, +0.5) and scale(-1, -1, 1) align entity-model space.
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.scale(-1.0F, -1.0F, 1.0F);

        UshankaModel model = UshankaModel.getItemInstance();
        model.young = false;
        model.setAllVisible(false);
        model.head.visible = true;
        model.head.resetPose();
        // Neutral pose, rotated 180 so the brow flap faces the camera
        model.head.xRot = 0.0F;
        model.head.yRot = (float) Math.PI;
        model.head.zRot = 0.0F;
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(UshankaItem.TEXTURE));
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay);

        poseStack.popPose();
    }
}
