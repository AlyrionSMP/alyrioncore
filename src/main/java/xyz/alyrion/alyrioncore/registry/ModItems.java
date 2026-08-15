package xyz.alyrion.alyrioncore.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import xyz.alyrion.alyrioncore.AlyrionCore;

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

    public static final DeferredItem<Item> DRY_ICE_SHARD = ITEMS.registerSimpleItem(
            "dry_ice_shard",
            new Item.Properties()
    );

    public static final DeferredItem<Item> MARTIAN_ROCK_SAMPLE = ITEMS.registerSimpleItem(
            "martian_rock_sample",
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
}
