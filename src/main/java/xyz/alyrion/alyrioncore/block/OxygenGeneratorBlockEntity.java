package xyz.alyrion.alyrioncore.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import xyz.alyrion.alyrioncore.registry.ModBlockEntities;

/**
 * The Oxygen Generator's block entity: holds the machine's FE buffer and drives
 * the ACTIVE blockstate. While the buffer is above zero the generator "runs":
 * it slowly consumes FE (the habitat's power bill) and the sealed room around it
 * stays breathable. When the buffer hits zero the machine goes dark and any
 * sealed habitat it was supplying depressurizes.
 *
 * Power is accepted from ANY standard Forge Energy source through the
 * {@code energyStorage} capability — Power Grid's Device Connector / FE Inverter,
 * cables, batteries, other mods' FE producers. There is no output side: the
 * energy is spent internally.
 */
public class OxygenGeneratorBlockEntity extends BlockEntity implements IEnergyStorage {

    /** Total FE the machine can hold. */
    public static final int CAPACITY = 16_000;
    /** Maximum FE accepted per tick from the grid. */
    public static final int MAX_RECEIVE = 1_024;
    /** FE drained per tick while running (~80 FE/s: a full buffer lasts ~3.3 min). */
    public static final int CONSUME_PER_TICK = 4;

    private int energy;

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
        if (energy > 0) {
            energy = Math.max(0, energy - CONSUME_PER_TICK);
            setChanged();
        }
        boolean active = energy > 0;
        if (active != state.getValue(OxygenGeneratorBlock.ACTIVE)) {
            level.setBlock(pos, state.setValue(OxygenGeneratorBlock.ACTIVE, active), 3);
        }
    }

    /** True while this machine has stored FE and is producing oxygen. */
    public boolean hasPower() {
        return energy > 0;
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
    // Persistence
    // ------------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("Energy", energy);
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
    }

    // ------------------------------------------------------------------
    // Client sync — the stored FE is included so the client-side seal check
    // and addons like Jade can see how charged the machine is.
    // ------------------------------------------------------------------

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        tag.putInt("Energy", energy);
        return tag;
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
}
