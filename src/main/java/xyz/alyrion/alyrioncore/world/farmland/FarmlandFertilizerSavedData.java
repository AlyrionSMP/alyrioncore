package xyz.alyrion.alyrioncore.world.farmland;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class FarmlandFertilizerSavedData extends SavedData {
    public static final String DATA_NAME = "alyrion_fertilized_farmland";
    public static final SavedData.Factory<FarmlandFertilizerSavedData> FACTORY =
            new SavedData.Factory<>(FarmlandFertilizerSavedData::new, FarmlandFertilizerSavedData::load);

    private final LongSet fertilizedPositions = new LongOpenHashSet();

    public FarmlandFertilizerSavedData() {
    }

    public static FarmlandFertilizerSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public static FarmlandFertilizerSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        FarmlandFertilizerSavedData data = new FarmlandFertilizerSavedData();
        if (tag.contains("FertilizedFarmland")) {
            long[] array = tag.getLongArray("FertilizedFarmland");
            for (long pos : array) {
                data.fertilizedPositions.add(pos);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLongArray("FertilizedFarmland", this.fertilizedPositions.toLongArray());
        return tag;
    }

    public boolean isFertilized(BlockPos pos) {
        return this.fertilizedPositions.contains(pos.asLong());
    }

    public void setFertilized(BlockPos pos, boolean fertilized) {
        long packed = pos.asLong();
        boolean changed = fertilized ? this.fertilizedPositions.add(packed) : this.fertilizedPositions.remove(packed);
        if (changed) {
            this.setDirty();
        }
    }

    public void remove(BlockPos pos) {
        if (this.fertilizedPositions.remove(pos.asLong())) {
            this.setDirty();
        }
    }

    public int getFertilizedCount() {
        return this.fertilizedPositions.size();
    }
}
