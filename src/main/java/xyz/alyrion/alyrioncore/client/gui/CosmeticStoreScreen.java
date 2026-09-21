package xyz.alyrion.alyrioncore.client.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xyz.alyrion.alyrioncore.AlyrionCore;
import xyz.alyrion.alyrioncore.client.renderer.ClientCosmeticsRenderers;
import xyz.alyrion.alyrioncore.client.renderer.CosmeticRenderer;
import xyz.alyrion.alyrioncore.client.renderer.WardrobeRenderer;
import xyz.alyrion.alyrioncore.compat.NumismaticsCompat;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticDefinition;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticType;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticsManager;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticsRegistry;
import xyz.alyrion.alyrioncore.cosmetics.TaskDefinition;
import xyz.alyrion.alyrioncore.network.CosmeticNetworking;
import xyz.alyrion.alyrioncore.store.ItemPackDefinition;
import xyz.alyrion.alyrioncore.store.ItemPacksRegistry;

public class CosmeticStoreScreen extends CosmeticScreen {
   private static final Object TASKS_TAB = new Object();
   private static final Object PACKS_TAB = new Object();
   private static final Object EXCHANGE_TAB = new Object();
   private ItemPackDefinition selectedPack = null;
   private double tasksScroll = 0.0;
   private double catalogScroll = 0.0;
   private String highlightedTaskId = null;
   private long highlightTick = 0L;
   private List<Component> pendingTooltip = null;
   private final List<Object> tabs = new ArrayList<>();
   private int tabIndex = 0;
   private CosmeticDefinition selected = null;

   private int pw() {
      return this.width;
   }

   private int ph() {
      return this.height;
   }

   private int ox() {
      return 0;
   }

   private int oy() {
      return 0;
   }

   private int sideX() {
      return 6;
   }

   private int sideW() {
      return 44;
   }

   private int bodyTop() {
      return 30;
   }

   private int bodyBottom() {
      return this.height - 34;
   }

   private int catW() {
      return Math.max(160, Math.min(260, (int)((double)this.width * 0.28)));
   }

   private int catX() {
      return this.width - 8 - this.catW();
   }

   private int prevX() {
      return this.sideX() + this.sideW() + 8;
   }

   private int prevRight() {
      return this.catX() - 8;
   }

   private int prevCenterX() {
      return this.prevX() + (this.prevRight() - this.prevX()) / 2;
   }

   private int tabH() {
      int avail = this.bodyBottom() - this.bodyTop() - Math.max(1, this.tabs.size()) * 4;
      return Math.max(26, Math.min(38, avail / Math.max(1, this.tabs.size())));
   }

   public CosmeticStoreScreen() {
      super(Component.literal("Alyrion Wardrobe"));
   }

   @Override
   protected void init() {
      this.buildTabs();
      super.init();
   }

   private void buildTabs() {
      this.tabs.clear();

      for (CosmeticType type : CosmeticType.values()) {
         if (!CosmeticsRegistry.getByType(type).isEmpty()) {
            this.tabs.add(type);
         }
      }

      this.tabs.add(PACKS_TAB);
      this.tabs.add(TASKS_TAB);
      if (NumismaticsCompat.isNumismaticsInstalled()) {
         this.tabs.add(EXCHANGE_TAB);
      }
      if (this.tabIndex >= this.tabs.size()) {
         this.tabIndex = 0;
      }

      this.selectDefault();
   }

   private void selectDefault() {
      this.catalogScroll = 0.0;
      if (this.onTasks() || this.onExchange()) {
         this.selected = null;
         this.selectedPack = null;
      } else if (this.onPacks()) {
         this.selected = null;
         List<ItemPackDefinition> packs = ItemPacksRegistry.all();
         if (packs.isEmpty()) {
            this.selectedPack = null;
         } else {
            if (this.selectedPack == null || !packs.contains(this.selectedPack)) {
               this.selectedPack = packs.get(0);
            }
         }
      } else {
         this.selectedPack = null;
         List<CosmeticDefinition> items = this.getSortedCosmetics(this.currentType());
         if (items.isEmpty()) {
            this.selected = null;
         } else {
            if (this.selected == null || this.selected.getType() != this.currentType() || !items.contains(this.selected)) {
               this.selected = items.get(0);
            }
         }
      }
   }

   private List<CosmeticDefinition> getSortedCosmetics(CosmeticType type) {
      if (type == null) {
         return Collections.emptyList();
      } else {
         List<CosmeticDefinition> list = new ArrayList<>(CosmeticsRegistry.getByType(type));
         CosmeticsManager manager = CosmeticsManager.get();
         list.sort((a, b) -> {
            boolean aEquipped = manager.isEquipped(a);
            boolean bEquipped = manager.isEquipped(b);
            if (aEquipped != bEquipped) {
               return aEquipped ? -1 : 1;
            } else {
               boolean aUnlocked = manager.isUnlocked(a);
               boolean bUnlocked = manager.isUnlocked(b);
               if (aUnlocked != bUnlocked) {
                  return aUnlocked ? -1 : 1;
               } else {
                  return 0;
               }
            }
         });
         return list;
      }
   }

   private boolean onTasks() {
      return !this.tabs.isEmpty() && this.tabs.get(this.tabIndex) == TASKS_TAB;
   }

   private boolean onPacks() {
      return !this.tabs.isEmpty() && this.tabs.get(this.tabIndex) == PACKS_TAB;
   }

   private boolean onExchange() {
      return !this.tabs.isEmpty() && this.tabs.get(this.tabIndex) == EXCHANGE_TAB;
   }

   private CosmeticType currentType() {
      if (this.tabs.isEmpty()) {
         return null;
      } else {
         return this.tabs.get(this.tabIndex) instanceof CosmeticType type ? type : null;
      }
   }

   protected void rebuildWidgets() {
      this.clearWidgets();
      this.addRenderableWidget(Button.builder(Component.literal(""), btn -> this.onClose()).bounds(this.ox() + 4, this.oy() + 4, 16, 16).build());

      for (int i = 0; i < this.tabs.size(); i++) {
         int index = i;
         this.addRenderableWidget(Button.builder(Component.literal(""), btn -> {
            this.tabIndex = index;
            this.catalogScroll = 0.0;
            this.selectDefault();
            this.rebuildWidgets();
         }).bounds(this.sideX(), this.bodyTop() + i * (this.tabH() + 4), this.sideW(), this.tabH()).build());
      }
   }

   @Override
   protected void renderBackdrop(GuiGraphics guiGraphics) {
      int x = this.ox();
      int y = this.oy();
      int w = this.pw();
      int h = this.ph();
      guiGraphics.fill(x, y, x + w, y + h, -15723489);
      guiGraphics.fill(x, y, x + w, y + 24, -15986400);
      guiGraphics.fill(x, y + 24, x + w, y + 25, -1395960);
      guiGraphics.fill(x, y + h - 30, x + w, y + h - 29, -1395960);
      guiGraphics.fill(x, y + h - 29, x + w, y + h, -15986400);
   }

