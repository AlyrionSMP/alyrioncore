package xyz.alyrion.alyrioncore.registry;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import xyz.alyrion.alyrioncore.AlyrionCore;

/**
 * Registers the Oxygen Generator's capabilities. The block entity implements
 * {@link net.neoforged.neoforge.energy.IEnergyStorage} and
 * {@link net.neoforged.neoforge.fluids.capability.IFluidHandler} directly, so
 * any standard FE source can charge it (Power Grid's Device Connector / FE
 * Inverter, its cables and batteries, or FE producers from other mods) and any
 * standard fluid transport can feed it water (Create pumps/pipes/valves).
 */
@EventBusSubscriber(modid = AlyrionCore.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModCapabilities {

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.OXYGEN_GENERATOR.get(),
                (blockEntity, side) -> (net.neoforged.neoforge.energy.IEnergyStorage) blockEntity
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.OXYGEN_GENERATOR.get(),
                (blockEntity, side) -> (net.neoforged.neoforge.fluids.capability.IFluidHandler) blockEntity
        );
    }
}
