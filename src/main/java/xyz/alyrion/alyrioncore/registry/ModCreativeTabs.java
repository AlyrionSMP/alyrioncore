package xyz.alyrion.alyrioncore.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import xyz.alyrion.alyrioncore.AlyrionCore;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AlyrionCore.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MARS_TAB =
            CREATIVE_MODE_TABS.register("mars_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.alyrioncore.mars"))
                    .icon(() -> ModItems.MARTIAN_ROCK_SAMPLE.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        // Minerals & Raw Materials
                        output.accept(ModItems.MARTIAN_ROCK_SAMPLE.get());
                        output.accept(ModItems.HEMATITE_NODULE.get());
                        output.accept(ModItems.RAW_METEORIC_IRON.get());
                        output.accept(ModItems.METEORIC_IRON_INGOT.get());
                        output.accept(ModItems.RAW_MARTIAN_COPPER.get());
                        output.accept(ModItems.SULFUR_DUST.get());
                        output.accept(ModItems.OLIVINE_GEM.get());
                        output.accept(ModItems.DRY_ICE_SHARD.get());

                        // Meteoric Equipment
                        output.accept(ModItems.METEORIC_IRON_SWORD.get());
                        output.accept(ModItems.METEORIC_IRON_PICKAXE.get());
                        output.accept(ModItems.METEORIC_IRON_AXE.get());
                        output.accept(ModItems.METEORIC_IRON_SHOVEL.get());
                        output.accept(ModItems.METEORIC_IRON_HOE.get());

                        // Block Reinforcement
                        output.accept(ModItems.IRON_REINFORCEMENT_PLATE.get());
                        output.accept(ModItems.DIAMOND_REINFORCEMENT_PLATE.get());
                        output.accept(ModItems.METEORIC_IRON_REINFORCEMENT_PLATE.get());
                        output.accept(ModItems.NETHERITE_REINFORCEMENT_PLATE.get());

                        // Resource & Storage Blocks
                        output.accept(ModItems.METEORIC_IRON_BLOCK_ITEM.get());
                        output.accept(ModItems.RAW_METEORIC_IRON_BLOCK_ITEM.get());
                        output.accept(ModItems.OLIVINE_BLOCK_ITEM.get());
                        output.accept(ModItems.SULFUR_BLOCK_ITEM.get());

                        // Soils & Regolith
                        output.accept(ModItems.MARTIAN_SAND_ITEM.get());
                        output.accept(ModItems.MARTIAN_REGOLITH_ITEM.get());
                        output.accept(ModItems.COARSE_MARTIAN_REGOLITH_ITEM.get());
                        output.accept(ModItems.FROST_DUSTED_REGOLITH_ITEM.get());
                        output.accept(ModItems.MARTIAN_PERMAFROST_ITEM.get());

                        // Rocks & Volcanics
                        output.accept(ModItems.MARTIAN_BASALT_ITEM.get());
                        output.accept(ModItems.POLISHED_MARTIAN_BASALT_ITEM.get());
                        output.accept(ModItems.MARTIAN_BASALT_BRICKS_ITEM.get());
                        output.accept(ModItems.MARTIAN_BASALT_TILES_ITEM.get());
                        output.accept(ModItems.STRATIFIED_MARTIAN_STONE_ITEM.get());
                        output.accept(ModItems.MARTIAN_VOLCANIC_SCORIA_ITEM.get());
                        output.accept(ModItems.MARTIAN_IMPACT_BRECCIA_ITEM.get());

                        // Ores
                        output.accept(ModItems.HEMATITE_ORE_ITEM.get());
                        output.accept(ModItems.METEORIC_IRON_ORE_ITEM.get());
                        output.accept(ModItems.MARTIAN_COPPER_ORE_ITEM.get());
                        output.accept(ModItems.MARTIAN_SULFUR_ORE_ITEM.get());
                        output.accept(ModItems.MARTIAN_OLIVINE_ORE_ITEM.get());

                        // Polar Ices
                        output.accept(ModItems.MARTIAN_ICE_ITEM.get());
                        output.accept(ModItems.DRY_ICE_BLOCK_ITEM.get());

                        // Technology, Habitat & Greenhouse
                        output.accept(ModItems.SLEEPING_POD_ITEM.get());
                        output.accept(ModItems.AIRLOCK_ITEM.get());
                        output.accept(ModItems.OXYGEN_GENERATOR_ITEM.get());
                        output.accept(ModItems.REGOLITH_FARMLAND_ITEM.get());
                        output.accept(ModItems.MARTIAN_POTATO.get());
                        output.accept(ModItems.BAKED_MARTIAN_POTATO.get());
                    })
                    .build()
            );
}
