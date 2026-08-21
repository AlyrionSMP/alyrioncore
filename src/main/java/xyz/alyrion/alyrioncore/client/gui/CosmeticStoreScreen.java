package xyz.alyrion.alyrioncore.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import xyz.alyrion.alyrioncore.AlyrionCore;
import xyz.alyrion.alyrioncore.client.renderer.ClientCosmeticsRenderers;
import xyz.alyrion.alyrioncore.client.renderer.CosmeticRenderer;
import xyz.alyrion.alyrioncore.client.renderer.WardrobeRenderer;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticDefinition;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticsManager;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticsRegistry;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticType;
import xyz.alyrion.alyrioncore.cosmetics.TaskDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * The Alyrion Wardrobe — a fixed-size, centered panel (vanilla-container
 * style, so it fits at every GUI scale) with a Bedrock/Essential layout:
 *
 * <pre>
 * ┌───────────────────────────────────────────────┐
 * │ ◀  ALYRION WARDROBE                    ⛃ 123  │
 * ├──────┬──────────────────────┬─────────────────┤
 * │ Capes│   [ character on a   │  CAPES (7)      │
 * │ Pets │     pedestal,        │  item card      │
 * │Trails│     rotating ]       │  item card      │
 * │ Tasks│   Name — 120 ⛃      │  item card      │
 * ├──────┴──────────────────────┴─────────────────┤
 * │              [ BUY · 120 ⛃ ]                 │
 * └───────────────────────────────────────────────┘
 * </pre>
 *
 * Vanilla buttons exist only for interaction (clicks, sounds); every visual
 * is custom-painted on top of them, so no stretched vanilla textures and no
 * element can ever overlap: all zones are fixed inside the panel and every
 * text is truncated to its box.
 */
public class CosmeticStoreScreen extends CosmeticScreen {

    private static final Object TASKS_TAB = new Object();

    // --- Fixed panel geometry (clamped for tiny windows) ---
    private int pw() {
        return Math.min(460, this.width - 8);
    }

    private int ph() {
        return Math.min(250, this.height - 8);
    }

    private int ox() {
        return (this.width - pw()) / 2;
    }

    private int oy() {
        return (this.height - ph()) / 2;
    }

    private int sideX() {
        return ox() + 6;
    }

    private int sideW() {
        return 40;
    }

    private int bodyTop() {
        return oy() + 30;
    }

    private int bodyBottom() {
        return oy() + ph() - 34;
    }

    private int catW() {
        return 148;
    }

    private int catX() {
        return ox() + pw() - 6 - catW();
    }

    private int prevX() {
        return sideX() + sideW() + 6;
    }

    private int prevRight() {
        return catX() - 6;
    }

    private int prevCenterX() {
        return prevX() + (prevRight() - prevX()) / 2;
    }

    private int tabH() {
        return Math.min(36, (bodyBottom() - bodyTop() - 12) / 4);
    }

    private final List<Object> tabs = new ArrayList<>();
    private int tabIndex = 0;
    private CosmeticDefinition selected = null;

    public CosmeticStoreScreen() {
        super(Component.literal("Alyrion Wardrobe"));
    }

    @Override
    protected void init() {
        buildTabs();
        super.init();
    }

    // --- Tabs / selection ---

    private void buildTabs() {
        tabs.clear();
        for (CosmeticType type : CosmeticType.values()) {
            if (!CosmeticsRegistry.getByType(type).isEmpty()) {
                tabs.add(type);
            }
        }
        tabs.add(TASKS_TAB);
        if (tabIndex >= tabs.size()) {
            tabIndex = 0;
        }
        selectDefault();
    }

    private void selectDefault() {
        if (onTasks()) {
            selected = null;
            return;
        }
        List<CosmeticDefinition> items = CosmeticsRegistry.getByType(currentType());
        if (items.isEmpty()) {
            selected = null;
            return;
        }
        if (selected == null || selected.getType() != currentType() || !items.contains(selected)) {
            selected = items.get(0);
        }
    }

    private boolean onTasks() {
        return !tabs.isEmpty() && tabs.get(tabIndex) == TASKS_TAB;
    }

    private CosmeticType currentType() {
        if (tabs.isEmpty()) return null;
        Object tab = tabs.get(tabIndex);
        return tab instanceof CosmeticType type ? type : null;
    }

    // --- Widgets (interaction only; painted over in renderContent) ---

