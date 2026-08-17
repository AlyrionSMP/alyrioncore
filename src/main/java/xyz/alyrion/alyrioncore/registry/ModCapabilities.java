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
 * Registers the Oxygen Generator's capabilities, gated by input side. The block
 * entity implements {@link IEnergyStorage} and {@link IFluidHandler} directly,
 * but the capabilities are only exposed on the matching input face of the
 * model, so cables and pipes must connect to the right port:
 *
 *  * power input  = the east face of the authored model  (facing CCW)
 *  * water input  = the west face of the authored model (facing CW)
 *
 * A {@code null} side (block-level queries, e.g. tooltips) still resolves to
 * the machine itself.
 */
@EventBusSubscriber(modid = AlyrionCore.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModCapabilities {

    private static Direction powerSide(BlockState state) {
        return state.getValue(OxygenGeneratorBlock.FACING).getCounterClockWise();
    }

    private static Direction waterSide(BlockState state) {
        return state.getValue(OxygenGeneratorBlock.FACING).getClockWise();
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
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.OXYGEN_GENERATOR.get(),
                (be, side) -> {
                    if (side == null || side == waterSide(be.getBlockState())) {
                        return (IFluidHandler) be;
                    }
                    return null;
                }
        );
    }
}
