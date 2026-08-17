package xyz.alyrion.alyrioncore.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import xyz.alyrion.alyrioncore.registry.ModBlockEntities;

/**
 * The Oxygen Generator's block entity: drives the ACTIVE blockstate and holds
 * the machine's two consumables — a Forge Energy buffer and a small internal
 * water tank. While BOTH are above zero the generator "runs": it consumes FE
 * and water (water → oxygen, the electrolysis principle) and the sealed room
 * around it stays breathable. When either runs dry the machine goes dark and
 * any sealed habitat it was supplying depressurizes.
 *
 * Energy is accepted from ANY standard Forge Energy source through the
 * {@code energyStorage} capability — Power Grid's Device Connector / FE
 * Inverter, cables, batteries, other mods' FE producers. Water is accepted
 * through the standard fluid capability, so Create pumps/pipes/valves can
 * feed it directly (hose pulley from a lake, a fluid tank filled with water
 * buckets from Martian Ice, ...). Both are receive-only: nothing is drained
 * out, everything is spent internally.
 */
public class OxygenGeneratorBlockEntity extends BlockEntity implements IEnergyStorage, IFluidHandler {

    /** Total FE the machine can hold. */
    public static final int CAPACITY = 16_000;
    /** Maximum FE accepted per tick from the grid. */
    public static final int MAX_RECEIVE = 1_024;
    /** FE drained per tick while running (~80 FE/s: a full buffer lasts ~3.3 min). */
    public static final int CONSUME_PER_TICK = 4;

    /** Internal water tank size (millibuckets). */
    public static final int WATER_CAPACITY = 8_000;
    /** Water drained per tick while running (40 mB/s: a full tank lasts ~3.3 min). */
    public static final int WATER_PER_TICK = 2;

    private int energy;
    private int water;

    public OxygenGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OXYGEN_GENERATOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, OxygenGeneratorBlockEntity be) {
        if (level.isClientSide) {
            return;
        }
        be.tickServer(level, pos, state);
    }

    private void tickServer(Level level, BlockPos pos, BlockState state) {
        boolean running = energy > 0 && water > 0;
        if (running) {
            energy = Math.max(0, energy - CONSUME_PER_TICK);
            water = Math.max(0, water - WATER_PER_TICK);
            setChanged();
        }
        boolean active = energy > 0 && water > 0;
        if (active != state.getValue(OxygenGeneratorBlock.ACTIVE)) {
            level.setBlock(pos, state.setValue(OxygenGeneratorBlock.ACTIVE, active), 3);
        }
    }

    /** True while this machine has both stored FE and water, and is producing oxygen. */
    public boolean isRunning() {
        return energy > 0 && water > 0;
    }

    // ------------------------------------------------------------------
    // IEnergyStorage — receive-only buffer (energy is spent internally)
    // ------------------------------------------------------------------

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive()) {
            return 0;
        }
        int accepted = Math.min(CAPACITY - energy, Math.min(MAX_RECEIVE, maxReceive));
        if (accepted < 0) {
            accepted = 0;
        }
        if (!simulate) {
            energy += accepted;
            setChanged();
        }
        return accepted;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0; // generators feed habitats, not grids
    }

    @Override
    public int getEnergyStored() {
        return energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return CAPACITY;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return energy < CAPACITY;
    }

    // ------------------------------------------------------------------
    // IFluidHandler — receive-only water tank (Create pipes pump it in)
    // ------------------------------------------------------------------

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return water > 0 ? new FluidStack(Fluids.WATER, water) : FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return WATER_CAPACITY;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return stack.getFluid() == Fluids.WATER;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || resource.getFluid() != Fluids.WATER) {
            return 0;
        }
        int accepted = Math.min(WATER_CAPACITY - water, resource.getAmount());
        if (accepted < 0) {
            accepted = 0;
        }
        if (action.execute() && accepted > 0) {
            water += accepted;
            setChanged();
        }
        return accepted;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.EMPTY; // receive-only: the water is spent on oxygen
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY;
    }

    // ------------------------------------------------------------------
    // Persistence
    // ------------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("Energy", energy);
        tag.putInt("Water", water);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        energy = tag.getInt("Energy");
        if (energy < 0) {
            energy = 0;
        }
        if (energy > CAPACITY) {
            energy = CAPACITY;
        }
        water = tag.getInt("Water");
        if (water < 0) {
            water = 0;
        }
        if (water > WATER_CAPACITY) {
            water = WATER_CAPACITY;
        }
    }

    // ------------------------------------------------------------------
    // Client sync — the stored FE and water are included so the client-side
    // seal check and addons like Jade can see the machine's state.
    // ------------------------------------------------------------------

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        tag.putInt("Energy", energy);
        tag.putInt("Water", water);
        return tag;
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
}