    @Override
    protected void rebuildWidgets() {
        this.clearWidgets();

        this.addRenderableWidget(Button.builder(Component.literal(""), btn -> this.onClose())
                .bounds(ox() + 4, oy() + 4, 16, 16).build());

        for (int i = 0; i < tabs.size(); i++) {
            final int index = i;
            this.addRenderableWidget(Button.builder(Component.literal(""), btn -> {
                tabIndex = index;
                selectDefault();
                rebuildWidgets();
            }).bounds(sideX(), bodyTop() + i * (tabH() + 4), sideW(), tabH()).build());
        }

        if (!onTasks()) {
            List<CosmeticDefinition> items = CosmeticsRegistry.getByType(currentType());
            int cardH = cardHeight(items.size());
            int y = bodyTop() + 14;
            for (CosmeticDefinition cosmetic : items) {
                if (y + cardH > bodyBottom()) break;
                final CosmeticDefinition def = cosmetic;
                this.addRenderableWidget(Button.builder(Component.literal(""), btn -> {
                    selected = def;
                    rebuildWidgets();
                }).bounds(catX(), y, catW(), cardH).build());
                y += cardH + 3;
            }
        }
    }

    private int cardHeight(int itemCount) {
        int available = bodyBottom() - (bodyTop() + 14);
        int gaps = Math.max(0, itemCount - 1) * 3;
        return Math.max(18, Math.min(24, (available - gaps) / Math.max(1, itemCount)));
    }

    // --- Backdrop ---

    @Override
    protected void renderBackdrop(GuiGraphics guiGraphics) {
        int x = ox();
        int y = oy();
        int w = pw();
        int h = ph();

        // Panel body + header + bottom bar
        guiGraphics.fill(x, y, x + w, y + h, 0xFF10141F);
        guiGraphics.fill(x, y, x + w, y + 24, 0xFF0C1120);
        guiGraphics.fill(x, y + 24, x + w, y + 25, 0xFFEAB308);
        guiGraphics.fill(x, y + h - 30, x + w, y + h - 29, 0xFFEAB308);
        guiGraphics.fill(x, y + h - 29, x + w, y + h, 0xFF0C1120);
        guiGraphics.renderOutline(x, y, w, h, 0xFF31405E);
    }

    // --- Content ---

    @Override
    protected void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        long tick = currentTick();
        CosmeticsManager manager = CosmeticsManager.get();

        // Header: back arrow, title, coins
        boolean backHover = mouseX >= ox() + 4 && mouseX < ox() + 20 && mouseY >= oy() + 4 && mouseY < oy() + 20;
        guiGraphics.fill(ox() + 4, oy() + 4, ox() + 20, oy() + 20, backHover ? 0xFF202B44 : 0xFF161D2E);
        guiGraphics.renderOutline(ox() + 4, oy() + 4, 16, 16, 0xFF31405E);
        guiGraphics.drawString(this.font, "§7◀", ox() + 8, oy() + 8, 0xFFFFFF, false);

        guiGraphics.drawString(this.font, "§6§lALYRION WARDROBE", ox() + 26, oy() + 8, 0xFFFFFF, true);

        String coinText = "§e⛃ §6" + manager.getCoins();
        int coinW = this.font.width(coinText) + 10;
        int coinX = ox() + pw() - 6 - coinW;
        guiGraphics.fill(coinX, oy() + 4, coinX + coinW, oy() + 20, 0xFF161D2E);
        guiGraphics.renderOutline(coinX, oy() + 4, coinW, 16, 0xFFEAB308);
        guiGraphics.drawString(this.font, coinText, coinX + 5, oy() + 8, 0xFFFFFF, true);

        renderSidebar(guiGraphics, mouseX, mouseY, tick);

