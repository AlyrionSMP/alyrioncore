package xyz.alyrion.alyrioncore.store;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import xyz.alyrion.alyrioncore.AlyrionCore;

import java.util.List;

/**
 * A purchasable bundle of items in the Alyrion store ("Item Packs" tab).
 *
 * A pack is a named collection of (item id, count) entries that the server
 * hands out on purchase — a consumable, unlike cosmetics which are unlocks.
 * Both sides construct this registry: the client to render the catalog
 * (name, description, price, icon stack), the server to fulfill purchases.
 *
 * @param id            stable registry key; also used in commands
 * @param displayName   shown in the store UI
 * @param description   one-liner shown under the preview
 * @param price         cost in Alyrion coins
 * @param contents      resolved ItemStacks handed over on purchase
 * @param iconStack     representative stack drawn as the catalog icon
 */
public record ItemPackDefinition(
        String id,
        String displayName,
        String description,
        int price,
        List<ItemStack> contents,
        ItemStack iconStack) {

    public int totalItemCount() {
        int total = 0;
        for (ItemStack stack : contents) {
            total += stack.getCount();
        }
        return total;
    }

    /**
     * Resolve "minecraft:stick"-style ids against the vanilla+mod registries at
     * registration time. A missing item (e.g. Create not installed) logs a
     * warning and yields an empty stack that is skipped at purchase time.
     */
    public static ItemStack resolveStack(String itemId, int count) {
        ResourceLocation rl = ResourceLocation.parse(itemId);
        Item item = BuiltInRegistries.ITEM.getOptional(rl).orElse(null);
        if (item == null) {
            AlyrionCore.LOGGER.warn("ItemPack references unknown item '{}' — entry will be skipped", itemId);
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, count);
    }
}
