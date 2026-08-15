package xyz.alyrion.alyrioncore.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class SleepingPodBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<SleepingPodBlock> CODEC = simpleCodec(SleepingPodBlock::new);

    public static final EnumProperty<BedPart> PART = BlockStateProperties.BED_PART;
    public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;

    protected static final VoxelShape BASE_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0);

    @Override
    public MapCodec<SleepingPodBlock> codec() {
        return CODEC;
    }

    public SleepingPodBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, BedPart.FOOT)
                .setValue(OCCUPIED, false));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.CONSUME;
        }

        // If clicked on foot, redirect to head block
        BlockPos headPos = pos;
        BlockState headState = state;
        if (state.getValue(PART) != BedPart.HEAD) {
            headPos = pos.relative(state.getValue(FACING));
            headState = level.getBlockState(headPos);
            if (!headState.is(this)) {
                return InteractionResult.CONSUME;
            }
        }

        // Check if pod is occupied
        if (headState.getValue(OCCUPIED)) {
            player.displayClientMessage(Component.translatable("block.alyrioncore.sleeping_pod.occupied"), true);
            return InteractionResult.SUCCESS;
        }

        if (!player.isAlive()) {
            return InteractionResult.CONSUME;
        }

        // Set player respawn position on current dimension (Mars, Overworld, etc.)
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.setRespawnPosition(level.dimension(), headPos, player.getYRot(), false, true);
        }

        // Attempt sleeping
        player.startSleepInBed(headPos).ifLeft(problem -> {
            if (problem != null && problem.getMessage() != null) {
                player.displayClientMessage(problem.getMessage(), true);
            }
        });

        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        if (direction == getNeighbourDirection(state.getValue(PART), state.getValue(FACING))) {
            return neighborState.is(this) && neighborState.getValue(PART) != state.getValue(PART)
                    ? state.setValue(OCCUPIED, neighborState.getValue(OCCUPIED))
                    : Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    private static Direction getNeighbourDirection(BedPart part, Direction direction) {
        return part == BedPart.FOOT ? direction : direction.getOpposite();
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative()) {
            BedPart part = state.getValue(PART);
            if (part == BedPart.FOOT) {
                BlockPos headPos = pos.relative(state.getValue(FACING));
                BlockState headState = level.getBlockState(headPos);
                if (headState.is(this) && headState.getValue(PART) == BedPart.HEAD) {
                    level.setBlock(headPos, Blocks.AIR.defaultBlockState(), 35);
                    level.levelEvent(player, 2001, headPos, Block.getId(headState));
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getHorizontalDirection();
        BlockPos blockpos = context.getClickedPos();
        BlockPos headPos = blockpos.relative(direction);
        Level level = context.getLevel();
        return level.getBlockState(headPos).canBeReplaced(context) && level.getWorldBorder().isWithinBounds(headPos)
                ? this.defaultBlockState().setValue(FACING, direction)
                : null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            BlockPos headPos = pos.relative(state.getValue(FACING));
            level.setBlock(headPos, state.setValue(PART, BedPart.HEAD), 3);
            level.blockUpdated(pos, Blocks.AIR);
            state.updateNeighbourShapes(level, pos, 3);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BASE_SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART, OCCUPIED);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    // --- NeoForge IBlockExtension Bed Hooks ---

    @Override
    public boolean isBed(BlockState state, BlockGetter level, BlockPos pos, LivingEntity sleeper) {
        return true;
    }

    @Override
    public Direction getBedDirection(BlockState state, LevelReader level, BlockPos pos) {
        return state.getValue(FACING);
    }

    @Override
    public void setBedOccupied(BlockState state, Level level, BlockPos pos, LivingEntity sleeper, boolean occupied) {
        level.setBlock(pos, state.setValue(OCCUPIED, occupied), 3);
        Direction otherDir = getNeighbourDirection(state.getValue(PART), state.getValue(FACING));
        BlockPos otherPos = pos.relative(otherDir);
        BlockState otherState = level.getBlockState(otherPos);
        if (otherState.is(this)) {
            level.setBlock(otherPos, otherState.setValue(OCCUPIED, occupied), 3);
        }
    }

    @Override
    public Optional<ServerPlayer.RespawnPosAngle> getRespawnPosition(BlockState state, EntityType<?> type, LevelReader levelReader, BlockPos pos, float orientation) {
        Direction direction = state.getValue(FACING);
        return BedBlock.findStandUpPosition(type, levelReader, pos, direction, orientation)
                .map(vec3 -> ServerPlayer.RespawnPosAngle.of(vec3, pos));
    }
}