   @Override
   protected void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      long tick = currentTick();
      CosmeticsManager manager = CosmeticsManager.get();
      boolean backHover = mouseX >= this.ox() + 4 && mouseX < this.ox() + 20 && mouseY >= this.oy() + 4 && mouseY < this.oy() + 20;
      guiGraphics.fill(this.ox() + 4, this.oy() + 4, this.ox() + 20, this.oy() + 20, backHover ? -14668988 : -15327954);
      guiGraphics.renderOutline(this.ox() + 4, this.oy() + 4, 16, 16, -13549474);
      guiGraphics.drawString(this.font, "§7◀", this.ox() + 8, this.oy() + 8, 16777215, false);
      guiGraphics.drawString(this.font, "§6§lALYRION WARDROBE", this.ox() + 26, this.oy() + 8, 16777215, true);
      String coinText = "§e⛃ §6" + manager.getCoins();
      int coinW = this.font.width(coinText) + 10;
      int coinX = this.ox() + this.pw() - 6 - coinW;
      guiGraphics.fill(coinX, this.oy() + 4, coinX + coinW, this.oy() + 20, -15327954);
      guiGraphics.renderOutline(coinX, this.oy() + 4, coinW, 16, -1395960);
      guiGraphics.drawString(this.font, coinText, coinX + 5, this.oy() + 8, 16777215, true);
      this.renderSidebar(guiGraphics, mouseX, mouseY, tick);
      if (this.onTasks()) {
         this.renderTasksTab(guiGraphics, mouseX, mouseY, tick);
         String hint = "Tasks complete automatically while you play.";
         guiGraphics.drawCenteredString(this.font, this.fit("§7" + hint, this.pw() - 16), this.ox() + this.pw() / 2, this.oy() + this.ph() - 17, 11184810);
      } else if (this.onPacks()) {
         this.renderPacksCatalog(guiGraphics, mouseX, mouseY);
         this.renderPackActionBar(guiGraphics, mouseX, mouseY);

         try {
            this.renderPackPreview(guiGraphics);
         } catch (Throwable var14) {
            AlyrionCore.LOGGER.debug("Store pack preview failed: {}", var14.toString());
         }
      } else if (this.onExchange()) {
         this.renderExchangeTab(guiGraphics, mouseX, mouseY, tick);
         String hint = "1 Alyrion Coin (⛃) = 1 Create Numismatics Spur (⚙)";
         guiGraphics.drawCenteredString(this.font, this.fit("§e" + hint, this.pw() - 16), this.ox() + this.pw() / 2, this.oy() + this.ph() - 17, 16777215);
      } else {
         this.renderCatalog(guiGraphics, mouseX, mouseY, tick);
         this.renderActionBar(guiGraphics, mouseX, mouseY);

         try {
            this.renderPreview(guiGraphics, tick, partialTick, mouseX, mouseY);
         } catch (Throwable var13) {
            AlyrionCore.LOGGER.debug("Store preview failed: {}", var13.toString());
         }
      }

