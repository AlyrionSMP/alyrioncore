package xyz.alyrion.alyrioncore.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import xyz.alyrion.alyrioncore.cosmetics.CapeDefinition;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticConfig;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticsManager;
import xyz.alyrion.alyrioncore.cosmetics.TaskDefinition;

public class CosmeticStoreScreen extends Screen {

    public enum Tab {
        STORE,
        TASKS,
        DEV
    }

    private Tab currentTab = Tab.STORE;
    private CapeDefinition selectedCape = CapeDefinition.TWO_YEAR_CELEBRATION;
    private int scrollOffset = 0;

    public CosmeticStoreScreen() {
        super(Component.literal("Alyrion Cosmetic Store"));
    }

    @Override
    protected void init() {
        super.init();
        if (this.minecraft != null && this.minecraft.player != null) {
            CosmeticsManager.get().checkTasks(this.minecraft.player);
        }
        rebuildWidgets();
    }

    @Override
    protected void rebuildWidgets() {
        this.clearWidgets();
        CosmeticsManager manager = CosmeticsManager.get();

        int topY = 30;
        int tabWidth = 95;
        int tabHeight = 20;
        int tabStartX = (this.width - (CosmeticConfig.DEV_MODE ? (tabWidth * 3 + 8) : (tabWidth * 2 + 4))) / 2;

        // Tab Buttons
        this.addRenderableWidget(Button.builder(Component.literal(currentTab == Tab.STORE ? "§6§lStore & Wardrobe" : "Store & Wardrobe"), btn -> {
            currentTab = Tab.STORE;
            scrollOffset = 0;
            rebuildWidgets();
        }).bounds(tabStartX, topY, tabWidth, tabHeight).build());

        this.addRenderableWidget(Button.builder(Component.literal(currentTab == Tab.TASKS ? "§e§lTasks & Playtime" : "Tasks & Playtime"), btn -> {
            currentTab = Tab.TASKS;
            scrollOffset = 0;
            rebuildWidgets();
        }).bounds(tabStartX + tabWidth + 4, topY, tabWidth, tabHeight).build());

        if (CosmeticConfig.DEV_MODE) {
            this.addRenderableWidget(Button.builder(Component.literal(currentTab == Tab.DEV ? "§d§lDev Controls" : "Dev Controls"), btn -> {
                currentTab = Tab.DEV;
                rebuildWidgets();
            }).bounds(tabStartX + (tabWidth + 4) * 2, topY, tabWidth, tabHeight).build());
        }

        int contentY = 56;

        if (currentTab == Tab.STORE) {
            initStoreWidgets(manager, contentY);
        } else if (currentTab == Tab.TASKS) {
            initTasksWidgets(manager, contentY);
        } else if (currentTab == Tab.DEV && CosmeticConfig.DEV_MODE) {
            initDevWidgets(manager, contentY);
        }

        // Bottom Close Button
        this.addRenderableWidget(Button.builder(Component.literal("Close"), btn -> this.onClose())
                .bounds(this.width / 2 - 50, this.height - 24, 100, 18).build());
    }

