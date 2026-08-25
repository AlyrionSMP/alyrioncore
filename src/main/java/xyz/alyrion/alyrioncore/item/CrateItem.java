package xyz.alyrion.alyrioncore.item;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import xyz.alyrion.alyrioncore.menu.CrateMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import xyz.alyrion.alyrioncore.AlyrionCore;

import java.util.List;

/**
 * A "Crate" — a portable chest that carries the contents of a purchased item
 * pack inside the vanilla {@link DataComponents#CONTAINER} component (the
 * 1.21 data-component replacement for chest NBT).
 *
 * Right-clicking opens a stock vanilla 3-row chest GUI. The server binds the
 * menu to a {@link CrateInventory} wrapper that persists every change straight
 * back into the item stack, so the crate behaves like a shulker box without
 * any custom menu type, screen, block entity or networking — the vanilla
 * client renders the familiar chest screen on its own.
 */
public class CrateItem extends Item {

    /** Slot count of the crate's inventory (3 rows of 9, like a single chest). */
    public static final int SIZE = 27;

    public CrateItem(Properties properties) {
        super(properties);
    }

    /**
     * Build a crate pre-filled with the given stacks. Counts larger than a
     * slot's max stack size are split across multiple slots; anything beyond
     * {@link #SIZE} slots is returned to be handed out separately.
     */
    public static ItemStack createFilled(List<ItemStack> contents, List<ItemStack> overflow) {
        NonNullList<ItemStack> slots = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        int slot = 0;
        for (ItemStack source : contents) {
            int remaining = source.getCount();
            while (remaining > 0 && slot < SIZE) {
                int chunk = Math.min(remaining, source.getMaxStackSize());
                ItemStack part = source.copyWithCount(chunk);
                slots.set(slot++, part);
                remaining -= chunk;
            }
            if (remaining > 0) {
                overflow.add(source.copyWithCount(remaining));
            }
        }

        ItemStack crate = new ItemStack(xyz.alyrion.alyrioncore.registry.ModItems.CRATE.get());
        crate.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(slots));
        return crate;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isSpectator()) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide()) {
            Component title = stack.has(DataComponents.CUSTOM_NAME)
                    ? stack.getHoverName()
                    : this.getDescription();
            // The stack travels in the open packet's extra buffer; the client-side
            // CrateMenu factory decodes it from the same buffer.
            player.openMenu(new SimpleMenuProvider(
                    (id, inventory, p) -> new CrateMenu(id, inventory, stack),
                    title), buf -> ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7Portable store delivery."));
        tooltip.add(Component.literal("§7Right-click to open."));
    }

    /**
     * A {@link net.minecraft.world.SimpleContainer} view of the crate stack's
     * {@code CONTAINER} component. Every mutation is written back into the
     * stack immediately ({@link #setChanged}) and once more when the menu
     * closes ({@link #stopOpen}), so the contents survive crashes and drops.
     */
    public static class CrateInventory extends net.minecraft.world.SimpleContainer {
        private final ItemStack crateStack;

        public CrateInventory(ItemStack crateStack) {
            super(SIZE);
            this.crateStack = crateStack;
            ItemContainerContents contents =
                    crateStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
            try {
                contents.copyInto(this.getItems());
            } catch (Exception e) {
                AlyrionCore.LOGGER.debug("Crate contents larger than {} slots, truncating", SIZE, e);
            }
        }

        @Override
        public void setChanged() {
            super.setChanged();
            persist();
        }

        @Override
        public void stopOpen(Player player) {
            persist();
        }

        private void persist() {
            if (!crateStack.isEmpty()) {
                crateStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.getItems()));
            }
        }
    }
}
