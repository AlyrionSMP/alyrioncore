package xyz.alyrion.alyrioncore.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import xyz.alyrion.alyrioncore.cosmetics.CapeDefinition;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticsManager;
import xyz.alyrion.alyrioncore.network.CosmeticNetworking;

public class AlyrionCapeLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public AlyrionCapeLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            AbstractClientPlayer player,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        if (player.isInvisible() || !player.isModelPartShown(PlayerModelPart.CAPE)) {
            return;
        }

        ItemStack chestItem = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestItem.is(Items.ELYTRA)) {
            return;
        }

        CapeDefinition capeDef = null;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && player.getUUID().equals(mc.player.getUUID())) {
            capeDef = CosmeticsManager.get().getEquippedCape();
        } else {
            String syncedCapeId = CosmeticNetworking.getClientPlayerCape(player.getUUID());
            if (syncedCapeId != null) {
                capeDef = CapeDefinition.fromId(syncedCapeId);
            }
        }

        if (capeDef == null) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 0.125F);

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

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entitySolid(capeDef.getTextureLocation()));
        this.getParentModel().renderCloak(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }
}
