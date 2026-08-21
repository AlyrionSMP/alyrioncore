package xyz.alyrion.alyrioncore.item;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import xyz.alyrion.alyrioncore.AlyrionCore;

import java.util.List;

/**
 * The Soviet Ushanka — a battered winter fur cap salvaged from crashed Soviet
 * satellites. A real in-game item (wearable in the head slot, light protection),
 * not a cosmetic. Found in the {@code crashed_soviet_probe} chest loot.
 *
 * <p>Rendered by the custom {@code UshankaModel} (registered through
 * {@code IClientItemExtensions}) instead of the vanilla armor helmet box.
 */
public class UshankaItem extends ArmorItem {

    /** Dedicated worn-model sheet; see generate_ushanka.py for the UV layout. */
    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, "textures/models/armor/ushanka.png");

    public UshankaItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
            ArmorMaterial.Layer layer, boolean innerModel) {
        return TEXTURE;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag flag) {
        tooltipComponents.add(Component.literal("§7A battered Soviet winter cap."));
        tooltipComponents.add(Component.literal("§7Salvaged from a crashed satellite."));
    }
}
