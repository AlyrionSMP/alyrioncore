package xyz.alyrion.alyrioncore.registry;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import xyz.alyrion.alyrioncore.AlyrionCore;
import xyz.alyrion.alyrioncore.block.ReinforcementTier;
import xyz.alyrion.alyrioncore.item.DryIceShardItem;
import xyz.alyrion.alyrioncore.item.MartianRockSampleItem;
import xyz.alyrion.alyrioncore.item.ModToolTiers;
import xyz.alyrion.alyrioncore.item.ReinforcementPlateItem;

import java.util.List;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AlyrionCore.MODID);

    // --- Martian Geology Materials & Ores ---
    public static final DeferredItem<Item> HEMATITE_NODULE = ITEMS.registerSimpleItem(
            "hematite_nodule",
            new Item.Properties()
    );

    public static final DeferredItem<Item> RAW_METEORIC_IRON = ITEMS.registerSimpleItem(
            "raw_meteoric_iron",
            new Item.Properties()
    );

    public static final DeferredItem<Item> METEORIC_IRON_INGOT = ITEMS.registerSimpleItem(
            "meteoric_iron_ingot",
            new Item.Properties()
    );

    public static final DeferredItem<Item> RAW_MARTIAN_COPPER = ITEMS.registerSimpleItem(
            "raw_martian_copper",
            new Item.Properties()
    );

    public static final DeferredItem<Item> SULFUR_DUST = ITEMS.registerSimpleItem(
            "sulfur_dust",
            new Item.Properties()
    );

    public static final DeferredItem<Item> OLIVINE_GEM = ITEMS.registerSimpleItem(
            "olivine_gem",
            new Item.Properties()
    );

    public static final DeferredItem<Item> DRY_ICE_SHARD = ITEMS.registerItem(
            "dry_ice_shard",
            DryIceShardItem::new,
            new Item.Properties()
    );

    public static final DeferredItem<Item> MARTIAN_ROCK_SAMPLE = ITEMS.registerItem(
            "martian_rock_sample",
            MartianRockSampleItem::new,
            new Item.Properties()
    );

    // --- Meteoric Iron Tools & Weapons ---
    public static final DeferredItem<SwordItem> METEORIC_IRON_SWORD = ITEMS.registerItem(
            "meteoric_iron_sword",
            props -> new SwordItem(ModToolTiers.METEORIC_IRON, props),
            new Item.Properties().attributes(SwordItem.createAttributes(ModToolTiers.METEORIC_IRON, 3, -2.4F))
    );

    public static final DeferredItem<PickaxeItem> METEORIC_IRON_PICKAXE = ITEMS.registerItem(
            "meteoric_iron_pickaxe",
            props -> new PickaxeItem(ModToolTiers.METEORIC_IRON, props),
            new Item.Properties().attributes(PickaxeItem.createAttributes(ModToolTiers.METEORIC_IRON, 1.0F, -2.8F))
    );

    public static final DeferredItem<AxeItem> METEORIC_IRON_AXE = ITEMS.registerItem(
            "meteoric_iron_axe",
            props -> new AxeItem(ModToolTiers.METEORIC_IRON, props),
            new Item.Properties().attributes(AxeItem.createAttributes(ModToolTiers.METEORIC_IRON, 6.0F, -3.1F))
    );

    public static final DeferredItem<ShovelItem> METEORIC_IRON_SHOVEL = ITEMS.registerItem(
            "meteoric_iron_shovel",
            props -> new ShovelItem(ModToolTiers.METEORIC_IRON, props),
            new Item.Properties().attributes(ShovelItem.createAttributes(ModToolTiers.METEORIC_IRON, 1.5F, -3.0F))
    );

    public static final DeferredItem<HoeItem> METEORIC_IRON_HOE = ITEMS.registerItem(
            "meteoric_iron_hoe",
            props -> new HoeItem(ModToolTiers.METEORIC_IRON, props),
            new Item.Properties().attributes(HoeItem.createAttributes(ModToolTiers.METEORIC_IRON, -2.0F, -1.0F))
    );

    // --- Block Reinforcement Plates ---
    public static final DeferredItem<ReinforcementPlateItem> IRON_REINFORCEMENT_PLATE = ITEMS.registerItem(
            "iron_reinforcement_plate",
            props -> new ReinforcementPlateItem(ReinforcementTier.IRON, props),
            new Item.Properties()
    );

    public static final DeferredItem<ReinforcementPlateItem> DIAMOND_REINFORCEMENT_PLATE = ITEMS.registerItem(
            "diamond_reinforcement_plate",
            props -> new ReinforcementPlateItem(ReinforcementTier.DIAMOND, props),
            new Item.Properties()
    );

    public static final DeferredItem<ReinforcementPlateItem> METEORIC_IRON_REINFORCEMENT_PLATE = ITEMS.registerItem(
            "meteoric_iron_reinforcement_plate",
            props -> new ReinforcementPlateItem(ReinforcementTier.METEORIC_IRON, props),
            new Item.Properties()
    );

    public static final DeferredItem<ReinforcementPlateItem> NETHERITE_REINFORCEMENT_PLATE = ITEMS.registerItem(
            "netherite_reinforcement_plate",
            props -> new ReinforcementPlateItem(ReinforcementTier.NETHERITE, props),
            new Item.Properties()
    );

    // --- Block Items ---
    public static final DeferredItem<BlockItem> MARTIAN_SAND_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.MARTIAN_SAND);
    public static final DeferredItem<BlockItem> MARTIAN_REGOLITH_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.MARTIAN_REGOLITH);
    public static final DeferredItem<BlockItem> COARSE_MARTIAN_REGOLITH_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.COARSE_MARTIAN_REGOLITH);
    public static final DeferredItem<BlockItem> FROST_DUSTED_REGOLITH_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.FROST_DUSTED_REGOLITH);
    public static final DeferredItem<BlockItem> MARTIAN_PERMAFROST_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.MARTIAN_PERMAFROST);

    public static final DeferredItem<BlockItem> MARTIAN_BASALT_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.MARTIAN_BASALT);
    public static final DeferredItem<BlockItem> POLISHED_MARTIAN_BASALT_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.POLISHED_MARTIAN_BASALT);
    public static final DeferredItem<BlockItem> MARTIAN_BASALT_BRICKS_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.MARTIAN_BASALT_BRICKS);
    public static final DeferredItem<BlockItem> MARTIAN_BASALT_TILES_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.MARTIAN_BASALT_TILES);
    public static final DeferredItem<BlockItem> STRATIFIED_MARTIAN_STONE_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.STRATIFIED_MARTIAN_STONE);
    public static final DeferredItem<BlockItem> MARTIAN_VOLCANIC_SCORIA_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.MARTIAN_VOLCANIC_SCORIA);
    public static final DeferredItem<BlockItem> MARTIAN_IMPACT_BRECCIA_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.MARTIAN_IMPACT_BRECCIA);

    public static final DeferredItem<BlockItem> HEMATITE_ORE_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.HEMATITE_ORE);
    public static final DeferredItem<BlockItem> METEORIC_IRON_ORE_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.METEORIC_IRON_ORE);
    public static final DeferredItem<BlockItem> MARTIAN_COPPER_ORE_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.MARTIAN_COPPER_ORE);
    public static final DeferredItem<BlockItem> MARTIAN_SULFUR_ORE_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.MARTIAN_SULFUR_ORE);
    public static final DeferredItem<BlockItem> MARTIAN_OLIVINE_ORE_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.MARTIAN_OLIVINE_ORE);

    public static final DeferredItem<BlockItem> MARTIAN_ICE_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.MARTIAN_ICE);
    public static final DeferredItem<BlockItem> DRY_ICE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.DRY_ICE_BLOCK);

    public static final DeferredItem<BlockItem> METEORIC_IRON_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.METEORIC_IRON_BLOCK);
    public static final DeferredItem<BlockItem> RAW_METEORIC_IRON_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.RAW_METEORIC_IRON_BLOCK);
    public static final DeferredItem<BlockItem> OLIVINE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.OLIVINE_BLOCK);
    public static final DeferredItem<BlockItem> SULFUR_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.SULFUR_BLOCK);

    public static final DeferredItem<BlockItem> SLEEPING_POD_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.SLEEPING_POD);
    public static final DeferredItem<DoubleHighBlockItem> AIRLOCK_ITEM = ITEMS.registerItem(
            "airlock",
            props -> new DoubleHighBlockItem(ModBlocks.AIRLOCK.get(), props)
    );

    /**
     * The Oxygen Generator's item. Kept as a plain BlockItem so the custom 3D model
     * shows in the inventory; the hover text explains the machine's job (oxygen for
     * sealed habitats) and its FE appetite.
     */
    public static final DeferredItem<BlockItem> OXYGEN_GENERATOR_ITEM = ITEMS.registerItem(
            "oxygen_generator",
            props -> new BlockItem(ModBlocks.OXYGEN_GENERATOR.get(), props) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                            List<Component> tooltipComponents, TooltipFlag flag) {
                    tooltipComponents.add(Component.literal("§7Pressurizes sealed habitats"));
                    tooltipComponents.add(Component.literal("§7and keeps them breathable."));
                    tooltipComponents.add(Component.literal("§bNeeds FE and water"));
                    tooltipComponents.add(Component.literal("§b— pipe in Create water!"));
                }
            },
            new Item.Properties()
    );
    public static final DeferredItem<BlockItem> REGOLITH_FARMLAND_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.REGOLITH_FARMLAND);

    // --- Martian Food & Agriculture ---
    public static final net.minecraft.world.food.FoodProperties MARTIAN_POTATO_FOOD = new net.minecraft.world.food.FoodProperties.Builder()
            .nutrition(3)
            .saturationModifier(0.6F)
            .build();

    public static final net.minecraft.world.food.FoodProperties BAKED_MARTIAN_POTATO_FOOD = new net.minecraft.world.food.FoodProperties.Builder()
            .nutrition(6)
            .saturationModifier(0.8F)
            .build();

    public static final DeferredItem<ItemNameBlockItem> MARTIAN_POTATO = ITEMS.registerItem(
            "martian_potato",
            props -> new ItemNameBlockItem(ModBlocks.MARTIAN_POTATO_CROP.get(), props.food(MARTIAN_POTATO_FOOD))
    );

    public static final DeferredItem<Item> BAKED_MARTIAN_POTATO = ITEMS.registerItem(
            "baked_martian_potato",
            props -> new Item(props.food(BAKED_MARTIAN_POTATO_FOOD))
    );
}
