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
        // Exactly vanilla's skull-item path (SkullBlockRenderer.renderSkull with
        // direction=null): ItemRenderer has already translated -0.5 so the origin
        // is the cell center; translate(+0.5,+0,+0.5) cancels its horizontal half,
        // scale(-1,-1,1) bridges entity-model Y-down space, then the model is
        // centered vertically (hat spans y -9.5..+3.5 px, center -3).
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        // Post-flip, display_y = -model_y - t. The hat spans model y -9.5..+3.5
        // px (center -3.25), so t = +3.25/16 puts its center exactly on the
        // cell origin. This is the ONLY vertical correction — the model JSON
        // carries no translations, so nothing fights it. Nudge in 1/16 steps.
        poseStack.translate(0.0F, 3.25F / 16.0F, 0.0F);

        UshankaModel model = UshankaModel.getInstance();
        // Render ONLY the head part (the hat) — the shared HumanoidModel also
        // carries body/arm/leg cubes that the armor layer hides every frame.
        model.setAllVisible(false);
        model.head.visible = true;
        // Neutral pose, rotated 180 like vanilla so the brow flap faces the camera
        // (the armor layer leaves head rotation set from the last worn player).
        model.head.xRot = 0.0F;
        model.head.yRot = (float) Math.PI;
        model.head.zRot = 0.0F;
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(UshankaItem.TEXTURE));
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay);

        poseStack.popPose();
    }
}
