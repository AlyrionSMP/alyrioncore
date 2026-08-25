package xyz.alyrion.alyrioncore.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import xyz.alyrion.alyrioncore.item.CrateItem;
import xyz.alyrion.alyrioncore.item.CrateItem.CrateInventory;
import xyz.alyrion.alyrioncore.registry.ModMenus;

/**
 * A 3-row chest menu bound to a crate item stack, with one twist: the crate's
 * slots are take-out only. {@link TakeOnlySlot#mayPlace} returns false, which
 * every vanilla insert path consults (cursor clicks, number-key swaps,
 * drag-painting and shift-click via {@code moveItemStackTo}), so items can
 * leave the crate but never enter it.
 *
 * Registered as a real {@link MenuType} (see {@link ModMenus}) so the client
 * builds the identical menu from the crate stack sent in the open packet —
 * click prediction and server logic can never disagree.
 */
public class CrateMenu extends ChestMenu {

    public CrateMenu(int containerId, Inventory playerInventory, ItemStack crateStack) {
        super(ModMenus.CRATE.get(), containerId, playerInventory, new CrateInventory(crateStack), 3);
    }

    /** Client-side factory entry point: the crate stack arrives in the extra buffer. */
    public CrateMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory,
                ItemStack.OPTIONAL_STREAM_CODEC.decode(extraData));
    }

    /**
     * ChestMenu adds the container's slots first, then the player inventory —
     * so every slot added while fewer than {@link CrateItem#SIZE} slots exist
     * is a crate slot and gets wrapped take-out only. addSlot is dispatched
     * virtually from the super constructor, which is exactly what makes this
     * wrapper trick work.
     */
    @Override
    protected Slot addSlot(Slot slot) {
        if (this.slots.size() < CrateItem.SIZE) {
            return super.addSlot(new TakeOnlySlot(slot));
        }
        return super.addSlot(slot);
    }

    /** Delegating view of a crate slot that refuses any insertion. */
    private static final class TakeOnlySlot extends Slot {
        private TakeOnlySlot(Slot wrapped) {
            super(wrapped.container, wrapped.getContainerSlot(), wrapped.x, wrapped.y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
