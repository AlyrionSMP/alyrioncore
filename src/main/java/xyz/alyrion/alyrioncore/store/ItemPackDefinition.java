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
 * @param iconStack     representative stack drawn as the catalog icon
 */
public record ItemPackDefinition(
        String id,
        String displayName,
        String description,
        int price,
        List<PackEntry> contents,
        ItemStack iconStack) {

    /** One line of a pack: an item id, how many of it, and enchantments to apply. */
    public record PackEntry(String itemId, int count, Map<String, Integer> enchantments) {

        public static PackEntry of(String itemId, int count) {
            return new PackEntry(itemId, count, Map.of());
        }

        /** Enchantment ids ("minecraft:sharpness") to levels. */
        public static PackEntry enchanted(String itemId, int count, Map<String, Integer> enchantments) {
            return new PackEntry(itemId, count, Map.copyOf(enchantments));
        }
    }

    public int totalItemCount() {
        int total = 0;
        for (PackEntry entry : contents) {
            total += entry.count();
        }
        return total;
    }

    /**
     * Resolve the pack into the stacks that are handed over — and that the store
     * preview draws, so the preview shows exactly what is delivered.
     *
     * Enchantments are datapack entries, so applying them needs a
     * {@link RegistryAccess}; without one (a client before it joins a world) the
     * affected stacks come out unenchanted instead of throwing. Unknown items and
     * enchantments log a warning and are skipped rather than breaking the pack.
     */
    public List<ItemStack> buildContents(@Nullable RegistryAccess registries) {
        List<ItemStack> stacks = new ArrayList<>(contents.size());
        for (PackEntry entry : contents) {
            ItemStack stack = resolveStack(entry.itemId(), entry.count());
            if (stack.isEmpty()) {
                continue;
            }
            for (Map.Entry<String, Integer> enchantment : entry.enchantments().entrySet()) {
                applyEnchantment(stack, enchantment.getKey(), enchantment.getValue(), registries);
            }
            stacks.add(stack);
        }
        return stacks;
    }

    private static void applyEnchantment(ItemStack stack, String enchantmentId, int level,
                                         @Nullable RegistryAccess registries) {
        if (registries == null) {
            return;
        }
        ResourceLocation id = ResourceLocation.tryParse(enchantmentId);
        if (id == null) {
            return;
        }
        Registry<Enchantment> enchantments = registries.registryOrThrow(Registries.ENCHANTMENT);
        ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, id);
        Holder<Enchantment> holder = enchantments.getHolder(key).orElse(null);
        if (holder == null) {
            AlyrionCore.LOGGER.warn("ItemPack references unknown enchantment '{}' — entry stays unenchanted",
                    enchantmentId);
            return;
        }
        stack.enchant(holder, level);
    }

    /**
     * Resolve "minecraft:stick"-style ids against the vanilla+mod registries. A
     * missing item (e.g. Create not installed) logs a warning and yields an empty
     * stack that is skipped when the pack is built.
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
