package xyz.alyrion.alyrioncore.registry;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import xyz.alyrion.alyrioncore.AlyrionCore;
import xyz.alyrion.alyrioncore.block.OxygenGeneratorBlock;

/**
 * Registers the Oxygen Generator's capabilities. The block entity implements
 * {@link IEnergyStorage} and {@link IFluidHandler} directly.
 *
 *  * Energy is gated to the power-input face (the east face of the authored
 *    model = facing CCW), so Power Grid cables/connectors attach at the power
 *    terminal.
 *  * Water is exposed on EVERY face — Create pipes attach no matter which side
 *    they touch (the water flange on the model marks the intended input).
 *
 * A {@code null} side (block-level queries, e.g. tooltips) also resolves to
 * the machine itself.
 */
@EventBusSubscriber(modid = AlyrionCore.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModCapabilities {

    private static Direction powerSide(BlockState state) {
        return state.getValue(OxygenGeneratorBlock.FACING).getCounterClockWise();
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.OXYGEN_GENERATOR.get(),
                (be, side) -> {
                    if (side == null || side == powerSide(be.getBlockState())) {
                        return (IEnergyStorage) be;
                    }
                    return null;
                }
        );
        // Water on every face: Create pipes must attach reliably regardless of
        // which side they are placed against.
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.OXYGEN_GENERATOR.get(),
                (be, side) -> (IFluidHandler) be
        );
    }
}
