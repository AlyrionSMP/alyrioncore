package xyz.alyrion.alyrioncore.item;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import xyz.alyrion.alyrioncore.AlyrionCore;
import xyz.alyrion.alyrioncore.menu.CrateMenu;
import xyz.alyrion.alyrioncore.registry.ModItems;

public class CrateItem extends Item {
   public static final int SIZE = 27;

   public CrateItem(Properties properties) {
      super(properties);
   }

   public static ItemStack createFilled(List<ItemStack> contents, List<ItemStack> overflow) {
      NonNullList<ItemStack> slots = NonNullList.withSize(27, ItemStack.EMPTY);
      int slot = 0;

      for (ItemStack source : contents) {
         int remaining = source.getCount();

         while (remaining > 0 && slot < 27) {
            int chunk = Math.min(remaining, source.getMaxStackSize());
            ItemStack part = source.copyWithCount(chunk);
            slots.set(slot++, part);
            remaining -= chunk;
         }

         if (remaining > 0) {
            overflow.add(source.copyWithCount(remaining));
         }
      }

      ItemStack crate = new ItemStack((ItemLike)ModItems.CRATE.get());
      crate.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(slots));
      return crate;
   }

   public static boolean isCrateEmpty(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         ItemContainerContents contents = (ItemContainerContents)stack.get(DataComponents.CONTAINER);
         if (contents == null) {
            return true;
         } else {
            NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
            contents.copyInto(items);

            for (ItemStack item : items) {
               if (!item.isEmpty()) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return true;
      }
   }

   public static void playDestroyEffects(Level level, Player player) {
      level.playSound(
         null, player.getX(), player.getY(), player.getZ(), SoundEvents.WOOD_BREAK, SoundSource.PLAYERS, 1.0F, 0.9F + level.getRandom().nextFloat() * 0.2F
      );
      level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.8F, 1.0F);
      if (level instanceof ServerLevel serverLevel) {
         serverLevel.sendParticles(ParticleTypes.POOF, player.getX(), player.getY() + 0.8, player.getZ(), 12, 0.25, 0.25, 0.25, 0.05);
         serverLevel.sendParticles(
            new ItemParticleOption(ParticleTypes.ITEM, new ItemStack((ItemLike)ModItems.CRATE.get())),
            player.getX(),
            player.getY() + 0.8,
            player.getZ(),
            16,
            0.2,
            0.2,
            0.2,
            0.08
         );
      }
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (player.isSpectator()) {
         return InteractionResultHolder.pass(stack);
      } else if (isCrateEmpty(stack)) {
         if (!level.isClientSide()) {
            stack.shrink(1);
            player.setItemInHand(hand, ItemStack.EMPTY);
            player.getInventory().setChanged();
            playDestroyEffects(level, player);
         }

         return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
      } else {
         if (!level.isClientSide()) {
            Component title = stack.has(DataComponents.CUSTOM_NAME) ? stack.getHoverName() : this.getDescription();
            player.openMenu(
               new SimpleMenuProvider((id, inventory, p) -> new CrateMenu(id, inventory, stack), title),
               buf -> ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack)
            );
         }

         return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
      }
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.literal("§7Portable store delivery."));
      tooltip.add(Component.literal("§7Right-click to open."));
   }

   public static class CrateInventory extends SimpleContainer {
      private final ItemStack crateStack;

      public CrateInventory(ItemStack crateStack) {
         super(27);
         this.crateStack = crateStack;
         ItemContainerContents contents = (ItemContainerContents)crateStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);

         try {
            contents.copyInto(this.getItems());
         } catch (Exception var4) {
            AlyrionCore.LOGGER.debug("Crate contents larger than {} slots, truncating", 27, var4);
         }
      }

      public void setChanged() {
         super.setChanged();
         this.persist();
      }

      public void stopOpen(Player player) {
         this.persist();
      }

      private void persist() {
         if (!this.crateStack.isEmpty()) {
            this.crateStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.getItems()));
         }
      }
   }
}
