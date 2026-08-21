package xyz.alyrion.alyrioncore.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import xyz.alyrion.alyrioncore.AlyrionCore;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticDefinition;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticsManager;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticsRegistry;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticType;
import xyz.alyrion.alyrioncore.network.CosmeticNetworking;

/**
 * One generic player render layer that draws every equipped cosmetic of every
 * type by dispatching to the type's {@link CosmeticRenderer}. Replaces the old
 * per-type layers ({@code AlyrionCapeLayer}, {@code SatellitePetLayer}) — a new
 * cosmetic type needs no new layer.
 */
public class CosmeticRenderLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    /**
     * Store-preview override: while the wardrobe renders the local player it
     * temporarily sets what each slot should show, so a selected (not yet
     * equipped) cosmetic appears on the model. Render-thread only.
     */
    private static final java.util.EnumMap<CosmeticType, CosmeticDefinition> PREVIEW = new java.util.EnumMap<>(CosmeticType.class);

    public static void setPreview(CosmeticType type, CosmeticDefinition def) {
        if (type == null) return;
        if (def == null) {
            PREVIEW.remove(type);
        } else {
            PREVIEW.put(type, def);
        }
    }

    public static void clearPreview() {
        PREVIEW.clear();
    }

    /** True while the wardrobe is rendering the local player with a preview
     *  override for the given type (used e.g. by the trail renderer to switch
     *  from world particles to its in-GUI particle draw). */
    public static boolean isPreviewing(CosmeticType type) {
        return PREVIEW.containsKey(type);
    }

    public CosmeticRenderLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
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
        if (player.isInvisible()) {
            return;
        }

        CosmeticRenderContext ctx = new CosmeticRenderContext(
                poseStack, buffer, packedLight, player,
                limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch,
                this.getParentModel()
        );

        for (CosmeticType type : CosmeticType.values()) {
            CosmeticDefinition cosmetic = getEquipped(player, type);
            if (cosmetic == null) {
                continue;
            }
            CosmeticRenderer renderer = ClientCosmeticsRenderers.get(type);
            if (renderer == null) {
                continue;
            }
            try {
                renderer.render(ctx, cosmetic);
            } catch (Throwable t) {
                AlyrionCore.LOGGER.debug("Failed to render cosmetic {}: {}", cosmetic.getId(), t.toString());
            }
        }
    }

    private CosmeticDefinition getEquipped(AbstractClientPlayer player, CosmeticType type) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && player.getUUID().equals(mc.player.getUUID())) {
            CosmeticDefinition preview = PREVIEW.get(type);
            if (preview != null) {
                return preview;
            }
            return CosmeticsManager.get().getEquipped(type);
        }
        String syncedId = CosmeticNetworking.getClientPlayerCosmetic(player.getUUID(), type.getId());
        return syncedId != null ? CosmeticsRegistry.fromId(syncedId) : null;
    }
}
