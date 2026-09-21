package xyz.alyrion.alyrioncore.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import xyz.alyrion.alyrioncore.item.CrateItem;
import xyz.alyrion.alyrioncore.registry.ModItems;
import xyz.alyrion.alyrioncore.registry.ModMenus;

public class CrateMenu extends ChestMenu {
   private final ItemStack crateStack;
   private final Player player;
   private boolean destroyed = false;

   public CrateMenu(int containerId, Inventory playerInventory, ItemStack crateStack) {
      super((MenuType)ModMenus.CRATE.get(), containerId, playerInventory, new CrateItem.CrateInventory(crateStack), 3);
      this.crateStack = crateStack;
      this.player = playerInventory.player;
   }

   public CrateMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
      this(containerId, playerInventory, (ItemStack)ItemStack.OPTIONAL_STREAM_CODEC.decode(extraData));
   }

   protected Slot addSlot(Slot slot) {
      return this.slots.size() < 27 ? super.addSlot(new CrateMenu.TakeOnlySlot(slot)) : super.addSlot(new CrateMenu.LockedCrateSlot(slot, this.crateStack));
   }

   public void broadcastChanges() {
      super.broadcastChanges();
      this.checkEmptyAndDestroy();
   }

   public void clicked(int slotId, int button, ClickType clickType, Player player) {
      super.clicked(slotId, button, clickType, player);
      this.checkEmptyAndDestroy();
   }

   public ItemStack quickMoveStack(Player player, int index) {
      ItemStack result = super.quickMoveStack(player, index);
      this.checkEmptyAndDestroy();
      return result;
   }

   public void removed(Player player) {
      super.removed(player);
      this.checkEmptyAndDestroy();
   }

   private void checkEmptyAndDestroy() {
      if (!this.destroyed) {
         if (this.getContainer().isEmpty()) {
            if (this.getCarried().isEmpty()) {
               this.destroyed = true;
               this.destroyCrate();
            }
         }
      }
   }

   private void destroyCrate() {
      if (!this.player.level().isClientSide()) {
         this.crateStack.setCount(0);
         Inventory inv = this.player.getInventory();

         for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s == this.crateStack || s.is((Item)ModItems.CRATE.get()) && CrateItem.isCrateEmpty(s)) {
               inv.setItem(i, ItemStack.EMPTY);
            }
         }

         if (this.player.getMainHandItem() == this.crateStack) {
            this.player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
         }

         if (this.player.getOffhandItem() == this.crateStack) {
            this.player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
         }

         inv.setChanged();
         CrateItem.playDestroyEffects(this.player.level(), this.player);
         if (this.player instanceof ServerPlayer serverPlayer && serverPlayer.containerMenu == this) {
            serverPlayer.closeContainer();
         }
      }
   }

   private static final class LockedCrateSlot extends Slot {
      private final ItemStack crateStack;

      private LockedCrateSlot(Slot wrapped, ItemStack crateStack) {
         super(wrapped.container, wrapped.getContainerSlot(), wrapped.x, wrapped.y);
         this.crateStack = crateStack;
      }

      public boolean mayPickup(Player player) {
         return this.getItem() == this.crateStack ? false : super.mayPickup(player);
      }
   }

   private static final class TakeOnlySlot extends Slot {
      private TakeOnlySlot(Slot wrapped) {
         super(wrapped.container, wrapped.getContainerSlot(), wrapped.x, wrapped.y);
      }

      public boolean mayPlace(ItemStack stack) {
         return false;
      }
   }
}