      if (this.pendingTooltip != null) {
         guiGraphics.renderComponentTooltip(this.font, this.pendingTooltip, mouseX, mouseY);
         this.pendingTooltip = null;
      }
   }

   private void renderSidebar(GuiGraphics guiGraphics, int mouseX, int mouseY, long tick) {
      for (int i = 0; i < this.tabs.size(); i++) {
         Object tab = this.tabs.get(i);
         int x = this.sideX();
         int y = this.bodyTop() + i * (this.tabH() + 4);
         boolean active = i == this.tabIndex;
         boolean hover = mouseX >= x && mouseX < x + this.sideW() && mouseY >= y && mouseY < y + this.tabH();
         guiGraphics.fill(x, y, x + this.sideW(), y + this.tabH(), active ? -14404782 : (hover ? -14668988 : -15327954));
         guiGraphics.renderOutline(x, y, this.sideW(), this.tabH(), active ? -10496 : -13549474);
         int iconX = x + (this.sideW() - 16) / 2;
         int iconY = y + 3;
         if (tab instanceof CosmeticType) {
            CosmeticType type = (CosmeticType)tab;
            List<CosmeticDefinition> items = CosmeticsRegistry.getByType(type);
            CosmeticRenderer renderer = ClientCosmeticsRenderers.get(type);
            if (!items.isEmpty() && renderer != null) {
               renderer.drawStoreIcon(guiGraphics, items.get(0), iconX, iconY, 16, tick);
            }

            if (type == CosmeticType.CAPE) {
               this.drawSmallStar(guiGraphics, iconX + 13, iconY - 1);
            }

            guiGraphics.drawCenteredString(
               this.font, this.fit("§7" + type.getDisplayName(), this.sideW() - 4), x + this.sideW() / 2, y + this.tabH() - 10, 16777215
            );
         } else if (tab == PACKS_TAB) {
            List<ItemPackDefinition> packs = ItemPacksRegistry.all();
            if (!packs.isEmpty()) {
               guiGraphics.renderItem(packs.get(0).iconStack(), iconX, iconY);
            }

            guiGraphics.drawCenteredString(this.font, "§7Packs", x + this.sideW() / 2, y + this.tabH() - 10, 16777215);
         } else if (tab == TASKS_TAB) {
            this.drawTaskStar(guiGraphics, iconX, iconY);
            guiGraphics.drawCenteredString(this.font, "§7Tasks", x + this.sideW() / 2, y + this.tabH() - 10, 16777215);
         } else if (tab == EXCHANGE_TAB) {
            Item spur = NumismaticsCompat.getSpurItem();
            if (spur != null && spur != Items.AIR) {
               guiGraphics.renderItem(new ItemStack(spur), iconX, iconY);
            } else {
               guiGraphics.drawCenteredString(this.font, "§e⛃", iconX + 8, iconY + 4, 16777215);
            }
            guiGraphics.drawCenteredString(this.font, "§eExchange", x + this.sideW() / 2, y + this.tabH() - 10, 16777215);
         }
      }
   }

   private void drawSmallStar(GuiGraphics guiGraphics, int x, int y) {
      guiGraphics.fill(x + 1, y, x + 3, y + 1, -10496);
      guiGraphics.fill(x, y + 1, x + 4, y + 3, -10496);
      guiGraphics.fill(x + 1, y + 3, x + 3, y + 4, -10496);
   }

   private void drawTaskStar(GuiGraphics guiGraphics, int x, int y) {
      int cx = x + 8;
      int cy = y + 8;
      int color = -1395960;
      guiGraphics.fill(cx - 2, cy - 5, cx + 2, cy - 2, color);
      guiGraphics.fill(cx - 4, cy - 2, cx + 4, cy + 1, color);
      guiGraphics.fill(cx - 4, cy + 1, cx + 4, cy + 3, color);
      guiGraphics.fill(cx - 2, cy + 3, cx + 2, cy + 5, color);
   }

   private void renderCatalog(GuiGraphics guiGraphics, int mouseX, int mouseY, long tick) {
      CosmeticsManager manager = CosmeticsManager.get();
      CosmeticType type = this.currentType();
      CosmeticRenderer renderer = ClientCosmeticsRenderers.get(type);
      List<CosmeticDefinition> items = this.getSortedCosmetics(type);
      int cardH = 24;
      int listTop = this.bodyTop() + 14;
      int listBottom = this.bodyBottom();
      int contentH = listBottom - listTop;
      int totalHeight = items.size() * (cardH + 3);
      double maxScroll = (double)Math.max(0, totalHeight - contentH);
      this.catalogScroll = Mth.clamp(this.catalogScroll, 0.0, maxScroll);
      int scrollOff = (int)this.catalogScroll;
      guiGraphics.drawString(
         this.font, "§6§l" + type.getDisplayName().toUpperCase() + " §7(" + items.size() + ")", this.catX() + 4, this.bodyTop() + 2, 16777215, true
      );
      if (type == CosmeticType.CAPE) {
         guiGraphics.drawString(
            this.font, "§e★ = Earn via Tasks", this.catX() + this.catW() - this.font.width("§e★ = Earn via Tasks") - 4, this.bodyTop() + 2, 16777215, false
         );
      }

      guiGraphics.enableScissor(this.catX() - 1, listTop, this.catX() + this.catW() + 1, listBottom);
      int y = listTop - scrollOff;

      for (CosmeticDefinition cosmetic : items) {
         if (y + cardH >= listTop && y <= listBottom) {
            boolean isSelected = cosmetic == this.selected;
            boolean hover = mouseX >= this.catX()
               && mouseX < this.catX() + this.catW()
               && mouseY >= y
               && mouseY < y + cardH
               && mouseY >= listTop
               && mouseY <= listBottom;
            TaskDefinition task = TaskDefinition.forReward(cosmetic);
            boolean hasTask = task != null;
            guiGraphics.fill(this.catX(), y, this.catX() + this.catW(), y + cardH, isSelected ? -14997696 : (hover ? -14668988 : -15327954));
            guiGraphics.renderOutline(this.catX(), y, this.catW(), cardH, isSelected ? -10496 : -13549474);
            int iconX = this.catX() + 5;
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
               status = "§e★ Task";
            } else if (cosmetic.isFree()) {
               status = "§dFree";
            } else if (hasTask) {
               status = "§e★ §6" + cosmetic.getPrice() + "⛃";
            } else {
               status = "§6" + cosmetic.getPrice() + "⛃";
            }

            int statusW = this.font.width(status);
            guiGraphics.drawString(this.font, status, this.catX() + this.catW() - statusW - 4, y + (cardH - 8) / 2, 16777215, true);
            String name = (manager.isEquipped(cosmetic) ? "§a" : "§f") + this.fit(cosmetic.getDisplayName(), this.catW() - 24 - statusW - 6);
            guiGraphics.drawString(this.font, name, this.catX() + 22, y + (cardH - 8) / 2, 16777215, false);
            if (hover && hasTask) {
               List<Component> tip = new ArrayList<>();
               tip.add(Component.literal("§6§l" + cosmetic.getDisplayName()));
               tip.add(Component.literal("§e★ Earnable via Task: §f" + task.getTitle()));
               tip.add(Component.literal("§7Goal: " + task.getDescription()));
               boolean done = manager.isTaskCompleted(task.getId());
               tip.add(Component.literal(done ? "§a✔ You already completed this task!" : "§c⏳ Task in progress"));
               if (cosmetic.isPurchasable()) {
                  tip.add(Component.literal("§7(Can also buy for §6" + cosmetic.getPrice() + " Coins§7)"));
               } else {
                  tip.add(Component.literal("§d★ Task Exclusive (cannot be bought)"));
               }

               tip.add(Component.literal("§8▶ Select this cape to view details & jump to task"));
               this.pendingTooltip = tip;
            }
         }

         y += cardH + 3;
      }

      guiGraphics.disableScissor();
      if (maxScroll > 0.0) {
         int trackX = this.catX() + this.catW() - 2;
         guiGraphics.fill(trackX, listTop, trackX + 2, listBottom, -15064784);
         int thumbH = Math.max(12, (int)((long)contentH * (long)contentH / (long)totalHeight));
         int thumbY = listTop + (int)((double)(contentH - thumbH) * (this.catalogScroll / maxScroll));
         guiGraphics.fill(trackX, thumbY, trackX + 2, thumbY + thumbH, -1395960);
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button != 0) {
         return super.mouseClicked(mouseX, mouseY, button);
      } else {
         if (this.onExchange()) {
            if (this.handleExchangeClick(mouseX, mouseY)) {
               return true;
            }
         } else if (this.onPacks()) {
            int bx = this.prevCenterX() - 75;
            int by = this.oy() + this.ph() - 26;
            int bw = 150;
            int bh = 18;
            if (mouseX >= (double)bx && mouseX < (double)(bx + bw) && mouseY >= (double)by && mouseY < (double)(by + bh)) {
               if (this.selectedPack != null) {
                  CosmeticsManager manager = CosmeticsManager.get();
                  if (manager.getCoins() >= this.selectedPack.price()) {
                     CosmeticNetworking.sendPurchaseItemPack(this.selectedPack.id());
                  }
               }

               return true;
            }
         } else if (!this.onTasks() && !this.onExchange() && this.selected != null) {
            TaskDefinition task = TaskDefinition.forReward(this.selected);
            int by = this.oy() + this.ph() - 26;
            int bh = 18;
            if (task != null) {
               int totalW = 230;
               int btnW = 110;
               int gap = 10;
               int taskBtnX = this.prevCenterX() - totalW / 2;
               int actBtnX = taskBtnX + btnW + gap;
               if (mouseX >= (double)taskBtnX && mouseX < (double)(taskBtnX + btnW) && mouseY >= (double)by && mouseY < (double)(by + bh)) {
                  this.navigateToTask(task);
                  return true;
               }

               if (mouseX >= (double)actBtnX && mouseX < (double)(actBtnX + btnW) && mouseY >= (double)by && mouseY < (double)(by + bh)) {
                  this.executeCosmeticAction(this.selected);
                  return true;
               }
            } else {
               int bw = 150;
               int bx = this.prevCenterX() - bw / 2;
               if (mouseX >= (double)bx && mouseX < (double)(bx + bw) && mouseY >= (double)by && mouseY < (double)(by + bh)) {
                  this.executeCosmeticAction(this.selected);
                  return true;
               }
            }
         }

         if (this.onTasks()
            && mouseX >= (double)this.prevX()
            && mouseX < (double)(this.catX() + this.catW())
            && mouseY >= (double)(this.bodyTop() + 36)
            && mouseY < (double)this.bodyBottom()) {
            TaskDefinition[] tasks = TaskDefinition.values();
            int tx = this.prevX();
            int tw = this.catX() + this.catW() - tx;
            int listTop = this.bodyTop() + 36;
            int curY = listTop - (int)this.tasksScroll;

            for (int i = 0; i < tasks.length; i++) {
               TaskDefinition task = tasks[i];
               boolean hasCape = task.getReward() != null;
               int textSpace = hasCape ? tw - 76 : tw - 8;
               String[] lines = this.wrap("§7" + task.getDescription(), textSpace);
               int cardH = 18 + lines.length * 10;
               if (hasCape) {
                  cardH = Math.max(38, cardH + 6);
                  if (mouseX >= (double)(tx + tw - 72)
                     && mouseX <= (double)(tx + tw - 4)
                     && mouseY >= (double)(curY + 4)
                     && mouseY <= (double)(curY + cardH - 4)) {
                     this.navigateToCape(task.getReward());
                     return true;
                  }
               }

               curY += cardH + 3;
            }
         }

         if (!this.onTasks()
            && !this.onExchange()
            && mouseX >= (double)this.catX()
            && mouseX < (double)(this.catX() + this.catW())
            && mouseY >= (double)(this.bodyTop() + 14)
            && mouseY < (double)this.bodyBottom()) {
            int cardH = 24;
            int clickedIndex = (int)((mouseY - (double)(this.bodyTop() + 14) + this.catalogScroll) / (double)(cardH + 3));
            if (this.onPacks()) {
               List<ItemPackDefinition> packs = ItemPacksRegistry.all();
               if (clickedIndex >= 0 && clickedIndex < packs.size()) {
                  this.selectedPack = packs.get(clickedIndex);
                  Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                  return true;
               }
            } else {
               List<CosmeticDefinition> items = this.getSortedCosmetics(this.currentType());
               if (clickedIndex >= 0 && clickedIndex < items.size()) {
                  this.selected = items.get(clickedIndex);
                  Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                  return true;
               }
            }
         }

         return super.mouseClicked(mouseX, mouseY, button);
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      if (scrollY != 0.0) {
         if (this.onTasks()
            && mouseX >= (double)this.prevX()
            && mouseX < (double)(this.catX() + this.catW())
            && mouseY >= (double)this.bodyTop()
            && mouseY < (double)this.bodyBottom()) {
            this.tasksScroll -= scrollY * 16.0;
            return true;
         }

         if (!this.onTasks()
            && !this.onExchange()
            && mouseX >= (double)this.catX()
            && mouseX <= (double)(this.catX() + this.catW() + 10)
            && mouseY >= (double)this.bodyTop()
            && mouseY <= (double)this.bodyBottom()) {
            this.catalogScroll -= scrollY * 16.0;
            return true;
         }
      }

      return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
   }

   private void renderPreview(GuiGraphics guiGraphics, long tick, float partialTick, int mouseX, int mouseY) {
      CosmeticsManager manager = CosmeticsManager.get();
      if (this.selected != null) {
         int px = this.prevX();
         int pw = this.prevRight() - px;
         int modelTop = this.bodyTop() + 10;
         int modelBottom = this.bodyBottom() - 48;
         int availHeight = Math.max(50, modelBottom - modelTop);
         int modelHeight = (int)((float)availHeight * 0.82F);
         int cx = this.prevCenterX();
         int feetY = modelBottom - 8;
         int cy = feetY - modelHeight / 2;
         CosmeticDefinition preview = this.selected;
         CosmeticDefinition cape = preview.getType() == CosmeticType.CAPE ? preview : manager.getEquipped(CosmeticType.CAPE);
         CosmeticDefinition pet = preview.getType() == CosmeticType.PET ? preview : manager.getEquipped(CosmeticType.PET);
         CosmeticDefinition trail = preview.getType() == CosmeticType.TRAIL ? preview : manager.getEquipped(CosmeticType.TRAIL);
         int[] bandW = new int[]{50, 38, 26, 14};

         for (int i = 0; i < bandW.length; i++) {
            int bw = bandW[i];
            int alpha = 85 - i * 16;
            guiGraphics.fill(cx - bw / 2, feetY + 2 + i, cx + bw / 2, feetY + 3 + i, alpha << 24);
         }

         guiGraphics.fill(cx - 20, feetY + 6, cx + 20, feetY + 7, 1726657288);
         WardrobeRenderer.drawPlayerModel(guiGraphics, cx, cy, modelHeight, tick, partialTick, mouseX, mouseY, cape, pet, trail);
         String nameHeader = "§e§l" + this.fit(preview.getDisplayName(), pw - 8);
         if (manager.isEquipped(preview)) {
            nameHeader = nameHeader + " §a[Equipped]";
         } else if (manager.isUnlocked(preview)) {
            nameHeader = nameHeader + " §b[Owned]";
         }

         guiGraphics.drawCenteredString(this.font, nameHeader, cx, modelBottom + 8, 16777215);
         TaskDefinition task = TaskDefinition.forReward(preview);
         if (task != null) {
            boolean done = manager.isTaskCompleted(task.getId());
            guiGraphics.drawCenteredString(
               this.font, this.fit("§e★ Task: §f" + task.getTitle() + (done ? " §a(Completed ✔)" : " §c(Incomplete)"), pw - 8), cx, modelBottom + 20, 16777215
            );
            String goalLine = "§7Goal: " + task.getDescription();
            if (!manager.isUnlocked(preview)) {
               if (preview.isPurchasable()) {
                  goalLine = goalLine + " §8· §6" + preview.getPrice() + " Coins";
               } else {
                  goalLine = goalLine + " §8· §dTask Exclusive";
               }
            }

            guiGraphics.drawCenteredString(this.font, this.fit(goalLine, pw - 8), cx, modelBottom + 31, 11184810);
         } else {
            String info;
            if (manager.isEquipped(preview)) {
               info = "§aEquipped";
            } else if (manager.isUnlocked(preview)) {
               info = "§bOwned";
            } else if (preview.isFree()) {
               info = "§dFree — claim it";
            } else {
               info = "§6"
                  + preview.getPrice()
                  + " Coins"
                  + (manager.getCoins() >= preview.getPrice() ? "" : " §7(need " + (preview.getPrice() - manager.getCoins()) + " more)");
            }

            guiGraphics.drawCenteredString(this.font, this.fit(info, pw - 8), cx, modelBottom + 20, 16777215);
         }
      }
   }

   private void renderPacksCatalog(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      List<ItemPackDefinition> packs = ItemPacksRegistry.all();
      int cardH = 24;
      int listTop = this.bodyTop() + 14;
      int listBottom = this.bodyBottom();
      int contentH = listBottom - listTop;
      int totalHeight = packs.size() * (cardH + 3);
      double maxScroll = (double)Math.max(0, totalHeight - contentH);
      this.catalogScroll = Mth.clamp(this.catalogScroll, 0.0, maxScroll);
      int scrollOff = (int)this.catalogScroll;
      guiGraphics.drawString(this.font, "§6§lITEM PACKS §7(" + packs.size() + ")", this.catX() + 4, this.bodyTop() + 2, 16777215, true);
      guiGraphics.enableScissor(this.catX() - 1, listTop, this.catX() + this.catW() + 1, listBottom);
      int y = listTop - scrollOff;

      for (ItemPackDefinition pack : packs) {
         if (y + cardH >= listTop && y <= listBottom) {
            boolean isSelected = pack == this.selectedPack;
            boolean hover = mouseX >= this.catX()
               && mouseX < this.catX() + this.catW()
               && mouseY >= y
               && mouseY < y + cardH
               && mouseY >= listTop
               && mouseY <= listBottom;
            guiGraphics.fill(this.catX(), y, this.catX() + this.catW(), y + cardH, isSelected ? -14997696 : (hover ? -14668988 : -15327954));
            guiGraphics.renderOutline(this.catX(), y, this.catW(), cardH, isSelected ? -10496 : -13549474);
            guiGraphics.renderItem(pack.iconStack(), this.catX() + 4, y + (cardH - 16) / 2);
            guiGraphics.renderItemDecorations(this.font, pack.iconStack(), this.catX() + 4, y + (cardH - 16) / 2);
            String status = "§6" + pack.price() + "⛃";
            int statusW = this.font.width(status);
            guiGraphics.drawString(this.font, status, this.catX() + this.catW() - statusW - 4, y + (cardH - 8) / 2, 16777215, true);
            String name = "§f" + this.fit(pack.displayName(), this.catW() - 24 - statusW - 6);
            guiGraphics.drawString(this.font, name, this.catX() + 23, y + (cardH - 8) / 2, 16777215, false);
         }

         y += cardH + 3;
      }

      guiGraphics.disableScissor();
      if (maxScroll > 0.0) {
         int trackX = this.catX() + this.catW() - 2;
         guiGraphics.fill(trackX, listTop, trackX + 2, listBottom, -15064784);
         int thumbH = Math.max(12, (int)((long)contentH * (long)contentH / (long)totalHeight));
         int thumbY = listTop + (int)((double)(contentH - thumbH) * (this.catalogScroll / maxScroll));
         guiGraphics.fill(trackX, thumbY, trackX + 2, thumbY + thumbH, -1395960);
      }
   }

   private void renderPackPreview(GuiGraphics guiGraphics) {
      if (this.selectedPack != null) {
         CosmeticsManager manager = CosmeticsManager.get();
         int px = this.prevX();
         int pw = this.prevRight() - px;
         int cx = this.prevCenterX();
         int iconSize = 36;
         int iconY = this.bodyTop() + 14;
         guiGraphics.fill(cx - iconSize / 2 - 4, iconY - 4, cx + iconSize / 2 + 4, iconY + iconSize + 4, -15327954);
         guiGraphics.renderOutline(cx - iconSize / 2 - 4, iconY - 4, iconSize + 8, iconSize + 8, -1395960);
         guiGraphics.renderItem(this.selectedPack.iconStack(), cx - 8, iconY + 8);
         guiGraphics.renderItemDecorations(this.font, this.selectedPack.iconStack(), cx - 8, iconY + 8);
         guiGraphics.drawCenteredString(this.font, "§e§l" + this.fit(this.selectedPack.displayName(), pw - 8), cx, iconY + iconSize + 10, 16777215);
         String info;
         if (manager.getCoins() >= this.selectedPack.price()) {
            info = "§6" + this.selectedPack.price() + " Coins — ready to buy";
         } else {
            info = "§6" + this.selectedPack.price() + " Coins §7(need " + (this.selectedPack.price() - manager.getCoins()) + " more)";
         }

         guiGraphics.drawCenteredString(this.font, this.fit(info, pw - 8), cx, iconY + iconSize + 22, 16777215);
         guiGraphics.drawCenteredString(this.font, this.fit("§7" + this.selectedPack.description(), this.tw()), cx, iconY + iconSize + 38, 11184810);
         this.renderPackContents(guiGraphics, cx, iconY + iconSize + 50);
      }
   }

   private int tw() {
      return this.prevRight() - this.prevX();
   }

   @Nullable
   private static RegistryAccess clientRegistryAccess() {
      Level level = Minecraft.getInstance().level;
      return level != null ? level.registryAccess() : null;
   }

   private void renderPackContents(GuiGraphics guiGraphics, int centerX, int topY) {
      if (this.selectedPack.delivery() != ItemPackDefinition.Delivery.CLAIM_CHUNKS && this.selectedPack.claimChunks() <= 0) {
         List<ItemStack> contents = new ArrayList<>();

         for (ItemStack stack : this.selectedPack.buildContents(clientRegistryAccess())) {
            if (!stack.isEmpty()) {
               contents.add(stack);
            }
         }

         int cell = 20;
         int cols = Math.max(1, this.tw() / (cell + 2));
         int rows = Math.min(3, (contents.size() + cols - 1) / cols);
         guiGraphics.drawCenteredString(this.font, "§7Contains:", centerX, topY, 11184810);

         for (int i = 0; i < Math.min(contents.size(), rows * cols); i++) {
            ItemStack stackx = contents.get(i);
            int col = i % cols;
            int row = i / cols;
            int x = centerX - (cols * (cell + 2) - 2) / 2 + col * (cell + 2);
            int y = topY + 12 + row * (cell + 2);
            guiGraphics.renderItem(stackx, x, y);
            guiGraphics.renderItemDecorations(this.font, stackx, x, y);
         }
      } else {
         int chunks = this.selectedPack.claimChunks();
         guiGraphics.drawCenteredString(this.font, "§7Grants: §a+" + chunks + " Claim Chunk" + (chunks > 1 ? "s" : ""), centerX, topY, 16777215);
         guiGraphics.drawCenteredString(this.font, "§8(Open Parties and Claims)", centerX, topY + 12, 8947848);
      }
   }

   private void renderPackActionBar(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      if (this.selectedPack != null) {
         CosmeticsManager manager = CosmeticsManager.get();
         int bx = this.prevCenterX() - 75;
         int by = this.oy() + this.ph() - 26;
         int bw = 150;
         int bh = 18;
         boolean hover = mouseX >= bx && mouseX < bx + bw && mouseY >= by && mouseY < by + bh;
         boolean canAfford = manager.getCoins() >= this.selectedPack.price();
         String label = "§6BUY · " + this.selectedPack.price() + " ⛃";
         int fill = canAfford ? -12964846 : -15064784;
         int border = canAfford ? -4617681 : -13549474;
         guiGraphics.fill(bx, by, bx + bw, by + bh, canAfford && hover ? -14011056 : fill);
         guiGraphics.renderOutline(bx, by, bw, bh, border);
         guiGraphics.drawCenteredString(this.font, canAfford ? label : "§7" + label.substring(2), bx + bw / 2, by + 5, 16777215);
      }
   }

   private void renderActionBar(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      if (this.selected != null) {
         CosmeticsManager manager = CosmeticsManager.get();
         TaskDefinition task = TaskDefinition.forReward(this.selected);
         int by = this.oy() + this.ph() - 26;
         int bh = 18;
         if (task != null) {
            int totalW = 230;
            int btnW = 110;
            int gap = 10;
            int taskBtnX = this.prevCenterX() - totalW / 2;
            int actBtnX = taskBtnX + btnW + gap;
            boolean taskHover = mouseX >= taskBtnX && mouseX < taskBtnX + btnW && mouseY >= by && mouseY < by + bh;
            guiGraphics.fill(taskBtnX, by, taskBtnX + btnW, by + bh, taskHover ? -14009772 : -14997696);
            guiGraphics.renderOutline(taskBtnX, by, btnW, bh, taskHover ? -10496 : -1395960);
            guiGraphics.drawCenteredString(this.font, "§e★ VIEW TASK", taskBtnX + btnW / 2, by + 5, 16777215);
            this.renderSingleActionButton(guiGraphics, mouseX, mouseY, actBtnX, by, btnW, bh, this.selected, manager);
         } else {
            int bw = 150;
            int bx = this.prevCenterX() - bw / 2;
            this.renderSingleActionButton(guiGraphics, mouseX, mouseY, bx, by, bw, bh, this.selected, manager);
         }
      }
   }

   private void renderSingleActionButton(
      GuiGraphics guiGraphics, int mouseX, int mouseY, int bx, int by, int bw, int bh, CosmeticDefinition cosmetic, CosmeticsManager manager
   ) {
      boolean hover = mouseX >= bx && mouseX < bx + bw && mouseY >= by && mouseY < by + bh;
      boolean isEquipped = manager.isEquipped(cosmetic);
      boolean isUnlocked = manager.isUnlocked(cosmetic);
      String label;
      boolean enabled;
      int fill;
      int border;
      if (isEquipped) {
         label = "§cUNEQUIP";
         enabled = true;
         fill = -12969182;
         border = -7718073;
      } else if (isUnlocked) {
         label = "§aEQUIP";
         enabled = true;
         fill = -15255002;
         border = -13661609;
      } else if (!cosmetic.isPurchasable()) {
         label = "§dTASK REWARD";
         enabled = false;
         fill = -15064784;
         border = -13549474;
      } else if (cosmetic.isFree()) {
         label = "§bCLAIM FREE";
         enabled = true;
         fill = -15582652;
         border = -13661536;
      } else {
         boolean canAfford = manager.getCoins() >= cosmetic.getPrice();
         label = "§6BUY · " + cosmetic.getPrice() + " ⛃";
         enabled = canAfford;
         fill = canAfford ? -12964846 : -15064784;
         border = canAfford ? -4617681 : -13549474;
      }

      guiGraphics.fill(bx, by, bx + bw, by + bh, enabled && hover ? -14011056 : fill);
      guiGraphics.renderOutline(bx, by, bw, bh, border);
      guiGraphics.drawCenteredString(this.font, enabled ? label : "§7" + label.substring(2), bx + bw / 2, by + 5, 16777215);
   }

   private void executeCosmeticAction(CosmeticDefinition def) {
      CosmeticsManager manager = CosmeticsManager.get();
      if (manager.isEquipped(def)) {
         manager.unequip(def.getType());
      } else if (manager.isUnlocked(def)) {
         manager.equip(def);
      } else if (def.isPurchasable() && manager.getCoins() >= def.getPrice()) {
         manager.purchase(def);
      }

      this.rebuildWidgets();
   }

   private void navigateToTask(TaskDefinition task) {
      int idx = this.tabs.indexOf(TASKS_TAB);
      if (idx >= 0) {
         this.tabIndex = idx;
         this.highlightedTaskId = task.getId();
         this.highlightTick = currentTick();
         TaskDefinition[] tasks = TaskDefinition.values();
         int tx = this.prevX();
         int tw = this.catX() + this.catW() - tx;
         int listTop = this.bodyTop() + 36;
         int listBottom = this.bodyBottom();
         int contentH = listBottom - listTop;
         int yAccum = 0;
         int targetY = 0;
         int targetH = 36;

         for (int i = 0; i < tasks.length; i++) {
            boolean hasCape = tasks[i].getReward() != null;
            int textSpace = hasCape ? tw - 76 : tw - 8;
            String[] lines = this.wrap("§7" + tasks[i].getDescription(), textSpace);
            int h = 18 + lines.length * 10;
            if (hasCape) {
               h = Math.max(38, h + 6);
            }

            if (tasks[i] == task) {
               targetY = yAccum;
               targetH = h;
            }

            yAccum += h + 3;
         }

         double maxScroll = (double)Math.max(0, yAccum - contentH);
         this.tasksScroll = Mth.clamp((double)(targetY - (contentH - targetH) / 2), 0.0, maxScroll);
         Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
         this.rebuildWidgets();
      }
   }

   private void navigateToCape(CosmeticDefinition cosmetic) {
      int idx = this.tabs.indexOf(CosmeticType.CAPE);
      if (idx >= 0) {
         this.tabIndex = idx;
         this.selected = cosmetic;
         this.highlightTick = currentTick();
         List<CosmeticDefinition> items = this.getSortedCosmetics(CosmeticType.CAPE);
         int itemIndex = items.indexOf(cosmetic);
         if (itemIndex >= 0) {
            int cardH = 24;
            int listTop = this.bodyTop() + 14;
            int listBottom = this.bodyBottom();
            int contentH = listBottom - listTop;
            int totalHeight = items.size() * (cardH + 3);
            double maxScroll = (double)Math.max(0, totalHeight - contentH);
            int targetY = itemIndex * (cardH + 3);
            this.catalogScroll = Mth.clamp((double)(targetY - (contentH - cardH) / 2), 0.0, maxScroll);
         }

         Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
         this.rebuildWidgets();
      }
   }

   private void renderTasksTab(GuiGraphics guiGraphics, int mouseX, int mouseY, long tick) {
      CosmeticsManager manager = CosmeticsManager.get();
      int tx = this.prevX();
      int tw = this.catX() + this.catW() - tx;
      int py = this.bodyTop();
      int playH = 30;
      guiGraphics.fill(tx, py, tx + tw, py + playH, -15327954);
      guiGraphics.renderOutline(tx, py, tw, playH, -13549474);
      long seconds = manager.getPlaytimeSeconds();
      long hours = seconds / 3600L;
      long minutes = seconds % 3600L / 60L;
      long secs = seconds % 60L;
      CosmeticDefinition equippedCape = manager.getEquipped(CosmeticType.CAPE);
      boolean isPalestineEquipped = equippedCape != null && "palestine".equalsIgnoreCase(equippedCape.getId());
      String rateLabel = isPalestineEquipped ? "§a1.1 Coins / 1h §7(§a1.1x Multiplier§7)" : "§61 Coin / 1h";
      guiGraphics.drawString(
         this.font,
         this.fit(String.format("§eSurvival playtime: §f%dh %02dm %02ds §7| " + rateLabel, hours, minutes, secs), tw - 8),
         tx + 4,
         py + 4,
         16777215,
         true
      );
      int barX = tx + 4;
      int barY = py + 16;
      int barW = tw - 8;
      int barH = 9;
      int cycleSecs = 3600;
      long cycleProgress = seconds % (long)cycleSecs;
      float progress = Mth.clamp((float)cycleProgress / (float)cycleSecs, 0.0F, 1.0F);
      guiGraphics.fill(barX, barY, barX + barW, barY + barH, -16777216);
      guiGraphics.fill(barX + 1, barY + 1, barX + 1 + (int)((float)(barW - 2) * progress), barY + barH - 1, isPalestineEquipped ? -15681151 : -680437);
      guiGraphics.renderOutline(barX, barY, barW, barH, isPalestineEquipped ? -16411031 : -9735552);
      long remainingSecs = (long)cycleSecs - cycleProgress;
      String progressLabel = isPalestineEquipped
         ? String.format(
            "§fNext in %dm %02ds (%d%%) §a[1.1x Boost]",
            remainingSecs / 60L,
            remainingSecs % 60L,
            (int)(progress * 100.0F)
         )
         : String.format("§fNext in %dm %02ds (%d%%)", remainingSecs / 60L, remainingSecs % 60L, (int)(progress * 100.0F));
      guiGraphics.drawCenteredString(this.font, this.fit(progressLabel, barW - 4), barX + barW / 2, barY + 1, 16777215);
      int listTop = py + playH + 6;
      int listBottom = this.bodyBottom();
      TaskDefinition[] tasks = TaskDefinition.values();
      int[] heights = new int[tasks.length];
      String[][] wrapped = new String[tasks.length][];
      int totalHeight = 0;

      for (int i = 0; i < tasks.length; i++) {
         boolean hasCape = tasks[i].getReward() != null;
         int textSpace = hasCape ? tw - 76 : tw - 8;
         wrapped[i] = this.wrap("§7" + tasks[i].getDescription(), textSpace);
         int baseH = 18 + wrapped[i].length * 10;
         if (hasCape) {
            baseH = Math.max(38, baseH + 6);
         }

         heights[i] = baseH;
         totalHeight += heights[i];
      }

      totalHeight += Math.max(0, tasks.length - 1) * 3;
      int contentH = listBottom - listTop;
      double maxScroll = (double)Math.max(0, totalHeight - contentH);
      this.tasksScroll = Mth.clamp(this.tasksScroll, 0.0, maxScroll);
      int scrollOff = (int)this.tasksScroll;
      guiGraphics.enableScissor(tx, listTop, tx + tw, listBottom);
      int ty = listTop - scrollOff;

      for (int i = 0; i < tasks.length; i++) {
         TaskDefinition task = tasks[i];
         int cardH = heights[i];
         if (ty + cardH > listTop && ty < listBottom) {
            boolean completed = manager.isTaskCompleted(task.getId());
            boolean hasCape = task.getReward() != null;
            boolean isHighlighted = task.getId().equalsIgnoreCase(this.highlightedTaskId);
            int fillCol = completed ? -15783650 : (hasCape ? -15261645 : -14735049);
            int borderCol = completed ? -15681151 : (hasCape ? -1395960 : -11840157);
            guiGraphics.fill(tx, ty, tx + tw, ty + cardH, fillCol);
            guiGraphics.renderOutline(tx, ty, tw, cardH, borderCol);
            if (isHighlighted) {
               int pulseAlpha = (int)(160.0 + 95.0 * Math.sin((double)(tick - this.highlightTick) * 0.25));
               guiGraphics.renderOutline(tx - 1, ty - 1, tw + 2, cardH + 2, pulseAlpha << 24 | 16776960);
            }

            String reward = "§6+" + task.getCoinReward() + "⛃";
            int rW = this.font.width(reward);
            guiGraphics.drawString(this.font, reward, tx + tw - rW - 4, ty + 4, 16777215, true);
            int titleMaxW = tw - rW - (hasCape ? 76 : 14);
            guiGraphics.drawString(this.font, (completed ? "§a✔ " : "§e⏳ ") + this.fit(task.getTitle(), titleMaxW), tx + 4, ty + 4, 16777215, true);
            if (!hasCape) {
               for (int l = 0; l < wrapped[i].length; l++) {
                  guiGraphics.drawString(this.font, wrapped[i][l], tx + 4, ty + 14 + l * 10, 11184810, false);
               }
            } else {
               guiGraphics.drawString(this.font, "§e★ Cape: §b" + this.fit(task.getReward().getDisplayName(), titleMaxW), tx + 4, ty + 14, 16777215, false);

               for (int l = 0; l < wrapped[i].length; l++) {
                  guiGraphics.drawString(this.font, wrapped[i][l], tx + 4, ty + 24 + l * 10, 11184810, false);
               }

               int capeIconX = tx + tw - 68;
               int capeIconY = ty + (cardH - 16) / 2;
               CosmeticRenderer capeRenderer = ClientCosmeticsRenderers.get(CosmeticType.CAPE);
               if (capeRenderer != null) {
                  capeRenderer.drawStoreIcon(guiGraphics, task.getReward(), capeIconX, capeIconY, 16, tick);
               }

               int btnW = 46;
               int btnH = 14;
               int btnX = tx + tw - btnW - 4;
               int btnY = ty + (cardH - btnH) / 2;
               boolean btnHover = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
               guiGraphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnHover ? -14011056 : -15327954);
               guiGraphics.renderOutline(btnX, btnY, btnW, btnH, btnHover ? -10785132 : -13549474);
               guiGraphics.drawCenteredString(this.font, btnHover ? "§fVIEW ▶" : "§7VIEW ▶", btnX + btnW / 2, btnY + 3, 16777215);
            }
         }

         ty += cardH + 3;
      }

      guiGraphics.disableScissor();
      if (this.highlightedTaskId != null && this.highlightTick > 0L && tick - this.highlightTick > 100L) {
         this.highlightedTaskId = null;
      }

      if (maxScroll > 0.0) {
         int trackX = tx + tw - 3;
         guiGraphics.fill(trackX, listTop, trackX + 2, listBottom, -15064784);
         int thumbH = Math.max(12, (int)((long)contentH * (long)contentH / (long)totalHeight));
         int thumbY = listTop + (int)((double)(contentH - thumbH) * (this.tasksScroll / maxScroll));
         guiGraphics.fill(trackX, thumbY, trackX + 2, thumbY + thumbH, -1395960);
      }
   }

   private String[] wrap(String text, int maxW) {
      List<String> lines = new ArrayList<>();
      StringBuilder line = new StringBuilder();

      for (String word : text.split(" ")) {
         String candidate = line.length() == 0 ? word : line + " " + word;
         if (this.font.width(candidate) <= maxW) {
            line = new StringBuilder(candidate);
         } else if (this.font.width(word) <= maxW) {
            lines.add(line.toString());
            line = new StringBuilder(word);
         } else {
            if (line.length() > 0) {
               lines.add(line.toString());
               line = new StringBuilder();
            }

            String last = null;

            for (char c : word.toCharArray()) {
               String next = last == null ? String.valueOf(c) : last + c;
               if (this.font.width(next) > maxW && last != null) {
                  lines.add(last);
                  last = String.valueOf(c);
               } else {
                  last = next;
               }
            }

            if (last != null) {
               line = new StringBuilder(last);
            }
         }
      }

      if (line.length() > 0) {
         lines.add(line.toString());
      }

      return lines.toArray(new String[0]);
   }

   private void renderExchangeTab(GuiGraphics guiGraphics, int mouseX, int mouseY, long tick) {
      CosmeticsManager manager = CosmeticsManager.get();
      int cx = this.prevX();
      int cw = this.catX() + this.catW() - cx;
      int py = this.bodyTop();

      // Top banner
      int bannerH = 44;
      guiGraphics.fill(cx, py, cx + cw, py + bannerH, -15327954);
      guiGraphics.renderOutline(cx, py, cw, bannerH, -13549474);
      guiGraphics.drawString(this.font, "§6§lCREATE: NUMISMATICS CURRENCY EXCHANGE", cx + 8, py + 6, 16777215, true);
      guiGraphics.drawString(this.font, "§7Convert seamlessly between Alyrion Coins and Create Numismatics Spurs.", cx + 8, py + 18, 11184810, false);
      guiGraphics.drawString(this.font, "§aRate: §e1 Alyrion Coin (⛃) §7= §61 Numismatics Spur (⚙)", cx + 8, py + 29, 16777215, true);

      // Balance bar
      int balY = py + bannerH + 6;
      int balH = 22;
      guiGraphics.fill(cx, balY, cx + cw, balY + balH, -15723489);
      guiGraphics.renderOutline(cx, balY, cw, balH, -13549474);
      String coinBalance = "§e⛃ Alyrion Coins: §6" + manager.getCoins();
      guiGraphics.drawString(this.font, coinBalance, cx + 8, balY + 7, 16777215, true);

      int spurs = NumismaticsCompat.countSpurs(Minecraft.getInstance().player);
      String spurBalance = "§6⚙ Inventory Spurs: §e" + spurs;
      int sbW = this.font.width(spurBalance);
      int spurIconX = cx + cw - sbW - 28;
      int spurIconY = balY + 3;
      Item spurItem = NumismaticsCompat.getSpurItem();
      if (spurItem != null && spurItem != Items.AIR) {
         guiGraphics.renderItem(new ItemStack(spurItem), spurIconX, spurIconY);
      }
      guiGraphics.drawString(this.font, spurBalance, cx + cw - sbW - 8, balY + 7, 16777215, true);

      // Two Cards: Withdraw & Deposit
      int cardsY = balY + balH + 8;
      int cardH = this.bodyBottom() - cardsY - 4;
      int gap = 8;
      int cardW = (cw - gap) / 2;
      int btnH = 20;
      int btnGap = 6;
      int btnTop = cardsY + 54;
      int btnW = cardW - 16;

      // Card 1: Withdraw Coins -> Spurs
      int leftCardX = cx;
      guiGraphics.fill(leftCardX, cardsY, leftCardX + cardW, cardsY + cardH, -15327954);
      guiGraphics.renderOutline(leftCardX, cardsY, cardW, cardH, -13549474);
      guiGraphics.drawString(this.font, "§e⛃ Coins §7➔ §6⚙ Spurs", leftCardX + 8, cardsY + 8, 16777215, true);
      guiGraphics.drawString(this.font, "§7Withdraw coins into physical Spurs to trade.", leftCardX + 8, cardsY + 22, 11184810, false);
      guiGraphics.drawString(this.font, "§8Received directly into inventory.", leftCardX + 8, cardsY + 33, 7829367, false);

      String[] withdrawLabels = new String[] {
         "Withdraw 1 Spur   (§6-1 Coin§f)",
         "Withdraw 16 Spurs (§6-16 Coins§f)",
         "Withdraw 64 Spurs (§6-64 Coins§f)",
         "Withdraw All (§6-" + manager.getCoins() + " Coins§f)"
      };
      int[] withdrawCosts = new int[] { 1, 16, 64, manager.getCoins() };

      int leftBtnX = leftCardX + 8;
      for (int i = 0; i < 4; i++) {
         int by = btnTop + i * (btnH + btnGap);
         boolean canAfford = withdrawCosts[i] > 0 && manager.getCoins() >= withdrawCosts[i];
         boolean hover = canAfford && mouseX >= leftBtnX && mouseX < leftBtnX + btnW && mouseY >= by && mouseY < by + btnH;
         int bg = canAfford ? (hover ? -14011056 : -15064784) : -15921907;
         int border = canAfford ? (hover ? -10496 : -13549474) : -15527148;
         guiGraphics.fill(leftBtnX, by, leftBtnX + btnW, by + btnH, bg);
         guiGraphics.renderOutline(leftBtnX, by, btnW, btnH, border);
         int textCol = canAfford ? 16777215 : 7829367;
         guiGraphics.drawCenteredString(this.font, withdrawLabels[i], leftBtnX + btnW / 2, by + 6, textCol);
      }

      // Card 2: Deposit Spurs -> Coins
      int rightCardX = cx + cardW + gap;
      guiGraphics.fill(rightCardX, cardsY, rightCardX + cardW, cardsY + cardH, -15327954);
      guiGraphics.renderOutline(rightCardX, cardsY, cardW, cardH, -13549474);
      guiGraphics.drawString(this.font, "§6⚙ Spurs §7➔ §e⛃ Coins", rightCardX + 8, cardsY + 8, 16777215, true);
      guiGraphics.drawString(this.font, "§7Deposit physical Spurs from inventory.", rightCardX + 8, cardsY + 22, 11184810, false);
      guiGraphics.drawString(this.font, "§8Credited to your Alyrion wallet.", rightCardX + 8, cardsY + 33, 7829367, false);

      String[] depositLabels = new String[] {
         "Deposit 1 Spur   (§a+1 Coin§f)",
         "Deposit 16 Spurs (§a+16 Coins§f)",
         "Deposit 64 Spurs (§a+64 Coins§f)",
         "Deposit All (§a+" + spurs + " Coins§f)"
      };
      int[] depositReqs = new int[] { 1, 16, 64, spurs };

      int rightBtnX = rightCardX + 8;
      for (int i = 0; i < 4; i++) {
         int by = btnTop + i * (btnH + btnGap);
         boolean hasSpurs = depositReqs[i] > 0 && spurs >= depositReqs[i];
         boolean hover = hasSpurs && mouseX >= rightBtnX && mouseX < rightBtnX + btnW && mouseY >= by && mouseY < by + btnH;
         int bg = hasSpurs ? (hover ? -14011056 : -15064784) : -15921907;
         int border = hasSpurs ? (hover ? -10496 : -13549474) : -15527148;
         guiGraphics.fill(rightBtnX, by, rightBtnX + btnW, by + btnH, bg);
         guiGraphics.renderOutline(rightBtnX, by, btnW, btnH, border);
         int textCol = hasSpurs ? 16777215 : 7829367;
         guiGraphics.drawCenteredString(this.font, depositLabels[i], rightBtnX + btnW / 2, by + 6, textCol);
      }
   }

   private boolean handleExchangeClick(double mouseX, double mouseY) {
      if (!NumismaticsCompat.isNumismaticsInstalled()) {
         return false;
      }
      CosmeticsManager manager = CosmeticsManager.get();
      int cx = this.prevX();
      int cw = this.catX() + this.catW() - cx;
      int py = this.bodyTop();
      int bannerH = 44;
      int cardsY = py + bannerH + 36;
      int gap = 8;
      int cardW = (cw - gap) / 2;
      int btnH = 20;
      int btnGap = 6;
      int btnTop = cardsY + 54;
      int btnW = cardW - 16;

      // Left Card: Withdraw Coins -> Spurs
      int leftCardX = cx;
      int leftBtnX = leftCardX + 8;
      int coins = manager.getCoins();
      int[] amounts = new int[] { 1, 16, 64, coins };

      for (int i = 0; i < 4; i++) {
         int by = btnTop + i * (btnH + btnGap);
         if (mouseX >= (double)leftBtnX && mouseX < (double)(leftBtnX + btnW) && mouseY >= (double)by && mouseY < (double)(by + btnH)) {
            int amt = amounts[i];
            if (amt > 0 && coins >= amt) {
               CosmeticNetworking.sendConvertCurrency(true, amt);
               Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
               return true;
            }
         }
      }

      // Right Card: Deposit Spurs -> Coins
      int rightCardX = cx + cardW + gap;
      int rightBtnX = rightCardX + 8;
      int spurs = NumismaticsCompat.countSpurs(Minecraft.getInstance().player);
      int[] spurAmounts = new int[] { 1, 16, 64, spurs };

      for (int i = 0; i < 4; i++) {
         int by = btnTop + i * (btnH + btnGap);
         if (mouseX >= (double)rightBtnX && mouseX < (double)(rightBtnX + btnW) && mouseY >= (double)by && mouseY < (double)(by + btnH)) {
            int amt = spurAmounts[i];
            if (amt > 0 && spurs >= amt) {
               CosmeticNetworking.sendConvertCurrency(false, amt);
               Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
               return true;
            }
         }
      }

      return false;
   }
}
