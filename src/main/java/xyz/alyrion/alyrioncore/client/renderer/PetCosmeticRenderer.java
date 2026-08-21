package xyz.alyrion.alyrioncore.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticDefinition;

import java.util.HashMap;
import java.util.Map;

/**
 * Renders equipped pets orbiting above the player's head.
 *
 * Pets are dispatched by cosmetic id through {@link #visuals}: the satellite
 * pet uses the {@link SatellitePetModel} box model; future pets simply register
 * their own baked model parts here without touching the framework.
 */
public class PetCosmeticRenderer implements CosmeticRenderer {

    /** Orbital period in ticks for a full loop around the head. */
    private static final float ORBIT_SPEED = 0.15F;
    /** Orbit radius in model units (16 units = 1 block). */
    private static final float ORBIT_RADIUS = 15.0F;
    /**
     * Orbit centre height in BLOCKS, in layer space. The player renderer bakes a
     * Y-flip and a -1.501 translate into the poseStack, so world-up is -Y here:
     * world_y = 1.501 - layer_y. Head top sits at layer_y ≈ -0.5, so an orbit
     * centre just above the head (~1.85 blocks in world) needs layer_y ≈ -0.35;
     * half a block higher (~2.35 blocks in world) needs layer_y ≈ -0.85.
     */
    private static final float ORBIT_HEIGHT = -0.85F;
    /** Vertical bob amplitude in model units. */
    private static final float ORBIT_BOB = 1.5F;
    /** Scale factor applied to the whole satellite. */
    private static final float SCALE = 0.6F;
    /** Spin of the satellite around its own axis, degrees per second. */
    private static final float SELF_SPIN_SPEED = 8.0F;
    /** Blink cycle length in ticks. */
    private static final float BLINK_CYCLE = 8.0F;

    /** Baked model parts per pet cosmetic id. */
    private record PetVisual(ModelPart satellite, ModelPart light) {
    }

    private final Map<String, PetVisual> visuals = new HashMap<>();

    public PetCosmeticRenderer() {
        ModelPart root = Minecraft.getInstance().getEntityModels().bakeLayer(SatellitePetModel.LAYER);
        visuals.put("satellite", new PetVisual(root.getChild("satellite"), root.getChild("light")));
    }

    @Override
    public void render(CosmeticRenderContext ctx, CosmeticDefinition cosmetic) {
        PetVisual visual = visuals.get(cosmetic.getId());
        if (visual == null) {
            return; // pet id without a registered model: nothing to draw
        }

        var player = ctx.player();
        if (player.isInvisible()) {
            return;
        }

        float time = ctx.ageInTicks() + ctx.partialTick();

        // Orbit around the head, with a gentle vertical bob.
        float angle = time * ORBIT_SPEED;
        float bob = Mth.sin(time * 0.25F) * ORBIT_BOB / 16.0F;
        float orbitX = Mth.sin(angle) * ORBIT_RADIUS / 16.0F;
        float orbitZ = Mth.cos(angle) * ORBIT_RADIUS / 16.0F;

        PoseStack poseStack = ctx.poseStack();
        poseStack.pushPose();
        poseStack.translate(orbitX, ORBIT_HEIGHT + bob, orbitZ);
        poseStack.scale(SCALE, SCALE, SCALE);

        // Keep the dish angled toward the player, then add a lazy self-spin and wobble.
        poseStack.mulPose(Axis.YP.rotationDegrees(-angle * (180.0F / (float) Math.PI)));
        poseStack.mulPose(Axis.YP.rotationDegrees(time * SELF_SPIN_SPEED));
        poseStack.mulPose(Axis.XP.rotationDegrees(12.0F + Mth.sin(time * 0.4F) * 4.0F));

        VertexConsumer consumer = ctx.buffer().getBuffer(RenderType.entityCutout(cosmetic.getTextureLocation()));
        visual.satellite().render(poseStack, consumer, ctx.packedLight(), OverlayTexture.NO_OVERLAY);

        // Blinking beacon light on the antenna.
        float blinkPhase = time % BLINK_CYCLE;
        boolean blinkOn = blinkPhase < 3.0F;
        if (blinkOn) {
            float pulse = 1.0F - (blinkPhase / 3.0F);
            int color = (int) (0xFF000000 | (0xFF - (int) (pulse * 90)) << 16 | 0xFF << 8 | (int) (0xFF - (int) (pulse * 60)));
            visual.light().render(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, color);
        } else {
            visual.light().render(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        }

        poseStack.popPose();
    }

    /** 3D model preview scaled down to serve as the store list icon. */
    @Override
    public void drawStoreIcon(GuiGraphics guiGraphics, CosmeticDefinition cosmetic, int x, int y, int size, long tick) {
        PetVisual visual = visuals.get(cosmetic.getId());
        if (visual == null) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        int centerX = x + size / 2;
        int centerY = y + size / 2;
        // The model spans 32 units wide (solar wings) and -4..+10 tall, and
        // ModelPart geometry renders at 16 units per GUI pixel — so at scale 1
        // the satellite was just 2 px. Scale to fill the slot instead.
        float scale = size / 2.0F;

        long gameTick = mc.level != null ? mc.level.getGameTime() : tick;
        float spin = (gameTick % 240L) / 240.0F * 360.0F;

        guiGraphics.drawManaged(() -> {
            PoseStack pose = guiGraphics.pose();
            pose.pushPose();
            // Re-center vertically: the model's midpoint sits at ~+3.25 units.
            pose.translate(centerX, centerY - 3.25F * scale / 16.0F, 120.0F);
            pose.scale(scale, scale, -scale);
            pose.mulPose(Axis.XP.rotationDegrees(-18.0F));
            pose.mulPose(Axis.YP.rotationDegrees(spin));

            VertexConsumer consumer = guiGraphics.bufferSource().getBuffer(RenderType.entityCutoutNoCull(cosmetic.getTextureLocation()));
            visual.satellite().render(pose, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

            guiGraphics.flush();
            pose.popPose();
        });
    }

    /** Spinning 3D model render for the preview panel. */
    @Override
    public void drawStorePreview(GuiGraphics guiGraphics, CosmeticDefinition cosmetic, int x, int y, int w, int h, long tick) {
        PetVisual visual = visuals.get(cosmetic.getId());
        if (visual == null) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        int centerX = x + w / 2;
        int centerY = y + h / 2;
        int scale = Math.max(14, Math.min(30, h / 4));

        long gameTick = mc.level != null ? mc.level.getGameTime() : tick;
        float spin = (gameTick % 200L) / 200.0F * 360.0F;

        guiGraphics.drawManaged(() -> {
            PoseStack pose = guiGraphics.pose();
            pose.pushPose();
            pose.translate(centerX, centerY, 120.0F);
            pose.scale(scale, scale, scale);
            pose.mulPose(Axis.XP.rotationDegrees(-18.0F));
            pose.mulPose(Axis.YP.rotationDegrees(spin));

            VertexConsumer consumer = guiGraphics.bufferSource().getBuffer(RenderType.entityCutoutNoCull(cosmetic.getTextureLocation()));
            visual.satellite().render(pose, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

            guiGraphics.flush();
            pose.popPose();
        });
    }
}