    private void initStoreWidgets(CosmeticsManager manager, int contentY) {
        int listX = 16;
        int listWidth = this.width / 2 + 15;
        CapeDefinition[] capes = CapeDefinition.values();
        int availableHeight = this.height - contentY - 30;
        int itemSpacing = 3;
        int itemHeight = Math.max(26, Math.min(32, (availableHeight - (capes.length - 1) * itemSpacing) / capes.length));

        for (int i = 0; i < capes.length; i++) {
            CapeDefinition cape = capes[i];
            int itemY = contentY + i * (itemHeight + itemSpacing) - scrollOffset;

            // Only add widgets if in visible viewport
            if (itemY + itemHeight < contentY || itemY > this.height - 28) continue;

            boolean isUnlocked = manager.isCapeUnlocked(cape);
            boolean isEquipped = manager.isCapeEquipped(cape);

            // Select Cape card button
            int btnSelectX = listX;
            int btnSelectWidth = listWidth - 85;
            this.addRenderableWidget(Button.builder(Component.literal("§f" + cape.getDisplayName()), btn -> {
                this.selectedCape = cape;
            }).bounds(btnSelectX, itemY, btnSelectWidth, itemHeight).build());

            // Action Button (Equip / Unequip / Buy / Claim)
            int actionBtnX = listX + listWidth - 80;
            int actionBtnWidth = 78;
            int actionBtnHeight = Math.min(20, itemHeight - 4);
            int actionBtnY = itemY + (itemHeight - actionBtnHeight) / 2;
            Button actionBtn;

            if (isEquipped) {
                actionBtn = Button.builder(Component.literal("§cUnequip"), btn -> {
                    manager.unequipCape();
                    rebuildWidgets();
                }).bounds(actionBtnX, actionBtnY, actionBtnWidth, actionBtnHeight).build();
            } else if (isUnlocked) {
                actionBtn = Button.builder(Component.literal("§aEquip"), btn -> {
                    manager.equipCape(cape);
                    rebuildWidgets();
                }).bounds(actionBtnX, actionBtnY, actionBtnWidth, actionBtnHeight).build();
            } else if (cape.isFree()) {
                actionBtn = Button.builder(Component.literal("§bClaim Free"), btn -> {
                    manager.purchaseCape(cape);
                    rebuildWidgets();
                }).bounds(actionBtnX, actionBtnY, actionBtnWidth, actionBtnHeight).build();
            } else {
                boolean canAfford = manager.getCoins() >= cape.getPrice();
                actionBtn = Button.builder(Component.literal(canAfford ? "§6Buy (" + cape.getPrice() + "⛃)" : "§7" + cape.getPrice() + " ⛃"), btn -> {
                    if (canAfford) {
                        manager.purchaseCape(cape);
                        rebuildWidgets();
                    }
                }).bounds(actionBtnX, actionBtnY, actionBtnWidth, actionBtnHeight).build();
                actionBtn.active = canAfford;
            }

            this.addRenderableWidget(actionBtn);
        }

        // Preview panel action button
        int previewX = this.width / 2 + 38;
        int previewWidth = this.width - previewX - 16;
        if (selectedCape != null) {
            boolean isUnlocked = manager.isCapeUnlocked(selectedCape);
            boolean isEquipped = manager.isCapeEquipped(selectedCape);
            int actionBtnY = this.height - 48;

            if (isEquipped) {
                this.addRenderableWidget(Button.builder(Component.literal("§cUnequip Cape"), btn -> {
                    manager.unequipCape();
                    rebuildWidgets();
                }).bounds(previewX + (previewWidth - 110) / 2, actionBtnY, 110, 20).build());
            } else if (isUnlocked) {
                this.addRenderableWidget(Button.builder(Component.literal("§aEquip Cape"), btn -> {
                    manager.equipCape(selectedCape);
                    rebuildWidgets();
                }).bounds(previewX + (previewWidth - 110) / 2, actionBtnY, 110, 20).build());
            } else if (selectedCape.isFree()) {
                this.addRenderableWidget(Button.builder(Component.literal("§bClaim Free"), btn -> {
                    manager.purchaseCape(selectedCape);
                    rebuildWidgets();
                }).bounds(previewX + (previewWidth - 110) / 2, actionBtnY, 110, 20).build());
            } else {
                boolean canAfford = manager.getCoins() >= selectedCape.getPrice();
                Button buyBtn = Button.builder(Component.literal("§6Buy (" + selectedCape.getPrice() + " Coins)"), btn -> {
                    if (canAfford) {
                        manager.purchaseCape(selectedCape);
                        rebuildWidgets();
                    }
                }).bounds(previewX + (previewWidth - 120) / 2, actionBtnY, 120, 20).build();
                buyBtn.active = canAfford;
                this.addRenderableWidget(buyBtn);
            }
        }
    }

    private void initTasksWidgets(CosmeticsManager manager, int contentY) {
    }

