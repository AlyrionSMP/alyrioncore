package xyz.alyrion.alyrioncore.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.HolderLookup;
import xyz.alyrion.alyrioncore.registry.ModBlockEntities;

/**
 * Stores what a reinforced block actually is: the original {@link BlockState}
 * (so the renderer can draw it and breaking can drop it) and how many mining
 * cycles remain before the reinforcement gives out.
 */
public class ReinforcedBlockEntity extends BlockEntity {

    private BlockState originalState = Blocks.AIR.defaultBlockState();
    private int hitsRemaining = 1;
    /** 0..7 cumulative damage stage shown by the crack overlay (0 = pristine). */
    private int crackStage = 0;

    public ReinforcedBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REINFORCED.get(), pos, state);
    }

    public BlockState getOriginalState() {
        return this.originalState;
    }

    public void setOriginalState(BlockState originalState) {
        this.originalState = originalState;
    }

    public int getHitsRemaining() {
        return this.hitsRemaining;
    }

    public void setHitsRemaining(int hitsRemaining) {
        this.hitsRemaining = hitsRemaining;
    }

    public int getCrackStage() {
        return this.crackStage;
    }

    public void setCrackStage(int crackStage) {
        this.crackStage = crackStage;
    }

    /** Maps consumed hits to a 0..7 crack stage (0 = pristine, 7 = nearly broken). */
    public static int crackStageFor(int totalHits, int hitsRemaining) {
        int consumed = Math.max(0, totalHits - hitsRemaining);
        return consumed * 7 / Math.max(1, totalHits - 1);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("original_state", NbtUtils.writeBlockState(this.originalState));
        tag.putInt("hits_remaining", this.hitsRemaining);
        tag.putInt("crack_stage", this.crackStage);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("original_state")) {
            this.originalState = NbtUtils.readBlockState(
                    registries.lookupOrThrow(Registries.BLOCK), tag.getCompound("original_state"));
        }
        this.hitsRemaining = tag.getInt("hits_remaining");
        this.crackStage = tag.getInt("crack_stage");
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // Carries original_state + hits_remaining (via saveAdditional) to the
        // client on placement and chunk load, so the renderer knows what to draw.
        return this.saveWithoutMetadata(registries);
    }
}
