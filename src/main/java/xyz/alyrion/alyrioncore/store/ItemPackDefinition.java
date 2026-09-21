package xyz.alyrion.alyrioncore.store;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;
import xyz.alyrion.alyrioncore.AlyrionCore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A purchasable bundle of items in the Alyrion store ("Item Packs" tab).
 *
 * A pack is a named collection of entries (item id, count, optional
 * enchantments) that the server hands out on purchase — a consumable, unlike
 * cosmetics which are unlocks. Both sides construct this registry: the client
 * to render the catalog (name, description, price, icon stack), the server to
 * fulfill purchases.
 *
 * Entries stay unresolved because enchantments live in a datapack registry:
 * the stacks are built by {@link #buildContents(RegistryAccess)} with whatever
 * registry access the caller has.
 *
 * @param id            stable registry key; also used in commands
 * @param displayName   shown in the store UI
 * @param description   one-liner shown under the preview
 * @param price         cost in Alyrion coins
 * @param contents      entries resolved into ItemStacks on demand
 * @param delivery      how the resolved stacks reach the player
 * @param iconStack     representative stack drawn as the catalog icon
 * @param claimChunks   number of bonus claim chunks granted if delivery is CLAIM_CHUNKS
 */
public record ItemPackDefinition(
        String id,
        String displayName,
        String description,
        int price,
        List<PackEntry> contents,
        Delivery delivery,
        ItemStack iconStack,
        int claimChunks) {

    public ItemPackDefinition(
            String id,
            String displayName,
            String description,
            int price,
            List<PackEntry> contents,
            Delivery delivery,
            ItemStack iconStack) {
        this(id, displayName, description, price, contents, delivery, iconStack, 0);
    }

    /** Helper factory for an OPAC bonus claim chunk pack. */
    public static ItemPackDefinition claimChunks(
            String id,
            String displayName,
            String description,
            int price,
            int chunks,
            ItemStack iconStack) {
        return new ItemPackDefinition(id, displayName, description, price, List.of(), Delivery.CLAIM_CHUNKS, iconStack, chunks);
    }

    /**
     * How a purchased pack reaches the player.
     */
    public enum Delivery {
        /** Handed over as a filled crate — the contents live in its container component. */
        CRATE,
        /** Given straight to the player's inventory; anything that does not fit drops at their feet. */
        DIRECT,
        /** Grants bonus claim chunks via Open Parties and Claims. */
        CLAIM_CHUNKS
    }

    /** One line of a pack: an item id, how many of it, and enchantments to apply. */
    public record PackEntry(String itemId, int count, Map<String, Integer> enchantments) {

        public static PackEntry of(String itemId, int count) {
            return new PackEntry(itemId, count, Map.of());
        }

        public static PackEntry of(String itemId, int count, Map<String, Integer> enchantments) {
            return new PackEntry(itemId, count, enchantments);
        }

        public static PackEntry enchanted(String itemId, int count, Map<String, Integer> enchantments) {
            return new PackEntry(itemId, count, enchantments);
        }
    }

    /** Sum of all counts across every entry (e.g. 192 for the tracks entry alone). */
    public int totalItemCount() {
        int total = 0;
        for (PackEntry entry : contents) {
            total += entry.count();
        }
        return total;
    }

    /**
     * Resolves every entry into real {@link ItemStack}s. Safe on both sides;
     * unknown item ids drop with a warning so a missing optional dependency
     * degrades rather than crashes. Enchantments are applied only when a non-null
     * registry access is provided (the server has this at purchase time).
     */
    public List<ItemStack> buildContents(@Nullable RegistryAccess registries) {
        List<ItemStack> stacks = new ArrayList<>(contents.size());
        for (PackEntry entry : contents) {
            ItemStack stack = resolveStack(entry.itemId(), entry.count());
            if (stack.isEmpty()) continue;
            for (Map.Entry<String, Integer> enchantment : entry.enchantments().entrySet()) {
                applyEnchantment(stack, enchantment.getKey(), enchantment.getValue(), registries);
            }
            stacks.add(stack);
        }
        return stacks;
    }

    private static void applyEnchantment(ItemStack stack, String enchantmentId, int level,
                                         @Nullable RegistryAccess registries) {
        if (registries == null) return;
        ResourceLocation id = ResourceLocation.tryParse(enchantmentId);
        if (id == null) return;
        Registry<Enchantment> registry = registries.registry(Registries.ENCHANTMENT).orElse(null);
        if (registry == null) return;
        Holder.Reference<Enchantment> holder =
                registry.getHolder(ResourceKey.create(Registries.ENCHANTMENT, id)).orElse(null);
        if (holder == null) {
            AlyrionCore.LOGGER.debug("Enchantment {} not found in registry, skipping", id);
            return;
        }
        stack.enchant(holder, level);
    }

    /**
     * Builds one stack from an id and count. Returns {@link ItemStack#EMPTY}
     * with a debug log when the id isn't registered, so missing content is
     * harmless.
     */
    public static ItemStack resolveStack(String itemId, int count) {
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null) return ItemStack.EMPTY;
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == net.minecraft.world.item.Items.AIR) {
            AlyrionCore.LOGGER.debug("Item {} not found in registry (mod missing?)", id);
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, Math.max(1, count));
    }
}
