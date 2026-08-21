package xyz.alyrion.alyrioncore.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import xyz.alyrion.alyrioncore.AlyrionCore;

import java.util.List;
import java.util.Map;

/**
 * Armor materials for wearable gear. Currently only the Soviet Ushanka's fur
 * material — a very light cloth material so the hat is wearable but grants
 * barely any protection (more a cosmetic-real-item than armour).
 */
public class ModArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, AlyrionCore.MODID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> USHANKA =
            ARMOR_MATERIALS.register("ushanka", () -> new ArmorMaterial(
                    Map.of(ArmorItem.Type.HELMET, 1),
                    9,
                    SoundEvents.ARMOR_EQUIP_LEATHER,
                    () -> Ingredient.of(Items.LEATHER),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, "ushanka"))),
                    0.0F,
                    0.0F
            ));
}
