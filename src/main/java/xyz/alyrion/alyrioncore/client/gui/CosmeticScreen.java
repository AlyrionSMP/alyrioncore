package xyz.alyrion.alyrioncore.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticsManager;

/**
 * Base class for cosmetic store screens.
 *
 * Paints a flat dark backdrop (no blurred world) and splits rendering into
 * layers so panels are always painted before widgets and custom visuals
 * always after:
 * <ol>
 *   <li>{@link #renderScreenBackground} — flat dark gradient</li>
 *   <li>{@link #renderBackdrop} — panels and frames</li>
 *   <li>widgets (interaction only; their vanilla look is painted over)</li>
 *   <li>{@link #renderContent} — the custom-drawn UI</li>
 * </ol>
 */
public abstract class CosmeticScreen extends Screen {

    private int lastRevision = -1;

    protected CosmeticScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();
        CosmeticsManager.get().ensureSynced();
        rebuildWidgets();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        CosmeticsManager manager = CosmeticsManager.get();

        // Live-refresh whenever the server syncs new state while the store is open.
        if (manager.getRevision() != lastRevision) {
            lastRevision = manager.getRevision();
            rebuildWidgets();
        }

        renderScreenBackground(guiGraphics);
        renderBackdrop(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderContent(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        // Keep the world running while the store is open (like the inventory screen)
        return false;
    }

    /** Flat dark backdrop instead of the blurred world. */
    protected void renderScreenBackground(GuiGraphics guiGraphics) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, 0xFF10141F, 0xFF070A12);
    }

    /** Panels and frames, painted before the widgets. */
    protected void renderBackdrop(GuiGraphics guiGraphics) {
    }

    /** The custom-drawn UI, painted after the widgets. */
    protected abstract void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);

    /** Truncates text with an ellipsis so it never breaks out of its box. */
    protected String fit(String text, int maxW) {
        if (this.font.width(text) <= maxW) {
            return text;
        }
        String t = text;
        while (t.length() > 1 && this.font.width(t + "…") > maxW) {
            t = t.substring(0, t.length() - 1);
        }
        return t + "…";
    }

    protected static long currentTick() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null ? mc.level.getGameTime() : 0L;
    }
}