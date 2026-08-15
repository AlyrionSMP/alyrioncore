package xyz.alyrion.alyrioncore;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import xyz.alyrion.alyrioncore.registry.ModBlocks;
import xyz.alyrion.alyrioncore.registry.ModCreativeTabs;
import xyz.alyrion.alyrioncore.registry.ModItems;

@Mod(AlyrionCore.MODID)
public class AlyrionCore {
    public static final String MODID = "alyrioncore";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AlyrionCore(IEventBus modEventBus) {
        LOGGER.info("Initializing AlyrionCore for Alyrion SMP...");

        // Register Deferred Registers
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("AlyrionCore common setup initialized. Mars planetary dimension ready.");
    }
}
