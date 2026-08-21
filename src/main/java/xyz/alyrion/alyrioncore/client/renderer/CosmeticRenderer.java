package xyz.alyrion.alyrioncore.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticDefinition;

/**
 * Client-side renderer for one {@link xyz.alyrion.alyrioncore.cosmetics.CosmeticType}.
 *
 * Implementations draw the equipped cosmetic on players (via
 * {@link CosmeticRenderLayer}) and optionally provide the store's list icon and
 * preview-panel artwork. Register an implementation per type in
 * {@link ClientCosmeticsRenderers} — that is the only place a new cosmetic kind
 * needs wiring on the client.
 */
public interface CosmeticRenderer {

    /** Render the equipped cosmetic on a player during the player render pass. */
    void render(CosmeticRenderContext ctx, CosmeticDefinition cosmetic);

    /** Small icon drawn in the store's list rows. */
    default void drawStoreIcon(GuiGraphics guiGraphics, CosmeticDefinition cosmetic, int x, int y, int size, long tick) {
        guiGraphics.fill(x, y, x + size, y + size, 0xFF1F2937);
        guiGraphics.renderOutline(x, y, size, size, 0xFF4B5563);
        String letter = cosmetic.getDisplayName().isEmpty() ? "?" : cosmetic.getDisplayName().substring(0, 1);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, letter, x + size / 2, y + size / 2 - 4, 0xFFFFFF);
    }

    /** Large artwork drawn in the store's preview panel (may use the full rect). */
    default void drawStorePreview(GuiGraphics guiGraphics, CosmeticDefinition cosmetic, int x, int y, int w, int h, long tick) {
    }
}
