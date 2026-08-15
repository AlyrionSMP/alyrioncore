package xyz.alyrion.alyrioncore.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import xyz.alyrion.alyrioncore.registry.ModBlockEntities;

/**
 * A two-block-tall pressurized habitat airlock. Extends {@link DoorBlock} so placement,
 * hinge/facing logic, redstone and the habitat seal system all keep working unchanged;
 * the visual door leaf is animated by {@link AirlockBlockEntity} / its renderer.
 */
public class AirlockBlock extends DoorBlock implements EntityBlock {

    public static final MapCodec<AirlockBlock> CODEC = simpleCodec(AirlockBlock::new);

    public AirlockBlock(BlockBehaviour.Properties properties) {
        super(BlockSetType.IRON, properties);
    }

    @Override
    public MapCodec<AirlockBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AirlockBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlockEntities.AIRLOCK.get()) {
            return (lvl, pos, st, be) -> AirlockBlockEntity.tick(lvl, pos, st, (AirlockBlockEntity) be);
        }
        return null;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Closed: the armored leaf + frame seal the whole doorway. Open: only the bulkhead
        // frame remains solid, leaving a walkable doorway opening (the leaf has folded against
        // the jamb inside the block cell).
        if (!state.getValue(OPEN)) {
            return Shapes.block();
        }
        boolean alongX = state.getValue(FACING).getAxis() == Direction.Axis.X;
        return Shapes.or(
                alongX ? JAMB_X_NORTH : JAMB_Z_WEST,
                alongX ? JAMB_X_SOUTH : JAMB_Z_EAST
        );
    }

    // Frame jambs (2 px thick), authored for facing=east (y=0). For east/west facings the
    // doorway runs along X and the jambs are the north/south walls; for north/south facings
    // the doorway runs along Z and the jambs are the west/east walls.
    private static final VoxelShape JAMB_X_NORTH = Block.box(0, 0, 0, 16, 16, 2);
    private static final VoxelShape JAMB_X_SOUTH = Block.box(0, 0, 14, 16, 16, 16);
    private static final VoxelShape JAMB_Z_WEST = Block.box(0, 0, 0, 2, 16, 16);
    private static final VoxelShape JAMB_Z_EAST = Block.box(14, 0, 0, 16, 16, 16);

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        // Toggle open/closed state on right click with pneumatic airlock sound
        state = state.cycle(OPEN);
        level.setBlock(pos, state, 10);
        float pitch = state.getValue(OPEN) ? 0.85F : 1.15F;
        level.playSound(player, pos, state.getValue(OPEN) ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, pitch);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public static boolean isAirtight(BlockState state) {
        return !state.getValue(OPEN);
    }
}
