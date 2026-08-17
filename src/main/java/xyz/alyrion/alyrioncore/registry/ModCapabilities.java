package xyz.alyrion.alyrioncore.registry;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import xyz.alyrion.alyrioncore.AlyrionCore;

/**
 * Registers the Oxygen Generator's capabilities. The block entity implements
 * {@link IEnergyStorage} and {@link IFluidHandler} directly, and BOTH are
 * exposed on every face — standard FE and fluid connections work from any
 * side, so no per-mod compat is needed for other FE mods' cables or Create's
 * pipes. The power terminal and water flange on the model are visual markers
 * for the intended inputs, not hard requirements.
 */
@EventBusSubscriber(modid = AlyrionCore.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModCapabilities {

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.OXYGEN_GENERATOR.get(),
                (be, side) -> (IEnergyStorage) be
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.OXYGEN_GENERATOR.get(),
                (be, side) -> (IFluidHandler) be
        );
    }
}
