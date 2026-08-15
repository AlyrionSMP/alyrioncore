package xyz.alyrion.alyrioncore.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;
import xyz.alyrion.alyrioncore.registry.ModItems;

public class ModToolTiers {
    public static final Tier METEORIC_IRON = new SimpleTier(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            650,
            7.5F,
            2.5F,
            16,
            () -> Ingredient.of(ModItems.METEORIC_IRON_INGOT.get())
    );
}
