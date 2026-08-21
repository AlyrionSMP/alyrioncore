package xyz.alyrion.alyrioncore.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticDefinition;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticType;

/**
 * Renders the local player into the store exactly as they look in the world —
 * real skin, real animations, cape physics, orbiting pet — by reusing the
 * vanilla inventory-screen entity render, viewed from the BACK so the cape is
 * the star of the preview.
 *
 * The cape/pet arguments are applied as a temporary render-layer preview
 * override (see {@link CosmeticRenderLayer#setPreview}), so the selected item
 * shows on the model before it is equipped or bought.
 */
public final class WardrobeRenderer {

    private WardrobeRenderer() {
    }

    public static void drawPlayerModel(GuiGraphics guiGraphics, int cx, int cy, int heightPx, long tick,
                                       float partialTick, int mouseX, int mouseY,
                                       CosmeticDefinition cape, CosmeticDefinition pet, CosmeticDefinition trail) {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.player instanceof AbstractClientPlayer player)) return;

        CosmeticRenderLayer.setPreview(CosmeticType.CAPE, cape);
        CosmeticRenderLayer.setPreview(CosmeticType.PET, pet);
        CosmeticRenderLayer.setPreview(CosmeticType.TRAIL, trail);

        float scale = heightPx / player.getBbHeight();

        // Live paper-doll like the vanilla inventory: head & body track the
        // mouse around the BACK view (yaw 0; vanilla's front view uses 180),
        // plus a gentle idle sway.
        float yawMouse = (float) Math.atan((cx - mouseX) / 40.0F);
        float pitchMouse = (float) Math.atan((cy - mouseY) / 40.0F);
        float sway = Mth.sin((tick + partialTick) / 40.0F) * 4.0F;

        float bodyRot = player.yBodyRot;
        float yRot = player.getYRot();
        float xRot = player.getXRot();
        float headRotO = player.yHeadRotO;
        float headRot = player.yHeadRot;
        player.yBodyRot = yawMouse * 20.0F + sway;
        player.setYRot(yawMouse * 40.0F + sway);
        player.setXRot(-pitchMouse * 20.0F);
        player.yHeadRot = player.getYRot();
        player.yHeadRotO = player.getYRot();

        Quaternionf pose = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf camera = new Quaternionf();
        try {
            InventoryScreen.renderEntityInInventory(guiGraphics, cx, cy, scale,
                    new Vector3f(0.0F, player.getBbHeight() / 2.0F, 0.0F), pose, camera, player);
        } finally {
            player.yBodyRot = bodyRot;
            player.setYRot(yRot);
            player.setXRot(xRot);
            player.yHeadRotO = headRotO;
            player.yHeadRot = headRot;
            CosmeticRenderLayer.clearPreview();
        }
    }
}