package xyz.alyrion.alyrioncore.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticDefinition;

/**
 * Renders an equipped cape with vanilla cloak physics, exactly like the old
 * {@code AlyrionCapeLayer}. Moved into the generic renderer dispatch so capes
 * are just one cosmetic type among many.
 */
public class CapeCosmeticRenderer implements CosmeticRenderer {

    @Override
    public void render(CosmeticRenderContext ctx, CosmeticDefinition cosmetic) {
        var player = ctx.player();
        if (player.isInvisible() || !player.isModelPartShown(PlayerModelPart.CAPE)) {
            return;
        }

        ItemStack chestItem = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestItem.is(Items.ELYTRA)) {
            return;
        }

        var poseStack = ctx.poseStack();
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 0.125F);

        float partialTick = ctx.partialTick();
        double d0 = Mth.lerp((double) partialTick, player.xCloakO, player.xCloak)
                - Mth.lerp((double) partialTick, player.xo, player.getX());
        double d1 = Mth.lerp((double) partialTick, player.yCloakO, player.yCloak)
                - Mth.lerp((double) partialTick, player.yo, player.getY());
        double d2 = Mth.lerp((double) partialTick, player.zCloakO, player.zCloak)
                - Mth.lerp((double) partialTick, player.zo, player.getZ());

        float bodyRot = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
        double d3 = (double) Mth.sin(bodyRot * (float) (Math.PI / 180.0));
        double d4 = (double) (-Mth.cos(bodyRot * (float) (Math.PI / 180.0)));

        float f1 = (float) d1 * 10.0F;
        f1 = Mth.clamp(f1, -6.0F, 32.0F);

        float f2 = (float) (d0 * d3 + d2 * d4) * 100.0F;
        f2 = Mth.clamp(f2, 0.0F, 150.0F);

        float f3 = (float) (d0 * d4 - d2 * d3) * 100.0F;
        f3 = Mth.clamp(f3, -20.0F, 20.0F);

        if (f2 < 0.0F) {
            f2 = 0.0F;
        }

        float f4 = Mth.lerp(partialTick, player.oBob, player.bob);
        f1 += Mth.sin(Mth.lerp(partialTick, player.walkDistO, player.walkDist) * 6.0F) * 32.0F * f4;

        if (player.isCrouching()) {
            f1 += 25.0F;
            poseStack.translate(0.0F, 0.15F, 0.0F);
        }

        poseStack.mulPose(Axis.XP.rotationDegrees(6.0F + f2 / 2.0F + f1));
        poseStack.mulPose(Axis.ZP.rotationDegrees(f3 / 2.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - f3 / 2.0F));

        VertexConsumer vertexConsumer = ctx.buffer().getBuffer(RenderType.entitySolid(cosmetic.getTextureLocation()));
        ctx.parentModel().renderCloak(poseStack, vertexConsumer, ctx.packedLight(), OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }

    /** Mini cape sprite in the store list rows (10:16 design area, like the old list icon). */
    @Override
    public void drawStoreIcon(GuiGraphics guiGraphics, CosmeticDefinition cosmetic, int x, int y, int size, long tick) {
        int iconH = size;
        int iconW = (int) (iconH * 10.0F / 16.0F);
        guiGraphics.blit(
                cosmetic.getTextureLocation(),
                x, y,
                iconW, iconH,
                12.0F, 1.0F,
                10, 16,
                64, 32
        );
    }

    /** Large cape blit for the store preview panel. */
    @Override
    public void drawStorePreview(GuiGraphics guiGraphics, CosmeticDefinition cosmetic, int x, int y, int w, int h, long tick) {
        int capeDrawH = Math.min(h, w * 16 / 10);
        int capeDrawW = (int) (capeDrawH * 10.0F / 16.0F);
        int drawX = x + (w - capeDrawW) / 2;
        int drawY = y + (h - capeDrawH) / 2;

        guiGraphics.fill(drawX - 3, drawY - 3, drawX + capeDrawW + 3, drawY + capeDrawH + 3, 0xFF000000);
        guiGraphics.renderOutline(drawX - 3, drawY - 3, capeDrawW + 6, capeDrawH + 6, 0xFF60A5FA);

        guiGraphics.blit(
                cosmetic.getTextureLocation(),
                drawX, drawY,
                capeDrawW, capeDrawH,
                12.0F, 1.0F,
                10, 16,
                64, 32
        );
    }
}
