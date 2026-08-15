package xyz.alyrion.alyrioncore.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticsManager;
import xyz.alyrion.alyrioncore.cosmetics.PetDefinition;
import xyz.alyrion.alyrioncore.network.CosmeticNetworking;

/**
 * Renders the equipped pet orbiting above the player's head. For the local
 * player the equipped pet comes from {@link CosmeticsManager}; for other
 * players it comes from the {@code S2CSyncPetPayload} the server broadcast.
 */
public class SatellitePetLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

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

    private final ModelPart satellite;
    private final ModelPart light;

    public SatellitePetLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
        ModelPart root = Minecraft.getInstance().getEntityModels().bakeLayer(SatellitePetModel.LAYER);
        this.satellite = root.getChild("satellite");
        this.light = root.getChild("light");
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
        if (player.isInvisible()) {
            return;
        }

        PetDefinition pet = null;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && player.getUUID().equals(mc.player.getUUID())) {
            pet = CosmeticsManager.get().getEquippedPet();
        } else {
            String syncedPetId = CosmeticNetworking.getClientPlayerPet(player.getUUID());
            if (syncedPetId != null) {
                pet = PetDefinition.fromId(syncedPetId);
            }
        }

        if (pet == null) {
            return;
        }

        float time = ageInTicks + partialTick;

        // Orbit around the head, with a gentle vertical bob.
        float angle = time * ORBIT_SPEED;
        float bob = Mth.sin(time * 0.25F) * ORBIT_BOB / 16.0F;
        float orbitX = Mth.sin(angle) * ORBIT_RADIUS / 16.0F;
        float orbitZ = Mth.cos(angle) * ORBIT_RADIUS / 16.0F;

        poseStack.pushPose();
        poseStack.translate(orbitX, ORBIT_HEIGHT + bob, orbitZ);
        poseStack.scale(SCALE, SCALE, SCALE);

        // Keep the dish angled toward the player, then add a lazy self-spin and wobble.
        poseStack.mulPose(Axis.YP.rotationDegrees(-angle * (180.0F / (float) Math.PI)));
        poseStack.mulPose(Axis.YP.rotationDegrees(time * SELF_SPIN_SPEED));
        poseStack.mulPose(Axis.XP.rotationDegrees(12.0F + Mth.sin(time * 0.4F) * 4.0F));

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutout(pet.getTextureLocation()));
        this.satellite.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);

        // Blinking beacon light on the antenna.
        float blinkPhase = time % BLINK_CYCLE;
        boolean blinkOn = blinkPhase < 3.0F;
        if (blinkOn) {
            float pulse = 1.0F - (blinkPhase / 3.0F);
            int color = (int) (0xFF000000 | (0xFF - (int) (pulse * 90)) << 16 | 0xFF << 8 | (int) (0xFF - (int) (pulse * 60)));
            this.light.render(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, color);
        } else {
            this.light.render(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        }

        poseStack.popPose();
    }
}
