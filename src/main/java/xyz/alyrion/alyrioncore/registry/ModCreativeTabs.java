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
                        // Minerals & Items
                        output.accept(ModItems.MARTIAN_ROCK_SAMPLE.get());
                        output.accept(ModItems.HEMATITE_NODULE.get());
                        output.accept(ModItems.RAW_METEORIC_IRON.get());
                        output.accept(ModItems.METEORIC_IRON_INGOT.get());
                        output.accept(ModItems.RAW_MARTIAN_COPPER.get());
                        output.accept(ModItems.SULFUR_DUST.get());
                        output.accept(ModItems.OLIVINE_GEM.get());
                        output.accept(ModItems.DRY_ICE_SHARD.get());

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
                    })
                    .build()
            );
}