    private void initDevWidgets(CosmeticsManager manager, int contentY) {
        int centerX = this.width / 2;
        int btnWidth = 170;
        int btnHeight = 20;

        int col1X = centerX - btnWidth - 10;
        int y = contentY + 14;

        this.addRenderableWidget(Button.builder(Component.literal("§a✔ Complete: Space Task"), btn -> {
            manager.completeTask(TaskDefinition.GOING_TO_SPACE, true);
            rebuildWidgets();
        }).bounds(col1X, y, btnWidth, btnHeight).build());

        this.addRenderableWidget(Button.builder(Component.literal("§a✔ Complete: Moon Task"), btn -> {
            manager.completeTask(TaskDefinition.GOING_TO_MOON, true);
            rebuildWidgets();
        }).bounds(col1X, y + 25, btnWidth, btnHeight).build());

        this.addRenderableWidget(Button.builder(Component.literal("§a✔ Complete: Mars Task"), btn -> {
            manager.completeTask(TaskDefinition.GOING_TO_MARS, true);
            rebuildWidgets();
        }).bounds(col1X, y + 50, btnWidth, btnHeight).build());

        this.addRenderableWidget(Button.builder(Component.literal("§a✔ Complete: Dragon Egg"), btn -> {
            manager.completeTask(TaskDefinition.OBTAINING_DRAGON_EGG, true);
            rebuildWidgets();
        }).bounds(col1X, y + 75, btnWidth, btnHeight).build());

        int col2X = centerX + 10;

        this.addRenderableWidget(Button.builder(Component.literal("§e+1 Hour Playtime (+1 Coin)"), btn -> {
            manager.devAddPlaytime(3600);
            rebuildWidgets();
        }).bounds(col2X, y, btnWidth, btnHeight).build());

        this.addRenderableWidget(Button.builder(Component.literal("§6+10 Coins"), btn -> {
            manager.devAddCoins(10);
            rebuildWidgets();
        }).bounds(col2X, y + 25, btnWidth, btnHeight).build());

        this.addRenderableWidget(Button.builder(Component.literal("§cReset All Tasks Progress"), btn -> {
            manager.devResetAllTasks();
            rebuildWidgets();
        }).bounds(col2X, y + 50, btnWidth, btnHeight).build());

        this.addRenderableWidget(Button.builder(Component.literal("§4Reset All Cosmetic Unlocks"), btn -> {
            manager.devResetCosmetics();
            rebuildWidgets();
        }).bounds(col2X, y + 75, btnWidth, btnHeight).build());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (currentTab == Tab.STORE || currentTab == Tab.TASKS) {
            if (scrollY != 0) {
                scrollOffset = Math.max(0, scrollOffset - (int) (scrollY * 16));
                rebuildWidgets();
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        CosmeticsManager manager = CosmeticsManager.get();

        // Top Header
        guiGraphics.fill(0, 0, this.width, 26, 0xDD0C0F17);
        guiGraphics.fill(0, 25, this.width, 26, 0xFFEAB308);
        guiGraphics.drawString(this.font, "§6§l✦ ALYRION COSMETIC STORE & REWARDS ✦", 14, 8, 0xFFFFFF, true);

        // Coin Counter Badge
        String coinText = "§6Coins: §e⛃ " + manager.getCoins();
        int coinWidth = this.font.width(coinText) + 16;
        int coinBoxX = this.width - coinWidth - 12;
        guiGraphics.fill(coinBoxX, 3, coinBoxX + coinWidth, 23, 0xCC1E2333);
        guiGraphics.renderOutline(coinBoxX, 3, coinWidth, 20, 0xFFEAB308);
        guiGraphics.drawString(this.font, coinText, coinBoxX + 8, 9, 0xFFFFFF, true);

        int contentY = 56;

        if (currentTab == Tab.STORE) {
            renderStoreTab(guiGraphics, manager, contentY);
        } else if (currentTab == Tab.TASKS) {
            renderTasksTab(guiGraphics, manager, contentY);
        } else if (currentTab == Tab.DEV && CosmeticConfig.DEV_MODE) {
            renderDevTab(guiGraphics, manager, contentY);
        }
    }

    private void renderStoreTab(GuiGraphics guiGraphics, CosmeticsManager manager, int contentY) {
        int listX = 16;
        int listWidth = this.width / 2 + 15;
        CapeDefinition[] capes = CapeDefinition.values();
        int availableHeight = this.height - contentY - 30;
        int itemSpacing = 3;
        int itemHeight = Math.max(26, Math.min(32, (availableHeight - (capes.length - 1) * itemSpacing) / capes.length));

        // Render cape list items
        for (int i = 0; i < capes.length; i++) {
            CapeDefinition cape = capes[i];
            int itemY = contentY + i * (itemHeight + itemSpacing) - scrollOffset;

            if (itemY + itemHeight < contentY || itemY > this.height - 28) continue;

            boolean isUnlocked = manager.isCapeUnlocked(cape);
            boolean isEquipped = manager.isCapeEquipped(cape);
            boolean isSelected = cape == selectedCape;

            // Highlight border if selected
            if (isSelected) {
                guiGraphics.renderOutline(listX - 1, itemY - 1, listWidth + 2, itemHeight + 2, 0xFFFFD700);
            }

            // Draw miniature cape texture preview inside button
            int iconX = listX + 5;
            int iconY = itemY + 3;
            int iconH = itemHeight - 6;
            int iconW = (int) (iconH * 10.0F / 16.0F);

            guiGraphics.blit(
                    cape.getTextureLocation(),
                    iconX, iconY,
                    iconW, iconH,
                    12.0F, 1.0F,
                    10, 16,
                    64, 32
            );

            // Subtitle info
            String statusText;
            if (isEquipped) {
                statusText = "§a✔ EQUIPPED";
            } else if (isUnlocked) {
                statusText = "§b✔ UNLOCKED";
            } else if (cape.isFree()) {
                statusText = "§d★ FREE";
            } else {
                statusText = "§6" + cape.getPrice() + " Coins";
            }
            if (itemHeight >= 28) {
                guiGraphics.drawString(this.font, statusText, listX + iconW + 10, itemY + itemHeight - 11, 0xAAAAAA, false);
            }
        }

        // Right Preview Showcase Panel
        int previewX = this.width / 2 + 38;
        int previewY = contentY;
        int previewWidth = this.width - previewX - 16;
        int previewHeight = this.height - previewY - 30;

        guiGraphics.fill(previewX, previewY, previewX + previewWidth, previewY + previewHeight, 0xCC111827);
        guiGraphics.renderOutline(previewX, previewY, previewWidth, previewHeight, 0xFF3B82F6);

        if (selectedCape != null) {
            guiGraphics.drawCenteredString(this.font, "§e§l" + selectedCape.getDisplayName(), previewX + previewWidth / 2, previewY + 8, 0xFFFFFF);

            // Draw Large 2D Cape Display
            int capeDrawH = Math.min(65, previewHeight - 85);
            int capeDrawW = (int) (capeDrawH * 10.0F / 16.0F);
            int capeDrawX = previewX + (previewWidth - capeDrawW) / 2;
            int capeDrawY = previewY + 22;

            guiGraphics.fill(capeDrawX - 3, capeDrawY - 3, capeDrawX + capeDrawW + 3, capeDrawY + capeDrawH + 3, 0xFF000000);
            guiGraphics.renderOutline(capeDrawX - 3, capeDrawY - 3, capeDrawW + 6, capeDrawH + 6, 0xFF60A5FA);

            guiGraphics.blit(
                    selectedCape.getTextureLocation(),
                    capeDrawX, capeDrawY,
                    capeDrawW, capeDrawH,
                    12.0F, 1.0F,
                    10, 16,
                    64, 32
            );

            // Cape Description
            int descY = capeDrawY + capeDrawH + 8;
            guiGraphics.drawWordWrap(this.font, Component.literal("§7" + selectedCape.getDescription()), previewX + 8, descY, previewWidth - 16, 0xCCCCCC);

            // Status label
            boolean isUnlocked = manager.isCapeUnlocked(selectedCape);
            boolean isEquipped = manager.isCapeEquipped(selectedCape);
            String stateStr = isEquipped ? "§aStatus: Equipped" : (isUnlocked ? "§bStatus: Unlocked" : "§6Status: Locked (" + selectedCape.getPrice() + " Coins)");
            guiGraphics.drawCenteredString(this.font, stateStr, previewX + previewWidth / 2, this.height - 60, 0xFFFFFF);
        }
    }

    private void renderTasksTab(GuiGraphics guiGraphics, CosmeticsManager manager, int contentY) {
        int cardX = 20;
        int cardW = this.width - 40;

        // Playtime Header Card
        int playCardH = 40;
        guiGraphics.fill(cardX, contentY, cardX + cardW, contentY + playCardH, 0xCC111827);
        guiGraphics.renderOutline(cardX, contentY, cardW, playCardH, 0xFFEAB308);

        long seconds = manager.getPlaytimeSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        long nextCoinSecs = seconds % 3600;

        String playtimeStr = String.format("§eSurvival Playtime: §f%dh %02dm %02ds §7| §61 Coin awarded every 1 Hour", hours, minutes, secs);
        guiGraphics.drawString(this.font, playtimeStr, cardX + 8, contentY + 6, 0xFFFFFF, true);

        // Progress Bar to next coin
        int barX = cardX + 8;
        int barY = contentY + 20;
        int barW = cardW - 16;
        int barH = 12;
        float progress = (float) nextCoinSecs / 3600.0F;

        guiGraphics.fill(barX, barY, barX + barW, barY + barH, 0xFF000000);
        guiGraphics.fill(barX + 1, barY + 1, barX + (int) ((barW - 2) * progress), barY + barH - 1, 0xFFF59E0B);
        guiGraphics.renderOutline(barX, barY, barW, barH, 0xFF6B7280);

        String progressPercent = String.format("Next Coin: %dm %02ds / 60m (%d%%)", nextCoinSecs / 60, nextCoinSecs % 60, (int) (progress * 100));
        guiGraphics.drawCenteredString(this.font, "§f" + progressPercent, barX + barW / 2, barY + 2, 0xFFFFFF);

        // Tasks List
        int taskListY = contentY + playCardH + 8;
        TaskDefinition[] tasks = TaskDefinition.values();
        int availableH = this.height - taskListY - 30;
        int taskCardH = Math.max(26, Math.min(32, (availableH - (tasks.length - 1) * 4) / tasks.length));

        for (int i = 0; i < tasks.length; i++) {
            TaskDefinition task = tasks[i];
            int ty = taskListY + i * (taskCardH + 4);
            if (ty + taskCardH > this.height - 26) break;

            boolean completed = manager.getData().isTaskCompleted(task.getId());

            guiGraphics.fill(cardX, ty, cardX + cardW, ty + taskCardH, completed ? 0xCC0F291E : 0xCC1F2937);
            guiGraphics.renderOutline(cardX, ty, cardW, taskCardH, completed ? 0xFF10B981 : 0xFF4B5563);

            guiGraphics.drawString(this.font, (completed ? "§a§l✔ " : "§e§l⏳ ") + task.getTitle(), cardX + 8, ty + 4, 0xFFFFFF, true);
            guiGraphics.drawString(this.font, "§7" + task.getDescription(), cardX + 8, ty + 15, 0xAAAAAA, false);

            String rewardText = "§6+" + task.getCoinReward() + " Coins" + (task.getCapeReward() != null ? " §b+ " + task.getCapeReward().getDisplayName() : "");
            int rWidth = this.font.width(rewardText);
            guiGraphics.drawString(this.font, rewardText, cardX + cardW - rWidth - 8, ty + 5, 0xFFFFFF, true);

            String statusBadge = completed ? "§a[COMPLETED]" : "§7[IN PROGRESS]";
            int sWidth = this.font.width(statusBadge);
            guiGraphics.drawString(this.font, statusBadge, cardX + cardW - sWidth - 8, ty + 16, 0xFFFFFF, false);
        }
    }

    private void renderDevTab(GuiGraphics guiGraphics, CosmeticsManager manager, int contentY) {
        int cardX = 20;
        int cardW = this.width - 40;
        int cardH = this.height - contentY - 30;

        guiGraphics.fill(cardX, contentY, cardX + cardW, contentY + cardH, 0xCC2A1B38);
        guiGraphics.renderOutline(cardX, contentY, cardW, cardH, 0xFFC084FC);

        guiGraphics.drawCenteredString(this.font, "§d§l🛠 DEV MODE ACTIVE: INSTANT TASK & ECONOMY TESTING 🛠", this.width / 2, contentY + 8, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, "§7Use these controls to complete any task on any world or server, add coins/time, or reset.", this.width / 2, contentY + 22, 0xCCCCCC);
    }
}
