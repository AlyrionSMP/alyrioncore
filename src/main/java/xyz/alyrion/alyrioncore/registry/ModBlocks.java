package xyz.alyrion.alyrioncore.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import xyz.alyrion.alyrioncore.AlyrionCore;
import xyz.alyrion.alyrioncore.block.AirlockBlock;
import xyz.alyrion.alyrioncore.block.DryIceBlock;
import xyz.alyrion.alyrioncore.block.MartianPotatoCropBlock;
import xyz.alyrion.alyrioncore.block.MartianSandBlock;
import xyz.alyrion.alyrioncore.block.OxygenGeneratorBlock;
import xyz.alyrion.alyrioncore.block.RegolithFarmlandBlock;
import xyz.alyrion.alyrioncore.block.SleepingPodBlock;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AlyrionCore.MODID);

    // --- Martian Soils & Dust ---
    public static final DeferredBlock<Block> MARTIAN_SAND = BLOCKS.registerBlock(
            "martian_sand",
            props -> new MartianSandBlock(0xC05832, props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .strength(0.5F)
                    .sound(SoundType.SAND)
    );

    public static final DeferredBlock<Block> MARTIAN_REGOLITH = BLOCKS.registerSimpleBlock(
            "martian_regolith",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_ORANGE)
                    .strength(0.8F)
                    .sound(SoundType.GRAVEL)
    );

    public static final DeferredBlock<Block> COARSE_MARTIAN_REGOLITH = BLOCKS.registerSimpleBlock(
            "coarse_martian_regolith",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_RED)
                    .strength(0.9F)
                    .sound(SoundType.GRAVEL)
    );

    public static final DeferredBlock<Block> FROST_DUSTED_REGOLITH = BLOCKS.registerSimpleBlock(
            "frost_dusted_regolith",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GRAY)
                    .strength(0.8F)
                    .sound(SoundType.SNOW)
    );

    public static final DeferredBlock<Block> MARTIAN_PERMAFROST = BLOCKS.registerSimpleBlock(
            "martian_permafrost",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.ICE)
                    .strength(1.8F, 3.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.GLASS)
    );

    // --- Martian Stones & Rocks ---
    public static final DeferredBlock<Block> MARTIAN_BASALT = BLOCKS.registerSimpleBlock(
            "martian_basalt",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(1.8F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.DEEPSLATE)
    );

    public static final DeferredBlock<Block> POLISHED_MARTIAN_BASALT = BLOCKS.registerSimpleBlock(
            "polished_martian_basalt",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(2.0F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)
    );

    public static final DeferredBlock<Block> MARTIAN_BASALT_BRICKS = BLOCKS.registerSimpleBlock(
            "martian_basalt_bricks",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(2.0F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)
    );

    public static final DeferredBlock<Block> MARTIAN_BASALT_TILES = BLOCKS.registerSimpleBlock(
            "martian_basalt_tiles",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(2.0F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)
    );

    public static final DeferredBlock<Block> STRATIFIED_MARTIAN_STONE = BLOCKS.registerSimpleBlock(
            "stratified_martian_stone",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_BROWN)
                    .strength(1.5F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)
    );

    public static final DeferredBlock<Block> MARTIAN_VOLCANIC_SCORIA = BLOCKS.registerSimpleBlock(
            "martian_volcanic_scoria",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(1.4F, 4.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.BASALT)
    );

    public static final DeferredBlock<Block> MARTIAN_IMPACT_BRECCIA = BLOCKS.registerSimpleBlock(
            "martian_impact_breccia",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_GRAY)
                    .strength(2.2F, 8.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.TUFF)
    );

    // --- Martian Ores & Minerals ---
    public static final DeferredBlock<Block> HEMATITE_ORE = BLOCKS.registerSimpleBlock(
            "hematite_ore",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_RED)
                    .strength(3.0F, 3.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)
    );

    public static final DeferredBlock<Block> METEORIC_IRON_ORE = BLOCKS.registerSimpleBlock(
            "meteoric_iron_ore",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(4.5F, 5.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.NETHER_GOLD_ORE)
    );

    public static final DeferredBlock<Block> MARTIAN_COPPER_ORE = BLOCKS.registerSimpleBlock(
            "martian_copper_ore",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_ORANGE)
                    .strength(3.0F, 3.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)
    );

    public static final DeferredBlock<Block> MARTIAN_SULFUR_ORE = BLOCKS.registerSimpleBlock(
            "martian_sulfur_ore",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(2.5F, 3.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)
    );

    public static final DeferredBlock<Block> MARTIAN_OLIVINE_ORE = BLOCKS.registerSimpleBlock(
            "martian_olivine_ore",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(3.5F, 4.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)
    );

    // --- Polar & Ice Blocks ---
    public static final DeferredBlock<Block> MARTIAN_ICE = BLOCKS.registerSimpleBlock(
            "martian_ice",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.ICE)
                    .friction(0.98F)
                    .strength(1.2F)
                    .sound(SoundType.GLASS)
    );

    public static final DeferredBlock<Block> DRY_ICE_BLOCK = BLOCKS.registerBlock(
            "dry_ice_block",
            DryIceBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SNOW)
                    .friction(0.985F)
                    .strength(1.0F)
                    .sound(SoundType.GLASS)
    );

    // --- Mineral & Resource Blocks ---
    public static final DeferredBlock<Block> METEORIC_IRON_BLOCK = BLOCKS.registerSimpleBlock(
            "meteoric_iron_block",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5.0F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.NETHERITE_BLOCK)
    );

    public static final DeferredBlock<Block> RAW_METEORIC_IRON_BLOCK = BLOCKS.registerSimpleBlock(
            "raw_meteoric_iron_block",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.RAW_IRON)
                    .strength(4.5F, 5.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.ANCIENT_DEBRIS)
    );

    public static final DeferredBlock<Block> OLIVINE_BLOCK = BLOCKS.registerSimpleBlock(
            "olivine_block",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(4.0F, 5.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.AMETHYST)
    );

    public static final DeferredBlock<Block> SULFUR_BLOCK = BLOCKS.registerSimpleBlock(
            "sulfur_block",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(2.0F, 3.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)
    );

    // --- Technology, Life Support & Greenhouse ---
    public static final DeferredBlock<SleepingPodBlock> SLEEPING_POD = BLOCKS.registerBlock(
            "sleeping_pod",
            SleepingPodBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.5F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.NETHERITE_BLOCK)
                    .noOcclusion()
    );

    public static final DeferredBlock<AirlockBlock> AIRLOCK = BLOCKS.registerBlock(
            "airlock",
            AirlockBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(4.0F, 8.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.NETHERITE_BLOCK)
                    .noOcclusion()
    );

    public static final DeferredBlock<OxygenGeneratorBlock> OXYGEN_GENERATOR = BLOCKS.registerBlock(
            "oxygen_generator",
            OxygenGeneratorBlock::new,
            OxygenGeneratorBlock.machineProperties()
    );

    public static final DeferredBlock<RegolithFarmlandBlock> REGOLITH_FARMLAND = BLOCKS.registerBlock(
            "regolith_farmland",
            RegolithFarmlandBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_ORANGE)
                    .strength(0.6F)
                    .sound(SoundType.GRAVEL)
                    .randomTicks()
    );

    public static final DeferredBlock<MartianPotatoCropBlock> MARTIAN_POTATO_CROP = BLOCKS.registerBlock(
            "martian_potato_crop",
            MartianPotatoCropBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.CROP)
    );
}