        if (onTasks()) {
            renderTasksTab(guiGraphics);
            String hint = "Tasks complete automatically while you play — rewards are credited instantly.";
            guiGraphics.drawCenteredString(this.font, fit("§7" + hint, pw() - 16), ox() + pw() / 2, oy() + ph() - 17, 0xAAAAAA);
        } else {
            renderCatalog(guiGraphics, mouseX, mouseY, tick);
            // Action bar paints BEFORE the 3D preview so it can never be
            // skipped if the character render misbehaves.
            renderActionBar(guiGraphics, mouseX, mouseY);
            try {
                renderPreview(guiGraphics, tick, partialTick, mouseX, mouseY);
            } catch (Throwable t) {
                AlyrionCore.LOGGER.debug("Store preview failed: {}", t.toString());
            }
        }
    }

    private void renderSidebar(GuiGraphics guiGraphics, int mouseX, int mouseY, long tick) {
        for (int i = 0; i < tabs.size(); i++) {
            Object tab = tabs.get(i);
            int x = sideX();
            int y = bodyTop() + i * (tabH() + 4);
            boolean active = i == tabIndex;
            boolean hover = mouseX >= x && mouseX < x + sideW() && mouseY >= y && mouseY < y + tabH();

            guiGraphics.fill(x, y, x + sideW(), y + tabH(), active ? 0xFF243352 : hover ? 0xFF202B44 : 0xFF161D2E);
            guiGraphics.renderOutline(x, y, sideW(), tabH(), active ? 0xFFFFD700 : 0xFF31405E);

            int iconX = x + (sideW() - 16) / 2;
            int iconY = y + 3;
            if (tab instanceof CosmeticType type) {
                List<CosmeticDefinition> items = CosmeticsRegistry.getByType(type);
                CosmeticRenderer renderer = ClientCosmeticsRenderers.get(type);
                if (!items.isEmpty() && renderer != null) {
                    renderer.drawStoreIcon(guiGraphics, items.get(0), iconX, iconY, 16, tick);
                }
                guiGraphics.drawCenteredString(this.font, fit("§7" + type.getDisplayName(), sideW() - 4), x + sideW() / 2, y + tabH() - 10, 0xFFFFFF);
            } else {
                drawTaskStar(guiGraphics, iconX, iconY);
                guiGraphics.drawCenteredString(this.font, "§7Tasks", x + sideW() / 2, y + tabH() - 10, 0xFFFFFF);
            }
        }
    }

    private void drawTaskStar(GuiGraphics guiGraphics, int x, int y) {
        int cx = x + 8;
        int cy = y + 8;
        int color = 0xFFEAB308;
        guiGraphics.fill(cx - 2, cy - 5, cx + 2, cy - 2, color);
        guiGraphics.fill(cx - 4, cy - 2, cx + 4, cy + 1, color);
        guiGraphics.fill(cx - 4, cy + 1, cx + 4, cy + 3, color);
        guiGraphics.fill(cx - 2, cy + 3, cx + 2, cy + 5, color);
    }

    private void renderCatalog(GuiGraphics guiGraphics, int mouseX, int mouseY, long tick) {
        CosmeticsManager manager = CosmeticsManager.get();
        CosmeticType type = currentType();
        CosmeticRenderer renderer = ClientCosmeticsRenderers.get(type);
        List<CosmeticDefinition> items = CosmeticsRegistry.getByType(type);
        int cardH = cardHeight(items.size());
        int y = bodyTop() + 14;

        guiGraphics.drawString(this.font, "§6§l" + type.getDisplayName().toUpperCase() + " §7(" + items.size() + ")",
                catX() + 4, bodyTop() + 2, 0xFFFFFF, true);

        for (CosmeticDefinition cosmetic : items) {
            if (y + cardH > bodyBottom()) break;
            boolean isSelected = cosmetic == selected;
            boolean hover = mouseX >= catX() && mouseX < catX() + catW() && mouseY >= y && mouseY < y + cardH;

            guiGraphics.fill(catX(), y, catX() + catW(), y + cardH, isSelected ? 0xFF1B2740 : hover ? 0xFF202B44 : 0xFF161D2E);
            guiGraphics.renderOutline(catX(), y, catW(), cardH, isSelected ? 0xFFFFD700 : 0xFF31405E);

            int iconX = catX() + 5;
            int iconY = y + (cardH - 14) / 2;
            if (renderer != null) {
                renderer.drawStoreIcon(guiGraphics, cosmetic, iconX, iconY, 14, tick);
            }

            String status;
            if (manager.isEquipped(cosmetic)) {
                status = "§a✔ Eq.";
            } else if (manager.isUnlocked(cosmetic)) {
                status = "§b✔";
            } else if (!cosmetic.isPurchasable()) {
                status = "§e★";
            } else if (cosmetic.isFree()) {
                status = "§dFree";
            } else {
                status = "§6" + cosmetic.getPrice() + "⛃";
            }
            int statusW = this.font.width(status);
            guiGraphics.drawString(this.font, status, catX() + catW() - statusW - 4, y + (cardH - 8) / 2, 0xFFFFFF, true);

            String name = (manager.isEquipped(cosmetic) ? "§a" : "§f") + fit(cosmetic.getDisplayName(), catW() - 24 - statusW - 6);
            guiGraphics.drawString(this.font, name, catX() + 22, y + (cardH - 8) / 2, 0xFFFFFF, false);

            y += cardH + 3;
        }
    }

    /** The action bar (Equip/Unequip/Buy/Claim) is painted purely by
     *  {@link #renderActionBar}; clicking is handled here instead of by a
     *  vanilla widget so no stock button texture can ever show through. */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && !onTasks() && selected != null) {
            int bx = prevCenterX() - 75;
            int by = oy() + ph() - 26;
            int bw = 150;
            int bh = 18;
            if (mouseX >= bx && mouseX < bx + bw && mouseY >= by && mouseY < by + bh) {
                CosmeticsManager manager = CosmeticsManager.get();
                if (manager.isEquipped(selected)) {
                    manager.unequip(selected.getType());
                } else if (manager.isUnlocked(selected)) {
                    manager.equip(selected);
                } else if (selected.isPurchasable() && manager.getCoins() >= selected.getPrice()) {
                    manager.purchase(selected);
                }
                rebuildWidgets();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderPreview(GuiGraphics guiGraphics, long tick, float partialTick, int mouseX, int mouseY) {
        CosmeticsManager manager = CosmeticsManager.get();
        if (selected == null) return;

        int px = prevX();
        int pw = prevRight() - px;
        int modelTop = bodyTop() + 6;
        int modelBottom = bodyBottom() - 34;
        int modelHeight = Math.max(60, modelBottom - modelTop);
        int cx = prevCenterX();
        int cy = (modelTop + modelBottom) / 2;

        CosmeticDefinition preview = selected;
        CosmeticDefinition cape = preview.getType() == CosmeticType.CAPE ? preview : manager.getEquipped(CosmeticType.CAPE);
        CosmeticDefinition pet = preview.getType() == CosmeticType.PET ? preview : manager.getEquipped(CosmeticType.PET);
        CosmeticDefinition trail = preview.getType() == CosmeticType.TRAIL ? preview : manager.getEquipped(CosmeticType.TRAIL);

        // Pedestal shadow under the feet
        int feetY = modelBottom;
        int[] bandW = {56, 42, 28, 14};
        for (int i = 0; i < bandW.length; i++) {
            int bw = bandW[i];
            int alpha = 0x55 - i * 0x10;
            guiGraphics.fill(cx - bw / 2, feetY + 2 + i, cx + bw / 2, feetY + 3 + i, (alpha << 24));
        }
        guiGraphics.fill(cx - 22, feetY + 6, cx + 22, feetY + 7, 0x66EAB308);

        // The trail is drawn live onto the 3D character below (same render
        // layer the world uses), so there is no separate 2D preview pass.
        WardrobeRenderer.drawPlayerModel(guiGraphics, cx, cy, modelHeight, tick, partialTick, mouseX, mouseY, cape, pet, trail);

        // Name + status under the character
        guiGraphics.drawCenteredString(this.font, "§e§l" + fit(preview.getDisplayName(), pw - 8), cx, modelBottom + 12, 0xFFFFFF);

        String info;
        if (manager.isEquipped(preview)) {
            info = "§aStatus: Equipped";
        } else if (manager.isUnlocked(preview)) {
            info = "§bStatus: Owned";
        } else if (!preview.isPurchasable()) {
            info = "§d" + taskTitleFor(preview);
        } else if (preview.isFree()) {
            info = "§dFree — claim it";
        } else {
            info = "§6" + preview.getPrice() + " Coins" + (manager.getCoins() >= preview.getPrice() ? "" : " §7(need " + (preview.getPrice() - manager.getCoins()) + " more)");
        }
        guiGraphics.drawCenteredString(this.font, fit(info, pw - 8), cx, modelBottom + 24, 0xFFFFFF);
    }

    private static String taskTitleFor(CosmeticDefinition cosmetic) {
        for (TaskDefinition task : TaskDefinition.values()) {
            if (cosmetic.equals(task.getReward())) {
                return "★ Task: " + task.getTitle();
            }
        }
        return "★ Task reward";
    }

    private void renderActionBar(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (selected == null) return;
        CosmeticsManager manager = CosmeticsManager.get();

        int bx = prevCenterX() - 75;
        int by = oy() + ph() - 26;
        int bw = 150;
        int bh = 18;
        boolean hover = mouseX >= bx && mouseX < bx + bw && mouseY >= by && mouseY < by + bh;

        boolean isEquipped = manager.isEquipped(selected);
        boolean isUnlocked = manager.isUnlocked(selected);

        String label;
        boolean enabled;
        int fill;
        int border;
        if (isEquipped) {
            label = "§cUNEQUIP";
            enabled = true;
            fill = 0xFF3A1B22;
            border = 0xFF8A3B47;
        } else if (isUnlocked) {
            label = "§aEQUIP";
            enabled = true;
            fill = 0xFF173A26;
            border = 0xFF2F8A57;
        } else if (!selected.isPurchasable()) {
            label = "§dTASK REWARD";
            enabled = false;
            fill = 0xFF1A2130;
            border = 0xFF31405E;
        } else if (selected.isFree()) {
            label = "§bCLAIM FREE";
            enabled = true;
            fill = 0xFF123A44;
            border = 0xFF2F8AA0;
        } else {
            boolean canAfford = manager.getCoins() >= selected.getPrice();
            label = "§6BUY · " + selected.getPrice() + " ⛃";
            enabled = canAfford;
            fill = canAfford ? 0xFF3A2C12 : 0xFF1A2130;
            border = canAfford ? 0xFFB98A2F : 0xFF31405E;
        }

        guiGraphics.fill(bx, by, bx + bw, by + bh, enabled && hover ? 0xFF2A3550 : fill);
        guiGraphics.renderOutline(bx, by, bw, bh, border);
        guiGraphics.drawCenteredString(this.font, enabled ? label : "§7" + label.substring(2), bx + bw / 2, by + 5, 0xFFFFFF);
    }

    private void renderTasksTab(GuiGraphics guiGraphics) {
        CosmeticsManager manager = CosmeticsManager.get();

        int tx = prevX();
        int tw = (catX() + catW()) - tx;

        // Playtime card
        int py = bodyTop();
        int playH = 30;
        guiGraphics.fill(tx, py, tx + tw, py + playH, 0xFF161D2E);
        guiGraphics.renderOutline(tx, py, tw, playH, 0xFF31405E);

        long seconds = manager.getPlaytimeSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        long nextCoinSecs = seconds % 3600;

        guiGraphics.drawString(this.font,
                fit(String.format("§ePlaytime: §f%dh %02dm %02ds §7| §61 Coin / 1h", hours, minutes, secs), tw - 8),
                tx + 4, py + 4, 0xFFFFFF, true);

        int barX = tx + 4;
        int barY = py + 16;
        int barW = tw - 8;
        int barH = 9;
        float progress = (float) nextCoinSecs / 3600.0F;

        guiGraphics.fill(barX, barY, barX + barW, barY + barH, 0xFF000000);
        guiGraphics.fill(barX + 1, barY + 1, barX + 1 + (int) ((barW - 2) * progress), barY + barH - 1, 0xFFF59E0B);
        guiGraphics.renderOutline(barX, barY, barW, barH, 0xFF6B7280);
        guiGraphics.drawCenteredString(this.font,
                fit(String.format("§fNext: %dm %02ds (%d%%)", nextCoinSecs / 60, nextCoinSecs % 60, (int) (progress * 100)), barW - 4),
                barX + barW / 2, barY + 1, 0xFFFFFF);

        // Task cards
        int ty = py + playH + 6;
        TaskDefinition[] tasks = TaskDefinition.values();
        int gaps = (tasks.length - 1) * 3;
        int cardH = Math.max(18, Math.min(30, (bodyBottom() - ty - gaps) / Math.max(1, tasks.length)));

        for (TaskDefinition task : tasks) {
            if (ty + cardH > bodyBottom()) break;
            boolean completed = manager.isTaskCompleted(task.getId());

            guiGraphics.fill(tx, ty, tx + tw, ty + cardH, completed ? 0xFF0F291E : 0xFF1F2937);
            guiGraphics.renderOutline(tx, ty, tw, cardH, completed ? 0xFF10B981 : 0xFF4B5563);

            String reward = "§6+" + task.getCoinReward() + "⛃" + (task.getReward() != null ? " §b+" + fit(task.getReward().getDisplayName(), tw / 3) : "");
            int rW = this.font.width(reward);
            guiGraphics.drawString(this.font, reward, tx + tw - rW - 4, ty + 3, 0xFFFFFF, true);

            guiGraphics.drawString(this.font,
                    (completed ? "§a✔ " : "§e⏳ ") + fit(task.getTitle(), tw - rW - 14),
                    tx + 4, ty + 3, 0xFFFFFF, true);
            guiGraphics.drawString(this.font, "§7" + fit(task.getDescription(), tw - 8), tx + 4, ty + 13, 0xAAAAAA, false);

            ty += cardH + 3;
        }
    }
}