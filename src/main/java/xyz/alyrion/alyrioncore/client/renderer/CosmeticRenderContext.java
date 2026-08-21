package xyz.alyrion.alyrioncore.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Everything a {@link CosmeticRenderer} may need while rendering an equipped
 * cosmetic on a player. Carries the full vanilla render-layer argument set so
 * any cosmetic kind (cape physics, orbiting models, particle trails, ...) can
 * implement its own rendering without the dispatch layer knowing about it.
 */
public record CosmeticRenderContext(
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        AbstractClientPlayer player,
        float limbSwing,
        float limbSwingAmount,
        float partialTick,
        float ageInTicks,
        float netHeadYaw,
        float headPitch,
        PlayerModel<AbstractClientPlayer> parentModel
) {
}
